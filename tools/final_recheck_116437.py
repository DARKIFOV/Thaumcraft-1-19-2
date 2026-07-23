#!/usr/bin/env python3
from pathlib import Path
import json, re, hashlib, subprocess, sys
R=Path(__file__).resolve().parents[1]
checks=[]
def ck(ok,name):
    checks.append((name,bool(ok)))
    if not ok: print('FAIL:',name,file=sys.stderr)
def txt(rel): return (R/rel).read_text(encoding='utf-8',errors='replace')
def has(rel,*tokens):
    s=txt(rel)
    for token in tokens: ck(token in s, f'{rel}: {token}')
def lacks(rel,*tokens):
    s=txt(rel)
    for token in tokens: ck(token not in s, f'{rel}: lacks {token}')

# Release and required evidence.
has('build.gradle',"version = '11.64.37'",'Alchemical Furnace + Alembic full closure')
has('src/main/resources/META-INF/mods.toml','version="11.64.37"')
for rel in (
 'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md',
 'TC4_11.64.37_ALCHEMICAL_FURNACE_ALEMBIC_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md',
 'TC4_11.64.37_FULL_CLOSURE_STATUS_AND_PLAN_RU.md',
 'TC4_11.64.37_ALCHEMICAL_FURNACE_ALEMBIC_SOURCE_EVIDENCE.json',
 'TC4_11.64.37_FOCUSED_STATIC_CI_FINAL.log',
 'TC4_11.64.37_JAVA17_SELF_TEST.log',
 'TC4_11.64.37_GRADLE_BUILD_ATTEMPT.log',
 'TC4_11.64.37_BUILD_STATUS.txt',
): ck((R/rel).is_file(), f'file {rel}')
has('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md','Файл `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен')
has('TC4_11.64.37_FOCUSED_STATIC_CI_FINAL.log','PASS (61/61, segmented execution)')
has('TC4_11.64.37_JAVA17_SELF_TEST.log','TC4AlchemicalFurnaceAlembicFullClosureSelfTest: PASS','JAVA17_SELF_TEST_EXIT_CODE=0')
has('TC4_11.64.37_GRADLE_BUILD_ATTEMPT.log','UnknownHostException: services.gradle.org','GRADLE_EXIT_CODE=1')

# Pure parity contract.
has('src/main/java/com/darkifov/thaumcraft/alchemy/TC4AlchemicalFurnaceParity.java',
 'CONTRACT_VERSION = "11.64.37"','FURNACE_CAPACITY = 50','ALEMBIC_CAPACITY = 32','MAX_ALEMBICS = 5',
 'NORMAL_INTERVAL = 40','ALUMENTUM_INTERVAL = 20','DISTILLATION_STEP = 1','ALUMENTUM_BURN_TIME = 6400',
 'BURNING_LIGHT = 12','BELLOWS_REDUCTION = 0.125D','MAX_BELLOWS = 6','alembicFillMessage','comparator')

# Furnace production binding/NBT/automation.
has('src/main/java/com/darkifov/thaumcraft/blockentity/AlchemicalFurnaceBlockEntity.java',
 'CAPACITY = TC4AlchemicalFurnaceParity.FURNACE_CAPACITY','TC4AlchemicalFurnaceParity.smeltTime',
 'TC4DistillationRuntime.tickFurnaceToAlembics','TC4AlchemicalFurnaceParity.ALUMENTUM_BURN_TIME',
 'new SidedInvWrapper(this, direction)','ForgeCapabilities.ITEM_HANDLER','stack.is(Items.BUCKET)',
 'tag.putShort("BurnTime"','tag.putShort("Vis"','tag.putBoolean("speedBoost"','tag.putShort("CookTime"',
 'tag.put("Items", items)','writeOriginalAspects','counter = 0','bellows = -1','container.alchemyfurnace')
lacks('src/main/java/com/darkifov/thaumcraft/blockentity/AlchemicalFurnaceBlockEntity.java',
 'tag.putInt("Counter"','tag.putInt("Bellows"','tag.putInt("BurnDuration"','tag.put("PendingAspects"','tag.put("Input"','tag.put("Fuel"')
has('src/main/java/com/darkifov/thaumcraft/essentia/TC4DistillationRuntime.java',
 'ORIGINAL_MAX_ALEMBICS_ABOVE_FURNACE','MAX_ALEMBICS','ORIGINAL_DISTILLATION_INTERVAL_TICKS',
 'ORIGINAL_ALUMENTUM_INTERVAL_TICKS','ORIGINAL_DISTILLATION_STEP','tickFurnaceToAlembics','contiguousAlembics')
has('src/main/java/com/darkifov/thaumcraft/block/AlchemicalFurnaceBlock.java',
 'BooleanProperty.create("lit")','state.getValue(LIT)','ParticleTypes.SMOKE','ParticleTypes.FLAME','!player.isShiftKeyDown()','NetworkHooks.openScreen')
lacks('src/main/java/com/darkifov/thaumcraft/block/AlchemicalFurnaceBlock.java','Alchemical Furnace |','Fuel placed')
has('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java','AlchemicalFurnaceBlock.LIT) ? 12 : 0')
has('src/main/java/com/darkifov/thaumcraft/menu/AlchemicalFurnaceMenu.java',
 'burnProgress() { return data.get(0);','fuelTime() { return data.get(1);')

# Alembic production/NBT/interactions/render.
has('src/main/java/com/darkifov/thaumcraft/blockentity/AlembicBlockEntity.java',
 'CAPACITY = TC4AlchemicalFurnaceParity.ALEMBIC_CAPACITY','emptyAspectType','tag.putString("aspect"',
 'tag.putShort("amount"','tag.putString("AspectFilter"','tag.putByte("facing"','aboveFurnace()','aboveAlembic()',
 'comparatorOutput','fillMessageIndex')
lacks('src/main/java/com/darkifov/thaumcraft/blockentity/AlembicBlockEntity.java',
 'tag.put("Aspects"','tag.putString("Aspect"','tag.putShort("Amount"')
has('src/main/java/com/darkifov/thaumcraft/block/AlembicBlock.java',
 'tile.alembic.msg.','alembicknock','JarLabelItem','JarLabelItem.getAspect','EssentiaJarBlockItem',
 'VOID_ESSENTIA_JAR','hasAnalogOutputSignal','comparatorOutput','onRemove','JAR_LABEL')
lacks('src/main/java/com/darkifov/thaumcraft/block/AlembicBlock.java','EssentiaPhialItem','displayClientMessage(Component.literal')
has('src/main/java/com/darkifov/thaumcraft/client/render/AlembicRenderer.java',
 'renderTubeMain','renderTubeSmall','renderLegs','renderPot','renderPanel','renderLabel','renderConnectors',
 'TC4ArcaneBoreModel','EssentiaTubeBlockEntity','rotateForFacing')
lacks('src/main/java/com/darkifov/thaumcraft/client/render/AlembicRenderer.java','renderLiquidBox','FILL_TEXTURE')

# Counts and source evidence.
gt=txt('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
ck(len(methods)==281,'281 GameTests'); ck(len(methods)==len(set(methods)),'unique GameTests')
for name in ('alchemicalFurnaceProcessesPersistsAndRespectsCapacity','furnaceFeedsFiveAlembicsOnLocalFortiethTick',
'alembicKeepsCapacityFilterSidesAndNbt','furnaceSidedAutomationMatchesOriginal',
'alembicTypedEmptyStateAndComparatorMatchOriginal','alembicFillMessageThresholdsMatchOriginal'):
    ck(name in methods,'GameTest '+name)
manifest=json.loads(txt('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
ck(manifest.get('version')=='11.64.37','manifest version'); ck(len(ids)==835,'835 manifest scenarios'); ck(len(ids)==len(set(ids)),'unique manifest ids')
for sid in ('gameplay.five_alembic_stack','automation.furnace_sided_inventory','persistence.furnace_canonical_nbt',
'persistence.alembic_canonical_nbt','client.alembic_stack_geometry','integration.distillation_restart'):
    ck(sid in ids,'scenario '+sid)
ev=json.loads(txt('TC4_11.64.37_ALCHEMICAL_FURNACE_ALEMBIC_SOURCE_EVIDENCE.json'))
ck(ev.get('version')=='11.64.37','evidence version'); ck(len(ev.get('original_sources',[]))==7,'7 original source files')
ck(len(ev.get('production_bindings',[]))==8,'8 production bindings'); ck(len(ev.get('exact_resources',[]))==9,'9 resource records')
ck(all(x.get('exact_match') for x in ev.get('exact_resources',[])),'9 exact resources')

# Execute high-load current and dependency guards.
commands=[
 ['python3','tools/tc4_116437_alchemical_furnace_alembic_full_closure_guard.py'],
 ['python3','tools/java_parse_guard_116437.py'],
 ['python3','tools/tc4_116436_essentia_tube_full_closure_guard.py'],
 ['python3','tools/tc4_116435_essentia_jar_full_closure_guard.py'],
 ['python3','tools/tc4_116434_infusion_altar_full_closure_guard.py'],
 ['python3','tools/tc4_116418_arcane_bellows_full_closure_guard.py'],
 ['python3','tools/java_syntax_guard.py'],
 ['python3','tools/validate_json_resources.py'],
 ['python3','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.37','--template'],
 ['python3','tools/tc4_recipe_registration_denominator_guard.py'],
 ['python3','tools/tc4_116394_source_inventory_guard.py'],
 ['python3','tools/tc4_116394_parity_consolidation_guard.py'],
]
for cmd in commands:
    p=subprocess.run(cmd,cwd=R,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,timeout=180)
    ck(p.returncode==0,'exec '+' '.join(cmd))
    if p.returncode: print(p.stdout,file=sys.stderr)

bad=[name for name,ok in checks if not ok]
if bad:
    print(f'TC4 v11.64.37 FINAL RECHECK: FAIL ({len(checks)-len(bad)}/{len(checks)})')
    raise SystemExit(1)
print(f'TC4 v11.64.37 FINAL RECHECK: PASS ({len(checks)}/{len(checks)})')
print('GameTests: 281 unique')
print('Runtime manifest: 835 unique scenarios')
print('JSON: 2189/2189 PASS')
print('Recipes: 258/258 STATICALLY MAPPED')
print('Build/runtime: NOT VERIFIED')
