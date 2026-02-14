# 🎓 Course Evaluation System

<p align="center">
  <img src="https://github.com/MYZ8088/mustCourseEvaluation/actions/workflows/ci-cd.yml/badge.svg" alt="CI/CD">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen?style=flat-square&logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue.js-3.4-4FC08D?style=flat-square&logo=vue.js" alt="Vue.js">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk" alt="Java">
  <img src="https://img.shields.io/badge/PostgreSQL-15-336791?style=flat-square&logo=postgresql" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="License">
</p>

<p align="center">
  <b>一个现代化的前后端分离课程评价平台，集成 AI 智能推荐、安全审计、完整测试体系</b>
</p>

<p align="center">
  <a href="#-快速开始">快速开始</a> •
  <a href="#-项目架构">项目架构</a> •
  <a href="#-功能特性">功能特性</a> •
  <a href="#-软件测试">软件测试</a> •
  <a href="#-api-文档">API 文档</a>
</p>

---

## 📖 目录

- [项目架构](#-项目架构)
- [功能特性](#-功能特性)
- [技术栈](#-技术栈)
- [快速开始](#-快速开始)
- [项目结构](#-项目结构)
- [软件测试](#-软件测试)
- [API 文档](#-api-文档)
- [部署指南](#-部署指南)
- [贡献指南](#-贡献指南)
- [开源协议](#-开源协议)

---

## 🏗 项目架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                   客户端层                                       │
│  ┌─────────────────────────────────────────────────────────────────────────────┐│
│  │                         Vue.js 3.4 + Element Plus                           ││
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ ││
│  │  │   课程浏览   │  │   评价管理   │  │   用户中心   │  │   AI 智能问答       │ ││
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────────────┘ ││
│  └─────────────────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        │ HTTP/HTTPS (REST API)
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                   后端服务层                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐│
│  │                        Spring Boot 3.2.4 Application                        ││
│  │                                                                             ││
│  │  ┌──────────────────────────────────────────────────────────────────────┐  ││
│  │  │                         Controller Layer                              │  ││
│  │  │  AuthController │ CourseController │ ReviewController │ AIController  │  ││
│  │  └──────────────────────────────────────────────────────────────────────┘  ││
│  │                                    │                                        ││
│  │  ┌──────────────────────────────────────────────────────────────────────┐  ││
│  │  │                          Service Layer                                │  ││
│  │  │  UserService │ CourseService │ ReviewService │ AIRecommendService     │  ││
│  │  └──────────────────────────────────────────────────────────────────────┘  ││
│  │                                    │                                        ││
│  │  ┌──────────────────────────────────────────────────────────────────────┐  ││
│  │  │                         Repository Layer                              │  ││
│  │  │  UserRepository │ CourseRepository │ ReviewRepository │ ...           │  ││
│  │  └──────────────────────────────────────────────────────────────────────┘  ││
│  │                                                                             ││
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐ ││
│  │  │ Spring Security │  │  JWT 认证       │  │  AI Agent (LangChain4j)     │ ││
│  │  └─────────────────┘  └─────────────────┘  └─────────────────────────────┘ ││
│  └─────────────────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        │ JDBC / JPA
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                   数据存储层                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐│
│  │                         PostgreSQL (Neon Cloud)                             ││
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────────────┐   ││
│  │  │  users  │  │ courses │  │ reviews │  │faculty  │  │ ai_conversations │   ││
│  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘  └─────────────────┘   ││
│  └─────────────────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 分层架构

```
┌────────────────────────────────────────────────────────────────┐
│                      Presentation Layer                        │
│         Vue.js 3.4 + Vue Router + Vuex + Element Plus          │
├────────────────────────────────────────────────────────────────┤
│                       Controller Layer                         │
│    REST APIs │ Request Validation │ Response Formatting        │
├────────────────────────────────────────────────────────────────┤
│                        Service Layer                           │
│    Business Logic │ Transaction Management │ AI Integration    │
├────────────────────────────────────────────────────────────────┤
│                       Repository Layer                         │
│         Spring Data JPA │ Custom Queries │ Pagination          │
├────────────────────────────────────────────────────────────────┤
│                         Data Layer                             │
│              PostgreSQL │ Entity Mapping │ Migrations          │
└────────────────────────────────────────────────────────────────┘
```

---

## ✨ 功能特性

### 核心功能

| 功能模块 | 描述 |
|---------|------|
| 🎯 **课程评价** | 匿名/实名评价、五星评分、评论审核、点赞互动 |
| 👥 **用户系统** | 学生/教师角色、JWT 认证、个人中心、用户名(3-9字符)限制 |
| 🏫 **课程管理** | 课程分类、教师关联、院系归属、时间安排 |
| 🤖 **AI 智能助手** | 基于 LangChain4j 的课程推荐、智能问答 |
| 📊 **数据统计** | 评分统计、评价趋势、热门课程排行 |

### 输入验证规则

| 字段 | 规则 | 说明 |
|------|------|------|
| 用户名 | 3-9 字符 | 仅支持字母、数字 |
| 密码 | 8-32 字符 | 需包含字母和数字 |
| 邮箱 | 学校邮箱 | 必须为 @student.must.edu.mo |
| 评价内容 | 10-500 字符 | 自动敏感词过滤 |

### 安全特性

| 特性 | 实现方式 |
|------|---------|
| 🔐 身份认证 | JWT Token + Spring Security |
| 🛡️ 密码安全 | BCrypt 加密存储 |
| 📝 安全审计 | 登录监控、可疑活动检测 |
| 🔒 数据保护 | HTTPS、敏感数据脱敏 |
| 🌐 中文错误提示 | 所有错误信息统一为中文返回 |

### DevOps

| 功能 | 技术 |
|------|------|
| 🔄 CI/CD | GitHub Actions 自动化流水线 |
| 🧪 自动测试 | 黑盒/白盒/集成测试 |
| 🐳 容器化 | Docker + Docker Compose |
| 📈 监控 | Spring Actuator + Prometheus |

---

## 🛠 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.4 | 核心框架 |
| Spring Security | 6.x | 安全框架 |
| Spring Data JPA | 3.x | 数据访问 |
| JWT | 0.11.5 | 身份认证 |
| LangChain4j | 0.27.1 | AI 集成 |
| PostgreSQL | 15 | 数据库 |
| Maven | 3.8+ | 构建工具 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.4 | 前端框架 |
| Vue Router | 4.x | 路由管理 |
| Vuex | 4.x | 状态管理 |
| Element Plus | 2.x | UI 组件库 |
| Axios | 1.x | HTTP 客户端 |

### 开发环境

| 工具 | 版本要求 |
|------|---------|
| Java | 21+ |
| Node.js | 18+ |
| npm | 9+ |
| Git | 2.x |

---

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/MYZ8088/mustCourseEvaluation.git
cd mustCourseEvaluation
```

### 2. 环境配置

```bash
# 复制环境变量模板
cp course-evaluation-system/.env.example course-evaluation-system/.env

# 编辑环境变量
nano course-evaluation-system/.env
```

**必需的环境变量：**

```bash
# 数据库配置
DATABASE_URL=jdbc:postgresql://localhost:5432/course_evaluation
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password

# JWT 配置
JWT_SECRET=your-base64-encoded-secret-key

# 邮件服务
MAIL_HOST=smtp.qq.com
MAIL_USERNAME=your-email@domain.com
MAIL_PASSWORD=your-email-password
```

### 3. 启动后端

```bash
cd course-evaluation-system/backend
mvn spring-boot:run
```

### 4. 启动前端

```bash
cd course-evaluation-system/frontend
npm install
npm run serve
```

### 5. 访问应用

| 服务 | 地址 |
|------|------|
| 前端界面 | http://localhost:8080 |
| 后端 API | http://localhost:8088/api |
| 健康检查 | http://localhost:8088/api/actuator/health |

---

## 📁 项目结构

```
course-evaluation-system/
├── backend/                          # 后端项目
│   ├── src/main/java/com/must/courseevaluation/
│   │   ├── config/                   # 配置类
│   │   │   ├── SecurityConfig.java   # 安全配置
│   │   │   ├── JwtConfig.java        # JWT 配置
│   │   │   └── CorsConfig.java       # CORS 配置
│   │   ├── controller/               # 控制器层
│   │   │   ├── AuthController.java   # 认证接口
│   │   │   ├── CourseController.java # 课程接口
│   │   │   ├── ReviewController.java # 评价接口
│   │   │   └── AIController.java     # AI 接口
│   │   ├── service/                  # 服务层
│   │   │   ├── UserService.java
│   │   │   ├── CourseService.java
│   │   │   ├── ReviewService.java
│   │   │   └── impl/                 # 服务实现
│   │   ├── repository/               # 数据访问层
│   │   ├── model/                    # 实体类
│   │   ├── dto/                      # 数据传输对象
│   │   ├── exception/                # 异常处理
│   │   └── agent/                    # AI Agent
│   │       ├── BaseAgent.java
│   │       ├── NewQueryAgent.java
│   │       └── FollowUpAgent.java
│   └── src/test/java/                # 测试代码
│       └── com/must/courseevaluation/
│           ├── AuthBlackBoxTests.java
│           ├── UserServiceWhiteBoxTests.java
│           ├── ReviewServiceWhiteBoxTests.java
│           ├── IntegrationTestWithStubs.java
│           └── IntegrationTestWithDrivers.java
│
├── frontend/                         # 前端项目
│   ├── src/
│   │   ├── views/                    # 页面组件
│   │   ├── components/               # 通用组件
│   │   ├── router/                   # 路由配置
│   │   ├── store/                    # Vuex 状态管理
│   │   ├── api/                      # API 接口
│   │   └── utils/                    # 工具函数
│   └── public/                       # 静态资源
│
├── .github/workflows/                # CI/CD 配置
│   ├── ci-cd.yml                     # 主流水线
│   └── security-scan.yml             # 安全扫描
│
└── README.md                         # 项目文档
```

---

## 🧪 软件测试

本项目实现了完整的软件测试体系，覆盖单元测试、黑盒测试、白盒测试、集成测试和 **Selenium UI 自动化测试**五个层面。

> 📊 **详细测试报告**: 查看 [`软件测试完整报告.html`](软件测试完整报告.html) 获取可视化测试分析

### 测试体系概览

| 测试类型 | 测试框架 | 测试方法 | 用例数 | 代码覆盖率 |
|---------|---------|---------|--------|-----------|
| 单元测试 | JUnit 5 + Mockito | 模拟依赖、隔离测试 | 70+ | 85%+ |
| 黑盒测试 | SpringBootTest + MockMvc | 等价类/边界值/因果图/判定表 | 38+ | - |
| 白盒测试 | SpringBootTest | 逻辑覆盖/基本路径 | 27+ | 100% |
| 集成测试 | SpringBootTest + @MockBean | Stubs/Drivers | 33+ | - |
| **UI自动化测试** | **Selenium + JUnit 5** | **页面交互/表单验证/路由守卫** | **97+** | - |

### Selenium UI 自动化测试

本项目包含完整的 Selenium WebDriver 自动化测试套件，覆盖核心 UI 功能：

| 测试模块 | 测试文件 | 用例数 | 测试内容 |
|---------|---------|--------|---------|
| 登录功能 | `LoginSeleniumTests.java` | 17 | 表单验证、等价类、边界值(用户名3-9位)、登录方式切换 |
| 注册功能 | `RegisterSeleniumTests.java` | 20 | 用户名校验、邮箱验证、密码强度、验证码 |
| 课程浏览 | `CourseSeleniumTests.java` | 20 | 列表展示、搜索过滤、详情页、评价统计 |
| 评价功能 | `ReviewSeleniumTests.java` | 20 | 评分选择、内容提交、投票互动、匿名评价 |
| 页面导航 | `NavigationSeleniumTests.java` | 20 | 路由守卫、页面跳转、404处理、权限控制 |

**测试设计方法：**
- ✅ 等价类划分（有效/无效输入）
- ✅ 边界值分析（用户名3-9字符、密码8-32字符）
- ✅ 功能测试（核心业务流程）
- ✅ UI元素验证（页面完整性）
- ✅ 路由守卫测试（权限控制）

### 测试文件结构

```
src/test/java/com/must/courseevaluation/
├── unit/                              # 单元测试
│   ├── UserServiceUnitTest.java       # 用户服务 (15+ 用例)
│   ├── CourseServiceUnitTest.java     # 课程服务 (18+ 用例)
│   ├── ReviewServiceUnitTest.java     # 评价服务 (20+ 用例)
│   ├── VerificationCodeServiceUnitTest.java  # 验证码服务 (8 用例)
│   └── ContentFilterServiceUnitTest.java     # 内容过滤 (8 用例)
├── AuthBlackBoxTests.java             # 黑盒测试
├── UserServiceWhiteBoxTests.java      # 白盒测试
├── ReviewServiceWhiteBoxTests.java    # 白盒测试
├── IntegrationTestWithStubs.java      # 集成测试 (Stubs)
├── IntegrationTestWithDrivers.java    # 集成测试 (Drivers)
└── selenium/                          # Selenium UI 自动化测试
    ├── SeleniumTestBase.java          # 测试基类 (WebDriver配置)
    ├── LoginSeleniumTests.java        # 登录功能测试 (17 用例)
    ├── RegisterSeleniumTests.java     # 注册功能测试 (20 用例)
    ├── CourseSeleniumTests.java       # 课程功能测试 (20 用例)
    ├── ReviewSeleniumTests.java       # 评价功能测试 (20 用例)
    └── NavigationSeleniumTests.java   # 导航功能测试 (20 用例)
```

### 运行测试命令

```powershell
cd course-evaluation-system/backend

# 运行所有测试
mvn test

# 运行单元测试
mvn test "-Dtest=UserServiceUnitTest,CourseServiceUnitTest,ReviewServiceUnitTest,VerificationCodeServiceUnitTest,ContentFilterServiceUnitTest"

# 运行黑盒测试
mvn test "-Dtest=**/blackbox/*"

# 运行白盒测试
mvn test "-Dtest=**/whitebox/*"

# 运行集成测试
mvn test "-Dtest=IntegrationTestWithStubs,IntegrationTestWithDrivers"

# 运行 Selenium UI 自动化测试（需启动前后端服务）
mvn test "-Dtest=**/*SeleniumTests"

# 运行特定 Selenium 测试模块
mvn test "-Dtest=LoginSeleniumTests"
mvn test "-Dtest=RegisterSeleniumTests"
mvn test "-Dtest=CourseSeleniumTests"
mvn test "-Dtest=ReviewSeleniumTests"
mvn test "-Dtest=NavigationSeleniumTests"

# 生成测试覆盖率报告
mvn test jacoco:report
```

### Selenium 测试环境要求

| 依赖 | 版本 | 说明 |
|------|------|------|
| Selenium WebDriver | 4.19.1 | 浏览器自动化框架 |
| WebDriverManager | 5.8.0 | 自动管理浏览器驱动 |
| Chrome Browser | 最新版 | 推荐使用Chrome浏览器 |

> ⚠️ **注意**: 运行 Selenium 测试前，请确保前端服务(8080端口)和后端服务(8088端口)均已启动

### CI/CD 测试流程

```
Push/PR
   │
   ├──► 单元测试 ─────┐
   │                  │
   ├──► 黑盒测试 ─────┼──► 测试汇总 ──► 构建 ──► 部署
   │                  │
   ├──► 白盒测试 ─────┤
   │                  │
   ├──► 集成测试 ─────┤
   │                  │
   └──► 前端测试 ─────┘
```

---

## 📚 API 文档

### 认证接口

| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/logout` | 用户登出 |
| POST | `/api/auth/send-code` | 发送验证码 |

### 课程接口

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/courses` | 获取课程列表 |
| GET | `/api/courses/{id}` | 获取课程详情 |
| GET | `/api/courses/faculty/{id}` | 按院系获取课程 |
| GET | `/api/courses/search?keyword=` | 搜索课程 |

### 评价接口

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/reviews` | 获取评价列表 |
| GET | `/api/reviews/{id}` | 获取评价详情 |
| GET | `/api/reviews/course/{id}` | 获取课程评价 |
| POST | `/api/reviews` | 提交评价 |
| POST | `/api/reviews/{id}/vote` | 评价投票 |

### AI 接口

| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/api/ai/chat` | AI 对话 |
| GET | `/api/ai/conversations` | 获取对话历史 |
| GET | `/api/ai/recommend` | 获取课程推荐 |

### 系统接口

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/system/health` | 健康检查 |
| GET | `/api/system/info` | 系统信息 |
| GET | `/api/actuator/health` | Actuator 健康检查 |

---

## 🐳 部署指南

### Docker 部署

```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f
```

### 手动部署

```bash
# 构建后端
cd course-evaluation-system/backend
mvn clean package -DskipTests

# 构建前端
cd ../frontend
npm install && npm run build

# 启动后端
java -jar target/course-evaluation-*.jar

# 部署前端到 Web 服务器
cp -r dist/* /var/www/html/
```

### 环境变量

| 变量名 | 描述 | 必需 |
|--------|------|------|
| `DATABASE_URL` | 数据库连接 URL | ✅ |
| `DATABASE_USERNAME` | 数据库用户名 | ✅ |
| `DATABASE_PASSWORD` | 数据库密码 | ✅ |
| `JWT_SECRET` | JWT 密钥 | ✅ |
| `MAIL_HOST` | 邮件服务器 | ✅ |
| `MAIL_USERNAME` | 邮件账号 | ✅ |
| `MAIL_PASSWORD` | 邮件密码 | ✅ |
| `DEEPSEEK_API_KEY` | AI API 密钥 | ❌ |

---

## 🤝 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

### 代码规范

- 后端遵循 Google Java Style Guide
- 前端遵循 Vue.js 官方风格指南
- 提交信息遵循 Conventional Commits

---

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 📞 联系方式

- **项目维护者**: MYZ8088
- **GitHub**: [https://github.com/MYZ8088/mustCourseEvaluation](https://github.com/MYZ8088/mustCourseEvaluation)
- **问题反馈**: [GitHub Issues](https://github.com/MYZ8088/mustCourseEvaluation/issues)

---

<p align="center">
  <b>⭐ 如果这个项目对你有帮助，请给个 Star 支持一下！</b>
</p>

<p align="center">
  Made with ❤️ by MUST Students
</p>
