<script setup>
import { ref } from 'vue';

import SideBar from '../components/SideBar.vue'
import { ElMessage } from 'element-plus';

import backend from '../backend'

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

defineExpose({showErrorMessage})

const taskValue = ref([])
async function taskSubmit() {
    taskValue.value = await backend.post('/task/submit',{
        url : urlInput
    })

}


const selectedOptions = ref([])
</script>
<template>
    <div>
        <el-header style="background-color: #000; color: #fff; text-align: left;width: 100%;">
            <h3 style="margin:0%;padding: 1rem;">Download Server</h3>
        </el-header>
        <div style="display: flex;">
            <SideBar />
            <div style="flex-grow: 1;margin-top: 40px;">
                <el-col class="flex justify-center align-center w-[80vw]">
                    <el-row justify="space-between">
                        <el-col :span="16">
                            <el-input v-model="urlInput" size="large" style="width: 100%;margin-left: 40px;"
                                placeholder="Please input" />
                        </el-col>
                        <el-col :span="8">
                            <el-button type="primary" round>Submit</el-button>
                        </el-col>
                    </el-row>
                </el-col>
                <el-col style="margin-left: 40px;">
                    <el-select v-model="value" placeholder="Select" style="width: 240px">
                        <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
                    </el-select>
                </el-col>
                <div class="w-[75vw] ml-[25px]">
                    <div v-for="(item, index) in data" :key="index" class="border-2 m-4">
                        <div class="m-4 w-[70vw]" style="display: flex;">
                            <input type="checkbox" :id="`option-${index}`" :value="item.value"
                                v-model="selectedOptions">
                            <div class="m-4 flex flex-col flex-1 ">
                                <div class="flex justify-between ">
                                    <label class="w-[20vw] flex-none" :for="`option-${index}`">{{ item.label }}</label>
                                    <el-input-number v-model="taskNum" :min="1" :max="10" @change="handleChange" />
                                    <div class="flex">
                                        <img src="../assets/shuaxin.svg" class="h-[16px] m-[5px]">
                                        <img src="../assets/kaishi.svg" class="h-[16px] m-[5px]">
                                        <img src="../assets/shanchu.svg" class="h-[16px] m-[5px]">
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
