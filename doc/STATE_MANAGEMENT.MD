# State Management

## General

- Init
  - Happens as early as OpenGL and resources are present (crash early)
- Deinit
  - Deprecate logic (would happen on game close)
- Reload
  - On shaderpack change
  - On resourcepack change
  - On world dim change
- Window resize
  - Do early (and explicit)

## Stages

Calls to *start*, no calls to *end*

- Terrain
- Particles
- Entities
- etc...

## Wrappers

Calls to *start* and calls to *end*

Eg. wrap "block destroy progress"

Sometime wrappers have nesting where "begin"/"end" may be called multiple times?
Such as where multiple nested hooks are needed, check the font hooks.

## Conditionals

Handling cases where text or whatever needs to render on a sign

Or where something custom needs to use a basic/textured/textured_lit shader on demand, but then switch back to entity/block

