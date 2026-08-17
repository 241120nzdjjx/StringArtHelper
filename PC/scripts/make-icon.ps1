# SPDX-License-Identifier: GPL-3.0-only
# Copyright (C) 2026 牛杂の经济学
#
# Generates the PC app icon from the Android launcher artwork
# (background #17171F, purple disc, white thread chords) as multi-size
# PNGs plus a Vista-style ICO container (PNG-embedded).
#
# Usage: powershell -ExecutionPolicy Bypass -File scripts/make-icon.ps1
# Output: assets/icon.ico (packaging), assets/icon.png (256px, window icon)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$assets = Join-Path $root 'src\renderer\assets'

Add-Type -AssemblyName System.Drawing

# Design space matches the Android vector: 108x108 viewport.
# Disc: centre (54,54) radius 42. Chords (white, width 3, round joins).
$disc = @{ cx = 54; cy = 54; r = 42 }
$chords = @(
  @(20, 50), @(82, 25), @(32, 83), @(88, 58), @(25, 28), @(74, 87), @(20, 50)
)

function New-IconBitmap([int]$size) {
  $scale = $size / 108.0
  $bmp = New-Object System.Drawing.Bitmap($size, $size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
  $g = [System.Drawing.Graphics]::FromImage($bmp)
  $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
  $g.Clear([System.Drawing.Color]::Transparent)

  # Background square (launcher_background #17171F).
  $bgBrush = New-Object System.Drawing.SolidBrush([System.Drawing.ColorTranslator]::FromHtml('#17171F'))
  $g.FillRectangle($bgBrush, 0, 0, $size, $size)

  # Purple disc.
  $discBrush = New-Object System.Drawing.SolidBrush([System.Drawing.ColorTranslator]::FromHtml('#9769FF'))
  $x = ($disc.cx - $disc.r) * $scale
  $y = ($disc.cy - $disc.r) * $scale
  $w = (2 * $disc.r) * $scale
  $g.FillEllipse($discBrush, $x, $y, $w, $w)

  # White chords.
  $pen = New-Object System.Drawing.Pen([System.Drawing.Color]::White, [float](3 * $scale))
  $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
  $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
  $pen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
  $points = New-Object 'System.Drawing.Point[]' ($chords.Count)
  for ($i = 0; $i -lt $chords.Count; $i++) {
    $points[$i] = New-Object System.Drawing.Point(
      [int][math]::Round($chords[$i][0] * $scale),
      [int][math]::Round($chords[$i][1] * $scale))
  }
  $g.DrawLines($pen, $points)

  $g.Dispose()
  $bgBrush.Dispose()
  $discBrush.Dispose()
  $pen.Dispose()
  return $bmp
}

# ---- PNGs ----
$sizes = @(16, 24, 32, 48, 64, 128, 256)
$pngFiles = @{}
foreach ($size in $sizes) {
  $bmp = New-IconBitmap $size
  $path = Join-Path $env:TEMP ("sah-icon-$size.png")
  $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
  $pngFiles[$size] = $path
  $bmp.Dispose()
}

# Main window icon (256px).
Copy-Item $pngFiles[256] (Join-Path $assets 'icon.png') -Force

# ---- ICO container (PNG-embedded, Vista+ style) ----
$ordered = @(256, 128, 64, 48, 32, 24, 16)
$blobs = @()
foreach ($size in $ordered) {
  $blobs += ,[System.IO.File]::ReadAllBytes($pngFiles[$size])
}

$count = $blobs.Count
$headerSize = 6
$entrySize = 16
$total = $headerSize + ($entrySize * $count)

$out = New-Object System.IO.MemoryStream
$writer = New-Object System.IO.BinaryWriter($out)
$writer.Write([uint16]0)            # reserved
$writer.Write([uint16]1)            # type: icon
$writer.Write([uint16]$count)       # image count

$offset = $total
# Pass 1: directory entries (offsets known before any payload is written).
for ($i = 0; $i -lt $count; $i++) {
  $size = $ordered[$i]
  $dim = if ($size -ge 256) { 0 } else { $size }
  $writer.Write([byte]$dim)         # width
  $writer.Write([byte]$dim)         # height
  $writer.Write([byte]0)            # palette
  $writer.Write([byte]0)            # reserved
  $writer.Write([uint16]1)          # planes
  $writer.Write([uint16]32)         # bpp
  $writer.Write([uint32]$blobs[$i].Length)  # size
  $writer.Write([uint32]$offset)    # offset
  $offset += $blobs[$i].Length
}
# Pass 2: PNG payloads.
for ($i = 0; $i -lt $count; $i++) {
  $writer.Write($blobs[$i])
}
$writer.Flush()
[System.IO.File]::WriteAllBytes((Join-Path $assets 'icon.ico'), $out.ToArray())
$writer.Dispose()
$out.Dispose()

foreach ($size in $sizes) { Remove-Item $pngFiles[$size] -Force -ErrorAction SilentlyContinue }

Write-Output "icon.ico + icon.png generated in $assets"
Get-ChildItem $assets | Select-Object Name, Length
