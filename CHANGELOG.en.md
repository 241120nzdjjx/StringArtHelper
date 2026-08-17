<p align="center">
  <a href="./CHANGELOG.md">简体中文</a> | <strong>English</strong>
</p>

# Changelog

This file records the major changes in official Android releases. See the Git commit history for the complete development history.

## 0.1.0 (first PC desktop release)

- Added the `PC/` directory: an Electron desktop version that extracts the core capabilities from the Android source instead of rewriting the project.
- Ported the greedy generator 1:1 from Android's `StringArtGenerator.java` to dependency-free JS (256×256 work area, thread width/opacity model, recent-pin avoidance, auto-stop). Generation runs in a worker thread, keeping the UI responsive with live per-line preview.
- Redesigned for the desktop: images, `.txt` sequences and `.sar`/`.bin` saves can be dropped straight into the window; the square crop box is draggable, wheel-zoomable and double-click resets it.
- Sequence player: previous / next / play-pause / jump / replay with adjustable step delay, thread display in actual ratio or a custom diameter, nail-number labels, wheel-zoom and drag-pan, and optional system-voice narration.
- Saves are byte-compatible with Android and the Mini Program (SAR2/SAR3/SAR4 and `.bin`); projects auto-save with 192×192 monochrome PNG thumbnails; deletion uses the OS trash.
- Exports Android-style TXT sequences (with the physical-parameter header) and tiled A4 nail-template PDFs (trim lines, alignment marks, 100 mm calibration ruler).
- Dark theme reuses the Android palette; bilingual UI; `npm test` covers SAR/TXT/generator/PDF including byte-compatibility fixtures shared with the Mini Program suite.

## 26.5.9 (versionCode 72)

- Restored standard Chinese cardinal-number wording from 0 through 1000; for example, 119 is spoken as “one hundred nineteen” and 171 as “one hundred seventy-one.”
- Added Faster and Slower shortcuts to the Android home screen. Each tap shortens or extends the post-speech delay by 0.5 seconds.
- Added Faster and Slower mappings for phone volume keys and wearable wrist gestures.
- Added explicit speed controls to the wearable home and settings pages. Changes sync to the phone when connected and directly reschedule offline playback.
- Updated both Android and wearable apps to 26.5.9.

## 26.5.8 (versionCode 71)

- Fixed `.sar` saves still showing “Import” and not opening immediately after import.
- Both `.bin` and `.sar` saves now consistently use “Import and Open”; duplicate-name keep-both and replace paths also open the imported project immediately.
- Synchronized the companion wearable version to 26.5.8 with no functional code changes.

## 26.5.7 (versionCode 70)

- Added a note to the save manager explaining that saves can be synced to a Xiaomi Smart Band for phone-free use.
- Chinese nail numbers are now passed to TTS digit by digit; for example, 119 is spoken as “one-one-nine” instead of a cardinal number.
- Updated the companion wearable app to 26.5.7.

## 26.5.6 (versionCode 69)

- Swapped the displayed inward/outward wrist-turn labels without changing the existing gesture mapping logic.
- A successful tap on Next on the phone now triggers long vibration feedback on the band when enabled.
- Updated the companion wearable app to 26.5.6.

## 26.3.5 (versionCode 61)

- Changed the confirmation action for importing `.bin` project saves to “Import and Open,” so the imported project opens immediately and work can continue.
- Added the same import-and-open behavior to both the “Keep Both” and “Replace” paths when a `.bin` save has the same name as an existing project.
- Added the Mini Program code, complete WeChat entry text, and a copy button to the “WeChat Mini Program” menu.

## 26.3.4 (versionCode 60)

- Added an estimated remaining time to the home screen.
- The estimate dynamically adjusts based on the remaining steps, voice-guidance settings, and the actual working pace.
- Estimates under one minute are shown as “Less than one minute remaining”; longer estimates are displayed in minutes or hours and minutes.
- Pausing, switching to the background, stepping backward, and jumping between steps do not affect pace calculations.

## 26.3.3 (versionCode 59)

- Added information about the open-source GitHub repository and a clickable link to the About page.
- Added information about the WeChat Mini Program, cross-platform project compatibility, and privacy to the README.

## 26.3.2 (versionCode 58)

- Renamed the first-use entry and top button on the home screen to “Import.”
- Import now automatically recognizes TXT pin sequences and `.bin` project saves, while retaining compatibility with `.sar` saves.
- Unsupported file formats now display a clear message.

## 26.3.1 (versionCode 57)

- Added a WeChat Mini Program entry to the More page; search for “绕线画助手” in WeChat to use it.
- Added cross-platform project continuation between the Android app and WeChat Mini Program through project saves and TXT sequences.
- Adjusted the home-screen button layout and wording for a consistent visual style.
- Added one-tap copying of the author's Genshin Impact UID to the Feedback and Contact page.
- Fixed several menu-layout and interface details.
