const { languagePack } = require('./utils/i18n')
const projectStore = require('./utils/project-store')

App({
  globalData: {
    language: 'zh',
    text: languagePack('zh'),
    sourceImagePath: '',
    sourceImageName: '',
    cropImagePath: '',
    crop: null,
    generation: null,
    current: null
  },

  onLaunch() {
    const savedLanguage = wx.getStorageSync('sah_language')
    let systemLanguage = 'zh'
    try {
      systemLanguage = (wx.getSystemInfoSync().language || 'zh').toLowerCase()
    } catch (error) {
      systemLanguage = 'zh'
    }
    this.setLanguage(savedLanguage || (systemLanguage.indexOf('en') === 0 ? 'en' : 'zh'))
    try {
      projectStore.initialize()
      this.globalData.current = projectStore.loadCurrent()
    } catch (error) {
      this.globalData.current = null
      setTimeout(() => wx.showModal({
        title: this.globalData.text.error,
        content: error.message || String(error),
        showCancel: false
      }), 0)
    }
  },

  onHide() {
    try {
      projectStore.flushScheduledSave()
      projectStore.forceSaveCurrent(this.globalData.current)
    } catch (error) {
      wx.showModal({
        title: this.globalData.text.error,
        content: '自动保存失败：' + (error.message || String(error)),
        showCancel: false
      })
    }
  },

  setLanguage(language) {
    const next = language === 'en' ? 'en' : 'zh'
    this.globalData.language = next
    this.globalData.text = languagePack(next)
    wx.setStorageSync('sah_language', next)
  },

  setCurrent(current, options) {
    this.globalData.current = current
    if (!current) return
    if (options && options.immediate) projectStore.forceSaveCurrent(current)
    else projectStore.scheduleSaveCurrent(current, (error) => wx.showModal({
      title: this.globalData.text.error,
      content: '自动保存失败：' + (error.message || String(error)),
      showCancel: false
    }))
  },

  activateNewProject(project) {
    const current = projectStore.activateNewProject(this.globalData.current, project)
    this.globalData.current = current
    return current
  },

  openStoredProject(id) {
    const current = projectStore.openProject(this.globalData.current, id)
    this.globalData.current = current
    return current
  }
})
