# Stage263-272 — TC4 Passage/Library/Nest + Garbage Recipe Cleanup Parity

Base: `thaumcraft_legacy_rebuild_STAGE253_262_TC4_FULL_STAGE_AUDIT_CLEANUP_BATCH_1192_PARITY.zip`  
Target: Minecraft/Forge 1.19.2  
Reference: original Thaumcraft 4 for Minecraft 1.7.10.

## What changed

This mega-stage continues the Outer Lands parity line without adding new mechanics outside TC4.

### GenPassage feature tail

Added `TC4OuterLandsPassageFeatureAdapter`, a 1.19.2-safe bridge for the original `GenPassage.generateRoom` feature-specific endings:

- feature `11`: trapped passage, now reinforces exposed shell with `eldritch_trap` equivalents;
- feature `12`: fleshy passage pocket using the same organic code path as TC4 code `21`;
- feature `13`: taint pocket using `taint_fibres` with TC4-style age `0/1` spread anchors;
- feature `14`: vishroom/MindSpider room equivalent, using mushroom placement plus a spawner tagged with `TC4Original=Thaumcraft.MindSpider` until a dedicated MindSpider entity is ported.

`TC4OuterLandsRoomAdapter.generatePassage` now calls this adapter after the structural pass and doorway generation.

### GenLibraryRoom parity

`TC4OuterLandsGenCommonAdapter.generateLibraryRoom` now includes the original high-impact library column anchors:

- four lower `blockEldritch:5` pedestal columns;
- four upper inverted slab/column anchors;
- central lower and upper pedestal stacks;
- `Smooth Stone Slab` top/bottom placement equivalents for the original `blockSlabStone` metadata use.

### GenNestRoom parity

Nest-room generation now includes:

- random hanging `eldritch_crystal` anchors where the original placed `blockCrystal:7` with `ForgeDirection.DOWN`;
- randomized side protrusions around the central organic mass;
- additional upper-center organic branch parity.

### Garbage cleanup

Stage253-262 hid garbage/addon/debug items from creative and loot. Stage263-272 physically removes quarantined garbage recipe JSON files so these non-core items are no longer craftable through datapack recipes while keeping registry ids/resources for save compatibility.

The exact removed file manifest is stored in:

- `docs/TC4_STAGE263_272_REMOVED_GARBAGE_RECIPES.json`

## Checks

Run:

```bash
python3 scripts/java_syntax_guard.py
python3 scripts/github_static_audit.py
python3 scripts/github_ci_guard.py
python3 scripts/tc4_stage263_272_mega_passage_cleanup_audit.py
```

Expected result: all pass, except full Gradle compile may still fail in the sandbox if Gradle must be downloaded from the internet.
