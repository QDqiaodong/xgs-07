# 语音朗诵与文稿记录空间

服务朗诵、朗读爱好者，支持录入朗诵文稿、记录练习感悟，分享文本内容与朗读心得的全栈应用。

## 技术栈

- **前端**: Vue 3 + Vite + Element Plus
- **后端**: Spring Boot 3.3 + JDK 17 + Spring Data JPA
- **数据库**: MySQL 8.0 + Redis 7
- **部署**: Docker + Docker Compose
- **包管理**: npm (前端) + Maven (后端)

## 固定端口表

| 服务 | 端口 | 说明 | 避开默认端口 |
|------|------|------|------------|
| 前端 Nginx | 18035 | 外部访问端口 | 避开 80/443/8080 |
| 后端 API | 19035 | REST 接口端口 | 避开 8080/9090 |
| MySQL | 13335 | 数据库端口 | 避开 3306 |
| Redis | 16435 | 缓存端口 | 避开 6379 |

> **重要**: 所有端口均绑定 `127.0.0.1`，禁止外部直接访问。

## 快速开始

### 方式一：一键启动脚本（推荐）

```bash
./start.sh
```

脚本自动完成：
1. ✅ 端口占用检查（冲突则显示占用进程）
2. ✅ Docker 镜像构建与容器启动
3. ✅ 等待服务健康检查通过
4. ✅ 自动打印前端访问地址

### 方式二：Docker Compose 直接启动

```bash
# 启动所有服务（首次会自动构建）
docker compose up --build -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f

# 停止服务
docker compose down
```

### 方式三：本地开发模式

#### 1. 启动基础设施

```bash
docker compose -f docker-compose.dev.yml up -d
```

#### 2. 启动后端

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

## 访问地址

- **前端页面**: http://localhost:18035
- **前端页面 (IP)**: http://127.0.0.1:18035
- **后端 API**: http://127.0.0.1:19035/api

> 确保 localhost 和 127.0.0.1 访问返回完全一致的内容。

## 核心功能

### 📝 朗诵文稿录入
- 录入诗歌、散文、短文等文稿内容
- 标注题材分类与朗读难度
- 支持保存草稿或公开发布
- **编辑内容本地临时缓存**：3秒自动保存草稿，刷新页面可恢复

### 📚 文稿分类查阅
- 8个内置分类（现代文、古诗词、寓言故事、诗歌、散文、经典名著、演讲文稿、儿童文学）
- 按分类筛选他人分享的文稿
- **大篇幅文稿内容分区渲染**：自动识别段落与小标题
- 热门文稿推荐（Redis 哈希结构缓存）

### ⭐ 文稿收藏管理
- 收藏/取消收藏心仪文稿
- 个人收藏列表管理
- 收藏数实时统计

### 📒 练习笔记记录
- 针对单篇文稿记录练习难点
- 语气把控要点记录
- 情感表达心得记录
- 形成完整的练习笔记体系

## 项目结构

```
.
├── .env                      # 全局环境配置（端口、镜像仓库等）
├── .dockerignore             # Docker 构建忽略文件
├── .gitignore                # Git 忽略文件
├── docker-compose.yml        # 生产环境 Compose 配置
├── docker-compose.dev.yml    # 开发环境 Compose 配置
├── start.sh                  # 一键启动脚本
├── README.md                 # 项目说明文档
├── backend/                  # 后端 Spring Boot 项目
│   ├── pom.xml               # Maven 依赖配置
│   ├── settings.xml          # Maven 镜像源配置（京东+阿里云）
│   ├── Dockerfile            # 后端 Docker 构建文件
│   └── src/main/
│       ├── java/com/recitation/
│       │   ├── RecitationApplication.java
│       │   ├── common/       # 通用返回结果
│       │   ├── config/       # 跨域、Redis 配置
│       │   ├── controller/   # REST API 控制器
│       │   ├── dto/          # 数据传输对象
│       │   ├── entity/       # JPA 实体类
│       │   ├── repository/   # 数据访问层
│       │   └── service/      # 业务逻辑层
│       └── resources/
│           ├── application.yml
│           ├── application-dev.yml
│           └── application-prod.yml
├── frontend/                 # 前端 Vue 3 项目
│   ├── package.json          # npm 依赖配置
│   ├── .npmrc                # npm 镜像源配置（淘宝）
│   ├── vite.config.js        # Vite 构建配置
│   ├── Dockerfile            # 前端 Docker 构建文件
│   ├── nginx.conf            # Nginx 配置
│   ├── index.html
│   └── src/
│       ├── api/              # Axios 接口封装
│       ├── components/       # 公共组件
│       ├── router/           # Vue Router 路由
│       ├── utils/            # 工具函数
│       ├── styles/           # 全局样式
│       ├── views/            # 页面组件
│       ├── App.vue
│       └── main.js
└── docker/
    └── mysql/
        └── init.sql          # 数据库初始化脚本（内置分类数据）
```

## Docker 构建优化说明

### 🔄 分层缓存机制
**仅使用 Docker 原生分层缓存，不使用任何外部语法糖。**

#### 前端 Dockerfile 分层策略
```dockerfile
# 第1层：仅拷贝依赖文件，变更触发依赖重新安装
COPY package*.json ./
RUN npm ci

# 第2层：拷贝业务源码，仅触发重新构建
COPY . .
RUN npm run build
```

#### 后端 Dockerfile 分层策略
```dockerfile
# 第1层：Maven 配置和依赖文件，变更触发依赖重新下载
COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .
RUN mvn dependency:go-offline -DskipTests

# 第2层：拷贝业务源码，仅触发重新编译
COPY src ./src
RUN mvn clean package -DskipTests
```

**缓存复用规则**：
- ✅ `pom.xml` / `package.json` 无变更 → 复用依赖层缓存，不重新下载
- ✅ 仅 `src/` 源码修改 → 仅执行重新编译/构建
- ❌ 依赖文件变更 → 触发完整依赖下载流程

### 🌐 镜像源统一配置
**所有基础镜像通过 `DOCKER_REGISTRY` 环境变量统一前缀**：

```dockerfile
ARG DOCKER_REGISTRY
FROM ${DOCKER_REGISTRY}node:18-alpine
FROM ${DOCKER_REGISTRY}nginx:alpine
FROM ${DOCKER_REGISTRY}maven:3.9-eclipse-temurin-17
FROM ${DOCKER_REGISTRY}eclipse-temurin:17-jre-alpine
FROM ${DOCKER_REGISTRY}mysql:8.0-alpine
FROM ${DOCKER_REGISTRY}redis:7-alpine
```

**⚠️ 2024年国内公共 Docker 镜像源已普遍失效，强烈推荐：配置阿里云个人加速器**

1. 访问 https://cr.console.aliyun.com/ 获取免费专属加速器
2. Docker Desktop → Settings → Docker Engine 添加：
```json
{ "registry-mirrors": ["https://xxx.mirror.aliyuncs.com"] }
```
3. Apply & Restart 后即可，无需修改项目文件

**本项目默认配置**：`.env` 中 `DOCKER_REGISTRY=` 留空，直连 Docker Hub（配合加速器使用最佳）

### 📦 依赖国内镜像
- **前端 npm**: 淘宝镜像 `https://registry.npmmirror.com/`
- **后端 Maven**: 京东镜像 + 阿里云镜像双配置

## 配置文件说明

### .env 关键配置

```env
# 项目名称（决定容器命名前缀）
COMPOSE_PROJECT_NAME=recitation-space

# Docker 镜像仓库（统一前缀）
# 推荐配置阿里云加速器后留空直连 Docker Hub
DOCKER_REGISTRY=

# 端口配置（全部自定义，避开默认）
FRONTEND_PORT=18035
BACKEND_PORT=19035
MYSQL_PORT=13335
REDIS_PORT=16435

# 数据库配置
MYSQL_ROOT_PASSWORD=recitation123
MYSQL_DATABASE=recitation_db
```

### Vite 端口配置（严格模式）

```javascript
server: {
  host: '127.0.0.1',
  port: 18035,
  strictPort: true    // 端口被占用直接报错，不自动换端口
},
preview: {
  host: '127.0.0.1',
  port: 18035,
  strictPort: true
}
```

### Docker Compose 端口映射

```yaml
ports:
  - "127.0.0.1:${FRONTEND_PORT}:80"    # 强制绑定 127.0.0.1
  - "127.0.0.1:${BACKEND_PORT}:9035"
  - "127.0.0.1:${MYSQL_PORT}:3306"
  - "127.0.0.1:${REDIS_PORT}:6379"
```

## 容器命名规范

所有容器名称以项目名称为前缀，与项目名称保持一致：

| 服务 | 容器名称 |
|------|---------|
| MySQL | recitation-space-mysql |
| Redis | recitation-space-redis |
| 后端 | recitation-space-backend |
| 前端 | recitation-space-frontend |

数据卷命名：
- `recitation-space-mysql-data`
- `recitation-space-redis-data`

## 端口冲突处理

### 启动脚本自动检测
`start.sh` 脚本启动前会自动检测所有端口：
- ✅ 端口可用 → 继续启动
- ❌ 端口被占用 → 立即报错并显示占用进程详情

```bash
# 手动检测端口占用
lsof -nP -iTCP:18035 -sTCP:LISTEN
```

### 端口被占用的处理步骤

1. 查看哪个进程占用了端口：
```bash
lsof -nP -iTCP:18035 -sTCP:LISTEN
```

2. 停止占用进程（慎重操作），或修改 `.env` 中的端口号：
```env
# 修改为其他未被占用的端口
FRONTEND_PORT=18036
```

3. 重新启动：
```bash
docker compose down
./start.sh
```

## 验证清单

启动完成后，请逐项验证：

### 1. 端口监听检查
```bash
lsof -nP -iTCP:18035 -sTCP:LISTEN
lsof -nP -iTCP:19035 -sTCP:LISTEN
```

### 2. 前端访问一致性验证
```bash
# 两者返回内容必须完全一致
curl -sS http://127.0.0.1:18035 | head -20
curl -sS http://localhost:18035 | head -20
```

### 3. 后端 API 验证
```bash
curl -sS http://127.0.0.1:19035/api/categories
```

### 4. 浏览器访问验证
- ✅ http://localhost:18035 正常打开
- ✅ http://127.0.0.1:18035 正常打开
- ✅ 两者显示内容完全一致

## 常见问题

### Q: 首次构建很慢？
A: 首次构建需要下载所有依赖，属于正常现象。后续构建只要依赖文件不变更，会自动复用缓存。

### Q: 镜像拉取失败？
A: 检查 `.env` 中的 `DOCKER_REGISTRY` 配置，可尝试更换为其他国内镜像源。

### Q: 端口被占用怎么办？
A: 修改 `.env` 文件中的端口号，然后执行 `docker compose down && ./start.sh` 重启。

### Q: 如何查看服务日志？
```bash
# 查看所有服务日志
docker compose logs -f

# 查看特定服务日志
docker compose logs -f frontend
docker compose logs -f backend
```

## 开发指南

### 新增 API 接口
1. 在 `backend/src/main/java/com/recitation/controller/` 新增 Controller
2. 在 `frontend/src/api/index.js` 新增对应接口函数
3. 在页面组件中调用

### 新增页面
1. 在 `frontend/src/views/` 新增页面组件
2. 在 `frontend/src/router/index.js` 添加路由配置

## License

MIT License
