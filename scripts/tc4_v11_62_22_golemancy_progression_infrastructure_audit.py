#!/usr/bin/env python3
"""v11.62.22 exact Golemancy progression + capability infrastructure audit."""
from pathlib import Path
import json, sys
ROOT=Path(__file__).resolve().parents[1]
errors=[]

def read(rel):
 p=ROOT/rel
 if not p.exists():
  errors.append(f'missing: {rel}')
  return ''
 return p.read_text(encoding='utf-8')

def load(rel):
 try: return json.loads(read(rel))
 except Exception as exc:
  errors.append(f'invalid json {rel}: {exc}'); return {}

def require(rel,*needles):
 text=read(rel)
 for n in needles:
  if n not in text: errors.append(f'{rel}: missing {n!r}')

def recipe(rel,catalyst,result,aspects):
 d=load(rel)
 if d.get('catalyst') != catalyst: errors.append(f'{rel}: catalyst {d.get("catalyst")} != {catalyst}')
 if d.get('result',{}).get('item') != result: errors.append(f'{rel}: result mismatch')
 if d.get('aspects') != aspects: errors.append(f'{rel}: aspects mismatch {d.get("aspects")}')
 if not d.get('v11_62_22_strict_original'): errors.append(f'{rel}: strict marker missing')
 return d

bodies=[
('golemstraw','minecraft:hay_block','thaumcraft:tc4_golem_straw',4),
('golemwood','thaumcraft:greatwood_log','thaumcraft:tc4_golem_wood',4),
('golemtallow','thaumcraft:tc4_block_tallow','thaumcraft:tc4_golem_tallow',8),
('golemclay','minecraft:bricks','thaumcraft:tc4_golem_clay',4),
('golemflesh','thaumcraft:flesh_block','thaumcraft:tc4_golem_flesh',8),
('golemstone','minecraft:stone_bricks','thaumcraft:tc4_golem_stone',4),
('golemiron','minecraft:iron_block','thaumcraft:tc4_golem_iron',4),
('golemthaumium','thaumcraft:tc4_block_thaumium','thaumcraft:tc4_golem_thaumium',8),
]
for name,cat,res,amount in bodies:
 recipe(f'src/main/resources/data/thaumcraft/thaumcraft_alchemy/tc4_{name}.json',cat,res,{'HUMANUS':amount,'MOTUS':amount,'SPIRITUS':amount})

core_alchemy=[
('coregather','thaumcraft:tc4_golem_core_blank','thaumcraft:tc4_golem_core_gather',{'LUCRUM':5,'TERRA':5}),
('corefill','thaumcraft:tc4_golem_core_blank','thaumcraft:tc4_golem_core_fill',{'FAMES':5,'VACUOS':5}),
('coreempty','thaumcraft:tc4_golem_core_blank','thaumcraft:tc4_golem_core_empty',{'LUCRUM':5,'VACUOS':5}),
('coreharvest','thaumcraft:tc4_golem_core_blank','thaumcraft:tc4_golem_core_harvest',{'METO':5,'MESSIS':5}),
('coreguard','thaumcraft:tc4_golem_core_blank','thaumcraft:tc4_golem_core_guard',{'TELUM':5,'VINCULUM':5}),
('corebutcher','thaumcraft:tc4_golem_core_guard','thaumcraft:tc4_golem_core_butcher',{'CORPUS':5,'BESTIA':5}),
('coreliquid','thaumcraft:tc4_golem_core_blank','thaumcraft:tc4_golem_core_liquid',{'AQUA':5,'VACUOS':5}),
]
for name,cat,res,asp in core_alchemy:
 recipe(f'src/main/resources/data/thaumcraft/thaumcraft_alchemy/tc4_{name}.json',cat,res,asp)

arc='src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/'
checks={
 'tc4_golembell.json':([' QQ',' QQ','S  '],{'Q':'minecraft:quartz','S':'minecraft:stick'},'thaumcraft:tc4_ironbell'),
 'tc4_coreblank.json':([' C ','CNC',' C '],{'C':'minecraft:brick','N':'thaumcraft:tc4_nitor'},'thaumcraft:tc4_golem_core_blank'),
 'tc4_upgradeair.json':(['NNN','NCN','NNN'],{'N':'minecraft:gold_nugget','C':'thaumcraft:aer_shard'},'thaumcraft:tc4_golem_upgrade_air'),
 'tc4_upgradeearth.json':(['NNN','NCN','NNN'],{'N':'minecraft:gold_nugget','C':'thaumcraft:terra_shard'},'thaumcraft:tc4_golem_upgrade_earth'),
 'tc4_upgradefire.json':(['NNN','NCN','NNN'],{'N':'minecraft:gold_nugget','C':'thaumcraft:ignis_shard'},'thaumcraft:tc4_golem_upgrade_fire'),
 'tc4_upgradewater.json':(['NNN','NCN','NNN'],{'N':'minecraft:gold_nugget','C':'thaumcraft:aqua_shard'},'thaumcraft:tc4_golem_upgrade_water'),
 'tc4_upgradeorder.json':(['NNN','NCN','NNN'],{'N':'minecraft:gold_nugget','C':'thaumcraft:ordo_shard'},'thaumcraft:tc4_golem_upgrade_order'),
 'tc4_upgradeentropy.json':(['NNN','NCN','NNN'],{'N':'minecraft:gold_nugget','C':'thaumcraft:perditio_shard'},'thaumcraft:tc4_golem_upgrade_entropy'),
 'tc4_tinyhat.json':([' C ',' G ','CCC'],{'C':'minecraft:black_wool','G':'minecraft:gold_ingot'},'thaumcraft:tc4_golemdecotophat'),
 'tc4_tinyfez.json':(['CCS','CCS','  S'],{'C':'minecraft:red_wool','S':'minecraft:string'},'thaumcraft:tc4_golemdecofez'),
 'tc4_tinybowtie.json':(['CSC','C C'],{'C':'minecraft:black_wool','S':'minecraft:string'},'thaumcraft:tc4_golemdecobowtie'),
 'tc4_tinyglasses.json':(['GIG'],{'G':'minecraft:glass','I':'minecraft:iron_ingot'},'thaumcraft:tc4_golemdecoglasses'),
 'tc4_tinydart.json':(['AIA','ADA','AIA'],{'A':'minecraft:arrow','I':'minecraft:iron_ingot','D':'minecraft:dispenser'},'thaumcraft:tc4_golemdecodart'),
 'tc4_tinyvisor.json':(['IHI'],{'I':'minecraft:iron_ingot','H':'minecraft:iron_helmet'},'thaumcraft:tc4_golemdecovisor'),
 'tc4_tinyarmor.json':(['I I','IAI'],{'I':'minecraft:iron_ingot','A':'minecraft:iron_chestplate'},'thaumcraft:tc4_golemdecoarmor'),
 'tc4_tinyhammer.json':(['III','III',' I '],{'I':'minecraft:iron_ingot'},'thaumcraft:tc4_golemdecomace'),
}
for fn,(pattern,key,result) in checks.items():
 d=load(arc+fn)
 if d.get('pattern')!=pattern: errors.append(f'{fn}: pattern mismatch')
 if d.get('key')!=key: errors.append(f'{fn}: key mismatch {d.get("key")}')
 if d.get('result',{}).get('item')!=result: errors.append(f'{fn}: result mismatch')
 if not d.get('v11_62_22_strict_original'): errors.append(f'{fn}: strict marker missing')

inf='src/main/resources/data/thaumcraft/thaumcraft_infusion/'
inf_checks={
 'tc4_corealchemy.json':('thaumcraft:tc4_golem_core_liquid',['thaumcraft:essentia_jar','minecraft:potion','minecraft:potion','minecraft:potion'],'thaumcraft:tc4_golem_core_essentia'),
 'tc4_coresorting.json':('thaumcraft:tc4_brain',['thaumcraft:tc4_golem_core_fill','minecraft:comparator','thaumcraft:tc4_golem_core_empty','minecraft:paper'],'thaumcraft:tc4_golem_core_sorting'),
 'tc4_corelumber.json':('thaumcraft:tc4_golem_core_harvest',['thaumcraft:tc4_elementalaxe','minecraft:iron_axe','minecraft:iron_axe','minecraft:iron_axe'],'thaumcraft:tc4_golem_core_lumber'),
 'tc4_corefishing.json':('thaumcraft:tc4_golem_core_harvest',['minecraft:fishing_rod','minecraft:cod','minecraft:pufferfish','minecraft:salmon'],'thaumcraft:tc4_golem_core_fish'),
 'tc4_coreuse.json':('thaumcraft:tc4_golem_core_empty',['minecraft:comparator','minecraft:flint_and_steel','minecraft:shears','minecraft:lever'],'thaumcraft:tc4_golem_core_use'),
}
for fn,(cat,components,result) in inf_checks.items():
 d=load(inf+fn)
 if d.get('catalyst')!=cat or d.get('components')!=components or d.get('result',{}).get('item')!=result:
  errors.append(f'{fn}: exact infusion mapping mismatch')

for name,_,body,_ in bodies:
 suffix=body.split('tc4_golem_',1)[1]
 d=load(inf+f'tc4_advancedgolem_{suffix}.json')
 r=d.get('result',{})
 if d.get('catalyst')!=body or r.get('item')!=body or r.get('output_nbt_label')!='advanced' or r.get('output_nbt_value')!=1:
  errors.append(f'advanced golem {suffix}: central-output NBT mismatch')
 if d.get('components')!=['minecraft:redstone','minecraft:glowstone_dust','minecraft:gunpowder','thaumcraft:essentia_jar','thaumcraft:tc4_brain']:
  errors.append(f'advanced golem {suffix}: components mismatch')

require('src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeItemResolver.java',
 'field_151097_aZ", "minecraft:shears"','field_151118_aC", "minecraft:brick"',
 'field_151132_bS", "minecraft:comparator"','field_150367_z", "minecraft:dispenser"',
 'blockCosmeticSolid:4", "thaumcraft:tc4_block_thaumium"','blockCosmeticSolid:5", "thaumcraft:tc4_block_tallow"')
require('src/main/java/com/darkifov/thaumcraft/golem/GolemItemHandlerContainerAdapter.java',
 'IItemHandlerModifiable','extractItem','insertItem','insertIntoSlots')
require('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java',
 'ForgeCapabilities.ITEM_HANDLER','ForgeCapabilities.FLUID_HANDLER','ForgeCapabilities.FLUID_HANDLER_ITEM',
 'AILiquidGather:capability','AILiquidEmpty:capability','ReservoirEssentiaEndpoint','AlembicEssentiaEndpoint',
 'scheduler:short-backoff','scheduler:stuck-recovery','schedulerStuckTicks >= 100')
placer=read('src/main/java/com/darkifov/thaumcraft/item/TC4GolemPlacerItem.java')
if 'Research locked: GOLEMS' in placer: errors.append('synthetic GOLEMS placement gate still present')
require('build.gradle',"version = '11.62.22'","version = '11.62.21'")
require('src/main/resources/META-INF/mods.toml','version="11.62.22"')
require('.github/workflows/main.yml','tc4_v11_62_22_golemancy_progression_infrastructure_audit.py','v11.62.22-github-jar','v11.62.22-build-reports')
mapd=load('src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_v11_62_22_golemancy_progression_infrastructure.json')
if mapd.get('version')!='11.62.22' or not mapd.get('strict_original'): errors.append('v11.62.22 mapping metadata mismatch')
if errors:
 print('TC4 v11.62.22 Golemancy progression/infrastructure audit FAILED:')
 for e in errors: print(' -',e)
 sys.exit(1)
print('TC4 v11.62.22 Golemancy progression + infrastructure audit: OK')
