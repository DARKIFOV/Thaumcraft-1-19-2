#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re,zipfile
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.29 Growth Lamp full-closure guard: FAIL: '+msg)
def version(p):
    m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',text(p));req(m,'version '+p);return tuple(map(int,m.groups()))
def sha(b): return hashlib.sha256(b).hexdigest()
req(version('build.gradle')>=(11,64,29),'build version')
req(version('src/main/resources/META-INF/mods.toml')>=(11,64,29),'mods version')
par=text('src/main/java/com/darkifov/thaumcraft/block/TC4GrowthLampParity.java')
for token in ('CONTRACT_VERSION = "11.64.29"','RESEARCH = "LAMPGROWTH"','RADIUS = 6','DIAMETER = RADIUS * 2 + 1','COLUMN_COUNT = DIAMETER * DIAMETER','CHARGES_PER_ESSENTIA = 100','DRAW_INTERVAL_TICKS = 5','SUCTION = 128','ACTIVE_LIGHT = 15','INACTIVE_LIGHT = 8','SPARKLE_COLOR = 4_259_648','SPARKLE_RANGE = 32','USES_SINGLE_NATURAL_TICK = true','dx * dx + dy * dy + dz * dz < RADIUS * RADIUS'):
    req(token in par,'parity '+token)
block=text('src/main/java/com/darkifov/thaumcraft/block/TC4EssentiaLampBlock.java')
for token in ('BooleanProperty ACTIVE','context.getClickedFace().getOpposite()','!level.getBlockState(pos.relative(state.getValue(FACING))).isAir()','changedSide == state.getValue(FACING) && changedState.isAir()','Block.box(4.0D, 2.0D, 4.0D, 12.0D, 14.0D, 12.0D)'):
    req(token in block,'block '+token)
req('isFaceSturdy' not in block,'sturdy-face invention')
be=text('src/main/java/com/darkifov/thaumcraft/blockentity/TC4EssentiaLampBlockEntity.java')
for token in ('charges = TC4GrowthLampParity.CHARGES_PER_ESSENTIA','if (!reserve && drawEssentia(Aspect.HERBA))','if (charges == 0)','if (++drawDelay % TC4GrowthLampParity.DRAW_INTERVAL_TICKS != 0','Aspect.HERBA','growthColumns.remove(0)','worldPosition.offset(dx, GROWTH_RADIUS, dz)','Collections.swap','TC4GrowthLampParity.insideSphere','state.randomTick(level, cursor, level.getRandom())','state.is(GROWTH_BLACKLIST)','CocoaBlock','NetherWartBlock','CactusBlock','SugarCaneBlock','ManaPodBlock.AGE','SPARKLE_COLOR','SPARKLE_RANGE','NBT_ORIENTATION','NBT_RESERVE','NBT_CHARGES'):
    req(token in be,'block entity '+token)
for forbidden in ('hasChunkAt','performBonemeal','tag.putInt("drawDelay"','tag.putInt("fertilityCounter"'):
    req(forbidden not in be,'non-original production path '+forbidden)
renderer=text('src/main/java/com/darkifov/thaumcraft/client/render/TC4EssentiaLampRenderer.java')
for token in ('textures/models/Bore.png','model.renderNozzle','ArcaneBoreBaseBlockEntity','facing.getOpposite()','case DOWN','case UP','case NORTH','case SOUTH','case WEST','case EAST'):
    req(token in renderer,'renderer '+token)
client=text('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
req('TC4EssentiaLampRenderer::new' in client,'renderer registration')
mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for token in ('TC4_LAMP_GROWTH = BLOCKS.register("tc4_block_lamp_growth"','TC4GrowthLampParity.BLOCK_HARDNESS','TC4GrowthLampParity.BLOCK_RESISTANCE','TC4GrowthLampParity.ACTIVE_LIGHT','TC4GrowthLampParity.INACTIVE_LIGHT','TC4EssentiaLampBlock.Kind.GROWTH'):
    req(token in mod,'registry '+token)
growth_item=re.search(r'TC4_LAMP_GROWTH_ITEM\s*=.*?;\n',mod,re.S)
req(growth_item and '.rarity(' not in growth_item.group(0),'Growth Lamp rarity must be common')
recipe=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_infusion/tc4_lamp_growth.json'))
req(recipe['research']=='LAMPGROWTH' and recipe['instability']==4,'recipe header')
req(recipe['catalyst']=='thaumcraft:tc4_block_arcane_lamp','recipe catalyst')
req(recipe['components']==['minecraft:gold_ingot','minecraft:black_dye','thaumcraft:terra_shard']*2,'recipe components')
req(recipe['aspects']=={'HERBA':16,'LUX':8,'VICTUS':16},'recipe aspects')
req(recipe['result']=={'item':'thaumcraft:tc4_block_lamp_growth','count':1},'recipe result')
bs=json.loads(text('src/main/resources/assets/thaumcraft/blockstates/tc4_block_lamp_growth.json'))
req(len(bs.get('variants',{}))==12,'12 active/facing variants')
for key,value in bs['variants'].items():
    req('x' not in value and 'y' not in value,'body model must not rotate '+key)
    req(value['model'].endswith('_off') == key.startswith('active=false'),'active/off model mapping '+key)
for p in ('src/main/resources/assets/thaumcraft/models/block/tc4_block_lamp_growth.json','src/main/resources/assets/thaumcraft/models/block/tc4_block_lamp_growth_off.json'):
    model=json.loads(text(p)); req('elements' in model and len(model['elements'])==1,'body-only model '+p)
source_zip=R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip'
with zipfile.ZipFile(source_zip) as z:
    prefix='Thaumcraft4-1.7.10-master/'
    originals={
      'thaumcraft/common/tiles/TileArcaneLampGrowth.java':('private boolean reserve = false','public int charges = -1','this.charges = 100','this.reserve = true','this.charges = -1','int distance = 6','Collections.shuffle','this.charges -= 1','func_147464_a','"orientation"','"reserve"','"charges"','++this.drawDelay % 5','Aspect.PLANT','? 128 : 0','4259648','32.0D'),
      'thaumcraft/common/config/ConfigRecipes.java':('ConfigResearch.recipes.put("LampGrowth"','addInfusionCraftingRecipe("LAMPGROWTH"','Aspect.PLANT, 16','Aspect.LIGHT, 8','Aspect.LIFE, 16'),
      'thaumcraft/common/config/ConfigResearch.java':('new ResearchItem("LAMPGROWTH", "ARTIFICE"','Aspect.LIGHT, 3','Aspect.PLANT, 6','Aspect.LIFE, 3','Aspect.CROP, 3','-4, 3, 2','.setHidden()','.setAspectTriggers','.setParents(new String[] { "ARCANELAMP", "INFUSION" })'),
      'thaumcraft/common/config/Config.java':('addStandardCrop(new ItemStack(Blocks.field_150440_ba), 32767)','addStandardCrop(new ItemStack(Blocks.field_150423_aK), 32767)','addStandardCrop(new ItemStack(ConfigBlocks.blockManaPod), 7)','addStackedCrop(Blocks.field_150436_aH, 32767)','addStackedCrop(Blocks.field_150434_aF, 32767)'),
      'thaumcraft/common/lib/utils/CropUtils.java':('isGrownCrop','standardCrops','stackedCrops','lampBlacklist','doesLampGrow')}
    for rel,tokens in originals.items():
        s=z.read(prefix+rel).decode('utf-8',errors='replace')
        for token in tokens:req(token in s,'original '+rel+' '+token)
    resources=['lamp_grow_side.png','lamp_grow_side.png.mcmeta','lamp_grow_side_off.png','lamp_grow_top.png','lamp_grow_top.png.mcmeta','lamp_grow_top_off.png']
    for name in resources:
        cur=R/'src/main/resources/assets/thaumcraft/textures/block/tc4'/name
        req(cur.is_file(),'missing resource '+name)
        req(sha(cur.read_bytes())==sha(z.read(prefix+'assets/thaumcraft/textures/blocks/'+name)),'resource hash '+name)
    cur=R/'src/main/resources/assets/thaumcraft/textures/models/Bore.png'
    req(sha(cur.read_bytes())==sha(z.read(prefix+'assets/thaumcraft/textures/models/Bore.png')),'Bore texture hash')
src=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',src,re.S)
req(len(methods)>=240 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for name in ('growthLampChargeReserveAndSuctionMatchOriginal','growthLampAcceptsAnyNonAirSupportAndUsesDynamicLight','growthLampBlockEntityAndOriginalNbtRoundTrip','growthLampColumnScanAndSphereMatchOriginal','growthLampUsesSingleNaturalTickNotGuaranteedBonemeal','growthLampRecipeResearchAndRarityMatchOriginal','growthLampRendererUsesOriginalBoreNozzleAndOffTextures'):
    req(name in methods,'GameTest '+name)
man=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'));ids=[x['id'] for x in man['tests']]
req(tuple(map(int,man['version'].split('.'))) >= (11,64,29),'manifest version')
req(len(ids)>=720 and len(ids)==len(set(ids)),f'manifest {len(ids)}')
for sid in ('gameplay.growth_lamp_non_air_support','gameplay.growth_lamp_herba_direct_input','gameplay.growth_lamp_primary_charge_and_reserve','gameplay.growth_lamp_13x13_column_shuffle','gameplay.growth_lamp_single_natural_tick','gameplay.growth_lamp_blacklist_tag','visual.growth_lamp_nozzle_six_faces','persistence.growth_lamp_original_nbt','regression.growth_lamp_fertility_scope_isolation'):
    req(sid in ids,'scenario '+sid)
prompt=(R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md').read_bytes();prompt2=(R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes()
req(prompt==prompt2,'mandatory prompt copies differ')
print('TC4 v11.64.29 Growth Lamp full-closure guard: PASS')
