import { createApp } from 'vue'
import App from './App.vue'

import 'normalize.css/normalize.css'
import '@/styles/index.scss'

import router from "./router/index";
import '@/router/config'

import { createPinia } from 'pinia'
import plugins from '_p/'
//svg,导入Svg图片插件，可以在页面上显示Svg图片
import 'virtual:svg-icons-register';
import SvgIcon from '_c/SvgIcon/index.vue'
import Table from '_c/Table/index.vue'

const app=createApp(App)
app.component('svg-icon',SvgIcon).component('Table',Table)
app.use(plugins).use(router).use(createPinia())

app.mount('#app')
