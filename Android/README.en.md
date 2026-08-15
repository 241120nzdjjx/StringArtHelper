<p align="center">
  <a href="./README.md">简体中文</a> | <strong>English</strong>
</p>

<p align="center">
  <a href="../README.en.md">Project Home</a> · <strong>Android</strong> · <a href="../wechat-miniprogram/README.en.md">wechat mini Program</a> · <a href="../Wearable/README.en.md">Xiaomi Smart Band</a>
</p>

# StringArtHelper for Android

Current official version: **v26.5.9 (versionCode 72)**  
Package name: `com.niuza.stringartreader2`

The Android edition provides the complete image-to-string-art workflow, live previews, offline voice guidance, save management, PDF pin templates, and Xiaomi Smart Band 9 Pro management.

## Main Features

- Local image cropping and path generation for 100–500 pins and up to 20,000 string segments
- Step-by-step generation animation, zoomable live preview, and current-segment highlighting
- Offline Chinese and English nail guidance; Chinese values from 0 through 1000 use standard cardinal-number wording
- Faster and Slower shortcuts adjust the post-speech delay directly and can be mapped to volume keys or wearable wrist gestures
- Step control through volume buttons, Bluetooth headsets, media buttons, and on-screen controls
- Automatic recovery, manual snapshots, and TXT, `.bin`, and `.sar` import/export
- Automatic A4 tiled PDF templates with cutting lines, alignment marks, and a 100 mm calibration ruler
- Xiaomi Smart Band connection status, step feedback, and offline-save synchronization
- Chinese and English interfaces in portrait and landscape

## Cross-platform Use

- The [WeChat Mini Program](../wechat-miniprogram/README.en.md) exchanges TXT, `.bin`, and `.sar` files with Android.
- The [Xiaomi Smart Band app](../Wearable/README.en.md) reads live status and receives saves through Android. Synchronized saves remain usable without the phone.

## Build
Requirements: JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3, and Android SDK 35 / Build Tools 35.0.0.
The Android app also depends on Xiaomi Wearable SDK 1.4. The SDK is not redistributed in this repository. Obtain `xms-wearable-lib_1.4_release.aar` from Xiaomi Vela's official Interconnect Development and Testing Demo and place it at:

```text
Android/app/libs/xms-wearable-lib_1.4_release.aar
```

GitHub Actions automatically obtains this file from Xiaomi's official demo during CI builds.

Run inside this directory:

```bash
./gradlew --no-daemon :app:assembleDebug
```

On Windows:

```bat
gradlew.bat --no-daemon :app:assembleDebug
```

Official APKs must be signed with the author's offline official key. Private keys, passwords, and local signing configuration are never included in the repository or CI.

## Installation and Verification

- Minimum Android version: Android 8.0 (API 26)
- Target API level: API 35
- Official certificate SHA-256:

```text
6A:06:8C:3D:40:95:25:1F:E7:1D:00:09:FC:06:51:30:C5:E6:62:BA:32:97:ED:A8:C3:8F:CE:94:CA:70:76:4F
```

Android accepts an in-place update only when both the package name and signing certificate match. Download packages and checksums from the project [Releases](https://github.com/241120nzdjjx/StringArtHelper/releases) page.

## Directory Layout

```text
Android/
├── app/                    Application source and resources
├── gradle/wrapper/         Gradle Wrapper
├── docs/                   Android release and signing notes
├── CHANGELOG.md            Chinese Android changelog
├── CHANGELOG.en.md         English Android changelog
└── README.md / README.en.md
```

See [CHANGELOG.en.md](./CHANGELOG.en.md) for version history. Privacy, security, and licensing information is available in [PRIVACY.md](./PRIVACY.md), [SECURITY.md](./SECURITY.md), [LICENSE](./LICENSE), and [NOTICE](./NOTICE).
