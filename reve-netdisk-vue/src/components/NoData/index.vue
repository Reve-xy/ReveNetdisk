<!--
* @author Rêve
* @date 2023/8/14 18:37
* @description 
-->
<script setup lang="ts">
import {defineProps} from "vue";
defineProps({
    msg:{
        type:String
    }
})
</script>

<template>
<div class="no-data">
    <div>
        <Icon icon-name="no_data" :width="120" fit="fill"/>
    </div>
    <div class="msg">{{msg}}</div>
</div>
</template>

<style lang="scss" scoped>
.no-data{
    text-align: center;
    padding: 10px 0px;
    .icon-empty{
        font-size: 50px;
        color:#bbb;
    }
    .msg{
        margin-top: 10px;
        color:#909399;
        font-size: 14px;
    }
}
</style>