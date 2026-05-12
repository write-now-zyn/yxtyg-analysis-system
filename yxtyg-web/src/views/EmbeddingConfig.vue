<template>
  <div class="page-container">
    <el-card>
      <div slot="header">
        <span>向量化模型配置</span>
        <el-button type="primary" size="small" style="float: right" @click="handleAdd">新增配置</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="配置名称" width="160" />
        <el-table-column prop="provider" label="提供商" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.provider === 'ollama'" type="success">Ollama</el-tag>
            <el-tag v-else-if="scope.row.provider === 'openai'" type="primary">OpenAI</el-tag>
            <el-tag v-else type="info">自定义</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="baseUrl" label="API地址" show-overflow-tooltip />
        <el-table-column prop="model" label="模型名" width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isDefault" label="默认" width="80" align="center">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.isDefault" :active-value="1" :inactive-value="0" @change="handleDefaultChange(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="220" align="center">
          <template slot-scope="scope">
            <el-button size="mini" type="text" @click="handleTest(scope.row)">测试连接</el-button>
            <el-button size="mini" type="text" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="text" style="color: #f56c6c" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="提供商" prop="provider">
          <el-select v-model="form.provider" placeholder="请选择提供商" style="width: 100%">
            <el-option label="Ollama" value="ollama" />
            <el-option label="OpenAI" value="openai" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="API地址" prop="baseUrl">
          <el-input v-model="form.baseUrl" placeholder="请输入Embedding接口地址">
            <el-button slot="append" @click="handleTestForm">测试连接</el-button>
          </el-input>
        </el-form-item>
        <el-form-item label="模型名称" prop="model">
          <el-input v-model="form.model" placeholder="请输入Embedding模型名称" />
        </el-form-item>
        <el-form-item label="API密钥" prop="apiKey">
          <el-input v-model="form.apiKey" placeholder="可选，如需要请输入API密钥" show-password />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  addEmbeddingConfig,
  deleteEmbeddingConfig,
  getEmbeddingConfigList,
  setDefaultEmbeddingConfig,
  testEmbeddingConnection,
  testEmbeddingConnectionByConfig,
  updateEmbeddingConfig
} from '@/api/embeddingConfig'

function createEmptyForm() {
  return {
    id: null,
    name: '',
    provider: 'ollama',
    baseUrl: '',
    apiKey: '',
    model: '',
    status: 1,
    isDefault: 0,
    remark: ''
  }
}

export default {
  name: 'EmbeddingConfig',
  data() {
    return {
      loading: false,
      submitLoading: false,
      tableData: [],
      dialogVisible: false,
      dialogTitle: '',
      form: createEmptyForm(),
      rules: {
        name: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
        provider: [{ required: true, message: '请选择提供商', trigger: 'change' }],
        baseUrl: [{ required: true, message: '请输入API地址', trigger: 'blur' }],
        model: [{ required: true, message: '请输入模型名称', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      getEmbeddingConfigList().then(res => {
        this.tableData = res.data || []
      }).finally(() => {
        this.loading = false
      })
    },
    handleAdd() {
      this.dialogTitle = '新增向量化模型配置'
      this.form = createEmptyForm()
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogTitle = '编辑向量化模型配置'
      this.form = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitLoading = true
        testEmbeddingConnectionByConfig(this.form).then(res => {
          if (res.data) {
            const action = this.form.id ? updateEmbeddingConfig : addEmbeddingConfig
            return action(this.form)
          }
          this.$message.error('连接测试失败，请检查配置')
          return Promise.reject(new Error('连接测试失败'))
        }).then(() => {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadData()
        }).catch(error => {
          if (error.message !== '连接测试失败') {
            this.$message.error('保存失败：' + error.message)
          }
        }).finally(() => {
          this.submitLoading = false
        })
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该配置吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteEmbeddingConfig(row.id).then(() => {
          this.$message.success('删除成功')
          this.loadData()
        })
      })
    },
    handleTest(row) {
      testEmbeddingConnection(row.id).then(res => {
        if (res.data) {
          this.$message.success('连接成功')
        } else {
          this.$message.error('连接失败')
        }
      })
    },
    handleTestForm() {
      if (!this.form.baseUrl || !this.form.model) {
        this.$message.warning('请先输入API地址和模型名称')
        return
      }
      testEmbeddingConnectionByConfig(this.form).then(res => {
        if (res.data) {
          this.$message.success('连接成功')
        } else {
          this.$message.error('连接失败')
        }
      })
    },
    handleDefaultChange(row) {
      if (row.isDefault === 1) {
        setDefaultEmbeddingConfig(row.id).then(() => {
          this.$message.success('设置成功')
          this.loadData()
        }).catch(() => {
          row.isDefault = 0
        })
      } else {
        this.$message.warning('至少需要保留一个默认配置')
        row.isDefault = 1
      }
    }
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
