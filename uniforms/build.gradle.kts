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

    // TODO: Is there a cleaner way of doing this?
    testAnnotationProcessor(testCompileOnly("org.projectlombok:lombok:1.18.42")!!)

    testImplementation("org.ow2.asm:asm-debug-all:5.0.3")
    testImplementation("org.joml:joml:1.10.8")
    testImplementation("it.unimi.dsi:fastutil:8.5.16")
    testImplementation("org.apache.logging.log4j:log4j-api:2.0-beta9")
    testImplementation("org.apache.commons:commons-lang3:3.3.2")
    testImplementation("org.jetbrains:annotations:26.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.0.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.3")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
        events("passed")
    }
    workingDir = file(layout.buildDirectory).resolve("tmp/runTest")
    doFirst {
        workingDir.mkdirs()
        workingDir.listFiles()!!.forEach { it ->
            if (!(it.isDirectory && it.name == "logs")) {
                it.deleteRecursively()
            }
        }
    }
}
