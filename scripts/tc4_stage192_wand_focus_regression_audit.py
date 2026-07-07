#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
read = lambda p: (ROOT / p).read_text(encoding='utf-8') if (ROOT / p).exists() else ''

files = {
    'focus_runtime': read('src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java'),
    'focus_upgrade': read('src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeRuntime.java'),
    'focus_upgrade_type': read('src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeType.java'),
    'manager': read('src/main/java/com/darkifov/thaumcraft/wand/WandManagerRuntime.java'),
    'architect': read('src/main/java/com/darkifov/thaumcraft/wand/FocusArchitectRuntime.java'),
    'pouch': read('src/main/java/com/darkifov/thaumcraft/block/FocusPouchItem.java'),
    'pouch_menu': read('src/main/java/com/darkifov/thaumcraft/menu/FocusPouchMenu.java'),
    'projectile_base': read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4FocusProjectileEntity.java'),
    'frost': read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4FrostShardEntity.java'),
    'explosive': read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4ExplosiveOrbEntity.java'),
    'shock': read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4ShockOrbEntity.java'),
    'primal': read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4PrimalOrbEntity.java'),
    'renderer': read('src/main/java/com/darkifov/thaumcraft/client/render/WandItemRenderer.java'),
    'proj_renderer': read('src/main/java/com/darkifov/thaumcraft/client/render/TC4FocusProjectileRenderer.java'),
    'components': read('src/main/java/com/darkifov/thaumcraft/wand/WandComponentData.java'),
    'crafting': read('src/main/java/com/darkifov/thaumcraft/wand/WandCraftingRuntime.java'),
    'configrecipes': read('src/main/java/com/darkifov/thaumcraft/wand/TC4ConfigRecipesWandIndex.java'),
    'arcane_be': read('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java'),
    'arcane_menu': read('src/main/java/com/darkifov/thaumcraft/menu/ArcaneWorkbenchMenu.java'),
    'build': read('build.gradle'),
    'mods': read('src/main/resources/META-INF/mods.toml'),
    'workflow': read('.github/workflows/main.yml'),
    'guard': read('scripts/github_ci_guard.py'),
}

checks = {
    'version_192': ("version = '2.04.0'" in files['build'] or "version = '1.98.0'" in files['build'] or "version = '2.00.0'" in files['build']) and ('version="2.04.0"' in files['mods'] or 'version="1.98.0"' in files['mods'] or 'version="2.00.0"' in files['mods'] or 'version="2.02.0"' in files['mods']),
    'focus_original_stack_nbt': all(token in files['focus_runtime'] for token in ['TAG_ORIGINAL_FOCUS_STACK = "focus"', 'saveFocusStack', 'ItemStack.of']),
    'focus_upgrades_original_nbt': all(token in files['focus_upgrade'] + files['focus_upgrade_type'] for token in ['TAG_UPGRADE = "upgrade"', 'MAX_RANK = 5', 'POTENCY', 'FRUGAL', 'ENLARGE', 'EXTEND', 'TREASURE', 'SILK_TOUCH']),
    'continuous_focus_hooks_survived': all(token in files['focus_runtime'] for token in ['onUsingFocusTick', 'onPlayerStoppedUsingFocus', 'TC4EmberEntity', 'fireloop', 'EXCAVATION_BREAKCOUNT']),
    'architect_original_nbt': all(token in files['architect'] for token in ['areax', 'areay', 'areaz', 'aread', 'picked', 'toggleMisc']),
    'manager_focus_flow': all(token in files['manager'] for token in ['TreeMap', 'higherEntry', 'ceilingEntry', 'cameraticks', 'FocusPouchItem']),
    'pouch_original_inventory_nbt': all(token in files['pouch'] + files['pouch_menu'] for token in ['Inventory', 'Slot', '18', 'removeFocusAt', 'addExactFocusStack']),
    'projectile_entities_registered_behaviour': all(token in ''.join(files[k] for k in ['projectile_base','frost','explosive','shock','primal']) for token in ['maxLife', 'discard', 'explode', 'WEAKNESS', '5000']),
    'projectile_renderer_assets': all(token in files['proj_renderer'] for token in ['frostshard.png', 'particles.png', 'particles2.png', 'primal']),
    'wand_renderer_layers': all(token in files['renderer'] for token in ['ModelWand', 'focus cube', 'depth', 'ornament', 'script.png', 'hasRunes']),
    'rod_cap_sceptre_root_nbt': all(token in files['components'] + files['crafting'] for token in ['ORIGINAL_TAG_ROD', 'ORIGINAL_TAG_CAP', 'ORIGINAL_TAG_SCEPTRE', 'root.putString', 'setSceptre']),
    'configrecipes_chain': all(token in files['configrecipes'] + files['crafting'] for token in ['ConfigRecipes lines', 'WandCapGold', 'WandRodGreatwoodStaff', 'ArcaneWandRecipe', 'ArcaneSceptreRecipe']),
    'arcane_slot_stage191_not_regressed': all(token in files['arcane_be'] + files['arcane_menu'] for token in ['RecipeType.CRAFTING', 'consumeOriginalCraftMatrix', 'getCraftingRemainingItem', 'canDragTo', 'ClickType.THROW']),
    'workflow_and_guard': 'tc4_stage192_wand_focus_regression_audit.py' in files['workflow'] and 'tc4_stage192_wand_focus_regression_audit.py' in files['guard'],
}

errors = [name for name, ok in checks.items() if not ok]
if errors:
    for error in errors:
        print(f'::error::Stage192 wand/focus regression audit failed: {error}')
    sys.exit(1)
print('Stage192 wand/focus regression audit: OK')
