# 生成算法 1:1 移植审查（generator.js ↔ StringArtGenerator.java）

审查日期：2026-08-18
结论：**算法逻辑与数值语义均与 Android 参考实现一致；相同输入下生成的序列与真实 Java 输出逐位相同（两组用例零差异）。**

## 审查方式

1. 将 Android `app/src/main/java/com/niuza/stringartreader2/StringArtGenerator.java` **1:1 复制**为 `tests/java/GenCore.java`（唯一改动：`android.graphics.Bitmap` 换成 `int[] pixels + width + height`，`getPixel()` 换成数组下标，其余逐行未动）。
2. 用同一份合成图（256×256 暗盘/半平面、ARGB 与 RGBA 等价）分别驱动 **Java** 与 **JS** 生成器，对比序列。
3. `npm run test:java` 固化该对照（需本机 JDK）。

## 对照结果

| 用例 | 参数 | Java | JS | 一致 |
| --- | --- | --- | --- | --- |
| CASE1（暗盘） | 220 钉 · 800 线 · 260mm · 0.2mm · autoStop | 801 步 | 801 步 | ✅ 逐位相同 |
| CASE2（半平面+裁剪） | 160 钉 · 500 线 · crop(0.25,0.5,1.6) | 501 步 | 501 步 | ✅ 逐位相同 |

## 关键实现点（float32 语义）

Android 全程使用 Java `float`（32 位 IEEE-754），JS 原生是 `double`。为做到"运算级 1:1"，JS 端在每个 Java float 运算处用 `Math.fround` 包裹，residual 数组用 `Float32Array`（等价 Java `float[]`）：

| 位置 | Java | JS 模拟 |
| --- | --- | --- |
| residual 存储 | `float[]` | `Float32Array` |
| 采样坐标 `round(y0 + y/(size-1)·(crop-1))` | float 链式运算 | 每步 `F()` |
| luminance（0.2126f/0.7152f/0.0722f 常量） | float32 常量 + float 运算 | `F(0.2126)` 等 + 每步 `F()` |
| `squaredErrorGain` / `scoreLine` | float 累加 | `F(score + F(...))` |
| `subtractLine`（法线/覆盖率/fringe） | float | `F()` |
| 钉子坐标 `round(center + (float)cos(angle)·radius)` | cos 转 float | `F(F(Math.cos(angle))·radius)` |
| `threadWidthPx/threadOpacity/lineDarkness` | float | `F()` |
| `threadMm` / `areaMm2` / autoStop 覆盖率 | Java `double` | 保持 double ✅ |

## 已知差异（不影响一致性）

| 差异 | 说明 |
| --- | --- |
| 进度回调节流 | Android 每步回调；JS 每 16 步批量（仅限 IPC 流量，不影响算法输出） |
| 路径缓存策略 | Android 全量 `int[pinCount²][]` 矩阵；JS 用 LRU（`makePath` 确定性 → 淘汰重算结果不变） |
| 输入像素格式 | Android 直接读 `Bitmap` ARGB；JS 接收 RGBA 字节（`buildResidual` 内等价换算） |

## 跨端字节兼容（SAR / TXT）

- `tests/interop.test.js`：PC 与微信小程序原版模块交叉验证 **SAR4 编码逐字节相同**、TXT 导出逐字节相同、文件名规则/元数据正则逐字符相同（10 项）。
- `tests/fixtures/`：SAR2/3/4 十六进制夹具解码/编码字节一致（与小程序测试同源）。
- 真实设备存档（SAR4，220 钉/260mm/4255 步）由 PC 解码正确。
