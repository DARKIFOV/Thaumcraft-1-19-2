#!/usr/bin/env python3
from pathlib import Path
import sys, json
ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

errors=[]
be = read('src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java')
bonus = read('src/main/java/com/darkifov/thaumcraft/research/ResearchTableBonusRuntime.java')
runtime = read('src/main/java/com/darkifov/thaumcraft/research/ResearchTableInventoryRuntime.java')
block = read('src/main/java/com/darkifov/thaumcraft/block/ResearchTableBlock.java')
mapping = ROOT/'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_research_table_bonus_aspects_stage169.json'
report = ROOT/'STAGE169_RESEARCH_TABLE_BONUS_ASPECTS_REPORT.json'

for rel in [mapping, report]:
    if not rel.exists():
        errors.append(f'missing {rel.relative_to(ROOT)}')
if 'private final AspectList bonusAspects' not in be:
    errors.append('ResearchTableBlockEntity must own persistent bonusAspects')
if 'nextRecalc' not in be or 'RECALCULATE_INTERVAL_TICKS' not in be:
    errors.append('ResearchTableBlockEntity must store original nextRecalc cadence')
if 'tag.put("bonusAspects", saveBonusAspects())' not in be or 'loadBonusAspects' not in be:
    errors.append('bonusAspects must save/load in table NBT')
if 'ResearchTableBonusRuntime.recalculateInto' not in be:
    errors.append('table must use ResearchTableBonusRuntime.recalculateInto')
if 'ResearchTableBlockEntity::serverTick' not in block:
    errors.append('ResearchTableBlock must tick server-side for recalculateBonus parity')
for token in ['SCAN_RADIUS = 8', 'RECALCULATE_INTERVAL_TICKS = 600', 'level.getRandom().nextInt(chance)', 'Blocks.BOOKSHELF']:
    if token not in bonus:
        errors.append(f'ResearchTableBonusRuntime missing token {token}')
if 'mergeBonusAspectsIntoPlayer(player' in be:
    errors.append('Stage163 drift remains: createResearchNote still merges bonus aspects into player')
if 'return 0;' not in runtime or 'Deprecated Stage163 compatibility method' not in runtime:
    errors.append('old mergeBonusAspectsIntoPlayer must be disabled/deprecated')
if mapping.exists():
    data=json.loads(mapping.read_text(encoding='utf-8'))
    if data.get('ownership') != 'bonusAspects are stored on ResearchTableBlockEntity and are not merged into player pool':
        errors.append('stage169 mapping must document original table-local ownership')

if errors:
    for e in errors:
        print(f'::error::{e}')
    sys.exit(1)
print('Stage169 Research Table bonus aspects audit: OK')
