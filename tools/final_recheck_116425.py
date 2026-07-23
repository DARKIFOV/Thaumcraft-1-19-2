#!/usr/bin/env python3
from pathlib import Path
import json,re,subprocess
R=Path(__file__).resolve().parents[1]; passed=[]
def run(name,cmd,timeout=240):
 q=subprocess.run(cmd,cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=timeout)
 if q.returncode:
  print('FAIL | '+name); print(q.stdout); raise SystemExit(q.returncode)
 print('PASS | '+name); passed.append(name)
def req(name,ok,detail=''):
 if not ok: raise SystemExit(f'FAIL | {name}: {detail}')
 print('PASS | '+name); passed.append(name)
b=(R/'build.gradle').read_text(); m=(R/'src/main/resources/META-INF/mods.toml').read_text()
req('version agreement',"version = '11.64.25'" in b and 'version="11.64.25"' in m)
p1=R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md'; p2=R/'PROMPT_FOR_FUTURE_CHAT_RU.md'
req('mandatory prompt identity',p1.read_bytes()==p2.read_bytes())
for token in ('Один релиз — один предмет или одна цельная механика','Упаковка архива без этого файла запрещена','SOURCE CLOSED','RESOURCE CLOSED','BUILD VERIFIED','RUNTIME VERIFIED'):
 req('prompt token '+token,token in p1.read_text())
run('Thaumometer full-closure guard',['python3','tools/tc4_116425_thaumometer_full_closure_guard.py'])
run('historical Thaumometer guard',['python3','tools/tc4_116395_thaumometer_parity_guard.py'])
run('legacy Thaumometer scan guard',['python3','tools/thaumometer_scan_guard.py'])
run('Java 17 self-test and targeted parse',['python3','tools/java_parse_guard_116425.py'])
run('Java syntax guard',['python3','tools/java_syntax_guard.py'])
run('JSON resources',['python3','tools/validate_json_resources.py'])
run('runtime manifest',['python3','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.25','--template'])
run('recipe denominator',['python3','tools/tc4_recipe_registration_denominator_guard.py'])
gt=(R/'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java').read_text()
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req('GameTest uniqueness',len(methods)==211 and len(methods)==len(set(methods)),str(len(methods)))
man=json.loads((R/'runtime_artifacts/runtime_test_manifest.template.json').read_text()); ids=[x['id'] for x in man['tests']]
req('manifest uniqueness',man['version']=='11.64.25' and len(ids)==655 and len(ids)==len(set(ids)),f"{man['version']}/{len(ids)}")
req('phenomena integration scenario','integration.thaumometer_scan_event_handler' in ids)
forbidden=[]
for p in R.rglob('*'):
 rel=p.relative_to(R)
 if any(x in {'build','.gradle','__pycache__'} for x in rel.parts) or (p.is_file() and p.suffix in {'.class','.pyc'}): forbidden.append(str(rel))
req('clean source tree',not forbidden,', '.join(forbidden[:10]))
report=R/'TC4_11.64.25_THAUMOMETER_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md'
ev=R/'tools/data/tc4_thaumometer_full_source_evidence_v11.64.25.json'; root_ev=R/'TC4_11.64.25_THAUMOMETER_SOURCE_EVIDENCE.json'
req('report and evidence',report.is_file() and ev.is_file() and root_ev.is_file())
req('evidence copies identity',ev.read_bytes()==root_ev.read_bytes())
e=json.loads(ev.read_text())
req('honest statuses',e['source_closure']=='CLOSED' and e['resource_closure']=='CLOSED' and e['build_status']=='NOT_OBTAINED' and e['runtime_status']=='NOT_VERIFIED')
req('evidence counts',e['static_proof']['gametests']==211 and e['static_proof']['runtime_scenarios']==655 and e['static_proof']['forge_parse_files']==16)
log=(R/'TC4_11.64.25_GRADLE_BUILD_ATTEMPT.log').read_text(errors='replace'); code=(R/'TC4_11.64.25_GRADLE_BUILD_EXITCODE.txt').read_text().strip()
req('honest Gradle evidence','UnknownHostException: services.gradle.org' in log and 'BUILD SUCCESSFUL' not in log and code=='1')
req('required Java evidence','openjdk version "21.0.10"' in log and 'требует JDK 17' in report.read_text())
ci=(R/'TC4_11.64.25_FOCUSED_STATIC_CI_FINAL.log').read_text(errors='replace')
req('focused CI evidence','FOCUSED STATIC CI v11.64.25: PASS (38/38)' in ci)
rt=(R/'src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerPhenomenaRegistry.java').read_text()
target=(R/'src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerTargeting.java').read_text()
req('phenomena registry production link','interface Handler' in rt and 'first non-null phenomenon wins' in rt and 'TC4ThaumometerPhenomenaRegistry.find(player, scanner, partialTick)' in target)
progress=(R/'src/main/java/com/darkifov/thaumcraft/research/OriginalResearchProgression.java').read_text()
req('silent clue path','Research discovered:' not in progress and 'displayClientMessage' not in progress and '"@"+chosen.key()' in progress)
renderer=(R/'src/main/java/com/darkifov/thaumcraft/client/render/ThaumometerItemRenderer.java').read_text()
req('no fabricated unknown marker','renderQuestionMark(' not in renderer)
req('source/resource status in report','SOURCE CLOSED: YES' in report.read_text() and 'RESOURCE CLOSED: YES' in report.read_text())
req('build/runtime status in report','BUILD VERIFIED: NO' in report.read_text() and 'RUNTIME VERIFIED: NO' in report.read_text())
req('README current',(R/'README.md').read_text().startswith('# v11.64.25'))
req('known deviations current',(R/'KNOWN_DEVIATIONS.md').read_text().startswith('# v11.64.25'))
print(f'FINAL RECHECK v11.64.25: PASS ({len(passed)}/{len(passed)})')
