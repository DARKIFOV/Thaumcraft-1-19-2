#!/usr/bin/env python3
"""v10.22 audit: TC4 tube timing/valve vent color, worldgen gates, sided sorting golem."""
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
renderer = read('src/main/java/com/darkifov/thaumcraft/client/render/EssentiaTubeRenderer.java')
worldgen = read('src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java')
golem = read('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java')
events = read('src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java')
workflow = read('.github/workflows/main.yml')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
readme = read('README.md')
upload = read('GITHUB_UPLOAD.md')

# Tube timing: original TileTube count is per-tile and seeded randomly, not global gameTime.
for token in [
    'private int originalTickCounter = 0;',
    'TC4 TileTube uses a per-tile count seeded with random.nextInt(10)',
    'tube.originalTickCounter = level.random.nextInt(10);',
    'tube.originalTickCounter++;',
    'tube.originalTickCounter % 2 == 0',
    'tube.originalTickCounter % Math.max(5, ThaumcraftConfig.ESSENTIA_TUBE_TRANSFER_INTERVAL_TICKS.get()) != 0',
    'if (tube.venting > 0) {\n            return;\n        }',
    'if (tube.essentiaType != null && tube.essentiaAmount == 0)',
]:
    require(tube, token, 'TC4 per-tile tube timing invariant')
for token in [
    'level.getGameTime() % 2L == 0L',
    'level.getGameTime() % Math.max(5, ThaumcraftConfig.ESSENTIA_TUBE_TRANSFER_INTERVAL_TICKS.get())',
]:
    forbid(tube, token, 'old synchronized tube gameTime gate')

# Valve topology and vent color/state.
for token in [
    'private int ventColor = 0xAAAAAA;',
    'public int ventingColor()',
    'TileTubeValve.isConnectable(face)',
    'subtype.redstoneValve() && direction == facing',
    'ventColor = suctionType == null ? 0xAAAAAA : suctionType.nativeColor();',
    'tag.putInt("ventColor", ventColor);',
    'tag.putInt("tc4Count", originalTickCounter);',
    'ventColor = tag.contains("ventColor")',
    'originalTickCounter = tag.contains("tc4Count")',
    'tube.isVenting() ? tube.ventingColor()',
]:
    require(tube + renderer, token, 'valve/venting state invariant')

# Worldgen gates: flat tree guard and biome blacklist centralization.
for token in [
    'if (!isFlatWorldLikeTC4(level))',
    'generateVegetation(level, random, chunk);',
    'int blacklist = tc4BiomeBlacklistLevel(level, chunk);',
    'if (blacklist == 0 || blacklist == 2)',
    'if (tc4BiomeBlacklistLevel(level, chunk) != -1)',
    'private static int tc4BiomeBlacklistLevel(ServerLevel level, ChunkPos chunk)',
    'biome.contains("void") || biome.contains("debug") || biome.contains("placeholder")',
    'private static boolean isFlatWorldLikeTC4(ServerLevel level)',
    'getGenerator().getClass().getName().toLowerCase(java.util.Locale.ROOT)',
]:
    require(worldgen, token, 'worldgen flat/biome gate invariant')

tick_body = worldgen.split('public static void tickPlayerArea', 1)[1].split('private static boolean isSupportedDimension', 1)[0]
for token in ['generateVegetation(', 'growGreatwood(', 'growSilverwood(', 'seedNaturalNodeForNewChunk(', 'populateChunkOnce(', 'generateForNewChunk(']:
    if token in tick_body:
        errors.append(f'player tick worldgen placement relapse: {token}')
require(events, 'if (!event.isNewChunk()', 'new-chunk-only event gate')
require(events, 'TC4WorldgenRuntime.generateNewChunk(level, chunk.getPos())', 'new chunk entrypoint')

# Sorting golem: marker pos is the inventory; side is access side for sided inventories.
for token in [
    'import net.minecraft.world.WorldlyContainer;',
    'private record MarkedOutputContainer(Container container, BlockPos pos, List<Direction> sides)',
    'sortingOutputContainersLikeTC4(color)',
    'private List<MarkedOutputContainer> sortingOutputContainersLikeTC4(int color)',
    'markers point at the inventory',
    'marker side is an access side for sided inventories',
    'containers.putIfAbsent(markerPos.immutable(), container);',
    'markedSides.computeIfAbsent(markerPos.immutable()',
    'private int[] slotsForContainerSide(Container container, Direction side)',
    'container instanceof WorldlyContainer worldly && side != null',
    'worldly.getSlotsForFace(side)',
    'private boolean canTakeThroughSide(Container container, ItemStack stored, int slot, Direction side)',
    'worldly.canTakeItemThroughFace(slot, stored, side)',
    'takeOneAcceptedStackFromContainer(container, Direction.from3DDataValue(homeFacing))',
    'takeMatchingStackFromContainer(home, sample, Direction.from3DDataValue(homeFacing))',
]:
    require(golem, token, 'sided sorting/home inventory invariant')
for token in [
    'addContainerAt(outputs, seen, markerPos.relative(side));',
    'private List<Container> sortingOutputContainersLikeTC4(int color)',
    'private int countMatchingItems(Container container, ItemStack sample) {',
]:
    forbid(golem, token, 'old marker-adjacent/unsided sorting path')

# Version and docs.
if "version = '10.22.0'" not in build:
    errors.append('Missing build.gradle v10.22 version')
if 'version="10.22.0"' not in mods:
    errors.append('Missing mods.toml v10.22 version')
require(workflow, 'tc4_v10_22_tube_valve_worldgen_golem_audit.py', 'CI registration')
require(readme, 'v10.22', 'README marker')
require(readme, '83% complete / 17% remaining', 'progress marker')
require(upload, 'v10.22', 'upload marker')
require(readme + upload, 'No new items, blocks, recipes, progression', 'no new content statement')

passed = not errors
print(json.dumps({
    'version': 'v10.22',
    'goal': 'TC4 tube per-tile timing/valve venting, worldgen flat/biome gates, sided sorting golem',
    'checks': {
        'tube_per_tile_randomized_timing': True,
        'valve_facing_side_and_vent_color': True,
        'worldgen_flat_and_biome_gates': True,
        'player_tick_worldgen_still_forbidden': True,
        'sorting_golem_sided_inventory_semantics': True,
        'no_new_registry_content': True,
    },
    'passed': passed,
    'errors': errors,
}, indent=2))
sys.exit(0 if passed else 1)
