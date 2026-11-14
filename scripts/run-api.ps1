$ErrorActionPreference = 'Stop'
Set-Location (Resolve-Path (Join-Path $PSScriptRoot ".."))
$javaFiles = Get-ChildItem src/main/java -Filter *.java -Recurse | Select-Object -ExpandProperty FullName
if (!(Test-Path bin)) { New-Item -ItemType Directory bin | Out-Null }
$compiler = (Get-Command javac).Source
$runtime = Join-Path (Split-Path $compiler) 'java.exe'
& $compiler -encoding UTF-8 -cp 'lib/*;bin' -d bin $javaFiles
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed' }
Copy-Item src/main/resources/* bin -Force
& $runtime '-Dfile.encoding=UTF-8' -cp 'bin;lib/*' com.linhkien.api.ApiServer
