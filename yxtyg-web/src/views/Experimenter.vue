<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams" size="small">
        <el-form-item label="地市">
          <el-select v-model="queryParams.city" clearable filterable placeholder="请选择" style="width: 150px">
            <el-option v-for="city in cities" :key="city" :label="city" :value="city" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="queryParams.name" placeholder="请输入" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="queryParams.phone" placeholder="请输入" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="是否接口人">
          <el-select v-model="queryParams.isContact" clearable placeholder="请选择" style="width: 120px">
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="table-card">
      <div class="btn-group">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAdd">新增</el-button>
        <el-button type="danger" size="small" icon="el-icon-delete" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
        <el-button type="success" size="small" icon="el-icon-download" @click="handleExport">导出</el-button>
        <el-upload
          class="upload-btn"
          action="/api/experimenter/import"
          accept=".xlsx,.xls"
          :show-file-list="false"
          :on-success="handleImportSuccess"
          :on-error="handleImportError"
          :before-upload="beforeUpload">
          <el-button type="warning" size="small" icon="el-icon-upload2">批量导入</el-button>
        </el-upload>
        <el-link type="primary" :underline="false" href="/api/experimenter/template" target="_blank" style="margin-left: 10px;">
          <i class="el-icon-document"></i> 下载导入模板
        </el-link>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe size="mini" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="city" label="地市" width="70" />
        <el-table-column prop="name" label="姓名" width="80" />
        <el-table-column prop="phone" label="电话" width="110" />
        <el-table-column prop="email" label="邮箱" min-width="200">
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.email || ''" placement="top" :disabled="!scope.row.email">
              <span class="copy-text" @click="copyText(scope.row.email)">{{ scope.row.email || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="110">
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.role || ''" placement="top" :disabled="!scope.row.role">
              <span class="copy-text" @click="copyText(scope.row.role)">{{ scope.row.role || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="250">
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.remark || ''" placement="top" :disabled="!scope.row.remark">
              <span class="copy-text" @click="copyText(scope.row.remark)">{{ scope.row.remark || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="接口人" width="80" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.isContact === 1" type="success" size="small">是</el-tag>
            <el-tag v-else type="info" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" size="small" @click="handleSetContact(scope.row)">
              {{ scope.row.isContact === 1 ? '取消接口人' : '设为接口人' }}
            </el-button>
            <el-button type="text" size="small" style="color: #f56c6c" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" size="small">
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
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色">
          <el-input v-model="form.role" placeholder="请输入角色" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getExperimenterList, addExperimenter, updateExperimenter, deleteExperimenter, setContact, batchDeleteExperimenters } from '@/api/experimenter'

export default {
  name: 'Experimenter',
  data() {
    return {
      cities: ['南京', '苏州', '无锡', '常州', '镇江', '扬州', '泰州', '南通', '盐城', '淮安', '连云港', '徐州', '宿迁'],
      loading: false,
      selectedIds: [], // 选中的ID列表
      queryParams: {
        city: '',
        name: '',
        phone: '',
        isContact: null,
        current: 1,
        size: 10
      },
      tableData: [],
      total: 0,
      dialogVisible: false,
      dialogTitle: '',
      form: {
        id: null,
        city: '',
        name: '',
        phone: '',
        email: '',
        role: '',
        remark: ''
      },
      rules: {
        city: [{ required: true, message: '请选择地市', trigger: 'change' }],
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { pattern: /^[\u4e00-\u9fa5]+$/, message: '姓名必须是中文汉字', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入11位有效手机号', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      getExperimenterList(this.queryParams).then(res => {
        this.tableData = res.data.records
        this.total = res.data.total
      }).finally(() => {
        this.loading = false
      })
    },
    handleSearch() {
      this.queryParams.current = 1
      this.loadData()
    },
    handleReset() {
      this.queryParams = {
        city: '',
        name: '',
        phone: '',
        isContact: null,
        current: 1,
        size: 10
      }
      this.loadData()
    },
    handleSizeChange(val) {
      this.queryParams.size = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.queryParams.current = val
      this.loadData()
    },
    handleAdd() {
      this.dialogTitle = '新增体验官'
      this.form = {
        id: null,
        city: '',
        name: '',
        phone: '',
        email: '',
        role: '',
        remark: ''
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.formRef.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogTitle = '编辑体验官'
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.formRef.validate(valid => {
        if (valid) {
          const api = this.form.id ? updateExperimenter : addExperimenter
          api(this.form).then(res => {
            if (res.code === 200) {
              this.$message.success('操作成功')
              this.dialogVisible = false
              this.loadData()
            }
          })
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该体验官吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteExperimenter(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          }
        })
      })
    },
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id)
    },
    handleBatchDelete() {
      if (this.selectedIds.length === 0) {
        this.$message.warning('请先选择要删除的数据')
        return
      }
      this.$confirm(`确定要删除选中的 ${this.selectedIds.length} 条数据吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        batchDeleteExperimenters(this.selectedIds).then(res => {
          if (res.code === 200) {
            this.$message.success('批量删除成功')
            this.selectedIds = []
            this.loadData()
          }
        })
      })
    },
    handleSetContact(row) {
      const newStatus = row.isContact === 1 ? 0 : 1
      const msg = newStatus === 1 ? '确定要设为接口人吗？' : '确定要取消接口人吗？'
      this.$confirm(msg, '提示', {
        type: 'warning'
      }).then(() => {
        setContact(row.id, newStatus).then(res => {
          if (res.code === 200) {
            this.$message.success('操作成功')
            this.loadData()
          }
        })
      })
    },
    handleExport() {
      // 过滤空值参数
      const params = {}
      if (this.queryParams.city) params.city = this.queryParams.city
      if (this.queryParams.name) params.name = this.queryParams.name
      if (this.queryParams.phone) params.phone = this.queryParams.phone
      if (this.queryParams.isContact !== null && this.queryParams.isContact !== undefined && this.queryParams.isContact !== '') {
        params.isContact = this.queryParams.isContact
      }
      params.current = 1
      params.size = 99999
      window.open(`/api/experimenter/export?${new URLSearchParams(params).toString()}`)
    },
    beforeUpload(file) {
      const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls')
      if (!isExcel) {
        this.$message.error('只能上传Excel文件')
        return false
      }
      return true
    },
    handleImportSuccess(res) {
      if (res.code === 200) {
        this.$message.success(res.data || '导入成功')
        this.loadData()
      } else {
        this.$message.warning(res.message || '导入失败')
      }
    },
    handleImportError(err) {
      this.$message.error('导入失败，请检查文件格式')
    },
    // 复制文本
    copyText(text) {
      if (!text) {
        this.$message.warning('内容为空')
        return
      }
      // 优先使用降级方案，兼容性更好
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.left = '-9999px'
      textarea.style.top = '-9999px'
      document.body.appendChild(textarea)
      textarea.focus()
      textarea.select()
      try {
        const successful = document.execCommand('copy')
        if (successful) {
          this.$message.success('已复制：' + (text.length > 20 ? text.substring(0, 20) + '...' : text))
        } else {
          this.$message.error('复制失败')
        }
      } catch (err) {
        this.$message.error('复制失败：' + err.message)
      }
      document.body.removeChild(textarea)
    }
  }
}
</script>

<style scoped>
.page-container {
  padding: 0;
}

.search-card {
  margin-bottom: 8px;
}
.search-card .el-form-item {
  margin-bottom: 0;
}
/deep/.search-card .el-form-item{
margin-bottom:0px
}

.table-card {
  background: #fff;
}

.btn-group {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.upload-btn {
  display: inline-block;
  margin-left: 10px;
}

.copy-text {
  cursor: pointer;
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.copy-text:hover {
  color: #409eff;
}
</style>
