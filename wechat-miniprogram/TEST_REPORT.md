# StringArtHelper 微信小程序版测试报告

测试日期：2026-07-31

源码版本：`v1.1.3`（待下一次合并提交）

## 环境

- Windows 10 x86-64
- 微信开发者工具 Stable `2.01.2510290`
- 基础库 `3.7.12`
- 项目类型：原生微信小程序
- 运行方式：本地开发者工具模拟器

## 结果

- JavaScript 语法检查：通过
- JSON 配置解析：通过
- 核心单元测试：通过
- 静态项目审计：通过
- 开发者工具普通编译：`v1.1.3` 通过
- 开发者工具“问题”面板：0 项
- 正式首页加载：通过
- 运行时自检：7/7 通过
  - TXT 序列解析
  - PDF 模板生成
  - 本地存储读写
  - Canvas 2D 绘制与像素读取
  - Worker 中运行绕线生成核心
  - 预览物理比例与跟随缩放
  - 内置离线数字语音资源加载与播放

`v1.1.0` 的开发者工具运行时自检为 7/7。`v1.1.2` 预览和窄屏布局修复完成后，
JavaScript、JSON、核心测试和静态审计均通过，开发者工具“问题”面板为 0 项。
本次模拟器重新启动被微信账号登录过期拦截，错误为“需要重新登录”，没有代替用户登录或修改账号。

`v1.1.3` 新增裁切预览与实际导出像素的双重灰阶保证，并精简为独立的微信版“关于”页。
生成核心改为弦像素缓存、平方误差下降、最近钉位抑制和线径自适应暗度模型；自动停止不再依赖经验覆盖率阈值。
本轮核心测试、静态项目审计、20 个 JavaScript 文件语法检查和 12 个 JSON 配置解析均通过。
开发者工具模拟器已载入 `v1.1.3` 首页，普通编译通过且“问题”面板为 0 项；未上传该版本。
当前 RC 版在 Windows 10 上不支持控制接口的窗口截图，嵌套模拟器页面未通过自动点击继续操作。

## 性能抽样

新核心在本机 Node 抽样中：`192 × 192`、200 钉、最多 3000 线约 `60 ms`，自动停止于 696 线；500 钉、最多 500 线约 `116 ms` 并完成 500 线。结果仅用于回归比较，不等同于不同手机上的实际耗时。

## 尚需真机完成

- 相册和相机授权流程
- TXT/PDF 文件分享
- 横竖屏手势体验与不同尺寸手机适配
- 使用正式 AppID 的预览、体验版和上传发布

当前版本内置约 83 KB 的中英文数字语音包，可完全离线播报 0–99999。微信小程序仍无法监听 Android 版使用的蓝牙媒体键。
# v1.2.0 Test 2 addendum

- Added WeChat friend-card and Moments share callbacks on Home and About, plus explicit share buttons.
- Added the Android open-source repository to About.
- Replaced the bundled number voices with CC BY-SA human recordings from `hugolpz/audio-cmn` and `Jakobovski/free-spoken-digit-dataset`; all 24 MP3 files decode successfully.
- English pin numbers are intentionally spoken digit by digit for consistent human-voice playback.
- Cached the completed preview thread layer and reduced large-array/high-frequency `setData` traffic.
- Chunked TXT thumbnail drawing so long sequences yield back to the UI thread.
- `npm test`: passed after the share and performance changes. The final repeat after voice substitution was blocked by the desktop permission service; all 24 final MP3 assets were decoded successfully, and the voice token expectations were updated, but that last full runner invocation remains unverified.
- WeChat DevTools GUI compilation of this updated workspace remains unverified because its screen-capture interface failed and permission review rejected opening the C-drive workspace. No upload was attempted.

## v1.2.0 Test 3 addendum

- Increased the default offline announcement rate to 1.25x and added a persistent 0.75x-1.60x voice-speed slider.
- Removed the board-size field from generator settings while retaining derived/internal compatibility values for PDF and legacy project formats.
- Added a live reader canvas: completed threads are incrementally cached in black, and the current thread is drawn in purple at exactly 1.5x the calculated physical thread width.
- Full preview animation now starts at the current project index, holds its final frame after completion, and restores the saved live-progress frame before the next control or button performs its normal action.
- Chinese and English UI strings were added for the live preview and the two independent speed controls.
- JavaScript syntax checks passed for all changed JavaScript files.
- Full automated suite passed: original regression; algorithm/TXT/SAR/crop snapping; file-backed storage and migration; static project audit.
- The already-open WeChat DevTools window requested for GUI verification was not present in the current Windows session, so Test 3 has not been recompiled or visually inspected in DevTools. No new DevTools instance was launched, and no upload, review submission, or release was attempted.

## v1.2.0 Test 4 addendum

- Changed the reader live preview into a draggable floating window with minimize, fullscreen and restore modes.
- Added extra bottom safe-area spacing so the final control row remains reachable above system gesture areas.
- Confirmed the Mandarin source recordings were still cached locally; no network download was required.
- Root cause of the broken Mandarin clips: fixed-threshold `silenceremove` reduced low-volume 三, 六 and 百 recordings to 0.09-0.17 second fragments.
- Rebuilt all 14 Mandarin clips from the locally cached original CC BY-SA recordings without threshold-based silence removal.
- All final Mandarin files decode successfully and are 0.885-1.135 seconds long; 三, 六 and 百 are now 1.085, 0.986 and 1.135 seconds.
- Added a regression audit that rejects any missing or suspiciously truncated Mandarin token.
- Full automated suite passed after the UI and voice repairs.

## v1.2.0 Test 5 addendum

- Restored the generated-result preview to its original independent behavior: it opens on the complete result, plays its own animation from the beginning, and no longer owns the live-progress/final-hold state.
- Removed the always-visible draggable preview window from the reader page.
- Kept one preview button at the bottom of the reader. It opens a single fullscreen canvas that combines live progress and full animation.
- The reader's full animation holds its final frame. The next preview control restores the actual reader index before performing its normal action.
- Minimizing the combined preview returns to the reader and its bottom preview button.
- The fullscreen reader preview is vertically scrollable and uses a height-aware canvas size so its animation controls remain reachable in landscape.
- Full automated suite and JavaScript syntax checks passed.
