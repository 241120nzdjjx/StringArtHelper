<p align="center">
  <a href="./README.md">简体中文</a> | <strong>English</strong>
</p>

<p align="center">
  <a href="../README.en.md">Project Home</a> · <a href="../Android/README.en.md">Android</a> · <a href="../wechat-miniprogram/README.en.md">wechat mini Program</a> · <strong>Xiaomi Smart Band</strong>
</p>

# StringArtHelper for Xiaomi Smart Band

Current version: **v26.5.9 (versionCode 72)**  
Target device: **Xiaomi Smart Band 9 Pro (336 × 480)**  
Validated firmware baseline: **HyperOS 3.1.175**

The wearable app accompanies the [Android edition](../Android/README.en.md) and uses the same package name, `com.niuza.stringartreader2`. While connected, it controls Android and reads live state. After saves are synchronized, it can also change steps and preserve progress without the phone.

## Main Features

- Previous, current, and next pin numbers, total progress, and connection state
- Previous, next, play/pause, replay, and jump controls
- Faster and Slower controls on the home screen, with matching wait-time actions in settings, key mappings, and wrist gestures
- Save list, save synchronization, and offline work
- Progress preview with pan, zoom, reset, and refresh
- Project details including progress, pin count, circle diameter, string diameter, estimated string length, and data source
- Configurable volume-button mappings, taps, swipes, and experimental wrist-turn controls
- Step vibration feedback that follows wearable settings

## Compatibility Requirements

- The Android APK and wearable RPK should use the same package name, official certificate, and version.
- This release uses v26.5.9 (72) on both Android and wearable.
- On first use, enable wearable control in Android and run “Synchronize all saves.”
- Even without an offline save, the band can display the phone's current save state while the phone connection is available.

## Build

Requirements: Node.js, npm, and the environment required by `aiot-toolkit 2.0.5`.

```bash
npm ci
npm run build
```

An official interoperable RPK must be signed with the same official private key as the Android APK. Private keys, passwords, PEM/JKS files, `node_modules`, `build`, and `dist` must not be committed.

## Directory Layout

```text
Wearable/
├── src/app.ux              Application entry point
├── src/manifest.json       Package, version, and page declarations
├── src/pages/              Home, saves, preview, jump, details, and settings
├── src/i18n/               Chinese, English, and default text
├── package.json            Build commands and tool versions
└── README.md / README.en.md
```

Source in this directory is covered by the repository's [GNU GPL v3.0](../LICENSE), [additional terms](../ADDITIONAL_TERMS.md), and [NOTICE](../NOTICE).
