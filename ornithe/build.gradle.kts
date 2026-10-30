plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.17.19"
    id("ploceus") version "1.17.6"
}

group = "com.ventooth"
version = "0.0.0-dev"

java.toolchain {
    languageVersion = JavaLanguageVersion.of(25)
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository { maven("https://maven.cloverclient.com/releases") }
        filter { includeGroup("pl.tomgirl") }
    }
}

configurations.configureEach {
    exclude(group = "org.lwjgl.lwjgl")
}

loom {
    accessWidenerPath = file("src/main/resources/swansong.classtweaker")

    runs.named("client") {
        jvmArguments.add("-XstartOnFirstThread")
        jvmArguments.addAll("--enable-native-access=ALL-UNNAMED", "--sun-misc-unsafe-memory-access=allow")
    }
}

ploceus {
    setIntermediaryGeneration(2)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings(ploceus.featherMappings("2"))

    modImplementation("net.fabricmc:fabric-loader:0.19.5")
    ploceus.dependOsl("0.22.0")
    modRuntimeOnly("pl.tomgirl:pylon:0.2.1")
    compileOnly("pl.tomgirl:pylon:0.2.1")

    listOf(":uniforms", ":shaderpack", ":gl").forEach { include(implementation(project(it))!!) }
    include(implementation("org.joml:joml:1.10.8")!!)
}

tasks.assemble {
    dependsOn("remapJar")
}

tasks.processResources {
    val v = project.version
    inputs.property("version", v)

    filesMatching("fabric.mod.json") {
        expand("version" to v)
    }
}
