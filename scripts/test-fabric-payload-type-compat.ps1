$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$fabricFile = Join-Path $root 'fabric\src\main\java\cn\chaosmp\camerafix\fabric\FabricProtocolPackets.java'
$source = Get-Content -LiteralPath $fabricFile -Raw

if ($source -match 'CustomPacketPayload\.createType\("neko:') {
    throw "FabricProtocolPackets still passes a namespaced string into CustomPacketPayload.createType(String). This crashes on Fabric 1.21-26.1 because createType uses the default namespace."
}

$versions = @(
    '1.21-Fabric 0.15.11',
    '1.21.1-Fabric 0.16.3',
    '1.21.2-Fabric 0.16.14',
    '1.21.3-Fabric 0.16.14',
    '1.21.4-Fabric 0.16.10',
    '1.21.4-Fabric 0.17.1',
    '1.21.5-Fabric 0.16.14',
    '1.21.7-Fabric 0.16.14',
    '1.21.8-Fabric 0.18.4',
    '1.21.11-Fabric 0.18.4'
)

foreach ($version in $versions) {
    $jar = Get-ChildItem -LiteralPath (Join-Path 'D:\HackClient\versions' $version) -Recurse -Filter 'client-intermediary.jar' |
        Select-Object -First 1 -ExpandProperty FullName
    if (-not $jar) {
        throw "Missing client-intermediary.jar for $version"
    }

    $out = (javap -classpath $jar -c 'net.minecraft.class_8710' 2>$null) -join "`n"
    if ($out -notmatch 'Method net/minecraft/class_2960\.method_60656:\(Ljava/lang/String;\)Lnet/minecraft/class_2960;') {
        throw "$version no longer shows CustomPacketPayload.createType(String) using default namespace semantics. Re-audit before changing compatibility logic."
    }
}

$namedJar = 'D:\HackClient\versions\26.1.1-Fabric 0.19.2\26.1.1-Fabric 0.19.2.jar'
$namedOut = (javap -classpath $namedJar -c 'net.minecraft.network.protocol.common.custom.CustomPacketPayload' 2>$null) -join "`n"
if ($namedOut -notmatch 'Method net/minecraft/resources/Identifier\.withDefaultNamespace:\(Ljava/lang/String;\)Lnet/minecraft/resources/Identifier;') {
    throw '26.1.1-Fabric 0.19.2 no longer shows CustomPacketPayload.createType(String) using default namespace semantics. Re-audit before changing compatibility logic.'
}

Write-Output 'Fabric payload type compatibility audit passed.'
