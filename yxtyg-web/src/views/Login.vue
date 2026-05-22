<template>
  <div class="login-page">
    <div class="login-panel">
      <h1>一线体验官专项数据分析系统</h1>
      <el-form ref="form" :model="form" :rules="rules" @keyup.enter.native="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" prefix-icon="el-icon-user" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" prefix-icon="el-icon-lock" placeholder="密码" show-password />
        </el-form-item>
        <el-button type="primary" class="login-button" :loading="loading" @click="handleLogin">登录</el-button>
      </el-form>
      <div class="login-hint">admin / devadmin / pm_zhang / pm_li，默认密码 123456</div>
    </div>
  </div>
</template>

<script>
import { login } from '@/api/auth'

export default {
  name: 'Login',
  data() {
    return {
      loading: false,
      form: {
        username: 'admin',
        password: '123456'
      },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    handleLogin() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.loading = true
        login(this.form).then(res => {
          if (res.code === 200) {
            this.$store.commit('setAuth', res.data)
            this.$router.replace('/demand-workload')
          }
        }).finally(() => {
          this.loading = false
        })
      })
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef2f7;
}

.login-panel {
  width: 380px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 32px;
  box-shadow: 0 12px 32px rgba(48, 65, 86, 0.12);
}

.login-panel h1 {
  font-size: 20px;
  text-align: center;
  margin-bottom: 24px;
  color: #303133;
}

.login-button {
  width: 100%;
}

.login-hint {
  margin-top: 16px;
  color: #909399;
  line-height: 1.5;
  text-align: center;
}
</style>
