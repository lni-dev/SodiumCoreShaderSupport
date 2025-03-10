# SodiumCoreShaderSupport

Enables resourcepacks to replace sodium's shaders, similar to resourcepacks being able to replace vanilla's core shaders.

## How to specify which sodium versions are supported
Inside your resourcepack create a new directory `assets/sodiumcoreshadersupport` and the file `versions.json` inside. Example:
```json
{
  "supported-versions": {
    "1.21": [ "0.5.11+mc1.21" ],
    "1.21.1": [ "0.5.11+mc1.21" ]
  }
}
```
Inside `supported-versions` must be a map, which maps different minecraft versions to an array of allowed sodium versions.
The SodiumCoreShaderSupport mod will check if the installed sodium version is contained in the array of the installed minecraft version.
<br>
- If the minecraft and/or sodium version is not inside `supported-versions`, the pack can be activated with a warning message.
- If the user does not have sodium installed the pack can be activated.
- If no versions.json is present, the pack can be activated with a warning message.
- If the versions.json is malformed, the pack can be activated with a warning message.

## How to write shaders for sodium?
Sodium shaders are similar to core shaders, but not the same.
The base Sodium Shaders can be found [here](https://github.com/CaffeineMC/sodium-fabric/tree/dev/common/src/main/resources/assets/sodium/shaders).
Sodium Shaders must be in `assets/sodium/shaders` directory. But you can `#import` files from
`assets/minecraft/shaders` using the `#import` directive. The following code will include
the file `assets/minecraft/shaders/include/test.glsl`:
```glsl
#import <minecraft:include/test.glsl>
```
I recommend to create custom glsl files, which you can `#import` in your shaders (in both core shaders and sodium shaders).
These files, should contain your main shader "logic". An example shaderpack, which
works on both vanilla and sodium, can be found [here](https://github.com/lni-dev/MinecraftShaders/tree/master/EnergyShaders%20%5BJava%5D/current/Energy%20Shaders%20%5BJava%5D/assets).

### Differentiate Terrain-Types in Shaders
In the sodium shaders `block_layer_opaque.fsh` and `block_layer_opaque.vsh` additional defines will be present for some
terrain-types:
- `RENDER_PASS_SOLID`: Solid Blocks
- `RENDER_PASS_CUTOUT`: Blocks like leaves, grass, glass, ...
- `RENDER_PASS_TRANSLUCENT`: Blocks with actual transparency, e.g. water, honey, slime, ...
