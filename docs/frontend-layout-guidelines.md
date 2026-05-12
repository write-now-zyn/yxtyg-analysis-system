# 前端布局与编码约定

适用范围：`yxtyg-web/src`

## 1. 页面外层间距

- 页面根容器统一使用 `.page-container`
- 页面外层留白只由 `App.vue` 中的 `.app-main` 控制
- 页面组件内不要再给 `.page-container` 增加额外 `padding`

原因：

- 避免同级页面出现双重留白
- 统一主内容区视觉节奏

当前统一入口：

- `yxtyg-web/src/styles/layout.css`

## 2. 卡片布局

- 页面中的一级内容块优先使用 `el-card.page-card`
- 多个卡片上下间距统一由 `.page-card` 控制
- 不要在每个页面重复写 `margin-bottom`

推荐写法：

```vue
<div class="page-container">
  <el-card class="page-card">
    <div slot="header" class="page-card__header">
      <span class="page-card__title">页面标题</span>
      <div class="page-card__actions">
        <el-button type="primary" size="small">操作</el-button>
      </div>
    </div>
  </el-card>
</div>
```

## 3. 样式职责边界

- 全局布局样式放到 `src/styles/layout.css`
- 页面内只保留业务样式，比如表格列、图片区、搜索项特殊宽度
- 不要在页面里重复定义通用类名的基础规则，比如 `.page-container { padding: ... }`

## 4. 内联样式约束

- 布局常量不要写内联样式
- 像 `padding`、`margin`、`background-color` 这类页面骨架样式应提取到公共 CSS
- 内联样式仅保留非常局部、一次性的组件细节，后续也应逐步清理

## 5. 新页面落地规则

- 第一步：页面根节点使用 `.page-container`
- 第二步：一级内容块使用 `.page-card`
- 第三步：卡片头部统一使用 `.page-card__header`、`.page-card__title`、`.page-card__actions`
- 第四步：如需新增通用布局规则，优先补到 `src/styles/layout.css`

## 6. 后续可继续演进的方向

- 抽 `PageCard` 基础组件，统一 `header` 结构
- 抽 `SearchCard + TableCard` 组合组件，覆盖列表型页面
- 补 ESLint 或 review 约束：禁止在页面内重新定义 `.page-container` 的 `padding`
