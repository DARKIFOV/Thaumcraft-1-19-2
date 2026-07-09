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

thaumometer = 'src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java'
matrix = 'src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java'
helper = 'src/main/java/com/darkifov/thaumcraft/infusion/InfusionProcessHelper.java'
policy = 'src/main/java/com/darkifov/thaumcraft/research/TC4ResearchFlagPolicy.java'
layout = 'src/main/java/com/darkifov/thaumcraft/client/screen/OriginalResearchLayout.java'
book = 'src/main/java/com/darkifov/thaumcraft/client/screen/ThaumonomiconScreen.java'
workflow = '.github/workflows/main.yml'
worldgen = 'src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java'
events = 'src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java'

for token in [
    'TAG_PENDING_ENTITY_SCAN',
    'setPendingEntityScan(stack, target)',
    'UUID pendingEntity = consumePendingEntityScan(stack)',
    'releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft)',
    'clearPendingScans(stack)',
    'isStableEntityScanTarget(player, target)',
    'return findScannableEntity(player) == target',
]:
    require(thaumometer, token, 'v8.62 stable/cancellable thaumometer target lifecycle')

for token in [
    'consumePedestalComponentPreservingContainer(Level level, ArcanePedestalBlockEntity pedestal)',
    'stack.shrink(1)',
    'Containers.dropItemStack(level',
]:
    require(helper, token, 'v8.62 pedestal component container remainder handling')

for token in [
    'lastFailureTravellingComponent',
    'recipefailurecomponent',
    'recipefailureSourceStack',
    'clearCraftingState(true, false)',
    'if (clearLastFailure)',
    'InfusionProcessHelper.consumePedestalComponentPreservingContainer(level, pedestal)',
]:
    require(matrix, token, 'v8.62 infusion failure-time telemetry and successful component remainder path')

for token in [
    'visibleInBook(ResearchEntry entry, boolean alreadyUnlocked, boolean parentsAvailable)',
    'hasOriginalPagePayload(ResearchEntry entry)',
    'has(entry, HIDDEN) || has(entry, LOST)',
]:
    require(policy, token, 'v8.62 research/book flag policy')

require(layout, 'TC4ResearchFlagPolicy.visibleInBook(entry, unlocked(unlockedResearch, entry), available(unlockedResearch, entry))', 'layout central visibility policy')
require(book, 'TC4ResearchFlagPolicy.hasOriginalPagePayload(selected)', 'no blank synthetic Thaumonomicon page open')
for token in [
    'public static void generateNewChunk(ServerLevel level, ChunkPos chunk)',
    'generateVegetation(level, random, chunk)',
]:
    require(worldgen, token, 'v8.62 tree worldgen-only guard')

worldgen_text = read(worldgen)
if 'do not place Greatwood/Silverwood from the player tick' not in worldgen_text \
        and 'World placement is intentionally not run from player ticks' not in worldgen_text:
    errors.append('TC4WorldgenRuntime must document that worldgen placement is not run from player ticks')

require(events, 'public static void onChunkLoad(ChunkEvent.Load event)', 'Forge new chunk worldgen hook')
require(events, 'TC4WorldgenRuntime.generateNewChunk(level, chunk.getPos())', 'Greatwood/Silverwood generation moved to chunk load path')

if 'private static void seedChunkOnce' in worldgen_text:
    seed_chunk = worldgen_text.split('private static void seedChunkOnce', 1)[1].split('private static void generateOres', 1)[0]
    if 'generateVegetation(level, random, chunk)' in seed_chunk:
        errors.append('TC4WorldgenRuntime.seedChunkOnce must not call generateVegetation from player tick fallback')
if 'TC4WorldgenRuntime.tickPlayerArea(level, player)' in read(events):
    errors.append('v8.62 forward guard: CommonEvents must not call worldgen player-area fallback')

require(workflow, 'tc4_v8_62_scan_infusion_research_audit.py', 'CI audit registration')

mod = read('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
if 'v8.62' in mod:
    errors.append('ThaumcraftMod.java should not contain v8.62 registration/content changes')

recipes = list((ROOT / 'src/main/resources/data').rglob('*.json'))
new_marked = [str(path.relative_to(ROOT)) for path in recipes if 'v8.62' in path.read_text(encoding='utf-8', errors='ignore')]
if new_marked:
    errors.append('v8.62 must not introduce marked recipe/progression JSON: ' + ', '.join(new_marked[:10]))

if errors:
    for e in errors:
        print('::error::' + e)
    raise SystemExit(1)
print('v8.62 scan infusion research audit: OK')
