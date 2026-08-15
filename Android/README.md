<p align="center">
  <strong>简体中文</strong> | <a href="./README.en.md">English</a>
</p>

<p align="center">
  <a href="../README.md">项目主页</a> · <strong>Android</strong> · <a href="../wechat-miniprogram/README.md">微信小程序</a> · <a href="../Wearable/README.md">小米手环</a>
</p>

# 绕线助手 Android 版

当前正式版本：**v26.5.9（versionCode 72）**  
包名：`com.niuza.stringartreader2`

Android 版提供从图片生成绕线序列、实时预览、离线语音播报、存档管理、PDF 钉位模板和 Xiaomi Smart Band 9 Pro 管理等完整功能。

## 主要功能

- 本地裁切图片并生成 100～500 颗钉子、最多 20,000 根线的绕线序列
- 逐线生成动画、可缩放实时预览和当前线段高亮
- 中英文离线钉号播报；中文钉号按正常数词朗读，例如 `119` 读作“一百一十九”
- “快一点 / 慢一点”可直接调整播报后的等待间隔，也可映射到音量键或手环翻腕动作
- 音量键、蓝牙耳机、媒体键和屏幕操作换步
- 自动续做、手动快照以及 TXT、`.bin`、`.sar` 导入导出
- 自动生成带裁切线、对齐标记和 100 mm 校准尺的 A4 拼页 PDF
- 连接小米手环，显示在线状态、发送换步反馈并同步离线存档
- 中英文界面，支持横屏与竖屏

## 与其他端互通

- [微信小程序版](../wechat-miniprogram/README.md)可与 Android 互相导入 TXT、`.bin` 和 `.sar` 文件。
- [小米手环版](../Wearable/README.md)通过 Android 配套接口读取当前状态和同步存档；同步后可脱离手机继续换步。

## 构建
要求：JDK 17、Gradle 8.9、Android Gradle Plugin 8.7.3，以及 Android SDK 35 / Build Tools 35.0.0。
Android 端还依赖 Xiaomi Wearable SDK 1.4。该 SDK 不随本仓库分发；请从小米 Vela 官方的 Interconnect 开发测试 Demo 获取 `xms-wearable-lib_1.4_release.aar`，并放到：

```text
Android/app/libs/xms-wearable-lib_1.4_release.aar
```

GitHub Actions 会在构建时自动从小米官方 Demo 获取该文件。

在本目录运行：

```bash
./gradlew --no-daemon :app:assembleDebug
```

Windows 可使用：

```bat
gradlew.bat --no-daemon :app:assembleDebug
```

正式 APK 必须由作者离线保管的正式密钥签名。私钥、密码和本地签名配置不会进入仓库或 CI。

## 安装与核验

- 最低系统：Android 8.0（API 26）
- 目标系统：API 35
- 正式证书 SHA-256：

```text
6A:06:8C:3D:40:95:25:1F:E7:1D:00:09:FC:06:51:30:C5:E6:62:BA:32:97:ED:A8:C3:8F:CE:94:CA:70:76:4F
```

Android 只有在包名和签名证书均一致时才允许覆盖更新。安装包及校验值请从项目 [Releases](https://github.com/241120nzdjjx/StringArtHelper/releases) 页面下载。

## 目录结构

```text
Android/
├── app/                    应用源码与资源
├── gradle/wrapper/         Gradle Wrapper
├── docs/                   Android 发布与签名说明
├── CHANGELOG.md            Android 中文更新记录
├── CHANGELOG.en.md         Android English changelog
└── README.md / README.en.md
```

完整版本记录见 [CHANGELOG.md](./CHANGELOG.md)。隐私、安全与许可说明见 [PRIVACY.md](./PRIVACY.md)、[SECURITY.md](./SECURITY.md)、[LICENSE](./LICENSE) 和 [NOTICE](./NOTICE)。
