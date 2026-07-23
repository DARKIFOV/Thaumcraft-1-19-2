#!/usr/bin/env python3
"""v11.64.24 guard: complete Arcane Bore/Base source and resource closure."""
from pathlib import Path
import hashlib,json,re,zipfile
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.24 Arcane Bore full-closure guard: FAIL: '+msg)
def sha(p): return hashlib.sha256((R/p).read_bytes()).hexdigest()
def sha_bytes(b): return hashlib.sha256(b).hexdigest()
def ver(p):
    s=text(p); m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',s)
    req(m,'version parse '+p); return tuple(map(int,m.groups()))
req(ver('build.gradle')>=(11,64,24),'build version')
req(ver('src/main/resources/META-INF/mods.toml')==ver('build.gradle'),'mods version')

par=text('src/main/java/com/darkifov/thaumcraft/blockentity/TC4ArcaneBoreParity.java')
for t in ('CONTRACT_VERSION = "11.64.24"','BASE_RADIUS = 2','MAX_DEPTH = 64','SPIRAL_STEP_DEGREES = 2',
          'VIS_REQUEST_CENTIVIS = 100','VIS_TO_SPEED_DIVISOR = 5.0F','ESSENTIA_SPEED_CREDIT = 20.0F',
          'PICKAXE_REPAIR_INTERVAL = 40','REPAIR_VIS_DRAIN_INTERVAL = 5','NBT_ORIENTATION = "orientation"',
          'NBT_BASE_ORIENTATION = "baseOrientation"','NBT_INVENTORY = "Inventory"','NBT_SLOT = "Slot"',
          'NBT_SPEEDY_TIME = "SpeedyTime"','LEGACY_PORT_NBT_SPIRAL = "SpiralIndex"',
          'width(','pickaxeIsNearBroken','miningDelay','addVisCredit','addEssentiaCredit','consumeAcceleratedBlock',
          'initialRadiusIncrement','record SpiralLane','nextLane','Math.toRadians','directionX','directionY'):
    req(t in par,'parity token '+t)

be=text('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneBoreBlockEntity.java')
for t in ('FakeThaumcraftBore','new ItemStackHandler(2)','getCount() { return 8; }','refillAcceleration(server, base)',
          'level.hasNeighborSignal(pos) || level.hasNeighborSignal(basePos)','tickPickaxe(server)',
          'AuraVisRelayNetwork.drainMachineVis(server, worldPosition, Aspect.PERDITIO','base.tryDrawPerditio()',
          'repairCounter++','PICKAXE_REPAIR_INTERVAL','REPAIR_VIS_DRAIN_INTERVAL','fake.tickCount = (int) repairCounter',
          'pickaxe.inventoryTick(server, fake, 0, true)','isExcavationFocus','pick.getItem() instanceof PickaxeItem',
          'TC4ArcaneBoreParity.pickaxeIsNearBroken','FocusUpgradeType.ENLARGE','FocusUpgradeType.POTENCY',
          'Enchantments.BLOCK_EFFICIENCY','FocusUpgradeType.TREASURE','Enchantments.BLOCK_FORTUNE',
          'FocusUpgradeType.SILK_TOUCH','Enchantments.SILK_TOUCH','ElementalPickaxeItem','FocusUpgradeType.DOWSING',
          'TC4ArcaneBoreParity.nextLane','for (int depth = 0; depth < TC4ArcaneBoreParity.MAX_DEPTH',
          'Block.getDrops','new AABB(target).inflate(1.0D)','WandFocusRuntime.applyDowsing',
          'ItemHandlerHelper.insertItemStacked','entity.setDeltaMovement','server.removeBlock(target, false)',
          'seedTunnelLightFromAdjacentLamp','tool.hurt(1, server.getRandom(), fakePlayer(server))',
          'tag.putInt(TC4ArcaneBoreParity.NBT_ORIENTATION','tag.putInt(TC4ArcaneBoreParity.NBT_BASE_ORIENTATION',
          'ListTag items = new ListTag()','item.putByte(TC4ArcaneBoreParity.NBT_SLOT','tag.putShort(TC4ArcaneBoreParity.NBT_SPEEDY_TIME',
          'inventory.deserializeNBT','SpiralIndex from older ports is intentionally not written again',
          'BlockParticleOption','clientTopRotation','CLIENT_TARGET','getUpdatePacket','invalidateCaps','reviveCaps'):
    req(t in be,'bore entity token '+t)
req(be.index('bore.refillAcceleration(server, base)') < be.index('boolean validBase'),'acceleration must run before conditions')
req(be.index('bore.mineTarget(basePos)') < be.index('bore.tickPickaxe(server)'),'repair path must follow dig path')
req('tag.putInt("SpiralIndex"' not in be,'SpiralIndex leaked into new save')

basebe=text('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneBoreBaseBlockEntity.java')
for t in ('SUCTION = 128','return true;','face == outputFace() ? 0 : SUCTION','return Aspect.PERDITIO',
          'for (Direction direction : Direction.values())','if (!tube.canOutputTo(tubeFace)) return false',
          'tube.getSuctionAmount(tubeFace) < suctionAmount(direction)','takeEssentiaOriginal(Aspect.PERDITIO, 1',
          'NBT_ORIENTATION','pendingOrientation','onLoad()'):
    req(t in basebe,'base entity token '+t)

block=text('src/main/java/com/darkifov/thaumcraft/block/ArcaneBoreBlock.java')
for t in ('BlockStateProperties.FACING','BooleanProperty INVERTED','supportFace.getAxis() != Direction.Axis.Y',
          'ARCANE_BORE_BASE','context.getNearestLookingDirection().getOpposite()','basePos','canSurvive',
          'isFaceSturdy','Shapes.box','direction.getStepX() < 0 ? -1.0D','direction.getStepX() > 0 ? 2.0D',
          'player.getItemInHand(hand).getItem() instanceof WandItem','state.setValue(FACING, hit.getDirection())',
          'TC4Sounds.event("tool")','NetworkHooks.openScreen','Containers.dropItemStack'):
    req(t in block,'bore block token '+t)
baseblock=text('src/main/java/com/darkifov/thaumcraft/block/ArcaneBoreBaseBlock.java')
for t in ('BlockStateProperties.FACING','context.getHorizontalDirection().getOpposite()',
          'state.setValue(FACING, hit.getDirection())','TC4Sounds.event("tool")','0.3F','1.9F'):
    req(t in baseblock,'base block token '+t)

menu=text('src/main/java/com/darkifov/thaumcraft/menu/ArcaneBoreMenu.java')
for t in ('new SimpleContainerData(8)','checkContainerDataCount(data, 8)','0, 26, 18','1, 74, 18',
          'TC4ArcaneBoreParity.width','nativeClusters()','pickaxeNearBroken()','return bore.stillValid(player)'):
    req(t in menu,'menu token '+t)
screen=text('src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneBoreScreen.java')
for t in ('imageWidth = 176','imageHeight = 141','leftPos + 74, topPos + 18, 184, 0, 16, 16',
          'poseStack.translate(112.0D, 8.0D, 505.0D)','poseStack.scale(0.5F, 0.5F, 1.0F)',
          '"Width: "','"Speed: +"','"Other properties:"','"Native Clusters"','0xC0C0C0','0xEEC64A','0x8080FF'):
    req(t in screen,'GUI token '+t)
renderer=text('src/main/java/com/darkifov/thaumcraft/client/render/ArcaneBoreRenderer.java')
for t in ('renderBoreMount','renderBoreNozzle','renderEmitter','hasFocusForRender','textures/misc/vortex.png',
          'renderVortex','textures/models/jar.png','TC4ArcaneBoreCoreModel','renderDigBeams','0x00FF66','0xFF88D5',
          'BlockEntityRenderer<ArcaneBoreBlockEntity>','shouldRenderOffScreen'):
    req(t in renderer,'renderer token '+t)
baserender=text('src/main/java/com/darkifov/thaumcraft/client/render/ArcaneBoreBaseRenderer.java')
for t in ('renderBaseBody','model.renderNozzle','switch (facing)','case NORTH -> 90','case SOUTH -> 270','case WEST -> 180'):
    req(t in baserender,'base renderer token '+t)
model=text('src/main/java/com/darkifov/thaumcraft/client/render/model/TC4ArcaneBoreModel.java')
for t in ('addBox(-6, 0, -6, 12, 2, 12)','addBox(-2, 2, -5.5F, 4, 8, 1)',
          'addBox(4, -2.5F, -2.5F, 4, 5, 5)','addBox(-2, -4, -4, 6, 8, 8)',
          'addBox(-1, 1, -1, 2, 11, 2)','addBox(-2, 12, -2, 4, 4, 4)',
          'renderBoreMount','renderBoreNozzle','renderEmitter','renderBaseBody','renderNozzle'):
    req(t in model,'model token '+t)
core=text('src/main/java/com/darkifov/thaumcraft/client/render/model/TC4ArcaneBoreCoreModel.java')
req('addBox(-5.0F, -12.0F, -5.0F, 10.0F, 12.0F, 10.0F)' in core,'jar core geometry')

mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for t in ('ARCANE_BORE_BASE = BLOCKS.register("tc4_block_arcane_bore_base"','Material.WOOD',
          '.strength(2.5F, 10.0F).sound(SoundType.WOOD).noOcclusion()',
          'ARCANE_BORE = BLOCKS.register("tc4_block_arcane_bore"','ARCANE_BORE_BASE_BLOCK_ENTITY',
          'ARCANE_BORE_BLOCK_ENTITY','ARCANE_BORE_MENU'):
    req(t in mod,'registration '+t)
for name in ('ARCANE_BORE_BASE_ITEM','ARCANE_BORE_ITEM'):
    m=re.search(name+r'\s*=.*?;\n',mod,re.S); req(m and '.rarity(' not in m.group(0),name+' must be common')

recipe=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcaneborebase.json'))
req(recipe['pattern']==['WIW','IDI','WIW'],'base recipe pattern')
req(recipe['key']=={'W':'thaumcraft:greatwood_planks','I':'minecraft:iron_ingot','D':'minecraft:oak_log'},'base recipe keys')
req(recipe['aspects']=={'AER':10,'ORDO':10},'base recipe aspects')
inf=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_infusion/tc4_arcane_bore.json'))
req(inf['instability']==4 and inf['research']=='ARCANEBORE','infusion identity')

with zipfile.ZipFile(R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    names=z.namelist()
    def original(suffix):
        n=next((x for x in names if x.endswith('/'+suffix)),None); req(n,'missing original '+suffix); return z.read(n).decode(errors='replace')
    ob=original('thaumcraft/common/tiles/TileArcaneBore.java')
    obase=original('thaumcraft/common/tiles/TileArcaneBoreBase.java')
    og=original('thaumcraft/client/gui/GuiArcaneBore.java')
    obr=original('thaumcraft/client/renderers/tile/TileArcaneBoreRenderer.java')
    om=original('thaumcraft/client/renderers/models/ModelBore.java')
    oe=original('thaumcraft/client/renderers/models/ModelBoreEmit.java')
    omb=original('thaumcraft/client/renderers/models/ModelBoreBase.java')
    orc=original('thaumcraft/common/config/ConfigRecipes.java')
    ors=original('thaumcraft/common/config/ConfigResearch.java')
    for t in ('Aspect.ENTROPY, 100) / 5.0F','this.speedyTime += 20.0F','FakeThaumcraftBore',
              'repairCounter++ % 40L','repairCounter % 5L','this.spiral += 2','for (int depth = 0; depth < 64; depth++)',
              'Math.max(10 - this.speed','this.count *= 4','this.speedyTime -= 1.0F','"orientation"','"baseOrientation"',
              '"Inventory"','"Slot"','"SpeedyTime"'):
        req(t in ob,'original bore '+t)
    for t in ('return true','Aspect.ENTROPY','return face != this.orientation ? 128 : 0;','drawEssentia','ForgeDirection.VALID_DIRECTIONS'):
        req(t in obase,'original base '+t)
    for t in ('field_146999_f = 176','field_147000_g = 141','var5 + 74, var6 + 18, 184, 0, 16, 16',
              '"Width: "','"Speed: +"','"Native Clusters"','"Fortune "','"Silk Touch"'):
        req(t in og,'original GUI '+t)
    for t in ('textures/misc/vortex.png','0.4F','0.3F','0.2F','textures/models/jar.png','this.modelJar.Core'):
        req(t in obr,'original renderer '+t)
    for t in ('renderBase()','renderNozzle()'):
        req(t in om,'original ModelBore '+t)
    req('if (focus) this.Knob' in oe,'original focus knob')
    req('renderNozzle()' in omb,'original base nozzle')
    for cur,suffix in [
      ('src/main/resources/assets/thaumcraft/textures/models/Bore.png','assets/thaumcraft/textures/models/Bore.png'),
      ('src/main/resources/assets/thaumcraft/textures/gui/gui_arcanebore.png','assets/thaumcraft/textures/gui/gui_arcanebore.png'),
      ('src/main/resources/assets/thaumcraft/textures/misc/vortex.png','assets/thaumcraft/textures/misc/vortex.png'),
      ('src/main/resources/assets/thaumcraft/textures/models/jar.png','assets/thaumcraft/textures/models/jar.png')]:
        n=next(x for x in names if x.endswith('/'+suffix)); req(sha(cur)==sha_bytes(z.read(n)),'texture hash '+cur)
    for t in ('ConfigResearch.recipes.put("ArcaneBoreBase"','"WIW", "IDI", "WIW"','Aspect.AIR, 10','Aspect.ORDER, 10',
              'ConfigResearch.recipes.put("ArcaneBore"','new ItemStack(Blocks.field_150343_Z)','new ItemStack(Items.field_151043_k)'):
        req(t in orc,'original recipe '+t)
    req('new ResearchItem("ARCANEBORE"' in ors,'original research')

gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=204 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for m in ('arcaneBoreParityFormulasMatchOriginal','arcaneBoreSpiralUsesOriginalRotatedLaneContract',
          'arcaneBoreRequiresVerticalBaseAndExtendsTowardNozzle','arcaneBoreSavesOriginalInventoryListAndOrientations',
          'arcaneBoreBaseRetainsAllFacePerditioSuction','arcaneBoreMenuAndItemsRetainOriginalContracts',
          'arcaneBoreResearchAndRecipesMatchOriginal'):
    req(m in methods,'GameTest '+m)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,64,24) and len(ids)>=638 and len(ids)==len(set(ids)),f'manifest {manifest["version"]}/{len(ids)}')
for sid in ('gametest.arcane_bore_parity_formulas','gametest.arcane_bore_exact_nbt',
            'gameplay.arcane_bore_vis_then_essentia_acceleration','gameplay.arcane_bore_pickaxe_repair_unpowered',
            'gameplay.arcane_bore_dowsing_native_clusters','persistence.arcane_bore_original_nbt_restart',
            'automation.arcane_bore_base_all_faces_nozzle_zero','client.arcane_bore_model_beams_vortex_gui',
            'dedicated.arcane_bore_multiplayer_persistence'):
    req(sid in ids,'scenario '+sid)
ev=json.loads(text('tools/data/tc4_arcane_bore_full_source_evidence_v11.64.24.json'))
req(ev==json.loads(text('TC4_11.64.24_ARCANE_BORE_SOURCE_EVIDENCE.json')),'evidence copies differ')
req(ev['source_closure']=='CLOSED' and ev['resource_closure']=='CLOSED','evidence closure')
req(ev['build_status']=='NOT_OBTAINED' and ev['runtime_status']=='NOT_VERIFIED','evidence honesty')
prompt=text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md')
req(prompt==text('PROMPT_FOR_FUTURE_CHAT_RU.md'),'prompt copies differ')
for t in ('Один релиз — один предмет или одна цельная механика','SOURCE CLOSED','RESOURCE CLOSED','BUILD VERIFIED','RUNTIME VERIFIED','Упаковка архива без этого файла запрещена'):
    req(t in prompt,'prompt token '+t)
report=text('TC4_11.64.24_ARCANE_BORE_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md')
for t in ('SOURCE CLOSED: YES','RESOURCE CLOSED: YES','BUILD VERIFIED: NO','RUNTIME VERIFIED: NO','Arcane Bore'):
    req(t in report,'report token '+t)
log=text('TC4_11.64.24_GRADLE_BUILD_ATTEMPT.log')
req('UnknownHostException: services.gradle.org' in log and 'BUILD SUCCESSFUL' not in log,'Gradle evidence')
print(f'TC4 v11.64.24 Arcane Bore full-closure guard: PASS ({len(methods)} GameTests; {len(ids)} scenarios; source/resource/prompt)')
