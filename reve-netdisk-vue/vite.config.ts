import {defineConfig, loadEnv} from 'vite'
import vue from '@vitejs/plugin-vue'
import {resolve} from 'path'

//svg
import {createSvgIconsPlugin} from 'vite-plugin-svg-icons';

// 自动导入vue中hook reactive ref等
import AutoImport from "unplugin-auto-import/vite"
//自动导入ui-组件 比如说ant-design-vue  element-plus等
import Components from 'unplugin-vue-components/vite';
//element
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers'


export default defineConfig(({command, mode}) => {
    const env = loadEnv(mode, process.cwd(), '')
    return {
        plugins: [
            vue(),
            //element自动导入
            AutoImport({
                //安装两行后你会发现在组件中不用再导入ref，reactive等
                imports: ['vue', 'vue-router'],
                // dts: "src/auto-import.d.ts",
                //element
                resolvers: [ElementPlusResolver()],
            }),
            Components({
                //element
                resolvers: [ElementPlusResolver()],
                //默认存放位置
                // dts: "src/components.d.ts",
            }),
            createSvgIconsPlugin({
                // 指定需要缓存的图标文件夹
                iconDirs: [resolve(process.cwd(), 'src/assets/icon/svg')],
                // 指定symbolId格式
                symbolId: 'icon-[dir]-[name]',
            }),
        ],
        // loadEnv 和 envPrefix; define可以设置常量，官方解释
        envPrefix: ['VITE', 'VUE'], // 环境变量前缀,默认只会暴露VITE开头变量，定义后可暴露VUE开头变量
        //   define: {
        //       'process.env.VITE_APP_BASE_API':JSON.stringify(env.VITE_APP_BASE_API)
        //     },
        resolve: {
            alias: {
                '@': resolve(__dirname, './src'),
                _v: resolve(__dirname, './src/views'),
                _c: resolve(__dirname, './src/components'),
                _a: resolve(__dirname, './src/assets'),
                _r: resolve(__dirname, './src/request'),
                _p: resolve(__dirname, './src/plugins'),
                _public: resolve(__dirname, './public')
            }
        },
        server: {
            port: 8080,
            open: env.VITE_OPEN,
            proxy: {
                '/api': {
                    target: env.VITE_BASE_URL_REAR,
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/api/, '')
                }
            }
        }
    }
})