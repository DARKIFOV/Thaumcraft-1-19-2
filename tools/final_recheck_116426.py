#!/usr/bin/env python3
from pathlib import Path
import json,re,subprocess,hashlib
R=Path(__file__).resolve().parents[1]; passed=[]
def run(name,cmd,timeout=300):
 q=subprocess.run(cmd,cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=timeout)
 if q.returncode:
  print('FAIL | '+name); print(q.stdout); raise SystemExit(q.returncode)
 print('PASS | '+name); passed.append(name)
def req(name,ok,detail=''):
 if not ok: raise SystemExit(f'FAIL | {name}: {detail}')
 print('PASS | '+name); passed.append(name)
b=(R/'build.gradle').read_text();m=(R/'src/main/resources/META-INF/mods.toml').read_text()
req('version agreement',"version = '11.64.26'" in b and 'version="11.64.26"' in m)
p1=R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md';p2=R/'PROMPT_FOR_FUTURE_CHAT_RU.md'
req('mandatory prompt identity',p1.read_bytes()==p2.read_bytes())
for token in ('Один релиз — один предмет или одна цельная механика','Упаковка архива без этого файла запрещена','SOURCE CLOSED','RESOURCE CLOSED','BUILD VERIFIED','RUNTIME VERIFIED'):
 req('prompt token '+token,token in p1.read_text())
run('Thaumonomicon full-closure guard',['python3','tools/tc4_116426_thaumonomicon_full_closure_guard.py'])
run('Thaumometer cumulative guard',['python3','tools/tc4_116425_thaumometer_full_closure_guard.py'])
run('Java 17 self-test and targeted parse',['python3','tools/java_parse_guard_116426.py'])
run('Java syntax guard',['python3','tools/java_syntax_guard.py'])
run('JSON resources',['python3','tools/validate_json_resources.py'])
run('runtime manifest',['python3','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.26','--template'])
run('recipe denominator',['python3','tools/tc4_recipe_registration_denominator_guard.py'])
gt=(R/'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java').read_text();methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req('GameTest uniqueness',len(methods)==218 and len(methods)==len(set(methods)),str(len(methods)))
man=json.loads((R/'runtime_artifacts/runtime_test_manifest.template.json').read_text());ids=[x['id'] for x in man['tests']]
req('manifest uniqueness',man['version']=='11.64.26' and len(ids)==672 and len(ids)==len(set(ids)),f"{man['version']}/{len(ids)}")
req('external acquisition scenario','integration.thaumonomicon_external_loot_contract' in ids)
forbidden=[]
for p in R.rglob('*'):
 rel=p.relative_to(R)
 if any(x in {'build','.gradle','__pycache__'} for x in rel.parts) or (p.is_file() and p.suffix in {'.class','.pyc'}):forbidden.append(str(rel))
req('clean source tree',not forbidden,', '.join(forbidden[:10]))
report=R/'TC4_11.64.26_THAUMONOMICON_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md';ev=R/'tools/data/tc4_thaumonomicon_full_source_evidence_v11.64.26.json';root_ev=R/'TC4_11.64.26_THAUMONOMICON_SOURCE_EVIDENCE.json'
req('report and evidence',report.is_file() and ev.is_file() and root_ev.is_file())
req('evidence copies identity',ev.read_bytes()==root_ev.read_bytes())
e=json.loads(ev.read_text())
req('honest statuses',e['source_closure']=='CLOSED_WITH_EXPLICIT_EXTERNAL_ACQUISITION_BOUNDARIES' and e['resource_closure']=='CLOSED' and e['build_status']=='NOT_OBTAINED' and e['runtime_status']=='NOT_VERIFIED')
req('evidence counts',e['static_proof']['gametests']==218 and e['static_proof']['runtime_scenarios']==672 and e['static_proof']['forge_parse_files']==16)
log=(R/'TC4_11.64.26_GRADLE_BUILD_ATTEMPT.log').read_text(errors='replace');code=(R/'TC4_11.64.26_GRADLE_BUILD_EXITCODE.txt').read_text().strip()
req('honest Gradle evidence','UnknownHostException: services.gradle.org' in log and 'BUILD SUCCESSFUL' not in log and code=='1')
req('required Java evidence','openjdk version "21.0.10"' in log and 'проект требует JDK 17' in report.read_text())
ci=(R/'TC4_11.64.26_FOCUSED_STATIC_CI_FINAL.log').read_text(errors='replace')
req('focused CI evidence','FOCUSED STATIC CI v11.64.26: PASS (40/40)' in ci)
normal=(R/'src/main/java/com/darkifov/thaumcraft/block/ThaumonomiconItem.java').read_text()
req('normal open sync','repairCompletedSiblingsOnBookOpen' in normal and 'syncResearch' in normal and 'syncAspectKnowledge' in normal)
cheat=(R/'src/main/java/com/darkifov/thaumcraft/block/CreativeThaumonomiconItem.java').read_text()
req('Cheat Sheet exact pool','originalEntries()' in cheat and 'CHEAT_ASPECT_POOL' in cheat and 'displayClientMessage' not in cheat)
state=(R/'src/main/java/com/darkifov/thaumcraft/client/screen/TC4ThaumonomiconClientState.java').read_text()
req('single shared browser state','EnumMap' not in state and 'Map<' not in state and 'DEFAULT_PAN_X' in state and 'DEFAULT_PAN_Y' in state)
page=(R/'src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java').read_text()
req('page history/even spread','spreadStart(initialPage)' in page and 'TC4ThaumonomiconPageHistory.push' in page and 'TC4ThaumonomiconPageHistory.pop' in page)
wand=(R/'src/main/java/com/darkifov/thaumcraft/block/WandItem.java').read_text();segment=wand[wand.index('if (state.is(Blocks.BOOKSHELF))'):wand.index('if (state.is(ThaumcraftMod.TABLE.get()))')]
req('bookshelf no-vis transform','SpecialItemEntity' in segment and 'Vec3.ZERO' in segment and 'consumeTransformationCost' not in segment and 'Aspect.ORDO' not in segment)
loot=(R/'src/main/java/com/darkifov/thaumcraft/research/TC4ThaumonomiconLootParity.java').read_text()
req('exact external loot contract','WIZARD_TOWER_WEIGHT = 20' in loot and 'RARE_CHEST_WEIGHT = 1' in loot and loot.count('"stronghold')>=3)
req('no fabricated book messages','Opening the Thaumonomicon' not in normal and 'Research completed:' not in normal+cheat)
req('source/resource status in report','SOURCE CLOSED: YES' in report.read_text() and 'RESOURCE CLOSED: YES' in report.read_text())
req('external blocker in report','EXTERNAL ACQUISITION INTEGRATION: BLOCKED' in report.read_text())
req('build/runtime status in report','BUILD VERIFIED: NO' in report.read_text() and 'RUNTIME VERIFIED: NO' in report.read_text())
req('README current',(R/'README.md').read_text().startswith('# v11.64.26'))
req('known deviations current',(R/'KNOWN_DEVIATIONS.md').read_text().startswith('# v11.64.26'))
req('status current',(R/'TC4_PORT_STATUS_V3.md').read_text().startswith('# TC4 PORT STATUS V3 — v11.64.26'))
for res in e['resources']:
 req('resource '+Path(res['port']).name,res['byte_identical'] and hashlib.sha256((R/res['port']).read_bytes()).hexdigest()==res['sha256'])
print(f'FINAL RECHECK v11.64.26: PASS ({len(passed)}/{len(passed)})')
