const { parseTxt, sequencePreview } = require('../../utils/sequence')
const { decodeText } = require('../../utils/text-codec')
const { decodeSar, MAX_FILE_BYTES } = require('../../utils/sar')
const share = require('../../utils/share')

const MAX_TXT_BYTES = 8 * 1024 * 1024
function message(zh, en) { return getApp().globalData.language === 'en' ? en : zh }

function currentSummary(current) {
  if (!current) return null
  return {
    name: current.name,
    index: Number(current.index) || 0,
    sequenceCount: Array.isArray(current.sequence) ? current.sequence.length : 0
  }
}

Page({
  data: {
    text: {},
    current: null
  },

  onShow() {
    const app = getApp()
    this.setData({
      text: app.globalData.text,
      current: currentSummary(app.globalData.current)
    })
    share.enableShareMenu()
    wx.setNavigationBarTitle({ title: app.globalData.text.appName })
  },

  onReady() {
    wx.createSelectorQuery().in(this).select('#importThumbCanvas').fields({ node: true, size: true }).exec((result) => {
      const field = result && result[0]
      if (!field || !field.node) return
      this.thumbCanvas = field.node
      this.thumbCanvas.width = 192
      this.thumbCanvas.height = 192
      this.thumbContext = this.thumbCanvas.getContext('2d')
    })
  },

  toggleLanguage() {
    const app = getApp()
    app.setLanguage(app.globalData.language === 'en' ? 'zh' : 'en')
    this.onShow()
  },

  chooseImage() {
    const app = getApp()
    const success = (result) => {
      const file = result.tempFiles && result.tempFiles[0]
      const path = file && (file.tempFilePath || file.path)
      if (!path) return
      app.globalData.sourceImagePath = path
      app.globalData.sourceImageName = file.name || String(path).split(/[\\/]/).pop() || ('image-' + Date.now())
      app.globalData.cropImagePath = ''
      wx.navigateTo({ url: '/pages/crop/crop' })
    }
    if (typeof wx.chooseMedia === 'function') {
      wx.chooseMedia({
        count: 1,
        mediaType: ['image'],
        sourceType: ['album', 'camera'],
        success
      })
    } else {
      wx.chooseImage({
        count: 1,
        sourceType: ['album', 'camera'],
        success(result) {
          success({ tempFiles: [{ tempFilePath: result.tempFilePaths[0] }] })
        }
      })
    }
  },

  importTxt() {
    const text = this.data.text
    wx.chooseMessageFile({
      count: 1,
      type: 'file',
      extension: ['txt', 'sar', 'bin'],
      success: (result) => {
        const file = result.tempFiles && result.tempFiles[0]
        if (!file) return
        const extension = String(file.name || file.path || '').split('.').pop().toLowerCase()
        const isSar = extension === 'sar' || extension === 'bin'
        const maximum = isSar ? MAX_FILE_BYTES : MAX_TXT_BYTES
        if (Number(file.size) > maximum) {
          wx.showModal({
            title: text.error,
            content: isSar
              ? message('SAR 文件不能超过 16 MiB', 'SAR files must not exceed 16 MiB.')
              : message('TXT 文件不能超过 8 MiB', 'TXT files must not exceed 8 MiB.'),
            showCancel: false
          })
          return
        }
        wx.getFileSystemManager().readFile({
          filePath: file.path,
          success: (readResult) => {
            if (isSar) {
              try {
                const project = decodeSar(readResult.data)
                project.sourceName = project.importedFileName || file.name
                project.source = 'sar'
                getApp().globalData.pendingSarImport = project
                wx.navigateTo({ url: '/pages/projects/projects?pendingImport=1' })
              } catch (error) {
                wx.showModal({ title: text.error, content: error.message || String(error), showCancel: false })
              }
              return
            }
            let parsed
            try {
              parsed = parseTxt(decodeText(readResult.data), file.name || '')
            } catch (error) {
              wx.showModal({ title: text.error, content: error.message || String(error), showCancel: false })
              return
            }
            const sequence = parsed.sequence
            if (sequence.length < 2) {
              wx.showModal({ title: text.error, content: text.noProject, showCancel: false })
              return
            }
            if (parsed.nails > 500 || sequence.some((value) => value >= parsed.nails || value >= 500)) {
              wx.showModal({
                title: text.error,
                content: message('TXT 钉数或钉号超过小程序支持的 500 钉', 'The TXT nail count or a nail number exceeds the Mini Program limit of 500.'),
                showCancel: false
              })
              return
            }
            wx.showModal({
              title: text.confirmImport,
              content: text.recognized + ' ' + sequence.length + ' ' + text.nailNumbers +
                '\n' + parsed.nails + ' ' + text.nailUnit + ' · ' + text.circleShort + ' ' + parsed.circleMm + ' mm · ' + text.threadShort + ' ' +
                parsed.lineMm.toFixed(2) + ' mm' +
                '\n\n' + sequencePreview(sequence, 14),
              success: (modal) => {
                if (!modal.confirm) return
                this.createSequenceThumbnail(sequence, parsed.nails, (thumbnailSourcePath) => {
                  const app = getApp()
                  let current
                  try {
                    current = app.activateNewProject({
                      name: file.name || 'TXT',
                      sourceName: file.name || 'TXT',
                      source: 'txt',
                      sequence,
                      index: 0,
                      params: {
                        nails: parsed.nails,
                        circleMm: parsed.circleMm,
                        boardMm: Math.max(300, parsed.circleMm),
                        lineMm: parsed.lineMm,
                        lines: sequence.length - 1,
                        autoStop: true
                      },
                      thumbnailSourcePath
                    })
                  } catch (error) {
                    wx.showModal({ title: text.error, content: error.message || String(error), showCancel: false })
                    return
                  }
                  this.setData({ current: currentSummary(current) })
                  wx.navigateTo({ url: '/pages/reader/reader' })
                })
              }
            })
          },
          fail: (error) => wx.showModal({
            title: text.error,
            content: error.errMsg || String(error),
            showCancel: false
          })
        })
      }
    })
  },

  createSequenceThumbnail(sequence, nails, callback) {
    if (!this.thumbCanvas || !this.thumbContext) {
      callback('')
      return
    }
    const ctx = this.thumbContext
    const side = 192
    const center = side / 2
    const radius = side * 0.46
    ctx.fillStyle = '#fff'
    ctx.fillRect(0, 0, side, side)
    ctx.strokeStyle = 'rgba(0,0,0,.10)'
    ctx.lineWidth = 0.45
    let start = 1
    const drawChunk = () => {
      const end = Math.min(sequence.length, start + 1600)
      ctx.beginPath()
      for (let index = start; index < end; index += 1) {
        const fromAngle = Math.PI * 2 * sequence[index - 1] / nails
        const toAngle = Math.PI * 2 * sequence[index] / nails
        ctx.moveTo(center + Math.cos(fromAngle) * radius, center + Math.sin(fromAngle) * radius)
        ctx.lineTo(center + Math.cos(toAngle) * radius, center + Math.sin(toAngle) * radius)
      }
      ctx.stroke()
      start = end
      if (start < sequence.length) {
        setTimeout(drawChunk, 0)
        return
      }
      wx.canvasToTempFilePath({
        canvas: this.thumbCanvas,
        width: side,
        height: side,
        destWidth: side,
        destHeight: side,
        fileType: 'png',
        success: (result) => callback(result.tempFilePath),
        fail: () => callback('')
      }, this)
    }
    drawChunk()
  },

  continueCurrent() {
    wx.navigateTo({ url: '/pages/reader/reader' })
  },

  openReader() {
    if (this.data.current) wx.navigateTo({ url: '/pages/reader/reader' })
  },

  openProjects() {
    wx.navigateTo({ url: '/pages/projects/projects' })
  },

  openAbout() {
    wx.navigateTo({ url: '/pages/about/about' })
  },

  onShareAppMessage() { return share.appMessage() },
  onShareTimeline() { return share.timeline() }
})
