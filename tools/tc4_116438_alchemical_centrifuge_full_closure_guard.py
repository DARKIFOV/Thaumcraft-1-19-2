#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re
R=Path(__file__).resolve().parents[1]
O=R/'reference/original_source/Thaumcraft4-1.7.10-master'
def text(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def original(p): return (O/p).read_text(encoding='utf-8',errors='replace')
def req(ok,msg):
    if not ok: raise SystemExit('TC4 v11.64.38 centrifuge full-closure guard: FAIL: '+msg)
def sha(p): return hashlib.sha256(Path(p).read_bytes()).digest()
req("version = '11.64.38'" in text('build.gradle'),'build version')
req('version="11.64.38"' in text('src/main/resources/META-INF/mods.toml'),'mods version')
parity=text('src/main/java/com/darkifov/thaumcraft/alchemy/TC4AlchemicalCentrifugeParity.java')
for t in ('CONTRACT_VERSION = "11.64.38"','INPUT_SUCTION = 64','PROCESS_START = 39','DRAW_INTERVAL_TICKS = 5','OUTPUT_CAPACITY = 1','DEFAULT_FACING_ORDINAL = 2','MAX_ROTATION_SPEED = 20.0F','ROTATION_ACCELERATION = 2.0F','ROTATION_DECELERATION = 0.5F','tickProcess','shouldProcess','nextRotationSpeed','shouldPlayPump','componentIndex'):
    req(t in parity,'parity '+t)
be=text('src/main/java/com/darkifov/thaumcraft/blockentity/AlchemicalCentrifugeBlockEntity.java')
for t in ('ORIGINAL_INPUT_SUCTION = TC4AlchemicalCentrifugeParity.INPUT_SUCTION','ORIGINAL_PROCESS_TICKS = TC4AlchemicalCentrifugeParity.PROCESS_START','ORIGINAL_DRAW_INTERVAL_TICKS = TC4AlchemicalCentrifugeParity.DRAW_INTERVAL_TICKS','counter % ORIGINAL_DRAW_INTERVAL_TICKS == 0','TC4AlchemicalCentrifugeParity.tickProcess','TC4AlchemicalCentrifugeParity.shouldProcess','level.random.nextInt(2)','Direction.DOWN ? ORIGINAL_INPUT_SUCTION : 0','face == Direction.UP || face == Direction.DOWN','face == Direction.DOWN','face == Direction.UP','aspect.isPrimal()','below instanceof EssentiaTubeBlockEntity','below instanceof EssentiaJarBlockEntity','below instanceof AlembicBlockEntity','below instanceof AlchemicalCentrifugeBlockEntity','tag.putString("aspectIn"','tag.putString("aspectOut"','tag.putInt("facing"','process = 0','counter = 0','rotation = 0.0F','rotationSpeed = 0.0F','TC4Sounds.event("pump")','playLocalSound','readOriginalNbt','handleUpdateTag(CompoundTag tag) { readOriginalNbt(tag); }','onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) { readOriginalNbt(packet.getTag()); }'):
    req(t in be,'production '+t)
for forbidden in ('tag.putInt("process"','tag.putInt("counter"','tag.putFloat("rotation"','tag.putFloat("rotationSpeed"','ServerLevel','playSound('):
    req(forbidden not in be,'temporary/server sound '+forbidden)
block=text('src/main/java/com/darkifov/thaumcraft/block/AlchemicalCentrifugeBlock.java')
for t in ('Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D)','RenderShape.ENTITYBLOCK_ANIMATED','getShape','getCollisionShape','AlchemicalCentrifugeBlockEntity::clientTick','AlchemicalCentrifugeBlockEntity::serverTick'):
    req(t in block,'block '+t)
req('displayClientMessage' not in block and 'Component.literal' not in block,'debug interaction remains')
reg=text('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
req('alchemicalCentrifugeBlock("alchemical_centrifuge"' in reg,'registration')
req('BlockBehaviour.Properties.of(Material.METAL).strength(0.5F, 5.0F).sound(SoundType.METAL).noOcclusion()' in reg,'hardness/resistance/sound')
renderer=text('src/main/java/com/darkifov/thaumcraft/client/render/AlchemicalCentrifugeRenderer.java')
for t in ('textures/models/centrifuge.png','model.renderBoxes','rotationDegrees(tile.rotation())','model.renderSpinnyBit','renderStandalone'):
    req(t in renderer,'renderer '+t)
model=text('src/main/java/com/darkifov/thaumcraft/client/render/model/TC4CentrifugeModel.java')
for t in ('crossbar','dingus1','dingus2','core','top','bottom','LayerDefinition.create(mesh, 64, 32)','renderBoxes','renderSpinnyBit'):
    req(t in model,'model '+t)
item=text('src/main/java/com/darkifov/thaumcraft/client/render/AlchemicalCentrifugeItemRenderer.java')
req('packedLight, packedOverlay, 0.0F' in item,'static inventory render')
req('getGameTime' not in item and 'currentTimeMillis' not in item,'animated item render remains')
blockmodel=json.loads(text('src/main/resources/assets/thaumcraft/models/block/alchemical_centrifuge.json'))
req(blockmodel.get('textures',{}).get('particle')=='thaumcraft:models/centrifuge','BER particle model')
recipe=json.loads(text('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_centrifuge.json'))
req(recipe.get('research')=='CENTRIFUGE','recipe research')
req(recipe.get('pattern')==[' T ','ACP',' T '],'recipe pattern')
req(recipe.get('key',{}).get('C')=='thaumcraft:tc4_block_alchemical_construct','recipe construct')
req(recipe.get('key',{}).get('A')=='thaumcraft:alembic' and recipe.get('key',{}).get('P')=='minecraft:piston','recipe components')
req(recipe.get('aspects')=={'AQUA':5,'ORDO':5,'PERDITIO':5},'recipe aspects')
research=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java')
for t in ('"CENTRIFUGE", "Alchemical Centrifuge", "Taking things apart"','"ALCHEMY", 10, 0, 2','aspects("PERDITIO", 3, "PRAECANTATIO", 3, "PERMUTATIO", 3, "FABRICO", 3)','new String[] {"TUBEFILTER"}','new String[] {"concealed"}','new String[] {"Centrifuge", "TubeBuffer"}'):
    req(t in research,'research '+t)
req('put(map, "CENTRIFUGE", "concealed")' in text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchMetadataIndex.java'),'research metadata')
orig=original('thaumcraft/common/tiles/TileCentrifuge.java')
for t in ('func_74779_i("aspectIn")','func_74779_i("aspectOut")','func_74762_e("facing")','func_74778_a("aspectIn"','func_74778_a("aspectOut"','func_74768_a("facing"','face == ForgeDirection.DOWN ? 64','this.process = 39','++this.count % 5 == 0','this.rotationSpeed < 20.0F','this.rotationSpeed += 2.0F','this.rotationSpeed -= 0.5F','nextInt(2)','ThaumcraftApiHelper.getConnectableTile'):
    req(t in orig,'original tile '+t)
orig_block=original('thaumcraft/common/blocks/BlockTube.java')
for t in ('func_149711_c(0.5F)','func_149752_b(5.0F)','func_149672_a(Block.field_149777_j)','func_149676_a(0.25F, 0.0F, 0.25F, 0.75F, 1.0F, 0.75F)'):
    req(t in orig_block,'original block '+t)
orig_model=original('thaumcraft/client/renderers/models/ModelCentrifuge.java')
for t in ('Crossbar','Dingus1','Dingus2','Core','Top','Bottom','renderBoxes','renderSpinnyBit'):
    req(t in orig_model,'original model '+t)
orig_renderer=original('thaumcraft/client/renderers/tile/TileCentrifugeRenderer.java')
for t in ('textures/models/centrifuge.png','renderBoxes','renderSpinnyBit','cf.rotation'):
    req(t in orig_renderer,'original renderer '+t)
orig_rec=original('thaumcraft/common/config/ConfigRecipes.java')
for t in ('recipes.put("Centrifuge"','new ItemStack(ConfigBlocks.blockMetalDevice, 1, 9)','Aspect.WATER, 5','Aspect.ORDER, 5','Aspect.ENTROPY, 5'):
    req(t in orig_rec,'original recipe '+t)
orig_res=original('thaumcraft/common/config/ConfigResearch.java')
for t in ('new ResearchItem("CENTRIFUGE"','Aspect.ENTROPY, 3','Aspect.MAGIC, 3','Aspect.EXCHANGE, 3','Aspect.CRAFT, 3','10, 0, 2','setParents(new String[] { "TUBEFILTER" })','setConcealed()'):
    req(t in orig_res,'original research '+t)
req(sha(O/'assets/thaumcraft/textures/models/centrifuge.png')==sha(R/'src/main/resources/assets/thaumcraft/textures/models/centrifuge.png'),'exact centrifuge texture')
gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=286 and len(methods)==len(set(methods)),f'GameTests {len(methods)}')
for name in ('centrifugeOnlySplitsCompoundFromBelowWithRedstonePause','centrifugePersistsOnlyOriginalNbtAndResumesImmediately','centrifugePullsCompoundDirectlyFromJarBelow','centrifugePullsCompoundDirectlyFromAlembicBelow','stackedCentrifugesCanRecursivelySplitCompoundOutput','centrifugeShapeAndTransportFacesMatchOriginal'):
    req(name in methods,'GameTest '+name)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest.get('version','0.0.0').split('.'))) >= (11,64,38),'manifest version')
req(len(ids)>=850 and len(ids)==len(set(ids)),f'manifest {len(ids)}')
for sid in ('gameplay.centrifuge_direct_jar_input','gameplay.centrifuge_direct_alembic_input','gameplay.centrifuge_recursive_stack','gameplay.centrifuge_primal_rejection','gameplay.centrifuge_random_component','gameplay.centrifuge_redstone_pause','transport.centrifuge_bottom_top_faces','persistence.centrifuge_canonical_nbt','persistence.centrifuge_processing_reload','client.centrifuge_full_model','client.centrifuge_rotation_sound','client.centrifuge_static_item','block.centrifuge_shape_hardness','integration.centrifuge_recipe_research','network.centrifuge_sync_preserves_spin'):
    req(sid in ids,'scenario '+sid)
prompt=text('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md')
req('Файл `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен' in prompt,'mandatory prompt')
print(f'TC4 v11.64.38 alchemical centrifuge full-closure guard: PASS ({len(methods)} GameTests, {len(ids)} scenarios, exact source/resource/recipe/research parity)')
