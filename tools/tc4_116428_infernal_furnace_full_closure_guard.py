#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re,zipfile
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.28 Infernal Furnace full-closure guard: FAIL: '+msg)
def version(p):
    m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',text(p));req(m,'version '+p);return tuple(map(int,m.groups()))
def sha(b):return hashlib.sha256(b).hexdigest()
req(version('build.gradle')>=(11,64,28),'build version')
req(version('src/main/resources/META-INF/mods.toml')>=(11,64,28),'mods version')
par=text('src/main/java/com/darkifov/thaumcraft/block/TC4InfernalFurnaceParity.java')
for token in ('INVENTORY_SIZE = 32','IGNIS_VIS_REQUEST_CENTIVIS = 5','IGNIS_ESSENTIA_SPEED_TICKS = 600','NOZZLE_DRAW_INTERVAL_TICKS = 5','NOZZLE_DRAW_ADMISSION_SPEED = 60','NOZZLE_SUCTION_ADMISSION_SPEED = 40','NOZZLE_SUCTION = 128','NORMAL_COOK_TIME = 140','SPEEDY_COOK_TIME = 80','COOK_REDUCTION_PER_BELLOWS = 20','MAX_BELLOWS = 3','BONUS_CHANCE_WITHOUT_BELLOWS = 0.25F','BONUS_CHANCE_PER_BELLOWS = 0.44F','FORMATION_IGNIS_CENTIVIS = 5000','FORMATION_TERRA_CENTIVIS = 5000','RESEARCH = "INFERNALFURNACE"'):
    req(token in par,'parity '+token)
mult=text('src/main/java/com/darkifov/thaumcraft/block/InfernalFurnaceMultiblock.java')
for token in ('PlayerThaumData.hasResearch','Blocks.OBSIDIAN','Blocks.NETHER_BRICKS','Blocks.NETHER_BRICK_FENCE','Blocks.LAVA','WandItem.modifiedVisCost','InfernalFurnaceBlock.PART','InfernalFurnaceBlock.LAYER','InfernalFurnaceBlock.FACING','spawnPunishmentBlaze'):
    req(token in mult,'multiblock '+token)
block=text('src/main/java/com/darkifov/thaumcraft/block/InfernalFurnaceBlock.java')
for token in ('CORE_SHAPE','ITEM_BOUNCE_Y','DamageSource.LAVA','LIVING_DAMAGE','LIVING_FIRE_SECONDS','structureIntact','MobEffects.REGENERATION','MobEffects.DAMAGE_RESISTANCE','ParticleTypes.LARGE_SMOKE'):
    req(token in block,'block '+token)
be=text('src/main/java/com/darkifov/thaumcraft/blockentity/InfernalFurnaceBlockEntity.java')
for token in ('AuraVisRelayNetwork.drainMachineVis','RecipeType.SMELTING','BellowsBlock.isActiveBellows','worldPosition.relative(direction, 2)','smeltingBonus','ExperienceOrb.getExperienceValue','server.blockEvent','NBT_COOK_TIME','NBT_SPEEDY_TIME','NBT_ITEMS','NBT_SLOT'):
    req(token in be,'block entity '+token)
for item in ('tc4_clustergold','tc4_clusteriron','tc4_clustercinnabar','tc4_clustercopper','tc4_clustertin','tc4_clustersilver','tc4_clusterlead','tc4_nuggetchicken','tc4_nuggetbeef','tc4_nuggetpork','tc4_nuggetfish'):
    req(item in be,'bonus mapping '+item)
req('Flux' not in be and 'flux' not in be,'invented flux mutation')
noz=text('src/main/java/com/darkifov/thaumcraft/blockentity/InfernalFurnaceNozzleBlockEntity.java')
for token in ('Aspect.IGNIS','NOZZLE_DRAW_INTERVAL_TICKS','NOZZLE_DRAW_ADMISSION_SPEED','NOZZLE_SUCTION_ADMISSION_SPEED','NOZZLE_SUCTION','IGNIS_ESSENTIA_SPEED_TICKS','takeEssentiaOriginal'):
    req(token in noz,'nozzle '+token)
tube=text('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java')
for token in ('InfernalFurnaceNozzleBlockEntity','InfernalFurnaceDestination','nozzle.acceptFromTube'):
    req(token in tube,'tube integration '+token)
mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for token in ('INFERNAL_FURNACE = BLOCKS.register("tc4_block_arcane_furnace"','INFERNAL_FURNACE_BLOCK_ENTITY','INFERNAL_FURNACE_NOZZLE_BLOCK_ENTITY','noLootTable()','TC4InfernalFurnaceParity.LIGHT_LEVEL'):
    req(token in mod,'registry '+token)
wand=text('src/main/java/com/darkifov/thaumcraft/block/WandItem.java')
req('InfernalFurnaceMultiblock.tryCreate' in wand,'wand activation path')
# resources
bs=json.loads(text('src/main/resources/assets/thaumcraft/blockstates/tc4_block_arcane_furnace.json'))
req(len(bs.get('variants',{}))==132,'blockstate variants')
models=list((R/'src/main/resources/assets/thaumcraft/models/block').glob('tc4_block_arcane_furnace_*.json'))
req(len(models)==132,'model count')
source_zip=R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip'
with zipfile.ZipFile(source_zip) as z:
    prefix='Thaumcraft4-1.7.10-master/'
    originals={
      'thaumcraft/common/tiles/TileArcaneFurnace.java':('new ItemStack[32]','this.speedyTime = VisNetHandler.drainVis','return (this.speedyTime > 0 ? 80 : 140) - 20 * getBellows()','Math.min(3, bellows)','getSmeltingBonus','EntityXPOrb'),
      'thaumcraft/common/tiles/TileArcaneFurnaceNozzle.java':('this.furnace.speedyTime < 60','this.furnace.speedyTime += 600','++this.drawDelay % 5','return 128','Aspect.FIRE'),
      'thaumcraft/common/blocks/BlockArcaneFurnace.java':('EntityItem','0.02500000037252903D','DamageSource.field_76371_c, 3.0F','func_70015_d(10)','Potion.field_76428_l','Potion.field_76429_m')}
    for rel,tokens in originals.items():
        s=z.read(prefix+rel).decode('utf-8',errors='replace')
        for token in tokens:req(token in s,'original '+rel+' '+token)
    names=sorted(n for n in z.namelist() if '/assets/thaumcraft/textures/blocks/furnace' in n and n.endswith('.png'))
    req(len(names)==25,'original furnace texture count')
    for n in names:
        cur=R/'src/main/resources/assets/thaumcraft/textures/block/tc4'/Path(n).name
        req(cur.is_file(),'missing texture '+cur.name)
        req(sha(cur.read_bytes())==sha(z.read(n)),'texture hash '+cur.name)
# counts
src=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',src,re.S)
req(len(methods)>=233 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for name in ('infernalFurnaceCookCadenceMatchesOriginal','infernalFurnaceInventoryAndNbtMatchOriginal','infernalFurnaceIgnisAccelerationMatchesOriginal','infernalFurnaceFormationCostMatchesOriginal','infernalFurnaceRestorationMapMatchesOriginal','infernalFurnaceCoreAndNozzleBlockEntitiesExist','infernalFurnaceLivingHazardAndBonusConstantsMatchOriginal'):
    req(name in methods,'GameTest '+name)
man=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'));ids=[x['id'] for x in man['tests']]
req(tuple(map(int,man['version'].split('.'))) >= (11,64,28),'manifest version')
req(len(ids)>=704 and len(ids)==len(set(ids)),f'manifest {len(ids)}')
for sid in ('gameplay.infernal_furnace_multiblock_formation','gameplay.infernal_furnace_ignis_essentia','gameplay.infernal_furnace_smelting_bonuses','gameplay.infernal_furnace_break_restoration','regression.infernal_furnace_no_flux_invention'):
    req(sid in ids,'scenario '+sid)
prompt=(R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md').read_bytes();prompt2=(R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes()
req(prompt==prompt2,'mandatory prompt copies differ')
print('TC4 v11.64.28 Infernal Furnace full-closure guard: PASS')
