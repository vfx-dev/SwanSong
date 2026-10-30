pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.ornithemc.net/releases")
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}

include("uniforms")
include("shaderpack")
include("gl")
include("forge1710")
include("ornithe")

rootProject.name = "SwanSong"
