# Stage208 TC4 Infusion Renderer + Enchantment Output Parity

Stage208 continues the strict original Thaumcraft 4 Infusion Matrix port from Stage207. The source of truth was the 1.7.10 code, especially:

- `thaumcraft/client/renderers/tile/TileRunicMatrixRenderer.java`
- `thaumcraft/client/renderers/models/ModelCube.java`
- `thaumcraft/common/tiles/TileInfusionMatrix.java`
- `thaumcraft/api/crafting/InfusionEnchantmentRecipe.java`
- `thaumcraft/common/lib/crafting/InfusionRunicAugmentRecipe.java`

## Renderer parity added

The Infusion Matrix no longer depends on a fake/static `cube_all` block model. `InfusionMatrixBlock` now returns `RenderShape.ENTITYBLOCK_ANIMATED`, and the client registers `InfusionMatrixRenderer` for the matrix block entity.

The renderer uses the original active resource path:

`assets/thaumcraft/textures/models/infuser.png`

It recreates the key original visual semantics:

- eight cubelets arranged around the center;
- TC4-style `0.45` cubelet scale and `±0.25` offsets;
- startup rotation around yaw/pitch/roll;
- active instability wobble based on current instability/craft count;
- translucent purple overlay pass while active;
- crafting halo/ray adapter while `crafting == true`.

This is still a modern Forge renderer, not a line-for-line GL11 copy. Exact blend-state and UV parity can be tightened later, but the visible behavior is now driven by the original model texture and the block entity state rather than a placeholder block model.

## Enchantment recipe parity added

Original TC4 treats infusion enchantments as `recipeType == 1`, not as normal item-output recipes. Stage208 adds a runtime adapter for that path.

`InfusionRecipe` now carries:

- `recipeType`;
- `enchantmentId`;
- `outputNbtLabel` / `outputNbt` for NBTBase-style outputs;
- catalyst wildcard semantics for central enchanted items.

`TC4InfusionEnchantmentAdapter` materializes the carried `TC4InfusionEnchantmentIndex` into runtime recipes and implements the important original behaviors from `InfusionEnchantmentRecipe`:

- central item must accept the enchantment;
- current level must be below max;
- existing enchantments must be compatible;
- instability is `sum(existing enchant levels) / 2 + recipe instability`;
- XP is `recipeXP * (1 + current target enchantment level)`;
- essentia is scaled by `current target enchantment level + 0.1 * other enchantment levels`;
- finish increments the existing enchantment level on the central item.

## XP drain cycle

`InfusionMatrixBlockEntity` now saves and ticks `recipeXP`. During `craftCycle`, enchantment recipes drain one XP level at a time before essentia is drained, matching the original order.

The adapter searches players within radius 10. Non-creative players lose one level and take 0..1 magic damage; creative players satisfy the drain without losing XP. A source beam is emitted from the player to the matrix and the cycle waits 20 ticks before continuing.

The TC4 NBT aliases are also preserved:

- `recipetype`
- `recipexp`

## Output parity added

`craftingFinish` now has separate branches for:

1. normal `ItemStack` result;
2. enchantment output (`recipeType == 1`) by applying/incrementing the enchantment on the central item;
3. labelled NBTBase-style output by attaching a copied tag to the central item with `addTagElement`.

This prepares the matrix for exact original output variants beyond simple item replacement.

## Component matching improvement

Component checks and delayed component pulling now call `recipe.componentMatches(...)` instead of raw item-id-only removal. The actual consumed pedestal stack also preserves crafting container items through `getCraftingRemainingItem()`.

This is closer to original `areItemStacksEqual(..., true)` behavior, but it is not the final state. Full damage/OreDictionary/NBT equivalence for every original component variant is still a Stage209+ task.

## Still not complete

Stage208 does not claim final Infusion Matrix parity. Remaining major gaps:

- exact `PacketFXInfusionSource` / `PacketFXBlockZap` equivalents;
- exact weighted instability/failure event table from `craftCycle` cases 0..20;
- flux goo/gas and block zap effects;
- runic augment dynamic NBT/component parity;
- full original ItemStack damage/OreDictionary/NBT component matching;
- registration of custom Thaumcraft enchantments such as Repair and Haste.
