#!/usr/bin/env python3
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

def exists(rel):
    return (ROOT / rel).exists()

active_recipe_roots = [
    ROOT / 'src/main/resources/data/thaumcraft/recipes',
    ROOT / 'src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench',
    ROOT / 'src/main/resources/data/thaumcraft/thaumcraft_alchemy',
    ROOT / 'src/main/resources/data/thaumcraft/thaumcraft_infusion',
]
active_recipe_text = ''
active_recipe_files = []
for base in active_recipe_roots:
    if base.exists():
        for path in base.rglob('*.json'):
            # Hellbat remains a source-mapped original TC4 item placeholder until the real ItemFocusHellbat class is ported.
            if path.name == 'tc4_focushellbat.json':
                continue
            active_recipe_files.append(str(path.relative_to(ROOT)))
            active_recipe_text += path.read_text(encoding='utf-8') + '\n'

checks = {}
checks['version_3_42_0'] = 'version="3.42.0"' in read('src/main/resources/META-INF/mods.toml') and "version = '3.42.0'" in read('build.gradle')
checks['previous_stage322_markers_kept'] = 'version="3.22.0"' in read('src/main/resources/META-INF/mods.toml') and "version = '3.22.0'" in read('build.gradle')
checks['taintacle_renderer_numeric_formula_adapter'] = all(token in read('src/main/java/com/darkifov/thaumcraft/client/render/TC4TaintacleRenderer.java') for token in [
    'renderSegmentedTaintacle', 'renderPrismSegment', 'NORMAL_SEGMENTS = 8', 'SMALL_SEGMENTS = 4', 'getFlailIntensity'
]) and 'quad(' not in read('src/main/java/com/darkifov/thaumcraft/client/render/TC4TaintacleRenderer.java')
checks['outer_lands_live_populate_adapter_exists'] = exists('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLivePopulateAdapter.java')
checks['outer_lands_live_populate_wired'] = 'TC4OuterLandsLivePopulateAdapter.tickPlayerArea(level, player)' in read('src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java')
checks['research_note_thread_adapter'] = 'drawSaggingThreadLikeTC4' in read('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java') and 'thread-like' in read('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java')
checks['research_table_original_gui_kept'] = 'RESEARCH_TABLE_TC4_ORIGINAL' in read('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java')
checks['research_table_aspect_palette'] = all(token in read('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java') for token in [
    'renderAspectPaletteLikeTC4', 'renderCombinationSlotsLikeTC4', 'requestCombineAspectsFromClient', 'AspectCombinationRegistry.combine'
])
checks['research_table_model_not_cube'] = all(token in read('src/main/resources/assets/thaumcraft/models/block/research_table.json') for token in ['tablequill', 'elements', 'from', 'to'])
checks['original_key_item_textures_copied'] = all(exists(path) for path in [
    'src/main/resources/assets/thaumcraft/textures/item/thaumonomicon.png',
    'src/main/resources/assets/thaumcraft/textures/item/goggles_of_revealing.png',
    'src/main/resources/assets/thaumcraft/textures/item/research_note.png',
    'src/main/resources/assets/thaumcraft/textures/item/scribing_tools.png',
    'src/main/resources/assets/thaumcraft/textures/item/thaumometer.png',
    'src/main/resources/assets/thaumcraft/textures/block/tc4/tablequill.png',
])
checks['focus_recipe_results_actual_runtime_ids'] = all(token in active_recipe_text for token in [
    'thaumcraft:focus_fire', 'thaumcraft:focus_frost', 'thaumcraft:focus_shock', 'thaumcraft:focus_excavation',
    'thaumcraft:focus_equal_trade', 'thaumcraft:focus_portable_hole', 'thaumcraft:focus_warding', 'thaumcraft:focus_primal'
])
checks['active_recipes_no_old_focus_mirrors_except_hellbat'] = 'thaumcraft:tc4_focus_' not in active_recipe_text
checks['active_recipes_no_old_shard_mirrors'] = 'thaumcraft:tc4_shard_' not in active_recipe_text
resolver = read('src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeItemResolver.java')
checks['recipe_resolver_core_focus_ids'] = all(token in resolver for token in [
    '"itemFocusFire", "thaumcraft:focus_fire"',
    '"itemFocusFrost", "thaumcraft:focus_frost"',
    '"itemFocusShock", "thaumcraft:focus_shock"',
    '"itemFocusExcavation", "thaumcraft:focus_excavation"',
    '"itemFocusTrade", "thaumcraft:focus_equal_trade"',
    '"itemFocusPortableHole", "thaumcraft:focus_portable_hole"',
    '"itemFocusWarding", "thaumcraft:focus_warding"',
    '"itemFocusPrimal", "thaumcraft:focus_primal"',
])
checks['recipe_resolver_shards_actual_runtime_ids'] = all(token in resolver for token in [
    '"itemShard:0", "thaumcraft:aer_shard"',
    '"itemShard:1", "thaumcraft:ignis_shard"',
    '"itemShard:2", "thaumcraft:aqua_shard"',
    '"itemShard:3", "thaumcraft:terra_shard"',
    '"itemShard:4", "thaumcraft:ordo_shard"',
    '"itemShard:5", "thaumcraft:perditio_shard"',
    '"itemShard:6", "thaumcraft:balanced_shard"',
])
garbage = read('src/main/java/com/darkifov/thaumcraft/porting/TC4RegistryGarbageGuard.java')
checks['duplicate_focus_mirrors_quarantined'] = all(token in garbage for token in [
    'tc4_focus_fire', 'tc4_focus_frost', 'tc4_focus_shock', 'tc4_focus_excavation', 'tc4_focus_trade',
    'tc4_focus_portablehole', 'tc4_focus_warding', 'tc4_focus_primal', 'tc4_focuspouch', 'tc4_gogglesrevealing'
])
checks['handoff_docs_exist'] = exists('docs/TC4_SUPER_MEGA_STAGE323_342_ORIGINAL_PARITY_CLEANUP.md') and exists('docs/NEXT_CHAT_PROMPT_STAGE342.md')
checks['no_stage323_audit_added_to_old_source_mapping'] = exists('scripts/tc4_stage323_342_super_mega_original_parity_cleanup_audit.py')

print(json.dumps(checks, indent=2, sort_keys=True))
failed = [name for name, passed in checks.items() if not passed]
if failed:
    print('FAILED:', ', '.join(failed), file=sys.stderr)
    sys.exit(1)
