<template>
  <div id="app">
    <router-view v-if="$route.path === '/login'" />
    <el-container v-else style="height: 100vh">
      <!-- 侧边栏 -->
      <el-aside width="200px" style="background-color: #304156" overflow-y="hidden">
        <div class="logo">
          <span>一线体验官</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <el-menu-item v-if="hasPermission('demand:view')" index="/demand-workload">
            <i class="el-icon-s-order"></i>
            <span>工作量核定</span>
          </el-menu-item>

          <!-- 智能分单 -->
          <el-submenu index="smart">
            <template slot="title">
              <i class="el-icon-magic-stick"></i>
              <span>智能分单</span>
            </template>
            <el-menu-item index="/smart-recommend">
              <i class="el-icon-s-custom"></i>
              <span>智能推荐</span>
            </el-menu-item>
            <el-menu-item index="/vector-search">
              <i class="el-icon-search"></i>
              <span>向量搜索</span>
            </el-menu-item>
          </el-submenu>

          <!-- 数据看板 -->
          <el-submenu index="dashboard">
            <template slot="title">
              <i class="el-icon-data-board"></i>
              <span>数据看板</span>
            </template>
            <el-menu-item index="/dashboard">
              <i class="el-icon-s-marketing"></i>
              <span>月度看板</span>
            </el-menu-item>
          </el-submenu>

          <!-- 数据管理 -->
          <el-submenu index="data">
            <template slot="title">
              <i class="el-icon-s-management"></i>
              <span>数据管理</span>
            </template>
            <el-menu-item index="/workorder">
              <i class="el-icon-document"></i>
              <span>需求提单</span>
            </el-menu-item>
            <el-menu-item index="/review">
              <i class="el-icon-edit-outline"></i>
              <span>评审统计</span>
            </el-menu-item>
            <el-menu-item index="/training">
              <i class="el-icon-s-cooperation"></i>
              <span>转培上报</span>
            </el-menu-item>
            <el-menu-item index="/experimenter">
              <i class="el-icon-user"></i>
              <span>体验官管理</span>
            </el-menu-item>
          </el-submenu>

          <!-- 模型设置 -->
          <el-submenu index="model">
            <template slot="title">
              <i class="el-icon-setting"></i>
              <span>模型设置</span>
            </template>
            <el-menu-item index="/model-config">
              <i class="el-icon-connection"></i>
              <span>大模型设置</span>
            </el-menu-item>
            <el-menu-item index="/embedding-config">
              <i class="el-icon-share"></i>
              <span>向量化模型</span>
            </el-menu-item>
            <el-menu-item index="/agent-config">
              <i class="el-icon-cpu"></i>
              <span>智能体参数</span>
            </el-menu-item>
          </el-submenu>

          <!-- 系统管理 -->
          <el-submenu v-if="hasPermission('user:manage') || hasPermission('role:manage')" index="system">
            <template slot="title">
              <i class="el-icon-s-tools"></i>
              <span>系统管理</span>
            </template>
            <el-menu-item v-if="hasPermission('user:manage')" index="/users">
              <i class="el-icon-user-solid"></i>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item v-if="hasPermission('role:manage')" index="/roles">
              <i class="el-icon-key"></i>
              <span>角色权限</span>
            </el-menu-item>
          </el-submenu>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <el-header class="app-header" height="48px">
          <div class="app-header__title">{{ $route.meta.title }}</div>
          <div class="app-header__actions">
            <el-popover placement="bottom-end" width="360" trigger="click" @show="loadNotifications">
              <div class="notification-list">
                <div v-if="notifications.length === 0" class="notification-empty">暂无通知</div>
                <div v-for="item in notifications" :key="item.id" class="notification-item" :class="{ unread: item.isRead === 0 }" @click="markRead(item)">
                  <div class="notification-title">{{ item.title }}</div>
                  <div class="notification-content">{{ item.content }}</div>
                </div>
              </div>
              <el-badge slot="reference" :value="unreadCount" :hidden="unreadCount === 0">
                <el-button icon="el-icon-bell" circle size="mini" />
              </el-badge>
            </el-popover>
            <span class="current-user">{{ currentUserText }}</span>
            <el-button type="text" @click="handleLogout">退出</el-button>
          </div>
        </el-header>
        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { logout } from '@/api/auth'
import { getNotifications, markNotificationRead } from '@/api/notification'

export default {
  name: 'App',
  data() {
    return {
      notifications: []
    }
  },
  computed: {
    activeMenu() {
      return this.$route.path
    },
    unreadCount() {
      return this.notifications.filter(item => item.isRead === 0).length
    },
    currentUserText() {
      const user = this.$store.state.user
      return user ? `${user.name} / ${user.roleName}` : ''
    }
  },
  created() {
    if (this.$store.state.token) {
      this.loadNotifications()
    }
  },
  methods: {
    hasPermission(code) {
      return this.$store.getters.hasPermission(code)
    },
    loadNotifications() {
      if (!this.$store.state.token) return
      getNotifications().then(res => {
        if (res.code === 200) {
          this.notifications = res.data || []
        }
      })
    },
    markRead(item) {
      if (item.isRead === 1) return
      markNotificationRead(item.id).then(() => {
        item.isRead = 1
      })
    },
    handleLogout() {
      logout().finally(() => {
        this.$store.commit('clearAuth')
        this.$router.replace('/login')
      })
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #263445;
}

.el-menu {
  border-right: none !important;
}

.el-menu-item {
  text-align: left;
}

.el-menu-item i {
  margin-right: 10px;
}

.el-submenu__title i {
  margin-right: 10px;
}

.el-submenu .el-menu-item {
  padding-left: 50px !important;
  min-width: auto;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 0 16px;
}

.app-header__title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.app-header__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.current-user {
  color: #606266;
}

.notification-list {
  max-height: 320px;
  overflow-y: auto;
}

.notification-empty {
  color: #909399;
  text-align: center;
  padding: 20px 0;
}

.notification-item {
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
}

.notification-item.unread {
  background: #ecf5ff;
}

.notification-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.notification-content {
  color: #606266;
  line-height: 1.4;
}
</style>
