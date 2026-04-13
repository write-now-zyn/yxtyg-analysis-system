<template>
  <div class="page-container">
    <el-card>
      <div slot="header">
        <span>大模型设置</span>
        <el-button type="primary" size="small" style="float: right" @click="handleAdd">新增配置</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="配置名称" width="150" />
        <el-table-column prop="provider" label="提供商" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.provider === 'ollama'" type="success">Ollama</el-tag>
            <el-tag v-else-if="scope.row.provider === 'openai'" type="primary">OpenAI</el-tag>
            <el-tag v-else type="info">自定义</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="baseUrl" label="API地址" show-overflow-tooltip />
        <el-table-column prop="models" label="模型列表" width="200">
          <template slot-scope="scope">
            <el-tooltip v-if="scope.row.models" placement="top" :content="formatModels(scope.row.models)">
              <span class="model-list">{{ formatModels(scope.row.models) }}</span>
            </el-tooltip>
            <span v-else style="color: #909399">暂无模型</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isDefault" label="默认" width="80" align="center">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.isDefault" :active-value="1" :inactive-value="0" @change="handleDefaultChange(scope.row)"></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center">
          <template slot-scope="scope">
            <el-button size="mini" type="text" @click="handleTest(scope.row)">测试连接</el-button>
            <el-button size="mini" type="text" @click="handleFetchModels(scope.row)">拉取模型</el-button>
            <el-button size="mini" type="text" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="text" style="color: #f56c6c" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入配置名称（别名）" />
        </el-form-item>
        <el-form-item label="提供商" prop="provider">
          <el-select v-model="form.provider" placeholder="请选择提供商" style="width: 100%">
            <el-option label="Ollama" value="ollama" />
            <el-option label="OpenAI" value="openai" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="API地址" prop="baseUrl">
          <el-input v-model="form.baseUrl" placeholder="请输入API地址，如 http://localhost:11434">
            <el-button slot="append" @click="handleTestForm">测试连接</el-button>
          </el-input>
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
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getModelConfigList, addModelConfig, updateModelConfig, deleteModelConfig, testConnection, testConnectionByConfig, fetchModels, setDefaultConfig } from '@/api/modelConfig'

export default {
  name: 'ModelConfig',
  data() {
    return {
      loading: false,
      submitLoading: false,
      tableData: [],
      dialogVisible: false,
      dialogTitle: '',
      form: {
        id: null,
        name: '',
        provider: 'ollama',
        baseUrl: '',
        apiKey: '',
        status: 1,
        isDefault: 0
      },
      rules: {
        name: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
        provider: [{ required: true, message: '请选择提供商', trigger: 'change' }],
        baseUrl: [{ required: true, message: '请输入API地址', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    formatModels(modelsJson) {
      if (!modelsJson) return ''
      try {
        const models = JSON.parse(modelsJson)
        return models.join(', ')
      } catch (e) {
        return modelsJson
      }
    },
    loadData() {
      this.loading = true
      getModelConfigList().then(res => {
        this.tableData = res.data
      }).finally(() => {
        this.loading = false
      })
    },
    handleAdd() {
      this.dialogTitle = '新增大模型配置'
      this.form = {
        id: null,
        name: '',
        provider: 'ollama',
        baseUrl: '',
        apiKey: '',
        status: 1,
        isDefault: 0
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogTitle = '编辑大模型配置'
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.submitLoading = true
          testConnectionByConfig(this.form).then(res => {
            if (res.data) {
              const action = this.form.id ? updateModelConfig : addModelConfig
              return action(this.form)
            } else {
              this.$message.error('连接测试失败，请检查配置')
              return Promise.reject(new Error('连接测试失败'))
            }
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
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该配置吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteModelConfig(row.id).then(() => {
          this.$message.success('删除成功')
          this.loadData()
        })
      })
    },
    handleTest(row) {
      testConnection(row.id).then(res => {
        if (res.data) {
          this.$message.success('连接成功')
        } else {
          this.$message.error('连接失败')
        }
      })
    },
    handleTestForm() {
      if (!this.form.baseUrl) {
        this.$message.warning('请先输入API地址')
        return
      }
      testConnectionByConfig(this.form).then(res => {
        if (res.data) {
          this.$message.success('连接成功')
        } else {
          this.$message.error('连接失败')
        }
      })
    },
    handleFetchModels(row) {
      this.loading = true
      fetchModels(row.id).then(res => {
        if (res.data && res.data.length > 0) {
          this.$message.success(`成功拉取${res.data.length}个模型`)
          this.loadData()
        } else {
          this.$message.warning('未获取到模型列表')
        }
      }).finally(() => {
        this.loading = false
      })
    },
    handleDefaultChange(row) {
      if (row.isDefault === 1) {
        setDefaultConfig(row.id).then(() => {
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
.model-list {
  display: inline-block;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #606266;
}
</style>
