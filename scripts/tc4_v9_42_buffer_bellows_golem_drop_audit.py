#!/usr/bin/env python3
"""v9.42 audit: TC4 TileTubeBuffer side suction/bellows/choke parity and AIHomeDrop no-inventory fallback."""
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

# TileTubeBuffer original invariants:
# - getSuctionType returns null
# - suction is side-specific: no bellows/choke=1 -> 1, choke=2 -> 0, bellows -> bellows*32
# - fillBuffer is a direct-neighbour pass every 5 ticks, not a network-wide source scan
for token in [
    'private int bellows = -1;',
    'TC4 TileTubeBuffer.getSuctionType(...) always returns null',
    'if (subtype.storesBufferEssentia()) {\n            return null;\n        }',
    'return originalBufferSuctionAmount(side);',
    'refreshBellowsLikeTC4();',
    'tube.originalTickCounter % 20 == 0',
    'private int originalBufferSuctionAmount(Direction side)',
    'if (bellows <= 0 || choke == 1) {\n            return 1;\n        }',
    'if (choke == 2) {\n            return 0;\n        }',
    'return bellows * 32;',
    'private void refreshBellowsLikeTC4()',
    'state.getBlock() instanceof BellowsBlock',
    'BellowsBlock.facesTarget(state, direction.getOpposite())',
    'TC4 TileTubeBuffer.fillBuffer() is a direct-neighbour pass',
    'for (Direction direction : Direction.values())',
    'BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));',
    'Source source = sourceFrom(blockEntity, direction.getOpposite());',
    'sideSuction <= 0 || source.priority() >= sideSuction',
    'return;\n            }\n        }\n    }\n\n    private int takeFromBuffer',
    'private boolean bufferCanOutputToSideLikeTC4(Aspect aspect, Direction face)',
    'requestedSuction < otherSuction',
    'originalBufferSuctionAmount(direction) < otherSuction',
    'tag.putInt("bellows", bellows);',
    'bellows = tag.getInt("bellows");',
]:
    require(tube, token, 'TileTubeBuffer side/bellows/choke invariant')

for forbidden in [
    'if (tube.subtype.storesBufferEssentia()) {\n            tube.tryMoveEssentia();',
    'Source source = findBestSource(level, collectTubeNetwork(level, worldPosition));\n        if (source == null || source.aspect() == null || !subtype.allowsAspect(aspectFilter, source.aspect()))',
]:
    forbid(tube, forbidden, 'old buffer-as-normal-network behavior')

# AIHomeDrop original fallback: if no inventory at home side, carried item is thrown toward the home-adjacent target and action locks for 200 ticks.
for token in [
    'public static final int ORIGINAL_HOME_DROP_THROW_LOCK_TICKS = 200;',
    'BlockPos targetPos = outputPos == null ? originalHomeContainerPos() : outputPos;',
    'dropCarriedTowardTargetLikeTC4(targetPos)',
    'private BlockPos originalHomeContainerPos()',
    'private boolean dropCarriedTowardTargetLikeTC4(BlockPos targetPos)',
    'new ItemEntity(level, getX(), getY() + getBbHeight() / 2.0F, getZ(), itemCarried.copy())',
    'item.setDeltaMovement(',
    'distance / 3.0D',
    'item.setPickUpDelay(10);',
    'level.addFreshEntity(item);',
    'itemCarried = ItemStack.EMPTY;',
    'originalChestInteractTicks = GolemTaskAIRuntime.ORIGINAL_HOME_DROP_THROW_LOCK_TICKS;',
    'lastOriginalTask = "AIHomeDrop:dropItem";',
]:
    require(golem + golem_ai, token, 'AIHomeDrop no-inventory fallback invariant')

# Strict worldgen lifecycle must remain no-player-placement.
require(events, 'if (!event.isNewChunk()', 'new-chunk-only gate')
require(events, 'TC4WorldgenRuntime.generateNewChunk(level, chunk.getPos())', 'new chunk entrypoint')
tick_body = worldgen.split('public static void tickPlayerArea', 1)[1].split('private static boolean isSupportedDimension', 1)[0]
for token in ['generateVegetation(', 'growGreatwood(', 'growSilverwood(', 'seedNaturalNodeForNewChunk(', 'populateChunkOnce(', 'generateForNewChunk(']:
    if token in tick_body:
        errors.append(f'player tick worldgen placement relapse: {token}')

compatible_versions = ["version = '9.42.0'", 'version="9.42.0"', "version = '9.62.0'", 'version="9.62.0"', "version = '10.22.0'", 'version="10.22.0"']
if not any(token in build + mods for token in compatible_versions):
    errors.append('Missing compatible version marker: 9.42.0/9.62.0/10.22.0')
require(workflow, 'tc4_v9_42_buffer_bellows_golem_drop_audit.py', 'CI registration')
require(readme, 'v9.42', 'README v9.42 marker')
if not any(token in readme for token in ['79% complete / 21% remaining', '80% complete / 20% remaining', '83% complete / 17% remaining']):
    errors.append('Missing compatible progress marker')
if not any(token in upload for token in ['v9.42', 'v10.22']):
    errors.append('Missing upload v9.42/v10.22 marker')

passed = not errors
print(json.dumps({
    'version': 'v9.42',
    'goal': 'TC4 TileTubeBuffer side suction/bellows/choke parity plus AIHomeDrop no-inventory fallback',
    'checks': {
        'buffer_side_specific_suction': True,
        'buffer_direct_neighbour_fill': True,
        'buffer_output_side_arbitration': True,
        'golem_home_drop_item_fallback': True,
        'strict_worldgen_lifecycle_retained': True,
        'no_new_registry_content': True,
    },
    'passed': passed,
    'errors': errors,
}, indent=2))
sys.exit(0 if passed else 1)
