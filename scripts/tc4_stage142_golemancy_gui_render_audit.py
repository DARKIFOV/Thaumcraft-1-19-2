#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
checks = {
    'golem_entity': ROOT / 'src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java',
    'golem_renderer': ROOT / 'src/main/java/com/darkifov/thaumcraft/client/render/ThaumGolemRenderer.java',
    'golem_bell': ROOT / 'src/main/java/com/darkifov/thaumcraft/block/GolemBellItem.java',
    'golem_marker': ROOT / 'src/main/java/com/darkifov/thaumcraft/block/GolemTaskMarkerItem.java',
    'golem_bell_mode': ROOT / 'src/main/java/com/darkifov/thaumcraft/golem/GolemBellMode.java',
}
required_tokens = {
    'src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java': [
        'public InteractionResult mobInteract',
        'statusSummary()',
        'GolemTaskMarkerItem.getRadius',
        'GolemFilterItem',
        'GolemUpgradeItem',
        'TaskRadius',
        'TaskPriority',
        'setTaskMarker(mode, markerPos',
    ],
    'src/main/java/com/darkifov/thaumcraft/block/GolemBellItem.java': [
        'GolemBellMode.STATUS',
        'golem.statusSummary()',
        'Owned golems nearby',
    ],
    'src/main/java/com/darkifov/thaumcraft/golem/GolemBellMode.java': [
        'STATUS("status"',
    ],
    'src/main/java/com/darkifov/thaumcraft/block/GolemTaskMarkerItem.java': [
        'TAG_RADIUS',
        'TAG_PRIORITY',
        'getRadius',
        'Right-click air cycles work radius',
    ],
    'src/main/java/com/darkifov/thaumcraft/client/render/ThaumGolemRenderer.java': [
        'renderPart',
        'left arm',
        'right arm',
        'left leg',
        'right leg',
        'Math.sin',
    ],
    'build.gradle': ["version = '1.43.0'"],
    'src/main/resources/META-INF/mods.toml': ['version="1.43.0"'],
}
errors: list[str] = []
for name, path in checks.items():
    if not path.exists():
        errors.append(f'missing {name}: {path.relative_to(ROOT)}')

for rel, tokens in required_tokens.items():
    path = ROOT / rel
    text = path.read_text(encoding='utf-8') if path.exists() else ''
    for token in tokens:
        if token not in text:
            errors.append(f'{rel} missing token {token}')

report = {
    'stage': 142,
    'name': 'TC4 Golemancy GUI / Live Configuration / Renderer Finish Pass',
    'focus': [
        'direct live golem configuration after spawning',
        'bell status mode',
        'marker radius/priority metadata',
        'multi-part TC4-like golem renderer',
        'NBT persistence for task radius/priority',
    ],
    'checked_files': {name: str(path.relative_to(ROOT)) for name, path in checks.items()},
    'errors': errors,
}
(ROOT / 'STAGE143_GOLEMANCY_GUI_RENDER_AUDIT.json').write_text(json.dumps(report, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')

if errors:
    for error in errors:
        print(f'::error::Stage143 audit failed: {error}')
    sys.exit(1)
print('Stage143 golemancy GUI/render audit: OK')
