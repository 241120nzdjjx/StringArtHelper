<p align="center">
  <strong>简体中文</strong> | <a href="./README.en.md">English</a>
</p>

<p align="center">
  <strong>项目主页</strong> · <a href="./Android/README.md">Android</a> · <a href="./PC/README.md">PC</a> · <a href="./wechat-miniprogram/README.md">微信小程序</a> · <a href="./Wearable/README.md">小米手环</a>
</p>

# 绕线助手 / StringArtHelper

免费、开源、无广告的绕线画生成与制作辅助工具。项目包含 Android 应用、PC 桌面版、微信小程序和 Xiaomi Smart Band 9 Pro 配套手环应用。

图片处理、绕线序列生成、预览、语音播报和存档均以本地处理为原则。Android、PC 与微信小程序可通过 TXT、`.bin` 和 `.sar` 文件互通；Android 存档还可同步至小米手环，在手环脱离手机后继续使用。

## 选择平台

| 平台 | 当前版本 | 用途 | 源码与说明 |
| --- | --- | --- | --- |
| Android | v26.5.9（72） | 完整生成、预览、播报、存档及手环管理 | [进入 Android 目录](./Android/README.md) |
| PC | 0.1.0 | 桌面端生成、播放与跨端存档（拖入图片/序列/存档） | [进入 PC 目录](./PC/README.md) |
| 微信小程序 | v1.2.2 | 无需安装应用，在微信内完成生成与制作 | [进入微信小程序目录](./wechat-miniprogram/README.md) |
| 小米手环 | v26.5.9（72） | 查看钉号、换步、跳转、预览及离线存档 | [进入手环目录](./Wearable/README.md) |

## 仓库结构

```text
StringArtHelper/
├── Android/                Android 应用源码及中英文说明
├── PC/                     PC 桌面版（Electron）源码及中英文说明
├── wechat-miniprogram/     微信小程序源码及中英文说明
├── Wearable/               Xiaomi Smart Band 9 Pro 源码及中英文说明
├── .github/workflows/      Android 与手环构建、小程序测试
├── README.md               项目总说明（简体中文）
├── README.en.md            Project overview (English)
├── CHANGELOG.md            中文更新记录
├── CHANGELOG.en.md         English changelog
└── LICENSE                 开源许可证
```

每个端的 README 顶部都提供“项目主页 / Android / PC / 微信小程序 / 小米手环”相对链接，并分别跳转到同语言页面。下载仓库到本地后，这些链接也能继续使用。

## 下载

正式安装包与对应源码压缩包请从 [GitHub Releases](https://github.com/241120nzdjjx/StringArtHelper/releases) 下载，并使用同一 Release 中的 `StringArtHelper-v0.1.0-SHA256.txt` 核对文件完整性。

本次发布文件：

- `StringArtHelper-v26.5.9-official.apk`
- `StringArtHelper-Android-v26.5.9-source.zip`
- `StringArtHelper-Wearable-v26.5.9-MiBand9Pro.rpk`
- `StringArtHelper-Wearable-v26.5.9-source.zip`
- `StringArtHelper-WeChat-MiniProgram-v1.2.2-source.zip`
- `StringArtHelper-PC-0.1.0-x64.exe`（Windows 便携版，无需安装）
- `StringArtHelper-PC-0.1.0-source.zip`
- `StringArtHelper-v26.5.9-SHA256.txt`（全部文件哈希）

> 说明：Release 页面还会自动附带 GitHub 生成的 `Source code` 两个源码压缩包。

## 兼容关系

- Android APK 与手环 RPK 的包名均为 `com.niuza.stringartreader2`。
- Android 与手环版本应保持一致，本次均为 v26.5.9（versionCode 72）。
- Android、PC 与微信小程序支持互相导入 TXT、`.bin` 和 `.sar` 存档。
- 手环存档由 Android 应用同步；同步完成后可在手环端离线换步并保存进度。

## 隐私与安全

- Android 应用不申请网络权限，不包含广告或统计 SDK。
- 微信小程序不使用云开发、项目服务器或主动网络请求。
- 正式签名私钥、密码、微信 AppSecret、Token 和本地私有配置不会进入源码仓库或源码压缩包。

详见 [隐私说明](./PRIVACY.md) 与 [安全策略](./SECURITY.md)。

## 开源许可

本项目原创代码采用 [GNU GPL v3.0 only](./LICENSE)，并附带兼容的 [第 7 条附加条款](./ADDITIONAL_TERMS.md)。内置第三方代码和语音素材仍采用各自许可证，详见 [NOTICE](./NOTICE) 及各端目录内的声明。

## 反馈与联系

- GitHub：[241120nzdjjx/StringArtHelper](https://github.com/241120nzdjjx/StringArtHelper)
- Bilibili：[牛杂の经济学](https://b23.tv/K3Cp0ZZ)
- 邮箱：241120nzdjjx@gmail.com
- X：[@nzdjjx241120](https://x.com/nzdjjx241120)
- Telegram：[@nzdjjx](https://t.me/nzdjjx)
