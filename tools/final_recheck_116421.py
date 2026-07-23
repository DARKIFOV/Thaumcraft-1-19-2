#!/usr/bin/env python3
from pathlib import Path
import json,re,subprocess
R=Path(__file__).resolve().parents[1]
checks=[]

def run(name, cmd):
    r=subprocess.run(cmd,cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    if r.returncode:
        print(f'FAIL | {name}')
        print(r.stdout)
        raise SystemExit(r.returncode)
    print(f'PASS | {name}')
    checks.append(name)

def req(name, cond, detail=''):
    if not cond:
        raise SystemExit(f'FAIL | {name}: {detail}')
    print(f'PASS | {name}')
    checks.append(name)

build=(R/'build.gradle').read_text(encoding='utf-8')
mods=(R/'src/main/resources/META-INF/mods.toml').read_text(encoding='utf-8')
req('version agreement', "version = '11.64.21'" in build and 'version="11.64.21"' in mods)
p1=(R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md')
p2=(R/'PROMPT_FOR_FUTURE_CHAT_RU.md')
req('mandatory prompt identity',p1.read_bytes()==p2.read_bytes())
prompt=p1.read_text(encoding='utf-8')
for token in ('Один релиз — один предмет или одна цельная механика','Упаковка архива без этого файла запрещена','SOURCE CLOSED','RESOURCE CLOSED','BUILD VERIFIED','RUNTIME VERIFIED'):
    req('mandatory prompt token '+token, token in prompt)
run('Arcane Pressure Plate full-closure guard',['python3','tools/tc4_116421_arcane_pressure_plate_full_closure_guard.py'])
run('Java 17 target self-test and targeted parse',['python3','tools/java_parse_guard_116421.py'])
gt=(R/'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java').read_text(encoding='utf-8')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req('GameTest uniqueness',len(methods)==183 and len(methods)==len(set(methods)),str(len(methods)))
manifest=json.loads((R/'runtime_artifacts/runtime_test_manifest.template.json').read_text(encoding='utf-8'))
ids=[x['id'] for x in manifest['tests']]
req('manifest uniqueness',manifest['version']=='11.64.21' and len(ids)==594 and len(ids)==len(set(ids)),f"{manifest['version']}/{len(ids)}")
run('JSON resources',['python3','tools/validate_json_resources.py'])
run('recipe denominator',['python3','tools/tc4_recipe_registration_denominator_guard.py'])
forbidden=[]
for p in R.rglob('*'):
    rel=p.relative_to(R)
    if any(part in {'build','.gradle','__pycache__'} for part in rel.parts) or (p.is_file() and p.suffix in {'.class','.pyc'}):
        forbidden.append(str(rel))
req('clean source tree',not forbidden,', '.join(forbidden[:10]))
report=R/'TC4_11.64.21_ARCANE_PRESSURE_PLATE_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md'
ev=R/'tools/data/tc4_arcane_pressure_plate_full_source_evidence_v11.64.21.json'
root_ev=R/'TC4_11.64.21_ARCANE_PRESSURE_PLATE_SOURCE_EVIDENCE.json'
req('report and evidence', report.is_file() and ev.is_file() and root_ev.is_file())
ev_data=json.loads(ev.read_text(encoding='utf-8'))
req('honest evidence statuses',ev_data.get('source_closure')=='CLOSED' and ev_data.get('resource_closure')=='CLOSED' and ev_data.get('build_status')=='NOT_OBTAINED' and ev_data.get('runtime_status')=='NOT_VERIFIED')
build_log=(R/'TC4_11.64.21_GRADLE_BUILD_ATTEMPT.log').read_text(encoding='utf-8',errors='replace')
exit_code=(R/'TC4_11.64.21_GRADLE_BUILD_EXITCODE.txt').read_text(encoding='utf-8').strip()
req('honest Gradle failure evidence','UnknownHostException: services.gradle.org' in build_log and 'BUILD SUCCESSFUL' not in build_log and exit_code=='1')
for f in ('TC4_11.64.21_BUILD_STATUS.txt','TC4_11.64.21_JAVA_PARSE_STATUS.txt','TC4_11.64.21_STATIC_CI_FINAL_SUMMARY.txt','TC4_11.64.21_FOCUSED_STATIC_CI_FINAL.log','TC4_11.64.21_ZIP_INTEGRITY.txt'):
    req('root artifact '+f,(R/f).is_file())
print(f'FINAL RECHECK v11.64.21: PASS ({len(checks)}/{len(checks)})')
