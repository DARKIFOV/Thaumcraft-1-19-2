Continue the Thaumcraft 4 -> Forge/Minecraft 1.19.2 port from this archive:
`thaumcraft_legacy_rebuild_STAGE273_282_TC4_ELDRITCH_TILES_MINDSPIDER_BATCH_1192_PARITY.zip`.

Reference original source archive:
`Thaumcraft4-1.7.10-master.zip`.

Current stage: Mega Stage273-282, mod version `2.82.0`.

Important constraints:
- Keep the target on Forge/Minecraft 1.19.2.
- Do not reintroduce 1.7.10-only APIs such as `NBTTag*`, `func_*`, old `TileEntity` signatures or metadata-dependent runtime code.
- Preserve original TC4 names/tags as breadcrumbs where runtime equivalents are flattened.
- Continue making mega-stages of 5-10 internal stages per archive.
- Keep adding a new `docs/NEXT_CHAT_PROMPT_STAGE###.md` inside every archive.
- Keep running `java_syntax_guard.py`, `github_static_audit.py`, `github_ci_guard.py`, previous active mega-stage audits and a new audit for the current batch.

Already completed through Stage273-282:
- Infusion Matrix parity, enchantment recipes, instability/failure/FX.
- Runic shielding, fortress armor/masks, champion runtime/generation/FX.
- Eldritch Guardian/Warden/Golem/Crab, orbs, boss renderers, Outer Lands maze/rooms/loot/decor queues.
- Cleanup/quarantine of debug/addon/fallback garbage items and recipe JSON.
- Dedicated MindSpider entity and real spawner target for GenPassage feature 14.
- Runtime block-entity equivalents for Eldritch Cap/Lock/Trap/Crystal.
- Split blockEldritch metadata variants for crust/decorative/door/lock/spawner/trap.

Suggested next mega-stage Stage283-292:
1. Replace `TC4EldritchLockBossSpawner` taint/cultist placeholders with dedicated `CultistPortal` and `TaintacleGiant` equivalents.
2. Add real block-entity renderers for Eldritch Cap/Lock/Crystal/Nothing/Obelisk using the original TC4 renderer constants/textures.
3. Deepen `TileEldritchAltar`/key-room lock flow so the awakened key and boss-room exit metadata match TC4 more exactly.
4. Continue baked model parity for Guardian/Warden/Golem/Crab and add a dedicated MindSpider renderer instead of the temporary block-mob renderer.
5. Continue Outer Lands dimension/chunk integration and avoid generating rooms in normal overworld chunks except through the current compatibility adapter.
