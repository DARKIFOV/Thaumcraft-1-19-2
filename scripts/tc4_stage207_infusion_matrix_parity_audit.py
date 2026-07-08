#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

def exists(rel):
    return (ROOT / rel).exists()

errors = []
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
matrix = read('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java')
block = read('src/main/java/com/darkifov/thaumcraft/block/InfusionMatrixBlock.java')
structure = read('src/main/java/com/darkifov/thaumcraft/infusion/InfusionAltarStructure.java')
report = read('src/main/java/com/darkifov/thaumcraft/infusion/InfusionStructureReport.java')
helper = read('src/main/java/com/darkifov/thaumcraft/infusion/InfusionProcessHelper.java')
runtime = read('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionRuntime.java')
overlay = read('src/main/java/com/darkifov/thaumcraft/client/InfusionOverlayEvents.java')

version = re.search(r"version\s*=\s*['\"]([0-9]+)\.([0-9]+)\.([0-9]+)['\"]", build)
if not version or tuple(map(int, version.groups())) < (2, 7, 0):
    errors.append('Stage207 build.gradle version must be 2.07.0 or later')
mod_version = re.search(r'version="([0-9]+)\.([0-9]+)\.([0-9]+)"', mods)
if not mod_version or tuple(map(int, mod_version.groups())) < (2, 7, 0):
    errors.append('Stage207 mods.toml version must be 2.07.0 or later')

checks = {
    'two_phase_active_field': 'private boolean active = false;' in matrix and 'private boolean crafting = false;' in matrix,
    'tc4_wand_two_phase_method': 'onWandRightClick(Player player)' in matrix and 'activateMatrix(player)' in matrix and 'return startInfusion(player);' in matrix,
    'strict_valid_location_activation': 'report.strictTc4Location()' in matrix and 'center pedestal and four infusion pillars' in matrix,
    'crafting_state_set_on_start': 'crafting = true;' in matrix and 'craftCount++' in matrix,
    'tc4_original_nbt_keys': 'tag.putBoolean("active", active)' in matrix and 'tag.putBoolean("crafting", crafting)' in matrix and 'tag.putShort("instability"' in matrix and 'tag.putInt("recipeinst"' in matrix,
    'craft_state_keeps_active_after_finish_fail': 'clearCraftingState(true)' in matrix and 'active = keepActive && active;' in matrix,
    'component_travel_delay': 'private ResourceLocation travellingComponent' in matrix and 'TravellingComponent' in matrix and 'ITEM_PULL_DELAY' in matrix,
    'tc4_cycle_delay_not_drain_range_delay': 'CRAFT_CYCLE_DELAY' in runtime and 'cycleDelay = TC4InfusionRuntime.CRAFT_CYCLE_DELAY' in matrix,
    'essentia_range_12': 'ESSENTIA_DRAIN_RANGE = 12' in runtime and 'RADIUS = TC4InfusionRuntime.ESSENTIA_DRAIN_RANGE' in helper,
    'symmetry_penalty_reported': 'originalSymmetryPenalty' in report and 'tc4Penalty,' in structure,
    'matrix_block_uses_wand_two_phase': 'matrix.onWandRightClick(player);' in block,
    'overlay_only_while_crafting': 'matrix.crafting()' in overlay,
    'original_infuser_texture_active': exists('src/main/resources/assets/thaumcraft/textures/models/infuser.png'),
    'stage207_next_prompt': exists('docs/NEXT_CHAT_PROMPT_STAGE207.md'),
    'stage207_notes': exists('docs/TC4_INFUSION_MATRIX_PARITY_STAGE207.md'),
    'stage207_report': exists('STAGE207_TC4_INFUSION_MATRIX_PARITY_START_REPORT.json'),
}

for name, ok in checks.items():
    if not ok:
        errors.append(f'Missing Stage207 infusion matrix parity marker: {name}')

if errors:
    for error in errors:
        print(f'::error::{error}')
    sys.exit(1)

print('Stage207 infusion matrix parity audit OK')
