const { toTxt, txtFilename } = require('../../utils/sequence')
const { writeUserFile, shareFile } = require('../../utils/files')
const { generateNailTemplate, pdfFilename } = require('../../utils/pdf')
const {
  BASE_RADIUS_RATIO,
  clampZoom,
  maxPan,
  threadMetrics,
  nailMetrics
} = require('../../utils/preview-metrics')

const THREAD_PATH_BATCH = 32

function touchDistance(touches) {
  if (!touches || touches.length < 2) return 0
  const dx = touches[0].x - touches[1].x
  const dy = touches[0].y - touches[1].y
  return Math.sqrt(dx * dx + dy * dy)
}

Page({
  data: {
    text: {},
    canvasSize: 320,
    params: {},
    sequenceCount: 0,
    threadMeters: '0.00',
    actualRatio: true,
    displayLineMm: '0.10',
    lineSlider: 10,
    zoomSlider: 100,
    zoomLabel: '1.00×',
    animationSpeed: 5,
    animationRunning: false
  },

  onLoad() {
    const app = getApp()
    const generation = app.globalData.generation
    const current = app.globalData.current
    if (!current || !current.sequence || current.sequence.length < 2) {
      wx.navigateBack()
      return
    }
    const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    const size = Math.max(260, Math.min(info.windowWidth - 28, info.windowHeight * 0.56))
    this.sequence = generation && generation.sequence ? generation.sequence : current.sequence
    this.params = Object.assign({
      nails: Math.max.apply(null, this.sequence) + 1,
      circleMm: 260,
      boardMm: 300,
      lineMm: 0.1
    }, current.params || {})
    this.zoom = 1
    this.panX = 0
    this.panY = 0
    this.renderIndex = this.sequence.length - 1
    const savedPreview = wx.getStorageSync('sah_preview_settings') || {}
    this.customLineMm = Math.max(0.01, Math.min(1,
      Number(savedPreview.customLineMm) || Number(this.params.lineMm)))
    const actualRatio = savedPreview.actualRatio !== false
    this.setData({
      text: app.globalData.text,
      canvasSize: Math.floor(size),
      params: this.params,
      sequenceCount: this.sequence.length,
      threadMeters: Number(current.threadMeters || 0).toFixed(2),
      actualRatio,
      displayLineMm: (actualRatio ? Number(this.params.lineMm) : this.customLineMm).toFixed(2),
      lineSlider: Math.round(this.customLineMm * 100)
    })
    wx.setNavigationBarTitle({ title: app.globalData.text.previewTitle })
  },

  onReady() {
    wx.createSelectorQuery()
      .in(this)
      .select('#previewCanvas')
      .fields({ node: true, size: true })
      .exec((result) => {
        const field = result && result[0]
        if (!field || !field.node) return
        this.canvas = field.node
        this.ctx = this.canvas.getContext('2d')
        const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
        this.pixelRatio = Math.min(3, info.pixelRatio || 1)
        this.cssSize = field.width
        this.canvas.width = Math.round(field.width * this.pixelRatio)
        this.canvas.height = Math.round(field.height * this.pixelRatio)
        this.draw()
      })
  },

  onUnload() {
    this.stopAnimation()
    if (this.drawTimer) clearTimeout(this.drawTimer)
    this.drawTimer = null
  },

  pinPoint(pin, centerX, centerY, radius) {
    const angle = Math.PI * 2 * pin / this.params.nails
    return {
      x: centerX + Math.cos(angle) * radius,
      y: centerY + Math.sin(angle) * radius
    }
  },

  draw() {
    if (!this.ctx || !this.sequence) return
    const ctx = this.ctx
    const side = this.cssSize
    const ratio = this.pixelRatio
    const centerX = side * 0.5 + this.panX
    const centerY = side * 0.5 + this.panY
    const radius = side * BASE_RADIUS_RATIO * this.zoom
    const lineMm = this.data.actualRatio ? Number(this.params.lineMm) : this.customLineMm
    const thread = threadMetrics(radius, side, lineMm, this.params.circleMm)
    ctx.setTransform(ratio, 0, 0, ratio, 0, 0)
    ctx.clearRect(0, 0, side, side)
    ctx.fillStyle = '#f8f7fb'
    ctx.fillRect(0, 0, side, side)
    if (!this.drawCachedThreads(ctx, centerX, centerY)) {
      ctx.strokeStyle = 'rgba(18,18,18,' + thread.alpha.toFixed(3) + ')'
      ctx.lineWidth = thread.stroke
      ctx.lineCap = 'butt'
      ctx.lineJoin = 'miter'
      this.drawThreadSegments(ctx, centerX, centerY, radius)
    }
    ctx.beginPath()
    ctx.arc(centerX, centerY, radius, 0, Math.PI * 2)
    ctx.strokeStyle = '#333'
    ctx.lineWidth = 1.5
    ctx.stroke()
    this.drawNails(ctx, centerX, centerY, radius)
  },

  drawThreadSegments(ctx, centerX, centerY, radius) {
    const end = Math.min(this.renderIndex, this.sequence.length - 1)
    for (let first = 1; first <= end; first += THREAD_PATH_BATCH) {
      const last = Math.min(end, first + THREAD_PATH_BATCH - 1)
      ctx.beginPath()
      for (let i = first; i <= last; i += 1) {
        const from = this.pinPoint(this.sequence[i - 1], centerX, centerY, radius)
        const to = this.pinPoint(this.sequence[i], centerX, centerY, radius)
        ctx.moveTo(from.x, from.y)
        ctx.lineTo(to.x, to.y)
      }
      ctx.stroke()
    }
  },

  invalidateThreadLayer() {
    this.threadLayerKey = ''
  },

  drawCachedThreads(ctx, centerX, centerY) {
    if (this.data.animationRunning || this.renderIndex !== this.sequence.length - 1 ||
        typeof wx.createOffscreenCanvas !== 'function') return false
    const lineMm = this.data.actualRatio ? Number(this.params.lineMm) : this.customLineMm
    const key = this.sequence.length + ':' + this.params.nails + ':' + this.params.circleMm + ':' + lineMm.toFixed(4)
    if (!this.threadLayer || this.threadLayerKey !== key) {
      try {
        const size = 768
        const canvas = wx.createOffscreenCanvas({ type: '2d', width: size, height: size })
        const layer = canvas.getContext('2d')
        const center = size / 2
        const radius = size * BASE_RADIUS_RATIO
        const metrics = threadMetrics(radius, size, lineMm, this.params.circleMm)
        layer.clearRect(0, 0, size, size)
        layer.strokeStyle = 'rgba(18,18,18,' + metrics.alpha.toFixed(3) + ')'
        layer.lineWidth = metrics.stroke
        layer.lineCap = 'butt'
        layer.lineJoin = 'miter'
        for (let first = 1; first < this.sequence.length; first += THREAD_PATH_BATCH) {
          const last = Math.min(this.sequence.length - 1, first + THREAD_PATH_BATCH - 1)
          layer.beginPath()
          for (let i = first; i <= last; i += 1) {
            const a = Math.PI * 2 * this.sequence[i - 1] / this.params.nails
            const b = Math.PI * 2 * this.sequence[i] / this.params.nails
            layer.moveTo(center + Math.cos(a) * radius, center + Math.sin(a) * radius)
            layer.lineTo(center + Math.cos(b) * radius, center + Math.sin(b) * radius)
          }
          layer.stroke()
        }
        this.threadLayer = canvas
        this.threadLayerKey = key
      } catch (error) {
        this.threadLayer = null
        this.threadLayerKey = ''
        return false
      }
    }
    const displaySide = this.cssSize * this.zoom
    ctx.drawImage(this.threadLayer, centerX - displaySide / 2, centerY - displaySide / 2, displaySide, displaySide)
    return true
  },

  drawNails(ctx, centerX, centerY, radius) {
    const count = this.params.nails
    const metrics = nailMetrics(count, radius)
    const base = metrics.baseText
    const dotRadius = metrics.dotRadius
    const labelRadius = metrics.labelRadius
    ctx.textBaseline = 'middle'
    for (let i = 0; i < count; i += 1) {
      const angle = Math.PI * 2 * i / count
      const cos = Math.cos(angle)
      const sin = Math.sin(angle)
      ctx.beginPath()
      ctx.arc(centerX + cos * radius, centerY + sin * radius, dotRadius, 0, Math.PI * 2)
      ctx.fillStyle = '#55555f'
      ctx.fill()
      const size = i % 10 === 0 ? base * 2 : (i % 5 === 0 ? base * 1.5 : base)
      ctx.save()
      ctx.translate(centerX + cos * labelRadius, centerY + sin * labelRadius)
      if (cos < 0) {
        ctx.rotate(angle + Math.PI)
        ctx.textAlign = 'right'
      } else {
        ctx.rotate(angle)
        ctx.textAlign = 'left'
      }
      ctx.fillStyle = '#42424c'
      ctx.font = (i % 10 === 0 ? '700 ' : '500 ') + size.toFixed(2) + 'px sans-serif'
      ctx.fillText(String(i), 0, 0)
      ctx.restore()
    }
  },

  onActualRatio(event) {
    this.setData({
      actualRatio: !!event.detail.value,
      displayLineMm: (event.detail.value ? Number(this.params.lineMm) : this.customLineMm).toFixed(2)
    })
    this.persistPreviewSettings()
    this.invalidateThreadLayer()
    this.requestDraw()
  },

  onLineSlider(event) {
    this.customLineMm = Math.max(0.01, Math.min(1, Number(event.detail.value) / 100))
    this.setData({
      actualRatio: false,
      lineSlider: Number(event.detail.value),
      displayLineMm: this.customLineMm.toFixed(2)
    })
    this.persistPreviewSettings()
    this.invalidateThreadLayer()
    this.requestDraw()
  },

  persistPreviewSettings() {
    wx.setStorageSync('sah_preview_settings', {
      actualRatio: this.data.actualRatio,
      customLineMm: this.customLineMm
    })
  },

  onZoomSlider(event) {
    this.zoom = clampZoom(Number(event.detail.value) / 100)
    this.clampPan()
    this.setData({
      zoomSlider: Number(event.detail.value),
      zoomLabel: this.zoom.toFixed(2) + '×'
    })
    this.requestDraw()
  },

  onSpeed(event) {
    this.setData({ animationSpeed: Number(event.detail.value) })
  },

  onTouchStart(event) {
    if (this.data.animationRunning) return
    const touches = event.touches || []
    if (touches.length >= 2) {
      this.pinching = true
      this.lastDistance = touchDistance(touches)
    } else if (touches.length === 1) {
      this.pinching = false
      this.lastX = touches[0].x
      this.lastY = touches[0].y
    }
  },

  onTouchMove(event) {
    if (this.data.animationRunning) return
    const touches = event.touches || []
    if (this.pinching && touches.length >= 2) {
      const distance = touchDistance(touches)
      if (this.lastDistance > 0) {
        this.zoom = clampZoom(this.zoom * distance / this.lastDistance)
        this.lastDistance = distance
        const now = Date.now()
        if (now - (this.lastZoomLabelPaint || 0) >= 70) {
          this.lastZoomLabelPaint = now
          this.setData({ zoomSlider: Math.round(this.zoom * 100), zoomLabel: this.zoom.toFixed(2) + '×' })
        }
      }
    } else if (touches.length === 1) {
      this.panX += touches[0].x - this.lastX
      this.panY += touches[0].y - this.lastY
      this.lastX = touches[0].x
      this.lastY = touches[0].y
    }
    this.clampPan()
    this.requestDraw()
  },

  onTouchEnd(event) {
    if (!event.touches || event.touches.length < 2) this.pinching = false
  },

  clampPan() {
    const limit = maxPan(this.cssSize, this.zoom, 12)
    this.panX = Math.max(-limit, Math.min(limit, this.panX))
    this.panY = Math.max(-limit, Math.min(limit, this.panY))
  },

  requestDraw() {
    if (this.drawTimer) return
    this.drawTimer = setTimeout(() => {
      this.drawTimer = null
      this.draw()
    }, 16)
  },

  toggleAnimation() {
    if (this.data.animationRunning) this.stopAnimation()
    else this.startAnimation()
  },

  startAnimation() {
    this.renderIndex = 0
    this.setData({ animationRunning: true })
    this.animateFrame()
  },

  animateFrame() {
    if (!this.data.animationRunning) return
    const targetFrames = Math.max(24, 145 - this.data.animationSpeed * 5)
    const batch = Math.max(1, Math.ceil((this.sequence.length - 1) / targetFrames))
    this.renderIndex = Math.min(this.sequence.length - 1, this.renderIndex + batch)
    this.draw()
    if (this.renderIndex >= this.sequence.length - 1) {
      this.setData({ animationRunning: false })
      return
    }
    this.animationTimer = setTimeout(() => this.animateFrame(), 34)
  },

  stopAnimation() {
    if (this.animationTimer) clearTimeout(this.animationTimer)
    this.animationTimer = null
    if (this.data.animationRunning) {
      this.renderIndex = this.sequence.length - 1
      this.setData({ animationRunning: false })
      this.draw()
    }
  },

  openReader() {
    wx.navigateTo({ url: '/pages/reader/reader' })
  },

  saveProject() {
    wx.navigateTo({ url: '/pages/projects/projects?save=1' })
  },

  exportTxt() {
    try {
      const current = getApp().globalData.current
      const filename = txtFilename(
        current.sourceName || current.name,
        this.params,
        getApp().globalData.language
      )
      const path = writeUserFile(filename, toTxt(this.sequence, this.params), 'utf8')
      shareFile(path, filename, this.data.text)
    } catch (error) {
      wx.showModal({ title: this.data.text.error, content: error.message || String(error), showCancel: false })
    }
  },

  exportPdf() {
    try {
      const filename = pdfFilename(this.params, getApp().globalData.language)
      const bytes = generateNailTemplate({
        nails: this.params.nails,
        circleMm: this.params.circleMm
      })
      const path = writeUserFile(filename, bytes.buffer)
      shareFile(path, filename, this.data.text)
    } catch (error) {
      wx.showModal({ title: this.data.text.error, content: error.message || String(error), showCancel: false })
    }
  }
})
