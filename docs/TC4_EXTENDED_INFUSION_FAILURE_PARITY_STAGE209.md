# Stage209 — TC4 extended infusion failure parity

Stage209 continues from Stage208 and compares the current 1.19.2 port against the original Thaumcraft 4.2.3.5 / Minecraft 1.7.10 implementation, especially `thaumcraft/common/tiles/TileInfusionMatrix.java`, `PacketFXInfusionSource`, `PacketFXBlockZap`, `BlockFluxGoo`, `BlockFluxGas`, and `thaumcraft/api/crafting/InfusionRecipe.java`.

## Ported in this stage

- Infusion instability now rolls on the TC4 `craftCycle` cadence instead of every progress/render tick. A triggered event consumes the current cycle, matching the original `if (valid) return;` path after an instability event.
- Invalid catalyst/structure paths now roll a final instability event before failing, closer to the original `(!valid) || instability roll` branch in `TileInfusionMatrix#craftCycle`.
- The existing 0..20 instability table was hardened and audited against TC4 cases:
  - ejected component cases: `0,2,10,13`, `6,17`, `1,11`, `19`, `7`, `4,15`;
  - zap/harm cases: `3,8,14`, `5,16`, `12`, `18`;
  - explosion case `9` and warp case `20`.
- Failure goo/gas now places real `FLUX_GOO` / `FLUX_GAS` blocks instead of taint/particle-only placeholders.
- Added 1.19.2 packet adapters for original visual payloads:
  - `PacketFXInfusionSource` for pedestal/entity-to-matrix source arcs;
  - `PacketFXBlockZap` for block/entity zap arcs.
- XP drain now sends the entity-source infusion packet path, matching the original enchantment XP drain visual branch.
- Added `TC4InfusionItemMatcher` for original-style item matching semantics:
  - item id equality;
  - damage/metadata equality where modern `ItemStack` exposes it;
  - TC4 wildcard damage sentinel `32767`;
  - strict NBT equality when recipe JSON supplies NBT.
- Infusion recipe JSON now accepts object-form catalyst/components with `damage`, `meta`, `damage_wildcard`, and `nbt` for future exact materialization of damage/NBT-sensitive TC4 recipes.
- Flux goo/gas behavior was narrowed toward the original finite spill behavior: movement damping/contact exhaustion, slower taint conversion, gas venting, and finite dissipation.

## Still intentionally deferred

- Full pre-flattening OreDictionary equivalence is not complete; the matcher is conservative and prepared for modern tag-backed data, but current materialized recipes mostly use concrete ids.
- Runic augmentation (`InfusionRunicAugmentRecipe` / `RS.HARDEN`) still needs a dedicated Stage210 pass.
- Exact original client particle textures/curves are approximated with 1.19.2 particles; the payload semantics and source/target arcs are now present.
- A full Gradle build still requires online dependency resolution for the Forge/Gradle wrapper in this sandbox.
