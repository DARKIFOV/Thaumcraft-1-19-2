#!/usr/bin/env python3
"""v11.64.20 guard: complete Arcane Levitator source/resource closure."""
from pathlib import Path
import hashlib,json,re,zipfile,io
from PIL import Image
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.20 Arcane Levitator full-closure guard: FAIL: '+msg)
def ver(s):
    m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',s); req(m,'version parse'); return tuple(map(int,m.groups()))
def sha(p): return hashlib.sha256((R/p).read_bytes()).hexdigest()
req(ver(text('build.gradle')) >= (11,64,20),'build version')
req(ver(text('src/main/resources/META-INF/mods.toml')) >= (11,64,20),'mods version')

c=text('src/main/java/com/darkifov/thaumcraft/block/TC4ArcaneLevitatorParity.java')
for t in ('CONTRACT_VERSION = "11.64.20"','BLOCK_HARDNESS = 2.5F','BLOCK_EXPLOSION_RESISTANCE = 15.0F',
 'REFRESH_INTERVAL_TICKS = 100','BASE_RANGE = 10','RANGE_PER_LOWER_LEVITATOR = 10',
 'MAX_UPWARD_VELOCITY = 0.3499999940395355D','LIFT_INCREMENT = 0.10000000149011612D',
 'SNEAK_DESCENT_MULTIPLIER = 0.8999999761581421D','INVENTORY_TOP_GLOW = 0x00A000',
 'INVENTORY_SIDE_GLOW = 0xEECCFF','WORLD_SIDE_GLOW = 0xDD11FF',
 'ACTIVE_GLOW_LEGACY_BRIGHTNESS = 180','ACTIVE_GLOW_BLOCK_LIGHT = ACTIVE_GLOW_LEGACY_BRIGHTNESS >> 4',
 'PARTICLE_MULTIPLIER = 6','PARTICLE_LIFETIME = 3 * PARTICLE_MULTIPLIER',
 'PARTICLE_GRAVITY = -0.3F','PARTICLE_GATE_DENOMINATOR = 6','PARTICLE_ALL_THRESHOLD = 4',
 'PARTICLE_DECREASED_THRESHOLD = 2','PARTICLE_MINIMAL_THRESHOLD = 0','lowerSegmentContributes','connectsRedstoneOnLegacySide'):
    req(t in c,'contract token '+t)

b=text('src/main/java/com/darkifov/thaumcraft/block/ArcaneLevitatorBlock.java')
for t in ('BooleanProperty POWERED = BlockStateProperties.POWERED','getBlockSupportShape','HORIZONTAL_SUPPORT_SHAPE',
 'direction.getAxis().isHorizontal()','markStackBelow(level, pos)','origin.below().mutable()',
 'powered != levitator.lastPowerState()','state.setValue(POWERED, powered)',
 'TC4ArcaneLevitatorEffectsBridge.spawn(level, x, pos.getY() + 1.0D, z, random)','0.2F + random.nextFloat() * 0.6F'):
    req(t in b,'block token '+t)

be=text('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneLevitatorBlockEntity.java')
for t in ('levitator.counter++','levitator.counter % TC4ArcaneLevitatorParity.REFRESH_INTERVAL_TICKS == 0',
 'new AABB(pos.getX(), pos.getY() + 1.0D, pos.getZ()','entity instanceof ItemEntity','entity.isPushable()',
 'entity instanceof AbstractHorse','entity.isShiftKeyDown()','entity.fallDistance = 0.0F',
 'level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above())',
 'TC4ArcaneLevitatorParity.lowerSegmentContributes(level.hasNeighborSignal(below))',
 'aboveState.isSolidRender(level, above)','state.setValue(ArcaneLevitatorBlock.POWERED, levitator.lastPowerState)'):
    req(t in be,'block entity token '+t)
for stale in ('tag.putInt("Counter"','tag.putInt("RangeAbove"','tag.putBoolean("RequiresUpdate"','tag.putBoolean("LastPowerState"'):
    req(stale not in be,'transient field persisted '+stale)

fx=text('src/main/java/com/darkifov/thaumcraft/client/fx/TC4ArcaneLevitatorSparkleParticle.java')
for t in ('VELOCITY_DAMPING = 0.9080000019073486D','this.alpha = 0.75F','this.hasPhysics = false',
 'this.lifetime = TC4ArcaneLevitatorParity.PARTICLE_LIFETIME','this.yd -= 0.04D * this.gravity',
 'ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT','return 0xF000F0','this.age / TC4ArcaneLevitatorParity.PARTICLE_MULTIPLIER'):
    req(t in fx,'particle token '+t)


bridge=text('src/main/java/com/darkifov/thaumcraft/block/TC4ArcaneLevitatorEffectsBridge.java')
for t in ('private static final SparkleSpawner NO_OP','public static void install','public static void spawn'):
    req(t in bridge,'effects bridge '+t)
clientfx=text('src/main/java/com/darkifov/thaumcraft/client/fx/TC4ArcaneLevitatorClientEffects.java')
for t in ('minecraft.options.particles().get()','case ALL -> TC4ArcaneLevitatorParity.PARTICLE_ALL_THRESHOLD',
 'case DECREASED -> TC4ArcaneLevitatorParity.PARTICLE_DECREASED_THRESHOLD',
 'case MINIMAL -> TC4ArcaneLevitatorParity.PARTICLE_MINIMAL_THRESHOLD',
 'random.nextInt(TC4ArcaneLevitatorParity.PARTICLE_GATE_DENOMINATOR)','double greenRandomUnit = random.nextFloat()',
 'minecraft.particleEngine.createParticle'):
    req(t in clientfx,'client density gate '+t)

mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for t in ('PARTICLE_TYPES.register("arcane_levitator_sparkle"','TC4ArcaneLevitatorParity.BLOCK_HARDNESS',
 'TC4ArcaneLevitatorParity.BLOCK_EXPLOSION_RESISTANCE','new BlockItem(ARCANE_LEVITATOR.get()',
 'ArcaneLevitatorBlockEntity::new, ARCANE_LEVITATOR.get()'):
    req(t in mod,'registration '+t)
req('ARCANE_LEVITATOR.get(), new Item.Properties().tab(THAUMCRAFT_TAB).rarity' not in mod,'non-original rarity')
client=text('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
for t in ('TC4ArcaneLevitatorSparkleParticle.Provider::new','TC4ArcaneLevitatorParity.WORLD_TOP_GLOW',
 'TC4ArcaneLevitatorParity.WORLD_SIDE_GLOW','TC4ArcaneLevitatorParity.INVENTORY_TOP_GLOW',
 'TC4ArcaneLevitatorParity.INVENTORY_SIDE_GLOW','TC4ArcaneLevitatorEffectsBridge.install(TC4ArcaneLevitatorClientEffects::spawn)'):
    req(t in client,'client token '+t)

state=json.loads(text('src/main/resources/assets/thaumcraft/blockstates/tc4_block_levitator.json'))
req(state['variants']['powered=false']['model']=='thaumcraft:block/tc4_block_levitator','active model variant')
req(state['variants']['powered=true']['model']=='thaumcraft:block/tc4_block_levitator_powered','powered model variant')
active=json.loads(text('src/main/resources/assets/thaumcraft/models/block/tc4_block_levitator.json'))
powered=json.loads(text('src/main/resources/assets/thaumcraft/models/block/tc4_block_levitator_powered.json'))
req(active.get('ambientocclusion') is False and powered.get('ambientocclusion') is False,'model AO')
req(len(active['elements'])==3 and len(powered['elements'])==3,'world model elements')
req(active['elements'][0]['from']==[0,0,0] and active['elements'][0]['to']==[16,16,16],'base cube')
for idx in (1,2):
    req(active['elements'][idx].get('forge_data')=={'block_light':11,'sky_light':0,'ambient_occlusion':False},'active glow light')
    req('forge_data' not in powered['elements'][idx],'powered normal light')
req(active['elements'][1]['faces']['up'].get('cullface')=='up','top culling')
for face in ('north','south','west','east'):
    req(active['elements'][2]['faces'][face].get('cullface')==face,'side culling '+face)
inv=json.loads(text('src/main/resources/assets/thaumcraft/models/item/tc4_block_levitator_inventory.json'))
req(len(inv['elements'])==3,'inventory model elements')
req(inv['elements'][1]['from']==[0.16,14.4,0.16] and inv['elements'][1]['to']==[15.84,15.84,15.84],'inventory top volume')
req(inv['elements'][2]['from']==[0.16,1.6,0.16] and inv['elements'][2]['to']==[15.84,14.4,15.84],'inventory side volume')
item=json.loads(text('src/main/resources/assets/thaumcraft/models/item/tc4_block_levitator.json'))
req(item.get('parent')=='thaumcraft:item/tc4_block_levitator_inventory','inventory parent')

recipe=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_levitator.json'))
req(recipe['research']=='LEVITATOR' and recipe['pattern']==['WEW','BNB','WAW'],'recipe pattern/research')
req(recipe['key']=={'W':'thaumcraft:greatwood_planks','E':'thaumcraft:terra_shard','B':'minecraft:iron_ingot','N':'thaumcraft:nitor','A':'thaumcraft:aer_shard'},'recipe key mapping')
req(recipe['aspects']=={'AER':10,'TERRA':5} and recipe.get('v11_64_20_exact_source') is True,'recipe costs/marker')
research=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java')
for t in ('"LEVITATOR", "Arcane Levitator", "Next best thing to flying"','"ARTIFICE", -3, -3, 1',
 'aspects("MOTUS", 3, "VOLATUS", 3, "AER", 3)','new String[] {"NITOR"}',
 'new String[] {"concealed"}','new String[] {"TEXT", "ARCANE_CRAFTING"}','new String[] {"Levitator"}'):
    req(t in research,'research '+t)

for tex in ('liftertop','lifterside','arcaneearbottom','animatedglow'):
    req(sha('src/main/resources/assets/thaumcraft/original_tc4_1710/textures/blocks/'+tex+'.png')==sha('src/main/resources/assets/thaumcraft/textures/block/tc4/'+tex+'.png'),'texture '+tex)
atlas=Image.open(R/'src/main/resources/assets/thaumcraft/original_tc4_1710/textures/misc/particles.png').convert('RGBA')
for frame in range(4):
    expected=atlas.crop((frame*16,4*16,frame*16+16,4*16+16))
    actual=Image.open(R/f'src/main/resources/assets/thaumcraft/textures/particle/arcane_levitator_sparkle_{frame}.png').convert('RGBA')
    req(actual.size==(16,16) and actual.tobytes()==expected.tobytes(),'particle frame '+str(frame))
particle=json.loads(text('src/main/resources/assets/thaumcraft/particles/arcane_levitator_sparkle.json'))
req(particle['textures']==[f'thaumcraft:arcane_levitator_sparkle_{i}' for i in range(4)],'particle descriptor')

with zipfile.ZipFile(R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    def orig(suffix):
        n=next((n for n in z.namelist() if n.endswith('/'+suffix)),None); req(n,'original missing '+suffix); return z.read(n).decode(errors='replace')
    ot=orig('thaumcraft/common/tiles/TileLifter.java')
    ob=orig('thaumcraft/common/blocks/BlockLifter.java')
    ore=orig('thaumcraft/client/renderers/block/BlockLifterRenderer.java')
    ofx=orig('thaumcraft/client/fx/particles/FXSparkle.java')
    ocp=orig('thaumcraft/client/ClientProxy.java')
    orc=orig('thaumcraft/common/config/ConfigRecipes.java')
    ors=orig('thaumcraft/common/config/ConfigResearch.java')
    ocol=orig('thaumcraft/common/blocks/BlockCustomOreItem.java')
for t in ('this.counter % 100 == 0','int max = 10','max += 10','this.rangeAbove += 1',
 'e instanceof EntityItem','e.func_70104_M()','e instanceof EntityHorse','0.8999999761581421D',
 '0.3499999940395355D','0.10000000149011612D','e.field_70143_R = 0.0F'):
    req(t in ot,'original TileLifter '+t)
req('func_72864_z(this.field_145851_c, this.field_145848_d - count, this.field_145849_e)' in ot,'original lower-position power query')
req('func_72864_z(this.field_145851_c, this.field_145848_d + 1, this.field_145849_e)' in ot,'original above power query')
req('func_74782_a' not in ot and 'func_145839_a' not in ot,'original has no NBT persistence')
for t in ('func_149711_c(2.5F)','func_149752_b(15.0F)','side != ForgeDirection.UP','side != ForgeDirection.DOWN',
 'return side > 1','updateLifterStack(world, x, y, z)','i + 0.2F + r.nextFloat() * 0.6F'):
    req(t in ob,'original BlockLifter '+t)
for t in ('colors[4]','colors[5]','0.01F, 0.9F, 0.01F, 0.99F, 0.99F, 0.99F',
 '0.01F, 0.1F, 0.01F, 0.99F, 0.9F, 0.99F','func_78378_d(14488063)','bb = 180'):
    req(t in ore,'original renderer '+t)
for t in ('this.field_70547_e = (3 * m)','this.multiplier = m','this.field_70552_h = 0.2F',
 'this.field_70553_i = (0.7F + world.field_73012_v.nextFloat() * 0.3F)','this.field_70545_g',
 'this.field_70181_x -= 0.04D * this.field_70545_g','0.9080000019073486D','public int particle = 16'):
    req(t in ofx,'original sparkle '+t)
req('new FXSparkle(getClientWorld(), x, y, z, size, color, 6)' in ocp and 'fx.setGravity(gravity)' in ocp,'original proxy sparkle')
req('addArcaneCraftingRecipe("LEVITATOR"' in orc and '"WEW", "BNB", "WAW"' in orc,'original recipe')
req('new ResearchItem("LEVITATOR", "ARTIFICE"' in ors and 'Aspect.MOTION, 3' in ors and 'Aspect.FLIGHT, 3' in ors and 'Aspect.AIR, 3' in ors,'original research')
req('{ 16777215, 16777086, 16727041, 37119, 40960, 15650047, 5592439 }' in ocol,'original colors')

gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=177 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for m in ('arcaneLevitatorVelocityRangeAndEntityAdmissionMatchOriginal','arcaneLevitatorStackObstructionAndTransientStateMatchOriginal',
 'arcaneLevitatorProductionTickLiftsItemsAndResetsFallDistance','arcaneLevitatorHorizontalSupportRedstoneAndItemPropertiesMatchOriginal',
 'arcaneLevitatorResearchAndRecipeIndexMatchOriginal'):
    req(m in methods,'GameTest '+m)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int, manifest['version'].split('.'))) >= (11,64,20) and len(ids)>=579 and len(ids)==len(set(ids)),f'manifest {manifest["version"]}/{len(ids)}')
for sid in ('gametest.arcane_levitator_velocity_range_filter','gametest.arcane_levitator_stack_obstruction_transient',
 'gametest.arcane_levitator_production_tick','gametest.arcane_levitator_support_redstone_item',
 'gametest.arcane_levitator_research_recipe','gameplay.arcane_levitator_lower_power_sampling',
 'persistence.arcane_levitator_transient_recompute','client.arcane_levitator_active_powered_glow',
 'client.arcane_levitator_exact_sparkle','jei.arcane_levitator_exact_recipe','dedicated.arcane_levitator_multiplayer_sneak'):
    req(sid in ids,'scenario '+sid)
ev=json.loads(text('tools/data/tc4_arcane_levitator_full_source_evidence_v11.64.20.json'))
req(ev['round']=='11.64.20' and ev['source_closure']=='CLOSED' and ev['resource_closure']=='CLOSED','evidence closure')
req(ev['build_status']=='NOT_OBTAINED' and ev['runtime_status']=='NOT_VERIFIED','evidence honesty')
prompt=text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md')
req(prompt==text('PROMPT_FOR_FUTURE_CHAT_RU.md'),'prompt copies differ')
for t in ('Один релиз — один предмет или одна цельная механика','SOURCE CLOSED','RESOURCE CLOSED','BUILD VERIFIED','RUNTIME VERIFIED','Упаковка архива без этого файла запрещена'):
    req(t in prompt,'prompt token '+t)
print(f'TC4 v11.64.20 Arcane Levitator full-closure guard: PASS ({len(methods)} GameTests; {len(ids)} scenarios; source/resource/prompt)')
