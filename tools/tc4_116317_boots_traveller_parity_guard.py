#!/usr/bin/env python3
from pathlib import Path
import json, sys
R=Path(__file__).resolve().parents[1]; checks=[]
def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def need(p,t): checks.append((f'{p}:{t[:78]}',t in text(p)))
def ok(n,v): checks.append((n,bool(v)))
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids={x.get('id') for x in manifest['tests']}
for n,v in [('build', any(f"version = '{v}'" in text('build.gradle') for v in ['11.63.23','11.63.23'])),('mods', any(f'version="{v}"' in text('src/main/resources/META-INF/mods.toml') for v in ['11.63.23','11.63.23'])),('manifest',manifest['version'] in {'11.63.23','11.63.24','11.63.26','11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43','11.63.44','11.63.45','11.63.46', '11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52','11.63.53','11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61'}),('count_172',len(manifest['tests'])>=172)]: ok(n,v)
for p,tokens in {
'src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java':['import com.darkifov.thaumcraft.item.gear.BootsOfTravellerItem;','case "tc4_bootstraveler" -> new BootsOfTravellerItem(functionalProperties)'],
'src/main/java/com/darkifov/thaumcraft/item/gear/TC4TravellerArmorMaterial.java':['implements ArmorMaterial','slot == EquipmentSlot.FEET ? 350 : 0','slot == EquipmentSlot.FEET ? 1 : 0','return 25;','return Ingredient.EMPTY;','return "thaumcraft:traveller";'],
'src/main/java/com/darkifov/thaumcraft/item/gear/BootsOfTravellerItem.java':['extends ArmorItem','TC4TravellerArmorMaterial.INSTANCE, EquipmentSlot.FEET','return Rarity.RARE;','thaumcraft:textures/models/bootstraveler.png','TC4RunicArmorHelper.appendTooltip'],
'src/main/java/com/darkifov/thaumcraft/item/gear/BootsOfTravellerRuntime.java':['@Mod.EventBusSubscriber','TickEvent.PlayerTickEvent','event.phase != TickEvent.Phase.END','ForgeMod.STEP_HEIGHT_ADDITION.get()','0.4D','GROUND_ACCELERATION = 0.055F','WATER_DIVISOR = 4.0F','AIR_ACCELERATION = 0.03F','JUMP_BONUS = 0.2750000059604645D','FALL_DISTANCE_REDUCTION = 0.25F','player.zza > 0.0F','player.isOnGround()','player.isInWater()','player.isInLava()','player.moveRelative','LivingEvent.LivingJumpEvent','motion.y + JUMP_BONUS','attribute.removeModifier(STEP_HEIGHT_UUID)','attribute.addTransientModifier(STEP_HEIGHT)','!player.isShiftKeyDown()','instanceof BootsOfTravellerItem'],
'src/main/java/com/darkifov/thaumcraft/infusion/TC4RunicArmorHelper.java':['"thaumcraft:tc4_bootstraveler"'],
'src/main/resources/data/thaumcraft/tags/items/repairable.json':['"thaumcraft:tc4_bootstraveler"'],
}.items():
 for t in tokens: need(p,t)
for tid in ['gear.boots_traveller_material_rarity_texture_and_runic_augment','gear.boots_traveller_ground_forward_acceleration','gear.boots_traveller_water_quarter_acceleration','gear.boots_traveller_step_height_sneak_and_unequip_cleanup','gear.boots_traveller_jump_air_control_and_fall_distance','gear.boots_traveller_multiplayer_server_client_and_save_reload']: ok('manifest:'+tid,tid in ids)
for lang,name in [('en_us','Boots of the Traveller'),('ru_ru','Сапоги Путешественника')]:
 d=json.loads(text(f'src/main/resources/assets/thaumcraft/lang/{lang}.json')); ok('lang:'+lang,d.get('item.thaumcraft.tc4_bootstraveler')==name)
model=json.loads(text('src/main/resources/assets/thaumcraft/models/item/tc4_bootstraveler.json')); ok('model_texture',model.get('textures',{}).get('layer0')=='thaumcraft:item/tc4/bootstraveler')
for p in ['src/main/resources/assets/thaumcraft/textures/item/tc4/bootstraveler.png','src/main/resources/assets/thaumcraft/textures/models/bootstraveler.png','src/main/resources/assets/thaumcraft/original_tc4_1710/textures/items/bootstraveler.png','src/main/resources/assets/thaumcraft/original_tc4_1710/textures/models/bootstraveler.png']: ok('asset:'+p,(R/p).is_file())
recipe=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_infusion/tc4_bootstraveller.json')); ok('infusion_result',recipe.get('result',{}).get('item')=='thaumcraft:tc4_bootstraveler')
for wf in ['.github/workflows/build.yml','.github/workflows/release.yml']: ok('workflow:'+wf,'tc4_116317_boots_traveller_parity_guard.py' in text(wf))
fail=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(fail)}/{len(checks)} passed'); sys.exit(bool(fail))
