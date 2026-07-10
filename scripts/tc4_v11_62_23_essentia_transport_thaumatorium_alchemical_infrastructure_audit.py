#!/usr/bin/env python3
"""v11.62.23 Essentia transport + Thaumatorium + alchemical infrastructure audit."""
from pathlib import Path
import json, sys
ROOT=Path(__file__).resolve().parents[1]
errors=[]
def read(rel):
 p=ROOT/rel
 if not p.exists(): errors.append(f'missing: {rel}'); return ''
 return p.read_text(encoding='utf-8')
def load(rel):
 try: return json.loads(read(rel))
 except Exception as exc: errors.append(f'invalid json {rel}: {exc}'); return {}
def require(rel,*needles):
 t=read(rel)
 for n in needles:
  if n not in t: errors.append(f'{rel}: missing {n!r}')
def recipe(name,result,pattern,aspects):
 rel=f'src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/{name}.json'
 d=load(rel)
 if d.get('result',{}).get('item')!=result: errors.append(f'{name}: result mismatch')
 if d.get('pattern')!=pattern: errors.append(f'{name}: pattern mismatch')
 if d.get('aspects')!=aspects: errors.append(f'{name}: aspects mismatch {d.get("aspects")}')
 if not d.get('v11_62_23_strict_original'): errors.append(f'{name}: strict marker missing')
 return d

recipe('tc4_alchemyfurnace','thaumcraft:alchemical_furnace',['SCS','SFS','SSS'],{'IGNIS':5,'AQUA':5})
recipe('tc4_alembic','thaumcraft:alembic',['FIG','IBI','I I'],{'AER':5,'AQUA':5})
recipe('tc4_tube','thaumcraft:essentia_tube',[' Q ','IGI',' B '],{'AQUA':5,'ORDO':5})
recipe('tc4_tubebuffer','thaumcraft:essentia_tube_buffer',['PVP','T T','PRP'],{'AQUA':5,'ORDO':5})
recipe('tc4_centrifuge','thaumcraft:alchemical_centrifuge',[' T ','ACP',' T '],{'AQUA':5,'ORDO':5,'PERDITIO':5})
recipe('tc4_alchemicalconstruct','thaumcraft:thaumatorium',['VTF','TWT','FTV'],{'AQUA':5,'ORDO':5})
recipe('tc4_mnemonicmatrix','thaumcraft:mnemonic_matrix',['IAI','ABA','IAI'],{'IGNIS':5,'AQUA':5,'ORDO':5})
recipe('tc4_advalchemyconstruct','thaumcraft:advanced_alchemical_furnace',['VAV','APA','VAV'],{'AQUA':10,'ORDO':30,'TERRA':10})
for name,result,aspects in [
 ('tc4_tubevalve','thaumcraft:essentia_valve',{'AQUA':5,'ORDO':5}),
 ('tc4_tubefilter','thaumcraft:essentia_tube_filter',{'AQUA':5,'ORDO':16}),
 ('tc4_tuberestrict','thaumcraft:essentia_tube_restrict',{'AQUA':5,'TERRA':16}),
 ('tc4_tubeoneway','thaumcraft:essentia_tube_oneway',{'AQUA':5,'ORDO':8,'PERDITIO':8})]:
 recipe(name,result,[],aspects)

require('src/main/java/com/darkifov/thaumcraft/blockentity/ThaumatoriumBlockEntity.java',
 'ORIGINAL_CRAFT_INTERVAL_TICKS = 5','ORIGINAL_SUCTION = 128','ORIGINAL_HEAT_REFRESH_TICKS = 40',
 'fillOneFromAdjacentTube','essentia.removeAll(cost)','TC4ItemTransferRuntime.insert','1 + matrices * 2',
 'acceptEssentiaFromGolem','worldPosition.below(2)')
require('src/main/java/com/darkifov/thaumcraft/blockentity/AlchemicalCentrifugeBlockEntity.java',
 'ORIGINAL_INPUT_SUCTION = 64','ORIGINAL_PROCESS_TICKS = 39','ORIGINAL_DRAW_INTERVAL_TICKS = 5',
 'aspect.isPrimal()','level.random.nextBoolean()','face == Direction.DOWN ? ORIGINAL_INPUT_SUCTION : 0')
require('src/main/java/com/darkifov/thaumcraft/client/render/AlchemicalCentrifugeRenderer.java',
 'ModelCentrifuge','tile.rotation(partialTick)','textures/models/centrifuge.png')
require('src/main/java/com/darkifov/thaumcraft/blockentity/AlchemicalFurnaceBlockEntity.java',
 'implements WorldlyContainer','CAPACITY = 50','ADVANCED_CAPACITY = 500','MAX_BELLOWS = 4',
 'return 6400;','side == Direction.DOWN ? FUEL_SLOT : INPUT_SLOT','ForgeHooks.getBurnTime')
require('src/main/java/com/darkifov/thaumcraft/essentia/TC4DistillationRuntime.java',
 'ORIGINAL_MAX_ALEMBICS_ABOVE_FURNACE = 5','ORIGINAL_DISTILLATION_INTERVAL_TICKS = 40',
 'ORIGINAL_ALUMENTUM_INTERVAL_TICKS = 20','served.add(stored)','aspectFilter()')
require('src/main/java/com/darkifov/thaumcraft/blockentity/AlembicBlockEntity.java',
 'CAPACITY = 32','aspectFilter','private Direction facing','face != Direction.DOWN')
require('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java',
 'CentrifugeSource','ThaumatoriumBlockEntity.resolveAt','alembic.canOutputTo')
require('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java',
 'CentrifugeEssentiaEndpoint','ThaumatoriumEssentiaEndpoint','simulateEssentiaAcceptance')
resolver=read('src/main/java/com/darkifov/thaumcraft/essentia/EssentiaSuctionResolver.java')
if 'instanceof AlchemicalFurnaceBlockEntity' in resolver: errors.append('ordinary furnace still leaks essentia directly into tubes')
if (ROOT/'src/main/resources/data/thaumcraft/recipes/alchemical_furnace.json').exists(): errors.append('stale vanilla alchemical furnace recipe still present')
require('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java','ALCHEMICAL_CENTRIFUGE','ALCHEMICAL_CENTRIFUGE_BLOCK_ENTITY')
require('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java','AlchemicalCentrifugeRenderer::new')
require('build.gradle',"version = '11.62.23'","version = '11.62.22'")
require('src/main/resources/META-INF/mods.toml','version="11.62.23"','version="11.62.22"')
require('.github/workflows/main.yml','tc4_v11_62_23_essentia_transport_thaumatorium_alchemical_infrastructure_audit.py','v11.62.23-github-jar','v11.62.23-build-reports')
mapd=load('src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_v11_62_23_essentia_transport_thaumatorium_alchemical_infrastructure.json')
if mapd.get('version')!='11.62.23' or not mapd.get('strict_original'): errors.append('v11.62.23 mapping metadata mismatch')
if errors:
 print('TC4 v11.62.23 Essentia/Thaumatorium/alchemical infrastructure audit FAILED:')
 for e in errors: print(' -',e)
 sys.exit(1)
print('TC4 v11.62.23 Essentia transport + Thaumatorium + alchemical infrastructure audit: OK')
