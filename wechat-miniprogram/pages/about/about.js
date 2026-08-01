const share = require('../../utils/share')

Page({
  data: {
    text: {},
    language: 'zh'
  },

  onLoad() {
    const app = getApp()
    this.setData({
      text: app.globalData.text,
      language: app.globalData.language
    })
    wx.setNavigationBarTitle({ title: app.globalData.text.aboutTitle })
    share.enableShareMenu()
  },

  copyValue(event) {
    wx.setClipboardData({
      data: event.currentTarget.dataset.value,
      success: () => wx.showToast({ title: this.data.text.copied, icon: 'success' })
    })
  },

  onShareAppMessage() { return share.appMessage() },
  onShareTimeline() { return share.timeline() }
})
