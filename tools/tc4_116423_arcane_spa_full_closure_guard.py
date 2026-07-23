#!/usr/bin/env python3
"""v11.64.23 guard: complete Arcane Spa source/resource closure."""
from pathlib import Path
import hashlib, json, re, zipfile
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.23 Arcane Spa full-closure guard: FAIL: '+msg)
def sha_bytes(b): return hashlib.sha256(b).hexdigest()
def sha(p): return hashlib.sha256((R/p).read_bytes()).hexdigest()
def version(p):
    s=text(p); m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',s)
    req(m,'version parse '+p); return tuple(map(int,m.groups()))
req(version('build.gradle')>=(11,64,23),'build version')
req(version('src/main/resources/META-INF/mods.toml')>=(11,64,23),'mods version')

par=text('src/main/java/com/darkifov/thaumcraft/block/TC4ArcaneSpaParity.java')
for t in ('CONTRACT_VERSION = "11.64.23"','CAPACITY_MB = 5000','BUCKET_MB = 1000',
 'CHECK_INTERVAL_TICKS = 40','OUTPUT_RADIUS = 2','SLOT_COUNT = 1','BLOCK_USE_DISTANCE_SQUARED = 64',
 'BLOCK_HARDNESS = 3.0F','BLOCK_RESISTANCE = 25.0F','CONSUME_BEFORE_PLACEMENT = true',
 'NBT_MIX = "mix"','NBT_ITEMS = "Items"','LEGACY_PORT_NBT_MIX = "Mix"',
 'LEGACY_PORT_NBT_TANK = "Tank"','LEGACY_PORT_NBT_SALTS = "Salts"',
 'shouldRunCycle','exposesAutomationSide','canAcceptFilledContainer','acceptedFluidAmount',
 'canPlaceOutput','emptyFluidMaskHeight','candidateOffsetX','candidateOffsetZ'):
    req(t in par,'parity token '+t)

block=text('src/main/java/com/darkifov/thaumcraft/block/ArcaneSpaBlock.java')
for t in ('player.isShiftKeyDown()','inspectFilledContainer(held)','ForgeCapabilities.FLUID_HANDLER_ITEM',
 'handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE)',
 'spa.fillFromHeldContainer(filled.fluid()) > 0','consumeFilledContainer(player, hand, held, filled.emptyContainer())',
 'SoundEvents.GENERIC_SWIM','0.33F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.3F',
 'NetworkHooks.openScreen','buffer.writeBlockPos(pos)','return InteractionResult.sidedSuccess(level.isClientSide)',
 'spa.removeAllBathSalts()','Containers.dropItemStack'):
    req(t in block,'block use token '+t)
req(block.index('inspectFilledContainer(held)') < block.index('NetworkHooks.openScreen'),'filled-container path must precede GUI')
req('FluidUtil.interactWithFluidHandler' not in block,'generic FluidUtil path would lose TC4 whole-container contract')

be=text('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneSpaBlockEntity.java')
for t in ('CAPACITY = TC4ArcaneSpaParity.CAPACITY_MB','CHECK_INTERVAL = TC4ArcaneSpaParity.CHECK_INTERVAL_TICKS',
 'TC4ArcaneSpaParity.shouldRunCycle(spa.counter++)','level.hasNeighborSignal(pos)','stored.getAmount() < BUCKET',
 'salts.getStackInSlot(0).isEmpty()','isVanillaWater(stored.getFluid())','ThaumcraftMod.PURIFYING_FLUID.get()',
 'level.dimensionType().ultraWarm() && isVanillaWater(targetFluid)',
 'for (int x = -TC4ArcaneSpaParity.OUTPUT_RADIUS','for (int z = -TC4ArcaneSpaParity.OUTPUT_RADIUS',
 'touchesTargetSource','for (Direction direction : Direction.values())','state.isSource()',
 'state.isAir() || state.getMaterial().isReplaceable()','isFaceSturdy(level, below, Direction.UP)',
 'TC4ArcaneSpaParity.canPlaceOutput(support, replaceable, false)',
 'tank.drain(BUCKET, IFluidHandler.FluidAction.EXECUTE)','salts.extractItem(0, 1, false)',
 'level.setBlock(output, outputState, 3)','canAcceptFilledContainer','fillFromHeldContainer',
 'tag.putBoolean(TC4ArcaneSpaParity.NBT_MIX, mixing)','fluid.writeToNBT(tag)',
 'tag.put(TC4ArcaneSpaParity.NBT_ITEMS, items)','FluidStack.loadFluidStackFromNBT(tag)',
 'LEGACY_PORT_NBT_MIX','LEGACY_PORT_NBT_TANK','LEGACY_PORT_NBT_SALTS',
 'TC4ArcaneSpaParity.exposesAutomationSide(sideOrdinal)','side.get3DDataValue()'):
    req(t in be,'block entity token '+t)
req(be.index('tank.drain(BUCKET') < be.index('level.setBlock(output'),'fluid must be consumed before placement')
req(be.index('salts.extractItem(0, 1, false)') < be.index('level.setBlock(output'),'salt must be consumed before placement')
req('level.getBlockEntity(pos) == null' not in be,'non-original block-entity exclusion')
req('if (level.setBlock(output' not in be,'placement result must be ignored')
req('tag.put("Tank"' not in be and 'tag.put("Salts"' not in be and 'tag.putBoolean("Mix"' not in be,'legacy keys leaked into new saves')

menu=text('src/main/java/com/darkifov/thaumcraft/menu/ArcaneSpaMenu.java')
for t in ('BUTTON_TOGGLE_MIX = 1','SlotItemHandler(spa.saltsHandler(), 0, 65, 31)',
 '8 + col * 18, 84 + row * 18','8 + col * 18, 142','spa.toggleMixing()',
 'moveItemStackTo(stack, 0, SPA_SLOT_COUNT, false)','TC4ArcaneSpaParity.emptyFluidMaskHeight'):
    req(t in menu,'menu token '+t)

screen=text('src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneSpaScreen.java')
for t in ('textures/gui/gui_spa.png','TOGGLE_X = 89','TOGGLE_Y = 35','TOGGLE_SIZE = 8',
 'TANK_X = 107','TANK_Y = 15','TANK_WIDTH = 8','TANK_HEIGHT = 48',
 'menu.isMixing() ? 16 : 32','renderFluid(poseStack, fluid','menu.emptyFluidMaskHeight(TANK_HEIGHT)',
 'TANK_X, TANK_Y, 10, emptyMask','leftPos + 106, topPos + 11, 232, 0, 10, 55',
 'offset += 8','menu.fluidAmount() + " mb"','TOGGLE_X - 1, TOGGLE_Y - 1, 10, 10',
 'TANK_X - 3, 10, 10, 55','event("cameraclack")','0.4F, 1.0F'):
    req(t in screen,'screen token '+t)

mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for t in ('new ArcaneSpaBlock(BlockBehaviour.Properties.of(Material.STONE).noOcclusion()',
 'TC4ArcaneSpaParity.BLOCK_HARDNESS, TC4ArcaneSpaParity.BLOCK_RESISTANCE',
 '.sound(SoundType.STONE)','new BlockItem(ARCANE_SPA.get(), new Item.Properties().tab(THAUMCRAFT_TAB))',
 'ARCANE_SPA_BLOCK_ENTITY','ARCANE_SPA_MENU'):
    req(t in mod,'registration '+t)
spa_item=re.search(r'ARCANE_SPA_ITEM\s*=.*?;\n',mod,re.S)
req(spa_item and '.rarity(' not in spa_item.group(0),'Arcane Spa must have common rarity')

recipe=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcanespa.json'))
req(recipe['tc4_key']=='ArcaneSpa' and recipe['research']=='ARCANESPA','recipe identity')
req(recipe['pattern']==['QIQ','SJS','SPS'],'recipe pattern')
req(recipe['key']=={'P':'minecraft:piston','J':'thaumcraft:essentia_jar','S':'thaumcraft:tc4_block_arcane_stone','Q':'minecraft:quartz_block','I':'minecraft:iron_bars'},'recipe key')
req(recipe['aspects']=={'AQUA':16,'ORDO':8,'TERRA':4},'recipe vis')
req(recipe['result']=={'item':'thaumcraft:tc4_block_arcane_spa','count':1},'recipe result')
model=json.loads(text('src/main/resources/assets/thaumcraft/models/block/tc4_block_arcane_spa.json'))
req(model['parent']=='minecraft:block/cube_bottom_top','spa full cube model')
req(model['textures']=={'side':'thaumcraft:block/tc4/spa_side','top':'thaumcraft:block/tc4/spa_top','bottom':'thaumcraft:block/tc4/pedestal_top'},'spa face textures')
state=json.loads(text('src/main/resources/assets/thaumcraft/blockstates/tc4_block_arcane_spa.json'))
req(state['variants'].get('')=={'model':'thaumcraft:block/tc4_block_arcane_spa'},'blockstate model')

with zipfile.ZipFile(R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    names=z.namelist()
    def original(suffix):
        n=next((n for n in names if n.endswith('/'+suffix)),None); req(n,'original missing '+suffix); return z.read(n).decode(errors='replace')
    ot=original('thaumcraft/common/tiles/TileSpa.java')
    ob=original('thaumcraft/common/blocks/BlockStoneDevice.java')
    oi=original('thaumcraft/common/blocks/BlockStoneDeviceItem.java')
    oc=original('thaumcraft/common/container/ContainerSpa.java')
    og=original('thaumcraft/client/gui/GuiSpa.java')
    orc=original('thaumcraft/common/config/ConfigRecipes.java')
    ors=original('thaumcraft/common/config/ConfigResearch.java')
    for cur,orig_path in [
      ('src/main/resources/assets/thaumcraft/textures/block/tc4/spa_side.png','assets/thaumcraft/textures/blocks/spa_side.png'),
      ('src/main/resources/assets/thaumcraft/textures/block/tc4/spa_top.png','assets/thaumcraft/textures/blocks/spa_top.png'),
      ('src/main/resources/assets/thaumcraft/textures/block/tc4/pedestal_top.png','assets/thaumcraft/textures/blocks/pedestal_top.png'),
      ('src/main/resources/assets/thaumcraft/textures/gui/gui_spa.png','assets/thaumcraft/textures/gui/gui_spa.png'),
      ('src/main/resources/assets/thaumcraft/textures/block/tc4/fluidpure.png','assets/thaumcraft/textures/blocks/fluidpure.png'),
      ('src/main/resources/assets/thaumcraft/textures/item/tc4/bath_salts.png','assets/thaumcraft/textures/items/bath_salts.png')]:
        n=next(n for n in names if n.endswith('/'+orig_path)); req(sha(cur)==sha_bytes(z.read(n)),'texture hash '+cur)
for t in ('private boolean mix = true','counter++ % 40 == 0','func_72864_z','hasIngredients()',
 'for (int xx = -2; xx <= 2; xx++)','for (int zz = -2; zz <= 2; zz++)','consumeIngredients();',
 'func_147449_b','isSideSolid','isReplaceable','BlockUtils.isBlockTouching','new FluidTank(5000)',
 'return from != ForgeDirection.UP','"mix"','"Items"','FluidStack.loadFluidStackFromNBT(nbttagcompound)'):
    req(t in ot,'original TileSpa '+t)
req(ot.index('consumeIngredients();',ot.index('func_145845_h')) < ot.index('func_147449_b',ot.index('func_145845_h')),'original consume-before-placement order')
for t in ('super(Material.field_151576_e)','func_149711_c(3.0F)','func_149752_b(25.0F)',
 'func_149672_a(Block.field_149769_e)','if (md == 12)','this.iconSpa[0]','this.iconSpa[1]',
 'metadata == 12','FluidContainerRegistry.getFluidForFilledItem','tile.tank.getFluidAmount() < tile.tank.getCapacity()',
 'player.openGui(Thaumcraft.instance, 19','"game.neutral.swim"'):
    req(t in ob,'original BlockStoneDevice '+t)
req('metadata == 12' not in oi or 'TileSpa' not in oi,'BlockStoneDeviceItem has no spa-specific rarity/placement mutation')
for t in ('SlotLimitedByClass(ItemBathSalts.class, tileEntity, 0, 65, 31)','button == 1','this.spa.toggleMix()',
 '8 + j * 18, 84 + i * 18','8 + i * 18, 142'):
    req(t in oc,'original ContainerSpa '+t)
for t in ('baseX + 104','baseY + 10','fluid.amount + " mb"','baseX + 88','baseY + 34',
 'k + 89, l + 35, 208, 16, 8, 8','k + 89, l + 35, 208, 32, 8, 8',
 'GL11.glScalef(8.0F, 8.0F, 8.0F)','for (int a = 0; a < 6; a++)',
 'k + 107, l + 15, 107, 15, 10','k + 106, l + 11, 232, 0, 10, 55',
 '"thaumcraft:cameraclack", 0.4F, 1.0F'):
    req(t in og,'original GuiSpa '+t)
req('ConfigResearch.recipes.put("ArcaneSpa", ThaumcraftApi.addArcaneCraftingRecipe("ARCANESPA"' in orc,'original recipe registration')
for t in ('"QIQ", "SJS", "SPS"','Aspect.WATER, 16','Aspect.ORDER, 8','Aspect.EARTH, 4',
 'new ItemStack(Blocks.field_150331_J)','new ItemStack(ConfigBlocks.blockJar)',
 'new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 6)','new ItemStack(Blocks.field_150371_ca)','new ItemStack(Blocks.field_150411_aY)'):
    req(t in orc,'original recipe '+t)
for t in ('new ResearchItem("ARCANESPA", "ALCHEMY"','Aspect.WATER, 3','Aspect.MECHANISM, 3','Aspect.ORDER, 3',
 '-6, -5, 1','recipes.get("ArcaneSpa")','setSecondary()','setParents(new String[] { "BATHSALTS" })'):
    req(t in ors,'original research '+t)

gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=197 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for m in ('arcaneSpaParityConstantsAndCandidateOrder','arcaneSpaFilledContainerUseConsumesWholePartialBucket',
 'arcaneSpaEmptyContainerOpensGuiWithoutDraining','arcaneSpaExactNbtAndLegacyPortMigration',
 'arcaneSpaMixingCycleAndExpansionOrder','arcaneSpaAutomationSidesAndRedstoneGate',
 'arcaneSpaFillModeRecipeResearchAndBlockPropertiesMatchOriginal'):
    req(m in methods,'GameTest '+m)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,64,23) and len(ids)>=622 and len(ids)==len(set(ids)),f'manifest {manifest["version"]}/{len(ids)}')
for sid in ('gametest.arcane_spa_filled_container_partial','gametest.arcane_spa_exact_nbt_migration',
 'gameplay.arcane_spa_consume_before_failed_placement','gameplay.arcane_spa_ultrawarm_water_rejection',
 'persistence.arcane_spa_root_nbt_chunk_restart','automation.arcane_spa_top_rejection_other_sides',
 'client.arcane_spa_gui_fluid_mask_model','dedicated.arcane_spa_multiplayer_container_toggle','jei.arcane_spa_exact_arcane_recipe'):
    req(sid in ids,'scenario '+sid)
ev=json.loads(text('tools/data/tc4_arcane_spa_full_source_evidence_v11.64.23.json'))
root_ev=json.loads(text('TC4_11.64.23_ARCANE_SPA_SOURCE_EVIDENCE.json'))
req(ev==root_ev,'evidence copies differ')
req(ev['round']=='11.64.23' and ev['source_closure']=='CLOSED' and ev['resource_closure']=='CLOSED','evidence closure')
req(ev['build_status']=='NOT_OBTAINED' and ev['runtime_status']=='NOT_VERIFIED','evidence honesty')
prompt=text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md')
req(prompt==text('PROMPT_FOR_FUTURE_CHAT_RU.md'),'prompt copies differ')
for t in ('Один релиз — один предмет или одна цельная механика','SOURCE CLOSED','RESOURCE CLOSED','BUILD VERIFIED','RUNTIME VERIFIED','Упаковка архива без этого файла запрещена'):
    req(t in prompt,'prompt token '+t)
report=text('TC4_11.64.23_ARCANE_SPA_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md')
for t in ('SOURCE CLOSED: YES','RESOURCE CLOSED: YES','BUILD VERIFIED: NO','RUNTIME VERIFIED: NO','Arcane Spa'):
    req(t in report,'report token '+t)
print(f'TC4 v11.64.23 Arcane Spa full-closure guard: PASS ({len(methods)} GameTests; {len(ids)} scenarios; source/resource/prompt)')
