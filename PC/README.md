<p align="center">
  <strong>简体中文</strong> | <a href="./README.en.md">English</a>
</p>

<p align="center">
  <a href="../README.md">项目主页</a> · <a href="../Android/README.md">Android</a> · <strong>PC</strong> · <a href="../wechat-miniprogram/README.md">微信小程序</a> · <a href="../Wearable/README.md">小米手环</a>
</p>

# StringArtHelper 桌面版（PC）

当前版本：**0.1.0**

> 本目录是 [StringArtHelper](https://github.com/241120nzdjjx/StringArtHelper) 项目的 **PC 桌面版**（Electron）。

StringArtHelper 是免费、开源、无广告的绕线画生成与制作辅助工具。本 PC 版**不是重写**，而是基于 **Android 版源码抽取核心能力**：保留核心生成算法与跨端文件格式，去掉手环（小米手环）相关功能，并针对桌面环境重新设计了交互——把图片、序列、存档直接拖进窗口即可开始。

## 功能特性

- **拖拽即用**：把图片（PNG/JPG/WebP/GIF/BMP）、`.txt` 序列或 `.sar`/`.bin` 存档直接拖入窗口，自动进入对应流程。
- **图片生成序列**：交互式方形裁剪框（拖动 / 滚轮缩放 / 双击复位），参数与 Android 版一致（钉子数、最大线数、钉位圆直径、线径、自动停止）。
- **实时生成预览**：生成在独立 worker 线程中运行，界面不卡顿，边生成边逐线揭示预览。
- **序列播放器**：上一步 / 下一步 / 播放暂停 / 跳转 / 重播，可调步进间隔并支持“快一点 / 慢一点”；显示前一个、当前、后一个钉号；支持实际比例或自定义线宽显示（滑块 + 精确输入）、钉号标注、缩放平移（滚轮 + 拖动）。
- **朗读钉号**：可选使用系统语音朗读当前钉号（中文数字格式与 Android 版一致），支持每个钉号重复播报两次与语速调节。
- **项目列表自动保存**：生成的项目自动存入项目目录（`.sar`），可随时打开、另存、重命名、删除。
- **导出**：导出 Android 同款 TXT 序列（带参数头，可被 Android/小程序导入）、打印用钉位模板 PDF（A4 最少页数拼接 + 裁切线 + 100 mm 校准尺）。
- **完整关于页**：应用简介、支持作者（支付宝 / 微信赞赏码、微信小程序码）、反馈与联系（Bilibili / 邮箱 / X / Telegram 跳转，原神 UID 复制）、版本与开源信息（含防盗版声明），与 Android 版关于页对齐。
- **应用图标**：沿用 Android 版启动图标（深色底 + 紫色弦盘 + 白色绕线），窗口图标与安装包均使用该图标。

## 与 Android / 微信小程序版的关系

| 能力 | 说明 |
| --- | --- |
| 核心算法 | 贪心生成算法按 Android 版 `StringArtGenerator.java` 1:1 移植（256×256 工作区、线宽/透明度模型、最近钉回避、自动停止）。 |
| 存档格式 | `.sar`（SAR2/SAR3/SAR4）与 `.bin` 存档、TXT 序列均与 Android、微信小程序**字节级互通**，可互相导入导出。 |
| 缩略图 | SAR4 缩略图为 192×192 单色 PNG，语义与 Android 版一致。 |
| 手环 | 本版不包含小米手环相关功能（PC 无对应软硬件）。 |
| 播报 | Android 用系统 TTS 播报钉号；PC 版使用系统语音（可关闭）。 |

## 开发与运行

要求：Node.js ≥ 20。

```bash
npm install        # 安装依赖（Electron）
npm start          # 启动应用
npm test           # 运行核心模块测试（SAR/TXT/生成算法/PDF）
```

> 国内网络下载 Electron 二进制较慢时，可先执行：
> `$env:ELECTRON_MIRROR = "https://npmmirror.com/mirrors/electron/"; npm install`
>
> 若 npm ≥ 11 提示 `allow-scripts` 拦截了 Electron 的安装脚本，可执行
> `npm approve-scripts electron` 或手动运行一次
> `node node_modules/electron/install.js` 补下载二进制。

## 目录结构

```text
PC/
├── src/
│   ├── main/                 Electron 主进程
│   │   ├── main.js           窗口、菜单、IPC、存档目录管理
│   │   └── generate-worker.js 生成算法 worker_thread（不阻塞 UI）
│   ├── preload.js            渲染进程安全桥接（contextBridge）
│   ├── core/                 纯 JS 核心模块（无平台依赖，主进程/测试共用）
│   │   ├── generator.js      生成算法（Android StringArtGenerator 移植）
│   │   ├── sar.js            SAR2/3/4 存档编解码（Java modified UTF-8）
│   │   ├── text-codec.js     TXT 序列解析/导出、文本编码识别
│   │   ├── number-format.js  中文数字（Android NailNumberFormatter 移植）
│   │   └── pdf-template.js   钉位模板 PDF 生成
│   └── renderer/             桌面 UI（深色主题，沿用 Android 配色）
│       ├── index.html
│       ├── styles.css
│       ├── app.js            交互逻辑与 Canvas 预览渲染
│       └── assets/           应用图标（PNG/ICO/SVG）与关于页图片
├── scripts/
│   └── make-icon.ps1         从安卓图标矢量数据生成多尺寸 PNG/ICO
└── tests/
    ├── run-tests.js          Node 测试（npm test）
    ├── smoke.js              自动化 UI 冒烟测试（SAH_SMOKE=1 npm start）
    └── fixtures/             SAR 字节兼容测试夹具（与小程序测试同源）
```

## 技术要点

- **纯 JS 核心**：`src/core/` 全部为无依赖的 CommonJS 模块，可在主进程、worker 线程与 Node 测试中直接运行，保证算法与格式单一实现。
- **1:1 算法移植**：生成算法按 Android `StringArtGenerator.java` 逐行移植，并全程模拟 Java `float`（32 位）数值语义（`Math.fround` + `Float32Array`）；`npm run test:java` 用真实 Java 参考实现（`tests/java/GenCore.java`，Android 源码的 1:1 复制）做同输入对照，两组用例**序列逐位一致**。详见 [docs/generator-audit.md](./docs/generator-audit.md)。
- **Worker 线程生成**：生成过程通过 `worker_threads` 运行，进度与已生成序列分段回传渲染进程，实现实时预览且界面零卡顿。
- **跨端字节兼容**：SAR 编解码、TXT 解析均以 Android 版为基准，并由与微信小程序版共享的测试夹具做字节级回归（`tests/interop.test.js` 验证 SAR4/TXT 与小程序原版模块逐字节一致）。
- **安全基线**：`contextIsolation: true`、`nodeIntegration: false`，渲染进程仅通过 preload 暴露的最小 API 与主进程通信；删除存档走系统回收站（`shell.trashItem`）。
- **主题**：深色主题取色与 Android 版一致（`#101016` / `#1d1d27` / 强调色 `#9769ff`）。

## 许可

本目录代码遵循与项目一致的 [GPL-3.0-only](../LICENSE) 许可。其中 `src/core/sar.js`、`src/core/text-codec.js`、`src/core/pdf-template.js` 移植自同项目 `wechat-miniprogram/` 下的模块（同一许可，已在文件头注明）。
