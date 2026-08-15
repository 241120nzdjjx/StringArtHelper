const projectStore = require('../../utils/project-store')
const { decodeSar, encodeSar4, MAX_FILE_BYTES, sarFilename } = require('../../utils/sar')
const { writeUserFile, shareFile } = require('../../utils/files')

function pad(value) { return String(value).padStart(2, '0') }
function message(zh, en) { return getApp().globalData.language === 'en' ? en : zh }
function currentSummary(current) {
  if (!current) return null
  return {
    name: current.name,
    index: Number(current.index) || 0,
    sequenceCount: Array.isArray(current.sequence) ? current.sequence.length : 0,
    params: Object.assign({}, current.params)
  }
}

Page({
  data: {
    text: {}, current: null, autos: [], manuals: [], badCount: 0,
    busy: false, menuProject: null, editMode: '', editValue: '', importProject: null,
    importHasConflict: false
  },

  onLoad(options) {
    this.openSaveEditor = !!(options && options.save === '1')
    this.openPendingImport = !!(options && options.pendingImport === '1')
  },

  onShow() {
    const app = getApp()
    this.setData({ text: app.globalData.text, current: currentSummary(app.globalData.current) })
    this.refreshList()
    if (this.openSaveEditor) {
      this.openSaveEditor = false
      setTimeout(() => this.startSaveCurrent(), 0)
    }
    if (this.openPendingImport) {
      this.openPendingImport = false
      const project = app.globalData.pendingSarImport
      app.globalData.pendingSarImport = null
      if (project) {
        this.setData({ busy: true })
        setTimeout(() => this.prepareImport(project), 0)
      }
    }
    wx.setNavigationBarTitle({ title: app.globalData.text.projectsTitle })
  },

  refreshList() {
    const autoResult = projectStore.listProjects('auto')
    const manualResult = projectStore.listProjects('manual')
    const binding = projectStore.currentBinding()
    const decorate = (item) => Object.assign({}, item, {
      stepText: (item.currentIndex + 1) + ' / ' + item.sequenceCount,
      dateText: this.formatDate(item.updatedAt),
      isCurrent: !!binding && binding.mode === 'auto' && binding.projectId === item.id
    })
    this.setData({
      autos: autoResult.items.map(decorate),
      manuals: manualResult.items.map(decorate),
      badCount: autoResult.bad.length + manualResult.bad.length,
      current: currentSummary(getApp().globalData.current)
    })
    if (autoResult.bad.length + manualResult.bad.length) {
      wx.showToast({ title: message('已跳过损坏的项目索引', 'Skipped damaged project entries'), icon: 'none' })
    }
  },

  formatDate(timestamp) {
    const date = new Date(Number(timestamp) || Date.now())
    return pad(date.getMonth() + 1) + '-' + pad(date.getDate()) + ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes())
  },

  findEntry(id) {
    return this.data.autos.concat(this.data.manuals).find((item) => item.id === id)
  },

  openProject(event) {
    if (this.data.busy) return
    const id = event.currentTarget.dataset.id
    try {
      getApp().openStoredProject(id)
      wx.navigateTo({ url: '/pages/reader/reader' })
    } catch (error) { this.showError(error) }
  },

  showMenu(event) {
    const entry = this.findEntry(event.currentTarget.dataset.id)
    if (entry) this.setData({ menuProject: entry })
  },

  closeMenu() { this.setData({ menuProject: null }) },
  noop() {},

  menuOpen() {
    const entry = this.data.menuProject
    this.closeMenu()
    if (entry) this.openProject({ currentTarget: { dataset: { id: entry.id } } })
  },

  startSaveCurrent() {
    const current = getApp().globalData.current
    if (!current || this.data.busy) return
    const value = current.name + ' · 第' + (current.index + 1) + '步'
    this.setData({ editMode: 'save', editValue: value, menuProject: null })
  },

  startRename() {
    const entry = this.data.menuProject
    if (!entry) return
    this.setData({ editMode: 'rename:' + entry.id, editValue: entry.name, menuProject: null })
  },

  onEditInput(event) { this.setData({ editValue: event.detail.value }) },
  cancelEdit() { this.setData({ editMode: '', editValue: '' }) },

  confirmEdit() {
    if (this.data.busy) return
    const mode = this.data.editMode
    const name = String(this.data.editValue || '').trim()
    if (!name || name.length > 200) {
      wx.showModal({ title: this.data.text.error, content: message('名称不能为空且不能超过 200 个字符', 'Name is required and must be no longer than 200 UTF-16 code units.'), showCancel: false })
      return
    }
    this.setData({ busy: true })
    try {
      if (mode === 'save') {
        projectStore.createManualSnapshot(getApp().globalData.current, name)
        wx.showToast({ title: message('已保存：“', 'Saved: “') + name + '”', icon: 'none' })
      } else if (mode.indexOf('rename:') === 0) {
        const id = mode.slice(7)
        const result = projectStore.renameProject(id, name)
        if (result.isCurrent) getApp().globalData.current.name = name
        wx.showToast({ title: message('已重命名', 'Renamed'), icon: 'success' })
      }
      this.setData({ editMode: '', editValue: '' })
      this.refreshList()
    } catch (error) { this.showError(error) }
    this.setData({ busy: false })
  },

  overwriteSelected() {
    const entry = this.data.menuProject
    const current = getApp().globalData.current
    if (!entry || !current) return
    this.closeMenu()
    wx.showModal({
      title: message('确认覆盖', 'Confirm overwrite'),
      content: message('将使用当前第 ', 'Overwrite “' + entry.name + '” with current step ') +
        (current.index + 1) + message(' 步覆盖“' + entry.name + '”。原内容无法恢复。', '? The previous snapshot cannot be recovered.'),
      confirmColor: '#9769ff',
      success: (result) => {
        if (!result.confirm) return
        try {
          projectStore.overwriteProject(entry.id, current)
          wx.showToast({ title: message('已覆盖', 'Overwritten'), icon: 'success' })
          this.refreshList()
        } catch (error) { this.showError(error) }
      }
    })
  },

  freezeSar(entry) {
    const project = projectStore.loadProject(entry.id)
    if (!project) throw new Error('存档文件不存在或已损坏')
    const bytes = encodeSar4(Object.assign({}, project, {
      importedFileName: project.sourceName,
      thumbnail: projectStore.readThumbnail(project)
    }))
    return { bytes, project }
  },

  exportSelected() { this.outputSelected(false) },
  shareSelected() { this.outputSelected(true) },

  outputSelected(share) {
    const entry = this.data.menuProject
    if (!entry || this.data.busy) return
    this.closeMenu()
    this.setData({ busy: true })
    try {
      const frozen = this.freezeSar(entry)
      const filename = sarFilename(frozen.project, getApp().globalData.language)
      const path = writeUserFile(filename, frozen.bytes.buffer)
      if (share) shareFile(path, filename, this.data.text)
      else wx.showModal({ title: message('已导出', 'Exported'), content: path, showCancel: false })
    } catch (error) { this.showError(error) }
    this.setData({ busy: false })
  },

  deleteSelected() {
    const entry = this.data.menuProject
    if (!entry) return
    this.closeMenu()
    const currentWarning = entry.isCurrent
      ? message('“' + entry.name + '”是当前自动项目。\n删除后会同时清空当前项目，且不会自动恢复。', '“' + entry.name + '” is the current Auto Resume project.\nDeleting it also clears the current project and it will not be recreated.')
      : message('确定删除“' + entry.name + '”吗？', 'Delete “' + entry.name + '”?')
    wx.showModal({
      title: this.data.text.deleteArchive, content: currentWarning, confirmColor: '#d65a70',
      success: (result) => {
        if (!result.confirm) return
        try {
          const deleted = projectStore.deleteProject(entry.id, getApp().globalData.current)
          if (deleted.currentCleared) getApp().globalData.current = null
          wx.showToast({ title: message('已删除', 'Deleted'), icon: 'success' })
          this.refreshList()
        } catch (error) { this.showError(error) }
      }
    })
  },

  importSar() {
    if (this.data.busy) return
    wx.chooseMessageFile({
      count: 1, type: 'file', extension: ['sar', 'bin'],
      success: (result) => {
        const file = result.tempFiles && result.tempFiles[0]
        if (!file) return
        if (Number(file.size) > MAX_FILE_BYTES) return this.showError(new Error(message('SAR 文件不能超过 16 MiB', 'SAR files must not exceed 16 MiB.')))
        this.setData({ busy: true })
        wx.getFileSystemManager().readFile({
          filePath: file.path,
          success: (readResult) => {
            try {
              const project = decodeSar(readResult.data)
              project.sourceName = project.importedFileName || file.name
              project.source = 'sar'
              this.prepareImport(project)
            } catch (error) {
              this.setData({ busy: false })
              this.showError(error)
            }
          },
          fail: (error) => { this.setData({ busy: false }); this.showError(error) }
        })
      }
    })
  },

  prepareImport(project) {
    const accept = () => {
      const hasConflict = this.data.manuals.some((item) => item.name === project.name)
      this.setData({ importProject: project, importHasConflict: hasConflict, busy: false })
    }
    if (!project.thumbnail || !project.thumbnail.length) {
      accept()
      return
    }
    const path = wx.env.USER_DATA_PATH + '/sah_import_thumbnail_check.png'
    try { wx.getFileSystemManager().writeFileSync(path, project.thumbnail.buffer) } catch (error) {
      this.setData({ busy: false }); this.showError(new Error(message('SAR 缩略图无法写入验证', 'Could not prepare the SAR thumbnail for validation.')))
      return
    }
    wx.getImageInfo({
      src: path,
      success: accept,
      fail: () => { this.setData({ busy: false }); this.showError(new Error(message('SAR 缩略图损坏或无法解码', 'The SAR thumbnail is damaged or cannot be decoded.'))) }
    })
  },

  cancelImport() { this.setData({ importProject: null, importHasConflict: false }) },
  confirmImport() { this.persistImport('keep') },
  overwriteImport() { this.persistImport('overwrite') },
  keepBothImport() { this.persistImport('keep') },

  persistImport(mode) {
    const project = this.data.importProject
    if (!project || this.data.busy) return
    this.setData({ busy: true })
    try {
      let thumbnailSourcePath = ''
      if (project.thumbnail && project.thumbnail.length) {
        thumbnailSourcePath = wx.env.USER_DATA_PATH + '/sah_import_thumbnail.tmp.png'
        wx.getFileSystemManager().writeFileSync(thumbnailSourcePath, project.thumbnail.buffer)
      }
      const saved = projectStore.importManual(Object.assign({}, project, {
        thumbnailSourcePath,
        params: Object.assign({ boardMm: Math.max(300, project.params.circleMm), autoStop: true }, project.params)
      }), mode)
      this.setData({ importProject: null, importHasConflict: false })
      this.refreshList()
      wx.showModal({
        title: message('已导入：“', 'Imported: “') + saved.name + '”', content: message('存档已作为手动快照保存。', 'The archive was saved as an independent manual snapshot.'), confirmText: this.data.text.openNow, cancelText: this.data.text.later,
        success: (result) => {
          if (!result.confirm) return
          try { getApp().openStoredProject(saved.id); wx.navigateTo({ url: '/pages/reader/reader' }) } catch (error) { this.showError(error) }
        }
      })
    } catch (error) { this.showError(error) }
    this.setData({ busy: false })
  },

  showError(error) {
    wx.showModal({ title: this.data.text.error || '出现问题', content: error.message || error.errMsg || String(error), showCancel: false })
  }
})
