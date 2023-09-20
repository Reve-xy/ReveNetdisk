<!--
* @author Rêve
* @date 2023/9/6 13:23
* @description 
-->
<script setup lang="ts">
import {defineExpose, defineProps} from "vue";

const props=defineProps({
    fileNameTitle:{
        type:Object,
        default:{}
    },
    detailsObj:{
        type:Array,
        default:[]
    }
})
const dialogConfig = ref({
    dialogVisible: false,
    title: `分享详情`,
    showCancel:false,
});
const showShareFileDetails=()=>{
    dialogConfig.value.dialogVisible=true;
}
defineExpose({showShareFileDetails})
</script>

<template>
    <confirm-dialog
        :dialogVisible="dialogConfig.dialogVisible"
        :title="dialogConfig.title"
        :buttons="dialogConfig.buttons"
        width="600px"
        :showCancel="dialogConfig.showCancel"
        @close-dialog="dialogConfig.dialogVisible = false"
    >
        <div class="file-name-title text-ellip">
            <icon file-type="fileNameTitle.fileType"/>
            <span>{{ fileNameTitle.fileName }}</span>
        </div>
        <div v-if="detailsObj.length>0">
            <template v-for="(item,index) in detailsObj">
                <div class="details-item">
                    <div class="details-label">item.label</div>
                    <div class="details-value">item.value</div>
                </div>
            </template>
        </div>
    </confirm-dialog>
</template>

<style lang="scss" scoped>
.file-name-title{
    padding: 15px 0 7px;
    width: 100%;
    font-size: 14px;

}
.details-item{
    padding: 7px 0;
    line-height: 20px;
    .details-label{
        color: #878c9c;
    }
    .details-value{
        color: #03081a;
    }
}
</style>