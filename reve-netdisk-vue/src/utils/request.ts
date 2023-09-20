import axios from 'axios'
import {getToken} from "./auth";
import errorCode from "./errorCode";
import {userStore} from "@/pinia";

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'

// 创建axios实例
const service: any = axios.create({
    // axios中请求配置有baseURL选项，表示请求URL公共部分
    baseURL: '/api',
    // 超时
    timeout: 10000
})

//在外初始化会在pinia挂载前初始化，无法使用
let store
service.interceptors.request.use((config: any) => {
     store= userStore();
    // 是否需要设置 token
    const isToken:boolean = (config.headers || {}).isToken === false
    if (getToken() && !isToken) {
        config.headers['Authorization'] = getToken() // 让每个请求携带自定义token 请根据实际情况自行修改
    }
    // get请求映射params参数
    if (config.method === 'get' && config.params) {
        let url = config.url + '?'
        for (const propName of Object.keys(config.params)) {
            const value = config.params[propName]
            var part = encodeURIComponent(propName) + '='
            if (value !== null && typeof (value) !== 'undefined') {
                if (typeof value === 'object') {
                    for (const key of Object.keys(value)) {
                        if (value[key] !== null && typeof (value[key]) !== 'undefined') {
                            const params = propName + '[' + key + ']'
                            const subPart = encodeURIComponent(params) + '='
                            url += subPart + encodeURIComponent(value[key]) + '&'
                        }
                    }
                } else {
                    url += part + encodeURIComponent(value) + '&'
                }
            }
        }
        url = url.slice(0, -1)
        config.params = {}
        config.url = url
    }
    return config
}, error => {
    console.log(error)
    Promise.reject(error)
})

import errorShareValidCode from "@/utils/errorShareValidCode";

// 响应拦截器
service.interceptors.response.use(  (res:any) => {
        // 未设置状态码则默认成功状态
        const code = res.data.code || 200
        // 获取错误信息
        const msg = errorCode[code] || res.data.msg || errorCode['default']
        // 二进制数据则直接返回
        if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
            return res.data
        }
        if (code.toString().startsWith("6")) {
            return res.data
        }
        //分享页响应码需返回resolve自行处理
        if(Object.values(errorShareValidCode).includes(code)){
            return res.data
        }
        if (code === 401) {
            ElMessageBox.confirm('登录状态已过期，您可以继续留在该页面，或者重新登录', '系统提示', {
                    confirmButtonText: '重新登录',
                    cancelButtonText: '取消',
                    type: 'warning'
                }
            ).then(() => {
                 store.FedLogOut()
            }).catch(() => {
            })
            return Promise.reject('无效的会话，或者会话已过期，请重新登录。')
        } else if (code === 500) {
            ElMessage({
                message: msg,
                type: 'error'
            })
            return Promise.reject(new Error(msg))
        } else if (code !== 200) {
            ElNotification.error({
                title: msg
            })
            return Promise.reject(msg)
        } else {
            if(res.data.data){
                if (res.data.data.total) {
                    res.data.data.total = parseInt(res.data.data.total)
                }
                return res.data.data
            }else{
                return res.data
            }
        }
    },
    error => {
        console.log('err' + error)
        let {message} = error
        if (message === 'Network Error') {
            message = '后端接口连接异常'
        } else if (message.includes('timeout')) {
            message = '系统接口请求超时'
        } else if (message.includes('Request failed with status code')) {
            message = '系统接口' + message.substr(message.length - 3) + '异常'
        }
        ElMessage({
            message: message,
            type: 'error',
            duration: 5 * 1000
        })
        return Promise.reject(error)
    })

export default service