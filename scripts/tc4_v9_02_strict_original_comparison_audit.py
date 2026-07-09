#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding='utf-8')

def require(rel: str, token: str, label: str) -> None:
    text = read(rel)
    if token not in text:
        errors.append(f"{rel}: missing {label}: {token}")

def forbid(rel: str, token: str, label: str) -> None:
    text = read(rel)
    if token in text:
        errors.append(f"{rel}: forbidden {label}: {token}")

worldgen = 'src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java'
tree = 'src/main/java/com/darkifov/thaumcraft/world/TC4TreeGenerator.java'
tube = 'src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java'
subtype = 'src/main/java/com/darkifov/thaumcraft/essentia/EssentiaTubeSubtype.java'
policy = 'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchFlagPolicy.java'
events = 'src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java'
workflow = '.github/workflows/main.yml'
readme = 'README.md'
upload = 'GITHUB_UPLOAD.md'
build = 'build.gradle'
mods = 'src/main/resources/META-INF/mods.toml'

# Worldgen lifecycle must stay chunk-only, never player-proximity placement.
require(events, 'if (!event.isNewChunk()', 'new-chunk-only worldgen gate')
require(events, 'TC4WorldgenRuntime.generateNewChunk(level, chunk.getPos())', 'single worldgen entrypoint')
for token in [
    'TC4WorldgenRuntime.tickPlayerArea(level, player)',
    'AuraNodeWorldRuntime.seedNearbyNaturalNodes(level)',
    'TC4OuterLandsLivePopulateAdapter.tickPlayerArea(level, player)',
    'TC4OuterLandsMazeHandler.tickPlayerArea(level, player)',
]:
    forbid(events, token, 'player-proximity world placement')

wg = read(worldgen)
tick_body = wg.split('public static void tickPlayerArea', 1)[1].split('private static boolean isSupportedDimension', 1)[0]
for token in ['generateVegetation(', 'generateOres(', 'generateTaintPockets(', 'seedNaturalNodeForNewChunk', 'populateChunkOnce', 'generateForNewChunk']:
    if token in tick_body:
        errors.append(f'TC4WorldgenRuntime.tickPlayerArea still contains placement path: {token}')

# Original silverwood shape invariants from WorldGenSilverwoodTrees: central + cardinal trunk,
# buttress/base roots, top flare, height 7..10 and chance-driven node only.
for token in [
    'int height = 7 + random.nextInt(4)',
    'canGrowSilverwoodLikeTC4(level, base, height)',
    'makeSilverwoodCrownLikeTC4(level, base, height, random)',
    'int nodeChance = Math.max(1, (int) (height * 1.5D))',
    'worldgen && y > 0 && !lastNode && random.nextInt(nodeChance) == 0',
    'nodeChance += height',
    'setReplaceable(level, trunk.west(), log)',
    'setReplaceable(level, trunk.east(), log)',
    'setReplaceable(level, trunk.north(), log)',
    'setReplaceable(level, trunk.south(), log)',
    'setReplaceable(level, base.offset(-2, -1, 0), log)',
    'setReplaceable(level, base.offset(2, -1, 0), log)',
    'setReplaceable(level, base.offset(0, -1, -2), log)',
    'setReplaceable(level, base.offset(0, -1, 2), log)',
    'int start = height - 5',
    'int end = height + 3 + random.nextInt(3)',
    'dist < 10 + random.nextInt(8)',
]:
    require(tree, token, 'TC4 silverwood shape/source invariant')
for token in [
    'Original WorldGenSilverwoodTrees is a single trunk',
    'if (worldgen || random.nextInt(3) != 0)',
]:
    forbid(tree, token, 'old incorrect silverwood assumption')

# Essentia restrict tube: exactly one TC4 suction transform, not pre-half + subtype half.
require(tube, 'TC4 TileTube.calculateSuction applies exactly one transform', 'restrict tube single-transform comment')
require(tube, 'setSuction(source.aspect(), subtype.transformNeighbourSuction(neighbourSuction));', 'single transform call')
forbidden_restrict = '''if (subtype.restrictsSuction()) {
            // TileTubeRestrict deliberately chokes incoming suction before the shared TileTube transfer step.
            neighbourSuction = Math.max(0, neighbourSuction / 2);
        }'''
forbid(tube, forbidden_restrict, 'double restrict suction halving')
require(subtype, 'return Math.max(0, neighbourSuction / 2);', 'TC4 TileTubeRestrict suck/2 transform')
require(subtype, 'return Math.max(0, neighbourSuction - 1);', 'TC4 normal TileTube suck-1 transform')

# Research table must not invent notes from rebuild placeholders lacking original page payload.
require(policy, 'if (!hasOriginalPagePayload(entry))', 'strict payload guard for normal research notes')
require(policy, 'port from fabricating progression out of rebuild-only placeholder nodes', 'reason for payload guard')
require(policy, 'has(entry, AUTO_UNLOCK) || has(entry, STUB) || has(entry, HIDDEN) || has(entry, LOST)', 'original flag note creation guard')

for token in ['version = \'9.02.0\'', "version=\"9.02.0\""]:
    rel = build if token.startswith('version =') else mods
    require(rel, token, 'v9.02 version marker')
require(workflow, 'tc4_v9_02_strict_original_comparison_audit.py', 'CI registration for v9.02 audit')
require(readme, 'v9.02', 'public v9.02 readme marker')
require(readme, '77% complete / 23% remaining', 'updated port progress')
require(upload, 'v9.02', 'github upload marker')

if errors:
    for error in errors:
        print('::error::' + error)
    raise SystemExit(1)
print('v9.02 strict original comparison audit: OK')
