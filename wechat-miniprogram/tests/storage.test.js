const assert = require('assert')

function createWxMock() {
  const storage = new Map()
  const files = new Map()
  const directories = new Set(['/data'])
  let failNextWrite = false
  const fs = {
    accessSync(path) { if (!files.has(path) && !directories.has(path)) throw new Error('ENOENT') },
    mkdirSync(path) { directories.add(path) },
    writeFileSync(path, data, encoding) {
      if (failNextWrite) { failNextWrite = false; throw new Error('disk full') }
      if (typeof data === 'string') files.set(path, data)
      else {
        const bytes = data instanceof Uint8Array ? data : new Uint8Array(data)
        files.set(path, new Uint8Array(bytes))
      }
    },
    readFileSync(path, encoding) {
      if (!files.has(path)) throw new Error('ENOENT')
      const value = files.get(path)
      if (encoding) return typeof value === 'string' ? value : Buffer.from(value).toString(encoding)
      return typeof value === 'string' ? Buffer.from(value) : new Uint8Array(value)
    },
    renameSync(from, to) {
      if (!files.has(from)) throw new Error('ENOENT')
      files.set(to, files.get(from)); files.delete(from)
    },
    unlinkSync(path) { if (!files.delete(path)) throw new Error('ENOENT') }
  }
  return {
    env: { USER_DATA_PATH: '/data' },
    getFileSystemManager() { return fs },
    getStorageSync(key) { return storage.has(key) ? storage.get(key) : '' },
    setStorageSync(key, value) {
      if (failNextWrite) { failNextWrite = false; throw new Error('storage full') }
      storage.set(key, JSON.parse(JSON.stringify(value)))
    },
    removeStorageSync(key) { storage.delete(key) },
    getStorageInfoSync() { return { keys: Array.from(storage.keys()) } },
    __storage: storage,
    __files: files,
    __failNextWrite() { failNextWrite = true }
  }
}

function base(name, index) {
  return {
    name, sourceName: name + '.txt', index: index || 0, sequence: [0, 2, 4, 1, 3, 0],
    params: { nails: 5, circleMm: 260, boardMm: 300, lineMm: 0.2, autoStop: true, lines: 5 }
  }
}

function freshStore(mock) {
  global.wx = mock
  const modulePath = require.resolve('../utils/project-store')
  delete require.cache[modulePath]
  return require('../utils/project-store')
}

function run() {
  const mock = createWxMock()
  const store = freshStore(mock)
  store.initialize()

  const first = store.activateNewProject(null, base('图片一'))
  assert.strictEqual(store.listProjects('auto').items.length, 1, 'new image project must create auto resume immediately')
  const second = store.activateNewProject(first, base('TXT 二'))
  assert.notStrictEqual(first.id, second.id)
  assert.strictEqual(store.listProjects('auto').items.length, 2, 'new project must not overwrite previous auto resume')

  const manual = store.createManualSnapshot(second, '固定节点')
  second.index = 2
  store.forceSaveCurrent(second)
  assert.strictEqual(store.loadProject(manual.id).index, 0, 'manual snapshot must not follow current progress')

  const fromManual = store.openProject(second, manual.id)
  fromManual.index = 1
  store.forceSaveCurrent(fromManual)
  assert.notStrictEqual(fromManual.id, manual.id, 'editing a manual snapshot must branch to a new auto project')
  assert.strictEqual(store.loadProject(manual.id).index, 0)

  const viewedManual = store.openProject(fromManual, manual.id)
  store.deleteProject(manual.id, viewedManual)
  assert.ok(store.currentBinding() && store.currentBinding().mode === 'auto', 'deleting an open manual snapshot must preserve current work as auto resume')
  assert.ok(store.loadProject(viewedManual.id))
  Object.assign(fromManual, viewedManual)

  const nonCurrentManual = store.createManualSnapshot(fromManual, '只删我')
  store.deleteProject(nonCurrentManual.id, fromManual)
  assert.ok(store.loadProject(fromManual.id), 'deleting a non-current snapshot must not affect current project')

  const renamed = store.renameProject(fromManual.id, '改名后的当前项目')
  assert.ok(renamed.isCurrent)
  assert.strictEqual(store.currentBinding().projectId, fromManual.id)

  const snapshotBeforeCompletion = store.createManualSnapshot(fromManual, '完成前快照')
  fromManual.index = fromManual.sequence.length - 2
  store.forceSaveCurrent(fromManual)
  assert.strictEqual(store.loadProject(fromManual.id), null, 'near-complete auto resume must be discarded')
  assert.ok(store.loadProject(snapshotBeforeCompletion.id), 'near-complete cleanup must not delete manual snapshots')

  const current = store.activateNewProject(fromManual, base('待删除当前'))
  const currentId = current.id
  const deleted = store.deleteProject(currentId, current)
  assert.ok(deleted.currentCleared)
  assert.strictEqual(store.currentBinding(), null)
  store.forceSaveCurrent(current)
  assert.strictEqual(store.loadProject(currentId), null, 'deleted current auto project must not revive')

  const active = store.activateNewProject(null, base('导入基线'))
  const imported = Object.assign({}, base('同名'), { type: 'manual' })
  const sameA = store.importManual(imported, 'keep')
  const sameB = store.importManual(imported, 'keep')
  assert.strictEqual(sameB.name, '同名（2）')
  const replaced = store.importManual(Object.assign({}, imported, { index: 2 }), 'overwrite')
  assert.strictEqual(replaced.id, sameA.id)
  assert.strictEqual(replaced.index, 2)

  for (let index = 0; index < 25; index += 1) store.createManualSnapshot(active, '手动 ' + index)
  assert.ok(store.listProjects('manual').items.length > 20, 'manual snapshots must never be silently sliced to 20')

  const stable = store.createManualSnapshot(active, '原子写入')
  const original = store.loadProject(stable.id)
  mock.__failNextWrite()
  assert.throws(() => store.overwriteProject(stable.id, Object.assign({}, active, { index: 3 })), /disk full/)
  assert.strictEqual(store.loadProject(stable.id).index, original.index, 'failed overwrite must retain original snapshot')

  const migrationMock = createWxMock()
  migrationMock.setStorageSync('string_art_project_v1_saved', base('旧版项目'))
  const migrationStore = freshStore(migrationMock)
  const migration = migrationStore.initialize()
  assert.strictEqual(migration.migrated, 1)
  assert.strictEqual(migrationMock.getStorageSync('string_art_project_v1_saved'), '')
  assert.strictEqual(migrationStore.listProjects('manual').items.length, 1)

  console.log('File-backed project store and migration tests passed')
}

run()
