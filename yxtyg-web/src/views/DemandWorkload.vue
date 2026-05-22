<template>
  <div class="page-container">
    <el-row :gutter="12" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-label">全部需求</div>
          <div class="stat-value">{{ statistics.total || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-label">待填写</div>
          <div class="stat-value pending">{{ statistics.pendingCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-label">已填写</div>
          <div class="stat-value filled">{{ statistics.filledCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-label">核减工作量</div>
          <div class="stat-value confirmed">{{ statistics.reductionWorkloadTotal || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="page-card">
      <div slot="header" class="page-card__header">
        <span class="page-card__title">筛选条件</span>
      </div>
      <el-form :inline="true" :model="queryParams" size="small">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" clearable placeholder="编号/名称/描述" style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" clearable placeholder="全部" style="width: 120px">
            <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="canManageDemand" label="产品经理">
          <el-select v-model="queryParams.productManagerId" clearable filterable placeholder="全部" style="width: 140px">
            <el-option v-for="item in productManagers" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="归属系统">
          <el-input v-model="queryParams.systemName" clearable placeholder="请输入" style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="page-card">
      <div slot="header" class="page-card__header">
        <span class="page-card__title">工作量核定</span>
        <div class="page-card__actions">
          <el-button v-if="canManageDemand" type="primary" icon="el-icon-plus" @click="openForm()">新增</el-button>
          <el-upload v-if="canManageDemand" action="" accept=".xlsx,.xls" :show-file-list="false" :before-upload="handleImport">
            <el-button icon="el-icon-upload2">导入</el-button>
          </el-upload>
          <el-button v-if="canManageDemand" icon="el-icon-download" @click="handleTemplate">模板</el-button>
          <el-button icon="el-icon-document" @click="handleExport">导出</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe size="mini" v-loading="loading">
        <el-table-column prop="demandNo" label="需求编号" width="140">
          <template slot-scope="scope">
            <el-link type="primary" @click="openDetail(scope.row)">{{ scope.row.demandNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="demandName" label="需求名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="productManagerName" label="产品经理" width="100" />
        <el-table-column prop="systemName" label="归属系统" width="140" show-overflow-tooltip />
        <el-table-column prop="initialWorkload" label="初核" width="80" />
        <el-table-column prop="finalWorkload" label="最终" width="80">
          <template slot-scope="scope">{{ scope.row.finalWorkload == null ? '-' : scope.row.finalWorkload }}</template>
        </el-table-column>
        <el-table-column prop="reductionWorkload" label="核减" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="statusType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="openFinal(scope.row)">填报</el-button>
            <el-button v-if="canManageDemand" type="text" @click="handleConfirm(scope.row)">核定</el-button>
            <el-button v-if="canManageDemand" type="text" @click="openReminder(scope.row)">催办</el-button>
            <el-button v-if="canManageDemand" type="text" @click="openForm(scope.row)">编辑</el-button>
            <el-button v-if="canManageDemand" type="text" class="danger-text" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog :title="form.id ? '编辑需求' : '新增需求'" :visible.sync="formVisible" width="720px">
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="需求编号" prop="demandNo">
          <el-input v-model="form.demandNo" />
        </el-form-item>
        <el-form-item label="需求名称" prop="demandName">
          <el-input v-model="form.demandName" />
        </el-form-item>
        <el-form-item label="产品经理" prop="productManagerId">
          <el-select v-model="form.productManagerId" filterable placeholder="请选择" style="width: 100%">
            <el-option v-for="item in productManagers" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="归属系统" prop="systemName">
          <el-input v-model="form.systemName" />
        </el-form-item>
        <el-form-item label="初核工作量" prop="initialWorkload">
          <el-input-number v-model="form.initialWorkload" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="初核金额">
          <el-input-number v-model="form.initialAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="需求状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="需求描述">
          <el-input v-model="form.demandDescription" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog title="填写最终核定工作量" :visible.sync="finalVisible" width="520px">
      <el-form :model="finalForm" label-width="140px">
        <el-form-item label="最终核定工作量">
          <el-input-number v-model="finalForm.finalWorkload" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="finalForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="finalVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFinal">提交</el-button>
      </span>
    </el-dialog>

    <el-dialog title="催办" :visible.sync="reminderVisible" width="520px">
      <el-input v-model="reminderContent" type="textarea" :rows="4" placeholder="请填写催办内容" />
      <span slot="footer">
        <el-button @click="reminderVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReminder">发送</el-button>
      </span>
    </el-dialog>

    <el-dialog title="需求详情" :visible.sync="detailVisible" width="760px">
      <el-descriptions v-if="detailData" :column="2" border size="small">
        <el-descriptions-item label="需求编号">{{ detailData.demandNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailData.status }}</el-descriptions-item>
        <el-descriptions-item label="需求名称" :span="2">{{ detailData.demandName }}</el-descriptions-item>
        <el-descriptions-item label="产品经理">{{ detailData.productManagerName }}</el-descriptions-item>
        <el-descriptions-item label="归属系统">{{ detailData.systemName }}</el-descriptions-item>
        <el-descriptions-item label="初核工作量">{{ detailData.initialWorkload }}</el-descriptions-item>
        <el-descriptions-item label="最终核定工作量">{{ detailData.finalWorkload || '-' }}</el-descriptions-item>
        <el-descriptions-item label="核减工作量">{{ detailData.reductionWorkload }}</el-descriptions-item>
        <el-descriptions-item label="初核金额">{{ detailData.initialAmount }}</el-descriptions-item>
        <el-descriptions-item label="需求描述" :span="2">{{ detailData.demandDescription || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-timeline v-if="detailData && detailData.reminders && detailData.reminders.length" class="reminder-timeline">
        <el-timeline-item v-for="item in detailData.reminders" :key="item.id" :timestamp="item.createdAt">
          {{ item.reminderByName }} 催办 {{ item.reminderToName }}：{{ item.reminderContent }}
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script>
import {
  getDemandWorkloadList,
  getDemandWorkloadDetail,
  createDemandWorkload,
  updateDemandWorkload,
  deleteDemandWorkload,
  importDemandWorkload,
  downloadDemandTemplate,
  exportDemandWorkload,
  submitFinalWorkload,
  confirmDemandWorkload,
  remindDemandWorkload,
  getDemandStatistics
} from '@/api/demandWorkload'
import { getProductManagers } from '@/api/user'
import { downloadBlob } from '@/utils/download'

export default {
  name: 'DemandWorkload',
  data() {
    return {
      statusOptions: ['待填写', '已填写', '已核定'],
      loading: false,
      tableData: [],
      total: 0,
      statistics: {},
      productManagers: [],
      queryParams: {
        keyword: '',
        status: '',
        productManagerId: '',
        systemName: '',
        current: 1,
        size: 10
      },
      formVisible: false,
      finalVisible: false,
      reminderVisible: false,
      detailVisible: false,
      activeRow: null,
      detailData: null,
      reminderContent: '',
      finalForm: {
        finalWorkload: 0,
        remark: ''
      },
      form: this.emptyForm(),
      rules: {
        demandNo: [{ required: true, message: '请输入需求编号', trigger: 'blur' }],
        demandName: [{ required: true, message: '请输入需求名称', trigger: 'blur' }],
        productManagerId: [{ required: true, message: '请选择产品经理', trigger: 'change' }],
        systemName: [{ required: true, message: '请输入归属系统', trigger: 'blur' }],
        initialWorkload: [{ required: true, message: '请输入初核工作量', trigger: 'change' }]
      }
    }
  },
  computed: {
    canManageDemand() {
      return this.$store.getters.hasPermission('demand:manage')
    }
  },
  created() {
    if (this.canManageDemand) {
      this.loadProductManagers()
    }
    this.loadData()
  },
  methods: {
    emptyForm() {
      return {
        id: null,
        demandNo: '',
        demandName: '',
        demandDescription: '',
        productManagerId: '',
        systemName: '',
        initialWorkload: 0,
        initialAmount: 0,
        finalWorkload: null,
        status: '待填写',
        remark: ''
      }
    },
    loadProductManagers() {
      getProductManagers().then(res => {
        if (res.code === 200) this.productManagers = res.data || []
      })
    },
    loadData() {
      this.loading = true
      Promise.all([
        getDemandWorkloadList(this.queryParams),
        getDemandStatistics(this.queryParams)
      ]).then(([listRes, statRes]) => {
        if (listRes.code === 200) {
          this.tableData = listRes.data.records
          this.total = listRes.data.total
        }
        if (statRes.code === 200) {
          this.statistics = statRes.data || {}
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
      this.queryParams = { keyword: '', status: '', productManagerId: '', systemName: '', current: 1, size: 10 }
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
      this.form = row ? { ...row } : this.emptyForm()
      this.formVisible = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.id ? updateDemandWorkload(this.form.id, this.form) : createDemandWorkload(this.form)
        request.then(res => {
          if (res.code === 200) {
            this.$message.success('保存成功')
            this.formVisible = false
            this.loadData()
          }
        })
      })
    },
    openFinal(row) {
      this.activeRow = row
      this.finalForm = { finalWorkload: row.finalWorkload || 0, remark: row.remark || '' }
      this.finalVisible = true
    },
    submitFinal() {
      submitFinalWorkload(this.activeRow.id, this.finalForm).then(res => {
        if (res.code === 200) {
          this.$message.success('提交成功')
          this.finalVisible = false
          this.loadData()
        }
      })
    },
    handleConfirm(row) {
      this.$confirm('确认将该需求标记为已核定吗？', '提示', { type: 'warning' }).then(() => {
        confirmDemandWorkload(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('核定成功')
            this.loadData()
          }
        })
      })
    },
    openReminder(row) {
      this.activeRow = row
      this.reminderContent = '请尽快填写最终核定工作量'
      this.reminderVisible = true
    },
    submitReminder() {
      remindDemandWorkload(this.activeRow.id, { content: this.reminderContent }).then(res => {
        if (res.code === 200) {
          this.$message.success('催办已发送')
          this.reminderVisible = false
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定删除该需求吗？', '提示', { type: 'warning' }).then(() => {
        deleteDemandWorkload(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          }
        })
      })
    },
    handleImport(file) {
      importDemandWorkload(file).then(res => {
        if (res.code === 200) {
          const data = res.data || {}
          if (data.failCount > 0) {
            this.$alert((data.failMessages || []).join('\n'), data.message || '导入完成', { type: 'warning' })
          } else {
            this.$message.success(data.message || '导入成功')
          }
          this.loadData()
        }
      })
      return false
    },
    handleTemplate() {
      downloadDemandTemplate().then(res => downloadBlob(res, '工作量核定导入模板.xlsx'))
    },
    handleExport() {
      exportDemandWorkload(this.queryParams).then(res => downloadBlob(res, '工作量核定报表.xlsx'))
    },
    openDetail(row) {
      getDemandWorkloadDetail(row.id).then(res => {
        if (res.code === 200) {
          this.detailData = res.data
          this.detailVisible = true
        }
      })
    },
    statusType(status) {
      if (status === '待填写') return 'warning'
      if (status === '已填写') return ''
      if (status === '已核定') return 'success'
      return 'info'
    }
  }
}
</script>

<style scoped>
.stat-row {
  margin-bottom: 12px;
}

.stat-card {
  height: 86px;
}

.stat-label {
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
}

.stat-value.pending {
  color: #e6a23c;
}

.stat-value.filled {
  color: #409eff;
}

.stat-value.confirmed {
  color: #67c23a;
}

.danger-text {
  color: #f56c6c;
}

.reminder-timeline {
  margin-top: 18px;
}
</style>
