<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams" size="small">
        <el-form-item label="会议时间">
          <el-date-picker v-model="meetingTimeRange" type="daterange" range-separator="~" start-placeholder="开始日期" end-placeholder="结束日期" value-format="yyyy-MM-dd" style="width: 280px" />
        </el-form-item>
        <el-form-item label="与会人员">
          <el-input v-model="queryParams.participantName" placeholder="与会人员姓名" clearable style="width: 150px" />
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
        <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAdd">新增会议纪要</el-button>
        <!--
        <el-button type="success" size="small" icon="el-icon-download" @click="handleExport">导出</el-button>
        -->
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe size="mini" v-loading="loading">
        <el-table-column prop="meetingTime" label="会议时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.meetingTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="demandNo" label="需求号" width="170" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="participants" label="与会人" min-width="300">
          <template slot-scope="scope">
            {{ formatParticipants(scope.row.participants) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
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
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" size="small">
        <el-form-item label="需求号" prop="demandNo">
          <el-input v-model="form.demandNo" placeholder="请输入评审方案需求号" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="会议时间" prop="meetingTime">
          <el-date-picker v-model="form.meetingTime" type="datetime" placeholder="请选择会议时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="与会人员" prop="participants">
          <div class="participant-list">
            <div v-for="(item, index) in form.participantList" :key="index" class="participant-item">
              <el-input v-model="item.name" placeholder="姓名" style="width: 120px; margin-right: 10px" />
              <el-select v-model="item.city" placeholder="地市" filterable style="width: 120px; margin-right: 10px">
                <el-option v-for="city in cities" :key="city" :label="city" :value="city" />
              </el-select>
              <el-button type="danger" icon="el-icon-delete" circle size="mini" @click="removeParticipant(index)" />
            </div>
            <div class="participant-actions">
              <el-button type="primary" size="small" icon="el-icon-plus" @click="addParticipant">添加与会人</el-button>
              <el-button type="success" size="small" icon="el-icon-magic-stick" @click="showAiDialog">AI识别</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="会议内容">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入会议内容" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </span>
    </el-dialog>

    <!-- AI识别弹窗 -->
    <el-dialog title="AI识别与会人员" :visible.sync="aiDialogVisible" width="500px" append-to-body>
      <el-input
        v-model="aiInputText"
        type="textarea"
        :rows="10"
        placeholder="请粘贴与会人员信息，例如：&#10;苏州任丽华&#10;盐城衡旭东&#10;淮安张和玲&#10;..."
      />
      <div slot="footer">
        <el-button @click="aiDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="aiLoading" @click="handleAiParse">识别</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getReviewList, addReview, updateReview, deleteReview, parseParticipants } from '@/api/review'

export default {
  name: 'Review',
  data() {
    return {
      cities: ['南京', '苏州', '无锡', '常州', '镇江', '扬州', '泰州', '南通', '盐城', '淮安', '连云港', '徐州', '宿迁'],
      loading: false,
      meetingTimeRange: [],
      queryParams: {
        meetingTimeStart: '',
        meetingTimeEnd: '',
        participantName: '',
        current: 1,
        size: 10
      },
      tableData: [],
      total: 0,
      dialogVisible: false,
      dialogTitle: '',
      form: {
        id: null,
        demandNo: '',
        title: '',
        meetingTime: '',
        participants: '',
        participantList: [],
        content: ''
      },
      rules: {
        demandNo: [{ required: true, message: '请输入需求号', trigger: 'blur' }],
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        meetingTime: [{ required: true, message: '请选择会议时间', trigger: 'change' }]
      },
      aiDialogVisible: false,
      aiInputText: '',
      aiLoading: false
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      if (this.meetingTimeRange && this.meetingTimeRange.length === 2) {
        this.queryParams.meetingTimeStart = this.meetingTimeRange[0]
        this.queryParams.meetingTimeEnd = this.meetingTimeRange[1]
      } else {
        this.queryParams.meetingTimeStart = ''
        this.queryParams.meetingTimeEnd = ''
      }
      getReviewList(this.queryParams).then(res => {
        this.tableData = res.data.records
        this.total = res.data.total
      }).finally(() => {
        this.loading = false
      })
    },
    formatDateTime(str) {
      if (!str) return ''
      // 返回完整时间格式 yyyy-MM-dd HH:mm:ss
      const formatted = str.replace('T', ' ')
      return formatted.length >= 19 ? formatted.substring(0, 19) : formatted + ':00'
    },
    formatParticipants(json) {
      if (!json) return ''
      try {
        const list = JSON.parse(json)
        return list.map(item => `${item.name}(${item.city})`).join('、')
      } catch {
        return json
      }
    },
    handleSearch() {
      this.queryParams.current = 1
      this.loadData()
    },
    handleReset() {
      this.meetingTimeRange = []
      this.queryParams = {
        meetingTimeStart: '',
        meetingTimeEnd: '',
        participantName: '',
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
      this.dialogTitle = '新增会议纪要'
      this.form = {
        id: null,
        demandNo: '',
        title: '',
        meetingTime: '',
        participants: '',
        participantList: [{ name: '', city: '' }],
        content: ''
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.formRef.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogTitle = '编辑会议纪要'
      this.form = {
        ...row,
        meetingTime: this.formatDateTime(row.meetingTime),
        participantList: row.participants ? JSON.parse(row.participants) : [{ name: '', city: '' }]
      }
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.formRef.validate(valid => {
        if (valid) {
          // 校验与会人
          const validParticipants = this.form.participantList.filter(item => item.name)
          if (validParticipants.length === 0) {
            this.$message.error('请至少添加一个与会人员')
            return
          }
          // 校验地市是否都已选择
          const missingCity = validParticipants.find(item => !item.city)
          if (missingCity) {
            this.$message.error(`请为"${missingCity.name}"选择所属地市`)
            return
          }
          this.form.participants = JSON.stringify(validParticipants)
          
          const api = this.form.id ? updateReview : addReview
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
      this.$confirm('确定要删除该会议纪要吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteReview(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          }
        })
      })
    },
    addParticipant() {
      this.form.participantList.push({ name: '', city: '' })
    },
    removeParticipant(index) {
      this.form.participantList.splice(index, 1)
    },
    handleExport() {
      // 过滤空值参数
      const params = {}
      if (this.queryParams.meetingTimeStart) params.meetingTimeStart = this.queryParams.meetingTimeStart
      if (this.queryParams.meetingTimeEnd) params.meetingTimeEnd = this.queryParams.meetingTimeEnd
      if (this.queryParams.participantName) params.participantName = this.queryParams.participantName
      params.current = 1
      params.size = 99999
      window.open(`/api/review/export?${new URLSearchParams(params).toString()}`)
    },
    showAiDialog() {
      this.aiInputText = ''
      this.aiDialogVisible = true
    },
    handleAiParse() {
      if (!this.aiInputText.trim()) {
        this.$message.warning('请输入要识别的文本')
        return
      }
      this.aiLoading = true
      parseParticipants(this.aiInputText).then(res => {
        if (res.code === 200 && res.data && res.data.length > 0) {
          // 清空现有列表，填充AI识别结果
          this.form.participantList = res.data.map(item => ({
            name: item.name || '',
            city: item.city || ''
          }))
          this.$message.success(`成功识别 ${res.data.length} 位与会人员`)
          this.aiDialogVisible = false
        } else {
          this.$message.warning('未能识别出与会人员，请检查输入格式')
        }
      }).catch(() => {
        this.$message.error('AI识别失败，请确保Ollama服务已启动')
      }).finally(() => {
        this.aiLoading = false
      })
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

.table-card {
  background: #fff;
}

.btn-group {
  margin-bottom: 15px;
}

.participant-list {
  width: 100%;
}

.participant-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.participant-actions {
  margin-top: 10px;
}
</style>
