<template>
  <div id="app">
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-content">
          <div class="logo">
            <el-icon :size="28" color="#409eff"><Reading /></el-icon>
            <span class="title">朗诵文稿空间</span>
          </div>
          <el-menu mode="horizontal" :router="true" class="nav-menu">
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/training-packages">训练包</el-menu-item>
            <el-menu-item index="/manuscripts">文稿广场</el-menu-item>
            <el-menu-item index="/create">录入文稿</el-menu-item>
            <el-menu-item index="/favorites">我的收藏</el-menu-item>
            <el-menu-item index="/notes">练习笔记</el-menu-item>
          </el-menu>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
      <el-footer class="footer">
        <p>© 2024 语音朗诵与文稿记录空间 - 服务朗诵、朗读爱好者</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
</script>

<style scoped>
.main-container {
  min-height: 100vh;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.nav-menu {
  border-bottom: none;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 24px;
}

.footer {
  background: #f5f7fa;
  text-align: center;
  color: #909399;
  font-size: 14px;
  line-height: 50px;
}
</style>
