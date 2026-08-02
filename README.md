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
(受限于微信小程序审核 此仓库中的小程序版本可能高于实际线上版本 实际使用请以线上版为准)

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

开发者工具导入方法、平台限制和测试等详细说明见 [`wechat-miniprogram/README.md`](wechat-miniprogram/README.md)。
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


---

# StringArtHelper

A free, open-source, ad-free string art creation tool whose core features run entirely on the local device. It is available as an Android app and a WeChat Mini Program.

From image cropping, string-path generation, and pin-template creation to step-by-step previews, voice guidance, and project saves, all processing is performed locally. TXT sequences and project save files can be transferred between the Android app and the WeChat Mini Program, allowing work to continue across devices.

## Main Features of the Android App

- Local image cropping with edge snapping and a black-and-white preview of the final selected area
- Local path generation for 100–500 pins and up to 20,000 string segments
- Pin-circle diameters from 80 to 1,200 mm, with a reminder to leave at least 10 mm of board space on every side
- Step-by-step visualization of the generation process at animation speeds from 1× to 20×
- Adjustable pin count, maximum string segment count, pin-circle diameter, string diameter, and automatic over-darkening prevention
- A unified Import entry that automatically recognizes TXT pin sequences and `.bin` or `.sar` project saves
- TXT sequence import and export, with offline number voice guidance in Chinese and English
- Optional temporary audio ducking of other Android media during voice guidance
- Step control using volume buttons, Bluetooth headsets, and media buttons
- Dynamic remaining-time estimates based on the number of steps left and the actual working pace
- A real-time preview that can be zoomed, panned, minimized, and displayed in full screen
- Purple highlighting for the current string segment, with optional true-scale rendering based on the physical string diameter
- Automatic progress recovery and manual snapshots; `.bin` and `.sar` saves can be imported, while projects are exported and shared in `.sar` format
- Automatic generation of A4 tiled pin templates using the minimum required number of pages, including cutting lines, alignment marks, and a 100 mm calibration ruler
- Chinese and English interfaces with portrait and landscape support
- Sharing of the currently installed APK with other users

## WeChat Mini Program

> Due to the WeChat Mini Program review process, the Mini Program source code in this repository may be newer than the version currently available online. For actual use, refer to the version published on WeChat.

If you do not want to install the Android app, or need to use StringArtHelper on a non-Android device, scan the Mini Program code below with WeChat or search for **“绕线画助手”** in WeChat:

<p align="center">
  <img src="docs/images/wechat-miniprogram-code.png" alt="StringArtHelper WeChat Mini Program code" width="320">
</p>

WeChat Mini Program entry text—copy it and open it inside WeChat:

```text
#小程序://绕线画助手/3BsgpZwfWylWK9d
```

Some screens and interactions differ slightly from the Android app, but the core features—including image-to-path generation, previews, voice guidance, project saves, and PDF pin templates—remain available.

The Android app and WeChat Mini Program can import and export compatible `.bin` or `.sar` project saves and TXT pin sequences, making it possible to continue a project on another device.

Because of WeChat platform restrictions, TXT and BIN files must first be forwarded to a WeChat chat before they can be selected and uploaded from Chat Files.

> All generation and project data is still processed locally on the device. Some capabilities may be limited by the WeChat Mini Program platform.

For instructions on importing the project into WeChat DevTools, platform limitations, and testing, see [`wechat-miniprogram/README.md`](wechat-miniprogram/README.md).

The Mini Program source code is located in [`wechat-miniprogram/`](wechat-miniprogram/).

## Privacy

The Android app does not request network access and contains no advertising or analytics SDKs. The WeChat Mini Program does not use WeChat Cloud Development, a project-operated server, or network requests.

Images, TXT sequences, and project saves are processed only on the local device and are not uploaded by this project. Android system cloud backup is disabled.

When you open GitHub, Bilibili, X, Telegram, an email address, or a third-party string art generation website, the request is handed over to the corresponding external app, WeChat, or web browser.

See [PRIVACY.md](PRIVACY.md) for details.

## Download and Verification

Download the official APK from the **Releases** page of this repository.

Android package name:

```text
com.niuza.stringartreader2
```

SHA-256 fingerprint of the official Android signing certificate:

```text
6A:06:8C:3D:40:95:25:1F:E7:1D:00:09:FC:06:51:30:C5:E6:62:BA:32:97:ED:A8:C3:8F:CE:94:CA:70:76:4F
```

Android permits an existing installation to be updated only when both the package name and signing certificate match. The About page in the app also displays the signing-certificate fingerprint of the currently installed APK.

## Source Code Structure

```text
StringArtHelper/
├── app/                    Android application source code
├── wechat-miniprogram/     WeChat Mini Program source code
├── docs/                   Release, signing, and image resources
└── .github/workflows/      Android automated build workflows
```

## Building and Testing

### Android

Requirements:

- JDK 17
- Gradle 8.9
- Android Gradle Plugin 8.7.3
- Android SDK 35 / Build Tools 35.0.0

```bash
gradle --no-daemon :app:assembleDebug
```

GitHub Actions builds Debug and unsigned Release APKs from the public source code and uploads them as workflow artifacts.

Official APKs must be signed using the author's official signing key, which is stored offline. The private key and its passwords are never included in the repository or CI environment.

### WeChat Mini Program

Import the [`wechat-miniprogram/`](wechat-miniprogram/) directory directly into WeChat DevTools.

Node.js is required only for automated tests:

```bash
cd wechat-miniprogram
npm test
```

The Mini Program does not use WeChat Cloud Development or a project-operated server.

Automated tests cover the core generation algorithm, TXT and SAR2/3/4 parsing, file-based project saves and migration, crop-edge snapping, and static checks of important pages. Album selection, camera access, Chat Files selection, and file sharing must still be verified on a physical device running WeChat.

See [CHANGELOG.md](CHANGELOG.md) for the version history and [docs/RELEASE_SIGNING.md](docs/RELEASE_SIGNING.md) for signing and official release instructions.

## Project Information

- Current official Android version: `26.3.5` (`versionCode 61`)
- Minimum Android version: Android 8.0 (API 26)
- Target Android API level: API 35
- License: GNU GPL v3.0 only, with compatible additional terms under Section 7
- Author: 牛杂の经济学

Version numbers are based on the year. Releases made in 2026 use the `26.x.x` series. Feature updates increment the minor version, bug-fix updates increment the patch version, and Android `versionCode` values increase independently.

Modified versions must provide the corresponding source code, preserve reasonable attribution, and be clearly identified as modified. They must not be presented as official builds.

See [LICENSE](LICENSE) and [ADDITIONAL_TERMS.md](ADDITIONAL_TERMS.md) for details.

## Feedback and Contact

- GitHub: [241120nzdjjx/StringArtHelper](https://github.com/241120nzdjjx/StringArtHelper)
- Bilibili: [牛杂の经济学](https://b23.tv/K3Cp0ZZ)
- Email: 241120nzdjjx@gmail.com
- X: [@nzdjjx241120](https://x.com/nzdjjx241120)
- Telegram: [@nzdjjx](https://t.me/nzdjjx)
- WeChat Mini Program: search for **“绕线画助手”** in WeChat, or copy `#小程序://绕线画助手/3BsgpZwfWylWK9d` and open it inside WeChat

For general questions, use GitHub Issues. For security-related matters, read [SECURITY.md](SECURITY.md).
