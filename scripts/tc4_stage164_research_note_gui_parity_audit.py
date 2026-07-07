#!/usr/bin/env python3
from __future__ import annotations
import json, sys
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []
def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding='utf-8')
required = [
    'src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java',
    'src/main/java/com/darkifov/thaumcraft/research/ResearchNoteGrid.java',
    'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_research_note_gui_stage164.json',
]
for rel in required:
    if not (ROOT / rel).exists():
        errors.append(f'missing Stage164 file: {rel}')
screen = read(required[0]) if (ROOT / required[0]).exists() else ''
grid = read(required[1]) if (ROOT / required[1]).exists() else ''
for token in [
    'private Aspect draggedAspect;',
    'private boolean draggingAspect;',
    'mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)',
    'mouseReleased(double mouseX, double mouseY, int button)',
    'paletteAspectAt(double mouseX, double mouseY)',
    'renderDraggedAspect(PoseStack poseStack, int mouseX, int mouseY)',
    'ResearchNoteGrid.hitTest(',
]:
    if token not in screen:
        errors.append(f'ResearchNoteScreen missing Stage164 token: {token}')
for token in ['hitTest(int relativeX, int relativeY, int radius)', 'bestDistance <= 144.0D', 'activeSlotsForRadius(actual)']:
    if token not in grid:
        errors.append(f'ResearchNoteGrid missing Stage164 token: {token}')
json_path = ROOT / required[2]
if json_path.exists():
    data = json.loads(json_path.read_text(encoding='utf-8'))
    if data.get('stage') != 164:
        errors.append('Stage164 json has wrong stage')
    if not data.get('no_new_puzzle_rules'):
        errors.append('Stage164 json must state no new puzzle rules')
workflow = read('.github/workflows/main.yml') if (ROOT / '.github/workflows/main.yml').exists() else ''
guard = read('scripts/github_ci_guard.py') if (ROOT / 'scripts/github_ci_guard.py').exists() else ''
if 'tc4_stage164_research_note_gui_parity_audit.py' not in workflow or 'tc4_stage164_research_note_gui_parity_audit.py' not in guard:
    errors.append('Stage164 workflow/guard missing audit')
if not any(name in workflow for name in ['thaumcraft-legacy-rebuild-stage194-jars', 'thaumcraft-legacy-rebuild-stage165-jars', 'thaumcraft-legacy-rebuild-stage164-jars']):
    errors.append('Stage164 workflow missing current jar artifact')
if errors:
    for error in errors:
        print(f'::error::{error}')
    sys.exit(1)
print('Stage164 research note GUI parity audit: OK')
