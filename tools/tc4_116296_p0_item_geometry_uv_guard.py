#!/usr/bin/env python3
from pathlib import Path
import json, sys
ROOT=Path(__file__).resolve().parents[1]
J=ROOT/'src/main/java/com/darkifov/thaumcraft'
R=ROOT/'src/main/resources/assets/thaumcraft'
checks=[]
def check(name, cond):
    checks.append((name,bool(cond)))
def text(p): return p.read_text(encoding='utf-8')
def data(p): return json.loads(text(p))

check('version_116296', '11.63.23' in text(ROOT/'build.gradle'))
mod=text(J/'ThaumcraftMod.java')
for cls in ['BellowsBlockItem','AlchemicalCentrifugeBlockItem','InfusionMatrixBlockItem','BrainJarBlockItem']:
    check('registry_'+cls, ('new '+cls+'(') in mod)
client=text(J/'client/ClientModEvents.java')
for token in ['TC4BellowsModel.FRAME_LAYER','TC4BellowsModel.BAG_LAYER','TC4CentrifugeModel.LAYER']:
    check('layer_'+token.replace('.','_'), token in client)

bell=text(J/'client/render/model/TC4BellowsModel.java')
for token in ['texOffs(0, 0)','texOffs(0, 36)','texOffs(48, 0)','LayerDefinition.create(mesh, 64, 32)',
              '12.03333F','0.125F + bounded * 0.875F']:
    check('bellows_'+token.replace(' ','_'), token in bell)
cent=text(J/'client/render/model/TC4CentrifugeModel.java')
for token in ['texOffs(16, 0)','texOffs(0, 16)','texOffs(20, 16)',
              'addBox(-1.5F, -4.0F, -1.5F, 3.0F, 8.0F, 3.0F)','LayerDefinition.create(mesh, 64, 32)']:
    check('centrifuge_'+token.replace(' ','_'), token in cent)
renderer=text(J/'client/render/AlchemicalCentrifugeRenderer.java')
check('centrifuge_original_texture', 'textures/models/centrifuge.png' in renderer)
check('centrifuge_no_generic_uv', 'Generic UVs' not in renderer and 'private static void cube' not in renderer)

for item in ['bellows','alchemical_centrifuge','infusion_matrix','tc4_jar_brain']:
    check(item+'_builtin_entity', data(R/f'models/item/{item}.json').get('parent')=='builtin/entity')
check('matrix_eight_piece_renderer', 'for (int a = 0; a < 2; a++)' in text(J/'client/render/InfusionMatrixItemRenderer.java'))
brain=text(J/'client/render/BrainJarItemRenderer.java')
check('brain_real_block', 'renderSingleBlock' in brain)
check('brain_brine', 'BRINE_TEXTURE' in brain and 'box(matrix, brine' in brain)

res=data(R/'models/block/essentia_reservoir.json')
check('reservoir_obj_loader', res.get('loader')=='forge:obj')
check('reservoir_obj_path', res.get('model')=='thaumcraft:models/block/tc4_essentia_reservoir.obj')
check('reservoir_obj_exists', (R/'models/block/tc4_essentia_reservoir.obj').exists())
check('reservoir_mtl_exists', (R/'models/block/tc4_essentia_reservoir.mtl').exists())
adv=data(R/'models/item/advanced_alchemical_furnace.json')
check('adv_item_obj_loader', adv.get('loader')=='forge:obj')
check('adv_item_obj_path', adv.get('model')=='thaumcraft:models/block/tc4_advanced_alchemical_furnace_item.obj')
check('adv_item_obj_exists', (R/'models/block/tc4_advanced_alchemical_furnace_item.obj').exists())
check('adv_item_not_generated', adv.get('parent')!='minecraft:item/generated')

failed=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+': '+n)
print(f'SUMMARY: {len(checks)-len(failed)}/{len(checks)} PASS')
sys.exit(1 if failed else 0)
