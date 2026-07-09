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
events = 'src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java'
aura = 'src/main/java/com/darkifov/thaumcraft/aura/AuraNodeWorldRuntime.java'
maze = 'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsMazeHandler.java'
workflow = '.github/workflows/main.yml'
readme = 'README.md'
research_table_parity = 'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchTableParity.java'

require(events, 'if (!event.isNewChunk()', 'Forge new-chunk gate before world placement')
require(events, 'TC4WorldgenRuntime.generateNewChunk(level, chunk.getPos())', 'single worldgen entrypoint')
forbidden_event_calls = [
    'TC4WorldgenRuntime.tickPlayerArea(level, player)',
    'AuraNodeWorldRuntime.seedNearbyNaturalNodes(level)',
    'TC4OuterLandsLivePopulateAdapter.tickPlayerArea',
    'TC4OuterLandsMazeHandler.tickPlayerArea',
]
for token in forbidden_event_calls:
    forbid(events, token, 'player-tick world placement call')

require(worldgen, 'public static void generateNewChunk(ServerLevel level, ChunkPos chunk)', 'new chunk worldgen entrypoint')
require(worldgen, 'TC4OuterLandsLivePopulateAdapter.populateChunkOnce(level, chunk.x, chunk.z)', 'Outer Lands populate moved to chunk path')
require(worldgen, 'TC4OuterLandsMazeHandler.generateForNewChunk(level, chunk)', 'Outer Lands maze chunk path')
require(worldgen, '// TC4 generateSurface order: vegetation first, then ores, then aura/structures.', 'TC4 surface order comment')
# Order check: original generateSurface runs vegetation before ores before aura.
wg = read(worldgen)
try:
    body = wg.split('public static void generateNewChunk', 1)[1].split('private static void generateOres', 1)[0]
    positions = [
        body.index('generateVegetation(level, random, chunk)'),
        body.index('generateOres(level, random, chunk)'),
        body.index('AuraNodeWorldRuntime.seedNaturalNodeForNewChunk(level, chunk, random)'),
    ]
    if positions != sorted(positions):
        errors.append('TC4WorldgenRuntime.generateNewChunk order must be vegetation -> ores -> aura node')
except ValueError as exc:
    errors.append(f'Could not verify generateNewChunk order: {exc}')

tick_body = wg.split('public static void tickPlayerArea', 1)[1].split('private static boolean isSupportedDimension', 1)[0]
for token in [
    'generateVegetation(',
    'generateOres(',
    'generateTaintPockets(',
    'AuraNodeWorldRuntime.seedNaturalNodeForNewChunk',
    'TC4OuterLandsLivePopulateAdapter.populateChunkOnce',
    'TC4OuterLandsMazeHandler.generateForNewChunk',
    'seedChunkOnce',
]:
    if token in tick_body:
        errors.append(f'TC4WorldgenRuntime.tickPlayerArea must not place worldgen content: {token}')

for token in [
    'private static float greatwoodChance',
    'random.nextInt(60) == 3',
    'random.nextInt(25) == 7',
    'random.nextFloat() < chance',
    'private static boolean supportsSilverwood',
    'biome.contains("taint")',
    'biome.contains("jungle")',
    'biome.contains("dark_forest")',
]:
    require(worldgen, token, 'TC4 tree biome/chance parity guard')

require(aura, 'seedNaturalNodeForNewChunk(ServerLevel level, ChunkPos chunkPos, RandomSource random)', 'natural node chunk-generation lifecycle')

aura_text = read(aura)
if 'random.nextInt(36) != 0' not in aura_text and 'random.nextInt(TC4_DEFAULT_NODE_RARITY) != 0' not in aura_text:
    errors.append('missing TC4 default Config.nodeRarity baseline: random.nextInt(36) != 0 or TC4_DEFAULT_NODE_RARITY')
for token in ['seedNearbyNaturalNodes', 'for (ServerPlayer player : level.players())']:
    forbid(aura, token, 'player-nearby natural aura node fallback')

require(maze, 'generateForNewChunk(ServerLevel level, ChunkPos chunk)', 'Outer Lands maze chunk-generation lifecycle')
# The legacy player tick method may exist for audit compatibility, but it must not be called from events/worldgen.
for token in ['TC4OuterLandsMazeHandler.tickPlayerArea(level, player)', 'TC4OuterLandsLivePopulateAdapter.tickPlayerArea(level, player)']:
    forbid(worldgen, token, 'Outer Lands player-tick placement bridge')

require(workflow, 'tc4_v8_82_strict_worldgen_lifecycle_audit.py', 'CI registration for v8.82 audit')
require(readme, 'v8.82', 'public batch documentation')
require(readme, '76% complete / 24% remaining', 'updated porting estimate')
require(research_table_parity, 'NOTE_HEX_HIT_RADIUS_SQ = 144.0D', 'TC4 research note 12px hit radius squared')

if errors:
    for error in errors:
        print('::error::' + error)
    raise SystemExit(1)
print('v8.82 strict worldgen lifecycle audit: OK')
