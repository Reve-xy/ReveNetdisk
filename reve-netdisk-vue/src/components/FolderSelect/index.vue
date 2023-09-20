<!--
* @author Rêve
* @date 2023/8/28 18:04
* @description 
-->
<script setup lang="ts">

import {getAllFolder} from "@/api/file";

const router = useRouter();
const route = useRoute();
const navigationRef = ref();
let loading=ref(false)
const dialogConfig = ref({
    dialogVisible: false,
    title: "移动到",
    buttons: [
        {
            type: "primary",
            click: () => {
                ConfirmSelect();
            },
            text: "移动到此",
        },
    ],
    footAlign:'right'
});

const filePid = ref("0");
const folderList = ref([{}]);
//获取当前目录
const loadAllFolder = async () => {
    loading.value=true
    const res=await getAllFolder({filePid:filePid.value})
    if (!res) {
        return;
    }
    folderList.value = res;
    loading.value=false
};

const close = () => {
    dialogConfig.value.dialogVisible = false;
};


//展示弹出框对外的方法
const showFolderDialog = () => {
    folderList.value=[{}]
    dialogConfig.value.dialogVisible = true;
    filePid.value = "0";
    nextTick(() => {
        navigationRef.value.init();
    });
};

defineExpose({
    showFolderDialog,
    close,
});
//选择目录
const selectFolder = (data) => {
    navigationRef.value.openFolder(data);
};

//当前的目录
const currentFolder = ref({});
//导航改变回调
const navChange = (data) => {
    const { curFolder } = data;
    currentFolder.value = curFolder;
    filePid.value = curFolder.fileId;
    loadAllFolder();
};

const emit = defineEmits(["ConfirmSelect"]);

const ConfirmSelect = () => {
    const navFolder=navigationRef.value.getNavigation()
    const navFolderIds=[]
    navFolder.forEach(item=>{
        navFolderIds.push(item.fileId)
    })
    emit("ConfirmSelect", filePid.value,navFolderIds);
};
</script>

<template>
    <div>
        <confirm-dialog
            :dialogVisible="dialogConfig.dialogVisible"
            :title="dialogConfig.title"
            :buttons="dialogConfig.buttons"
            :foot-align="dialogConfig.footAlign"
            width="700px"
            @close-dialog="close"
        >
            <div class="navigation-panel">
                <bread-crumbs
                    ref="navigationRef"
                    @navChange="navChange"
                    :watchPath="false"
                ></bread-crumbs>
            </div>
            <el-scrollbar  max-height="300px">
            <div class="folder-list" v-if="folderList.length > 0" v-loading="loading" element-loading-text="加载中...">
                <div
                    class="folder-item"
                    v-for="item in folderList"
                    @click="selectFolder(item)"
                >
                    <icon :fileType="0"></icon>
                    <span class="file-name text-ellip">{{ item.fileName }}</span>
                </div>
            </div>
            <div v-else class="tips">
                <div>
                    <icon icon-name="empty_folder" :width="90"/>
                </div>
                移动到 <span>{{ currentFolder.fileName }}</span> 文件夹
            </div>
            </el-scrollbar>
        </confirm-dialog>
    </div>
</template>

<style lang="scss" scoped>
.navigation-panel {
    height: 100%;
    padding-left: 10px;
    background: #fafafc;
}
.folder-list {
    height:300px;
    .folder-item {
        cursor: pointer;
        display: flex;
        align-items: center;
        padding: 10px;
        .file-name {
            display: inline-block;
            margin-left: 10px;
        }
        &:hover {
            background: #f7f9fc;
        }
    }
    max-height: calc(100vh - 200px);
}
.tips {
    height:300px;
    text-align: center;
    padding: 90px;
    span {
        color: #06a7ff;
    }
}
</style>