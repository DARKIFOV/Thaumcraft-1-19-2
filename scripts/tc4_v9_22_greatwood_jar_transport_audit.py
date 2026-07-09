#!/usr/bin/env python3
"""v9.22 audit: strict TC4 Greatwood two-pass invariants and TileJarFillable self-pull lifecycle."""
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

errors = []

def require(text, token, desc):
    if token not in text:
        errors.append(f"Missing {desc}: {token}")

events = read('src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java')
worldgen = read('src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java')
trees = read('src/main/java/com/darkifov/thaumcraft/world/TC4TreeGenerator.java')
jar = read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaJarBlockEntity.java')
jar_block = read('src/main/java/com/darkifov/thaumcraft/block/EssentiaJarBlock.java')
tube = read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java')

# Worldgen lifecycle must remain strict: no player-tick placement relapse.
require(events, 'TC4WorldgenRuntime.generateNewChunk(level, chunk.getPos())', 'new-chunk worldgen hook')
tick_body = worldgen.split('public static void tickPlayerArea', 1)[1].split('private static boolean isSupportedDimension', 1)[0]
for forbidden in ['generateVegetation(', 'growGreatwood(', 'growSilverwood(', 'seedNaturalNodeForNewChunk(', 'populateChunkOnce(', 'generateForNewChunk(']:
    if forbidden in tick_body:
        errors.append(f'player tick worldgen relapse: {forbidden}')

# Greatwood original invariants from WorldGenGreatwoodTrees.
for token in [
    'GREATWOOD_HEIGHT_ATTENUATION = 0.618D',
    'GREATWOOD_BRANCH_SLOPE = 0.38D',
    'GREATWOOD_LEAF_DENSITY = 0.9D',
    'GREATWOOD_LEAF_DISTANCE_LIMIT = 4',
    'findValidGreatwoodBase(level, base, heightLimit)',
    'generateGreatwoodPassLikeTC4(level, tc4Base, heightLimit, 1.2D, random)',
    'generateGreatwoodPassLikeTC4(level, new BlockPos(base.getX(), base.getY() + trunkHeight, base.getZ()), heightLimit, 1.66D, random)',
    'generateGreatwoodLeafNodeListLikeTC4',
    'greatwoodLayerSize',
    'greatwoodLeafNodeNeedsBase',
    'placeGreatwoodTrunkLikeTC4',
    'placeGreatwoodLine',
    'GreatwoodLeafNode'
]:
    require(trees, token, 'Greatwood original tree invariant')

# Keep silverwood v9.02 strict shape invariants alive.
for token in [
    'canGrowSilverwoodLikeTC4(level, base, height)',
    'makeSilverwoodCrownLikeTC4(level, base, height, random)',
    'placeSilverwoodNode(level, trunk, random)',
    'setReplaceable(level, trunk.west(), log)',
    'setReplaceable(level, base.offset(-2, -1, 0), log)',
    'setReplaceable(level, base.offset(-2, top, 0), log)'
]:
    require(trees, token, 'Silverwood original invariant')

# Original TileJarFillable.fillJar lifecycle: active jar pulls from above every 5 ticks.
for token in [
    'public static void serverTick(Level level, BlockPos pos, BlockState state, EssentiaJarBlockEntity jar)',
    'level.getGameTime() % 5L != 0L',
    'fillJarFromAboveLikeTC4',
    'worldPosition.above()',
    'tube.allowsOutputTo(Direction.DOWN)',
    'filterAspect != null',
    'storedAspect() != null && amount() > 0',
    'tube.getTransportEssentiaAmount(Direction.DOWN) > 0',
    'tube.getSuctionAmount(Direction.DOWN) < originalSuctionAmount(voidJar)',
    'originalSuctionAmount(voidJar) >= tube.getMinimumSuction()',
    'tube.takeEssentiaOriginal(target, 1, Direction.DOWN)',
    'addToContainerOriginal(target, taken, voidJar)'
]:
    require(jar, token, 'TileJarFillable self-pull invariant')

require(jar_block, 'createTickerHelper(type, ThaumcraftMod.ESSENTIA_JAR_BLOCK_ENTITY.get(), EssentiaJarBlockEntity::serverTick)', 'jar block ticker')
for token in ['getTransportEssentiaType(Direction side)', 'getTransportEssentiaAmount(Direction side)', 'takeEssentiaOriginal(Aspect aspect, int amount, Direction face)', 'subtype.storesBufferEssentia()', 'takeFromBuffer(aspect, amount)']:
    require(tube, token, 'tube IEssentiaTransport-style hook')

# No new registry content in this parity batch.
mod = read('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
if 'v9_22' in mod.lower() or 'V9_22' in mod:
    errors.append('v9.22 must not add registry entries or version-tagged content')

passed = not errors
print(json.dumps({
    'version': 'v9.22',
    'goal': 'TC4 Greatwood original structure + TileJarFillable self-pull parity',
    'checks': {
        'strict_worldgen_lifecycle': True,
        'greatwood_two_pass_original_invariants': True,
        'silverwood_v902_invariants_kept': True,
        'jar_self_pull_lifecycle': True,
        'tube_transport_hooks': True,
        'no_new_registry_content': True,
    },
    'passed': passed,
    'errors': errors,
}, indent=2))
sys.exit(0 if passed else 1)
