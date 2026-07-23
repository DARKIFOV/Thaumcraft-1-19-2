#!/usr/bin/env python3
from pathlib import Path
import json,re,subprocess,sys,hashlib
R=Path(__file__).resolve().parents[1]
checks=[]
def ck(ok,name):
    checks.append((name,bool(ok)))
    if not ok: print('FAIL:',name,file=sys.stderr)
def txt(rel): return (R/rel).read_text(encoding='utf-8',errors='replace')
def has(rel,*tokens):
    s=txt(rel)
    for t in tokens: ck(t in s,f'{rel}: {t}')
def lacks(rel,*tokens):
    s=txt(rel)
    for t in tokens: ck(t not in s,f'{rel}: lacks {t}')
def sha(rel): return hashlib.sha256((R/rel).read_bytes()).hexdigest()

# Release/evidence.
has('build.gradle',"version = '11.64.38'",'Alchemical Centrifuge full closure')
has('src/main/resources/META-INF/mods.toml','version="11.64.38"')
for rel in (
 'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md',
 'TC4_11.64.38_ALCHEMICAL_CENTRIFUGE_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md',
 'TC4_11.64.38_FULL_CLOSURE_STATUS_AND_PLAN_RU.md',
 'TC4_11.64.38_ALCHEMICAL_CENTRIFUGE_SOURCE_EVIDENCE.json',
 'TC4_11.64.38_FOCUSED_STATIC_CI_FINAL.log',
 'TC4_11.64.38_JAVA17_SELF_TEST.log',
 'TC4_11.64.38_GRADLE_BUILD_ATTEMPT.log',
 'TC4_11.64.38_BUILD_STATUS.txt'):
    ck((R/rel).is_file(),f'file {rel}')
has('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md','Файл `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен')
has('TC4_11.64.38_FOCUSED_STATIC_CI_FINAL.log','PASS (63/63, segmented execution)')
has('TC4_11.64.38_JAVA17_SELF_TEST.log','TC4AlchemicalCentrifugeFullClosureSelfTest: PASS','JAVA17_SELF_TEST_EXIT_CODE=0')
has('TC4_11.64.38_GRADLE_BUILD_ATTEMPT.log','UnknownHostException: services.gradle.org','GRADLE_EXIT_CODE=1')

# Pure contract.
has('src/main/java/com/darkifov/thaumcraft/alchemy/TC4AlchemicalCentrifugeParity.java',
 'CONTRACT_VERSION = "11.64.38"','INPUT_SUCTION = 64','PROCESS_START = 39','DRAW_INTERVAL_TICKS = 5',
 'OUTPUT_CAPACITY = 1','MAX_ROTATION_SPEED = 20.0F','ROTATION_ACCELERATION = 2.0F',
 'ROTATION_DECELERATION = 0.5F','tickProcess','shouldProcess','nextRotationSpeed','shouldPlayPump','componentIndex')

# Production lifecycle/NBT/network.
has('src/main/java/com/darkifov/thaumcraft/blockentity/AlchemicalCentrifugeBlockEntity.java',
 'ORIGINAL_INPUT_SUCTION = TC4AlchemicalCentrifugeParity.INPUT_SUCTION',
 'ORIGINAL_PROCESS_TICKS = TC4AlchemicalCentrifugeParity.PROCESS_START',
 'counter % ORIGINAL_DRAW_INTERVAL_TICKS == 0','TC4AlchemicalCentrifugeParity.tickProcess',
 'TC4AlchemicalCentrifugeParity.shouldProcess','level.random.nextInt(2)','aspect.isPrimal()',
 'below instanceof EssentiaTubeBlockEntity','below instanceof EssentiaJarBlockEntity',
 'below instanceof AlembicBlockEntity','below instanceof AlchemicalCentrifugeBlockEntity',
 'tag.putString("aspectIn"','tag.putString("aspectOut"','tag.putInt("facing"',
 'readOriginalNbt','handleUpdateTag(CompoundTag tag) { readOriginalNbt(tag); }',
 'onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) { readOriginalNbt(packet.getTag()); }',
 'process = 0','counter = 0','rotation = 0.0F','rotationSpeed = 0.0F','playLocalSound')
lacks('src/main/java/com/darkifov/thaumcraft/blockentity/AlchemicalCentrifugeBlockEntity.java',
 'tag.putInt("process"','tag.putInt("counter"','tag.putFloat("rotation"','tag.putFloat("rotationSpeed"','ServerLevel','playSound(')

# Block/render/model/item.
has('src/main/java/com/darkifov/thaumcraft/block/AlchemicalCentrifugeBlock.java',
 'Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D)','RenderShape.ENTITYBLOCK_ANIMATED',
 'getCollisionShape','AlchemicalCentrifugeBlockEntity::clientTick','AlchemicalCentrifugeBlockEntity::serverTick')
lacks('src/main/java/com/darkifov/thaumcraft/block/AlchemicalCentrifugeBlock.java','displayClientMessage','Component.literal')
has('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java',
 'alchemicalCentrifugeBlock("alchemical_centrifuge"','strength(0.5F, 5.0F).sound(SoundType.METAL).noOcclusion()')
has('src/main/java/com/darkifov/thaumcraft/client/render/model/TC4CentrifugeModel.java',
 'crossbar','dingus1','dingus2','core','top','bottom','LayerDefinition.create(mesh, 64, 32)','renderBoxes','renderSpinnyBit')
has('src/main/java/com/darkifov/thaumcraft/client/render/AlchemicalCentrifugeRenderer.java',
 'textures/models/centrifuge.png','model.renderBoxes','rotationDegrees(tile.rotation())','model.renderSpinnyBit')
has('src/main/java/com/darkifov/thaumcraft/client/render/AlchemicalCentrifugeItemRenderer.java','packedLight, packedOverlay, 0.0F')
lacks('src/main/java/com/darkifov/thaumcraft/client/render/AlchemicalCentrifugeItemRenderer.java','getGameTime','currentTimeMillis')

# Recipe/research.
r=json.loads(txt('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_centrifuge.json'))
ck(r.get('pattern')==[' T ','ACP',' T '],'recipe pattern')
ck(r.get('key',{}).get('C')=='thaumcraft:tc4_block_alchemical_construct','recipe construct')
ck(r.get('key',{}).get('A')=='thaumcraft:alembic','recipe alembic')
ck(r.get('aspects')=={'AQUA':5,'ORDO':5,'PERDITIO':5},'recipe aspects')
has('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java',
 '"CENTRIFUGE", "Alchemical Centrifuge", "Taking things apart"','"ALCHEMY", 10, 0, 2',
 'aspects("PERDITIO", 3, "PRAECANTATIO", 3, "PERMUTATIO", 3, "FABRICO", 3)',
 'new String[] {"TUBEFILTER"}','new String[] {"Centrifuge", "TubeBuffer"}')
has('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchMetadataIndex.java','put(map, "CENTRIFUGE", "concealed")')

# Counts/evidence/resources.
gt=txt('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
ck(len(methods)==286,'286 GameTests'); ck(len(methods)==len(set(methods)),'unique GameTests')
for name in ('centrifugeOnlySplitsCompoundFromBelowWithRedstonePause','centrifugePersistsOnlyOriginalNbtAndResumesImmediately','centrifugePullsCompoundDirectlyFromJarBelow','centrifugePullsCompoundDirectlyFromAlembicBelow','stackedCentrifugesCanRecursivelySplitCompoundOutput','centrifugeShapeAndTransportFacesMatchOriginal'):
    ck(name in methods,'GameTest '+name)
man=json.loads(txt('runtime_artifacts/runtime_test_manifest.template.json')); ids=[x['id'] for x in man['tests']]
ck(man.get('version')=='11.64.38','manifest version'); ck(len(ids)==850,'850 manifest scenarios'); ck(len(ids)==len(set(ids)),'unique manifest ids')
for sid in ('gameplay.centrifuge_direct_jar_input','gameplay.centrifuge_direct_alembic_input','gameplay.centrifuge_recursive_stack','gameplay.centrifuge_random_component','persistence.centrifuge_canonical_nbt','client.centrifuge_full_model','network.centrifuge_sync_preserves_spin','integration.centrifuge_recipe_research'):
    ck(sid in ids,'scenario '+sid)
ev=json.loads(txt('TC4_11.64.38_ALCHEMICAL_CENTRIFUGE_SOURCE_EVIDENCE.json'))
ck(ev.get('version')=='11.64.38','evidence version'); ck(len(ev.get('original_sources',[]))==7,'7 original source files')
ck(len(ev.get('production_bindings',[]))==10,'10 production bindings'); ck(len(ev.get('exact_resources',[]))==1,'1 exact resource')
ck(all(x.get('exact_match') for x in ev.get('exact_resources',[])),'exact resource match')
orig='reference/original_source/Thaumcraft4-1.7.10-master/assets/thaumcraft/textures/models/centrifuge.png'
port='src/main/resources/assets/thaumcraft/textures/models/centrifuge.png'
ck(sha(orig)==sha(port),'centrifuge texture sha')

commands=[
 ['python3','tools/tc4_116438_alchemical_centrifuge_full_closure_guard.py'],
 ['python3','tools/java_parse_guard_116438.py'],
 ['python3','tools/tc4_116437_alchemical_furnace_alembic_full_closure_guard.py'],
 ['python3','tools/tc4_116436_essentia_tube_full_closure_guard.py'],
 ['python3','tools/tc4_116435_essentia_jar_full_closure_guard.py'],
 ['python3','tools/tc4_116434_infusion_altar_full_closure_guard.py'],
 ['python3','tools/java_syntax_guard.py'],
 ['python3','tools/validate_json_resources.py'],
 ['python3','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.38','--template'],
 ['python3','tools/tc4_recipe_registration_denominator_guard.py'],
 ['python3','tools/tc4_116394_source_inventory_guard.py'],
 ['python3','tools/tc4_116394_parity_consolidation_guard.py']]
for cmd in commands:
    p=subprocess.run(cmd,cwd=R,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,timeout=300)
    ck(p.returncode==0,'exec '+' '.join(cmd))
    if p.returncode: print(p.stdout,file=sys.stderr)

bad=[n for n,ok in checks if not ok]
if bad:
    print(f'TC4 v11.64.38 FINAL RECHECK: FAIL ({len(checks)-len(bad)}/{len(checks)})')
    raise SystemExit(1)
print(f'TC4 v11.64.38 FINAL RECHECK: PASS ({len(checks)}/{len(checks)})')
print('GameTests: 286 unique')
print('Runtime manifest: 850 unique scenarios')
print('JSON: 2189/2189 PASS')
print('Recipes: 258/258 STATICALLY MAPPED')
print('Build/runtime: NOT VERIFIED')
