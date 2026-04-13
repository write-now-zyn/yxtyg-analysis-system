<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams" size="small">
        <el-form-item label="编号">
          <el-input v-model="queryParams.orderNo" placeholder="请输入" clearable style="width: 135px" />
        </el-form-item>
        <el-form-item label="发起人">
          <el-input v-model="queryParams.initiatorName" placeholder="请输入" clearable style="width: 90px" />
        </el-form-item>
        <el-form-item label="地市">
          <el-select v-model="queryParams.city" clearable filterable placeholder="请选择" style="width: 90px">
            <el-option v-for="city in cities" :key="city" :label="city" :value="city" />
          </el-select>
        </el-form-item>
        <el-form-item label="月份">
          <el-date-picker
            v-model="queryParams.month"
            type="month"
            placeholder="选择月份"
            value-format="yyyyMM"
            style="width: 110px"
            clearable
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" clearable placeholder="请选择" style="width: 100px">
            <el-option v-for="status in statusOptions" :key="status" :label="status" :value="status" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="搜索工单内容" clearable style="width: 160px" />
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
        <el-upload
          class="upload-btn"
          action="/api/workorder/import"
          accept=".xlsx,.xls"
          :show-file-list="false"
          :on-success="handleImportSuccess"
          :on-error="handleImportError"
          :before-upload="beforeUpload">
          <el-button type="primary" size="small" icon="el-icon-upload2">导入工单</el-button>
        </el-upload>
        <el-button type="danger" size="small" icon="el-icon-delete" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe size="mini" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="orderNo" label="编号" width="150">
          <template slot-scope="scope">
            <el-link type="primary" @click="handleViewDetail(scope.row)">{{ scope.row.orderNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="initiatorName" label="发起人" width="100" />
        <el-table-column prop="city" label="地市" width="80" />
        <el-table-column prop="content" label="工单内容" min-width="300">
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.content" placement="top" :disabled="!scope.row.content || scope.row.content.length < 20" :open-delay="500" popper-class="content-tooltip">
              <span class="content-text single-line">{{ scope.row.content || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag size="small" :type="getStatusType(scope.row.status)">{{ scope.row.status || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template slot-scope="scope">
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

    <!-- 详情弹窗 -->
    <el-dialog title="工单详情" :visible.sync="detailVisible" width="800px" top="5vh">
      <div v-if="detailData" class="detail-container">
        <!-- 基本信息 -->
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="BOMC流水号">{{ detailData.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="申告电话">{{ detailData.declarePhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理单位">{{ detailData.handleUnit || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ detailData.handlerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申告地市">{{ detailData.city || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailData.createTimeStr || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发起人姓名">{{ detailData.initiatorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="small">{{ detailData.status || '-' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理满意度评分">{{ detailData.handleSatisfactionScore || '-' }}</el-descriptions-item>
          <el-descriptions-item label="派单满意度评分">{{ detailData.dispatchSatisfactionScore || '-' }}</el-descriptions-item>
          <el-descriptions-item label="不满意原因" :span="2">{{ detailData.unsatisfiedReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="工单内容" :span="2">
            <div class="content-box">{{ detailData.content || '-' }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 解决方案历史（折叠面板） -->
        <el-collapse v-if="detailData.solutionHistoryList && detailData.solutionHistoryList.length > 0" style="margin-top: 20px">
          <el-collapse-item>
            <template slot="title">
              <span class="collapse-title">解决方案历史（{{ detailData.solutionHistoryList.length }}条记录）</span>
            </template>
            <el-timeline>
              <el-timeline-item v-for="item in detailData.solutionHistoryList" :key="item.id" :timestamp="item.handlerName" placement="top">
                <el-card shadow="hover" size="small">
                  <div>{{ item.opinionContent }}</div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </el-collapse-item>
        </el-collapse>
        <el-empty v-else description="暂无解决方案历史" :image-size="80" style="margin-top: 20px" />
      </div>
      <span slot="footer">
        <el-button @click="detailVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getWorkOrderList, getWorkOrderDetail, deleteWorkOrder, batchDeleteWorkOrder } from '@/api/workorder'

export default {
  name: 'WorkOrder',
  data() {
    return {
      cities: ['南京', '苏州', '无锡', '常州', '镇江', '扬州', '泰州', '南通', '盐城', '淮安', '连云港', '徐州', '宿迁'],
      statusOptions: [], // 状态选项，从后端获取
      loading: false,
      selectedIds: [],
      queryParams: {
        orderNo: '',
        initiatorName: '',
        city: '',
        month: '',
        status: '',
        keyword: '',
        current: 1,
        size: 10
      },
      tableData: [],
      total: 0,
      detailVisible: false,
      detailData: null
    }
  },
  created() {
    this.loadStatusOptions()
    this.loadData()
  },
  methods: {
    loadStatusOptions() {
      // 从后端获取去重的状态列表
      getWorkOrderList({ current: 1, size: 1000 }).then(res => {
        const statusSet = new Set()
        res.data.records.forEach(item => {
          if (item.status) {
            statusSet.add(item.status)
          }
        })
        this.statusOptions = Array.from(statusSet)
      })
    },
    getStatusType(status) {
      if (!status) return 'info'
      if (status === '关闭') return 'info'
      if (status === '已回复') return 'success'
      return '' // 默认蓝色
    },
    loadData() {
      this.loading = true
      getWorkOrderList(this.queryParams).then(res => {
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
        orderNo: '',
        initiatorName: '',
        city: '',
        month: '',
        status: '',
        keyword: '',
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
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id)
    },
    handleViewDetail(row) {
      this.detailVisible = true
      getWorkOrderDetail(row.id).then(res => {
        if (res.code === 200) {
          this.detailData = res.data
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该工单吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteWorkOrder(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          }
        })
      })
    },
    handleBatchDelete() {
      if (this.selectedIds.length === 0) {
        this.$message.warning('请先选择要删除的数据')
        return
      }
      this.$confirm(`确定要删除选中的 ${this.selectedIds.length} 条数据吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        batchDeleteWorkOrder(this.selectedIds).then(res => {
          if (res.code === 200) {
            this.$message.success('批量删除成功')
            this.selectedIds = []
            this.loadData()
          }
        })
      })
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
        this.$message.success(res.data.message || '导入成功')
        this.loadData()
      } else {
        this.$message.warning(res.message || '导入失败')
      }
    },
    handleImportError(err) {
      this.$message.error('导入失败，请检查文件格式')
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
  display: flex;
  align-items: center;
}

.upload-btn {
  display: inline-block;
  margin-right: 10px;
}

.content-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.content-text.single-line {
  -webkit-line-clamp: 1;
}

.detail-container {
  padding: 10px;
}

.content-box {
  max-height: 150px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.collapse-title {
  font-weight: bold;
  font-size: 14px;
}
</style>

<style>
/* tooltip宽度限制（全局样式） */
.content-tooltip {
  max-width: 400px;
  word-break: break-all;
}
</style>
