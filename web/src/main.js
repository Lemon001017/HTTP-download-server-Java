import { createApp } from 'vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css';
import './main.css'; // 引入 Tailwind CSS



import './style.css'
import App from './App.vue'

const app = createApp(App);



app.use(router).use(ElementPlus).mount('#app')
