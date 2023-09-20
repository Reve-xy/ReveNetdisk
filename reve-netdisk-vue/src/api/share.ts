/**
 * @author Rêve
 * @date 2023/9/5 17:16
 * @description
 */
import request from "@/utils/request";
const prefixUrl='/share'
export function listShareFile(params){
    return request({
        url: `${prefixUrl}/listShareFile`,
        method:'get',
        params
    })
}

export function addShareFile(data){
    return request({
        url: `${prefixUrl}/shareFile`,
        method:'post',
        data
    })
}

export function delShareFile(data){
    return request({
        url: `${prefixUrl}/delShareFile`,
        method:'delete',
        data
    })
}

/**
 * 获取外链信息，封面信息
 * @param params
 */
export function getWebShareInfo(params){
    return request({
        url: `${prefixUrl}/getShareInfo`,
        method:'get',
        params
    })
}

export function validShare(params){
    return request({
        url: `${prefixUrl}/getShareValidInfo`,
        method:'get',
        params
    })
}

/**
 * 验证提取码
 * @param data
 */
export function checkShareCode(data){
    return request({
        url: `${prefixUrl}/checkShareCode`,
        method:'post',
        data
    })
}


/**
 * 验证提取码
 * @param data
 */
export function getShareFileList(params){
    return request({
        url: `${prefixUrl}/loadShareFileList`,
        method:'get',
        params
    })
}

/**
 * 保存到网盘
 * @param data
 */
export function saveShare(data){
    return request({
        url: `${prefixUrl}/saveShare`,
        method:'post',
        data
    })
}


