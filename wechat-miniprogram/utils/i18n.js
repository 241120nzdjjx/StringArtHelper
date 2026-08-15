const ZH = {
  appName: '绕线助手',
  versionLabel: '微信小程序版 · v1.2.2',
  tagline: '免费 · 开源 · 纯本地运行',
  chooseImage: '选择图片并生成',
  importTxt: '导入 TXT / SAR 存档',
  fileImportHint: '上传 TXT 和 BIN 文件，需先转发至聊天，再从聊天上传。',
  fileExportHint: '下载 TXT 和 BIN 文件，需先转发至聊天，再从聊天下载。',
  continueProject: '继续当前项目',
  projects: '存档管理',
  about: '关于',
  language: 'English',
  noProject: '还没有载入绕线序列',
  currentProgress: '当前进度',
  steps: '步',
  cropTitle: '裁切图片',
  cropHint: '拖动取景 · 双指缩放',
  cropExplain: '图片会转换为保留明暗层次的灰阶图；圆框内是最终参与生成的区域，图片以外自动按白色板材处理。',
  reset: '重置',
  fitWhole: '完整放入圆内',
  next: '下一步',
  generatorTitle: '图片 → 绕线序列',
  nails: '钉子数',
  lines: '绕线步数',
  circle: '钉位圆直径（mm）',
  thread: '线径（mm）',
  autoStop: '自动防全黑',
  start: '开始生成',
  cancel: '取消',
  generating: '正在本地生成',
  generated: '生成完成',
  previewTitle: '生成结果预览',
  actualRatio: '使用实际线径比例',
  previewThread: '预览线径',
  zoom: '缩放',
  playAnimation: '播放完整动画',
  stopAnimation: '停止动画',
  animationSpeed: '动画速度',
  openReader: '载入步骤阅读器',
  saveProject: '保存项目',
  exportTxt: '导出 TXT',
  exportPdf: '生成钉位模板 PDF',
  readerTitle: '绕线步骤',
  previous: '上一步',
  previousShort: '上一步',
  previousPinShort: '前一个',
  currentShort: '当前',
  nextPinShort: '后一个',
  nextShort: '下一步',
  replay: '重播',
  play: '播放',
  pause: '暂停',
  jump: '跳转',
  current: '当前',
  previousPin: '前一个',
  nextPin: '下一个',
  offlineVoice: '离线语音播报',
  voiceSpeed: '播报语速',
  stepInterval: '步骤间隔',
  realtimePreview: '实时进度预览',
  readerCombinedPreview: '实时 / 完整动画预览',
  minimize: '最小化',
  fullscreen: '全屏',
  restore: '恢复窗口',
  showPreview: '显示实时预览',
  voiceReady: '内置中英文数字语音包 · 完全离线',
  voiceUnsupported: '当前微信基础库不支持本地音频播放，仅保留视觉步骤播放。',
  projectsTitle: '存档管理',
  emptyProjects: '暂无存档',
  open: '打开',
  rename: '重命名',
  delete: '删除',
  aboutTitle: '关于',
  wechatEdition: '微信小程序版',
  aboutIntro: '绕线助手微信小程序版，帮助你在本地完成图片裁切、绕线路径生成和步骤阅读。',
  suggestions: '改进建议',
  suggestionNote: '如果你有改进建议、遇到问题，或发现哪里用起来不顺手，欢迎发送到下面的邮箱。',
  bilibiliNote: '欢迎来看看安卓版的演示、功能和后续更新。',
  copyBilibili: '复制 Bilibili 链接',
  androidRepoNote: '安卓版 StringArtHelper 的开源源码、版本记录与发布信息。',
  copyAndroidRepo: '复制安卓版 GitHub 仓库链接',
  shareApp: '分享小程序',
  feedback: '反馈与联系',
  support: '支持作者',
  copy: '复制',
  copied: '已复制',
  done: '完成',
  error: '出现问题',
  confirmImport: '确认导入',
  recognized: '识别到',
  nailNumbers: '个钉号',
  fileSaved: '文件已生成',
  shareUnsupported: '当前环境不支持直接分享，请在真机中使用。',
  workerUnsupported: '当前基础库不支持 Worker。',
  imageLoadFailed: '图片读取失败',
  generationFailed: '生成失败',
  projectName: '项目名称',
  save: '保存',
  generatedProject: '生成项目'
  ,projectHelp: '自动续做会随进度更新；手动存档会固定保留某个节点，除非主动覆盖或删除。'
  ,currentProject: '当前项目'
  ,noCurrentProject: '尚未打开项目'
  ,saveCheckpoint: '📌 保存当前节点'
  ,importArchive: '📥 导入存档'
  ,autoResume: '🔄 自动续做'
  ,manualArchives: '📌 手动存档'
  ,emptyAuto: '暂无自动续做项目'
  ,emptyManual: '暂无手动存档'
  ,nailUnit: '钉'
  ,circleShort: '圆径'
  ,threadShort: '线径'
  ,readArchive: '读取这个存档'
  ,overwriteProgress: '用当前进度覆盖'
  ,exportArchive: '导出存档'
  ,shareArchive: '分享存档'
  ,deleteArchive: '删除这个存档'
  ,saveCheckpointTitle: '保存当前节点'
  ,importArchiveTitle: '导入存档'
  ,overwriteSame: '覆盖同名存档'
  ,keepBoth: '保留两份'
  ,later: '稍后'
  ,openNow: '立即打开'
  ,importAction: '导入'
  ,cancelAction: '取消'
  ,saveAction: '保存'
  ,currentBadge: '当前'
  ,sar2Warning: 'SAR2 不含物理尺寸，已使用兼容默认值：圆径 260 mm、线径 0.20 mm。'
}

const EN = {
  appName: 'String Art Helper',
  versionLabel: 'WeChat Mini Program · v1.2.2',
  tagline: 'Free · open source · fully offline',
  chooseImage: 'Choose image and generate',
  importTxt: 'Import TXT / SAR archive',
  fileImportHint: 'To upload TXT or BIN files, first forward them to a chat, then choose them from that chat.',
  fileExportHint: 'To download TXT or BIN files, first forward them to a chat, then download them from that chat.',
  continueProject: 'Continue current project',
  projects: 'Projects',
  about: 'About',
  language: '中文',
  noProject: 'No string sequence loaded',
  currentProgress: 'Current progress',
  steps: 'steps',
  cropTitle: 'Crop image',
  cropHint: 'Drag to frame · pinch to zoom',
  cropExplain: 'The image is converted to grayscale while preserving tonal detail. Only the circle is used for generation; areas outside the image become white board.',
  reset: 'Reset',
  fitWhole: 'Fit whole image',
  next: 'Next',
  generatorTitle: 'Image → string sequence',
  nails: 'Number of nails',
  lines: 'String steps',
  circle: 'Nail circle diameter (mm)',
  thread: 'Thread diameter (mm)',
  autoStop: 'Prevent over-darkening',
  start: 'Generate',
  cancel: 'Cancel',
  generating: 'Generating locally',
  generated: 'Generation complete',
  previewTitle: 'Generated preview',
  actualRatio: 'Use actual thread scale',
  previewThread: 'Preview thread',
  zoom: 'Zoom',
  playAnimation: 'Play full animation',
  stopAnimation: 'Stop animation',
  animationSpeed: 'Animation speed',
  openReader: 'Open step reader',
  saveProject: 'Save project',
  exportTxt: 'Export TXT',
  exportPdf: 'Generate nail-template PDF',
  readerTitle: 'Stringing steps',
  previous: 'Previous',
  previousShort: 'Prev',
  previousPinShort: 'Prev',
  currentShort: 'Now',
  nextPinShort: 'Next',
  nextShort: 'Next',
  replay: 'Replay',
  play: 'Play',
  pause: 'Pause',
  jump: 'Jump',
  current: 'Current',
  previousPin: 'Previous',
  nextPin: 'Next',
  offlineVoice: 'Offline voice',
  voiceSpeed: 'Voice speed',
  stepInterval: 'Step interval',
  realtimePreview: 'Live progress preview',
  readerCombinedPreview: 'Live / full animation preview',
  minimize: 'Minimize',
  fullscreen: 'Full screen',
  restore: 'Restore window',
  showPreview: 'Show live preview',
  voiceReady: 'Bundled Chinese and English number voices · fully offline',
  voiceUnsupported: 'This WeChat base library cannot play local audio. Visual playback remains available.',
  projectsTitle: 'Projects',
  emptyProjects: 'No projects yet',
  open: 'Open',
  rename: 'Rename',
  delete: 'Delete',
  aboutTitle: 'About',
  wechatEdition: 'WeChat Mini Program',
  aboutIntro: 'The WeChat Mini Program edition of String Art Helper for local image cropping, path generation and step-by-step reading.',
  suggestions: 'Suggestions',
  suggestionNote: 'If you have an improvement idea, encounter a problem, or find something awkward to use, please send it to the email below.',
  bilibiliNote: 'Visit Bilibili for demos, features and updates from the Android edition.',
  copyBilibili: 'Copy Bilibili link',
  androidRepoNote: 'Open-source Android code, release history and project updates for StringArtHelper.',
  copyAndroidRepo: 'Copy Android GitHub repository link',
  shareApp: 'Share',
  feedback: 'Feedback & contact',
  support: 'Support the author',
  copy: 'Copy',
  copied: 'Copied',
  done: 'Done',
  error: 'Something went wrong',
  confirmImport: 'Confirm import',
  recognized: 'Recognized',
  nailNumbers: 'nail numbers',
  fileSaved: 'File generated',
  shareUnsupported: 'Direct sharing is unavailable here. Please use a physical device.',
  workerUnsupported: 'The current base library does not support Worker.',
  imageLoadFailed: 'Could not read the image',
  generationFailed: 'Generation failed',
  projectName: 'Project name',
  save: 'Save',
  generatedProject: 'Generated project'
  ,projectHelp: 'Auto Resume follows your latest progress. Manual Archives stay at a fixed checkpoint until you explicitly overwrite or delete them.'
  ,currentProject: 'Current project'
  ,noCurrentProject: 'No project is open'
  ,saveCheckpoint: '📌 Save checkpoint'
  ,importArchive: '📥 Import archive'
  ,autoResume: '🔄 Auto Resume'
  ,manualArchives: '📌 Manual Archives'
  ,emptyAuto: 'No Auto Resume projects'
  ,emptyManual: 'No Manual Archives'
  ,nailUnit: 'nails'
  ,circleShort: 'circle'
  ,threadShort: 'thread'
  ,readArchive: 'Open this archive'
  ,overwriteProgress: 'Overwrite with current progress'
  ,exportArchive: 'Export archive'
  ,shareArchive: 'Share archive'
  ,deleteArchive: 'Delete this archive'
  ,saveCheckpointTitle: 'Save checkpoint'
  ,importArchiveTitle: 'Import archive'
  ,overwriteSame: 'Overwrite same-name archive'
  ,keepBoth: 'Keep both copies'
  ,later: 'Later'
  ,openNow: 'Open now'
  ,importAction: 'Import'
  ,cancelAction: 'Cancel'
  ,saveAction: 'Save'
  ,currentBadge: 'Current'
  ,sar2Warning: 'SAR2 has no physical dimensions. Compatibility defaults are used: 260 mm circle and 0.20 mm thread.'
}

function languagePack(language) {
  return language === 'en' ? EN : ZH
}

module.exports = { languagePack }
