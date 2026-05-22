import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    redirect: '/demand-workload'
  },
  {
    path: '/demand-workload',
    name: 'DemandWorkload',
    component: () => import('../views/DemandWorkload.vue'),
    meta: { title: '工作量核定', permission: 'demand:view' }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { title: '月度看板', permission: 'dashboard:view' }
  },
  {
    path: '/review',
    name: 'Review',
    component: () => import('../views/Review.vue'),
    meta: { title: '评审统计', permission: 'data:manage' }
  },
  {
    path: '/training',
    name: 'Training',
    component: () => import('../views/Training.vue'),
    meta: { title: '转培上报', permission: 'data:manage' }
  },
  {
    path: '/experimenter',
    name: 'Experimenter',
    component: () => import('../views/Experimenter.vue'),
    meta: { title: '体验官管理', permission: 'data:manage' }
  },
  {
    path: '/workorder',
    name: 'WorkOrder',
    component: () => import('../views/WorkOrder.vue'),
    meta: { title: '需求提单', permission: 'data:manage' }
  },
  {
    path: '/model-config',
    name: 'ModelConfig',
    component: () => import('../views/ModelConfig.vue'),
    meta: { title: '大模型设置', permission: 'model:manage' }
  },
  {
    path: '/embedding-config',
    name: 'EmbeddingConfig',
    component: () => import('../views/EmbeddingConfig.vue'),
    meta: { title: '向量化模型配置', permission: 'model:manage' }
  },
  {
    path: '/agent-config',
    name: 'AgentConfig',
    component: () => import('../views/AgentConfig.vue'),
    meta: { title: '智能体参数', permission: 'model:manage' }
  },
  {
    path: '/users',
    name: 'UserManagement',
    component: () => import('../views/UserManagement.vue'),
    meta: { title: '用户管理', permission: 'user:manage' }
  },
  {
    path: '/roles',
    name: 'RoleManagement',
    component: () => import('../views/RoleManagement.vue'),
    meta: { title: '角色权限', permission: 'role:manage' }
  },
  {
    path: '/smart-recommend',
    name: 'SmartRecommend',
    component: () => import('../views/SmartRecommend.vue'),
    meta: { title: '智能推荐', permission: 'smart:use' }
  },
  {
    path: '/vector-search',
    name: 'VectorSearch',
    component: () => import('../views/VectorSearch.vue'),
    meta: { title: '向量搜索', permission: 'smart:use' }
  }
]

const router = new VueRouter({
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title + ' - 一线体验官专项数据分析系统'
  }
  if (!to.meta.public && !localStorage.getItem('yxtyg_token')) {
    next('/login')
    return
  }
  const permissions = JSON.parse(localStorage.getItem('yxtyg_permissions') || '[]')
  if (to.meta.permission && !permissions.includes(to.meta.permission)) {
    next('/demand-workload')
    return
  }
  if (to.path === '/login' && localStorage.getItem('yxtyg_token')) {
    next('/demand-workload')
    return
  }
  next()
})

export default router
