<!--
* @author Rêve
* @date 2023/5/22 16:19
* @description 
-->
<script setup lang="ts">
import {useRouter,useRoute} from "vue-router";
import {userStore,fileStore} from "@/pinia";
import {require} from "@/utils/common";
import {covertSize} from "@/utils/common";
import {defineExpose} from "vue";

const router=useRouter()
const route=useRoute()
const uStore = userStore()
const fStore=fileStore()


const logoUrl = require('@/assets/images/logo.png')
const logout = () => {

}

const menus = [
    {
        icon: 'cloud',
        name: '首页',
        menuCode: 'main',
        path: '/main/all',
        allShow: true,
        children: [
            {
                icon: 'all',
                name: '全部',
                category: 'all',
                path: '/main/all',
            },
            {
                icon: 'video',
                name: '视频',
                category: 'video',
                path: '/main/video',
            },
            {
                icon: 'music',
                name: '音频',
                category: 'music',
                path: '/main/music',
            },
            {
                icon: 'image',
                name: '图片',
                category: 'image',
                path: '/main/image',
            },
            {
                icon: 'doc',
                name: '文档',
                category: 'doc',
                path: '/main/doc',
            },
            {
                icon: 'more',
                name: '其他',
                category: 'others',
                path: '/main/others',
            },
        ]
    },
    {
        icon: 'share',
        name: '分享',
        menuCode: 'share',
        path: '/myshare',
        allShow: true,
        children: [
            {
                name: '分享记录',
                path: '/myshare',
            }]
    },
    {
        icon: 'recycle',
        name: '回收站',
        menuCode: 'recycle',
        path: '/recycle',
        tips: '回收站会为你保留十天的记录',
        allShow: true,
        children: [
            {
                name: '删除的文件',
                path: '/recycle',
            }]
    },
]
const currentMenu=ref({})
const currentPath=ref()
const jump=(item)=>{
    if(!item.path||item.menuCode==currentMenu.value.menuCode){
        return
    }
    router.push(item.path)
}
const setMenu=(menuCode,path)=>{
    const menu=menus.find((item)=>{
        return item.menuCode===menuCode
    })

    currentMenu.value=menu
    currentPath.value=path
}

const showUploader=ref(false)
const uploaderRef=ref(null)
//添加文件到列表
const addFile=(data)=>{
    const { file, filePid } = data;
    showUploader.value = true;
    uploaderRef.value.uploadFile(file, filePid);
}

const routerViewRef=ref(null)
const uploadedCallback = () => {
    nextTick(() => {
        routerViewRef.value.reload();
        uStore.getUserSpace()
    });
};

watch(()=>route,(newVal:any)=>{
    if(newVal.meta.menuCode){
        setMenu(newVal.meta.menuCode,newVal.path)
    }
},{immediate:true,deep:true})

</script>

<template>
        <div class="navbar">
            <img class="logo" :src="logoUrl" @click="router.push({ name: 'Home' })"/>

            <div class="right-menu">
                <el-popover
                    :width="700"
                    trigger="click"
                    v-model:visible="showUploader"
                    :offset="20"
                    :hide-after="0"
                    :popper-style="{padding:'10px'}"
                >
                    <template #reference>
                        <svg-icon icon-class="transfer" style="margin-right:20px"/>
                    </template>
                    <template #default>
                        <uploader
                            ref="uploaderRef"
                            @uploadedCallback="uploadedCallback"
                        />
                    </template>
                </el-popover>
                <el-dropdown class="avatar-container" trigger="click">
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
            </div>
        </div>
        <div class="body">
            <div class="left-side">
                <div class="menu-list">
                    <div :class="['menu-item',item.menuCode==currentMenu.menuCode?'active':'']" v-for="item in menus" @click="jump(item)">
                        <svg-icon :icon-class="item.icon" class="iconfont"/>
                        <div class="text">{{ item.name }}</div>
                    </div>

                </div>
                <div class="menu-sub-list">
                    <div @click="jump(sub)" :class="['menu-item-sub',currentPath==sub.path?'active':'']" v-for="sub in currentMenu.children">
                        <i :class="['iconfont', 'icon-' + sub.icon]" v-if="sub.icon"/>
                        <span class="text">{{ sub.name }}</span>
                    </div>
                    <div class="tips" v-if="currentMenu&&currentMenu.tips">{{ currentMenu.tips }}</div>
                    <div class="space-info">
                        <div >空间使用</div>
                        <div class="percent">
                            <el-progress
                                :percentage="
                                      Math.floor(
                                        (uStore.getUseSpace / uStore.getTotalSpace) * 100
                                      )
                                    "
                                color="#409eff"
                            />
                        </div>
                        <div class="space-use">
                            <div class="use">
                                {{ covertSize(uStore.getUseSpace) }} /
                                {{covertSize( uStore.getTotalSpace) }}
                            </div>
                            <div class="iconfont icon-refresh" @click="getUseSpace"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="body-content">
                <router-view v-slot="{ Component }">
                    <component
                        @addFile="addFile"
                        ref="routerViewRef"
                        :is="Component"
                    />
                </router-view>
            </div>
        </div>
</template>

<style lang="scss" scoped>
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
        margin: 4px 24px 0 24px;
        display: inline-block;
        height: 40px;
        cursor: pointer;
    }
}
.body {
    display: flex;

    .left-side {
        border-right: 1px solid #f1f2f4;
        display: flex;

        .menu-list {
            height: calc(100vh - 56px);
            width: 80px;
            box-shadow: 0 3px 10px 0 rgb(0 0 0 / 6%);
            border-right: 1px solid #f1f2f4;

            .menu-item {
                text-align: center;
                font-size: 14px;
                font-weight: bold;
                padding: 20px 0px;
                cursor: pointer;
                .iconfont{
                    font-weight: normal;
                    font-size: 28px;
                }
                &:hover {
                    background: #f3f3f3;
                }
            }
            .active {
                .iconfont {
                    color: #06a7ff;
                }
                .text {
                    color: #06a7ff;
                }
            }
        }

        .menu-sub-list {
            width: 200px;
            padding: 20px 10px 0px;
            position: relative;

            .menu-item-sub {
                text-align: center;
                line-height: 40px;
                border-radius: 5px;
                cursor: pointer;
                .iconfont{
                    margin-right: 10px;
                    font-size: 14px;
                }
                &:hover {
                    background: #f3f3f3;
                }
                .text {
                    font-size: 13px;
                }
            }

            .active {
                background: #eef9fe;
                .iconfont {
                    color: #05a1f5;
                }
                .text {
                    color: #05a1f5;
                }
            }
        }

        .tips {
            margin-top: 10px;
            color: #888888;
            font-size: 13px;
        }

        .space-info {
            position: absolute;
            bottom: 10px;
            width: 100%;
            padding: 0px 5px;
            .iconfont{
                cursor: pointer;
                margin-right: 20px;
                color: #05a1f5
            }
            .percent {
                padding-right: 10px;
            }
        }

        .space-use {
            margin-top: 5px;
            color:#afb3bf;
            display: flex;
            justify-content: space-around;
            font-size: 12px;
            .use {
                flex: 1;
            }
        }
    }
    .body-content {
        flex: 1;
        width: 0;
        padding-left: 20px;
    }

}
</style>