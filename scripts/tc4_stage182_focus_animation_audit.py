#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

wand = read('src/main/java/com/darkifov/thaumcraft/block/WandItem.java')
focus = read('src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java')
renderer = read('src/main/java/com/darkifov/thaumcraft/client/render/WandItemRenderer.java')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')

for token in [
    'return UseAnim.NONE;',
    'Do not let vanilla draw the wand as a bow',
]:
    if token not in wand:
        errors.append(f'WandItem missing Stage182 no-bow animation token {token}')
if 'UseAnim.BOW' in wand:
    errors.append('WandItem still uses vanilla UseAnim.BOW drift')

for token in [
    'public enum WandFocusAnimation',
    'WAVE,',
    'CHARGE',
    'public static WandFocusAnimation focusAnimation(ItemStack wandStack)',
    'return focusHasUpgrade(wandStack, FocusUpgradeType.FIREBALL) ? WandFocusAnimation.WAVE : WandFocusAnimation.CHARGE',
    'return focusHasUpgrade(wandStack, FocusUpgradeType.EARTH_SHOCK) ? WandFocusAnimation.WAVE : WandFocusAnimation.CHARGE',
    'type == WandFocusType.EXCAVATION',
    'return WandFocusAnimation.WAVE',
]:
    if token not in focus:
        errors.append(f'WandFocusRuntime missing original WandFocusAnimation token {token}')

for token in [
    'applyFocusUseAnimation(stack, poseStack)',
    'Stage182 adapter for original ItemWandRenderer held focus animation',
    'player.getTicksUsingItem() + minecraft.getFrameTime()',
    'WandFocusRuntime.focusAnimation(stack)',
    'Mth.sin(ticks / 10.0F) * 10.0F',
    'Mth.sin(ticks / 15.0F) * 10.0F',
    'Mth.sin(ticks / 0.8F) * 1.0F',
    'Mth.sin(ticks / 0.7F) * 1.0F',
    'Vector3f.ZP.rotationDegrees(waveZ)',
    'Vector3f.XP.rotationDegrees(chargeX)',
]:
    if token not in renderer:
        errors.append(f'WandItemRenderer missing Stage182 animation token {token}')

for token in ['tc4_stage182_focus_animation_audit.py', 'python scripts/tc4_stage182_focus_animation_audit.py', 'thaumcraft-legacy-rebuild-stage204-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing Stage182 token {token}')

if not (("version = '2.04.0'" in build or "version = '1.98.0'" in build or "version = '2.00.0'" in build) and ('version="2.04.0"' in mods or 'version="1.98.0"' in mods or 'version="2.00.0"' in mods)):
    errors.append('project version must be 1.98.0 or 2.00.0')

if errors:
    for e in errors:
        print('::error::' + e)
    sys.exit(1)
print('Stage182 focus animation audit: OK')
