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
runtime = read('src/main/java/com/darkifov/thaumcraft/golem/GolemTaskAIRuntime.java')
entity = read('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java')
report = json.loads(read('STAGE197_GOLEM_TASK_AI_REPORT.json')) if exists('STAGE197_GOLEM_TASK_AI_REPORT.json') else {}
workflow = read('.github/workflows/main.yml')

checks = {
    'version_198': ("version = '2.04.0'" in build or "version = '1.98.0'" in build or "version = '2.00.0'" in build or "version = '2.02.0'" in build or "version = '2.02.0'" in build) and ('version="2.04.0"' in mods or 'version="1.98.0"' in mods or 'version="2.00.0"' or 'version="2.02.0"' in mods),
    'report_stage_197': report.get('stage') == 197 and report.get('version') in {'2.04.0', '1.98.0', '2.00.0'},
    'original_ai_class_names': all(token in runtime for token in [
        'AIHomeReplace', 'AIHomeTake', 'AIHomeDrop', 'AIItemPickup', 'AIFillGoto', 'AIFillTake',
        'AIEmptyGoto', 'AIEmptyPlace', 'AISortingPlace'
    ]),
    'original_core_task_mapping': all(token in runtime for token in [
        'GolemCoreType.FILL', 'OriginalTask.AIFillGoto', 'OriginalTask.AIFillTake',
        'GolemCoreType.EMPTY', 'OriginalTask.AIEmptyGoto', 'OriginalTask.AIEmptyPlace',
        'GolemCoreType.SORTING', 'OriginalTask.AISortingPlace', 'OriginalTask.AIHomeReplace'
    ]),
    'entity_has_original_carried_stack': all(token in entity for token in [
        'private ItemStack itemCarried = ItemStack.EMPTY',
        'GolemOriginalRuntime.NBT_ITEM_CARRIED',
        'itemCarried.save(carriedTag)',
        'ItemStack.of(tag.getCompound(GolemOriginalRuntime.NBT_ITEM_CARRIED))'
    ]),
    'entity_invokes_task_adapters': all(token in entity for token in [
        'runOriginalHomeTake', 'runOriginalHomeDrop', 'runOriginalHomeReplace', 'runOriginalItemPickup',
        'runOriginalFillGotoTake', 'runOriginalEmptyGotoPlace', 'runOriginalSortingPlace',
        'GolemTaskAIRuntime.originalDelayReady'
    ]),
    'original_home_facing_container_adapter': all(token in entity for token in [
        'Direction.from3DDataValue(homeFacing)',
        'homePos.subtract',
        'ORIGINAL_HOME_INTERACT_DISTANCE_SQ',
        'ORIGINAL_CHEST_INTERACT_TICKS'
    ]),
    'workflow_runs_stage197': 'python scripts/tc4_stage197_golem_task_ai_audit.py' in workflow,
}

errors = [name for name, ok in checks.items() if not ok]
if errors:
    for error in errors:
        print(f'::error::Stage197 golem task AI audit failed: {error}')
    sys.exit(1)
print('Stage197 golem task AI audit: OK')
