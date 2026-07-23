#!/usr/bin/env python3
from pathlib import Path
import hashlib, json, sys, re, subprocess
R=Path(__file__).resolve().parents[1]; checks=[]
# v11.64.21 is the source-correcting full closure of this older stage.
_build=(R/'build.gradle').read_text(encoding='utf-8',errors='ignore')
_m=re.search(r"(?m)^\s*version\s*=\s*['\"](\d+)\.(\d+)\.(\d+)['\"]",_build)
if _m and tuple(map(int,_m.groups())) >= (11,64,21):
    r=subprocess.run([sys.executable,'tools/tc4_116421_arcane_pressure_plate_full_closure_guard.py'],cwd=R)
    if r.returncode: raise SystemExit(r.returncode)
    print('TC4 v11.63.27 Arcane Pressure Plate guard: PASS via superseding v11.64.21 full-closure guard')
    raise SystemExit(0)
def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def ok(n,v): checks.append((n,bool(v)))
def need(p,t): ok(f'{p}:{t[:96]}',t in text(p))
def sha(p): return hashlib.sha256((R/p).read_bytes()).hexdigest()
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids={x.get('id') for x in manifest.get('tests',[])}
audit=json.loads(text('reports/tc4_remaining_objects_audit_v11.63.27.json'))

ok('build_version_116327',"version = '11.63.27'" in text('build.gradle'))
ok('mods_version_116327',any(v in text('src/main/resources/META-INF/mods.toml') for v in ('version="11.63.27"','version="11.63.28"','version="11.63.29"')))
ok('manifest_version_116327',manifest.get('version') in ('11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61'))
ok('manifest_count_at_least_226',len(manifest.get('tests',[]))>=226)

for p,tokens in {
'src/main/java/com/darkifov/thaumcraft/block/ArcanePressurePlateBlock.java':[
 'extends BaseEntityBlock','BooleanProperty POWERED = BlockStateProperties.POWERED','IntegerProperty SETTING = IntegerProperty.create("setting", 0, 2)',
 'Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.0D, 15.0D)','Block.box(1.0D, 0.0D, 1.0D, 15.0D, 0.5D, 15.0D)',
 'new AABB(0.125D, 0.0D, 0.125D, 0.875D, 0.25D, 0.875D)','CHECK_TICKS = 20',
 'registerDefaultState','builder.add(POWERED, SETTING)','new ArcanePressurePlateBlockEntity(pos, state)','plate.initializeOwner(player)',
 'plate.cycleSetting(player)','message.thaumcraft.arcane_pressure_plate.setting_','updatePlate(level, pos','entity.isIgnoringBlockTriggers()',
 'level.getEntitiesOfClass(Entity.class, scan, plate::shouldTrigger)','level.scheduleTick(pos, this, CHECK_TICKS)',
 'SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON','SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF','SoundSource.BLOCKS, 0.2F',
 'shouldPower ? 0.6F : 0.5F','state.getValue(POWERED) ? 15 : 0','side == Direction.UP ? 15 : 0',
 'Shapes.empty()','isFaceSturdy','PushReaction.BLOCK','canEntityDestroy'],
'src/main/java/com/darkifov/thaumcraft/blockentity/ArcanePressurePlateBlockEntity.java':[
 'extends BlockEntity','private UUID owner = NIL_UUID','standardAccess = new LinkedHashSet','fullAccess = new LinkedHashSet','private byte setting',
 'initializeOwner(Player player)','player.getUUID()','hasAccess(Player player)','hasFullAccess(Player player)','mayBindKey(Player player, boolean goldKey)',
 'isOwner(player) || (!goldKey && hasFullAccess(player))','mayConfigure(Player player)','addAccess(Player player, boolean full)',
 '(setting() + 1) % 3','entity.isIgnoringBlockTriggers()','mode == 1 ? !hasAccess(player) : hasAccess(player)',
 'state.setValue(ArcanePressurePlateBlock.SETTING, setting())','tag.putUUID("Owner"','tag.putByte("setting"',
 'tag.put("StandardAccess"','tag.put("FullAccess"','NbtUtils.createUUID','NbtUtils.loadUUID','getUpdateTag()','getUpdatePacket()','onDataPacket'],
'src/main/java/com/darkifov/thaumcraft/item/ArcaneKeyItem.java':[
 'extends Item','private final boolean gold','properties.stacksTo(64)','instanceof ArcaneAccessTarget target',
 'target.mayBindKey(player, gold)','ItemStack bound = new ItemStack(this)','bind(bound, context.getLevel(), target.keyBindingPos())',
 'deliverBoundCopy','TC4Sounds.event("key")','SoundSource.BLOCKS, 1.0F, 0.9F','matches(held, context.getLevel(), target.keyBindingPos())',
 'target.hasAccess(player)','target.addAccess(player, gold)','held.shrink(1)','SoundSource.BLOCKS, 1.0F, 1.1F',
 'player.setItemInHand(context.getHand(), bound)','player.addItem(bound)','player.drop(bound, false)',
 'tag.putString(TAG_DIMENSION, level.dimension().location().toString())','ResourceLocation.tryParse','dimension.equals(level.dimension().location())',
 'isFoil(ItemStack stack)','tooltip.thaumcraft.arcane_key.blank','tooltip.thaumcraft.arcane_key.bound'],
'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java':[
 'ARCANE_PRESSURE_PLATE = BLOCKS.register("tc4_block_arcane_pressure_plate"','new ArcanePressurePlateBlock(',
 'ARCANE_PRESSURE_PLATE_ITEM = ITEMS.register("tc4_block_arcane_pressure_plate"','Map.entry("tc4_block_arcane_pressure_plate", ARCANE_PRESSURE_PLATE_ITEM)',
 'ARCANE_PRESSURE_PLATE_BLOCK_ENTITY','ArcanePressurePlateBlockEntity::new, ARCANE_PRESSURE_PLATE.get()'],
'src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java':[
 'case "tc4_keyiron" -> new ArcaneKeyItem','case "tc4_keygold" -> new ArcaneKeyItem','false);','true);'],
'src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java':[
 'instanceof ArcanePressurePlateBlockEntity plate','!plate.isOwner(player)','!player.getAbilities().instabuild',
 'message.thaumcraft.arcane_pressure_plate.protected','event.setCanceled(true)']
}.items():
 for token in tokens: need(p,token)

# Resources and model semantics.
resources=['src/main/resources/assets/thaumcraft/blockstates/tc4_block_arcane_pressure_plate.json',
 'src/main/resources/assets/thaumcraft/models/item/tc4_block_arcane_pressure_plate.json',
 'src/main/resources/data/thaumcraft/loot_tables/blocks/tc4_block_arcane_pressure_plate.json']
for setting in range(3):
 for suffix in ('up','down'):
  resources.append(f'src/main/resources/assets/thaumcraft/models/block/tc4_block_arcane_pressure_plate_{setting}_{suffix}.json')
for p in resources: ok('resource:'+p,(R/p).is_file())
bs=json.loads(text('src/main/resources/assets/thaumcraft/blockstates/tc4_block_arcane_pressure_plate.json'))
for setting in range(3):
 for powered,suffix in ((False,'up'),(True,'down')):
  key=f'powered={str(powered).lower()},setting={setting}'
  ok('blockstate:'+key,bs.get('variants',{}).get(key,{}).get('model')==f'thaumcraft:block/tc4_block_arcane_pressure_plate_{setting}_{suffix}')
for setting in range(3):
 for suffix,height in (('up',1.0),('down',0.5)):
  model=json.loads(text(f'src/main/resources/assets/thaumcraft/models/block/tc4_block_arcane_pressure_plate_{setting}_{suffix}.json'))
  ok(f'model_one_element_{setting}_{suffix}',len(model.get('elements',[]))==1)
  elem=model.get('elements',[{}])[0]
  ok(f'model_from_{setting}_{suffix}',elem.get('from')==[1,0,1])
  ok(f'model_to_{setting}_{suffix}',elem.get('to')==[15,height,15])
  ok(f'model_texture_{setting}_{suffix}',model.get('textures',{}).get('plate')==f'thaumcraft:block/tc4/applate{setting+1}')
item=json.loads(text('src/main/resources/assets/thaumcraft/models/item/tc4_block_arcane_pressure_plate.json'))
ok('item_3d_parent',item.get('parent')=='thaumcraft:block/tc4_block_arcane_pressure_plate_0_up')
loot=json.loads(text('src/main/resources/data/thaumcraft/loot_tables/blocks/tc4_block_arcane_pressure_plate.json'))
ok('loot_self','thaumcraft:tc4_block_arcane_pressure_plate' in json.dumps(loot))
axe=json.loads(text('src/main/resources/data/minecraft/tags/blocks/mineable/axe.json'))
ok('axe_tag','thaumcraft:tc4_block_arcane_pressure_plate' in axe.get('values',[]))
for lang,plate,iron,gold in [('en_us.json','Arcane Pressure Plate','Iron Key','Gold Key'),('ru_ru.json','Магическая нажимная плита','Железный ключ','Золотой ключ')]:
 data=json.loads(text('src/main/resources/assets/thaumcraft/lang/'+lang))
 ok('lang_item_plate_'+lang,data.get('item.thaumcraft.tc4_block_arcane_pressure_plate')==plate)
 ok('lang_block_plate_'+lang,data.get('block.thaumcraft.tc4_block_arcane_pressure_plate')==plate)
 ok('lang_iron_'+lang,data.get('item.thaumcraft.tc4_keyiron')==iron)
 ok('lang_gold_'+lang,data.get('item.thaumcraft.tc4_keygold')==gold)
 for key in ['setting_0','setting_1','setting_2','protected']:
  ok(f'lang_plate_message_{lang}_{key}',f'message.thaumcraft.arcane_pressure_plate.{key}' in data)
 for key in ['no_permission','bound_plate','wrong_target','already_authorized','access_granted','full_access_granted']:
  ok(f'lang_key_message_{lang}_{key}',f'message.thaumcraft.arcane_key.{key}' in data)

# Retained source assets.
for name in ['applate1','applate2','applate3']:
 current=f'src/main/resources/assets/thaumcraft/textures/block/tc4/{name}.png'
 original=f'src/main/resources/assets/thaumcraft/original_tc4_1710/textures/blocks/{name}.png'
 ok('byte_exact:'+name,(R/current).is_file() and (R/original).is_file() and sha(current)==sha(original))
for name in ['keyiron','keygold']:
 current=f'src/main/resources/assets/thaumcraft/textures/item/tc4/{name}.png'
 original=f'src/main/resources/assets/thaumcraft/original_tc4_1710/textures/items/{name}.png'
 ok('byte_exact:'+name,(R/current).is_file() and (R/original).is_file() and sha(current)==sha(original))
ok('byte_exact:key_sound',sha('src/main/resources/assets/thaumcraft/sounds/key.ogg')==sha('src/main/resources/assets/thaumcraft/original_tc4_1710/sounds/key.ogg'))

for tid in ['blocks.arcane_pressure_plate_three_owner_modes','blocks.arcane_pressure_plate_shape_signal_and_tick_release',
 'blocks.arcane_pressure_plate_owner_break_and_piston_protection','items.arcane_key_owner_binding_stack_handling',
 'items.arcane_key_access_levels_and_wrong_target','blocks.arcane_pressure_plate_nbt_sync_and_multiplayer']:
 ok('runtime_id:'+tid,tid in ids)

ok('audit_version',audit.get('version')=='11.63.27')
ok('audit_fallback_189',audit.get('generic_fallback_ids')==189)
ok('audit_item_146',audit.get('generic_fallback_item_like_ids')==146)
ok('audit_block_43',audit.get('generic_fallback_block_alias_ids')==43)
for rid in ['tc4_block_arcane_pressure_plate','tc4_keyiron','tc4_keygold']:
 ok('not_fallback:'+rid,rid not in audit.get('generic_fallback',[]))
for p,token in [('README.md','11.63.27 — Arcane Pressure Plate'),('KNOWN_DEVIATIONS.md','11.63.27 Arcane Pressure Plate'),
 ('TC4_11.63.27_ARCANE_PRESSURE_PLATE_KEYS_PORT_REPORT_RU.md','Arcane Pressure Plate / Магическая нажимная плита'),
 ('TC4_11.63.27_REMAINING_OBJECTS_AUDIT_RU.md','**189**')]: need(p,token)
for wf in ['.github/workflows/build.yml','.github/workflows/release.yml']:
 need(wf,'tc4_116326_arcane_ear_parity_guard.py')
 need(wf,'tc4_116327_arcane_pressure_plate_parity_guard.py')
failed=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed')
sys.exit(1 if failed else 0)
