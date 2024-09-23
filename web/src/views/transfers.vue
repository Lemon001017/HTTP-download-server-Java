<script setup>
import { ref } from 'vue';

import SideBar from '../components/SideBar.vue'
import { ElMessage } from 'element-plus';

import backend from '../backend'

const mouseleave = false

const urlInput = ref('')
const value = ref('all')
const options = ref([
    { value: 'all', label: 'All' },
    { value: 'downloaded', label: 'Downloaded' },
    { value: 'pending', label: 'Pending' },
    { value: 'canceled', label: 'Canceled' },
    { value: 'downloading', label: 'Downloading' },
    { value: 'failed', label: 'Failed' }
])

const data = [
    { value: 'all', label: 'All' },
    { value: 'downloaded', label: 'Downloaded' },
    { value: 'pending', label: 'Pending' },
    { value: 'canceled', label: 'Canceled' },
    { value: 'downloading', label: 'Downloading' },
    { value: 'failed', label: 'Failed' }
]

const showErrorMessage = () => {
    ElMessage({
        showClose: true,
        message: '下载出现错误',
        type: 'error'

    })
}

defineExpose({ showErrorMessage })

const taskValue = ref([])
async function taskSubmit() {
    taskValue.value = await backend.post('/task/submit', {
        url: urlInput
    })

}


const selectedOptions = ref([])
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
                        <el-button type="primary" round>Submit</el-button>
                    </div>
                </el-col>
                <el-col class="ml-[40px] mb-4">
                    <el-select v-model="value" placeholder="Select" style="width: 240px">
                        <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
                    </el-select>
                </el-col>
                <div class="flex justify-end w-[95%]">
                    <button type="button" class="hover:bg-blue-100 duration-200 rounded-full">
                        <img src="../assets/shuaxin.svg" alt="Button Image"
                            class="h-[30px] m-[8px]  hover:opacity-75 active:scale-75 transition-all ">
                    </button>
                    <button type="button" class="hover:bg-blue-100 duration-200 rounded-full">
                        <img src="../assets/kaishi.svg" alt="Button Image"
                            class="h-[30px] m-[8px]  hover:opacity-75 active:scale-75 transition-all ">
                    </button>
                    <button type="button" class="hover:bg-blue-100 duration-200 rounded-full">
                        <img src="../assets/shanchu.svg" alt="Button Image"
                            class="h-[34px] m-[6px]  hover:opacity-75 active:scale-75 transition-all ">
                    </button>
                </div>
                <div class="w-[95%] ml-[25px]">
                    <div v-for="(item, index) in data" :key="index" class="border-2 m-4 rounded-xl">
                        <div class="mx-2 my-4" style="display: flex;">
                            <input type="checkbox" :id="`option-${index}`" :value="item.value" v-model="selectedOptions"
                                class="p-2 w-[18px] mx-4">
                            <div class="m-4 flex flex-col flex-1 ">
                                <div class="flex justify-between ">
                                    <label class="w-[20vw] flex-none" :for="`option-${index}`">{{ item.label }}</label>
                                    <el-input-number v-model="taskNum" :min="1" :max="10" @change="handleChange" />
                                    <div class="flex">
                                        <button type="button" class="hover:bg-blue-100 duration-200 rounded-full">
                                            <img src="../assets/shuaxin.svg" alt="Button Image"
                                                class="h-[20px] m-[5px]  hover:opacity-75 active:scale-75 transition-all ">
                                        </button>
                                        <button type="button" class="hover:bg-blue-100 duration-200 rounded-full">
                                            <img src="../assets/kaishi.svg" alt="Button Image"
                                                class="h-[20px] m-[5px]  hover:opacity-75 active:scale-75 transition-all ">
                                        </button>
                                        <button type="button" class="hover:bg-blue-100 duration-200 rounded-full">
                                            <img src="../assets/shanchu.svg" alt="Button Image"
                                                class="h-[24px] m-[4px]  hover:opacity-75 active:scale-75 transition-all ">
                                        </button>
                                    </div>
                                </div>
                                <div class="mt-2">
                                    <el-progress :percentage="50"></el-progress>
                                </div>
                                <div class="flex justify-between">
                                    <p>{{ 1 }}MB/s / {{ 2 }}MB</p>
                                    <div class="flex">
                                        <img src="../assets/时间_.svg">
                                        <p>{{ 1 }}</p>
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
