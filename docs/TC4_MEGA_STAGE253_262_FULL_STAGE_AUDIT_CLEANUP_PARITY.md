# Stage253-262 — Full stage audit + garbage cleanup + Eldritch block split

Target: Minecraft/Forge 1.19.2. Original source: `Thaumcraft4-1.7.10-master.zip`.

## What this mega-stage does

- Keeps the project on the core TC4 line instead of drifting into addons/debug items.
- Adds `TC4RegistryGarbageGuard` and hides accumulated non-core/debug/addon placeholders from the Thaumcraft creative tab.
- Prevents Outer Lands lootbag generation from returning quarantined placeholder/addon stacks.
- Adds flattened 1.19.2 block equivalents for old TC4 tile-backed variants:
  - `eldritch_cap` for `TileEldritchCap` / `blockEldritch` meta 3;
  - `eldritch_lock` for `TileEldritchLock` / meta 8;
  - `eldritch_trap` for `TileEldritchTrap` / meta 10;
  - `eldritch_crystal` for `TileEldritchCrystal` / `BlockCrystal` meta 7 renderer anchor.
- Updates `TC4EldritchBlockVariantAdapter` so Outer Lands generation uses these split blocks instead of routing those metadata values back to generic `eldritch_stone`.

## Full-stage audit result

Current active guards pass:

- `java_syntax_guard.py`
- `github_static_audit.py`
- `github_ci_guard.py`
- Stage205-222 parity audits checked individually or in grouped runs
- Stage223-232 mega audit
- Stage233-242 mega audit
- Stage243-252 mega audit
- Stage253-262 cleanup audit

Some older micro-audits are intentionally brittle and check exact string anchors from their original stage. Where newer stages replaced those paths with baked model adapters, Stage253-262 treats the old script as superseded and checks the current behavior instead.

## Garbage cleanup

Quarantined registry ids detected: **216**.

These are not physically removed from the registry yet because worlds/data packs may reference them. Instead they are removed from the player-facing creative tab and from generated loot until an addon-specific port explicitly re-enables them.

## Remaining to 100%

After this batch, core TC4 is still not 100%. Honest estimate:

- playable core parity: about 20-35 mega-stage units of 5-10 internal stages if each batch remains controlled;
- strict 100% core parity: about 35-60 mega-stage units, mostly because research, aura/nodes, golemancy, worldgen and render polish still need regression passes;
- addons should remain separate until core TC4 stops drifting.
