# SodiumCoreShaderSupport

Enables resourcepacks to replace sodium's shaders, similar to resourcepacks being able to replace vanilla's core shaders.

## How to write shaders for sodium?
Sodium shaders are similar to core shaders, but not the same.
The base Sodium Shaders can be found [here](https://github.com/CaffeineMC/sodium-fabric/tree/dev/src/main/resources/assets/sodium/shaders).
Sodium Shaders must be in `assets/sodium/shaders` directory. But you can `#import` files from
`assets/minecraft/shaders` using the `#import` directive. The following code will include
the file `assets/minecraft/shaders/include/test.glsl`:
```glsl
#import <minecraft:include/test.glsl>
```
I recommend to create custom glsl files, which you can `#import` in your shaders (in both core shaders and sodium shaders).
These files, should contain your main shader "logic". An example shaderpack, which
works on both vanilla and sodium, can be found [here](https://github.com/lni-dev/MinecraftShaders/tree/master/EnergyShaders%20%5BJava%5D/current/Energy%20Shaders%20%5BJava%5D/assets).
