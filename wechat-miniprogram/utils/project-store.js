const INDEX_KEY = 'sah_project_index_v2'
const CURRENT_KEY = 'sah_current_project_v2'
const MIGRATION_KEY = 'sah_project_migration_v2_done'
const OLD_CURRENT_KEY = 'sah_current_project_v1'
const OLD_PROJECTS_KEY = 'sah_projects_v1'
const PROJECT_DIR = 'sah_projects_v2'
const SAVE_DEBOUNCE_MS = 500

let pendingTimer = null
let pendingCurrent = null
let pendingErrorHandler = null

function getWx() {
  if (typeof wx === 'undefined') throw new Error('微信文件系统不可用')
  return wx
}

function safeGet(key, fallback) {
  try {
    const value = getWx().getStorageSync(key)
    return value === '' || value == null ? fallback : value
  } catch (error) {
    return fallback
  }
}

function setRequired(key, value) {
  getWx().setStorageSync(key, value)
}

function rootPath() {
  return getWx().env.USER_DATA_PATH + '/' + PROJECT_DIR
}

function projectPath(id) {
  return rootPath() + '/' + id + '.json'
}

function progressPath(id) {
  return rootPath() + '/' + id + '.progress.json'
}

function thumbnailPath(id) {
  return rootPath() + '/' + id + '.png'
}

function tmpPath(path) {
  return path + '.tmp'
}

function backupPath(path) {
  return path + '.bak'
}

function exists(path) {
  try {
    getWx().getFileSystemManager().accessSync(path)
    return true
  } catch (error) {
    return false
  }
}

function ensureDirectory() {
  const fs = getWx().getFileSystemManager()
  const path = rootPath()
  if (!exists(path)) fs.mkdirSync(path, true)
  return path
}

function removeFile(path) {
  if (!exists(path)) return
  try { getWx().getFileSystemManager().unlinkSync(path) } catch (error) {}
}

function atomicWrite(path, data, encoding) {
  ensureDirectory()
  const fs = getWx().getFileSystemManager()
  const temporary = tmpPath(path)
  const backup = backupPath(path)
  removeFile(temporary)
  fs.writeFileSync(temporary, data, encoding)
  removeFile(backup)
  let movedOriginal = false
  try {
    if (exists(path)) {
      fs.renameSync(path, backup)
      movedOriginal = true
    }
    fs.renameSync(temporary, path)
    removeFile(backup)
  } catch (error) {
    removeFile(temporary)
    if (movedOriginal && !exists(path) && exists(backup)) {
      try { fs.renameSync(backup, path) } catch (restoreError) {}
    }
    throw error
  }
}

function readJsonFile(path) {
  const raw = getWx().getFileSystemManager().readFileSync(path, 'utf8')
  return JSON.parse(raw)
}

function listIndex() {
  const value = safeGet(INDEX_KEY, [])
  return Array.isArray(value) ? value.slice() : []
}

function saveIndex(index) {
  setRequired(INDEX_KEY, index)
}

function idExists(id, index) {
  return index.some((item) => item.id === id) || exists(projectPath(id))
}

function createId(index, now, randomValue) {
  const time = Number(now) || Date.now()
  for (let attempt = 0; attempt < 100; attempt += 1) {
    const random = randomValue == null ? Math.random() : (Number(randomValue) + attempt * 0.000001)
    const suffix = Math.floor(Math.abs(random % 1) * 0x100000000).toString(36).padStart(7, '0')
    const id = 'sah_' + time.toString(36) + '_' + suffix
    if (!idExists(id, index || listIndex())) return id
  }
  throw new Error('无法生成唯一项目 ID')
}

function normalizeParams(params, sequence) {
  const values = params || {}
  return {
    nails: Number(values.nails) || Math.max.apply(null, sequence) + 1,
    circleMm: Number(values.circleMm) || 260,
    boardMm: Number(values.boardMm) || Math.max(300, Number(values.circleMm) || 260),
    lineMm: Number(values.lineMm) || 0.2,
    autoStop: values.autoStop !== false,
    lines: Number(values.lines) || Math.max(1, sequence.length - 1)
  }
}

function normalizeProject(project, type, preserved) {
  const sequence = Array.from(project.sequence || [])
  if (sequence.length < 2) throw new Error('项目至少需要两个钉号')
  const now = Date.now()
  const value = {
    id: preserved && preserved.id ? preserved.id : project.id,
    type: type || project.type || 'auto',
    name: String(project.name || '未命名项目').trim().slice(0, 200),
    sourceName: String(project.sourceName || project.importedFileName || project.name || ''),
    source: project.source || 'unknown',
    index: Math.max(0, Math.min(sequence.length - 1, Number(project.index) || 0)),
    sequence,
    params: normalizeParams(project.params, sequence),
    threadMeters: Number(project.threadMeters) || 0,
    thumbnailPath: project.thumbnailPath || '',
    cropImagePath: project.cropImagePath || '',
    createdAt: preserved && preserved.createdAt ? preserved.createdAt : (Number(project.createdAt) || now),
    updatedAt: Number(project.updatedAt) || (preserved && Number(preserved.updatedAt)) || now
  }
  if (!value.name) throw new Error('项目名称不能为空')
  return value
}

function indexEntry(project) {
  return {
    id: project.id,
    type: project.type,
    name: project.name,
    sourceName: project.sourceName,
    currentIndex: project.index,
    sequenceCount: project.sequence.length,
    nails: project.params.nails,
    circleMm: project.params.circleMm,
    lineMm: project.params.lineMm,
    thumbnailPath: project.thumbnailPath || '',
    projectPath: projectPath(project.id),
    createdAt: project.createdAt,
    updatedAt: project.updatedAt
  }
}

function writeProject(project, preserveIdentity) {
  const index = listIndex()
  const originalIndex = index.slice()
  const existing = project.id ? index.find((item) => item.id === project.id) : null
  const normalized = normalizeProject(Object.assign({}, project, { updatedAt: Date.now() }), project.type, preserveIdentity || existing)
  if (!normalized.id) normalized.id = createId(index)
  const canonicalThumbnail = thumbnailPath(normalized.id)
  let thumbnailBytes = null
  if (project.thumbnailSourcePath && exists(project.thumbnailSourcePath)) {
    try {
      const bytes = getWx().getFileSystemManager().readFileSync(project.thumbnailSourcePath)
      const length = bytes && (bytes.byteLength == null ? bytes.length : bytes.byteLength)
      if (length > 0 && length <= 256 * 1024) {
        thumbnailBytes = bytes
        normalized.thumbnailPath = canonicalThumbnail
      }
    } catch (error) {}
  } else if (normalized.thumbnailPath && normalized.thumbnailPath !== canonicalThumbnail && exists(normalized.thumbnailPath)) {
    thumbnailBytes = getWx().getFileSystemManager().readFileSync(normalized.thumbnailPath)
    normalized.thumbnailPath = canonicalThumbnail
  }
  const bodyPath = projectPath(normalized.id)
  const stepPath = progressPath(normalized.id)
  const fs = getWx().getFileSystemManager()
  const oldBody = exists(bodyPath) ? fs.readFileSync(bodyPath) : null
  const oldProgress = exists(stepPath) ? fs.readFileSync(stepPath) : null
  const oldThumbnail = exists(canonicalThumbnail) ? fs.readFileSync(canonicalThumbnail) : null
  const nextEntry = indexEntry(normalized)
  const position = index.findIndex((item) => item.id === normalized.id)
  if (position >= 0) index[position] = nextEntry
  else index.push(nextEntry)
  try {
    atomicWrite(bodyPath, JSON.stringify(normalized), 'utf8')
    atomicWrite(stepPath, JSON.stringify({ index: normalized.index, updatedAt: normalized.updatedAt }), 'utf8')
    if (thumbnailBytes) atomicWrite(canonicalThumbnail, thumbnailBytes)
    saveIndex(index)
  } catch (error) {
    try {
      if (oldBody == null) removeFile(bodyPath); else atomicWrite(bodyPath, oldBody)
      if (oldProgress == null) removeFile(stepPath); else atomicWrite(stepPath, oldProgress)
      if (oldThumbnail == null) removeFile(canonicalThumbnail); else atomicWrite(canonicalThumbnail, oldThumbnail)
      saveIndex(originalIndex)
    } catch (restoreError) {}
    throw error
  }
  return normalized
}

function loadProject(id) {
  const entry = listIndex().find((item) => item.id === id)
  if (!entry || !exists(projectPath(id))) return null
  try {
    const project = readJsonFile(projectPath(id))
    if (exists(progressPath(id))) {
      const progress = readJsonFile(progressPath(id))
      if (Number.isInteger(progress.index)) project.index = progress.index
      if (Number.isFinite(progress.updatedAt)) project.updatedAt = progress.updatedAt
    }
    return normalizeProject(project, entry.type, entry)
  } catch (error) {
    return null
  }
}

function listProjects(type) {
  const bad = []
  const items = listIndex().filter((entry) => {
    if (type && entry.type !== type) return false
    if (!exists(projectPath(entry.id))) {
      bad.push(entry.id)
      return false
    }
    return true
  }).sort((a, b) => b.updatedAt - a.updatedAt)
  return { items, bad }
}

function setCurrentBinding(projectId, mode, manualIndex) {
  if (!projectId) {
    try { getWx().removeStorageSync(CURRENT_KEY) } catch (error) {}
    return
  }
  setRequired(CURRENT_KEY, { projectId, mode: mode || 'auto', manualIndex })
}

function loadCurrent() {
  const binding = safeGet(CURRENT_KEY, null)
  if (!binding || !binding.projectId) return null
  const project = loadProject(binding.projectId)
  if (!project) return null
  project.currentMode = binding.mode || 'auto'
  project.manualIndex = Number(binding.manualIndex)
  return project
}

function forceSaveCurrent(current) {
  if (!current || !current.sequence || current.sequence.length < 2) return true
  if (current.autoDiscarded) return true
  const binding = safeGet(CURRENT_KEY, null)
  if (binding && binding.mode === 'manual-view' && current.index === binding.manualIndex) return true
  let project = current
  if (!binding || binding.mode !== 'auto' || binding.projectId !== current.id) {
    project = Object.assign({}, current, { id: '', type: 'auto', currentMode: 'auto' })
    project = writeProject(project)
    setCurrentBinding(project.id, 'auto')
    Object.assign(current, project)
  } else {
    const now = Date.now()
    const index = listIndex()
    const entry = index.find((item) => item.id === current.id)
    if (!entry) {
      project = writeProject(Object.assign({}, current, { type: 'auto' }))
    } else {
      const path = progressPath(current.id)
      const oldProgress = exists(path) ? getWx().getFileSystemManager().readFileSync(path) : null
      const oldIndex = listIndex()
      try {
        atomicWrite(path, JSON.stringify({ index: current.index, updatedAt: now }), 'utf8')
        entry.currentIndex = current.index
        entry.updatedAt = now
        saveIndex(index)
        current.updatedAt = now
      } catch (error) {
        try {
          if (oldProgress == null) removeFile(path); else atomicWrite(path, oldProgress)
          saveIndex(oldIndex)
        } catch (restoreError) {}
        throw error
      }
    }
  }
  discardAutoIfComplete(current)
  return true
}

function scheduleSaveCurrent(current, onError) {
  if (!current || current.autoDiscarded) return
  pendingCurrent = current
  pendingErrorHandler = typeof onError === 'function' ? onError : pendingErrorHandler
  if (pendingTimer) clearTimeout(pendingTimer)
  pendingTimer = setTimeout(() => {
    const value = pendingCurrent
    pendingCurrent = null
    pendingTimer = null
    const errorHandler = pendingErrorHandler
    pendingErrorHandler = null
    try { forceSaveCurrent(value) } catch (error) {
      if (errorHandler) errorHandler(error)
    }
  }, SAVE_DEBOUNCE_MS)
}

function flushScheduledSave() {
  if (pendingTimer) clearTimeout(pendingTimer)
  pendingTimer = null
  const current = pendingCurrent
  pendingCurrent = null
  pendingErrorHandler = null
  return current ? forceSaveCurrent(current) : true
}

function readThumbnail(project) {
  const path = project && project.thumbnailPath
  if (!path || !exists(path)) return new Uint8Array(0)
  const value = getWx().getFileSystemManager().readFileSync(path)
  return value instanceof Uint8Array ? value : new Uint8Array(value)
}

function currentBinding() {
  return safeGet(CURRENT_KEY, null)
}

function activateNewProject(oldCurrent, project) {
  if (oldCurrent && !forceSaveCurrent(oldCurrent)) throw new Error('旧项目自动保存失败，已取消打开新项目。')
  const normalized = writeProject(Object.assign({}, project, { id: '', type: 'auto', index: 0 }))
  setCurrentBinding(normalized.id, 'auto')
  return normalized
}

function openProject(oldCurrent, id) {
  if (oldCurrent && !forceSaveCurrent(oldCurrent)) throw new Error('旧项目自动保存失败，已取消打开新项目。')
  const target = loadProject(id)
  if (!target) throw new Error('项目文件不存在或已损坏')
  if (target.type === 'manual') {
    target.currentMode = 'manual-view'
    target.manualIndex = target.index
    setCurrentBinding(target.id, 'manual-view', target.index)
  } else {
    target.currentMode = 'auto'
    setCurrentBinding(target.id, 'auto')
  }
  return target
}

function createManualSnapshot(current, name) {
  if (!current) throw new Error('尚未打开项目')
  const value = String(name || '').trim()
  if (!value || value.length > 200) throw new Error('名称不能为空且不能超过 200 个字符')
  return writeProject(Object.assign({}, current, {
    id: '',
    type: 'manual',
    name: value,
    index: current.index
  }))
}

function overwriteProject(id, current) {
  const entry = listIndex().find((item) => item.id === id)
  if (!entry) throw new Error('要覆盖的存档不存在')
  return writeProject(Object.assign({}, current, {
    id: entry.id,
    type: entry.type,
    name: entry.name,
    createdAt: entry.createdAt
  }), entry)
}

function renameProject(id, name) {
  const value = String(name || '').trim()
  if (!value || value.length > 200) throw new Error('名称不能为空且不能超过 200 个字符')
  const project = loadProject(id)
  if (!project) throw new Error('项目不存在')
  project.name = value
  const saved = writeProject(project, project)
  const binding = safeGet(CURRENT_KEY, null)
  return { project: saved, isCurrent: !!binding && binding.projectId === id }
}

function deleteProject(id, current) {
  let index = listIndex()
  const entry = index.find((item) => item.id === id)
  if (!entry) return { currentCleared: false }
  const binding = safeGet(CURRENT_KEY, null)
  const currentAuto = !!binding && binding.mode === 'auto' && binding.projectId === id
  const currentManualView = !!binding && binding.mode === 'manual-view' && binding.projectId === id
  if (currentManualView && current) {
    setCurrentBinding(null)
    forceSaveCurrent(current)
    index = listIndex()
  }
  removeFile(projectPath(id))
  removeFile(progressPath(id))
  removeFile(thumbnailPath(id))
  removeFile(tmpPath(projectPath(id)))
  removeFile(backupPath(projectPath(id)))
  saveIndex(index.filter((item) => item.id !== id))
  if (currentAuto) {
    setCurrentBinding(null)
    if (current) current.autoDiscarded = true
  }
  return { currentCleared: currentAuto }
}

function discardThreshold(sequenceLength) {
  return Math.max(3, Math.ceil(sequenceLength * 0.001))
}

function shouldDiscardAuto(sequenceLength, currentIndex) {
  const remaining = Math.max(0, sequenceLength - 1 - currentIndex)
  return remaining <= discardThreshold(sequenceLength)
}

function discardAutoIfComplete(current) {
  if (!current || current.autoDiscarded || !shouldDiscardAuto(current.sequence.length, current.index)) return false
  const binding = safeGet(CURRENT_KEY, null)
  if (!binding || binding.mode !== 'auto' || binding.projectId !== current.id) return false
  deleteProject(current.id, current)
  current.autoDiscarded = true
  return true
}

function uniqueName(base, names) {
  const value = String(base || '导入存档').trim() || '导入存档'
  if (names.indexOf(value) < 0) return value
  let suffix = 2
  while (names.indexOf(value + '（' + suffix + '）') >= 0) suffix += 1
  return value + '（' + suffix + '）'
}

function importManual(project, conflictMode) {
  const manuals = listProjects('manual').items
  const same = manuals.find((item) => item.name === project.name)
  if (same && conflictMode === 'cancel') return null
  if (same && conflictMode === 'overwrite') {
    return writeProject(Object.assign({}, project, {
      id: same.id,
      type: 'manual',
      createdAt: same.createdAt
    }), same)
  }
  const name = same ? uniqueName(project.name, manuals.map((item) => item.name)) : project.name
  return writeProject(Object.assign({}, project, { id: '', type: 'manual', name }))
}

function migrateLegacy() {
  if (safeGet(MIGRATION_KEY, false)) return { migrated: 0 }
  const created = []
  try {
    const oldProjects = safeGet(OLD_PROJECTS_KEY, [])
    const oldCurrent = safeGet(OLD_CURRENT_KEY, null)
    const candidates = []
    if (Array.isArray(oldProjects)) oldProjects.forEach((item) => candidates.push({ item, type: 'manual' }))
    const info = getWx().getStorageInfoSync ? getWx().getStorageInfoSync() : { keys: [] }
    ;(info.keys || []).filter((key) => key.indexOf('string_art_project_v1_') === 0).forEach((key) => {
      const item = safeGet(key, null)
      if (item && item.sequence) candidates.push({ item, type: 'manual', key })
    })
    candidates.forEach((candidate) => {
      const saved = writeProject(Object.assign({}, candidate.item, { id: '', type: candidate.type }))
      created.push(saved.id)
    })
    if (oldCurrent && oldCurrent.sequence && oldCurrent.sequence.length >= 2) {
      const savedCurrent = writeProject(Object.assign({}, oldCurrent, { id: '', type: 'auto' }))
      created.push(savedCurrent.id)
      setCurrentBinding(savedCurrent.id, 'auto')
    }
    setRequired(MIGRATION_KEY, true)
    try { getWx().removeStorageSync(OLD_PROJECTS_KEY) } catch (error) {}
    try { getWx().removeStorageSync(OLD_CURRENT_KEY) } catch (error) {}
    ;(info.keys || []).filter((key) => key.indexOf('string_art_project_v1_') === 0).forEach((key) => {
      try { getWx().removeStorageSync(key) } catch (error) {}
    })
    return { migrated: created.length }
  } catch (error) {
    created.forEach((id) => {
      try { deleteProject(id) } catch (cleanupError) {}
    })
    throw new Error('旧项目迁移失败，原数据已保留：' + (error.message || String(error)))
  }
}

function initialize() {
  ensureDirectory()
  return migrateLegacy()
}

module.exports = {
  CURRENT_KEY,
  INDEX_KEY,
  MIGRATION_KEY,
  SAVE_DEBOUNCE_MS,
  activateNewProject,
  atomicWrite,
  createId,
  createManualSnapshot,
  currentBinding,
  deleteProject,
  discardAutoIfComplete,
  discardThreshold,
  flushScheduledSave,
  forceSaveCurrent,
  importManual,
  indexEntry,
  initialize,
  listProjects,
  loadCurrent,
  loadProject,
  migrateLegacy,
  openProject,
  overwriteProject,
  projectPath,
  readThumbnail,
  renameProject,
  scheduleSaveCurrent,
  shouldDiscardAuto,
  thumbnailPath,
  uniqueName,
  writeProject
}
