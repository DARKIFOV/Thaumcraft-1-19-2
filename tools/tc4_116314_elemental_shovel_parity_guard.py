#!/usr/bin/env python3
from pathlib import Path
import json, hashlib, sys
R=Path(__file__).resolve().parents[1]; checks=[]
def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def need(p,t): checks.append((f'{p}:{t[:70]}',t in text(p)))
def ok(n,v): checks.append((n,bool(v)))
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids={x.get('id') for x in manifest['tests']}
for n,v in [('build',"version = '11.63.23'" in text('build.gradle')),('mods','version="11.63.23"' in text('src/main/resources/META-INF/mods.toml')),('manifest',manifest['version'] in ('11.63.23','11.63.24','11.63.26','11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61')),('count',len(manifest['tests'])>=154)]: ok(n,v)
for p,tokens in {
'src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java':['ElementalShovelItem','case "tc4_elementalshovel" -> new ElementalShovelItem(functionalProperties)'],
'src/main/java/com/darkifov/thaumcraft/item/ElementalShovelItem.java':['extends ShovelItem','ThreadLocal<Boolean> INTERNAL_HARVEST','BlockTags.MINEABLE_WITH_SHOVEL','gameMode.destroyBlock(target)','new FollowingItemEntity(level','player, 3','NBT_ORIENTATION','cycleOrientation','placementPlane','sourceState.is(Blocks.GRASS_BLOCK)','Blocks.DIRT.defaultBlockState()','FluidTags.LAVA','BlockSnapshot.create','ForgeEventFactory.onBlockPlace','snapshot.restore(true, false)','state.canSurvive','hurtAndBreak(1'],
'src/main/java/com/darkifov/thaumcraft/client/ElementalShovelPreviewClient.java':['AFTER_TRANSLUCENT_BLOCKS','isShiftKeyDown','previewPositions','LevelRenderer.renderLineBox'],
'src/main/java/com/darkifov/thaumcraft/network/RequestWandArchitectTogglePacket.java':['ElementalShovelItem.cycleOrientation','message.thaumcraft.elemental_shovel.mode'],
'src/main/java/com/darkifov/thaumcraft/client/ClientWandArchitectEvents.java':['heldElementalShovel','requestWandArchitectToggleFromClient'],
}.items():
 for t in tokens: need(p,t)
for tid in ['tools.elemental_shovel_material_rarity_repair_and_sneak_bypass','tools.elemental_shovel_three_by_three_mining_face_planes','tools.elemental_shovel_three_orientation_cycle_and_nbt','tools.elemental_shovel_exact_state_placement_inventory_and_grass_fallback','tools.elemental_shovel_block_place_event_lava_and_block_entity_rejection','tools.elemental_shovel_preview_multiplayer_durability_and_save_reload']: ok('manifest:'+tid,tid in ids)
lang=json.loads(text('src/main/resources/assets/thaumcraft/lang/en_us.json')); ok('lang',lang.get('item.thaumcraft.tc4_elementalshovel')=='Shovel of the Earthmover')
tex=R/'src/main/resources/assets/thaumcraft/textures/item/tc4/elementalshovel.png'; ok('texture_exists',tex.is_file()); ok('texture_sha',tex.is_file() and hashlib.sha256(tex.read_bytes()).hexdigest()=='b62238822a08e2c048b649f98334f947141f30465ba5bfd5948f2e3dc5ca4e78')
fail=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(fail)}/{len(checks)} passed'); sys.exit(bool(fail))
