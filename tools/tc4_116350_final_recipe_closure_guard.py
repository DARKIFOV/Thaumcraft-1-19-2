#!/usr/bin/env python3
"""Static contract guard for v11.63.50 final 258/258 original runtime-registration closure."""
from __future__ import annotations
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
A="src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/"
I="src/main/resources/data/thaumcraft/thaumcraft_infusion/"
def read(p): return (ROOT/p).read_text(encoding="utf-8")
def load(p): return json.loads(read(p))
def req(c,m):
    if not c: raise AssertionError(m)
def arc(name, pattern, key, aspects, result, count=1):
    d=load(A+name)
    req(d['pattern']==pattern,f'{name}: pattern')
    if key is not None: req(d.get('key')==key,f'{name}: key')
    req(d['aspects']==aspects,f'{name}: aspects')
    req(d['result']=={'item':result,'count':count},f'{name}: result')
    req(d.get('v11_63_50_exact_source') is True,f'{name}: marker')
def main():
    req(any(f"version = '{v}'" in read('build.gradle') for v in ('11.63.50','11.63.51','11.63.52', '11.63.53', '11.63.54', '11.63.55')),'build version')
    req(any(f'version="{v}"' in read('src/main/resources/META-INF/mods.toml') for v in ('11.63.50','11.63.51','11.63.52', '11.63.53', '11.63.54', '11.63.55')),'mods version')
    g=load('src/main/resources/data/thaumcraft/thaumcraft_alchemy/tc4_golemflesh.json')
    req(g['catalyst']=='thaumcraft:flesh_block' and g['result']=={'item':'thaumcraft:tc4_golem_flesh','count':1},'GolemFlesh io')
    req(g['aspects']=={'HUMANUS':8,'MOTUS':8,'SPIRITUS':8} and g.get('v11_63_50_exact_source') is True,'GolemFlesh aspects')
    arc('tc4_wardedglass.json',['GGG','WBW','GGG'],{'B':'thaumcraft:tc4_brain','G':'minecraft:glass','W':'thaumcraft:greatwood_planks'},{'AQUA':5,'ORDO':10,'TERRA':5,'IGNIS':5},'thaumcraft:warded_glass',8)
    arc('tc4_node_relay.json',[' I ','ISI',' I '],{'I':'minecraft:iron_ingot','S':'thaumcraft:balanced_shard'},{'IGNIS':8,'ORDO':8},'thaumcraft:vis_relay',2)
    arc('tc4_nodechargerelay.json',[' R ','W W','I I'],{'I':'minecraft:iron_ingot','R':'thaumcraft:vis_relay','W':'thaumcraft:greatwood_wand_core'},{'IGNIS':16,'ORDO':16,'AER':16},'thaumcraft:vis_charge_relay')
    arc('tc4_jar_void.json',['O','J','P'],{'O':'minecraft:obsidian','J':'thaumcraft:essentia_jar','P':'minecraft:blaze_powder'},{'AQUA':5,'PERDITIO':15},'thaumcraft:void_essentia_jar')
    arc('tc4_hungrychest.json',['WTW','W W','WWW'],{'W':'#minecraft:planks','T':'minecraft:oak_trapdoor'},{'AER':5,'ORDO':3,'PERDITIO':3},'thaumcraft:hungry_chest')
    arc('tc4_alchemyfurnace.json',['SCS','SFS','SSS'],{'S':'thaumcraft:arcane_stone','C':'thaumcraft:crucible','F':'minecraft:furnace'},{'IGNIS':5,'AQUA':5},'thaumcraft:alchemical_furnace')
    f=load(A+'tc4_tubefilter.json')
    req(f['pattern']==[] and f['ingredients']==['thaumcraft:essentia_tube','thaumcraft:tc4_filter'],'TubeFilter inputs')
    req(f['aspects']=={'AQUA':5,'ORDO':16} and f.get('v11_63_50_exact_source') is True,'TubeFilter contract')
    arc('tc4_centrifuge.json',[' T ','ACP',' T '],{'T':'thaumcraft:essentia_tube','A':'thaumcraft:alembic','C':'thaumcraft:thaumatorium','P':'minecraft:piston'},{'AQUA':5,'ORDO':5,'PERDITIO':5},'thaumcraft:alchemical_centrifuge')
    mapping={'straw':'thaumcraft:tc4_golem_straw','wood':'thaumcraft:tc4_golem_wood','tallow':'thaumcraft:tc4_golem_tallow','clay':'thaumcraft:tc4_golem_clay','flesh':'thaumcraft:tc4_golem_flesh','stone':'thaumcraft:tc4_golem_stone','iron':'thaumcraft:tc4_golem_iron','thaumium':'thaumcraft:tc4_golem_thaumium'}
    comps=['minecraft:shears','minecraft:glowstone_dust','minecraft:gunpowder','thaumcraft:essentia_jar','thaumcraft:tc4_brain']
    for suffix,item in mapping.items():
        d=load(I+f'tc4_advancedgolem_{suffix}.json')
        req(d['catalyst']==item and d['result']['item']==item,f'advanced {suffix}: catalyst/result')
        req(d['components']==comps,f'advanced {suffix}: components')
        req(d['aspects']=={'COGNITIO':8,'SENSUS':8,'VICTUS':8} and d['instability']==3,f'advanced {suffix}: cost')
        r=d['result']; req(r['output_nbt_label']=='advanced' and r['output_nbt_type']=='byte' and r['output_nbt_value']==1,f'advanced {suffix}: NBT')
        req(d.get('v11_63_50_exact_source') is True and d.get('v11_63_50_legacy_wildcard_metadata')==32767,f'advanced {suffix}: markers')
    idx=read('src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_original_recipes_index.json')
    req('\\"ADVANCEDGOLEM\\"' in idx and 'new NBTTagByte(1)' in idx and 'itemGolemPlacer, 1, 32767' in idx,'AdvancedGolem source')
    stage=load('src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_stage155_recipe_resolver_exact_pass.json')
    a=next(x for x in stage['entries'] if x.get('key')=='AdvancedGolem')
    req(a['stage155_components']==['minecraft:shears','minecraft:glowstone_dust','minecraft:gunpowder','thaumcraft:tc4_block_essentia_jar','thaumcraft:tc4_brain'],'AdvancedGolem MCP mapping')
    matrix=read('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java')
    req('recipe.hasNbtOutput()' in matrix and 'catalystPedestal.stored().copy()' in matrix and 'central.addTagElement(recipe.outputNbtLabel(), recipe.outputNbt())' in matrix,'central NBT copy runtime')
    closure=load('src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_final_recipe_closure_v11_63_50.json')
    req(closure['exact_runtime_source_record_count']==258 and closure['remaining_unresolved_count']==0 and closure['v11_63_50_promoted_count']==10,'closure counts')
    tests=read('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
    req(tests.count('@GameTest(')>=18,'GameTest count')
    req('finalOriginalArcaneAndGolemFleshRecipesMatchTC4' in tests and 'advancedGolemWildcardFamilyMatchesTC4' in tests,'GameTest methods')
    manifest=load('runtime_artifacts/runtime_test_manifest.template.json')
    req(manifest['version'] in ('11.63.50','11.63.51','11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58', '11.63.59', '11.63.60', '11.63.61','11.63.62','11.63.63','11.63.64','11.63.65') and len(manifest['tests'])>=346,'manifest count/version')
    req('python3 tools/tc4_116350_final_recipe_closure_guard.py' in read('tools/run_full_static_ci_116350.py'),'runner missing guard')
    print('TC4 v11.63.50 final recipe closure guard: PASS (258/258 STATICALLY MAPPED, 18 GameTests, 346 scenarios)')
    return 0
if __name__=='__main__': raise SystemExit(main())
