<template>
  <div class="page-container">
    <!-- 输入区域 -->
    <el-card class="input-card">
      <div slot="header" class="card-header">
        <span>智能推荐</span>
        <span class="header-tip">粘贴新工单内容，系统将基于历史经验推荐处理人</span>
      </div>
      <el-input
        v-model="content"
        type="textarea"
        :rows="4"
        placeholder="请粘贴待处理的工单内容..."
        maxlength="5000"
        show-word-limit
      />
      <div class="action-bar">
        <el-button type="primary" icon="el-icon-magic-stick" :loading="recommendLoading" :disabled="!content.trim()" @click="handleRecommend">
          {{ recommendLoading ? '分析中，请稍候...' : '智能推荐' }}
        </el-button>
        <el-button icon="el-icon-refresh" @click="handleClear">清空</el-button>
      </div>
    </el-card>

    <!-- 推荐结果 -->
    <div v-if="recommendResult" class="result-section">
      <!-- 推荐理由 -->
      <el-card class="reason-card">
        <div slot="header" class="card-header">
          <i class="el-icon-chat-line-square"></i>
          <span>推荐理由</span>
        </div>
        <div class="reason-text">{{ recommendResult.reason }}</div>
      </el-card>

      <!-- 推荐处理人 -->
      <el-card class="person-card">
        <div slot="header" class="card-header">
          <i class="el-icon-user"></i>
          <span>推荐处理人</span>
        </div>
        <div class="person-list">
          <div v-for="(person, index) in recommendResult.recommendPersons" :key="index" class="person-item">
            <div class="person-rank">{{ index + 1 }}</div>
            <div class="person-info">
              <div class="person-name">
                {{ person.name }}
                <el-tag v-if="index === 0" type="danger" size="mini" effect="dark" style="margin-left: 6px">最佳</el-tag>
              </div>
              <div class="person-reason">{{ person.reason }}</div>
            </div>
            <div class="person-meta">
              <div class="confidence-value">{{ (person.confidence * 100).toFixed(0) }}%</div>
              <div class="confidence-label">置信度</div>
              <div v-if="person.ticketCount" class="ticket-count">{{ person.ticketCount }}单</div>
            </div>
          </div>
        </div>
        <el-empty v-if="!recommendResult.recommendPersons || recommendResult.recommendPersons.length === 0" description="暂无推荐处理人" :image-size="60" />
      </el-card>

      <!-- 相似工单 -->
      <el-card class="similar-card">
        <div slot="header" class="card-header">
          <i class="el-icon-document-copy"></i>
          <span>相似工单参考</span>
        </div>
        <el-table :data="recommendResult.similarTickets" border stripe size="mini">
          <el-table-column prop="orderNo" label="工单号" width="150" />
          <el-table-column prop="handlerName" label="处理人" width="90" />
          <el-table-column prop="city" label="地市" width="80" />
          <el-table-column prop="status" label="状态" width="80">
            <template slot-scope="scope">
              <el-tag size="mini" :type="getStatusType(scope.row.status)">{{ scope.row.status || '-' }}</el-tag>
            </template>
          </el-table-column>
        <el-table-column prop="contentSummary" label="工单内容" min-width="250">
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.contentSummary" placement="top" :disabled="!scope.row.contentSummary || scope.row.contentSummary.length < 30" :open-delay="500" popper-class="content-tooltip">
              <span class="content-ellipsis">{{ scope.row.contentSummary || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
          <el-table-column prop="solutionSummary" label="解决方案摘要" min-width="200">
            <template slot-scope="scope">
              <span class="content-ellipsis">{{ scope.row.solutionSummary || '-' }}</span>
            </template>
          </el-table-column>
        <el-table-column prop="similarityScore" label="相似度" width="80">
          <template slot-scope="scope">
            <span class="score-text">{{ scope.row.similarityScore ? (scope.row.similarityScore * 100).toFixed(1) + '%' : '-' }}</span>
          </template>
        </el-table-column>
        </el-table>
        <el-empty v-if="!recommendResult.similarTickets || recommendResult.similarTickets.length === 0" description="暂无相似工单" :image-size="60" />
      </el-card>
    </div>
  </div>
</template>

<script>
import { recommend } from '@/api/vector'

export default {
  name: 'SmartRecommend',
  data() {
    return {
      content: '',
      recommendLoading: false,
      recommendResult: null
    }
  },
  methods: {
    handleRecommend() {
      if (!this.content.trim()) return
      this.recommendLoading = true
      this.recommendResult = null
      recommend({ content: this.content.trim() }).then(res => {
        if (res.code === 200) {
          this.recommendResult = res.data
          if (!this.recommendResult.recommendPersons || this.recommendResult.recommendPersons.length === 0) {
            this.$message.warning('未找到推荐处理人，请尝试向量搜索')
          }
        }
      }).finally(() => {
        this.recommendLoading = false
      })
    },
    handleClear() {
      this.content = ''
      this.recommendResult = null
    },
    getStatusType(status) {
      if (!status) return 'info'
      if (status === '关闭') return 'info'
      if (status === '已回复') return 'success'
      return ''
    }
  }
}
</script>

<style scoped>
.page-container {
  padding: 0;
}

.input-card {
  margin-bottom: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: bold;
  font-size: 14px;
}

.card-header i {
  margin-right: 6px;
}

.header-tip {
  font-weight: normal;
  font-size: 12px;
  color: #909399;
  margin-left: 12px;
}

.action-bar {
  margin-top: 12px;
  display: flex;
  align-items: center;
}

.result-section {
  margin-top: 12px;
}

.result-section .el-card {
  margin-bottom: 12px;
}

.reason-card {
  margin-bottom: 12px;
}

.reason-text {
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-all;
}

.person-card {
  margin-bottom: 12px;
}

.person-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.person-item {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 280px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  transition: border-color 0.3s;
}

.person-item:hover {
  border-color: #409EFF;
}

.person-item:first-child {
  border-color: #f56c6c;
  background: #fef0f0;
}

.person-rank {
  width: 32px;
  height: 32px;
  line-height: 32px;
  text-align: center;
  border-radius: 50%;
  background: #909399;
  color: #fff;
  font-weight: bold;
  font-size: 14px;
  flex-shrink: 0;
  margin-right: 12px;
}

.person-item:first-child .person-rank {
  background: #f56c6c;
}

.person-info {
  flex: 1;
  min-width: 0;
}

.person-name {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
}

.person-reason {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.person-meta {
  text-align: center;
  margin-left: 12px;
  flex-shrink: 0;
}

.confidence-value {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.confidence-label {
  font-size: 11px;
  color: #909399;
}

.ticket-count {
  font-size: 11px;
  color: #67c23a;
  margin-top: 2px;
}

.content-ellipsis {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.score-text {
  color: #409EFF;
  font-weight: bold;
  font-size: 13px;
}
</style>
