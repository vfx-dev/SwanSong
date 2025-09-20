<!--
Copyright 2024 WhyFenceCode

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
-->

## Camera/Player
| Uniform                     | Type  | Value range  | Description                                                        | Tag  |
|-----------------------------|-------|--------------|--------------------------------------------------------------------|------|
| cameraPosition              | vec3  |              | Position of the camera in world space                              |      |
| eyeAltitude                 | float |              | Y coordinate of the player in blocks                               |      |
| cameraPositionFract         | vec3  | [0,1)        | Fractional component of the camera position in world space         | Iris |
| cameraPositionInt           | ivec3 |              | Integer component of the camera position in world space            | Iris |
| previousCameraPosition      | vec3  |              | Value of `cameraPosition` from the previous frame                  | Iris |
| previousCameraPositionFract | vec3  | [0,1)        | Value of `cameraPositionFract` from the previous frame             | Iris |
| previousCameraPositionInt   | ivec3 |              | Value of `cameraPositionInt` from the previous frame               | Iris |
| eyePosition                 | vec3  |              | World space position of the player's head model                    | Iris |
| relativeEyePosition         | vec3  |              | World space offset from the player head to the camera              | Iris |
| playerBodyVector            | vec3  | [0,1]        | World aligned direction of player model's body                     | Iris |
| playerLookVector            | vec3  | [0,1]        | World aligned direction of player model's head                     | Iris |
| upPosition                  | vec3  | [0, 100]     | Upwards direction in view space, length of 100                     |      |
| eyeBrightness               | ivec2 | [0, 240]     | Light value at the player's location: (block, sky)                 |      |
| eyeBrightnessSmooth         | ivec2 | [0, 240]     | `eyeBrightness` smoothed over time by `eyeBrightnessHalfLife`      |      |
| centerDepthSmooth           | float | [0,1]        | Depth buffer value at the center of the screen, smoothed over time |      |
| firstPersonCamera           | bool  | true / false | Whether the player camera is in first person mode                  | Iris |

## Player Status
| Uniform             | Type  | Value range   | Description                                                    | Tag  |
|---------------------|-------|---------------|----------------------------------------------------------------|------|
| isEyeInWater        | int   | 0, 1, 2, 3    | Fluid that the camera is currently in                          |      |
| isSpectator         | bool  | true / false  | Whether the player is currently in spectator mode              | Iris |
| isRightHanded       | bool  | true / false  | Whether the player's main hand is set to right hand            | Iris |
| blindness           | float | [0,1]         | Blindness effect multiplier                                    |      |
| darknessFactor      | float | [0,1]         | Strength of the darkness effect                                |      |
| darknessLightFactor | float | [0,1]         | Strength of the dimming effect from the darkness status effect |      |
| nightVision         | float | [0,1]         | Night vision effect multiplier                                 |      |
| playerMood          | float | [0,1]         | Player mood value                                              |      |
| constantMood        | float | [0,1]         | `playerMood` but it doesn't reset at `1.0`                     | Iris |
| currentPlayerAir    | float | [0,1]         | Normalized air the player has remaining                        | Iris |
| maxPlayerAir        | float | -1, 300       | Maximum player air when underwater                             | Iris |
| currentPlayerArmor  | float | -1, [0,1]     | Normalized armor player has equipped                           | Iris |
| maxPlayerArmor      | float | 50            | Maximum player armor value                                     | Iris |
| currentPlayerHealth | float | -1, [0,1]     | Normalized health the player has remaining                     | Iris |
| maxPlayerHealth     | float | -1, [0, 1024] | Maximum player health value                                    | Iris |
| currentPlayerHunger | float | -1, [0,1]     | Normalized hunger level of player                              | Iris |
| maxPlayerHunger     | float | 20            | Maximum player hunger value                                    | Iris |
| is_burning          | bool  | true / false  | Whether the player is currently on fire                        | CU   |
| is_hurt             | bool  | true / false  | Whether the player is currently taking damage                  | CU   |
| is_invisible        | bool  | true / false  | Whether the player is invisible                                | CU   |
| is_on_ground        | bool  | true / false  | Whether the player is currently touching the ground            | CU   |
| is_sneaking         | bool  | true / false  | Whether the player is currently sneaking                       | CU   |
| is_sprinting        | bool  | true / false  | Whether the player is currently sprinting                      | CU   |
| hideGUI             | bool  | true / false  | Whether the player's GUI is hidden                             |      |

## Screen/System
| Uniform           | Type  | Value range   | Description                                                    | Tag  |
|-------------------|-------|---------------|----------------------------------------------------------------|------|
| viewHeight        | float | [1,∞)         | Height of the game window in pixels                            |      |
| viewWidth         | float | [1,∞)         | Width of the game window in pixels                             |      |
| aspectRatio       | float | (0,∞)         | Aspect ratio of the game window                                |      |
| screenBrightness  | float | [0,1]         | Screen brightness from video settings                          |      |
| frameCounter      | int   | [0, 720719]   | Number of frames since start of program                        |      |
| frameTime         | float | (0,∞)         | Frame time of the previous frame in seconds                    |      |
| frameTimeCounter  | float | [0, 3600)     | Running time of the game in seconds                            |      |
| currentColorSpace | int   | 0, 1, 2, 3, 4 | Display color space, controlled through video settings         | Iris |
| currentDate       | ivec3 |               | System date: (year, month, day)                                | Iris |
| currentTime       | ivec3 |               | System time: (hour, minute, second)                            | Iris |
| currentYearTime   | ivec2 |               | Time since beginning of the year and until the end of the year | Iris |

## ID
| Uniform                 | Type | Value range     | Description                                                             | Tag    |
|-------------------------|------|-----------------|-------------------------------------------------------------------------|--------|
| entityId                | int  | [0, 65535]      | ID of the currently rendering entity (gbuffers_entities)                |        |
| blockEntityId           | int  | [-32768, 32767] | ID of the currently rendering block entity (gbuffers_block)             | NOT CU |
| currentRenderedItemId   | int  | [0,65535]       | Item ID of currently rendering item/armor/trim                          | Iris   |
| currentSelectedBlockId  | int  | [-32768, 32767] | Block ID of block selected by the player                                | Iris   |
| currentSelectedBlockPos | vec3 |                 | Player space position of the center of the block selected by the player | Iris   |
| heldItemId              | int  | [0, 65535]      | Item ID of the item in the player's hand                                |        |
| heldItemId2             | int  | [0, 65535]      | Item ID of the item in the player's offhand                             |        |
| heldBlockLightValue     | int  | [0,15]          | Light value of the item held in the player's hand                       |        |
| heldBlockLightValue2    | int  | [0,15]          | Light value of the item held in the player's offhand                    |        |

## World/Weather
| Uniform               | Type  | Value range | Description                                                  | Tag  |
|-----------------------|-------|-------------|--------------------------------------------------------------|------|
| sunPosition           | vec3  | [0,100]     | Position of the sun in view space, length of 100             |      |
| moonPosition          | vec3  | [0,100]     | Position of the moon in view space, length of 100            |      |
| shadowLightPosition   | vec3  | [0,100]     | Position of shadow source in view space, length of 100       |      |
| sunAngle              | float | [0,1]       | Angle of the sun within the complete day-night cycle         |      |
| shadowAngle           | float | [0,0.5]     | Angle of shadow source (whichever is higher in the sky)      |      |
| moonPhase             | int   | [0,7]       | Current moon phase                                           |      |
| rainStrength          | float | [0,1]       | Current strength of rain                                     |      |
| wetness               | float | [0,1]       | `rainStrength` but smoothed over time with `wetnessHalfLife` |      |
| thunderStrength       | float | [0,1]       | Current strength of thunderstorm                             | Iris |
| lightningBoltPosition | vec4  |             | Position of a lightning bolt being rendered, or `vec4(0.0)`  | Iris |
| worldTime             | int   | [0, 23999]  | Current in-game time                                         |      |
| worldDay              | int   | [0 - )      | Number of in-game days passed                                |      |

## Biome/Dimension
| Uniform             | Type  | Value range   | Description                                                         | Tag  |
|---------------------|-------|---------------|---------------------------------------------------------------------|------|
| biome               | int   |               | Biome currently occupied by the player                              | CU   |
| biome_category      | int   |               | Category of the biome currently occupied by the player              | CU   |
| biome_precipitation | int   | 0, 1, 2       | Type of precipitation in the current biome                          | CU   |
| rainfall            | float | [0,1]         | Rainfall property of the current biome                              | CU   |
| temperature         | float |               | Temperature property of the current biome                           | CU   |
| ambientLight        | float | [0,1]         | Ambient light property of the current dimension                     | Iris |
| bedrockLevel        | int   | [-2032, 2016] | Y coordinate of the bedrock floor in the current dimension          | Iris |
| cloudHeight         | float |               | Y coordinate of the vanilla cloud plane                             | Iris |
| hasCeiling          | bool  | true / false  | Whether the current dimension has a ceiling                         | Iris |
| hasSkylight         | bool  | true / false  | Whether the current dimension has sky lighting                      | Iris |
| heightLimit         | int   | [16, 4064]    | Distance from maximum to minimum block heights in current dimension | Iris |
| logicalHeightLimit  | int   |               | Logical height of the current dimension                             | Iris |

## Rendering
| Uniform      | Type  | Value range      | Description                                                       | Tag    |
|--------------|-------|------------------|-------------------------------------------------------------------|--------|
| near         | float | 0.05             | Near clipping plane distance                                      |        |
| far          | float | (0, - )          | Current render distance in blocks                                 |        |
| alphaTestRef | float | [0,1]            | Cutout alpha discard threshold                                    |        |
| chunkOffset  | vec3  |                  | Chunk offset for terrain model space position                     | NOT CU |
| entityColor  | vec4  | [0,1]            | Entity tint color                                                 | NOT CU |
| blendFunc    | ivec4 |                  | Alpha blending function multipliers: (srcRGB, dstRGB, srcA, dstA) |        |
| atlasSize    | ivec2 | [0, - )          | Resolution of the texture atlas, `0` if not bound                 |        |
| renderStage  | int   |                  | "Render stage" of the current geometry                            |        |
| fogColor     | vec3  | [0,1]            | Horizon fog color                                                 | NOT CU |
| skyColor     | vec3  | [0,1]            | Upper sky color                                                   |        |
| fogDensity   | float | [0,1]            | Relative fog density                                              |        |
| fogStart     | float | (0, - )          | Starting fog distance in blocks                                   |        |
| fogEnd       | float | (0, - )          | Ending fog distance in blocks                                     |        |
| fogMode      | int   | 2048, 2049, 2048 | Fog type used for vanilla fog                                     | NOT CU |
| fogShape     | int   | 0,1              | Fog shape used for vanilla fog                                    |        |

## Matrices
| Uniform                   | Type | Value range | Description                                                            | Tag |
|---------------------------|------|-------------|------------------------------------------------------------------------|-----|
| gbufferModelView          | mat4 |             | Player space to view space in general                                  |     |
| gbufferModelViewInverse   | mat4 |             | Converts from view space to player space in general                    |     |
| gbufferProjection         | mat4 |             | Converts from view space to clip space in general                      |     |
| gbufferProjectionInverse  | mat4 |             | Converts from clip/screen to view space in general                     |     |
| shadowModelView           | mat4 |             | Converts from player space to shadow view space in general             |     |
| shadowModelViewInverse    | mat4 |             | Converts from shadow view space to player space in general             |     |
| shadowProjection          | mat4 |             | Converts from shadow view space to shadow clip space in general        |     |
| shadowProjectionInverse   | mat4 |             | Converts from shadow clip/screen space to shadow view space in general |     |
| gbufferPreviousModelView  | mat4 |             | Value of `gbufferModelView` from the previous frame                    |     |
| gbufferPreviousProjection | mat4 |             | Value of `gbufferProjection` from the previous frame                   |     |
| modelViewMatrix           | mat4 |             | Converts from model space to view space in general                     |     |
| modelViewMatrixInverse    | mat4 |             | Converts from view space to model space for current geometry           |     |
| projectionMatrix          | mat4 |             | Converts from view space to clip space for current geometry            |     |
| projectionMatrixInverse   | mat4 |             | Converts from clip/screen space to view space for current geometry     |     |
| normalMatrix              | mat3 |             | Converts normals from model space to view space for current geometry   |     |
| textureMatrix             | mat4 |             | Transforms texture coordinates before sampling                         |     |