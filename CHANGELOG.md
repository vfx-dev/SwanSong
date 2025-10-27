# Changelog

## [1.2.5] - 2025-10-27

### Changed

- Internal logic changes for FalseTweaks' animated PBR texture fixes

### Fixed

- Animated PBRs on items completely broken

## [1.2.4] - 2025-10-27

### Added

- Normals/Speculars can now inherit animations from the base texture

## [1.2.3] - 2025-10-27

### Changed

- Updated zn_CN.lang

### Fixed

- NullPointerException crash when reloading shaders
- Crash with NTM: Space

## [1.2.2] - 2025-10-16

### Changed

- Internal debug shaderpack is now hidden by default

## [1.2.1] - 2025-09-28

### Added

- Compatibility for the Modern Warfare mod

### Fixed

- Crash with DragonAPI when shaders are disabled
- Actually releasing shader objects after compilation and linking

## [1.2.0] - 2025-09-26

### Added

- Option for turning off shaders
- Config option for toggling zoom sound

### Fixed

- NTM: Space rendering a black screen with Chocapic13
- Thaumcraft Cultist floaty line rendering

## [1.1.2] - 2025-09-25

### Added

- Warning if RPLE is installed and the player loads an incompatible shaderpack

### Removed

- Incorrect call to glValidateProgram()

## [1.1.1] - 2025-09-24

### Fixed

- Broken worldDay uniform

## [1.1.0] - 2025-09-24

### Changed

- Shader option's default values now count as an allowed value

### Added

- Support for smoothCenterDepth (Needs to be enabled in the config, will be automatic in the next update)
- Support for atlasSize
- Debug marker for screenshots
- Better error handling on shader initialization
- Support for custom textures
- zh_CN translations ([#6](https://github.com/vfx-dev/SwanSong/pull/6)) (Omgise)
- Camera position and eye brightness uniforms

### Fixed

- Mipmaps in final pass not generating
- Logspam when loading PBRs
- Wrong URL in mcmod.info
- Invalid eyeBrightnessSmoothRound making Chocapic13 shaders too bright
- Wrong framebuffer attachment indices breaking most shaderpacks
- Preprocessor generating broken code with compact mode enabled (default)

## [1.0.8] - 2025-09-21

### Changed

- Shader validation issues causing shaderpacks to fail
  - Intel IGPUs implement the spec differently, causing validation to always fail at the point where we use it

## [1.0.7] - 2025-09-21

### Fixed

- Broken integer parsing causing various issues across the codebase

## [1.0.6] - 2025-09-21

### Added

- Compatibility with arbitrary atlases
  - Immersive Engineering revolver uses this for instance

## [1.0.5] - 2025-09-21

### Fixed

- Font renderer crash with Hodgepodge/LegacyFixes

## [1.0.4] - 2025-09-20

### Changed

- Disable FunkyZoom is Zume is present

## [1.0.3] - 2025-09-20

### Fixed

- Crash in the settings hooks for anaglyph

## [1.0.2] - 2025-09-20

### Fixed

- Anaglyph slider not working with NotFine

## [1.0.1] - 2025-09-20

### Fixed

- Crash with LWJGL3ify

## [1.0.0] - 2025-09-20

_Initial release._