# Sodium Core Shader Support
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/G8yJPRdl?logo=modrinth&label=downloads&link=https%3A%2F%2Fmodrinth.com%2Fmod%2Fsodium-core-shader-support)](https://modrinth.com/mod/sodium-core-shader-support)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/956376?logo=curseforge&link=https%3A%2F%2Fwww.curseforge.com%2Fminecraft%2Fmc-mods%2Fsodium-core-shader-support)](https://www.curseforge.com/minecraft/mc-mods/sodium-core-shader-support)
![License](https://img.shields.io/github/license/lni-dev/sodiumcoreshadersupport?color=%23f9af8b)
![Environment Client](https://img.shields.io/badge/environment-client-blue)
[![Discord](https://img.shields.io/discord/317290087383826442?label=discord)](https://discord.gg/shVe3cR)

Enables resourcepacks to replace sodium's shaders, similar to resourcepacks being able to replace vanilla's core shaders.
If you like my mods consider supporting the development by buying me a coffee:

[![ko-fi](https://github.com/lni-dev/lni-dev/blob/main/images/support-me-on-ko-fi-mc-banner-smaller.png?raw=true)](https://ko-fi.com/T6T41BS1C9)

## Documentation for Users

Sodium Core Shader Support allows resourcepacks to replace sodiums core shaders with their own. That does **not** mean, that
this mod makes every vanilla resourcepack work on sodium. The resourcepacks will only work if they specifically state, that
they are compatible with sodium core shader support.
<br><br>
If you are using a supported resourcepack you can simply activate it like any other resourcepack.

### Resourcepack incompatible
Sodium may display the message `The following resource packs are incompatible with Sodium` in the top right corner after
reloading resources and listing your active core
shaders. This message will always appear, even if the resourcepack supports sodium core shaders and can be ignored.

## List of Resourcepacks
This is a small list of resourcepacks that work on sodium. If you have created a resourcepack yourself feel free to
[open an issue on my github](https://github.com/lni-dev/SodiumCoreShaderSupport/issues) to add it to this list!
- [Energy Shaders \[Java\]](https://modrinth.com/shader/energy-shaders-java)
- [Night Vision Shaders \[Java\]](https://modrinth.com/shader/night-vision-shaders)

## Documentation for Shader Developers
It is important to understand that your vanilla shaders cannot just be copied to sodium shaders. Sodium has their own shaders
for blocks and clouds:
```
- assets/sodium/shaders/
   | - clouds.fsh
   | - clouds.vsh
   | - blocks/
        | - block_layer_opaque.fsh
        | - block_layer_opaque.vsh
   | - include/
        | - fog.glsl 
        | - chunk_material.glsl
        | - chunk_matrices.glsl
        | - chunk_vertex.glsl
```
Unless you want to change sodium clouds the `block_layer_opaque` are usually the most important ones.
You should not have to touch `chunk_material.glsl`, `chunk_matrices.glsl` and `chunk_vertex.glsl` unless you know what you
are doing.

### Retrieving sodium shaders source code
To retrieve the sodium shader source code for a specific sodium version, download the `.jar` file of that version and
extract it like a `.zip` archive. Inside the extracted archive you will find a `assets` folder containing the directory
structure mentioned above.

### block_layer_opaque
The `block_layer_opaque` is used for all blocks and block entities. The vanilla minecraft equivalent is `terrain.fsh`
and `terrain.vsh` (In older versions of minecraft the equivalent is `rendertype_solid`, `rendertype_cutout`, `rendertype_cutout_mipped`, ...).

Additionally, sodium core shader support adds a few define in the sodium shaders `block_layer_opaque.fsh`
and `block_layer_opaque.vsh` for some terrain-types:
- `RENDER_PASS_SOLID`: Solid Blocks
- `RENDER_PASS_CUTOUT`: Blocks like leaves, grass, glass, ...
- `RENDER_PASS_TRANSLUCENT`: Blocks with actual transparency, e.g. water, honey, slime, ...

These can be used like this:
```glsl
#ifdef RENDER_PASS_SOLID
    // special shading for solid stuff
#endif
```

### How to specify which sodium versions are supported
The sodium devs will change their internal shaders and shader related code without further notice. That's why it is
important that you specify with which versions of sodium and minecraft your pack is compatible. This can be done in a special
`versions.json` file. It should be located in your resourcepack in a new directory `assets/sodiumcoreshadersupport` with
the file name `versions.json` inside. The contents of the file could look like this:
```json
{
  "supported-versions": {
    "1.21": [ "0.5.11+mc1.21" ],
    "1.21.1": [ "0.5.11+mc1.21" ]
  }
}
```
In the above example the resourcepack states, that it is compatible with sodium `0.5.11` on minecraft `1.21` and `1.21.1`.
An abstract description is below:<br>
More specifically, `versions.json` must contain a question object with the key `supported-versions`.
The value of `supported-versions` must be a map, which maps different minecraft versions to an array of allowed sodium versions.
The SodiumCoreShaderSupport mod will check if the installed sodium version is contained in the array of the installed minecraft version.
<br>
- If the minecraft and/or sodium version is not inside `supported-versions`, the pack can be activated with a warning message.
- If the user does not have sodium installed the pack can be activated.
- If no versions.json is present, the pack can be activated with a warning message.
- If the versions.json is malformed, the pack can be activated with a warning message.

### Imports
Sodium Shaders must be in `assets/sodium/shaders` directory. But you can `#import` files from
`assets/minecraft/shaders` using the `#import` directive. The following code will include
the file `assets/minecraft/shaders/include/test.glsl`:
```glsl
#import <minecraft:include/test.glsl>
```
With this trick you can create custom glsl files, which you can `#import` in your shaders (in both core shaders and sodium shaders).
These files, should contain your main shader "logic". An example shaderpack, which
works with this trick on both vanilla and sodium, can be found [here](https://github.com/lni-dev/MinecraftShaders/tree/master/EnergyShaders%20%5BJava%5D/current/Energy%20Shaders%20%5BJava%5D/assets).

