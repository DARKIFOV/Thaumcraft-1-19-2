#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,sys
R=Path(__file__).resolve().parents[1]; checks=[]
def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def ok(n,v): checks.append((n,bool(v)))
def need(p,t): ok(f'{p}:{t[:90]}',t in text(p))
def sha(p): return hashlib.sha256((R/p).read_bytes()).hexdigest()
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids={x.get('id') for x in manifest.get('tests',[])}
ok('build_version_116324',"version = '11.63.24'" in text('build.gradle'))
ok('mods_version_116324','version="11.63.24"' in text('src/main/resources/META-INF/mods.toml'))
ok('manifest_version_116324',manifest.get('version') in ('11.63.24','11.63.26','11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61'))
ok('manifest_count_at_least_214',len(manifest.get('tests',[]))>=214)
for p,tokens in {
'src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneSpaBlockEntity.java':[
 'public static final int CAPACITY = 5000','public static final int CHECK_INTERVAL = 40','private static final int BUCKET = 1000',
 'private boolean mixing = true','private int counter','spa.counter++ % CHECK_INTERVAL','level.hasNeighborSignal(pos)',
 'salts.getStackInSlot(0).isEmpty()','isVanillaWater(stored.getFluid())','ThaumcraftMod.PURIFYING_FLUID.get()',
 'tank.drain(BUCKET','salts.extractItem(0, 1, false)','for (int x = -2; x <= 2; x++)','for (int z = -2; z <= 2; z++)',
 'for (Direction direction : Direction.values())','state.isSource()','level.getBlockEntity(pos) == null',
 'state.isAir() || state.getMaterial().isReplaceable()','isFaceSturdy(level, below, Direction.UP)',
 'side != Direction.UP && capability == ForgeCapabilities.FLUID_HANDLER','side != Direction.UP && capability == ForgeCapabilities.ITEM_HANDLER',
 'tag.putBoolean("Mix", mixing)','tag.put("Tank"','tag.put("Salts"','mixing = !tag.contains("Mix") || tag.getBoolean("Mix")'],
'src/main/java/com/darkifov/thaumcraft/block/ArcaneSpaBlock.java':[
 'extends BaseEntityBlock','new ArcaneSpaBlockEntity(pos, state)','ArcaneSpaBlockEntity::serverTick','NetworkHooks.openScreen',
 'spa.removeAllBathSalts()','Containers.dropItemStack'],
'src/main/java/com/darkifov/thaumcraft/menu/ArcaneSpaMenu.java':[
 'BUTTON_TOGGLE_MIX = 1','SlotItemHandler','65, 31','stack.is(ThaumcraftMod.BATH_SALTS.get())','spa.toggleMixing()'],
'src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneSpaScreen.java':[
 'TOGGLE_X = 89','TOGGLE_Y = 35','TANK_X = 107','TANK_Y = 15','TANK_HEIGHT = 48','event("cameraclack")','0.4F, 1.0F'],
'src/main/java/com/darkifov/thaumcraft/block/BathSaltsItem.java':['DISSOLVE_TICKS = 200','getEntityLifespan'],
'src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java':[
 'onBathSaltsExpire','itemEntity.getItem().is(ThaumcraftMod.BATH_SALTS.get())','fluidState.isSource()',
 'fluidState.getType() != net.minecraft.world.level.material.Fluids.WATER','ThaumcraftMod.PURIFYING_FLUID_BLOCK.get().defaultBlockState()'],
'src/main/java/com/darkifov/thaumcraft/block/PurifyingFluidBlock.java':[
 'class PurifyingFluidBlock extends LiquidBlock','level.getFluidState(pos).isSource()','PlayerThaumData.getWarpPerm(player)',
 'Math.floor(Math.sqrt(permanentWarp))','Math.min(32000, 200000 / divisor)','WARP_WARD.get()','Blocks.AIR.defaultBlockState()',
 'ParticleTypes.BUBBLE','SoundEvents.LAVA_POP'],
'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java':[
 'ARCANE_SPA = BLOCKS.register("tc4_block_arcane_spa"','ARCANE_SPA_ITEM = ITEMS.register("tc4_block_arcane_spa"',
 'BATH_SALTS = ITEMS.register("tc4_bath_salts"','PURIFYING_FLUID_BLOCK','ARCANE_SPA_BLOCK_ENTITY','ARCANE_SPA_MENU']
}.items():
 for token in tokens: need(p,token)
for p in [
 'src/main/resources/assets/thaumcraft/blockstates/tc4_block_arcane_spa.json',
 'src/main/resources/assets/thaumcraft/models/block/tc4_block_arcane_spa.json',
 'src/main/resources/assets/thaumcraft/models/item/tc4_block_arcane_spa.json',
 'src/main/resources/assets/thaumcraft/models/item/tc4_bath_salts.json',
 'src/main/resources/data/thaumcraft/loot_tables/blocks/tc4_block_arcane_spa.json']:
 ok('resource:'+p,(R/p).is_file())
for current,original in [
 ('src/main/resources/assets/thaumcraft/textures/block/tc4/spa_side.png','src/main/resources/assets/thaumcraft/original_tc4_1710/textures/blocks/spa_side.png'),
 ('src/main/resources/assets/thaumcraft/textures/block/tc4/spa_top.png','src/main/resources/assets/thaumcraft/original_tc4_1710/textures/blocks/spa_top.png'),
 ('src/main/resources/assets/thaumcraft/textures/block/tc4/fluidpure.png','src/main/resources/assets/thaumcraft/original_tc4_1710/textures/blocks/fluidpure.png'),
 ('src/main/resources/assets/thaumcraft/textures/item/tc4/bath_salts.png','src/main/resources/assets/thaumcraft/original_tc4_1710/textures/items/bath_salts.png'),
 ('src/main/resources/assets/thaumcraft/textures/gui/gui_spa.png','src/main/resources/assets/thaumcraft/original_tc4_1710/textures/gui/gui_spa.png')]:
 ok('byte_exact:'+current,(R/current).is_file() and (R/original).is_file() and sha(current)==sha(original))
for tid in [
 'blocks.arcane_spa_water_salts_mix_output','blocks.arcane_spa_raw_fluid_mode_and_5x5_spread',
 'blocks.arcane_spa_six_direction_source_adjacency','blocks.arcane_spa_redstone_and_independent_40_tick_cadence',
 'blocks.arcane_spa_side_capability_and_top_rejection','blocks.arcane_spa_bath_salts_expiry_purifying_fluid_warp_ward']:
 ok('runtime_id:'+tid,tid in ids)
for p in ['README.md','KNOWN_DEVIATIONS.md','TC4_11.63.24_ARCANE_SPA_PORT_REPORT_RU.md','TC4_11.63.24_REMAINING_OBJECTS_AUDIT_RU.md']:
 ok('doc:'+p,(R/p).is_file() and '11.63.24' in text(p))
for wf in ['.github/workflows/build.yml','.github/workflows/release.yml']:
 need(wf,'tc4_116324_arcane_spa_parity_guard.py')
failed=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed')
sys.exit(1 if failed else 0)
