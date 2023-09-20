<!--
* @author Rêve
* @date 2023/5/21 16:00
* @description 
-->
<script setup lang="ts">
import {getCaptcha} from "@/api/login";
import {userStore} from "@/pinia";
import type {FormInstance,FormRules} from 'element-plus'
import Cookies from "js-cookie";
import {encrypt, decrypt} from '@/utils/jsencrypt'

const $modal = inject('$modal')
const router = useRouter()
const route=useRoute()
const store = userStore()

interface LoginForm {
    username: string,
    password: string,
    uuid: string,
    captcha: string,
    rememberMe: boolean
}

//表单配置
let errorLogin = ref<String>()
let errorCaptcha= ref<String>()
let loginForm =reactive<LoginForm>({
    username: '',
    password: '',
    uuid: '',
    captcha: '',
    rememberMe: false
})

//查看密码
let passwordType = ref('password')
const showPwd = () => {
    if (passwordType.value === 'password') {
        passwordType.value = 'text'
    } else {
        passwordType.value = 'password'
    }
}

//获取缓存的cookie
const getCookie = () => {
    const username = Cookies.get("username");
    const password = Cookies.get("password");
    const rememberMe = Cookies.get('rememberMe')
    loginForm.username = username === undefined ? loginForm.username : username
    loginForm.password = password === undefined ? loginForm.password : decrypt(password)
    loginForm.rememberMe = rememberMe === undefined ? false : Boolean(rememberMe)
}

//获取验证码
let captchaImg = ref('')
let captchaEnabled = ref(false)
const getCaptchaImg = async () => {
    try {
        const res: any = await getCaptcha()
        captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled;
        if (captchaEnabled) {
            captchaImg.value = res.captcha
            loginForm.uuid = res.uuid
        }
    } catch (err) {
        $modal.msgError("获取验证码失败")
    }
}

let redirect:string
watch(()=>route.query,(newVal:any)=>{
    redirect=newVal.redirect
},{immediate:true})

//登录相关
const loginRule = reactive<FormRules>({
    username: [
        {trigger: 'blur', required: true, message: '请输入用户名'},
        // {pattern:'^[a-zA-Z0-9_-]{5,19}$', message: '用户名格式不正确，包含非法字符', trigger: 'blur'}
    ],
    password: [
        {trigger: 'blur', required: true, message: '请输入密码'},
    ],
    captcha: [
        {require: true, trigger: 'blur', message: '请输入验证码'}
    ]
})
let loading = ref<Boolean>(false)
const loginFormRef = ref<FormInstance>()
const handleLogin = (formEl: FormInstance | undefined) => {
    if (!formEl) return
    formEl.validate(async vaild => {
        if (vaild) {
            loading.value = true;
            errorLogin.value = ''
            errorCaptcha.value = ''
            if (loginForm.rememberMe) {
                Cookies.set("username", loginForm.username, {expires: 30});
                Cookies.set("password", encrypt(loginForm.password), {expires: 30});
                Cookies.set('rememberMe', loginForm.rememberMe, {expires: 30});
            } else {
                Cookies.remove("username");
                Cookies.remove("password");
                Cookies.remove('rememberMe');
            }
            try {
                const res: object = await store.login(loginForm)
                if (res.code === 61) {
                    errorLogin.value = res.msg
                    loading.value = false
                    return
                } else if (res.code === 62) {
                    errorCaptcha.value = res.msg
                    loading.value = false
                    return
                }
                router.push(redirect||'/').catch(() => {
                });
            } catch (err) {
                console.log(err)
                loading.value = false;
                await getCaptchaImg();
            }
        }
    })
}

getCookie()
getCaptchaImg()
</script>

<template>
    <div class="login">
        <!-- status-icon，展示校验状态 -->
        <el-form :model="loginForm" ref="loginFormRef" class="login-form" :rules="loginRule">
            <h3 class="title">登 录</h3>
            <el-form-item prop="username" :error="errorLogin">
                <el-input
                    type="text"
                    v-model="loginForm.username"
                    placeholder="账号"
                    maxlength="25"
                    autocomplete="on"
                >
                    <template #prefix>
                        <svg-icon icon-class="user"/>
                    </template>
                </el-input>
            </el-form-item>
            <el-form-item prop="password">
                <el-input
                    :type="passwordType"
                    v-model="loginForm.password"
                    placeholder="密码"
                    @keyup.enter.native="handleLogin(loginFormRef)"
                    autocomplete="off"
                >
                    <template #prefix>
                        <svg-icon icon-class="password"/>
                    </template>
                    <template #suffix>
                        <svg-icon
                            :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'"
                            class="pointer-select"
                            @click="showPwd"
                        />
                    </template>
                </el-input>
            </el-form-item>
            <el-form-item prop="captcha" v-if="captchaEnabled" :error="errorCaptcha">
                <el-input
                    type="text"
                    v-model="loginForm.captcha"
                    placeholder="验证码"
                    :clearable="true"
                    maxlength="8"
                    style="width:65%; float: left;"
                    @keyup.enter.native="handleLogin(loginFormRef)"
                >
                    <template #prefix>
                        <svg-icon icon-class="validCode"/>
                    </template>
                </el-input>
                <div class="login-code">
                    <img :src="captchaImg" @click="getCaptcha" class="login-code-img"/>
                </div>
            </el-form-item>
            <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
            <el-form-item>
                <el-button
                    :loading="loading"
                    type="primary"
                    @click="handleLogin(loginFormRef)"
                    style="width: 100%"
                    :disable="loading"
                >
                    <span v-if="!loading">登 录</span>
                    <span v-else>登 录 中...</span>
                </el-button>
                <div style="float: right;">
                    <router-link class="link-type" :to="'/register'">立即注册</router-link>
                </div>
            </el-form-item>
        </el-form>
        <!--  底部  -->
        <div class="el-login-footer">
            <span>Copyright © 2020-2023 @Rêve All Rights Reserved.</span>
        </div>
    </div>
</template>

<style lang="scss">

$light_gray: #fff;
$cursor: #fff;
$dark_gray: #889aa4;
$light_gray: #eee;
.login {
    display: flex;
    justify-content: right;
    align-items: center;
    height: 100%;
    background: url("../../../assets/images/login-background.jpg");
    background-size: cover;
}

.title {
    text-align: center;
    color: #707070;
}

/*内边距撑开白色框*/
.login-form {
    border-radius: 6px;
    background: #ffffff;
    width: 400px;
    padding: 25px 25px 5px 25px;
    margin-right: 80px;

    .el-input {
        height: 38px;

        input {
            height: 38px;
        }
    }
}

.login-code {
    float: left;
    margin-left: 8px;
    border-radius: 4px;
    height: 38px;

    img {
        cursor: pointer;
        vertical-align: middle;
    }
}

//底部显示
.el-login-footer {
    height: 40px;
    line-height: 40px;
    position: fixed;
    bottom: 0;
    width: 100%;
    text-align: center;
    color: #fff;
    font-family: Arial;
    font-size: 12px;
    letter-spacing: 1px;
}

.pointer-select {
    cursor: pointer;
    user-select: none;
}

.login-code-img {
    height: 38px;
}

</style>
