#!/usr/bin/env python3
from pathlib import Path
import json, hashlib, sys
R=Path(__file__).resolve().parents[1]; checks=[]
def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def need(p,t): checks.append((f'{p}:{t[:70]}',t in text(p)))
def ok(n,v): checks.append((n,bool(v)))
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids={x.get('id') for x in manifest['tests']}
for n,v in [('build',"version = '11.63.23'" in text('build.gradle')),('mods','version="11.63.23"' in text('src/main/resources/META-INF/mods.toml')),('manifest',manifest['version'] in ('11.63.23','11.63.24','11.63.26','11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61')),('count_160',len(manifest['tests'])>=160)]: ok(n,v)
for p,tokens in {
'src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java':['ElementalSwordItem','case "tc4_elementalsword" -> new ElementalSwordItem(functionalProperties)'],
'src/main/java/com/darkifov/thaumcraft/item/ElementalSwordItem.java':['extends SwordItem','ThreadLocal<Boolean> SWEEPING','return UseAnim.BLOCK','return 72_000','player.startUsingItem(hand)','y /= 1.2000000476837158D','player.fallDistance /= 1.2F','y += 0.07999999821186066D','if (y > 0.5D) y = 0.20000000298023224D','inflate(2.5D)','!(entity instanceof Player)','away.scale(1.0D / (2.5D * distance))','TC4Sounds.event("wind")','ticks % 20 == 0','hurtAndBreak(1','primaryTarget.getBoundingBox().inflate(1.2D, 1.1D, 1.2D)','player.attack(target)','TC4Sounds.event("swing")','target instanceof TamableAnimal','target instanceof ThaumGolemEntity','getOwnerUuid()','return Rarity.RARE;'],
}.items():
 for t in tokens: need(p,t)
for tid in ['tools.elemental_sword_material_repair_rarity_and_primary_attack','tools.elemental_sword_wide_arc_secondary_target_filtering','tools.elemental_sword_wind_sphere_entity_and_projectile_push','tools.elemental_sword_lift_fall_mitigation_and_velocity_cap','tools.elemental_sword_durability_sound_particles_and_break','tools.elemental_sword_multiplayer_events_save_reload_and_pvp']: ok('manifest:'+tid,tid in ids)
lang=json.loads(text('src/main/resources/assets/thaumcraft/lang/en_us.json')); ok('lang_en',lang.get('item.thaumcraft.tc4_elementalsword')=='Sword of the Zephyr'); lang=json.loads(text('src/main/resources/assets/thaumcraft/lang/ru_ru.json')); ok('lang_ru',lang.get('item.thaumcraft.tc4_elementalsword')=='Меч Бури')
tex=R/'src/main/resources/assets/thaumcraft/textures/item/tc4/elementalsword.png'; ok('texture_exists',tex.is_file()); ok('texture_sha',tex.is_file() and hashlib.sha256(tex.read_bytes()).hexdigest()=='17bff7c0e26bf316ab81b0adb5356a763c4c28ef0a6d2a557a61bf6a145202d7')
for wf in ['.github/workflows/build.yml','.github/workflows/release.yml']: ok('workflow:'+wf,'tc4_116315_elemental_sword_parity_guard.py' in text(wf))
fail=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(fail)}/{len(checks)} passed'); sys.exit(bool(fail))
