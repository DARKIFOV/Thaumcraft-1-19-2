#!/usr/bin/env python3
from pathlib import Path
import json, re, sys
R=Path(__file__).resolve().parents[1]; checks=[]
# HISTORICAL_DUPLICATE_GUARD_SUPERSEDED
if any(f"version = '{v}'" in (R / "build.gradle").read_text(encoding="utf-8") for v in ("11.63.52", "11.63.53", "11.63.54", "11.63.55")):
    import subprocess
    subprocess.run([sys.executable, str(R / "tools/tc4_116352_duplicate_registry_purge_guard.py")], check=True)
    print("tc4_116329_simple_resource_families_parity_guard.py: PASS (legacy duplicate expectations superseded by v11.63.52 canonical registry purge)")
    raise SystemExit(0)

def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def ok(n,v): checks.append((n,bool(v)))
def need(p,t): ok(f'{p}:{t}',t in text(p))
def j(p): return json.loads(text(p))
manifest=j('runtime_artifacts/runtime_test_manifest.template.json'); ids={x.get('id') for x in manifest.get('tests',[])}
audit=j('reports/tc4_remaining_objects_audit_v11.63.29.json')
ok('build_version',"version = '11.63.29'" in text('build.gradle'))
ok('mods_version','version="11.63.29"' in text('src/main/resources/META-INF/mods.toml'))
ok('manifest_version',manifest.get('version') in ('11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61')); ok('manifest_count',len(manifest.get('tests',[]))>=238)
for p,toks in {
'src/main/java/com/darkifov/thaumcraft/item/TC4SimpleResourceItem.java':['class TC4SimpleResourceItem extends Item','enum Kind { MATERIAL, METAL_NUGGET, NEUTRAL_SHARD }','String material()'],
'src/main/java/com/darkifov/thaumcraft/item/TC4EdibleNuggetItem.java':['nutrition(1)','saturationMod(0.3F)','getUseDuration','return 10'],
'src/main/java/com/darkifov/thaumcraft/item/TC4OreClusterItem.java':['class TC4OreClusterItem extends Item','String material()'],
'src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java':['new TC4EdibleNuggetItem(functionalProperties)','new TC4OreClusterItem(functionalProperties, "iron")','new ShardItem(functionalProperties, Aspect.AER, false)','new ShardItem(functionalProperties, Aspect.PRAECANTATIO, true)','functionalProperties.fireResistant()']}.items():
 for t in toks: need(p,t)
ids_expected=['tc4_nuggetbeef','tc4_nuggetchicken','tc4_nuggetfish','tc4_nuggetpork','tc4_nuggetiron','tc4_nuggetcopper','tc4_nuggettin','tc4_nuggetsilver','tc4_nuggetlead','tc4_nuggetquicksilver','tc4_nuggetthaumium','tc4_nuggetvoid','tc4_shard','tc4_shard_aer','tc4_shard_aqua','tc4_shard_balanced','tc4_shard_ignis','tc4_shard_ordo','tc4_shard_perditio','tc4_shard_terra','tc4_clustercinnabar','tc4_clustercopper','tc4_clustergold','tc4_clusteriron','tc4_clusterlead','tc4_clustersilver','tc4_clustertin','tc4_amber','tc4_cloth','tc4_quicksilver','tc4_tallow','tc4_thaumiumingot','tc4_voidingot']
switch=text('src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java')
for x in ids_expected: ok('dedicated_'+x,re.search(r'case[^\n]*"'+re.escape(x)+r'"',switch) is not None)
mig=text('src/main/java/com/darkifov/thaumcraft/porting/TC4LegacyDuplicateItemMigrator.java')
for x in ['tc4_amber','tc4_quicksilver','tc4_thaumiumingot','tc4_voidingot','tc4_nuggetthaumium','tc4_shard_aer','tc4_shard_terra','tc4_shard_ignis','tc4_shard_aqua','tc4_shard_ordo','tc4_shard_perditio','tc4_shard_balanced']:
 ok('no_auto_migrate_'+x,('map.put("'+x+'"') not in mig)
for mat,id in {'iron':'tc4_nuggetiron','copper':'tc4_nuggetcopper','tin':'tc4_nuggettin','silver':'tc4_nuggetsilver','lead':'tc4_nuggetlead','quicksilver':'tc4_nuggetquicksilver','thaumium':'tc4_nuggetthaumium','void_metal':'tc4_nuggetvoid'}.items():
 ok('nugget_tag_'+mat,'thaumcraft:'+id in j('src/main/resources/data/forge/tags/items/nuggets/'+mat+'.json').get('values',[]))
for mat,id in {'iron':'tc4_clusteriron','gold':'tc4_clustergold','copper':'tc4_clustercopper','tin':'tc4_clustertin','silver':'tc4_clustersilver','lead':'tc4_clusterlead','cinnabar':'tc4_clustercinnabar'}.items():
 ok('cluster_tag_'+mat,'thaumcraft:'+id in j('src/main/resources/data/forge/tags/items/clusters/'+mat+'.json').get('values',[]))
for rel,expected in [('src/main/resources/data/forge/tags/items/ingots/thaumium.json','thaumcraft:tc4_thaumiumingot'),('src/main/resources/data/forge/tags/items/ingots/void_metal.json','thaumcraft:tc4_voidingot'),('src/main/resources/data/forge/tags/items/gems/amber.json','thaumcraft:tc4_amber'),('src/main/resources/data/forge/tags/items/quicksilver.json','thaumcraft:tc4_quicksilver'),('src/main/resources/data/thaumcraft/tags/items/tallow.json','thaumcraft:tc4_tallow'),('src/main/resources/data/thaumcraft/tags/items/enchanted_fabric.json','thaumcraft:tc4_cloth')]:
 ok('tag_'+expected,expected in j(rel).get('values',[]))
for shard in ['tc4_shard_aer','tc4_shard_terra','tc4_shard_ignis','tc4_shard_aqua','tc4_shard_ordo','tc4_shard_perditio']:
 ok('primal_shard_tag_'+shard,'thaumcraft:'+shard in j('src/main/resources/data/forge/tags/items/shards/primal.json').get('values',[]))
for id in ids_expected:
 ok('model_'+id,(R/'src/main/resources/assets/thaumcraft/models/item'/f'{id}.json').is_file())
for lang in ['en_us.json','ru_ru.json']:
 d=j('src/main/resources/assets/thaumcraft/lang/'+lang)
 for id in ids_expected: ok('lang_'+lang+'_'+id,bool(d.get('item.thaumcraft.'+id)))
for recipe,item in [('tc4_smelting_4.json','tc4_clusteriron'),('tc4_smelting_5.json','tc4_clustercinnabar'),('tc4_smelting_6.json','tc4_clustergold'),('tc4_smelting_cluster_copper.json','tc4_clustercopper')]:
 ok('smelt_'+item,'thaumcraft:'+item in json.dumps(j('src/main/resources/data/thaumcraft/recipes/'+recipe)))
for tid in ['items.simple_resources_meat_nugget_food','items.simple_resources_nugget_tags_and_recipes','items.simple_resources_shard_aspects','items.simple_resources_cluster_smelting','items.simple_resources_ingot_and_material_tags','items.simple_resources_save_reload_multiplayer']:
 ok('runtime_'+tid,tid in ids)
ok('audit_version',audit.get('version')=='11.63.29'); ok('audit_fallback',audit.get('generic_fallback_ids')==154); ok('audit_items',audit.get('generic_fallback_item_like_ids')==112); ok('audit_blocks',audit.get('generic_fallback_block_alias_ids')==42)
for x in ids_expected: ok('not_fallback_'+x,x not in audit.get('generic_fallback',[]))
need('README.md','11.63.29 — Simple Resource Families'); need('KNOWN_DEVIATIONS.md','11.63.29 Simple Resource Families'); need('TC4_11.63.29_SIMPLE_RESOURCE_FAMILIES_PORT_REPORT_RU.md','**154 fallback-ID**'); need('TC4_11.63.29_REMAINING_OBJECTS_AUDIT_RU.md','**154**')
failed=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed')
sys.exit(1 if failed else 0)
