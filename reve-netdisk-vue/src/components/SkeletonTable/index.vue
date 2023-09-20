<!--
* @author Rêve
* @date 2023/8/28 16:16
* @description 
-->
<script setup lang="ts">
import {defineProps} from "vue";

const props=defineProps({
    tableHeight:{
        type: Number,
        default: 500
    },
})
const skeletonTbData=[{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}]
</script>

<template>
    <el-table :data="skeletonTbData" :border="false" :height="props.tableHeight" @mousewheel.prevent>
        <el-table-column align="left" :prop="fileName" width="40">
            <template #header>
                <el-skeleton-item variant="text" style="width:100%;" />
            </template>
        </el-table-column>
        <el-table-column align="left" :prop="fileName">
            <template #header>
                <el-skeleton-item variant="text" style="width: 30px;height:20px;" />
            </template>
            <template #default="scope">
                <el-skeleton-item variant="text" style="width: 30px;height: 30px;" />
                <el-skeleton-item variant="text" style="width: 50%;height:20px;margin-left: 20px;" />
            </template>
        </el-table-column>
        <el-table-column align="left" width="200" :prop="updateTime">
            <template #header>
                <el-skeleton-item variant="text" style="width: 40px;height:20px;" />
            </template>
            <template #default="scope">
                <el-skeleton-item variant="text" style="width: 80px;height:20px;" />
            </template>
        </el-table-column>
        <el-table-column align="left" width="200" :prop="fileSize">
            <template #header>
                <el-skeleton-item variant="text" style="width: 40px;height:20px ;" />
            </template>
            <template #default="scope">
                <el-skeleton-item variant="text" style="width: 40px;height:20px;" />
            </template>
        </el-table-column>
    </el-table>
</template>

<style lang="scss" scoped>
//此处都是skeleton必须用的属性
:deep(.el-skeleton__item){
    vertical-align: middle;
}
//隐藏滚动条
:deep(.el-scrollbar__bar.is-vertical){
    width: 0 !important;
}
//el-table 取消鼠标悬浮时行高亮效果
:deep(.el-table__row) {
    pointer-events: none;
}
</style>