/** vite的特殊性, 需要处理图片 */
export const require = (imgPath: string) => {
    try {
        const handlePath = imgPath.replace('@', '..');
        // https://vitejs.cn/guide/assets.html#the-public-directory
        return new URL(handlePath, import.meta.url).href;
    } catch (error) {
        console.warn(error);
    }
};

//转换单位7
const point=2
export const covertSize=(size:number)=>{
    let covertSize=''
    if(size<0.1*1024){      //小于0.1KB，转换为B
        covertSize=size.toFixed(point)+'B'
    }else if(size<0.1*1024*1024){  //小于0.1MB，转换为KB
        covertSize= (size/1024).toFixed(point)+'KB'
    }else if(size<0.1*1024*1024*1024){ //小于0.1GB，转换为MB
        covertSize= (size/1024/1024).toFixed(point)+'MB'
    }else{ //其他转换为GB
        covertSize= (size/1024/1024/1024).toFixed(point)+'GB'
    }
    let sizeStr=covertSize
    let index=sizeStr.indexOf('.')
    let dou=sizeStr.substring(index+1,index+3)     //取两位
    if(dou=='00'){  //删除00
        covertSize=sizeStr.substring(0,index)+sizeStr.substring(index+3,index+4)
    }
    return covertSize
}


const getFileSuffix=(fileName:string)=>{
    const index=fileName.lastIndexOf(".")
    if(index===-1){
        return ''
    }
    return fileName.substring(index)
}

import fileTypeMap from '../constant/fileType'
//根据后缀获取icon
export const getFileIcon=(fileName:string)=>{
    let iconName='other'
    const suffix=getFileSuffix(fileName)
    if(!suffix)return iconName
    for (const key in fileTypeMap) {
        if(fileTypeMap[key].indexOf(suffix)!==-1){
            iconName=key
            break
        }
    }
    console.log(iconName)
    return iconName
}
