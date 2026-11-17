# YukiWorld 7.2 — скачивает серверные моды в папку mods (с проверкой хэшей)
$ErrorActionPreference = "Stop"
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$mods = Join-Path $root "mods"
New-Item -ItemType Directory -Force -Path $mods | Out-Null
$list = Import-Csv -Path (Join-Path $root "mods-list.csv") -Delimiter ";" -Encoding UTF8
$i = 0; $fail = @()
foreach ($m in $list) {
    $i++
    $dst = Join-Path $mods $m.file
    if (Test-Path $dst) {
        if ((Get-FileHash $dst -Algorithm $m.algo.ToUpper()).Hash.ToLower() -eq $m.hash) { continue }
    }
    Write-Host ("[{0}/{1}] {2}" -f $i, $list.Count, $m.file)
    $ok = $false
    for ($try = 1; $try -le 3 -and -not $ok; $try++) {
        try {
            Invoke-WebRequest -Uri $m.url -OutFile $dst -UseBasicParsing -UserAgent "YukiWorld-Installer"
            if ((Get-FileHash $dst -Algorithm $m.algo.ToUpper()).Hash.ToLower() -eq $m.hash) { $ok = $true }
            else { Write-Host "  хэш не совпал, повтор..." -ForegroundColor Yellow }
        } catch { Write-Host "  ошибка: $($_.Exception.Message), повтор..." -ForegroundColor Yellow; Start-Sleep 2 }
    }
    if (-not $ok) { $fail += $m.file; Remove-Item $dst -ErrorAction SilentlyContinue }
}
Write-Host ""
if ($fail.Count -eq 0) { Write-Host "ГОТОВО: все моды скачаны ($($list.Count + 1) шт. вместе с YukiCore)." -ForegroundColor Green }
else { Write-Host "Не скачались ($($fail.Count)): $($fail -join ', ') — запусти ещё раз." -ForegroundColor Red }
