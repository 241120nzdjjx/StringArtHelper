<p align="center">
  <a href="./README.md">简体中文</a> | <strong>English</strong>
</p>

<p align="center">
  <strong>Project Home</strong> · <a href="./Android/README.en.md">Android</a> · <a href="./wechat-miniprogram/README.en.md">wechat mini Program</a> · <a href="./Wearable/README.en.md">Xiaomi Smart Band</a>
</p>

# StringArtHelper

StringArtHelper is a free, open-source, ad-free toolkit for generating and building string art. The project includes an Android app, a WeChat Mini Program, and a companion app for Xiaomi Smart Band 9 Pro.

Image processing, path generation, previews, voice guidance, and save management are designed to run locally. Android and the WeChat Mini Program exchange TXT, `.bin`, and `.sar` files. Android saves can also be synchronized to the band for continued use without the phone.

## Choose a Platform

| Platform | Current version | Purpose | Source and documentation |
| --- | --- | --- | --- |
| Android | v26.5.9 (72) | Full generation, preview, guidance, saves, and band management | [Open Android](./Android/README.en.md) |
| WeChat Mini Program | v1.2.2 | Generate and build string art inside WeChat | [Open WeChat Mini Program](./wechat-miniprogram/README.en.md) |
| Xiaomi Smart Band | v26.5.9 (72) | View pins, change steps, jump, preview, and use offline saves | [Open Wearable](./Wearable/README.en.md) |

## Repository Layout

```text
StringArtHelper/
├── Android/                Android source and bilingual documentation
├── wechat-miniprogram/     WeChat Mini Program source and documentation
├── Wearable/               Xiaomi Smart Band 9 Pro source and documentation
├── .github/workflows/      Android and wearable builds, plus Mini Program tests
├── README.md               Project overview in Simplified Chinese
├── README.en.md            Project overview in English
├── CHANGELOG.md            Chinese changelog
├── CHANGELOG.en.md         English changelog
└── LICENSE                 Open-source license
```

Every platform README contains relative links to Project Home, Android, WeChat Mini Program, and Xiaomi Smart Band. Each link stays in the currently selected language and also works in a downloaded copy of the repository.

## Downloads

Download official packages and matching source archives from [GitHub Releases](https://github.com/241120nzdjjx/StringArtHelper/releases). Verify them with `StringArtHelper-v26.5.9-SHA256.txt` from the same release.

Release files:

- `StringArtHelper-v26.5.9-official.apk`
- `StringArtHelper-Android-v26.5.9-source.zip`
- `StringArtHelper-Wearable-v26.5.9-MiBand9Pro.rpk`
- `StringArtHelper-Wearable-v26.5.9-source.zip`
- `StringArtHelper-WeChat-MiniProgram-v1.2.2-source.zip`

## Compatibility

- The Android APK and wearable RPK both use package name `com.niuza.stringartreader2`.
- Android and wearable versions should match; this release uses v26.5.9 (versionCode 72) on both platforms.
- Android and the WeChat Mini Program can exchange TXT, `.bin`, and `.sar` saves.
- Wearable saves are synchronized by Android. Once synchronized, the band can change steps and preserve progress offline.

## Privacy and Security

- The Android app requests no network permission and includes no advertising or analytics SDK.
- The WeChat Mini Program uses neither WeChat Cloud Development nor a project-operated server or active network requests.
- Official signing private keys, passwords, WeChat AppSecrets, tokens, and local private configuration files are excluded from the repository and source archives.

See the [Privacy Policy](./PRIVACY.md) and [Security Policy](./SECURITY.md).

## License

Original project code is released under [GNU GPL v3.0 only](./LICENSE), with compatible [Section 7 additional terms](./ADDITIONAL_TERMS.md). Bundled third-party code and voice assets remain under their respective licenses; see [NOTICE](./NOTICE) and the notices in each platform directory.

## Feedback and Contact

- GitHub: [241120nzdjjx/StringArtHelper](https://github.com/241120nzdjjx/StringArtHelper)
- Bilibili: [牛杂の经济学](https://b23.tv/K3Cp0ZZ)
- Email: 241120nzdjjx@gmail.com
- X: [@nzdjjx241120](https://x.com/nzdjjx241120)
- Telegram: [@nzdjjx](https://t.me/nzdjjx)
