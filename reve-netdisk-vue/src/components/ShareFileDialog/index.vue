<!--
* @author Rêve
* @date 2023/9/5 17:34
* @description 
-->
<script setup lang="ts">
import useClipboard from "vue-clipboard3";
const { toClipboard } = useClipboard();
import { useRouter } from "vue-router";
import {onlyNumAndLetter} from "@/utils/regexValue";
import {addShareFile} from "@/api/share";

const $modal = inject('$modal')
const loading=ref<Boolean>(false)
const router = useRouter()
const shareUrl = ref(document.location.origin + "/share/");

//创建与复制的表单dom
const showType = ref(0);
const formData = ref({
    //默认选择七天与系统随机提取码
    expireType: 1,
    isDiy: false,
    autoFillPwd:true
});

let dialogBtnFunc=ref();
const formDataRef = ref(null);
const dialogConfig = ref({
    dialogVisible: false,
    title: `分享文件(夹):`,
    showCancel:false,
    btnWidth:128,
    buttons: [
        {
            type: "primary",
            text: "创建链接",
            click: dialogBtnFunc
        },
    ],
});

const successContent=ref()

//    展示分享对话框
const showShareDialog = (data) => {
    dialogConfig.value.title=`分享文件(夹):${data.fileName}`
    dialogConfig.value.dialogVisible = true;
    formData.value = Object.assign({
        //默认选择七天与系统随机提取码
        expireType: 1,
        isDiy: false,
        autoFillPwd:true
    },
    data
    )
    showType.value = 0;
    resultInfo.value = {};
    dialogConfig.value.buttons[0].text = "创建链接";
    dialogBtnFunc.value=()=>{share()}
    dialogConfig.value.btnWidth=128
};
//外部调用
defineExpose({ showShareDialog });

//封装返回结果对象
const resultInfo = ref({});
const share = async () => {
    if(formData.value.isDiy&&!new RegExp(onlyNumAndLetter).test(formData.value.code)){
        $modal.msgError("自定义提取码仅可输入五位数字和字母")
        return
    }
    try {
        loading.value=true
        const res=await addShareFile(formData.value)
        loading.value=false
        $modal.msgSuccess('创建成功')
        //切换表单
        if(formData.value.autoFillPwd){
            successContent.value="成功创建分享链接，访问者无需提取码可直接查看分享文件"
        }else{
            successContent.value="成功创建私密链接"
        }
        showType.value = 1;
        resultInfo.value = res;
        dialogConfig.value.buttons[0].text = "复制链接及提取码";
        dialogBtnFunc.value=()=>{copy()}
        dialogConfig.value.btnWidth=185
    }catch (err){
        console.log(err)
    }finally {
        loading.value=false
    }

}

const copy = async () => {
    const fillPwdContent:string=formData.value.autoFillPwd?`?pwd=${resultInfo.value.code}`:''
    await toClipboard(
        `链接:${shareUrl.value}${resultInfo.value.shareId}${fillPwdContent} 提取码: ${resultInfo.value.code}`
    );
    $modal.msgSuccess("复制成功");
};
</script>

<template>
    <confirm-dialog
                    :dialogVisible="dialogConfig.dialogVisible"
                    :title="dialogConfig.title"
                    :buttons="dialogConfig.buttons"
                    width="600px"
                    :showCancel="dialogConfig.showCancel"
                    :btn-width="dialogConfig.btnWidth"
                    @close-dialog="dialogConfig.dialogVisible = false"
    >
                <template v-if="showType == 0">
                    <div  class="form-code"  v-loading="loading" element-loading-text="正在创建分享链接...">
                    <el-form
                        :model="formData"
                        ref="formDataRef"
                        label-width="180px"
                        hide-required-asterisk
                    >
                    <el-form-item label="有效期：" prop="expireType">
                        <el-radio-group v-model="formData.expireType">
                            <el-radio :label="0">1天</el-radio>
                            <el-radio :label="1">7天</el-radio>
                            <el-radio :label="2">30天</el-radio>
                            <el-radio :label="3">永久有效</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item label="提取码：" prop="isDiy">
                        <el-radio-group v-model="formData.isDiy" class="code_radio_group">
                            <el-radio :label="false">系统随机生成提取码</el-radio>
                            <el-radio :label="true">
                                <el-input
                                          clearable
                                          placeholder="请输入5位提取码"
                                          :disabled="!formData.isDiy"
                                          v-model.trim="formData.code" maxLength="8"
                                          :style="{ width: '130px' }"
                                >

                                </el-input>
                            </el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item>
                        <el-checkbox v-model="formData.autoFillPwd">分享链接自动填充提取码</el-checkbox>
                        <el-tooltip placement="right" >
                            <template #content> 开启后分享链接会带上提取码，<br/>用户点击链接可自动填充提取码。 </template>
                            <i class="iconfont icon-outline" style="margin-left: 6px"/>
                        </el-tooltip>
                    </el-form-item>
            </el-form>
                    </div>
        </template>
                <template v-else>
                    <div style="padding: 0px 24px">
                        <div style="margin-top: 18px">
                         <span style="color:#06a7ff;font-weight: bold">
                           <sapn style="font-size: 16px"><i class="iconfont icon-success"/></sapn>
                           <span style="margin-left: 4px">{{successContent}}</span>
                       </span>
                        </div>
                        <div style="margin-top: 18px">
                            <label>分享链接：</label>
                            {{ shareUrl }}{{ resultInfo.shareId }}
                        </div>
                        <div style="margin-top: 18px">
                            <label>提取码：</label>
                            {{ resultInfo.code }}
                        </div>
                        <div style="margin-top: 18px">
                           <span style="font-size: 12px;color:#4f526c;line-height: 17px" v-if="resultInfo.expireType!=-1">
                                链接将于<span style="color:#06a7ff">{{ resultInfo.expireType }}天</span>后失效</span>
                            <span style="font-size: 12px;color:#4f526c;line-height: 17px" v-else>
                                <span style="color:#06a7ff">永久有效</span></span>
                         </div>
                    </div>
                    </template>
    </confirm-dialog>
</template>

<style lang="scss" scoped>
.form-code{
    padding-top: 24px;
}
:deep(.el-radio-group) {
      display: block;

}
:deep(.code_radio_group .el-radio) {
    display: block;
    margin-right: 0px;
    margin-bottom:10px;
}
:deep(.el-form-item__label){
    font-weight: 700;
}
</style>