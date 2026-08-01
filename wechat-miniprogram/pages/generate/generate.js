const TARGET_SIZE = 256

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value))
}

function derivedBoardMm(circleMm) {
  return Math.min(1200, Math.max(300, circleMm + 40))
}

Page({
  data: {
    text: {},
    cropImagePath: '',
    nails: 200,
    lines: 3000,
    circleMm: 260,
    lineMm: '0.10',
    lineSlider: 10,
    autoStop: true,
    generating: false,
    progress: 0,
    complete: 0
  },

  onLoad() {
    const app = getApp()
    if (!app.globalData.cropImagePath) {
      wx.navigateBack()
      return
    }
    const saved = wx.getStorageSync('sah_generator_settings') || {}
    this.setData({
      text: app.globalData.text,
      cropImagePath: app.globalData.cropImagePath,
      nails: clamp(Number(saved.nails) || 200, 100, 500),
      lines: clamp(Number(saved.lines) || 3000, 1000, 20000),
      circleMm: clamp(Number(saved.circleMm) || 260, 80, 1200),
      lineMm: clamp(Number(saved.lineMm) || 0.1, 0.01, 1).toFixed(2),
      lineSlider: Math.round(clamp(Number(saved.lineMm) || 0.1, 0.01, 1) * 100),
      autoStop: saved.autoStop !== false
    })
    wx.setNavigationBarTitle({ title: app.globalData.text.generatorTitle })
  },

  onReady() {
    wx.createSelectorQuery()
      .in(this)
      .select('#targetCanvas')
      .fields({ node: true, size: true })
      .exec((result) => {
        const field = result && result[0]
        if (!field || !field.node) return
        this.targetCanvas = field.node
        this.targetCanvas.width = TARGET_SIZE
        this.targetCanvas.height = TARGET_SIZE
        this.targetContext = this.targetCanvas.getContext('2d')
      })
  },

  onUnload() {
    this.terminateWorker()
  },

  onNails(event) {
    this.setData({ nails: Number(event.detail.value) })
  },

  onLines(event) {
    this.setData({ lines: Number(event.detail.value) })
  },

  onInput(event) {
    this.setData({ [event.currentTarget.dataset.field]: event.detail.value })
  },

  onLineInput(event) {
    const value = event.detail.value
    const number = Number(value)
    const update = { lineMm: value }
    if (Number.isFinite(number)) update.lineSlider = Math.round(clamp(number, 0.01, 1) * 100)
    this.setData(update)
  },

  onLineSlider(event) {
    const slider = Math.max(1, Math.min(100, Number(event.detail.value)))
    this.setData({
      lineSlider: slider,
      lineMm: (slider / 100).toFixed(2)
    })
  },

  onAutoStop(event) {
    this.setData({ autoStop: !!event.detail.value })
  },

  onSizeBlur() {
    const circle = clamp(Math.round(Number(this.data.circleMm) || 260), 80, 1200)
    this.setData({ circleMm: circle })
  },

  onLineBlur() {
    const line = clamp(Number(this.data.lineMm) || 0.1, 0.01, 1)
    this.setData({
      lineMm: line.toFixed(2),
      lineSlider: Math.round(line * 100)
    })
  },

  normalizeValues() {
    const circle = clamp(Math.round(Number(this.data.circleMm) || 260), 80, 1200)
    const line = clamp(Number(this.data.lineMm) || 0.1, 0.01, 1)
    const values = {
      nails: clamp(Math.round(Number(this.data.nails)), 100, 500),
      lines: clamp(Math.round(Number(this.data.lines)), 1000, 20000),
      circleMm: circle,
      boardMm: derivedBoardMm(circle),
      lineMm: Number(line.toFixed(2)),
      autoStop: !!this.data.autoStop
    }
    this.setData(Object.assign({}, values, {
      lineMm: values.lineMm.toFixed(2),
      lineSlider: Math.round(values.lineMm * 100)
    }))
    wx.setStorageSync('sah_generator_settings', {
      nails: values.nails,
      lines: values.lines,
      circleMm: values.circleMm,
      lineMm: values.lineMm,
      autoStop: values.autoStop
    })
    return values
  },

  startGeneration() {
    if (this.data.generating || !this.targetCanvas || !this.targetContext) return
    if (typeof wx.createWorker !== 'function') {
      wx.showModal({
        title: this.data.text.error,
        content: this.data.text.workerUnsupported,
        showCancel: false
      })
      return
    }
    const values = this.normalizeValues()
    this.setData({ generating: true, progress: 0, complete: 0 })
    const image = this.targetCanvas.createImage()
    image.onload = () => {
      try {
        const context = this.targetContext
        context.clearRect(0, 0, TARGET_SIZE, TARGET_SIZE)
        context.fillStyle = '#fff'
        context.fillRect(0, 0, TARGET_SIZE, TARGET_SIZE)
        context.drawImage(image, 0, 0, TARGET_SIZE, TARGET_SIZE)
        const imageData = context.getImageData(0, 0, TARGET_SIZE, TARGET_SIZE)
        this.launchWorker(Array.prototype.slice.call(imageData.data), values)
      } catch (error) {
        this.generationError(error)
      }
    }
    image.onerror = () => this.generationError(new Error(this.data.text.imageLoadFailed))
    image.src = this.data.cropImagePath
  },

  launchWorker(pixels, values) {
    try {
      this.worker = wx.createWorker('workers/generator.js')
      this.worker.onMessage((message) => {
        if (!message) return
        if (message.type === 'progress') {
          const now = Date.now()
          if (message.complete < message.total && now - (this.lastProgressPaint || 0) < 90) return
          this.lastProgressPaint = now
          this.setData({
            complete: message.complete,
            progress: Math.min(100, Math.round(message.complete / message.total * 100))
          })
        } else if (message.type === 'result') {
          this.completeGeneration(message.result, values)
        } else if (message.type === 'error') {
          this.generationError(new Error(message.message))
        }
      })
      if (typeof this.worker.onError === 'function') {
        this.worker.onError((error) => this.generationError(error))
      }
      this.worker.postMessage({
        type: 'generate',
        options: {
          pixels,
          size: TARGET_SIZE,
          pinCount: values.nails,
          requestedLines: values.lines,
          circleMm: values.circleMm,
          lineMm: values.lineMm,
          autoStop: values.autoStop
        }
      })
    } catch (error) {
      this.generationError(error)
    }
  },

  completeGeneration(result, values) {
    this.terminateWorker()
    if (!result || !result.sequence || result.sequence.length < 2) {
      this.generationError(new Error(this.data.text.generationFailed))
      return
    }
    this.prepareThumbnailPixels()
    wx.canvasToTempFilePath({
      canvas: this.targetCanvas,
      x: 0,
      y: 0,
      width: TARGET_SIZE,
      height: TARGET_SIZE,
      destWidth: 192,
      destHeight: 192,
      fileType: 'png',
      success: (thumbnail) => this.finishGeneration(result, values, thumbnail.tempFilePath),
      fail: () => this.finishGeneration(result, values, '')
    }, this)
  },

  prepareThumbnailPixels() {
    const context = this.targetContext
    const imageData = context.getImageData(0, 0, TARGET_SIZE, TARGET_SIZE)
    const pixels = imageData.data
    const center = (TARGET_SIZE - 1) * 0.5
    const radiusSquared = (center - 2) * (center - 2)
    for (let y = 0; y < TARGET_SIZE; y += 1) {
      for (let x = 0; x < TARGET_SIZE; x += 1) {
        const offset = (y * TARGET_SIZE + x) * 4
        const dx = x - center
        const dy = y - center
        const value = dx * dx + dy * dy <= radiusSquared && pixels[offset] < 200 ? 0 : 255
        pixels[offset] = value
        pixels[offset + 1] = value
        pixels[offset + 2] = value
        pixels[offset + 3] = 255
      }
    }
    context.putImageData(imageData, 0, 0)
  },

  finishGeneration(result, values, thumbnailSourcePath) {
    const app = getApp()
    const generation = {
      sequence: result.sequence,
      params: values,
      threadMeters: result.threadMeters,
      scoreStride: result.scoreStride,
      cropImagePath: app.globalData.cropImagePath,
      name: this.data.text.generatedProject + ' · ' + values.nails
    }
    app.globalData.generation = generation
    try {
      app.activateNewProject({
        name: generation.name,
        sourceName: app.globalData.sourceImageName || generation.name,
        source: 'generator',
        sequence: result.sequence,
        index: 0,
        params: values,
        threadMeters: result.threadMeters,
        cropImagePath: app.globalData.cropImagePath,
        thumbnailSourcePath
      })
    } catch (error) {
      this.generationError(error)
      return
    }
    this.setData({
      generating: false,
      complete: result.sequence.length - 1,
      progress: 100
    })
    wx.redirectTo({ url: '/pages/preview/preview' })
  },

  generationError(error) {
    this.terminateWorker()
    this.setData({ generating: false })
    wx.showModal({
      title: this.data.text.error,
      content: (error && (error.message || error.errMsg)) || this.data.text.generationFailed,
      showCancel: false
    })
  },

  cancelGeneration() {
    this.terminateWorker()
    this.setData({ generating: false, progress: 0, complete: 0 })
  },

  terminateWorker() {
    if (!this.worker) return
    try { this.worker.terminate() } catch (error) {}
    this.worker = null
  }
})
