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
mapping=ROOT/'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_wand_focus_behavior_stage171.json'
report=ROOT/'STAGE171_WAND_FOCUS_BEHAVIOR_REPORT.json'
for p in [mapping, report]:
    if not p.exists():
        errors.append(f'missing {p.relative_to(ROOT)}')
if mapping.exists():
    data=json.loads(mapping.read_text(encoding='utf-8'))
    if not data.get('strict_original'):
        errors.append('stage171 mapping must be strict_original')
    for key in ['fire','frost','shock','excavation','equal_trade','primal']:
        if key not in data.get('foci',{}):
            errors.append(f'stage171 mapping missing focus {key}')
for token in ['castFire', 'castFrost', 'castShock', 'castExcavation', 'castEqualTrade', 'castPrimal', 'FocusArchitectRuntime', 'equalTradeArchitectBlocks', 'equalTradeLinkedBlocks']:
    if token not in runtime:
        errors.append(f'WandFocusRuntime missing {token}')
for token in ['FIRE("fire"', 'FROST("frost"', 'SHOCK("shock"', 'EXCAVATION("excavation"', 'EQUAL_TRADE("equal_trade"', 'PRIMAL("primal"']:
    if token not in types:
        errors.append(f'WandFocusType missing {token}')
if 'original-style connected swap' not in mapping.read_text(encoding='utf-8') if mapping.exists() else True:
    errors.append('stage171 mapping must document Stage177 equal-trade TC4 adapter')
for token in ['tc4_stage171_wand_focus_behavior_audit.py','python scripts/tc4_stage171_wand_focus_behavior_audit.py']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing {token}')
if errors:
    for e in errors:
        print('::error::'+e)
    sys.exit(1)
print('Stage171 wand focus behavior audit: OK')
