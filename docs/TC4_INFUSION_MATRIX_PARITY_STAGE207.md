# Stage207 TC4 Infusion Matrix Parity Start

Stage207 starts the strict original Thaumcraft 4 Infusion Matrix parity pass against the 1.7.10 sources:

- `thaumcraft/common/tiles/TileInfusionMatrix.java`
- `thaumcraft/common/blocks/BlockStoneDevice.java`
- `thaumcraft/client/renderers/tile/TileRunicMatrixRenderer.java`
- `thaumcraft/common/config/ConfigRecipes.java`

## Ported in this stage

### Original two-phase wand state

TC4's runic matrix is not supposed to immediately craft on the first wand click. The original `onWandRightClick` flow is:

1. if inactive and `validLocation()` passes, set `active = true`;
2. if active and not crafting, call `craftingStart(player)`;
3. while crafting, `craftCycle()` runs over time.

The Forge 1.19.2 adapter now mirrors that with separate `active` and `crafting` state. Empty-hand inspection still shows structure/status, but wand interaction now goes through `InfusionMatrixBlockEntity.onWandRightClick` rather than directly starting the recipe.

### Original validLocation gate

The activation path now checks the TC4 multiblock shape before any recipe work:

- center pedestal exactly at `matrix.y - 2`;
- four infusion pillars at diagonal offsets `(±1, ±1)` on the same Y as the center pedestal.

The stricter `strictTc4Location` gate is used for activation and ongoing invalidation. Recipe start still additionally requires component pedestals, catalyst, research, components and essentia.

### Original symmetry/instability basis

The structure scanner now exposes `originalSymmetryPenalty`, derived from the same TC4 idea used by `getSurroundings`: unpaired pedestals/items and stabilizers increase the penalty, mirrored counterparts cancel it. Crafting starts from `symmetry + recipeInstability` rather than the earlier simplified one-shot instability calculation.

### CraftCycle pacing and source effects

The runtime constants now distinguish:

- `ESSENTIA_DRAIN_RANGE = 12`, matching `EssentiaHandler.drainEssentia(..., 12)`;
- `CRAFT_CYCLE_DELAY = 10`, matching TC4's normal `countDelay`;
- `ITEM_PULL_DELAY = 5`, matching the original `itemCount` delay before a component is removed from its pedestal.

Components now enter a `TravellingComponent` delay state: source particles are spawned first, then the pedestal component is consumed after the TC4 five-tick travel delay.

### Original NBT aliases

The 1.19.2 block entity continues to write modern safe fields, but now also writes original TC4 key aliases for parity/debugging:

- `active`
- `crafting`
- `instability`
- `recipeinst`
- `recipetype`
- `recipeplayer`

Loading falls back to those aliases when the modern keys are not present.

### Original assets kept available

The original TC4 matrix model texture `textures/models/infuser.png` is now available in active resources at:

`assets/thaumcraft/textures/models/infuser.png`

A full 1.19.2 block-entity renderer that recreates `TileRunicMatrixRenderer` cube splitting, startup rotation and halo rendering remains a next-stage task.

## Not done yet

Stage207 is the start of full matrix parity, not the end. Remaining work:

- exact `TileRunicMatrixRenderer` visual model and halo rendering;
- exact recipe output variants for NBT/enchantment outputs, not only simple ItemStack outputs;
- exact infusion enchantment XP drain loop;
- exact flux goo/gas block effects once original flux blocks are fully available;
- exact pedestal/component ItemStack NBT/damage matching instead of item-id-only matching where recipes require it;
- client source FX packets equivalent to `PacketFXInfusionSource` / `PacketFXBlockZap`.
