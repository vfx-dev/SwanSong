# Changelog

## [Unreleased]

### Added

- Warning if RPLE is installed and the player loads an incompatible shaderpack

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