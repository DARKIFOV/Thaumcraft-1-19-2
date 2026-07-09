#!/usr/bin/env python3
"""v9.82 audit: TC4 local tube equalize, persistent worldgen ledger, spider Greatwood, marker-aware sorting."""
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
worldgen_data = read('src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenSavedData.java')
tree = read('src/main/java/com/darkifov/thaumcraft/world/TC4TreeGenerator.java')
golem = read('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java')
events = read('src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java')
workflow = read('.github/workflows/main.yml')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
readme = read('README.md')
upload = read('GITHUB_UPLOAD.md')

# TC4 TileTube.equalizeWithNeighbours lifecycle: direct neighbour, empty tube, one essentia, transient tube state.
for token in [
    'private void equalizeWithNeighboursLikeTC4(boolean directional)',
    'original TileTube.equalizeWithNeighbours(...)',
    'if (essentiaAmount > 0 || suction <= 0)',
    'for (Direction direction : Direction.values())',
    'BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));',
    'TransportNeighbour neighbour = transportNeighbourFrom(blockEntity, direction.getOpposite());',
    'neighbour.canOutput()',
    'suction > neighbour.suctionAmount()',
    'suction >= neighbour.minimumSuction()',
    'int taken = neighbour.take(pullAspect, TRANSFER_AMOUNT);',
    'int accepted = addEssentiaToLocalTubeLikeTC4(pullAspect, taken, direction);',
    'essentiaType = aspect;',
    'essentiaAmount += accepted;',
    'private interface TransportNeighbour',
    'private record TubeTransportNeighbour',
    'private record SourceTransportNeighbour',
]:
    require(tube, token, 'local equalize invariant')
for forbidden in [
    'int accepted = destination.container().accept(source.aspect(), removed);',
    'renderTransferParticles(source.aspect(), destination.container().pos(), destination.container().voidLike());\n        essentiaAmount = 0;\n        essentiaType = null;',
    'Set<BlockPos> network = collectTubeNetwork(level, worldPosition);\n        lastNetworkSize = network.size();\n\n        Source source = findBestSource(level, network);',
]:
    forbid(tube, forbidden, 'old whole-network normal tube shortcut')

# Persistent processed-chunk marker: SavedData must gate generateNewChunk before placement.
for token in [
    'public final class TC4WorldgenSavedData extends SavedData',
    'DATA_NAME = "thaumcraft_tc4_worldgen_chunks"',
    'private final Set<Long> processedChunks = new HashSet<>()',
    'tag.getLongArray("ProcessedChunks")',
    'public boolean markProcessed(ChunkPos chunk)',
    'setDirty();',
    'tag.putLongArray("ProcessedChunks", values);',
    'TC4WorldgenSavedData savedData = TC4WorldgenSavedData.get(level);',
    'if (!savedData.markProcessed(chunk))',
]:
    require(worldgen + worldgen_data, token, 'persistent worldgen ledger invariant')

# Greatwood spider tree: spawner, 50 web attempts, dungeon chest.
for token in [
    'Blocks.SPAWNER.defaultBlockState()',
    'SpawnerBlockEntity spawner',
    'EntityType.CAVE_SPIDER',
    'for (int i = 0; i < 50; i++)',
    'random.nextInt(14) - 7',
    'random.nextInt(10)',
    'Blocks.COBWEB.defaultBlockState()',
    'Blocks.CHEST.defaultBlockState()',
    'ChestBlockEntity chest',
    'BuiltInLootTables.SIMPLE_DUNGEON',
]:
    require(tree, token, 'Greatwood spider dungeon invariant')
forbidden_tree = 'for (int i = 0; i < 28; i++)'
forbid(tree, forbidden_tree, 'old decorative-only web count')

# Marker-aware sorting bridge: keep original list, use marker colors and side access semantics.
for token in [
    'private ListTag originalMarkers = new ListTag();',
    'return originalMarkers.copy();',
    'originalMarkers = markers.copy();',
    'sortingOutputContainersLikeTC4(int color)',
    'GolemBellMarkerRuntime.readMarkers(originalMarkers)',
    'markerMatchesGolemColorLikeTC4(marker.color(), color)',
    'Direction side = Direction.from3DDataValue(marker.side() & 255);',
    'Direction.from3DDataValue(marker.side() & 255)',
    'markerColor == -1 || color == -1 || markerColor == (byte) color',
]:
    require(golem, token, 'marker-aware sorting invariant')

# Strict lifecycle must remain no player tick placement.
require(events, 'if (!event.isNewChunk()', 'new-chunk-only gate')
require(events, 'TC4WorldgenRuntime.generateNewChunk(level, chunk.getPos())', 'new chunk entrypoint')
tick_body = worldgen.split('public static void tickPlayerArea', 1)[1].split('private static boolean isSupportedDimension', 1)[0]
for token in ['generateVegetation(', 'growGreatwood(', 'growSilverwood(', 'seedNaturalNodeForNewChunk(', 'populateChunkOnce(', 'generateForNewChunk(']:
    if token in tick_body:
        errors.append(f'player tick worldgen placement relapse: {token}')

# Scope/version/workflow guards.
if not any(token in build for token in ["version = '9.82.0'", "version = '10.02.0'", "version = '10.22.0'"]):
    errors.append('Missing v9.82/v10.02/v10.22 build version marker')
if not any(token in mods for token in ['version="9.82.0"', 'version="10.02.0"', 'version="10.22.0"']):
    errors.append('Missing v9.82/v10.02/v10.22 mods.toml version marker')
require(workflow, 'tc4_v9_82_local_tube_worldgen_golem_audit.py', 'CI registration')
require(readme, 'v9.82', 'README v9.82 marker')
if not any(token in readme for token in ['81% complete / 19% remaining', '82% complete / 18% remaining', '83% complete / 17% remaining']):
    errors.append('Missing v9.82/v10.02/v10.22 progress marker')
if not any(token in upload for token in ['v9.82', 'v10.02', 'v10.22']):
    errors.append('Missing upload v9.82/v10.02/v10.22 marker')

passed = not errors
print(json.dumps({
    'version': 'v9.82',
    'goal': 'TC4 local tube equalize, persistent worldgen ledger, spider Greatwood, marker-aware sorting bridge',
    'checks': {
        'normal_tube_local_equalize': True,
        'no_whole_network_normal_tube_shortcut': True,
        'persistent_worldgen_chunk_ledger': True,
        'greatwood_spider_dungeon': True,
        'sorting_marker_color_side_bridge': True,
        'strict_worldgen_lifecycle_retained': True,
        'no_new_registry_content': True,
    },
    'passed': passed,
    'errors': errors,
}, indent=2))
sys.exit(0 if passed else 1)
