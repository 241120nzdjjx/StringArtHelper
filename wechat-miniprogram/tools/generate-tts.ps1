param(
    [string]$OutputRoot = (Join-Path $PSScriptRoot '..\assets\tts')
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Speech

$format = New-Object System.Speech.AudioFormat.SpeechAudioFormatInfo(8000, 16, 1)
$tokens = @{
    zh = [ordered]@{
        zero=([string][char]0x96F6); one=([string][char]0x4E00)
        two=([string][char]0x4E8C); three=([string][char]0x4E09)
        four=([string][char]0x56DB); five=([string][char]0x4E94)
        six=([string][char]0x516D); seven=([string][char]0x4E03)
        eight=([string][char]0x516B); nine=([string][char]0x4E5D)
        ten=([string][char]0x5341); hundred=([string][char]0x767E)
        thousand=([string][char]0x5343); ten_thousand=([string][char]0x4E07)
    }
    en = [ordered]@{
        zero='zero'; one='one'; two='two'; three='three'; four='four'; five='five'
        six='six'; seven='seven'; eight='eight'; nine='nine'; ten='ten'
        eleven='eleven'; twelve='twelve'; thirteen='thirteen'; fourteen='fourteen'
        fifteen='fifteen'; sixteen='sixteen'; seventeen='seventeen'; eighteen='eighteen'
        nineteen='nineteen'; twenty='twenty'; thirty='thirty'; forty='forty'
        fifty='fifty'; sixty='sixty'; seventy='seventy'; eighty='eighty'
        ninety='ninety'; hundred='hundred'; thousand='thousand'
    }
}

$voices = @{ zh = 'Microsoft Huihui Desktop'; en = 'Microsoft Zira Desktop' }

foreach ($language in $tokens.Keys) {
    $directory = Join-Path $OutputRoot $language
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    $speaker = New-Object System.Speech.Synthesis.SpeechSynthesizer
    try {
        $speaker.SelectVoice($voices[$language])
        $speaker.Rate = 1
        foreach ($entry in $tokens[$language].GetEnumerator()) {
            $path = Join-Path $directory ($entry.Key + '.wav')
            $speaker.SetOutputToWaveFile($path, $format)
            $speaker.Speak([string]$entry.Value)
            $speaker.SetOutputToNull()
        }
    }
    finally {
        $speaker.Dispose()
    }
}

$ffmpeg = Get-Command ffmpeg -ErrorAction SilentlyContinue
if ($ffmpeg) {
    Get-ChildItem -Path $OutputRoot -Recurse -File -Filter *.wav | ForEach-Object {
        $mp3 = [System.IO.Path]::ChangeExtension($_.FullName, '.mp3')
        & $ffmpeg.Source -loglevel error -y -i $_.FullName `
            -af 'silenceremove=start_periods=1:start_duration=0.02:start_threshold=-45dB:stop_periods=-1:stop_duration=0.08:stop_threshold=-45dB,apad=pad_dur=0.04' `
            -ar 16000 -ac 1 -b:a 24k $mp3
        if ($LASTEXITCODE -ne 0) { throw "ffmpeg failed for $($_.FullName)" }
        Remove-Item -LiteralPath $_.FullName
    }
}

Get-ChildItem -Path $OutputRoot -Recurse -File |
    Select-Object FullName, Length
