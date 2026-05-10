<template>
  <div class="app">
    <header>
      <h1>Ray Tracer</h1>
    </header>

    <div class="layout">

      <!-- Scene builder panel — only shown when custom scene is selected -->
      <template v-if="isCustomScene">
        <aside
          class="scene-builder"
          :class="{ 'scene-builder--collapsed': builderCollapsed }"
          :style="builderCollapsed ? {} : { width: builderWidth + 'px' }"
        >
          <!-- Collapse toggle tab -->
          <button
            class="builder-collapse-tab"
            @click="builderCollapsed = !builderCollapsed"
            :title="builderCollapsed ? 'Expand scene builder' : 'Collapse scene builder'"
          >{{ builderCollapsed ? '›' : '‹' }}</button>

          <!-- Panel content — hidden when collapsed -->
          <template v-if="!builderCollapsed">
            <div class="builder-header">
              <span>Scene Builder</span>
              <button class="btn-export" @click="exportScene" title="Export scene JSON">Export JSON</button>
            </div>

            <!-- Sky -->
            <section>
              <h2>Sky</h2>
              <label>
                Type
                <select v-model="custom.sky.type" :disabled="isRendering">
                  <option value="default">Sky blue</option>
                  <option value="black">Black</option>
                  <option value="solid">Custom colour</option>
                </select>
              </label>
              <label v-if="custom.sky.type === 'solid'">
                Colour
                <input type="color" v-model="custom.sky.hex" :disabled="isRendering" class="color-input" />
              </label>
            </section>

            <!-- Light -->
            <section>
              <h2>Light</h2>
              <label>
                Source
                <select v-model="custom.light.type" :disabled="isRendering">
                  <option value="sun">Sun (white, overhead)</option>
                  <option value="sunset">Sunset (warm, side)</option>
                  <option value="none">None</option>
                </select>
              </label>
            </section>

            <!-- Floor -->
            <section>
              <h2>Floor</h2>
              <label>
                Surface
                <select v-model="custom.floor.type" :disabled="isRendering">
                  <option value="texture">Texture</option>
                  <option value="solid">Solid colour</option>
                  <option value="none">No floor</option>
                </select>
              </label>
              <template v-if="custom.floor.type === 'texture'">
                <label>
                  Texture
                  <select v-model="custom.floor.texture" :disabled="isRendering">
                    <option value="chequerboard">Chequerboard</option>
                    <option value="mc_grass">Minecraft grass</option>
                    <option value="mc_cobble">Minecraft cobble</option>
                    <option value="mc_dirt">Minecraft dirt</option>
                    <option value="greyscale_noise_random">Greyscale noise</option>
                    <option value="rainbow_noise_random">Rainbow noise</option>
                    <option value="debug">Debug</option>
                  </select>
                </label>
              </template>
              <template v-if="custom.floor.type === 'solid'">
                <label>
                  Colour
                  <input type="color" v-model="custom.floor.hex" :disabled="isRendering" class="color-input" />
                </label>
              </template>
              <template v-if="custom.floor.type !== 'none'">
                <label>
                  Roughness
                  <div class="slider-row">
                    <input type="range" v-model.number="custom.floor.roughness" min="0" max="1" step="0.05" :disabled="isRendering" />
                    <span class="value">{{ custom.floor.roughness.toFixed(2) }}</span>
                  </div>
                </label>
                <label>
                  Height
                  <input type="number" v-model.number="custom.floor.height" step="0.5" :disabled="isRendering" />
                </label>
              </template>
            </section>

            <!-- Spheres -->
            <section>
              <h2>Spheres</h2>
              <div v-for="(sphere, idx) in custom.spheres" :key="idx" class="sphere-card">
                <div class="sphere-card-header" @click="sphere.collapsed = !sphere.collapsed">
                  <span class="sphere-card-title">
                    <span class="sphere-collapse-icon">{{ sphere.collapsed ? '▶' : '▼' }}</span>
                    Sphere {{ idx + 1 }}
                  </span>
                  <span class="sphere-summary" v-if="sphere.collapsed">
                    r={{ sphere.radius }} · {{ sphere.materialType }}
                  </span>
                  <button class="btn-remove" @click.stop="removeSphere(idx)" :disabled="isRendering">✕</button>
                </div>

                <template v-if="!sphere.collapsed">
                  <label>
                    Material
                    <select v-model="sphere.materialType" :disabled="isRendering">
                      <option value="simple">Simple</option>
                      <option value="iridescent">Iridescent</option>
                      <option value="kaleidoscopic">Kaleidoscopic</option>
                    </select>
                  </label>

                  <template v-if="sphere.materialType === 'simple'">
                    <label>
                      Colour
                      <input type="color" v-model="sphere.hex" :disabled="isRendering" class="color-input" />
                    </label>
                    <label class="checkbox-label">
                      <input type="checkbox" v-model="sphere.emissive" :disabled="isRendering" />
                      Emissive
                    </label>
                    <label v-if="sphere.emissive">
                      Emission colour
                      <input type="color" v-model="sphere.emissionHex" :disabled="isRendering" class="color-input" />
                    </label>
                    <label>
                      Transparency
                      <div class="slider-row">
                        <input type="range" v-model.number="sphere.transparency" min="0" max="1" step="0.05" :disabled="isRendering" />
                        <span class="value">{{ sphere.transparency.toFixed(2) }}</span>
                      </div>
                    </label>
                    <label v-if="sphere.transparency > 0">
                      Index of refraction
                      <div class="slider-row">
                        <input type="range" v-model.number="sphere.ior" min="1" max="3" step="0.025" :disabled="isRendering" />
                        <span class="value">{{ sphere.ior.toFixed(3) }}</span>
                      </div>
                    </label>
                  </template>

                  <template v-if="sphere.materialType === 'iridescent'">
                    <label>
                      Transparency
                      <div class="slider-row">
                        <input type="range" v-model.number="sphere.transparency" min="0" max="1" step="0.05" :disabled="isRendering" />
                        <span class="value">{{ sphere.transparency.toFixed(2) }}</span>
                      </div>
                    </label>
                    <label v-if="sphere.transparency > 0">
                      Index of refraction
                      <div class="slider-row">
                        <input type="range" v-model.number="sphere.ior" min="1" max="3" step="0.025" :disabled="isRendering" />
                        <span class="value">{{ sphere.ior.toFixed(3) }}</span>
                      </div>
                    </label>
                  </template>

                  <template v-if="sphere.materialType !== 'kaleidoscopic'">
                    <label>
                      Roughness
                      <div class="slider-row">
                        <input type="range" v-model.number="sphere.roughness" min="0" max="1" step="0.05" :disabled="isRendering" />
                        <span class="value">{{ sphere.roughness.toFixed(2) }}</span>
                      </div>
                    </label>
                  </template>

                  <div class="xyz-row">
                    <label>
                      X
                      <input type="number" v-model.number="sphere.x" step="0.5" :disabled="isRendering" />
                    </label>
                    <label>
                      Y
                      <input type="number" v-model.number="sphere.y" step="0.5" :disabled="isRendering" />
                    </label>
                    <label>
                      Z
                      <input type="number" v-model.number="sphere.z" step="0.5" :disabled="isRendering" />
                    </label>
                  </div>
                  <label>
                    Radius
                    <input type="number" v-model.number="sphere.radius" min="0.05" step="0.1" :disabled="isRendering" />
                  </label>
                </template>
              </div>

              <button class="btn-add-sphere" @click="addSphere" :disabled="isRendering">+ Add sphere</button>
            </section>
          </template>
        </aside>

        <!-- Drag resize handle (only when expanded) -->
        <div
          v-if="!builderCollapsed"
          class="builder-resize-handle"
          @mousedown.prevent="startBuilderResize"
        ></div>
      </template>

      <!-- Controls sidebar -->
      <aside class="controls">

        <section>
          <h2>Scene</h2>
          <select v-model="selectedSceneName" @change="onSceneChange" :disabled="isRendering">
            <option v-for="s in scenes" :key="s.name" :value="s.name">{{ s.label }}</option>
            <option value="custom">Custom</option>
          </select>
        </section>

        <section>
          <h2>Settings</h2>
          <label>
            Width
            <input type="number" v-model.number="form.width" min="50" max="2000" :disabled="isRendering" />
          </label>
          <label>
            Height
            <input type="number" v-model.number="form.height" min="50" max="2000" :disabled="isRendering" />
          </label>
          <label>
            Render levels <span class="hint">(1–12)</span>
            <div class="slider-row">
              <input type="range" v-model.number="form.renderLevels" min="1" max="12" :disabled="isRendering" />
              <span class="value">{{ form.renderLevels }}</span>
            </div>
          </label>
          <label>
            Anti-alias <span class="hint">(1–6)</span>
            <div class="slider-row">
              <input type="range" v-model.number="form.antiAlias" min="1" max="6" :disabled="isRendering" />
              <span class="value">{{ form.antiAlias }}</span>
            </div>
          </label>
        </section>

        <section>
          <h2>Camera</h2>
          <div class="camera-grid">
            <button class="cam-btn" @click="camMove('barrelLeft')"   :disabled="isRendering" title="Barrel left">↺</button>
            <button class="cam-btn" @click="camMove('moveForward')"  :disabled="isRendering" title="Move forward">↑</button>
            <button class="cam-btn" @click="camMove('barrelRight')"  :disabled="isRendering" title="Barrel right">↻</button>

            <button class="cam-btn" @click="camMove('moveLeft')"     :disabled="isRendering" title="Move left">←</button>
            <button class="cam-btn" @click="camMove('moveBackward')" :disabled="isRendering" title="Move backward">↓</button>
            <button class="cam-btn" @click="camMove('moveRight')"    :disabled="isRendering" title="Move right">→</button>

            <button class="cam-btn" @click="camMove('turnLeft')"     :disabled="isRendering" title="Turn left">⟵</button>
            <button class="cam-btn" @click="camMove('moveUp')"       :disabled="isRendering" title="Move up">⬆</button>
            <button class="cam-btn" @click="camMove('turnRight')"    :disabled="isRendering" title="Turn right">⟶</button>

            <button class="cam-btn" @click="camMove('turnUp')"       :disabled="isRendering" title="Turn up">⤴</button>
            <button class="cam-btn" @click="camMove('moveDown')"     :disabled="isRendering" title="Move down">⬇</button>
            <button class="cam-btn" @click="camMove('turnDown')"     :disabled="isRendering" title="Turn down">⤵</button>
          </div>
          <label class="checkbox-label">
            <input type="checkbox" v-model="autoRerender" />
            Re-render on move
          </label>
        </section>

        <section>
          <h2>Accumulation</h2>
          <label class="checkbox-label">
            <input type="checkbox" v-model="form.accumulate" :disabled="isRendering" />
            Multi-pass accumulation
          </label>
          <label v-if="form.accumulate">
            Samples <span class="hint">(2–64)</span>
            <div class="slider-row">
              <input type="range" v-model.number="form.sampleCount" min="2" max="64" :disabled="isRendering" />
              <span class="value">{{ form.sampleCount }}</span>
            </div>
          </label>
        </section>

        <section class="actions">
          <button class="btn-primary" @click="startRender" :disabled="isRendering">
            {{ isRendering ? 'Rendering…' : 'Render' }}
          </button>
          <button class="btn-danger" @click="cancelRender" :disabled="!isRendering || !currentJobId">
            Cancel
          </button>
        </section>

        <section v-if="isRendering || statusMessage" class="status">
          <div v-if="isRendering" class="progress-bar-container">
            <div class="progress-bar" :style="{ width: progress + '%' }"></div>
          </div>
          <p v-if="isRendering" class="progress-label">
            {{ progress }}%
            <span v-if="totalSamples > 1" class="sample-label">
              — pass {{ completedSamples + (progress < 100 ? 1 : 0) }}/{{ totalSamples }}
            </span>
          </p>
          <p v-if="statusMessage" :class="['status-msg', statusClass]">{{ statusMessage }}</p>
        </section>

      </aside>

      <!-- Image panel -->
      <main class="canvas-panel">
        <div class="image-frame">
          <img v-if="imageUrl" :src="imageUrl" class="render-img" alt="Render output" />
          <div v-else class="placeholder">
            <span v-if="isRendering" class="spinner"></span>
            <span v-else>No render yet</span>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import axios from 'axios'

// ─── state ────────────────────────────────────────────────────────────────────
const scenes = ref([])
const selectedSceneName = ref('')
const camera = ref(null)
const imageUrl = ref(null)
const currentJobId = ref(null)
const statusMessage = ref('')
const statusClass = ref('')
const pollTimer = ref(null)
const isRendering = ref(false)
const progress = ref(0)
const completedSamples = ref(0)
const totalSamples = ref(1)
const autoRerender = ref(false)

const form = ref({
  width: 500,
  height: 500,
  renderLevels: 4,
  antiAlias: 1,
  accumulate: false,
  sampleCount: 8,
})

const isCustomScene = computed(() => selectedSceneName.value === 'custom')

// ─── scene builder panel state ────────────────────────────────────────────────
const builderCollapsed = ref(false)
const builderWidth = ref(280)
const MIN_BUILDER_WIDTH = 180
const MAX_BUILDER_WIDTH = 600

function startBuilderResize(e) {
  const startX = e.clientX
  const startWidth = builderWidth.value

  function onMouseMove(ev) {
    const delta = ev.clientX - startX
    builderWidth.value = Math.min(MAX_BUILDER_WIDTH, Math.max(MIN_BUILDER_WIDTH, startWidth + delta))
  }

  function onMouseUp() {
    window.removeEventListener('mousemove', onMouseMove)
    window.removeEventListener('mouseup', onMouseUp)
  }

  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseup', onMouseUp)
}

// ─── custom scene state ───────────────────────────────────────────────────────
const defaultSphere = () => ({
  materialType: 'simple',
  hex: '#cc8888',
  emissive: false,
  emissionHex: '#ffffff',
  roughness: 0.8,
  transparency: 0,
  ior: 1.05,
  x: 0, y: 0.5, z: 0,
  radius: 0.5,
  collapsed: false,
})

const custom = ref({
  sky: { type: 'default', hex: '#ffffff' },
  light: { type: 'sun' },
  floor: { type: 'texture', texture: 'chequerboard', hex: '#888888', roughness: 1.0, height: 0 },
  spheres: [defaultSphere()],
})

// ─── scene loading ────────────────────────────────────────────────────────────
async function loadScenes() {
  try {
    const { data } = await axios.get('/api/render/scenes')
    scenes.value = data
    if (data.length) {
      selectedSceneName.value = data[0].name
      camera.value = data[0].defaultCamera
    }
  } catch {
    scenes.value = []
  }
}

function onSceneChange() {
  if (selectedSceneName.value === 'custom') {
    camera.value = { position: [3, 3, 3], look: [-0.577, -0.577, -0.577] }
    return
  }
  const scene = scenes.value.find(s => s.name === selectedSceneName.value)
  if (scene) camera.value = { ...scene.defaultCamera }
}

// ─── custom scene helpers ─────────────────────────────────────────────────────
function addSphere() {
  custom.value.spheres.push(defaultSphere())
}

function removeSphere(idx) {
  custom.value.spheres.splice(idx, 1)
}

function hexToRgb(hex) {
  const n = parseInt(hex.replace('#', ''), 16)
  return [((n >> 16) & 255) / 255, ((n >> 8) & 255) / 255, (n & 255) / 255]
}

function buildCustomPayload() {
  const c = custom.value

  const floorDto = c.floor.type === 'none' ? null : {
    type: c.floor.type,
    texture: c.floor.type === 'texture' ? c.floor.texture : null,
    color: c.floor.type === 'solid' ? hexToRgb(c.floor.hex) : null,
    roughness: c.floor.roughness,
    height: c.floor.height,
  }

  const skyDto = {
    type: c.sky.type,
    preset: c.sky.type !== 'solid' ? c.sky.type : null,
    color: c.sky.type === 'solid' ? hexToRgb(c.sky.hex) : null,
  }

  const spheres = c.spheres.map(s => ({
    x: s.x, y: s.y, z: s.z,
    radius: s.radius,
    materialType: s.materialType,
    color: s.materialType === 'simple' ? hexToRgb(s.hex) : null,
    emission: (s.materialType === 'simple' && s.emissive) ? hexToRgb(s.emissionHex) : null,
    roughness: s.roughness,
    transparency: s.transparency,
    indexOfRefraction: s.ior,
  }))

  return { floor: floorDto, sky: skyDto, light: c.light, spheres }
}

// ─── export ───────────────────────────────────────────────────────────────────
function exportScene() {
  const payload = buildCustomPayload()
  const json = JSON.stringify(payload, null, 2)
  const blob = new Blob([json], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  const now = new Date()
  const ts = now.toISOString().replace(/[:.]/g, '-').slice(0, 19)
  a.href = url
  a.download = `${ts}_scene.json`
  a.click()
  URL.revokeObjectURL(url)
}

// ─── render ───────────────────────────────────────────────────────────────────
async function startRender() {
  clearPoll()
  imageUrl.value = null
  progress.value = 0
  completedSamples.value = 0
  totalSamples.value = form.value.accumulate ? form.value.sampleCount : 1
  isRendering.value = true
  setStatus('', '')

  const sampleCount = form.value.accumulate ? form.value.sampleCount : 1
  const renderParams = {
    width: form.value.width,
    height: form.value.height,
    renderLevels: form.value.renderLevels,
    antiAlias: form.value.antiAlias,
    sampleCount,
  }

  try {
    let response
    if (isCustomScene.value) {
      response = await axios.post('/api/render/custom', {
        ...renderParams,
        scene: buildCustomPayload(),
        camera: camera.value,
      })
    } else {
      const sceneDescriptor = {
        name: selectedSceneName.value,
        label: scenes.value.find(s => s.name === selectedSceneName.value)?.label ?? selectedSceneName.value,
        defaultCamera: camera.value,
      }
      response = await axios.post('/api/render', { ...renderParams, scene: sceneDescriptor })
    }
    currentJobId.value = response.data.jobId
    pollTimer.value = setInterval(pollStatus, 500)
  } catch (e) {
    isRendering.value = false
    setStatus('Failed to start render: ' + e.message, 'error')
  }
}

async function pollStatus() {
  if (!currentJobId.value) return
  try {
    const { data } = await axios.get(`/api/render/${currentJobId.value}/status`)
    progress.value = data.progress ?? 0
    completedSamples.value = data.completedSamples ?? 0
    if (data.status === 'COMPLETE') {
      clearPoll()
      isRendering.value = false
      progress.value = 100
      setStatus('Done!', 'success')
      imageUrl.value = `/api/render/${currentJobId.value}/image?t=${Date.now()}`
    } else if (data.status === 'ERROR') {
      clearPoll()
      isRendering.value = false
      setStatus('Render failed: ' + (data.error || 'unknown error'), 'error')
    }
  } catch { /* transient, keep polling */ }
}

async function cancelRender() {
  if (!currentJobId.value) return
  clearPoll()
  try { await axios.delete(`/api/render/${currentJobId.value}`) } catch { /* ignore */ }
  isRendering.value = false
  setStatus('Cancelled', '')
}

// ─── camera ───────────────────────────────────────────────────────────────────
async function camMove(action) {
  if (!camera.value) return
  try {
    const { data } = await axios.post('/api/camera/move', { camera: camera.value, action })
    camera.value = data
    if (autoRerender.value && !isRendering.value) startRender()
  } catch (e) {
    setStatus('Camera error: ' + e.message, 'error')
  }
}

// ─── helpers ──────────────────────────────────────────────────────────────────
function clearPoll() {
  if (pollTimer.value) { clearInterval(pollTimer.value); pollTimer.value = null }
}

function setStatus(msg, cls) {
  statusMessage.value = msg
  statusClass.value = cls
}

onUnmounted(clearPoll)
loadScenes()
</script>

<style>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

body {
  font-family: system-ui, sans-serif;
  background: #1a1a2e;
  color: #e0e0e0;
  min-height: 100vh;
}

.app { display: flex; flex-direction: column; min-height: 100vh; }

header {
  background: #16213e;
  padding: 0.75rem 2rem;
  border-bottom: 1px solid #0f3460;
  flex-shrink: 0;
}

header h1 { font-size: 1.3rem; color: #e94560; letter-spacing: 0.05em; }

.layout { display: flex; flex: 1; overflow: hidden; }

/* ── Shared sidebar styles ── */
.controls, .scene-builder {
  background: #16213e;
  border-right: 1px solid #0f3460;
  padding: 1.25rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  overflow-y: auto;
  flex-shrink: 0;
}

.controls {
  width: 260px;
  min-width: 260px;
}

.scene-builder {
  position: relative;
  width: 280px;
  min-width: 280px;
  overflow-x: hidden;
}

.scene-builder--collapsed {
  width: 28px !important;
  min-width: 28px !important;
  padding: 0;
  overflow: hidden;
  gap: 0;
}

/* ── Builder collapse tab ── */
.builder-collapse-tab {
  position: absolute;
  top: 50%;
  right: 0;
  transform: translateY(-50%);
  width: 18px;
  height: 48px;
  background: #0f3460;
  border: 1px solid #1a4a7a;
  border-right: none;
  border-radius: 4px 0 0 4px;
  color: #aaa;
  font-size: 0.9rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  padding: 0;
  transition: background 0.12s, color 0.12s;
}
.builder-collapse-tab:hover { background: #1a5090; color: #e0e0e0; }

.scene-builder--collapsed .builder-collapse-tab {
  right: auto;
  left: 5px;
  border-right: 1px solid #1a4a7a;
  border-left: none;
  border-radius: 0 4px 4px 0;
}

/* ── Drag resize handle ── */
.builder-resize-handle {
  width: 4px;
  cursor: col-resize;
  background: transparent;
  flex-shrink: 0;
  transition: background 0.15s;
}
.builder-resize-handle:hover { background: #e94560; }

/* ── Sidebar headings ── */
.controls h2, .scene-builder h2 {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: #666;
  margin-bottom: 0.6rem;
}

/* ── Builder header ── */
.builder-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 0.85rem;
  font-weight: 600;
  color: #ccc;
  padding-bottom: 0.25rem;
  border-bottom: 1px solid #0f3460;
  padding-right: 20px;
}

.btn-export {
  background: #0f3460;
  border: 1px solid #1a4a7a;
  border-radius: 4px;
  color: #aaa;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  cursor: pointer;
  transition: background 0.12s;
  white-space: nowrap;
}
.btn-export:hover { background: #1a5090; color: #e0e0e0; }

/* ── Sphere cards ── */
.sphere-card {
  background: #0f1e3a;
  border: 1px solid #1a3a6a;
  border-radius: 6px;
  padding: 0;
  margin-bottom: 0.75rem;
  overflow: hidden;
}

.sphere-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: #aaa;
  padding: 0.5rem 0.65rem;
  cursor: pointer;
  user-select: none;
  gap: 0.4rem;
}
.sphere-card-header:hover { background: #162040; }

.sphere-card > template + * {
  padding: 0 0.75rem 0.75rem;
}

/* body of an open sphere card — first non-header child after the template */
.sphere-card label,
.sphere-card select,
.sphere-card .xyz-row,
.sphere-card .slider-row,
.sphere-card input[type="number"] {
  /* inherits sidebar styles; add left/right padding via wrapping */
}

.sphere-card > .sphere-card-header ~ label,
.sphere-card > .sphere-card-header ~ .xyz-row,
.sphere-card > .sphere-card-header ~ template {
  padding: 0 0.75rem;
}

/* Simpler: add padding to the card body via a wrapper div */
.sphere-card-body {
  padding: 0.1rem 0.75rem 0.75rem;
}

.sphere-card-title {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-weight: 500;
}

.sphere-collapse-icon {
  font-size: 0.6rem;
  color: #555;
}

.sphere-summary {
  flex: 1;
  font-size: 0.72rem;
  color: #555;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: right;
  padding-right: 0.4rem;
}

.btn-remove {
  background: none;
  border: none;
  color: #555;
  cursor: pointer;
  font-size: 0.85rem;
  padding: 0 0.2rem;
  line-height: 1;
  transition: color 0.12s;
  flex-shrink: 0;
}
.btn-remove:hover:not(:disabled) { color: #e94560; }
.btn-remove:disabled { opacity: 0.3; cursor: not-allowed; }

.btn-add-sphere {
  width: 100%;
  padding: 0.5rem;
  background: #0f3460;
  border: 1px dashed #1a4a7a;
  border-radius: 4px;
  color: #888;
  font-size: 0.82rem;
  cursor: pointer;
  transition: background 0.12s, color 0.12s;
}
.btn-add-sphere:hover:not(:disabled) { background: #1a5090; color: #e0e0e0; }
.btn-add-sphere:disabled { opacity: 0.4; cursor: not-allowed; }

/* ── XYZ row ── */
.xyz-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 0.4rem;
}
.xyz-row label { margin-bottom: 0; }
.xyz-row input[type="number"] { padding: 0.3rem 0.4rem; font-size: 0.8rem; }

/* ── Colour input ── */
.color-input {
  width: 100%;
  height: 32px;
  padding: 2px 4px;
  background: #0f3460;
  border: 1px solid #1a4a7a;
  border-radius: 4px;
  cursor: pointer;
}

/* ── Form controls (shared) ── */
select, input[type="number"] {
  width: 100%;
  padding: 0.4rem 0.6rem;
  background: #0f3460;
  border: 1px solid #1a4a7a;
  border-radius: 4px;
  color: #e0e0e0;
  font-size: 0.875rem;
}
select:disabled, input:disabled { opacity: 0.5; cursor: not-allowed; }

label {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  font-size: 0.825rem;
  color: #aaa;
  margin-bottom: 0.65rem;
}

.slider-row { display: flex; align-items: center; gap: 0.5rem; }
.slider-row input[type="range"] { flex: 1; accent-color: #e94560; }
.value { font-size: 0.875rem; color: #e0e0e0; min-width: 2.8rem; text-align: right; }
.hint { font-size: 0.72rem; color: #555; }

/* ── Camera grid ── */
.camera-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px;
  margin-bottom: 0.6rem;
}

.cam-btn {
  background: #0f3460;
  border: 1px solid #1a4a7a;
  border-radius: 4px;
  color: #e0e0e0;
  font-size: 1rem;
  padding: 0.35rem;
  cursor: pointer;
  text-align: center;
  line-height: 1;
  transition: background 0.12s;
}
.cam-btn:hover:not(:disabled) { background: #1a5090; }
.cam-btn:disabled { opacity: 0.4; cursor: not-allowed; }

.checkbox-label {
  flex-direction: row;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.825rem;
  color: #aaa;
  cursor: pointer;
  margin-bottom: 0;
}
.checkbox-label input[type="checkbox"] { width: auto; accent-color: #e94560; }

/* ── Actions ── */
.actions { display: flex; flex-direction: column; gap: 0.5rem; }

.btn-primary, .btn-danger {
  padding: 0.55rem 1rem;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: opacity 0.15s;
}
.btn-primary { background: #e94560; color: white; }
.btn-danger  { background: #333; color: #ccc; }
.btn-primary:disabled, .btn-danger:disabled { opacity: 0.4; cursor: not-allowed; }

/* ── Progress ── */
.progress-bar-container {
  height: 6px;
  background: #0f3460;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 0.3rem;
}
.progress-bar {
  height: 100%;
  background: #e94560;
  border-radius: 3px;
  transition: width 0.3s ease;
}
.progress-label { font-size: 0.78rem; color: #888; text-align: right; }
.sample-label { color: #666; }

.status-msg { font-size: 0.82rem; padding: 0.4rem 0.5rem; border-radius: 4px; background: #0f3460; margin-top: 0.3rem; }
.status-msg.success { color: #4caf50; }
.status-msg.error   { color: #e94560; }

/* ── Image panel ── */
.canvas-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #0a0a1a;
  padding: 1rem;
}

.image-frame {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: black;
  border: 1px solid #0f3460;
  border-radius: 4px;
  overflow: hidden;
}

.render-img {
  max-width: 100%;
  max-height: 100%;
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.placeholder {
  color: #333;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid #222;
  border-top-color: #e94560;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>
