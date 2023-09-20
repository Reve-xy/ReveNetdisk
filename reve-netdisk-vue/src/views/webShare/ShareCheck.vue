<!--
* @author Rêve
* @date 2023/9/6 12:24
* @description 
-->
<script setup lang="ts">
import {useRouter, useRoute} from "vue-router";
import {require} from "@/utils/common";
import {checkShareCode, getWebShareInfo} from "@/api/share";
import Cookies from 'js-cookie'
import {userStore} from "@/pinia";
import errorShareValidCode from "@/utils/errorShareValidCode";

const $modal=inject("$modal")
const uStore=userStore()
const router = useRouter();
const route = useRoute();

const shareId = route.params.shareId;
const pwd=route.query.pwd   //提取码
const shareInfo = ref({});
let loading=ref(false)


const formData = ref({});
const formDataRef = ref();

const checkShare = async () => {
    loading.value=true
    debugger
    try {
        //返回shareValidToken
        const res=await checkShareCode({code:formData.value.code,shareId:shareId})
        if(!res){
            return
        }
        if(res.code==errorShareValidCode.SHARE_CODE_ERR){
            $modal.msgError(res.msg)
        } else{
            //shareValidToken过期一个小时
            Cookies.set(`svtk${shareId}`,res,{expires:1/24})
            router.push(`/share/${shareId}`);
        }
    }catch (err){
        console.log(err)
    } finally {
        loading.value=false
    }

};

//获取分享信息
const getShareInfo = async () => {
    try {
        const res=await getWebShareInfo({shareId,shareToken:Cookies.get(`svtk${shareId}`)})
        if (!res.data&&res.code==200) {
            //token认证成功
            router.push(`/share/${shareId}`)
            return;
        }
        //与登录用户相同，直接跳转信息页
        if(uStore.getUserId==res.userId){
            router.push(`/share/${shareId}`)
            return
        }
        shareInfo.value=res;
    }catch (err){
        console.log(err)
    }
};

const init=async ()=>{
    if(pwd){
        formData.value.code=pwd
       await checkShare()
    }
}
getShareInfo();
init()
</script>

<template>
    <div class="share">
        <div class="body-content">
            <el-card>
                <template #header>
                    <div class="logo">
                        <img :src="require('@/assets/images/logo.png')" style="width: 60px;height:60px"/>
                        <span class="name">Reve网盘</span>
                    </div>
                </template>
                <el-form :model="formData" ref="formDataRef" @submit.prevent>
                    <el-form-item>
                        <div class="avatar">
                            <img :src="shareInfo.avatar||require('@/assets/images/avatar.gif')" class="img-circle"
                                 style="width: 97px;height:97px"/>
                        </div>
                        <div class="share-info">
                            <div class="user-info">
                                <span class="nick-name text-ellip">{{ shareInfo.nickName }}Reve wdwdwdwdwd</span>
                            </div>
                            <div class="author_sign text-ellip">个性签名呜呜呜呜呜呜</div>
                        </div>
                    </el-form-item>
                    <!--input输入-->
                    <el-form-item prop="code">
                        <el-input class="input" v-model="formData.code"
                                  placeholder="请输入提取码，不区分大小写"
                                  @keyup.enter="checkShare"
                                  maxlength="32"
                                  type="password"
                        ></el-input>
                    </el-form-item>
                    <!--button提取-->
                    <el-form-item>
                        <el-button
                            type="primary"
                            @click="checkShare"
                            style="width: 300px;height: 55px;border-radius: 28px;"
                            :disabled="loading"
                        >
                            <span  class="btn_span" v-if="!loading">提取文件</span>
                            <span  class="btn_span" v-else>提取中...</span>
                        </el-button>
                    </el-form-item>
                </el-form>
            </el-card>
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
.btn {
    margin-right: 10px;
}

.search-panel {
    margin-left: 10px;
    width: 300px;
}

.icon-refresh {
    cursor: pointer;
    margin-left: 10px;
}

.not-allow {
    background: #d2d2d2 !important;
    cursor: not-allowed;
}

.file-item {
    display: flex;
    align-items: center;
    padding: 6px 0px;
}

.author_sign {
    font-size: 16px;
    color: black;
    padding: 10px 0px;
}

.edit-panel {
    flex: 1;
    width: 0;
    display: flex;
    align-items: center;
    margin: 0px 5px;
}

.edit-panel .iconfont {
    margin-left: 10px;
    background: #0c95f7;
    color: #fff;
    padding: 3px 5px;
    border-radius: 5px;
    cursor: pointer;
}


.share {
    height: calc(100vh);
    background: url("../../assets/share_bg.png");
    background-repeat: repeat-x;
    background-position: 0 bottom;
    background-color: #eef2f6;
    display: flex;
    justify-content: center;
}

.share-info{
    width: 196px;
}
.body-content {

    margin-top: calc(100vh / 5);
    width: 500px;
}
.btn_span{
    color: #ffffff;font-weight: bold;font-size: 18px
}
.logo {
    display: flex;
    align-items: center;
    justify-content: center;
}

.icon-pan {
    font-size: 60px;
    color: #409eff;
}

.name {
    font-weight: bold;
    margin-left: 5px;
    font-size: 25px;
    color: #409eff;
}


.code-panel {
    margin-top: 20px;
    background: #fff;
    border-radius: 5px;
    overflow: hidden;
    box-shadow: 0 0 7px 1px #5757574f;
}

.file-info {
    padding: 10px 20px;
    background: #409eff;
    color: #fff;
    display: flex;
    align-items: center;
}

.avatar {
    margin-right: 5px;
}


.user-info {
    display: flex;
    align-items: center;
    padding: 10px 0px;
}

.nick-name {
    font-size: 24px;
    font-weight: 700;
    color: black;

}

.share-time {
    margin-left: 20px;
    font-size: 12px;
}


.file-name {
    margin-top: 10px;
    font-size: 12px;
}

.code-body {
    padding: 30px 20px 60px 20px;
}

.input-area {
    margin-top: 10px;
}

.input {
    flex: 1;
    margin-right: 10px;
}

:deep(.el-card){
    background-color: rgba(255,255,255,.4);
}
:deep(.el-card__header){
    padding: 10px 20px;
}

:deep(.el-form-item__content) {
    justify-content: center;
}
:deep(.el-input){
    flex:none;
    width: 300px;
    height: 55px;
    margin-right: 0px;
}
:deep(.el-input__wrapper){
    border-radius: 28px;
    background: rgba(0,0,0,.2);
}
:deep(.el-input__inner::placeholder) {
    font-size: 16px;
    color: #3A3D3F;
}
:deep(.el-input__inner){
    padding-left: 40px;
    font-size: 18px;
}
</style>