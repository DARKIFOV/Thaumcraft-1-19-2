#!/usr/bin/env python3
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
def text(path): return (root / path).read_text(encoding='utf-8')
def need(path, *tokens):
    value = text(path)
    for token in tokens:
        assert token in value, f'{path}: missing {token!r}'

assert any(f"version = '{v}'" in text('build.gradle') for v in ('11.63.46', '11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.51', '11.63.52', '11.63.53', '11.63.54', '11.63.55'))
assert any(f'version="{v}"' in text('src/main/resources/META-INF/mods.toml') for v in ('11.63.46', '11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.51', '11.63.52', '11.63.53', '11.63.54', '11.63.55'))

need('src/main/java/com/darkifov/thaumcraft/block/FumeDissipatorBlock.java',
     'extends BaseEntityBlock',
     'BlockStateProperties.FACING',
     'context.getClickedFace().getOpposite()',
     'RenderShape.ENTITYBLOCK_ANIMATED',
     'FumeDissipatorBlockEntity::serverTick')
block = text('src/main/java/com/darkifov/thaumcraft/block/FumeDissipatorBlock.java')
assert 'cleanseEntities' not in block
assert 'cleanseCrucibles' not in block
assert 'removeEffect' not in block

need('src/main/java/com/darkifov/thaumcraft/blockentity/FumeDissipatorBlockEntity.java',
     'FLUX_RADIUS = 16',
     'POSITIONS_PER_TICK = 16',
     'VIS_REQUEST_CENTIVIS = 10',
     'VIS_COST_PER_FLUX = 5',
     'CHARGES_PER_CONVERSION = 4',
     'ESSENTIA_CAPACITY = 4',
     'AuraVisRelayNetwork.drainMachineVis(serverLevel, pos, Aspect.AER, VIS_REQUEST_CENTIVIS)',
     'serverLevel.random.nextInt(4) == 0',
     'Collections.shuffle(checklist',
     'target.distSqr(worldPosition) >= FLUX_RADIUS_SQUARED',
     'state.is(ThaumcraftMod.FLUX_GOO.get()) || state.is(ThaumcraftMod.FLUX_GAS.get())',
     'Aspect.PRAECANTATIO',
     'canOutputTo(Direction face)',
     'tag.putInt("charges"',
     'tag.putInt("power"',
     'tag.putInt("essentia"',
     'ClientboundBlockEntityDataPacket.create(this)')

need('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java',
     'RegistryObject<BlockEntityType<FumeDissipatorBlockEntity>> FUME_DISSIPATOR_BLOCK_ENTITY',
     'BLOCK_ENTITIES.register("flux_scrubber"',
     'FumeDissipatorBlockEntity::new, FUME_DISSIPATOR.get()')
need('src/main/java/com/darkifov/thaumcraft/essentia/EssentiaTubeConnections.java',
     'neighbor instanceof FumeDissipatorBlockEntity scrubber',
     'scrubber.canOutputTo(direction.getOpposite())')
need('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java',
     'FumeDissipatorSource',
     'return new FumeDissipatorSource(scrubber)',
     'scrubber.takeEssentia(Aspect.PRAECANTATIO, amount, scrubber.facing())')

need('src/main/java/com/darkifov/thaumcraft/client/render/FumeDissipatorRenderer.java',
     'textures/models/fluxscrubber.png',
     'TC4FluxScrubberModel.CAP_TRIANGLES',
     'TC4FluxScrubberModel.TIP_TRIANGLES',
     'Mth.sin(q / 8.0F) * 0.075F + 0.075F')
need('src/main/java/com/darkifov/thaumcraft/client/render/model/TC4FluxScrubberModel.java',
     'Embedded triangle data from the original TC4 obelisk_cap.obj',
     'CAP_TRIANGLES', 'TIP_TRIANGLES')
need('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java',
     'FUME_DISSIPATOR_BLOCK_ENTITY.get()',
     'FumeDissipatorRenderer::new')

need('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java',
     'fluxScrubberRemovesFluxAndExportsPraecantatio',
     'FumeDissipatorBlockEntity.VIS_COST_PER_FLUX',
     'Flux Scrubber did not remove flux within radius 16',
     'exported essentia from the wrong face',
     'export Praecantatio from its facing side')

manifest = json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
assert manifest['version'] in ('11.63.46', '11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58', '11.63.59', '11.63.60', '11.63.61')
assert len(manifest['tests']) >= 325
by_id = {case['id']: case for case in manifest['tests']}
for case_id in (
    'flux_scrubber.autonomous_aer_cleanup',
    'flux_scrubber.praecantatio_output',
    'gametest.flux_scrubber_cleanup_persistence_output'):
    assert case_id in by_id, case_id
    assert by_id[case_id]['status'] == 'NOT_TESTED'
assert 'at most 16 shuffled positions per tick' in by_id['flux_scrubber.autonomous_aer_cleanup']['expected']
assert 'caps at four' in by_id['flux_scrubber.praecantatio_output']['expected']

for workflow in ('.github/workflows/build.yml', '.github/workflows/release.yml'):
    wf = text(workflow)
    assert 'python3 tools/tc4_116346_flux_scrubber_gametest_guard.py' in wf
    assert './gradlew runGameTestServer --stacktrace --no-daemon' in wf
    assert '--version 11.63.46' in wf or '--version 11.63.47' in wf or '--version 11.63.48' in wf or '--version 11.63.49' in wf or '--version 11.63.50' in wf or '--version 11.63.51' in wf or '--version 11.63.52' in wf or '--version 11.63.53' in wf or '--version 11.63.54' in wf or '--version 11.63.55' in wf

print('TC4 v11.63.46 Flux Scrubber autonomous vis/essentia parity plus required GameTest guard: PASS')
