#!/usr/bin/env python3
from pathlib import Path
import json, sys
R=Path(__file__).resolve().parents[1]; checks=[]
def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def need(p,t): checks.append((f'{p}:{t[:88]}',t in text(p)))
def ok(n,v): checks.append((n,bool(v)))
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids={x.get('id') for x in manifest['tests']}
for n,v in [('build',any(f"version = '{v}'" in text('build.gradle') for v in ['11.63.23','11.63.23'])),('mods',any(f'version="{v}"' in text('src/main/resources/META-INF/mods.toml') for v in ['11.63.23','11.63.23'])),('manifest',manifest['version'] in {'11.63.23','11.63.24','11.63.26','11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43','11.63.44','11.63.45','11.63.46', '11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52','11.63.53','11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61'}),('count_at_least_178',len(manifest['tests'])>=178)]: ok(n,v)
for p,tokens in {
'src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java':['import com.darkifov.thaumcraft.item.gear.HoverHarnessItem;','import com.darkifov.thaumcraft.item.gear.HoverGirdleItem;','case "tc4_hoverharness" -> new HoverHarnessItem(functionalProperties)','case "tc4_hovergirdle" -> new HoverGirdleItem(functionalProperties)'],
'src/main/java/com/darkifov/thaumcraft/item/gear/TC4HoverHarnessArmorMaterial.java':['implements ArmorMaterial','slot == EquipmentSlot.CHEST ? 400 : 0','slot == EquipmentSlot.CHEST ? 3 : 0','return 25;','Ingredient.of(Items.GOLD_INGOT)','return "thaumcraft:hover_harness";'],
'src/main/java/com/darkifov/thaumcraft/item/gear/HoverHarnessItem.java':['extends ArmorItem implements TC4VisDiscountGear','EquipmentSlot.CHEST','return Rarity.EPIC;','textures/models/hoverharness.png','aspect == Aspect.AER ? 5 : 2','NetworkHooks.openScreen','HoverHarnessMenu','TAG_JAR = "jar"','TAG_HOVER = "hover"','TAG_CHARGE = "charge"','EFFICIENCY = 360','EssentiaJarBlockItem.itemAspects','Aspect.POTENTIA','threshold = Math.round','girdle ? 0.8F : 1.0F','root.put(TAG_JAR','TC4RunicArmorHelper.appendTooltip'],
'src/main/java/com/darkifov/thaumcraft/item/gear/HoverGirdleItem.java':['extends Item','properties.stacksTo(1)','return Rarity.RARE;','TC4RunicArmorHelper.appendTooltip'],
'src/main/java/com/darkifov/thaumcraft/item/gear/HoverHarnessRuntime.java':['TickEvent.PlayerTickEvent','hasHoverGirdle(player)','GIRDLE_FALL_REDUCTION = 0.33F','HoverHarnessItem.expendCharge','grantFlight(player)','revokeGrantedFlight(player)','player.fallDistance = 0.0F','player.fallDistance *= 0.75F','TC4Sounds.event("jacobs")','0.7F + 0.075F * haste','girdle ? 0.21F','Math.min(1.0F','TC4BaubleSlotAdapter.findEquippedBaubles','player.getOffhandItem()','player.getInventory().items','getAbilities().mayfly = true','getAbilities().flying = false','serverPlayer.onUpdateAbilities()','getAbilities().instabuild','player.isSpectator()'],
'src/main/java/com/darkifov/thaumcraft/menu/HoverHarnessMenu.java':['HOVER_HARNESS_MENU.get()','new SimpleContainer(JAR_SLOTS)','addSlot(new Slot(jarInventory, 0, 80, 32)','HoverHarnessItem.isValidFuelJar','8 + column * 18, 84 + row * 18','8 + column * 18, 142','lockedMenuSlot','slotId == lockedMenuSlot','HoverHarnessItem.setJar','InteractionHand.OFF_HAND'],
'src/main/java/com/darkifov/thaumcraft/client/screen/HoverHarnessScreen.java':['guihoverharness.png','imageWidth = 176','imageHeight = 166','selectedHotbarSlot','240, 0, 16, 16'],
'src/main/java/com/darkifov/thaumcraft/client/ClientHoverKeybinds.java':['GLFW.GLFW_KEY_H','key.thaumcraft.toggle_hover','RegisterKeyMappingsEvent'],
'src/main/java/com/darkifov/thaumcraft/client/ClientHoverEvents.java':['InputEvent.Key','EquipmentSlot.CHEST','requestHoverToggleFromClient'],
'src/main/java/com/darkifov/thaumcraft/network/RequestHoverTogglePacket.java':['HoverHarnessRuntime.toggle(player)','context.getSender()','setPacketHandled(true)'],
'src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java':['RequestHoverTogglePacket.class','requestHoverToggleFromClient','new RequestHoverTogglePacket()'],
'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java':['import com.darkifov.thaumcraft.menu.HoverHarnessMenu;','HOVER_HARNESS_MENU','MENUS.register("hover_harness"'],
'src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java':['import com.darkifov.thaumcraft.client.screen.HoverHarnessScreen;','MenuScreens.register(ThaumcraftMod.HOVER_HARNESS_MENU.get()'],
'src/main/java/com/darkifov/thaumcraft/infusion/TC4RunicArmorHelper.java':['"thaumcraft:tc4_hoverharness"','"thaumcraft:tc4_hovergirdle"','id.equals("thaumcraft:tc4_hovergirdle")'],
'src/main/java/com/darkifov/thaumcraft/runic/TC4BaubleSlotAdapter.java':['findEquippedBaubles(Player player)','readCurios(Player player)','readLegacyBaubles(Player player)'],
'src/main/resources/data/thaumcraft/tags/items/repairable.json':['"thaumcraft:tc4_hoverharness"'],
}.items():
 for t in tokens: need(p,t)
for tid in ['gear.hover_harness_registration_material_repair_vis_runic_and_assets','gear.hover_harness_original_gui_jar_validation_and_persistence','gear.hover_harness_h_key_server_flight_toggle_and_no_fuel','gear.hover_harness_potentia_efficiency_girdle_modifier_and_save_reload','gear.hover_harness_haste_damping_fall_reset_sounds_and_girdle_control','gear.hover_girdle_curios_baubles_fallback_multiplayer_and_creative_safety']: ok('manifest:'+tid,tid in ids)
for lang,names in [('en_us',('Thaumostatic Harness','Thaumostatic Girdle')),('ru_ru',('Таумостатический ранец','Таумостатический пояс'))]:
 d=json.loads(text(f'src/main/resources/assets/thaumcraft/lang/{lang}.json')); ok('lang_harness:'+lang,d.get('item.thaumcraft.tc4_hoverharness')==names[0]); ok('lang_girdle:'+lang,d.get('item.thaumcraft.tc4_hovergirdle')==names[1]); ok('lang_key:'+lang,'key.thaumcraft.toggle_hover' in d)
for p in ['src/main/resources/assets/thaumcraft/textures/item/tc4/hoverharness.png','src/main/resources/assets/thaumcraft/textures/item/tc4/hovergirdle.png','src/main/resources/assets/thaumcraft/textures/models/hoverharness.png','src/main/resources/assets/thaumcraft/textures/models/hoverharness2.png','src/main/resources/assets/thaumcraft/textures/models/hoverharness.obj','src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/gui/guihoverharness.png']: ok('asset:'+p,(R/p).is_file())
for p in ['src/main/resources/assets/thaumcraft/sounds/hhon.ogg','src/main/resources/assets/thaumcraft/sounds/hhoff.ogg','src/main/resources/assets/thaumcraft/sounds/jacobs.ogg']: ok('sound:'+p,(R/p).is_file())
for wf in ['.github/workflows/build.yml','.github/workflows/release.yml']: ok('workflow:'+wf,'tc4_116318_hover_harness_girdle_parity_guard.py' in text(wf))
fail=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(fail)}/{len(checks)} passed'); sys.exit(bool(fail))
