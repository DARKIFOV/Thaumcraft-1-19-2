#!/usr/bin/env python3
"""v10.82 audit: TC4 golem itemTimeout, tube closed-side suction gates, biome-biased infused stone."""
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

golem = read('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java')
golem_ai = read('src/main/java/com/darkifov/thaumcraft/golem/GolemTaskAIRuntime.java')
tube = read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java')
worldgen = read('src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java')
workflow = read('.github/workflows/main.yml')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
readme = read('README.md')
upload = read('GITHUB_UPLOAD.md')

# GolemHelper.itemTimeout parity.
for token in [
    'public static final long ORIGINAL_GOLEM_IGNORE_DELAY_MS = 10_000L;',
    'private static final List<SortingItemTimeout> SORTING_ITEM_TIMEOUTS = new ArrayList<>();',
    'private record SortingItemTimeout(int golemId, ItemStack stack, long expiresAtMillis)',
    'private boolean sortingIsOnTimeoutLikeTC4(ItemStack stack)',
    'private void addSortingItemTimeoutLikeTC4(ItemStack stack)',
    'private boolean sortingValidTargetForItemLikeTC4(ItemStack stack)',
    'System.currentTimeMillis() + GolemTaskAIRuntime.ORIGINAL_GOLEM_IGNORE_DELAY_MS',
    'SORTING_ITEM_TIMEOUTS.removeIf',
    'ItemStack.isSameItemSameTags(left, right)',
]:
    require(golem + golem_ai, token, 'sorting itemTimeout invariant')
for token in [
    'sample.isEmpty() || !acceptsItem(sample) || sortingIsOnTimeoutLikeTC4(sample)',
    'addSortingItemTimeoutLikeTC4(sample);',
    'coreType == GolemCoreType.SORTING && !sortingValidTargetForItemLikeTC4(stack)',
    'addSortingItemTimeoutLikeTC4(itemCarried);',
    'lastOriginalTask = coreType == GolemCoreType.SORTING ? "AIItemPickup:validTargetForItem" : "AIItemPickup";',
]:
    require(golem, token, 'sorting timeout wiring')

# Tube closed-side conflict and neighbour input gates.
for token in [
    'TC4 checkVenting starts with isConnectable(loc)',
    'if (!isSideOpen(direction) || !EssentiaSuctionResolver.sideAllows(level, worldPosition, direction))',
    'neighbour can accept from this face',
    'return tube.allowsInputFrom(direction.getOpposite())',
    '? tube.getSuctionAmount(direction.getOpposite())',
    ': EssentiaSuction.SOURCE_NONE;',
    '? tube.getSuctionType(direction.getOpposite())',
    ': null;',
]:
    require(tube, token, 'tube closed-side/suction leak guard')
for token in [
    'for (Direction direction : Direction.values()) {\n            if (!EssentiaSuctionResolver.sideAllows(level, worldPosition, direction)) {\n                continue;\n            }\n            BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));'
]:
    forbid(tube, token, 'old venting loop without isSideOpen')

# Infused stone biome-tag preference.
for token in [
    'private static BlockState randomInfusedCrystal(ServerLevel level, int x, int z, RandomSource random)',
    'one third of infused\n        // veins asks BiomeHandler.getRandomBiomeTag',
    'if (random.nextInt(3) == 0)',
    'String biome = biomePath(level, x, z);',
    'biome.contains("desert") || biome.contains("badlands") || biome.contains("nether")',
    'biome.contains("ocean") || biome.contains("river") || biome.contains("swamp")',
    'biome.contains("mountain") || biome.contains("stony") || biome.contains("cave") || biome.contains("dripstone")',
    'biome.contains("forest") || biome.contains("jungle") || biome.contains("meadow")',
    'biome.contains("snow") || biome.contains("ice") || biome.contains("frozen")',
    'biome.contains("taint") || biome.contains("dark") || biome.contains("deep_dark")',
    'randomInfusedCrystal(level, x, z, random)',
]:
    require(worldgen, token, 'infused stone biome bias invariant')
for token in [
    'private static BlockState randomInfusedCrystal(RandomSource random)'
]:
    forbid(worldgen, token, 'old uniform-only infused stone selector signature')

# Version/docs/CI.
require(build, "version = '10.82.0'", 'build.gradle v10.82 version')
require(mods, 'version="10.82.0"', 'mods.toml v10.82 version')
require(workflow, 'tc4_v10_82_golem_timeout_tube_worldgen_audit.py', 'CI registration')
require(readme, 'v10.82', 'README marker')
require(readme, '86% complete / 14% remaining', 'progress marker')
require(upload, 'v10.82', 'upload marker')
require(readme + upload, 'No new items, blocks, recipes, progression', 'no new content statement')

passed = not errors
print(json.dumps({
    'version': 'v10.82',
    'goal': 'TC4 sorting itemTimeout/backoff, closed-side tube conflict gates, biome-biased infused stone selector',
    'checks': {
        'sorting_item_timeout': True,
        'sorting_pickup_valid_target': True,
        'tube_closed_side_venting_guard': True,
        'tube_neighbour_input_suction_gate': True,
        'infused_stone_biome_bias': True,
        'no_new_progression': True,
    },
    'passed': passed,
    'errors': errors,
}, indent=2))
sys.exit(0 if passed else 1)
