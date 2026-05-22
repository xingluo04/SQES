# 学生综合素质评价系统

学生综合素质评价系统（Student Quality Evaluation System）用于围绕德育、智育、体育、美育、劳动教育五个维度管理学生评价数据，并基于综合评价结果进行聚类分析和分组展示。

## 技术栈

- 前端：Vue 3、TypeScript、Vite、Pinia、Vue Router、Element Plus、UnoCSS
- 后端：Spring Boot 2.7、Java 8、Spring Security、MyBatis-Plus、JWT
- 数据库：MySQL 8.x
- 数据处理：Apache POI、K-Means++ 聚类、Z-Score 标准化、PCA、聚类指标评估

## 项目结构

```text
.
├── backend/          # Spring Boot 后端服务
├── frontend/         # Vue 3 前端应用
├── openapi.json      # API 描述文件
├── 算法设计文档.md   # 综合评价与聚类算法说明
└── README.md
```

## 快速开始

### 后端

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8089`。

### 前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，开发环境通过 `/api` 代理到后端服务。

## 数据库配置

默认数据库为：

```text
student_quality_evaluation
```

本地连接配置位于：

```text
backend/src/main/resources/application.yml
```

默认开发账号密码配置为 `root / 123456`，仅用于本地开发环境。

## 主要功能

- 用户登录、注册、角色权限控制
- 学生、班级、用户管理
- 德育、智育、体育、美育、劳动教育五维评价
- 综合评价数据聚合
- Excel 数据导入
- 聚类分析与动态命名
- 数据看板、通知公告、系统日志

## 常用命令

后端测试：

```bash
cd backend
mvn test
```

前端构建：

```bash
cd frontend
npm run build
```
