#!/usr/bin/env python3
from pathlib import Path
import json,re,subprocess,sys
R=Path(__file__).resolve().parents[1]
checks=[]
def req(ok,msg):
    checks.append((bool(ok),msg))
def text(p): return (R/p).read_text(encoding='utf-8',errors='replace')
# Required release files
required=[
'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md','TC4_11.64.36_ESSENTIA_TUBE_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md',
'TC4_11.64.36_FULL_CLOSURE_STATUS_AND_PLAN_RU.md','TC4_11.64.36_ESSENTIA_TUBE_SOURCE_EVIDENCE.json',
'TC4_11.64.36_FOCUSED_STATIC_CI_FINAL.log','TC4_11.64.36_JAVA17_SELF_TEST.log',
'TC4_11.64.36_GRADLE_BUILD_ATTEMPT.log','TC4_11.64.36_BUILD_STATUS.txt',
'TC4_PORT_STATUS_V3.md','tools/data/tc4_essentia_tube_full_source_evidence_v11.64.36.json',
'runtime_artifacts/runtime_test_manifest.template.json']
for p in required: req((R/p).is_file(),'required '+p)
req("version = '11.64.36'" in text('build.gradle'),'build version')
req('version="11.64.36"' in text('src/main/resources/META-INF/mods.toml'),'mods version')
# Execute release checks
commands=[
(['python3','tools/tc4_116436_essentia_tube_full_closure_guard.py'],'full closure guard','PASS'),
(['python3','tools/java_parse_guard_116436.py'],'targeted parse','PASS'),
(['python3','tools/validate_json_resources.py'],'json validation','OK'),
(['python3','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.36','--template'],'manifest validation','PASS'),
(['python3','tools/tc4_recipe_registration_denominator_guard.py'],'recipe denominator','258/258'),
(['python3','tools/tc4_116355_essentia_storage_mirror_runtime_guard.py'],'storage regression','PASS'),
(['python3','tools/tc4_116356_essentia_tube_transport_runtime_guard.py'],'tube regression','PASS'),
(['python3','tools/tc4_116372_essentia_parity_guard.py'],'essentia parity regression','PASS')]
for cmd,label,token in commands:
    r=subprocess.run(cmd,cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=300)
    req(r.returncode==0,label+' exit')
    req(token in r.stdout,label+' output')
# Counts
src=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',src,re.S)
req(len(methods)==278,'278 GameTests')
req(len(methods)==len(set(methods)),'unique GameTests')
man=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x.get('id') for x in man.get('tests',[])]
req(man.get('version')=='11.64.36','manifest version')
req(len(ids)==821,'821 scenarios')
req(len(ids)==len(set(ids)),'unique scenarios')
req('essentia_tubes' in man.get('subsystems',{}),'tube subsystem')
# Reports and honest statuses
for p in ('TC4_11.64.36_ESSENTIA_TUBE_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md','TC4_11.64.36_FULL_CLOSURE_STATUS_AND_PLAN_RU.md','TC4_PORT_STATUS_V3.md'):
    s=text(p); req('11.64.36' in s,p+' version'); req('BUILD VERIFIED: NO' in s or 'BUILD VERIFIED` или' in s,p+' honest build')
bs=text('TC4_11.64.36_BUILD_STATUS.txt')
for token in ('SOURCE CLOSED: YES','RESOURCE CLOSED: YES','BUILD VERIFIED: NO','RUNTIME VERIFIED: NO','JAR CREATED: NO','Gradle exit code: 1','Java 17'):
    req(token in bs,'build status '+token)
grad=text('TC4_11.64.36_GRADLE_BUILD_ATTEMPT.log')
req('UnknownHostException' in grad,'Gradle network failure recorded')
req('GRADLE_EXIT_CODE=1' in grad,'Gradle exit recorded')
req('PASS' in text('TC4_11.64.36_JAVA17_SELF_TEST.log'),'selftest log')
ci=text('TC4_11.64.36_FOCUSED_STATIC_CI_FINAL.log')
req('FOCUSED STATIC CI v11.64.36: PASS (59/59)' in ci,'focused CI final')
# Evidence
for p in ('TC4_11.64.36_ESSENTIA_TUBE_SOURCE_EVIDENCE.json','tools/data/tc4_essentia_tube_full_source_evidence_v11.64.36.json'):
    d=json.loads(text(p)); req(d.get('version')=='11.64.36',p+' version'); req(len(d.get('original_sources',[]))==10,p+' originals'); req(len(d.get('production_contracts',[]))>=13,p+' contracts')
# Production markers
production={
'src/main/java/com/darkifov/thaumcraft/essentia/TC4EssentiaTubeParity.java':['MINIMUM_SUCTION = 0','BUFFER_CAPACITY = 8','propagatedSuction','bufferComparator','nextValveRotation'],
'src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java':['tag.put("Aspects", list)','tag.putByteArray("choke"','tag.putBoolean("flow"','toggleManualFlowLikeTC4','refreshConnectionBlockState'],
'src/main/java/com/darkifov/thaumcraft/block/EssentiaTubeBlock.java':['held.getItem() instanceof JarLabelItem','hasAnalogOutputSignal','TC4EssentiaTubeParity.bufferComparator','onRemove'],
'src/main/java/com/darkifov/thaumcraft/client/render/EssentiaTubeRenderer.java':['renderBufferChokes','renderValve','ORIGINAL_VALVE_TEXTURE']}
for p,tokens in production.items():
    s=text(p)
    for token in tokens: req(token in s,p+' '+token)
# Forbidden output artifacts
bad=[]
for p in R.rglob('*'):
    rel=p.relative_to(R)
    if any(part in {'build','.gradle','__pycache__'} for part in rel.parts) or (p.is_file() and p.suffix in {'.class','.pyc'}): bad.append(str(rel))
req(not bad,'no forbidden generated files')
failed=[m for ok,m in checks if not ok]
if failed:
    print(f'FINAL RECHECK v11.64.36: FAIL ({len(failed)} failures / {len(checks)} checks)')
    for m in failed: print(' -',m)
    sys.exit(1)
print(f'FINAL RECHECK v11.64.36: PASS ({len(checks)}/{len(checks)})')
