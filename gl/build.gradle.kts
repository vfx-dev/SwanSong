plugins {
    id("java-library")
}

group = "com.ventooth"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository { maven("https://libraries.minecraft.net") }
        filter { includeGroup("org.lwjgl.lwjgl") }
    }
}

dependencies {
    api(project(":shaderpack"))

    annotationProcessor(compileOnly("org.projectlombok:lombok:1.18.42")!!)

    compileOnly("org.lwjgl.lwjgl:lwjgl:2.9.4-nightly-20150209") { isTransitive = false }
    compileOnly("net.java.dev.jna:jna:5.17.0")
    compileOnly("org.joml:joml:1.10.8")
    compileOnly("it.unimi.dsi:fastutil:8.5.16")
    compileOnly("org.apache.logging.log4j:log4j-api:2.0-beta9")
    compileOnly("org.apache.commons:commons-lang3:3.3.2")
    compileOnly("commons-io:commons-io:2.4")
    compileOnly("com.google.code.gson:gson:2.2.4")
    compileOnly("org.jetbrains:annotations:26.1.0")
}
