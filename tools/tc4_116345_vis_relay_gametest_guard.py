#!/usr/bin/env python3
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
def text(path): return (root / path).read_text(encoding='utf-8')
def need(path, *tokens):
    value = text(path)
    for token in tokens:
        assert token in value, f'{path}: missing {token!r}'

assert any(f"version = '{v}'" in text('build.gradle') for v in ('11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.51', '11.63.52', '11.63.53', '11.63.54', '11.63.55'))
assert any(f'version="{v}"' in text('src/main/resources/META-INF/mods.toml') for v in ('11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.51', '11.63.52', '11.63.53', '11.63.54', '11.63.55'))

need('src/main/java/com/darkifov/thaumcraft/blockentity/AuraNodeBlockEntity.java',
     'private final AspectList energizedVisBase',
     'private final AspectList energizedVis',
     'public int consumeEnergizedVis',
     'refreshEnergizedVisForTick()',
     'Math.floor(Math.sqrt',
     'case BRIGHT -> 1.20F', 'case PALE -> 0.80F', 'case FADING -> 0.50F',
     'reduceToPrimals(aspect.firstComponent()',
     'energizedVis.addAll(energizedVisBase)',
     'tag.put("EnergizedVisBase"',
     'level.getRandom().nextInt(500) == 1')
node = text('src/main/java/com/darkifov/thaumcraft/blockentity/AuraNodeBlockEntity.java')
assert 'aspects.add(aspect, 1);\n                    break;' not in node[node.index('public void tickEnergizedState'):node.index('public static void pulseNode')]

need('src/main/java/com/darkifov/thaumcraft/aura/AuraVisRelayNetwork.java',
     'NETWORK_RANGE = 8',
     'RELAY_GRAPH_LIMIT = 512',
     'AMULET_RELAY_DISTANCE_SQUARED = 26.0D',
     'chargeAmuletFromNearestRelay',
     'Math.min(5, room)',
     'consumeEnergizedVis(aspect, request)',
     'ClipContext.Block.COLLIDER',
     'ClipContext.Fluid.NONE',
     'level.hasChunkAt(pos)',
     'first == -1 || second == -1 || first == second',
     'triggerConsumeEffect',
     'public static int drainFromRelay',
     'public static int drainMachineVis')
network = text('src/main/java/com/darkifov/thaumcraft/aura/AuraVisRelayNetwork.java')
assert 'drainToWand' not in network
assert 'CENTIVIS_PER_NODE_POINT' not in network

need('src/main/java/com/darkifov/thaumcraft/item/simple/TC4VisAmuletRuntime.java',
     'player.tickCount % 5 != 0',
     'List<ItemStack> equipped = equippedAmulets(player)',
     'int moved = Math.min(5',
     'AuraVisRelayNetwork.chargeAmuletFromNearestRelay')
amulet_runtime = text('src/main/java/com/darkifov/thaumcraft/item/simple/TC4VisAmuletRuntime.java')
assert '&& !(player.getMainHandItem().getItem() instanceof WandItem' not in amulet_runtime
assert 'tickPlayerRecharge' not in text('src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java')

need('src/main/java/com/darkifov/thaumcraft/blockentity/VisRelayBlockEntity.java',
     'private byte attunement = -1',
     'private BlockPos parentPos',
     'private Aspect pulseAspect',
     'relay.relayTick % 40L == 0L',
     'public byte cycleAttunement()',
     'if (attunement > 5) attunement = -1',
     'tag.putByte("Attunement"',
     'tag.putLong("ParentPos"',
     'ClientboundBlockEntityDataPacket.create(this)',
     'public AABB getRenderBoundingBox()')
need('src/main/java/com/darkifov/thaumcraft/block/VisRelayBlock.java',
     'VisRelayBlockEntity::serverTick',
     'relay.cycleAttunement()',
     'relay.refreshParent',
     'TC4Sounds.event("crystal")')
need('src/main/java/com/darkifov/thaumcraft/block/VisChargeRelayBlock.java',
     'relay.cycleAttunement()', 'VisRelayBlock.attunementMessage')
need('src/main/java/com/darkifov/thaumcraft/blockentity/VisChargeRelayBlockEntity.java',
     'extends VisRelayBlockEntity',
     'MAX_TRANSFER_CENTIVIS = 5',
     'AuraVisRelayNetwork.drainFromRelay')

need('src/main/java/com/darkifov/thaumcraft/client/render/TC4VisRelayBeamRenderer.java',
     'textures/misc/beam1.png',
     'HALF_WIDTH = 0.15D * 0.7D',
     'baseOpacity = pulse > 0.0F ? 0.8F : 0.3F',
     'TC4RevealerHudAdapter.isRevealer',
     'TC4NodeRenderTypes.node(BEAM, true, false)',
     'float scroll = -ticks * 0.2F',
     'quad(matrix, consumer')
need('src/main/java/com/darkifov/thaumcraft/client/render/VisRelayRenderer.java',
     'TC4VisRelayBeamRenderer.render',
     'relay.pulseAspect()',
     'getViewDistance() { return 64; }')
need('src/main/java/com/darkifov/thaumcraft/client/render/VisChargeRelayRenderer.java',
     'TC4VisRelayBeamRenderer.render')
need('src/main/java/com/darkifov/thaumcraft/client/render/TC4WandPedestalRenderer.java',
     'private static final class VertexConsumerHelper',
     'static void beamQuad')

manifest = json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
assert manifest['version'] in ('11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58', '11.63.59', '11.63.60', '11.63.61')
assert len(manifest['tests']) >= 322
by_id = {case['id']: case for case in manifest['tests']}
for case_id in (
    'vis_network.tc4_energized_transient_pool',
    'vis_network.tc4_relay_range_los_attunement',
    'items.tc4_vis_amulet_relay_recharge',
    'vis_network.tc4_workbench_charger_centivis',
    'visuals.tc4_vis_relay_beam_pulse',
    'vis_network.tc4_relay_parent_persistence'):
    assert case_id in by_id, case_id
assert 'squared distance below 26' in by_id['items.tc4_vis_amulet_relay_recharge']['expected']
assert 'at most 5 centivis' in by_id['vis_network.tc4_workbench_charger_centivis']['expected']
assert 'half-width 0.105' in by_id['visuals.tc4_vis_relay_beam_pulse']['expected']

need('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java',
     'energizedNodeUsesTransientPerTickPool',
     'helper.runAfterDelay(1L',
     'visRelayGraphHonorsLosAttunementAndPersistence',
     'workbenchChargerTransfersFiveCentivisPerTick',
     'WandItem.getVis(workbench.getItem(ArcaneWorkbenchBlockEntity.SLOT_WAND), Aspect.AER) == 5')
for case_id in (
    'gametest.energized_node_transient_pool',
    'gametest.vis_relay_los_attunement_persistence',
    'gametest.workbench_charger_five_centivis'):
    assert case_id in by_id, case_id
    assert by_id[case_id]['status'] == 'NOT_TESTED'

for workflow in ('.github/workflows/build.yml', '.github/workflows/release.yml'):
    wf = text(workflow)
    assert 'python3 tools/tc4_116345_vis_relay_gametest_guard.py' in wf
    assert './gradlew runGameTestServer --stacktrace --no-daemon' in wf
    assert '--version 11.63.45' in wf or '--version 11.63.46' in wf or '--version 11.63.47' in wf or '--version 11.63.48' in wf or '--version 11.63.49' in wf or '--version 11.63.50' in wf or '--version 11.63.51' in wf or '--version 11.63.52' in wf or '--version 11.63.53' in wf or '--version 11.63.54' in wf or '--version 11.63.55' in wf

print('TC4 v11.63.45 energized Vis relay network plus required relay GameTests guard: PASS')
