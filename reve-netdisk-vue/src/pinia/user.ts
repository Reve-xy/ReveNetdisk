import {defineStore} from "pinia";
import {getToken, removeToken, setToken} from "@/utils/auth";
import {getInfo, getUserSpace, login} from "@/api/login";
import {require} from "@/utils/common";
import router from "@/router";

//pinia函数式编程，需要return
export const userStore=defineStore('userStore',{
    state:()=>{
     return {
         name: "",
         token: "",
         userId:"",
         sex:0,
         useSpace:0,
         totalSpace:0,
         avatar:'',
     }
    },
    getters: {
        // 用户姓名
        getName: (state) => state.name,
        // 用户头像
        getAvatar: (state) =>state.avatar,
        getUseSpace: (state) => state.useSpace,
        getTotalSpace: (state) => state.totalSpace,
        getUserId:(state) => state.userId
    },
    actions:{
        async login(userInfo:any){
            const username:string = userInfo.username.trim()
            const password:string = userInfo.password
            const captcha:string = userInfo.captcha
            const uuid:string = userInfo.uuid
            const res:any = await login(username, password, captcha, uuid)
            if(res.token){
                setToken(res.token)
                this.token=res.token
            }
            return res;
        },
        async getInfo(){
            const res:any=await getInfo()
            this.name=res.nickName
            this.avatar=res.avatar||require('@/assets/images/avatar.gif')
            this.sex=res.sex
            this.userId=res.userId
        },
        async getUserSpace(){
            try {
                const res:any=await getUserSpace()
                this.useSpace=parseInt(res.useSpace)
                this.totalSpace=parseInt(res.totalSpace)
            }catch (err){
                console.error(err)
            }
        },
        async FedLogOut(){
            removeToken()
            router.push('/login')
        }
    }
})

