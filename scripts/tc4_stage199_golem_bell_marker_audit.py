#!/usr/bin/env python3
from pathlib import Path
import sys
ROOT = Path(__file__).resolve().parents[1]
checks = {
    'GolemBellMarkerRuntime.java': [
        'golemid', 'golemhomex', 'golemhomey', 'golemhomez', 'golemhomeface', 'markers',
        'changeMarkers', 'bindGolem', 'getMarkersTag', 'Marker(int x, int y, int z, int dim, byte side, byte color)'
    ],
    'GolemBellItem.java': [
        'GolemBellMarkerRuntime.changeMarkers', 'GolemBellMarkerRuntime.boundGolem',
        'GolemBellMarkerRuntime.markerSummary', 'TC4 marker'
    ],
    'ThaumGolemEntity.java': [
        'GolemBellMarkerRuntime.bindGolem', 'originalMarkerListSnapshot', 'applyOriginalMarkerList',
        'tag.put(GolemOriginalRuntime.NBT_MARKERS, originalMarkerListSnapshot())'
    ],
}
paths = {
    'GolemBellMarkerRuntime.java': ROOT/'src/main/java/com/darkifov/thaumcraft/golem/GolemBellMarkerRuntime.java',
    'GolemBellItem.java': ROOT/'src/main/java/com/darkifov/thaumcraft/block/GolemBellItem.java',
    'ThaumGolemEntity.java': ROOT/'src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java',
}
errors=[]
for name, snippets in checks.items():
    path=paths[name]
    if not path.exists():
        errors.append(f'missing {name}')
        continue
    text=path.read_text(encoding='utf-8')
    for snippet in snippets:
        if snippet not in text:
            errors.append(f'{name} missing {snippet}')
if errors:
    for e in errors: print('::error::'+e)
    sys.exit(1)
print('Stage199 golem bell/marker parity audit: OK')
