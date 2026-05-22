<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-card__header">
        <span class="page-card__title">用户管理</span>
        <div class="page-card__actions">
          <el-button type="primary" icon="el-icon-plus" @click="openForm()">新增用户</el-button>
        </div>
      </div>
      <el-form :inline="true" :model="queryParams" size="small" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" clearable placeholder="用户名/姓名/电话" style="width: 180px" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryParams.roleCode" clearable placeholder="全部" style="width: 150px">
            <el-option v-for="item in roles" :key="item.code" :label="item.name" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" clearable placeholder="全部" style="width: 110px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe size="mini" v-loading="loading">
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="roleName" label="角色" width="140" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="openForm(scope.row)">编辑</el-button>
            <el-button type="text" class="danger-text" :disabled="scope.row.status === 0" @click="handleDisable(scope.row)">禁用</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-size="queryParams.size"
        :current-page="queryParams.current"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 15px; text-align: right"
      />
    </el-card>

    <el-dialog :title="form.id ? '编辑用户' : '新增用户'" :visible.sync="formVisible" width="560px">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item :label="form.id ? '新密码' : '密码'" :prop="form.id ? '' : 'password'">
          <el-input v-model="form.password" show-password :placeholder="form.id ? '留空则不修改' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="form.roleCode" style="width: 100%">
            <el-option v-for="item in roles" :key="item.code" :label="item.name" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, createUser, updateUser, disableUser } from '@/api/user'
import { getRoles } from '@/api/role'

export default {
  name: 'UserManagement',
  data() {
    return {
      loading: false,
      roles: [],
      tableData: [],
      total: 0,
      formVisible: false,
      queryParams: {
        keyword: '',
        roleCode: '',
        status: '',
        current: 1,
        size: 10
      },
      form: this.emptyForm(),
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        roleCode: [{ required: true, message: '请选择角色', trigger: 'change' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }]
      }
    }
  },
  created() {
    this.loadRoles()
    this.loadData()
  },
  methods: {
    emptyForm() {
      return { id: null, username: '', password: '', name: '', roleCode: '', phone: '', email: '', status: 1 }
    },
    loadRoles() {
      getRoles().then(res => {
        if (res.code === 200) this.roles = res.data || []
      })
    },
    loadData() {
      this.loading = true
      getUserList(this.queryParams).then(res => {
        if (res.code === 200) {
          this.tableData = res.data.records
          this.total = res.data.total
        }
      }).finally(() => {
        this.loading = false
      })
    },
    handleSearch() {
      this.queryParams.current = 1
      this.loadData()
    },
    handleReset() {
      this.queryParams = { keyword: '', roleCode: '', status: '', current: 1, size: 10 }
      this.loadData()
    },
    handleSizeChange(size) {
      this.queryParams.size = size
      this.loadData()
    },
    handleCurrentChange(current) {
      this.queryParams.current = current
      this.loadData()
    },
    openForm(row) {
      this.form = row ? { ...row, password: '' } : this.emptyForm()
      this.formVisible = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.id ? updateUser(this.form.id, this.form) : createUser(this.form)
        request.then(res => {
          if (res.code === 200) {
            this.$message.success('保存成功')
            this.formVisible = false
            this.loadData()
          }
        })
      })
    },
    handleDisable(row) {
      this.$confirm(`确定禁用用户 ${row.name} 吗？`, '提示', { type: 'warning' }).then(() => {
        disableUser(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('已禁用')
            this.loadData()
          }
        })
      })
    }
  }
}
</script>

<style scoped>
.filter-form {
  margin-bottom: 12px;
}

.danger-text {
  color: #f56c6c;
}
</style>
