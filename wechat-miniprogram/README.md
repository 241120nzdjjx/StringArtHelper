# 绕线画助手｜StringArtHelper 微信小程序版

(受限于微信小程序审核 此仓库中的小程序版本可能高于实际线上版本 实际使用请以线上版为准)

免费、开源、纯本地运行的绕线画生成与制作辅助工具。

当前版本：**v1.2.2**

## 主要功能

- 从图片生成绕线画钉位路径
- 灰阶预处理、圆形裁切、拖动与双指缩放
- 支持 100～500 个钉位
- 自定义线条数量、线径和钉位圆直径
- 自动防止生成结果过黑
- 生成过程动画与最终效果预览
- 实时绕线进度预览
- 上一步、下一步、跳转及自动播放
- 显示前一个、当前、后一个钉号
- 中英文离线数字语音播报
- 自动续做与手动存档
- 导入、导出 Android 兼容的 SAR 存档
- 导入、导出带物理参数的 TXT 绕线序列
- 生成真实毫米尺寸的 A4 PDF 钉位模板
- 支持中文、英文及横竖屏

## 隐私与离线原则

绕线画助手不依赖服务器。

图片处理、路径计算、项目存档和语音播报均在本地完成。应用不会主动上传用户选择的图片、绕线序列或存档内容。

## 文件导入与导出

支持的导入文件：

- `.txt`：绕线钉号序列
- `.sar`：StringArtHelper 存档
- `.bin`：内容与 SAR 相同的兼容存档

微信小程序选择非图片文件时存在平台限制：

> 上传 TXT 和 BIN 文件，需先转发至聊天，再从聊天上传。

导出后如需保存到设备：

> 下载 TXT 和 BIN 文件，需先转发至聊天，再从聊天下载。

## TXT 格式

正式导出的 TXT 文件包含：

- 钉数
- 线径
- 钉位圆直径
- 完整钉号序列

示例：

```text
# 绕线助手导出
# 钉数: 300
# 线径: 0.20 mm
# 钉位圆直径: 260 mm
# 钉号：0 号正右，顺时针递增
# 共 5001 个钉号

0 → 87 → 162 → 39 → 204 → 11
```

同时兼容空格、逗号、换行、箭头和步骤编号等常见格式。

## SAR 存档兼容性

小程序支持读取：

- SAR2
- SAR3
- SAR4

正式导出格式为 SAR4，与 Android StringArtHelper 兼容。

存档包含：

- 项目名称
- 当前进度
- 钉数
- 钉位圆直径
- 线径
- 缩略图
- 完整钉号序列

## PDF 钉位模板

PDF 模板按照真实毫米尺寸生成，并支持大尺寸自动分页拼接。

打印时请选择：

> 实际大小 / 100%

请勿选择“适合页面”或自动缩放。打印后可使用 PDF 中的 100 mm 校准尺检查比例。

## 项目管理

存档分为两类：

- **自动续做**：持续记录当前项目的最新进度
- **手动存档**：用户主动保存的固定节点，不会被后续进度自动覆盖

读取手动存档后继续操作，会建立新的自动续做项目，原手动存档保持不变。

## 开发与测试

这是一个微信原生小程序项目，可直接使用微信开发者工具打开。

基础要求：

- 微信开发者工具
- 支持 Canvas 2D、Worker 和本地文件接口的微信基础库
- Node.js（仅用于运行自动测试）

运行测试：

```bash
npm test
```

项目不依赖远程服务器，也不需要配置后端地址。

## Android 版本

Android StringArtHelper 的源码、版本记录和发布信息：

[GitHub：StringArtHelper](https://github.com/241120nzdjjx/StringArtHelper)

## 反馈与联系

如果你有改进建议、遇到问题，或发现哪里使用不顺手，欢迎联系：

- 邮箱：241120nzdjjx@gmail.com
- [Bilibili](https://b23.tv/K3Cp0ZZ)

## 开源许可

本项目按照 [GNU General Public License v3.0](../LICENSE) 发布。

你可以在许可证允许的范围内使用、研究、修改和分发本项目源码。分发修改版本时，请遵守 GPL v3.0 的开源义务，并保留原有许可证和版权说明。


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
