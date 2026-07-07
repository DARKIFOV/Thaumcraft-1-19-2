#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

renderer = read('src/main/java/com/darkifov/thaumcraft/client/render/WandItemRenderer.java')
focus_type = read('src/main/java/com/darkifov/thaumcraft/wand/WandFocusType.java')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')

for token in [
    'ORIGINAL_FOCUS_MODEL',
    'textures/original/thaumcraft4/models/wand.png',
    'ORIGINAL_SCRIPT',
    'textures/original/thaumcraft4/misc/script.png',
    'renderOriginalTC4FocusLayers',
    'renderOriginalTC4Runes',
    'focusDepthTexture(WandFocusType focus)',
    'focusOrnamentTexture(WandFocusType focus)',
    'focus_portablehole_depth',
    'focus_warding_depth',
    'focus_primal_depth',
    'focus_trade_orn',
    'focus_warding_orn',
    'ORIGINAL_FOCUS_MODEL',
    'depth != null ? 153 : 242',
    '0.525F, 0.5525F, 0.525F',
    '0.500F, 0.500F, 0.500F',
    '0.165F, 0.1765F, 0.165F',
    '0.160F, 0.160F, 0.160F',
]:
    if token not in renderer:
        errors.append(f'WandItemRenderer missing Stage183 focus layer token {token}')

for token in [
    '15028484',
    '5204428',
    '10466239',
    '409606',
    '594985',
    '8747923',
    '16771535',
    '10854849',
]:
    if token not in focus_type:
        errors.append(f'WandFocusType missing original TC4 focus color {token}')

for rel in [
    'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/wand.png',
    'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/misc/script.png',
    'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/focus_trade_orn.png',
    'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/focus_warding_depth.png',
    'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/focus_primal_depth.png',
    'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/focus_portablehole_depth.png',
]:
    if not (ROOT / rel).exists():
        errors.append(f'missing original renderer asset {rel}')

for token in ['tc4_stage183_focus_renderer_layers_audit.py', 'python scripts/tc4_stage183_focus_renderer_layers_audit.py', 'thaumcraft-legacy-rebuild-stage204-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing Stage183 token {token}')

if not (("version = '2.04.0'" in build or "version = '1.98.0'" in build or "version = '2.00.0'" in build) and ('version="2.04.0"' in mods or 'version="1.98.0"' in mods or 'version="2.00.0"' in mods)):
    errors.append('project version must be 1.98.0 or 2.00.0')

if errors:
    for e in errors:
        print('::error::' + e)
    sys.exit(1)
print('Stage183 focus renderer layers audit: OK')
