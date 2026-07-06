#!/usr/bin/env python3
from pathlib import Path
import json, re, hashlib

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/thaumcraft'

def texture_path(ref):
    if ':' not in ref:
        return None
    ns, path = ref.split(':', 1)
    if ns != 'thaumcraft':
        return None
    return ASSETS / 'textures' / (path + '.png')

refs = []
for p in list((ASSETS / 'models').rglob('*.json')) + list((ASSETS / 'blockstates').rglob('*.json')):
    try:
        data = json.load(open(p, encoding='utf-8'))
    except Exception as exc:
        refs.append({'file': str(p.relative_to(ROOT)), 'error': str(exc)})
        continue
    def walk(x):
        if isinstance(x, dict):
            if isinstance(x.get('textures'), dict):
                for slot, ref in x['textures'].items():
                    if isinstance(ref, str):
                        t = texture_path(ref)
                        if t is not None:
                            refs.append({'file': str(p.relative_to(ROOT)), 'slot': slot, 'texture': ref, 'exists': t.exists(), 'resolved': str(t.relative_to(ROOT))})
            for value in x.values():
                walk(value)
        elif isinstance(x, list):
            for value in x:
                walk(value)
    walk(data)

orig = sorted((ASSETS / 'original_tc4_1710').rglob('*.png'))
modern = sorted((ASSETS / 'textures').rglob('*.png'))
missing = [r for r in refs if r.get('exists') is False]
report = {
    'stage': 139,
    'model_texture_refs': len([r for r in refs if 'texture' in r]),
    'missing_texture_refs': len(missing),
    'original_tc4_png_textures': len(orig),
    'modern_png_textures': len(modern),
    'original_sound_files': len(list((ASSETS / 'original_tc4_1710/sounds').glob('*.ogg'))),
    'modern_sound_files': len(list((ASSETS / 'sounds').glob('*.ogg'))),
    'missing': missing[:200],
}
print(json.dumps(report, indent=2, ensure_ascii=False))
if missing:
    raise SystemExit(1)
