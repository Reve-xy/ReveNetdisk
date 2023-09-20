<!--
* @author Rêve
* @date 2023/5/22 21:02
* @description 
-->
<script setup lang="ts">
import useClipboard from "vue-clipboard3";
const { toClipboard } = useClipboard();
import SystemConf from "@/constant/tableConstants";
import {delShareFile, listShareFile} from "@/api/share";
let {extHeight,showPagination}=SystemConf
const $modal = inject('$modal')
const router = useRouter();
const route = useRoute();
let loading=ref(false)
const shareTable=ref(null)
let total=ref(0)
const shareUrl = ref(document.location.origin + "/share/");

//列表
const columns = [
    {
        label: "分享文件",
        prop: "fileName",
        scopedSlots: "fileName",
        customHeader:'fileNameHeader'
    },
    {
        label: "分享时间",
        prop: "createTime",
        width: 200,
    },
    {
        label: "状态",
        prop: "timeLeft",
        scopedSlots: "timeLeft",
        width: 200,
    },
    {
        label:'浏览次数',
        prop:"viewCount",
        scopedSlots: 'viewCount',
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
const topHeight=56+33.8
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
        const res=await listShareFile(params)
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
        selectFileIdList.value.push(item.shareId);
        item.selected=true
    });
};

const rowClick=(row)=>{
    if(selectFileIdList.value.length==1&&selectFileIdList.value[0]==row.shareId){
        shareTable.value.setSelectToRow(row,false)
        row.selected=false
    }else{
        clearSelection()
        shareTable.value.setRowSelect(row)
    }
}


//对话框配置
const autoFillPwd=ref<Boolean>(false)
let currentRow=ref({})
let dialogContentText=ref('是否开启「自动填充提取码」\n' +
    '\n' +
    '开启「自动填充提取码」后，访问者无需输入提取码，\n' +
    '可直接查看分享文件？')
let dialogBtnFunc=ref();
const dialogConfig=ref({
    title:'复制分享链接',
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
    shareTable.value.clearSelection()
    tableData.value.forEach(item=>
        item.selected=false)
}

//呼出对话框
const copyLink=(row)=>{
    if(row.shareId){
        clearSelection()
        shareTable.value.setSelectToRow(row,true)
    }
    currentRow.value={}
    dialogConfig.value.title='复制分享链接'
    dialogContentText.value='是否开启「自动填充提取码」？开启「自动填充提取码」后，访问者无需输入提取码，即可直接查看分享文件'
    dialogBtnFunc.value=()=>{
        dialogConfig.value.dialogVisible=false
        autoFillPwd.value=true
        handleCopyLink()
    }
    dialogConfig.value.dialogVisible=true
    currentRow.value=row
}

//复制
const handleCopyLink = async () => {
    const fillPwdContent:string=autoFillPwd.value?`?pwd=${currentRow.value.code}`:''
    await toClipboard(
        `链接:${shareUrl.value}${currentRow.value.shareId}${fillPwdContent} 提取码: ${currentRow.value.code}`
    );
    //设置延时，否则会因为dialog的关闭动画影响message位置
    $modal.msgSuccess("复制成功，快去分享给好友吧");
    currentRow.value={}
};

//取消分享
const cancelShare = (row) => {
    if(row.shareId){
        clearSelection()
        shareTable.value.setSelectToRow(row,true)
    }
    dialogContentText.value='取消分享后，该条分享记录将被删除，好友将无法再访问此分享链接。 您确认要取消分享吗？'
    dialogConfig.value.title='确定取消分享'
    dialogConfig.value.dialogVisible=true
    dialogBtnFunc.value=()=>{handleCancel()}
};

const handleCancel = async () => {
    if (selectFileIdList.value.length == 0) {
        $modal.msgError('暂未选择文件')
        return;
    }
    loading.value=true
    try{
        await delShareFile(selectFileIdList.value)
        closeDialog()
        $modal.msgSuccess('取消分享成功')
        await getTableData()
    }catch (err){
        console.log(err)
    }finally {
        loading.value=false
    }
};

const closeDialog=(isCancelBtn)=>{
    dialogConfig.value.dialogVisible=false
    if(isCancelBtn&&currentRow.value.shareId){
        autoFillPwd.value=false
        handleCopyLink()
    }
}

//分享详情
const shareFileDetailsRef=ref(null)
const detailObj=ref<Array<Object>>([{}])
const fileNameTitle=ref<Object>({})
const viewFileDetails=(row)=>{
    detailObj.value=[{
        label:"分享时间",
        value:row.createTime
       },
        {
            label:"有效期",
            value:row.timeLeft+'天后过期'
        },
        {
            label:"提取码",
            value:row.code
        },
        {
            label:"浏览",
            value:row.viewCount+'次'
        },
        {
            label:"保存",
            value:row.saveCount+'次'
        },
        {
            label:"下载",
            value:row.downloadCount+'次'
        },
    ]
     fileNameTitle.value={
        fileName:row.fileName,
         fileType:row.fileType
     }
    shareFileDetailsRef.value.showShareFileDetails()
}

getTableData()
</script>

<template>
    <div class="header_title_info">
        <span class="name">分享文件</span>
        <span class="count">已全部加载,共{{total}}个</span>
    </div>
    <div  v-if="tableData && tableData.length > 0">
        <div class="file-list">
            <Table
                v-loading="loading"
                element-loading-text="加载中..."
                ref="shareTable"
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
                            <span class="iconfont icon-cancel" @click.stop="cancelShare()" >取消分享</span>
                        </span>
                    </span>
                    <span v-else>分享文件</span>
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
                        <span class="file-name" :title="row.fileName" @click.stop="viewFileDetails(row)">
                            <span>{{ row.fileName }}</span>
                        </span>
                        <span class="table-operation-btn" v-if="row.showOp && row.shareId">
                                <span class="iconfont icon-copy_link" @click.stop="copyLink(row)">复制链接</span>
                                <span class="iconfont icon-cancel" @click.stop="cancelShare(row)">取消分享</span>
                        </span>
                    </div>
                </template>
                <template #timeLeft="{ index, row }">
                    <span v-if="row.timeLeft>0">{{ row.timeLeft }}天后过期</span>
                    <span v-else >已过期</span>
                </template>
                <template #viewCount="{ index, row }">
                    <span >{{ row.viewCount }}次</span>
                </template>
            </Table>
        </div>
    </div>
    <confirm-dialog
        :dialog-visible="dialogConfig.dialogVisible"
        :buttons="dialogConfig.buttons"
        :foot-align="dialogConfig.footAlign"
        :title="dialogConfig.title"
        @closeDialog="closeDialog"
    >
        <div style="text-align: center;margin-top: 30px">
            <p style="padding: 12px 30px">{{ dialogContentText }}</p>
        </div>
    </confirm-dialog>
    <share-file-details
        ref="shareFileDetailsRef"
        :details-obj="detailObj"
        :file-name-title="fileNameTitle"
    />
</template>

<style lang="scss" scoped>
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
    margin-left:10px;
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
    width: 150px;
}
</style>