#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []

def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding='utf-8')

state = read('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java')
solver = read('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java')
item = read('src/main/java/com/darkifov/thaumcraft/block/ResearchNoteItem.java')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')

for token in [
    'isSolvedForPlayer(ItemStack stack, Player player)',
    'allAnchorsConnected(stack, true, player)',
    'PlayerAspectKnowledge.knows(player, currentAspect)',
    'pruneDisconnectedNonAnchors(stack)',
    'reachableFilledSlots',
]:
    if token not in state:
        errors.append(f'ResearchNoteState missing Stage162 token: {token}')

for token in [
    'ResearchNoteState.isSolvedForPlayer(note, player)',
    'ResearchNoteState.markSolved(note)',
    'convertSolvedNote(Player player, ItemStack note)',
    'OriginalResearchBridge.unlock(player, target.get())',
    'ResearchTableInventoryRuntime.consumePoolOrTableBonus(player, aspect)',
    'PlayerAspectKnowledge.addPool(player, removed.get(), 1)',
]:
    if token not in solver:
        errors.append(f'ResearchNoteSolver missing Stage162 token: {token}')

if 'ResearchNoteSolver.convertSolvedNote(player, stack)' not in item:
    errors.append('ResearchNoteItem must convert solved notes through ResearchNoteSolver')

json_path = ROOT / 'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_research_note_completion_stage162.json'
if not json_path.exists():
    errors.append('missing tc4_research_note_completion_stage162.json')
else:
    data = json.loads(json_path.read_text(encoding='utf-8'))
    if data.get('stage') != 162:
        errors.append('Stage162 json has wrong stage')
    if 'all type=1 original research anchors' not in data.get('completion_rule', ''):
        errors.append('Stage162 completion rule must document all type=1 anchor connectivity')

if 'tc4_stage162_research_note_completion_parity_audit.py' not in workflow or 'tc4_stage162_research_note_completion_parity_audit.py' not in guard:
    errors.append('Stage162 workflow/guard missing audit')
if not any(name in workflow for name in ['thaumcraft-legacy-rebuild-stage194-jars', 'thaumcraft-legacy-rebuild-stage165-jars', 'thaumcraft-legacy-rebuild-stage164-jars']):
    errors.append('Stage162 workflow missing current jar artifact')

if errors:
    for error in errors:
        print(f'::error::{error}')
    sys.exit(1)
print('Stage162 research note completion parity audit: OK')
