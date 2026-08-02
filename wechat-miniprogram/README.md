# 绕线画助手｜StringArtHelper 微信小程序版

> 受限于微信小程序审核 此仓库中的小程序版本可能高于实际线上版本 实际使用请以线上版为准

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

# StringArtHelper | WeChat Mini Program

> Due to the WeChat Mini Program review process, the source code in this repository may be newer than the version currently available online. For actual use, refer to the version published on WeChat.

A free, open-source string art generation and creation assistant that runs entirely on the local device.

Current version: **v1.2.2**

## Main Features

- Generate string art pin paths from images
- Grayscale preprocessing, circular cropping, dragging, and two-finger zooming
- Support for 100–500 pins
- Custom maximum string segment count, string diameter, and pin-circle diameter
- Automatic prevention of excessively dark results
- Generation-process animation and final-result preview
- Real-time preview of the current stringing progress
- Previous step, next step, step jump, and automatic playback
- Display of the previous, current, and next pin numbers
- Offline number voice guidance in Chinese and English
- Automatic progress recovery and manual project saves
- Import and export of SAR project saves compatible with the Android app
- Import and export of TXT stringing sequences containing physical parameters
- Generation of A4 PDF pin templates at true physical dimensions in millimetres
- Chinese and English interfaces with portrait and landscape support

## Privacy and Offline Operation

StringArtHelper does not depend on a server.

Image processing, path calculation, project saves, and voice guidance are all performed locally. The Mini Program does not actively upload selected images, stringing sequences, or project-save contents.

## File Import and Export

Supported import formats:

- `.txt`: string art pin-number sequences
- `.sar`: StringArtHelper project saves
- `.bin`: compatible project saves containing the same data as SAR files

Because of WeChat platform restrictions when selecting non-image files:

> To upload a TXT or BIN file, first forward it to a WeChat chat, then select it from Chat Files.

To save an exported file to the device:

> To download a TXT or BIN file, first forward it to a WeChat chat, then download it from the chat.

## TXT Format

TXT files officially exported by StringArtHelper contain:

- Pin count
- String diameter
- Pin-circle diameter
- Complete pin-number sequence

Example:

```text
# StringArtHelper export
# Pin count: 300
# String diameter: 0.20 mm
# Pin-circle diameter: 260 mm
# Pin numbering: Pin 0 is at the rightmost point; numbers increase clockwise
# Total pin entries: 5001

0 → 87 → 162 → 39 → 204 → 11
```

The importer also accepts common formats that use spaces, commas, line breaks, arrows, or numbered steps.

## SAR Project-Save Compatibility

The Mini Program can read:

- SAR2
- SAR3
- SAR4

The official export format is SAR4, which is compatible with the Android version of StringArtHelper.

A project save contains:

- Project name
- Current progress
- Pin count
- Pin-circle diameter
- String diameter
- Thumbnail
- Complete pin-number sequence

## PDF Pin Templates

PDF pin templates are generated at true physical dimensions in millimetres. Large templates are automatically divided across multiple pages for assembly.

When printing, select:

> Actual size / 100%

Do not select “Fit to page” or any automatic scaling option. After printing, use the 100 mm calibration ruler included in the PDF to verify the scale.

## Project Management

There are two types of project saves:

- **Automatic progress recovery:** continuously records the latest progress of the current project
- **Manual save:** a fixed snapshot created by the user that is not overwritten by later progress

If you load a manual save and continue working, a new automatic recovery project is created. The original manual save remains unchanged.

## Development and Testing

This is a native WeChat Mini Program project and can be opened directly in WeChat DevTools.

Requirements:

- WeChat DevTools
- A WeChat base library supporting Canvas 2D, Workers, and local file APIs
- Node.js, required only for automated tests

Run the tests with:

```bash
npm test
```

The project does not depend on a remote server and does not require a backend address.

## Android Version

Source code, version history, and release information for the Android version of StringArtHelper:

[GitHub: StringArtHelper](https://github.com/241120nzdjjx/StringArtHelper)

## Feedback and Contact

If you have a suggestion, encounter a problem, or find any part of the Mini Program difficult to use, feel free to get in touch:

- Email: 241120nzdjjx@gmail.com
- [Bilibili](https://b23.tv/K3Cp0ZZ)

## Open-Source License

This project is released under the [GNU General Public License v3.0](../LICENSE).

You may use, study, modify, and distribute the source code under the terms of the license. When distributing a modified version, you must comply with the open-source obligations of GPL v3.0 and preserve the original license and copyright notices.
