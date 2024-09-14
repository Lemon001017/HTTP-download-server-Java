import { createRouter, createWebHistory } from 'vue-router';
import HelloWorld from "./views/HelloWorld.vue"
import file from "./views/file.vue"
import settings from "./views/settings.vue"
import transfers from "./views/transfers.vue"

const routes = [
    {
        path: '/',
        name:'Home',
        component:HelloWorld,
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