#!/usr/bin/env python3
from pathlib import Path
import json,re,sys
R=Path(__file__).resolve().parents[1]; checks=[]
def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def ok(n,v): checks.append((n,bool(v)))
def need(p,t): ok(f'{p}:{t}',t in text(p))
def j(p): return json.loads(text(p))
manifest=j('runtime_artifacts/runtime_test_manifest.template.json'); ids={x.get('id') for x in manifest.get('tests',[])}
audit=j('reports/tc4_remaining_objects_audit_v11.63.31.json')
ok('build_version', any(f"version = '{v}'" in text('build.gradle') for v in ('11.63.30','11.63.31','11.63.32','11.63.33')))
ok('mods_version', any(f'version="{v}"' in text('src/main/resources/META-INF/mods.toml') for v in ('11.63.30','11.63.31','11.63.32','11.63.33')))
ok('manifest_version',manifest.get('version') in ('11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61')); ok('manifest_count',len(manifest.get('tests',[]))>=244)
for p,toks in {
'src/main/java/com/darkifov/thaumcraft/item/TC4SimpleResourceItem.java':['CRAFTING_COMPONENT','String material()'],
'src/main/java/com/darkifov/thaumcraft/item/TC4EmptyGolemUpgradeItem.java':['class TC4EmptyGolemUpgradeItem extends Item','properties.stacksTo(64)','tc4_golem_upgrade_empty.desc'],
'src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java':['case "tc4_coin"','"gold_coin"','case "tc4_dust"','"salis_mundus"','case "tc4_filter"','"vis_filter"','case "tc4_mirrorglass"','"mirrored_glass"','case "tc4_voidseed"','"void_seed"','case "tc4_golem_upgrade_empty"','Rarity.UNCOMMON']}.items():
 for t in toks: need(p,t)
items=['tc4_coin','tc4_dust','tc4_filter','tc4_mirrorglass','tc4_voidseed','tc4_golem_upgrade_empty']
switch=text('src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java')
for x in items: ok('dedicated_'+x,re.search(r'case[^\n]*"'+re.escape(x)+r'"',switch) is not None)
for x in items: ok('model_'+x,(R/'src/main/resources/assets/thaumcraft/models/item'/f'{x}.json').is_file())
for x in items:
 src=R/'src/main/resources/assets/thaumcraft/textures/item/tc4'/f'{x[4:]}.png'
 orig=R/'src/main/resources/assets/thaumcraft/original_tc4_1710/textures/items'/f'{x[4:]}.png'
 ok('texture_'+x,src.is_file() and orig.is_file() and src.read_bytes()==orig.read_bytes())
for lang in ['en_us.json','ru_ru.json']:
 d=j('src/main/resources/assets/thaumcraft/lang/'+lang)
 for x in items: ok('lang_'+lang+'_'+x,bool(d.get('item.thaumcraft.'+x)))
 ok('tooltip_'+lang,bool(d.get('item.thaumcraft.tc4_golem_upgrade_empty.desc')))
expected_names={'tc4_coin':'Gold Coin','tc4_dust':'Salis Mundus','tc4_filter':'Vis Filter','tc4_mirrorglass':'Mirrored Glass','tc4_voidseed':'Void Seed','tc4_golem_upgrade_empty':'Blank Golem Upgrade'}
en=j('src/main/resources/assets/thaumcraft/lang/en_us.json')
for x,n in expected_names.items(): ok('english_name_'+x,en.get('item.thaumcraft.'+x)==n)
tagmap={
 'src/main/resources/data/thaumcraft/tags/items/coins.json':['thaumcraft:tc4_coin'],
 'src/main/resources/data/thaumcraft/tags/items/salis_mundus.json':['thaumcraft:tc4_dust'],
 'src/main/resources/data/thaumcraft/tags/items/vis_filters.json':['thaumcraft:tc4_filter'],
 'src/main/resources/data/thaumcraft/tags/items/mirrored_glass.json':['thaumcraft:tc4_mirrorglass'],
 'src/main/resources/data/thaumcraft/tags/items/void_seeds.json':['thaumcraft:tc4_voidseed'],
 'src/main/resources/data/thaumcraft/tags/items/golem_upgrades/empty.json':['thaumcraft:tc4_golem_upgrade_empty']}
for p,vals in tagmap.items():
 data=j(p).get('values',[])
 for v in vals: ok('tag_'+v,v in data)
allvals=j('src/main/resources/data/thaumcraft/tags/items/passive_crafting_components.json').get('values',[])
for x in items: ok('family_tag_'+x,'thaumcraft:'+x in allvals)
# Existing preserved recipe references remain concrete IDs.
recipe_blob='\n'.join(p.read_text(encoding='utf-8',errors='ignore') for p in (R/'src/main/resources/data/thaumcraft').rglob('*.json'))
for x in ['tc4_dust','tc4_filter','tc4_mirrorglass','tc4_voidseed','tc4_golem_upgrade_empty']:
 ok('recipe_reference_'+x,('thaumcraft:'+x) in recipe_blob)
for tid in ['items.passive_components_registry_and_names','items.passive_components_recipe_compatibility','items.passive_components_tags','items.blank_golem_upgrade_properties','items.passive_components_save_reload','items.passive_components_multiplayer_dedicated']:
 ok('runtime_'+tid,tid in ids)
ok('audit_version',audit.get('version') in ('11.63.30','11.63.31','11.63.32','11.63.33')); ok('audit_fallback',audit.get('generic_fallback_ids',999)<=148); ok('audit_items',audit.get('generic_fallback_item_like_ids',999)<=106); ok('audit_blocks',audit.get('generic_fallback_block_alias_ids')==42)
for x in items: ok('not_fallback_'+x,x not in audit.get('generic_fallback',[]))
for wf in ['.github/workflows/build.yml','.github/workflows/release.yml']:
 need(wf,'python3 tools/tc4_116330_passive_crafting_components_parity_guard.py')
need('README.md','11.63.30 — Passive Crafting Components'); need('KNOWN_DEVIATIONS.md','11.63.30 Passive Crafting Components'); need('TC4_11.63.30_PASSIVE_CRAFTING_COMPONENTS_PORT_REPORT_RU.md','**148 fallback-ID**'); need('TC4_11.63.30_REMAINING_OBJECTS_AUDIT_RU.md','**148**')
failed=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed')
sys.exit(1 if failed else 0)
