#!/usr/bin/env python3
"""v11.64.18 guard: complete Arcane Bellows source/resource closure."""
from pathlib import Path
import hashlib, json, re, zipfile
R=Path(__file__).resolve().parents[1]
def text(p): return (R/p).read_text(encoding='utf-8')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.18 Arcane Bellows full-closure guard: FAIL: '+msg)
def ver(s):
    m=re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',s); req(m,'version parse'); return tuple(map(int,m.groups()))
def sha(p): return hashlib.sha256((R/p).read_bytes()).hexdigest()
req(ver(text('build.gradle')) >= (11,64,18),'build version')
req(ver(text('src/main/resources/META-INF/mods.toml')) >= (11,64,18),'mods version')

c=text('src/main/java/com/darkifov/thaumcraft/block/TC4ArcaneBellowsParity.java')
for t in ('CONTRACT_VERSION = "11.64.18"','BLOCK_HARDNESS = 2.5F','BLOCK_RESISTANCE = 10.0F',
 'SHAPE_MIN_XZ = 0.1D','SHAPE_MAX_XZ = 0.9D','INITIAL_INFLATION_BASE = 0.35F',
 'INITIAL_INFLATION_RANGE = 0.55F','DEFLATE_STEP = 0.075F','INFLATE_STEP = 0.025F',
 'SOUND_VOLUME = 0.01F','VANILLA_FURNACE_DELAY_TICKS = 2','MAX_GENERIC_ATTACHED_BELLOWS = 6',
 'MAX_INFERNAL_FURNACE_BELLOWS = 3','BUFFER_SUCTION_PER_BELLOWS = 32',
 'CRUCIBLE_BASE_HEAT_GAIN = 1','CRUCIBLE_HEAT_GAIN_PER_BELLOWS = 2',
 'ALCHEMICAL_FURNACE_TIME_REDUCTION_PER_BELLOWS = 0.125F',
 'INFERNAL_FURNACE_BONUS_CHANCE_PER_BELLOWS = 0.44F','playerTicks / 8.0F',
 'case 2 -> new Offset(0, 0, -1)','default -> new Offset(0, 0, 0)'):
    req(t in c,'contract token '+t)

b=text('src/main/java/com/darkifov/thaumcraft/block/BellowsBlock.java')
for t in ('Block.box(','SHAPE_MIN_XZ * 16.0D','context.getClickedFace().getOpposite()',
 'bellows.refreshAttachment()','return SHAPE','instanceof BellowsBlockEntity',
 'facesTarget(state, directionFromBellowsToTarget)','!level.hasNeighborSignal(pos)',
 'countActiveBellows(Level level, BlockPos targetPos, Direction[] directions)'):
    req(t in b,'block path '+t)
be=text('src/main/java/com/darkifov/thaumcraft/blockentity/BellowsBlockEntity.java')
for t in ('TC4ArcaneBellowsParity.initialInflation','TC4ArcaneBellowsParity.animationStep',
 'SoundEvents.GHAST_SHOOT','TC4ArcaneBellowsParity.soundPitch','VANILLA_FURNACE_DELAY_TICKS',
 'onVanillaFurnace = level.getBlockState(worldPosition.relative(facing)).is(Blocks.FURNACE)',
 'legacyVanillaFurnaceOffset(facing().get3DDataValue())','canAdvanceVanillaFurnace(cookTime)',
 'tag.putByte("orientation"','tag.putBoolean("onVanillaFurnace"','tag.getBoolean("OnVanillaFurnace")'):
    req(t in be,'block entity path '+t)
for forbidden in ('tag.putInt("Delay"','tag.putFloat("inflation"','previousInflation','AttachmentInitialized'):
    req(forbidden not in be,'transient/stale state remains '+forbidden)

cr=text('src/main/java/com/darkifov/thaumcraft/blockentity/CrucibleBlockEntity.java')
req('TC4ArcaneBellowsParity.crucibleHeatGain(bellowsCount)' in cr,'crucible heat production bridge')
req('for (Direction direction : Direction.Plane.HORIZONTAL)' in cr and 'instanceof BellowsBlock' in cr,'crucible unfiltered scan')
af=text('src/main/java/com/darkifov/thaumcraft/blockentity/AlchemicalFurnaceBlockEntity.java')
req('MAX_BELLOWS = TC4ArcaneBellowsParity.MAX_GENERIC_ATTACHED_BELLOWS' in af,'six alchemical bellows')
req('BellowsBlock.countActiveBellows(level, worldPosition, Direction.values())' in af,'alchemical generic scan')
req('TC4ArcaneBellowsParity.alchemicalFurnaceSmeltTime' in af,'alchemical formula bridge')
tube=text('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java')
req('TC4ArcaneBellowsParity.bufferSuction(bellows, choke)' in tube,'buffer suction bridge')
req('BellowsBlock.countActiveBellows(level, worldPosition, Direction.values())' in tube,'buffer generic scan')

mod=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
for t in ('BELLOWS = bellowsBlock("bellows"','.strength(TC4ArcaneBellowsParity.BLOCK_HARDNESS, TC4ArcaneBellowsParity.BLOCK_RESISTANCE)',
 '.sound(SoundType.WOOD).noOcclusion()','new BellowsBlockItem(block.get()','BELLOWS_BLOCK_ENTITY'):
    req(t in mod,'registration '+t)
renderer=text('src/main/java/com/darkifov/thaumcraft/client/render/BellowsRenderer.java')
for t in ('textures/models/bellows.png','bellows.inflation()','case DOWN','rotationDegrees(90.0F)',
 'case UP','rotationDegrees(270.0F)','case NORTH','rotationDegrees(180.0F)'):
    req(t in renderer,'world renderer '+t)
item=text('src/main/java/com/darkifov/thaumcraft/client/render/BellowsItemRenderer.java')
for t in ('TC4ArcaneBellowsParity.inventoryInflation','rotationDegrees(90.0F)','translate(-0.5D, -0.5D, -0.5D)',
 'rotationDegrees(180.0F)'):
    req(t in item,'item renderer '+t)
for f in ('type.firstPerson()','TransformType.GUI','TransformType.GROUND','TransformType.FIXED'):
    req(f not in item,'approximate item context remains '+f)
model=text('src/main/java/com/darkifov/thaumcraft/client/render/model/TC4BellowsModel.java')
for t in ('LayerDefinition.create(mesh, 128, 64)','LayerDefinition.create(mesh, 64, 32)',
 'float bounded = inflation;','float separation = 0.125F + bounded * 0.875F',
 'addBox(-10.0F, -12.03333F, -10.0F, 20.0F, 24.0F, 20.0F)'):
    req(t in model,'model/UV '+t)
req(sha('src/main/resources/assets/thaumcraft/textures/models/bellows.png') ==
 'fe822f07c4f535bb4645ee10205f1e7863a1ab0cb3866566334665b0fdf02ebf','bellows texture hash')
for p in ('src/main/resources/assets/thaumcraft/models/item/bellows.json','src/main/resources/assets/thaumcraft/models/item/tc4_block_bellows.json'):
    d=json.loads(text(p)); req(d.get('parent')=='builtin/entity' and d.get('gui_light')=='front','BEWLR model '+p)

r=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_bellows.json'))
req(r['research']=='BELLOWS' and r['pattern']==['WW ','LCI','WW '],'recipe pattern/research')
req(r['key']=={'W':'#forge:planks','L':'minecraft:sugar','C':'thaumcraft:aer_shard','I':'minecraft:iron_ingot'},'recipe key')
req(r['aspects']=={'AER':10,'ORDO':5} and r['result']=={'item':'thaumcraft:tc4_block_bellows','count':1},'recipe costs/result')
research=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java')
for t in ('"BELLOWS", "Arcane Bellows", "Stoking the flames"','"ARTIFICE", -6, -2, 1',
 'aspects("AER", 6, "MACHINA", 3, "MOTUS", 3)','new String[] {"INFERNALFURNACE"}',
 'new String[] {"secondary", "concealed"}','new String[] {"TEXT", "ARCANE_CRAFTING", "TEXT"}',
 'new String[] {"Bellows"}'):
    req(t in research,'research '+t)

with zipfile.ZipFile(R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    def orig(suffix):
        n=next((n for n in z.namelist() if n.endswith('/'+suffix)),None); req(n,'original missing '+suffix); return z.read(n).decode(errors='replace')
    ot=orig('thaumcraft/common/tiles/TileBellows.java'); ob=orig('thaumcraft/common/blocks/BlockWoodenDevice.java')
    oi=orig('thaumcraft/common/blocks/BlockWoodenDeviceItem.java'); oc=orig('thaumcraft/common/tiles/TileCrucible.java')
    oa=orig('thaumcraft/common/tiles/TileAlchemyFurnace.java'); obu=orig('thaumcraft/common/tiles/TileTubeBuffer.java')
    oinf=orig('thaumcraft/common/tiles/TileArcaneFurnace.java'); orend=orig('thaumcraft/client/renderers/tile/TileBellowsRenderer.java')
    orec=orig('thaumcraft/common/config/ConfigRecipes.java'); ores=orig('thaumcraft/common/config/ConfigResearch.java')
for t in ('0.35F + this.field_145850_b.field_73012_v.nextFloat() * 0.55F','this.inflation -= 0.075F',
 'this.inflation += 0.025F','mob.ghast.fireball','this.delay >= 2','this.field_145848_d, this.field_145849_e + dir.offsetZ',
 'field_145961_j > 0','field_145961_j < 199','"orientation"','"onVanillaFurnace"'):
    req(t in ot,'original TileBellows '+t)
for t in ('func_149711_c(2.5F)','func_149752_b(10.0F)','func_149676_a(0.1F, 0.0F, 0.1F, 0.9F, 1.0F, 0.9F)'):
    req(t in ob,'original block '+t)
req('ForgeDirection.getOrientation(side).getOpposite()' in oi and 'onVanillaFurnace = true' in oi,'original placement')
req('this.heat + (1 + this.bellows * 2)' in oc,'original crucible formula')
req('vs * 10 * (1.0F - 0.125F * this.bellows)' in oa,'original alchemical formula')
req('this.bellows * 32' in obu,'original buffer suction')
for t in ('dir != ForgeDirection.UP','dir.offsetX * 2','return Math.min(3, bellows)',
 '(this.speedyTime > 0 ? 80 : 140) - 20 * getBellows()','nextFloat() < 0.44F'):
    req(t in oinf,'original Infernal Furnace '+t)
req('field_70173_aa / 8.0F' in orend and 'scale * 0.875F' in orend,'original renderer animation')
req('addArcaneCraftingRecipe("BELLOWS"' in orec and 'Aspect.AIR, 10' in orec and 'Aspect.ORDER, 5' in orec,'original recipe')
req('new ResearchItem("BELLOWS", "ARTIFICE"' in ores and 'Aspect.AIR, 6' in ores and 'Aspect.MECHANISM, 3' in ores and 'Aspect.MOTION, 3' in ores,'original research')

gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=167 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for m in ('arcaneBellowsShapePlacementAndNbtMatchOriginal','arcaneBellowsAnimationAndInventoryInflationMatchOriginal',
 'arcaneBellowsBoostsOnlyLegacyHorizontalVanillaFurnaceTarget','arcaneBellowsConsumerCountingMatchesOriginal',
 'arcaneBellowsResearchRecipeAndInfernalContractMatchOriginal'):
    req(m in methods,'GameTest '+m)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json')); ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,64,18) and len(ids)>=549 and len(ids)==len(set(ids)),f'manifest {manifest["version"]}/{len(ids)}')
for sid in ('gametest.arcane_bellows_shape_placement_nbt','gametest.arcane_bellows_animation_inventory',
 'gametest.arcane_bellows_vanilla_furnace_legacy_offsets','gametest.arcane_bellows_consumer_counting',
 'research.arcane_bellows_recipe_and_entry','external.arcane_bellows_infernal_furnace_contract'):
    req(sid in ids,'scenario '+sid)
ev=json.loads(text('tools/data/tc4_arcane_bellows_full_source_evidence_v11.64.18.json'))
req(ev['round']=='11.64.18' and ev['source_closure']=='CLOSED' and ev['resource_closure']=='CLOSED','evidence closure')
req(ev['build_status']=='NOT_OBTAINED' and ev['runtime_status']=='NOT_VERIFIED','evidence honesty')
prompt=text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md')
req(prompt==text('PROMPT_FOR_FUTURE_CHAT_RU.md'),'prompt copies differ')
for t in ('Один релиз — один предмет или одна цельная механика','SOURCE CLOSED','RESOURCE CLOSED','BUILD VERIFIED','RUNTIME VERIFIED','Упаковка архива без этого файла запрещена'):
    req(t in prompt,'prompt token '+t)
report=text('TC4_11.64.18_ARCANE_BELLOWS_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md')
req('Infernal Furnace' in report and 'BUILD VERIFIED: нет' in report and 'RUNTIME VERIFIED: нет' in report,'report proof boundary')
print(f'TC4 v11.64.18 Arcane Bellows full-closure guard: PASS ({len(methods)} GameTests; {len(ids)} scenarios; source/resource/prompt/external-contract)')
