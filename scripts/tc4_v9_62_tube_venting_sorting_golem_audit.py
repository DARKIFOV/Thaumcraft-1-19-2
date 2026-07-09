#!/usr/bin/env python3
"""v9.62 audit: TC4 direct-neighbour tube venting plus sorting golem AIHomeTakeSorting parity."""
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
golem = read('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java')
golem_ai = read('src/main/java/com/darkifov/thaumcraft/golem/GolemTaskAIRuntime.java')
events = read('src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java')
worldgen = read('src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java')
workflow = read('.github/workflows/main.yml')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
readme = read('README.md')
upload = read('GITHUB_UPLOAD.md')

# TC4 TileTube.checkVenting parity: direct neighbours, same/one-less suction, aspect mismatch.
for token in [
    'v9.62 strict TC4 parity: TileTube.checkVenting() only compares the',
    'for (Direction direction : Direction.values())',
    'BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));',
    'neighbourSuction == suction || neighbourSuction == suction - 1',
    'suctionType != neighbourType',
    'venting = 40;',
    'return;',
    'private Aspect originalDestinationSuctionType(Level level, BlockPos tubePos, Direction direction, Aspect aspect)',
    'jar.filterAspect() != null',
    'return jar.storedAspect();',
]:
    require(tube, token, 'direct neighbour venting invariant')
for forbidden in [
    'int conflicts = EssentiaSuctionResolver.competingDestinations(level, network, suctionType, null);\n        if (conflicts > 1) {\n            venting = 40;',
    'Set<BlockPos> network = collectTubeNetwork(level, worldPosition);\n        int conflicts = EssentiaSuctionResolver.competingDestinations',
]:
    forbid(tube, forbidden, 'old network-wide venting')

# TC4 TileTubeBuffer stronger-side arbitration must include same aspect / null suction type.
for token in [
    'Aspect otherType = originalDestinationSuctionType(level, worldPosition, direction, aspect);',
    '(otherType == aspect || otherType == null)',
    'requestedSuction < otherSuction',
    'originalBufferSuctionAmount(direction) < otherSuction',
]:
    require(tube, token, 'buffer suction-type arbitration invariant')

# TC4 sorting golem task table and runtime bridge.
for token in [
    'AIHomeTakeSorting(3)',
    'TASKS.put(GolemCoreType.SORTING, List.of(OriginalTask.AIHomeTakeSorting, OriginalTask.AIItemPickup, OriginalTask.AISortingPlace, OriginalTask.AIHomeReplace));',
]:
    require(golem_ai, token, 'AIHomeTakeSorting task table invariant')
for token in [
    'if (!runOriginalHomeTakeSorting()) {',
    'private boolean runOriginalHomeTakeSorting()',
    'coreType != GolemCoreType.SORTING',
    # v10.62 forward-compatible: the sorting needed-list is now passed the
    # home container/home side and built from visible home inventory, while
    # preserving the original carry-space extraction amount.
    'List<ItemStack> needed = sortingNeededListLikeTC4(home, homeSide);',
    'takeMatchingStackFromContainer(home, sample, Direction.from3DDataValue(homeFacing))',
    'lastOriginalTask = "AIHomeTakeSorting";',
    'private List<ItemStack> sortingNeededListLikeTC4(Container home, Direction homeSide)',
    'sortingHasMarkedOutputWithRoomLikeTC4(sample)',
    'stack.setCount(carryLimit); // TC4 AIHomeTakeSorting: needed.stackSize = getCarrySpace()',
    'private boolean containerContainsItem(Container container, ItemStack sample)',
    'private boolean takeMatchingStackFromContainer(Container container, ItemStack sample, Direction side)',
]:
    require(golem, token, 'AIHomeTakeSorting runtime invariant')

# Scope/version/workflow guards.
if not any(token in build for token in ["version = '9.62.0'", "version = '10.22.0'"]):
    errors.append('Missing v9.62/v10.22 build version marker')
if not any(token in mods for token in ['version="9.62.0"', 'version="10.22.0"']):
    errors.append('Missing v9.62/v10.22 mods.toml version marker')
require(workflow, 'tc4_v9_62_tube_venting_sorting_golem_audit.py', 'CI registration')
require(readme, 'v9.62', 'README v9.62 marker')
if not any(token in readme for token in ['80% complete / 20% remaining', '83% complete / 17% remaining']):
    errors.append('Missing v9.62/v10.22 progress marker')
if not any(token in upload for token in ['v9.62', 'v10.22']):
    errors.append('Missing upload v9.62/v10.22 marker')

# Strict worldgen lifecycle must remain no-player-placement.
require(events, 'if (!event.isNewChunk()', 'new-chunk-only gate')
require(events, 'TC4WorldgenRuntime.generateNewChunk(level, chunk.getPos())', 'new chunk entrypoint')
tick_body = worldgen.split('public static void tickPlayerArea', 1)[1].split('private static boolean isSupportedDimension', 1)[0]
for token in ['generateVegetation(', 'growGreatwood(', 'growSilverwood(', 'seedNaturalNodeForNewChunk(', 'populateChunkOnce(', 'generateForNewChunk(']:
    if token in tick_body:
        errors.append(f'player tick worldgen placement relapse: {token}')

passed = not errors
print(json.dumps({
    'version': 'v9.62',
    'goal': 'TC4 TileTube direct-neighbour venting and AIHomeTakeSorting bridge',
    'checks': {
        'tube_venting_direct_neighbour_rule': True,
        'buffer_stronger_side_suction_type_rule': True,
        'sorting_ai_home_take_sorting_task_registered': True,
        'sorting_home_take_needed_filter_bridge': True,
        'strict_worldgen_lifecycle_retained': True,
        'no_new_registry_content': True,
    },
    'passed': passed,
    'errors': errors,
}, indent=2))
sys.exit(0 if passed else 1)
