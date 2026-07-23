#!/usr/bin/env python3
from pathlib import Path
import json,re,subprocess,hashlib,zipfile
R=Path(__file__).resolve().parents[1]
checks=[]
def req(name,ok,detail=''):
 checks.append((name,bool(ok),detail))
 if not ok: raise SystemExit(f'FINAL RECHECK v11.64.30: FAIL: {name}: {detail}')
def txt(p): return (R/p).read_text(encoding='utf-8',errors='replace')
req('build version',"version = '11.64.30'" in txt('build.gradle'))
req('mods version','version="11.64.30"' in txt('src/main/resources/META-INF/mods.toml'))
for p in ('TC4_11.64.30_FERTILITY_LAMP_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md','TC4_11.64.30_FULL_CLOSURE_STATUS_AND_PLAN_RU.md','TC4_11.64.30_FERTILITY_LAMP_SOURCE_EVIDENCE.json','tools/data/tc4_fertility_lamp_full_source_evidence_v11.64.30.json'):
 req('file '+p,(R/p).is_file())
req('prompt identical',(R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md').read_bytes()==(R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes())
par=txt('src/main/java/com/darkifov/thaumcraft/block/TC4FertilityLampParity.java')
for token in ('MAX_CHARGES = 4','BREEDING_COST = 2','BREEDING_INTERVAL_TICKS = 300','RADIUS = 7','MAX_EXISTING_SAME_CLASS = 7','ACTIVE_LIGHT = 15','INACTIVE_LIGHT = 8'):
 req('parity '+token,token in par)
be=txt('src/main/java/com/darkifov/thaumcraft/blockentity/TC4EssentiaLampBlockEntity.java')
for token in ('drawEssentia(Aspect.VICTUS)','TC4FertilityLampParity.isBreedingTick(fertilityCounter++)','animal.getClass().equals(first.getClass())','charges -= TC4FertilityLampParity.BREEDING_COST','neighbour instanceof EssentiaJarBlockEntity jar','sideFromNeighbour == Direction.UP'):
 req('production '+token,token in be)
req('no transient fertility NBT','tag.putInt("fertilityCounter"' not in be and 'tag.putInt("drawDelay"' not in be)
mod=txt('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
req('dynamic fertility light','TC4FertilityLampParity.ACTIVE_LIGHT' in mod and 'TC4FertilityLampParity.INACTIVE_LIGHT' in mod)
item=re.search(r'TC4_LAMP_FERTILITY_ITEM\s*=.*?;\n',mod,re.S)
req('common rarity',bool(item) and '.rarity(' not in item.group(0))
bs=json.loads(txt('src/main/resources/assets/thaumcraft/blockstates/tc4_block_lamp_fertility.json'))
req('12 variants',len(bs.get('variants',{}))==12)
req('off model',(R/'src/main/resources/assets/thaumcraft/models/block/tc4_block_lamp_fertility_off.json').is_file())
gt=txt('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req('GameTest count/unique',len(methods)==247 and len(set(methods))==247,str(len(methods)))
for name in ('fertilityLampChargeSuctionAndCadenceMatchOriginal','fertilityLampDirectJarVictusInputMatchesOriginal','fertilityLampExactClassPopulationAndBreedingMatchOriginal','fertilityLampRecipeResearchAndRarityMatchOriginal'):
 req('GameTest '+name,name in methods)
man=json.loads(txt('runtime_artifacts/runtime_test_manifest.template.json'));ids=[x['id'] for x in man['tests']]
req('manifest count/unique',man['version']=='11.64.30' and len(ids)==736 and len(set(ids))==736,f"{man['version']}/{len(ids)}")
for sid in ('gameplay.fertility_lamp_victus_direct_input','gameplay.fertility_lamp_population_cap','visual.fertility_lamp_dynamic_light','persistence.fertility_lamp_original_nbt'):
 req('scenario '+sid,sid in ids)
log=txt('TC4_11.64.30_FOCUSED_STATIC_CI_FINAL.log')
req('focused CI','FOCUSED STATIC CI v11.64.30: PASS (36/36)' in log)
req('JSON count','JSON resource validation: OK (2190 files)' in log)
gradle=txt('TC4_11.64.30_GRADLE_BUILD_ATTEMPT.log')
req('Gradle command','./gradlew build --no-daemon --console=plain' in gradle)
req('Gradle honest failure','UnknownHostException: services.gradle.org' in gradle and 'EXIT_CODE=1' in gradle)
req('Java env','21.0.10' in gradle)
req('build status honest','BUILD VERIFIED: NO' in txt('TC4_11.64.30_BUILD_STATUS.txt'))
req('start current','АКТУАЛЬНАЯ ВЕРСИЯ: 11.64.30' in txt('00_START_HERE_RU.txt'))
req('status current','# TC4 PORT STATUS V3 — v11.64.30' in txt('TC4_PORT_STATUS_V3.md'))
req('known boundary','## 11.64.30 Lamp of Fertility runtime boundary' in txt('KNOWN_DEVIATIONS.md'))
plan=txt('TC4_11.64.30_FULL_CLOSURE_STATUS_AND_PLAN_RU.md')
req('strict closed count','18. v11.64.30' in plan)
req('plan arcane workbench','Arcane Workbench / Mystical Workbench' in plan)
with zipfile.ZipFile(R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
 pref='Thaumcraft4-1.7.10-master/assets/thaumcraft/textures/blocks/'
 for name in ('lamp_fert_side.png','lamp_fert_side.png.mcmeta','lamp_fert_side_off.png','lamp_fert_top.png','lamp_fert_top.png.mcmeta','lamp_fert_top_off.png'):
  cur=(R/'src/main/resources/assets/thaumcraft/textures/block/tc4'/name).read_bytes()
  req('resource '+name,hashlib.sha256(cur).digest()==hashlib.sha256(z.read(pref+name)).digest())
forbidden=[]
for p in R.rglob('*'):
 if any(x in p.parts for x in ('build','.gradle','__pycache__')) or (p.is_file() and p.suffix in ('.class','.pyc')):
  forbidden.append(str(p.relative_to(R)))
req('clean tree',not forbidden,','.join(forbidden[:10]))
for name,cmd in [
 ('full guard',['python3','tools/tc4_116430_fertility_lamp_full_closure_guard.py']),
 ('parse guard',['python3','tools/java_parse_guard_116430.py']),
 ('manifest',['python3','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.30','--template']),
 ('growth regression',['python3','tools/tc4_116429_growth_lamp_full_closure_guard.py']),
 ('research regression',['python3','tools/tc4_116427_research_system_full_closure_guard.py'])]:
 r=subprocess.run(cmd,cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
 req(name,r.returncode==0,r.stdout[-500:])
print(f'FINAL RECHECK v11.64.30: PASS ({len(checks)}/{len(checks)})')
