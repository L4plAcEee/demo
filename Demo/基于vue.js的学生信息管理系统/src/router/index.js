import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/log' // 访问根路径时重定向到 /log
  },
  {
    path:'/log',
    name:'login',
    component: ()=>import('../views/auth/LoginView.vue'),
    meta: { title: '登录' }
  },  
  {
    path:'/reg',
    name:'register',
    component: ()=>import('../views/auth/RegisterView.vue'),
    meta: { title: '注册' }
  },
  {
    path:'/find',
    name:'find',
    component: ()=>import('../views/auth/FindView.vue'),
    meta: { title: '找回密码' }
  },
  {
    path: '/emp',
    name: 'emp',
    component: ()=>import('../views/tlias/EmpView.vue')
  },
  {
    path: '/dept',
    name: 'dept',
    component: () => import('../views/tlias/deptView.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/ClC',
    name: 'ClC',
    component: () => import('../views/tlias/ClassComponent.vue'),
    meta: { title: '首页' }
  }
]

const router = new VueRouter({
  mode: 'history', // 设置为 history 模式
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title;
  }
  next();
});

export default router
