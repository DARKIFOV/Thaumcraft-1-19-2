#!/usr/bin/env python3
from pathlib import Path
import sys, json
ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    p=ROOT/rel
    return p.read_text(encoding='utf-8') if p.exists() else ''
errors=[]
be=read('src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java')
menu=read('src/main/java/com/darkifov/thaumcraft/menu/ResearchTableMenu.java')
runtime=read('src/main/java/com/darkifov/thaumcraft/research/ResearchTableInventoryRuntime.java')
solver=read('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java')
screen=read('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java')
workflow=read('.github/workflows/main.yml')
guard=read('scripts/github_ci_guard.py')
mapping=ROOT/'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_research_table_bonus_sync_stage170.json'
report=ROOT/'STAGE170_RESEARCH_TABLE_BONUS_SYNC_REPORT.json'
for p in [mapping, report]:
    if not p.exists():
        errors.append(f'missing {p.relative_to(ROOT)}')
for token in ['getUpdateTag()', 'getUpdatePacket()', 'onDataPacket', 'syncToClient()', 'sendBlockUpdated']:
    if token not in be:
        errors.append(f'ResearchTableBlockEntity missing sync token {token}')
for token in ['tableBonusAspects()', 'tableBonusAmount', 'consumeBonusAspect']:
    if token not in menu:
        errors.append(f'ResearchTableMenu missing {token}')
for token in ['hasPoolOrTableBonus', 'consumePoolOrTableBonus', 'tableBonusAspects']:
    if token not in runtime:
        errors.append(f'ResearchTableInventoryRuntime missing {token}')
if 'ResearchTableInventoryRuntime.hasPoolOrTableBonus' not in solver or 'ResearchTableInventoryRuntime.consumePoolOrTableBonus' not in solver:
    errors.append('ResearchNoteSolver must consume player pool or table bonus aspect')
if 'Bonus ' not in screen or 'bonusSummary()' not in screen:
    errors.append('ResearchTableContainerScreen must display table bonus aspects')
for token in ['tc4_stage169_research_table_bonus_aspects_audit.py', 'tc4_stage170_research_table_bonus_sync_audit.py', 'thaumcraft-legacy-rebuild-stage194-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing {token}')
if "version = '1.94.0'" not in read('build.gradle') or 'version="1.94.0"' not in read('src/main/resources/META-INF/mods.toml'):
    errors.append('project version must be 1.94.0')
if mapping.exists():
    data=json.loads(mapping.read_text(encoding='utf-8'))
    if not data.get('strict_original'):
        errors.append('stage170 mapping must be strict_original')

if errors:
    for e in errors:
        print(f'::error::{e}')
    sys.exit(1)
print('Stage170 Research Table bonus sync audit: OK')
