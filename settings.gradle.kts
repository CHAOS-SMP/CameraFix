pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://repo.spongepowered.org/maven")
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        maven("https://mirrors.cloud.tencent.com/gradle-plugins/")
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "CameraFix"
include("fabric", "forge", "neoforge")
