plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}

include("uniforms")
include("shaderpack")
include("gl")
include("forge1710")
// TODO: Do we want a build-logic (reference bX)
// TODO: Do we want a version catalog, or is that 'too new for our pumps'?

rootProject.name = "SwanSong"
