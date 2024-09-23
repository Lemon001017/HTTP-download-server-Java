<script setup>
import { onMounted, ref, computed } from 'vue';
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

// const tableData = [
//   {
//     "name": "1-10.mp4",
//     "path": "\\storage\\1-10.mp4",
//     "size": 32003339,
//     "gmtModified": "2024-09-18T14:52:31.026+00:00"
//   },
//   {
//     "name": "PixPin_2024-08-22_19-11-22.png",
//     "path": "\\storage\\PixPin_2024-08-22_19-11-22.png",
//     "size": 15997,
//     "gmtModified": "2024-08-22T11:11:23.778+00:00"
//   },
//   {
//     "name": "b_54f53e1c231f2713ed264effe7a1b68b.jpg",
//     "path": "\\storage\\b_54f53e1c231f2713ed264effe7a1b68b.jpg",
//     "size": 41590,
//     "gmtModified": "2024-08-22T07:53:28.126+00:00"
//   }
// ]

const sortParams = ref({})
const handleSortChange = (column) => {
  const { prop, order } = column;
  if (order === null) {
    sortParams.value = {};
  } else {
    sortParams.value = {
      prop,
      order: order === 'ascending' ? 'up' : 'down'
    };
  }
  saveSetting(value.value)
}

const data = ref([])
const formattedTableData = ref([]);
const formatDate = (isoTimestamp) => {
  const date = new Date(isoTimestamp);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};


async function saveSetting(val) {
  data.value = await backend.post('/api/file/list', {
    type: val,
    sort: sortParams?.value.prop,
    order: sortParams?.value.order,
  })
  data.value.data.forEach(item => {
    if (item.size < 1024) item.size = item.size + 'B'
    if (item.size < 1024 * 1024) item.size = (item.size / 1024).toFixed(2) + 'KB'
    else item.size = (item.size / (1024 * 1024)).toFixed(2) + 'MB'
  })

  console.log(data.value)
}

onMounted(() => {
  saveSetting(value.value)
})


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
            \storage
          </el-col>
          <el-col :span="12">
            <el-select v-model="value" placeholder="Select" @change="saveSetting" class="w-[240px]">
              <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-col>
        </el-row>
        <div style="margin: 20px;">
          <el-table :data="data.data" @sort-change="handleSortChange">
            <el-table-column prop="name" label="File name" sortable="custom" />
            <el-table-column prop="path" label="Path" />
            <el-table-column prop="size" width="150" sortable="custom" label="Size" />
            <el-table-column prop="gmtCreated" width="200" sortable="custom" label="Create time">
              <template #default="{ row }">
                {{ formatDate(row.gmtModified) }}
              </template>
            </el-table-column>
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