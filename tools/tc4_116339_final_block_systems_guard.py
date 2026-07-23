#!/usr/bin/env python3
import json, re
from pathlib import Path
root=Path(__file__).resolve().parents[1]
def text(path): return (root/path).read_text(encoding='utf-8')
def need(path,*tokens):
    value=text(path)
    for token in tokens: assert token in value, f'{path}: missing {token!r}'
assert any(f"version = '{v}'" in text('build.gradle') for v in ('11.63.39','11.63.40','11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50'))
assert any(f'version="{v}"' in text('src/main/resources/META-INF/mods.toml') for v in ('11.63.39','11.63.40','11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50'))
need('src/main/java/com/darkifov/thaumcraft/blockentity/TC4EssentiaLampBlockEntity.java',
     'Aspect.HERBA','Aspect.VICTUS','charges = 100','reserve = true','fertilityCounter++ % 300','sameSpecies.size() > 7','performBonemeal','AlembicBlockEntity alembic','EssentiaReservoirBlockEntity reservoir','AlchemicalCentrifugeBlockEntity centrifuge')
need('src/main/java/com/darkifov/thaumcraft/blockentity/TC4WandPedestalBlockEntity.java',
     'ItemStackHandler(1)','ForgeCapabilities.ITEM_HANDLER','NODE_RADIUS = 8','CHARGE_INTERVAL = 5','RESCAN_INTERVAL = 100','CENTIVIS_PER_NODE_POINT = 100',
     'WandRodType.WOOD','WandCapType.IRON','drainForPedestal','firstPrimalWithRoom','addRealVis(target, aspect, CENTIVIS_PER_NODE_POINT)','comparatorSignal')
need('src/main/java/com/darkifov/thaumcraft/block/TC4WandPedestalFocusBlock.java',
     'TC4_WAND_PEDESTAL.get()','pos.below()','baseState.use')
need('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java','TC4EssentiaLampBlockEntity lamp','lamp.suctionType','lamp.suctionAmount')
need('src/main/java/com/darkifov/thaumcraft/essentia/EssentiaSuctionResolver.java','TC4EssentiaLampBlockEntity lamp')
need('src/main/java/com/darkifov/thaumcraft/blockentity/AuraNodeBlockEntity.java','drainForPedestal','jarred')
need('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java','TC4_WAND_PEDESTAL_BLOCK_ENTITY','TC4WandPedestalRenderer')
main=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for rid in ('tc4_block_lamp_growth','tc4_block_lamp_fertility','tc4_block_wand_pedestal','tc4_block_wand_pedestal_focus'):
    assert f'BLOCKS.register("{rid}"' in main
    assert f'ITEMS.register("{rid}"' in main
    assert f'Map.entry("{rid}"' in main
    for rel in (f'src/main/resources/assets/thaumcraft/blockstates/{rid}.json',
                f'src/main/resources/assets/thaumcraft/models/block/{rid}.json',
                f'src/main/resources/assets/thaumcraft/models/item/{rid}.json',
                f'src/main/resources/data/thaumcraft/loot_tables/blocks/{rid}.json'):
        assert (root/rel).is_file(), rel
registry=text('src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java')
entries_part=registry.split('private static final Map<String, Entry> BY_ID')[0]
entries=re.findall(r'e\("([^"]+)"\s*,',entries_part)
switch=registry[registry.index('private static Item createItem'):registry.index('public static Entry[] entries')]
case_groups=re.findall(r'case\s+((?:"[^"]+"\s*,?\s*)+)->',switch)
dedicated={x for g in case_groups for x in re.findall(r'"([^"]+)"',g)}
pre={
 'tc4_crystalessence','tc4_block_banner','tc4_bath_salts','tc4_bucket_pure','tc4_bucket_death',
 'tc4_block_arcane_spa','tc4_block_arcane_bore_base','tc4_block_arcane_bore','tc4_block_arcane_ear',
 'tc4_block_arcane_lamp','tc4_block_arcane_pressure_plate','tc4_arcanedoor','tc4_block_levitator',
 'tc4_jar_brain','tc4_mirrorframe','tc4_mirrorframe2','tc4_mirrorhand',
 'tc4_block_lamp_growth','tc4_block_lamp_fertility','tc4_block_wand_pedestal','tc4_block_wand_pedestal_focus'}
skipped={'tc4_block_focal_manipulator','tc4_block_thaumium','tc4_block_tallow','tc4_block_crystal_cluster'}
fallback=sorted(x for x in entries if x not in dedicated and x not in pre and x not in skipped)
assert fallback == [], fallback
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
assert manifest['version'] in ('11.63.39','11.63.40','11.63.41', '11.63.42','11.63.43','11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58', '11.63.59', '11.63.60', '11.63.61')
assert len(manifest['tests'])>=291
ids={x['id'] for x in manifest['tests']}
for tid in ('blocks.tc4_growth_lamp_herba_cycle','blocks.tc4_fertility_lamp_victus_population','blocks.tc4_wand_pedestal_node_charge','blocks.tc4_focused_wand_pedestal_compound'):
    assert tid in ids
manifest_by_id={x['id']:x for x in manifest['tests']}
assert '100 centivis' in manifest_by_id['blocks.tc4_wand_pedestal_node_charge']['expected']
assert '100 centivis' in manifest_by_id['blocks.tc4_focused_wand_pedestal_compound']['expected']
for wf in ('build.yml','release.yml'):
    assert 'python3 tools/tc4_116339_final_block_systems_guard.py' in text(f'.github/workflows/{wf}')
for artifact in ('TC4_11.63.39_FINAL_BLOCK_SYSTEMS_PORT_REPORT_RU.md','TC4_11.63.39_BUILD_STATUS.txt','TC4_11.63.39_REMAINING_OBJECTS_AUDIT_RU.md','reports/remaining_objects_v11.63.39.json'):
    assert (root/artifact).is_file(), artifact
print('TC4 v11.63.39 final block systems guard: PASS')
