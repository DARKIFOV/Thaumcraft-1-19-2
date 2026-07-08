# Stage211 — TC4 Runic Shield Runtime Parity (Forge 1.19.2)

This stage continues from Stage210 and ports the runtime side of TC4 runic shielding from `thaumcraft.common.lib.events.EventHandlerRunic` into a 1.19.2-safe implementation.

## Original TC4 sources checked

- `thaumcraft/common/lib/events/EventHandlerRunic.java`
- `thaumcraft/common/lib/network/playerdata/PacketRunicCharge.java`
- `thaumcraft/common/lib/network/fx/PacketFXShield.java`
- `thaumcraft/api/IRunicArmor.java`
- original sounds:
  - `runicShieldCharge.ogg`
  - `runicShieldEffect.ogg`

## Ported behavior

- Runtime charge map equivalent to TC4 `runicCharge`.
- Runtime max/upgrades map equivalent to TC4 `runicInfo`.
- Recharge cadence equivalent to TC4 `nextCycle` / `lastCharge`.
- Shield settings exposed with TC4 defaults:
  - `runicRechargeMs = 2000`
  - `runicRechargeDelayTicks = 80`
  - `runicCost = 50`
- Recharge consumes Aer + Terra vis from inventory wands.
- Damage is absorbed by current runic charge before normal health damage.
- Depleted shields trigger TC4-style upgrade effects:
  - charged ring recharge acceleration: `-500ms` per charged ring, min `500ms`;
  - kinetic girdle explosion on break, 20s cooldown;
  - regeneration ring effect, 20s cooldown;
  - emergency amulet restore: `8 * emergency count`, 60s cooldown.
- `PacketRunicCharge` sends entity id, current charge, and max charge to the client.
- `PacketFXShield` sends source/target ids and supports TC4 target sentinels `-1`, `-2`, `-3`.
- Client bridge spawns shield-rune particle rings around the protected entity.
- Small HUD mirror displays current runic charge.

## 1.19.2 adapter decisions

TC4 used Baubles slots. This port does not hard-depend on Baubles/Curios, so Stage211 uses the same strategy as the focus-pouch adapter:

- armor slots are scanned directly;
- offhand runic bauble mirrors are scanned first;
- then the first four runic bauble mirror items in main inventory are treated as Baubles slots `0..3`.

The implementation does not import or depend on TC4 1.7.10 APIs such as `IRunicArmor`, `NBTTag*`, `func_*`, or `field_*`.

## Files added/changed

- Added `src/main/java/com/darkifov/thaumcraft/runic/TC4RunicShieldRuntime.java`
- Added `src/main/java/com/darkifov/thaumcraft/network/PacketRunicCharge.java`
- Added `src/main/java/com/darkifov/thaumcraft/network/PacketFXShield.java`
- Added `src/main/java/com/darkifov/thaumcraft/client/RunicShieldClientState.java`
- Added `src/main/java/com/darkifov/thaumcraft/client/RunicShieldOverlayEvents.java`
- Added `src/main/java/com/darkifov/thaumcraft/client/fx/TC4ClientRunicShieldFx.java`
- Updated `TC4RunicArmorHelper` with bauble/variant discriminators
- Updated `ThaumcraftNetwork` with runic packet registration and helpers
- Updated `CommonEvents` with runic tick and hurt hooks
- Updated `ThaumcraftConfig` with TC4 runic shielding config keys
- Added `scripts/tc4_stage211_runic_shield_runtime_audit.py`

## Checks

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
- `scripts/tc4_stage211_runic_shield_runtime_audit.py`

Gradle build could not be executed in the sandbox because the Gradle wrapper attempts to download `gradle-7.5.1-bin.zip` from `services.gradle.org`, and network access is unavailable.

## Recommended Stage212 target

Continue with runic/armor parity:

- Fortress armor mask behavior from `EventHandlerRunic#entityHurt`;
- champion/eldritch shield FX branch for mobs;
- warped gear tooltip/runtime parity;
- Curios optional adapter for true 1.19.2 bauble slots if dependency-free reflection is acceptable;
- stricter mapping for original damage source sentinels used by `PacketFXShield`.
