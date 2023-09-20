<!--
* @author Rêve
* @date 2023/9/6 12:24
* @description 
-->
<script setup lang="ts">
import {useRouter, useRoute} from "vue-router";
import {userStore} from "@/pinia";
import {covertSize} from "@/utils/common";
import {getShareFileList, validShare} from "@/api/share";
import Cookies from "js-cookie";
import errorShareValidCode from "@/utils/errorShareValidCode";
import SystemConf from "@/constant/tableConstants";
import {require} from "@/utils/common";

let {extHeight,showPagination}=SystemConf

const uStore = userStore()
const router = useRouter();
const route = useRoute();
//0有数据。1链接不存在
const showType=ref(0)
const shareId = route.params.shareId;
const pwd = route.query.pwd
const shareToken=Cookies.get(`svtk${shareId}`)

let timer=null
let loading=ref(false)
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
        label: "修改时间",
        prop: "updateTime",
        width: 200,
    },
    {
        label: "大小",
        prop: "fileSize",
        scopedSlots: "fileSize",
        width: 200,
    },
];

const tableData = ref([{}]);
const tableConfig = {
    stripe: false,
    border: false,
    showSelection: true,    //展示单选框
    showIndex: false,   //展示序号
    tableHeight:0,
};

//表格高度自适应
const topHeight=224
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

const validResCode=(res:any)=>{
    if(res.code==errorShareValidCode.NO_SUCH_SHARE_LINK){
        //链接不存在
        showType.value=1
        return
    }else if(res.code==errorShareValidCode.SHARE_LINK_EXPIRED){
        //过期链接
        showType.value=2
        return
    }else if(res.code==errorShareValidCode.SHARE_TOKEN_VALID_EXPIRED){
        showType.value=3
        //认证过期
        router.push(`/shareCheck/${shareId}`);
    }
}

const shareInfo = ref({});
//首先验证
const getShareValidInfo= async () => {
    try {
        const res=await validShare({shareToken,shareId})
        // debugger
        validResCode(res)
        if(showType.value==0){
            shareInfo.value = res;
            await getTableData()
        }
    }catch (err){
        console.log(err)
    }

};
getShareValidInfo();


const getTableData = async () => {
    loading.value=true
    let params = {
        pageNum: 1,
        pageSize: 500,
        shareToken,
        shareId,
        filePid:currentFolder.value.fileId
    };
    try{
        const res=await getShareFileList(params)
        validResCode(res)
        if(showType.value==0) {
            tableData.value = res.rows
            total.value = res.total
        }
    }catch (err){
        console.log(err)
    }finally {
        loading.value=false
    }
};

const fileTable=ref(null)
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
}


//目录
const currentFolder = ref({fileId: 0});
const navChange = (data) => {
    console.log(data)
    const {curFolder} = data;
    currentFolder.value = curFolder;
    getTableData();
};

//查看
const previewRef = ref();
const breadCrumbsRef = ref();
const preview = (data) => {
    if (data.folderType == 0) {
        breadCrumbsRef.value.openFolder(data);
        return;
    }
   /* data.shareId = shareId;
    previewRef.value.showPreview(data, 2);*/
};

//下载文件
const download = async (fileId) => {

};

//保存到我的网盘
const folderSelectRef = ref();
const save2MyPanFileIdArray = [];
const save2MyPan = () => {
/*    if (selectFileIdList.value.length == 0) {
        return;
    }
    if (!proxy.VueCookies.get("userInfo")) {
        router.push("/login?redirectUrl=" + route.path);
        return;
    }
    save2MyPanFileIdArray.values = selectFileIdList.value;
    folderSelectRef.value.showFolderDialog();*/
};
const save2MyPanSingle = (row) => {
   /* if (!proxy.VueCookies.get("userInfo")) {
        router.push("/login?redirectUrl=" + route.path);
        return;
    }
    save2MyPanFileIdArray.values = [row.fileId];
    folderSelectRef.value.showFolderDialog();*/
};
//执行保存操作
const save2MyPanDone = async (folderId) => {
  /*  let result = await proxy.Request({
        url: api.saveShare,
        params: {
            shareId: shareId,
            shareFileIds: save2MyPanFileIdArray.values.join(","),
            myFolderId: folderId,
        },
    });
    if (!result) {
        return;
    }
    loadDataList();
    proxy.Message.success("保存成功");
    folderSelectRef.value.close();*/
};

//取消分享
const cancelShare = () => {
    /*proxy.Confirm(`你确定要取消分享吗？`, async () => {
        let result = await proxy.Request({
            url: api.cancelShare,
            params: {
                shareIds: shareId,
            },
        });
        if (!result) {
            return;
        }
        proxy.Message.success("取消分享成功");
        router.push("/");
    });*/
};

const jump = () => {
    router.push("/");
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
</script>

<template>
    <div class="navbar">
        <div class="logo">
            <img :src="require('@/assets/images/logo.png')" @click="router.push({ name: 'Home' })">
            <span class="name">Reve云盘</span>
        </div>
        <div class="right-menu">
            <el-dropdown class="avatar-container" trigger="click" v-if="uStore.getName">
                <div class="avatar-wrapper">
                    <img :src="uStore.getAvatar" class="user-avatar img-circle">
                    <i class="el-icon-caret-bottom"/>
                </div>
                <template #dropdown>
                    <el-dropdown-menu class="user-dropdown">
                        <router-link to="/">
                            <el-dropdown-item> 首页</el-dropdown-item>
                        </router-link>
                        <router-link to="/">
                            <el-dropdown-item> 个人中心</el-dropdown-item>
                        </router-link>
                        <el-dropdown-item divided @click.native="logout">
                            <span style="display: block">退出登录</span>
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
            <span v-else class="no-login">
                <span @click="pushLogin">登录</span>
                <span @click="pushRegister">注册</span>
            </span>
        </div>
    </div>

    <div class="share">
        <div class="share-body">
            <div class="user-info-aside">
                <div class="user_info">
                    <div class="avatar">
                        <img :src="require('@/assets/images/avatar.gif')" class="img-circle">
                    </div>
                    <div class="nick-name">nickName{{ shareInfo.nickName }} </div>
                    <div class="user-sign" v-if="shareInfo.signature">个性签名：{{ shareInfo.signature }}</div>
                    <div class="user-sign" v-else>暂无签名</div>
                </div>
            </div>
            <div class="share-file-main">
                    <template v-if="showType==0" >
                        <div class="share-file-header">
                            <div class="share-panel">
                                <div class="share-icon-name">
                                    <icon v-if="shareInfo.folderType==0" :file-type="shareInfo.folderType" :width="26"/>
                                    <icon v-if="shareInfo.folderType==1" :file-type="shareInfo.fileType" :width="26"/>
                                    <span class="share-file-name">{{ shareInfo.fileName }}</span>
                                </div>
                                <div class="share-op-btn">
                                    <el-button type="primary" @click="cancelShare"
                                               v-if="shareInfo.isCurrentUser"
                                    >
                                        <span class="iconfont icon-cancel"/>取消分享
                                    </el-button>
                                    <el-button type="primary" :disabled="selectFileIdList.length == 0"
                                               @click="save2MyPan"
                                               v-else
                                    ><span class="iconfont icon-save_disk"/>保存到我的网盘
                                    </el-button>
                                    <el-button type="primary" :disabled="selectFileIdList.length == 0"
                                    ><span class="iconfont icon-download"/>下载
                                    </el-button>
                                </div>
                            </div>
                            <div class="share-file-time">
                                <i class="iconfont icon-time"/>
                                <span>{{shareInfo.createTime}}</span>
                                <span class="share-file-timeLeft">
                                     <span v-if="shareInfo.timeLeft!=-1">过期时间：{{shareInfo.timeLeft}}天后过期</span>
                                     <span v-else>永久有效</span>
                                </span>
                            </div>
                        </div>

                        <!--导航-->
                        <bread-crumbs @navChange="navChange" ref="breadCrumbsRef" style="padding-left: 20px"/>
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
                                @rowClick="rowClick"
                                @rowDbClick="rowDbClick"
                                @rowSelected="rowSelected"
                            >
                                <template #fileNameHeader>
                            <span v-if="selectFileIdList.length>0">
                                已选{{selectFileIdList.length}}个文件/文件夹
                                    <span class="header_op">
                                        <span class="iconfont icon-revert" @click.stop="revert()" >保存到网盘</span>
                                    </span>
                            </span>
                                    <span v-else>文件名</span>
                                </template>
                                <template #fileName="{ index, row }">
                                    <div class="file-item" @mouseenter="showOp(row)" @mouseleave="cancelShowOp(row)">
                                        <template v-if="(row.fileType == 3 || row.fileType == 1) && row.status !== 0">
                                            <icon :cover="row.fileCover"></icon>
                                        </template>
                                        <template v-else>
                                            <icon v-if="row.folderType==0" :file-type="row.folderType" :width="32"/>
                                            <icon v-if="row.folderType==1" :file-type="row.fileType" :width="32"/>
                                        </template>

                                        <span class="file-name" :title="row.fileName">
                                     <span @click.stop="preview(row)">{{ row.fileName }}</span>
                                </span>

                                        <!-- operation_button -->
                                        <span class="operation_btn" v-if="row.showOp">
                                  <span v-if="row.folderType != 0" class="iconfont icon-download"
                                        @click.stop="download(row.fileId)"/>
                                    <span class="iconfont icon-save_disk" @click.stop="save2MyPanSingle(row)"
                                          v-if="!shareInfo.isCurrentUser"/>
                                 </span>
                                    </div>
                                </template>
                                <template #fileSize="{ index, row }">
                                    <span v-if="row.fileSize">{{ covertSize(row.fileSize) }}</span>
                                    <span v-else>-</span>
                                </template>
                                <template #empty>
                                    暂无数据
                                </template>
                            </Table>
                        </div>
                    </template>
            </div>

            <!--选择目录-->
            <!--<FolderSelect ref="folderSelectRef" @folderSelect="save2MyPanDone"></FolderSelect>-->
            <!--预览-->
            <!--<Preview ref="previewRef"></Preview>-->
        </div>
        <div class="el-login-footer">
            <span>Copyright © 2020-2023 @Rêve All Rights Reserved.</span>
        </div>
    </div>
</template>

<style lang="scss" scoped>
@import '../../assets/css/file_list.scss';

.share-body {
    display: flex;
    background-color:#F6F6F6;
    width: 100%;
    height: calc(100vh - 56px);
}

.user-info-aside{
    height:calc(100vh - 100px);
    width: 200px;
    margin:10px 10px 10px 30px;
    background-color: white;
    border-radius: 5px;
    border: 1px solid #e6e6e6;
}
.share-file-main{
    flex:1;
    width: 0;
    height:calc(100vh - 100px);
    margin:10px 30px 10px 0px;
    background-color: white;
    border-radius: 5px;
    border: 1px solid #e6e6e6;
}
.el-login-footer{
        height: 40px;
        line-height: 40px;
        position: fixed;
        bottom: 0;
        width: 100%;
        text-align: center;
        color: gray;
        font-family: Arial;
        font-size: 12px;
        letter-spacing: 1px;
}

.navbar {
    height: 56px;
    overflow: hidden;
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    background: #fff;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

    .right-menu {
        float: right;
        height: 100%;
        line-height: 50px;

        .no-login{
            font-size: 14px;
            margin-right: 20px;
            span{
                padding-right: 10px;
            }
        }

        //主要是取消元素的轮廓线,通常用于消除点击时的虚线框
        &:focus {
            outline: none;
        }

        .right-menu-item {
            display: inline-block;
            padding: 0 8px;
            height: 100%;
            font-size: 18px;
            color: #5a5e66;
            vertical-align: text-bottom;

            &.hover-effect {
                cursor: pointer;
                transition: background 0.3s;

                &:hover {
                    background: rgba(0, 0, 0, 0.025);
                }
            }
        }

        .avatar-container {
            margin-right: 30px;

            .avatar-wrapper {
                margin-top: 5px;
                position: relative;

                .user-avatar {
                    cursor: pointer;
                    width: 40px;
                    height: 40px;
                    //border-radius: 10px;
                }

                .el-icon-caret-bottom {
                    cursor: pointer;
                    position: absolute;
                    right: -20px;
                    top: 25px;
                    font-size: 12px;
                }
            }
        }
    }

    .logo {
        cursor: pointer;
        display: flex;
        align-items: center;
        img{
            display: inline-block;
            height: 40px;
            margin: 4px 0 0 24px;
        }
        .name{
            font-weight: 700;
            font-size: 20px;
            line-height: 20px;
        }
    }
}
.share-op-btn{
    .iconfont{
        margin-right:3px ;
    }
}


//logoname
.name {
    font-weight: bold;
    margin-left: 5px;
    font-size: 25px;
}

.share-file-header{
    padding:15px 20px 5px 20px;
    border-bottom: 1px solid #ddd;
    .share-panel {
        display: flex;
        width: 100%;
        justify-content: space-between;
        padding-bottom: 10px;
        .share-icon-name{
            display: flex;
            align-items: center;
            .share-file-name{
                margin-left: 8px;
                font-weight: 700;
                vertical-align: center;
            }
        }
    }
    .share-file-time{
        font-size: 12px;
    }
    .share-file-timeLeft{
        font-size: 12px;
        margin-left: 20px;
    }
}


.operation_btn{
    margin-left: 15px;
    .iconfont{
        font-size: 16px;
        margin-left: 10px;
        color:#06a7ff;
        cursor: pointer;
    }
    .iconfont::before{
        margin-right: 3px;
    }
}

.file-list {
    margin-top: 10px;
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
.user_info{
    padding: 10px 0px;
    .avatar{
        text-align: center;
        img{
            width: 55px;height:55px;
            border: 3px solid #d9e2ea

        }
    }
    .nick-name {
        text-align: center;
        font-size: 15px;
        padding: 10px 0px;
        color: black;
    }
    .user-sign {
        text-align: center;
        font-size: 12px;
    }
}

</style>