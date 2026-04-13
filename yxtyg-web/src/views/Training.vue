<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams" size="small">
        <el-form-item label="地市">
          <el-select v-model="queryParams.city" clearable filterable placeholder="请选择地市" style="width: 150px">
            <el-option v-for="city in cities" :key="city" :label="city" :value="city" />
          </el-select>
        </el-form-item>
        <el-form-item label="培训月份">
          <el-date-picker v-model="queryParams.reportMonth" type="month" placeholder="请选择月份" value-format="yyyy-MM" style="width: 150px" />
        </el-form-item>
        <el-form-item label="培训内容">
          <el-input v-model="queryParams.trainingContent" placeholder="请输入培训内容关键词" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="培训主体">
          <el-input v-model="queryParams.trainingSubject" placeholder="请输入培训主体关键词" clearable style="width: 150px" />
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
        <!--
        <el-button type="success" size="small" icon="el-icon-download" @click="handleExport">导出</el-button>
        -->
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe size="small" v-loading="loading">
        <el-table-column prop="city" label="地市" width="150" />
        <el-table-column prop="reportMonth" label="培训月份" width="150" />
        <el-table-column prop="recordCount" label="培训次数" width="150" align="center" />
        <el-table-column prop="trainingPersonCount" label="覆盖人次" width="150" align="center" />
        <el-table-column prop="reportTime" label="上报时间" width="150" />
        <el-table-column prop="reporter" label="上报人" width="100" />
        <el-table-column label="操作" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleViewDetail(scope.row)">查看详情</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog title="培训记录详情" :visible.sync="detailVisible" width="950px">
      <el-table :data="detailData.records" border stripe size="small" max-height="500">
        <el-table-column type="index" label="序号" width="45" />
        <el-table-column prop="trainingTime" label="培训时间" width="100" />
        <el-table-column prop="organizer" label="组织人员" width="100" />
        <el-table-column prop="trainingSubject" label="培训主体" width="150" />
        <el-table-column prop="coverageCount" label="覆盖人数" width="80" align="center" />
        <el-table-column prop="trainingContent" label="培训内容" min-width="150" show-overflow-tooltip />
        <el-table-column label="培训截图" width="100">
          <template slot-scope="scope">
            <el-button v-if="scope.row.hasScreenshot" type="text" size="small" @click="viewImage(scope.row)">查看</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 图片预览弹窗 -->
    <el-dialog :visible.sync="imageVisible" width="800px" top="15vh" custom-class="image-dialog" :show-close="true">
      <div class="image-toolbar">
        <el-button size="mini" :type="imageFullSize ? 'primary' : 'default'" @click="toggleImageSize">
          <i :class="imageFullSize ? 'el-icon-zoom-out' : 'el-icon-zoom-in'"></i>
          {{ imageFullSize ? '适应屏幕' : '原始大小' }}
        </el-button>
      </div>
      <div v-loading="imageLoading" element-loading-text="图片加载中..." class="image-container" :class="{ 'full-size': imageFullSize }">
        <img v-if="currentImage" :src="currentImage" @load="onImageLoad" @error="onImageError" />
        <div v-else-if="!imageLoading" style="text-align: center; color: #909399;">暂无图片</div>
      </div>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="1200px" top="5vh">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" size="small">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="地市" prop="city">
              <el-select v-model="form.city" placeholder="请选择地市" style="width: 100%">
                <el-option v-for="city in cities" :key="city" :label="city" :value="city" />
              </el-select>
            </el-form-item>
          </el-col>
            <el-col :span="8">
            <el-form-item label="培训月份" prop="reportMonth">
            <el-date-picker v-model="form.reportMonth" type="month" placeholder="请选择月份" value-format="yyyy-MM" style="width: 100%" />
            </el-form-item>
            </el-col>
          <el-col :span="8">
            <el-form-item label="上报时间" prop="reportTime">
              <el-date-picker v-model="form.reportTime" type="date" placeholder="请选择上报时间" value-format="yyyy-MM-dd" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="上报人" prop="reporter">
              <el-input v-model="form.reporter" placeholder="请输入上报人" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">培训记录</el-divider>

        <!-- 培训记录表格形式 -->
        <el-table :data="form.records" border stripe size="small" max-height="400">
          <el-table-column type="index" label="序号" width="45" />
          <el-table-column label="培训时间" width="150">
            <template slot-scope="scope">
              <el-date-picker v-model="scope.row.trainingTime" type="date" placeholder="培训时间" value-format="yyyy-MM-dd" style="width: 100%" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="组织人员" width="120">
            <template slot-scope="scope">
              <el-input v-model="scope.row.organizer" placeholder="组织人员" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="培训主体" width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.trainingSubject" placeholder="培训主体" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="覆盖人数" width="150">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.coverageCount" :min="0" size="small" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="培训内容" min-width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.trainingContent" placeholder="培训内容" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="培训截图" width="250">
            <template slot-scope="scope">
              <div
                class="upload-cell"
                tabindex="0"
                @paste="handlePasteImage($event, scope.row, scope.$index)"
                @click="focusCell($event)"
              >
                <el-upload
                  action="#"
                  :show-file-list="false"
                  :before-upload="(file) => handleImageUpload(file, scope.row)"
                  accept="image/*"
                >
                  <el-button size="mini" type="primary" icon="el-icon-upload">上传</el-button>
                  <div class="paste-hint">
                    <i class="el-icon-document-copy"></i>
                    <span>点击后粘贴</span>
                  </div>
                </el-upload>

                <div v-if="scope.row.screenshotName || scope.row.hasScreenshot" class="file-info">
                  <i class="el-icon-document"></i>
                  <span :title="scope.row.screenshotName">{{ (scope.row.screenshotName || '已有图片').substring(0, 8) }}...</span>
                  <el-button type="text" size="mini" @click.stop="viewImage(scope.row)">查看</el-button>
                  <el-button type="text" size="mini" style="color: #f56c6c" @click.stop="clearImage(scope.row, scope.$index)">删除</el-button>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60">
            <template slot-scope="scope">
              <el-button type="text" size="small" style="color: #f56c6c" @click="removeRecord(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="addRecord" style="margin-top: 10px">添加培训记录</el-button>
        <el-button type="success" size="small" icon="el-icon-magic-stick" @click="showAiDialog" style="margin-top: 10px">AI识别</el-button>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </span>
    </el-dialog>

    <!-- AI识别弹窗 -->
    <el-dialog title="AI识别培训记录" :visible.sync="aiDialogVisible" width="800px" append-to-body>
      <el-input
        v-model="aiInputText"
        type="textarea"
        :rows="15"
        placeholder="请粘贴培训记录表格文本，例如：&#10;地市	组织人员	辅导主题	辅导时间	覆盖人数&#10;淮安	政企客户部	开门红商客培训及复盘	20260302	90&#10;淮安	数智化支撑中心	磐匠数智员工培训	20260302	16&#10;..."
      />
      <div slot="footer">
        <el-button @click="aiDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="aiLoading" @click="handleAiParse">识别</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getTrainingReportList, getTrainingReportDetail, addTrainingReport, updateTrainingReport, deleteTrainingReport, parseTrainingRecords } from '@/api/training'

export default {
  name: 'Training',
  data() {
    return {
      cities: ['南京', '苏州', '无锡', '常州', '镇江', '扬州', '泰州', '南通', '盐城', '淮安', '连云港', '徐州', '宿迁'],
      loading: false,
      submitLoading: false,
      imageLoading: false, // 图片加载状态
      queryParams: {
        city: '',
        reportMonth: '',
        trainingContent: '',
        trainingSubject: '',
        current: 1,
        size: 10
      },
      tableData: [],
      total: 0,
      detailVisible: false,
      detailData: { records: [] },
      imageVisible: false,
      imageFullSize: false, // 图片是否显示原始大小
      currentImage: '',
      dialogVisible: false,
      dialogTitle: '',
      form: {
        id: null,
        city: '',
        reportMonth: '',
        reportTime: '',
        reporter: '',
        records: []
      },
      rules: {
        city: [{ required: true, message: '请选择地市', trigger: 'change' }],
        reportMonth: [{ required: true, message: '请选择培训月份', trigger: 'change' }],
        reportTime: [{ required: true, message: '请选择上报时间', trigger: 'change' }]
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
      getTrainingReportList(this.queryParams).then(res => {
        this.tableData = res.data.records
        this.total = res.data.total
      }).finally(() => {
        this.loading = false
      })
    },
    formatDateTime(str) {
      if (!str) return ''
      return str.replace('T', ' ').substring(0, 16)
    },
    handleSearch() {
      this.queryParams.current = 1
      this.loadData()
    },
    handleReset() {
      this.queryParams = {
        city: '',
        reportMonth: '',
        trainingContent: '',
        trainingSubject: '',
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
    handleViewDetail(row) {
      getTrainingReportDetail(row.id).then(res => {
        this.detailData = res.data
        this.detailVisible = true
      })
    },
    // 异步加载图片（点击查看时才加载）
    viewImage(row) {
      // 先清空图片，避免显示上一次的图片
      this.currentImage = ''
      this.imageFullSize = false // 重置为适应屏幕模式
      
      // 如果已有base64数据（新上传的），直接显示
      if (row.screenshotBase64) {
        this.imageLoading = true
        this.imageVisible = true
        this.$nextTick(() => {
          this.currentImage = `data:${row.screenshotType || 'image/jpeg'};base64,${row.screenshotBase64}`
        })
        return
      }
      // 否则异步加载图片（检查 id 是否有效）
      if (row.hasScreenshot && row.id && typeof row.id === 'number') {
        this.imageLoading = true
        this.imageVisible = true
        this.$nextTick(() => {
          this.currentImage = `/api/training/record/image/${row.id}`
        })
      } else if (row.hasScreenshot) {
        this.$message.warning('请先保存后再查看图片')
      }
    },
    // 切换图片大小
    toggleImageSize() {
      this.imageFullSize = !this.imageFullSize
    },
    // 图片加载完成
    onImageLoad() {
      this.imageLoading = false
    },
    // 图片加载失败
    onImageError() {
      this.imageLoading = false
      this.$message.error('图片加载失败')
      this.imageVisible = false
    },
    // 图片压缩方法
    async compressImage(file, maxSizeMB = 3) {
      return new Promise((resolve, reject) => {
        // 如果文件已经小于目标大小，直接返回
        if (file.size <= maxSizeMB * 1024 * 1024) {
          resolve(file)
          return
        }
        
        const reader = new FileReader()
        reader.onload = (e) => {
          const img = new Image()
          img.onload = () => {
            const canvas = document.createElement('canvas')
            const ctx = canvas.getContext('2d')
            
            let width = img.width
            let height = img.height
            
            // 如果图片尺寸过大，先缩放
            const maxDimension = 4096
            if (width > maxDimension || height > maxDimension) {
              if (width > height) {
                height = Math.round(height * maxDimension / width)
                width = maxDimension
              } else {
                width = Math.round(width * maxDimension / height)
                height = maxDimension
              }
            }
            
            canvas.width = width
            canvas.height = height
            ctx.drawImage(img, 0, 0, width, height)
            
            const maxSize = maxSizeMB * 1024 * 1024
            let quality = 0.9
            
            const tryCompress = () => {
              canvas.toBlob((blob) => {
                if (!blob) {
                  reject(new Error('压缩失败'))
                  return
                }
                if (blob.size <= maxSize || quality <= 0.1) {
                  resolve(blob)
                } else {
                  quality -= 0.1
                  ctx.drawImage(img, 0, 0, width, height)
                  canvas.toBlob(tryCompress, 'image/jpeg', quality)
                }
              }, 'image/jpeg', quality)
            }
            tryCompress()
          }
          img.onerror = () => reject(new Error('图片加载失败'))
          img.src = e.target.result
        }
        reader.onerror = () => reject(new Error('文件读取失败'))
        reader.readAsDataURL(file)
      })
    },
    createEmptyRecord() {
      return {
        trainingTime: '',
        organizer: '',
        trainingSubject: '',
        coverageCount: null,
        trainingContent: '',
        screenshotBase64: '',
        screenshotName: '',
        screenshotType: '',
        hasScreenshot: false
      }
    },
    handleAdd() {
      this.dialogTitle = '新增培训上报'
      this.form = {
        id: null,
        city: '',
        reportMonth: '',
        reportTime: '',
        reporter: '',
        records: [this.createEmptyRecord()]
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.formRef.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogTitle = '编辑培训上报'
      getTrainingReportDetail(row.id).then(res => {
        this.form = {
          id: res.data.id,
          city: res.data.city,
          reportMonth: res.data.reportMonth,
          reportTime: res.data.reportTime,
          reporter: res.data.reporter || '',
          records: res.data.records && res.data.records.length > 0 ? res.data.records : [this.createEmptyRecord()]
        }
        this.dialogVisible = true
      })
    },
    handleSubmit() {
      this.$refs.formRef.validate(valid => {
        if (valid) {
          // 校验培训记录
          const validRecords = this.form.records.filter(r => r.trainingTime || r.organizer || r.trainingSubject || r.coverageCount)
          if (validRecords.length === 0) {
            this.$message.error('请至少添加一条培训记录')
            return
          }
          
          this.submitLoading = true
          const api = this.form.id ? updateTrainingReport : addTrainingReport
          api(this.form).then(res => {
            if (res.code === 200) {
              this.$message.success('操作成功')
              this.dialogVisible = false
              this.loadData()
            }
          }).finally(() => {
            this.submitLoading = false
          })
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该培训上报记录吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteTrainingReport(row.id).then(() => {
          this.$message.success('删除成功')
          this.loadData()
        })
      })
    },
    addRecord() {
      this.form.records.push(this.createEmptyRecord())
    },
    removeRecord(index) {
      if (this.form.records.length > 1) {
        this.form.records.splice(index, 1)
      } else {
        this.$message.warning('至少保留一条培训记录')
      }
    },
    // 上传图片（自动压缩到3MB以内）
    handleImageUpload(file, record) {
      // 异步处理图片
      this.compressImage(file, 3).then(compressedFile => {
        const reader = new FileReader()
        reader.onload = (e) => {
          const base64 = e.target.result.split(',')[1]
          record.screenshotBase64 = base64
          record.screenshotName = file.name
          record.screenshotType = 'image/jpeg'
          record.hasScreenshot = true
          this.$message.success(`图片上传成功（已压缩至 ${(compressedFile.size / 1024 / 1024).toFixed(2)}MB）`)
        }
        reader.readAsDataURL(compressedFile)
      }).catch(error => {
        this.$message.error('图片处理失败：' + error.message)
      })
      // 返回 false 阻止 el-upload 的默认上传行为
      return false
    },
    // 点击单元格聚焦，以便接收粘贴事件
    focusCell(event) {
      event.currentTarget.focus()
    },
    // 处理粘贴图片（自动压缩到3MB以内）
    async handlePasteImage(event, record, index) {
      const items = event.clipboardData?.items
      if (!items) return

      for (let i = 0; i < items.length; i++) {
        const item = items[i]
        if (item.type.indexOf('image') !== -1) {
          const file = item.getAsFile()
          if (file) {
            try {
              // 压缩图片
              const compressedFile = await this.compressImage(file, 3)
              
              const reader = new FileReader()
              reader.onload = (e) => {
                const base64 = e.target.result.split(',')[1]
                record.screenshotBase64 = base64
                record.screenshotName = `粘贴图片_${Date.now()}.jpg`
                record.screenshotType = 'image/jpeg'
                record.hasScreenshot = true
                this.$message.success(`图片粘贴成功（已压缩至 ${(compressedFile.size / 1024 / 1024).toFixed(2)}MB）`)
                // 强制更新视图
                this.$set(this.form.records, index, { ...record })
              }
              reader.readAsDataURL(compressedFile)
            } catch (error) {
              this.$message.error('图片处理失败：' + error.message)
            }
          }
          break
        }
      }
      event.preventDefault()
    },
    // 清除图片
    clearImage(record, index) {
      record.screenshotBase64 = ''
      record.screenshotName = ''
      record.screenshotType = ''
      record.hasScreenshot = false
      // 强制更新视图
      this.$set(this.form.records, index, { ...record })
    },
    handleExport() {
      // 过滤空值参数
      const params = {}
      if (this.queryParams.city) params.city = this.queryParams.city
      if (this.queryParams.reportMonth) params.reportMonth = this.queryParams.reportMonth
      if (this.queryParams.trainingContent) params.trainingContent = this.queryParams.trainingContent
      if (this.queryParams.trainingSubject) params.trainingSubject = this.queryParams.trainingSubject
      params.current = 1
      params.size = 99999
      window.open(`/api/training/export?${new URLSearchParams(params).toString()}`)
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
      parseTrainingRecords(this.aiInputText).then(res => {
        if (res.code === 200 && res.data && res.data.length > 0) {
          // 转换数据格式并填充到records
          const newRecords = res.data.map(item => ({
            trainingTime: item.trainingTime || '',
            organizer: item.organizer || '',
            trainingSubject: item.trainingSubject || '',
            trainingContent: item.trainingContent || '',
            coverageCount: item.coverageCount ? parseInt(item.coverageCount) : null,
            screenshotBase64: '',
            screenshotName: '',
            screenshotType: '',
            hasScreenshot: false
          }))

          // 如果第一条记录有地市，自动填充到表单
          if (res.data[0].city) {
            this.form.city = res.data[0].city
          }
          
          // 合并或替换记录
          const existingValidRecords = this.form.records.filter(r => r.trainingTime || r.organizer)
          if (existingValidRecords.length === 0) {
            this.form.records = newRecords
          } else {
            this.form.records = [...existingValidRecords, ...newRecords]
          }
          
          this.$message.success(`成功识别 ${res.data.length} 条培训记录`)
          this.aiDialogVisible = false
        } else {
          this.$message.warning('未能识别出培训记录，请检查输入格式')
        }
      }).catch(() => {
        this.$message.error('AI识别失败，请确保大模型服务可用')
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

.upload-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 5px;
  padding: 5px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  min-height: 60px;
  cursor: pointer;
  outline: none;
  transition: border-color 0.2s;
}

.upload-cell:focus {
  border-color: #409eff;
  background-color: #f0f7ff;
}

.upload-cell:hover {
  border-color: #409eff;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #67c23a;
  width: 100%;
}

.file-info span {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.paste-hint {
  font-size: 11px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 3px;
  margin-left: 10px;
}

.paste-hint i {
  font-size: 12px;
}

/* 图片预览弹窗样式 */
.image-toolbar {
  text-align: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.image-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.image-container img {
  max-width: 100%;
  max-height: 500px;
  object-fit: contain;
}

.image-container.full-size img {
  max-width: none;
  max-height: none;
}

/* 弹窗内边距减少 */
/deep/ .image-dialog {
  padding: 10px 15px 15px;
}

/deep/ .image-dialog .el-dialog__header {
  display: none;
}

/deep/ .image-dialog .el-dialog__body {
  padding: 0;
}

/deep/ .el-upload{
  display: flex;
}
</style>
