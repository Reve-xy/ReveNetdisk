/**
 * @author Rêve
 * @date 2023/8/30 16:28
 * @description
 */
import request from "@/utils/request";
const prefixUrl='/recycle'

export function loadRecycleList(params){
    return request({
        url: `${prefixUrl}/loadRecycleList`,
        method:'get',
        params,
    })
}

export function recoverFile(data){
    return request({
        url: `${prefixUrl}/revertFile`,
        method:'post',
        data,
    })
}
export function deleteRecycleFile(data){
    return request({
        url: `${prefixUrl}/deleteRecycleFile`,
        method:'delete',
        data,
    })
}