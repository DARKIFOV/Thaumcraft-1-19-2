# Stage115 — TC4 ConfigAspects runtime port

This stage moves from source transfer to actual runtime use of original Thaumcraft 4 aspect data.

## Source of truth

- Original file: `docs/source_refs/tc4_1710_original_source/thaumcraft/common/config/ConfigAspects.java`
- We do **not** compile old 1.7.10 FML/MCP classes directly.
- Instead, registrations are extracted and normalized for Forge 1.19.2.

## Extracted from TC4

- Object tag registrations: 269
- Complex object tag registrations: 57
- Entity tag registrations: 69
- Legacy OreDictionary string keys: 40
- Runtime exact modern object/block/item entries: 190
- Runtime exact modern entity entries: 46

## Runtime files added

- `src/main/java/com/darkifov/thaumcraft/source/TC4ObjectAspectRegistry.java`
- `src/main/java/com/darkifov/thaumcraft/source/TC4EntityAspectRegistry.java`

`AspectDatabase` now checks `TC4ObjectAspectRegistry` before the older name-based fallback. This means the thaumometer, crucible, alchemical furnace and research table now receive original TC4 values when an exact or normalized mapping exists.

## Data files added

- `src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_config_aspects_object_tags_raw.json`
- `src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_config_aspects_entity_tags_raw.json`
- `src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_legacy_oredict_aliases_1192.json`
- `src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_modern_object_aspects_1192_runtime.json`
- `src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_modern_entity_aspects_1192_runtime.json`
- `docs/porting/tc4_config_aspects_object_tags_stage115.csv`
- `docs/porting/tc4_config_aspects_entity_tags_stage115.csv`

## Important limitation

The old source uses many 1.7.10 `Blocks.field_...`, `Items.field_...`, `ConfigItems.itemResource` metadata and `ConfigBlocks` metadata targets. Stage115 preserves **all raw registrations** and ports the clear mappings into runtime. Remaining metadata-heavy targets are now indexed and ready for Stage116/Stage117 registry remapping instead of being guessed.
