#!/usr/bin/env python3
from pathlib import Path
import json,re,subprocess,hashlib,zipfile
R=Path(__file__).resolve().parents[1]
checks=[]
def req(name,ok,detail=''):
    checks.append((name,bool(ok),detail))
    if not ok: raise SystemExit(f'FINAL RECHECK v11.64.29: FAIL: {name}: {detail}')
def txt(p): return (R/p).read_text(encoding='utf-8',errors='replace')
req('build version',"version = '11.64.29'" in txt('build.gradle'))
req('mods version','version="11.64.29"' in txt('src/main/resources/META-INF/mods.toml'))
req('report exists',(R/'TC4_11.64.29_GROWTH_LAMP_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md').is_file())
req('evidence exists',(R/'TC4_11.64.29_GROWTH_LAMP_SOURCE_EVIDENCE.json').is_file())
req('tool evidence exists',(R/'tools/data/tc4_growth_lamp_full_source_evidence_v11.64.29.json').is_file())
req('prompt identical',(R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md').read_bytes()==(R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes())
for p in ('src/main/java/com/darkifov/thaumcraft/block/TC4GrowthLampParity.java','src/main/java/com/darkifov/thaumcraft/block/TC4EssentiaLampBlock.java','src/main/java/com/darkifov/thaumcraft/blockentity/TC4EssentiaLampBlockEntity.java','src/main/java/com/darkifov/thaumcraft/client/render/TC4EssentiaLampRenderer.java'):
    req('production '+Path(p).name,(R/p).is_file())
be=txt('src/main/java/com/darkifov/thaumcraft/blockentity/TC4EssentiaLampBlockEntity.java')
for token in ('Aspect.HERBA','CHARGES_PER_ESSENTIA','growthColumns.remove(0)','TC4GrowthLampParity.insideSphere','state.randomTick(level, cursor, level.getRandom())','GROWTH_BLACKLIST','SPARKLE_COLOR','NBT_RESERVE'):
    req('production marker '+token,token in be)
req('no guaranteed bonemeal','performBonemeal' not in be)
req('no unloaded chunk skip','hasChunkAt' not in be)
req('no transient NBT','tag.putInt("drawDelay"' not in be and 'tag.putInt("fertilityCounter"' not in be)
block=txt('src/main/java/com/darkifov/thaumcraft/block/TC4EssentiaLampBlock.java')
req('non-air support','isFaceSturdy' not in block and 'changedState.isAir()' in block)
renderer=txt('src/main/java/com/darkifov/thaumcraft/client/render/TC4EssentiaLampRenderer.java')
req('shared nozzle renderer','textures/models/Bore.png' in renderer and 'ArcaneBoreBaseBlockEntity' in renderer)
bs=json.loads(txt('src/main/resources/assets/thaumcraft/blockstates/tc4_block_lamp_growth.json'))
req('12 blockstate variants',len(bs.get('variants',{}))==12)
req('blacklist tag',(R/'src/main/resources/data/thaumcraft/tags/blocks/growth_lamp_blacklist.json').is_file())
gt=txt('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req('GameTest uniqueness',len(methods)==240 and len(set(methods))==240,str(len(methods)))
for name in ('growthLampChargeReserveAndSuctionMatchOriginal','growthLampBlockEntityAndOriginalNbtRoundTrip','growthLampRecipeResearchAndRarityMatchOriginal'):
    req('GameTest '+name,name in methods)
man=json.loads(txt('runtime_artifacts/runtime_test_manifest.template.json'));ids=[x['id'] for x in man['tests']]
req('manifest uniqueness',man['version']=='11.64.29' and len(ids)==720 and len(set(ids))==720,f"{man['version']}/{len(ids)}")
req('focused CI','FOCUSED STATIC CI v11.64.29: PASS (34/34)' in txt('TC4_11.64.29_FOCUSED_STATIC_CI_FINAL.log'))
req('JSON count','JSON resource validation: OK (2189 files)' in txt('TC4_11.64.29_FOCUSED_STATIC_CI_FINAL.log'))
log=txt('TC4_11.64.29_GRADLE_BUILD_ATTEMPT.log')
req('Gradle command logged','./gradlew build --no-daemon --console=plain' in log)
req('Gradle honest failure','UnknownHostException: services.gradle.org' in log)
req('Java environment logged','21.0.10' in log)
req('build status honest','BUILD VERIFIED: NO' in txt('TC4_11.64.29_BUILD_STATUS.txt'))
req('start file current','АКТУАЛЬНАЯ ВЕРСИЯ: 11.64.29' in txt('00_START_HERE_RU.txt'))
req('status current','# TC4 PORT STATUS V3 — v11.64.29' in txt('TC4_PORT_STATUS_V3.md'))
req('known boundary','## 11.64.29 Lamp of Growth runtime boundary' in txt('KNOWN_DEVIATIONS.md'))
# Resource hashes independently.
with zipfile.ZipFile(R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    prefix='Thaumcraft4-1.7.10-master/assets/thaumcraft/textures/blocks/'
    for name in ('lamp_grow_side.png','lamp_grow_side.png.mcmeta','lamp_grow_side_off.png','lamp_grow_top.png','lamp_grow_top.png.mcmeta','lamp_grow_top_off.png'):
        cur=(R/'src/main/resources/assets/thaumcraft/textures/block/tc4'/name).read_bytes()
        req('resource '+name,hashlib.sha256(cur).digest()==hashlib.sha256(z.read(prefix+name)).digest())
# Clean tree.
forbidden=[]
for p in R.rglob('*'):
    if any(x in p.parts for x in ('build','.gradle','__pycache__')) or (p.is_file() and p.suffix in ('.class','.pyc')):
        forbidden.append(str(p.relative_to(R)))
req('clean source tree',not forbidden,','.join(forbidden[:10]))
for name,cmd in [
 ('full guard',['python3','tools/tc4_116429_growth_lamp_full_closure_guard.py']),
 ('parse guard',['python3','tools/java_parse_guard_116429.py']),
 ('manifest',['python3','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.29','--template']),
 ('infernal regression',['python3','tools/tc4_116428_infernal_furnace_full_closure_guard.py'])]:
    r=subprocess.run(cmd,cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    req(name,r.returncode==0,r.stdout[-500:])
print(f'FINAL RECHECK v11.64.29: PASS ({len(checks)}/{len(checks)})')
