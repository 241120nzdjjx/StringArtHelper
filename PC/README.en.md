<p align="center">
  <a href="./README.md">简体中文</a> | <strong>English</strong>
</p>

<p align="center">
  <a href="../README.en.md">Project Home</a> · <a href="../Android/README.en.md">Android</a> · <strong>PC</strong> · <a href="../wechat-miniprogram/README.en.md">wechat mini Program</a> · <a href="../Wearable/README.en.md">Xiaomi Smart Band</a>
</p>

# StringArtHelper — PC Desktop Version

Current version: **0.1.0**

> This directory is the **PC desktop version** (Electron) of the [StringArtHelper](https://github.com/241120nzdjjx/StringArtHelper) project.

StringArtHelper is a free, open-source, ad-free tool for generating and building string art. This PC version is **not a rewrite**: it extracts the core capabilities from the **Android source code**, keeps the core generation algorithm and the cross-platform file formats, drops the band (wearable) functionality that has no PC counterpart, and redesigns the interaction for the desktop — just drag an image, sequence or save into the window.

## Features

- **Drag and drop**: drop an image (PNG/JPG/WebP/GIF/BMP), a `.txt` sequence or a `.sar`/`.bin` save directly into the window; the app routes to the right workflow automatically.
- **Image-to-sequence generation**: interactive square crop box (drag to move, wheel to zoom, double-click to reset) with the same parameters as Android (nails, max lines, nail-circle diameter, thread diameter, auto-stop).
- **Live generation preview**: generation runs in a worker thread so the UI stays responsive, and chords are revealed on the preview as they are produced.
- **Sequence player**: previous / next / play-pause / jump / replay with adjustable step delay plus “Slower / Faster” shortcuts; shows the previous, current and next nail numbers; thread display in actual ratio or a custom diameter (slider + exact input), nail-number labels, and wheel-zoom / drag-pan.
- **Optional narration**: speak the current nail number with the system voice (Chinese number wording matches the Android app), with repeat-twice and speech-rate options.
- **Auto-saved project list**: generated projects are stored as `.sar` in the project directory and can be opened, saved-as, renamed or deleted at any time.
- **Exports**: Android-style TXT sequences (with a parameter header, importable by Android and the Mini Program) and a printable nail-template PDF (fewest A4 pages + trim lines + 100 mm calibration ruler).
- **Full about page**: app intro, support the author (Alipay / WeChat QR codes, Mini Program code), feedback & contact (Bilibili / Email / X / Telegram links, Genshin UID copy), version & open-source info (with anti-impersonation notice), aligned with the Android about page.
- **App icon**: reuses the Android launcher artwork (dark background + purple thread disc + white chords) for both the window and the installer.

## Relation to Android / WeChat Mini Program

| Capability | Description |
| --- | --- |
| Core algorithm | Greedy generator ported 1:1 from Android's `StringArtGenerator.java` (256×256 working area, thread width/opacity model, recent-pin avoidance, auto-stop). |
| Save formats | `.sar` (SAR2/SAR3/SAR4) and `.bin` saves and TXT sequences are **byte-compatible** with Android and the Mini Program; files can be exchanged in both directions. |
| Thumbnails | SAR4 thumbnails are 192×192 monochrome PNGs with the same semantics as Android. |
| Band | No wearable features are included in this version (PC has no matching hardware). |
| Narration | Android uses system TTS; the PC version uses the system voice (can be disabled). |

## Development & Running

Requires Node.js ≥ 20.

```bash
npm install        # install dependencies (Electron)
npm start          # launch the app
npm test           # run core module tests (SAR/TXT/generator/PDF)
```

> If downloading the Electron binary is slow on your network:
> `$env:ELECTRON_MIRROR = "https://npmmirror.com/mirrors/electron/"; npm install`
>
> On npm ≥ 11, if `allow-scripts` blocks Electron's installer, either run
> `npm approve-scripts electron` or fetch the binary once with
> `node node_modules/electron/install.js`.

## Directory Layout

```text
PC/
├── src/
│   ├── main/                  Electron main process
│   │   ├── main.js            window, menu, IPC, project directory management
│   │   └── generate-worker.js generator worker_thread (keeps the UI responsive)
│   ├── preload.js             safe renderer bridge (contextBridge)
│   ├── core/                  pure-JS core modules (no platform deps; shared by main & tests)
│   │   ├── generator.js       generation algorithm (Android StringArtGenerator port)
│   │   ├── sar.js             SAR2/3/4 save codec (Java modified UTF-8)
│   │   ├── text-codec.js      TXT sequence parsing/export, text encoding detection
│   │   ├── number-format.js   Chinese numbers (Android NailNumberFormatter port)
│   │   └── pdf-template.js    nail template PDF generation
│   └── renderer/              desktop UI (dark theme using the Android palette)
│       ├── index.html
│       ├── styles.css
│       ├── app.js             interaction logic and canvas preview rendering
│       └── assets/            app icons (PNG/ICO/SVG) and about-page images
├── scripts/
│   └── make-icon.ps1          generates multi-size PNG/ICO from the Android icon vectors
└── tests/
    ├── run-tests.js           Node test runner (npm test)
    ├── smoke.js               automated UI smoke test (SAH_SMOKE=1 npm start)
    └── fixtures/              SAR byte-compatibility fixtures (shared with the Mini Program suite)
```

## Technical Notes

- **Pure-JS core**: everything in `src/core/` is dependency-free CommonJS, runnable in the main process, a worker thread, and Node tests — one implementation for the algorithm and formats.
- **Worker-thread generation**: generation runs in a `worker_threads` worker; progress and the partial sequence are streamed back for live preview without blocking the UI.
- **Cross-platform byte compatibility**: SAR and TXT handling are based on the Android implementation and regression-tested byte-for-byte with the fixtures shared by the Mini Program suite.
- **Security baseline**: `contextIsolation: true`, `nodeIntegration: false`; the renderer only talks to the main process through the minimal preload API; deleting saves uses the OS trash (`shell.trashItem`).
- **Theme**: the dark theme reuses the Android palette (`#101016` / `#1d1d27`, accent `#9769ff`).

## License

This directory is licensed under the same [GPL-3.0-only](../LICENSE) as the project. `src/core/sar.js`, `src/core/text-codec.js` and `src/core/pdf-template.js` are adapted from modules under `wechat-miniprogram/` in the same project (same license; attributed in the file headers).
