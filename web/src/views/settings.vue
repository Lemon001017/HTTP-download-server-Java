<script setup>
import { ref } from 'vue';
import SideBar from '../components/SideBar.vue'
import backend from '../backend'


const taskNum = ref(1)
const speedNum = ref(1)
const downloadPath = ref('')

async function saveSetting() {
    await backend.post('/settings/save',{
        settingName: downloadPath.value,
        settingValue:1
    })

}


</script>
<template>
    <div>
        <el-header style="background-color: #000; color: #fff; text-align: left;width: 100%;">
            <h3 style="margin:0%;padding: 1rem;">Download Server</h3>
        </el-header>
        <div style="display: flex;">
            <SideBar />
            <div style="flex-grow: 1;margin-top: 40px;margin-left: 40px;">
                <el-col>
                    <el-row>
                        <h3>Download Path</h3>
                    </el-row>
                    <el-row style="margin-bottom: 30px;">
                        <el-input size="large" v-model="downloadPath" placeholder="/data/downloads"
                            style="width: 300px;"></el-input>
                    </el-row>
                    <el-row>
                        <div style="display: flex;">
                            <h3>Max Tasks</h3>
                            <span>Allow maximum task parallelism</span>
                        </div>
                    </el-row>
                    <el-row style="margin-bottom: 30px;">
                        <el-input-number v-model="taskNum" :min="1" :max="10" @change="handleChange" />
                    </el-row>
                    <el-row>
                        <div style="display: flex;">
                            <h3>Max Download Speed</h3>
                            <span>0 not limit</span>
                        </div>
                    </el-row>
                    <el-row style="margin-bottom: 30px;">
                        <div style="display: flex;">
                            <el-input-number v-model="speedNum" :min="1" :max="10" @change="handleChange" />
                            <span>MB/s</span>
                        </div>
                    </el-row>
                    <el-row>
                        <el-button type="primary" round @click="saveSetting">Save Setting</el-button>
                    </el-row>
                </el-col>
            </div>
            <router-view />
        </div>
    </div>
</template>

<style scoped>
.el-row {
    align-items: center;
    /* 垂直居中 */
}

.el-col {
    border-radius: 4px;
    text-align: center;
    line-height: 36px;
    height: 36px;
}

.el-input-number {
    margin-right: 10px;
}

.el-button {
    padding: 20px;
}

h3 {
    margin-right: 40px;
}
</style>