<!--
* @author Rêve
* @date 2023/8/14 17:31
* @description 
-->
<script setup lang="ts">
import {covertSize,getFileIcon} from "@/utils/common";
import {STATUS} from "@/constant/fileStatus";
import SparkMD5 from "spark-md5";
import {checkFileUploadedByMD5, initUpload, mergeMultipartUpload} from "@/api/file";
import axios from "axios";
import {defineEmits} from "vue";

const $modal = inject("$modal")
let flow_progress=ref(false)
//分片大小
const chunkSize = parseInt(import.meta.env.VITE_CHUNK_SIZE) * 1024 * 1024
const fileList = ref([])
const uploadFile = async (file, filePid) => {
    const fileItem = {
        uploadId: '',
        // 文件，文件大小，文件流。文件名
        file: file,
        uid: file.uid,
        icon:getFileIcon(file.name),
        //计算md5进度
        md5Progress: 0,
        md5: null,
        fileName: file.name,
        // 状态
        status: STATUS.init.value,
        //    已经上传大小
        uploadSize: 0,
        //    总大小
        totalSize: file.size,
        //    上传进度
        uploadProgress: 0,
        //    上传速度
        uploadSpeed: 0,
        //    暂停
        pause: false,
        //删除
        del: false,
        //    当前分片
        chunkIndex: 1,
        //    父级id
        filePid: filePid,
        uploadedChunkList: [],
        chunkList: [],
        errMsg: ''
    }
    fileList.value.push(fileItem)
    if (fileItem.totalSize == 0) {
        fileItem.status = STATUS.emptyFile.value
        return
    }
    //1.获取md5
    await getFileMd5(fileItem.uid)
    if (!fileItem.md5) {
        console.log('md5为空')
        return
    }
    let res;
    //2.判断文件是否已经上传过
    try {
        res = await checkFileUploadedByMD5({
            fileMd5: fileItem.md5,
            fileName: fileItem.file.name,
            fileSize: fileItem.totalSize,
            filePid: fileItem.filePid,
        })
        if (res && res.status) {
            const currentFile=getFileByUid(fileItem.uid)
            currentFile.status = STATUS[res.status].value
            if (!res.uploadId) { //秒传
                console.log('秒传')
                currentFile.uploadProgress = 100
                return
            } else if (res.uploadId && res.chunkList.length > 0) {
                currentFile.uploadId = res.uploadId
                currentFile.uploadedChunkList = res.chunkList
                console.log(`uploadId=${currentFile.uploadId},已经上传的分片列表=${currentFile.uploadedChunkList}`)
                //不用设置uploading状态，在md5解析完成后已经设置过
            } else {
                console.error("检查文件返回值出错")
            }
        } else {
            console.log('未上传')
        }
        handlerUpload(fileItem.uid)
    } catch (err) {
        console.log(err)
    }
}
defineExpose({uploadFile});

//获取md5
const getFileMd5 = (uid) => {
    const currentFile: any = getFileByUid(uid)
    const file = currentFile.file
    const blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice
    const fileReader = new FileReader()
    // 计算分片数
    const totalChunks = Math.ceil(file.size / chunkSize)
    console.log('总分片数：' + totalChunks)
    let currentChunk = 0
    const spark = new SparkMD5.ArrayBuffer()
    const loadNext = () => {
        const start = currentChunk * chunkSize
        const end = ((start + chunkSize) >= file.size) ? file.size : start + chunkSize
        // 注意这里的 fileRaw
        fileReader.readAsArrayBuffer(blobSlice.call(file, start, end))
    }
    loadNext()
    return new Promise((resolve, reject) => {
        fileReader.onload = function (e) {
            try {
                spark.append(e.target.result)
            } catch (error) {
                console.log('获取Md5错误：' + currentChunk)
            }
            if (currentChunk < totalChunks) {
                console.log(`第${file.name},${currentChunk}解析完成`)
                currentChunk++
                currentFile.md5Progress = Math.floor((currentChunk / totalChunks) * 100)
                loadNext()
            } else {
                currentFile.md5 = spark.end()
                spark.destroy()
                currentFile.md5Progress = 100
                currentFile.status = STATUS.uploading.value
                resolve()
            }
        }
        fileReader.onerror = function () {
            console.error('读取Md5失败，文件读取错误')
            $modal.msgError('读取Md5失败，文件读取错误')
            currentFile.md5Progress = -1
            currentFile.status = STATUS.uploadFailed.value
            resolve()
        }
    }).catch(err => {
        console.log(err)
    })
}

//根据uid定位当前文件
const getFileByUid = (uid) => {
    const file = fileList.value.find((item: any) => {
        return item.uid === uid
    })
    return file
}

const emits = defineEmits(['uploadedCallback'])
const handlerUpload = async (uid) => {
    const currentFile: any = getFileByUid(uid)


          //3.获取分片
        const currentFileChunks = createFileChunks(currentFile.file)
        const totalChunks: Number = currentFileChunks.length

        let data = {
            uploadId: currentFile.uploadId,
            filePid: currentFile.filePid,
            fileName: currentFile.fileName,
            fileSize: currentFile.totalSize,
            chunkIndex: currentFile.chunkIndex,
            chunks: totalChunks,
            fileMd5: currentFile.md5,
            chunkUploadedList: currentFile.chunkUploadedList//已上传的分片索引+1
        }
        // 4.获取上传地址
        const res = await initUpload(data)
        if (res && !res.uploadId && !res.urlList) {
            $modal.msgError('上传地址获取错误')
            console.log('上传地址获取错误')
            return
        }
        //加入chunkList
        currentFileChunks.map((chunkItem, index) => {
            //未上传过的分片，依次加入到chunkList中
            if(currentFile.uploadedChunkList&&currentFile.uploadedChunkList.indexOf(index+1)==-1){
                currentFile.chunkList.push({
                    chunkNumber: index + 1,
                    chunk: chunkItem,
                    uploadUrl: res.urlList[index]
                })
            }
        })
        //更新chunk位置
        currentFile.chunkIndex=currentFile.uploadedChunkList?currentFile.uploadedChunkList.length+1:1
        //存入uploadId
        currentFile.uploadId = res.uploadId

    try {
        flow_progress.value=true
        //5.上传分片
        const chunkList = await uploadChunkBase(currentFile)
        if (chunkList.length > 0) {
            console.log("未上传完成，不进行分片")
            return
        }

        console.log('分片上传完成')
        //6.合并
        await mergeMultipartUpload({
            uploadId: currentFile.uploadId,
            fileMd5: currentFile.md5,
            filePid: currentFile.filePid,
        })
        flow_progress.value=false
        currentFile.uploadProgress = 100
        currentFile.status = STATUS.uploadFinished.value
        //刷新列表
        emits('uploadedCallback')
    } catch (err) {
        console.error(err)
    }

}

const createFileChunks = (file: any) => {
    const fileChunkList = []
    let count = 0
    while (count < file.size) {
        fileChunkList.push({
            file: file.slice(count, count + chunkSize),
        })
        count += chunkSize
    }
    return fileChunkList
}

const createChunksMap=()=>{

}

const uploadChunkBase = (currentFile) => {
    let chunkList: Array<Object> = currentFile.chunkList
    let totalChunks = chunkList.length
    const fileSize = currentFile.totalSize
    return new Promise((resolve, reject) => {
        const handler = () => {
            if (chunkList.length && !currentFile.pause && !currentFile.del) {
                const chunkItem: any = chunkList.shift()
                // 直接上传二进制，不需要构造 FormData，否则上传后文件损坏
                axios.put(chunkItem.uploadUrl, chunkItem.chunk.file, {
                    // 上传进度处理
                    onUploadProgress: (event) => {
                        let loaded = event.loaded;
                        if (loaded > fileSize) {
                            loaded = fileSize;
                        }
                        currentFile.uploadSize = (currentFile.chunkIndex - 1) * chunkSize + loaded;
                        currentFile.uploadProgress = Math.floor(
                            (currentFile.uploadSize / fileSize) * 100
                        );
                    },
                    headers: {
                        'Content-Type': 'application/octet-stream'
                    }
                }).then(response => {
                    if (response.status === 200) {
                        console.log('分片：' + chunkItem.chunkNumber + ' 上传成功')
                        ++currentFile.chunkIndex
                        // 继续上传下一个分片
                        handler()
                    } else {
                        console.log('上传失败：' + response.status + '，' + response.statusText)
                    }
                })
                    .catch(error => {
                        // 更新状态
                        console.log('分片：' + chunkItem.chunkNumber + ' 上传失败，' + error)
                        // 重新添加到队列中
                        chunkList.push(chunkItem)
                        handler()
                    })
            } else {
                resolve(chunkList)
            }
        }
        handler()
        /*  // 并发
          for (let i = 0; i < this.simultaneousUploads; i++) {
              handler()
          }*/
    })
}

//继续上传
const startUpload = (uid) => {
    const fileItem = getFileByUid(uid)
    fileItem.pause = false
    handlerUpload(fileItem.uid)
}

//暂停上传
const pauseUpload = (uid) => {
    getFileByUid(uid).pause = true
}
const delUpload = (uid, index) => {
    // getFileByUid(uid).pause=true，先暂停后会动画浮现图标
    getFileByUid(uid).del = true
    fileList.value.splice(index, 1)
}
</script>

<template>
    <div class="upload-panel">
        <div class="upload-title">
            <span>上传任务</span>
            <span class="tips">（仅展示本次上传任务）</span>
        </div>
        <el-scrollbar max-height="400px">
        <div class="file-list">
            <div class="file-item" v-for="(item,index) in fileList" :key="index">
                <div style="margin-right: 5px">
                    <icon :icon-name="item.icon" :width="40"/>
                </div>
                <div class="file-panel">
                    <div class="file-name">
                        {{ item.fileName }}
                    </div>
                    <div class="progress">
                        <el-progress
                            :percentage="item.uploadProgress"
                            :striped="flow_progress"
                            :striped-flow="flow_progress"
                            v-if="
                                  item.status==STATUS.uploading.value||
                                  item.status==STATUS.uploadSeconds.value||
                                  item.status==STATUS.uploadFinished.value
                                 "
                        >
                        </el-progress>
                    </div>
                    <div class="upload-status">
                        <!--  图标  -->
                        <span
                            :class="['iconfont icon-'+STATUS[item.status].icon]"
                            :style="{color:STATUS[item.status].color}"
                        />
                        <!--  状态描述  -->
                        <span class="status" :style="{color:STATUS[item.status].color}">
                            {{
                                item.status === 'fail' ? item.errMsg : STATUS[item.status].desc
                            }}
                        </span>
                        <!--  上传中  -->
                        <span
                            class="upload-info"
                            v-if="item.status===STATUS.uploading.value">
                            {{ covertSize(item.uploadSize) }}/{{ covertSize(item.totalSize) }}
                        </span>
                    </div>
                </div>
                <div class="op">
                    <!--  MD5  -->
                    <el-progress
                        type="circle"
                        :width="50"
                        :percentage="item.md5Progress"
                        v-if="item.status===STATUS.init.value"
                    />
                    <div class="op-btn">
                            <span v-if="item.status===STATUS.uploading.value">
                                <Icon
                                    :width="28"
                                    class="btn-item"
                                    icon-name="upload"
                                    v-if="item.pause"
                                    title="上传"
                                    @click="startUpload(item.uid)"
                                />
                                 <Icon
                                     :width="28"
                                     class="btn-item"
                                     icon-name="pause"
                                     v-else
                                     title="暂停"
                                     @click="pauseUpload(item.uid)"
                                 />
                                 <Icon
                                     :width="28"
                                     class="del btn-item"
                                     icon-name="del"
                                     v-if="
                                         item.status !==  STATUS.init.value &&
                                         item.status !== STATUS.uploadFinished.value&&
                                         item.status!=STATUS.uploadSeconds.value

"
                                     title="删除"
                                     @click="delUpload(item.uid,index)"
                                 />
                            </span>
                                <Icon
                                    :width="28"
                                    class="del btn-item"
                                    icon-name="del"
                                    title="清除"
                                    v-if="
                                         item.status == STATUS.uploadFinished.value||
                                         item.status==STATUS.uploadSeconds.value
"
                            @click="delUpload(item.uid,index)"
                        />
                    </div>
                </div>
            </div>
            <div v-if="fileList.length===0" class="nodata">
                <no-data msg="暂无上传任务"/>
            </div>

    </div>
        </el-scrollbar>
    </div>

</template>

<style lang="scss" scoped>
.upload-panel {
    .upload-title {
        border-bottom: 1px solid #f2f6fd;
        line-height: 40px;
        padding: 0px 10px;
        font-size: 15px;

        .tips {
            font-size: 13px;
            color: rgb(169, 169, 169);
        }
    }

    .file-list {
        height: 400px;
        .file-item {
            position: relative;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 10px 0px;
            background-color:   #fff;
            border-bottom: 1px solid #f2f6fd;
        }

       /* .file-item:nth-child(even) {
            background-color: #fcf8f4;
        }*/

        .file-panel {
            flex: 1;
            //display: flex;

            .file-name {
                color: rgb(64, 62, 62);
            }

            .upload-status {
                display: flex;
                align-items: center;
                margin-top: 10px;

                .iconfont {
                    margin-right: 3px;
                }

                .status {
                    color: red;
                    font-size: 13px;
                }

                .upload-info {
                    margin-left: 5px;
                    font-size: 12px;
                    color: rgb(112, 111, 111);
                }
            }

            .progress {
                height: 10px;
                margin-top: 5px;
            }
        }

        .op {
            width: 100px;
            display: flex;
            align-items: center;
            justify-content: center;

            .op-btn {
                .btn-item {
                    cursor: pointer;
                }

                .del,
                .clean {
                    margin-left: 12px;
                }
            }
        }
    }
}
:deep(.progress .el-progress__text) {
    min-width: 0px !important;
    margin-left: 10px;
}
:deep(.progress .el-progress-bar__inner) {
    background-color: #10b8ff;
}
.nodata{
    padding-top: 70px;
}
</style>