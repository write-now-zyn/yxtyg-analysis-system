<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-card__header">
        <span class="page-card__title">角色权限</span>
      </div>
      <el-table :data="roles" border stripe size="mini" v-loading="loading">
        <el-table-column prop="name" label="角色" width="140" />
        <el-table-column prop="code" label="编码" width="180" />
        <el-table-column prop="description" label="说明" min-width="220" />
        <el-table-column label="权限" min-width="360">
          <template slot-scope="scope">
            <el-checkbox-group v-model="scope.row.permissions">
              <el-checkbox v-for="item in permissions" :key="item.code" :label="item.code">{{ item.name }}</el-checkbox>
            </el-checkbox-group>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="savePermissions(scope.row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getRoles, getPermissions, updateRolePermissions } from '@/api/role'

export default {
  name: 'RoleManagement',
  data() {
    return {
      loading: false,
      roles: [],
      permissions: []
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      Promise.all([getRoles(), getPermissions()]).then(([roleRes, permissionRes]) => {
        if (roleRes.code === 200) this.roles = roleRes.data || []
        if (permissionRes.code === 200) this.permissions = permissionRes.data || []
      }).finally(() => {
        this.loading = false
      })
    },
    savePermissions(row) {
      updateRolePermissions(row.code, { permissionCodes: row.permissions }).then(res => {
        if (res.code === 200) {
          this.$message.success('保存成功')
        }
      })
    }
  }
}
</script>

<style scoped>
.el-checkbox {
  margin-right: 16px;
  margin-bottom: 6px;
}
</style>
