<template>
  <div class="page-container">
    <el-card>
      <div slot="header">
        <span>智能体参数设置</span>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="agentName" label="智能体名称" width="150" />
        <el-table-column prop="description" label="功能描述" show-overflow-tooltip />
        <el-table-column prop="modelName" label="使用模型" width="150" />
        <el-table-column prop="temperature" label="温度" width="80" align="center" />
        <el-table-column prop="topP" label="Top P" width="80" align="center" />
        <el-table-column prop="repetitionPenalty" label="重复惩罚" width="90" align="center" />
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button size="mini" type="text" @click="handleEdit(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="编辑智能体参数" :visible.sync="dialogVisible" width="800px">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-form-item label="智能体名称">
          <el-input v-model="form.agentName" disabled />
        </el-form-item>
        <el-form-item label="功能描述">
          <el-input v-model="form.description" type="textarea" :rows="2" disabled />
        </el-form-item>
        <el-form-item label="大模型配置" prop="modelConfigId">
          <el-select v-model="form.modelConfigId" filterable placeholder="请选择大模型配置" style="width: 100%" @change="handleModelConfigChange">
            <el-option v-for="config in modelConfigs" :key="config.id" :label="config.name" :value="config.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="使用模型" prop="modelName">
          <el-select v-model="form.modelName" placeholder="请选择或输入模型名称" filterable allow-create default-first-option style="width: 100%">
            <el-option v-for="model in availableModels" :key="model" :label="model" :value="model" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">模型参数</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Temperature" prop="temperature">
              <el-slider v-model="form.temperature" :min="0" :max="2" :step="0.1" show-input />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Top P" prop="topP">
              <el-slider v-model="form.topP" :min="0" :max="1" :step="0.05" show-input />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="重复惩罚" prop="repetitionPenalty">
              <el-slider v-model="form.repetitionPenalty" :min="0" :max="2" :step="0.1" show-input />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大Token" prop="maxTokens">
              <el-input-number v-model="form.maxTokens" :min="100" :max="32000" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">系统提示词</el-divider>

        <el-form-item label="系统提示词" prop="systemPrompt">
          <el-input v-model="form.systemPrompt" type="textarea" :rows="3" placeholder="请输入系统提示词" />
        </el-form-item>

        <el-divider content-position="left">用户提示词</el-divider>

        <el-form-item label="用户提示词" prop="userPrompt">
          <div slot="label">
            <span>用户提示词</span>
            <el-tooltip content="使用 {content} 作为占位符，系统会自动替换为实际输入内容" placement="top">
              <i class="el-icon-question" style="color: #909399; margin-left: 5px;"></i>
            </el-tooltip>
          </div>
          <el-input v-model="form.userPrompt" type="textarea" :rows="7" placeholder="请输入用户提示词模板，使用 {content} 作为占位符" />
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
import { getAgentConfigList, updateAgentConfig } from '@/api/agentConfig'
import { getModelConfigList } from '@/api/modelConfig'

export default {
  name: 'AgentConfig',
  data() {
    return {
      loading: false,
      submitLoading: false,
      tableData: [],
      modelConfigs: [],
      availableModels: [],
      dialogVisible: false,
      form: {
        id: null,
        agentCode: '',
        agentName: '',
        description: '',
        modelConfigId: null,
        modelName: '',
        temperature: 0.7,
        topP: 0.9,
        repetitionPenalty: 1.1,
        systemPrompt: '',
        userPrompt: '{content}',
        maxTokens: 2048
      },
      rules: {
        modelConfigId: [{ required: true, message: '请选择大模型配置', trigger: 'change' }],
        modelName: [{ required: true, message: '请选择或输入模型名称', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadData()
    this.loadModelConfigs()
  },
  methods: {
    loadData() {
      this.loading = true
      getAgentConfigList().then(res => {
        this.tableData = res.data
      }).finally(() => {
        this.loading = false
      })
    },
    loadModelConfigs() {
      getModelConfigList().then(res => {
        this.modelConfigs = res.data
      })
    },
    handleEdit(row) {
      this.form = { ...row }
      this.dialogVisible = true
      this.loadAvailableModels(row.modelConfigId)
    },
    loadAvailableModels(modelConfigId) {
      const config = this.modelConfigs.find(c => c.id === modelConfigId)
      if (config && config.models) {
        try {
          this.availableModels = JSON.parse(config.models)
        } catch (e) {
          this.availableModels = []
        }
      } else {
        this.availableModels = []
      }
    },
    handleModelConfigChange(configId) {
      this.loadAvailableModels(configId)
      this.form.modelName = ''
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.submitLoading = true
          updateAgentConfig(this.form).then(() => {
            this.$message.success('保存成功')
            this.dialogVisible = false
            this.loadData()
          }).finally(() => {
            this.submitLoading = false
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
