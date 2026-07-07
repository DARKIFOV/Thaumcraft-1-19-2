#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

architect = read('src/main/java/com/darkifov/thaumcraft/wand/FocusArchitectRuntime.java')
focus = read('src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java')
upgrade = read('src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeRuntime.java')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')

for token in [
    'Stage177 strict adapter for original TC4 WandManager/IArchitect area state',
    'TAG_AREA_X = "areax"',
    'TAG_AREA_Y = "areay"',
    'TAG_AREA_Z = "areaz"',
    'TAG_AREA_DIM = "aread"',
    'TAG_PICKED_BLOCK = "picked"',
    '3 + enlarge * 2',
    '3 + enlarge',
    'toggleMisc',
    'showAxis',
    'equalTradeArchitectBlocks',
    'equalTradeLinkedBlocks',
    'wardingArchitectBlocks',
    'dir != side && dir != side.getOpposite()',
    'insideOriginalBounds',
    'isBlockExposed',
]:
    if token not in architect:
        errors.append(f'FocusArchitectRuntime missing original architect token {token}')

for token in [
    'storePickedBlock(wandStack',
    'getPickedBlock(wandStack)',
    'FocusArchitectRuntime.equalTradeArchitectBlocks',
    'FocusArchitectRuntime.equalTradeLinkedBlocks',
    'replaceBlockWithEqualTrade',
    'FocusArchitectRuntime.wardingArchitectBlocks',
    'WardedBlockRuntime.isWarded',
    'sparkleBlock(level, target, Aspect.ORDO.argbColor())',
    'TC4Sounds.event("zap")',
]:
    if token not in focus:
        errors.append(f'WandFocusRuntime missing Stage177 integration token {token}')

for token in [
    'canApplyUpgrade(ItemStack focusStack, WandFocusType focus, FocusUpgradeType type)',
    'focus == WandFocusType.WARDING && type == FocusUpgradeType.ENLARGE',
    'FocusUpgradeType.ARCHITECT',
    'focus == WandFocusType.SHOCK && type == FocusUpgradeType.ENLARGE',
    'focus == WandFocusType.FIRE && type == FocusUpgradeType.ALCHEMISTS_FIRE',
    'focus == WandFocusType.FROST && type == FocusUpgradeType.ALCHEMISTS_FROST',
]:
    if token not in upgrade:
        errors.append(f'FocusUpgradeRuntime missing original canApply gate {token}')

if 'equalTradeTargets(' in focus:
    errors.append('old square equalTradeTargets helper must be removed in favor of original linked/architect logic')

if not any(f"version = '{v}'" in build for v in ['2.04.0', '2.00.0']):
    errors.append('build.gradle must be version 2.04.0 or compatible')
if not any(f'version="{v}"' in mods for v in ['2.04.0', '2.00.0']):
    errors.append('mods.toml must be version 2.04.0 or compatible')

for token in ['tc4_stage177_focus_architect_area_audit.py', 'python scripts/tc4_stage177_focus_architect_area_audit.py', 'thaumcraft-legacy-rebuild-stage204-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing {token}')

if errors:
    for e in errors:
        print('::error::' + e)
    sys.exit(1)
print('Stage177 focus architect area audit: OK')
