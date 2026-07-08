# Stage210 — TC4 Runic Augmentation parity on Minecraft/Forge 1.19.2

Stage210 continues from Stage209 and keeps the port target fixed at Minecraft/Forge **1.19.2** (`mappings official 1.19.2`, Forge `1.19.2-43.x`).  The stage ports the original Thaumcraft 4.2.3.5 `InfusionRunicAugmentRecipe` and its `EventHandlerRunic` helpers through 1.19.2-safe adapters.

## Original TC4 behaviour checked

Source compared against `Thaumcraft4-1.7.10-master.zip`:

- `thaumcraft/common/lib/crafting/InfusionRunicAugmentRecipe.java`
- `thaumcraft/common/lib/events/EventHandlerRunic.java`
- `thaumcraft/common/tiles/TileInfusionMatrix.java`

Important original formulas:

- recipe research: `RUNICAUGMENTATION`;
- catalyst must implement TC4 `IRunicArmor`;
- components: iron ingot + Salis Mundus + one extra Salis Mundus per `getFinalCharge(input)`;
- output: central item copy with byte tag `RS.HARDEN` incremented by one;
- aspects: `vis = 32 * 2^finalCharge`; `ARMOR = vis/2`, `MAGIC = vis/2`, `ENERGY = vis`;
- instability: `5 + getFinalCharge(input) / 2`;
- `getFinalCharge` equals base runic charge + `RS.HARDEN`.

## Ported 1.19.2 implementation

Added:

- `TC4RunicArmorHelper`
  - mirrors original `IRunicArmor` base charge semantics for de-metadata'd TC4 item ids;
  - preserves the original NBT key `RS.HARDEN`;
  - exposes `getHardening`, `getFinalCharge`, `addHardening`;
  - adds tooltip lines for runic charge and hardening level.
- `TC4InfusionRunicAugmentAdapter`
  - runtime materialization of the dynamic `RUNICAUGMENTATION` infusion recipe;
  - catalyst-dependent components, essentia cost and instability;
  - output application by incrementing `RS.HARDEN` on the central item.

Changed:

- `InfusionRecipe`
  - added `isRunicAugmentRecipe`;
  - added catalyst-aware `componentsFor`, `aspectCostFor`, `instabilityFor`;
  - added `runicAugmentRecipe` factory.
- `InfusionRecipes`
  - registers the runtime runic augment recipe next to enchantment runtime recipes.
- `InfusionProcessHelper`
  - component and aspect reporting now support catalyst-dependent recipes.
- `InfusionMatrixBlockEntity`
  - start cycle now snapshots dynamic runic components/aspects/instability;
  - failure roll bounds use the effective `recipeInstability`, not the static recipe field;
  - finish cycle applies the `RS.HARDEN` output path.
- `InfusionMatrixAuxiliaryHelper`
  - aspect-powered helper checks catalyst-dependent aspect costs.
- `TC4ResearchComponentItem`
  - shows runic shield/hardening tooltip for runic-capable TC4 items.

## 1.19.2 compatibility guard

Stage210 deliberately does not import or call original 1.7.10 runtime APIs such as:

- `net.minecraft.item.ItemStack`
- `net.minecraft.nbt.NBTTag*`
- `thaumcraft.api.IRunicArmor`
- `func_77983_a`, `field_77990_d`

The new audit script enforces this for all files touched by the stage.

## Validation

Passed:

- `scripts/java_syntax_guard.py`
- `scripts/github_static_audit.py`
- `scripts/github_ci_guard.py`
- `scripts/tc4_stage205_hard_parity_reset_audit.py`
- `scripts/tc4_stage206_original_parity_repair_audit.py`
- `scripts/tc4_stage207_infusion_matrix_parity_audit.py`
- `scripts/tc4_stage208_infusion_renderer_enchantment_audit.py`
- `scripts/tc4_stage209_extended_infusion_failure_audit.py`
- `scripts/tc4_stage210_runic_augment_1192_audit.py`

Gradle build was attempted but could not complete in the sandbox because the Gradle wrapper could not download `gradle-7.5.1-bin.zip` from `services.gradle.org`.

## Next likely parity work

Stage211 should continue with full runtime runic shielding consumption/recharge parity:

- player damage absorption using stored runic shield pool;
- recharge timers and `runicShieldCharge` / `runicShieldEffect` sounds;
- emergency/charged/regeneration ring/girdle behaviours;
- Baubles replacement semantics for 1.19.2 equipment/Curios-like environments;
- remaining TC4 ItemStack output metadata/NBT specializations for runic item variants.
