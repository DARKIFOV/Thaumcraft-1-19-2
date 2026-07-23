#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re,subprocess,sys
R=Path(__file__).resolve().parents[1]
checks=0

def req(ok,msg):
    global checks
    checks += 1
    if not ok:
        raise SystemExit(f'TC4 v11.64.35 final recheck: FAIL [{checks}] {msg}')

def txt(p): return (R/p).read_text(encoding='utf-8',errors='replace')
def run(cmd, token):
    p=subprocess.run(cmd,cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=360)
    req(p.returncode==0,f'command failed: {" ".join(cmd)}\n{p.stdout}')
    req(token in p.stdout,f'command token missing: {token}\n{p.stdout}')

run(['python3','tools/tc4_116435_essentia_jar_full_closure_guard.py'],'PASS (274 GameTests, 807 scenarios, 14 exact assets)')
run(['python3','tools/java_parse_guard_116435.py'],'PASS (Java 17 self-test + 15 Forge parse checks)')
run(['python3','tools/tc4_116355_essentia_storage_mirror_runtime_guard.py'],'PASS')
run(['python3','tools/tc4_116356_essentia_tube_transport_runtime_guard.py'],'PASS')
run(['python3','tools/tc4_116372_essentia_parity_guard.py'],'PASS')
run(['python3','tools/validate_json_resources.py'],'JSON resource validation: OK (2189 files)')
run(['python3','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.35','--template'],'runtime manifest: PASS (807 tests; template=True)')
run(['python3','tools/tc4_recipe_registration_denominator_guard.py'],'258/258 STATICALLY MAPPED')
run(['python3','tools/java_syntax_guard.py'],'Java syntax guard: OK')

req("version = '11.64.35'" in txt('build.gradle'),'build version')
req('version="11.64.35"' in txt('src/main/resources/META-INF/mods.toml'),'mods version')
req('FOCUSED STATIC CI v11.64.35: PASS (57/57)' in txt('TC4_11.64.35_FOCUSED_STATIC_CI_FINAL.log'),'focused CI final log')
req('TC4 essentia jar full-closure self-test: PASS' in txt('TC4_11.64.35_JAVA17_SELF_TEST.log'),'Java 17 self-test log')
req('GRADLE_EXIT_CODE=1' in txt('TC4_11.64.35_GRADLE_BUILD_ATTEMPT.log'),'Gradle exit code')
req('UnknownHostException: services.gradle.org' in txt('TC4_11.64.35_GRADLE_BUILD_ATTEMPT.log'),'Gradle blocker')
for token in ('SOURCE_CLOSED=YES','RESOURCE_CLOSED=YES','BUILD_VERIFIED=NO','RUNTIME_VERIFIED=NO','JAR_CREATED=NO'):
    req(token in txt('TC4_11.64.35_BUILD_STATUS.txt'),'status '+token)

required=[
'TC4_11.64.35_ESSENTIA_JAR_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md',
'TC4_11.64.35_FULL_CLOSURE_STATUS_AND_PLAN_RU.md',
'TC4_11.64.35_ESSENTIA_JAR_SOURCE_EVIDENCE.json',
'tools/data/tc4_essentia_jar_full_source_evidence_v11.64.35.json',
'TC4_11.64.35_FOCUSED_STATIC_CI_FINAL.log',
'TC4_11.64.35_JAVA17_SELF_TEST.log',
'TC4_11.64.35_GRADLE_BUILD_ATTEMPT.log',
'TC4_11.64.35_BUILD_STATUS.txt',
'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md',
'PROMPT_FOR_FUTURE_CHAT_RU.md']
for p in required: req((R/p).is_file(),f'missing {p}')
req((R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md').read_bytes()==(R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes(),'prompt copies differ')
req('Файл `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен' in txt('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md'),'mandatory prompt wording')

for p in ('README.md','00_START_HERE_RU.txt','TC4_PORT_STATUS_V3.md'):
    req('11.64.35' in txt(p),f'{p} current version')
req('23. **v11.64.35' in txt('TC4_11.64.35_FULL_CLOSURE_STATUS_AND_PLAN_RU.md'),'closure registry entry')
req('Essentia tubes, valves и buffers' in txt('TC4_11.64.35_FULL_CLOSURE_STATUS_AND_PLAN_RU.md'),'next object')

for p in ('src/main/resources/data/thaumcraft/recipes/essentia_jar.json','src/main/resources/data/thaumcraft/recipes/void_essentia_jar.json','src/main/resources/data/thaumcraft/recipes/filtered_essentia_jar.json'):
    req(not (R/p).exists(),f'wrong recipe still exists: {p}')
hidden=json.loads(txt('src/main/resources/data/c/tags/items/hidden_from_recipe_viewers.json'))
req('thaumcraft:filtered_essentia_jar' in hidden.get('values',[]),'filtered alias hidden tag')

manifest=json.loads(txt('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(manifest.get('version')=='11.64.35','manifest version')
req(len(ids)==807,'manifest count')
req(len(ids)==len(set(ids)),'manifest unique')
scenario_ids=[
'gameplay.essentia_jar_capacity_suction','gameplay.void_jar_overflow_suction',
'gameplay.jar_top_side_transport','gameplay.jar_five_tick_pull',
'gameplay.jar_label_apply_remove','gameplay.jar_sneak_empty',
'gameplay.jar_phial_atomic_transfer','persistence.jar_block_entity_nbt',
'persistence.filled_jar_item_nbt','persistence.jar_legacy_migration',
'integration.hover_harness_jar_nbt','client.jar_liquid_label_renderer',
'client.filled_jar_item_renderer','integration.filtered_jar_alias_hidden']
for sid in scenario_ids: req(sid in ids,'scenario '+sid)

gt=txt('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)==274,'GameTest count')
req(len(methods)==len(set(methods)),'GameTest unique')
for name in ('essentiaJarCanonicalNbtMatchesOriginal','voidJarSuctionAndOverflowMatchOriginal','labelledJarRetainsTypeWhenEmptied','filledJarItemUsesRootTc4Nbt','jarFacingAndPhialBoundariesMatchOriginal','jarLabelUsesOriginalZeroAmountAspectList','hoverHarnessKeepsOriginalJarItemNbt'):
    req(name in methods,'GameTest '+name)

p1=R/'TC4_11.64.35_ESSENTIA_JAR_SOURCE_EVIDENCE.json'; p2=R/'tools/data/tc4_essentia_jar_full_source_evidence_v11.64.35.json'
req(p1.read_bytes()==p2.read_bytes(),'evidence copies differ')
ev=json.loads(p1.read_text(encoding='utf-8'))
req(ev.get('version')=='11.64.35','evidence version')
req(len(ev.get('original_sources',[]))==10,'evidence original count')
req(len(ev.get('production_contracts',[]))==15,'evidence production count')
for entry in ev.get('original_sources',[]): req(bool(entry.get('path')),'evidence original path')
for entry in ev.get('production_contracts',[]): req(bool(entry.get('path')),'evidence production path')

print(f'TC4 v11.64.35 final recheck: PASS ({checks}/{checks})')
