#!/usr/bin/env python3
from pathlib import Path
import hashlib, json, sys
import subprocess
R=Path(__file__).resolve().parents[1]
# Forward-compatible: v11.64.16 replaced the earlier approximate opener/distance/drop
# adapter with the complete source-backed Hungry Chest closure.
if "version = '11.64.16'" in (R / "build.gradle").read_text(encoding="utf-8"):
    result = subprocess.run([sys.executable, "tools/tc4_116416_hungry_chest_full_closure_guard.py"], cwd=R)
    if result.returncode:
        raise SystemExit(result.returncode)
    print("TC4 v11.63.21 Hungry Chest historical guard: PASS via superseding v11.64.16 full-closure contract")
    raise SystemExit(0)
checks=[]
def text(p):
 q=R/p; return q.read_text(encoding='utf-8',errors='ignore') if q.is_file() else ''
def ok(n,v): checks.append((n,bool(v)))
def need(p,t): ok(f'{p}:{t[:96]}',t in text(p))
def sha(p): return hashlib.sha256((R/p).read_bytes()).hexdigest()
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids={x.get('id') for x in manifest.get('tests',[])}
ok('build_version_116321',"version = '11.63.23'" in text('build.gradle'))
ok('mods_version_116321','version="11.63.23"' in text('src/main/resources/META-INF/mods.toml'))
ok('manifest_version_116321',manifest.get('version') in ('11.63.23','11.63.24','11.63.26','11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61'))
ok('manifest_count_at_least_196',len(manifest.get('tests',[]))>=196)
for p,tokens in {
'src/main/java/com/darkifov/thaumcraft/block/HungryChestBlock.java':[
 'extends BaseEntityBlock','HorizontalDirectionalBlock.FACING','Direction.SOUTH',
 'Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D)',
 'return RenderShape.ENTITYBLOCK_ANIMATED','new HungryChestBlockEntity(pos, state)',
 'level.isClientSide ? HungryChestBlockEntity::clientTick : HungryChestBlockEntity::serverTick',
 'player.openMenu(chest)','entity instanceof ItemEntity itemEntity','itemEntity.isAlive()',
 'chest.eat(itemEntity)','Containers.dropContents(level, pos, chest)',
 'hasAnalogOutputSignal','AbstractContainerMenu.getRedstoneSignalFromContainer(chest)',
 'return false;'],
'src/main/java/com/darkifov/thaumcraft/blockentity/HungryChestBlockEntity.java':[
 'implements Container, MenuProvider','public static final int SIZE = 27',
 'NonNullList.withSize(SIZE, ItemStack.EMPTY)','private final InvWrapper forgeItemHandler = new InvWrapper(this)',
 'LazyOptional.of(() -> forgeItemHandler)','ForgeCapabilities.ITEM_HANDLER',
 'ItemHandlerHelper.insertItemStacked(forgeItemHandler, original.copy(), false)',
 'int moved = original.getCount() - remainder.getCount()','if (moved <= 0)',
 'itemEntity.discard()','itemEntity.setItem(remainder)',
 '(level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F',
 'SoundEvents.GENERIC_EAT','SoundSource.BLOCKS, 0.25F',
 'lidAngle = Math.max(lidAngle, 0.2F)','level.blockEvent(worldPosition, getBlockState().getBlock(), 2, 2)',
 'if (id == 1)','if (id == 2)','Math.max(0, data) / 10.0F',
 'if (++chest.ticksSinceSync % 80 == 0)','chest.recountOpeners()',
 'new net.minecraft.world.phys.AABB(worldPosition).inflate(5.0D)',
 'player.containerMenu instanceof ChestMenu menu','menu.getContainer() == this',
 'level.blockEvent(worldPosition, getBlockState().getBlock(), 1, openCount)',
 'openCount > 0 && lidAngle == 0.0F','lidAngle += openCount > 0 ? 0.1F : -0.1F',
 'lidAngle < 0.5F && oldAngle >= 0.5F','level.random.nextFloat() * 0.1F + 0.9F',
 'ChestMenu.threeRows(containerId, playerInventory, this)',
 'player.distanceToSqr(worldPosition.getX() + 0.5D','<= 64.0D',
 'ContainerHelper.saveAllItems(tag, items)','ContainerHelper.loadAllItems(tag, items)',
 'level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock())',
 'itemHandler.invalidate()','itemHandler = LazyOptional.of(() -> forgeItemHandler)'],
'src/main/java/com/darkifov/thaumcraft/client/render/model/TC4HungryChestModel.java':[
 'new ResourceLocation(ThaumcraftMod.MOD_ID, "hungry_chest")',
 'CubeListBuilder.create().texOffs(0, 19)','addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F)',
 'CubeListBuilder.create().texOffs(0, 0)','addBox(0.0F, -5.0F, -14.0F, 14.0F, 5.0F, 14.0F)',
 'PartPose.offset(1.0F, 7.0F, 15.0F)',
 'addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 1.0F)',
 'PartPose.offset(8.0F, 7.0F, 15.0F)','LayerDefinition.create(mesh, 64, 64)',
 'lid.xRot = xRotation','knob.xRot = xRotation'],
'src/main/java/com/darkifov/thaumcraft/client/render/HungryChestRenderer.java':[
 'implements BlockEntityRenderer<HungryChestBlockEntity>',
 'textures/original/thaumcraft4/models/chesthungry.png',
 '1.0F - open * open * open','-(open * ((float) Math.PI / 2.0F))',
 'poseStack.translate(0.0D, 1.0D, 1.0D)','poseStack.scale(1.0F, -1.0F, -1.0F)',
 'RenderType.entityCutoutNoCull(TEXTURE)','case NORTH -> 180.0F','case WEST -> 90.0F',
 'case EAST -> -90.0F'],
'src/main/java/com/darkifov/thaumcraft/block/HungryChestBlockItem.java':[
 'extends BlockItem','initializeClient(Consumer<IClientItemExtensions> consumer)',
 'getCustomRenderer()','HungryChestItemRenderer.instance()'],
'src/main/java/com/darkifov/thaumcraft/client/render/HungryChestItemRenderer.java':[
 'extends BlockEntityWithoutLevelRenderer','renderByItem(ItemStack stack',
 'TC4HungryChestModel.LAYER','HungryChestRenderer.renderModel(model, Direction.SOUTH, 0.0F',
 'type == ItemTransforms.TransformType.GUI','type.firstPerson()',
 'type == ItemTransforms.TransformType.GROUND','type == ItemTransforms.TransformType.FIXED'],
'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java':[
 'HungryChestBlock','HungryChestBlockItem','HUNGRY_CHEST = hungryChestBlock("hungry_chest"',
 '.strength(2.5F, 4.0F).sound(SoundType.WOOD).noOcclusion()',
 'HUNGRY_CHEST_BLOCK_ENTITY','BlockEntityType.Builder.of(HungryChestBlockEntity::new, HUNGRY_CHEST.get())'],
'src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java':[
 'TC4HungryChestModel.LAYER','TC4HungryChestModel::createBodyLayer',
 'HUNGRY_CHEST_BLOCK_ENTITY.get()','blockEntityRenderer(HungryChestRenderer::new)'],
}.items():
 for t in tokens: need(p,t)
# Data and resource contracts.
for p in [
 'src/main/resources/assets/thaumcraft/models/item/hungry_chest.json',
 'src/main/resources/assets/thaumcraft/blockstates/hungry_chest.json',
 'src/main/resources/assets/thaumcraft/models/block/hungry_chest.json',
 'src/main/resources/data/thaumcraft/loot_tables/blocks/hungry_chest.json',
 'src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_hungrychest.json']:
 ok('exists:'+p,(R/p).is_file())
item=json.loads(text('src/main/resources/assets/thaumcraft/models/item/hungry_chest.json'))
ok('item_builtin_entity',item.get('parent')=='builtin/entity' and item.get('gui_light')=='front')
state=json.loads(text('src/main/resources/assets/thaumcraft/blockstates/hungry_chest.json'))
variants=state.get('variants',{})
for facing,rot in [('north',180),('south',0),('west',90),('east',270)]:
 key='facing='+facing; ok('blockstate:'+key,key in variants)
 if key in variants:
  ok('blockstate_model:'+key,variants[key].get('model')=='thaumcraft:block/hungry_chest')
  ok('blockstate_rotation:'+key,variants[key].get('y',0)==rot)
for lang,name in [('en_us','Hungry Chest'),('ru_ru','Голодный сундук')]:
 data=json.loads(text(f'src/main/resources/assets/thaumcraft/lang/{lang}.json'))
 ok('lang_block:'+lang,data.get('block.thaumcraft.hungry_chest')==name)
 ok('lang_container:'+lang,data.get('container.thaumcraft.hungry_chest')==name)
orig='src/main/resources/assets/thaumcraft/original_tc4_1710/textures/models/chesthungry.png'
world='src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/chesthungry.png'
compat='src/main/resources/assets/thaumcraft/textures/models/chesthungry.png'
for p in [orig,world,compat]: ok('texture:'+p,(R/p).is_file())
ok('texture_world_byte_exact',(R/orig).is_file() and (R/world).is_file() and sha(orig)==sha(world))
ok('texture_compat_byte_exact',(R/orig).is_file() and (R/compat).is_file() and sha(orig)==sha(compat))
for tid in [
 'blocks.hungry_chest_item_absorption_full_partial_and_rejection',
 'blocks.hungry_chest_exact_eat_sound_and_lid_nudge',
 'blocks.hungry_chest_three_row_menu_multiplayer_openers',
 'blocks.hungry_chest_lid_sound_timing_and_facing',
 'blocks.hungry_chest_hopper_capability_comparator_and_break_drops',
 'blocks.hungry_chest_world_item_geometry_save_reload_and_resource_reload']:
 ok('manifest:'+tid,tid in ids)
for wf in ['.github/workflows/build.yml','.github/workflows/release.yml']:
 need(wf,'tc4_116321_hungry_chest_parity_guard.py')
 need(wf,'Validate v11.63.21 Hungry Chest parity')
need('README.md','11.63.21 — Hungry Chest inventory, pickup and lid parity')
need('KNOWN_DEVIATIONS.md','11.63.21 Hungry Chest runtime notes')
failed=[n for n,v in checks if not v]
for n,v in checks: print(('PASS' if v else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed')
sys.exit(1 if failed else 0)
