#!/usr/bin/env python3
from pathlib import Path
import json,re,subprocess,sys
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

build=(R/'build.gradle').read_text()
mods=(R/'src/main/resources/META-INF/mods.toml').read_text()
req('version agreement', "version = '11.64.20'" in build and 'version="11.64.20"' in mods)
req('mandatory prompt identity', (R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md').read_bytes()==(R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes())
run('Arcane Levitator full-closure guard',['python3','tools/tc4_116420_arcane_levitator_full_closure_guard.py'])
run('Java 17 self-test and targeted parse',['python3','tools/java_parse_guard_116420.py'])
gt=(R/'src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java').read_text()
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req('GameTest uniqueness',len(methods)==177 and len(methods)==len(set(methods)),str(len(methods)))
manifest=json.loads((R/'runtime_artifacts/runtime_test_manifest.template.json').read_text())
ids=[x['id'] for x in manifest['tests']]
req('manifest uniqueness',manifest['version']=='11.64.20' and len(ids)==579 and len(ids)==len(set(ids)),f"{manifest['version']}/{len(ids)}")
run('JSON resources',['python3','tools/validate_json_resources.py'])
run('recipe denominator',['python3','tools/tc4_recipe_registration_denominator_guard.py'])
forbidden=[]
for p in R.rglob('*'):
    rel=p.relative_to(R)
    if any(part in {'build','.gradle','__pycache__'} for part in rel.parts) or (p.is_file() and p.suffix in {'.class','.pyc'}):
        forbidden.append(str(rel))
req('clean source tree',not forbidden,', '.join(forbidden[:10]))
req('report and evidence', (R/'TC4_11.64.20_ARCANE_LEVITATOR_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md').is_file() and (R/'tools/data/tc4_arcane_levitator_full_source_evidence_v11.64.20.json').is_file())
print(f'FINAL RECHECK v11.64.20: PASS ({len(checks)}/{len(checks)})')
