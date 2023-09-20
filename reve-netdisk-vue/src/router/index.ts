import {createRouter, createWebHistory} from "vue-router";

const routes =
    [
        {
            path: '',
            name: 'Home',
            component: () => import('@/layout/index.vue'),
            redirect:'/main/all',
            meta: {
                title:
                    '开源网盘 | 多种存储方式 | 在线预览编辑 | 分片、断点续传 | 文件分享',
                content: {
                    description:
                        '基于 Spring Boot + Vue 3.0 框架开发的 Web 文件系统，旨在为用户提供一个简单、方便的文件存储方案'
                }
            },
            children: [
                {
                    path: '/main/:category',
                    name: '首页',
                    meta: {
                        auth: true,
                        menuCode: 'main',
                    },
                    component:()=>import('@/views/main/index.vue')
                },
                {
                    path: '/myshare',
                    name:'我的分享记录',
                    meta: {
                        auth: true,
                        menuCode: 'share',
                    },
                    component:()=>import('_v/share/index.vue')
                },
                {
                    path: '/recycle',
                    name: '回收站',
                    meta: {
                        auth: true,
                        menuCode: 'recycle',
                    },
                    component:()=>import('@/views/recycle/index.vue')
                },
            ]
        },
        {
            path: '/login',
            name: 'Login',
            meta:{
                title: '登录',
                auth:false
            },
            component: () => import('_v/user/login/index.vue'),
        },
        {
            path: '/shareCheck/:shareId',
            name: '分享校验',
            meta:{
                title: '分享校验',
                auth:false
            },
            component: () => import("_v/webShare/ShareCheck.vue")
        },
        {
            path: '/share/:shareId',
            name: '分享',
            meta:{
                title: '分享',
                auth:false
            },
            component: () => import("_v/webShare/index.vue")
        },

        {
            path:'/404',
            name: '404',
            meta:{
                title: '404',
                auth:false
            },
            component: () => import('@/views/error/404.vue')
        },
        /*{
            path:'/:pathMatch(.*)*',
            redirect: '/404',
            name: '404',
        },*/
    ]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router