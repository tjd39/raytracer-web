<template>
  <div class="app">
    <header>
      <h1>Ray Tracer</h1>
    </header>

    <div class="layout">
      <!-- Controls sidebar -->
      <aside class="controls">

        <section>
          <h2>Scene</h2>
          <select v-model="selectedSceneName" @change="onSceneChange" :disabled="isRendering">
            <option v-for="s in scenes" :key="s.name" :value="s.name">{{ s.label }}</option>
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
            <button class="cam-btn" @click="camMove('barrelLeft')"  :disabled="isRendering" title="Barrel left">↺</button>
            <button class="cam-btn" @click="camMove('moveForward')" :disabled="isRendering" title="Move forward">↑</button>
            <button class="cam-btn" @click="camMove('barrelRight')" :disabled="isRendering" title="Barrel right">↻</button>

            <button class="cam-btn" @click="camMove('moveLeft')"    :disabled="isRendering" title="Move left">←</button>
            <button class="cam-btn" @click="camMove('moveBackward')" :disabled="isRendering" title="Move backward">↓</button>
            <button class="cam-btn" @click="camMove('moveRight')"   :disabled="isRendering" title="Move right">→</button>

            <button class="cam-btn" @click="camMove('turnLeft')"    :disabled="isRendering" title="Turn left">⟵</button>
            <button class="cam-btn" @click="camMove('moveUp')"      :disabled="isRendering" title="Move up">⬆</button>
            <button class="cam-btn" @click="camMove('turnRight')"   :disabled="isRendering" title="Turn right">⟶</button>

            <button class="cam-btn" @click="camMove('turnUp')"      :disabled="isRendering" title="Turn up">⤴</button>
            <button class="cam-btn" @click="camMove('moveDown')"    :disabled="isRendering" title="Move down">⬇</button>
            <button class="cam-btn" @click="camMove('turnDown')"    :disabled="isRendering" title="Turn down">⤵</button>
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
import { ref, onUnmounted } from 'vue'
import axios from 'axios'

// --- state ---
const scenes = ref([])
const selectedSceneName = ref('')
const camera = ref(null)   // CameraDto: { position: [x,y,z], look: [x,y,z] }
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

// --- scene ---
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
  const scene = scenes.value.find(s => s.name === selectedSceneName.value)
  if (scene) camera.value = { ...scene.defaultCamera }
}

// --- render ---
async function startRender() {
  clearPoll()
  imageUrl.value = null
  progress.value = 0
  completedSamples.value = 0
  totalSamples.value = form.value.accumulate ? form.value.sampleCount : 1
  isRendering.value = true
  setStatus('', '')

  const sceneDescriptor = {
    name: selectedSceneName.value,
    label: scenes.value.find(s => s.name === selectedSceneName.value)?.label ?? selectedSceneName.value,
    defaultCamera: camera.value,
  }

  try {
    const { data } = await axios.post('/api/render', {
      scene: sceneDescriptor,
      width: form.value.width,
      height: form.value.height,
      renderLevels: form.value.renderLevels,
      antiAlias: form.value.antiAlias,
      sampleCount: form.value.accumulate ? form.value.sampleCount : 1,
    })
    currentJobId.value = data.jobId
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

// --- camera ---
async function camMove(action) {
  if (!camera.value) return
  try {
    const { data } = await axios.post('/api/camera/move', { camera: camera.value, action })
    camera.value = data
    if (autoRerender.value && !isRendering.value) {
      startRender()
    }
  } catch (e) {
    setStatus('Camera error: ' + e.message, 'error')
  }
}

// --- helpers ---
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

/* ── Sidebar ── */
.controls {
  width: 260px;
  min-width: 260px;
  background: #16213e;
  border-right: 1px solid #0f3460;
  padding: 1.25rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  overflow-y: auto;
}

.controls h2 {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: #666;
  margin-bottom: 0.6rem;
}

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
.value { font-size: 0.875rem; color: #e0e0e0; min-width: 1.5rem; text-align: right; }
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

/* Fixed frame that never changes — the image letterboxes inside it */
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

/* object-fit:contain handles letterboxing at any aspect ratio */
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
