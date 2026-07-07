#!/usr/bin/env python3
from __future__ import annotations

import json
import re
import sys
from pathlib import Path
from collections import Counter

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

def exists(rel: str) -> bool:
    return (ROOT / rel).exists()

spec_path = ROOT / 'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_original_recipe_requirements_stage152.json'
requirements_spec = json.loads(spec_path.read_text(encoding='utf-8')) if spec_path.exists() else {}
requirements = requirements_spec.get('requirements', [])
page_types = Counter(r.get('page_type') for r in requirements)
resolutions = Counter(r.get('resolution') for r in requirements)
required_research = [r.get('required_research', '') for r in requirements]
recipe_keys = [r.get('recipe_key', '') for r in requirements]

index_java = read('src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeRequirementIndex.java')
page_screen = read('src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java')
arcane_block_entity = read('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java')
arcane_menu = read('src/main/java/com/darkifov/thaumcraft/menu/ArcaneWorkbenchMenu.java')
infusion_recipe = read('src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipe.java')
infusion_matrix = read('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java')
alchemy_recipe = read('src/main/java/com/darkifov/thaumcraft/alchemy/AlchemyRecipe.java')
crucible_block = read('src/main/java/com/darkifov/thaumcraft/block/CrucibleBlock.java')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')
status = read('docs/ORIGINAL_TC4_PORTING_STATUS.md')
prompt = read('docs/NEXT_CHAT_PROMPT_STAGE152.md')

checks = {
    'version_stage152': any((f"version = '{v}'" in build and f'version="{v}"' in mods) for v in ["2.04.0", "2.02.0", "2.00.0", "1.98.0", "1.78.0", "1.76.0", "1.70.0", "1.65.0", "1.64.0", "1.63.0", "1.62.0", "1.61.0", "1.60.0", "1.59.0", "1.58.0", "1.57.0", "1.56.0", "1.55.0", "1.54.0", "1.53.0", "1.52.0"]),
    'stage152_spec_exists': spec_path.exists(),
    'stage152_spec_counts': requirements_spec.get('page_recipe_slots') == 281 and requirements_spec.get('unique_recipe_keys') == 280 and len(requirements) == 281,
    'stage152_spec_no_blank_required_research': bool(requirements) and all(required_research),
    'stage152_spec_expected_page_types': page_types.get('ARCANE_CRAFTING') == 90 and page_types.get('INFUSION_CRAFTING') == 63 and page_types.get('CRUCIBLE_CRAFTING') == 49 and page_types.get('NORMAL_CRAFTING') == 45,
    'stage152_spec_resolves_runtime_bridge': resolutions.get('tc4_recipe_runtime_bridge', 0) >= 270,
    'stage152_spec_handles_dynamic_research_group': 'RESEARCH:SCEPTRE' in recipe_keys and resolutions.get('research_dynamic_group', 0) >= 1,
    'stage152_index_class_exists': exists('src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeRequirementIndex.java'),
    'stage152_index_has_unlock_api': 'requiredResearchForRuntimeRecipe' in index_java and 'isRuntimeRecipeUnlocked' in index_java and 'recipesUnlockedBy' in index_java,
    'stage152_research_pages_use_recipe_gate': 'TC4RecipeRequirementIndex.requiredResearchFor(recipeKey, entry.key())' in page_screen and 'Locked original TC4 recipe' in page_screen,
    'stage152_arcane_runtime_uses_original_gate': 'TC4RecipeRequirementIndex.requiredResearchForRuntimeRecipe(recipe.tc4Key(), recipe.research())' in arcane_block_entity and 'craftFromOutput(player)' in arcane_menu,
    'stage152_infusion_keeps_tc4_key_and_gate': 'private final String tc4Key;' in infusion_recipe and 'json.has("tc4_key")' in infusion_recipe and 'TC4RecipeRequirementIndex.requiredResearchForRuntimeRecipe(recipe.tc4Key(), recipe.research())' in infusion_matrix,
    'stage152_crucible_keeps_tc4_key_and_direct_gate': 'private final String tc4Key;' in alchemy_recipe and 'json.has("tc4_key")' in alchemy_recipe and 'TC4RecipeRequirementIndex.requiredResearchForRuntimeRecipe(catalystRecipe.tc4Key(), catalystRecipe.research())' in crucible_block,
    'stage152_json_preserved_in_jar_resource_check': 'tc4_original_recipe_requirements_stage152.json' in build,
    'stage152_workflow_and_guard_run_audit': 'tc4_stage152_recipe_unlock_parity_audit.py' in workflow and 'tc4_stage152_recipe_unlock_parity_audit.py' in guard and any(name in workflow for name in ['thaumcraft-legacy-rebuild-stage204-jars', 'thaumcraft-legacy-rebuild-stage165-jars', 'thaumcraft-legacy-rebuild-stage164-jars', 'thaumcraft-legacy-rebuild-stage163-jars', 'thaumcraft-legacy-rebuild-stage162-jars', 'thaumcraft-legacy-rebuild-stage161-jars', 'thaumcraft-legacy-rebuild-stage160-jars', 'thaumcraft-legacy-rebuild-stage159-jars', 'thaumcraft-legacy-rebuild-stage158-jars', 'thaumcraft-legacy-rebuild-stage155-jars', 'thaumcraft-legacy-rebuild-stage154-jars', 'thaumcraft-legacy-rebuild-stage153-jars', 'thaumcraft-legacy-rebuild-stage152-jars']) and any(name in guard for name in ['thaumcraft-legacy-rebuild-stage204-jars', 'thaumcraft-legacy-rebuild-stage165-jars', 'thaumcraft-legacy-rebuild-stage164-jars', 'thaumcraft-legacy-rebuild-stage163-jars', 'thaumcraft-legacy-rebuild-stage162-jars', 'thaumcraft-legacy-rebuild-stage161-jars', 'thaumcraft-legacy-rebuild-stage160-jars', 'thaumcraft-legacy-rebuild-stage159-jars', 'thaumcraft-legacy-rebuild-stage158-jars', 'thaumcraft-legacy-rebuild-stage155-jars', 'thaumcraft-legacy-rebuild-stage154-jars', 'thaumcraft-legacy-rebuild-stage153-jars', 'thaumcraft-legacy-rebuild-stage152-jars']),
    'stage152_status_and_prompt_updated': 'Stage152' in status and ('Stage153' in prompt or 'Stage154' in prompt or 'Stage182' in status) and any(v in status for v in ['8–31','8-31','10–33','10-33','40-65','39-64']),
}

failed = [name for name, ok in checks.items() if not ok]
if failed:
    for name in failed:
        print(f'::error::Stage152 recipe unlock parity audit failed: {name}')
    print(json.dumps({
        'page_types': dict(page_types),
        'resolutions': dict(resolutions),
        'spec_counts': {k: requirements_spec.get(k) for k in ['page_recipe_slots', 'unique_recipe_keys']},
    }, indent=2))
    sys.exit(1)

print('Stage152 recipe unlock parity audit: OK')
print(json.dumps({
    'page_recipe_slots': requirements_spec.get('page_recipe_slots'),
    'unique_recipe_keys': requirements_spec.get('unique_recipe_keys'),
    'page_types': dict(page_types),
    'resolutions': dict(resolutions),
    'remaining_stage_estimate_after_stage152': '40-65 stages',
}, indent=2))
