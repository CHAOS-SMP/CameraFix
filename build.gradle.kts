import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    base
}

val mavenGroup = providers.gradleProperty("maven_group").get()
val modVersion = providers.gradleProperty("mod_version").get()
val targetJavaVersion = providers.gradleProperty("target_java_version").get().toInt()

group = mavenGroup
version = modVersion

allprojects {
    group = mavenGroup
    version = modVersion

    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://repo.spongepowered.org/maven")
        maven("http://110.42.99.246:8080/releases") {
            isAllowInsecureProtocol = true
        }
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        mavenCentral()
    }
}

subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
    }

    tasks.withType<ProcessResources>().configureEach {
        filteringCharset = "UTF-8"
    }
}

tasks.named("build").configure {
    dependsOn(subprojects.map { "${it.path}:build" })
}

tasks.named("assemble").configure {
    dependsOn(subprojects.map { "${it.path}:assemble" })
}

tasks.named("clean").configure {
    dependsOn(subprojects.map { "${it.path}:clean" })
}
