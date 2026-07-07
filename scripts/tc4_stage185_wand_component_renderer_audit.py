#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

renderer = read('src/main/java/com/darkifov/thaumcraft/client/render/WandItemRenderer.java')
data = read('src/main/java/com/darkifov/thaumcraft/wand/WandComponentData.java')
wand = read('src/main/java/com/darkifov/thaumcraft/block/WandItem.java')
focus = read('src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')

for token in [
    'renderOriginalTC4WandComponents',
    'renderOriginalModelWandRodBox',
    'renderOriginalModelWandCapBox',
    'originalGlowingRodLight',
    '200.0F + Mth.sin(ticks) * 5.0F + 5.0F',
    'poseStack.translate(0.0D, -0.10D, 0.0D)',
    'poseStack.scale(1.20F, 2.00F, 1.20F)',
    'poseStack.scale(1.30F, 1.10F, 1.30F)',
    'poseStack.scale(1.20F, 1.00F, 1.20F)',
    'WandComponentData.isSceptre(stack)',
    'poseStack.scale(1.30F, 1.30F, 1.30F)',
    'poseStack.translate(0.0D, 0.30D, 0.0D)',
    'poseStack.scale(1.00F, 0.66F, 1.00F)',
    'poseStack.translate(0.0D, 0.225D, 0.0D)',
    'poseStack.translate(0.0D, 0.65D, 0.0D)',
    'data.hasRunes()'
]:
    if token not in renderer:
        errors.append(f'WandItemRenderer missing Stage185 renderer token {token}')

for token in [
    'isSceptre(ItemStack stack)',
    'root.contains("sceptre")',
    'hasRunes()',
    'rod == WandRodType.PRIMAL_STAFF',
    'capacity(ItemStack stack)',
    'capacity = (int)Math.floor(capacity * 1.5F)',
    'visCostModifier(ItemStack stack, Aspect aspect)',
    'modifier -= 0.1F',
    'Math.max(modifier, 0.1F)'
]:
    if token not in data:
        errors.append(f'WandComponentData missing Stage185 NBT/capacity token {token}')

if 'WandComponentData.from(stack).capacity(stack)' not in wand:
    errors.append('WandItem stack capacity is not using Stage185 sceptre capacity adapter')
if 'visCostModifier(wandStack, aspect)' not in focus:
    errors.append('WandFocusRuntime focus vis cost is not using Stage185 sceptre cost adapter')

for token in ['tc4_stage185_wand_component_renderer_audit.py', 'python scripts/tc4_stage185_wand_component_renderer_audit.py', 'thaumcraft-legacy-rebuild-stage194-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing Stage185 token {token}')

if "version = '1.94.0'" not in build or 'version="1.94.0"' not in mods:
    errors.append('project version must be 1.94.0')

if errors:
    for e in errors:
        print('::error::' + e)
    sys.exit(1)
print('Stage185 wand component renderer audit: OK')
