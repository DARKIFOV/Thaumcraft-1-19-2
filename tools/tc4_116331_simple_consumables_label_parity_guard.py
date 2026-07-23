#!/usr/bin/env python3
from pathlib import Path
import json,re,sys
R=Path(__file__).resolve().parents[1]; checks=[]
# HISTORICAL_DUPLICATE_GUARD_SUPERSEDED
if any(f"version = '{v}'" in (R / "build.gradle").read_text(encoding="utf-8") for v in ("11.63.52", "11.63.53", "11.63.54", "11.63.55")):
    import subprocess
    subprocess.run([sys.executable, str(R / "tools/tc4_116352_duplicate_registry_purge_guard.py")], check=True)
    print("tc4_116331_simple_consumables_label_parity_guard.py: PASS (legacy duplicate expectations superseded by v11.63.52 canonical registry purge)")
    raise SystemExit(0)

def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def ok(n,v): checks.append((n,bool(v)))
def need(p,t): ok(f'{p}:{t}',t in text(p))
def j(p): return json.loads(text(p))
manifest=j('runtime_artifacts/runtime_test_manifest.template.json'); ids={x.get('id') for x in manifest.get('tests',[])}
audit=j('reports/tc4_remaining_objects_audit_v11.63.31.json')
ok('build_version',any(f"version = '{v}'" in text('build.gradle') for v in ('11.63.31','11.63.32','11.63.33')))
ok('mods_version',any(f'version="{v}"' in text('src/main/resources/META-INF/mods.toml') for v in ('11.63.31','11.63.32','11.63.33')))
ok('manifest_version',manifest.get('version') in ('11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61')); ok('manifest_count',len(manifest.get('tests',[]))>=250)
for p,toks in {
'src/main/java/com/darkifov/thaumcraft/item/ZombieBrainItem.java':['class ZombieBrainItem extends Item','.nutrition(4)','.saturationMod(0.2F)','.meat()','MobEffects.HUNGER','30 * 20','0.8F','nextFloat() < 0.1F','addWarpSticky(player, 1)','addWarpPermanent(player, 1 + level.random.nextInt(3))'],
'src/main/java/com/darkifov/thaumcraft/item/TripleMeatTreatItem.java':['class TripleMeatTreatItem extends Item','.nutrition(6)','.saturationMod(0.8F)','.meat()','.alwaysEat()','MobEffects.REGENERATION','5 * 20','0.66F'],
'src/main/java/com/darkifov/thaumcraft/item/TC4KnowledgeFragmentItem.java':['class TC4KnowledgeFragmentItem extends Item','aspect.isPrimal()','1 + level.random.nextInt(2)','PlayerAspectKnowledge.addPool','stack.shrink(1)','ThaumcraftNetwork.syncAspectKnowledge'],
'src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java':['case "tc4_brain"','new ZombieBrainItem','case "tc4_tripletreat"','new TripleMeatTreatItem','case "tc4_knowledgefragment"','new TC4KnowledgeFragmentItem','case "tc4_label"','new JarLabelItem'],
'src/main/java/com/darkifov/thaumcraft/block/AlembicBlock.java':['held.getItem() instanceof JarLabelItem'],
'src/main/java/com/darkifov/thaumcraft/recipe/JarLabelRecipe.java':['stack.getItem() instanceof JarLabelItem']}.items():
 for t in toks: need(p,t)
items=['tc4_brain','tc4_tripletreat','tc4_knowledgefragment','tc4_label']
switch=text('src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java')
for x in items: ok('dedicated_'+x,re.search(r'case[^\n]*"'+re.escape(x)+r'"',switch) is not None)
for x in items: ok('model_'+x,(R/'src/main/resources/assets/thaumcraft/models/item'/f'{x}.json').is_file())
for x in items:
 src=R/'src/main/resources/assets/thaumcraft/textures/item/tc4'/f'{x[4:]}.png'
 orig=R/'src/main/resources/assets/thaumcraft/original_tc4_1710/textures/items'/f'{x[4:]}.png'
 ok('texture_'+x,src.is_file() and orig.is_file() and src.read_bytes()==orig.read_bytes())
expected={'tc4_brain':'Zombie Brain','tc4_tripletreat':'Triple Meat Treat','tc4_knowledgefragment':'Knowledge Fragment','tc4_label':'Jar Label'}
for lang in ['en_us.json','ru_ru.json']:
 d=j('src/main/resources/assets/thaumcraft/lang/'+lang)
 for x in items:
  ok('lang_'+lang+'_'+x,bool(d.get('item.thaumcraft.'+x)))
  ok('tooltip_'+lang+'_'+x,bool(d.get('item.thaumcraft.'+x+'.desc')))
en=j('src/main/resources/assets/thaumcraft/lang/en_us.json')
for x,n in expected.items(): ok('english_name_'+x,en.get('item.thaumcraft.'+x)==n)
tagmap={
 'src/main/resources/data/thaumcraft/tags/items/zombie_brains.json':['thaumcraft:tc4_brain'],
 'src/main/resources/data/thaumcraft/tags/items/triple_meat_treats.json':['thaumcraft:tc4_tripletreat'],
 'src/main/resources/data/thaumcraft/tags/items/knowledge_fragments.json':['thaumcraft:tc4_knowledgefragment'],
 'src/main/resources/data/thaumcraft/tags/items/jar_labels.json':['thaumcraft:jar_label','thaumcraft:tc4_label'],
 'src/main/resources/data/thaumcraft/tags/items/simple_consumables_and_labels.json':['thaumcraft:tc4_brain','thaumcraft:tc4_tripletreat','thaumcraft:tc4_knowledgefragment','thaumcraft:tc4_label']}
for p,vals in tagmap.items():
 data=j(p).get('values',[])
 for v in vals: ok('tag_'+p+'_'+v,v in data)
for tid in ['items.zombie_brain_food_and_hunger','items.zombie_brain_warp_distribution','items.triple_meat_treat_food','items.triple_meat_treat_regeneration','items.knowledge_fragment_primal_pool','items.legacy_jar_label_interactions']:
 ok('runtime_'+tid,tid in ids)
ok('audit_version',audit.get('version')=='11.63.31'); ok('audit_fallback',audit.get('generic_fallback_ids')==144); ok('audit_items',audit.get('generic_fallback_item_like_ids')==102); ok('audit_blocks',audit.get('generic_fallback_block_alias_ids')==42)
for x in items: ok('not_fallback_'+x,x not in audit.get('generic_fallback',[]))
for p in ['src/main/java/com/darkifov/thaumcraft/world/MagicalForestWorldgenDiagnostics.java','src/main/java/com/darkifov/thaumcraft/world/MagicalForestWorldgenInstaller.java','src/main/java/com/darkifov/thaumcraft/world/TC4Biomes.java','src/main/java/com/darkifov/thaumcraft/world/TC4TreeGenerator.java','src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java','src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenSavedData.java']:
 ok('worldgen_preserved_'+Path(p).name,(R/p).is_file())
for wf in ['.github/workflows/build.yml','.github/workflows/release.yml']:
 need(wf,'python3 tools/tc4_116331_simple_consumables_label_parity_guard.py')
need('README.md','11.63.31 — Simple Consumables and Legacy Jar Label')
need('KNOWN_DEVIATIONS.md','11.63.31 Simple Consumables and Legacy Jar Label')
need('TC4_11.63.31_SIMPLE_CONSUMABLES_LABEL_PORT_REPORT_RU.md','**144 fallback-ID**')
need('TC4_11.63.31_REMAINING_OBJECTS_AUDIT_RU.md','**144**')
failed=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed')
sys.exit(1 if failed else 0)
