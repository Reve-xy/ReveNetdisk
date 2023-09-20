<!--
* @author Rêve
* @date 2023/5/22 21:02
* @description 
-->
<script setup lang="ts">
import {Pointer, Search} from "@element-plus/icons-vue";
import {createFolder, delFile, download, loadFileList, moveFile, renameFile} from "@/api/file";
import {covertSize} from "@/utils/common";
import SystemConf from "@/constant/tableConstants";
let {extHeight,showPagination,topHeight}=SystemConf
    const $modal = inject('$modal')
const fileTable=ref(null)
let tableData = ref<Array<object>>()
let showPageSize = ref<Boolean>(true)
let total = ref<number>()
let loading = ref(false)
let skLoading=ref(false)
let timer=null

//查询表单
const queryForms = ref<object>({
    fileName: '',
    filePid: 0,
    category: 'all',
    order: '',
    desc: '',
    dir: '',
    pageSize: 10,
    pageNum: 1
})

//表格配置
const config = reactive<object>({
    stripe: false,
    border: false,
    showSelection: true,    //展示单选框
    showIndex: false,   //展示序号
    tableHeight:0,
})
//表格列配置
const columns = reactive<Array<object>>([
    {
        prop: 'fileName',
        label: '文件名',
        scopedSlots: 'fileName',
    }, {
        prop: 'updateTime',
        label: '修改时间',
        width: 200,
        sortable: true
    }, {
        prop: 'size',
        label: '大小',
        sortable: true,
        width: 200,
        scopedSlots: 'fileSize'
    }
])

/*----------------------table方法--------------------------*/
let initTableData = false   //是否初始化表格数据
//表格方法定义
const getTableData = async () => {
    skLoading.value = true
    queryForms.value.filePid = currentFolder.value.fileId
    const res: any = await loadFileList(queryForms.value)
    tableData.value = res.rows
    total.value = res.total
    skLoading.value = false
}

//多选 批量选择
const selectFileIdList = ref([]);
const rowSelected = (rows) => {
    selectFileIdList.value = [];
        rows.forEach((item) => {
            //不使用map，数据小map效率低于push
            selectFileIdList.value.push(item.fileId);
            item.selected=true
        });
};

//行点击(单选select）
const rowClick = (row) => {
    clearTimeout(timer)
    timer=setTimeout(()=>{
        if(selectFileIdList.value.length==1&&selectFileIdList.value[0]==row.fileId){
                fileTable.value.setSelectToRow(row,false)
                row.selected=false
        }else{
            clearSelection()
            fileTable.value.setRowSelect(row)
        }
    },500)
}
const clearSelection=()=>{
    fileTable.value.clearSelection()
    tableData.value.forEach(item=>
            item.selected=false)
}

const rowDbClick=(row)=>{
    clearTimeout(timer)
    selectFileIdList.value=[]
    openFile(row)
}

/*操作按钮方法定义*/
const emits = defineEmits(['addFile'])
//上传文件
const uploadFile = async (fileData) => {
    emits("addFile", {file: fileData.file, filePid: currentFolder.value.fileId});
}

const currentFolder = ref({fileId: 0, fileName: '根目录'})
const navChange = (data) => {
    const {curFolder, categoryId} = data;
    currentFolder.value = curFolder;
    queryForms.value.category = categoryId;
    getTableData();
};

//面包屑ref
const breadCrumbsRef = ref(null)
const openFile = (row) => {
    if (!row.fileId) {
        $modal.msgError('此目录为空目录，无法打开')
        return
    }
    if (row.folderType == 0) {
        breadCrumbsRef.value.openFolder(row);
        return;
    }
}


//一次只能有一个输入框在表格中
const isEditing = ref(false)
//input_html
const editNameRef = ref<HTMLInputElement | HTMLTextAreaElement>(null)
//新建文件夹
const newFolder = () => {
    if (isEditing.value) {
        return
    }
    tableData.value.forEach(item => {
        item.showEdit = false
    })
    isEditing.value = true
    tableData.value.unshift({
        showEdit: true,
        folderType: 0,
        fileId: '',
        filePid: currentFolder.value.fileId
    })
    nextTick(() => {
        editNameRef.value.focus()
    })
}

//重命名
const rename = (index) => {
    isEditing.value = false
    if (tableData.value[0].fileId == "") {
        tableData.value.splice(0, 1)
        --index
    }
    tableData.value.forEach(item => {
        item.showEdit = false
    })
    let currentData = tableData.value[index]
    currentData.showEdit = true
    currentData.showOp = false
    let fileName = currentData.fileName
    const delimiter = fileName.indexOf('.')
    //文件夹
    if (currentData.folderType == 0) {
        currentData.fileRealName = fileName
        currentData.fileSuffix = ''
    } else {
        if(delimiter!=-1){
            currentData.fileRealName = fileName.substring(0, delimiter)
            currentData.fileSuffix = fileName.substring(delimiter)
        }else{
            currentData.fileRealName = fileName
            currentData.fileSuffix = ''
        }
    }
    nextTick(() => {
        editNameRef.value.focus()
    })
}

//取消修改
const cancelEditName = (index) => {
    const fileData = tableData.value[index]
    if (fileData.fileId) {
        fileData.showEdit = false
    } else {
        tableData.value.splice(index, 1)
        isEditing.value = false
    }
}

import {specialStr} from "@/utils/regexValue";
import {defineEmits, defineExpose} from "vue";
//保存修改
const saveEditName = async (index) => {
    const {fileId, filePid, fileRealName, fileName,fileSuffix} = tableData.value[index]
    console.log(fileRealName+fileSuffix)
    if (!fileRealName) {
        $modal.msgWarning("文件(夹)名不能为空，请先输入文件名称")
        return
    }
    if (new RegExp(specialStr).test(fileRealName)) {
        $modal.msgWarning("文件(夹)名不能包含以下字符：<,>,|,*,?,,/")
        return
    }
    //文件名不能以.开头))
    if (fileId == '') {
        loading.value=true
        const res: any = await createFolder({fileName: fileRealName, filePid: filePid})
        if (res.code == 200) {
            $modal.msgSuccess("创建文件夹成功")
        }
        loading.value=false
    }
    //重命名不刷新列表
    else {
        if (fileRealName !== fileName) {
            loading.value=true
            const res: any = await renameFile({fileId: fileId, fileName: fileRealName+fileSuffix})
            if (res.code == 200) {
                $modal.msgSuccess("重命名成功")
            }
            loading.value=false
        }
    }
    await getTableData()
    isEditing.value = false
}

//展示操作按钮
const showOp = (row) => {
    tableData.value.forEach(item => {
        item.showOp = false
    })
    if (!row.showEdit) {
        row.showOp = true
    }
}
const closeOp = (row) => {
    row.showOp = false
}


//对话框配置
const dialogConfig=ref({
    title:'确认删除',
    footAlign:'center',
    dialogVisible:false,
    buttons:[
        {
            type:'primary',
            click:()=>{
                handleDel()
            },
            text:'确认删除'
        }
    ]
})

const openDelDialog = (row) => {
    if (!row.fileId && selectFileIdList.value.length === 0) {
        $modal.msgWarning('暂未选择要删除的文件')
        return
    }
    if(row.fileId){
        clearSelection()
        fileTable.value.setSelectToRow(row,true)
    }
    dialogConfig.value.dialogVisible = true
    console.log(selectFileIdList.value)
}

const handleDel=async ()=>{
    dialogConfig.value.dialogVisible=false
    loading.value=true
    try {
        await delFile(selectFileIdList.value)
        $modal.msgSuccess('删除成功')
        loading.value=false
        await getTableData()
    }catch (err){
        console.log(err)
    }
}

const closeDialog=()=>{
    dialogConfig.value.dialogVisible=false
}

//移动文件
const folderSelectRef = ref(null);
const moveFolder = (row) => {
    if(row.fileId){
        clearSelection()
        fileTable.value.setSelectToRow(row,true)
    }
    folderSelectRef.value.showFolderDialog();
    console.log(selectFileIdList.value)
};

//三个参数分别是要分别为：目标文件夹ID，导航文件表
const handleMove=async (folderId,navFolder,)=>{
    let fileIdsArray = [];
    fileIdsArray = fileIdsArray.concat(selectFileIdList.value);
    for (let i = 0; i <fileIdsArray.length ; i++) {
        if(currentFolder.value.fileId==folderId||navFolder.includes(fileIdsArray[i])){
            console.log(fileIdsArray)
            $modal.msgError('不能移动到当前目录或当前目录的子目录')
            return
        }
    }

    folderSelectRef.value.close();
    loading=true
    try {
        await moveFile({fileIds:fileIdsArray,filePid:folderId})
        $modal.msgSuccess("移动成功")
        selectFileIdList.value=[]
    }catch (err){
        console.log(err)
    }
    loading=false
    getTableData()
}

//表格高度自适应
if(showPagination)extHeight=50
const getTableHeight=() =>{
    config.tableHeight = window.innerHeight - topHeight-extHeight;
    if ( config.tableHeight <= 300) {
        config.tableHeight = 300;
    }
}
window.onresize=()=>{
    getTableHeight()
}

//上传文件的回调
const reload = () => {
    loading.value = true
    setTimeout(() => {
        loading.value=false
        getTableData()
    }, 0)
}
defineExpose({reload})

const downloadFile=async (row)=>{
    try {
        $modal.msgSuccess("正在获取下载链接，请稍后")
        await download({
            fileName:row.fileName
        })
    }catch (err){
        console.log(err)
    }

}

const shareFileDialogRef=ref(null)
const shareFile=(row)=>{
    if(row.fileId){
        clearSelection()
        fileTable.value.setSelectToRow(row,true)
    }

    let fileName:String
    for(let i=0;i<tableData.value.length;++i){
        if(selectFileIdList.value.includes(tableData.value[i].fileId)){
            fileName=tableData.value[i].fileName
            break
        }
    }
    //名字缩略
    if(selectFileIdList.value.length>1){
        fileName=fileName+'等'
    }
    shareFileDialogRef.value.showShareDialog({
        fileIds:selectFileIdList.value,
        fileName
    })
}

getTableHeight()

</script>

<template>
    <div class="operation-row">
        <template v-if="selectFileIdList.length===0">
            <el-upload
                :show-file-list="false"
                :with-credentials="true"
                :multiple="true"
                :http-request="uploadFile"
            >
                <el-button round type="primary">
                    <i class="iconfont icon-upload"/>
                    上传
                </el-button>
            </el-upload>
            <el-button round type="success" @click="newFolder" style="margin-left: 6px">
                <i class="iconfont icon-mkdir"/>
                新建文件夹
            </el-button>
        </template>
        <template v-else>
            <el-button-group>
                <el-button type="primary" round @click="shareFile">
                    <i class="iconfont icon-share"/>
                    分享
                </el-button>
                <el-button type="success" round>
                    <i class="iconfont icon-download"/>
                    下载
                </el-button>
                <el-button type="danger" round @click="openDelDialog">
                    <i class="iconfont icon-delete"/>
                    删除
                </el-button>
                <el-button type="warning" round @click="moveFolder">
                    <i class="iconfont icon-move"/>
                    移动
                </el-button>
            </el-button-group>
        </template>

        <el-input
            v-model="queryForms.fileName"
            placeholder="请输入文件名进行搜索"
            @keyup.enter="getTableData"
            @clear="getTableData"
            clearable
            style="width: 300px;margin-left: 10px">
            <template #suffix>
                <el-icon class="el-input__icon" style="cursor: pointer" @click="getTableData">
                    <search/>
                </el-icon>
            </template>
        </el-input>
    </div>
    <bread-crumbs @navChange="navChange" ref="breadCrumbsRef"/>
    <el-skeleton :loading="skLoading" animated :throttle="500">
        <template #template>
            <skeleton-table :table-height="config.tableHeight"/>
        </template>
        <template #default>
            <div class="file-list" v-if="tableData && tableData.length > 0">
                <Table
                    v-loading="loading"
                    element-loading-text="加载中..."
                    ref="fileTable"
                    :table-data="tableData"
                    :show-pagination="showPagination"
                    :show-page-size="showPageSize"
                    :total="total"
                    :query-forms="queryForms"
                    :config="config"
                    :columns="columns"
                    :get-table-data="getTableData"
                    :init-table-data="initTableData"
                    @rowClick="rowClick"
                    @rowDbClick="rowDbClick"
                    @rowSelected="rowSelected"
                >
                    <template #fileName="{index,row}">
                        <div
                            class="file-item"
                            @mouseenter="showOp(row)"
                            @mouseleave="closeOp(row)"
                        >
                            <template
                                v-if="(row.fileType===1||row.fileType===3)&&row.status===2"
                            >
                                <icon :cover="row.fileCover" :width="32"/>
                            </template>
                            <template v-else>
                                <icon v-if="row.folderType==0" :file-type="row.folderType" :width="32"/>
                                <icon v-if="row.folderType==1" :file-type="row.fileType" :width="32"/>
                            </template>
                            <span class="file-name text-ellip" :title="row.fileName" v-if="!row.showEdit">
                                     <span @click.stop="openFile(row)">{{ row.fileName }}</span>
                                <!-- <span v-if="row.status==0" class="transfer-status">转码中</span>
                                 <span v-if="row.status ==1" class="transfer-status transfer-fail"
                                      >转码失败</span>-->
                            </span>
                            <div class="edit-panel" v-if="row.showEdit">
                                <el-input
                                    v-model.trim="row.fileRealName"
                                    ref="editNameRef"
                                    :maxlength="20"
                                    @keyup.enter="saveEditName(index)"
                                    @click.stop
                                    @dblclick.stop
                                >
                                    <template #suffix>
                                        {{ row.fileSuffix }}
                                    </template>
                                </el-input>
                                <span
                                    :class="['iconfont icon-check',row.fileRealName?'':'not-allow']"
                                    @click.stop="saveEditName(index)"
                                />
                                <span
                                    class="iconfont icon-close"
                                    @click.stop="cancelEditName(index)"
                                />
                            </div>
                            <!--操作按钮-->
                            <span class="table-operation-btn" v-if="row.showOp&&row.fileId&&row.status==2">
                                <span class="iconfont icon-share" title="分享" @click.stop="shareFile(row)">分享</span>
                                <span class="iconfont icon-download" title="下载" @click.stop='downloadFile(row)' v-if="row.folderType!=0">下载</span>
                                <span class="iconfont icon-delete" title="删除" @click.stop="openDelDialog(row)">删除</span>
                                <span class="iconfont icon-rename" title="重命名" @click.stop="rename(index)">重命名</span>
                                <span class="iconfont icon-move" title="移动" @click.stop="moveFolder(row)">移动</span>
                            </span>
                        </div>
                    </template>
                    <template #fileSize="{index,row}">
                        <span v-if="row.fileSize">{{ covertSize(row.fileSize) }}</span>
                        <span v-else style="justify-content: center">-</span>
                    </template>
                </Table>
            </div>
            <div class="no-data" v-else>
                <div class="no-data-inner">
                    <Icon iconName="no_data" :width="120" fit="fill"></Icon>
                    <div class="tips">当前目录为空，上传你的第一个文件吧</div>
                    <div class="op-list">
                        <el-upload
                            :show-file-list="false"
                            :with-credentials="true"
                            :multiple="true"
                            :http-request="uploadFile"
                            :accept="fileAccept"
                        >
                            <div class="op-item">
                                <Icon iconName="file" :width="60"></Icon>
                                <div>上传文件</div>
                            </div>
                        </el-upload>
                        <div class="op-item" v-if="queryForms.category == 'all'" @click="newFolder">
                            <Icon iconName="folder" :width="60"></Icon>
                            <div>新建目录</div>
                        </div>
                    </div>
                </div>
            </div>
        </template>
    </el-skeleton>
            <confirm-dialog
                :dialog-visible="dialogConfig.dialogVisible"
                :buttons="dialogConfig.buttons"
                :foot-align="dialogConfig.footAlign"
                :title="dialogConfig.title"
                @closeDialog="closeDialog"
            >
            <div style="text-align: center;margin-top: 34px">
                <icon icon-name="del_warning" :width="50"/>
                <p style="margin-top: 12px;margin-bottom: 4px">确定删除所选文件吗？</p>
                <span>文件删除后十天内可在回收站进行还原</span>
            </div>
        </confirm-dialog>
        <folder-select
            ref="folderSelectRef"
            @confirmSelect="handleMove"
        />
        <share-file-dialog
            ref="shareFileDialogRef"
        />
</template>

<style lang="scss" scoped>
@import '../../assets/css/file_list.scss';

.operation-row {
    display: flex;
    align-items: center;
    margin-top: 20px;

    .iconfont {
        margin-right: 6px;
        font-size: 14px;
    }
}


</style>