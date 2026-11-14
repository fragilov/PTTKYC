Set-Location (Resolve-Path (Join-Path $PSScriptRoot ".."))
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"

Write-Host "=========================================================================" -ForegroundColor Cyan
Write-Host "  KHOI CHAY HE THONG QUAN LY LINH KIEN VA GIO HANG" -ForegroundColor Cyan
Write-Host "=========================================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }

Write-Host "1. Dang bien dich ma nguon Java..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path "src/main/java" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -cp "lib/*;bin" -d "bin" $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "[LOI] Bien dich that bai!" -ForegroundColor Red
    exit 1
}

Copy-Item -Path "src/main/resources/*" -Destination "bin" -Force

Write-Host "2. Dang khoi chay ung dung..." -ForegroundColor Green
Write-Host "=========================================================================" -ForegroundColor Cyan
java -cp "bin;lib/*" com.linhkien.Main
