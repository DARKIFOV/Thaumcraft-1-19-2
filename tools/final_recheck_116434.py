#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re,subprocess,sys
R=Path(__file__).resolve().parents[1]
passed=0
def txt(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def req(name,ok,detail=''):
    global passed
    if not ok:
        raise SystemExit(f'FINAL RECHECK v11.64.34: FAIL: {name}: {detail}')
    passed += 1

def run(name,cmd,needle):
    global passed
    r=subprocess.run(cmd,cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=300)
    if r.returncode or needle not in r.stdout:
        print(r.stdout)
        raise SystemExit(f'FINAL RECHECK v11.64.34: FAIL: {name}')
    passed += 1

req('build version', "version = '11.64.34'" in txt('build.gradle'))
req('mods version', 'version="11.64.34"' in txt('src/main/resources/META-INF/mods.toml'))
required=[
 'TC4_11.64.34_INFUSION_ALTAR_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md',
 'TC4_11.64.34_FULL_CLOSURE_STATUS_AND_PLAN_RU.md',
 'TC4_11.64.34_INFUSION_ALTAR_SOURCE_EVIDENCE.json',
 'tools/data/tc4_infusion_altar_full_source_evidence_v11.64.34.json',
 'TC4_11.64.34_FOCUSED_STATIC_CI_FINAL.log',
 'TC4_11.64.34_JAVA17_SELF_TEST.log',
 'TC4_11.64.34_GRADLE_BUILD_ATTEMPT.log',
 'TC4_11.64.34_BUILD_STATUS.txt',
 'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md',
 'PROMPT_FOR_FUTURE_CHAT_RU.md']
for p in required: req('file '+p,(R/p).is_file())
req('prompt byte identity',(R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md').read_bytes()==(R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes())
req('prompt mandatory wording','Файл `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен' in txt('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md'))

parity=txt('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionAltarFullClosureParity.java')
for token in ('CONTRACT_VERSION = "11.64.34"','RESEARCH_KEY = "INFUSION"','ALTAR_VIS_COST_PER_PRIMAL = 25','PRIMAL_ASPECT_COUNT = 6','MATRIX_LIGHT_LEVEL = 10','CRAFTING_BREAK_EXPLOSION_STRENGTH = 2.0F','CRAFT_CYCLE_INTERVAL = 10','ENCHANTMENT_XP_CYCLE_INTERVAL = 20','COMPONENT_TRAVEL_CYCLES = 5','MATRIX_ASPECTS_NBT = "Aspects"','MATRIX_RECIPE_INPUTS_NBT = "recipein"','MATRIX_RECIPE_OUTPUT_NBT = "recipeout"'):
    req('parity '+token,token in parity)

mult=txt('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionAltarMultiblock.java')
for token in ('PlayerThaumData.hasResearch','ALTAR_ORIGIN_SCAN_MIN','ALTAR_ORIGIN_SCAN_MAX','consumeAllVisAtomically','matrix.activateFromMultiblock()','setPillar(level'):
    req('multiblock '+token,token in mult)
req('single altar implementation',not (R/'src/main/java/com/darkifov/thaumcraft/infusion/InfusionAltarRitual.java').exists())
req('wand production binding','TC4InfusionAltarMultiblock.tryCreate' in txt('src/main/java/com/darkifov/thaumcraft/block/WandItem.java'))

matrix=txt('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java')
for token in ('tag.put("Aspects", aspects)','tag.put("recipein", recipeInputs)','tag.putString("rotype"','tag.put("recipeout"','tag.put("recipeinput"','tag.putInt("recipeinst"','tag.putInt("recipetype"','tag.putInt("recipexp"','tag.putString("recipeplayer"','tag.putString("recipeid"'):
    req('canonical matrix save '+token,token in matrix)
for forbidden in ('tag.put("PendingAspects"','tag.putString("PendingComponents"','tag.put("PendingComponentSpecList"','tag.put("LockedRecipeAspectTypes"','tag.put("LockedCatalystSnapshot"','tag.putString("LockedCatalystId"','tag.putInt("CountDelay"','tag.putInt("ItemCount"','tag.put("TravellingComponent"'):
    req('no non-original write '+forbidden,forbidden not in matrix)
req('migration reads retained','tag.contains("PendingAspects")' in matrix and 'tag.contains("LockedCatalystSnapshot")' in matrix)
req('invalid catalyst direct event','InfusionInstabilityEvents.triggerWeightedEvent' in matrix)
req('scheduler exact','shouldRunCraftCycle(count, countDelay)' in matrix)
req('no accelerator cadence','progress++;' in matrix and 'progress += Math.max' not in matrix)
req('component five cycles','itemCount = TC4InfusionAltarFullClosureParity.COMPONENT_TRAVEL_CYCLES' in matrix)
req('reload resets transient','itemCount = 0;' in matrix and 'travellingComponentSource = null;' in matrix)

ped=txt('src/main/java/com/darkifov/thaumcraft/blockentity/ArcanePedestalBlockEntity.java')
for token in ('PEDESTAL_ITEMS_NBT','PEDESTAL_SLOT_NBT','getSlots()','return 1;','ForgeCapabilities.ITEM_HANDLER','ClientboundBlockEntityDataPacket.create(this)'):
    req('pedestal '+token,token in ped)
pillar=txt('src/main/java/com/darkifov/thaumcraft/block/InfusionPillarBlock.java')
for token in ('DoubleBlockHalf.LOWER','DoubleBlockHalf.UPPER','level.destroyBlock(partnerPos, true)','case 2 -> Direction.SOUTH','case 5 -> Direction.NORTH'):
    req('pillar '+token,token in pillar)

methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',txt('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java'),re.S)
req('GameTest count',len(methods)==267,str(len(methods)))
req('GameTest unique',len(methods)==len(set(methods)))
for name in ('infusionAltarBlueprintAndVisCostMatchTc4','infusionAltarWandOriginSearchAndResearchMatchTc4','infusionPillarOrientationsAndDropsMatchTc4','infusionMatrixSchedulerMatchesTc4','infusionComponentTravelUsesFiveCraftCycles','infusionPedestalNbtAndComparatorUseProductionPath','infusionPedestalAndStabilizerScanBoundsMatchTc4','infusionMatrixLightAndCraftingBreakExplosionMatchTc4'):
    req('GameTest '+name,name in methods)

manifest=json.loads(txt('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req('manifest version',manifest.get('version')=='11.64.34')
req('manifest count',len(ids)==793,str(len(ids)))
req('manifest unique',len(ids)==len(set(ids)))
for sid in ('gameplay.infusion_altar_raw_blueprint','gameplay.infusion_altar_vis_atomic_cost','persistence.infusion_pedestal_items_slot_nbt','gameplay.infusion_matrix_scheduler','gameplay.infusion_component_five_cycles','gameplay.infusion_matrix_break_explosion','multiplayer.infusion_server_authority','persistence.infusion_full_cycle_reload'):
    req('scenario '+sid,sid in ids)

for p in ('TC4_11.64.34_INFUSION_ALTAR_SOURCE_EVIDENCE.json','tools/data/tc4_infusion_altar_full_source_evidence_v11.64.34.json'):
    evidence=json.loads(txt(p))
    req('evidence version '+p,evidence.get('version')=='11.64.34')
    req('evidence originals '+p,len(evidence.get('original_sources',[]))==13)
    req('evidence production '+p,len(evidence.get('production_contracts',[]))==16)

req('focused CI log','FOCUSED STATIC CI v11.64.34: PASS (52/52)' in txt('TC4_11.64.34_FOCUSED_STATIC_CI_FINAL.log'))
req('self-test log','TC4InfusionAltarFullClosureSelfTest: PASS' in txt('TC4_11.64.34_JAVA17_SELF_TEST.log'))
gradle=txt('TC4_11.64.34_GRADLE_BUILD_ATTEMPT.log')
req('gradle attempted','$ ./gradlew build --no-daemon --console=plain' in gradle)
req('gradle honest failure','UnknownHostException: services.gradle.org' in gradle and 'EXIT_CODE=1' in gradle)
status=txt('TC4_11.64.34_BUILD_STATUS.txt')
for token in ('SOURCE CLOSED: YES','RESOURCE CLOSED: YES','BUILD VERIFIED: NO','RUNTIME VERIFIED: NO','JAR CREATED: NO'):
    req('status '+token,token in status)
req('start current','АКТУАЛЬНАЯ ВЕРСИЯ: 11.64.34' in txt('00_START_HERE_RU.txt'))
req('README current',txt('README.md').startswith('# Thaumcraft Legacy Rebuild — v11.64.34'))
req('status current','# TC4 PORT STATUS V3 — v11.64.34' in txt('TC4_PORT_STATUS_V3.md'))
req('plan closed 22','22. **v11.64.34' in txt('TC4_11.64.34_FULL_CLOSURE_STATUS_AND_PLAN_RU.md'))
req('known boundary','## 11.64.34 Infusion altar runtime boundary' in txt('KNOWN_DEVIATIONS.md'))
report=txt('TC4_11.64.34_INFUSION_ALTAR_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md')
for token in ('Focused static CI: 52/52 PASS','GameTest declarations: 267 unique','Runtime manifest: 793 unique scenarios','JSON: 2191/2191 PASS','Recipes: 258/258 STATICALLY MAPPED'):
    req('report '+token,token in report)

run('full closure guard',['python3','tools/tc4_116434_infusion_altar_full_closure_guard.py'],'PASS')
run('Java parse',['python3','tools/java_parse_guard_116434.py'],'PASS')
run('Java syntax',['python3','tools/java_syntax_guard.py'],'OK')
run('JSON',['python3','tools/validate_json_resources.py'],'2191 files')
run('manifest',['python3','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.34','--template'],'793 tests')
run('recipes',['python3','tools/tc4_recipe_registration_denominator_guard.py'],'258/258')

# javac may emit an argument file while intentionally parsing Forge sources;
# remove that transient proof artifact before packaging.
for generated in R.glob('javac.*.args'):
    generated.unlink()
strays=[]
for pattern in ('*.class','*.pyc','javac.*.args'):
    strays.extend(p for p in R.rglob(pattern) if 'reference/' not in p.as_posix())
strays.extend(p for p in R.rglob('__pycache__') if 'reference/' not in p.as_posix())
req('no generated strays',not strays,', '.join(str(x.relative_to(R)) for x in strays[:20]))
print(f'FINAL RECHECK v11.64.34: PASS ({passed}/{passed})')
