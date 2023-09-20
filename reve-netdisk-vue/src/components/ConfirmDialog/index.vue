<!--
* @author Rêve
* @date 2023/8/27 18:22
* @description 封装dialog
-->
<script setup lang="ts">
const props = defineProps({
    title: {
        type: String,
    },
    dialogVisible: {
        type: Boolean,
        default: false,
    },
    showClose: {
        type: Boolean,
        default: true,
    },
    showCancel: {
        type: Boolean,
        default: true,
    },
    top: {
        type: Number,
        default: 0,
    },
    width: {
        type: String,
        default: "30%",
    },
    buttons: {
        type: Array,
    },
    draggable:{
        type: Boolean,
        default: false,
    },
    footAlign:{
        type:String,
        default:'center'
    },
    escClose:{
        type:Boolean,
        default:true
    },
    btnHeight:{
        type:Number,
        default:36
    },
    btnWidth:{
        type:Number,
        default:128
    }
});
const emit = defineEmits();
const close = (isCancelBtn) => {
    emit("closeDialog",isCancelBtn);
};
</script>

<template>
    <!--不加div，无法覆盖el-dialog，因为append-to-body-->
    <div>
        <el-dialog
            :model-value="dialogVisible"
            :title="title"
            :show-close="showClose"
            :draggable="draggable"
            :top="top + 'px'"
            :width="width"
            :close-on-press-escape="escClose"
            align-center
            @close="close"
            >
            <div >
                <slot ></slot>
            </div>
            <template v-if="(buttons && buttons.length > 0) || showCancel">
                <div class="dialog-footer"  :style="{ 'text-align': footAlign}">
                    <!--true代表按下了取消按钮-->
                    <el-button @click="close(true)" v-if="showCancel" round color="#f2faff"
                               :style="{width:btnWidth+'px',height:btnHeight+'px'}"
                    >
                        <span style="color: #06a7ff;font-weight: bold">取消</span>
                    </el-button>
                    <el-button
                        v-for="btn in buttons"
                        :type="btn.type || 'primary'"
                        @click="btn.click"
                        round
                        :disabled="btn.disabled||false"
                        :style="{width:btnWidth+'px',height:btnHeight+'px'}"
                    >
                        <span style="color: #ffffff;font-weight: bold">{{ btn.text }}</span>
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<style lang="scss" scoped>
:deep(.el-dialog) {
    border-radius: 10px;
}
:deep(.el-dialog__header) {
    border-bottom: 1px solid #f2f6fd;
}
:deep(.el-dialog__header .el-dialog__title) {
    font-size: 14px;
    font-weight: bold;
}

:deep(.el-dialog__body) {
    padding: 0px 0px;
}

.dialog-footer{
    padding: 24px 12px;

}
</style>