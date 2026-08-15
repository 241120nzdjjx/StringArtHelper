const HOME_PATH = '/pages/home/home'

function enableShareMenu() {
  if (typeof wx === 'undefined' || typeof wx.showShareMenu !== 'function') return
  wx.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline']
  })
}

function title() {
  const app = typeof getApp === 'function' ? getApp() : null
  const language = app && app.globalData ? app.globalData.language : 'zh'
  return language === 'en'
    ? 'String Art Helper · Create string art fully offline'
    : '绕线画助手 · 纯本地生成绕线路径'
}

function appMessage() {
  return { title: title(), path: HOME_PATH }
}

function timeline() {
  return { title: title(), query: '' }
}

module.exports = { enableShareMenu, appMessage, timeline }
