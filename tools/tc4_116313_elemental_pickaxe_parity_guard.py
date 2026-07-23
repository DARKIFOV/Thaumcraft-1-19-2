#!/usr/bin/env python3
from pathlib import Path
import json, sys
R=Path(__file__).resolve().parents[1]; checks=[]
def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def need(p,t): checks.append((f'{p}:{t[:70]}',t in text(p)))
def ok(n,v): checks.append((n,bool(v)))
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids={x.get('id') for x in manifest['tests']}
for n,v in [('build',"version = '11.63.23'" in text('build.gradle')),('mods','version="11.63.23"' in text('src/main/resources/META-INF/mods.toml')),('manifest',manifest['version'] in ('11.63.23','11.63.24','11.63.26','11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61')),('count',len(manifest['tests'])>=148)]: ok(n,v)
for p,tokens in {
'src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java':['ElementalPickaxeItem','case "tc4_elementalpick" -> new ElementalPickaxeItem(functionalProperties)'],
'src/main/java/com/darkifov/thaumcraft/item/ElementalPickaxeItem.java':['extends PickaxeItem','SCAN_RADIUS = 8','SCAN_DURATION_TICKS = 100','target.setSecondsOnFire(2)','server.isPvpAllowed()','stack.hurtAndBreak(5','TC4Sounds.event("wandfail")'],
'src/main/java/com/darkifov/thaumcraft/item/ElementalPickaxeRuntime.java':['BlockEvent.BreakEvent','level.getGameTime() + 1L','0.2F + pending.fortune * 0.075F','tc4_clusteriron','tc4_clustergold','tc4_clustercinnabar','tc4_clustercopper','tc4_clustertin','tc4_clustersilver','tc4_clusterlead','new ResourceLocation("forge", "ores/" + material)'],
'src/main/java/com/darkifov/thaumcraft/client/ElementalPickScanClient.java':['AFTER_TRANSLUCENT_BLOCKS','FluidTags.WATER','FluidTags.LAVA','Tags.Blocks.ORES','totalAmount()','elapsed / 30.0F','remaining / 5.0F','distanceSquared() / 64.0F','aspectValue() / 7.0F','ClientPlayerNetworkEvent.LoggingOut'],
'src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java':['ElementalPickaxeRuntime.tick(level)','ElementalPickaxeRuntime.onBlockBreak(event)','event.setUseBlock(Event.Result.DENY)'],
}.items():
 for t in tokens: need(p,t)
for tid in ['tools.elemental_pick_material_rarity_repair_and_sneak_bypass','tools.elemental_pick_entity_ignition_and_pvp_guard','tools.elemental_pick_native_cluster_chance_fortune_and_counts','tools.elemental_pick_scan_radius_fluids_ores_and_fade','tools.elemental_pick_interactive_block_durability_sound_and_hand_swing','tools.elemental_pick_multiplayer_save_reload_and_modded_ore_tags']: ok('manifest:'+tid,tid in ids)
lang=json.loads(text('src/main/resources/assets/thaumcraft/lang/en_us.json')); ok('lang',lang.get('item.thaumcraft.tc4_elementalpick')=='Pickaxe of the Core')
fail=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(fail)}/{len(checks)} passed'); sys.exit(bool(fail))
