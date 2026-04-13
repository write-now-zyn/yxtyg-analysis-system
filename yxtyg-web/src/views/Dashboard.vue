<template>
  <div class="dashboard-container">
    <!-- 顶部区域标题 -->
    <!-- <div class="section-header">
      <i class="el-icon-data-board"></i>
      <span>达标情况总览</span>
    </div> -->

    <!-- 月份选择器 -->
    <div class="month-selector">
      <span class="label">选择月份：</span>
      <el-date-picker
        v-model="selectedMonth"
        type="month"
        placeholder="选择月份"
        format="yyyy年MM月"
        value-format="yyyyMM"
        @change="loadData"
      />
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-container">
      <el-spinner />
    </div>

    <!-- 数据展示 -->
    <div v-else-if="dashboardData" class="dashboard-content">
      <!-- 三个总览卡片同一行 -->
      <el-row :gutter="20" class="overview-row">
        <!-- 需求提单统计 -->
        <el-col :span="8">
          <el-card class="overview-card" shadow="hover">
            <div class="card-chart">
              <div :id="'demand-chart'" class="pie-chart"></div>
            </div>
            <div class="card-info">
              <div class="card-title-row">
                <i class="el-icon-document"></i>
                <span class="card-title">需求提单</span>
              </div>
              <div class="card-standard">达标标准：≥{{ statistics.demand.standard }}次</div>
              <div class="card-rate" :class="getRateClass(getReachedRate(statistics.demand))">
                达标率：{{ getReachedRate(statistics.demand) }}%
              </div>
            </div>
            <div class="card-divider"></div>
            <div class="card-cities">
              <template v-if="statistics.demand.unreachedCities.length > 0">
                <div class="cities-label">
                  <i class="el-icon-warning"></i>
                  <span>未达标地市：</span>
                </div>
                <div class="cities-tags">
                  <span
                    v-for="item in statistics.demand.unreachedCities"
                    :key="item.city"
                    class="city-tag unreached"
                  >
                    {{ item.city }}
                  </span>
                </div>
              </template>
              <template v-else>
                <div class="all-reached">
                  <i class="el-icon-circle-check"></i>
                  <span>全部达标！</span>
                </div>
              </template>
            </div>
          </el-card>
        </el-col>

        <!-- 评审参与统计 -->
        <el-col :span="8">
          <el-card class="overview-card" shadow="hover">
            <div class="card-chart">
              <div :id="'review-chart'" class="pie-chart"></div>
            </div>
            <div class="card-info">
              <div class="card-title-row">
                <i class="el-icon-edit-outline"></i>
                <span class="card-title">评审参与</span>
              </div>
              <div class="card-standard">达标标准：≥{{ statistics.review.standard }}次</div>
              <div class="card-rate" :class="getRateClass(getReachedRate(statistics.review))">
                达标率：{{ getReachedRate(statistics.review) }}%
              </div>
            </div>
            <div class="card-divider"></div>
            <div class="card-cities">
              <template v-if="statistics.review.unreachedCities.length > 0">
                <div class="cities-label">
                  <i class="el-icon-warning"></i>
                  <span>未达标地市：</span>
                </div>
                <div class="cities-tags">
                  <span
                    v-for="item in statistics.review.unreachedCities"
                    :key="item.city"
                    class="city-tag unreached"
                  >
                    {{ item.city }}
                  </span>
                </div>
              </template>
              <template v-else>
                <div class="all-reached">
                  <i class="el-icon-circle-check"></i>
                  <span>全部达标！</span>
                </div>
              </template>
            </div>
          </el-card>
        </el-col>

        <!-- 培训上报统计 -->
        <el-col :span="8">
          <el-card class="overview-card" shadow="hover">
            <div class="card-chart">
              <div :id="'training-chart'" class="pie-chart"></div>
            </div>
            <div class="card-info">
              <div class="card-title-row">
                <i class="el-icon-s-management"></i>
                <span class="card-title">培训上报</span>
              </div>
              <div class="card-standard">达标标准：≥{{ statistics.training.standard }}次</div>
              <div class="card-rate" :class="getRateClass(getReachedRate(statistics.training))">
                达标率：{{ getReachedRate(statistics.training) }}%
              </div>
            </div>
            <div class="card-divider"></div>
            <div class="card-cities">
              <template v-if="statistics.training.unreachedCities.length > 0">
                <div class="cities-label">
                  <i class="el-icon-warning"></i>
                  <span>未达标地市：</span>
                </div>
                <div class="cities-tags">
                  <span
                    v-for="item in statistics.training.unreachedCities"
                    :key="item.city"
                    class="city-tag unreached"
                  >
                    {{ item.city }}
                  </span>
                </div>
              </template>
              <template v-else>
                <div class="all-reached">
                  <i class="el-icon-circle-check"></i>
                  <span>全部达标！</span>
                </div>
              </template>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 排行榜区域 -->
      <el-row :gutter="20" class="ranking-row">
        <!-- 需求提单排行 -->
        <el-col :span="8">
          <el-card class="ranking-card">
            <div slot="header" class="card-header">
              <span class="card-title">需求提单排行</span>
              <span class="card-avg">均值：{{ getAverage(statistics.demand.ranking) }}次</span>
            </div>
            <el-table :data="statistics.demand.ranking" size="small" max-height="300">
              <el-table-column type="index" label="排名" width="60" />
              <el-table-column prop="city" label="地市" />
              <el-table-column prop="count" label="提单数">
                <template slot-scope="scope">
                  <span :class="{ 'highlight-reached': scope.row.reached }">
                    {{ scope.row.count }}
                  </span>
                  <el-tag v-if="scope.row.reached" type="success" size="mini">达标</el-tag>
                  <el-tag v-else type="danger" size="mini">未达标</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <!-- 评审参与排行 -->
        <el-col :span="8">
          <el-card class="ranking-card">
            <div slot="header" class="card-header">
              <span class="card-title">评审参与排行</span>
              <span class="card-avg">均值：{{ getAverage(statistics.review.ranking) }}次</span>
            </div>
            <el-table :data="statistics.review.ranking" size="small" max-height="300">
              <el-table-column type="index" label="排名" width="60" />
              <el-table-column prop="city" label="地市" />
              <el-table-column prop="count" label="参与数">
                <template slot-scope="scope">
                  <span :class="{ 'highlight-reached': scope.row.reached }">
                    {{ scope.row.count }}
                  </span>
                  <el-tag v-if="scope.row.reached" type="success" size="mini">达标</el-tag>
                  <el-tag v-else type="danger" size="mini">未达标</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <!-- 培训上报排行 -->
        <el-col :span="8">
          <el-card class="ranking-card">
            <div slot="header" class="card-header">
              <span class="card-title">培训上报排行</span>
              <span class="card-avg">均值：{{ getAverage(statistics.training.ranking) }}次</span>
            </div>
            <el-table :data="statistics.training.ranking" size="small" max-height="300">
              <el-table-column type="index" label="排名" width="60" />
              <el-table-column prop="city" label="地市" />
              <el-table-column prop="count" label="上报数">
                <template slot-scope="scope">
                  <span :class="{ 'highlight-reached': scope.row.reached }">
                    {{ scope.row.count }}
                  </span>
                  <el-tag v-if="scope.row.reached" type="success" size="mini">达标</el-tag>
                  <el-tag v-else type="danger" size="mini">未达标</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { getDashboardOverview } from '@/api/dashboard'
import * as echarts from 'echarts'

export default {
  name: 'Dashboard',
  data() {
    return {
      loading: false,
      selectedMonth: this.getCurrentMonth(),
      dashboardData: null,
      charts: {
        demand: null,
        review: null,
        training: null
      }
    }
  },
  computed: {
    statistics() {
      return this.dashboardData?.statistics || {
        demand: { standard: 6, reachedCount: 0, totalCount: 0, unreachedCities: [], ranking: [] },
        review: { standard: 2, reachedCount: 0, totalCount: 0, unreachedCities: [], ranking: [] },
        training: { standard: 8, reachedCount: 0, totalCount: 0, unreachedCities: [], ranking: [] }
      }
    }
  },
  watch: {
    dashboardData: {
      handler() {
        this.$nextTick(() => {
          this.initCharts()
        })
      },
      deep: true
    }
  },
  created() {
    this.loadData()
  },
  beforeDestroy() {
    this.disposeCharts()
  },
  methods: {
    getCurrentMonth() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      return `${year}${month}`
    },
    getAverage(ranking) {
      if (!ranking || ranking.length === 0) return 0
      const sum = ranking.reduce((acc, item) => acc + (item.count || 0), 0)
      return (sum / ranking.length).toFixed(1)
    },
    getReachedRate(stat) {
      if (!stat.totalCount || stat.totalCount === 0) return 0
      return Math.round((stat.reachedCount / stat.totalCount) * 100)
    },
    getRateClass(rate) {
      if (rate >= 80) return 'rate-high'
      if (rate >= 50) return 'rate-medium'
      return 'rate-low'
    },
    async loadData() {
      this.loading = true
      try {
        const res = await getDashboardOverview({ month: this.selectedMonth })
        if (res.code === 200) {
          this.dashboardData = res.data
        } else {
          this.$message.error(res.message || '获取数据失败')
        }
      } catch (error) {
        console.error('获取看板数据失败：', error)
        this.$message.error('获取数据失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    initCharts() {
      this.initChart('demand', this.statistics.demand)
      this.initChart('review', this.statistics.review)
      this.initChart('training', this.statistics.training)
    },
    initChart(type, data) {
      const chartDom = document.getElementById(`${type}-chart`)
      if (!chartDom) return

      if (this.charts[type]) {
        this.charts[type].dispose()
      }

      const myChart = echarts.init(chartDom)
      this.charts[type] = myChart

      const reached = data.reachedCount || 0
      const unreached = data.unreachedCities?.length || 0
      const total = data.totalCount || 0

      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}个地市 ({d}%)'
        },
        legend: {
          show: false
        },
        series: [
          {
            type: 'pie',
            radius: ['55%', '75%'],
            center: ['50%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 6,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: true,
              position: 'center',
              formatter: () => {
                return `{a|${reached}}/{b|${total}}`
              },
              rich: {
                a: {
                  fontSize: 26,
                  fontWeight: 'bold',
                  color: '#303133',
                  lineHeight: 36
                },
                b: {
                  fontSize: 14,
                  color: '#909399'
                }
              }
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 26,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: [
              {
                value: reached,
                name: '达标',
                itemStyle: {
                  color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                    { offset: 0, color: '#67C23A' },
                    { offset: 1, color: '#95D475' }
                  ])
                }
              },
              {
                value: unreached,
                name: '未达标',
                itemStyle: {
                  color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                    { offset: 0, color: '#F56C6C' },
                    { offset: 1, color: '#FAB6B6' }
                  ])
                }
              }
            ]
          }
        ]
      }

      myChart.setOption(option)
    },
    disposeCharts() {
      Object.values(this.charts).forEach(chart => {
        if (chart) {
          chart.dispose()
        }
      })
    }
  }
}
</script>

<style scoped>

.dashboard-container {
  padding: 0;
}

.section-header {
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  color: #fff;
  padding: 15px 20px;
  font-size: 18px;
  font-weight: bold;
  border-radius: 4px;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-header i {
  font-size: 20px;
}

.month-selector {
  margin-bottom: 20px;
  background: #fff;
  padding: 15px 20px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.month-selector .label {
  font-weight: bold;
  margin-right: 10px;
  color: #606266;
}

.loading-container {
  text-align: center;
  padding: 100px 0;
}

.overview-row {
  margin-bottom: 20px;
}

.overview-card {
  height: 100%;
  border: none;
  border-radius: 8px;
  min-height: 380px;
}

.overview-card:hover {
  transform: translateY(-2px);
  transition: all 0.3s ease;
}

.card-chart {
  display: flex;
  justify-content: center;
  align-items: center;
  /* padding: 10px 0; */
}

.pie-chart {
  width: 140px;
  height: 140px;
}

.card-info {
  text-align: center;
  padding: 0 10px;
}

.card-title-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 6px;
}

.card-title-row i {
  color: #409EFF;
  font-size: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.card-standard {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.card-rate {
  font-size: 18px;
  font-weight: bold;
}

.rate-high {
  color: #67C23A;
}

.rate-medium {
  color: #E6A23C;
}

.rate-low {
  color: #F56C6C;
}

.card-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #E4E7ED, transparent);
  margin: 12px 15px;
}

.card-cities {
  padding: 0 15px 15px;
  min-height: 80px;
}

.cities-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 10px;
}

.cities-label i {
  color: #E6A23C;
}

.cities-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.city-tag {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  display: inline-block;
  line-height: 18px;
}

.city-tag.unreached {
  background: linear-gradient(135deg, #FEF0F0 0%, #FFEBEB 100%);
  border: 1px solid #FBC4C4;
  color: #F56C6C;
}

.all-reached {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #67C23A;
  font-size: 14px;
  font-weight: 500;
  padding: 15px 0;
}

.all-reached i {
  font-size: 18px;
}

.ranking-row {
  margin-top: 20px;
}

.ranking-card {
  height: 100%;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-avg {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.highlight-reached {
  color: #67C23A;
  font-weight: bold;
}
</style>
