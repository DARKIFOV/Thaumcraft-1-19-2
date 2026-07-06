#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
checks = {
    'golem_upgrade_enum': ROOT / 'src/main/java/com/darkifov/thaumcraft/golem/GolemUpgradeType.java',
    'golem_bell_mode_enum': ROOT / 'src/main/java/com/darkifov/thaumcraft/golem/GolemBellMode.java',
    'golem_marker_mode_enum': ROOT / 'src/main/java/com/darkifov/thaumcraft/golem/GolemMarkerMode.java',
    'golem_filter_item': ROOT / 'src/main/java/com/darkifov/thaumcraft/block/GolemFilterItem.java',
    'golem_task_marker_item': ROOT / 'src/main/java/com/darkifov/thaumcraft/block/GolemTaskMarkerItem.java',
    'golem_upgrade_item': ROOT / 'src/main/java/com/darkifov/thaumcraft/block/GolemUpgradeItem.java',
    'golem_entity': ROOT / 'src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java',
    'golem_bell': ROOT / 'src/main/java/com/darkifov/thaumcraft/block/GolemBellItem.java',
}
required_tokens = {
    'src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java': [
        'BODYGUARD ->', 'BUTCHER ->', 'FISH ->',
        'GolemUpgradeType.AIR', 'GolemUpgradeType.EARTH', 'filterAllowList',
        'inputPos', 'outputPos', 'guardPos', 'workPos', 'setTaskMarker', 'loadGolemConfiguration'
    ],
    'src/main/java/com/darkifov/thaumcraft/block/GolemBellItem.java': [
        'case HOME ->', 'case MARKER ->', 'case RETASK ->', 'mode == GolemBellMode.WAIT'
    ],
    'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java': [
        'GOLEM_TASK_MARKER', 'GOLEM_FILTER', 'GOLEM_UPGRADE_AIR', 'GOLEM_UPGRADE_ENTROPY'
    ],
}
errors = []
for name, path in checks.items():
    if not path.exists():
        errors.append(f'missing {name}: {path.relative_to(ROOT)}')

for rel, tokens in required_tokens.items():
    text = (ROOT / rel).read_text(encoding='utf-8') if (ROOT / rel).exists() else ''
    for token in tokens:
        if token not in text:
            errors.append(f'{rel} missing token {token}')

for model in ['golem_task_marker', 'golem_filter', 'golem_upgrade_air', 'golem_upgrade_fire', 'golem_upgrade_water', 'golem_upgrade_earth', 'golem_upgrade_order', 'golem_upgrade_entropy']:
    if not (ROOT / f'src/main/resources/assets/thaumcraft/models/item/{model}.json').exists():
        errors.append(f'missing item model for {model}')

report = {
    'stage': 141,
    'name': 'TC4 Golemancy Controls / Filters / Upgrades Parity',
    'checked_files': {name: str(path.relative_to(ROOT)) for name, path in checks.items()},
    'new_items': ['golem_task_marker', 'golem_filter', 'golem_upgrade_air', 'golem_upgrade_fire', 'golem_upgrade_water', 'golem_upgrade_earth', 'golem_upgrade_order', 'golem_upgrade_entropy'],
    'new_core_modes': ['bodyguard', 'butcher', 'fish', 'liquid', 'essentia', 'patrol'],
    'errors': errors,
}
(ROOT / 'STAGE141_GOLEMANCY_CONTROLS_AUDIT.json').write_text(json.dumps(report, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')

if errors:
    for error in errors:
        print(f'::error::Stage141 audit failed: {error}')
    sys.exit(1)
print('Stage141 golemancy controls audit: OK')
