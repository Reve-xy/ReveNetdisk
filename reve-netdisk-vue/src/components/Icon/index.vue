<!--
* @author Rêve
* @date 2023/7/10 19:46
* @description 
-->
<script setup lang="ts">

import {defineProps} from "vue";
import {require} from "@/utils/common";

const props=defineProps({
    fileType:{
        type:Number
    },
    iconName:{
        type:String
    },
    cover:{
        type:String
    },
    width:{
        type:Number,
        default:32
    },
    fit:{
        type:String,
        default:'cover'
    }
})

const fileTypeMap={
    0:{desc:"目录",icon:'folder'},
    1:{desc:"视频",icon:'video'},
    2:{desc:"音频",icon:'music'},
    3:{desc:"图片",icon:'image'},
    4:{desc:"PDF",icon:'pdf'},
    5:{desc:"Excel",icon:'excel'},
    6:{desc:"Word",icon:'word'},
    7:{desc:"PPT",icon:'ppt'},
    8:{desc:"文本文档",icon:'txt'},
    9:{desc:"program",icon:'program'},
    10:{desc:"可执行文件",icon:'exec'},
    11:{desc:"Zip",icon:'zip'},
    12:{desc:"其他",icon:'other'},
}
const getImage=()=>{
    if(props.cover){
        return props.cover
    }
    let icon=''
    if(props.iconName){
        icon=props.iconName
    }else{
        const iconMap=fileTypeMap[props.fileType]
        if(iconMap){
            icon=iconMap['icon']
        }
    }
    return require(`@/assets/icon-image/${icon}.png`)
}
</script>

<template>
    <span :style="{width:width+'px',height:width+'px'}" class="icon">
        <img :src="getImage()" :style="{'object-fit':fit}"/>
    </span>
</template>

<style lang="scss" scoped>
.icon{
    text-align: center;
    display: inline-block;
    border-radius: 3px;
    img{
        width: 100%;
        height: 100%;
    }
}
</style>