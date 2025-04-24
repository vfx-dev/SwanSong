# Pre-Release TODO

Big list of things we need to do before a public release.

## Crashes (Probably works?)

### Problem

Currently loading a bad shader will crash the game. This should never happen if we can avoid it. In particular the crashes relate to "broken" shader packs that lack some essential shader (e.g. `gbuffers_basic`).

Shaders that fail to load should switch back to the FALLBACK shader, and report the appropriate exception.

### Notes

- Tested it by typing random stuff into BSL basic shader, successfully falls back to internal shader
- Further testing by loading bad/malformed shaderpacks, perhaps random modern ones!

## Debug/Logging

### Problem

As of writing, the debug flags default to `true`. This must default to `false` in future releases.

The biggest lack in debug logging is the lack of a full report when ShaderPack loading succeeds/fails. With information such as:

- How long the loading took?
- What shaders were found/loaded?
- Information about custom textures (if any)
- How many draw buffers were used, with what formats?
- What shaders were loaded directly or used a fallback instead?

Keep in mind this report MUST be printed IN FULL at the very end of the shader loading. Partial messages may be printed under debug/trace/warn/error, but a successful operation should only print within a single info block once loading is complete.

There are also numerous places where logging is done to `System.out` or `System.err` which should be routed to either the `Share.log`, or into dedicated loggers (dedicated is preferred, but decide case-by-case depending on importance of the info being logged)

Exception logging is also sometimes routed via `Throwable#printStackTrace()`, these should also be routed into the logging framework.

Using the `assert` statement with `-ea:com.ventooth.swansong...` should be preferred over `throw new AssertionError`

### Notes

- Debugging still enabled by default?
- 90% Complete, should try it out with a few broken shader examples and see how easy it is to diagnose.

## Uniforms

### Problem

Some of the expected uniforms are not yet being provided, alongside using floats instead of doubles on the CPU side.
We should always use doubles as the internal backing and just cast to floats when needed.

### Notes

- Ported uniforms to doubles
- Added more of the uniforms to the custom uniform compiler

## Custom Textures

### Problem

Currently the only custom texture we handle is the noise, some shader packs want more than that.

### Notes

I am unsure about the semantics of what happens when a custom texture overlaps with a color buffer, need to test
against Iris/OptiFine and treat that as the ground truth.

## Windows/Nvidia GPU Testing

### Problem

Broken on Houston's machine. Need to setup Ven's vms to test.

### Notes

Yeah.

## Beddium Compat Moment

### Problem

Beddium hijacks the TileEntity rendering, potentially unbinding our shaders.

### Notes

Yeah!!