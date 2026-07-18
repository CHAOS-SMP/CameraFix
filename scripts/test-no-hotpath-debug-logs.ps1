$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$listenerFile = Join-Path $root 'src\main\java\cn\chaosmp\camerafix\mixins\MixinPacketListener.java'
$source = Get-Content -LiteralPath $listenerFile -Raw

if ($source -match 'System\.err\.println') {
    throw 'MixinPacketListener still contains System.err.println in the packet send hot path.'
}

Write-Output 'No packet hot-path debug logs found.'
