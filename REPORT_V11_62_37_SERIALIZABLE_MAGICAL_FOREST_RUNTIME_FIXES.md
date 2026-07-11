# Thaumcraft Legacy Rebuild v11.62.37

This batch continues the Forge 1.19.2 port from v11.62.36 and preserves its integrated Research Table puzzle, aura-node blend split, Thaumometer readout, and original node-stabilizer model.

## Runtime fixes

- Replaced the custom `MagicalForestBiomeSource` instance codec. Its `Codec.unit(this)` implementation could not be serialized by Minecraft and produced `Unknown registry element ... biome_source` while saving a world.
- The installer now copies the active Overworld `MultiNoiseBiomeSource`, substitutes Flower Forest climate points with `thaumcraft:magical_forest`, and installs a standard serializable source.
- Added the narrow Forge access transformer required to read the climate parameters and replace the generator source.
- Installation is verified both at server start and after startup. Diagnostics require the biome to be present in `possibleBiomes()`, which makes `/locate biome thaumcraft:magical_forest` able to search newly generated terrain.
- Migration-only `tc4_*` item aliases no longer belong to any creative tab, preventing unusable clones from leaking into vanilla Search.
- Reduced and recentered wand item transforms in GUI, first-person, and third-person views.

## Verification performed

- Java 17 compilation succeeded against Forge 43.5.2 / Minecraft 1.19.2.
- Clean GameTest server boot succeeded.
- The same generated world was loaded a second time successfully.
- Both runs reported `sourceClass=net.minecraft.world.level.biome.MultiNoiseBiomeSource`, `sourcePossible=true`, and `locateVisible=true`.
- No `Unknown registry element`, `WorldGenSettings`, or biome-source serialization error appeared on save/reload.
- Resource audit: 689 item models, 0 visible clone leaks, 0 resource problems.
- Research Table, worldgen, and visual parity guards passed.

The port is still an active reconstruction and is not claimed to be complete TC4 parity. Client-side visual and gameplay testing remains necessary for every legacy system.
