<template>
    <div class="box">
      <div class="left"></div>
      <div class="right">
        <h4> 注  册 </h4>
        <form @submit.prevent="handleSubmit">
          <input class="acc" type="text" v-model="username" placeholder="用户名" />
          <input class="acc" type="password" v-model="password" placeholder="密码" />
          <input class="acc" type="password" v-model="confirmPassword" placeholder="确认密码" />
          <input class="submit" type="submit" value="注册" />
        </form>
        <div class="fn">
          <a href="javascript:;" @click="goToLogin">返回登录</a>
        </div>
      </div>
    </div>
  </template>
  
  <style scoped>
@import '@/assets/css/LoginView.css';
  </style>
  
<script>
  import axios from 'axios'; 
  export default {
    data() {
      return {
        username: '',
        password: '',
        confirmPassword: ''
      };
    },
    methods: {
      async handleSubmit() {
        if (this.password !== this.confirmPassword) {
          alert('两次输入的密码不一致，请重新输入');
          return;
        }

        try {
          const response = await axios.post('https://api.example.com/register', {
            username: this.username,
            password: this.password,
          });

          console.log('注册成功:', response.data);
          alert('注册成功！');
          this.goToLogin();
        } catch (error) {
          console.error('注册失败:', error.response ? error.response.data : error.message);
          alert('注册失败，请稍后再试');
        }
      },

      goToLogin() {
        // 使用路由进行跳转
        this.$router.push('/login');
      },

      validateForm() {
        // 添加更多的表单验证逻辑
        if (!this.username || !this.password || !this.confirmPassword) {
          alert('请填写所有必填字段');
          return false;
        }
        if (this.password.length < 6) {
          alert('密码长度至少为6个字符');
          return false;
        }
        return true;
      },

      resetForm() {
        this.username = '';
        this.password = '';
        this.confirmPassword = '';
      }
    }
  };
  </script>