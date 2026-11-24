$ErrorActionPreference = 'Stop'
$oboe = Join-Path $PSScriptRoot '..\app\src\main\cpp\third_party\oboe'
if (Test-Path (Join-Path $oboe 'CMakeLists.txt')) {
    Write-Host 'Oboe já presente.'
    exit 0
}
New-Item -ItemType Directory -Force -Path (Split-Path $oboe) | Out-Null
Write-Host 'Baixando Oboe (apenas desenvolvimento; o APP é offline em runtime)...'
git clone --depth 1 https://github.com/google/oboe.git $oboe
Write-Host 'OK.'
