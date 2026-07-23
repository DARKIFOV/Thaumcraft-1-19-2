#!/usr/bin/env python3
from pathlib import Path
import json,re,subprocess
R=Path(__file__).resolve().parents[1]
checks=[]
def req(name,ok,detail=''):
 checks.append((name,bool(ok),detail))
 if not ok: raise SystemExit(f'FINAL RECHECK v11.64.28: FAIL: {name}: {detail}')
def txt(p):return (R/p).read_text(encoding='utf-8',errors='replace')
req('build version',"version = '11.64.28'" in txt('build.gradle'))
req('mods version','version="11.64.28"' in txt('src/main/resources/META-INF/mods.toml'))
req('report exists',(R/'TC4_11.64.28_INFERNAL_FURNACE_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md').is_file())
req('evidence exists',(R/'TC4_11.64.28_INFERNAL_FURNACE_SOURCE_EVIDENCE.json').is_file())
req('prompt identical',(R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md').read_bytes()==(R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes())
for p in ('src/main/java/com/darkifov/thaumcraft/block/InfernalFurnaceBlock.java','src/main/java/com/darkifov/thaumcraft/block/InfernalFurnaceMultiblock.java','src/main/java/com/darkifov/thaumcraft/blockentity/InfernalFurnaceBlockEntity.java','src/main/java/com/darkifov/thaumcraft/blockentity/InfernalFurnaceNozzleBlockEntity.java'):
 req('production '+Path(p).name,(R/p).is_file())
be=txt('src/main/java/com/darkifov/thaumcraft/blockentity/InfernalFurnaceBlockEntity.java')
for token in ('INVENTORY_SIZE','RecipeType.SMELTING','worldPosition.relative(direction, 2)','new Bonus(Items.GOLD_NUGGET, 0)','ExperienceOrb.getExperienceValue','NBT_SPEEDY_TIME'):
 req('production marker '+token,token in be)
req('no invented flux','flux' not in be.lower())
req('132 models',len(list((R/'src/main/resources/assets/thaumcraft/models/block').glob('tc4_block_arcane_furnace_*.json')))==132)
bs=json.loads(txt('src/main/resources/assets/thaumcraft/blockstates/tc4_block_arcane_furnace.json'))
req('132 variants',len(bs['variants'])==132)
gt=txt('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java');methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req('GameTest uniqueness',len(methods)==233 and len(set(methods))==233,str(len(methods)))
man=json.loads(txt('runtime_artifacts/runtime_test_manifest.template.json'));ids=[x['id'] for x in man['tests']]
req('manifest uniqueness',man['version']=='11.64.28' and len(ids)==704 and len(set(ids))==704,f"{man['version']}/{len(ids)}")
req('JSON count','JSON resource validation: OK (2187 files)' in txt('TC4_11.64.28_FOCUSED_STATIC_CI_FINAL.log'))
req('focused CI','FOCUSED STATIC CI v11.64.28: PASS (32/32)' in txt('TC4_11.64.28_FOCUSED_STATIC_CI_FINAL.log'))
log=txt('TC4_11.64.28_GRADLE_BUILD_ATTEMPT.log')
req('Gradle command logged','./gradlew build --no-daemon --console=plain' in log)
req('Gradle honest failure','UnknownHostException: services.gradle.org' in log)
req('Java environment logged','21.0.10' in log)
req('build status honest','BUILD VERIFIED: NO' in txt('TC4_11.64.28_BUILD_STATUS.txt'))
forbidden=[]
for p in R.rglob('*'):
 if any(x in p.parts for x in ('build','.gradle','__pycache__')) or (p.is_file() and p.suffix in ('.class','.pyc')):forbidden.append(str(p.relative_to(R)))
req('clean source tree',not forbidden,','.join(forbidden[:10]))
# Execute key guards again.
for name,cmd in [('full guard',['python3','tools/tc4_116428_infernal_furnace_full_closure_guard.py']),('parse guard',['python3','tools/java_parse_guard_116428.py']),('manifest',['python3','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.28','--template'])]:
 r=subprocess.run(cmd,cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
 req(name,r.returncode==0,r.stdout[-500:])
print(f'FINAL RECHECK v11.64.28: PASS ({len(checks)}/{len(checks)})')
