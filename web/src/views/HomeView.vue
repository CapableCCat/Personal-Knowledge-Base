<script setup>
/**
 * 首页工作台（P-010）—— ⬜ **T9 实现，当前为占位**
 *
 * T9 要落地的形态（PRD §8 / P-010，已冻结）：
 *   桌面（≥1024）：顶部输入框（回车即建 inbox，不跳页）+ 格式切换按钮 + 内联搜索框
 *                  状态条「待整理 12 · 已归档 348 · 项目 7 · 本周 +9」（可点，跳对应列表）
 *                  三列：项目 / 最近 / 待整理，**三列各自滚动**
 *   手机（<768） ：顶部输入框常驻 + 搜索图标 + 精简状态条（横滑）
 *                  底部 3 Tab：最近 / 待整理 / 项目（**切 Tab 只换内容区**）
 *
 * ⚠️ 三列各自滚动是滚动模型里最难的一段：外壳 `height:100% + overflow:hidden`，
 *    `.app-body` / `.app-main` 的 `min-height:0` 不能删，**每一列容器还要逐层加**
 *    `min-height:0 + overflow:auto`，否则只有最外层滚、列内容被裁（OSC 踩过，见清单 T9/R3）。
 */
</script>

<template>
  <div class="home">
    <el-card shadow="never" class="home__placeholder">
      <h2 class="home__title">PKB 个人知识库 · 空壳已就位</h2>
      <p class="home__desc">
        T1 脚手架落地已完成：OSC 业务代码已清空、包名与配置已改名为 <code>com.pkb</code> /
        <code>pkb-server</code> / 库 <code>pkb</code> / 桶 <code>pkb</code>。
      </p>
      <p class="home__desc">首页工作台（三列 / 3 Tab / 状态条）由 <strong>T9</strong> 实现。</p>
      <el-divider />
      <p class="home__hint">下一步：T2 数据层（4 张业务表 + <code>user</code> 表简化）。</p>
    </el-card>
  </div>
</template>

<style scoped>
.home {
  padding: var(--space-4);
}

.home__placeholder {
  max-width: 720px;
  margin: 0 auto;
}

.home__title {
  margin: 0 0 var(--space-3);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.home__desc {
  margin: 0 0 var(--space-2);
  line-height: 1.8;
  color: var(--text-regular);
}

.home__hint {
  margin: 0;
  color: var(--text-secondary);
}

code {
  padding: 1px 5px;
  font-size: 12px;
  background: var(--el-fill-color-light, #f5f7fa);
  border-radius: 3px;
}
</style>
