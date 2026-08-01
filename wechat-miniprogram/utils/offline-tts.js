const DIGITS = ['zero', 'one', 'two', 'three', 'four', 'five', 'six', 'seven', 'eight', 'nine']

function englishTokens(value) {
  const number = Math.max(0, Math.min(99999, Math.round(Number(value) || 0)))
  return String(number).split('').map((digit) => DIGITS[Number(digit)])
}

function chineseUnder100(value, omitLeadingOne) {
  if (value < 10) return [DIGITS[value]]
  const tens = Math.floor(value / 10)
  const result = []
  if (tens > 1 || !omitLeadingOne) result.push(DIGITS[tens])
  result.push('ten')
  if (value % 10) result.push(DIGITS[value % 10])
  return result
}

function chineseUnder10000(value, omitLeadingOne) {
  if (value < 100) return chineseUnder100(value, omitLeadingOne)
  if (value >= 1000) {
    const result = [DIGITS[Math.floor(value / 1000)], 'thousand']
    const rest = value % 1000
    if (rest) {
      if (rest < 100) result.push('zero')
      result.push.apply(result, chineseUnder10000(rest, false))
    }
    return result
  }
  const result = [DIGITS[Math.floor(value / 100)], 'hundred']
  const rest = value % 100
  if (rest) {
    if (rest < 10) result.push('zero')
    result.push.apply(result, chineseUnder100(rest, false))
  }
  return result
}

function chineseTokens(value) {
  const number = Math.max(0, Math.min(99999, Math.round(Number(value) || 0)))
  if (number < 10000) return chineseUnder10000(number, true)
  const result = chineseUnder10000(Math.floor(number / 10000), true).concat(['ten_thousand'])
  const rest = number % 10000
  if (rest) {
    if (rest < 1000) result.push('zero')
    result.push.apply(result, chineseUnder10000(rest, false))
  }
  return result
}

function numberTokens(value, language) {
  return language === 'en' ? englishTokens(value) : chineseTokens(value)
}

function normalizeRate(value) {
  return Math.max(0.75, Math.min(1.6, Number(value) || 1.25))
}

class OfflineNumberSpeaker {
  constructor() {
    this.context = null
    this.tokens = []
    this.index = 0
    this.language = 'zh'
    this.resolve = null
    this.job = 0
    this.rate = 1.25
  }

  supported() {
    return typeof wx !== 'undefined' && typeof wx.createInnerAudioContext === 'function'
  }

  ensureContext() {
    if (this.context || !this.supported()) return
    this.context = wx.createInnerAudioContext()
    this.context.obeyMuteSwitch = false
    this.context.volume = 1
    this.applyRate()
    this.context.onEnded(() => this.playNext())
    this.context.onError(() => this.playNext())
  }

  speak(value, language) {
    this.stop()
    if (!this.supported()) return Promise.resolve(false)
    this.ensureContext()
    this.language = language === 'en' ? 'en' : 'zh'
    this.tokens = numberTokens(value, this.language)
    this.index = 0
    const job = ++this.job
    return new Promise((resolve) => {
      this.resolve = (played) => {
        if (job === this.job) this.resolve = null
        resolve(played)
      }
      this.playNext()
    })
  }

  playNext() {
    if (!this.context) return this.finish(false)
    if (this.index >= this.tokens.length) return this.finish(true)
    const token = this.tokens[this.index]
    this.index += 1
    this.applyRate()
    this.context.src = '/assets/tts/' + this.language + '/' + token + '.mp3'
    this.context.play()
  }

  applyRate() {
    if (!this.context) return
    try { this.context.playbackRate = this.rate } catch (error) {}
  }

  setRate(value) {
    this.rate = normalizeRate(value)
    this.applyRate()
    return this.rate
  }

  finish(played) {
    const done = this.resolve
    this.resolve = null
    this.tokens = []
    if (done) done(played)
  }

  stop() {
    this.job += 1
    if (this.context) {
      try { this.context.stop() } catch (error) {}
    }
    this.finish(false)
  }

  destroy() {
    this.stop()
    if (this.context) {
      try { this.context.destroy() } catch (error) {}
      this.context = null
    }
  }
}

const speaker = new OfflineNumberSpeaker()

module.exports = {
  numberTokens,
  normalizeRate,
  speakNumber: (value, language) => speaker.speak(value, language),
  setRate: (value) => speaker.setRate(value),
  stop: () => speaker.stop(),
  destroy: () => speaker.destroy(),
  supported: () => speaker.supported()
}
