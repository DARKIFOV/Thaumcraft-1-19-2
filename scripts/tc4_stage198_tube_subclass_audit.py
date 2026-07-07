#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(path: str) -> str:
    return (ROOT / path).read_text(encoding='utf-8')

def exists(path: str) -> bool:
    return (ROOT / path).exists()

build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
subtype = read('src/main/java/com/darkifov/thaumcraft/essentia/EssentiaTubeSubtype.java')
tube_block = read('src/main/java/com/darkifov/thaumcraft/block/EssentiaTubeBlock.java')
valve_block = read('src/main/java/com/darkifov/thaumcraft/block/EssentiaValveBlock.java')
tube_be = read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java')
mod = read('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
report = json.loads(read('STAGE198_TUBE_SUBCLASS_REPORT.json')) if exists('STAGE198_TUBE_SUBCLASS_REPORT.json') else {}
workflow = read('.github/workflows/main.yml')

checks = {
    'version_198': ("version = '2.04.0'" in build or "version = '1.98.0'" in build or "version = '2.00.0'" in build or "version = '2.02.0'" in build or "version = '2.02.0'" in build) and ('version="2.04.0"' in mods or 'version="1.98.0"' in mods or 'version="2.00.0"' or 'version="2.02.0"' in mods),
    'report_stage_198': report.get('stage') == 198 and report.get('version') in {'2.04.0', '1.98.0', '2.00.0'},
    'subtypes_explicit_original_classes': all(token in subtype for token in [
        'TileTube', 'TileTubeFilter', 'TileTubeRestrict', 'TileTubeOneway', 'TileTubeBuffer', 'TileTubeValve',
        'FILTER', 'RESTRICT', 'ONEWAY', 'BUFFER', 'VALVE'
    ]),
    'registered_tube_variant_blocks': all(token in mod for token in [
        'ESSENTIA_TUBE_FILTER', 'ESSENTIA_TUBE_RESTRICT', 'ESSENTIA_TUBE_ONEWAY', 'ESSENTIA_TUBE_BUFFER',
        'EssentiaTubeSubtype.FILTER', 'EssentiaTubeSubtype.RESTRICT', 'EssentiaTubeSubtype.ONEWAY', 'EssentiaTubeSubtype.BUFFER'
    ]),
    'block_entity_type_includes_variants': all(token in mod for token in [
        'ESSENTIA_TUBE_FILTER.get()', 'ESSENTIA_TUBE_RESTRICT.get()', 'ESSENTIA_TUBE_ONEWAY.get()', 'ESSENTIA_TUBE_BUFFER.get()', 'ESSENTIA_VALVE.get()'
    ]),
    'tube_be_subclass_state_nbt': all(token in tube_be for token in [
        'private EssentiaTubeSubtype subtype', 'private Aspect aspectFilter', 'private byte[] chokedSides',
        'private final AspectList bufferAspects', 'private boolean allowFlow', 'tag.putString("tc4Subtype"',
        'tag.putString("AspectFilter"', 'tag.putByteArray("choke"', 'tag.put("buffer"', 'tag.putBoolean("flow"'
    ]),
    'tube_be_behavior_hooks': all(token in tube_be for token in [
        'subtype.directionalFlow()', 'subtype.restrictsSuction()', 'subtype.transformNeighbourSuction',
        'subtype.allowsAspect(aspectFilter', 'subtype.storesBufferEssentia()', 'fillBufferSnapshot()',
        'toggleSideWithNeighbour', 'cycleChoke', 'level.hasNeighborSignal(pos)'
    ]),
    'wand_toggle_adapter': 'player.getItemInHand(hand).getItem() instanceof WandItem' in tube_block and 'return super.use(state, level, pos, player, hand, hit)' in valve_block,
    'variant_resources_exist': all(exists(path) for path in [
        'src/main/resources/assets/thaumcraft/blockstates/essentia_tube_filter.json',
        'src/main/resources/assets/thaumcraft/blockstates/essentia_tube_restrict.json',
        'src/main/resources/assets/thaumcraft/blockstates/essentia_tube_oneway.json',
        'src/main/resources/assets/thaumcraft/blockstates/essentia_tube_buffer.json',
        'src/main/resources/data/thaumcraft/loot_tables/blocks/essentia_tube_filter.json',
        'src/main/resources/data/thaumcraft/loot_tables/blocks/essentia_tube_buffer.json'
    ]),
    'workflow_runs_stage198': 'python scripts/tc4_stage198_tube_subclass_audit.py' in workflow,
}

errors = [name for name, ok in checks.items() if not ok]
if errors:
    for error in errors:
        print(f'::error::Stage198 tube subclass audit failed: {error}')
    sys.exit(1)
print('Stage198 tube subclass audit: OK')
