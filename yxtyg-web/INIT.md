# yxtyg-web 初始化文档

## 1. 项目概览

- 技术栈：Vue 2、Vue Router 3、Element UI、Axios、ECharts
- 入口文件：`src/main.js`
- 路由配置：`src/router/index.js`
- 默认端口：`8081`
- 接口前缀：`/api`

## 2. 本地依赖

- Node.js `14+`
- npm `6+`

## 3. 安装依赖

```bash
cd yxtyg-web
npm install
```

仓库里已经存在 `node_modules`，但仍建议按当前环境重新执行一次依赖安装，避免本机版本不一致。

## 4. 开发环境配置

配置文件：`vue.config.js`

当前开发代理：

- 前端端口：`8081`
- `/api` 代理目标：`http://localhost:8080`

因此本地联调前要确保后端 `yxtyg-admin` 已经启动在 `8080`。

## 5. 启动步骤

```bash
cd yxtyg-web
npm run serve
```

启动后访问：

- `http://localhost:8081`

## 6. 打包

```bash
cd yxtyg-web
npm run build
```

构建产物在：

- `dist/`

## 7. 页面模块

- `/dashboard`：月度看板
- `/review`：评审统计
- `/training`：转培上报
- `/experimenter`：体验官管理
- `/workorder`：需求提单
- `/model-config`：大模型设置
- `/agent-config`：智能体参数

## 8. 请求约定

请求封装文件：`src/utils/request.js`

- `baseURL` 固定为 `/api`
- 业务成功码固定判断为 `200`
- 非 `200` 业务响应会弹顶部通知，但 Promise 仍然返回给调用方
- 网络异常/500 会直接弹 `Message.error`

## 9. 联调自检

启动后建议先检查：

1. 首页能否正常跳转到 `/dashboard`
2. `月度看板` 是否能正常拉到 `/api/dashboard/overview`
3. `体验官管理`、`转培上报`、`评审统计` 页面列表是否能加载

## 10. 已知注意点

- 页面大量使用 `window.open('/api/...')` 做导出，生产环境需要保证网关或 Nginx 正确转发 `/api`
- AI 识别依赖后端模型配置可用，前端本身不直接连模型服务
- 截图预览依赖后端 `/api/training/record/image/{id}` 返回图片流，数据库未初始化完整时该功能会直接失效
