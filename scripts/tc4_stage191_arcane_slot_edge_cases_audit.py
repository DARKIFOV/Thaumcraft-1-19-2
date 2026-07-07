#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
read = lambda p: (ROOT / p).read_text(encoding='utf-8') if (ROOT / p).exists() else ''

be = read('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java')
menu = read('src/main/java/com/darkifov/thaumcraft/menu/ArcaneWorkbenchMenu.java')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')

checks = {
    'version_192': "version = '1.94.0'" in build and 'version="1.94.0"' in mods,
    'vanilla_first_fallback': all(token in be for token in [
        'previewVanillaCraftingResult',
        'findMatchingVanillaCraftingRecipe',
        'RecipeType.CRAFTING',
        'CraftingContainer',
        'TC4DummyCraftingMenu',
        'does it ask ThaumcraftCraftingManager'
    ]) and be.index('previewVanillaCraftingResult') < be.index('findMatchingArcaneRecipe(player, false)'),
    'output_take_vanilla_then_arcane': all(token in be for token in [
        'tryConsumeVanillaCraftingResult(player)',
        'consumeOriginalCraftMatrix(player)',
        'getCraftingRemainingItem',
        'Containers.dropItemStack'
    ]),
    'original_matrix_consumption_path': 'for (int slot = SLOT_INGREDIENT_START; slot <= SLOT_INGREDIENT_END; slot++)' in be and 'stack.shrink(1)' in be,
    'no_shift_click_autofill_grid': 'moveItemStackTo(stack, MENU_SLOT_GRID_START' not in menu and 'does not shift-click ordinary inventory stacks into the 3x3 grid' in menu,
    'shift_click_original_ranges': all(token in menu for token in [
        'MENU_PLAYER_INV_START', 'MENU_PLAYER_INV_END', 'MENU_HOTBAR_START', 'MENU_HOTBAR_END',
        'output only moves into player inventory + hotbar',
        'Grid and wand slot move back to inventory/hotbar'
    ]),
    'right_click_and_throw_restrictions': all(token in menu for token in [
        'public void clicked(int slotId, int dragType, ClickType clickType, Player player)',
        'ClickType.THROW',
        'slotId == MENU_SLOT_OUTPUT || slotId == MENU_SLOT_WAND',
        'dragType = 0'
    ]),
    'drag_split_rejected_for_workbench_slots': 'public boolean canDragTo(Slot slot)' in menu and 'slot.container != workbench' in menu,
    'server_wand_slot_rejects_staff': 'return stack.getItem() instanceof WandItem && !WandItem.isStaffStack(stack);' in be,
    'workflow_and_guard': 'tc4_stage191_arcane_slot_edge_cases_audit.py' in workflow and 'tc4_stage191_arcane_slot_edge_cases_audit.py' in guard,
}

errors = [name for name, ok in checks.items() if not ok]
if errors:
    for error in errors:
        print(f'::error::Stage191 Arcane Workbench slot edge-case audit failed: {error}')
    sys.exit(1)
print('Stage191 Arcane Workbench slot edge-case audit: OK')
