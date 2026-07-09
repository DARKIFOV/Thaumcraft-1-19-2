#!/usr/bin/env python3
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

def exists(rel):
    return (ROOT / rel).exists()

recipe_text = ''
for base in [
    ROOT / 'src/main/resources/data/thaumcraft/recipes',
    ROOT / 'src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench',
    ROOT / 'src/main/resources/data/thaumcraft/thaumcraft_alchemy',
    ROOT / 'src/main/resources/data/thaumcraft/thaumcraft_infusion',
]:
    if base.exists():
        for path in base.rglob('*.json'):
            if path.name == 'tc4_focushellbat.json':
                continue
            recipe_text += path.read_text(encoding='utf-8') + '\n'

matrix = read('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java')
runtime = read('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionRuntime.java')
node_hud = read('src/main/java/com/darkifov/thaumcraft/client/TC4RevealerHudAdapter.java')
helmet_overlay = read('src/main/java/com/darkifov/thaumcraft/client/HelmetRevealingOverlayEvents.java')
aura_block = read('src/main/java/com/darkifov/thaumcraft/block/AuraNodeBlock.java')
infusion_overlay = read('src/main/java/com/darkifov/thaumcraft/client/InfusionOverlayEvents.java')
book_page = read('src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java')

checks = {}
checks['version_3_62_0_or_newer'] = ('version="3.62.0"' in read('src/main/resources/META-INF/mods.toml') or 'version="3.82.0"' in read('src/main/resources/META-INF/mods.toml')) and ("version = '3.62.0'" in read('build.gradle') or "version = '3.82.0'" in read('build.gradle'))
checks['previous_stage342_marker_kept'] = 'version="3.42.0"' in read('src/main/resources/META-INF/mods.toml') and "version = '3.42.0'" in read('build.gradle')
checks['unused_duplicate_thaumometer_source_removed'] = not exists('src/main/java/com/darkifov/thaumcraft/item/ThaumometerItem.java')
checks['infusion_recipe_lock_adapter'] = all(token in matrix for token in [
    'lockedCatalystId', 'lockedRecipeStillMatchesCurrentPedestals', 'componentsHaveStartedTravelling', 'recipeobject', 'itemIdFor', 'ForgeRegistries.ITEMS.getKey'
])
checks['infusion_no_catalyst_only_fallback'] = 'do not fall back to catalyst-only lookup' in matrix and 'return null;' in matrix
checks['infusion_ordered_component_cycle'] = 'orderedComponentPullList' in runtime and 'ConfigRecipes component order' in runtime and 'new ArrayList<>(recipe.componentsFor(catalyst))' in runtime
checks['infusion_overlay_uses_live_pending_counts'] = all(token in infusion_overlay for token in ['pendingEssentiaAmount()', 'pendingComponentAmount()', 'currentInstability()'])
checks['revealer_hud_adapter_exists'] = all(token in node_hud for token in [
    'TC4 IRevealer/IGoggles node HUD', 'isRevealerStack', 'targetedNode', 'renderNodeHud', 'textures/aspects/'
])
checks['revealer_overlay_uses_adapter'] = 'TC4RevealerHudAdapter.isRevealer' in helmet_overlay and 'TC4RevealerHudAdapter.renderNodeHud' in helmet_overlay
checks['aura_node_scan_uses_real_items_not_string_contains'] = all(token in aura_block for token in [
    'stack.is(ThaumcraftMod.THAUMOMETER.get())', 'stack.is(ThaumcraftMod.GOGGLES_OF_REVEALING.get())', 'stack.is(ThaumcraftMod.HELMET_OF_REVEALING.get())'
]) and 'itemId.contains("thaumometer")' not in aura_block and 'itemId.contains("goggles")' not in aura_block
checks['thaumonomicon_no_modern_button_widgets'] = 'Button;' not in book_page and 'addRenderableWidget(new Button' not in book_page and 'renderOriginalNavigationHotspots' in book_page and 'mouseClicked' in book_page
checks['active_recipes_still_no_old_focus_mirrors_except_hellbat'] = 'thaumcraft:tc4_focus_' not in recipe_text
checks['active_recipes_still_no_old_shard_mirrors'] = 'thaumcraft:tc4_shard_' not in recipe_text
checks['stage_docs_exist'] = exists('docs/TC4_SUPER_MEGA_STAGE343_362_INFUSION_NODE_THAUMONOMICON.md') and exists('docs/NEXT_CHAT_PROMPT_STAGE362.md')
checks['stage_report_exists'] = exists('STAGE343_362_TC4_SUPER_MEGA_INFUSION_NODE_THAUMONOMICON_REPORT.json')

print(json.dumps(checks, indent=2, sort_keys=True))
failed = [name for name, passed in checks.items() if not passed]
if failed:
    print('FAILED:', ', '.join(failed), file=sys.stderr)
    sys.exit(1)
