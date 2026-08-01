function sanitizeName(value) {
  return String(value || 'string-art')
    .replace(/[\\/:*?"<>|]/g, '_')
    .replace(/\s+/g, ' ')
    .trim()
    .slice(0, 180) || 'string-art'
}

function writeUserFile(filename, data, encoding) {
  const fs = wx.getFileSystemManager()
  const path = wx.env.USER_DATA_PATH + '/' + sanitizeName(filename)
  fs.writeFileSync(path, data, encoding)
  return path
}

function shareFile(path, filename, text) {
  if (typeof wx.shareFileMessage === 'function') {
    wx.shareFileMessage({
      filePath: path,
      fileName: filename,
      fail() {
        wx.showModal({ title: text.error, content: text.shareUnsupported, showCancel: false })
      }
    })
    return
  }
  wx.showModal({
    title: text.fileSaved,
    content: path + '\n\n' + text.shareUnsupported,
    showCancel: false
  })
}

module.exports = { sanitizeName, writeUserFile, shareFile }
