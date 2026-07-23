#!/usr/bin/env python3
import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
def text(path): return (root / path).read_text(encoding='utf-8')
def need(path, *tokens):
    value = text(path)
    for token in tokens:
        assert token in value, f'{path}: missing {token!r}'

assert any(f"version = '{v}'" in text('build.gradle') for v in ('11.63.42', '11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.51', '11.63.52', '11.63.53', '11.63.54', '11.63.55'))
assert any(f'version="{v}"' in text('src/main/resources/META-INF/mods.toml') for v in ('11.63.42', '11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.51', '11.63.52', '11.63.53', '11.63.54', '11.63.55'))

need('src/main/java/com/darkifov/thaumcraft/block/ManaPodBlock.java',
     'BlockStateProperties.AGE_7', 'random.nextInt(30) == 0',
     'state.is(Blocks.OAK_LOG)', 'state.is(Blocks.SPRUCE_LOG)',
     'GREATWOOD_LOG.get()', 'SILVERWOOD_LOG.get()',
     'level.scheduleTick(pos, this, 1)', 'level.destroyBlock(pos, true)',
     'age == 7 && builder.getLevel().random.nextFloat() > 0.33F ? 2 : 1',
     'TC4ManaBeanItem.setAspect(bean, aspect)',
     'state.getValue(AGE)')
need('src/main/java/com/darkifov/thaumcraft/blockentity/ManaPodBlockEntity.java',
     'if (age == 3)', 'Direction.Plane.HORIZONTAL',
     'AspectCombinationRegistry.combine', 'weighted.add(combo)',
     'weighted.add(combo)', 'random.nextInt(8) == 0 ? Aspect.HERBA',
     'tag.putString("Aspect", aspect.id())', 'ClientboundBlockEntityDataPacket.create(this)',
     'public AspectList exposedAspects()', 'state.getValue(ManaPodBlock.AGE) != 7')
need('src/main/java/com/darkifov/thaumcraft/item/simple/TC4ManaBeanItem.java',
     'context.getClickedFace() != Direction.DOWN',
     'ManaPodBlock.isSupportedLog(support)', 'ManaPodBlock.isMagicalBiome(level, podPos)',
     'TC4_MANA_POD.get().defaultBlockState()', 'pod.setAspect(getAspect(context.getItemInHand()))',
     'public static int tint(ItemStack stack)')
need('src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java',
     'if (magicalForest)', 'generateManaPods(level, random, chunk)',
     'for (int attempt = 0; attempt < 10; attempt++)',
     'Math.max(64, level.getMinBuildHeight() + 1)',
     'int initialAge = 2 + random.nextInt(5)', 'pod.checkGrowth(random)',
     'level.hasChunkAt(pos)')
need('src/main/java/com/darkifov/thaumcraft/client/render/ManaPodRenderer.java',
     'textures/models/manapod_0.png', 'textures/models/manapod_2.png',
     'age == 7', 'float mix = age - 2.0F',
     'Mth.sin((gameTime + partialTick + Math.floorMod(pod.hashCode(), 100)) / 8.0F) * 0.1F + 0.9F',
     '0.125F * age * pulse', '0.15F * age')
need('src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerTargeting.java',
     'blockEntity instanceof ManaPodBlockEntity pod', 'pod.exposedAspects()')
need('src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java',
     'blockEntity instanceof ManaPodBlockEntity pod', 'pod.exposedAspects()')
need('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java',
     'TC4_MANA_POD.get(), RenderType.cutout()',
     'MANA_POD_BLOCK_ENTITY.get()', 'ManaPodRenderer::new',
     'TC4ManaPodModel.LAYER', 'TC4ManaBeanItem.tint(stack)')
main = text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for token in ('BLOCKS.register("tc4_block_mana_pod"',
              'new ManaPodBlock(', 'randomTicks()',
              'lightLevel(state -> state.getValue(ManaPodBlock.AGE))',
              'BLOCK_ENTITIES.register("tc4_block_mana_pod"'):
    assert token in main, token

assets = [
 'src/main/resources/assets/thaumcraft/textures/block/manapod_stem_0.png',
 'src/main/resources/assets/thaumcraft/textures/block/manapod_stem_1.png',
 'src/main/resources/assets/thaumcraft/textures/block/manapod_stem_2.png',
 'src/main/resources/assets/thaumcraft/textures/models/manapod_0.png',
 'src/main/resources/assets/thaumcraft/textures/models/manapod_2.png',
 'src/main/resources/assets/thaumcraft/blockstates/tc4_block_mana_pod.json',
 'src/main/resources/data/thaumcraft/tags/worldgen/biome/is_magical.json',
 'src/main/resources/data/thaumcraft/loot_tables/blocks/tc4_block_mana_pod.json',
]
for rel in assets:
    assert (root / rel).is_file(), rel

manifest = json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
assert manifest['version'] in ('11.63.42', '11.63.43','11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58', '11.63.59', '11.63.60', '11.63.61')
assert len(manifest['tests']) >= 302
by_id = {case['id']: case for case in manifest['tests']}
for case_id in ('plants.tc4_mana_bean_magical_planting',
                'plants.tc4_mana_pod_growth_crossbreeding',
                'plants.tc4_mana_pod_aspect_drops',
                'worldgen.tc4_magical_forest_mana_pods',
                'visuals.tc4_mana_pod_growth_render',
                'plants.tc4_mana_pod_mature_aspect_scan'):
    assert case_id in by_id, case_id
assert 'probability 1/30' in by_id['plants.tc4_mana_pod_growth_crossbreeding']['expected']
assert 'greater than 0.33' in by_id['plants.tc4_mana_pod_aspect_drops']['expected']
assert 'ten Mana Pod attempts' in by_id['worldgen.tc4_magical_forest_mana_pods']['expected']

for wf in ('build.yml', 'release.yml'):
    workflow = text(f'.github/workflows/{wf}')
    assert any(f'--version {v}' in workflow for v in ('11.63.43','11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.51', '11.63.52', '11.63.53', '11.63.54', '11.63.55'))
    assert 'python3 tools/tc4_116342_mana_pod_parity_guard.py' in workflow

print('TC4 v11.63.42 Mana Pod cultivation, crossbreeding, worldgen and visual parity guard: PASS')
