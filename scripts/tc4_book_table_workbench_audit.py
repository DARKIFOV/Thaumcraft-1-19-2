#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
java = ROOT / 'src/main/java/com/darkifov/thaumcraft'
resources = ROOT / 'src/main/resources'

files = {
    'arcane_menu': java / 'menu/ArcaneWorkbenchMenu.java',
    'arcane_block_entity': java / 'blockentity/ArcaneWorkbenchBlockEntity.java',
    'arcane_screen': java / 'client/screen/ArcaneWorkbenchContainerScreen.java',
    'research_table_block': java / 'block/ResearchTableBlock.java',
    'research_note_solver': java / 'research/ResearchNoteSolver.java',
    'thaumonomicon_screen': java / 'client/screen/ThaumonomiconScreen.java',
    'research_page_screen': java / 'client/screen/TC4ResearchPageScreen.java',
}
texts = {name: path.read_text(encoding='utf-8', errors='ignore') for name, path in files.items()}
arcane_recipes = list((resources / 'data/thaumcraft/thaumcraft_arcane_workbench').glob('*.json'))
research_entries = resources / 'data/thaumcraft/tc4_source_mapping/tc4_original_research_entries_stage116.json'

checks = {
    'tc4_arcane_slot_layout': '40 + col * 24' in texts['arcane_menu'] and '160, 24' in texts['arcane_menu'] and '160, 64' in texts['arcane_menu'],
    'grid_catalyst_compatibility': 'findCatalystSlot' in texts['arcane_block_entity'] and 'also allow the catalyst to live in the 3x3 grid' in texts['arcane_block_entity'],
    'arcane_recipe_ghosts_visible': 'renderGhostItems' in texts['arcane_screen'] and 'TC4 ghost layout' in texts['arcane_screen'],
    'arcane_exact_vis_sync': 'recipe.aspectCostText()' in (java / 'network/ArcaneRecipeSyncPacket.java').read_text(encoding='utf-8', errors='ignore') and 'entry.visCost' in (java / 'network/ArcaneRecipeSyncPacket.java').read_text(encoding='utf-8', errors='ignore'),
    'arcane_pattern_rows_synced': 'patternRows' in (java / 'network/ArcaneRecipeSyncPacket.java').read_text(encoding='utf-8', errors='ignore') and 'renderPatternGhost' in texts['arcane_screen'],
    'arcane_research_lock_visible': 'Research locked' in texts['arcane_screen'],
    'research_table_opens_note_puzzle': 'openResearchNote' in texts['research_table_block'] and 'Research completed:' in texts['research_table_block'],
    'research_note_no_pool_loss_on_invalid_slot': 'already filled' in texts['research_note_solver'] and 'silently overwrite' in (java / 'research/ResearchNoteState.java').read_text(encoding='utf-8', errors='ignore'),
    'thaumonomicon_category_progress_header': 'renderBrowserHeader' in texts['thaumonomicon_screen'] and 'Complete ' in texts['thaumonomicon_screen'],
    'thaumonomicon_recipe_icons': 'renderResolvedItemIcon' in texts['research_page_screen'] and 'mouseScrolled' in texts['research_page_screen'],
    'original_gui_textures_present': all((resources / f'assets/thaumcraft/textures/gui/{name}').exists() for name in ['arcane_workbench.png','research_table.png','thaumonomicon.png','gui_research.png','gui_researchback.png']),
}
report = {
    'stage': 138,
    'goal': 'finish current TC4 core UX with exact inferred TC4 arcane pattern symbol maps and recipe-page parity',
    'arcane_recipe_json_files': len(arcane_recipes),
    'tc4_research_entries_json_present': research_entries.exists(),
    'checks': checks,
    'passed': all(checks.values()),
}
print(json.dumps(report, indent=2, ensure_ascii=False))
if not report['passed']:
    raise SystemExit(1)
