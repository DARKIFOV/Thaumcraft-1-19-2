#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
read = lambda p: (ROOT / p).read_text(encoding='utf-8') if (ROOT / p).exists() else ''

files = {
    'index': read('src/main/java/com/darkifov/thaumcraft/wand/TC4ConfigRecipesWandIndex.java'),
    'recipes': read('src/main/java/com/darkifov/thaumcraft/arcane/ArcaneWorkbenchRecipes.java'),
    'crafting': read('src/main/java/com/darkifov/thaumcraft/wand/WandCraftingRuntime.java'),
    'components': read('src/main/java/com/darkifov/thaumcraft/wand/WandComponentData.java'),
    'be': read('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java'),
    'build': read('build.gradle'),
    'mods': read('src/main/resources/META-INF/mods.toml'),
    'workflow': read('.github/workflows/main.yml'),
    'guard': read('scripts/github_ci_guard.py'),
}

checks = {
    'configrecipes_index_exists': 'class TC4ConfigRecipesWandIndex' in files['index'],
    'original_source_lines_named': 'ConfigRecipes lines 1058-1215' in files['index'] and '1051-1055' in files['index'],
    'component_kind': 'TC4_CONFIGRECIPES_WAND_COMPONENT' in files['index'],
    'cap_recipes_materialized': all(token in files['index'] for token in [
        'WandCapGold', 'WandCapCopper', 'WandCapSilverInert', 'WandCapThaumiumInert', 'WandCapVoidInert'
    ]),
    'rod_recipes_materialized': all(token in files['index'] for token in [
        'WandRodGreatwood', 'WandRodGreatwoodStaff', 'WandRodObsidianStaff', 'WandRodSilverwoodStaff', 'WandRodBlazeStaff', 'WandRodBoneStaff'
    ]),
    'exact_config_costs': all(token in files['index'] for token in [
        'Aspect.ORDO', 'Aspect.IGNIS', 'Aspect.AER', 'Aspect.PERDITIO', 'cost * 3', 'cost * 2'
    ]),
    'recipe_chain_includes_components_before_assembly': 'TC4ConfigRecipesWandIndex.generatedArcaneComponentRecipes()' in files['recipes'] and 'WandCraftingRuntime.generatedWandAssemblyRecipes()' in files['recipes'],
    'assembly_result_uses_root_nbt': all(token in files['crafting'] + files['components'] for token in ['ORIGINAL_TAG_ROD', 'ORIGINAL_TAG_CAP', 'ORIGINAL_TAG_SCEPTRE', 'root.putString']),
    'prebuilt_shortcuts_marked_drift': 'PREBUILT_WAND_SHORTCUTS_ARE_DRIFT_ADAPTER' in files['index'] and 'compatibility shortcuts' in files['index'],
    'generated_assembly_preview_used': 'WandCraftingRuntime.resultFor(recipe, this)' in files['be'],
    'version_190': ("version = '2.04.0'" in files['build'] or "version = '1.98.0'" in files['build'] or "version = '2.00.0'" in files['build']) and ('version="2.04.0"' in files['mods'] or 'version="1.98.0"' in files['mods'] or 'version="2.00.0"' in files['mods'] or 'version="2.02.0"' in files['mods']),
    'workflow_and_guard': 'tc4_stage190_wand_configrecipes_audit.py' in files['workflow'] and 'tc4_stage190_wand_configrecipes_audit.py' in files['guard'],
    'stage190_artifact': 'thaumcraft-legacy-rebuild-stage204-jars' in files['workflow'] and 'thaumcraft-legacy-rebuild-stage204-jars' in files['guard'],
}

errors = [name for name, ok in checks.items() if not ok]
if errors:
    for error in errors:
        print(f'::error::Stage190 wand ConfigRecipes audit failed: {error}')
    sys.exit(1)
print('Stage190 wand ConfigRecipes audit: OK')
