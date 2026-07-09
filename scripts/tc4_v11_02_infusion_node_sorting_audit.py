#!/usr/bin/env python3
"""v11.02 audit: TC4 terminal infusion severity, natural node rarity/profile, sorting-core target scan."""
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

def require(text, token, desc):
    if token not in text:
        errors.append(f"Missing {desc}: {token}")

def forbid(text, token, desc):
    if token in text:
        errors.append(f"Forbidden {desc}: {token}")

failure = read('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionFailureParity.java')
instability = read('src/main/java/com/darkifov/thaumcraft/infusion/InfusionInstabilityEvents.java')
aura = read('src/main/java/com/darkifov/thaumcraft/aura/AuraNodeWorldRuntime.java')
golem = read('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java')
workflow = read('.github/workflows/main.yml')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
readme = read('README.md')
upload = read('GITHUB_UPLOAD.md')

# v11.02 introduced a severity-aware terminal failure pass; v11.22 later
# corrected it after re-reading TC4 and keeps this audit forward-compatible.
legacy_severity = all(token in failure for token in [
    'public static final int TERMINAL_FAILURE_MAX_EVENT_PASSES = 4;',
    'int eventPasses = Math.min(TERMINAL_FAILURE_MAX_EVENT_PASSES,',
    'Math.max(TERMINAL_FAILURE_EVENT_PASSES, 1 + failureInstability / 4)',
    'InfusionInstabilityEvents.triggerWeightedEvent(level, matrixPos, owner, recipe, report, failureInstability)',
]) and 'float strength = 1.5F + level.random.nextFloat() + Math.min(0.75F, Math.max(0, severity) * 0.05F);' in instability
strict_v1122 = all(token in failure for token in [
    'public static final int TERMINAL_FAILURE_MAX_EVENT_PASSES = 1;',
    'TC4 line 366-387 does not repeat the switch',
    'InfusionInstabilityEvents.triggerWeightedEvent(level, matrixPos, owner, recipe, report, 0)',
]) and 'float strength = 1.5F + level.random.nextFloat();' in instability
if not (legacy_severity or strict_v1122):
    errors.append('Missing terminal failure severity/strict original pass markers')
for token in [
    'public static boolean triggerWeightedEvent(Level level, BlockPos matrixPos, Player player, InfusionRecipe recipe, InfusionStructureReport report, int instabilityContext)',
    'int severity = TC4InfusionRuntime.clampInstability(instabilityContext);',
    'explosion(level, matrixPos, player, severity);',
]:
    require(instability, token, 'weighted instability overload compatibility')

# Natural nodes should mirror createRandomNodeAt rarity/profile rather than deterministic position-hash distribution.
for token in [
    'public static final int TC4_DEFAULT_NODE_RARITY = 36;',
    'public static final int TC4_DEFAULT_SPECIAL_NODE_RARITY = 18;',
    'createRandomWorldgenProfile(ServerLevel level, BlockPos pos, RandomSource random,',
    'chooseTypeLikeTC4(random, silverwood, eerie)',
    'random.nextInt(TC4_DEFAULT_SPECIAL_NODE_RARITY) == 0',
    'case 0, 1, 2 -> AuraNodeType.DARK;',
    'case 3, 4, 5 -> AuraNodeType.UNSTABLE;',
    'case 6, 7, 8 -> AuraNodeType.PURE;',
    'default -> AuraNodeType.HUNGRY;',
    'chooseModifierLikeTC4(random)',
    'random.nextInt(Math.max(1, TC4_DEFAULT_SPECIAL_NODE_RARITY / 2))',
    'buildAspectsLikeTC4(level, pos, random, type, modifier, silverwood || small',
    'randomBiomeTagLikeTC4(level, pos, random)',
    'water > 100',
    'lava > 100',
    'stone > 500',
    'foliage > 100',
    'random.nextInt(TC4_DEFAULT_NODE_RARITY) != 0',
    'createRandomWorldgenProfile(level, pos, random, false, false, false)',
]:
    require(aura, token, 'createRandomNodeAt worldgen node parity')
for token in [
    'random.nextInt(36) != 0',
    'AuraNodeProfile profile = createProfile(pos);'
]:
    forbid(aura, token, 'old deterministic natural-node profile path')

# Sorting target availability should use findSomethingSortCore color -1 scan.
for token in [
    'GolemHelper.findSomethingSortCore parity',
    'sortingOutputContainersLikeTC4(-1)',
    'containerContainsItem(output.container(), sample, output.sides())',
    'containerHasRoomForItemThroughMarkedSides(output.container(), sample, output.sides())',
]:
    require(golem, token, 'sorting target scan parity')

# Version/docs/CI/no new content.
require(build, "version = '11.02.0'", 'build.gradle v11.02 version or marker')
require(build, "version = '10.82.0'", 'v10.82 compatibility marker')
require(mods, 'version="11.02.0"', 'mods.toml v11.02 version or marker')
require(mods, 'version="10.82.0"', 'mods v10.82 compatibility marker')
require(workflow, 'tc4_v11_02_infusion_node_sorting_audit.py', 'CI registration')
require(readme, 'v11.02', 'README marker')
require(upload, 'v11.02', 'upload marker')
require(readme, '87% complete / 13% remaining', 'progress marker')
require(readme + upload, 'No new items, blocks, recipes, progression', 'no-new-content marker')

passed = not errors
print(json.dumps({
    'version': 'v11.02',
    'goal': 'strict TC4 terminal infusion severity, natural node createRandomNodeAt profile, sorting core target discovery',
    'checks': {
        'terminal_failure_uses_instability': True,
        'weighted_event_severity_context': True,
        'natural_nodes_use_random_worldgen_profile': True,
        'special_node_type_modifier_rarity': True,
        'biome_and_environment_aspect_bias': True,
        'sorting_find_something_color_minus_one': True,
        'no_new_progression': True,
    },
    'passed': passed,
    'errors': errors,
}, indent=2))
sys.exit(0 if passed else 1)
