$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$mainPath = Join-Path $root 'src\main\java\cn\chaosmp\camerafix\Main.java'
$source = Get-Content -LiteralPath $mainPath -Raw

if ($source -match 'return\s+ENABLE_HOOK\s*&&\s*!isSingleplayer\(\)\s*;') {
    throw 'Main.shouldUseProtocol() still disables protocol mode in singleplayer.'
}

if ($source -notmatch 'return\s+ENABLE_HOOK\s*;') {
    throw 'Main.shouldUseProtocol() is not reduced to the single ENABLE_HOOK gate.'
}

Write-Output 'Fabric protocol gate audit passed.'
