const offlineTts = require('../../utils/offline-tts')
const {
  BASE_RADIUS_RATIO,
  threadMetrics,
  nailMetrics
} = require('../../utils/preview-metrics')

const THREAD_PATH_BATCH = 64

Page({
  data: {
    text: {},
    current: {},
    sequenceCount: 0,
    index: 0,
    previousValue: '—',
    currentValue: '—',
    nextValue: '—',
    progress: 0,
    playing: false,
    speaking: false,
    voiceEnabled: true,
    voiceSupported: true,
    jumpValue: '',
    delayStep: 15,
    delayLabel: '1.5 s',
    voiceRateStep: 125,
    voiceRateLabel: '1.25×',
    canvasSize: 300,
    readerPreviewOpen: false,
    previewAnimationSpeed: 5,
    previewAnimationRunning: false
  },

  onLoad() {
    const app = getApp()
    const current = app.globalData.current
    if (!current || !current.sequence || current.sequence.length < 2) {
      wx.navigateBack()
      return
    }
    const delayStep = Math.max(5, Math.min(50, Number(wx.getStorageSync('sah_reader_delay')) || 15))
    const voiceRateStep = Math.max(75, Math.min(160, Number(wx.getStorageSync('sah_reader_voice_rate')) || 125))
    const storedVoice = wx.getStorageSync('sah_reader_voice')
    offlineTts.setRate(voiceRateStep / 100)
    this.current = current
    this.sequence = current.sequence
    this.params = Object.assign({
      nails: Math.max.apply(null, this.sequence) + 1,
      circleMm: 260,
      lineMm: 0.1
    }, current.params || {})
    this.index = Math.max(0, Math.min(Number(current.index) || 0, this.sequence.length - 1))
    const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    this.windowWidth = info.windowWidth
    this.windowHeight = info.windowHeight
    this.fullPreviewSize = this.calculateFullPreviewSize(info)
    this.setData({
      text: app.globalData.text,
      current: { name: current.name },
      sequenceCount: this.sequence.length,
      delayStep,
      delayLabel: (delayStep / 10).toFixed(1) + ' s',
      voiceRateStep,
      voiceRateLabel: (voiceRateStep / 100).toFixed(2) + '×',
      canvasSize: this.fullPreviewSize,
      voiceEnabled: storedVoice !== false,
      voiceSupported: offlineTts.supported()
    })
    this.refresh()
    wx.setNavigationBarTitle({ title: app.globalData.text.readerTitle })
  },

  onReady() {
    // The combined live/full-animation canvas is created only after the bottom preview button is pressed.
  },

  calculateFullPreviewSize(info) {
    return Math.floor(Math.max(120, Math.min(info.windowWidth - 24, info.windowHeight - 350)))
  },

  initializePreviewCanvas() {
    wx.createSelectorQuery()
      .in(this)
      .select('#readerPreviewCanvas')
      .fields({ node: true, size: true })
      .exec((result) => {
        const field = result && result[0]
        if (!field || !field.node) return
        this.previewCanvas = field.node
        this.previewCtx = field.node.getContext('2d')
        const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
        this.previewRatio = Math.min(3, info.pixelRatio || 1)
        this.previewSide = field.width
        field.node.width = Math.round(field.width * this.previewRatio)
        field.node.height = Math.round(field.height * this.previewRatio)
        this.resetPreviewLayer()
        this.drawRealtimePreview()
      })
  },

  onResize(event) {
    const size = event && event.size ? event.size : event
    if (!size || !size.windowWidth || !size.windowHeight) return
    this.windowWidth = size.windowWidth
    this.windowHeight = size.windowHeight
    this.fullPreviewSize = this.calculateFullPreviewSize(size)
    if (this.data.readerPreviewOpen) {
      this.setData({ canvasSize: this.fullPreviewSize }, () => this.initializePreviewCanvas())
    }
  },

  onHide() {
    this.pause()
    this.stopReaderPreviewAnimation()
    this.forceSave()
  },

  onUnload() {
    this.pause()
    this.forceSave()
    offlineTts.destroy()
    if (this.previewDrawTimer) clearTimeout(this.previewDrawTimer)
    this.previewDrawTimer = null
    if (this.previewAnimationTimer) clearTimeout(this.previewAnimationTimer)
    this.previewAnimationTimer = null
  },

  forceSave() {
    try {
      getApp().setCurrent(this.current, { immediate: true })
    } catch (error) {
      wx.showModal({
        title: this.data.text.error,
        content: '自动保存失败：' + (error.message || String(error)),
        showCancel: false
      })
    }
  },

  refresh() {
    const last = this.sequence.length - 1
    this.setData({
      index: this.index,
      previousValue: this.index > 0 ? this.sequence[this.index - 1] : '—',
      currentValue: this.sequence[this.index],
      nextValue: this.index < last ? this.sequence[this.index + 1] : '—',
      progress: last > 0 ? Math.round(this.index / last * 100) : 0
    })
    this.current.index = this.index
    getApp().setCurrent(this.current)
    this.requestPreviewDraw()
  },

  previewPoint(pin, center, radius) {
    const angle = Math.PI * 2 * pin / this.params.nails
    return {
      x: center + Math.cos(angle) * radius,
      y: center + Math.sin(angle) * radius
    }
  },

  drawPreviewSegments(ctx, first, last, center, radius) {
    for (let batchStart = first; batchStart <= last; batchStart += THREAD_PATH_BATCH) {
      const batchEnd = Math.min(last, batchStart + THREAD_PATH_BATCH - 1)
      ctx.beginPath()
      for (let i = batchStart; i <= batchEnd; i += 1) {
        const from = this.previewPoint(this.sequence[i - 1], center, radius)
        const to = this.previewPoint(this.sequence[i], center, radius)
        ctx.moveTo(from.x, from.y)
        ctx.lineTo(to.x, to.y)
      }
      ctx.stroke()
    }
  },

  resetPreviewLayer() {
    this.previewLayer = null
    this.previewLayerCtx = null
    this.previewLayerEnd = 0
    if (typeof wx.createOffscreenCanvas !== 'function' || !this.previewSide) return
    try {
      const pixels = Math.round(this.previewSide * this.previewRatio)
      this.previewLayer = wx.createOffscreenCanvas({ type: '2d', width: pixels, height: pixels })
      this.previewLayerCtx = this.previewLayer.getContext('2d')
      this.previewLayerCtx.setTransform(this.previewRatio, 0, 0, this.previewRatio, 0, 0)
    } catch (error) {
      this.previewLayer = null
      this.previewLayerCtx = null
    }
  },

  syncPreviewLayer(end, metrics, center, radius) {
    const ctx = this.previewLayerCtx
    if (!ctx) return false
    if (end < this.previewLayerEnd) {
      ctx.clearRect(0, 0, this.previewSide, this.previewSide)
      this.previewLayerEnd = 0
    }
    if (end > this.previewLayerEnd) {
      ctx.strokeStyle = 'rgba(18,18,18,' + metrics.alpha.toFixed(3) + ')'
      ctx.lineWidth = metrics.stroke
      ctx.lineCap = 'butt'
      ctx.lineJoin = 'miter'
      this.drawPreviewSegments(ctx, this.previewLayerEnd + 1, end, center, radius)
      this.previewLayerEnd = end
    }
    return true
  },

  drawPreviewNails(ctx, center, radius) {
    const metrics = nailMetrics(this.params.nails, radius)
    ctx.textBaseline = 'middle'
    for (let i = 0; i < this.params.nails; i += 1) {
      const angle = Math.PI * 2 * i / this.params.nails
      const cos = Math.cos(angle)
      const sin = Math.sin(angle)
      ctx.beginPath()
      ctx.arc(center + cos * radius, center + sin * radius, metrics.dotRadius, 0, Math.PI * 2)
      ctx.fillStyle = '#55555f'
      ctx.fill()
      const fontSize = i % 10 === 0
        ? metrics.baseText * 2
        : (i % 5 === 0 ? metrics.baseText * 1.5 : metrics.baseText)
      ctx.save()
      ctx.translate(center + cos * metrics.labelRadius, center + sin * metrics.labelRadius)
      if (cos < 0) {
        ctx.rotate(angle + Math.PI)
        ctx.textAlign = 'right'
      } else {
        ctx.rotate(angle)
        ctx.textAlign = 'left'
      }
      ctx.fillStyle = '#42424c'
      ctx.font = (i % 10 === 0 ? '700 ' : '500 ') + fontSize.toFixed(2) + 'px sans-serif'
      ctx.fillText(String(i), 0, 0)
      ctx.restore()
    }
  },

  drawRealtimePreview() {
    if (!this.previewCtx || !this.sequence) return
    const ctx = this.previewCtx
    const side = this.previewSide
    const ratio = this.previewRatio
    const center = side * 0.5
    const radius = side * BASE_RADIUS_RATIO
    const metrics = threadMetrics(radius, side, Number(this.params.lineMm), Number(this.params.circleMm))
    const displayIndex = this.data.previewAnimationRunning || this.previewAnimationFinalHeld
      ? this.previewRenderIndex
      : this.index
    const baseEnd = Math.max(0, displayIndex - 1)
    ctx.setTransform(ratio, 0, 0, ratio, 0, 0)
    ctx.clearRect(0, 0, side, side)
    ctx.fillStyle = '#f8f7fb'
    ctx.fillRect(0, 0, side, side)
    if (this.syncPreviewLayer(baseEnd, metrics, center, radius)) {
      ctx.drawImage(this.previewLayer, 0, 0, side, side)
    } else if (baseEnd > 0) {
      ctx.strokeStyle = 'rgba(18,18,18,' + metrics.alpha.toFixed(3) + ')'
      ctx.lineWidth = metrics.stroke
      ctx.lineCap = 'butt'
      ctx.lineJoin = 'miter'
      this.drawPreviewSegments(ctx, 1, baseEnd, center, radius)
    }
    if (displayIndex > 0) {
      ctx.strokeStyle = '#9769ff'
      ctx.lineWidth = metrics.stroke * 1.5
      ctx.lineCap = 'butt'
      ctx.beginPath()
      const from = this.previewPoint(this.sequence[displayIndex - 1], center, radius)
      const to = this.previewPoint(this.sequence[displayIndex], center, radius)
      ctx.moveTo(from.x, from.y)
      ctx.lineTo(to.x, to.y)
      ctx.stroke()
    }
    ctx.beginPath()
    ctx.arc(center, center, radius, 0, Math.PI * 2)
    ctx.strokeStyle = '#333'
    ctx.lineWidth = 1.25
    ctx.stroke()
    this.drawPreviewNails(ctx, center, radius)
  },

  requestPreviewDraw() {
    if (!this.previewCtx || this.previewDrawTimer) return
    this.previewDrawTimer = setTimeout(() => {
      this.previewDrawTimer = null
      this.drawRealtimePreview()
    }, 16)
  },

  restoreReaderPreviewAfterHold() {
    if (!this.previewAnimationFinalHeld) return false
    this.previewAnimationFinalHeld = false
    this.previewRenderIndex = this.index
    this.drawRealtimePreview()
    return true
  },

  openReaderPreview() {
    this.pause()
    this.previewAnimationFinalHeld = false
    this.previewRenderIndex = this.index
    this.setData({
      readerPreviewOpen: true,
      canvasSize: this.fullPreviewSize,
      previewAnimationRunning: false
    }, () => this.initializePreviewCanvas())
  },

  closeReaderPreview() {
    this.restoreReaderPreviewAfterHold()
    this.stopReaderPreviewAnimation()
    this.previewCtx = null
    this.previewCanvas = null
    this.setData({ readerPreviewOpen: false })
  },

  onPreviewAnimationSpeed(event) {
    this.restoreReaderPreviewAfterHold()
    this.setData({ previewAnimationSpeed: Number(event.detail.value) })
  },

  toggleReaderPreviewAnimation() {
    if (this.data.previewAnimationRunning) {
      this.stopReaderPreviewAnimation()
      return
    }
    this.restoreReaderPreviewAfterHold()
    this.startReaderPreviewAnimation()
  },

  startReaderPreviewAnimation() {
    this.pause()
    this.previewAnimationFinalHeld = false
    this.previewRenderIndex = 0
    this.setData({ previewAnimationRunning: true })
    this.animateReaderPreviewFrame()
  },

  animateReaderPreviewFrame() {
    if (!this.data.previewAnimationRunning) return
    const targetFrames = Math.max(24, 145 - this.data.previewAnimationSpeed * 5)
    const batch = Math.max(1, Math.ceil((this.sequence.length - 1) / targetFrames))
    this.previewRenderIndex = Math.min(this.sequence.length - 1, this.previewRenderIndex + batch)
    this.drawRealtimePreview()
    if (this.previewRenderIndex >= this.sequence.length - 1) {
      this.previewAnimationFinalHeld = true
      this.setData({ previewAnimationRunning: false })
      return
    }
    this.previewAnimationTimer = setTimeout(() => this.animateReaderPreviewFrame(), 34)
  },

  stopReaderPreviewAnimation() {
    if (this.previewAnimationTimer) clearTimeout(this.previewAnimationTimer)
    this.previewAnimationTimer = null
    if (this.data.previewAnimationRunning) {
      this.previewAnimationFinalHeld = false
      this.previewRenderIndex = this.index
      this.setData({ previewAnimationRunning: false })
      this.drawRealtimePreview()
    }
  },

  previous() {
    this.prepareManualStep()
    if (this.index > 0) {
      this.index -= 1
      this.refresh()
      this.speakCurrent()
    }
  },

  next() {
    this.prepareManualStep()
    if (this.index < this.sequence.length - 1) {
      this.index += 1
      this.refresh()
      this.speakCurrent()
    }
  },

  prepareManualStep() {
    if (this.data.previewAnimationRunning) this.stopReaderPreviewAnimation()
    this.restoreReaderPreviewAfterHold()
    this.pause()
  },

  togglePlay() {
    if (this.data.playing) this.pause()
    else this.play()
  },

  play() {
    if (this.index >= this.sequence.length - 1) {
      this.index = 0
      this.refresh()
    }
    this.setData({ playing: true })
    this.playSession = (this.playSession || 0) + 1
    this.scheduleCurrent(this.playSession)
  },

  pause() {
    if (this.timer) clearTimeout(this.timer)
    this.timer = null
    this.playSession = (this.playSession || 0) + 1
    this.manualSpeakSession = (this.manualSpeakSession || 0) + 1
    offlineTts.stop()
    if (this.data.playing || this.data.speaking) this.setData({ playing: false, speaking: false })
  },

  async scheduleCurrent(session) {
    if (!this.data.playing || session !== this.playSession) return
    if (this.data.voiceEnabled && this.data.voiceSupported) {
      this.setData({ speaking: true })
      await offlineTts.speakNumber(this.sequence[this.index], getApp().globalData.language)
      if (!this.data.playing || session !== this.playSession) return
      this.setData({ speaking: false })
    }
    this.timer = setTimeout(() => {
      if (!this.data.playing || session !== this.playSession) return
      if (this.index >= this.sequence.length - 1) {
        this.pause()
        return
      }
      this.index += 1
      this.refresh()
      this.scheduleCurrent(session)
    }, this.data.delayStep * 100)
  },

  speakCurrent() {
    if (!this.data.voiceEnabled || !this.data.voiceSupported) return
    const session = (this.manualSpeakSession || 0) + 1
    this.manualSpeakSession = session
    this.setData({ speaking: true })
    offlineTts.speakNumber(this.sequence[this.index], getApp().globalData.language)
      .then(() => {
        if (session === this.manualSpeakSession) this.setData({ speaking: false })
      })
  },

  onVoice(event) {
    const enabled = !!event.detail.value
    wx.setStorageSync('sah_reader_voice', enabled)
    if (!enabled) {
      this.manualSpeakSession = (this.manualSpeakSession || 0) + 1
      offlineTts.stop()
    }
    this.setData({ voiceEnabled: enabled, speaking: false })
  },

  onDelay(event) {
    const delayStep = Number(event.detail.value)
    this.setData({ delayStep, delayLabel: (delayStep / 10).toFixed(1) + ' s' })
    wx.setStorageSync('sah_reader_delay', delayStep)
  },

  onVoiceRate(event) {
    const voiceRateStep = Math.max(75, Math.min(160, Number(event.detail.value)))
    offlineTts.setRate(voiceRateStep / 100)
    this.setData({
      voiceRateStep,
      voiceRateLabel: (voiceRateStep / 100).toFixed(2) + '×'
    })
    wx.setStorageSync('sah_reader_voice_rate', voiceRateStep)
  },

  onJumpInput(event) {
    this.setData({ jumpValue: event.detail.value })
  },

  jump() {
    const value = String(this.data.jumpValue == null ? '' : this.data.jumpValue).trim()
    if (!value) return
    const step = Math.round(Number(value))
    if (!Number.isFinite(step)) return
    this.prepareManualStep()
    this.index = Math.max(0, Math.min(this.sequence.length - 1, step - 1))
    this.setData({ jumpValue: '' })
    this.refresh()
    this.speakCurrent()
  },

  openProjects() {
    this.pause()
    wx.navigateTo({ url: '/pages/projects/projects' })
  }
})
