<!--
* @author Rêve
* @date 2023/8/24 21:01
* @description 
-->
<script setup lang="ts">
import {getFolderNavigation} from "@/api/file";

const emit = defineEmits(["navChange"])
const router = useRouter()
const route = useRoute()

const folderList = ref([])
const currentFolder = ref({ fileId: "0" })
const category = ref()

const props = defineProps({
    watchPath: {
        type: Boolean,
        default: true
    },
    shareId: {
        type: String
    },
    adminShow: {
        type: Boolean,
        default: false
    }
})

const init = () => {
    folderList.value = []
    currentFolder.value = { fileId: "0" }
    navChangeCallback()
}

//打开文件夹，设置导航
const openFolder = (data) => {
    const { fileId, fileName } = data
    const folder = {
        fileName: fileName,
        fileId: fileId
    }
    folderList.value.push(folder)
    currentFolder.value = folder;
    //跳转路由
    setPath()
}

//返回上一级
const backParent = () => {
    setCurrentFolder(folderList.value.length - 2)
}

const backAll = () => {
    setCurrentFolder(-1)
}


const setCurrentFolder = (index) => {
    if (index === -1) {
        currentFolder.value = { fileId: "0" };
        folderList.value = [];
    } else {
        currentFolder.value = folderList.value[index];
        folderList.value.splice(index + 1, folderList.value.length);
    }
    setPath();
}

//设置路由，并跳转
const setPath = () => {
    if (!props.watchPath) {
        navChangeCallback()
        return
    }
    let pathArray = []
    folderList.value.forEach(item => {
        pathArray.push(item.fileName)
    })
    router.push({
        path: route.path,
        query: pathArray.length == 0 ? "" : { path: pathArray.join("/") }
    })
}

const getNavigationFolder = async (path) => {
 /*   let url = "/file/getFolderInfo"
    if (props.shareId) {
        url = "/file/getFolderInfo"
    }
    if (props.adminShow) {
        url = "/file/getFolderInfo4Admin"
    }
    let result = await proxy.Request({
        url: url,
        showLoading: false,
        params: {
            path: path,
            shareId: props.shareId
        }
    })
    if (!result) {
        return
    }
    folderList.value = result.data*/
    const res = await getFolderNavigation({userId:props.shareId,path:path})
    if(!res){
        return
    }
    folderList.value=res
}

const navChangeCallback = () => {
    emit("navChange", {
        categoryId: category.value,
        curFolder: currentFolder.value
    })
}

//监听路由变化，路由变化刷新页面
watch(
    () => route,
    async (newVal:any, oldVal) =>  {

        if (!props.watchPath) {
            return
        }
       /* if (newVal.path.indexOf("/main") === -1 &&
            newVal.path.indexOf("/settings/fileList") === -1 &&
            newVal.path.indexOf("/shareInfo") === -1) {
            return
        }*/


        const path = newVal.query.path
        category.value = newVal.params.category

        if (path == undefined) {
            init()
        } else {
            try {
                //获取面包屑，只在刷新网页时请求接口
                if(folderList.value&&folderList.value.length===0){
                    await getNavigationFolder(path)
                }
                let pathArray = path.split("/")
                currentFolder.value = {
                    fileId:folderList.value[folderList.value.length-1].fileId,
                    fileName: pathArray[pathArray.length - 1]
                }
                navChangeCallback()
            }catch (err){
                router.push({
                    path: '/',
                })
                console.error(err)
            }
        }
    },
    { immediate: true, deep: true }
)
const getNavigation=()=>{
    return folderList.value
}
defineExpose({
    openFolder,
    getNavigation,
    init
})
</script>

<template>
    <div class="top-navigation">
        <template v-if="folderList.length > 0">
            <span class="back link" @click="backParent">返回上一级</span>
            <el-divider direction="vertical"></el-divider>
        </template>
        <span v-if="folderList.length == 0" class="all-file">全部文件</span>
        <span v-if="folderList.length > 0" class="link" @click="backAll">全部文件</span>
        <template v-if="folderList.length>3">
            <span class="iconfont icon-right"/>
            <span class="text" v-if="folderList.length>3">...</span>
        </template>
        <template v-for="(item, index) in folderList">
            <template v-if="index>=folderList.length-3&&index<folderList.length-1">
                <span class="iconfont icon-right"/>
                <span class="link text-ellipsis" @click="setCurrentFolder(index)">{{item.fileName}}</span>
            </template>
            <template v-else-if="index === folderList.length-1 ">
                <span class="iconfont icon-right"/>
                <span class="text text-ellipsis">{{item.fileName}}</span>
            </template>
        </template>
    </div>
</template>

<style lang="scss" scoped>
.top-navigation {
    font-size: 13px;
    display: flex;
    align-items: center;
    line-height: 40px;
}

.all-file {
    font-weight: bold;
}

.link {
    color: #06a7ff;
    cursor: pointer;
}
.text-ellipsis{
    max-width: 120px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
}

.icon-right {
    color: #06a7ff;
    padding: 0px 5px;
    font-size: 13px;
}
</style>