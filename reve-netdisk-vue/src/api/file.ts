import request from '@/utils/request'
import downloadService from "@/utils/downloadRequest";

const prefixUrl='/file'
// 加载文件列表
export function loadFileList(params) {
    return request({
        url: `${prefixUrl}/listFile`,
        method: 'get',
        params
    })
}

// 新建目录
export function createFolder(data) {
    return request({
        url: `${prefixUrl}/folder`,
        method: 'post',
        data
    })
}

export function renameFile(data) {
    return request({
        url: `${prefixUrl}/rename`,
        method: 'put',
        data
    })
}

// 获取用户详细信息
export function checkFileUploadedByMD5(data) {
    return request({
        url: `${prefixUrl}/multipart/check`,
        method: 'post',
        data
    })
}

// 初始化获取上传url
export function initUpload(data) {
    return request({
        url: `${prefixUrl}/initMultipart`,
        method: 'post',
        data
    })
}

// 合并分片
export function mergeMultipartUpload(data) {
    return request({
        url: `${prefixUrl}/mergeMultipartUpload`,
        method: 'post',
        data
    })
}

//获取面包屑导航
export function getFolderNavigation(params){
    return request({
        url: `${prefixUrl}/getFolderNavigation`,
        method:'get',
        params
    })
}

//删除文件
export function delFile(data){
    return request({
        url: `${prefixUrl}/deleteFiles`,
        method:'delete',
        data
    })
}

export function getAllFolder(params){
    return request({
        url: `${prefixUrl}/getAllFolder`,
        method:'get',
        params
    })
}

export function moveFile(data){
    return request({
        url: `${prefixUrl}/moveFile`,
        method:'put',
        data
    })
}

export function download(params){
    return downloadService({
        url: `${prefixUrl}/downloadFile`,
        method:'get',
        params
    })
}