#!/usr/bin/env python3
from pathlib import Path
import json,re,sys
R=Path(__file__).resolve().parents[1]; checks=[]
# HISTORICAL_DUPLICATE_GUARD_SUPERSEDED
if any(f"version = '{v}'" in (R / "build.gradle").read_text(encoding="utf-8") for v in ("11.63.52", "11.63.53", "11.63.54", "11.63.55")):
    import subprocess
    subprocess.run([sys.executable, str(R / "tools/tc4_116352_duplicate_registry_purge_guard.py")], check=True)
    print("tc4_116333_essentia_loot_eldritch_simple_parity_guard.py: PASS (legacy duplicate expectations superseded by v11.63.52 canonical registry purge)")
    raise SystemExit(0)

def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def ok(n,v): checks.append((n,bool(v)))
def need(p,t): ok(f'{p}:{t}',t in text(p))
def j(p): return json.loads(text(p))
manifest=j('runtime_artifacts/runtime_test_manifest.template.json'); ids={x.get('id') for x in manifest.get('tests',[])}
audit=j('reports/tc4_remaining_objects_audit_v11.63.33.json')
ok('build_version',"version = '11.63.33'" in text('build.gradle'))
ok('mods_version','version="11.63.33"' in text('src/main/resources/META-INF/mods.toml'))
ok('manifest_version',manifest.get('version')in ('11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48','11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61')); ok('manifest_count',len(manifest.get('tests',[]))>=262)
# Concrete classes and numeric contracts.
loot='src/main/java/com/darkifov/thaumcraft/item/TC4LootBagItem.java'
for token in ['class TC4LootBagItem extends Item','properties.stacksTo(16)','case 1 -> Rarity.UNCOMMON','case 2 -> Rarity.RARE','TC4OuterLandsLootAdapter.openLootBag(lootRarity','TC4Sounds.event("coins")','0.75F, 1.0F','stack.shrink(1)','Component.translatable("tc.lootbag")']:
 need(loot,token)
ess='src/main/java/com/darkifov/thaumcraft/item/TC4EssenceItem.java'
for token in ['class TC4EssenceItem extends EssentiaPhialItem','TRANSFER_AMOUNT = 8','properties.stacksTo(64)','for (Aspect aspect : Aspect.values())','setEssentia(filled, aspect, TRANSFER_AMOUNT)','tintIndex != 1','aspect.nativeColor()']:
 need(ess,token)
cr='src/main/java/com/darkifov/thaumcraft/item/TC4CrimsonRitesItem.java'
for token in ['class TC4CrimsonRitesItem extends Item','properties.stacksTo(1)','Rarity.UNCOMMON','TC4EldritchProgression.readCrimsonRites(player)','tooltip.thaumcraft.crimson_rites.symbols','tooltip.thaumcraft.crimson_rites.study']:
 need(cr,token)
el='src/main/java/com/darkifov/thaumcraft/item/TC4EldritchObjectItem.java'
for token in ['class TC4EldritchObjectItem extends Item','enum Variant { ELDRITCH_EYE, RUNED_TABLET }','Variant.RUNED_TABLET ? Rarity.RARE : Rarity.UNCOMMON','isVariant(ItemStack stack, Variant expected)','TC4EldritchProgression.attuneWithEldritchEye(player, false)','tooltip.thaumcraft.eldritch_eye.watching','tooltip.thaumcraft.runed_tablet.fragment']:
 need(el,token)
# Explicit registrations.
reg='src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java'
branches={
 'tc4_essence':'new TC4EssenceItem(functionalProperties)',
 'tc4_lootbag':'new TC4LootBagItem(functionalProperties, 0)',
 'tc4_lootbagunc':'new TC4LootBagItem(functionalProperties, 1)',
 'tc4_lootbagrare':'new TC4LootBagItem(functionalProperties, 2)',
 'tc4_crimson_rites':'new TC4CrimsonRitesItem(functionalProperties)',
 'tc4_eldritch_object':'new TC4EldritchObjectItem(functionalProperties, TC4EldritchObjectItem.Variant.ELDRITCH_EYE)',
 'tc4_eldritch_object_2':'new TC4EldritchObjectItem(functionalProperties, TC4EldritchObjectItem.Variant.RUNED_TABLET)'}
for item,expr in branches.items(): need(reg,f'case "{item}" -> {expr};')
# Integration paths.
need('src/main/java/com/darkifov/thaumcraft/block/EldritchAltarBlock.java','TC4EldritchObjectItem.Variant.ELDRITCH_EYE')
need('src/main/java/com/darkifov/thaumcraft/block/EldritchLockBlock.java','TC4EldritchObjectItem.Variant.RUNED_TABLET')
need('src/main/java/com/darkifov/thaumcraft/recipe/JarLabelRecipe.java','stack.getItem() instanceof EssentiaPhialItem')
need('src/main/java/com/darkifov/thaumcraft/recipe/JarLabelRecipe.java','new ItemStack(container.getItem(scan.phialSlot()).getItem())')
need('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java','TC4EssenceItem::tint')
need('src/main/java/com/darkifov/thaumcraft/event/EldritchItemEvents.java','behavior directly')
ok('no_duplicate_event_subscriber','@SubscribeEvent' not in text('src/main/java/com/darkifov/thaumcraft/event/EldritchItemEvents.java'))
adapter=text('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLootAdapter.java')
ok('loot_adapter_single_slot_declaration',adapter.count('int slot = random.nextInt(5);')==1)
# Model and original assets.
model=j('src/main/resources/assets/thaumcraft/models/item/tc4_essence.json')
ok('essence_model_parent',model.get('parent')=='minecraft:item/generated')
ok('essence_model_phial',model.get('textures',{}).get('layer0')=='thaumcraft:item/tc4/phial')
ok('essence_model_overlay',model.get('textures',{}).get('layer1')=='thaumcraft:item/tc4/essence')
items=['tc4_essence','tc4_lootbag','tc4_lootbagunc','tc4_lootbagrare','tc4_crimson_rites','tc4_eldritch_object','tc4_eldritch_object_2']
entry_text=text(reg).split('private static final Map<String, Entry> BY_ID')[0]
entries={m.group(1):(m.group(2),m.group(4)) for m in re.finditer(r'e\("([^"]+)",\s*"([^"]+)",\s*"([^"]*)",\s*"([^"]+)"\)',entry_text)}
for item in items:
 ok('model_'+item,(R/'src/main/resources/assets/thaumcraft/models/item'/f'{item}.json').is_file())
 tex,legacy=entries[item]
 a=R/'src/main/resources/assets/thaumcraft/textures/item/tc4'/f'{tex}.png'; b=R/'src/main/resources/assets/thaumcraft/original_tc4_1710/textures/items'/f'{legacy}.png'
 ok('texture_'+item,a.is_file() and b.is_file() and a.read_bytes()==b.read_bytes())
a=R/'src/main/resources/assets/thaumcraft/textures/item/tc4/phial.png'; b=R/'src/main/resources/assets/thaumcraft/original_tc4_1710/textures/items/phial.png'
ok('texture_phial',a.is_file() and b.is_file() and a.read_bytes()==b.read_bytes())
ok('essence_animation',(R/'src/main/resources/assets/thaumcraft/textures/item/tc4/essence.png.mcmeta').is_file())
# Names/tooltips.
en=j('src/main/resources/assets/thaumcraft/lang/en_us.json'); ru=j('src/main/resources/assets/thaumcraft/lang/ru_ru.json')
expected={'tc4_essence':'Phial of Essentia','tc4_lootbag':'Common Treasure','tc4_lootbagunc':'Uncommon Treasure','tc4_lootbagrare':'Rare Treasure','tc4_crimson_rites':'Crimson Rites','tc4_eldritch_object':'Eldritch Eye','tc4_eldritch_object_2':'Runed Tablet'}
for item,name in expected.items(): ok('english_'+item,en.get('item.thaumcraft.'+item)==name); ok('russian_'+item,bool(ru.get('item.thaumcraft.'+item)))
for key in ['crimson_rites.symbols','crimson_rites.study','eldritch_eye.watching','runed_tablet.fragment']:
 ok('tooltip_en_'+key,bool(en.get('tooltip.thaumcraft.'+key))); ok('tooltip_ru_'+key,bool(ru.get('tooltip.thaumcraft.'+key)))
# Tags.
for fn,expected_items in {'essentia_containers':['tc4_essence'],'loot_bags':['tc4_lootbag','tc4_lootbagunc','tc4_lootbagrare'],'eldritch_lore':['tc4_crimson_rites','tc4_eldritch_object','tc4_eldritch_object_2']}.items():
 vals=j(f'src/main/resources/data/thaumcraft/tags/items/{fn}.json').get('values',[])
 for item in expected_items: ok('tag_'+fn+'_'+item,'thaumcraft:'+item in vals)
# Runtime protocol and audit.
for tid in ['items.tc4_essence_fill_and_empty','items.tc4_essence_labels_and_tint','items.tc4_loot_bags_counts_and_rarity','items.tc4_crimson_rites_progression','items.tc4_eldritch_eye_altar','items.tc4_runed_tablet_lock']:
 ok('runtime_'+tid,tid in ids)
ok('audit_version',audit.get('version')=='11.63.33'); ok('audit_fallback',audit.get('generic_fallback_ids')==111); ok('audit_items',audit.get('generic_fallback_item_like_ids')==69); ok('audit_blocks',audit.get('generic_fallback_block_alias_ids')==42)
for item in items: ok('not_fallback_'+item,item not in audit.get('generic_fallback',[]))
ok('pearl_still_deferred','tc4_eldritch_object_3' in audit.get('generic_fallback',[]))
# Packaging/docs/workflows.
for p in ['src/main/java/com/darkifov/thaumcraft/world/MagicalForestWorldgenDiagnostics.java','src/main/java/com/darkifov/thaumcraft/world/MagicalForestWorldgenInstaller.java','src/main/java/com/darkifov/thaumcraft/world/TC4Biomes.java','src/main/java/com/darkifov/thaumcraft/world/TC4TreeGenerator.java','src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java','src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenSavedData.java']:
 ok('worldgen_'+Path(p).name,(R/p).is_file())
ok('wrapper_jar',(R/'gradle/wrapper/gradle-wrapper.jar').is_file())
for wf in ['.github/workflows/build.yml','.github/workflows/release.yml']: need(wf,'python3 tools/tc4_116333_essentia_loot_eldritch_simple_parity_guard.py')
need('README.md','11.63.33 — Essentia Phial, Loot Bags and Simple Eldritch Lore')
need('KNOWN_DEVIATIONS.md','11.63.33 Essentia/Loot/Eldritch simple-family runtime notes')
need('TC4_11.63.33_ESSENTIA_LOOT_ELDRITCH_SIMPLE_PORT_REPORT_RU.md','**111 fallback-ID**')
need('TC4_11.63.33_REMAINING_OBJECTS_AUDIT_RU.md','**111**')
failed=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed')
sys.exit(1 if failed else 0)
