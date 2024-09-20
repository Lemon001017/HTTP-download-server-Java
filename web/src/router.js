import { createRouter, createWebHistory } from 'vue-router';
import file from "./views/file.vue"
import settings from "./views/settings.vue"
import transfers from "./views/transfers.vue"

const routes = [
    {
        path: '/', 
        redirect: '/file'
    },
    {
        path: '/file',
        name:'File',
        component:file,
    },
    {
        path: '/settings',
        name:'Settings',
        component:settings,
    },
    {
        path: '/transfers',
        name:'Transfers',
        component:transfers,
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes 
});

export default router;