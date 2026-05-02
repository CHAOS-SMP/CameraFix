import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.kotlin.dsl.getByType
import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    java
    id("fabric-loom") version "1.12-SNAPSHOT"
}

val minecraftVersion = providers.gradleProperty("minecraft_version").get()
val fabricLoaderVersion = providers.gradleProperty("fabric_loader_version").get()
val fabricVersion = providers.gradleProperty("fabric_version").get()
val modVersion = providers.gradleProperty("mod_version").get()
val modId = providers.gradleProperty("mod_id").get()
val modName = providers.gradleProperty("mod_name").get()
val artifactVersion = providers.gradleProperty("artifact_version").get()
val sourceSets = extensions.getByType<SourceSetContainer>()
val loom = extensions.getByType<LoomGradleExtensionAPI>()

dependencies {
    add("minecraft", "com.mojang:minecraft:$minecraftVersion")
    add("mappings", loom.officialMojangMappings())
    add("modImplementation", "net.fabricmc:fabric-loader:$fabricLoaderVersion")
    add("modImplementation", "net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    include(implementation("cn.dg32z.neko:NekoReflection:1.4.0")!!)
}

sourceSets.named("main") {
    java.srcDir(rootProject.file("src/main/java"))
    resources.srcDir(rootProject.file("src/main/resources"))
}

loom.mods {
    register(modId) {
        sourceSet(sourceSets.getByName("main"))
    }
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", modVersion)
    inputs.property("minecraft_version", minecraftVersion)
    inputs.property("fabric_loader_version", fabricLoaderVersion)
    inputs.property("mod_id", modId)
    inputs.property("mod_name", modName)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to modVersion,
                "minecraft_version" to minecraftVersion,
                "fabric_loader_version" to fabricLoaderVersion,
                "mod_id" to modId,
                "mod_name" to modName,
            )
        )
    }
}

tasks.named<AbstractArchiveTask>("remapJar") {
    archiveFileName.set("CameraFix-fabric-$artifactVersion.jar")
    destinationDirectory.set(rootProject.layout.buildDirectory)
}
