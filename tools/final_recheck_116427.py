#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re,subprocess
R=Path(__file__).resolve().parents[1]; passed=[]
def req(name,ok,detail=''):
 if not ok: raise SystemExit(f'FAIL | {name}: {detail}')
 print('PASS | '+name);passed.append(name)
def run(name,cmd,timeout=420):
 q=subprocess.run(cmd,cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=timeout)
 if q.returncode:
  print('FAIL | '+name);print(q.stdout);raise SystemExit(q.returncode)
 print('PASS | '+name);passed.append(name)

def t(p):return (R/p).read_text(encoding='utf-8',errors='replace')
# 1-5 version/prompt
req('version agreement',"version = '11.64.27'" in t('build.gradle') and 'version="11.64.27"' in t('src/main/resources/META-INF/mods.toml'))
p1=R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md';p2=R/'PROMPT_FOR_FUTURE_CHAT_RU.md'
req('mandatory prompt identity',p1.read_bytes()==p2.read_bytes())
prompt=p1.read_text(encoding='utf-8')
req('one release one mechanic rule','Один релиз — один предмет или одна цельная механика' in prompt)
req('prompt packaging prohibition','Упаковка архива без этого файла запрещена' in prompt)
req('prompt honest status tokens',all(x in prompt for x in ('SOURCE CLOSED','RESOURCE CLOSED','BUILD VERIFIED','RUNTIME VERIFIED')))
# 6-12 executable proofs
run('research-system full-closure guard',['python3','tools/tc4_116427_research_system_full_closure_guard.py'])
run('Thaumonomicon cumulative guard',['python3','tools/tc4_116426_thaumonomicon_full_closure_guard.py'])
run('Java 17 self-test and targeted parse',['python3','tools/java_parse_guard_116427.py'])
run('Java syntax guard',['python3','tools/java_syntax_guard.py'])
run('JSON resources',['python3','tools/validate_json_resources.py'])
run('runtime manifest',['python3','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.27','--template'])
run('recipe denominator',['python3','tools/tc4_recipe_registration_denominator_guard.py'])
# 13-18 counts/report/evidence
gt=t('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java');methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req('GameTest uniqueness',len(methods)==226 and len(methods)==len(set(methods)),str(len(methods)))
man=json.loads(t('runtime_artifacts/runtime_test_manifest.template.json'));ids=[x['id'] for x in man['tests']]
req('manifest uniqueness',man['version']=='11.64.27' and len(ids)==688 and len(ids)==len(set(ids)),f"{man['version']}/{len(ids)}")
report=R/'TC4_11.64.27_RESEARCH_SYSTEM_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md'
req('full report exists',report.is_file() and report.stat().st_size>4000)
ev=R/'tools/data/tc4_research_system_full_source_evidence_v11.64.27.json';root_ev=R/'TC4_11.64.27_RESEARCH_SYSTEM_SOURCE_EVIDENCE.json'
req('evidence copies identity',ev.is_file() and ev.read_bytes()==root_ev.read_bytes())
e=json.loads(ev.read_text(encoding='utf-8'))
req('honest evidence statuses',e['source_closure']=='CLOSED' and e['resource_closure']=='CLOSED' and e['build_status']=='NOT_OBTAINED' and e['runtime_status']=='NOT_VERIFIED')
req('evidence counts',e['static_proof']['gametests']==226 and e['static_proof']['runtime_scenarios']==688 and e['static_proof']['forge_parse_files']==19)
# 19-23 build/cleanliness
log=t('TC4_11.64.27_GRADLE_BUILD_ATTEMPT.log');code=t('TC4_11.64.27_GRADLE_BUILD_EXITCODE.txt').strip()
req('honest Gradle failure','UnknownHostException: services.gradle.org' in log and code=='1')
req('required Java evidence','openjdk version "21.0.10"' in log and 'проект Forge 1.19.2 требует JDK 17' in report.read_text(encoding='utf-8'))
ci=t('TC4_11.64.27_FOCUSED_STATIC_CI_FINAL.log')
req('focused CI evidence','FOCUSED STATIC CI v11.64.27: PASS (42/42)' in ci)
req('no false build success','BUILD SUCCESSFUL' not in log and 'JAR CREATED: NO' in t('TC4_11.64.27_BUILD_STATUS.txt'))
forbidden=[]
for p in R.rglob('*'):
 rel=p.relative_to(R)
 if any(x in {'build','.gradle','__pycache__'} for x in rel.parts) or (p.is_file() and p.suffix in {'.class','.pyc'}):forbidden.append(str(rel))
req('clean source tree',not forbidden,', '.join(forbidden[:10]))
# 24-42 production/resource contracts
par=t('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchSystemFullClosureParity.java')
req('original registry denominator','ORIGINAL_RESEARCH_COUNT = 201' in par)
knowledge=t('src/main/java/com/darkifov/thaumcraft/research/PlayerAspectKnowledge.java')
req('zero primal starter pool','known.putBoolean(aspect.id(), true)' in knowledge and 'list.add(aspect, 10)' not in knowledge and 'list.add(aspect, 5)' not in knowledge)
req('legacy wallet max migration','Math.max(pool.get(aspect)' in knowledge and 'persistent.remove(LEGACY_WALLET_ROOT)' in knowledge)
wallet=t('src/main/java/com/darkifov/thaumcraft/research/OriginalAspectWallet.java')
req('single unified aspect wallet','PlayerAspectKnowledge.pool' in wallet and 'ThaumcraftOriginalAspectWallet' not in wallet)
requirements=t('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteRequirements.java')
req('exact original note tags','original.get().aspects().keySet()' in requirements and 'return exact;' in requirements)
creator=t('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteCreator.java')
req('creative cannot bypass note cost','paper.shrink(1)' in creator and 'consumeInk(tools, INK_COST)' in creator and 'instabuild' not in creator)
req('existing note short-circuits cost','findResearchNote(player, entry.key())' in creator and 'return existing;' in creator)
be=t('src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java')
req('table create button disabled','public boolean createResearchNote(ServerPlayer player) {\n        return false;' in be)
req('table complete button disabled','public boolean completeResearchNote(ServerPlayer player) {\n        return false;' in be)
action=t('src/main/java/com/darkifov/thaumcraft/network/RequestResearchTableActionPacket.java')
req('table actions silent and exact','displayClientMessage' not in action and 'createResearchNote(' not in action and 'completeResearchNote(' not in action and 'copyCompletedResearchNote' in action)
packet_names=('RequestSolveResearchNotePacket.java','RequestPlaceResearchNoteAspectPacket.java','RequestClearResearchNoteSlotPacket.java','RequestCombineAspectsPacket.java','RequestSelectResearchPacket.java')
req('research packets silent',all('displayClientMessage' not in t('src/main/java/com/darkifov/thaumcraft/network/'+x) for x in packet_names))
copy_seg=be[be.index('public boolean copyCompletedResearchNote'):be.index('private boolean hasCopyAspectCost')]
req('duplicate overstack preserved','note.grow(1)' in copy_seg and 'getMaxStackSize' not in copy_seg)
req('duplicate exact escalating cost','Math.max(0, aspectEntry.getValue()) + Math.max(0, copies)' in be and 'Items.PAPER' in copy_seg and 'Items.INK_SAC' in copy_seg)
bridge=t('src/main/java/com/darkifov/thaumcraft/research/OriginalResearchBridge.java')
req('secondary exact research costs','costs.put(aspect.getKey().toLowerCase(), Math.max(0, aspect.getValue()))' in bridge and 'OriginalAspectWallet.consume(player, costsFor(entry))' in bridge)
solver=t('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java')
req('original prerequisite error only','Component.translatable("tc.researcherror")' in solver)
joined='\n'.join(t(p) for p in ['src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java','src/main/java/com/darkifov/thaumcraft/research/OriginalResearchBridge.java','src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java','src/main/java/com/darkifov/thaumcraft/network/RequestResearchTableActionPacket.java'])
req('no invented research chat',all(x not in joined for x in ('Research completed:','Research solved:','Research note prepared','bonus aspects:','Discovery completed:')))
behavior=t('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchTableBehaviorParity.java')
req('table recalc cadence 600','RECALCULATE_THRESHOLD = 600' in behavior or '> 600' in behavior)
bonus=t('src/main/java/com/darkifov/thaumcraft/research/ResearchTableBonusRuntime.java')
req('table bonus radius 8','SCAN_RADIUS = TC4ResearchEfficiencyParity.BONUS_SCAN_RADIUS' in bonus and 'BONUS_SCAN_RADIUS = 8' in t('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchEfficiencyParity.java'))
req('all 53 original resources byte-identical',len(e['resources'])==53 and all(x['byte_identical'] for x in e['resources']))
# 43-46 current docs
req('README current',t('README.md').startswith('# v11.64.27'))
req('known deviations current',t('KNOWN_DEVIATIONS.md').startswith('# v11.64.27'))
req('status current',t('TC4_PORT_STATUS_V3.md').startswith('# TC4 PORT STATUS V3 — v11.64.27'))
req('start-here current','v11.64.27 RESEARCH_SYSTEM_FULL_CLOSURE_PARITY' in t('00_START_HERE_RU.txt'))
print(f'FINAL RECHECK v11.64.27: PASS ({len(passed)}/{len(passed)})')
