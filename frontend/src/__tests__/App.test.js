import { mount, flushPromises } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import App from '../App.vue'

// ── axios mock ──────────────────────────────────────────────────────────────
vi.mock('axios', () => {
  const get  = vi.fn()
  const post = vi.fn()
  const del  = vi.fn()
  return { default: { get, post, delete: del } }
})

import axios from 'axios'

const SCENES = [
  { name: 'simple_ball',  label: 'Simple ball',  defaultCamera: { position: [3,3,3], look: [-0.577,-0.577,-0.577] } },
  { name: 'pool_balls',   label: 'Pool balls',   defaultCamera: { position: [1,2,-2], look: [-0.2,-0.5, 0.7] } },
  { name: 'sunset_balls', label: 'Sunset balls', defaultCamera: { position: [9,2,9],  look: [-0.6,-0.1,-0.6] } },
  { name: 'cowboy_pingy', label: 'Cowboy pingy', defaultCamera: { position: [3,3,3],  look: [-0.577,-0.577,-0.577] } },
]

// Reset all mock state between every test so calls don't bleed across describes
beforeEach(() => { vi.clearAllMocks() })

function setupMocks({
  statusSequence = [{ status: 'COMPLETE', progress: 100 }],
  cameraResponse = { position: [2.9, 2.9, 2.9], look: [-0.577, -0.577, -0.577] },
} = {}) {
  let pollCall = 0
  axios.get.mockImplementation((url) => {
    if (url === '/api/render/scenes') return Promise.resolve({ data: SCENES })
    if (url.includes('/status')) {
      const res = statusSequence[Math.min(pollCall++, statusSequence.length - 1)]
      return Promise.resolve({ data: res })
    }
    return Promise.reject(new Error('unexpected GET ' + url))
  })
  axios.post.mockImplementation((url) => {
    if (url === '/api/render')      return Promise.resolve({ data: { jobId: 'test-job-123' } })
    if (url === '/api/camera/move') return Promise.resolve({ data: cameraResponse })
    return Promise.reject(new Error('unexpected POST ' + url))
  })
  axios.delete.mockResolvedValue({})
}

// ── Scene loading ────────────────────────────────────────────────────────────

describe('scene loading', () => {
  it('populates the scene selector with all presets', async () => {
    setupMocks()
    const wrapper = mount(App)
    await flushPromises()

    const options = wrapper.findAll('select option')
    // preset scenes + the static "Custom" option
    expect(options).toHaveLength(SCENES.length + 1)
    expect(options[0].text()).toBe('Simple ball')
    expect(options[1].text()).toBe('Pool balls')
    expect(options[SCENES.length].text()).toBe('Custom')
  })

  it('sets the first scene as selected by default', async () => {
    setupMocks()
    const wrapper = mount(App)
    await flushPromises()

    expect(wrapper.find('select').element.value).toBe('simple_ball')
  })

  it('updates camera when a different scene is selected', async () => {
    setupMocks()
    const wrapper = mount(App)
    await flushPromises()

    await wrapper.find('select').setValue('pool_balls')
    await wrapper.find('select').trigger('change')

    await wrapper.find('button.btn-primary').trigger('click')
    await flushPromises()

    const renderCall = axios.post.mock.calls.find(c => c[0] === '/api/render')
    expect(renderCall[1].scene.defaultCamera.position).toEqual([1, 2, -2])
  })
})

// ── Render submission ────────────────────────────────────────────────────────

describe('render submission', () => {
  beforeEach(() => { vi.useFakeTimers() })
  afterEach(() => { vi.useRealTimers() })

  it('sends scene descriptor and settings to POST /api/render', async () => {
    setupMocks()
    const wrapper = mount(App)
    await flushPromises()

    await wrapper.find('button.btn-primary').trigger('click')
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith('/api/render', expect.objectContaining({
      scene: expect.objectContaining({ name: 'simple_ball' }),
      width: 500,
      height: 500,
    }))
  })

  it('includes current camera in the render request', async () => {
    setupMocks()
    const wrapper = mount(App)
    await flushPromises()

    await wrapper.find('button.btn-primary').trigger('click')
    await flushPromises()

    const call = axios.post.mock.calls.find(c => c[0] === '/api/render')
    expect(call[1].scene.defaultCamera.position).toHaveLength(3)
    expect(call[1].scene.defaultCamera.look).toHaveLength(3)
  })

  it('disables Render button while rendering', async () => {
    setupMocks({ statusSequence: [{ status: 'RUNNING', progress: 10 }] })
    const wrapper = mount(App)
    await flushPromises()

    await wrapper.find('button.btn-primary').trigger('click')
    await flushPromises()

    expect(wrapper.find('button.btn-primary').attributes('disabled')).toBeDefined()
  })
})

// ── Progress polling ─────────────────────────────────────────────────────────

describe('progress polling', () => {
  beforeEach(() => { vi.useFakeTimers() })
  afterEach(() => { vi.useRealTimers() })

  it('shows progress bar while rendering', async () => {
    setupMocks({ statusSequence: [{ status: 'RUNNING', progress: 30 }, { status: 'COMPLETE', progress: 100 }] })
    const wrapper = mount(App)
    await flushPromises()

    await wrapper.find('button.btn-primary').trigger('click')
    await flushPromises()

    vi.advanceTimersByTime(500)
    await flushPromises()

    expect(wrapper.find('.progress-bar').exists()).toBe(true)
  })

  it('updates progress percentage text', async () => {
    setupMocks({ statusSequence: [{ status: 'RUNNING', progress: 42 }, { status: 'COMPLETE', progress: 100 }] })
    const wrapper = mount(App)
    await flushPromises()

    await wrapper.find('button.btn-primary').trigger('click')
    await flushPromises()

    vi.advanceTimersByTime(500)
    await flushPromises()

    expect(wrapper.find('.progress-label').text()).toContain('42')
  })

  it('shows image and Done status when render completes', async () => {
    setupMocks({ statusSequence: [{ status: 'COMPLETE', progress: 100 }] })
    const wrapper = mount(App)
    await flushPromises()

    await wrapper.find('button.btn-primary').trigger('click')
    await flushPromises()

    vi.advanceTimersByTime(500)
    await flushPromises()

    expect(wrapper.find('img.render-img').exists()).toBe(true)
    expect(wrapper.find('img.render-img').attributes('src')).toContain('test-job-123')
    expect(wrapper.find('.status-msg').text()).toContain('Done')
  })

  it('shows error message when render fails', async () => {
    setupMocks({ statusSequence: [{ status: 'ERROR', progress: 50, error: 'out of memory' }] })
    const wrapper = mount(App)
    await flushPromises()

    await wrapper.find('button.btn-primary').trigger('click')
    await flushPromises()

    vi.advanceTimersByTime(500)
    await flushPromises()

    expect(wrapper.find('.status-msg.error').text()).toContain('out of memory')
  })

  it('cancel button calls DELETE and re-enables Render', async () => {
    setupMocks({ statusSequence: [{ status: 'RUNNING', progress: 20 }] })
    const wrapper = mount(App)
    await flushPromises()

    await wrapper.find('button.btn-primary').trigger('click')
    await flushPromises()

    const cancelBtn = wrapper.find('button.btn-danger')
    expect(cancelBtn.attributes('disabled')).toBeUndefined()
    await cancelBtn.trigger('click')
    await flushPromises()

    expect(axios.delete).toHaveBeenCalledWith('/api/render/test-job-123')
    expect(wrapper.find('button.btn-primary').attributes('disabled')).toBeUndefined()
  })
})

// ── Camera controls ──────────────────────────────────────────────────────────

describe('camera controls', () => {
  it('renders 12 camera buttons', async () => {
    setupMocks()
    const wrapper = mount(App)
    await flushPromises()

    expect(wrapper.findAll('.cam-btn')).toHaveLength(12)
  })

  it('clicking a camera button calls POST /api/camera/move with current camera', async () => {
    setupMocks()
    const wrapper = mount(App)
    await flushPromises()

    await wrapper.findAll('.cam-btn')[1].trigger('click') // moveForward
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith('/api/camera/move', expect.objectContaining({
      camera: expect.objectContaining({ position: expect.any(Array) }),
      action: expect.any(String),
    }))
  })

  it('camera state is updated with response from /api/camera/move', async () => {
    const newPos = [2.9, 2.9, 2.9]
    setupMocks({ cameraResponse: { position: newPos, look: [-0.577, -0.577, -0.577] } })
    const wrapper = mount(App)
    await flushPromises()

    await wrapper.findAll('.cam-btn')[1].trigger('click') // moveForward
    await flushPromises()

    // Subsequent render should carry the updated camera
    await wrapper.find('button.btn-primary').trigger('click')
    await flushPromises()

    const renderCall = axios.post.mock.calls.find(c => c[0] === '/api/render')
    expect(renderCall[1].scene.defaultCamera.position).toEqual(newPos)
  })

  it('camera buttons are disabled while rendering', async () => {
    vi.useFakeTimers()
    setupMocks({ statusSequence: [{ status: 'RUNNING', progress: 5 }] })
    const wrapper = mount(App)
    await flushPromises()

    await wrapper.find('button.btn-primary').trigger('click')
    await flushPromises()

    wrapper.findAll('.cam-btn').forEach(btn => {
      expect(btn.attributes('disabled')).toBeDefined()
    })
    vi.useRealTimers()
  })
})

// ── Auto re-render ───────────────────────────────────────────────────────────

describe('auto re-render on camera move', () => {
  it('does NOT auto-render when checkbox is unchecked (default)', async () => {
    setupMocks()
    const wrapper = mount(App)
    await flushPromises()

    vi.clearAllMocks() // clear the loadScenes GET call counts
    setupMocks()       // re-setup so camera/move still works

    const checkbox = wrapper.find('input[type="checkbox"]')
    expect(checkbox.element.checked).toBe(false)

    await wrapper.findAll('.cam-btn')[1].trigger('click')
    await flushPromises()

    const renderCalls = axios.post.mock.calls.filter(c => c[0] === '/api/render')
    expect(renderCalls).toHaveLength(0)
  })

  it('auto-renders when checkbox is checked and camera moves', async () => {
    vi.useFakeTimers()
    setupMocks({ statusSequence: [{ status: 'RUNNING', progress: 0 }, { status: 'COMPLETE', progress: 100 }] })
    const wrapper = mount(App)
    await flushPromises()

    await wrapper.find('input[type="checkbox"]').setValue(true)
    await wrapper.findAll('.cam-btn')[1].trigger('click')
    await flushPromises()

    const renderCalls = axios.post.mock.calls.filter(c => c[0] === '/api/render')
    expect(renderCalls).toHaveLength(1)
    vi.useRealTimers()
  })
})

// ── Image display / letterboxing ─────────────────────────────────────────────

describe('image display', () => {
  beforeEach(() => { vi.useFakeTimers() })
  afterEach(() => { vi.useRealTimers() })

  it('shows placeholder text before first render', async () => {
    setupMocks()
    const wrapper = mount(App)
    await flushPromises()

    expect(wrapper.find('.placeholder').exists()).toBe(true)
    expect(wrapper.find('.placeholder').text()).toContain('No render yet')
  })

  it('image-frame container exists for letterboxing', async () => {
    setupMocks()
    const wrapper = mount(App)
    await flushPromises()

    expect(wrapper.find('.image-frame').exists()).toBe(true)
  })

  it('render-img appears with correct src after render completes', async () => {
    setupMocks({ statusSequence: [{ status: 'COMPLETE', progress: 100 }] })
    const wrapper = mount(App)
    await flushPromises()

    await wrapper.find('button.btn-primary').trigger('click')
    await flushPromises()

    vi.advanceTimersByTime(500)
    await flushPromises()

    const img = wrapper.find('img.render-img')
    expect(img.exists()).toBe(true)
    expect(img.attributes('src')).toContain('/api/render/test-job-123/image')
  })

  it('render-img has render-img class (carrier for object-fit:contain)', async () => {
    setupMocks({ statusSequence: [{ status: 'COMPLETE', progress: 100 }] })
    const wrapper = mount(App)
    await flushPromises()

    await wrapper.find('button.btn-primary').trigger('click')
    await flushPromises()

    vi.advanceTimersByTime(500)
    await flushPromises()

    expect(wrapper.find('img.render-img').exists()).toBe(true)
  })

  it('placeholder is replaced by image after render completes', async () => {
    setupMocks({ statusSequence: [{ status: 'COMPLETE', progress: 100 }] })
    const wrapper = mount(App)
    await flushPromises()

    expect(wrapper.find('.placeholder').exists()).toBe(true)

    await wrapper.find('button.btn-primary').trigger('click')
    await flushPromises()

    vi.advanceTimersByTime(500)
    await flushPromises()

    expect(wrapper.find('.placeholder').exists()).toBe(false)
    expect(wrapper.find('img.render-img').exists()).toBe(true)
  })
})
