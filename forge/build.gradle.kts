import net.minecraftforge.gradle.userdev.UserDevExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.kotlin.dsl.getByType
import org.spongepowered.asm.gradle.plugins.MixinExtension

plugins {
    id("net.minecraftforge.gradle") version "6.0.53"
    id("org.spongepowered.mixin") version "0.7.38"
}

val minecraftVersion = providers.gradleProperty("minecraft_version").get()
val forgeVersion = providers.gradleProperty("forge_version").get()
val modVersion = providers.gradleProperty("mod_version").get()
val modId = providers.gradleProperty("mod_id").get()
val modName = providers.gradleProperty("mod_name").get()
val artifactVersion = providers.gradleProperty("artifact_version").get()
val mixinVersion = providers.gradleProperty("mixin_version").get()
val targetJavaVersion = providers.gradleProperty("target_java_version").get().toInt()
val includedDependencies by configurations.creating
val sourceSets = extensions.getByType<SourceSetContainer>()

dependencies {
    minecraft("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
    compileOnly("org.spongepowered:mixin:$mixinVersion")
    annotationProcessor("org.spongepowered:mixin:$mixinVersion:processor")
    implementation("cn.dg32z.neko:NekoReflection:1.4.0")
    includedDependencies("cn.dg32z.neko:NekoReflection:1.4.0")
}

configurations.named(sourceSets.named("main").get().runtimeClasspathConfigurationName) {
    extendsFrom(includedDependencies)
}

sourceSets {
    named("main") {
        java.srcDir(rootProject.file("src/main/java"))
        resources.srcDir(rootProject.file("src/main/resources"))
    }
}

configure<UserDevExtension> {
    mappings("official", minecraftVersion)
}

configure<MixinExtension> {
    add(sourceSets["main"], "$modId.refmap.json")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", modVersion)
    inputs.property("minecraft_version", minecraftVersion)
    inputs.property("forge_version", forgeVersion)
    inputs.property("mod_id", modId)
    inputs.property("mod_name", modName)
    filteringCharset = "UTF-8"

    filesMatching("META-INF/mods.toml") {
        expand(
            mapOf(
                "version" to modVersion,
                "minecraft_version" to minecraftVersion,
                "forge_version" to forgeVersion,
                "mod_id" to modId,
                "mod_name" to modName,
            )
        )
    }
}

tasks.named<Jar>("jar") {
    manifest.attributes(mapOf("MixinConfigs" to "$modId.mixins.json"))
    from(includedDependencies.map { dependency ->
        if (dependency.isDirectory) dependency else zipTree(dependency)
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("CameraFix-forge-$artifactVersion.jar")
    destinationDirectory.set(rootProject.layout.buildDirectory)
}
