# 绕线助手 / String Art Helper

免费、开源、无广告、完全离线的 Android 绕线画制作工具。
从图片裁切、生成绕线序列和钉位模板，到逐步预览、语音播报与项目存档，都在手机本地完成。

## 主要功能

- 本地图片裁切，带边缘吸附和黑白最终区域预览
- 100～500 颗钉子、最多 20,000 根线的本地序列生成
- 圆径支持 80～1200 mm；界面提示板材四边至少预留 10 mm
- 生成过程逐线可视化，以及 1×～20× 动画速度
- 结果页调整钉数、最大线数、圆径、线径与自动防全黑
- TXT 序列导入、导出和中英文数字 TTS 播报
- 播报时可请求 Android 临时降低其他媒体声音
- 音量键、蓝牙耳机和媒体键换步
- 可缩放、移动、最小化和全屏的实时预览
- 当前线段紫色高亮，线径可按真实毫米比例显示
- 自动续做项目、手动快照，以及 `.sar` 存档导入、导出和分享
- 自动生成最少 A4 拼页的钉位模板 PDF，包含裁切线、对齐标记与 100 mm 校准尺
- 中英文界面，支持横屏和竖屏
- 分享当前安装包给朋友

## 隐私

应用不申请网络权限，不包含广告或统计 SDK。图片、TXT 和 `.sar` 存档只在本机处理，不会由应用上传。Android 系统云备份已禁用。打开 Bilibili、X、Telegram、邮箱或第三方生成网站时，会交给对应外部应用或浏览器处理。

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

## 构建

要求：

- JDK 17
- Gradle 8.9
- Android Gradle Plugin 8.7.3
- Android SDK 35 / Build Tools 35.0.0

```bash
gradle --no-daemon :app:assembleDebug
```

GitHub Actions 会对公开源码执行 Debug 与未签名 Release 构建，并把 APK 作为工作流构件上传。正式 APK 必须使用作者离线保管的官方密钥签名；私钥和密码不会进入仓库或 CI。

发布说明见 [docs/RELEASE_SIGNING.md](docs/RELEASE_SIGNING.md)。

## 项目信息

- 当前版本：`26.3.0`（versionCode 56）
- 最低 Android：8.0（API 26）
- 目标 Android：API 35
- 许可证：GNU GPL v3.0 only，另有兼容的第 7 条附加条款
- 作者：牛杂の经济学

版本号按年份命名：2026 年为 `26.x.x`；功能更新递增次版本，修复更新递增补丁版本，Android `versionCode` 独立递增。

修改版必须提供对应源码、保留合理声明并明确标注修改，不能冒充官方构建。详见 [LICENSE](LICENSE) 与 [ADDITIONAL_TERMS.md](ADDITIONAL_TERMS.md)。

## 反馈与联系

- Bilibili：[牛杂の经济学](https://b23.tv/K3Cp0ZZ)
- 邮箱：241120nzdjjx@gmail.com
- X：[@nzdjjx241120](https://x.com/nzdjjx241120)
- Telegram：[@nzdjjx](https://t.me/nzdjjx)

普通问题可使用 GitHub Issues；安全问题请阅读 [SECURITY.md](SECURITY.md)。
