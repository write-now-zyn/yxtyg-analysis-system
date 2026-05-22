# Docker 本地开发运行

本说明用于在本机只依赖 Docker 的前提下运行一线体验官专项数据分析系统。JDK、Maven、Node、npm、MySQL、Redis 都在容器内运行，不需要安装到宿主机。

## 启动

```powershell
docker compose up --build
```

首次启动会下载镜像、安装前端 npm 依赖、解析后端 Maven 依赖，并初始化 MySQL 数据库。

## 访问地址

- 前端页面：http://localhost:18011
- 后端接口：http://localhost:18010

前端容器内通过 `http://backend:10010` 代理 `/api` 请求。MySQL 和 Redis 默认只在 Docker 网络内访问，不暴露宿主机端口。

## 默认账号

初始化数据库会创建以下本地开发账号，默认密码均为 `123456`：

- `admin`：系统管理员，可管理用户、角色权限和全部工作量需求
- `devadmin`：开发管理员，可导入需求、核定工作量、催办产品经理
- `pm_zhang`：产品经理，只能查看并填写本人负责的需求
- `pm_li`：产品经理，只能查看并填写本人负责的需求

工作量核定模块入口为“工作量核定”，支持需求 CRUD、Excel 模板下载、批量导入、导出、最终工作量填报、核定、催办和站内通知。

## 配置

如需修改端口或外部 AI 服务地址：

```powershell
Copy-Item .env.example .env
```

然后编辑 `.env`。常用配置：

- `FRONTEND_HOST_PORT`：前端宿主机端口，默认 `18011`
- `BACKEND_HOST_PORT`：后端宿主机端口，默认 `18010`
- `AI_OLLAMA_URL`：外部 Ollama 或兼容服务地址
- `AI_OLLAMA_MODEL`：默认模型名

## 停止和清理

停止容器：

```powershell
docker compose down
```

清空数据库、Redis、依赖缓存等 Docker volume：

```powershell
docker compose down -v
```

修改 `yxtyg-admin/src/main/resources/db/init.sql` 后，如果希望重新执行初始化脚本，请使用：

```powershell
docker compose down -v
docker compose up --build
```

只停止容器不会重新初始化 MySQL，已有业务数据会保留在 Docker volume 中。
