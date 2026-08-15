/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 */
package com.niuza.stringartreader2;

import java.util.HashMap;
import java.util.Map;

/** Central text mapping for the programmatic UI. Chinese remains the source locale. */
final class EnglishText {
    private static final Map<String, String> EXACT = new HashMap<String, String>();
    static {
        put("生成图片", "Generate image"); put("导入", "Import");
        put("预览", "Preview"); put("关预览", "Hide preview"); put("更多", "More");
        put("更多功能", "More tools");
        put("手环控制", "Watch control");
        put("小米手环控制", "Xiaomi watch control");
        put("允许小米手环控制播报器", "Allow Xiaomi watch to control the reader");
        put("重新检测连接", "Check connection again");
        put("在手环上打开应用", "Open app on watch");
        put("请先开启手环控制", "Turn on watch control first");
        put("存档", "Projects"); put("前一个", "Previous"); put("当前", "Current");
        put("下一个", "Next"); put("跳转", "Jump"); put("上一步", "Previous");
        put("下一步", "Next"); put("重播", "Replay"); put("播放", "Play");
        put("暂停", "Pause"); put("实时预览", "Live preview");
        put("快一点", "Faster"); put("慢一点", "Slower");
        put("已经是最快间隔", "Already at the shortest delay");
        put("已经是最慢间隔", "Already at the longest delay");
        put("完整动画", "Full animation"); put("动画速度", "Animation speed");
        put("速度", "Speed");
        put("实际比例", "Actual scale"); put("预览线径", "Preview thread diameter");
        put("缩小", "Minimize"); put("退出全屏", "Exit fullscreen"); put("关闭", "Close");
        put("取消", "Cancel"); put("保存", "Save"); put("放弃", "Discard");
        put("重新调整", "Adjust"); put("开始生成", "Generate");
        put("选择图片", "Choose image"); put("选择保存位置", "Choose save location");
        put("覆盖", "Overwrite"); put("删除", "Delete");
        put("图片生成绕线画", "Generate string art");
        put("重新裁切图片", "Crop again"); put("图片 → 绕线序列", "Image → string sequence");
        put("生成预览", "Generation preview"); put("TXT 导入预览", "TXT import preview");
        put("正在本地生成", "Generating locally");
        put("生成匹配的钉位模板 PDF", "Generate matching nail-template PDF");
        put("生成钉位模板 PDF", "Generate nail-template PDF");
        put("导出本次生成的 TXT", "Export this TXT"); put("载入播报器", "Open reader");
        put("导入并打开", "Import and open");
        put("更多", "More");
        put("导出当前 TXT", "Export current TXT");
        put("导出钉位图 PDF", "Export nail diagram PDF");
        put("关于与反馈", "About & feedback"); put("关于", "About");
        put("分享应用", "Share app"); put("支持作者", "Support author");
        put("微信小程序版", "WeChat Mini Program");
        put("反馈联系", "Contact"); put("反馈与联系", "Feedback & contact");
        put("版本与开源信息", "Version & open-source details");
        put("GitHub 开源仓库", "GitHub repository");
        put("项目管理", "Project manager"); put("自动项目（点项目直接进入）", "Auto projects (tap to open)");
        put("手动存档（点项目直接进入）", "Manual saves (tap to open)"); put("暂无", "None yet");
        put("🔄 自动续做", "🔄 Auto-resume projects");
        put("📌 手动存档", "📌 Manual snapshots");
        put("📌 保存当前", "📌 Save current");
        put("📥 导入存档", "📥 Import save");
        put("暂无存档", "No saves yet");
        put("新建存档", "New manual save"); put("读取这个存档", "Open this save");
        put("用当前进度覆盖", "Overwrite with current progress"); put("重命名", "Rename");
        put("导出存档", "Export save"); put("分享存档", "Share save");
        put("分享安装包", "Share APK");
        put("导入存档", "Import save"); put("保留两份", "Keep both");
        put("保留两份并打开", "Keep both and open");
        put("覆盖同名存档", "Replace same-name save");
        put("覆盖并打开", "Replace and open");
        put("删除这个存档", "Delete this save"); put("覆盖存档？", "Overwrite save?");
        put("重命名存档", "Rename save"); put("删除存档？", "Delete save?");
        put("删除存档失败", "Could not delete the save"); put("已删除", "Deleted");
        put("出现问题", "Something went wrong"); put("知道了", "OK");
        put("跳转进度", "Jump to step");
        put("快捷：等待间隔", "Quick: delay"); put("快捷：播报语速", "Quick: speech rate");
        put("播报设置", "Speech settings"); put("回到开头", "Back to start");
        put("语言", "Language"); put("界面语言", "Interface language");
        put("跟随系统", "Follow system");
        put("每个钉号重复播报两次", "Repeat each nail number twice");
        put("TTS 音量", "TTS volume");
        put("用音量键换步（音量+ 上一步，音量− 下一步）",
                "Use volume keys to change steps (Volume+ previous, Volume− next)");
        put("🎵 媒体声音", "🎵 Other media");
        put("播报数字时降低其他媒体声音",
                "Lower other media while speaking a number");
        put("由 Android 在播报瞬间暂时压低音乐，数字读完后立即恢复；不同播放器的降低幅度可能略有差异。",
                "Android briefly ducks music as each number starts and restores it as soon as speech ends. The amount may vary slightly between media players.");
        put("回到实际比例（按线径 ÷ 钉位圆显示）",
                "Use actual scale (thread diameter ÷ nail circle)");
        put("绕线助手 · 作者与反馈", "String Art Helper · Author & feedback");
        put("Bilibili：牛杂の经济学", "Bilibili: 牛杂の经济学");
        put("点一下即可跳转至 bilibili", "Tap to open the Bilibili app");
        put("邮箱：241120nzdjjx@gmail.com", "Email: 241120nzdjjx@gmail.com");
        put("点一下即可跳转至邮箱", "Tap to open your email app");
        put("推特（X）：@nzdjjx241120", "X (Twitter): @nzdjjx241120");
        put("点一下即可跳转至 X", "Tap to open X");
        put("Telegram：@nzdjjx", "Telegram: @nzdjjx");
        put("点一下即可跳转至 Telegram", "Tap to open Telegram");
        put("或者找作者玩原神🤓☝️ UID：305028021", "Or play Genshin Impact with the author 🤓☝️ UID: 305028021");
        put("点一下即可复制 UID", "Tap to copy UID");
        put("已复制到剪贴板，原神启动！", "Copied to clipboard. Genshin, launch!");
        put("自动防全黑（达到安全墨量后提前停止）",
                "Prevent over-darkening automatically (stop at the safe ink limit)");
        put("\n反馈与联系", "\nFeedback & contact");
        put("导入", "Import"); put("导入\nTXT", "Import\nTXT");
        put("尚未导入序列", "No sequence loaded");
        put("钉位模板 PDF 已保存", "Nail-template PDF saved");
        put("TXT 序列已导出", "TXT sequence exported");
        put("未找到所选语言的语音，已使用系统默认语音",
                "Voice data for the selected language was not found; using the system default voice");
        put("请先导入 TXT 序列", "Import a TXT sequence first");
        put("系统语音尚未准备好", "System speech is not ready yet");
        put("已经到最后一个钉号", "Already at the last nail number");
        put("已经在第一个钉号", "Already at the first nail number");
        put("系统文字转语音初始化失败", "System text-to-speech initialization failed");
        put("序列播报完成", "Sequence playback complete");
        put("语音播报失败", "Speech playback failed");
        put("已取消生成", "Generation cancelled");
        put("已载入播报器", "Opened in reader");
        put("已导入并打开播报器", "Imported and opened in reader");
        put("还没有可导出的序列", "There is no sequence to export");
        put("没有可导出的生成结果", "There is no generated result to export");
        put("已覆盖存档", "Save overwritten");
        put("已重命名", "Renamed");
        put("存档已导出，可在其他设备的项目管理中导入",
                "Save exported. Import it from Project manager on another device.");
        put("请输入数字", "Enter a number");
        put("请输入存档名", "Enter a save name");
        put("没有识别到有效的钉号序列。TXT 可使用换行、空格、逗号或 0 → 87 格式。",
                "No valid nail-number sequence was found. TXT may use line breaks, spaces, commas, or the format 0 → 87.");
        put("无法读取", "Unavailable");
    }
    private static void put(String zh, String en) { EXACT.put(zh, en); }
    private EnglishText() { }
    static String translate(String value) {
        String exact = EXACT.get(value);
        if (exact != null) return exact;
        String out = value;
        out = out.replace("当前项目", "Current project").replace("尚未打开项目", "No project open")
                .replace("可直接从下方自动项目或手动存档恢复", "Open an auto or manual save below")
                .replace("可从下方存档恢复，或导入其他设备的 .sar 存档",
                        "Open a save below, or import a .sar save from another device")
                .replace("将当前进度另存为手动存档", "Save current progress as manual save")
                .replace("存档进度：第 ", "Saved progress: step ")
                .replace("第 ", "Step ").replace(" 步", "")
                .replace("自动项目", "Auto project").replace("手动存档", "Manual save")
                .replace("钉子数", "Nails").replace("绕线步数", "Strings")
                .replace("钉位圆直径", "Nail circle diameter")
                .replace("线的直径 / 粗细", "Thread diameter / thickness")
                .replace("等待间隔", "Delay").replace("播报语速", "Speech rate")
                .replace("TTS 音量：", "TTS volume: ")
                .replace("间隔 ", "Delay ").replace("语速 ", "Rate ")
                .replace("已加快，等待 ", "Faster · delay ")
                .replace("已减慢，等待 ", "Slower · delay ")
                .replace("界面语言　当前：", "Interface language  Current: ")
                .replace("　当前：", "  Current: ").replace("当前：", "Current: ")
                .replace("预览线条粗细", "Preview line width")
                .replace("实际比例（线径/钉位圆）", "Actual scale (thread/circle)")
                .replace("自定义", "Custom").replace(" 秒", " s").replace("秒", "s")
                .replace("每个钉号播报完后等待：", "Wait after each nail number: ")
                .replace("当前语速：", "Current speech rate: ")
                .replace("正在计算第 ", "Calculating string ")
                .replace(" 根线", " strings").replace("钉号", "nail numbers")
                .replace("黑色：已完成", "Black: completed").replace("紫色：当前正在绕的线", "Purple: current thread")
                .replace("实时预览 · 紫色为当前线段", "Live preview · purple is the current thread")
                .replace("点当前数字重播；长按当前数字或点“跳转”输入步骤", "Tap the current number to replay; long-press it or tap Jump to enter a step")
                .replace("黑白预览 · 拖动取景 · 双指缩放", "Monochrome preview · drag to frame · pinch to zoom")
                .replace("拖动取景 · 双指缩放", "Drag to frame · pinch to zoom")
                .replace("点一下即可跳转至", "Tap to open ");
        out = out.replace("导出 TXT 失败：", "TXT export failed: ")
                .replace("读取图片失败：", "Could not read the image: ")
                .replace("图片无法使用：", "The image cannot be used: ")
                .replace("识别到的数字过多（", "Too many numbers were recognized (")
                .replace(" 个）。", ").")
                .replace("读取文件失败：", "Could not read the file: ")
                .replace("解析文件失败：", "Could not parse the file: ")
                .replace("旧项目自动保存失败，已取消导入：",
                        "The previous project could not be saved; import was cancelled: ")
                .replace("本地生成失败：", "Local generation failed: ")
                .replace("旧项目自动保存失败，未替换当前项目：",
                        "The previous project could not be saved; the current project was kept: ")
                .replace("读取存档失败：", "Could not open the save: ")
                .replace("无法导入存档：", "Could not import the save: ")
                .replace("导入存档失败：", "Save import failed: ")
                .replace("导出存档失败：", "Save export failed: ")
                .replace("准备导出存档失败：", "Could not prepare the save for export: ")
                .replace("准备分享存档失败：", "Could not prepare the save for sharing: ")
                .replace("覆盖失败：", "Overwrite failed: ")
                .replace("保存失败：", "Save failed: ")
                .replace("重命名失败：", "Rename failed: ");
        return out;
    }
}
