<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="queryParams" inline size="small">
        <el-form-item label="地市">
          <el-select v-model="queryParams.city" placeholder="请选择地市" clearable style="width: 120px">
            <el-option v-for="city in cities" :key="city" :label="city" :value="city" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="queryParams.phone" placeholder="请输入电话" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="接口人">
          <el-select v-model="queryParams.isContact" placeholder="全部" clearable style="width: 100px">
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAdd">新增</el-button>
        <el-button type="success" size="small" icon="el-icon-download" @click="handleExport">导出</el-button>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe v-loading="loading" size="small">
        <el-table-column prop="city" label="地市" width="100" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="150" />
        <el-table-column prop="role" label="角色" width="100" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="isContact" label="接口人" width="80" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.isContact === 1" type="success" size="mini">是</el-tag>
            <el-tag v-else type="info" size="mini">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" size="small" @click="handleToggleContact(scope.row)">
              {{ scope.row.isContact === 1 ? '取消接口人' : '设为接口人' }}
            </el-button>
            <el-button type="text" size="small" style="color: #f56c6c" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        :page-size="queryParams.pageSize"
        :current-page="queryParams.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="地市" prop="city">
          <el-select v-model="form.city" placeholder="请选择地市" style="width: 100%">
            <el-option v-for="city in cities" :key="city" :label="city" :value="city" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>
        <el-form-item label="角色">
          <el-input v-model="form.role" placeholder="请输入角色（选填）" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="接口人">
          <el-switch v-model="form.isContact" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getExperimenterList, addExperimenter, updateExperimenter, deleteExperimenter, exportExperimenter } from '@/api/experimenter'
import { CITIES } from '@/utils/city'

export default {
  name: 'Experimenter',
  data() {
    return {
      cities: CITIES,
      loading: false,
      tableData: [],
      total: 0,
      queryParams: {
        city: '',
        name: '',
        phone: '',
        isContact: null,
        pageNum: 1,
        pageSize: 10
      },
      dialogVisible: false,
      dialogTitle: '新增体验官',
      form: {
        id: null,
        city: '',
        name: '',
        phone: '',
        email: '',
        role: '',
        remark: '',
        isContact: 0
      },
      rules: {
        city: [{ required: true, message: '请选择地市', trigger: 'change' }],
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入电话', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadList()
  },
  methods: {
    // 加载列表
    async loadList() {
      this.loading = true
      try {
        const res = await getExperimenterList(this.queryParams)
        this.tableData = res.data.records
        this.total = res.data.total
      } finally {
        this.loading = false
      }
    },
    // 搜索
    handleSearch() {
      this.queryParams.pageNum = 1
      this.loadList()
    },
    // 重置
    handleReset() {
      this.queryParams = {
        city: '',
        name: '',
        phone: '',
        isContact: null,
        pageNum: 1,
        pageSize: 10
      }
      this.loadList()
    },
    // 分页
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.loadList()
    },
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.loadList()
    },
    // 新增
    handleAdd() {
      this.dialogTitle = '新增体验官'
      this.form = {
        id: null,
        city: '',
        name: '',
        phone: '',
        email: '',
        role: '',
        remark: '',
        isContact: 0
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.clearValidate()
      })
    },
    // 编辑
    handleEdit(row) {
      this.dialogTitle = '编辑体验官'
      this.form = { ...row }
      this.dialogVisible = true
    },
    // 切换接口人
    async handleToggleContact(row) {
      const newStatus = row.isContact === 1 ? 0 : 1
      await updateExperimenter({ id: row.id, isContact: newStatus })
      this.$message.success('操作成功')
      this.loadList()
    },
    // 删除
    handleDelete(row) {
      this.$confirm('确定要删除该体验官吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        await deleteExperimenter(row.id)
        this.$message.success('删除成功')
        this.loadList()
      })
    },
    // 提交
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          if (this.form.id) {
            await updateExperimenter(this.form)
          } else {
            await addExperimenter(this.form)
          }
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadList()
        }
      })
    },
    // 导出
    async handleExport() {
      const res = await exportExperimenter(this.queryParams)
      this.downloadFile(res, '体验官列表.xlsx')
    },
    // 下载文件
    downloadFile(response, filename) {
      const blob = new Blob([response.data])
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = filename
      link.click()
      window.URL.revokeObjectURL(url)
    }
  }
}
</script>

<style scoped>
.page-container {
  height: 100%;
}

.search-card {
  margin-bottom: 15px;
}

.table-card {
  height: calc(100% - 100px);
}

.toolbar {
  margin-bottom: 15px;
}

.pagination {
  margin-top: 15px;
  text-align: right;
}
</style>
