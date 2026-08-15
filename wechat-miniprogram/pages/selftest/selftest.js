const sequence = require('../../utils/sequence')
const pdf = require('../../utils/pdf')
const offlineTts = require('../../utils/offline-tts')
const previewMetrics = require('../../utils/preview-metrics')

Page({
  data: {
    checks: [],
    done: false,
    passed: false,
    summary: '正在检查…'
  },

  onReady() {
    this.runChecks()
  },

  addCheck(name, ok, detail) {
    const checks = this.data.checks.concat([{
      name,
      ok,
      status: ok ? 'PASS' : ('FAIL' + (detail ? ': ' + detail : ''))
    }])
    this.setData({ checks })
  },

  async runChecks() {
    try {
      const values = sequence.parseSequence('0, 10, 25, 3')
      this.addCheck('TXT 序列解析', values.join(',') === '0,10,25,3')
    } catch (error) {
      this.addCheck('TXT 序列解析', false, error.message)
    }

    try {
      const bytes = pdf.generateNailTemplate({ nails: 100, circleMm: 260 })
      const header = String.fromCharCode.apply(null, Array.from(bytes.slice(0, 8)))
      this.addCheck('PDF 模板生成', bytes.length > 1000 && header.indexOf('%PDF-1.4') === 0)
    } catch (error) {
      this.addCheck('PDF 模板生成', false, error.message)
    }

    try {
      const key = 'sah_runtime_selftest'
      wx.setStorageSync(key, { ok: true, value: 44 })
      const stored = wx.getStorageSync(key)
      wx.removeStorageSync(key)
      this.addCheck('本地存储', stored && stored.ok && stored.value === 44)
    } catch (error) {
      this.addCheck('本地存储', false, error.message)
    }

    await this.testCanvas()
    await this.testWorker()
    this.testPreviewMetrics()
    await this.testOfflineAudio()

    const passed = this.data.checks.length === 7 && this.data.checks.every((item) => item.ok)
    this.setData({
      done: true,
      passed,
      summary: passed ? 'SELFTEST PASS · 7/7' : 'SELFTEST FAIL · 请检查失败项'
    })
  },

  testCanvas() {
    return new Promise((resolve) => {
      wx.createSelectorQuery()
        .select('#selftestCanvas')
        .fields({ node: true, size: true })
        .exec((result) => {
          try {
            const canvas = result && result[0] && result[0].node
            if (!canvas) throw new Error('canvas node unavailable')
            canvas.width = 64
            canvas.height = 64
            const context = canvas.getContext('2d')
            context.fillStyle = '#ffffff'
            context.fillRect(0, 0, 64, 64)
            context.fillStyle = '#000000'
            context.fillRect(16, 16, 32, 32)
            const data = context.getImageData(0, 0, 64, 64).data
            const center = (32 * 64 + 32) * 4
            this.addCheck('Canvas 2D / 像素读取', data.length === 16384 && data[center] === 0)
          } catch (error) {
            this.addCheck('Canvas 2D / 像素读取', false, error.message)
          }
          resolve()
        })
    })
  },

  testWorker() {
    return new Promise((resolve) => {
      let completed = false
      let timeout = null
      try {
        const size = 64
        const pixels = new Uint8ClampedArray(size * size * 4)
        for (let y = 0; y < size; y += 1) {
          for (let x = 0; x < size; x += 1) {
            const index = (y * size + x) * 4
            const dark = Math.hypot(x - 32, y - 32) < 16
            const value = dark ? 25 : 255
            pixels[index] = value
            pixels[index + 1] = value
            pixels[index + 2] = value
            pixels[index + 3] = 255
          }
        }
        const instance = wx.createWorker('workers/generator.js')
        const finish = (ok, detail) => {
          if (completed) return
          completed = true
          if (timeout) clearTimeout(timeout)
          instance.terminate()
          this.addCheck('Worker / 绕线生成核心', ok, detail)
          resolve()
        }
        instance.onMessage((message) => {
          if (message.type === 'result') {
            const result = message.result
            finish(Boolean(result && result.sequence && result.sequence.length >= 2))
          } else if (message.type === 'error') {
            finish(false, message.message)
          }
        })
        instance.onProcessKilled(() => finish(false, 'worker killed'))
        instance.postMessage({
          type: 'generate',
          options: {
            pixels,
            size,
            pinCount: 40,
            requestedLines: 40,
            circleMm: 260,
            lineMm: 0.1,
            autoStop: true
          }
        })
        timeout = setTimeout(() => finish(false, 'timeout'), 10000)
      } catch (error) {
        this.addCheck('Worker / 绕线生成核心', false, error.message)
        resolve()
      }
    })
  },

  testPreviewMetrics() {
    try {
      const normal = previewMetrics.threadMetrics(100, 300, 0.5, 200)
      const zoomed = previewMetrics.threadMetrics(200, 300, 0.5, 200)
      const nailsNormal = previewMetrics.nailMetrics(200, 100)
      const nailsZoomed = previewMetrics.nailMetrics(200, 200)
      this.addCheck(
        '预览物理比例 / 跟随缩放',
        zoomed.stroke === normal.stroke * 2 &&
          nailsZoomed.baseText > nailsNormal.baseText &&
          offlineTts.numberTokens(110, 'zh').join(',') === 'one,hundred,one,ten'
      )
    } catch (error) {
      this.addCheck('预览物理比例 / 跟随缩放', false, error.message)
    }
  },

  testOfflineAudio() {
    return new Promise((resolve) => {
      if (!offlineTts.supported()) {
        this.addCheck('内置离线数字语音', false, 'audio unsupported')
        resolve()
        return
      }
      const audio = wx.createInnerAudioContext()
      let finished = false
      const timeout = setTimeout(() => finish(false, 'timeout'), 5000)
      const finish = (ok, detail) => {
        if (finished) return
        finished = true
        clearTimeout(timeout)
        try { audio.destroy() } catch (error) {}
        this.addCheck('内置离线数字语音', ok, detail)
        resolve()
      }
      audio.volume = 0
      audio.onEnded(() => finish(true))
      audio.onError((error) => finish(false, error.errMsg || error.message))
      audio.src = '/assets/tts/zh/one.mp3'
      audio.play()
    })
  }
})
