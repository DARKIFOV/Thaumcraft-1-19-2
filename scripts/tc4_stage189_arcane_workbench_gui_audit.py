#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
read = lambda p: (ROOT / p).read_text(encoding='utf-8') if (ROOT / p).exists() else ''

files = {
    'be': read('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java'),
    'menu': read('src/main/java/com/darkifov/thaumcraft/menu/ArcaneWorkbenchMenu.java'),
    'screen': read('src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchContainerScreen.java'),
    'wand': read('src/main/java/com/darkifov/thaumcraft/block/WandItem.java'),
    'build': read('build.gradle'),
    'mods': read('src/main/resources/META-INF/mods.toml'),
    'workflow': read('.github/workflows/main.yml'),
    'guard': read('scripts/github_ci_guard.py'),
}

checks = {
    'original_tile_slot_layout': all(token in files['be'] for token in [
        'SLOT_INGREDIENT_START = 0', 'SLOT_INGREDIENT_END = 8', 'SLOT_OUTPUT = 9', 'SLOT_WAND = 10'
    ]),
    'legacy_catalyst_hidden_only': 'SLOT_LEGACY_CATALYST = 11' in files['be'] and 'SLOT_CATALYST' not in files['be'],
    'original_menu_order': all(token in files['menu'] for token in [
        'MENU_SLOT_OUTPUT = 0', 'MENU_SLOT_WAND = 1', 'MENU_SLOT_GRID_START = 2', 'MENU_HOTBAR_END = 46'
    ]),
    'output_take_crafts': 'craftFromOutput(player)' in files['menu'] and 'slotsChanged(Container container)' in files['menu'],
    'slot_limited_by_wand_staff_guard': 'WandItem.isStaffStack' in files['menu'] and 'SlotLimitedByWand' in files['wand'],
    'server_preview_flow': all(token in files['be'] for token in [
        'updateOutputPreview', 'findMatchingArcaneRecipe', 'previewResult', 'ThaumcraftCraftingManager.findMatchingArcaneRecipe'
    ]),
    'screen_has_original_dimensions': 'imageWidth = 190' in files['screen'] and 'imageHeight = 234' in files['screen'],
    'screen_has_original_aspect_locs': all(token in files['screen'] for token in ['{72, 21}', '{24, 43}', '{24, 102}', '{72, 124}', '{120, 102}', '{120, 43}']),
    'screen_removed_recipe_browser': all(token not in files['screen'] for token in ['EditBox', 'new Button', 'Search recipe', 'requestArcaneMenuCraftFromClient']),
    'screen_original_empty_foreground': 'renderLabels' in files['screen'] and 'Original GuiArcaneWorkbench leaves the foreground layer empty' in files['screen'],
    'insufficient_vis_adapter': 'Insufficient vis' in files['screen'] and 'WandItem.modifiedVisCost' in files['screen'],
    'version_190': "version = '1.94.0'" in files['build'] and 'version="1.94.0"' in files['mods'],
    'workflow_and_guard': 'tc4_stage189_arcane_workbench_gui_audit.py' in files['workflow'] and 'tc4_stage189_arcane_workbench_gui_audit.py' in files['guard'],
}

errors = [name for name, ok in checks.items() if not ok]
if errors:
    for error in errors:
        print(f'::error::Stage189 arcane workbench GUI audit failed: {error}')
    sys.exit(1)
print('Stage189 arcane workbench GUI audit: OK')
