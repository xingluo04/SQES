# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

学生综合素质评价系统（Student Quality Evaluation System），包含 Vue 3 前端和 Spring Boot 后端。系统基于德育、智育、体育、美育、劳动教育五个维度对学生进行综合评价，并通过 K-Means++ 聚类算法自动对学生分组。

## 项目结构说明

```text
.
├── AGENTS.md                 # Codex 工作约定
├── CLAUDE.md                 # Claude 工作约定
├── openapi.json              # API 描述文件
├── 算法设计文档.md           # 综合评价与聚类算法说明
├── backend/                  # Spring Boot 后端
└── frontend/                 # Vue 3 前端
```

后端目录：

```text
backend/
├── pom.xml
├── uploads/                  # 文件上传目录
└── src/
    ├── main/
    │   ├── java/com/sqe/
    │   │   ├── common/       # Result<T>、PageResult<T>
    │   │   ├── config/       # Security、MyBatis-Plus、CORS、MVC 配置
    │   │   ├── controller/   # REST 接口，统一返回 Result<T>
    │   │   ├── dto/          # 请求/响应 DTO
    │   │   ├── entity/       # MyBatis-Plus 实体
    │   │   ├── exception/    # 全局异常处理
    │   │   ├── filter/       # JWT 认证过滤器
    │   │   ├── mapper/       # BaseMapper 接口
    │   │   ├── service/      # 业务逻辑
    │   │   └── utils/        # JwtUtils 等工具类
    │   └── resources/
    │       └── application.yml
    └── test/java/com/sqe/    # 后端接口测试
```

前端目录：

```text
frontend/
├── package.json
├── vite.config.ts
├── uno.config.ts
├── index.html
└── src/
    ├── api/                  # Axios 接口封装
    ├── assets/               # 静态资源
    ├── components/           # 通用组件
    ├── layouts/              # DefaultLayout
    ├── router/               # Vue Router 与前端角色守卫
    ├── stores/               # Pinia 状态
    ├── styles/               # 全局样式、Element Plus 变量
    ├── types/                # TypeScript 类型
    └── views/                # 页面模块
```

`backend/target/`、`frontend/dist/`、`frontend/node_modules/` 是生成物或依赖目录，通常不要手动修改。

## 前端启动与构建命令

所有前端命令在 `frontend/` 目录下执行：

```bash
cd frontend
npm install       # 首次安装依赖
npm run dev       # 启动开发服务，默认端口 5173
npm run build     # 类型检查并构建生产包
npm run preview   # 本地预览构建产物
```

Vite 开发服务通过 `/api` 代理到后端 `http://localhost:8089`，配置见 `frontend/vite.config.ts`。

## 后端启动与测试命令

所有后端命令在 `backend/` 目录下执行：

```bash
cd backend
mvn clean compile                 # 编译
mvn spring-boot:run               # 启动后端，端口 8089
mvn clean package                 # 打包 JAR
mvn test                          # 运行全部测试
mvn test -Dtest=ApiTest           # 运行指定测试类
mvn test -Dtest=ApiTest#testUserInfo  # 运行单个测试方法
```

## 数据库说明

- 数据库类型：MySQL 8.x。
- 默认连接：`localhost:3306/student_quality_evaluation`。
- 默认账号密码在 `backend/src/main/resources/application.yml` 中配置为 `root / 123456`，仅作为本地开发配置使用。
- 当前仓库没有独立的 `.sql` 建表脚本或迁移脚本。
- 表结构主要由 `backend/src/main/java/com/sqe/entity/` 下的实体类和 `@TableName` 体现。
- 主要表包括：`sys_user`、`student_info`、`class_info`、`moral_evaluation`、`academic_evaluation`、`physical_evaluation`、`art_evaluation`、`practice_evaluation`、`comprehensive_evaluation`、`cluster_result`、`sys_notice`、`sys_log`。
- MyBatis-Plus 全局配置：ID 自增、逻辑删除字段 `deleted`（0=未删除，1=已删除）、下划线转驼峰、分页插件。
- 测试用例依赖已有用户和数据，例如 `admin`、`teacher1`、`student1`、`parent1`，修改测试或初始化方式时要保持可复现。

## 主要业务模块

- 认证与权限：`AuthController`、`AuthService`、`SecurityConfig`、`JwtAuthenticationFilter`。`/api/auth/**` 公开，其余接口需 Bearer Token。
- 用户管理：`UserController`、`UserService`，支持分页、增删改、重置密码、状态切换。
- 学生与班级管理：`StudentController`、`ClassController`。
- 五维评价：`EvaluationController`、`EvaluationService`，覆盖德育、智育、体育、美育、劳动教育。
- 综合评价：`DataAggregationService` 按德育25%、智育50%、体育8%、美育8%、劳动9%聚合。
- Excel 导入：`ImportController`、`ExcelImportService`，使用 Apache POI。
- 聚类分析：`ClusterService`、`ClusterEvaluationService`、`ClusterNamingService`，包含 Z-Score、PCA、K-Means++、Gap Statistic、Silhouette/DBI/CHI、ANOVA 和动态命名。
- 数据看板、通知、日志：`DashboardController`、`NoticeController`、`LogController`。

## 代码风格要求

- 后端使用 Java 8 + Spring Boot 2.7 风格，保持现有分层：`Controller -> Service -> Mapper -> MySQL`。
- Controller 只做参数接收、认证上下文读取和结果封装，业务逻辑放在 Service。
- 所有新增后端接口必须返回 `Result<T>`；分页响应保持现有 `PageResult<T>` 或 MyBatis-Plus 分页结构。
- 实体类使用 Lombok `@Data`，不要手写 getter/setter。
- Mapper 继承 MyBatis-Plus `BaseMapper<T>`；复杂条件优先使用 `LambdaQueryWrapper`。
- 时间格式统一 `yyyy-MM-dd HH:mm:ss`，时区 `Asia/Shanghai`。
- 分数、权重、金额类精确数值优先使用 `BigDecimal`，避免用 `double` 直接做持久化分数计算。
- 前端使用 Vue 3 `<script setup>`、TypeScript、Pinia、Vue Router、Element Plus 和现有 `api/request.ts` 请求封装。
- 前端新增接口放在 `frontend/src/api/`，类型放在 `frontend/src/types/`，页面放在对应 `frontend/src/views/` 子目录。
- 前端鉴权状态目前保存在 `localStorage` 和 Pinia 中；新增路由要同步维护 `meta.roles` 和侧边栏显示逻辑。
- 保持中文业务命名和现有页面文案风格一致。

## 每次修改后的检查流程

1. 查看改动范围，确认只修改了本次任务相关文件。
2. 后端代码修改后，至少运行：

   ```bash
   cd backend
   mvn test
   ```

   如果只是局部后端修改，可以先运行对应测试类或方法，但提交前应优先跑完整 `mvn test`。

3. 前端代码修改后，至少运行：

   ```bash
   cd frontend
   npm run build
   ```

4. 同时修改前后端接口时，检查：
   - 后端接口路径、方法、请求参数、响应结构是否与前端 `src/api` 保持一致。
   - 是否仍符合 `{ code, message, data }` 的统一响应约定。
   - 路由角色、后端认证和页面入口是否一致。

5. 涉及数据库字段或实体变更时，检查：
   - 实体字段、表字段命名、前端类型定义是否同步。
   - 是否需要补充迁移脚本或初始化说明。
   - 现有测试数据和登录账号是否仍可用。

6. 涉及聚类、综合评价、Excel 导入等核心算法时，检查权重、学年、空值、重复学生、边界分数和小样本数据。

## 禁止事项

- 不要修改与当前任务无关的文件。
- 不要删除、弱化或绕过现有功能，除非用户明确要求。
- 不要改动真实密钥、生产凭据、数据库密码、JWT secret 或任何敏感配置；如需调整，先说明原因并让用户确认。
- 不要把本地生成物、依赖目录或构建缓存当作源码修改，例如 `backend/target/`、`frontend/dist/`、`frontend/node_modules/`。
- 不要随意重命名接口路径、角色名、数据库表名或字段名；这些会影响前后端联调和已有数据。
- 不要绕过统一响应格式，不要让新增接口直接返回裸对象或字符串。
- 不要在未确认影响范围的情况下修改聚类算法、综合评价权重或 Excel 导入字段映射。