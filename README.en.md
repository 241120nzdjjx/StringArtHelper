<p align="center">
  <a href="./README.md">简体中文</a> | <strong>English</strong>
</p>

# StringArtHelper

A free, open-source, ad-free string art creation tool whose core features run entirely on the local device. It is available as an Android app and a WeChat Mini Program.

From image cropping, string-path generation, and pin-template creation to step-by-step previews, voice guidance, and project saves, all processing is performed locally. TXT sequences and project save files can be transferred between the Android app and the WeChat Mini Program, allowing work to continue across devices.

## Main Features of the Android App

- Local image cropping with edge snapping and a black-and-white preview of the final selected area
- Local path generation for 100–500 pins and up to 20,000 string segments
- Pin-circle diameters from 80 to 1,200 mm, with a reminder to leave at least 10 mm of board space on every side
- Step-by-step visualization of the generation process at animation speeds from 1× to 20×
- Adjustable pin count, maximum string segment count, pin-circle diameter, string diameter, and automatic over-darkening prevention
- A unified Import entry that automatically recognizes TXT pin sequences and `.bin` or `.sar` project saves
- TXT sequence import and export, with offline number voice guidance in Chinese and English
- Optional temporary audio ducking of other Android media during voice guidance
- Step control using volume buttons, Bluetooth headsets, and media buttons
- Dynamic remaining-time estimates based on the number of steps left and the actual working pace
- A real-time preview that can be zoomed, panned, minimized, and displayed in full screen
- Purple highlighting for the current string segment, with optional true-scale rendering based on the physical string diameter
- Automatic progress recovery and manual snapshots; `.bin` and `.sar` saves can be imported, while projects are exported and shared in `.sar` format
- Automatic generation of A4 tiled pin templates using the minimum required number of pages, including cutting lines, alignment marks, and a 100 mm calibration ruler
- Chinese and English interfaces with portrait and landscape support
- Sharing of the currently installed APK with other users

## WeChat Mini Program

> Due to the WeChat Mini Program review process, the Mini Program source code in this repository may be newer than the version currently available online. For actual use, refer to the version published on WeChat.

If you do not want to install the Android app, or need to use StringArtHelper on a non-Android device, scan the Mini Program code below with WeChat or search for **“绕线画助手”** in WeChat:

<p align="center">
  <img src="docs/images/wechat-miniprogram-code.png" alt="StringArtHelper WeChat Mini Program code" width="320">
</p>

WeChat Mini Program entry text—copy it and open it inside WeChat:

```text
#小程序://绕线画助手/3BsgpZwfWylWK9d
```

Some screens and interactions differ slightly from the Android app, but the core features—including image-to-path generation, previews, voice guidance, project saves, and PDF pin templates—remain available.

The Android app and WeChat Mini Program can import and export compatible `.bin` or `.sar` project saves and TXT pin sequences, making it possible to continue a project on another device.

Because of WeChat platform restrictions, TXT and BIN files must first be forwarded to a WeChat chat before they can be selected and uploaded from Chat Files.

> All generation and project data is still processed locally on the device. Some capabilities may be limited by the WeChat Mini Program platform.

For instructions on importing the project into WeChat DevTools, platform limitations, and testing, see [`wechat-miniprogram/README.en.md`](wechat-miniprogram/README.en.md).

The Mini Program source code is located in [`wechat-miniprogram/`](wechat-miniprogram/).

## Privacy

The Android app does not request network access and contains no advertising or analytics SDKs. The WeChat Mini Program does not use WeChat Cloud Development, a project-operated server, or network requests.

Images, TXT sequences, and project saves are processed only on the local device and are not uploaded by this project. Android system cloud backup is disabled.

When you open GitHub, Bilibili, X, Telegram, an email address, or a third-party string art generation website, the request is handed over to the corresponding external app, WeChat, or web browser.

See [PRIVACY.md](PRIVACY.md) for details.

## Download and Verification

Download the official APK from the **Releases** page of this repository.

Android package name:

```text
com.niuza.stringartreader2
```

SHA-256 fingerprint of the official Android signing certificate:

```text
6A:06:8C:3D:40:95:25:1F:E7:1D:00:09:FC:06:51:30:C5:E6:62:BA:32:97:ED:A8:C3:8F:CE:94:CA:70:76:4F
```

Android permits an existing installation to be updated only when both the package name and signing certificate match. The About page in the app also displays the signing-certificate fingerprint of the currently installed APK.

## Source Code Structure

```text
StringArtHelper/
├── app/                    Android application source code
├── wechat-miniprogram/     WeChat Mini Program source code
├── docs/                   Release, signing, and image resources
└── .github/workflows/      Android automated build workflows
```

## Building and Testing

### Android

Requirements:

- JDK 17
- Gradle 8.9
- Android Gradle Plugin 8.7.3
- Android SDK 35 / Build Tools 35.0.0

```bash
gradle --no-daemon :app:assembleDebug
```

GitHub Actions builds Debug and unsigned Release APKs from the public source code and uploads them as workflow artifacts.

Official APKs must be signed using the author's official signing key, which is stored offline. The private key and its passwords are never included in the repository or CI environment.

### WeChat Mini Program

Import the [`wechat-miniprogram/`](wechat-miniprogram/) directory directly into WeChat DevTools.

Node.js is required only for automated tests:

```bash
cd wechat-miniprogram
npm test
```

The Mini Program does not use WeChat Cloud Development or a project-operated server.

Automated tests cover the core generation algorithm, TXT and SAR2/3/4 parsing, file-based project saves and migration, crop-edge snapping, and static checks of important pages. Album selection, camera access, Chat Files selection, and file sharing must still be verified on a physical device running WeChat.

See [CHANGELOG.en.md](CHANGELOG.en.md) for the version history and [docs/RELEASE_SIGNING.md](docs/RELEASE_SIGNING.md) for signing and official release instructions.

## Project Information

- Current official Android version: `26.3.5` (`versionCode 61`)
- Minimum Android version: Android 8.0 (API 26)
- Target Android API level: API 35
- Original project code: GNU GPL v3.0 only, with compatible additional terms under Section 7
- Bundled third-party code and voice assets remain under their respective licenses. See [NOTICE](./NOTICE) and the [WeChat Mini Program notices](./wechat-miniprogram/NOTICE).
- Author: 牛杂の经济学

Version numbers are based on the year. Releases made in 2026 use the `26.x.x` series. Feature updates increment the minor version, bug-fix updates increment the patch version, and Android `versionCode` values increase independently.

Modified versions must provide the corresponding source code, preserve reasonable attribution, and be clearly identified as modified. They must not be presented as official builds.

See [LICENSE](LICENSE) and [ADDITIONAL_TERMS.md](ADDITIONAL_TERMS.md) for details.

## Feedback and Contact

- GitHub: [241120nzdjjx/StringArtHelper](https://github.com/241120nzdjjx/StringArtHelper)
- Bilibili: [牛杂の经济学](https://b23.tv/K3Cp0ZZ)
- Email: 241120nzdjjx@gmail.com
- X: [@nzdjjx241120](https://x.com/nzdjjx241120)
- Telegram: [@nzdjjx](https://t.me/nzdjjx)
- WeChat Mini Program: search for **“绕线画助手”** in WeChat, or copy `#小程序://绕线画助手/3BsgpZwfWylWK9d` and open it inside WeChat

For general questions, use GitHub Issues. For security-related matters, read [SECURITY.md](SECURITY.md).
