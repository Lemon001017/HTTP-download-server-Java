<script setup>
import { ref, onMounted, watchEffect } from 'vue';
import { useRouter, onBeforeRouteUpdate } from 'vue-router';

const activeIndex = ref('/');

const router = useRouter();

onMounted(() => {
    const currentRoute = router.currentRoute.value.fullPath;
    activeIndex.value = currentRoute;
});

watchEffect(() => {
    const currentRoute = router.currentRoute.value.fullPath;
    activeIndex.value = currentRoute;
});
</script>

<template>
    <div>
        <el-menu :default-active="activeIndex" class="el-menu-vertical-demo" @open="handleOpen" @close="handleClose"
            router unique-opened active-text-color="#000">
            <el-menu-item index="/file">
                <el-icon>
                    <Folder />
                </el-icon>
                <template #title>
                    <span class="el-submenu">
                        File
                    </span>
                </template>
            </el-menu-item>
            <el-menu-item index="/transfers">
                <el-icon>
                    <Download />
                </el-icon>
                <template #title>
                    <span class="el-submenu">
                        Transfers
                    </span>
                </template>
            </el-menu-item>
            <el-menu-item index="/settings">
                <el-icon>
                    <Setting />
                </el-icon>
                <template #title>
                    <span class="el-submenu">
                        Settings
                    </span>
                </template>
            </el-menu-item>
        </el-menu>
        <router-view />
    </div>
</template>

<style>
.el-menu-vertical-demo:not(.el-menu--collapse) {
    width: 160px;
    min-height: 400px;
    text-align: left;
    margin-left: 30px;
    margin-top: 30px;
}

li.el-menu-item {
    margin-bottom: 5px;
    height: 40px;
    padding: 8px 16px;
}

.el-submenu {
    font-size: 16px;
    font-weight: bold;
}

.el-menu-item.is-active {

    border-radius: 10px 0px 0px 10px;
    background-color: #facc15;
}
</style>