#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

runtime = read('src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java')
focus_type = read('src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeType.java')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')

for token in [
    'focusVisCost(ItemStack wandStack, WandFocusType type, RandomSource random)',
    'activationCooldown(ItemStack wandStack, WandFocusType type)',
    'focusModifiedVisCost',
    'FocusUpgradeType.FRUGAL',
    'FocusUpgradeType.POTENCY',
    'FocusUpgradeType.ENLARGE',
    'FocusUpgradeType.EXTEND',
    'FocusUpgradeType.TREASURE',
    'FocusUpgradeType.SILK_TOUCH',
]:
    if token not in runtime:
        errors.append(f'WandFocusRuntime missing upgrade-effect token {token}')

for token in [
    'Aspect.IGNIS, 66, Aspect.PERDITIO, 33',
    'Aspect.AQUA, 20, Aspect.IGNIS, 2, Aspect.PERDITIO, 2, Aspect.AER, 5',
    'Aspect.AER, 75, Aspect.TERRA, 25',
    'Aspect.PERDITIO, 10, Aspect.AER, 10',
    '50 + random.nextInt(5) * 50',
    'capModifier - frugalModifier',
    'Math.max(0.1F',
    '1.0F + focusUpgradeLevel(wandStack, FocusUpgradeType.POTENCY) * 0.4F',
    '5 + potency * 2',
    '4.0F + potency * 2.0F',
    '3.0D + potency * 1.5D',
    '4.0F + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE) * 2.0F',
    '33 + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE) * 8',
    '120 + focusUpgradeLevel(wandStack, FocusUpgradeType.EXTEND) * 60',
    'Block.dropResources(state, level, target',
    'Enchantments.SILK_TOUCH',
    'Enchantments.BLOCK_FORTUNE',
    'FocusArchitectRuntime.equalTradeArchitectBlocks',
    'FocusArchitectRuntime.equalTradeLinkedBlocks',
]:
    if token not in runtime:
        errors.append(f'WandFocusRuntime missing original upgrade behavior fragment {token}')

for token in ['POTENCY(0', 'FRUGAL(1', 'TREASURE(2', 'ENLARGE(3', 'EXTEND(7', 'SILK_TOUCH(8']:
    if token not in focus_type:
        errors.append(f'FocusUpgradeType missing original id token {token}')

for forbidden in ['fake effect', 'TODO fake', 'placeholder upgrade']:
    if forbidden in runtime.lower():
        errors.append(f'forbidden fake upgrade marker present: {forbidden}')

for token in ['tc4_stage175_focus_upgrade_effects_audit.py', 'python scripts/tc4_stage175_focus_upgrade_effects_audit.py', 'thaumcraft-legacy-rebuild-stage194-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing {token}')

if "version = '1.94.0'" not in build or 'version="1.94.0"' not in mods:
    errors.append('project version must be 1.94.0')

if errors:
    for e in errors:
        print('::error::' + e)
    sys.exit(1)
print('Stage175 focus upgrade effects audit: OK')
