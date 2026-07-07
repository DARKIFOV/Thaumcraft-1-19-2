#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []

def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding='utf-8')

grid = read('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteGrid.java')
state = read('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java')
client = read('src/main/java/com/darkifov/thaumcraft/client/ClientResearchNoteData.java')
screen = read('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java')
packet = read('src/main/java/com/darkifov/thaumcraft/network/ResearchNoteSyncPacket.java')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')

for token in [
    'radius = 1 + min(3, research.getComplexity())',
    'public static final int MAX_RADIUS = 4',
    'public static final int SLOT_COUNT = 61',
    'TYPE_RESEARCH_ANCHOR = 1',
    'distributeRing(int radius, int entries)',
    'activeSlotsForRadius(int radius)',
]:
    if token not in grid:
        errors.append(f'ResearchNoteGrid missing Stage161 token: {token}')

for token in [
    'TAG_TYPES = "SlotTypes"',
    'rebuildOriginalGrid(root, target)',
    'ResearchNoteGrid.distributeRing(radius, anchors.size())',
    'removeOriginalComplexityBlanks(target, radius, types)',
    'slotTypes(ItemStack stack)',
]:
    if token not in state:
        errors.append(f'ResearchNoteState missing Stage161 token: {token}')

if 'Map<Integer, Integer> TYPES' not in client or 'typeAt(int slot)' not in client:
    errors.append('ClientResearchNoteData must sync active TC4 hex slot types')
if 'new ResearchNoteSyncPacket(' not in read('src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java') or 'ResearchNoteState.slotTypes(note)' not in read('src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java'):
    errors.append('ThaumcraftNetwork must sync ResearchNoteState.slotTypes(note)')
if 'writeVarInt(packet.radius)' not in packet or 'packet.types' not in packet:
    errors.append('ResearchNoteSyncPacket must carry radius and slot types')
if 'ClientResearchNoteData.activeAt(slot.index())' not in screen or 'ClientResearchNoteData.anchorAt(index)' not in screen:
    errors.append('ResearchNoteScreen must render/use synced active slots and anchors')

json_path = ROOT / 'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_research_note_grid_stage161.json'
if not json_path.exists():
    errors.append('missing tc4_research_note_grid_stage161.json')
else:
    data = json.loads(json_path.read_text(encoding='utf-8'))
    if data.get('stage') != 161:
        errors.append('Stage161 json has wrong stage')
    if data.get('hex_counts_before_blanks', {}).get('radius_4') != 61:
        errors.append('Stage161 json must document radius 4 = 61 hexes')

if 'tc4_stage161_research_note_grid_parity_audit.py' not in workflow or 'tc4_stage161_research_note_grid_parity_audit.py' not in guard:
    errors.append('Stage161 workflow/guard missing audit')
if not any(name in workflow for name in ['thaumcraft-legacy-rebuild-stage194-jars', 'thaumcraft-legacy-rebuild-stage165-jars', 'thaumcraft-legacy-rebuild-stage164-jars']):
    errors.append('Stage161 workflow missing current jar artifact')
if "version = '1.94.0'" not in read('build.gradle'):
    errors.append('Stage161 build.gradle missing current Stage168 version')

if errors:
    for error in errors:
        print(f'::error::{error}')
    sys.exit(1)
print('Stage161 research note grid parity audit: OK')
