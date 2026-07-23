#!/usr/bin/env python3
"""Guard for v11.63.10 all-block alpha/render-layer and core geometry repairs."""
from __future__ import annotations
import csv, json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources/assets/thaumcraft'
JAVA=ROOT/'src/main/java/com/darkifov/thaumcraft'
checks=[]
def add(name,ok,detail=''): checks.append((name,bool(ok),detail))
def load(p): return json.loads(p.read_text())

add('project version', "version = '11.63.23'" in (ROOT/'build.gradle').read_text())
# Frozen inventory from the 11.63.10 exhaustive audit.
partial='''advanced_alchemical_furnace aer_crystal amber_bricks aqua_crystal arcane_crafting_terminal eldritch_portal essentia_conversion_monitor essentia_export_bus essentia_import_bus essentia_level_emitter essentia_storage_bus essentia_storage_monitor essentia_terminal extras_water_block flux_gas flux_goo golem_seal_collect_block ignis_crystal matrix_accelerator matrix_stabilizer nitor_light ordo_crystal perditio_crystal taint_fibres tc4_block_crystal_cluster terra_crystal thaumic_crafting_cpu thaumic_me_cable thaumic_me_controller tt_enchanter tt_repairer'''.split()
cutout='''bellows extras_light_block fume_dissipator mnemonic_matrix tce_cactus tce_warded_glass tt_fire_air tt_fire_chaos tt_fire_earth tt_fire_order tt_fire_water tt_funnel tt_gaseous_light tt_gaseous_shadow tt_mob_magnet tt_nitor_gas tt_warp_gate vis_interface'''.split()
add('49-model mismatch inventory',len(partial)+len(cutout)==49,f'{len(partial)} translucent + {len(cutout)} cutout')
for bid in partial:
    p=RES/f'models/block/{bid}.json'; d=load(p) if p.exists() else {}
    add(f'{bid} translucent',d.get('render_type')=='minecraft:translucent')
for bid in cutout:
    p=RES/f'models/block/{bid}.json'; d=load(p) if p.exists() else {}
    add(f'{bid} cutout',d.get('render_type')=='minecraft:cutout')

for bid in ['aer_crystal','aqua_crystal','ignis_crystal','ordo_crystal','perditio_crystal','terra_crystal','tc4_block_crystal_cluster']:
    d=load(RES/f'models/block/{bid}.json')
    add(f'{bid} original OBJ geometry',d.get('loader')=='forge:obj' and d.get('model')=='thaumcraft:models/block/tc4_vcrystal.obj' and d.get('flip_v') is True)
add('crystal OBJ+MTL exist',all((RES/f'models/block/{f}').exists() for f in ['tc4_vcrystal.obj','tc4_vcrystal.mtl']))

goo=load(RES/'models/block/flux_goo.json')
add('Flux Goo 3/16 geometry',len(goo.get('elements',[]))==1 and goo['elements'][0].get('to',[0,0,0])[1]==3)
gas=load(RES/'models/block/flux_gas.json')
add('Flux Gas intersecting sheets',len(gas.get('elements',[]))>=3 and gas.get('ambientocclusion') is False)
bellows=load(RES/'models/block/bellows.json')
add('Bellows five source parts',len(bellows.get('elements',[]))==5 and {e.get('name') for e in bellows['elements']}=={'bottom_plank','middle_plank','top_plank','bag','nozzle'})
adv=load(RES/'models/block/advanced_alchemical_furnace.json')
add('Advanced furnace original OBJ groups',adv.get('loader')=='forge:obj' and adv.get('model')=='thaumcraft:models/block/tc4_advanced_alchemical_furnace.obj')
obj=(RES/'models/block/tc4_advanced_alchemical_furnace.obj').read_text()
add('Advanced furnace Base/Tank materials',all(t in obj for t in ['g Base','usemtl base','g Tank','usemtl tank']))
portal_block=(JAVA/'block/EldritchPortalBlock.java').read_text()
portal_renderer=(JAVA/'client/render/EldritchPortalRenderer.java').read_text()
client=(JAVA/'client/ClientModEvents.java').read_text()
portal_model=load(RES/'models/block/eldritch_portal.json')
add('Portal renderer-only block', 'RenderShape.INVISIBLE' in portal_block and portal_model.get('elements')==[])
add('Portal 16-frame original strip',all(t in portal_renderer for t in ['textures/misc/eldritch_portal.png','frame / 16.0F','(frame + 1) / 16.0F','LightTexture.FULL_BRIGHT','cameraOrientation()']))
add('Portal BER registered',all(t in client for t in ['ELDRITCH_PORTAL_BLOCK_ENTITY','EldritchPortalRenderer::new']))
add('Explicit runtime fallback layers',client.count('v11.63.10: explicit fallback layers')==1 and all(f'ThaumcraftMod.{c}.get()' in client for c in ['ADVANCED_ALCHEMICAL_FURNACE','AER_CRYSTAL','FLUX_GAS','FLUX_GOO','BELLOWS','ELDRITCH_PORTAL','VIS_INTERFACE']))

problems=[{'name':n,'detail':d} for n,ok,d in checks if not ok]
report={'version':'11.63.10','status':'PASS' if not problems else 'FAIL','checks':[{'name':n,'status':'PASS' if ok else 'FAIL','detail':d} for n,ok,d in checks],'problems':problems,'limitations':['Static source/resource contract only.','Runtime lighting, sorting, culling, animation and item transforms remain NOT TESTED.']}
(ROOT/'reports').mkdir(exist_ok=True)
out=ROOT/'reports/tc4_116295_all_block_render_layer_geometry_guard.json'
out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print(f"v11.63.10 all-block render/geometry guard: {len(checks)-len(problems)}/{len(checks)} PASS")
if problems:
 for p in problems: print('FAIL:',p['name'],p['detail'])
raise SystemExit(1 if problems else 0)
