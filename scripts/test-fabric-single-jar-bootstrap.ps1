$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$modJsonPath = Join-Path $root 'fabric\src\main\resources\fabric.mod.json'
$bootstrapPath = Join-Path $root 'fabric\src\main\java\cn\chaosmp\camerafix\fabric\FabricBootstrap.java'
$preLaunchPath = Join-Path $root 'fabric\src\main\java\cn\chaosmp\camerafix\fabric\FabricPreLaunch.java'
$intermediaryMixinPath = Join-Path $root 'fabric\src\main\resources\camerafix.intermediary.mixins.json'
$namedMixinPath = Join-Path $root 'fabric\src\main\resources\camerafix.named.mixins.json'

if (-not (Test-Path $bootstrapPath)) {
    throw 'Missing FabricBootstrap.java. Single-jar dual-namespace bootstrap has not been introduced.'
}

if (-not (Test-Path $preLaunchPath)) {
    throw 'Missing FabricPreLaunch.java. Single-jar dual-namespace preLaunch selector has not been introduced.'
}

if (-not (Test-Path $intermediaryMixinPath)) {
    throw 'Missing camerafix.intermediary.mixins.json.'
}

if (-not (Test-Path $namedMixinPath)) {
    throw 'Missing camerafix.named.mixins.json.'
}

$modJson = Get-Content -LiteralPath $modJsonPath -Raw

if ($modJson -match '"mixins"\s*:\s*\[\s*"camerafix\.mixins\.json"\s*\]') {
    throw 'fabric.mod.json still points at the old single mixin config.'
}

if ($modJson -notmatch '"main"\s*:\s*\[\s*"cn\.chaosmp\.camerafix\.fabric\.FabricBootstrap"\s*\]') {
    throw 'fabric.mod.json main entrypoint is not switched to FabricBootstrap.'
}

if ($modJson -notmatch '"preLaunch"\s*:\s*\[\s*"cn\.chaosmp\.camerafix\.fabric\.FabricPreLaunch"\s*\]') {
    throw 'fabric.mod.json is missing the preLaunch entrypoint for namespace-specific mixin loading.'
}

$bootstrapSource = Get-Content -LiteralPath $bootstrapPath -Raw
if ($bootstrapSource -match 'import\s+net\.minecraft\.') {
    throw 'FabricBootstrap.java must not directly import net.minecraft classes.'
}

$preLaunchSource = Get-Content -LiteralPath $preLaunchPath -Raw
if ($preLaunchSource -match 'import\s+net\.minecraft\.') {
    throw 'FabricPreLaunch.java must not directly import net.minecraft classes.'
}

Write-Output 'Fabric single-jar bootstrap architecture audit passed.'
