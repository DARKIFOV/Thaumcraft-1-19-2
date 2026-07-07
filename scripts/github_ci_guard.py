#!/usr/bin/env python3
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github/workflows/main.yml"

REQUIRED_FILES = [
    "build.gradle",
    "settings.gradle",
    "gradle.properties",
    "gradlew",
    "gradlew.bat",
    "gradle/wrapper/gradle-wrapper.jar",
    "gradle/wrapper/gradle-wrapper.properties",
    ".gitattributes",
    ".github/workflows/main.yml",
    "scripts/java_syntax_guard.py",
    "scripts/github_static_audit.py",
    "scripts/tc4_stage144_eldritch_warp_taint_audit.py",
    "scripts/tc4_stage145_taint_output_texture_audit.py",
    "scripts/tc4_stage146_worldgen_resources_audit.py",
    "scripts/tc4_stage147_strict_original_parity_audit.py",
    "scripts/tc4_stage148_research_icon_parity_audit.py",
    "scripts/tc4_stage149_research_page_parity_audit.py",
    "scripts/tc4_stage150_research_metadata_parity_audit.py",
    "scripts/tc4_stage151_research_progression_audit.py",
    "scripts/tc4_stage152_recipe_unlock_parity_audit.py",
    "scripts/tc4_stage153_recipe_materialization_parity_audit.py",
    "scripts/tc4_stage154_infusion_enchantment_parity_audit.py",
    "scripts/tc4_stage156_bulk_recipe_materialization_audit.py",
    "scripts/tc4_stage155_recipe_resolver_audit.py",
    "scripts/tc4_stage157_object_entity_aspect_parity_audit.py",
    "scripts/tc4_stage158_thaumometer_scan_runtime_audit.py",
    "scripts/tc4_stage159_player_scan_knowledge_audit.py",
    "scripts/tc4_stage160_research_table_aspect_foundation_audit.py",
    "scripts/tc4_stage161_research_note_grid_parity_audit.py",
    "scripts/tc4_stage162_research_note_completion_parity_audit.py",
    "scripts/tc4_stage163_research_table_inventory_ink_audit.py",
    "scripts/tc4_stage164_research_note_gui_parity_audit.py",
    "scripts/tc4_stage165_research_table_block_entity_audit.py",
    "scripts/tc4_stage166_research_table_gui_copy_audit.py",
    "scripts/tc4_stage166_original_drift_audit.py",
    "scripts/tc4_stage167_gui_research_table_visual_audit.py",
    "scripts/tc4_stage168_research_dupe_copy_audit.py",
    "scripts/tc4_stage169_research_table_bonus_aspects_audit.py",
    "scripts/tc4_stage170_research_table_bonus_sync_audit.py",
    "scripts/tc4_stage172_wand_focus_cost_sync_audit.py",
    "scripts/tc4_stage171_wand_focus_behavior_audit.py",
    "scripts/tc4_stage173_focus_upgrade_nbt_audit.py",
    "scripts/tc4_stage174_focus_projectile_entity_audit.py",
    "scripts/tc4_stage175_focus_upgrade_effects_audit.py",
    "scripts/tc4_stage176_focus_projectile_visuals_audit.py",
    "scripts/tc4_stage177_focus_architect_area_audit.py",
    "scripts/tc4_stage178_projectile_behavior_audit.py",
    "scripts/tc4_stage180_continuous_focus_use_audit.py",
    "scripts/tc4_stage181_focus_client_fx_audit.py",
    "scripts/tc4_stage182_focus_animation_audit.py",
    "scripts/tc4_stage183_focus_renderer_layers_audit.py",
    "scripts/tc4_stage184_remaining_focus_behavior_audit.py",
    "scripts/tc4_stage185_wand_component_renderer_audit.py",
    "scripts/tc4_stage186_focus_pouch_gui_audit.py",
    "scripts/tc4_stage187_wand_crafting_sceptre_audit.py",
    "scripts/tc4_stage188_focus_selection_packet_audit.py",
    "scripts/tc4_stage189_arcane_workbench_gui_audit.py",
    "scripts/tc4_stage190_wand_configrecipes_audit.py",
    "scripts/tc4_stage191_arcane_slot_edge_cases_audit.py",
    "scripts/tc4_stage192_wand_focus_regression_audit.py",
    "scripts/tc4_stage194_full_port_drift_ledger_audit.py",
    "scripts/tc4_stage193_arcane_cleanup_audit.py",
    "scripts/tc4_stage179_architect_client_overlay_audit.py",
    "scripts/tc4_stage195_golem_core_ai_audit.py",
    "scripts/tc4_stage196_essentia_suction_audit.py",
    "scripts/tc4_stage198_tube_subclass_audit.py",
    "scripts/tc4_stage197_golem_task_ai_audit.py",
    "scripts/tc4_stage199_golem_bell_marker_audit.py",
    "scripts/tc4_stage200_tube_jar_renderer_resource_audit.py",
    "scripts/tc4_stage201_golem_gui_container_audit.py",
    "scripts/tc4_stage202_jar_tube_interaction_audit.py",
    "scripts/tc4_stage203_golem_ghost_slot_audit.py",
]

REQUIRED_WORKFLOW_SNIPPETS = [
    "actions/checkout@v4",
    "actions/setup-java@v4",
    "distribution: temurin",
    "java-version: \"17\"",
    "gradle/actions/setup-gradle@v4",
    "chmod +x ./gradlew",
    "./gradlew --no-daemon clean build verifyJarResources verifyGithubOutputJarResources copyGithubOutputJar --stacktrace",
    "verifyJarResources",
    "verifyGithubOutputJarResources",
    "copyGithubOutputJar",
    "actions/upload-artifact@v4",
    "python scripts/java_syntax_guard.py",
    "python scripts/github_static_audit.py",
    "python scripts/tc4_stage144_eldritch_warp_taint_audit.py",
    "python scripts/tc4_stage145_taint_output_texture_audit.py",
    "python scripts/tc4_stage146_worldgen_resources_audit.py",
    "python scripts/tc4_stage147_strict_original_parity_audit.py",
    "python scripts/tc4_stage148_research_icon_parity_audit.py",
    "python scripts/tc4_stage149_research_page_parity_audit.py",
    "python scripts/tc4_stage150_research_metadata_parity_audit.py",
    "python scripts/tc4_stage151_research_progression_audit.py",
    "python scripts/tc4_stage152_recipe_unlock_parity_audit.py",
    "python scripts/tc4_stage153_recipe_materialization_parity_audit.py",
    "python scripts/tc4_stage154_infusion_enchantment_parity_audit.py",
    "python scripts/tc4_stage156_bulk_recipe_materialization_audit.py",
    "python scripts/tc4_stage155_recipe_resolver_audit.py",
    "python scripts/tc4_stage157_object_entity_aspect_parity_audit.py",
    "python scripts/tc4_stage158_thaumometer_scan_runtime_audit.py",
    "python scripts/tc4_stage159_player_scan_knowledge_audit.py",
    "python scripts/tc4_stage160_research_table_aspect_foundation_audit.py",
    "python scripts/tc4_stage162_research_note_completion_parity_audit.py",
    "python scripts/tc4_stage161_research_note_grid_parity_audit.py",
    "python scripts/tc4_stage163_research_table_inventory_ink_audit.py",
    "python scripts/tc4_stage164_research_note_gui_parity_audit.py",
    "python scripts/tc4_stage165_research_table_block_entity_audit.py",
    "python scripts/tc4_stage166_research_table_gui_copy_audit.py",
    "python scripts/tc4_stage166_original_drift_audit.py",
    "python scripts/tc4_stage167_gui_research_table_visual_audit.py",
    "python scripts/tc4_stage168_research_dupe_copy_audit.py",
    "python scripts/tc4_stage169_research_table_bonus_aspects_audit.py",
    "python scripts/tc4_stage170_research_table_bonus_sync_audit.py",
    "python scripts/tc4_stage172_wand_focus_cost_sync_audit.py",
    "python scripts/tc4_stage171_wand_focus_behavior_audit.py",
    "python scripts/tc4_stage173_focus_upgrade_nbt_audit.py",
    "python scripts/tc4_stage174_focus_projectile_entity_audit.py",
    "python scripts/tc4_stage175_focus_upgrade_effects_audit.py",
    "python scripts/tc4_stage176_focus_projectile_visuals_audit.py",
    "python scripts/tc4_stage177_focus_architect_area_audit.py",
    "python scripts/tc4_stage178_projectile_behavior_audit.py",
    "python scripts/tc4_stage180_continuous_focus_use_audit.py",
    "python scripts/tc4_stage181_focus_client_fx_audit.py",
    "python scripts/tc4_stage182_focus_animation_audit.py",
    "python scripts/tc4_stage183_focus_renderer_layers_audit.py",
    "python scripts/tc4_stage184_remaining_focus_behavior_audit.py",
    "python scripts/tc4_stage185_wand_component_renderer_audit.py",
    "python scripts/tc4_stage186_focus_pouch_gui_audit.py",
    "python scripts/tc4_stage187_wand_crafting_sceptre_audit.py",
    "python scripts/tc4_stage188_focus_selection_packet_audit.py",
    "python scripts/tc4_stage189_arcane_workbench_gui_audit.py",
    "python scripts/tc4_stage190_wand_configrecipes_audit.py",
    "python scripts/tc4_stage191_arcane_slot_edge_cases_audit.py",
    "python scripts/tc4_stage192_wand_focus_regression_audit.py",
    "python scripts/tc4_stage194_full_port_drift_ledger_audit.py",
    "python scripts/tc4_stage193_arcane_cleanup_audit.py",
    "python scripts/tc4_stage179_architect_client_overlay_audit.py",
    "python scripts/tc4_stage195_golem_core_ai_audit.py",
    "python scripts/tc4_stage196_essentia_suction_audit.py",
    "python scripts/tc4_stage198_tube_subclass_audit.py",
    "python scripts/tc4_stage197_golem_task_ai_audit.py",
    "python scripts/tc4_stage199_golem_bell_marker_audit.py",
    "python scripts/tc4_stage200_tube_jar_renderer_resource_audit.py",
    "python scripts/tc4_stage201_golem_gui_container_audit.py",
    "python scripts/tc4_stage202_jar_tube_interaction_audit.py",
    "python scripts/tc4_stage203_golem_ghost_slot_audit.py",
    "thaumcraft-legacy-rebuild-stage204-jars",
]

errors: list[str] = []

for rel in REQUIRED_FILES:
    if not (ROOT / rel).exists():
        errors.append(f"missing GitHub build file: {rel}")

workflow_text = WORKFLOW.read_text(encoding="utf-8") if WORKFLOW.exists() else ""
for snippet in REQUIRED_WORKFLOW_SNIPPETS:
    if snippet not in workflow_text:
        errors.append(f"workflow missing required snippet: {snippet}")

wrapper_props = ROOT / "gradle/wrapper/gradle-wrapper.properties"
if wrapper_props.exists() and "gradle-7.5.1-bin.zip" not in wrapper_props.read_text(encoding="utf-8"):
    errors.append("Gradle wrapper should be pinned to gradle-7.5.1-bin.zip for Forge 1.19.2")

build_gradle = ROOT / "build.gradle"
if build_gradle.exists():
    build_text = build_gradle.read_text(encoding="utf-8")
    if "version '5.1.76'" not in build_text:
        errors.append("ForgeGradle must be pinned to 5.1.76")
    if "1.19.2-43.5.2" not in build_text:
        errors.append("Forge dependency should stay pinned to 1.19.2-43.5.2")
    if "version = '2.04.0'" not in build_text:
        errors.append("Project version should be 2.04.0 for Stage204")

mods_toml = ROOT / "src/main/resources/META-INF/mods.toml"
if mods_toml.exists() and 'version="2.04.0"' not in mods_toml.read_text(encoding="utf-8"):
    errors.append("mods.toml should be version=\"1.51.0\" for Stage152")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("GitHub CI guard: OK")
