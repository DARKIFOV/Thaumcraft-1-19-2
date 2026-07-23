#!/usr/bin/env python3
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
def text(path): return (root / path).read_text(encoding='utf-8')
def need(path, *tokens):
    value = text(path)
    for token in tokens:
        assert token in value, f'{path}: missing {token!r}'

assert any(f"version = '{v}'" in text('build.gradle') for v in ('11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.51', '11.63.52', '11.63.53', '11.63.54', '11.63.55'))
assert any(f'version="{v}"' in text('src/main/resources/META-INF/mods.toml') for v in ('11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.51', '11.63.52', '11.63.53', '11.63.54', '11.63.55'))

need('src/main/java/com/darkifov/thaumcraft/aura/TC4NodeJarMultiblock.java',
     'PRIMAL_COST_CENTIVIS = 70',
     'Aspect.IGNIS, Aspect.TERRA, Aspect.ORDO',
     'Aspect.AER, Aspect.PERDITIO, Aspect.AQUA',
     'for (int dy = -3; dy <= 0; dy++)',
     'for (int dx = -1; dx <= 1; dx++)',
     'for (int dz = -1; dz <= 1; dz++)',
     'y == 2 ? state.is(BlockTags.WOODEN_SLABS) : state.is(Blocks.GLASS)',
     'new ArrayList<>(35)',
     'consumeCost(wandStack, player)',
     'serverLevel.setBlock(nodePos, ThaumcraftMod.NODE_JAR_BLOCK.get().defaultBlockState(), 3)',
     'jar.startCaptureAnimation()')

need('src/main/java/com/darkifov/thaumcraft/aura/TC4NodeJarRuntime.java',
     'MODIFIER_DAMAGE_CHANCE = 0.75F',
     'node.saveNodeJarTag()',
     'nodeTag.putInt("PreservationPercent", 100)',
     'case BRIGHT -> AuraNodeModifier.NORMAL.name()',
     'case NORMAL -> AuraNodeModifier.PALE.name()',
     'case PALE -> AuraNodeModifier.FADING.name()',
     'case FADING -> AuraNodeModifier.FADING.name()')
runtime = text('src/main/java/com/darkifov/thaumcraft/aura/TC4NodeJarRuntime.java')
assert 'scaled(' not in runtime
assert 'NORMAL_PRESERVATION_PERCENT' not in runtime

need('src/main/java/com/darkifov/thaumcraft/block/AuraNodeBlock.java',
     'TC4NodeJarMultiblock.tryCreate(level, pos, player, hand, stack)',
     'Block#use', 'Item#useOn')
need('src/main/java/com/darkifov/thaumcraft/block/WandItem.java',
     'TC4NodeJarMultiblock.tryCreate(level, pos, player, context.getHand(), wandStack)')

need('src/main/java/com/darkifov/thaumcraft/blockentity/AuraNodeBlockEntity.java',
     'private String nodeId = ""',
     'tag.putString("NodeId", nodeId())',
     'nodeId = nodeTag.contains("NodeId")',
     'jarred = false',
     'UUID.randomUUID().toString()')
need('src/main/java/com/darkifov/thaumcraft/blockentity/NodeJarBlockEntity.java',
     'TAG_ANIMATION_END = "CaptureAnimationEnd"',
     'captureAnimationEnd = level == null ? 20L : level.getGameTime() + 20L',
     '1.0F + 2.0F * Math.min(1.0F, remainingTicks / 20.0F)',
     'level.sendBlockUpdated',
     'ClientboundBlockEntityDataPacket.create(this)')
need('src/main/java/com/darkifov/thaumcraft/block/NodeJarBlock.java',
     'extends BaseEntityBlock',
     'return Shapes.block()',
     'node.initializeFromJarTag(nodeTag)',
     'new ItemStack(ThaumcraftMod.NODE_JAR.get())',
     'TC4NodeJarRuntime.TAG_NODE_JAR')
need('src/main/java/com/darkifov/thaumcraft/block/NodeJarItem.java',
     'extends BlockItem',
     'TC4NodeJarRuntime.hasNode(root)',
     'return super.useOn(context)',
     'NodeJarItemRenderer.instance()')
need('src/main/java/com/darkifov/thaumcraft/client/render/NodeJarRenderer.java',
     'jar.captureScale(partialTick)',
     'NodeJarItemRenderer.renderJarShell',
     'NodeJarItemRenderer.renderContainedNode')

main = text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for token in ('BLOCKS.register("node_jar"', 'ITEMS.register("node_jar"',
              'new NodeJarItem(NODE_JAR_BLOCK.get()', 'BLOCK_ENTITIES.register("node_jar"'):
    assert token in main, token
need('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java',
     'NODE_JAR_BLOCK_ENTITY.get()', 'NodeJarRenderer::new')
for rel in ('src/main/resources/assets/thaumcraft/blockstates/node_jar.json',
            'src/main/resources/assets/thaumcraft/models/block/node_jar.json',
            'src/main/resources/assets/thaumcraft/models/item/node_jar.json',
            'src/main/resources/data/thaumcraft/loot_tables/blocks/node_jar.json'):
    assert (root / rel).is_file(), rel

manifest = json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
assert manifest['version'] in ('11.63.41', '11.63.42','11.63.43','11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58', '11.63.59', '11.63.60', '11.63.61')
assert len(manifest['tests']) >= 296
by_id = {case['id']: case for case in manifest['tests']}
for case_id in ('aura_nodes.tc4_node_jar_multiblock_capture',
                'aura_nodes.tc4_node_jar_profile_preservation',
                'aura_nodes.tc4_node_jar_transport_release',
                'visuals.tc4_node_jar_collapse_render'):
    assert case_id in by_id, case_id
assert '70 centivis' in by_id['aura_nodes.tc4_node_jar_multiblock_capture']['expected']
assert '75 percent chance' in by_id['aura_nodes.tc4_node_jar_profile_preservation']['expected']
assert 'live unjarred aura node' in by_id['aura_nodes.tc4_node_jar_transport_release']['expected']
assert '3.0 to 1.0 over one second' in by_id['visuals.tc4_node_jar_collapse_render']['expected']

for wf in ('build.yml', 'release.yml'):
    workflow = text(f'.github/workflows/{wf}')
    assert any(f'--version {v}' in workflow for v in ('11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.51', '11.63.52', '11.63.53', '11.63.54', '11.63.55'))
    assert 'python3 tools/tc4_116341_node_jar_ritual_guard.py' in workflow
for artifact in ('TC4_11.63.41_NODE_JAR_RITUAL_PORT_REPORT_RU.md',
                 'TC4_11.63.41_REMAINING_OBJECTS_AUDIT_RU.md',
                 'reports/remaining_objects_v11.63.41.json'):
    assert (root / artifact).is_file(), artifact
print('TC4 v11.63.41 exact Node in a Jar ritual guard: PASS')
