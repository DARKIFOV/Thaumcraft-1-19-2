# TC4 Super-Mega Stage283-302 — Eldritch Boss / Tile Renderer Parity Batch

Base archive: `STAGE273_282_TC4_ELDRITCH_TILES_MINDSPIDER_BATCH_1192_PARITY`.
Target runtime: Minecraft Forge 1.19.2.
Reference source: `Thaumcraft4-1.7.10-master.zip`.

## Scope

This super-mega batch covers the next Outer Lands / Eldritch lock branch without importing old 1.7.10 APIs:

- `EntityCultistPortal` parity adapter.
- `EntityTaintacleGiant` parity adapter.
- Real lock-cycle boss spawning for cultist/taint variants instead of Warden/Praetor placeholders.
- Dedicated render registration for Cultist Portal, Giant Taintacle and Mind Spider.
- Active block-entity renderer bridge for `TileEldritchCap`, `TileEldritchLock`, `TileEldritchTrap`, `TileEldritchCrystal` equivalents.
- Non-`TC4ThaumcraftBossEntity` boss loot overload, so portal/taint bosses can share the existing Outer Lands boss loot adapter.

## Original anchors preserved

### EntityCultistPortal

Preserved names/semantics:

- NBT `stage`.
- NBT `stagecounter`.
- initial `stagecounter = 200`.
- stage 0 banner pulse at counter 160.
- loot crate pulses between counters 20 and 150 every 13 ticks.
- staged minion spawns for stages 0..11.
- boss/leader spawn at stage 12.
- self-damage after stage 12.
- contact zap damage.
- no movement / no gravity behavior.
- boss bar equivalent.

### EntityTaintacleGiant

Preserved names/semantics:

- NBT `Anger`.
- max health 125.
- attack damage 9.
- XP 20.
- champion-on-spawn behavior.
- 30 tick passive regen.
- damage cap at 35 with 200 tick anger/enrage branch.
- damage resistance / strength / speed enrage effects.
- no far despawn.
- no drowning.
- eldritch object boss drop branch.

## 1.19.2 adaptation notes

- Old `TileBanner` calls are mapped to existing `eldritch_decorative` anchors with particle/sound pulses.
- Old `blockLootCrate` metadata is mapped to `outer_lands_loot_crate[variant=0..2]`.
- Old `EntityCultistKnight/Cleric/Leader` are mapped to existing `CrimsonCultistEntity` role registrations.
- Old `EntityTaintacle` base class is represented by a stationary `Monster` adapter with manual target acquisition and taint-fibre anchoring.
- Old GL/OBJ tile renderers are represented by an active blockstate renderer bridge with bob/rotation hooks; full OBJ parity remains for a later client polish batch.

## Still remaining after this batch

- Full TC4 `EntityTaintacle` and `EntityTaintacleSmall` split entities.
- Full `ModelTaintacle` baked model tree.
- Exact `RenderCultistPortal` shader/quad timing.
- Exact `TileEldritchCapRenderer`, `TileEldritchLockRenderer`, `TileEldritchCrystalRenderer`, `TileEldritchNothingRenderer`, `TileEldritchObeliskRenderer` geometry.
- Outer Lands chunk-provider/dimension registration instead of portal-area ticking.
- More exact cultist equipment/AI/armor renderer parity.
