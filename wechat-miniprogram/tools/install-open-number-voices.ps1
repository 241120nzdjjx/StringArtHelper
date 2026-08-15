param([string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot))

$ErrorActionPreference = 'Stop'
$temp = Join-Path $env:TEMP 'sah-open-number-voices'
$zhOutput = Join-Path $ProjectRoot 'assets\tts\zh'
$enOutput = Join-Path $ProjectRoot 'assets\tts\en'
New-Item -ItemType Directory -Force -Path $temp,$zhOutput,$enOutput | Out-Null

$zh = [ordered]@{
  zero=[char]0x96F6; one=[char]0x4E00; two=[char]0x4E8C; three=[char]0x4E09
  four=[char]0x56DB; five=[char]0x4E94; six=[char]0x516D; seven=[char]0x4E03
  eight=[char]0x516B; nine=[char]0x4E5D; ten=[char]0x5341; hundred=[char]0x767E
  thousand=[char]0x5343; ten_thousand=[char]0x4E07
}
foreach ($entry in $zh.GetEnumerator()) {
  $encoded = [Uri]::EscapeDataString('cmn-' + $entry.Value + '.mp3')
  $source = Join-Path $temp ('zh-' + $entry.Key + '.mp3')
  $url = "https://raw.githubusercontent.com/hugolpz/audio-cmn/master/64k/hsk/$encoded"
  Invoke-WebRequest -Uri $url -OutFile $source -UseBasicParsing
  # Some valid Mandarin syllables begin or end below a fixed dB threshold.
  # Do not use silenceremove here: it previously reduced 三/六/百 to 0.09-0.17 s noise fragments.
  & ffmpeg -hide_banner -loglevel error -y -i $source -af 'loudnorm=I=-18:TP=-2:LRA=7,apad=pad_dur=0.035' -ar 24000 -ac 1 -b:a 40k (Join-Path $zhOutput ($entry.Key + '.mp3'))
  if ($LASTEXITCODE -ne 0) { throw "ffmpeg failed for Chinese $($entry.Key)" }
}

$digits = @('zero','one','two','three','four','five','six','seven','eight','nine')
for ($digit = 0; $digit -lt 10; $digit += 1) {
  $source = Join-Path $temp ("en-$digit.wav")
  $url = "https://raw.githubusercontent.com/Jakobovski/free-spoken-digit-dataset/master/recordings/${digit}_jackson_0.wav"
  Invoke-WebRequest -Uri $url -OutFile $source -UseBasicParsing
  & ffmpeg -hide_banner -loglevel error -y -i $source -af 'silenceremove=start_periods=1:start_duration=0.01:start_threshold=-48dB:stop_periods=1:stop_duration=0.05:stop_threshold=-48dB,apad=pad_dur=0.035,loudnorm=I=-18:TP=-2:LRA=7' -ar 24000 -ac 1 -b:a 40k (Join-Path $enOutput ($digits[$digit] + '.mp3'))
  if ($LASTEXITCODE -ne 0) { throw "ffmpeg failed for English digit $digit" }
}
