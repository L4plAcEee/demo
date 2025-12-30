import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import './assets/css/LoginView.css';

import  axios  from 'axios';
axios.interceptors.request.use(config=>{
  config.headers.token=sessionStorage.getItem("token")
  return config;
})


Vue.config.productionTip = false
Vue.use(ElementUI);
new Vue({
  router,
  render: h => h(App)
}).$mount('#app')

