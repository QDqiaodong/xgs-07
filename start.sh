#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ -f .env ]; then
  export $(cat .env | grep -v '^#' | xargs)
else
  echo "错误: .env 文件不存在"
  exit 1
fi

echo "========================================"
echo "  语音朗诵与文稿记录空间"
echo "  项目启动脚本"
echo "========================================"
echo ""

echo "[1/5] 检查 Docker 运行状态..."
if ! docker info > /dev/null 2>&1; then
  echo "❌  Docker 未运行，请先启动 Docker Desktop"
  exit 1
fi
echo "✅  Docker 运行正常"
echo ""

echo "[2/5] 检查端口占用情况..."

check_port() {
  local port=$1
  local name=$2
  if lsof -nP -iTCP:$port -sTCP:LISTEN > /dev/null 2>&1; then
    echo "❌  端口 $port ($name) 已被占用"
    lsof -nP -iTCP:$port -sTCP:LISTEN
    return 1
  else
    echo "✅  端口 $port ($name) 可用"
    return 0
  fi
}

check_port $FRONTEND_PORT "前端"
check_port $BACKEND_PORT "后端"
check_port $MYSQL_PORT "MySQL"
check_port $REDIS_PORT "Redis"

echo ""
echo "[3/5] 准备构建 Docker 容器..."
echo "  镜像源配置: ${DOCKER_REGISTRY:-直连 Docker Hub}"
echo ""

if [ -z "$DOCKER_REGISTRY" ]; then
  echo "  ⚠️  当前直连 Docker Hub"
  echo "  💡 如遇拉取失败，建议配置阿里云个人加速器："
  echo "     1. 访问 https://cr.console.aliyun.com/ 获取免费加速器"
  echo "     2. Docker Desktop → Settings → Docker Engine"
  echo "     3. 添加: \"registry-mirrors\": [\"https://xxx.mirror.aliyuncs.com\"]"
  echo "     4. Apply & Restart 后重新运行本脚本"
  echo ""
fi

echo "[4/5] 构建并启动容器（首次会较慢，请耐心等待）..."
echo ""

if ! docker compose up --build -d 2>&1; then
  echo ""
  echo "========================================"
  echo "  ❌ Docker 构建失败！"
  echo "========================================"
  echo ""
  echo "  🔍 常见原因及解决方案："
  echo ""
  echo "  方案 1：镜像拉取失败（最常见）"
  echo "    → 配置阿里云个人加速器（推荐，永久免费）"
  echo "      地址: https://cr.console.aliyun.com/"
  echo "      配置后无需修改项目任何文件"
  echo ""
  echo "  方案 2：有科学上网"
  echo "    → 确保系统代理已开启，Docker 走代理"
  echo ""
  echo "  方案 3：手动预拉取镜像"
  echo "    docker pull node:18-alpine"
  echo "    docker pull nginx:alpine"
  echo "    docker pull maven:3.9-eclipse-temurin-17"
  echo "    docker pull eclipse-temurin:17-jre-alpine"
  echo "    docker pull mysql:8.0-alpine"
  echo "    docker pull redis:7-alpine"
  echo "    然后重新运行: ./start.sh"
  echo ""
  echo "  方案 4：检查网络连接"
  echo "    ping hub.docker.com"
  echo ""
  echo "  详细日志排查："
  echo "    docker compose build --no-cache  # 查看具体哪个镜像拉取失败"
  echo "========================================"
  exit 1
fi

echo ""
echo "[5/5] 等待服务启动..."
echo ""

FRONTEND_URL="http://localhost:${FRONTEND_PORT}"
BACKEND_URL="http://localhost:${BACKEND_PORT}"

MAX_RETRIES=40
RETRY=0

while [ $RETRY -lt $MAX_RETRIES ]; do
  if curl -sSf "$FRONTEND_URL" > /dev/null 2>&1; then
    echo "✅  前端服务启动成功"
    break
  fi
  RETRY=$((RETRY + 1))
  if [ $((RETRY % 5)) -eq 0 ]; then
    echo "  等待中... 已等待 $((RETRY * 3)) 秒 ($RETRY/$MAX_RETRIES)"
  else
    echo "  等待前端服务... ($RETRY/$MAX_RETRIES)"
  fi
  sleep 3
done

if [ $RETRY -eq $MAX_RETRIES ]; then
  echo ""
  echo "⚠️  前端服务启动超时"
  echo "  可能原因：后端服务未就绪、容器启动较慢"
  echo "  排查命令："
  echo "    docker compose logs -f backend"
  echo "    docker compose logs -f frontend"
  echo "    docker compose ps"
  echo ""
fi

echo ""
echo "========================================"
echo "  🎉 项目启动完成！"
echo "========================================"
echo ""
echo "  🌐 前端访问地址:"
echo "     $FRONTEND_URL"
echo "     http://127.0.0.1:${FRONTEND_PORT}"
echo ""
echo "  🔌 后端 API 地址:"
echo "     $BACKEND_URL/api"
echo ""
echo "  🐳 容器状态:"
docker compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"
echo ""
echo "  📋 常用命令:"
echo "     查看日志:  docker compose logs -f"
echo "     停止服务:  docker compose down"
echo "     重启服务:  docker compose restart"
echo "========================================"
