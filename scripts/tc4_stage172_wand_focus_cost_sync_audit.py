#!/usr/bin/env python3
from pathlib import Path
import json, sys
ROOT=Path(__file__).resolve().parents[1]
errors=[]

def read(rel):
    p=ROOT/rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

runtime=read('src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java')
types=read('src/main/java/com/darkifov/thaumcraft/wand/WandFocusType.java')
workflow=read('.github/workflows/main.yml')
guard=read('scripts/github_ci_guard.py')
build=read('build.gradle')
mods=read('src/main/resources/META-INF/mods.toml')
mapping=ROOT/'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_wand_focus_cost_sync_stage172.json'
report=ROOT/'STAGE172_WAND_FOCUS_COST_SYNC_REPORT.json'
for p in [mapping, report]:
    if not p.exists():
        errors.append(f'missing {p.relative_to(ROOT)}')
if mapping.exists():
    data=json.loads(mapping.read_text(encoding='utf-8'))
    if data.get('base_cooldowns',{}).get('frost') != 200:
        errors.append('frost cooldown must be 200 from original ItemFocusFrost')
    if data.get('base_cooldowns',{}).get('shock') != 250:
        errors.append('shock cooldown must be 250 from original ItemFocusShock')
    if data.get('base_cooldowns',{}).get('primal') != 500:
        errors.append('primal cooldown must be 500 from original ItemFocusPrimal')
for token in ['cooldownTicks()', 'isVisCostPerTick()', 'FROST("frost", "Focus: Frost", "focus_frost", 0x83DEFF, 200', 'SHOCK("shock", "Focus: Shock", "focus_shock", 0xFFFF55, 250', 'PRIMAL("primal", "Focus: Primal", "focus_primal", 0xFFFFFF, 500']:
    if token not in types:
        errors.append(f'WandFocusType missing cooldown/cost token {token}')
for token in ['primalCost(RandomSource random)', '50 + random.nextInt(5) * 50', 'player.getCooldowns().addCooldown', 'TC4Sounds.event("wandfail")', 'soundFor(type)', 'pitchFor(type)']:
    if token not in runtime:
        errors.append(f'WandFocusRuntime missing Stage172 token {token}')
for token in ['tc4_stage172_wand_focus_cost_sync_audit.py','python scripts/tc4_stage172_wand_focus_cost_sync_audit.py','thaumcraft-legacy-rebuild-stage204-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing {token}')
if not (("version = '2.04.0'" in build or "version = '1.98.0'" in build or "version = '2.00.0'" in build) and ('version="2.04.0"' in mods or 'version="1.98.0"' in mods or 'version="2.00.0"' in mods)):
    errors.append('project version must be 1.98.0 or 2.00.0')
if errors:
    for e in errors:
        print('::error::'+e)
    sys.exit(1)
print('Stage172 wand focus cost/sync audit: OK')
