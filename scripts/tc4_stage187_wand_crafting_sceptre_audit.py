#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
read = lambda p: (ROOT / p).read_text(encoding='utf-8') if (ROOT / p).exists() else ''

errors = []
files = {
    'runtime': read('src/main/java/com/darkifov/thaumcraft/wand/WandCraftingRuntime.java'),
    'components': read('src/main/java/com/darkifov/thaumcraft/wand/WandComponentData.java'),
    'recipe': read('src/main/java/com/darkifov/thaumcraft/arcane/ArcaneWorkbenchRecipe.java'),
    'recipes': read('src/main/java/com/darkifov/thaumcraft/arcane/ArcaneWorkbenchRecipes.java'),
    'table': read('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java'),
    'wand': read('src/main/java/com/darkifov/thaumcraft/block/WandItem.java'),
    'build': read('build.gradle'),
    'mods': read('src/main/resources/META-INF/mods.toml'),
    'workflow': read('.github/workflows/main.yml'),
    'guard': read('scripts/github_ci_guard.py'),
}

checks = {
    'stage187_runtime_exists': bool(files['runtime']),
    'original_arcane_wand_recipe_source_named': 'ArcaneWandRecipe' in files['runtime'],
    'original_arcane_sceptre_recipe_source_named': 'ArcaneSceptreRecipe' in files['runtime'],
    'generated_wand_kind': 'TC4_ARCANE_WAND_RECIPE' in files['runtime'],
    'generated_sceptre_kind': 'TC4_ARCANE_SCEPTRE_RECIPE' in files['runtime'],
    'original_root_rod_cap_sceptre_nbt': all(token in files['components'] for token in ['ORIGINAL_TAG_ROD = "rod"', 'ORIGINAL_TAG_CAP = "cap"', 'ORIGINAL_TAG_SCEPTRE = "sceptre"']),
    'arcane_wand_pattern_slots': all(token in files['runtime'] for token in ['patternRow("  C")', 'patternRow(" R ")', 'patternRow("C  ")']),
    'arcane_sceptre_pattern_slots': all(token in files['runtime'] for token in ['patternRow("CR ")', 'patternRow("HC ")', 'tc4_charm']),
    'primal_wand_cost_formula': 'cap.craftCost() * rod.craftCost()' in files['runtime'],
    'primal_sceptre_cost_formula': 'cap.craftCost() * rod.craftCost() * 1.5F' in files['runtime'],
    'wood_iron_original_exclusion': 'rod == WandRodType.WOOD && cap == WandCapType.IRON' in files['runtime'],
    'generated_recipes_in_catalog': 'generatedWandAssemblyRecipes' in files['recipes'] and 'withWandAssembly' in files['recipes'],
    'table_generated_branch': 'tryCraftGeneratedWandAssembly' in files['table'] and 'matchesGeneratedAssembly' in files['table'],
    'slot_for_grid_adapter': 'slotForGrid(int row, int col)' in files['table'],
    'sceptre_cannot_focus': 'Original TC4 sceptres are crafting-only' in files['wand'],
    'version_188': "version = '1.94.0'" in files['build'] and 'version="1.94.0"' in files['mods'],
    'workflow_and_guard': 'tc4_stage187_wand_crafting_sceptre_audit.py' in files['workflow'] and 'tc4_stage187_wand_crafting_sceptre_audit.py' in files['guard'],
}

for name, ok in checks.items():
    if not ok:
        errors.append(name)

if errors:
    for error in errors:
        print(f'::error::Stage187 wand crafting/sceptre audit failed: {error}')
    sys.exit(1)
print('Stage187 wand crafting/sceptre audit: OK')
