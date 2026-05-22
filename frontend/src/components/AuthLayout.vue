<template>
  <div class="auth-page" @mousemove="handleMouseMove">
    <!-- 左半屏：品牌视觉区 -->
    <div class="left-panel" ref="leftPanelRef">
      <div class="left-bg" :style="parallaxStyle">
        <div class="ink-layer ink-layer--1" />
        <div class="ink-layer ink-layer--2" />
        <div class="ink-layer ink-layer--3" />
      </div>

      <!-- 鼠标跟随光晕 -->
      <div class="cursor-glow" :style="cursorGlowStyle" />

      <!-- 山水剪影 -->
      <div class="mountains">
        <div class="mountain mountain--far" />
        <div class="mountain mountain--mid" />
        <div class="mountain mountain--near" />
      </div>

      <!-- 浮动粒子 -->
      <div class="particles">
        <div
          v-for="p in particles"
          :key="p.id"
          class="particle"
          :style="{
            '--size': p.size + 'px',
            '--x': p.x + '%',
            '--y': p.y + '%',
            '--duration': p.duration + 's',
            '--delay': p.delay + 's',
            '--opacity': p.opacity,
          }"
        />
      </div>

      <!-- 品牌文字 -->
      <div class="brand-content">
        <h1 class="brand-title">
          <span v-for="(char, i) in titleChars" :key="i" class="title-char" :style="{ '--i': i }">{{ char }}</span>
        </h1>
        <div class="brand-divider" />
        <p class="brand-tagline">{{ tagline }}</p>
      </div>
    </div>

    <!-- 右半屏：表单区 -->
    <div class="right-panel">
      <div class="form-wrapper">
        <div class="form-header">
          <h2 class="form-title">{{ formTitle }}</h2>
          <p class="form-subtitle">{{ formSubtitle }}</p>
        </div>
        <slot name="form" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps<{
  mode: 'login' | 'register'
}>()

const leftPanelRef = ref<HTMLElement>()
const mouseX = ref(50)
const mouseY = ref(50)

const formTitle = computed(() => props.mode === 'login' ? '欢迎回来' : '创建账号')
const formSubtitle = computed(() => props.mode === 'login' ? '登录您的账户继续使用' : '注册新账户开始使用系统')
const tagline = computed(() =>
  props.mode === 'login'
    ? '立德树人 · 全面发展'
    : '博学笃行 · 知行合一'
)

const titleChars = ['素', '质', '评', '价']

const parallaxStyle = computed(() => ({
  transform: `translate(${(mouseX.value - 50) * 0.03}px, ${(mouseY.value - 50) * 0.03}px)`,
}))

const cursorGlowStyle = computed(() => ({
  left: mouseX.value + '%',
  top: mouseY.value + '%',
}))

function handleMouseMove(e: MouseEvent) {
  const rect = leftPanelRef.value?.getBoundingClientRect()
  if (!rect) return
  mouseX.value = ((e.clientX - rect.left) / rect.width) * 100
  mouseY.value = ((e.clientY - rect.top) / rect.height) * 100
}

const particles = Array.from({ length: 12 }, (_, i) => ({
  id: i,
  size: 4 + Math.random() * 8,
  x: 5 + Math.random() * 90,
  y: 5 + Math.random() * 90,
  duration: 12 + Math.random() * 18,
  delay: Math.random() * 8,
  opacity: 0.2 + Math.random() * 0.3,
}))
</script>

<style scoped lang="scss">
.auth-page {
  display: flex;
  min-height: 100vh;
  overflow: hidden;
}

/* ── 左半屏 ── */
.left-panel {
  position: relative;
  width: 42%;
  min-height: 100vh;
  overflow: hidden;
  flex-shrink: 0;
}

.left-bg {
  position: absolute;
  inset: -30px;
  background: linear-gradient(160deg, #080e14 0%, #0f1923 30%, #162331 60%, #0f1923 100%);
  transition: transform 0.4s ease-out;
}

.ink-layer {
  position: absolute;
  inset: 0;

  &--1 {
    background: radial-gradient(ellipse 70% 60% at 30% 70%, rgba(77, 182, 172, 0.12) 0%, transparent 70%);
    animation: inkFloat1 18s ease-in-out infinite alternate;
  }

  &--2 {
    background: radial-gradient(ellipse 50% 70% at 70% 30%, rgba(229, 115, 115, 0.08) 0%, transparent 70%);
    animation: inkFloat2 22s ease-in-out infinite alternate;
  }

  &--3 {
    background: radial-gradient(ellipse 60% 50% at 50% 50%, rgba(77, 182, 172, 0.06) 0%, transparent 60%);
    animation: inkFloat3 25s ease-in-out infinite alternate;
  }
}

@keyframes inkFloat1 {
  0% { transform: scale(1) translate(0, 0); }
  100% { transform: scale(1.15) translate(-3%, 2%); }
}

@keyframes inkFloat2 {
  0% { transform: scale(1) translate(0, 0); }
  100% { transform: scale(1.1) translate(2%, -3%); }
}

@keyframes inkFloat3 {
  0% { transform: scale(1) translate(0, 0); }
  100% { transform: scale(1.08) translate(-1%, 1%); }
}

/* ── 鼠标跟随光晕 ── */
.cursor-glow {
  position: absolute;
  width: 300px;
  height: 300px;
  transform: translate(-50%, -50%);
  background: radial-gradient(circle, rgba(77, 182, 172, 0.08) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  transition: left 0.15s ease-out, top 0.15s ease-out;
  z-index: 1;
}

/* ── 山水剪影 ── */
.mountains {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 45%;
  pointer-events: none;
}

.mountain {
  position: absolute;
  bottom: 0;
  left: -10%;
  right: -10%;

  &--far {
    height: 100%;
    background: linear-gradient(180deg, transparent 0%, rgba(12, 20, 30, 0.5) 40%, rgba(12, 20, 30, 0.8) 100%);
    clip-path: polygon(
      0% 100%, 5% 65%, 12% 45%, 20% 55%, 28% 30%, 35% 40%, 42% 20%,
      50% 35%, 58% 15%, 65% 28%, 72% 10%, 80% 25%, 88% 40%, 95% 50%, 100% 35%, 100% 100%
    );
    opacity: 0.6;
  }

  &--mid {
    height: 75%;
    background: linear-gradient(180deg, transparent 0%, rgba(18, 30, 42, 0.7) 30%, rgba(18, 30, 42, 0.9) 100%);
    clip-path: polygon(
      0% 100%, 8% 55%, 15% 70%, 25% 40%, 33% 55%, 45% 25%, 55% 45%,
      62% 30%, 70% 50%, 80% 35%, 90% 55%, 100% 40%, 100% 100%
    );
    opacity: 0.8;
  }

  &--near {
    height: 50%;
    background: linear-gradient(180deg, rgba(12, 20, 30, 0.6) 0%, rgba(12, 20, 30, 0.98) 100%);
    clip-path: polygon(
      0% 100%, 10% 60%, 20% 75%, 35% 45%, 50% 65%, 65% 40%, 75% 55%,
      85% 50%, 95% 70%, 100% 55%, 100% 100%
    );
  }
}

/* ── 浮动粒子 ── */
.particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.particle {
  position: absolute;
  width: var(--size);
  height: var(--size);
  left: var(--x);
  top: var(--y);
  background: radial-gradient(circle, rgba(77, 182, 172, var(--opacity)) 0%, rgba(77, 182, 172, 0) 70%);
  border-radius: 50%;
  box-shadow: 0 0 calc(var(--size) * 2) rgba(77, 182, 172, calc(var(--opacity) * 0.5));
  animation: particleDrift var(--duration) ease-in-out var(--delay) infinite alternate;
}

@keyframes particleDrift {
  0% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(20px, -30px) scale(1.3); }
  50% { transform: translate(-15px, 15px) scale(0.7); }
  75% { transform: translate(10px, -20px) scale(1.2); }
  100% { transform: translate(-8px, 10px) scale(0.9); }
}

/* ── 品牌文字 ── */
.brand-content {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 48px;
}

.brand-title {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.title-char {
  font-family: var(--font-display);
  font-size: 56px;
  font-weight: 700;
  color: var(--color-accent-jade);
  text-shadow: 0 0 30px rgba(77, 182, 172, 0.3);
  opacity: 0;
  transform: translateY(30px);
  animation: charReveal 0.6s ease-out calc(var(--i) * 0.12s + 0.2s) forwards;
  transition: text-shadow 0.3s ease, transform 0.3s ease;
  cursor: default;

  &:hover {
    text-shadow: 0 0 50px rgba(77, 182, 172, 0.6), 0 0 80px rgba(77, 182, 172, 0.3);
    transform: translateY(-4px) scale(1.05);
  }
}

@keyframes charReveal {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.brand-divider {
  width: 80px;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--color-accent-jade), transparent);
  margin-bottom: 24px;
  opacity: 0;
  animation: dividerExpand 1s ease-out 0.8s forwards;
}

@keyframes dividerExpand {
  from { width: 0; opacity: 0; }
  to { width: 80px; opacity: 1; }
}

.brand-tagline {
  font-family: var(--font-display);
  font-size: 16px;
  color: var(--color-text-secondary);
  letter-spacing: 6px;
  opacity: 0;
  animation: taglineFade 1s ease-out 1s forwards;
}

@keyframes taglineFade {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ── 右半屏 ── */
.right-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: linear-gradient(135deg, #121a24 0%, #1b2838 50%, #162030 100%);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -30%;
    right: -15%;
    width: 60%;
    height: 60%;
    background: radial-gradient(ellipse, rgba(77, 182, 172, 0.04) 0%, transparent 70%);
    pointer-events: none;
    animation: ambientGlow 8s ease-in-out infinite alternate;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -20%;
    left: -10%;
    width: 40%;
    height: 40%;
    background: radial-gradient(ellipse, rgba(229, 115, 115, 0.03) 0%, transparent 70%);
    pointer-events: none;
    animation: ambientGlow 10s ease-in-out 3s infinite alternate;
  }
}

@keyframes ambientGlow {
  0% { opacity: 0.5; transform: scale(1); }
  100% { opacity: 1; transform: scale(1.1); }
}

.form-wrapper {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  background: rgba(27, 40, 56, 0.4);
  border: 1px solid rgba(77, 182, 172, 0.1);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  animation: formSlideIn 0.7s ease-out 0.2s both;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    inset: -1px;
    border-radius: 16px;
    background: linear-gradient(135deg, rgba(77, 182, 172, 0.15), transparent 50%, rgba(77, 182, 172, 0.05));
    z-index: -1;
    animation: borderPulse 4s ease-in-out infinite alternate;
  }
}

@keyframes formSlideIn {
  from { opacity: 0; transform: translateX(40px) scale(0.97); }
  to { opacity: 1; transform: translateX(0) scale(1); }
}

@keyframes borderPulse {
  0% { opacity: 0.5; }
  100% { opacity: 1; }
}

.form-header {
  margin-bottom: 32px;
}

.form-title {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.form-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
}

/* ── 响应式 ── */
@media (max-width: 768px) {
  .auth-page {
    flex-direction: column;
  }

  .left-panel {
    width: 100%;
    min-height: 180px;
    height: 180px;
  }

  .title-char {
    font-size: 36px;
  }

  .brand-tagline {
    font-size: 13px;
    letter-spacing: 4px;
  }

  .mountains {
    height: 60%;
  }

  .cursor-glow {
    display: none;
  }

  .right-panel {
    padding: 24px 16px;
  }

  .form-wrapper {
    max-width: 100%;
    padding: 24px;
  }
}
</style>
