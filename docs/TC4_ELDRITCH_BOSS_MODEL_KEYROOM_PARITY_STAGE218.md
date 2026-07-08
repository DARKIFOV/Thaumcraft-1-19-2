# Stage218 — TC4 Eldritch boss model / key-room parity for Forge 1.19.2

Base archive: `thaumcraft_legacy_rebuild_STAGE217_TC4_ELDRITCH_RENDER_ROOM_1192_PARITY.zip`.
Target runtime remains Minecraft/Forge `1.19.2`.
Original parity source: `Thaumcraft4-1.7.10-master.zip`.

## Original TC4 anchors checked

- `EntityThaumcraftBoss` implements boss-display behaviour and drops `ConfigItems.itemEldritchObject` meta `3` plus `itemLootbag` meta `2`.
- `RenderEldritchGuardian` / `RenderEldritchGolem` use translucent rendering, Warden scale `1.5F`, Golem scale `2.15F`, and spawn sinking from `spawnTimer / 150.0F`.
- `ModelEldritchGuardian` contains the large named ModelRenderer tree (`HoodEye`, `Cloak*`, `Shoulderplate*`, `Legpanel*`, etc.).
- `EntityEldritchGolem` uses headless spawn timer `100` and beam charge `150`.
- `GenKeyRoom` creates a `15x15x13` room, places a center eldritch block at `y+2`, spawns `EntityPermanentItem(new ItemStack(ConfigItems.itemEldritchObject, 1, 2))`, and spawns `2/3/4` Eldritch Guardians depending on Peaceful/Easy, Normal, or Hard difficulty. If the count reaches 4, the first guardian becomes champion.

## Port changes

- Added `TC4EldritchBossModelParity`, a 1.19.2-safe metadata bridge that records original model part names and shared render constants. Renderers now consume this bridge instead of hard-coded magic numbers.
- Added `ServerBossEvent` support to `TC4ThaumcraftBossEntity`, mirroring TC4 boss-display behaviour with 1.19.2 APIs.
- Added `TC4OuterLandsLootAdapter`:
  - maps `itemEldritchObject:2` to the current registered TC4 research item when present, falling back to `ELDRITCH_EYE`;
  - maps `itemEldritchObject:3` to the current registered TC4 research item when present, falling back to `PRIMORDIAL_PEARL`;
  - maps rare lootbag to the registered TC4 research lootbag when present, falling back to `ELDRITCH_RELIC`;
  - marks permanent key-room item entities with `TC4PermanentItem` and unlimited lifetime.
- Replaced the temporary Stage217 key-room chest placeholder with original-style permanent item + guardian spawning.
- Kept all new logic free of 1.7.10-only APIs (`func_*`, `DataWatcher`, `NBTTag*`, `ForgeDirection`).

## Validation

Run from repository root:

```bash
python scripts/java_syntax_guard.py
python scripts/github_static_audit.py
python scripts/github_ci_guard.py
python scripts/tc4_stage217_eldritch_render_room_audit.py
python scripts/tc4_stage218_boss_model_keyroom_audit.py
```

Full Gradle build still requires network access for the Gradle wrapper download in this sandbox.
