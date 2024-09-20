<script setup>
import { ref } from 'vue';
import SideBar from '../components/SideBar.vue'
import backend from '../backend'


const value = ref('All')
const options = ref([
  { value: 'All', label: 'All(*.*)' },
  { value: 'Video', label: 'Video(*.mp4;*.mov)' },
  { value: 'Photo', label: 'Photo(*.png;*.jpg;*.gif)' },
  { value: 'Archive', label: 'Archive(*.zip;*.rar;*.tar)' },
  { value: 'Document', label: 'Document(*.pptx;*.docx;*.xlsx)' }
])

const tableData = [
  { fileName: '1.mp4', size: '1.2MB', createTime: '2023-03-01' },
  { fileName: '2.mp4', size: '1.5MB', createTime: '2023-03-03' },
  { fileName: '3.mp4', size: '1.7MB', createTime: '2023-03-02' },
]

async function saveSetting(val) {
  const data = await backend.post('/api/file/list', {
    type:val,
  })
}

</script>
<template>
  <div>
    <el-header class="bg-black text-white w-[100%] text-xl font-bold flex items-center">
      <h3 class="p-1 md:ml-[300px]">Download Server</h3>
    </el-header>
    <div class="flex mt-4 md:flex-row md:justify-between md:mx-[300px]">
      <SideBar />
      <div class="flex-1 mt-[20px]">
        <el-row justify="space-between">
          <el-col :span="4">
            /storage
          </el-col>
          <el-col :span="12">
            <el-select v-model="value" placeholder="Select" @change="saveSetting" class="w-[240px]">
              <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-col>
        </el-row>
        <div style="margin: 20px;">
          <el-table :data="tableData">
            <el-table-column prop="name" label="File name" sortable />
            <el-table-column prop="path" label="Path"  />
            <el-table-column prop="size" sortable label="Size" />
            <el-table-column prop="gmtModified" sortable label="Create time" />
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