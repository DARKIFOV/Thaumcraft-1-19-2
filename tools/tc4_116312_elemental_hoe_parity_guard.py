#!/usr/bin/env python3
from pathlib import Path
import json, sys
R=Path(__file__).resolve().parents[1]; checks=[]
def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def need(p,t): checks.append((f'{p}:{t[:70]}',t in text(p)))
def ok(n,v): checks.append((n,bool(v)))
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids={x.get('id') for x in manifest['tests']}
ok('build_version',"version = '11.63.23'" in text('build.gradle')); ok('mods_version','version="11.63.23"' in text('src/main/resources/META-INF/mods.toml')); ok('manifest_version',manifest['version'] in ('11.63.23','11.63.24','11.63.26','11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61')); ok('manifest_count',len(manifest['tests'])>=142)
for p,tokens in {
'src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java':['ElementalHoeItem','case "tc4_elementalhoe" -> new ElementalHoeItem(functionalProperties)'],
'src/main/java/com/darkifov/thaumcraft/item/ElementalHoeItem.java':['extends HoeItem','for (int dx = -1; dx <= 1; dx++)','for (int dz = -1; dz <= 1; dz++)','new BonemealEvent','MinecraftForge.EVENT_BUS.post(event)','TC4TreeGenerator.growGreatwood','TC4TreeGenerator.growSilverwood','remaining >= 20','remaining >= 150','damageGrowthUse(level, player, stack, context, 5)','damageGrowthUse(level, player, stack, context, 25)','TC4Sounds.event("wand")','return Rarity.RARE;'],
'src/main/java/com/darkifov/thaumcraft/item/gear/TC4ElementalToolTier.java':['return 1500;','return 10.0F;','return 3;','return 18;','THAUMIUM_INGOT'],
}.items():
 for t in tokens: need(p,t)
for tid in ['tools.elemental_hoe_material_repair_rarity_and_sneak_bypass','tools.elemental_hoe_three_by_three_tilling_and_events','tools.elemental_hoe_bonemeal_fallback_and_durability','tools.elemental_hoe_greatwood_silverwood_thresholds','tools.elemental_hoe_particles_sound_and_client_server_authority','tools.elemental_hoe_multiplayer_protection_and_save_reload']: ok('manifest:'+tid,tid in ids)
lang=json.loads(text('src/main/resources/assets/thaumcraft/lang/en_us.json')); ok('lang',lang.get('item.thaumcraft.tc4_elementalhoe')=='Hoe of Growth')
fail=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(fail)}/{len(checks)} passed')
sys.exit(bool(fail))
