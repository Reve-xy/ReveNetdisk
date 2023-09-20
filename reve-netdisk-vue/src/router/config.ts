import router from './index'
import {userStore} from "@/pinia";
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import {getToken} from '@/utils/auth'
import {ElMessage} from "element-plus";

NProgress.configure({showSpinner: false})

/*const whiteList = ['/login', '/auth-redirect', '/bind', '/register',]
const anonymousList=['/shareCheck','/share']*/

router.beforeEach(async (to, from, next) => {
  let store=userStore()
  NProgress.start()
  if (getToken()) {
    /* has token*/
    if (to.path === '/login') {
      next({path: '/'})
      NProgress.done()
    } else {
      if (store.getName==undefined||store.getName=='') { // 判断当前用户是否已拉取完user_info信息
        try {
          await store.getInfo()
          await store.getUserSpace()
          next()
        } catch (err) {
          console.log(err)
          await store.FedLogOut()
          ElMessage.error(err)
          next({ path: '/' })
        }
      } else {
        next()
      }
    }
  } else {
    console.log(to.path)
    // 没有token
      if (!to.meta.auth) {
        // 在免登录白名单，直接进入
        next()
      } else {
        next(`/login?redirect=${to.fullPath}`) // 否则全部重定向到登录页
        NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
