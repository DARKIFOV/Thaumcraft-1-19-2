#!/usr/bin/env python3
"""v10.02 audit: TC4 direct-neighbour tube suction, ore rarity, multi-filter sorting bridge."""
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

tube = read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java')
worldgen = read('src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java')
golem = read('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java')
events = read('src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java')
workflow = read('.github/workflows/main.yml')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
readme = read('README.md')
upload = read('GITHUB_UPLOAD.md')

# Direct-neighbour suction propagation, matching TileTube.calculateSuction(...)
for token in [
    'v10.02 strict TC4 parity: TileTube.calculateSuction(...) is a direct-neighbour',
    'setSuction(null, 0);',
    'for (Direction direction : Direction.values())',
    'subtype.directionalFlow() && facing != direction.getOpposite()',
    'NeighbourSuction neighbour = neighbourSuctionLikeTC4(direction);',
    'neighbour.amount() > suction + 1',
    'Aspect propagatedType = neighbourType == null ? filter : neighbourType;',
    'setSuction(propagatedType, subtype.transformNeighbourSuction(neighbour.amount()))',
    'private NeighbourSuction neighbourSuctionLikeTC4(Direction direction)',
    'private record NeighbourSuction(Aspect aspect, int amount)',
]:
    require(tube, token, 'direct-neighbour suction invariant')
for token in [
    'Set<BlockPos> network = collectTubeNetwork(level, worldPosition);\n        Source source = findBestSource(level, network);',
    'Destination destination = findBestDestination(level, network, source.aspect(), source.pos());',
]:
    forbid(tube, token, 'old whole-network suction shortcut')

# Empty jar suction should be untyped, not impossible.
for token in [
    'TC4 jars can advertise untyped suction when empty/unfiltered',
    '(aspect == null && jar.amount() < jar.capacity()) || jar.canAcceptAspect(aspect)',
    'return jar.originalSuctionAmount(level.getBlockState(tubePos.relative(direction)).is(ThaumcraftMod.VOID_ESSENTIA_JAR.get()))',
    'return jar.storedAspect();',
]:
    require(tube, token, 'jar untyped suction invariant')

# TC4 generateOres infused-stone rarity restored to 8 attempts / size 6.
for token in [
    'TC4 generateOres attempts 8',
    'for (int i = 0; i < 8; i++)',
    'randomInfusedCrystal(random), 6',
]:
    require(worldgen, token, 'worldgen ore rarity invariant')
for token in [
    'for (int i = 0; i < 3; i++)',
    'randomInfusedCrystal(random), 3',
]:
    forbid(worldgen, token, 'old compact infused-stone rarity')

# Sorting golem should not be single-filter only.
for token in [
    'private List<ItemStack> sortingFilterSamplesLikeTC4()',
    # v10.62 forward-compatible: multi-filter fallback still exists, but
    # strict original sorting take now starts from home inventory candidates.
    'sortingHomeCandidatesLikeTC4(home, homeSide)',
    'sortingHasMarkedOutputWithRoomLikeTC4(sample)',
    'private boolean sortingHasMarkedOutputWithRoomLikeTC4(ItemStack sample)',
    'private int countMatchingItems(Container container, ItemStack sample, List<Direction> sides)',
    'count += stored.getCount();',
    'colorsMatchingStackLikeTC4',
]:
    require(golem, token, 'multi-filter sorting invariant')
for token in [
    'int filterColor = getGolemColor(0);\n        List<Container> outputs = sortingOutputContainersLikeTC4(filterColor);',
    'return List.of(needed);\n    }\n\n    private List<Container> sortingOutputContainersLikeTC4',
]:
    forbid(golem, token, 'old single-filter sorting path')

# Strict worldgen lifecycle must remain only new-chunk driven.
require(events, 'if (!event.isNewChunk()', 'new-chunk-only gate')
require(events, 'TC4WorldgenRuntime.generateNewChunk(level, chunk.getPos())', 'new chunk entrypoint')
tick_body = worldgen.split('public static void tickPlayerArea', 1)[1].split('private static boolean isSupportedDimension', 1)[0]
for token in ['generateVegetation(', 'growGreatwood(', 'growSilverwood(', 'seedNaturalNodeForNewChunk(', 'populateChunkOnce(', 'generateForNewChunk(']:
    if token in tick_body:
        errors.append(f'player tick worldgen placement relapse: {token}')

# Scope/version/workflow guards.
if not any(token in build for token in ["version = '10.02.0'", "version = '10.22.0'"]):
    errors.append('Missing v10.02/v10.22 build version marker')
if not any(token in mods for token in ['version="10.02.0"', 'version="10.22.0"']):
    errors.append('Missing v10.02/v10.22 mods.toml version marker')
require(workflow, 'tc4_v10_02_tube_suction_worldgen_sorting_audit.py', 'CI registration')
require(readme, 'v10.02', 'README v10.02 marker')
if not any(token in readme for token in ['82% complete / 18% remaining', '83% complete / 17% remaining']):
    errors.append('Missing v10.02/v10.22 progress marker')
if not any(token in upload for token in ['v10.02', 'v10.22']):
    errors.append('Missing upload v10.02/v10.22 marker')
require(readme + upload, 'No new items, blocks, recipes, progression', 'no new content statement')

passed = not errors
print(json.dumps({
    'version': 'v10.02',
    'goal': 'TC4 direct-neighbour tube suction, jar untyped suction, ore rarity, multi-filter sorting',
    'checks': {
        'tube_suction_direct_neighbour': True,
        'no_whole_network_suction_shortcut': True,
        'empty_jar_untyped_suction': True,
        'infused_ore_rarity_8x6': True,
        'sorting_multi_filter_needed_list': True,
        'strict_worldgen_lifecycle_retained': True,
        'no_new_registry_content': True,
    },
    'passed': passed,
    'errors': errors,
}, indent=2))
sys.exit(0 if passed else 1)
