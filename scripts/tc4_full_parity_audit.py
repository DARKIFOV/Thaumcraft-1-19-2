#!/usr/bin/env python3
from pathlib import Path
import json, re

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/thaumcraft'
JAVA = ROOT / 'src/main/java'
SRCREF = ROOT / 'docs/source_refs/tc4_1710_original_source'

java_files = list(JAVA.rglob('*.java'))
orig_java = list(SRCREF.rglob('*.java')) if SRCREF.exists() else []
model_files = list((ASSETS / 'models').rglob('*.json'))
blockstate_files = list((ASSETS / 'blockstates').rglob('*.json'))
sound_events = json.load(open(ASSETS / 'sounds.json', encoding='utf-8'))

def count_occurrences(pattern, root):
    total = 0
    for p in root.rglob('*.java'):
        try:
            total += len(re.findall(pattern, p.read_text(encoding='utf-8', errors='ignore')))
        except Exception:
            pass
    return total

report = {
    'stage': 139,
    'goal': 'whole Thaumcraft 4 parity tracking plus Stage139 broad original-parity sweep',
    'original_tc4': {
        'java_files': len(orig_java),
        'png_textures': len(list((ASSETS / 'original_tc4_1710').rglob('*.png'))),
        'sound_files': len(list((ASSETS / 'original_tc4_1710/sounds').glob('*.ogg'))),
        'sound_events': len(sound_events),
        'lang_files': len(list((ASSETS / 'original_tc4_1710/lang').glob('*.lang'))),
    },
    'current_port': {
        'java_files': len(java_files),
        'model_json_files': len(model_files),
        'blockstate_json_files': len(blockstate_files),
        'png_textures': len(list((ASSETS / 'textures').rglob('*.png'))),
        'sound_files': len(list((ASSETS / 'sounds').glob('*.ogg'))),
    },
    'runtime_markers': {
        'tc4_research_component_entries': count_occurrences(r'e\("tc4_', ROOT / 'src/main/java/com/darkifov/thaumcraft/porting'),
        'tc4_sound_registry_class': (ROOT / 'src/main/java/com/darkifov/thaumcraft/porting/TC4Sounds.java').exists(),
        'tc4_full_parity_index_class': (ROOT / 'src/main/java/com/darkifov/thaumcraft/porting/TC4FullParityIndex.java').exists(),
    },
    'remaining_big_systems_to_finish': [
        'Crucible exact heat/boil/spill/flux behavior',
        'Full essentia suction/tube network parity',
        'Aura node renderer/beam polish and exact biome/world generation distribution',
        'Exact focus upgrade UI, focus animations and remaining TC4 focus edge cases',
        'Every golem core/upgrade/AI/seal behavior',
        'Taint/flux goo/gas spread and biome/worldgen behavior',
        'Eldritch progression, cultists, bosses and structures',
        'Exact renderers for jars, tubes, nodes, golems, wands, infusion matrix and special blocks',
    ]
}
print(json.dumps(report, indent=2, ensure_ascii=False))
