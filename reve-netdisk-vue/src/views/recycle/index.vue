<!--
* @author Rêve
* @date 2023/5/22 21:02
* @description 
-->
<script setup lang="ts">
import {covertSize} from "@/utils/common";
import SystemConf from "@/constant/tableConstants";
import {deleteRecycleFile, loadRecycleList, recoverFile} from "@/api/recycle";
let {extHeight,showPagination}=SystemConf
const $modal = inject('$modal')
const router = useRouter();
const route = useRoute();
let loading=ref(false)
const fileTable=ref(null)
let total=ref(0)

//列表
const columns = [
    {
        label: "文件名",
        prop: "fileName",
        scopedSlots: "fileName",
        customHeader:'fileNameHeader'
    },
    {
        label: "删除时间",
        prop: "recycleTime",
        width: 200,
    },
    {
        label: "大小",
        prop: "fileSize",
        scopedSlots: "fileSize",
        width: 200,
    },
    {
        label:'有效时间',
        prop:"timeLeft",
        scopedSlots: 'timeLeft',
        width: 200
    }
];
//列表
const tableData = ref([{}]);
const tableConfig = {
    stripe: false,
    border: false,
    showSelection: true,    //展示单选框
    showIndex: false,   //展示序号
    tableHeight:0,
};

//表格高度自适应
const topHeight=56+52+23.8+10
if(showPagination)extHeight=50
const getTableHeight=() =>{
    tableConfig.tableHeight = window.innerHeight - extHeight-topHeight;
    if ( tableConfig.tableHeight <= 300) {
        tableConfig.tableHeight = 300;
    }
}
window.onresize=()=>{
    getTableHeight()
}
getTableHeight()

const getTableData = async () => {
    loading.value=true
    let params = {
        pageNum: 1,
        pageSize: 500,
    };
    try{
        const res=await loadRecycleList(params)
        if(res&&res.rows){
            tableData.value = res.rows
            total.value=res.total
        }
    }catch (err){
        console.log(err)
    }finally {
        loading.value=false
    }
};

//展示操作按钮
const showOp = (row) => {
    tableData.value.forEach((element) => {
        element.showOp = false;
    });
    row.showOp = true;
};

const cancelShowOp = (row) => {
    row.showOp = false;
};

const selectFileIdList = ref([]);
const rowSelected = (rows) => {
    selectFileIdList.value = [];
    rows.forEach((item) => {
        //不使用map，数据小map效率低于push
        selectFileIdList.value.push(item.fileId);
        item.selected=true
    });
};

const rowClick=(row)=>{
    if(selectFileIdList.value.length==1&&selectFileIdList.value[0]==row.fileId){
        fileTable.value.setSelectToRow(row,false)
        row.selected=false
    }else{
        clearSelection()
        fileTable.value.setRowSelect(row)
    }
}


//对话框配置
let dialogBtnFunc=ref();
let dialogContentText=ref('确定要彻底删除所选中的文件吗？')
const dialogConfig=ref({
    title:'彻底删除',
    footAlign:'center',
    dialogVisible:false,
    buttons:[
        {
            type:'primary',
            click: dialogBtnFunc,
            text:'确定'
        }
    ]
})

const clearSelection=()=>{
    fileTable.value.clearSelection()
    tableData.value.forEach(item=>
        item.selected=false)
}

//恢复
const revert = (row) => {
    if(row.fileId){
        clearSelection()
        fileTable.value.setSelectToRow(row,true)
    }
   dialogContentText.value='您确定要还原选中的文件吗？'
    dialogConfig.value.title='还原'
    dialogConfig.value.dialogVisible=true
    dialogBtnFunc.value=()=>{handleRevert()}
};

const handleRevert = async() => {
    if (selectFileIdList.value.length == 0) {
        $modal.msgError('暂未选择文件')
        return;
    }
    loading.value=true
    try{
        await recoverFile(selectFileIdList.value)
        dialogConfig.value.dialogVisible=false
        $modal.msgSuccess('文件还原成功')
        await getTableData()
    }catch (err){
        console.log(err)
    }finally {
        loading.value=false
    }
};
//删除文件
const delFile = (row) => {
    if(row.fileId){
        clearSelection()
        fileTable.value.setSelectToRow(row,true)
    }
    dialogContentText.value='您确定要删除选中的文件吗？'
    dialogConfig.value.title='彻底删除'
    dialogConfig.value.dialogVisible=true
    dialogBtnFunc.value=()=>{handleDel()}
};

const handleDel = async () => {
    if (selectFileIdList.value.length == 0) {
        $modal.msgError('暂未选择文件')
        return;
    }
    loading.value=true
    try{
        await deleteRecycleFile(selectFileIdList.value)
        dialogConfig.value.dialogVisible=false
        $modal.msgSuccess('删除成功')
        await getTableData()
    }catch (err){
        console.log(err)
    }finally {
        loading.value=false
    }
};

const closeDialog=()=>{
    dialogConfig.value.dialogVisible=false
}

const empty_recycle=()=>{
    dialogContentText.value='确定清空回收站？'
    dialogConfig.value.title='确定'
    dialogConfig.value.dialogVisible=true
    dialogBtnFunc.value=()=>{handleEmpty()}
}
const handleEmpty= async ()=>{
    loading.value=true
    const allRecycleFileId=[]
    tableData.value.forEach(item=>{
        allRecycleFileId.push(item.fileId)
    })
    try{
        await deleteRecycleFile(allRecycleFileId)
        dialogConfig.value.dialogVisible=false
        $modal.msgSuccess('已清空')
        await getTableData()
    }catch (err){
        console.log(err)
    }finally {
        loading.value=false
    }
}

getTableData()
</script>

<template>
    <div  v-if="tableData && tableData.length > 0">
        <div class="top">
            <el-button type="primary"  @click="empty_recycle" round>
                <span class="iconfont icon-empty">
                清空回收站
                </span>
            </el-button>
            <el-button type="danger" v-if="selectFileIdList.length > 0" @click="delFile" round>
                <span class="iconfont icon-delete"/><span>批量删除</span>
            </el-button>
        </div>
        <div class="header_title_info">
            <span class="name">回收站</span>
            <span class="count">已全部加载,共{{total}}个</span>
        </div>
        <div class="file-list">
            <Table
                v-loading="loading"
                element-loading-text="加载中..."
                    ref="fileTable"
                    :columns="columns"
                    :show-pagination="SystemConf.showPagination"
                    :table-data="tableData"
                    :get-table-data="getTableData"
                   :config="tableConfig"
                   @rowSelected="rowSelected"
                   @rowClick="rowClick"
            >
                <template #fileNameHeader>
                    <span v-if="selectFileIdList.length>0">
                        已选{{selectFileIdList.length}}个文件/文件夹
                        <span class="header_op">
                            <span class="iconfont icon-revert" @click.stop="revert()" >还原</span>
                        </span>
                    </span>
                    <span v-else>文件名</span>
                </template>
                <template #fileName="{ index, row }">
                    <div class="file-item" @mouseenter="showOp(row)" @mouseleave="cancelShowOp(row)">
                        <template v-if="
                             (row.fileType == 3 || row.fileType == 1) && row.status !== 0
                              ">
                            <icon :cover="row.fileCover"></icon>
                        </template>
                        <template v-else>
                            <icon v-if="row.folderType==0" :file-type="row.folderType" :width="32"/>
                            <icon v-if="row.folderType==1" :file-type="row.fileType" :width="32"/>
                        </template>
                        <span class="file-name text-ellip" :title="row.fileName">
                            <span>{{ row.fileName }}</span>
                        </span>
                        <span class="table-operation-btn" v-if="row.showOp && row.fileId">
                                <span class="iconfont icon-revert" @click.stop="revert(row)">还原</span>
                                <span class="iconfont icon-delete" @click.stop="delFile(row)">删除</span>
                        </span>
                    </div>
                </template>
                <template #fileSize="{ index, row }">
                    <span v-if="row.fileSize">{{ covertSize(row.fileSize) }}</span>
                    <span v-else style="justify-content: center">-</span>
                </template>
                <template #timeLeft="{ index, row }">
                    <span >{{ row.timeLeft }}天</span>
                </template>
            </Table>
        </div>
    </div>
    <div v-else class="empty_recycle">
        <icon icon-name="empty_recycle" :width="85"/>
        <p style="font-weight: bold">您的回收站是空哦~</p>
        <p>回收站为您保存十天内删除的文件</p>
    </div>
    <confirm-dialog
        :dialog-visible="dialogConfig.dialogVisible"
        :buttons="dialogConfig.buttons"
        :foot-align="dialogConfig.footAlign"
        :title="dialogConfig.title"
        @closeDialog="closeDialog"
    >
        <div style="text-align: center;margin-top: 34px">
            <p style="margin-top: 12px;margin-bottom: 4px">{{ dialogContentText }}</p>
        </div>
    </confirm-dialog>
</template>

<style lang="scss" scoped>
.top {
    margin-top: 20px;
    .iconfont{
        font-size: 12px;
        margin-right: 4px;
    }
    span{
        font-weight: bold;
    }
}

.not-allow {
    background: #d2d2d2 !important;
    cursor: not-allowed;
}
.header_op{
    font-size: 14px;
    margin-left: 20px;
    color: #15a9fa;
    cursor: pointer;
    .iconfont::before{
        margin-right: 3px;
    }
}
.file-item {
    display: flex;
    align-items: center;
    padding: 6px 0px;
}

.file-name {
    margin-left: 8px;
    flex: 1;
    width: 0;

    cursor: pointer;
    span:hover {
        color: #06a7ff;
    }
}

.not-allow {
    cursor: not-allowed;
    background: #7cb1d7;
    color: #ddd;
    text-decoration: none;
}


.op {
    width: 280px;
    margin-left: 15px;
}

.op .iconfont {
    font-size: 14px;
    margin-left: 10px;
    color: #15a9fa;
    cursor: pointer;
}

.op .iconfont::before {
    margin-right: 3px;
}


.no-data {
    height: calc(100vh - 150px);
    display: flex;
    align-items: center;
    justify-content: center;
}

.file-list {
    margin-top: 10px;
}

.file-item .op {
    width: 120px;
}

.empty_recycle{
    position: absolute;
    top: 40%;
    left: 50%;
    text-align: center;
}
</style>