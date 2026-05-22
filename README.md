# 体验官专项数据分析系统

本仓库为“体验官专项数据分析系统”的本地开发版本，已补齐工作量核定管理相关用户故事，并提供 Docker Compose 一键运行方式。

## 本地运行

宿主机只需要 Docker：

```powershell
docker compose up --build
```

访问地址：

- 前端：http://localhost:18011
- 后端：http://localhost:18010

默认账号密码均为 `123456`：

| 账号 | 角色 |
| --- | --- |
| admin | 系统管理员 |
| devadmin | 开发管理员 |
| pm_zhang | 产品经理 |
| pm_li | 产品经理 |

更多容器化说明见 [README-docker.md](README-docker.md)。

## 验收材料

- [完成需求清单与证据 Markdown](docs/完成需求清单与证据.md)
- [完成需求清单与证据 Excel（标准浏览器截图版）](docs/完成需求清单与证据-标准浏览器截图.xlsx)
- [证据截图目录](docs/evidence/)

## 已补齐能力

- 登录、会话鉴权、角色权限
- 用户管理、角色权限配置
- 工作量核定需求 CRUD
- Excel 模板下载、批量导入、导出
- 产品经理最终工作量填报
- 开发管理员核定、催办
- 后端统一计算核减工作量
- 站内通知与未读提醒
- Docker Compose 本地隔离运行
