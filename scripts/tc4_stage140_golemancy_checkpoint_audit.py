#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
java = ROOT / 'src/main/java'
checks = {
    'golem_material_enum': (java / 'com/darkifov/thaumcraft/golem/GolemMaterial.java').exists(),
    'golem_core_enum': (java / 'com/darkifov/thaumcraft/golem/GolemCoreType.java').exists(),
    'golem_entity_material_persistence': 'GolemMaterial' in (java / 'com/darkifov/thaumcraft/entity/ThaumGolemEntity.java').read_text(encoding='utf-8'),
    'golem_entity_core_persistence': 'GolemCoreType' in (java / 'com/darkifov/thaumcraft/entity/ThaumGolemEntity.java').read_text(encoding='utf-8'),
    'golem_guard_behavior': 'guardOwnerArea' in (java / 'com/darkifov/thaumcraft/entity/ThaumGolemEntity.java').read_text(encoding='utf-8'),
    'golem_harvest_behavior': 'harvestNearbyCrops' in (java / 'com/darkifov/thaumcraft/entity/ThaumGolemEntity.java').read_text(encoding='utf-8'),
    'golem_lumber_behavior': 'lumberNearbyLogs' in (java / 'com/darkifov/thaumcraft/entity/ThaumGolemEntity.java').read_text(encoding='utf-8'),
    'golem_container_dropoff': 'deliverInventoryToNearbyContainer' in (java / 'com/darkifov/thaumcraft/entity/ThaumGolemEntity.java').read_text(encoding='utf-8'),
    'golem_core_nbt_selector': 'TC4GolemMaterial' in (java / 'com/darkifov/thaumcraft/block/GolemCoreItem.java').read_text(encoding='utf-8'),
    'golem_renderer_material_visuals': 'materialState' in (java / 'com/darkifov/thaumcraft/client/render/ThaumGolemRenderer.java').read_text(encoding='utf-8'),
}
report = {
    'stage': 140,
    'name': 'TC4 golemancy whole-port checkpoint audit',
    'checks': checks,
    'passed': all(checks.values()),
}
(ROOT / 'STAGE140_GOLEMANCY_CHECKPOINT_AUDIT.json').write_text(json.dumps(report, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')
if not report['passed']:
    for name, ok in checks.items():
        if not ok:
            print(f'::error::Stage140 audit failed: {name}')
    sys.exit(1)
print('Stage140 golemancy checkpoint audit: OK')
