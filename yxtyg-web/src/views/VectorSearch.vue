<template>
  <div class="page-container">
    <!-- 统计信息 -->
    <el-card class="stats-card">
      <div class="stats-row">
        <div class="stat-item">
          <div class="stat-value">{{ stats.vectorizedCount || 0 }}</div>
          <div class="stat-label">已向量化</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.vectorizableCount || 0 }}</div>
          <div class="stat-label">可向量化</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalWorkOrderCount || 0 }}</div>
          <div class="stat-label">工单总数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.lastSyncTime || '未同步' }}</div>
          <div class="stat-label">最近同步</div>
        </div>
        <div class="stat-actions">
          <el-button type="primary" size="small" icon="el-icon-refresh" :loading="syncLoading" @click="handleSync">
            {{ syncLoading ? '同步中...' : '增量同步' }}
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <div class="search-row">
        <el-input
          v-model="content"
          type="textarea"
          :rows="3"
          placeholder="请粘贴工单内容进行向量搜索..."
          maxlength="5000"
          show-word-limit
        />
        <el-button type="primary" icon="el-icon-search" :loading="searchLoading" :disabled="!content.trim()" @click="handleSearch" style="margin-top: 10px">
          向量搜索
        </el-button>
      </div>
    </el-card>

    <!-- 搜索结果 -->
    <el-card v-if="searchResults.length > 0 || searchLoading" class="result-card">
      <div slot="header" class="card-header">
        <i class="el-icon-document-copy"></i>
        <span>Top10 相似工单</span>
      </div>
      <el-table :data="searchResults" border stripe size="mini" v-loading="searchLoading">
        <el-table-column type="index" label="#" width="45" />
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
    </el-card>

    <!-- 空状态 -->
    <el-card v-if="searched && searchResults.length === 0 && !searchLoading" class="empty-card">
      <el-empty description="未找到相似工单" :image-size="100" />
    </el-card>
  </div>
</template>

<script>
import { vectorMatch, getVectorStats, syncVector } from '@/api/vector'

export default {
  name: 'VectorSearch',
  data() {
    return {
      content: '',
      searchLoading: false,
      syncLoading: false,
      searched: false,
      searchResults: [],
      stats: {
        vectorizedCount: 0,
        vectorizableCount: 0,
        totalWorkOrderCount: 0,
        lastSyncTime: ''
      }
    }
  },
  created() {
    this.loadStats()
  },
  methods: {
    loadStats() {
      getVectorStats().then(res => {
        if (res.code === 200) {
          this.stats = res.data || {}
        }
      })
    },
    handleSearch() {
      if (!this.content.trim()) return
      this.searchLoading = true
      this.searched = false
      vectorMatch({ content: this.content.trim() }).then(res => {
        if (res.code === 200) {
          // VectorMatchResultVO: { similarTickets: [...] }
          const data = res.data
          this.searchResults = (data && data.similarTickets) ? data.similarTickets : []
        }
      }).finally(() => {
        this.searchLoading = false
        this.searched = true
      })
    },
    handleSync() {
      this.syncLoading = true
      syncVector().then(res => {
        if (res.code === 200) {
          this.$message.success('同步完成')
          this.loadStats()
        }
      }).finally(() => {
        this.syncLoading = false
      })
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

.stats-card {
  margin-bottom: 12px;
}

.stats-row {
  display: flex;
  align-items: center;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 22px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.stat-actions {
  margin-left: auto;
}

.search-card {
  margin-bottom: 12px;
}

.search-row {
  display: flex;
  flex-direction: column;
}

.result-card {
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

.empty-card {
  text-align: center;
}
</style>
