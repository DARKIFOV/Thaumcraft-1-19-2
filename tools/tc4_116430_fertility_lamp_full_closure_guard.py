#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re,zipfile
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.30 Fertility Lamp full-closure guard: FAIL: '+msg)
def version(p):
    m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',text(p));req(m,'version '+p);return tuple(map(int,m.groups()))
def sha(b): return hashlib.sha256(b).hexdigest()
req(version('build.gradle')>=(11,64,30),'build version')
req(version('src/main/resources/META-INF/mods.toml')>=(11,64,30),'mods version')
par=text('src/main/java/com/darkifov/thaumcraft/block/TC4FertilityLampParity.java')
for token in ('CONTRACT_VERSION = "11.64.30"','RESEARCH = "LAMPFERTILITY"','RADIUS = 7','MAX_CHARGES = 4','BREEDING_COST = 2','BREEDING_INTERVAL_TICKS = 300','DRAW_INTERVAL_TICKS = 5','SUCTION_BASE = 128','SUCTION_PER_CHARGE = 10','MAX_EXISTING_SAME_CLASS = 7','ACTIVE_LIGHT = 15','INACTIVE_LIGHT = 8','exactClassCount <= MAX_EXISTING_SAME_CLASS','age == 0 && !inLove'):
    req(token in par,'parity '+token)
block=text('src/main/java/com/darkifov/thaumcraft/block/TC4EssentiaLampBlock.java')
for token in ('BooleanProperty ACTIVE','context.getClickedFace().getOpposite()','!level.getBlockState(pos.relative(state.getValue(FACING))).isAir()','changedSide == state.getValue(FACING) && changedState.isAir()','Block.box(4.0D, 2.0D, 4.0D, 12.0D, 14.0D, 12.0D)'):
    req(token in block,'block '+token)
req('isFaceSturdy' not in block,'sturdy-face invention')
be=text('src/main/java/com/darkifov/thaumcraft/blockentity/TC4EssentiaLampBlockEntity.java')
for token in ('TC4FertilityLampParity.canDraw(charges)','drawEssentia(Aspect.VICTUS)','TC4FertilityLampParity.canBreed(charges)','TC4FertilityLampParity.isBreedingTick(fertilityCounter++)','new AABB(worldPosition).inflate(TC4FertilityLampParity.RADIUS)','animal.getClass().equals(first.getClass())','TC4FertilityLampParity.populationAllowed','TC4FertilityLampParity.eligibleAnimal','charges -= TC4FertilityLampParity.BREEDING_COST','candidate.setInLove(null)','partner.setInLove(null)','neighbour instanceof EssentiaJarBlockEntity jar','sideFromNeighbour == Direction.UP','jar.takeFromContainerOriginal(aspect, 1)','NBT_ORIENTATION','NBT_CHARGES'):
    req(token in be,'block entity '+token)
for forbidden in ('tag.putBoolean(TC4GrowthLampParity.NBT_RESERVE, reserve);\n        }\n        tag.putInt(TC4GrowthLampParity.NBT_CHARGES, charges);\n        tag.putInt("fertilityCounter"','tag.putInt("drawDelay"','tag.putInt("fertilityCounter"'):
    req(forbidden not in be,'non-original fertility NBT '+forbidden)
renderer=text('src/main/java/com/darkifov/thaumcraft/client/render/TC4EssentiaLampRenderer.java')
for token in ('textures/models/Bore.png','model.renderNozzle','ArcaneBoreBaseBlockEntity','facing.getOpposite()','case DOWN','case UP','case NORTH','case SOUTH','case WEST','case EAST'):
    req(token in renderer,'renderer '+token)
mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for token in ('TC4_LAMP_FERTILITY = BLOCKS.register("tc4_block_lamp_fertility"','TC4FertilityLampParity.BLOCK_HARDNESS','TC4FertilityLampParity.BLOCK_RESISTANCE','TC4FertilityLampParity.ACTIVE_LIGHT','TC4FertilityLampParity.INACTIVE_LIGHT','TC4EssentiaLampBlock.Kind.FERTILITY'):
    req(token in mod,'registry '+token)
item=re.search(r'TC4_LAMP_FERTILITY_ITEM\s*=.*?;\n',mod,re.S)
req(item and '.rarity(' not in item.group(0),'Fertility Lamp rarity must be common')
recipe=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_infusion/tc4_lamp_fertility.json'))
req(recipe['research']=='LAMPFERTILITY' and recipe['instability']==4,'recipe header')
req(recipe['catalyst']=='thaumcraft:tc4_block_arcane_lamp','recipe catalyst')
req(recipe['components']==['minecraft:gold_ingot','minecraft:blaze_rod','thaumcraft:ignis_shard','minecraft:gold_ingot','minecraft:brewing_stand','thaumcraft:ignis_shard'],'recipe components')
req(recipe['aspects']=={'BESTIA':16,'VICTUS':16,'LUX':8},'recipe aspects')
req(recipe['result']=={'item':'thaumcraft:tc4_block_lamp_fertility','count':1},'recipe result')
bs=json.loads(text('src/main/resources/assets/thaumcraft/blockstates/tc4_block_lamp_fertility.json'))
req(len(bs.get('variants',{}))==12,'12 active/facing variants')
for key,value in bs['variants'].items():
    req('x' not in value and 'y' not in value,'body model must not rotate '+key)
    req(value['model'].endswith('_off') == key.startswith('active=false'),'active/off mapping '+key)
for p in ('src/main/resources/assets/thaumcraft/models/block/tc4_block_lamp_fertility.json','src/main/resources/assets/thaumcraft/models/block/tc4_block_lamp_fertility_off.json'):
    model=json.loads(text(p));req('elements' in model and len(model['elements'])==1,'body-only model '+p)
source_zip=R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip'
with zipfile.ZipFile(source_zip) as z:
    prefix='Thaumcraft4-1.7.10-master/'
    originals={
      'thaumcraft/common/tiles/TileArcaneLampFertility.java':('public int charges = 0','this.charges < 4','this.count++ % 300 == 0','int distance = 7','var7.getClass().equals(var4.getClass())','sa.size() <= 7','this.charges -= 2','func_146082_f(null)','"orientation"','"charges"','++this.drawDelay % 5','Aspect.LIFE','128 - this.charges * 10'),
      'thaumcraft/common/blocks/BlockMetalDevice.java':('metadata == 13','TileArcaneLampFertility','return 15','return 8','this.icon[18]','this.icon[19]','this.icon[20]','this.icon[21]'),
      'thaumcraft/common/config/ConfigRecipes.java':('ConfigResearch.recipes.put("LampFertility"','addInfusionCraftingRecipe("LAMPFERTILITY"','Aspect.BEAST, 16','Aspect.LIFE, 16','Aspect.LIGHT, 8'),
      'thaumcraft/common/config/ConfigResearch.java':('new ResearchItem("LAMPFERTILITY", "ARTIFICE"','Aspect.BEAST, 6','Aspect.LIFE, 6','Aspect.LIGHT, 3','-2, 3, 2','.setHidden()','.setAspectTriggers(new Aspect[] { Aspect.LIGHT, Aspect.LIFE })','.setParents(new String[] { "ARCANELAMP", "INFUSION" })'),
      'thaumcraft/common/tiles/TileJarFillable.java':('return face == ForgeDirection.UP','takeEssentia','canOutputTo(face)')}
    for rel,tokens in originals.items():
        s=z.read(prefix+rel).decode('utf-8',errors='replace')
        for token in tokens:req(token in s,'original '+rel+' '+token)
    resources=['lamp_fert_side.png','lamp_fert_side.png.mcmeta','lamp_fert_side_off.png','lamp_fert_top.png','lamp_fert_top.png.mcmeta','lamp_fert_top_off.png']
    for name in resources:
        cur=R/'src/main/resources/assets/thaumcraft/textures/block/tc4'/name
        req(cur.is_file(),'missing resource '+name)
        req(sha(cur.read_bytes())==sha(z.read(prefix+'assets/thaumcraft/textures/blocks/'+name)),'resource hash '+name)
    cur=R/'src/main/resources/assets/thaumcraft/textures/models/Bore.png'
    req(sha(cur.read_bytes())==sha(z.read(prefix+'assets/thaumcraft/textures/models/Bore.png')),'Bore texture hash')
src=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',src,re.S)
req(len(methods)>=247 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for name in ('fertilityLampChargeSuctionAndCadenceMatchOriginal','fertilityLampAcceptsAnyNonAirSupportAndUsesDynamicLight','fertilityLampOriginalNbtAndTransientCountersMatch','fertilityLampDirectJarVictusInputMatchesOriginal','fertilityLampExactClassPopulationAndBreedingMatchOriginal','fertilityLampRecipeResearchAndRarityMatchOriginal','fertilityLampRendererAndOffTexturesMatchOriginal'):
    req(name in methods,'GameTest '+name)
man=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'));ids=[x['id'] for x in man['tests']]
req(tuple(map(int,man['version'].split('.'))) >= (11,64,30),'manifest version')
req(len(ids)>=736 and len(ids)==len(set(ids)),f'manifest {len(ids)}')
for sid in ('gameplay.fertility_lamp_victus_direct_input','gameplay.fertility_lamp_charge_capacity','gameplay.fertility_lamp_first_cycle_immediate','gameplay.fertility_lamp_exact_class_pairing','gameplay.fertility_lamp_population_cap','gameplay.fertility_lamp_charge_cost','visual.fertility_lamp_dynamic_light','persistence.fertility_lamp_original_nbt','regression.fertility_growth_lamp_shared_transport'):
    req(sid in ids,'scenario '+sid)
req((R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md').read_bytes()==(R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes(),'mandatory prompt copies differ')
print('TC4 v11.64.30 Fertility Lamp full-closure guard: PASS')
