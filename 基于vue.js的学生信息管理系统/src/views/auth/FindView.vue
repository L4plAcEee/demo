<template>
    <div class="box">
      <div class="left"></div>
      <div class="right">
        <h4> 找回密码 </h4>
        <form @submit.prevent="handleRecover">
  
          <input 
            class="acc" 
            type="email" 
            v-model="recoverForm.email" 
            placeholder="请输入注册时的邮箱" 
          />
          <input 
            class="submit" 
            :disabled="isLoading" 
            type="submit" 
            value="找回密码" 
          />
  
        </form>
        <div class="fn">
          <a href="javascript:;" @click="goToLogin">返回登录</a>
          <a href="javascript:;" @click="goToRegister">注册账号</a>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  export default {
    data() {
      return {
        recoverForm: {
          email: '',
        },
        isLoading: false, // 提交按钮状态
      };
    },
    methods: {
      async handleRecover() {
        if (!this.recoverForm.email) {
          alert('邮箱不能为空');
          return;
        }
        try {
          this.isLoading = true;
          const response = await axios.post('http://localhost:8044/recover-password', {
            email: this.recoverForm.email
          }, {
            headers: {
              'Content-Type': 'application/json'
            }
          });
          // 处理成功响应
          console.log('找回密码成功:', response.data);
          alert('重置链接已发送到您的邮箱，请查收');
          this.$router.push('/login'); // 假设找回密码完成后跳转到登录页面
        } catch (error) {
          console.error('找回密码失败:', error.response ? error.response.data : error.message);
          alert('找回密码失败，请检查输入的邮箱地址');
        } finally {
          this.isLoading = false;
        }
      },
      goToLogin() {
        this.$router.push('/log');
      },
      goToRegister() {
        this.$router.push('/reg');
      }
    }
  };
  </script>
  
  <style scoped>
  @import '@/assets/css/LoginView.css';
  </style>
  