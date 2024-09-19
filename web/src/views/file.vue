<script setup>
import { ref } from 'vue';
import SideBar from '../components/SideBar.vue'
import backend from '../backend'


const value = ref('选项1')
const options = ref([
  { value: '选项1', label: 'All(*.*)' },
  { value: '选项2', label: 'Video(*.mp4;*.mov)' },
  { value: '选项3', label: 'Photo(*.png;*.jpg;*.gif)' },
  { value: '选项4', label: 'Archive(*.zip;*.rar;*.tar)' },
  { value: '选项5', label: 'Document(*.pptx;*.docx;*.xlsx)' }
])

const tableData = [
  { fileName: '1.mp4', size: '1.2MB', createTime: '2023-03-01' },
  { fileName: '2.mp4', size: '1.5MB', createTime: '2023-03-03' },
  { fileName: '3.mp4', size: '1.7MB', createTime: '2023-03-02' },
]

</script>
<template>
  <div>
    <el-header style="background-color: #000; color: #fff; text-align: left;width: 100%;">
      <h3 style="margin:0%;padding: 1rem;">Download Server</h3>
    </el-header>
    <div class="flex md:flex-row md:justify-between md:mx-[200px]">
      <SideBar />
      <div class="flex-1 mt-[20px]">
        <el-row justify="space-between">
          <el-col :span="8">
            香菜说这是文件路径
          </el-col>
          <el-col :span="12">
            <el-select v-model="value" placeholder="Select" style="width: 240px">
              <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-col>
        </el-row>
        <div style="margin: 20px;">
          <el-table :data="tableData" >
            <el-table-column prop="fileName" label="File name" sortable  />
            <el-table-column prop="size" label="Size" sortable  />
            <el-table-column prop="createTime" sortable label="Create time" />
          </el-table>
        </div>
      </div>

      <router-view />
    </div>
  </div>
</template>

<style scoped>
.el-row {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  /* 垂直居中 */
}

.el-col {
  border-radius: 4px;
  text-align: center;
  line-height: 36px;
  height: 36px;
}
</style>