<p align="center">
  <a href="./CHANGELOG.md">简体中文</a> | <strong>English</strong>
</p>

# Changelog

This file records the major changes in official Android releases. See the Git commit history for the complete development history.

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
