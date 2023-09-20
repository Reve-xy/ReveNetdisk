<!--
* @author Rêve
* @date 2023/5/23 17:08
* @description 仅用于对普通表格进行使用
* 配置具体
**-tableData:表格数据
**-showPagination:是否展示分页
**-showPageSize:分页组件layout是否可以自由切换pagesize
**-queryForms:查询表单
**-config:表格配置项（stripe斑马纹,border纵向边框，showSelection展示多选框,showIndex展示序号，tableHeight表格高度。extHeight）
**-columns:子项配置(必选--*>prop数据绑定，label表头，可选--*>align单元格对齐方式，width宽度。fixed是否固定列宽度，sortable是否可排序，
scopedSlots表示自定义渲染或操作)
**-getTableData:回调函数，用于生成数据源的table数据
**-initTableData:回调函数，是否初始化
**-rowSelected:回调函数，选中状态发生变化时的回调函数
**-rowClick:回调函数，用于点击行
**-rowDbClick:回调函数，用于双击击行
**-setRowSelect:回调函数，用于设置行选中
-->
<script setup lang="ts">
const emits = defineEmits(['rowSelected', 'rowClick'])
const props = defineProps({
    tableData: Object,
    showPagination: {
        type: Boolean,
        default: true
    },
    showPageSize: {
        type: Boolean,
        default: true
    },
    total:{
        type:Number,
        default:0
    },
    queryForms:{
        type:Object,
        default:{
            pageSize:10,
            pageNum:1
        }
    },
    //表格配置项
    config: {
        type: Object,
        required:true,
        default: {
            tableHeight:600,
            showIndex: false,
            showSelection: true,
            stripe:false
        }
    },
    //表格子项配置
    columns:Array,
    //获取表格数据
    getTableData:{
        type:Function,
        required:true
    },
    //是否需要第一次就请求表格数据
    initTableData:{
        type:Boolean,
        default:false
    },
})

const layout=computed(()=>{
    return `total,${props.showPageSize?"size":""},prev,pager,next,jumper`
})

const init=()=>{
    if(props.initTableData&&props.getTableData){
        props.getTableData()
    }
}
init()

let dataTable=ref()
const clearSelection=()=>{
    dataTable.value.clearSelection()
}
const setCurrentRow=(rowKey,rowValue)=> {
    let row = props.tableData.list.find(item => {
        return item[rowKey] === rowValue
    })
    dataTable.value.setCurrentRow(row)
}
//暴露当前行与清楚选中的方法

const handleRowClick=(row)=>{
    emits("rowClick",row)
}

const handleRowDbClick=(row)=>{
    emits("rowDbClick",row)
}

const handleSelectionChange=(row)=>{
    emits('rowSelected',row)
}

const setRowSelect=(row)=>{
    if(row.selected){
        dataTable.value.toggleRowSelection(row,false)
        row.selected=false
    }else{
        dataTable.value.toggleRowSelection(row,true)
        row.selected=true
    }
}

const setSelectToRow=(row,b)=>{
    dataTable.value.toggleRowSelection(row,b)
}

defineExpose({setCurrentRow,clearSelection,setRowSelect,setSelectToRow})

const handleSizeChange=(size)=>{
    props.queryForms.pageSize=size
    props.queryForms.pageSize=1
    props.getTableData()
}

const handleCurrentChange=(pageNum)=>{
    props.queryForms.pageNum=pageNum
    props.getTableData()
}
</script>

<template>
    <div class="reve_table">
        <el-table
            ref="dataTable"
            :data="tableData"
            :height="props.config.tableHeight"
            :stripe="props.config.stripe"
            :border="config.border"
            highlight-current-row
            @row-click="handleRowClick"
            @selection-change="handleSelectionChange"
            @row-dblclick="handleRowDbClick"
        >
            <!--selection-->
            <el-table-column
                v-if="config.showSelection"
                type="selection"
                width="50"
                align="center"
            >
            </el-table-column>
            <!--序号-->
            <el-table-column
                v-if="config.showIndex"
                label="序号"
                type="index"
                width="50"
                align="center"
            >
            </el-table-column>

            <template v-for="(column,index) in columns">
                    <el-table-column
                        :prop="column.prop"
                        :label="column.label"
                        :align="column.align||'left'"
                        :width="column.width"
                        :fixed="column.fixed"
                        :sortable="column.sortable"
                    >
                        <template #header v-if="column.customHeader">
                            <slot
                            :name="column.customHeader"
                            >
                            </slot>
                        </template>
                        <template #default="scope" v-if="column.scopedSlots">
                            <slot
                                :name="column.scopedSlots"
                                :index="scope.$index"
                                :row="scope.row"
                            ></slot>
                        </template>
                    </el-table-column>
            </template>
        </el-table>

        <div class="pagination" v-if="showPagination">
            <el-pagination
                v-if="total"
                background
                :total="total"
                :page-sizes="[10,30,50,100]"
                :page-size="queryForms.pageSize"
                :current-page="queryForms.pageNum"
                :layout="layout"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                style="text-align: right"
            >
            </el-pagination>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.pagination{
    padding-top: 10px;
    padding-right: 10px;
}
.el-pagination{
    justify-content: right;
}
:deep el-table_cell{
    padding: 4px 0px;
}
</style>