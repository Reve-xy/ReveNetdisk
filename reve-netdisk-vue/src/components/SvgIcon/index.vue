<!--
* @author Rêve
* @date 2023/5/21 16:19
* @description 
-->
<template>
    <svg :class="svgClass" v-bind="$attrs" :style="{ color: color }">
        <use :xlink:href="iconName"></use>
    </svg>
</template>

<script setup lang="ts">
import { computed, defineProps } from 'vue'
const props = defineProps({
    iconClass: {
        type: String,
        required: true
    },
    color: {
        type: String,
        default: ''
    }
})
const iconName = computed(() => `#icon-${ props.iconClass }`)
const svgClass = computed(() => {
    if(props.iconClass) return `svg-icon icon-${ props.iconClass }`
    return 'svg-icon'
})
</script>

<style scoped>
.svg-icon{width: 1em;height: 1em;fill:currentColor; vertical-align: middle;}
</style>
