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
suction = read('src/main/java/com/darkifov/thaumcraft/essentia/EssentiaSuction.java')
resolver = read('src/main/java/com/darkifov/thaumcraft/essentia/EssentiaSuctionResolver.java')
tube = read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java')
jar = read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaJarBlockEntity.java')
report = json.loads(read('STAGE196_ESSENTIA_SUCTION_REPORT.json')) if exists('STAGE196_ESSENTIA_SUCTION_REPORT.json') else {}
workflow = read('.github/workflows/main.yml')

checks = {
    'version_196': ("version = '2.04.0'" in build or "version = '1.98.0'" in build or "version = '2.00.0'" in build or "version = '2.02.0'" in build or "version = '2.02.0'" in build) and ('version="2.04.0"' in mods or 'version="1.98.0"' in mods or 'version="2.00.0"' or 'version="2.02.0"' in mods),
    'report_stage_196': report.get('stage') == 196 and report.get('version') in {'2.04.0', '1.96.0', '1.98.0', '2.00.0', '2.02.0'},
    'jar_suction_values': all(token in suction for token in [
        'JAR_NORMAL = 32', 'JAR_FILTERED = 64', 'JAR_VOID = 32', 'JAR_VOID_FILTERED = 48'
    ]),
    'resolver_uses_original_values': all(token in resolver for token in [
        'TileJarFillable', 'TileJarFillableVoid', 'jar.originalSuctionAmount(voidJar)', 'tube.isSideOpen(direction)'
    ]),
    'tube_original_state_nbt': all(token in tube for token in [
        'private Direction facing = Direction.NORTH', 'private boolean[] openSides', 'private Aspect essentiaType', 'private int essentiaAmount', 'private Aspect suctionType', 'private int suction', 'private int venting', 'tag.putString("type"', 'tag.putInt("amount"', 'tag.putInt("side"', 'tag.putByteArray("open"', 'tag.putString("stype"', 'tag.putInt("samount"'
    ]),
    'tube_tick_cadence_and_venting': all(token in tube for token in [
        'calculateSuctionSnapshot()', 'checkVentingSnapshot()', 'tube.originalTickCounter % 2 == 0', 'tube.originalTickCounter % Math.max(5, ThaumcraftConfig.ESSENTIA_TUBE_TRANSFER_INTERVAL_TICKS.get())', 'venting = 40'
    ]),
    'jar_original_nbt': all(token in jar for token in [
        'tag.putString("Aspect"', 'tag.putString("AspectFilter"', 'tag.putShort("Amount"', 'tag.putByte("facing"', 'Aspect.byId(tag.getString("Aspect"))', 'Aspect.byId(tag.getString("AspectFilter"))'
    ]),
    'workflow_runs_stage196': 'python scripts/tc4_stage196_essentia_suction_audit.py' in workflow and 'python scripts/tc4_stage195_golem_core_ai_audit.py' in workflow,
}

errors = [name for name, ok in checks.items() if not ok]
if errors:
    for error in errors:
        print(f'::error::Stage196 essentia suction audit failed: {error}')
    sys.exit(1)
print('Stage196 essentia suction audit: OK')
