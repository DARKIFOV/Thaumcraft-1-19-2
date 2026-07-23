#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re,zipfile
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.34 infusion altar full-closure guard: FAIL: '+msg)
def ver(p):
    m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',text(p)); req(m,'version '+p); return tuple(map(int,m.groups()))
def digest(b): return hashlib.sha256(b).digest()
req(ver('build.gradle')>=(11,64,34),'build version')
req(ver('src/main/resources/META-INF/mods.toml')>=(11,64,34),'mods version')
parity=text('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionAltarFullClosureParity.java')
for token in ('CONTRACT_VERSION = "11.64.34"','RESEARCH_KEY = "INFUSION"','PRIMAL_ASPECT_COUNT = 6','ALTAR_VIS_COST_PER_PRIMAL = 25','ALTAR_VIS_COST_PER_PRIMAL_CENTIVIS = 2500','ALTAR_ORIGIN_SCAN_MIN = -2','ALTAR_ORIGIN_SCAN_MAX = 0','ALTAR_BLUEPRINT_AIR_BLOCKS = 17','ORIGINAL_PILLAR_ORIENTATIONS = {2, 3, 4, 5}','MATRIX_LIGHT_LEVEL = 10','MATRIX_VALIDITY_IDLE_INTERVAL = 100','MATRIX_VALIDITY_CRAFTING_INTERVAL = 20','CRAFT_CYCLE_INTERVAL = 10','ENCHANTMENT_XP_CYCLE_INTERVAL = 20','COMPONENT_TRAVEL_CYCLES = 5','PEDESTAL_HORIZONTAL_RADIUS = 8','STABILIZER_HORIZONTAL_RADIUS = 12','PEDESTAL_ITEMS_NBT = "Items"','PEDESTAL_SLOT_NBT = "Slot"','PEDESTAL_CUSTOM_NAME_NBT = "CustomName"','MATRIX_RECIPE_INPUTS_NBT = "recipein"','MATRIX_RECIPE_OUTPUT_TYPE_NBT = "rotype"','MATRIX_RECIPE_OUTPUT_NBT = "recipeout"','LOWER_PILLAR_DROP = "thaumcraft:arcane_stone_bricks"','UPPER_PILLAR_DROP = "thaumcraft:arcane_stone"'):
    req(token in parity,'parity '+token)
mult=text('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionAltarMultiblock.java')
for token in ('PlayerThaumData.hasResearch(player, TC4InfusionAltarFullClosureParity.RESEARCH_KEY)','COST_PER_PRIMAL_CENTIVIS = TC4InfusionAltarFullClosureParity.ALTAR_VIS_COST_PER_PRIMAL_CENTIVIS','ALTAR_ORIGIN_SCAN_MIN','ALTAR_ORIGIN_SCAN_MAX','ThaumcraftMod.ARCANE_STONE.get()','ThaumcraftMod.ARCANE_STONE_BRICKS.get()','matrix.activateFromMultiblock()','setPillar(level, origin.offset(0, 0, 0), origin.offset(0, 1, 0), 2)','setPillar(level, origin.offset(2, 0, 2), origin.offset(2, 1, 2), 5)'):
    req(token in mult,'multiblock '+token)
req('InfusionAltarRitual' not in '\n'.join(text(p) for p in ('src/main/java/com/darkifov/thaumcraft/block/InfusionMatrixBlock.java','src/main/java/com/darkifov/thaumcraft/block/WandItem.java')),'duplicate activation path')
wand=text('src/main/java/com/darkifov/thaumcraft/block/WandItem.java')
req('TC4InfusionAltarMultiblock.tryCreate' in wand,'wand production binding')
block=text('src/main/java/com/darkifov/thaumcraft/block/InfusionMatrixBlock.java')
for token in ('MATRIX_LIGHT_LEVEL','matrix.onWandRightClick(player)','CRAFTING_BREAK_EXPLOSION_STRENGTH','Explosion.BlockInteraction.BREAK'):
    req(token in block or token in text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java'),'matrix block '+token)
pillar=text('src/main/java/com/darkifov/thaumcraft/block/InfusionPillarBlock.java')
for token in ('DoubleBlockHalf.LOWER','DoubleBlockHalf.UPPER','facingForOriginalOrientation','case 2 -> Direction.SOUTH','case 3 -> Direction.WEST','case 4 -> Direction.EAST','case 5 -> Direction.NORTH'):
    req(token in pillar,'pillar '+token)
pedblock=text('src/main/java/com/darkifov/thaumcraft/block/ArcanePedestalBlock.java')
for token in ('hand != InteractionHand.MAIN_HAND','held.shrink(1)','* 1.5F','* 1.6F','hasAnalogOutputSignal','pedestalComparator'):
    req(token in pedblock,'pedestal block '+token)
ped=text('src/main/java/com/darkifov/thaumcraft/blockentity/ArcanePedestalBlockEntity.java')
for token in ('getSlots()','return 1;','getSlotLimit','PEDESTAL_ITEMS_NBT','PEDESTAL_SLOT_NBT','PEDESTAL_CUSTOM_NAME_NBT','hasCustomName()','tag.contains("Stored"','ClientboundBlockEntityDataPacket.create(this)','ForgeCapabilities.ITEM_HANDLER'):
    req(token in ped,'pedestal BE '+token)
matrix=text('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java')
for token in ('countDelay = TC4InfusionAltarFullClosureParity.CRAFT_CYCLE_INTERVAL','tag.put("recipein", recipeInputs)','tag.putString("rotype"','tag.put("recipeout"','stackForComponentSpec','itemCount = TC4InfusionAltarFullClosureParity.COMPONENT_TRAVEL_CYCLES','MATRIX_VALIDITY_CRAFTING_INTERVAL','MATRIX_VALIDITY_IDLE_INTERVAL','shouldRunCraftCycle(count, countDelay)','checkSurroundings = true','sendOriginalBlockEvent(12)','countDelay = TC4InfusionAltarFullClosureParity.CRAFT_CYCLE_INTERVAL','itemCount = 0','Runtime counters,'):
    req(token in matrix,'matrix BE '+token)
runtime=text('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionRuntime.java')
req('return symmetry + recipeInstability;' in runtime,'raw starting instability')
req('return Math.min(MAX_INSTABILITY, value);' in runtime,'shortage upper cap only')
req('return current;' in runtime,'surroundings rescan does not rewrite running instability')
events=text('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionInstabilityEventTableParity.java')
req('roll0to499 <= instability' in events,'inclusive original instability gate')
req('progress++;' in matrix and 'auxiliary.speedMultiplier()' not in matrix,'no non-original craft accelerator')
structure=text('src/main/java/com/darkifov/thaumcraft/infusion/InfusionAltarStructure.java')
for token in ('PEDESTAL_TOP_OFFSET_FROM_MATRIX','PEDESTAL_BOTTOM_OFFSET_FROM_MATRIX','break; // TC4 skip=true','STABILIZER_TOP_OFFSET_FROM_MATRIX','STABILIZER_BOTTOM_OFFSET_FROM_MATRIX','countTc4Pillars'):
    req(token in structure,'structure '+token)
renderer=text('src/main/java/com/darkifov/thaumcraft/client/render/ArcanePedestalRenderer.java')
for token in ('PEDESTAL_ITEM_Y','PEDESTAL_BOB_DIVISOR','PEDESTAL_BOB_AMPLITUDE','PEDESTAL_BLOCK_SCALE','Minecraft.useFancyGraphics()','rotationDegrees(180.0F)'):
    req(token in renderer,'pedestal renderer '+token)
req('matrix.renderStartUp()' in text('src/main/java/com/darkifov/thaumcraft/client/render/InfusionMatrixRenderer.java'),'matrix renderer client state')
mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
req('INFUSION_PILLAR = internalBlock' in mod and 'new Item.Properties()));' in mod,'pillar hidden item')
loot=json.loads(text('src/main/resources/data/thaumcraft/loot_tables/blocks/infusion_pillar.json'))
loot_s=json.dumps(loot)
req('thaumcraft:arcane_stone_bricks' in loot_s and 'thaumcraft:arcane_stone' in loot_s and '"half": "lower"' in loot_s and '"half": "upper"' in loot_s,'pillar loot')
source_zip=R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip';prefix='Thaumcraft4-1.7.10-master/'
with zipfile.ZipFile(source_zip) as z:
    originals={
      'thaumcraft/common/items/wands/WandManager.java':('ResearchManager.isResearchComplete(player.func_70005_c_(), "INFUSION")','new AspectList().add(Aspect.FIRE, 25)','fitInfusionAltar(world, xx, yy, zz)','int[][][] blueprint = { { { 0, 0, 0 }, { 0, 9, 0 }','{ { 2, 0, 3 }, { 0, 0, 0 }, { 4, 0, 5 } }','tip.orientation = ((byte)blueprint[yy][xx][zz])'),
      'thaumcraft/common/tiles/TileInfusionMatrix.java':('private int countDelay = 10','this.count % (this.crafting ? 20 : 100) == 0','this.count % this.countDelay == 0','this.itemCount = 5','this.checkSurroundings = true','nextInt(500) <= this.instability','this.instability = (this.symmetry + this.recipeInstability)','for (int xx = -12; xx <= 12; xx++)','for (int yy = -5; yy <= 10; yy++)','int y = this.field_145848_d - yy','Math.abs(xx) <= 8'),
      'thaumcraft/common/tiles/TilePedestal.java':('func_150295_c("Items", 10)','func_74771_c("Slot")','func_74764_b("CustomName")'),
      'thaumcraft/common/blocks/BlockStoneDevice.java':('InventoryUtils.dropItemsAtEntity','* 1.5F','* 1.6F','func_72876_a(null, par2 + 0.5D','2.0F, true'),
      'thaumcraft/client/renderers/tile/TilePedestalRenderer.java':('1.15F + h','/ 16.0F) * 0.05F','ticks % 360.0F','GL11.glScalef(2.0F','GL11.glRotatef(180.0F'),
      'thaumcraft/client/renderers/tile/TileInfusionPillarRenderer.java':('textures/models/pillar.obj','textures/models/pillar.png','tile.orientation == 3','tile.orientation == 5')}
    for rel,tokens in originals.items():
        src=z.read(prefix+rel).decode('utf-8',errors='replace')
        for token in tokens: req(token in src,'original '+rel+' '+token)
    for orig,cur in (
      ('assets/thaumcraft/textures/models/pillar.png','src/main/resources/assets/thaumcraft/textures/models/pillar.png'),
      ('assets/thaumcraft/textures/models/infuser.png','src/main/resources/assets/thaumcraft/textures/models/infuser.png'),
      ('assets/thaumcraft/textures/blocks/pedestal_side.png','src/main/resources/assets/thaumcraft/textures/block/pedestal_side.png'),
      ('assets/thaumcraft/textures/blocks/pedestal_top.png','src/main/resources/assets/thaumcraft/textures/block/pedestal_top.png')):
        req(digest(z.read(prefix+orig))==digest((R/cur).read_bytes()),'resource hash '+orig)
    original_obj=z.read(prefix+'assets/thaumcraft/textures/models/pillar.obj').decode('utf-8',errors='replace').splitlines()
    current_obj=text('src/main/resources/assets/thaumcraft/models/block/tc4_infusion_pillar.obj').splitlines()
    current_obj=[line for line in current_obj if not line.startswith('mtllib ') and not line.startswith('usemtl ')]
    req(original_obj==current_obj,'pillar OBJ geometry')
gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=267 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for name in ('infusionAltarBlueprintAndVisCostMatchTc4','infusionAltarWandOriginSearchAndResearchMatchTc4','infusionPillarOrientationsAndDropsMatchTc4','infusionMatrixSchedulerMatchesTc4','infusionComponentTravelUsesFiveCraftCycles','infusionPedestalNbtAndComparatorUseProductionPath','infusionPedestalAndStabilizerScanBoundsMatchTc4','infusionMatrixLightAndCraftingBreakExplosionMatchTc4'):
    req(name in methods,'GameTest '+name)
man=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'));ids=[x['id'] for x in man['tests']]
req(tuple(map(int,man.get('version','0.0.0').split('.')))>=(11,64,34),'manifest version');req(len(ids)>=793 and len(ids)==len(set(ids)),f'manifest {len(ids)}')
for sid in ('gameplay.infusion_altar_raw_blueprint','gameplay.infusion_altar_research_gate','gameplay.infusion_altar_vis_atomic_cost','gameplay.infusion_pedestal_comparator','persistence.infusion_pedestal_items_slot_nbt','gameplay.infusion_matrix_scheduler','gameplay.infusion_component_five_cycles','gameplay.infusion_matrix_break_explosion','multiplayer.infusion_server_authority','persistence.infusion_full_cycle_reload'):
    req(sid in ids,'scenario '+sid)
for p in ('TC4_11.64.34_INFUSION_ALTAR_SOURCE_EVIDENCE.json','tools/data/tc4_infusion_altar_full_source_evidence_v11.64.34.json'):
    e=json.loads(text(p));req(e.get('version')=='11.64.34' and len(e.get('original_sources',[]))==13 and len(e.get('production_contracts',[]))==16,'evidence '+p)
prompt=R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md'
req(prompt.is_file() and prompt.read_bytes()==(R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes(),'mandatory prompt')
req('Файл `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен' in prompt.read_text(encoding='utf-8'),'prompt wording')
print('TC4 v11.64.34 infusion altar full-closure guard: PASS')
