#!/usr/bin/env python3
from pathlib import Path
import json, sys
ROOT=Path(__file__).resolve().parents[1]
errors=[]

def read(rel):
    p=ROOT/rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

mapping=ROOT/'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_focus_upgrade_nbt_stage173.json'
upgrade_type=read('src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeType.java')
runtime=read('src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeRuntime.java')
wand_runtime=read('src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java')
wand_item=read('src/main/java/com/darkifov/thaumcraft/block/WandItem.java')
focus_item=read('src/main/java/com/darkifov/thaumcraft/block/WandFocusItem.java')
workflow=read('.github/workflows/main.yml')
guard=read('scripts/github_ci_guard.py')
build=read('build.gradle')
mods=read('src/main/resources/META-INF/mods.toml')
if not mapping.exists():
    errors.append('missing tc4_focus_upgrade_nbt_stage173.json')
else:
    data=json.loads(mapping.read_text(encoding='utf-8'))
    ids={e['id'] for e in data.get('upgrade_types',[])}
    for required in [0,1,2,3,4,5,6,7,8,9,10,11,12,16,17,18,20]:
        if required not in ids:
            errors.append(f'missing original focus upgrade id {required}')
    if data.get('original_nbt_key') != 'upgrade':
        errors.append('original focus upgrade NBT key must stay upgrade')
    if data.get('max_rank') != 5:
        errors.append('original max focus rank must be 5')
for token in ['POTENCY(0', 'FRUGAL(1', 'SILK_TOUCH(8', 'FIREBALL(9', 'FIREBEAM(10', 'SCATTERSHOT(11', 'ICE_BOULDER(12', 'SEEKER(16', 'CHAIN_LIGHTNING(17', 'EARTH_SHOCK(18', 'DOWSING(20']:
    if token not in upgrade_type:
        errors.append(f'FocusUpgradeType missing {token}')
for token in ['TAG_UPGRADE = "upgrade"', 'MAX_RANK = 5', 'getAppliedUpgrades', 'applyUpgrade', 'possibleUpgrades', 'case FIRE', 'case FROST', 'case SHOCK', 'case EXCAVATION']:
    if token not in runtime:
        errors.append(f'FocusUpgradeRuntime missing {token}')
for token in ['TAG_ORIGINAL_FOCUS_STACK = "focus"', 'setFocusStack', 'getFocusStack', 'focusUpgradeLevel', 'focusHasUpgrade']:
    if token not in wand_runtime:
        errors.append(f'WandFocusRuntime missing original focus stack token {token}')
for token in ['WandFocusRuntime.setFocusStack', 'WandFocusRuntime.getFocusStack']:
    if token not in wand_item:
        errors.append(f'WandItem missing focus-stack preservation {token}')
if 'FocusUpgradeRuntime.getAppliedUpgrades' not in focus_item:
    errors.append('WandFocusItem tooltip must expose original upgrade NBT')
for token in ['tc4_stage173_focus_upgrade_nbt_audit.py','python scripts/tc4_stage173_focus_upgrade_nbt_audit.py','thaumcraft-legacy-rebuild-stage194-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing {token}')
if "version = '1.94.0'" not in build or 'version="1.94.0"' not in mods:
    errors.append('project version must be 1.94.0')
if errors:
    for e in errors:
        print('::error::'+e)
    sys.exit(1)
print('Stage173 focus upgrade NBT audit: OK')
