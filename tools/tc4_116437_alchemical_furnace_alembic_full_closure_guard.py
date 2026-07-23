#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re
R=Path(__file__).resolve().parents[1]
O=R/'reference/original_source/Thaumcraft4-1.7.10-master'
def text(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def original(p): return (O/p).read_text(encoding='utf-8',errors='replace')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.37 furnace/alembic full-closure guard: FAIL: '+msg)
def sha(p): return hashlib.sha256(Path(p).read_bytes()).digest()
req("version = '11.64.37'" in text('build.gradle') or "version = '11.64.38'" in text('build.gradle'),'build version')
req('version="11.64.37"' in text('src/main/resources/META-INF/mods.toml') or 'version="11.64.38"' in text('src/main/resources/META-INF/mods.toml'),'mods version')
parity=text('src/main/java/com/darkifov/thaumcraft/alchemy/TC4AlchemicalFurnaceParity.java')
for t in ('CONTRACT_VERSION = "11.64.37"','FURNACE_CAPACITY = 50','ALEMBIC_CAPACITY = 32','MAX_ALEMBICS = 5','NORMAL_INTERVAL = 40','ALUMENTUM_INTERVAL = 20','DISTILLATION_STEP = 1','ALUMENTUM_BURN_TIME = 6400','BURNING_LIGHT = 12','BELLOWS_REDUCTION = 0.125D','smeltTime','alembicFillMessage','comparator'):
    req(t in parity,'parity '+t)
furnace=text('src/main/java/com/darkifov/thaumcraft/blockentity/AlchemicalFurnaceBlockEntity.java')
for t in ('CAPACITY = TC4AlchemicalFurnaceParity.FURNACE_CAPACITY','TC4AlchemicalFurnaceParity.smeltTime','TC4AlchemicalFurnaceParity.ALUMENTUM_BURN_TIME','TC4DistillationRuntime.tickFurnaceToAlembics','tag.putShort("BurnTime"','tag.putShort("Vis"','tag.putBoolean("speedBoost"','tag.putShort("CookTime"','tag.put("Items", items)','writeOriginalAspects','counter = 0','bellows = -1','stack.is(Items.BUCKET)','new SidedInvWrapper(this, direction)','ForgeCapabilities.ITEM_HANDLER','Component.translatable("container.alchemyfurnace")'):
    req(t in furnace,'furnace '+t)
for forbidden in ('tag.putInt("Counter"','tag.putInt("Bellows"','tag.putInt("BurnDuration"','tag.put("PendingAspects"','tag.put("Input"','tag.put("Fuel"'):
    req(forbidden not in furnace,'temporary/legacy furnace NBT '+forbidden)
dist=text('src/main/java/com/darkifov/thaumcraft/essentia/TC4DistillationRuntime.java')
req('ORIGINAL_MAX_ALEMBICS_ABOVE_FURNACE = com.darkifov.thaumcraft.alchemy.TC4AlchemicalFurnaceParity.MAX_ALEMBICS' in dist,'five alembic production binding')
block=text('src/main/java/com/darkifov/thaumcraft/block/AlchemicalFurnaceBlock.java')
for t in ('BooleanProperty.create("lit")','state.getValue(LIT)','ParticleTypes.SMOKE','ParticleTypes.FLAME','!player.isShiftKeyDown()','NetworkHooks.openScreen'):
    req(t in block,'furnace block '+t)
req('Alchemical Furnace |' not in block and 'Fuel placed' not in block,'debug/direct insertion path remains')
reg=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
req('AlchemicalFurnaceBlock.LIT) ? 12 : 0' in reg,'dynamic original light')
menu=text('src/main/java/com/darkifov/thaumcraft/menu/AlchemicalFurnaceMenu.java')
req('public int burnProgress() { return data.get(0); }' in menu,'original data order cook')
req('public int fuelTime() { return data.get(1); }' in menu,'original data order burn')
quick=menu[menu.index('public ItemStack quickMoveStack'):]; req(quick.index('canPlaceItem(AlchemicalFurnaceBlockEntity.SLOT_FUEL') < quick.index('canPlaceItem(AlchemicalFurnaceBlockEntity.SLOT_INPUT'),'shift-click fuel priority')
alembic=text('src/main/java/com/darkifov/thaumcraft/blockentity/AlembicBlockEntity.java')
for t in ('CAPACITY = TC4AlchemicalFurnaceParity.ALEMBIC_CAPACITY','emptyAspectType','tag.putString("aspect"','tag.putShort("amount"','tag.putString("AspectFilter"','tag.putByte("facing"','aboveFurnace()','aboveAlembic()','comparatorOutput','fillMessageIndex'):
    req(t in alembic,'alembic '+t)
for forbidden in ('tag.put("Aspects"','tag.putString("Aspect"','tag.putShort("Amount"'):
    req(forbidden not in alembic,'nonoriginal alembic NBT '+forbidden)
ale_block=text('src/main/java/com/darkifov/thaumcraft/block/AlembicBlock.java')
for t in ('Component.translatable("tile.alembic.msg."','TC4Sounds.event("alembicknock")','held.getItem() instanceof JarLabelItem','JarLabelItem.getAspect','held.getItem() instanceof EssentiaJarBlockItem','ThaumcraftMod.VOID_ESSENTIA_JAR','hasAnalogOutputSignal','comparatorOutput','onRemove','ThaumcraftMod.JAR_LABEL'):
    req(t in ale_block,'alembic block '+t)
req('EssentiaPhialItem' not in ale_block,'nonoriginal phial interaction remains')
req('displayClientMessage(Component.literal' not in ale_block,'debug English messages remain')
renderer=text('src/main/java/com/darkifov/thaumcraft/client/render/AlembicRenderer.java')
for t in ('TC4AlembicModel.renderTubeMain','TC4AlembicModel.renderTubeSmall','TC4AlembicModel.renderLegs','TC4AlembicModel.renderPot','TC4AlembicModel.renderPanel','renderLabel','renderConnectors','TC4ArcaneBoreModel','EssentiaTubeBlockEntity','rotateForFacing'):
    req(t in renderer,'renderer '+t)
req('renderLiquidBox' not in renderer and 'FILL_TEXTURE' not in renderer,'invented liquid renderer remains')
orig_f=original('thaumcraft/common/tiles/TileAlchemyFurnace.java')
for t in ('while (deep < 5)','this.speedBoost ? 20 : 40','vs * 10 * (1.0F - 0.125F * this.bellows)','func_74777_a("BurnTime"','func_74777_a("Vis"','func_74757_a("speedBoost"','func_74777_a("CookTime"','func_74782_a("Items"','par2ItemStack.func_77973_b() == Items.field_151133_ar'):
    req(t in orig_f,'original furnace '+t)
orig_a=original('thaumcraft/common/tiles/TileAlembic.java')
for t in ('maxAmount = 32','func_74778_a("aspect"','func_74778_a("AspectFilter"','func_74777_a("amount"','func_74774_a("facing"','face != ForgeDirection.getOrientation(this.facing)','face != ForgeDirection.DOWN'):
    req(t in orig_a,'original alembic '+t)
orig_b=original('thaumcraft/common/blocks/BlockMetalDevice.java')
for t in ('tile.amount < tile.maxAmount * 0.4D','tile.amount < tile.maxAmount * 0.8D','"thaumcraft:alembicknock"','ConfigItems.itemResource, 1, 13','ConfigItems.itemJarFilled','drop.func_77960_j() == 3'):
    req(t in orig_b,'original interaction '+t)
# Exact resources already retained in the archive.
assets=[
('gui/gui_alchemyfurnace.png','gui/gui_alchemyfurnace.png'),
('models/alembic.png','models/alembic.png'),
('models/alembic.obj','models/alembic.obj'),
('models/label.png','models/label.png'),
('blocks/al_furnace_side.png','blocks/al_furnace_side.png'),
('blocks/al_furnace_top.png','blocks/al_furnace_top.png'),
('blocks/al_furnace_top_filled.png','blocks/al_furnace_top_filled.png'),
('blocks/al_furnace_front_off.png','blocks/al_furnace_front_off.png'),
('blocks/al_furnace_front_on.png','blocks/al_furnace_front_on.png')]
for orig_rel,port_rel in assets:
    req(sha(O/'assets/thaumcraft/textures'/orig_rel)==sha(R/'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4'/port_rel),'resource '+orig_rel)
gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=281 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for name in ('alchemicalFurnaceProcessesPersistsAndRespectsCapacity','furnaceFeedsFiveAlembicsOnLocalFortiethTick','alembicKeepsCapacityFilterSidesAndNbt','furnaceSidedAutomationMatchesOriginal','alembicTypedEmptyStateAndComparatorMatchOriginal','alembicFillMessageThresholdsMatchOriginal'):
    req(name in methods,'GameTest '+name)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest.get('version','0.0.0').split('.'))) >= (11,64,37),'manifest version')
req(len(ids)>=835 and len(ids)==len(set(ids)),'manifest count/unique')
for sid in ('gameplay.five_alembic_stack','automation.furnace_sided_inventory','persistence.furnace_canonical_nbt','persistence.alembic_canonical_nbt','client.alembic_stack_geometry','integration.distillation_restart'):
    req(sid in ids,'scenario '+sid)
req((R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md').is_file(),'universal prompt')
req('Файл `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен' in text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md'),'mandatory prompt wording')
print('TC4 v11.64.37 furnace/alembic full-closure guard: PASS (281 GameTests, 835 scenarios, 9 exact assets)')
