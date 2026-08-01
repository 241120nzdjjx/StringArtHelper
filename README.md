# 绕线助手 / String Art Helper

免费、开源、无广告、核心功能纯本地运行的绕线画制作工具，提供 Android 应用与微信小程序版。
从图片裁切、生成绕线序列和钉位模板，到逐步预览、语音播报与项目存档，都在设备本地完成；两个版本可通过 TXT 和项目存档跨设备继续制作。

## Android 版主要功能

- 本地图片裁切，带边缘吸附和黑白最终区域预览
- 100～500 颗钉子、最多 20,000 根线的本地序列生成
- 圆径支持 80～1200 mm；界面提示板材四边至少预留 10 mm
- 生成过程逐线可视化，以及 1×～20× 动画速度
- 结果页调整钉数、最大线数、圆径、线径与自动防全黑
- 统一“导入”入口，自动识别 TXT 钉号序列、`.bin` 与 `.sar` 项目存档
- TXT 序列导入、导出和中英文数字 TTS 播报
- 播报时可请求 Android 临时降低其他媒体声音
- 音量键、蓝牙耳机和媒体键换步
- 根据剩余步骤和实际制作节奏动态估算剩余时间
- 可缩放、移动、最小化和全屏的实时预览
- 当前线段紫色高亮，线径可按真实毫米比例显示
- 自动续做项目与手动快照；支持导入 `.bin` / `.sar` 存档，并以 `.sar` 格式导出和分享
- 自动生成最少 A4 拼页的钉位模板 PDF，包含裁切线、对齐标记与 100 mm 校准尺
- 中英文界面，支持横屏和竖屏
- 分享当前安装包给朋友

## 微信小程序版

不想下载应用，或需要在非 Android 设备上使用？使用微信扫描下方小程序码，或在微信中搜索 **“绕线画助手”**：

<p align="center">
  <img src="docs/images/wechat-miniprogram-code.png" alt="绕线画助手微信小程序码" width="320">
</p>

微信小程序入口（复制后在微信中打开）：

```text
#小程序://绕线画助手/3BsgpZwfWylWK9d
```

小程序版的部分界面和操作方式与 Android 版略有不同，但图片生成绕线序列、预览、播报、项目存档和钉位模板 PDF 等核心功能保持一致。Android 版与微信小程序版可以互相导入、导出 `.bin` / `.sar` 项目存档和 TXT 钉号序列，方便跨设备继续制作。

在微信中上传 TXT 或 BIN 文件时，需要先把文件转发到聊天，再从聊天文件中选择上传。

> 所有生成与项目数据仍在设备本地处理；实际可用能力可能受微信小程序平台限制。

开发者工具导入方法、平台限制和测试等详细说明见 [`wechat-miniprogram/StringArtHelper-WeChat-Miniprogram-README.md`](wechat-miniprogram/StringArtHelper-WeChat-Miniprogram-README.md)。
小程序源码见 [`wechat-miniprogram/`](wechat-miniprogram/)。

## 隐私

Android 应用不申请网络权限，不包含广告或统计 SDK；微信小程序不使用云开发、业务服务器或网络请求。图片、TXT 和项目存档只在本机处理，不会由本项目上传。Android 系统云备份已禁用。打开 GitHub、Bilibili、X、Telegram、邮箱或第三方生成网站时，会交给对应外部应用、微信或浏览器处理。

详见 [PRIVACY.md](PRIVACY.md)。

## 下载与核验

请从本仓库的 **Releases** 页面下载官方 APK。包名：

```text
com.niuza.stringartreader2
```

官方 Android 发布证书 SHA-256：

```text
6A:06:8C:3D:40:95:25:1F:E7:1D:00:09:FC:06:51:30:C5:E6:62:BA:32:97:ED:A8:C3:8F:CE:94:CA:70:76:4F
```

Android 只有在包名和签名证书都一致时才允许覆盖更新。应用“关于”页也会显示当前 APK 的签名指纹。

## 源码结构

```text
StringArtHelper/
├── app/                    Android 应用源码
├── wechat-miniprogram/     微信小程序源码
├── docs/                   发布、签名与图片资源
└── .github/workflows/      Android 自动构建工作流
```

## 构建与测试

### Android

要求：

- JDK 17
- Gradle 8.9
- Android Gradle Plugin 8.7.3
- Android SDK 35 / Build Tools 35.0.0

```bash
gradle --no-daemon :app:assembleDebug
```

GitHub Actions 会对公开源码执行 Debug 与未签名 Release 构建，并把 APK 作为工作流构件上传。正式 APK 必须使用作者离线保管的官方密钥签名；私钥和密码不会进入仓库或 CI。

### 微信小程序

使用微信开发者工具直接导入 [`wechat-miniprogram/`](wechat-miniprogram/) 目录。自动测试需要 Node.js：

```bash
cd wechat-miniprogram
npm test
```

小程序不使用云开发或业务服务器。测试覆盖核心生成算法、TXT 与 SAR2/3/4 解析、文件型存档及迁移、裁切吸附和关键页面静态审计；相册、相机、聊天文件选择与文件分享仍需在真机微信中验证。

版本记录见 [CHANGELOG.md](CHANGELOG.md)，签名与正式发布说明见 [docs/RELEASE_SIGNING.md](docs/RELEASE_SIGNING.md)。

## 项目信息

- 当前正式版本：`26.3.5`（versionCode 61）
- 微信小程序版本：`1.2.1`
- 最低 Android：8.0（API 26）
- 目标 Android：API 35
- 许可证：GNU GPL v3.0 only，另有兼容的第 7 条附加条款
- 作者：牛杂の经济学

版本号按年份命名：2026 年为 `26.x.x`；功能更新递增次版本，修复更新递增补丁版本，Android `versionCode` 独立递增。

修改版必须提供对应源码、保留合理声明并明确标注修改，不能冒充官方构建。详见 [LICENSE](LICENSE) 与 [ADDITIONAL_TERMS.md](ADDITIONAL_TERMS.md)。

## 反馈与联系

- GitHub：[241120nzdjjx/StringArtHelper](https://github.com/241120nzdjjx/StringArtHelper)
- Bilibili：[牛杂の经济学](https://b23.tv/K3Cp0ZZ)
- 邮箱：241120nzdjjx@gmail.com
- X：[@nzdjjx241120](https://x.com/nzdjjx241120)
- Telegram：[@nzdjjx](https://t.me/nzdjjx)
- 微信小程序：微信搜索 **“绕线画助手”**，或复制 `#小程序://绕线画助手/3BsgpZwfWylWK9d` 后在微信中打开

普通问题可使用 GitHub Issues；安全问题请阅读 [SECURITY.md](SECURITY.md)。
