<script setup>
import { onMounted, ref } from 'vue';

import SideBar from '../components/SideBar.vue'
import { ElMessage } from 'element-plus';

import backend from '../backend'
import { id, it } from 'element-plus/es/locale/index.mjs';

const BASE_URL = 'http://localhost:8080';

const mouseleave = false

const urlInput = ref('')
const optionsValue = ref('all')
const options = ref([
    { value: 'all', label: 'All' },
    { value: 'downloaded', label: 'Downloaded' },
    { value: 'pending', label: 'Pending' },
    { value: 'canceled', label: 'Canceled' },
    { value: 'downloading', label: 'Downloading' },
    { value: 'failed', label: 'Failed' }
])

const data = ref([])

const showErrorMessage = () => {
    ElMessage({
        showClose: true,
        message: '下载出现错误',
        type: 'error'

    })
}

defineExpose({ showErrorMessage })

let transfersSource = null

async function taskSubmit() {
    console.log(selectedOptions.value)


    const formData = new FormData();
    formData.append('url', urlInput.value);

    const resp = await fetch(BASE_URL + "/api/task/submit", {
        method: "POST",
        body: formData,

    })
    const data = await resp.json();
    const taskId = data.data;

    const eventUrl = `${BASE_URL}/api/event/${taskId}`;
    transfersSource = new EventSource(eventUrl);

    transfersSource.addEventListener('message', (event) => {
        const eventData = JSON.parse(event.data);
        console.log(eventData);
    });

    transfersSource.onerror = (error) => {
        console.error('Error in SSE:', error);
        transfersSource.close();
    };
}

const formatDate = (isoTimestamp) => {
    const date = new Date(isoTimestamp);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

async function getTaskList() {
    const formData = new FormData();
    formData.append('status', optionsValue.value);

    const response = await fetch(BASE_URL + "/api/task/list", {
        method: "POST",
        body: formData
    })
    const responseData = await response.json();
    data.value = responseData.data;
    data.value.forEach(item => {
        if (item.size < 1024) item.size = item.size + 'B'
        if (item.size < 1024 * 1024) item.size = (item.size / 1024).toFixed(2) + 'KB'
        else item.size = (item.size / (1024 * 1024)).toFixed(2) + 'MB'
    })
    console.log(data.value);
}
const selectedOptions = ref([])

async function resumeTasks(ids) {
    const formData = new FormData();
    let idsArray = Array.isArray(ids) ? ids : [ids];
    const requestBody = JSON.stringify(idsArray);
    await fetch(BASE_URL + '/api/task/resume', {
        method: "POST",
        headers:{
            'Content-Type': 'application/json'
        },
        body: requestBody
    })
    getTaskList();
}

async function restartTasks(ids) {
    const formData = new FormData();
    let idsArray = Array.isArray(ids) ? ids : [ids];
    const requestBody = JSON.stringify(idsArray);
    await fetch(BASE_URL + '/api/task/restart', {
        method: "POST",
        headers:{
            'Content-Type': 'application/json'
        },
        body: requestBody
    })
    getTaskList();
}

async function deleteTasks(ids) {
    const formData = new FormData();
    let idsArray = Array.isArray(ids) ? ids : [ids];
    const requestBody = JSON.stringify(idsArray);
    await fetch(BASE_URL + '/api/task/delete', {
        method: "POST",
        headers:{
            'Content-Type': 'application/json'
        },
        body: requestBody
    })
    getTaskList();
}

// async function resumeTasks(ids) {
//     let idsArray = Array.isArray(ids) ? ids : [ids];
//     await backend.post('/api/task/resume', {
//         ids: idsArray
//     })
// }
// async function restartTasks(ids) {
//     let idsArray = Array.isArray(ids) ? ids : [ids];
//     await backend.post('/api/task/restart', {
//         ids: idsArray
//     })
// }
// async function deleteTasks(ids) {
//     let idsArray = Array.isArray(ids) ? ids : [ids];
//     await backend.post('/api/task/delete', {
//         ids: idsArray
//     })
// }

onMounted(() => {
    getTaskList()
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
                <el-col class="flex justify-center align-center w-[100%]">
                    <div class="flex justify-center align-center w-[100%] mb-4 mx-10">
                        <el-input v-model="urlInput" size="large" class="flex-1" placeholder="Please input" />
                        <el-button type="primary" round @click="taskSubmit">Submit</el-button>
                    </div>
                </el-col>
                <el-col class="ml-[40px] mb-4">
                    <el-select v-model="optionsValue" placeholder="Select" class="w-[240px]" @change="getTaskList">
                        <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
                    </el-select>
                </el-col>
                <div class="flex justify-end w-[95%]">
                    <button type="button" class="hover:bg-blue-100 duration-200 rounded-full"  @click="resumeTasks(selectedOptions)">
                        <img src="../assets/shuaxin.svg" alt="Button Image"
                            class="h-[30px] m-[8px]  hover:opacity-75 active:scale-75 transition-all ">
                    </button>
                    <button type="button" class="hover:bg-blue-100 duration-200 rounded-full" @click="restartTasks(selectedOptions)">
                        <img src="../assets/kaishi.svg" alt="Button Image"
                            class="h-[30px] m-[8px]  hover:opacity-75 active:scale-75 transition-all ">
                    </button>
                    <button type="button" class="hover:bg-blue-100 duration-200 rounded-full" @click="deleteTasks(selectedOptions)">
                        <img src="../assets/shanchu.svg" alt="Button Image"
                            class="h-[34px] m-[6px]  hover:opacity-75 active:scale-75 transition-all ">
                    </button>
                </div>
                <div class="w-[95%] ml-[25px]">
                    <div v-for="(item, index) in data" :key="index" class="border-2 m-4 rounded-xl">
                        <div class="mx-2 my-2" style="display: flex;">
                            <input type="checkbox" :id="`option-${index}`" :value="item.id" v-model="selectedOptions"
                                class="p-2 w-[18px] mx-4">
                            <div class="m-4 flex flex-col flex-1 ">
                                <div class="flex justify-between ">
                                    <label class="w-[20vw] flex-none flex items-center" :for="`option-${index}`">{{
                                        item.name }}</label>
                                    <el-input-number v-model="item.threads" :min="1" :max="10" @change="handleChange" />
                                    <div class="flex">
                                        <button type="button" class="hover:bg-blue-100 duration-200 rounded-full"
                                            @click="resumeTasks(item.id)">
                                            <img src="../assets/shuaxin.svg" alt="Button Image"
                                                class="h-[20px] m-[5px]  hover:opacity-75 active:scale-75 transition-all ">
                                        </button>
                                        <button type="button" class="hover:bg-blue-100 duration-200 rounded-full"
                                            @click="restartTasks(item.id)">
                                            <img src="../assets/kaishi.svg" alt="Button Image"
                                                class="h-[20px] m-[5px]  hover:opacity-75 active:scale-75 transition-all ">
                                        </button>
                                        <button type="button" class="hover:bg-blue-100 duration-200 rounded-full"
                                            @click="deleteTasks(item.id)">
                                            <img src="../assets/shanchu.svg" alt="Button Image"
                                                class="h-[24px] m-[4px]  hover:opacity-75 active:scale-75 transition-all ">
                                        </button>
                                    </div>
                                </div>
                                <div class="mt-2">
                                    <el-progress :percentage="50"></el-progress>
                                </div>
                                <div class="flex justify-between">
                                    <p>{{ 1 }}MB/s / {{ item.size }}</p>
                                    <div class="flex m-2">
                                        <img src="../assets/时间_.svg" class="mx-1">
                                        <p>{{ formatDate(item.gmtCreated) }}</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <router-view />
        </div>
    </div>
</template>
<style scoped>
.el-row {
    width: 100%;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
}

.el-col {
    border-radius: 4px;
    text-align: center;
    display: flex;
}

.el-input {
    flex-grow: 1;
    margin-right: 40px;
}
</style>
