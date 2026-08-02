const assert = require('assert')
const fs = require('fs')
const path = require('path')

const root = path.resolve(__dirname, '..')
const app = JSON.parse(fs.readFileSync(path.join(root, 'app.json'), 'utf8'))

assert.ok(Array.isArray(app.pages) && app.pages.length >= 7)
assert.strictEqual(app.workers, 'workers')

app.pages.forEach((pagePath) => {
  const base = path.join(root, pagePath)
  const jsPath = base + '.js'
  const wxmlPath = base + '.wxml'
  const wxssPath = base + '.wxss'
  const jsonPath = base + '.json'
  ;[jsPath, wxmlPath, wxssPath, jsonPath].forEach((file) => {
    assert.ok(fs.existsSync(file), 'Missing page file: ' + file)
  })
  JSON.parse(fs.readFileSync(jsonPath, 'utf8'))
  const js = fs.readFileSync(jsPath, 'utf8')
  const wxml = fs.readFileSync(wxmlPath, 'utf8')
  const handlers = []
  const handlerPattern = /\bbind(?:tap|input|blur|change|changing|touchstart|touchmove|touchend|touchcancel)="([A-Za-z_$][\w$]*)"/g
  let match
  while ((match = handlerPattern.exec(wxml))) handlers.push(match[1])
  handlers.forEach((handler) => {
    assert.ok(
      new RegExp('\\b' + handler.replace(/[$]/g, '\\$&') + '\\s*\\(').test(js),
      pagePath + ' references missing handler ' + handler
    )
  })
})

assert.ok(fs.existsSync(path.join(root, 'workers', 'generator.js')))
assert.ok(fs.existsSync(path.join(root, 'assets', 'support_wechat.jpg')))
assert.ok(fs.existsSync(path.join(root, 'assets', 'support_alipay.jpg')))
const voiceFiles = [
  ['zh', 'zero'], ['zh', 'ten'], ['zh', 'hundred'], ['zh', 'ten_thousand'],
  ['en', 'zero'], ['en', 'one'], ['en', 'five'], ['en', 'nine']
]
voiceFiles.forEach(([language, token]) => {
  const voicePath = path.join(root, 'assets', 'tts', language, token + '.mp3')
  assert.ok(
    fs.existsSync(voicePath),
    'Missing offline voice token: ' + language + '/' + token
  )
  assert.ok(fs.statSync(voicePath).size > 1000, 'Offline voice token is unexpectedly small: ' + language + '/' + token)
})
const mandarinTokens = [
  'zero', 'one', 'two', 'three', 'four', 'five', 'six',
  'seven', 'eight', 'nine', 'ten', 'hundred', 'thousand', 'ten_thousand'
]
mandarinTokens.forEach((token) => {
  const voicePath = path.join(root, 'assets', 'tts', 'zh', token + '.mp3')
  assert.ok(fs.existsSync(voicePath), 'Missing Mandarin voice token: ' + token)
  assert.ok(fs.statSync(voicePath).size > 2500,
    'Mandarin voice token is probably truncated: ' + token)
})

const config = JSON.parse(fs.readFileSync(path.join(root, 'project.config.json'), 'utf8'))
assert.strictEqual(config.compileType, 'miniprogram')
assert.strictEqual(config.miniprogramRoot, './')

const previewJs = fs.readFileSync(path.join(root, 'pages', 'preview', 'preview.js'), 'utf8')
const previewWxml = fs.readFileSync(path.join(root, 'pages', 'preview', 'preview.wxml'), 'utf8')
const appWxss = fs.readFileSync(path.join(root, 'app.wxss'), 'utf8')
const readerWxss = fs.readFileSync(path.join(root, 'pages', 'reader', 'reader.wxss'), 'utf8')
const cropJs = fs.readFileSync(path.join(root, 'pages', 'crop', 'crop.js'), 'utf8')
const cropWxss = fs.readFileSync(path.join(root, 'pages', 'crop', 'crop.wxss'), 'utf8')
const generatorCore = fs.readFileSync(path.join(root, 'workers', 'generator-core.js'), 'utf8')
const storageJs = fs.readFileSync(path.join(root, 'utils', 'project-store.js'), 'utf8')
const projectsJs = fs.readFileSync(path.join(root, 'pages', 'projects', 'projects.js'), 'utf8')
const projectsWxml = fs.readFileSync(path.join(root, 'pages', 'projects', 'projects.wxml'), 'utf8')
const readerJs = fs.readFileSync(path.join(root, 'pages', 'reader', 'reader.js'), 'utf8')
const readerWxml = fs.readFileSync(path.join(root, 'pages', 'reader', 'reader.wxml'), 'utf8')
const generateJs = fs.readFileSync(path.join(root, 'pages', 'generate', 'generate.js'), 'utf8')
const generateWxml = fs.readFileSync(path.join(root, 'pages', 'generate', 'generate.wxml'), 'utf8')
const aboutWxml = fs.readFileSync(path.join(root, 'pages', 'about', 'about.wxml'), 'utf8')
const homeJs = fs.readFileSync(path.join(root, 'pages', 'home', 'home.js'), 'utf8')
const homeWxml = fs.readFileSync(path.join(root, 'pages', 'home', 'home.wxml'), 'utf8')
const voiceInstaller = fs.readFileSync(path.join(root, 'tools', 'install-open-number-voices.ps1'), 'utf8')
assert.ok(!previewJs.includes('ctx.clip()'), 'Thread endpoints must not be clipped at the nail circle')
assert.ok(previewWxml.includes('text.fileExportHint'), 'TXT export area must explain the chat-forward download workflow')
assert.ok(/THREAD_PATH_BATCH\s*=\s*32/.test(previewJs), 'Long thread paths must render in bounded batches')
assert.ok(previewJs.includes('requestDraw()'), 'Interactive preview redraw must be frame-throttled')
assert.ok(
  /<slider min="100" max="500"[^>]*value="\{\{zoomSlider\}\}"/.test(previewWxml),
  'Preview zoom must match the Android 1x-5x range'
)
assert.ok(/WORK_SIZE\s*=\s*256/.test(generatorCore), 'Generator work size must be 256')
assert.ok(generatorCore.includes('residual[y * size + x] = 1 - luminance'))
assert.ok(!generatorCore.includes('(0.9 - luminance) * 1.35'))
assert.ok(!generatorCore.includes('Math.max(0, residual'))
assert.ok(/score\s*\+=\s*squaredErrorGain/.test(generatorCore))
assert.ok(/RECENT_PIN_WINDOW\s*=\s*20/.test(generatorCore))
assert.ok(generatorCore.includes('best < 0 || bestScore <= 0'))
assert.ok(!storageJs.includes('.slice(0, 20)'), 'Project storage must not silently drop old projects')
assert.ok(storageJs.includes("type: 'manual'") && storageJs.includes("type: 'auto'"))
assert.ok(homeJs.includes('onShareAppMessage()') && homeJs.includes('onShareTimeline()'))
assert.ok(homeWxml.includes('open-type="share"'))
assert.ok(homeJs.includes("extension: ['txt', 'sar', 'bin']"), 'Home import must accept TXT and SAR content')
assert.ok(homeWxml.includes('text.fileImportHint'), 'Home non-image import entry must explain the chat-forward workflow')
assert.ok(aboutWxml.includes('https://github.com/241120nzdjjx/StringArtHelper'))
assert.ok(!readerJs.includes('sequence: this.sequence'), 'Reader must not copy large sequences into page data')
assert.ok(!previewJs.includes('sequence: this.sequence'), 'Preview must not copy large sequences into page data')
assert.ok(readerWxml.includes('bindchange="onVoiceRate"'), 'Reader must expose the voice-rate slider')
assert.ok(readerJs.includes("sah_reader_voice_rate"), 'Voice rate must persist locally')
const mandarinInstallBlock = voiceInstaller.slice(
  voiceInstaller.indexOf('foreach ($entry in $zh.GetEnumerator())'),
  voiceInstaller.indexOf('$digits =')
)
assert.ok(!mandarinInstallBlock.includes("-af 'silenceremove="),
  'Mandarin installer must not trim low-volume syllables with a fixed threshold')
assert.ok(readerWxml.includes('id="readerPreviewCanvas"'), 'Reader must show a live progress canvas')
assert.ok(
  /reader-preview-step-controls[\s\S]*?bindtap="previous"[\s\S]*?bindtap="next"/.test(readerWxml),
  'Reader full-screen preview must expose synchronized previous and next controls'
)
assert.ok(
  /reader-preview-jump-controls[\s\S]*?bindinput="onJumpInput"[\s\S]*?bindtap="jump"/.test(readerWxml),
  'Reader full-screen preview must expose a synchronized compact jump control'
)
assert.ok(
  /reader-preview-number-stage[\s\S]*?previousValue[\s\S]*?currentValue[\s\S]*?nextValue/.test(readerWxml),
  'Reader full-screen preview must show previous, current and next nail numbers'
)
assert.ok(readerWxml.includes('text.previousShort') && readerWxml.includes('text.nextShort'),
  'Narrow full-screen preview controls must use compact localized labels')
assert.ok(
  readerJs.includes('prepareManualStep()') && readerJs.includes('restoreReaderPreviewAfterHold()'),
  'Manual stepping must restore held animation state before changing the shared reader index'
)
assert.ok(
  readerJs.includes('manualSpeakSession') && readerJs.includes('session === this.manualSpeakSession'),
  'Rapid synchronized stepping must not let an older voice completion clear the new speaking state'
)
assert.ok(
  /\.reader-preview-step-controls button\s*\{[\s\S]*?flex:\s*1 1 0[\s\S]*?width:\s*0[\s\S]*?max-width:\s*calc\(50% - 6rpx\)/.test(readerWxss),
  'Reader preview step buttons must be forced into two shrinkable equal-width columns'
)
assert.ok(readerWxml.includes('readerPreviewOpen') &&
  readerWxml.includes('bindtap="openReaderPreview"') &&
  readerWxml.includes('bindtap="closeReaderPreview"'),
  'Reader must open and minimize its combined preview from the bottom button')
assert.ok(!readerWxml.includes('reader-preview-float') &&
  !readerWxml.includes('onPreviewDragMove'),
  'Reader must not keep the discarded floating preview window')
assert.ok(/\.reader-page\s*\{[\s\S]*?padding-bottom:\s*calc\(96rpx/.test(readerWxss) &&
  /\.reader-controls\s*\{[\s\S]*?padding:[\s\S]*?calc\(58rpx/.test(readerWxss),
  'Reader controls must retain extra bottom safe space')
assert.ok(readerJs.includes("ctx.strokeStyle = '#9769ff'") && readerJs.includes('metrics.stroke * 1.5'),
  'Reader must highlight the current thread in purple at 1.5x width')
assert.ok(readerJs.includes('syncPreviewLayer(baseEnd'), 'Reader preview must incrementally cache completed threads')
assert.ok(readerJs.includes('previewAnimationFinalHeld') &&
  readerJs.includes('restoreReaderPreviewAfterHold()') &&
  readerJs.includes('startReaderPreviewAnimation()'),
  'Reader canvas must combine live progress with a final-frame-holding full animation')
assert.ok(!previewJs.includes('animationFinalHeld') &&
  previewJs.includes('this.renderIndex = this.sequence.length - 1'),
  'Generated-result preview must retain its original independent behavior')
assert.ok(!generateWxml.includes('{{text.board}}'), 'Board-size setting must not remain in generator UI')
assert.ok(!generateJs.includes('saved.boardMm'), 'Board size must not remain a saved generator setting')
assert.ok(/onHide\(\)[\s\S]*?forceSave\(\)/.test(readerJs) && /onUnload\(\)[\s\S]*?forceSave\(\)/.test(readerJs))
assert.ok(projectsJs.includes('if (this.data.busy) return'), 'Project mutations must reject repeated taps while busy')
assert.ok(projectsJs.includes('wx.getImageInfo'), 'Imported thumbnails must be decoded before accepting SAR')
assert.ok(projectsJs.includes("extension: ['sar', 'bin']"), 'Archive picker must accept both SAR extensions')
assert.ok(projectsWxml.includes('text.fileImportHint'), 'Archive import entry must explain the chat-forward workflow')
assert.ok(projectsWxml.includes('text.fileExportHint'), 'Archive export menu must explain the chat-forward download workflow')
assert.ok(projectsWxml.includes('text.autoResume') && projectsWxml.includes('text.manualArchives'))
assert.ok(projectsWxml.includes('item.isCurrent') && storageJs.includes('b.updatedAt - a.updatedAt'))
assert.ok(
  /grid-template-columns:\s*repeat\(2,\s*minmax\(0,\s*1fr\)\)/.test(appWxss),
  'Two-column button grids must shrink within narrow screens'
)
assert.ok(
  /\.button-grid button\s*\{[\s\S]*?min-width:\s*0/.test(appWxss),
  'Grid buttons must not force their columns wider than the viewport'
)
assert.ok(
  /grid-template-columns:\s*repeat\(3,\s*minmax\(0,\s*1fr\)\)/.test(readerWxss),
  'Reader navigation must keep all three buttons visible'
)
assert.ok(
  cropJs.includes('applyGrayscalePixels()') && /filter:\s*grayscale\(100%\)/.test(cropWxss),
  'Crop preview and exported pixels must both be grayscale'
)
assert.ok(
  aboutWxml.includes('241120nzdjjx@gmail.com') &&
    aboutWxml.includes('https://b23.tv/K3Cp0ZZ') &&
    !aboutWxml.includes('Telegram') &&
    !aboutWxml.includes('Twitter'),
  'About page must contain only the requested email and Bilibili contacts'
)

console.log('Static project audit passed')
