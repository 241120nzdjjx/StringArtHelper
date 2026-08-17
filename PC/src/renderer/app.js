/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 *
 * Desktop renderer logic. The preview rendering mirrors the Android app's
 * GeneratedPreviewView + NailIndexRenderer on an HTML5 canvas, while the
 * window layout and drag-and-drop workflow are a fresh desktop design.
 */
'use strict';

/* ----------------------------- i18n ----------------------------- */

const I18N = {
  zh: {
    appName: '绕线助手',
    openImage: '打开图片',
    openSequence: '打开序列/存档',
    saveProject: '保存项目',
    exportTxt: '导出序列',
    exportPdf: '导出模板',
    projects: '项目',
    recent: '最近文件',
    openFolder: '打开目录',
    emptyTitle: '把图片、序列或存档拖到这里',
    emptySub: '支持 图片 / .txt 序列 / .sar · .bin 存档，与 Android、微信小程序版互通',
    chooseImage: '选择图片生成',
    chooseSequence: '打开序列 / 存档',
    cropHint: '拖动调整裁剪框 · 滚轮缩放 · 双击复位',
    playerHint: '滚轮缩放 · 拖动平移 · 空格播放/暂停',
    dropTitle: '松开鼠标导入',
    generator: '生成参数',
    nails: '钉子数',
    lines: '最大线数',
    linesNote: '自动停止时实际线数可能更少',
    circleMm: '钉位圆直径 (mm)',
    lineMm: '线径 (mm)',
    autoStop: '剩余像素耗尽时自动停止',
    generate: '生成序列',
    cancel: '取消',
    generating: '正在生成…',
    player: '播放器',
    prev: '上一步',
    next: '下一步',
    play: '播放',
    pause: '暂停',
    jump: '跳转…',
    replay: '重播当前',
    stepDelay: '步进间隔 (秒)',
    lineDisplay: '线宽显示',
    actualRatio: '实际比例',
    custom: '自定义',
    showIndices: '显示钉号',
    showAll: '显示完整效果',
    speak: '朗读钉号（系统语音）',
    saveToProjects: '存入项目列表',
    saveAs: '另存为…',
    noProject: '未打开项目',
    step: '第 {index} / {total} 步',
    estimated: '预计 {time}',
    newProject: '新生成项目',
    importProject: '导入项目',
    rename: '重命名',
    delete: '删除',
    open: '打开',
    reveal: '在资源管理器中显示',
    noProjects: '暂无项目，生成的序列会自动保存到这里',
    noRecent: '暂无最近文件',
    loading: '加载中…',
    saved: '已保存',
    savedAs: '已另存为',
    deleted: '已删除',
    renamed: '已重命名',
    error: '出错',
    cancelError: '已取消生成',
    generateDone: '生成完成：{lines} 线 · 用线 {meters} 米',
    invalidNails: '钉子数需在 100–500 之间',
    invalidCircle: '圆径需在 80–1200 mm 之间',
    invalidLine: '线径需在 0.01–1 mm 之间',
    invalidLines: '线数需在 10–100000 之间',
    noSequence: '没有可导出的序列',
    txtExported: '序列已导出',
    pdfExported: '模板已导出',
    jumpTitle: '跳转到第几步？',
    jumpPrompt: '输入 1–{total} 之间的步数：',
    language: '语言',
    done: '完成',
    mins: '{n} 分钟',
    hours: '{n} 小时 {m} 分',
    underMinute: '少于一分钟',
    stepName: '第 {n} 步',
    cannotDecode: '无法解析该文件：{msg}',
    /* player extras */
    prevPin: '前',
    nextPin: '后',
    slower: '慢一点',
    faster: '快一点',
    repeatTwice: '每个钉号重复播报两次',
    speechRate: '播报语速',
    previewAnimation: '预览动画',
    animSpeed: '动画速度',
    /* about / contact / support */
    about: '关于',
    supportAuthor: '支持作者',
    shareApp: '复制仓库链接',
    contact: '反馈联系',
    versionInfo: '版本与开源信息',
    githubRepo: 'GitHub 开源仓库',
    gotIt: '知道了',
    back: '返回',
    copied: '已复制到剪贴板',
    copiedGenshin: '已复制到剪贴板，原神启动！',
    alipay: '支付宝',
    wechatPay: '微信',
    miniProgram: '微信小程序',
    bilibili: 'Bilibili：牛杂の经济学',
    bilibiliSub: '点一下即可跳转至 bilibili',
    email: '邮箱：241120nzdjjx@gmail.com',
    emailSub: '点一下即可打开邮件客户端',
    x: '推特（X）：@nzdjjx241120',
    xSub: '点一下即可跳转至 X',
    telegram: 'Telegram：@nzdjjx',
    telegramSub: '点一下即可跳转至 Telegram',
    genshin: '或者找作者玩原神🤓☝️ UID：305028021',
    genshinSub: '点一下即可复制 UID',
    version: '版本',
    author: '作者',
    license: '许可证',
    runtime: '运行时',
    buildType: '构建类型',
    release: '发布版',
    test: '测试版',
    aboutIntro:
      '🧶 绕线助手 v{version}\n\n🔒 免费、开源、完全离线（绝对不是没钱租服务器，绝对不是）。图片、TXT 和项目存档只在你的设备上处理，不会上传任何数据。\n\n⭐ 项目源码、版本更新记录与问题反馈均可在 GitHub 仓库查看，也欢迎提出建议或参与完善。\n\n作者：牛杂の经济学 · GNU GPL v3.0 only',
    txtImportTitle: 'TXT 导入预览',
    importInfo: '钉数 {nails} · 共 {total} 个钉号',
    importLine: '线径（预览显示）',
    importCircle: '钉位圆直径',
    importAndOpen: '导入并打开',
    importHint: '若序列文件未附带物理参数，请按实际绕线的圆径和线径调整，预览线宽会随之变化。',
    rangeAdjusted: '已调整为有效范围 {min}–{max}',
    rangeAdjustedGen: '部分参数超出范围，已自动调整到有效值',
    saving: '保存中…',
    supportIntro:
      '感谢你支持这个项目继续完善。下面任选一种方式即可；如果暂时不方便赞助，去 Bilibili 点赞或投几个币也已经是很大的支持。',
    techInfo:
      '版本：{version}\n作者：牛杂の经济学\n许可证：GNU GPL v3.0 only\n运行时：Electron {electron} · Chromium {chrome} · Node {node}\n构建类型：{build}\n\n也请帮忙维护真正免费开源的版本：修改后再分发时必须保留必要的法律与作者声明，明确标注修改内容，且不得冒充官方构建。如果发现删署名、闭源倒卖或冒充官方的版本，请保留证据并联系作者。'
  },
  en: {
    appName: 'String Art Helper',
    openImage: 'Open Image',
    openSequence: 'Open Sequence/Save',
    saveProject: 'Save Project',
    exportTxt: 'Export TXT',
    exportPdf: 'Export Template',
    projects: 'Projects',
    recent: 'Recent',
    openFolder: 'Open Folder',
    emptyTitle: 'Drop an image, sequence or save here',
    emptySub: 'Images / .txt sequences / .sar · .bin saves — interoperable with the Android app and WeChat Mini Program',
    chooseImage: 'Choose an image',
    chooseSequence: 'Open sequence / save',
    cropHint: 'Drag to move the crop box · wheel to zoom · double-click to reset',
    playerHint: 'Wheel to zoom · drag to pan · Space to play/pause',
    dropTitle: 'Release to import',
    generator: 'Generator',
    nails: 'Nails',
    lines: 'Max lines',
    linesNote: 'auto-stop may produce fewer lines',
    circleMm: 'Nail circle diameter (mm)',
    lineMm: 'Thread diameter (mm)',
    autoStop: 'Auto-stop when the residual image is exhausted',
    generate: 'Generate',
    cancel: 'Cancel',
    generating: 'Generating…',
    player: 'Player',
    prev: 'Prev',
    next: 'Next',
    play: 'Play',
    pause: 'Pause',
    jump: 'Jump…',
    replay: 'Replay',
    stepDelay: 'Step delay (s)',
    lineDisplay: 'Thread display',
    actualRatio: 'Actual ratio',
    custom: 'Custom',
    showIndices: 'Show nail numbers',
    showAll: 'Show full artwork',
    speak: 'Speak nail numbers (system voice)',
    saveToProjects: 'Save to projects',
    saveAs: 'Save as…',
    noProject: 'No project',
    step: 'Step {index} / {total}',
    estimated: '~{time} left',
    newProject: 'New project',
    importProject: 'Imported project',
    rename: 'Rename',
    delete: 'Delete',
    open: 'Open',
    reveal: 'Show in Explorer',
    noProjects: 'No projects yet — generated sequences are saved here automatically',
    noRecent: 'No recent files',
    loading: 'Loading…',
    saved: 'Saved',
    savedAs: 'Saved as',
    deleted: 'Deleted',
    renamed: 'Renamed',
    error: 'Error',
    cancelError: 'Generation cancelled',
    generateDone: 'Done: {lines} lines · {meters} m of thread',
    invalidNails: 'Nails must be 100–500',
    invalidCircle: 'Circle diameter must be 80–1200 mm',
    invalidLine: 'Thread diameter must be 0.01–1 mm',
    invalidLines: 'Lines must be 10–100000',
    noSequence: 'Nothing to export',
    txtExported: 'Sequence exported',
    pdfExported: 'Template exported',
    jumpTitle: 'Jump to step',
    jumpPrompt: 'Enter a step from 1 to {total}:',
    language: 'Language',
    done: 'Done',
    mins: '{n} min',
    hours: '{n} h {m} min',
    underMinute: 'under a minute',
    stepName: 'Step {n}',
    cannotDecode: 'Cannot parse this file: {msg}',
    /* player extras */
    prevPin: 'Prev',
    nextPin: 'Next',
    slower: 'Slower',
    faster: 'Faster',
    repeatTwice: 'Speak each nail number twice',
    speechRate: 'Speech rate',
    previewAnimation: 'Preview animation',
    animSpeed: 'Animation speed',
    /* about / contact / support */
    about: 'About',
    supportAuthor: 'Support the author',
    shareApp: 'Copy repository link',
    contact: 'Feedback & contact',
    versionInfo: 'Version & open-source info',
    githubRepo: 'GitHub repository',
    gotIt: 'Got it',
    back: 'Back',
    copied: 'Copied to clipboard',
    copiedGenshin: 'UID copied to clipboard. Genshin, let’s go!',
    alipay: 'Alipay',
    wechatPay: 'WeChat',
    miniProgram: 'Mini Program',
    bilibili: 'Bilibili: 牛杂の经济学',
    bilibiliSub: 'Tap to open bilibili',
    email: 'Email: 241120nzdjjx@gmail.com',
    emailSub: 'Tap to open your mail client',
    x: 'X (Twitter): @nzdjjx241120',
    xSub: 'Tap to open X',
    telegram: 'Telegram: @nzdjjx',
    telegramSub: 'Tap to open Telegram',
    genshin: 'Or find the author in Genshin 🤓☝️ UID: 305028021',
    genshinSub: 'Tap to copy the UID',
    version: 'Version',
    author: 'Author',
    license: 'License',
    runtime: 'Runtime',
    buildType: 'Build',
    release: 'release',
    test: 'test',
    aboutIntro:
      '🧶 String Art Helper v{version}\n\n🔒 Free, open source and fully offline. Images, TXT files and project saves are processed only on your device and are never uploaded.\n\n⭐ Source code, release notes and issue reporting are available in our GitHub repository. Contributions and suggestions are welcome.\n\nAuthor: 牛杂の经济学 · GNU GPL v3.0 only',
    txtImportTitle: 'TXT Import Preview',
    importInfo: '{nails} nails · {total} pins total',
    importLine: 'Thread diameter (preview)',
    importCircle: 'Nail circle diameter',
    importAndOpen: 'Import & open',
    importHint: 'If the sequence file has no physical parameters attached, adjust the circle diameter and thread diameter to your actual board; the preview updates live.',
    rangeAdjusted: 'Adjusted to the valid range {min}–{max}',
    rangeAdjustedGen: 'Some values were out of range and were adjusted',
    saving: 'Saving…',
    supportIntro:
      'Thank you for supporting continued development. Choose either QR code below. If donating is not convenient, visiting Bilibili and leaving a like or a few coins is already a big help.',
    techInfo:
      'Version: {version}\nAuthor: 牛杂の经济学\nLicense: GNU GPL v3.0 only\nRuntime: Electron {electron} · Chromium {chrome} · Node {node}\nBuild: {build}\n\nPlease help protect the free open-source edition. Modified distributions must retain the required legal notices, clearly disclose modifications and never impersonate an official build. If you find an unattributed copy, a closed-source resale or a fake official version, please save evidence and contact the author.'
  }
};

/* ----------------------------- helpers ----------------------------- */

function $(id) {
  return document.getElementById(id);
}

function t(key, vars) {
  let text = (I18N[state.lang] && I18N[state.lang][key]) || I18N.zh[key] || key;
  if (vars) {
    Object.keys(vars).forEach((k) => {
      text = text.replace(new RegExp('\\{' + k + '\\}', 'g'), String(vars[k]));
    });
  }
  return text;
}

function applyI18n() {
  document.documentElement.lang = state.lang;
  document.querySelectorAll('[data-i18n]').forEach((el) => {
    const key = el.getAttribute('data-i18n');
    el.textContent = t(key);
  });
  document.querySelectorAll('title').forEach((el) => {
    el.textContent = t('appName') + ' · String Art Helper (PC)';
  });
  $('btn-language').textContent = state.lang === 'zh' ? 'EN' : '中文';
  refreshPlayerLabels();
}

function toast(message, ms) {
  const el = $('toast');
  el.textContent = message;
  el.classList.remove('hidden');
  clearTimeout(toast._timer);
  toast._timer = setTimeout(() => el.classList.add('hidden'), ms || 2600);
}

function clamp(v, min, max) {
  return Math.max(min, Math.min(max, v));
}

/**
 * Enforce min/max limits on a numeric input: on blur/change an out-of-range
 * value is corrected to the nearest bound and the user is told, instead of
 * silently accepting nonsense (matches Android's range validation behaviour).
 */
function bindNumericLimits(inputId, min, max, decimals) {
  const input = $(inputId);
  const apply = () => {
    let value = Number(input.value);
    if (!Number.isFinite(value)) {
      input.value = decimals != null ? min.toFixed(decimals) : String(min);
      return;
    }
    if (value < min || value > max) {
      const corrected = clamp(value, min, max);
      input.value = decimals != null ? corrected.toFixed(decimals) : String(Math.round(corrected));
      toast(t('rangeAdjusted', { min, max }));
    }
  };
  input.addEventListener('blur', apply);
  input.addEventListener('change', apply);
  return apply;
}

/* Estimated remaining time, ported from Android MainActivity.formatEstimatedTime
 * ((ms + 59999) / 60000 is a ceiling division; below a minute shows "under a minute"). */
function formatEstimatedTime(ms) {
  if (ms < 60000) return t('underMinute');
  const minutes = Math.max(1, Math.floor((ms + 59999) / 60000));
  if (minutes < 60) return t('mins', { n: minutes });
  const hours = Math.floor(minutes / 60);
  const remainder = minutes % 60;
  return t('hours', { n: hours, m: remainder });
}

/* ----------------------------- state ----------------------------- */

const state = {
  lang: localStorage.getItem('sah_lang') || 'zh',
  mode: 'empty',
  project: null, // { name, importedFileName, index, nails, circleMm, lineMm, sequence, thumbnail, filePath, autoSaveTimer }
  crop: null,    // { img, width, height, cropX, cropY, zoom, minZoom }
  gen: {
    nails: 220,
    requestedLines: 4000,
    circleMm: 260,
    lineMm: 0.2,
    autoStop: true
  },
  player: {
    playing: false,
    timer: null,
    delayMs: 2500,
    customLineMm: parseFloat(localStorage.getItem('sah_custom_line') || '0.10'),
    useActualRatio: localStorage.getItem('sah_line_actual') !== '0',
    showIndices: localStorage.getItem('sah_indices') !== '0',
    showAll: localStorage.getItem('sah_show_all') === '1',
    speak: localStorage.getItem('sah_speak') === '1',
    repeat: localStorage.getItem('sah_repeat') === '1',
    rate: parseFloat(localStorage.getItem('sah_rate') || '0.9'),
    animating: false,
    animRaf: null,
    animHoldTimer: null,
    animIndex: -1,          // animation render index; -1 = no animation / released
    animWasPlaying: false,
    animSpeed: clamp(parseInt(localStorage.getItem('sah_anim_speed') || '6', 10) || 6, 1, 20),
    threadLayer: null,       // offscreen layer caching completed chords
    threadLayerKey: '',
    threadLayerIndex: 0,
    zoom: 1,
    panX: 0,
    panY: 0,
    progressSequence: null,
    reveal: 0
  },
  info: null,
  recent: [],
  importing: false
};

let playerCanvas = null;
let playerCtx = null;
let cropCanvas = null;
let cropCtx = null;
let lastFrame = 0;
let resizeObserver = null;

/* ============================ PREVIEW ============================ */

/* Preview metrics reused 1:1 from the WeChat Mini Program
 * (utils/preview-metrics.js, same project, GPL-3.0-only). */
const PREVIEW_BASE_RADIUS_RATIO = 0.45;
const PREVIEW_MIN_ZOOM = 1;
const PREVIEW_MAX_ZOOM = 5;
const THREAD_PATH_BATCH = 32;
/** Offscreen chord layer size; radius = 1080 * 0.45 ≈ 486 px (phone-equivalent). */
const THREAD_LAYER_SIZE = 1080;

function clampZoomPreview(value) {
  return clamp(Number(value) || 1, PREVIEW_MIN_ZOOM, PREVIEW_MAX_ZOOM);
}

function maxPanPreview(side, zoom, edgeAllowance) {
  const baseRadius = Math.max(0, Number(side) || 0) * PREVIEW_BASE_RADIUS_RATIO;
  const radius = baseRadius * clampZoomPreview(zoom);
  const edge = Math.max(0, Number(edgeAllowance) || 12);
  return Math.max(edge, radius - baseRadius + edge);
}

function threadMetrics(radius, side, lineMm, circleMm) {
  const physicalRatio =
    Math.max(0.01, Number(lineMm) || 0.1) / Math.max(1, Number(circleMm) || 1);
  return {
    physicalRatio,
    stroke: Math.max(0.12, 2 * radius * physicalRatio),
    alpha: clamp(Math.round(26 + side * physicalRatio * 90), 26, 82) / 255
  };
}

function nailMetrics(nails, radius) {
  const count = Math.max(2, Number(nails) || 2);
  const arc = (Math.PI * 2 * radius) / count;
  const baseText = clamp(arc * 0.72, 3, 6);
  const dotRadius = clamp(arc * 0.14, 0.8, 2.2);
  return {
    arc,
    baseText,
    dotRadius,
    labelRadius: radius + dotRadius + Math.max(2, baseText * 0.62)
  };
}

function canvasSize(canvas) {
  const dpr = window.devicePixelRatio || 1;
  const rect = canvas.getBoundingClientRect();
  const width = Math.max(1, Math.round(rect.width * dpr));
  const height = Math.max(1, Math.round(rect.height * dpr));
  if (canvas.width !== width || canvas.height !== height) {
    canvas.width = width;
    canvas.height = height;
    return { width, height, resized: true };
  }
  return { width, height, resized: false };
}

function pinPoint(pin, nails, cx, cy, r) {
  const angle = (Math.PI * 2 * pin) / nails;
  return { x: cx + Math.cos(angle) * r, y: cy + Math.sin(angle) * r };
}

function drawPlayerFrame() {
  const canvas = playerCanvas;
  if (!canvas) return;
  const p = state.player;
  const generating = !!p.progressSequence;
  const seq = p.progressSequence || (state.project && state.project.sequence);
  if (!seq || seq.length === 0) return;
  if (!generating && state.mode !== 'player') return;

  // During generation the project object does not exist yet, so fall back to
  // the generator parameters for the ring geometry.
  const project = state.project || {
    nails: state.gen.nails,
    circleMm: state.gen.circleMm,
    lineMm: state.gen.lineMm
  };
  const { width: w, height: h } = canvasSize(canvas);
  const ctx = playerCtx;
  const dpr = window.devicePixelRatio || 1;

  // Rendering mirrors the WeChat Mini Program preview page:
  // CSS-space drawing scaled by pixelRatio; threadMetrics for stroke/alpha;
  // chords stroked in batches of 32 with butt caps and miter joins.
  const side = Math.min(w, h) / dpr;
  const centerX = side * 0.5 + p.panX / dpr;
  const centerY = side * 0.5 + p.panY / dpr;
  const radius = side * PREVIEW_BASE_RADIUS_RATIO * p.zoom;
  const lineMm = p.useActualRatio ? project.lineMm : p.customLineMm;
  const physicalRatio =
    Math.max(0.01, Number(lineMm) || 0.1) / Math.max(1, Number(project.circleMm) || 1);
  // Android StringArtPreview port: completed chords use a fixed alpha of 54
  // and a stroke proportional to the ring radius. The stroke is normalised to
  // a typical phone ring (~480 px) so 0.1 mm stays visible on a dpr=1 desktop
  // instead of ~0.12 px; the offscreen layer blit keeps it constant at any zoom.
  const thread = {
    stroke: Math.max(0.12, 2 * 480 * physicalRatio * p.zoom),
    alpha: 54 / 255
  };

  // Live progress preview (Android StringArtPreview semantics): only the
  // chords up to the current step are drawn; the current chord is accented.
  // The preview animation uses its own render index so the real project
  // progress stays untouched.
  let renderIndex; // array index of the "current" pin (chord count = index)
  if (generating) {
    renderIndex = Math.min(p.reveal || seq.length, seq.length) - 1;
  } else if (p.animIndex >= 0) {
    renderIndex = p.animIndex; // animation in progress or result held
  } else if (p.showAll) {
    renderIndex = seq.length - 1; // full finished artwork
  } else {
    renderIndex = clamp(project.index || 0, 0, seq.length - 1);
  }

  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  // Fill the WHOLE (rectangular, landscape) canvas — the Mini Program canvas
  // is square so it only clears side×side; on desktop the uncovered strips
  // would stay black and panning would leave stray chords there.
  ctx.fillStyle = '#f8f7fb';
  ctx.fillRect(0, 0, w / dpr, h / dpr);

  // Completed chords come from an offscreen layer (Android GenerationProgressView
  // bufferCanvas + Mini Program drawCachedThreads): the layer is appended to
  // incrementally and zoom/pan only blits it, so large sequences stay smooth.
  const layer = ensureThreadLayer(seq, project, lineMm, renderIndex, side);
  if (layer) {
    // The layer is a THREAD_LAYER_SIZE square whose ring radius is 0.45 × side;
    // blit it so the ring lands exactly on the live radius (not 0.9× it).
    const blitSize = (radius * 2) / (2 * PREVIEW_BASE_RADIUS_RATIO);
    ctx.drawImage(
      layer,
      0, 0, THREAD_LAYER_SIZE, THREAD_LAYER_SIZE,
      centerX - blitSize / 2, centerY - blitSize / 2, blitSize, blitSize
    );
  }

  // Highlight the current (active) chord.
  if (renderIndex >= 1 && renderIndex < seq.length) {
    const pa = pinPoint(seq[renderIndex - 1], project.nails, centerX, centerY, radius);
    const pb = pinPoint(seq[renderIndex], project.nails, centerX, centerY, radius);
    ctx.strokeStyle = 'rgba(151,105,255,0.95)';
    ctx.lineWidth = Math.max(thread.stroke * 1.5, 1.5);
    ctx.lineCap = 'round';
    ctx.beginPath();
    ctx.moveTo(pa.x, pa.y);
    ctx.lineTo(pb.x, pb.y);
    ctx.stroke();
    ctx.lineCap = 'butt';
  }

  // Rim circle.
  ctx.beginPath();
  ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
  ctx.strokeStyle = '#333';
  ctx.lineWidth = 1.5;
  ctx.stroke();

  const activePin = seq[clamp(renderIndex, 0, seq.length - 1)];
  drawNailRing(ctx, project.nails, centerX, centerY, radius, p.showIndices, activePin);

  // Bottom progress caption (matches Android's in-canvas "第 X / N 步").
  if (!generating) {
    ctx.fillStyle = 'rgba(80,80,92,0.95)';
    ctx.font = '13px sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'alphabetic';
    ctx.fillText(t('step', { index: renderIndex + 1, total: seq.length }), side / 2, side - 8);
  }
}

/**
 * Offscreen layer that caches the completed chords up to renderIndex.
 * New chords are appended in batches (Android GenerationProgressView style);
 * stepping back rebuilds the layer. Blitting this layer on zoom/pan makes
 * large sequences render smoothly.
 */
function ensureThreadLayer(seq, project, lineMm, renderIndex, side) {
  const p = state.player;
  // Blit correction: the layer is drawn at THREAD_LAYER_SIZE and scaled to the
  // live radius, so its stroke is pre-scaled to stay equivalent to a typical
  // phone ring radius (≈480 px) at ANY zoom level.
  const EQUIV_RADIUS_PX = 480;
  const key = project.nails + ':' + project.circleMm + ':' + lineMm.toFixed(4) + ':' + Math.round(side);
  let layer = p.threadLayer;
  if (!layer || p.threadLayerKey !== key) {
    layer = document.createElement('canvas');
    layer.width = THREAD_LAYER_SIZE;
    layer.height = THREAD_LAYER_SIZE;
    p.threadLayer = layer;
    p.threadLayerKey = key;
    p.threadLayerIndex = 0;
  }
  if (renderIndex < p.threadLayerIndex) {
    // User stepped back: rebuild from scratch.
    p.threadLayerIndex = 0;
  }
  const ctx = layer.getContext('2d');
  if (p.threadLayerIndex === 0) {
    ctx.fillStyle = '#f8f7fb';
    ctx.fillRect(0, 0, THREAD_LAYER_SIZE, THREAD_LAYER_SIZE);
  }
  const center = THREAD_LAYER_SIZE / 2;
  const radiusCache = THREAD_LAYER_SIZE * PREVIEW_BASE_RADIUS_RATIO;
  const ratio = Math.max(0.01, Number(lineMm) || 0.1) / Math.max(1, Number(project.circleMm) || 1);
  ctx.strokeStyle = 'rgba(18,18,18,0.21176470588235294)'; // alpha 54, Android StringArtPreview
  ctx.lineWidth = Math.max(0.12, 2 * EQUIV_RADIUS_PX * ratio * (THREAD_LAYER_SIZE / Math.max(1, side)));
  ctx.lineCap = 'butt';
  ctx.lineJoin = 'miter';
  for (let first = Math.max(1, p.threadLayerIndex); first < renderIndex; first += THREAD_PATH_BATCH) {
    const last = Math.min(renderIndex - 1, first + THREAD_PATH_BATCH - 1);
    ctx.beginPath();
    for (let i = first; i <= last; i++) {
      const a = seq[i - 1];
      const b = seq[i];
      if (a < 0 || a >= project.nails || b < 0 || b >= project.nails) continue;
      const pa = pinPoint(a, project.nails, center, center, radiusCache);
      const pb = pinPoint(b, project.nails, center, center, radiusCache);
      ctx.moveTo(pa.x, pa.y);
      ctx.lineTo(pb.x, pb.y);
    }
    ctx.stroke();
  }
  p.threadLayerIndex = renderIndex;
  return layer;
}

/**
 * Nail dots and radially oriented indices, ported from the Mini Program's
 * preview page (utils/preview-metrics.js + pages/preview/preview.js).
 */
function drawNailRing(ctx, nails, cx, cy, radius, showIndices, activePin) {
  if (nails < 2 || radius <= 0) return;
  const metrics = nailMetrics(nails, radius);
  const base = metrics.baseText;
  const dotRadius = metrics.dotRadius;
  const labelRadius = metrics.labelRadius;
  ctx.textBaseline = 'middle';

  for (let i = 0; i < nails; i++) {
    const angle = (Math.PI * 2 * i) / nails;
    const cos = Math.cos(angle);
    const sin = Math.sin(angle);
    ctx.beginPath();
    ctx.arc(cx + cos * radius, cy + sin * radius, dotRadius, 0, Math.PI * 2);
    ctx.fillStyle = i === activePin ? '#9769FF' : '#55555f';
    ctx.fill();
    if (!showIndices) continue;
    const size = i % 10 === 0 ? base * 2 : i % 5 === 0 ? base * 1.5 : base;
    ctx.save();
    ctx.translate(cx + cos * labelRadius, cy + sin * labelRadius);
    if (cos < 0) {
      ctx.rotate(angle + Math.PI);
      ctx.textAlign = 'right';
    } else {
      ctx.rotate(angle);
      ctx.textAlign = 'left';
    }
    ctx.fillStyle = '#42424c';
    ctx.font = (i % 10 === 0 ? '700 ' : '500 ') + size.toFixed(2) + 'px sans-serif';
    ctx.fillText(String(i), 0, 0);
    ctx.restore();
  }
}

function requestPlayerFrame() {
  const now = performance.now();
  if (now - lastFrame < 33) return;
  lastFrame = now;
  drawPlayerFrame();
}

/* ============================ CROP VIEW ============================ */

function enterCrop(img) {
  state.crop = {
    img,
    width: img.naturalWidth,
    height: img.naturalHeight,
    cropX: 0.5,
    cropY: 0.5,
    zoom: 1
  };
  setMode('crop');
  drawCropFrame();
}

function cropGeometry(crop, canvasW, canvasH) {
  const iw = crop.width;
  const ih = crop.height;
  const scale = Math.min(canvasW / iw, canvasH / ih);
  const dw = iw * scale;
  const dh = ih * scale;
  const ox = (canvasW - dw) / 2;
  const oy = (canvasH - dh) / 2;
  // cropZoom: crop box size = min(w,h)/zoom of the SOURCE image.
  const minSide = Math.min(iw, ih);
  const boxPx = (minSide / crop.zoom) * scale;
  const boxX = ox + crop.cropX * iw * scale - boxPx / 2;
  const boxY = oy + crop.cropY * ih * scale - boxPx / 2;
  return { scale, dw, dh, ox, oy, boxPx, boxX, boxY };
}

function drawCropFrame() {
  const canvas = cropCanvas;
  if (!canvas || !state.crop) return;
  const { width: w, height: h } = canvasSize(canvas);
  const ctx = cropCtx;
  const crop = state.crop;

  ctx.setTransform(1, 0, 0, 1, 0, 0);
  ctx.fillStyle = '#0a0a0f';
  ctx.fillRect(0, 0, w, h);

  const g = cropGeometry(crop, w, h);
  ctx.drawImage(crop.img, g.ox, g.oy, g.dw, g.dh);

  // Dim outside the crop box.
  ctx.fillStyle = 'rgba(10,10,15,0.62)';
  ctx.fillRect(0, 0, w, g.boxY);
  ctx.fillRect(0, g.boxY + g.boxPx, w, h - g.boxY - g.boxPx);
  ctx.fillRect(0, g.boxY, g.boxX, g.boxPx);
  ctx.fillRect(g.boxX + g.boxPx, g.boxY, w - g.boxX - g.boxPx, g.boxPx);

  // Crop box border + corner marks.
  ctx.strokeStyle = '#9769FF';
  ctx.lineWidth = 2;
  ctx.strokeRect(g.boxX, g.boxY, g.boxPx, g.boxPx);
  const m = 12;
  ctx.lineWidth = 3;
  ctx.beginPath();
  [
    [g.boxX, g.boxY, 1, 1],
    [g.boxX + g.boxPx, g.boxY, -1, 1],
    [g.boxX, g.boxY + g.boxPx, 1, -1],
    [g.boxX + g.boxPx, g.boxY + g.boxPx, -1, -1]
  ].forEach(([x, y, sx, sy]) => {
    ctx.moveTo(x, y + sy * m);
    ctx.lineTo(x, y);
    ctx.lineTo(x + sx * m, y);
  });
  ctx.stroke();
}

function setupCropInteractions() {
  let dragging = false;
  let lastX = 0;
  let lastY = 0;

  cropCanvas.addEventListener('mousedown', (event) => {
    dragging = true;
    lastX = event.clientX;
    lastY = event.clientY;
  });
  window.addEventListener('mousemove', (event) => {
    if (!dragging || !state.crop) return;
    const dx = event.clientX - lastX;
    const dy = event.clientY - lastY;
    lastX = event.clientX;
    lastY = event.clientY;
    const { width: w, height: h } = canvasSize(cropCanvas);
    const g = cropGeometry(state.crop, w, h);
    if (g.dw > 0) state.crop.cropX = clamp(state.crop.cropX + dx / g.dw, 0, 1);
    if (g.dh > 0) state.crop.cropY = clamp(state.crop.cropY + dy / g.dh, 0, 1);
    drawCropFrame();
  });
  window.addEventListener('mouseup', () => {
    dragging = false;
  });
  cropCanvas.addEventListener('wheel', (event) => {
    event.preventDefault();
    if (!state.crop) return;
    const factor = event.deltaY < 0 ? 1.1 : 1 / 1.1;
    state.crop.zoom = clamp(state.crop.zoom * factor, 1, 4);
    drawCropFrame();
  }, { passive: false });
  cropCanvas.addEventListener('dblclick', () => {
    if (!state.crop) return;
    state.crop.cropX = 0.5;
    state.crop.cropY = 0.5;
    state.crop.zoom = 1;
    drawCropFrame();
  });
}

/* ============================ GENERATION ============================ */

function validateGenParams() {
  const g = state.gen;
  const rawNails = Math.round(Number($('inp-nails').value) || 0);
  const rawLines = Math.round(Number($('inp-lines').value) || 0);
  const rawCircle = Math.round(Number($('inp-circle').value) || 0);
  const rawLine = Number($('inp-line').value) || 0;
  let adjusted = false;
  g.nails = clamp(rawNails, 100, 500);
  g.requestedLines = clamp(rawLines, 10, 20000);
  g.circleMm = clamp(rawCircle, 80, 1200);
  g.lineMm = clamp(rawLine, 0.01, 1);
  adjusted =
    g.nails !== rawNails ||
    g.requestedLines !== rawLines ||
    g.circleMm !== rawCircle ||
    g.lineMm !== rawLine;
  g.autoStop = $('inp-autostop').checked;
  $('inp-nails').value = g.nails;
  $('inp-lines').value = g.requestedLines;
  $('inp-circle').value = g.circleMm;
  $('inp-line').value = g.lineMm.toFixed(2);
  if (adjusted) toast(t('rangeAdjustedGen'));
  return g;
}

function imageToDownscaledPixels(img, maxSide) {
  const scale = Math.min(1, maxSide / Math.max(img.naturalWidth, img.naturalHeight));
  const w = Math.max(1, Math.round(img.naturalWidth * scale));
  const h = Math.max(1, Math.round(img.naturalHeight * scale));
  const canvas = document.createElement('canvas');
  canvas.width = w;
  canvas.height = h;
  const ctx = canvas.getContext('2d', { willReadFrequently: true });
  ctx.imageSmoothingEnabled = true;
  ctx.imageSmoothingQuality = 'high';
  ctx.drawImage(img, 0, 0, w, h);
  return { pixels: ctx.getImageData(0, 0, w, h).data, width: w, height: h };
}

async function startGeneration() {
  validateGenParams();
  if (!state.crop) return;
  const g = state.gen;

  setMode('generating');
  state.player.progressSequence = [0];
  state.player.reveal = 1;
  state.player.playing = false;
  stopAllPlayback();

  const downscaled = imageToDownscaledPixels(state.crop.img, 1024);
  const unsub = window.api.onGenerateProgress((payload) => {
    const total = Math.max(1, payload.total || 1);
    const pct = clamp(Math.round((payload.complete / total) * 100), 0, 100);
    $('gen-progress-bar').style.width = pct + '%';
    $('gen-progress-text').textContent = payload.complete + ' / ' + total;
    if (payload.sequence && state.player.progressSequence) {
      state.player.progressSequence = payload.sequence;
      state.player.reveal = payload.sequence.length;
      if (state.mode === 'generating') drawPlayerFrame();
    }
  });

  try {
    const result = await window.api.generate({
      pixels: downscaled.pixels,
      width: downscaled.width,
      height: downscaled.height,
      cropX: state.crop.cropX,
      cropY: state.crop.cropY,
      cropZoom: state.crop.zoom,
      pinCount: g.nails,
      requestedLines: g.requestedLines,
      circleMm: g.circleMm,
      lineMm: g.lineMm,
      autoStop: g.autoStop
    });
    if (!result || !result.sequence || result.sequence.length < 2) {
      toast(t('cancelError') || t('error'));
      setMode('empty');
      return;
    }
    state.project = {
      name: t('newProject') + ' · ' + new Date().toLocaleString(),
      importedFileName: '图片生成',
      index: 0,
      nails: g.nails,
      circleMm: g.circleMm,
      lineMm: g.lineMm,
      sequence: result.sequence,
      thumbnail: null,
      filePath: null,
      inProjectList: false
    };
    state.player.progressSequence = null;
    state.player.reveal = result.sequence.length;
    state.player.zoom = 1;
    state.player.panX = 0;
    state.player.panY = 0;
    setMode('player');
    toast(t('generateDone', { lines: result.lines, meters: result.threadMeters.toFixed(2) }));
    scheduleAutoSave(500);
    updateStatusBar();
  } catch (error) {
    console.error(error);
    toast(t('error') + ': ' + (error && error.message));
    setMode('crop');
  } finally {
    unsub();
  }
}

/* ============================ PLAYER ============================ */

function stopPlayTimer() {
  if (state.player.timer) {
    clearTimeout(state.player.timer);
    state.player.timer = null;
  }
  state.player.playing = false;
  refreshPlayButton();
}

/**
 * Fast visual replay from the current step to the end, mirroring Android's
 * startFullPreviewAnimation: the animation uses its own render index and
 * never moves the real project progress. When it reaches the end the result
 * stays visible (held) for a moment; any user action releases it and the
 * preview returns to the real current step.
 */
function startPreviewAnimation() {
  if (!state.project || state.project.sequence.length < 2) return;
  if (state.player.animIndex >= 0) {
    // Already animating or holding a result: stop and release it.
    stopPreviewAnimation();
    releaseHeldPreviewResult();
    return;
  }
  state.player.animWasPlaying = state.player.playing;
  stopPlayTimer();
  state.player.animating = true;
  state.player.animIndex = clamp(state.project.index, 0, state.project.sequence.length - 1);
  $('btn-animate').classList.add('active');
  state.player.animRaf = setTimeout(animatePreviewFrame, 34);
}

function animatePreviewFrame() {
  if (!state.player.animating || !state.project) {
    stopPreviewAnimation();
    return;
  }
  const last = state.project.sequence.length - 1;
  if (state.player.animIndex >= last) {
    // Hold the finished artwork for a beat (Android's 1 s holding delay).
    state.player.animating = false;
    state.player.animHoldTimer = setTimeout(() => stopPreviewAnimation(), 1000);
    return;
  }
  // Frame pacing reused from the Mini Program preview animation.
  const targetFrames = Math.max(24, 145 - state.player.animSpeed * 5);
  const batch = Math.max(1, Math.ceil(last / targetFrames));
  state.player.animIndex = Math.min(last, state.player.animIndex + batch);
  drawPlayerFrame();
  state.player.animRaf = setTimeout(animatePreviewFrame, 34);
}

/** End the animation but keep the last frame visible (result held). */
function stopPreviewAnimation() {
  state.player.animating = false;
  if (state.player.animRaf) {
    clearTimeout(state.player.animRaf);
    state.player.animRaf = null;
  }
  if (state.player.animHoldTimer) {
    clearTimeout(state.player.animHoldTimer);
    state.player.animHoldTimer = null;
  }
  const button = $('btn-animate');
  if (button) button.classList.remove('active');
}

/** Release a held animation result and return to the real progress. */
function releaseHeldPreviewResult() {
  if (state.player.animIndex < 0) return;
  state.player.animIndex = -1;
  drawPlayerFrame();
}

/** Fully leave the animation state (used when the user takes an action). */
function cancelAnimationForUserAction() {
  stopPreviewAnimation();
  releaseHeldPreviewResult();
}

function stopAllPlayback() {
  stopPlayTimer();
  stopPreviewAnimation();
  releaseHeldPreviewResult();
}

function startPlayTimer() {
  stopPlayTimer();
  state.player.playing = true;
  refreshPlayButton();
  tickPlay();
}

function tickPlay() {
  if (!state.player.playing || !state.project) return;
  if (state.project.index >= state.project.sequence.length - 1) {
    stopPlayTimer();
    return;
  }
  state.project.index += 1;
  afterStep();
  state.player.timer = setTimeout(tickPlay, state.player.delayMs);
}

function speakPin(pin) {
  if (!state.player.speak || !window.speechSynthesis) return;
  const text = state.lang === 'zh' ? window.api.chineseNumber(pin) : String(pin);
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = state.lang === 'zh' ? 'zh-CN' : 'en-US';
  utterance.rate = state.player.rate;
  speechSynthesis.cancel();
  speechSynthesis.speak(utterance);
  if (state.player.repeat) speechSynthesis.speak(new SpeechSynthesisUtterance(text));
}

function afterStep() {
  updatePlayerInfo();
  drawPlayerFrame();
  scheduleAutoSave(4000);
  speakPin(state.project.sequence[state.project.index]);
}

function moveStep(delta) {
  if (!state.project || !state.project.sequence.length) return;
  cancelAnimationForUserAction();
  const target = clamp(state.project.index + delta, 0, state.project.sequence.length - 1);
  if (target === state.project.index) return;
  state.project.index = target;
  afterStep();
}

function replayCurrent() {
  if (!state.project) return;
  cancelAnimationForUserAction();
  speakPin(state.project.sequence[state.project.index]);
  drawPlayerFrame();
}

function jumpToStep() {
  if (!state.project) return;
  cancelAnimationForUserAction();
  const total = state.project.sequence.length;
  let applied = false;
  const apply = () => {
    if (applied) return;
    const input = $('jump-input');
    const value = Number(input && input.value);
    if (!Number.isFinite(value)) return;
    applied = true;
    state.project.index = clamp(Math.round(value) - 1, 0, total - 1);
    closeModal();
    afterStep();
  };
  showModal(
    logoTitle(t('jumpTitle')),
    (body) => {
      const note = document.createElement('div');
      note.className = 'modal-note';
      note.textContent = t('jumpPrompt', { total });
      body.appendChild(note);
      const input = document.createElement('input');
      input.id = 'jump-input';
      input.type = 'number';
      input.min = 1;
      input.max = total;
      input.step = 1;
      input.value = String(state.project.index + 1);
      input.className = 'jump-input';
      input.addEventListener('keydown', (event) => {
        if (event.key === 'Enter') apply();
      });
      input.addEventListener('blur', () => {
        const value = Number(input.value);
        if (Number.isFinite(value)) {
          input.value = String(clamp(Math.round(value), 1, total));
        }
      });
      body.appendChild(input);
      setTimeout(() => {
        input.focus();
        input.select();
      }, 50);
    },
    null,
    () => [
      { label: t('cancel'), onClick: closeModal },
      { label: t('jump'), onClick: apply, primary: true }
    ]
  );
}

function refreshPlayButton() {
  $('btn-play').textContent = state.player.playing ? t('pause') : t('play');
}

function refreshPlayerLabels() {
  $('btn-line-actual').textContent = t('actualRatio');
  $('btn-line-custom').textContent = t('custom');
  const active = state.player.useActualRatio ? 'btn-line-actual' : 'btn-line-custom';
  $('btn-line-actual').classList.toggle('active', active === 'btn-line-actual');
  $('btn-line-custom').classList.toggle('active', active === 'btn-line-custom');
  $('inp-custom-line').value = Math.round(state.player.customLineMm * 100);
  $('inp-custom-line-number').value = state.player.customLineMm.toFixed(2);
  $('custom-line-wrap').classList.toggle('hidden', state.player.useActualRatio);
}

function updatePlayerInfo() {
  if (!state.project) return;
  const total = state.project.sequence.length;
  const index = state.project.index;
  const seq = state.project.sequence;
  $('player-number').textContent = String(seq[index]);
  $('player-prev').textContent = index >= 1 ? String(seq[index - 1]) : '–';
  $('player-next').textContent = index < total - 1 ? String(seq[index + 1]) : '–';
  const remaining = Math.max(0, total - 1 - index);
  const estimate = formatEstimatedTime(remaining * state.player.delayMs);
  $('player-progress').textContent = t('step', { index: index + 1, total }) + ' · ' + t('estimated', { time: estimate });
}

/* ============================ PAN / ZOOM ============================ */

function setupPlayerInteractions() {
  let panning = false;
  let lastX = 0;
  let lastY = 0;
  const dpr = () => window.devicePixelRatio || 1;

  playerCanvas.addEventListener('mousedown', (event) => {
    if (event.button !== 0) return;
    panning = true;
    lastX = event.clientX;
    lastY = event.clientY;
  });
  window.addEventListener('mousemove', (event) => {
    if (!panning) return;
    state.player.panX += (event.clientX - lastX) * dpr();
    state.player.panY += (event.clientY - lastY) * dpr();
    lastX = event.clientX;
    lastY = event.clientY;
    clampPan();
    requestPlayerFrame();
  });
  window.addEventListener('mouseup', () => {
    panning = false;
  });
  playerCanvas.addEventListener('wheel', (event) => {
    event.preventDefault();
    const factor = event.deltaY < 0 ? 1.12 : 1 / 1.12;
    const next = clamp(state.player.zoom * factor, 1, 5);
    if (next === state.player.zoom) return;
    // Zoom around the cursor.
    const rect = playerCanvas.getBoundingClientRect();
    const mx = (event.clientX - rect.left) * dpr();
    const my = (event.clientY - rect.top) * dpr();
    const { width: w, height: h } = canvasSize(playerCanvas);
    const ratio = next / state.player.zoom;
    state.player.panX = (mx - w / 2) - ratio * (mx - w / 2 - state.player.panX);
    state.player.panY = (my - h / 2) - ratio * (my - h / 2 - state.player.panY);
    state.player.zoom = next;
    clampPan();
    requestPlayerFrame();
  }, { passive: false });
}

function clampPan() {
  if (!state.project) return;
  const canvas = playerCanvas;
  const { width: w, height: h } = canvasSize(canvas);
  const side = Math.min(w, h);
  const baseRadius = side * 0.45;
  const radius = baseRadius * state.player.zoom;
  const edgeAllowance = 12 * (window.devicePixelRatio || 1);
  // Android clamps pan to (radius - baseRadius + edge), which at zoom=1 only
  // allows ~12 px of movement. On a wide desktop canvas users want to shift
  // the ring around at minimum zoom too, so add a generous extra allowance.
  const extra = baseRadius * 0.45;
  const maxPan = Math.max(edgeAllowance, radius - baseRadius + edgeAllowance + extra);
  state.player.panX = clamp(state.player.panX, -maxPan, maxPan);
  state.player.panY = clamp(state.player.panY, -maxPan, maxPan);
}

/* ============================ PROJECTS ============================ */

async function refreshProjectList() {
  try {
    const list = await window.api.listProjects();
    const container = $('project-list');
    container.innerHTML = '';
    if (!list.length) {
      const empty = document.createElement('div');
      empty.className = 'side-empty';
      empty.textContent = t('noProjects');
      container.appendChild(empty);
      return;
    }
    list.forEach((item) => {
      const row = document.createElement('div');
      row.className = 'project-item' + (state.project && state.project.filePath === item.path ? ' active' : '');
      if (item.error) {
        row.innerHTML =
          '<div class="project-meta"><div class="project-name">' + escapeHtml(item.name) +
          '</div><div class="project-sub">⚠ ' + escapeHtml(item.error) + '</div></div>';
        container.appendChild(row);
        return;
      }
      const thumb = document.createElement('img');
      thumb.className = 'project-thumb';
      thumb.src = item.thumbnail || thumbPlaceholder();
      const meta = document.createElement('div');
      meta.className = 'project-meta';
      const name = document.createElement('div');
      name.className = 'project-name';
      name.textContent = item.name;
      name.title = item.name;
      const sub = document.createElement('div');
      sub.className = 'project-sub';
      sub.textContent =
        t('stepName', { n: item.index + 1 }) + ' · ' + item.nails + ' 钉 · ' + item.circleMm + 'mm';
      meta.appendChild(name);
      meta.appendChild(sub);
      const actions = document.createElement('div');
      actions.className = 'project-actions';
      actions.appendChild(iconBtn('🗑', () => deleteProject(item.path), 'danger'));
      row.appendChild(thumb);
      row.appendChild(meta);
      row.appendChild(actions);
      row.addEventListener('click', (event) => {
        if (event.target.closest('.project-actions')) return;
        openProjectPath(item.path);
      });
      container.appendChild(row);
    });
  } catch (error) {
    console.error(error);
  }
}

function iconBtn(icon, onClick, danger) {
  const button = document.createElement('button');
  button.className = 'icon-btn' + (danger ? ' danger' : '');
  button.textContent = icon;
  button.title = t(danger ? 'delete' : 'open');
  button.addEventListener('click', (event) => {
    event.stopPropagation();
    onClick();
  });
  return button;
}

function thumbPlaceholder() {
  const canvas = document.createElement('canvas');
  canvas.width = 44;
  canvas.height = 44;
  const ctx = canvas.getContext('2d');
  ctx.fillStyle = '#f8f7fb';
  ctx.fillRect(0, 0, 44, 44);
  ctx.strokeStyle = '#c8c8d0';
  ctx.beginPath();
  ctx.arc(22, 22, 19, 0, Math.PI * 2);
  ctx.stroke();
  return canvas.toDataURL();
}

async function deleteProject(filePath) {
  if (!confirm(t('delete') + '?')) return;
  try {
    await window.api.deleteProject(filePath);
    toast(t('deleted'));
    if (state.project && state.project.filePath === filePath) {
      state.project.filePath = null;
    }
    refreshProjectList();
  } catch (error) {
    toast(t('error') + ': ' + (error && error.message));
  }
}

async function openProjectPath(filePath) {
  try {
    const project = await window.api.readProject(filePath);
    adoptProject(project, filePath);
    refreshProjectList();
    refreshRecentList();
  } catch (error) {
    toast(t('cannotDecode', { msg: error && error.message }));
  }
}

async function refreshRecentList() {
  try {
    const list = await window.api.listRecent();
    state.recent = list;
    const container = $('recent-list');
    container.innerHTML = '';
    if (!list.length) {
      const empty = document.createElement('div');
      empty.className = 'side-empty';
      empty.textContent = t('noRecent');
      container.appendChild(empty);
      return;
    }
    list.forEach((entry) => {
      const row = document.createElement('div');
      row.className = 'project-item';
      const name = document.createElement('div');
      name.className = 'project-name';
      name.textContent = entry.path.split(/[\\/]/).pop();
      name.title = entry.path;
      const sub = document.createElement('div');
      sub.className = 'project-sub';
      sub.textContent = entry.path;
      const meta = document.createElement('div');
      meta.className = 'project-meta';
      meta.appendChild(name);
      meta.appendChild(sub);
      row.appendChild(meta);
      row.addEventListener('click', () => openAnyPath(entry.path));
      container.appendChild(row);
    });
  } catch (error) {
    console.error(error);
  }
}

async function openAnyPath(filePath) {
  const lower = filePath.toLowerCase();
  try {
    if (/\.(sar|bin)$/.test(lower)) {
      await openProjectPath(filePath);
    } else if (/\.txt$/.test(lower)) {
      const text = await window.api.readTextFile(filePath);
      importTxt(text, filePath.split(/[\\/]/).pop());
    } else if (/\.(png|jpe?g|webp|gif|bmp|avif)$/.test(lower)) {
      const bytes = await window.api.readFileBytes(filePath);
      await loadImageFromBytes(bytes, filePath);
    } else {
      toast(t('cannotDecode', { msg: 'unsupported' }));
    }
  } catch (error) {
    toast(t('cannotDecode', { msg: error && error.message }));
  }
}

/* ============================ IMPORT ============================ */

/* TXT import preview state (Android showSequencePreview behaviour). */
let pendingImport = null;

async function importTxt(text, fileName) {
  let parsed;
  try {
    parsed = await window.api.parseTxt(text, fileName || '');
  } catch (error) {
    toast(t('cannotDecode', { msg: error && error.message }));
    return;
  }
  if (!parsed.sequence || parsed.sequence.length < 2) {
    toast(t('cannotDecode', { msg: 'no sequence' }));
    return;
  }
  pendingImport = {
    name: fileName || t('importProject'),
    importedFileName: fileName || '未导入序列',
    index: 0,
    nails: parsed.nails,
    circleMm: parsed.circleMm,
    lineMm: parsed.lineMm,
    sequence: parsed.sequence
  };
  showImportPanel();
}

/** Preview the parsed TXT with adjustable physical parameters (Android does
 *  this in the import-preview dialog; on the desktop it lives in the right
 *  inspector so the canvas stays fully visible). */
function showImportPanel() {
  if (!pendingImport) return;
  const pi = pendingImport;
  state.importing = true;
  state.project = Object.assign({}, pi, { thumbnail: null, filePath: null, inProjectList: false });
  state.player.progressSequence = null;
  state.player.reveal = pi.sequence.length;
  state.player.zoom = 1;
  state.player.panX = 0;
  state.player.panY = 0;
  setMode('player');
  $('import-info').textContent = t('importInfo', { nails: pi.nails, total: pi.sequence.length });
  $('import-line-range').value = Math.round(pi.lineMm * 100);
  $('import-line-value').textContent = pi.lineMm.toFixed(2) + ' mm';
  $('import-circle-range').value = pi.circleMm;
  $('import-circle-value').textContent = pi.circleMm + ' mm';
  updatePlayerInfo();
  drawPlayerFrame();
}

function confirmImport() {
  if (!pendingImport) return;
  const project = pendingImport;
  pendingImport = null;
  state.importing = false;
  setMode('player');
  adoptProject(project, null);
}

function cancelImport() {
  pendingImport = null;
  state.importing = false;
  state.project = null;
  setMode('empty');
}

async function loadImageFromBytes(bytes, sourceName) {
  // FileReader → data URL instead of a blob: URL: the page CSP would block
  // blob: images, which made local image opening report "cannot parse".
  const blob = new Blob([bytes]);
  let dataUrl;
  try {
    dataUrl = await new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = () => reject(new Error('read failed'));
      reader.readAsDataURL(blob);
    });
  } catch (error) {
    toast(t('cannotDecode', { msg: 'image' }));
    return;
  }
  await loadImageFromDataUrl(dataUrl);
}

async function loadImageFromDataUrl(dataUrl) {
  const img = new Image();
  img.onload = () => enterCrop(img);
  img.onerror = () => toast(t('cannotDecode', { msg: 'image' }));
  img.src = dataUrl;
}

function adoptProject(project, filePath) {
  stopAllPlayback();
  state.importing = false;
  state.project = {
    name: project.name || t('importProject'),
    importedFileName: project.importedFileName || '未导入序列',
    index: clamp(project.index || 0, 0, Math.max(0, project.sequence.length - 1)),
    nails: project.nails,
    circleMm: project.circleMm,
    lineMm: project.lineMm,
    sequence: project.sequence,
    thumbnail: project.thumbnail || null,
    filePath: filePath || null,
    inProjectList: !!(filePath && state.info && filePath.startsWith(state.info.projectsDir))
  };
  state.player.progressSequence = null;
  state.player.reveal = project.sequence.length;
  state.player.zoom = 1;
  state.player.panX = 0;
  state.player.panY = 0;
  setMode('player');
  updatePlayerInfo();
  drawPlayerFrame();
  updateStatusBar();
}

/* ============================ THUMBNAIL ============================ */

async function renderThumbnail(project) {
  // Mirrors Android createFinalThumbnail: 192×192 monochrome sequence preview.
  const canvas = document.createElement('canvas');
  canvas.width = 192;
  canvas.height = 192;
  const ctx = canvas.getContext('2d');
  ctx.fillStyle = '#F8F7FB';
  ctx.fillRect(0, 0, 192, 192);
  const center = 96;
  const radius = 192 * 0.46;
  const strokeRatio = clamp(project.lineMm, 0.01, 1) / Math.max(1, project.circleMm);
  const stroke = Math.max(0.12, 2 * radius * strokeRatio);
  const alpha = clamp(Math.round(26 + 192 * strokeRatio * 90), 26, 82);
  ctx.strokeStyle = 'rgba(18,18,18,' + alpha / 255 + ')';
  ctx.lineWidth = stroke;
  ctx.beginPath();
  for (let i = 1; i < project.sequence.length; i++) {
    const a = project.sequence[i - 1];
    const b = project.sequence[i];
    if (a < 0 || a >= project.nails || b < 0 || b >= project.nails) continue;
    const pa = pinPoint(a, project.nails, center, center, radius);
    const pb = pinPoint(b, project.nails, center, center, radius);
    ctx.moveTo(pa.x, pa.y);
    ctx.lineTo(pb.x, pb.y);
  }
  ctx.stroke();
  ctx.strokeStyle = '#4B4B52';
  ctx.lineWidth = 1.5;
  ctx.beginPath();
  ctx.arc(center, center, radius, 0, Math.PI * 2);
  ctx.stroke();
  return new Promise((resolve) => {
    let done = false;
    const finish = (blob) => {
      if (done) return;
      done = true;
      resolve(blob);
    };
    canvas.toBlob(async (blob) => {
      if (!blob) return finish(null);
      finish(new Uint8Array(await blob.arrayBuffer()));
    }, 'image/png');
    // Fallback: never let a hung toBlob block the save silently.
    setTimeout(() => finish(null), 1500);
  });
}

/* ============================ ABOUT / MODAL ============================ */

const GITHUB_URL = 'https://github.com/241120nzdjjx/StringArtHelper';
const BILIBILI_URL = 'https://b23.tv/K3Cp0ZZ';
const EMAIL_ADDR = '241120nzdjjx@gmail.com';
const X_URL = 'https://x.com/nzdjjx241120';
const TELEGRAM_URL = 'https://t.me/nzdjjx';
const GENSHIN_UID = '305028021';

function openModal() {
  $('modal-overlay').classList.remove('hidden');
}

function closeModal() {
  $('modal-overlay').classList.add('hidden');
}

function logoTitle(text) {
  return '<img class="modal-logo" src="assets/icon.png" alt="" />' + escapeHtml(text);
}

function modalBtnRow(items) {
  const row = document.createElement('div');
  row.className = 'modal-btn-row';
  items.forEach((item) => {
    const btn = document.createElement('button');
    btn.className = 'modal-btn';
    btn.innerHTML =
      '<span>' + escapeHtml(item.label) + '</span>' +
      (item.sub ? '<span class="sub">' + escapeHtml(item.sub) + '</span>' : '');
    btn.addEventListener('click', item.onClick);
    row.appendChild(btn);
  });
  return row;
}

function singleModalBtn(label, sub, onClick) {
  return modalBtnRow([{ label, sub, onClick }]);
}

function backButton(onClick) {
  const btn = document.createElement('button');
  btn.className = 'btn btn-ghost';
  btn.textContent = '← ' + t('back');
  btn.addEventListener('click', onClick);
  return btn;
}

function showModal(title, build, withBack, footerFactory) {
  const box = $('modal-box');
  box.innerHTML = '<div class="modal-title">' + title + '</div>';
  const body = document.createElement('div');
  body.className = 'modal-body';
  box.appendChild(body);
  const closeRow = document.createElement('div');
  closeRow.className = 'modal-close-row';
  if (withBack) closeRow.appendChild(backButton(withBack));
  const buttons = footerFactory ? footerFactory() : [{ label: t('gotIt'), onClick: closeModal }];
  buttons.forEach((btn) => {
    const el = document.createElement('button');
    el.className = 'btn' + (btn.primary ? ' btn-primary' : '');
    el.textContent = btn.label;
    el.addEventListener('click', btn.onClick);
    closeRow.appendChild(el);
  });
  box.appendChild(closeRow);
  build(body);
  openModal();
}

async function copyAndToast(text, message) {
  try {
    await window.api.copyText(text);
    toast(message || t('copied'));
  } catch (error) {
    toast(t('error') + ': ' + (error && error.message));
  }
}

function showAbout() {
  const info = state.info || {};
  const intro = t('aboutIntro', { version: info.version || '0.1.0' });
  showModal(logoTitle(t('about')), (body) => {
    const introEl = document.createElement('div');
    introEl.className = 'modal-intro';
    introEl.textContent = intro;
    body.appendChild(introEl);
    body.appendChild(modalBtnRow([
      { label: '💜 ' + t('supportAuthor'), sub: '', onClick: showAboutSupport },
      { label: '📤 ' + t('shareApp'), sub: '', onClick: () => copyAndToast(GITHUB_URL) },
      { label: '💬 ' + t('contact'), sub: '', onClick: showAboutContact }
    ]));
    body.appendChild(singleModalBtn(t('versionInfo'), '', showAboutTech));
    body.appendChild(singleModalBtn('⭐ ' + t('githubRepo'), '', () => window.api.openExternal(GITHUB_URL)));
  });
}

function figureImg(src, caption) {
  const fig = document.createElement('figure');
  const img = document.createElement('img');
  img.src = src;
  img.alt = caption;
  const cap = document.createElement('figcaption');
  cap.textContent = caption;
  fig.appendChild(img);
  fig.appendChild(cap);
  return fig;
}

function showAboutSupport() {
  showModal(logoTitle(t('supportAuthor')), (body) => {
    const note = document.createElement('div');
    note.className = 'modal-note';
    note.textContent = t('supportIntro');
    body.appendChild(note);
    const imgs = document.createElement('div');
    imgs.className = 'modal-support-imgs';
    imgs.appendChild(figureImg('assets/support_alipay.jpg', t('alipay')));
    imgs.appendChild(figureImg('assets/support_wechat.jpg', t('wechatPay')));
    imgs.appendChild(figureImg('assets/wechat_miniprogram_code.png', t('miniProgram')));
    body.appendChild(imgs);
  }, showAbout);
}

function showAboutContact() {
  showModal(logoTitle(t('contact')), (body) => {
    body.appendChild(singleModalBtn(t('bilibili'), t('bilibiliSub'), () => window.api.openExternal(BILIBILI_URL)));
    body.appendChild(singleModalBtn(t('email'), t('emailSub'), () => window.api.openExternal('mailto:' + EMAIL_ADDR + '?subject=' + encodeURIComponent('绕线助手反馈'))));
    body.appendChild(singleModalBtn(t('x'), t('xSub'), () => window.api.openExternal(X_URL)));
    body.appendChild(singleModalBtn(t('telegram'), t('telegramSub'), () => window.api.openExternal(TELEGRAM_URL)));
    body.appendChild(singleModalBtn(t('genshin'), t('genshinSub'), () => copyAndToast(GENSHIN_UID, t('copiedGenshin'))));
  }, showAbout);
}

function showAboutTech() {
  const info = state.info || {};
  const text = t('techInfo', {
    version: info.version || '0.1.0',
    electron: info.electron || '?',
    chrome: info.chrome || '?',
    node: info.node || '?',
    build: info.isPackaged ? t('release') : t('test')
  });
  showModal(logoTitle(t('versionInfo')), (body) => {
    const textEl = document.createElement('div');
    textEl.className = 'modal-intro';
    textEl.textContent = text;
    textEl.style.fontSize = '13.5px';
    body.appendChild(textEl);
  }, showAbout);
}

/* ============================ SAVE / EXPORT ============================ */

async function saveProjectToProjects() {
  if (!state.project || state.project.sequence.length < 2) {
    toast(t('noSequence'));
    return;
  }
  const button = $('btn-save-here');
  const topButton = $('btn-save-project');
  const savedLabel = button.textContent;
  if (button) {
    button.disabled = true;
    button.textContent = t('saving');
  }
  if (topButton) topButton.disabled = true;
  try {
    const thumbnail = await renderThumbnail(state.project);
    const projectPayload = {
      name: state.project.name,
      importedFileName: state.project.importedFileName,
      index: state.project.index,
      timestamp: state.project.timestamp || Date.now(),
      params: {
        nails: state.project.nails,
        circleMm: state.project.circleMm,
        lineMm: state.project.lineMm
      },
      thumbnail,
      sequence: state.project.sequence
    };
    let target = state.project.filePath;
    if (!target || !state.project.inProjectList) {
      // Always save into the app's project directory (the list), like
      // Android's save manager. Opening an EXTERNAL save must not silently
      // rewrite the original file while the list stays empty.
      target = await window.api.saveProject(projectPayload, null);
      state.project.filePath = target;
      state.project.inProjectList = true;
    } else {
      await window.api.saveProject(projectPayload, target);
    }
    toast('✅ ' + t('saved'), 3500);
    await refreshProjectList();
    // Flash the newest entry so the save is unmistakable.
    const items = document.querySelectorAll('#project-list .project-item');
    if (items.length) {
      items[0].classList.add('flash');
      setTimeout(() => items[0].classList.remove('flash'), 1600);
    }
  } catch (error) {
    console.error(error);
    toast(t('error') + ': ' + (error && error.message), 4000);
  } finally {
    if (button) {
      button.disabled = false;
      button.textContent = savedLabel;
    }
    if (topButton && state.mode === 'player') topButton.disabled = false;
  }
}

async function saveProjectAs() {
  if (!state.project || state.project.sequence.length < 2) {
    toast(t('noSequence'));
    return;
  }
  try {
    const thumbnail = await renderThumbnail(state.project);
    const path = await window.api.saveProjectAs({
      name: state.project.name,
      importedFileName: state.project.importedFileName,
      index: state.project.index,
      timestamp: Date.now(),
      params: {
        nails: state.project.nails,
        circleMm: state.project.circleMm,
        lineMm: state.project.lineMm
      },
      thumbnail,
      sequence: state.project.sequence
    });
    if (path) {
      toast(t('savedAs') + '：' + path.split(/[\\/]/).pop());
      refreshRecentList();
    }
  } catch (error) {
    toast(t('error') + ': ' + (error && error.message));
  }
}

function scheduleAutoSave(delay) {
  if (!state.project || state.project.sequence.length < 2) return;
  clearTimeout(state.project._saveTimer);
  state.project._saveTimer = setTimeout(() => {
    saveProjectToProjects();
  }, delay);
}

async function exportTxt() {
  if (!state.project || state.project.sequence.length < 2) {
    toast(t('noSequence'));
    return;
  }
  try {
    const path = await window.api.saveTxtAs({
      sequence: state.project.sequence,
      nails: state.project.nails,
      lineMm: state.project.lineMm,
      circleMm: state.project.circleMm,
      importedFileName: state.project.importedFileName
    });
    if (path) toast(t('txtExported'));
  } catch (error) {
    toast(t('error') + ': ' + (error && error.message));
  }
}

async function exportPdf() {
  if (!state.project) return;
  try {
    const path = await window.api.savePdfAs({
      nails: state.project.nails,
      circleMm: state.project.circleMm
    });
    if (path) toast(t('pdfExported'));
  } catch (error) {
    toast(t('error') + ': ' + (error && error.message));
  }
}

/* ============================ MODE ============================ */

function setMode(mode) {
  state.mode = mode;
  if (mode !== 'player') {
    stopAllPlayback();
    state.importing = false;
    pendingImport = null;
  }
  $('empty-state').classList.toggle('hidden', mode !== 'empty');
  $('crop-view').classList.toggle('hidden', mode !== 'crop');
  $('player-view').classList.toggle('hidden', mode !== 'player' && mode !== 'generating');
  $('gen-panel').classList.toggle('hidden', mode !== 'crop');
  $('gen-progress-panel').classList.toggle('hidden', mode !== 'generating');
  $('player-panel').classList.toggle('hidden', mode !== 'player' || state.importing);
  $('import-panel').classList.toggle('hidden', mode !== 'player' || !state.importing);
  $('btn-save-project').disabled = mode !== 'player';
  $('btn-export-txt').disabled = mode !== 'player';
  $('btn-export-pdf').disabled = mode !== 'player';
  if (mode === 'player' || mode === 'generating') {
    requestAnimationFrame(() => drawPlayerFrame());
  } else if (mode === 'crop') {
    requestAnimationFrame(() => drawCropFrame());
  }
}

/* ============================ DRAG & DROP ============================ */

function setupDragDrop() {
  let dragDepth = 0;
  const overlay = $('drop-overlay');

  window.addEventListener('dragenter', (event) => {
    event.preventDefault();
    dragDepth++;
    if (hasFiles(event)) overlay.classList.remove('hidden');
  });
  window.addEventListener('dragover', (event) => {
    event.preventDefault();
  });
  window.addEventListener('dragleave', (event) => {
    event.preventDefault();
    dragDepth = Math.max(0, dragDepth - 1);
    if (dragDepth === 0) overlay.classList.add('hidden');
  });
  window.addEventListener('drop', (event) => {
    event.preventDefault();
    dragDepth = 0;
    overlay.classList.add('hidden');
    const files = Array.from(event.dataTransfer && event.dataTransfer.files ? event.dataTransfer.files : []);
    const paths = files
      .map((file) => window.api.getPathForFile(file))
      .filter(Boolean);
    if (!paths.length) {
      // Fallback: read dropped File contents directly.
      handleDroppedFiles(files);
      return;
    }
    handleDroppedPaths(paths);
  });
}

function hasFiles(event) {
  return (
    event.dataTransfer &&
    event.dataTransfer.types &&
    Array.from(event.dataTransfer.types).some((type) => type === 'Files')
  );
}

async function handleDroppedPaths(paths) {
  const image = paths.find((p) => /\.(png|jpe?g|webp|gif|bmp|avif)$/i.test(p));
  const sequence = paths.find((p) => /\.txt$/i.test(p));
  const save = paths.find((p) => /\.(sar|bin)$/i.test(p));
  if (image) {
    const bytes = await window.api.readFileBytes(image);
    await loadImageFromBytes(bytes, image);
  } else if (save) {
    await openProjectPath(save);
  } else if (sequence) {
    const text = await window.api.readTextFile(sequence);
    importTxt(text, sequence.split(/[\\/]/).pop());
  }
}

async function handleDroppedFiles(files) {
  for (const file of files) {
    const name = file.name.toLowerCase();
    if (/\.(png|jpe?g|webp|gif|bmp|avif)$/.test(name)) {
      const bytes = new Uint8Array(await file.arrayBuffer());
      await loadImageFromBytes(bytes, file.name);
      return;
    }
    if (/\.(sar|bin)$/.test(name)) {
      const bytes = new Uint8Array(await file.arrayBuffer());
      try {
        const project = await window.api.decodeSave(bytes);
        adoptProject(project, null);
        refreshProjectList();
      } catch (error) {
        toast(t('cannotDecode', { msg: error && error.message }));
      }
      return;
    }
    if (/\.txt$/.test(name)) {
      const text = await file.text();
      await importTxt(text, file.name);
      return;
    }
  }
}

/* ============================ DIALOG FLOWS ============================ */

const IMAGE_FILTERS = [
  { name: '图片', extensions: ['png', 'jpg', 'jpeg', 'webp', 'gif', 'bmp', 'avif'] }
];
const SEQUENCE_FILTERS = [
  { name: '序列/存档', extensions: ['txt', 'sar', 'bin'] },
  { name: '所有文件', extensions: ['*'] }
];

async function openImageDialog() {
  const filePath = await window.api.openFile({ filters: IMAGE_FILTERS });
  if (!filePath) return;
  const bytes = await window.api.readFileBytes(filePath);
  await loadImageFromBytes(bytes, filePath);
}

async function openSequenceDialog() {
  const filePath = await window.api.openFile({ filters: SEQUENCE_FILTERS });
  if (!filePath) return;
  await openAnyPath(filePath);
}

/* ============================ KEYBOARD ============================ */

function setupKeyboard() {
  window.addEventListener('keydown', (event) => {
    const target = event.target;
    const typing =
      target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable);
    if (typing) return;
    if (!$('modal-overlay').classList.contains('hidden')) return;
    // Android dispatchKeyEvent semantics: while the preview animation runs
    // every key is swallowed; once it finished (result held) ANY key press
    // releases the held result and returns to the real progress.
    if (state.mode === 'player' && state.project) {
      if (state.player.animating) return;
      if (state.player.animIndex >= 0) releaseHeldPreviewResult();
    }
    if (event.code === 'Space') {
      event.preventDefault();
      if (state.mode !== 'player' || !state.project) return;
      cancelAnimationForUserAction();
      if (state.player.playing) stopPlayTimer();
      else startPlayTimer();
    } else if (event.key === 'ArrowLeft') {
      event.preventDefault();
      moveStep(-1);
    } else if (event.key === 'ArrowRight') {
      event.preventDefault();
      moveStep(1);
    } else if (event.key === 'Home') {
      event.preventDefault();
      if (state.project) {
        state.project.index = 0;
        afterStep();
      }
    } else if (event.key === 'End') {
      event.preventDefault();
      if (state.project) {
        state.project.index = Math.max(0, state.project.sequence.length - 1);
        afterStep();
      }
    }
  });
}

/* ============================ STATUS BAR ============================ */

function updateStatusBar() {
  const project = state.project;
  if (!project || state.mode !== 'player') {
    $('status-project').textContent = t('noProject');
    return;
  }
  $('status-project').textContent =
    project.name + ' · ' + t('step', { index: project.index + 1, total: project.sequence.length });
}

/* ============================ INIT ============================ */

function bindControls() {
  $('btn-open-image').addEventListener('click', openImageDialog);
  $('btn-empty-open-image').addEventListener('click', openImageDialog);
  $('btn-open-file').addEventListener('click', openSequenceDialog);
  $('btn-empty-open-file').addEventListener('click', openSequenceDialog);
  $('btn-save-project').addEventListener('click', saveProjectToProjects);
  $('btn-export-txt').addEventListener('click', exportTxt);
  $('btn-export-pdf').addEventListener('click', exportPdf);
  $('btn-open-projects-folder').addEventListener('click', () => window.api.openProjectsFolder());
  $('btn-language').addEventListener('click', () => {
    state.lang = state.lang === 'zh' ? 'en' : 'zh';
    localStorage.setItem('sah_lang', state.lang);
    applyI18n();
    updatePlayerInfo();
    updateStatusBar();
  });

  $('btn-cancel-crop').addEventListener('click', () => setMode('empty'));
  $('btn-generate').addEventListener('click', startGeneration);
  $('btn-cancel-generate').addEventListener('click', async () => {
    await window.api.cancelGenerate();
    toast(t('cancelError'));
    setMode('empty');
  });

  $('btn-prev').addEventListener('click', () => moveStep(-1));
  $('btn-next').addEventListener('click', () => moveStep(1));
  $('btn-play').addEventListener('click', () => {
    cancelAnimationForUserAction();
    if (state.player.playing) stopPlayTimer();
    else startPlayTimer();
  });
  $('btn-jump').addEventListener('click', jumpToStep);
  $('btn-replay').addEventListener('click', replayCurrent);

  /* TXT import preview panel */
  $('import-line-range').addEventListener('input', () => {
    if (!pendingImport || !state.project) return;
    const mm = Number($('import-line-range').value) / 100;
    pendingImport.lineMm = mm;
    state.project.lineMm = mm;
    $('import-line-value').textContent = mm.toFixed(2) + ' mm';
    drawPlayerFrame();
  });
  $('import-circle-range').addEventListener('input', () => {
    if (!pendingImport || !state.project) return;
    const mm = Number($('import-circle-range').value);
    pendingImport.circleMm = mm;
    state.project.circleMm = mm;
    $('import-circle-value').textContent = mm + ' mm';
    drawPlayerFrame();
  });
  $('btn-import-open').addEventListener('click', confirmImport);
  $('btn-import-cancel').addEventListener('click', cancelImport);
  $('btn-animate').addEventListener('click', startPreviewAnimation);
  $('inp-anim-speed').addEventListener('input', () => {
    state.player.animSpeed = clamp(parseInt($('inp-anim-speed').value, 10) || 6, 1, 20);
    localStorage.setItem('sah_anim_speed', String(state.player.animSpeed));
  });
  $('btn-save-here').addEventListener('click', saveProjectToProjects);
  $('btn-save-as').addEventListener('click', saveProjectAs);

  $('inp-delay').addEventListener('change', () => {
    state.player.delayMs = clamp(Number($('inp-delay').value) || 2.5, 0.5, 10) * 1000;
    $('inp-delay').value = (state.player.delayMs / 1000).toFixed(1);
    updatePlayerInfo();
  });
  $('btn-slower').addEventListener('click', () => {
    state.player.delayMs = clamp(state.player.delayMs + 500, 500, 10000);
    $('inp-delay').value = (state.player.delayMs / 1000).toFixed(1);
    updatePlayerInfo();
  });
  $('btn-faster').addEventListener('click', () => {
    state.player.delayMs = clamp(state.player.delayMs - 500, 500, 10000);
    $('inp-delay').value = (state.player.delayMs / 1000).toFixed(1);
    updatePlayerInfo();
  });
  $('btn-line-actual').addEventListener('click', () => {
    state.player.useActualRatio = true;
    localStorage.setItem('sah_line_actual', '1');
    refreshPlayerLabels();
    drawPlayerFrame();
  });
  $('btn-line-custom').addEventListener('click', () => {
    state.player.useActualRatio = false;
    localStorage.setItem('sah_line_actual', '0');
    refreshPlayerLabels();
    drawPlayerFrame();
  });
  $('inp-custom-line').addEventListener('input', () => {
    state.player.customLineMm = Number($('inp-custom-line').value) / 100;
    $('inp-custom-line-number').value = state.player.customLineMm.toFixed(2);
    localStorage.setItem('sah_custom_line', String(state.player.customLineMm));
    drawPlayerFrame();
  });
  $('inp-custom-line-number').addEventListener('change', () => {
    const value = clamp(Number($('inp-custom-line-number').value) || 0, 0.01, 1);
    state.player.customLineMm = value;
    $('inp-custom-line').value = Math.round(value * 100);
    $('inp-custom-line-number').value = value.toFixed(2);
    localStorage.setItem('sah_custom_line', String(value));
    drawPlayerFrame();
  });
  $('inp-show-indices').addEventListener('change', () => {
    state.player.showIndices = $('inp-show-indices').checked;
    localStorage.setItem('sah_indices', state.player.showIndices ? '1' : '0');
    drawPlayerFrame();
  });
  $('inp-show-all').addEventListener('change', () => {
    state.player.showAll = $('inp-show-all').checked;
    localStorage.setItem('sah_show_all', state.player.showAll ? '1' : '0');
    drawPlayerFrame();
  });
  $('inp-speak').addEventListener('change', () => {
    state.player.speak = $('inp-speak').checked;
    localStorage.setItem('sah_speak', state.player.speak ? '1' : '0');
  });
  $('inp-repeat').addEventListener('change', () => {
    state.player.repeat = $('inp-repeat').checked;
    localStorage.setItem('sah_repeat', state.player.repeat ? '1' : '0');
  });
  $('inp-rate').addEventListener('input', () => {
    state.player.rate = Number($('inp-rate').value) / 100;
    $('rate-value').textContent = state.player.rate.toFixed(1) + '×';
    localStorage.setItem('sah_rate', String(state.player.rate));
  });

  // Modal overlay: click outside or Escape closes it.
  $('modal-overlay').addEventListener('click', (event) => {
    if (event.target === $('modal-overlay')) closeModal();
  });
  window.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && !$('modal-overlay').classList.contains('hidden')) closeModal();
  });

  const menuOff = window.api.onMenu((channel) => {
    if (channel === 'menu:open-image') openImageDialog();
    else if (channel === 'menu:open-file') openSequenceDialog();
    else if (channel === 'menu:save-project') saveProjectToProjects();
    else if (channel === 'menu:export-txt') exportTxt();
    else if (channel === 'menu:export-pdf') exportPdf();
    else if (channel === 'menu:about') showAbout();
  });
  window.addEventListener('beforeunload', menuOff);
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

async function init() {
  playerCanvas = $('player-canvas');
  playerCtx = playerCanvas.getContext('2d');
  cropCanvas = $('crop-canvas');
  cropCtx = cropCanvas.getContext('2d');

  resizeObserver = new ResizeObserver(() => {
    if (state.mode === 'crop') drawCropFrame();
    else if (state.mode === 'player' || state.mode === 'generating') drawPlayerFrame();
  });
  resizeObserver.observe($('canvas-pane'));

  applyI18n();
  bindControls();
  setupCropInteractions();
  setupPlayerInteractions();
  setupDragDrop();
  setupKeyboard();
  setMode('empty');

  try {
    state.info = await window.api.getInfo();
  } catch (_) {
    /* ignore */
  }

  // Restore persisted player settings into the controls.
  $('inp-delay').value = (state.player.delayMs / 1000).toFixed(1);
  $('inp-show-indices').checked = state.player.showIndices;
  $('inp-show-all').checked = state.player.showAll;
  $('inp-speak').checked = state.player.speak;
  $('inp-repeat').checked = state.player.repeat;
  $('inp-rate').value = Math.round(state.player.rate * 100);
  $('rate-value').textContent = state.player.rate.toFixed(1) + '×';
  $('inp-anim-speed').value = state.player.animSpeed;
  refreshPlayerLabels();
  $('custom-line-wrap').classList.toggle('hidden', state.player.useActualRatio);

  // Input limits (blur/change corrects out-of-range values with a hint).
  bindNumericLimits('inp-nails', 100, 500, 0);
  bindNumericLimits('inp-lines', 10, 20000, 0);
  bindNumericLimits('inp-circle', 80, 1200, 0);
  bindNumericLimits('inp-line', 0.01, 1, 2);
  bindNumericLimits('inp-delay', 0.5, 10, 1);
  bindNumericLimits('inp-custom-line-number', 0.01, 1, 2);

  await refreshProjectList();
  await refreshRecentList();
  updateStatusBar();
}

document.addEventListener('DOMContentLoaded', init);

/* Debug/automation hook used by tests/smoke.js (SAH_SMOKE=1). */
window.__sah = {
  state,
  setMode,
  loadImageFromDataUrl,
  loadImageFromBytes,
  importTxt,
  startGeneration,
  openProjectPath,
  saveProjectToProjects,
  refreshProjectList,
  drawPlayerFrame,
  drawCropFrame,
  exportTxt,
  showAbout,
  showAboutSupport,
  showAboutContact,
  showAboutTech,
  closeModal,
  startPreviewAnimation,
  stopPreviewAnimation,
  jumpToStep,
  openAnyPath
};
