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

repositories.mavenCentral()

dependencies {
    annotationProcessor(compileOnly("org.projectlombok:lombok:1.18.42")!!)

    compileOnly("org.ow2.asm:asm-debug-all:5.0.3")
    compileOnly("org.joml:joml:1.10.8")
    compileOnly("it.unimi.dsi:fastutil:8.5.16")
    compileOnly("org.apache.logging.log4j:log4j-api:2.0-beta9")
    compileOnly("org.apache.commons:commons-lang3:3.3.2")
    compileOnly("org.jetbrains:annotations:26.1.0")
}
