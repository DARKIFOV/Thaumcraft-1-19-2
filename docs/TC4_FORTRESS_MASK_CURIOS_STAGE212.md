# Stage212 — TC4 Fortress Armor, Mask NBT, and Optional Curios/Baubles Runtime

Target: Minecraft/Forge 1.19.2.
Original parity reference: Thaumcraft 4.2.3.5 / 1.7.10.

## Scope

Stage212 continues from Stage211 runic shield runtime and ports the fortress-helmet branches that were still represented only as research/recipe metadata.

## Original TC4 source anchors

- `thaumcraft.common.items.armor.ItemFortressArmor`
- `thaumcraft.common.lib.events.EventHandlerRunic#entityHurt`
- `thaumcraft.common.lib.WarpEvents`
- `ConfigRecipes` infusion NBT outputs:
  - `new Object[] { "goggles", new NBTTagByte(1) }`
  - `new Object[] { "mask", new NBTTagInt(0) }`
  - `new Object[] { "mask", new NBTTagInt(1) }`
  - `new Object[] { "mask", new NBTTagInt(2) }`

## Implemented 1.19.2 adapters

- `TC4FortressArmorMaterial` — diamond-like fortress armor material.
- `TC4FortressArmorItem` — wearable fortress armor with tooltip bridge for `goggles`, `mask`, and `RS.HARDEN`.
- `TC4FortressMaskItem` — research/sprite mirror items; actual behavior is still on the helmet NBT, as in TC4.
- `TC4FortressMaskRuntime` — runtime mask effects:
  - Grinning Devil reduces warp event severity by `2 + rand(4)`.
  - Angry Ghost applies Wither for 80 ticks to attackers with chance `damage / 10`.
  - Sipping Fiend heals the wearer by 1 with chance `damage / 12` when attacking.
  - champion/eldritch shield FX branch sends `PacketFXShield` adapter.
- `TC4BaubleSlotAdapter` — optional reflective Curios/Baubles scan before Stage211 inventory/offhand fallback.

## Infusion NBT output

`InfusionRecipe` now supports generic 1.19.2 `Tag` output, not only `CompoundTag`, so byte/int TC4 outputs can be applied with `ItemStack#addTagElement`:

- `tc4_helm_goggles.json` => `goggles: 1b`
- `tc4_mask_grinning_devil.json` => `mask: 0`
- `tc4_mask_angry_ghost.json` => `mask: 1`
- `tc4_mask_sipping_fiend.json` => `mask: 2`

All four recipes use fortress helm catalyst with wildcard damage, mirroring `new ItemStack(ConfigItems.itemHelmetFortress, 1, 32767)`.

## Notes

The champion branch is bridged through persistent data tag `TC4ChampionMod`; value `5` maps to the original shielded champion modifier branch. This avoids adding hard dependencies or 1.7.10 attributes while preserving a stable runtime hook.
