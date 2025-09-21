plugins {
    id("com.falsepattern.fpgradle-mc") version "2.1.0"
}

group = "com.ventooth"

minecraft_fp {
    java {
        compatibility = jabel
    }

    mod {
        modid = "swansong"
        name = "SwanSong"
        rootPkg = "$group.swansong"
    }

    mixin {
        pkg = "mixin.mixins"
    }

    core {
        coreModClass = "asm.CoreLoadingPlugin"
        accessTransformerFile = "swansong_at.cfg"
    }

    tokens {
        tokenClass = "Tags"
    }

    publish {
        maven {
            repoUrl = "https://mvn.ventooth.com/releases"
            repoName = "venmaven"
        }
        curseforge {
            projectId = "1349982"
            dependencies {
                required("fplib")
            }
        }
        modrinth {
            projectId = "2mCumCZs"
            dependencies {
                required("fplib")
            }
        }
    }
}

tasks.runClient {
    jvmArgs("-ea:com.ventooth.swansong...")
}

val venterceptorVersion = "2.0.0-rc4"

tasks.processResources {
    val tmp = venterceptorVersion
    from(configurations.compileClasspath.map { it.filter { file -> file.name.contains("venterceptor-service-api") } }) {
        into("META-INF/falsepatternlib_repo/com/ventooth/venterceptor-service-api/${tmp}/")
    }
    from(file(".idea/icon.png")) {
        rename { "swansong.png" }
    }
    filesMatching("META-INF/swansong_deps.json", {
        expand("venterceptor_version" to tmp)
    })
    from(file("LICENSE"))
    from(file("LICENSES")) {
        into("LICENSES")
    }
}

repositories {
    exclusive(mavenpattern(), "com.falsepattern")
    exclusive(venmaven(), "com.ventooth")
    cursemavenEX()
    modrinthEX()
    exclusive(mega(), "codechicken")
    exclusive(horizon(), "com.github.GTNewHorizons")
    exclusive(ivy("mavenpattern_mirror", "https://mvn.falsepattern.com/releases/mirror/", "[orgPath]/[artifact]-[revision].[ext]"), "mirror.micdoodle")
    exclusive(ivy("github", "https://github.com/", "[orgPath]/releases/download/[revision]/[artifact]-[revision](-[classifier]).[ext]"), "jss2a98aj.NotFine")
}

dependencies {
    apiSplit("com.falsepattern:falsepatternlib-mc1.7.10:1.9.0")

    compileOnly("com.ventooth:venterceptor-service-api:${venterceptorVersion}")
    runtimeOnlyNonPublishable("com.ventooth:venterceptor-mc1.7.10:${venterceptorVersion}")

    compileOnly("org.joml:joml:1.10.8")
    compileOnly("it.unimi.dsi:fastutil:8.5.16")
    compileOnly("net.java.dev.jna:jna:5.17.0")

    val testOpenBlocks = false
    val testBiomesOPlenty = false
    val testThaumcraft = false
    val testNotEnoughItems = true
    val testRailcraft = false
    val testOpenComputers = false
    val testAvaritia = false
    val testCoFHCore = false
    val testThermalExpansion = false
    val testJourneyMap = false
    val testNTM = false
    val testBotania = false
    val testGardenOfGlass = false
    val testDecocraft = false
    val testNotFine = false

    // OpenModsLib-1.7.10-0.10-deobf.jar
    val OpenModsLib = "curse.maven:openmodslib-228815:2386729"
    // OpenBlocks-1.7.10-1.6-deobf.jar
    val OpenBlocks = "curse.maven:openblocks-228816:2386734"
    if (testOpenBlocks) {
        runtimeOnlyNonPublishable(OpenModsLib)
        runtimeOnlyNonPublishable(OpenBlocks)
    }

    // BiomesOPlenty-1.7.10-2.1.0.2308-universal.jar
    val BiomesOPlenty = deobfCurse("biomes-o-plenty-220318:2499612")
    if (testBiomesOPlenty) {
        runtimeOnlyNonPublishable(BiomesOPlenty)
    }

    // BaublesExpanded-2.2.1.jar
    val BaublesExpanded = deobfModrinth("baubles-expanded:2.2.1")
    if (testThaumcraft || testBotania) {
        runtimeOnlyNonPublishable(BaublesExpanded)
    }

    // Thaumcraft-1.7.10-4.2.3.5.jar
    val Thaumcraft = deobfCurse("thaumcraft-223628:2227552")
    if (testThaumcraft) {
        devOnlyNonPublishable(Thaumcraft)
    } else {
        compileOnly(Thaumcraft)
    }

    // notenoughitems-mc1.7.10-2.4.2-mega-dev.jar
    val NotEnoughItems = "codechicken:notenoughitems-mc1.7.10:2.4.2-mega:dev"
    if (testNotEnoughItems) {
        devOnlyNonPublishable(NotEnoughItems)
    }

    // Railcraft_1.7.10-9.12.2.1.jar
    val Railcraft = deobfCurse("railcraft-51195:2458987")
    if (testRailcraft) {
        runtimeOnlyNonPublishable(Railcraft)
    }

    // OpenComputers-1.11.16-GTNH-dev.jar
    val OpenComputers = "com.github.GTNewHorizons:OpenComputers:1.11.16-GTNH:dev"
    if (testOpenComputers) {
        devOnlyNonPublishable(OpenComputers)
    } else {
        compileOnly(OpenComputers)
    }

    // Avaritia-1.13.jar
    val Avaritia = deobfCurse("avaritia-233785:2519595")
    if (testAvaritia) {
        devOnlyNonPublishable(Avaritia)
    } else {
        compileOnly(Avaritia)
    }

    // CoFHCore-[1.7.10]3.1.4-329-dev.jar
    val CoFHCore = "curse.maven:cofh-core-69162:2388751"
    if (testCoFHCore || testThermalExpansion) {
        devOnlyNonPublishable(CoFHCore)
    } else {
        compileOnly(CoFHCore)
    }

    // ThermalFoundation-[1.7.10]1.2.6-118-dev.jar
    val ThermalFoundation = "curse.maven:thermal-foundation-222880:2388753"
    // ThermalExpansion-[1.7.10]4.1.5-248-dev.jar
    val ThermalExpansion = "curse.maven:thermal-expansion-69163:2388759"
    if (testThermalExpansion) {
        devOnlyNonPublishable(ThermalFoundation)
        devOnlyNonPublishable(ThermalExpansion)
    } else {
        compileOnly(ThermalExpansion)
        compileOnly(ThermalFoundation)
    }

    // journeymap-1.7.10-5.2.10-dev.jar
    val JourneyMap = "curse.maven:journeymap-32274:6706112"
    if (testJourneyMap) {
        devOnlyNonPublishable(JourneyMap)
    } else {
        compileOnly(JourneyMap)
    }

    // ntmspace-X5412_H261.jar
    val ntm = deobfModrinth("ntmspace:X5412_H261")
    if (testNTM) {
        devOnlyNonPublishable(ntm)
    } else {
        compileOnly(ntm)
    }

    // Botania r1.8-249.jar
    val botania = deobfCurse("botania-225643:2283837")
    if (testBotania) {
        devOnlyNonPublishable(botania)
    } else {
        compileOnly(botania)
    }

    // GardenOfGlass.jar
    val gardenOfGlass = deobfCurse("botania-garden-of-glass-232502:2246106")
    if (testGardenOfGlass) {
        devOnlyNonPublishable(gardenOfGlass)
    } else {
        compileOnly(gardenOfGlass)
    }

    // Decocraft-2.4.2_1.7.10.jar
    val decocraft = deobfCurse("decocraft-79616:2414535")
    if (testDecocraft) {
        devOnlyNonPublishable(decocraft)
    } else {
        compileOnly(decocraft)
    }

    val notfine = "jss2a98aj.NotFine:notfine:0.2.7:dev"
    if (testNotFine) {
        devOnlyNonPublishable(notfine)
    } else {
        compileOnly(notfine)
    }

    //Galacticraft Core 3.0.12.504
    //micdoodle8.com went down
    compileOnly(deobf("mirror.micdoodle:GalacticraftCore:1.7-3.0.12.504"))

    // DragonAPI 1.7.10 V33b.jar
    compileOnly(deobfCurse("dragonapi-235591:4722480"))
    // RotaryCraft 1.7.10 V33a.jar
    compileOnly(deobfCurse("rotarycraft-235596:4721191"))

    // zume-1.1.4.jar
    compileOnly(deobfModrinth("zume:1.1.4"))
}