import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("net.neoforged.moddev") version "2.0.141"
}

val minecraftVersion = providers.gradleProperty("minecraft_version").get()
val neoforgeVersion = providers.gradleProperty("neoforge_version").get()
val modVersion = providers.gradleProperty("mod_version").get()
val modId = providers.gradleProperty("mod_id").get()
val modName = providers.gradleProperty("mod_name").get()
val artifactVersion = providers.gradleProperty("artifact_version").get()
val mixinVersion = providers.gradleProperty("mixin_version").get()
val targetJavaVersion = providers.gradleProperty("target_java_version").get().toInt()

dependencies {
    compileOnly("org.spongepowered:mixin:$mixinVersion")
    implementation("cn.dg32z.neko:NekoReflection:1.4.0")
    jarJar("cn.dg32z.neko:NekoReflection:1.4.0")
}

sourceSets {
    named("main") {
        java.srcDir(rootProject.file("src/main/java"))
        resources.srcDir(rootProject.file("src/main/resources"))
    }
}

neoForge {
    version = neoforgeVersion
    mods {
        register(modId) {
            sourceSet(sourceSets["main"])
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", modVersion)
    inputs.property("minecraft_version", minecraftVersion)
    inputs.property("neoforge_version", neoforgeVersion)
    inputs.property("mod_id", modId)
    inputs.property("mod_name", modName)
    filteringCharset = "UTF-8"

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            mapOf(
                "version" to modVersion,
                "minecraft_version" to minecraftVersion,
                "neoforge_version" to neoforgeVersion,
                "mod_id" to modId,
                "mod_name" to modName,
            )
        )
    }
}

tasks.named<Jar>("jar") {
    manifest.attributes(mapOf("MixinConfigs" to "$modId.mixins.json"))
    archiveFileName.set("CameraFix-neoforge-$artifactVersion.jar")
    destinationDirectory.set(rootProject.layout.buildDirectory)
}
