# yxtyg-admin 初始化文档

## 1. 项目概览

- 技术栈：Spring Boot 2.7.18、MyBatis Plus 3.5.3.1、MySQL 8、EasyExcel
- 启动类：`src/main/java/com/jscm/yxtyg/YxtygApplication.java`
- 默认端口：`8080`
- 主要职责：体验官管理、工单导入/查询、评审纪要、转培上报、AI 解析、看板统计

## 2. 本地依赖

- JDK `1.8`
- Maven `3.6+`
- MySQL `8.x`
## 3. 关键配置

配置文件：`src/main/resources/application.yml`

默认本地配置如下：

- MySQL：`jdbc:mysql://127.0.0.1:3306/yxtyg_db`
- 用户名：`root`
- 密码：`123456`
- AI 服务：`http://gugang.i234.me:11868`

建议先按本地环境修改以下项：

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `ai.ollama.url`

## 4. 数据库初始化

### 4.1 全新库初始化

推荐顺序：

1. 执行 `src/main/resources/db/init_final.sql`
2. 执行 `docs/update_agent_user_prompt.sql`

说明：

- `init_final.sql` 已包含体验官、工单、评审、转培、大模型、智能体相关表
- 但当前代码里的 `AgentConfig` 已使用 `user_prompt` 字段，`init_final.sql` 还没有把这个字段建进去，所以必须再执行一次 `docs/update_agent_user_prompt.sql`

### 4.2 大图片上传补充配置

转培截图使用 `LONGBLOB` 存储，若导入/保存大图报 `PacketTooBigException`，执行：

- `docs/fix_mysql_packet_size.sql`

### 4.3 老库升级脚本

如果不是全新库，而是从早期版本升级，按需要补执行：

- `docs/update_training_report.sql`
- `docs/add_reporter_field.sql`
- `docs/update_model_settings.sql` 或 `docs/update_model_settings_utf8.sql`
- `docs/update_agent_user_prompt.sql`
- `src/main/resources/sql/add_embedding_config.sql`

## 5. 启动步骤

```bash
cd yxtyg-admin
mvn clean test
mvn spring-boot:run
```

如果只想打包运行：

```bash
cd yxtyg-admin
mvn clean package -DskipTests
java -jar target/yxtyg-admin-1.0.0.jar
```

## 6. 启动后自检

- 访问 `http://localhost:8080/api/dashboard/overview`
- 能返回 `code=200` 说明服务、数据库和 Mapper 基本正常
- 前端开发环境默认会把 `/api` 代理到这个服务

## 7. 主要接口分组

- `/api/dashboard`：月度看板
- `/api/experimenter`：体验官管理
- `/api/workorder`：工单导入、详情、删除
- `/api/review`：评审纪要
- `/api/training`：转培上报、截图读取
- `/api/model-config`：模型配置
- `/api/embedding-config`：向量化模型配置
- `/api/agent-config`：智能体参数
- `/api/ai`：AI 解析接口

## 8. 已知注意点

- 当前 `application.yml` 里的 AI 地址不是标准本地地址，直接启动前大概率需要改
- 向量检索、向量同步、智能推荐依赖“向量化模型配置”，首次使用前需先在页面中配置默认向量化模型
- `docs/update_agent_user_prompt.sql` 里默认值写的是 `{{content}}`，而代码替换占位符用的是 `{content}`，初始化后建议手动核对智能体配置里的提示词
- 如果前端用到截图上传，数据库 `max_allowed_packet` 不够会直接失败
