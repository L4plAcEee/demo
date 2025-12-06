<template>
  <div class="box">
    <div class="left"></div>
    <div class="right">
      <h4> 登  录 </h4>
      <form @submit.prevent="handleSubmit">

        <input class="acc" type="text" v-model="ruleForm.name" placeholder="用户名" />
        <input class="acc" type="password" v-model="ruleForm.password" placeholder="密码" />
        <input class="submit" :disabled="isLoading" type="submit" value="登录" />

      </form>
      <div class="fn">
        <a href="javascript:;" @click="goToRegister">注册账号</a>
        <a href="javascript:;" @click="recoverPassword">找回密码</a>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
export default {
    data() {
      return {
        ruleForm: {
          name: '',
          password: ''
        },
        isLoading: false,  // 登录按钮状态
      };
    },
    methods: {
      resetForm() {
        this.ruleForm.name = '';
        this.ruleForm.password = '';
      },
      async handleSubmit() {
        if (!this.ruleForm.name || !this.ruleForm.password) {
          alert('用户名和密码不能为空');
          return;
        }
        try {
          const response = await axios.post('http://localhost:8044/login', {
              name: this.ruleForm.name,
              password: this.ruleForm.password
              }, {headers:{
                'Content-Type': 'application/json'
              }
          });
          // 处理成功响应
          console.log('登录成功:', response.data);
          // 存储 token
          if (response.data.token) {
            sessionStorage.setItem('token', response.data.token);
          }
          // 页面跳转
          this.$router.push('/emp'); // 假设登录成功后跳转到仪表板页面
        } catch (error) {
          // 处理错误
          this.resetForm();
          console.error('登录失败:', error.response ? error.response.data : error.message);
          alert('登录失败，请检查用户名和密码');
        }
      },
      goToRegister() {
          this.$router.push('/reg');
      },
      recoverPassword() {
          this.$router.push('/find');
      }
    }
  }
</script>

<style scoped>
@import '@/assets/css/LoginView.css';
</style>