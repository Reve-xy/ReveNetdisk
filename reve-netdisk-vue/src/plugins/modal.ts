const system_info: string = '系统提示'
let loadingInstance: any;
export default {
    // 消息提示
    msg(content: string) {
        ElMessage.info(content)
    },
    msgError(content: string) {
        ElMessage.error(content)
    },
    msgSuccess(content: string) {
        ElMessage.success(content)
    },
    msgWarning(content: string) {
        ElMessage.warning(content)
    },
    //弹框
    alert(content: string) {
        ElMessageBox.alert(content, system_info)
    },
    alertError(content: string) {
        ElMessageBox.alert(content, system_info,{type:'error'})
    },
    alertSuccess(content: string) {
        ElMessageBox.alert(content, system_info,{type:'success'})
    },
    alertWarning(content: string) {
        ElMessageBox.alert(content, system_info,{type:'warning'})
    },
    // 信息提示
    notify(content: string) {
        ElNotification(content)
    },
    notifyError(content: string) {
        ElNotification.error(content)
    },
    notifySuccess(content: string) {
        ElNotification.success(content)
    },
    notifyWarning(content: string) {
        ElNotification.warning(content)
    },
    // 全局刷新
    loading(content:string){
        loadingInstance=ElLoading.service({
            lock: true,
            text: content,
            background: 'rgba(0, 0, 0, 0.7)',
        })
    },
    closeLoading(){
        loadingInstance.close()
    }
}
