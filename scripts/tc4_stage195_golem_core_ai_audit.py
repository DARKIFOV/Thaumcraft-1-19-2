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
material = read('src/main/java/com/darkifov/thaumcraft/golem/GolemMaterial.java')
core = read('src/main/java/com/darkifov/thaumcraft/golem/GolemCoreType.java')
upgrade = read('src/main/java/com/darkifov/thaumcraft/golem/GolemUpgradeType.java')
runtime = read('src/main/java/com/darkifov/thaumcraft/golem/GolemOriginalRuntime.java')
entity = read('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java')
report = json.loads(read('STAGE195_GOLEM_CORE_AI_REPORT.json')) if exists('STAGE195_GOLEM_CORE_AI_REPORT.json') else {}

checks = {
    'version_196': ("version = '2.04.0'" in build or "version = '1.98.0'" in build or "version = '2.00.0'" in build or "version = '2.02.0'" in build or "version = '2.02.0'" in build) and ('version="2.04.0"' in mods or 'version="1.98.0"' in mods or 'version="2.00.0"' or 'version="2.02.0"' in mods),
    'report_stage_195': report.get('stage') == 195 and report.get('version') in {'2.04.0', '1.96.0', '1.98.0', '2.00.0', '2.02.0'},
    'enum_golem_type_constants': all(token in material for token in [
        'STRAW("straw", 10, 0, 0.38F, false, 1, 1, 75, 0',
        'WOOD("wood", 20, 6, 0.35F, false, 1, 4, 75, 1',
        'THAUMIUM("thaumium", 40, 15, 0.32F, true, 2, 32, 100, 4',
        'carryLimit(int earthUpgradeAmount)',
        'upgradeSlots(boolean advanced)'
    ]),
    'core_metadata_order': all(token in core for token in [
        'FILL("fill", 0, true, true, true',
        'EMPTY("empty", 1, true, true, true',
        'GATHER("gather", 2, true, true, true',
        'ESSENTIA("essentia", 6, false, false, false',
        'FISH("fish", 11, false, false, false',
        'originalValues()'
    ]),
    'non_original_core_adapters_marked': 'not an original TC4 itemGolemCore metadata' in core and 'BODYGUARD' in core and 'PATROL' in core,
    'upgrade_metadata_order': all(token in upgrade for token in [
        'AIR("air", 0', 'EARTH("earth", 1', 'FIRE("fire", 2', 'WATER("water", 3', 'ORDER("order", 4', 'ENTROPY("entropy", 5', 'byOriginalId'
    ]),
    'original_runtime_nbt': all(token in runtime for token in [
        'NBT_HOME_FACING = "HomeFacing"', 'NBT_GOLEM_TYPE = "GolemType"', 'NBT_CORE = "Core"', 'NBT_DECORATION = "Decoration"', 'NBT_ADVANCED = "advanced"', 'NBT_COLORS = "colors"', 'NBT_UPGRADES = "upgrades"', 'NBT_MARKERS = "Markers"'
    ]),
    'entity_uses_original_runtime': all(token in entity for token in [
        'GolemOriginalRuntime.inventorySlotCount', 'GolemOriginalRuntime.movementSpeed', 'GolemOriginalRuntime.attackDamage', 'GolemOriginalRuntime.slotsFromUpgrades', 'GolemOriginalRuntime.NBT_GOLEM_TYPE', 'GolemOriginalRuntime.NBT_CORE', 'fireImmune()'
    ]),
    'prompt_updated': exists('docs/NEXT_CHAT_PROMPT_STAGE196.md') and 'Stage197 + Stage198' in read('docs/NEXT_CHAT_PROMPT_STAGE196.md'),
}

errors = [name for name, ok in checks.items() if not ok]
if errors:
    for error in errors:
        print(f'::error::Stage195 golem core/AI audit failed: {error}')
    sys.exit(1)
print('Stage195 golem core/AI audit: OK')
