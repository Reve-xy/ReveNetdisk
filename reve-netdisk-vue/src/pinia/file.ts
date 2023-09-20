import {defineStore} from "pinia";
import {STATUS} from "@/constant/fileStatus";

/**
 * @author Rêve
 * @date 2023/8/14 17:00
 * @description
 */

export const fileStore = defineStore('fileStore', {
    state: () => {
        return {
            showUploader: false,
            fileList: [],
        }
    },
    getters: {
        getShowUploader: (state) => state.showUploader,
        getFileList: (state) => state.fileList,
    },
    actions: {
        addFile(fileInfo: any) {
            if(!this.showUploader){
                this.showUploader=!this.showUploader
            }
            const {file,filePid} = fileInfo;

            this.fileList.push(fileItem)
        }
    }
})