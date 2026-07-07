#!/usr/bin/env python3
from __future__ import annotations
import json, sys
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []
def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding='utf-8')
files = {
    'runtime': 'src/main/java/com/darkifov/thaumcraft/research/ResearchTableInventoryRuntime.java',
    'table': 'src/main/java/com/darkifov/thaumcraft/block/ResearchTableBlock.java',
    'block_entity': 'src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java',
    'solver': 'src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java',
    'workflow': '.github/workflows/main.yml',
    'guard': 'scripts/github_ci_guard.py',
}
for name, rel in files.items():
    if not (ROOT / rel).exists():
        errors.append(f'missing Stage163 file: {rel}')
runtime = read(files['runtime']) if (ROOT / files['runtime']).exists() else ''
table = read(files['table']) if (ROOT / files['table']).exists() else ''
block_entity = read(files['block_entity']) if (ROOT / files['block_entity']).exists() else ''
solver = read(files['solver']) if (ROOT / files['solver']).exists() else ''
for token in [
    'SLOT_SCRIBING_TOOLS = 0',
    'SLOT_RESEARCH_NOTE = 1',
    'findInkedScribingTools(Player player)',
    'consumeInkForCreate(Player player)',
    'consumeInkForEdit(Player player)',
    'recalculateNearbyBonusAspects(Level level, BlockPos pos)',
    'mergeBonusAspectsIntoPlayer(Player player, Level level, BlockPos pos)',
]:
    if token not in runtime:
        errors.append(f'ResearchTableInventoryRuntime missing token: {token}')
# Stage165 moved the Stage163 validation from the right-click block adapter into
# the persistent ResearchTableBlockEntity primary path. Keep Stage163 behavior but
# do not require the old virtual-slot calls to remain in ResearchTableBlock.
for token in [
    'consumeInk(ResearchTableInventoryRuntime.INK_PER_NOTE_CREATE, player)',
    'ResearchTableBonusRuntime.summary(bonusAspects)',
    'hasInkedScribingTools()',
    'ResearchTableInventoryRuntime.missingToolsMessage()',
]:
    if token not in block_entity:
        errors.append(f'ResearchTableBlockEntity missing Stage163 behavior token: {token}')
if 'NetworkHooks.openScreen' not in table:
    errors.append('ResearchTableBlock must open the real Stage165 container screen')
for token in [
    'ResearchTableInventoryRuntime.checkInkForEdit(player)',
    'ResearchTableInventoryRuntime.consumeInkForEdit(player)',
    'shouldRefundClearedAspect(Player player)',
    'PlayerThaumData.hasResearch(player, "RESEARCHER2")',
    'PlayerThaumData.hasResearch(player, "RESEARCHER1")',
]:
    if token not in solver:
        errors.append(f'ResearchNoteSolver missing Stage163 token: {token}')
json_path = ROOT / 'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_research_table_inventory_ink_stage163.json'
if not json_path.exists():
    errors.append('missing tc4_research_table_inventory_ink_stage163.json')
else:
    data = json.loads(json_path.read_text(encoding='utf-8'))
    if data.get('stage') != 163:
        errors.append('Stage163 json has wrong stage')
    if data.get('original_slots', {}).get('0') != 'IScribeTools':
        errors.append('Stage163 json must preserve original slot 0 IScribeTools')
workflow = read(files['workflow']) if (ROOT / files['workflow']).exists() else ''
guard = read(files['guard']) if (ROOT / files['guard']).exists() else ''
for token in ['tc4_stage163_research_table_inventory_ink_audit.py', 'python scripts/tc4_stage163_research_table_inventory_ink_audit.py']:
    if token not in workflow and token not in guard:
        errors.append(f'Stage163 workflow/guard missing token: {token}')
if errors:
    for error in errors:
        print(f'::error::{error}')
    sys.exit(1)
print('Stage163 research table inventory/ink audit: OK')
