const TARGET_RATIO = 251 / 255
const FIT_MARGIN = 0.98
const MAX_ZOOM = 4
const cropSnap = require('../../utils/crop-snap')

function distance(touches) {
  if (!touches || touches.length < 2) return 0
  const dx = touches[0].x - touches[1].x
  const dy = touches[0].y - touches[1].y
  return Math.sqrt(dx * dx + dy * dy)
}

Page({
  data: {
    text: {},
    canvasSize: 320,
    zoomSliderMin: 1,
    zoomSlider: 100,
    zoomLabel: '1.00×',
    exporting: false
  },

  onLoad() {
    const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    const size = Math.max(220, Math.min(info.windowWidth, info.windowHeight - 205))
    this.setData({
      text: getApp().globalData.text,
      canvasSize: Math.floor(size)
    })
    wx.setNavigationBarTitle({ title: getApp().globalData.text.cropTitle })
    this.centerX = 0.5
    this.centerY = 0.5
    this.zoom = 1
    this.gestureZoom = 1
    this.snapController = cropSnap.createSnapController()
    this.pinching = false
  },

  onReady() {
    wx.createSelectorQuery()
      .in(this)
      .select('#cropCanvas')
      .fields({ node: true, size: true })
      .exec((result) => {
        const field = result && result[0]
        if (!field || !field.node) return
        this.canvas = field.node
        this.ctx = this.canvas.getContext('2d')
        const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
        this.pixelRatio = Math.min(3, info.pixelRatio || 1)
        this.canvas.width = Math.round(field.width * this.pixelRatio)
        this.canvas.height = Math.round(field.height * this.pixelRatio)
        this.cssSize = field.width
        this.loadImage()
      })
  },

  loadImage() {
    const path = getApp().globalData.sourceImagePath
    if (!path || !this.canvas) {
      wx.navigateBack()
      return
    }
    const image = this.canvas.createImage()
    image.onload = () => {
      this.image = image
      this.imageWidth = image.width
      this.imageHeight = image.height
      this.minimumZoom = Math.min(
        1,
        Math.min(image.width, image.height) * TARGET_RATIO * FIT_MARGIN /
          Math.sqrt(image.width * image.width + image.height * image.height)
      )
      this.zoom = this.minimumZoom
      this.gestureZoom = this.zoom
      this.centerX = 0.5
      this.centerY = 0.5
      this.updateZoomData()
      this.draw(true)
    }
    image.onerror = () => wx.showModal({
      title: this.data.text.error,
      content: this.data.text.imageLoadFailed,
      showCancel: false,
      success: () => wx.navigateBack()
    })
    image.src = path
  },

  cropSize() {
    return Math.min(this.imageWidth, this.imageHeight) / this.zoom
  },

  clampCenter() {
    const crop = this.cropSize()
    if (crop >= this.imageWidth) this.centerX = 0.5
    else {
      const half = crop / this.imageWidth * 0.5
      this.centerX = Math.max(half, Math.min(1 - half, this.centerX))
    }
    if (crop >= this.imageHeight) this.centerY = 0.5
    else {
      const half = crop / this.imageHeight * 0.5
      this.centerY = Math.max(half, Math.min(1 - half, this.centerY))
    }
  },

  drawPhoto() {
    const ctx = this.ctx
    const side = this.cssSize
    const crop = this.cropSize()
    const left = this.centerX * this.imageWidth - crop * 0.5
    const top = this.centerY * this.imageHeight - crop * 0.5
    ctx.fillStyle = '#ffffff'
    ctx.fillRect(0, 0, side, side)
    ctx.filter = 'grayscale(100%)'
    ctx.drawImage(
      this.image,
      -left / crop * side,
      -top / crop * side,
      this.imageWidth / crop * side,
      this.imageHeight / crop * side
    )
    ctx.filter = 'none'
  },

  applyGrayscalePixels() {
    const width = this.canvas.width
    const height = this.canvas.height
    const imageData = this.ctx.getImageData(0, 0, width, height)
    const pixels = imageData.data
    for (let index = 0; index < pixels.length; index += 4) {
      const gray = Math.round(
        pixels[index] * 0.299 +
        pixels[index + 1] * 0.587 +
        pixels[index + 2] * 0.114
      )
      pixels[index] = gray
      pixels[index + 1] = gray
      pixels[index + 2] = gray
    }
    this.ctx.putImageData(imageData, 0, 0)
  },

  draw(withOverlay) {
    if (!this.ctx || !this.image) return
    const ctx = this.ctx
    const side = this.cssSize
    const ratio = this.pixelRatio
    ctx.setTransform(ratio, 0, 0, ratio, 0, 0)
    ctx.clearRect(0, 0, side, side)
    this.drawPhoto()
    if (withOverlay) {
      ctx.fillStyle = 'rgba(0,0,0,.60)'
      ctx.fillRect(0, 0, side, side)
      ctx.save()
      ctx.beginPath()
      ctx.arc(side * 0.5, side * 0.5, side * 0.5 * TARGET_RATIO, 0, Math.PI * 2)
      ctx.clip()
      this.drawPhoto()
      ctx.restore()
      ctx.beginPath()
      ctx.arc(side * 0.5, side * 0.5, side * 0.5 * TARGET_RATIO, 0, Math.PI * 2)
      ctx.strokeStyle = '#ffffff'
      ctx.lineWidth = 2
      ctx.stroke()
      ctx.fillStyle = 'rgba(255,255,255,.86)'
      ctx.font = '16px sans-serif'
      ctx.textAlign = 'center'
      ctx.fillText(this.data.text.cropHint, side * 0.5, side * (0.5 + TARGET_RATIO * 0.5) - 14)
    }
  },

  updateZoomData() {
    const sliderMin = Math.max(1, Math.round(this.minimumZoom * 100))
    const slider = Math.max(sliderMin, Math.min(400, Math.round(this.zoom * 100)))
    this.setData({
      zoomSliderMin: sliderMin,
      zoomSlider: slider,
      zoomLabel: this.zoom.toFixed(2) + '×'
    })
  },

  onZoomSlider(event) {
    this.zoom = Math.max(this.minimumZoom, Math.min(MAX_ZOOM, Number(event.detail.value) / 100))
    this.gestureZoom = this.zoom
    cropSnap.beginPinch(this.snapController)
    this.clampCenter()
    this.updateZoomData()
    this.requestDraw()
  },

  resetCrop() {
    this.centerX = 0.5
    this.centerY = 0.5
    this.zoom = Math.max(1, this.minimumZoom)
    this.gestureZoom = this.zoom
    cropSnap.beginPinch(this.snapController)
    this.clampCenter()
    this.updateZoomData()
    this.draw(true)
  },

  fitWhole() {
    this.centerX = 0.5
    this.centerY = 0.5
    this.zoom = this.minimumZoom
    this.gestureZoom = this.zoom
    cropSnap.beginPinch(this.snapController)
    this.updateZoomData()
    this.draw(true)
  },

  onTouchStart(event) {
    const touches = event.touches || []
    if (touches.length >= 2) {
      this.pinching = true
      this.lastDistance = distance(touches)
      this.gestureZoom = this.zoom
      cropSnap.beginPinch(this.snapController)
      return
    }
    if (touches.length === 1) {
      this.pinching = false
      this.lastX = touches[0].x
      this.lastY = touches[0].y
    }
  },

  onTouchMove(event) {
    if (!this.image) return
    const touches = event.touches || []
    if (this.pinching && touches.length >= 2) {
      const nextDistance = distance(touches)
      if (this.lastDistance > 0) {
        this.gestureZoom = Math.max(
          this.minimumZoom,
          Math.min(MAX_ZOOM, this.gestureZoom * nextDistance / this.lastDistance)
        )
        this.zoom = cropSnap.applySnap(this.snapController, {
          gestureZoom: this.gestureZoom,
          minimumZoom: this.minimumZoom,
          maximumZoom: MAX_ZOOM,
          imageWidth: this.imageWidth,
          imageHeight: this.imageHeight,
          canvasSide: this.cssSize,
          now: Date.now()
        })
      }
      this.lastDistance = nextDistance
    } else if (touches.length === 1) {
      const crop = this.cropSize()
      this.centerX -= (touches[0].x - this.lastX) / this.cssSize * crop / this.imageWidth
      this.centerY -= (touches[0].y - this.lastY) / this.cssSize * crop / this.imageHeight
      this.lastX = touches[0].x
      this.lastY = touches[0].y
    }
    this.clampCenter()
    this.updateZoomData()
    this.requestDraw()
  },

  onTouchEnd(event) {
    if (!event.touches || event.touches.length < 2) {
      this.pinching = false
      this.gestureZoom = this.zoom
    }
    if (this.drawTimer) {
      clearTimeout(this.drawTimer)
      this.drawTimer = null
      this.draw(true)
    }
  },

  requestDraw() {
    if (this.drawTimer) return
    this.drawTimer = setTimeout(() => {
      this.drawTimer = null
      this.draw(true)
    }, 16)
  },

  confirmCrop() {
    if (!this.canvas || !this.image || this.data.exporting) return
    this.setData({ exporting: true })
    this.draw(false)
    this.applyGrayscalePixels()
    wx.canvasToTempFilePath({
      canvas: this.canvas,
      x: 0,
      y: 0,
      width: this.cssSize,
      height: this.cssSize,
      destWidth: 256,
      destHeight: 256,
      fileType: 'png',
      success: (result) => {
        const app = getApp()
        app.globalData.cropImagePath = result.tempFilePath
        app.globalData.crop = {
          centerX: this.centerX,
          centerY: this.centerY,
          zoom: this.zoom
        }
        wx.navigateTo({ url: '/pages/generate/generate' })
      },
      fail: (error) => wx.showModal({
        title: this.data.text.error,
        content: error.errMsg || String(error),
        showCancel: false
      }),
      complete: () => {
        this.setData({ exporting: false })
        this.draw(true)
      }
    }, this)
  }
})
