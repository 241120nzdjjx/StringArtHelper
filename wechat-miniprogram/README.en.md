<p align="center">
  <a href="./README.md">简体中文</a> | <strong>English</strong>
</p>
<p align="center">
  <a href="../README.en.md">Project Home</a> · <a href="../Android/README.en.md">Android</a> · <a href="../PC/README.en.md">PC</a> · <strong>WeChat Mini Program</strong> · <a href="../Wearable/README.en.md">Xiaomi Smart Band</a>
</p>

# StringArtHelper | WeChat Mini Program

> Due to the WeChat Mini Program review process, the source code in this repository may be newer than the version currently available online. For actual use, refer to the version published on WeChat.

A free, open-source string art generation and creation assistant that runs entirely on the local device.

Current version: **v1.2.2**

## Main Features

- Generate string art pin paths from images
- Grayscale preprocessing, circular cropping, dragging, and two-finger zooming
- Support for 100–500 pins
- Custom maximum string segment count, string diameter, and pin-circle diameter
- Automatic prevention of excessively dark results
- Generation-process animation and final-result preview
- Real-time preview of the current stringing progress
- Previous step, next step, step jump, and automatic playback
- Display of the previous, current, and next pin numbers
- Offline number voice guidance in Chinese and English
- Automatic progress recovery and manual project saves
- Import and export of SAR project saves compatible with the Android app
- Import and export of TXT stringing sequences containing physical parameters
- Generation of A4 PDF pin templates at true physical dimensions in millimetres
- Chinese and English interfaces with portrait and landscape support

## Privacy and Offline Operation

StringArtHelper does not depend on a server.

Image processing, path calculation, project saves, and voice guidance are all performed locally. The Mini Program does not actively upload selected images, stringing sequences, or project-save contents.

## File Import and Export

Supported import formats:

- `.txt`: string art pin-number sequences
- `.sar`: StringArtHelper project saves
- `.bin`: compatible project saves containing the same data as SAR files

Because of WeChat platform restrictions when selecting non-image files:

> To upload a TXT or BIN file, first forward it to a WeChat chat, then select it from Chat Files.

To save an exported file to the device:

> To download a TXT or BIN file, first forward it to a WeChat chat, then download it from the chat.

## TXT Format

TXT files officially exported by StringArtHelper contain:

- Pin count
- String diameter
- Pin-circle diameter
- Complete pin-number sequence

Example:

```text
# StringArtHelper export
# Pin count: 300
# String diameter: 0.20 mm
# Pin-circle diameter: 260 mm
# Pin numbering: Pin 0 is at the rightmost point; numbers increase clockwise
# Total pin entries: 5001

0 → 87 → 162 → 39 → 204 → 11
```

The importer also accepts common formats that use spaces, commas, line breaks, arrows, or numbered steps.

## SAR Project-Save Compatibility

The Mini Program can read:

- SAR2
- SAR3
- SAR4

The official export format is SAR4, which is compatible with the Android version of StringArtHelper.

A project save contains:

- Project name
- Current progress
- Pin count
- Pin-circle diameter
- String diameter
- Thumbnail
- Complete pin-number sequence

## PDF Pin Templates

PDF pin templates are generated at true physical dimensions in millimetres. Large templates are automatically divided across multiple pages for assembly.

When printing, select:

> Actual size / 100%

Do not select “Fit to page” or any automatic scaling option. After printing, use the 100 mm calibration ruler included in the PDF to verify the scale.

## Project Management

There are two types of project saves:

- **Automatic progress recovery:** continuously records the latest progress of the current project
- **Manual save:** a fixed snapshot created by the user that is not overwritten by later progress

If you load a manual save and continue working, a new automatic recovery project is created. The original manual save remains unchanged.

## Development and Testing

This is a native WeChat Mini Program project and can be opened directly in WeChat DevTools.

Requirements:

- WeChat DevTools
- A WeChat base library supporting Canvas 2D, Workers, and local file APIs
- Node.js, required only for automated tests

Run the tests with:

```bash
npm test
```

The project does not depend on a remote server and does not require a backend address.

## Android Version

Source code, version history, and release information for the Android version of StringArtHelper:

[GitHub: StringArtHelper](https://github.com/241120nzdjjx/StringArtHelper)

## Feedback and Contact

If you have a suggestion, encounter a problem, or find any part of the Mini Program difficult to use, feel free to get in touch:

- Email: 241120nzdjjx@gmail.com
- [Bilibili](https://b23.tv/K3Cp0ZZ)

## Open-Source License

Original code developed for this project is released under the [GNU General Public License v3.0](../LICENSE), with compatible additional terms under Section 7.

Bundled third-party code and voice assets remain under their respective licenses and are not relicensed under the GPL. See [NOTICE](./NOTICE) for complete source, attribution, and license information.

You may use, study, modify, and distribute the source code under the terms of the license. When distributing a modified version, you must comply with the open-source obligations of GPL v3.0 and preserve the original license and copyright notices.
