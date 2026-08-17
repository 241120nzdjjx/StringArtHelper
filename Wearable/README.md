<p align="center">
  <strong>简体中文</strong> | <a href="./README.en.md">English</a>
</p>

<p align="center">
  <a href="../README.md">项目主页</a> · <a href="../Android/README.md">Android</a> · <a href="../PC/README.md">PC</a> · <a href="../wechat-miniprogram/README.md">微信小程序</a> · <strong>小米手环</strong>
</p>

# 绕线助手小米手环版

当前版本：**v26.5.9（versionCode 72）**  
目标设备：**Xiaomi Smart Band 9 Pro（336 × 480）**  
验证固件基线：**HyperOS 3.1.175**

手环端是 [Android 版](../Android/README.md)的配套应用，包名同为 `com.niuza.stringartreader2`。在线时可控制手机并读取当前状态；同步存档后，也能脱离手机独立换步和保存进度。

## 主要功能

- 显示上一个、当前和下一个钉号，以及总进度和连接状态
- 上一步、下一步、播放/暂停、重播和跳转
- 主页直接“快一点 / 慢一点”，设置页、音量键映射和翻腕动作同步支持调整等待间隔
- 存档列表、存档同步和离线制作
- 可拖动、放大、缩小、复位和刷新的进度预览
- 项目详情：进度、钉数、圆径、线径、预计线长和数据来源
- 音量＋/－映射、点击、左右滑动和实验性翻腕控制
- 按设置提供换步振动反馈

## 兼容要求

- Android APK 与手环 RPK 应使用相同包名、相同正式证书和相同版本号。
- 本次 Android 与手环版本均为 v26.5.9（72）。
- 首次使用时需在 Android 端开启手环控制并执行“同步全部存档”。
- 未同步离线存档时，手环仍可在手机连接正常的情况下显示手机当前存档状态。

## 构建

要求：Node.js、npm，以及 `aiot-toolkit 2.0.5` 所需环境。

```bash
npm ci
npm run build
```

生成正式互联 RPK 时，必须使用与 Android APK 相同的正式私钥签名。私钥、密码、PEM/JKS 文件、`node_modules`、`build` 和 `dist` 均不应提交到源码仓库。

## 目录结构

```text
Wearable/
├── src/app.ux              应用入口
├── src/manifest.json       包名、版本和页面声明
├── src/pages/              首页、存档、预览、跳转、详情与设置
├── src/i18n/               中文、英文及默认文本
├── package.json            构建命令与工具版本
└── README.md / README.en.md
```

本目录源码适用项目根目录的 [GNU GPL v3.0](../LICENSE)、[附加条款](../ADDITIONAL_TERMS.md)及 [NOTICE](../NOTICE)。
