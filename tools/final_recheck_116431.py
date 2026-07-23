#!/usr/bin/env python3
from pathlib import Path
import json,re,subprocess,hashlib,zipfile
R=Path(__file__).resolve().parents[1]
checks=[]
def req(name,ok,detail=''):
 checks.append((name,bool(ok),detail))
 if not ok: raise SystemExit(f'FINAL RECHECK v11.64.31: FAIL: {name}: {detail}')
def txt(p): return (R/p).read_text(encoding='utf-8',errors='replace')
req('build version',"version = '11.64.31'" in txt('build.gradle'))
req('mods version','version="11.64.31"' in txt('src/main/resources/META-INF/mods.toml'))
for p in ('TC4_11.64.31_ARCANE_WORKBENCH_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md','TC4_11.64.31_FULL_CLOSURE_STATUS_AND_PLAN_RU.md','TC4_11.64.31_ARCANE_WORKBENCH_SOURCE_EVIDENCE.json','tools/data/tc4_arcane_workbench_full_source_evidence_v11.64.31.json','TC4_11.64.31_FOCUSED_STATIC_CI_FINAL.log','TC4_11.64.31_JAVA17_SELF_TEST.log','TC4_11.64.31_GRADLE_BUILD_ATTEMPT.log','TC4_11.64.31_BUILD_STATUS.txt'):
 req('file '+p,(R/p).is_file())
req('prompt identical',(R/'UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md').read_bytes()==(R/'PROMPT_FOR_FUTURE_CHAT_RU.md').read_bytes())
req('prompt mandatory wording','Файл `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен' in txt('UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md'))
be=txt('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java')
for token in ('implements WorldlyContainer, MenuProvider','public static final int SIZE = 11','transformFromTable','player.setItemInHand(hand, ItemStack.EMPTY)','Component.translatable("container.arcaneworkbench")','recipe.aspectCost().isEmpty()','tag.put("Inventory", inventory)','getSlotsForFace(Direction side)','ClientboundBlockEntityDataPacket.create(this)'):
 req('production '+token,token in be)
for token in ('public static final int ORDO_COST','public static final int SLOT_LEGACY_CATALYST','catalystSlot == SLOT_LEGACY_CATALYST'):
 req('removed '+token,token not in be)
vis=txt('src/main/java/com/darkifov/thaumcraft/arcane/TC4ArcaneWorkbenchVisCostParity.java')
for token in ('CONTRACT_VERSION = "11.64.31"','INVENTORY_SIZE = 11','EMPTY_ASPECT_LIST_COST = 0','CENTIVIS_MULTIPLIER = 100'):
 req('parity '+token,token in vis)
renderer=txt('src/main/java/com/darkifov/thaumcraft/client/render/ArcaneWorkbenchRenderer.java')
for token in ('WAND_RENDER_X','WAND_RENDER_Y','WAND_RENDER_Z','WAND_RENDER_X_ROTATION','WAND_RENDER_Z_ROTATION','rendered.setCount(1)'):
 req('renderer '+token,token in renderer)
req('renderer registration','ARCANE_WORKBENCH_BLOCK_ENTITY.get(), blockEntityRenderer(ArcaneWorkbenchRenderer::new)' in txt('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java'))
req('sneak pass','if (player.isShiftKeyDown())' in txt('src/main/java/com/darkifov/thaumcraft/block/ArcaneWorkbenchBlock.java'))
wand=txt('src/main/java/com/darkifov/thaumcraft/block/WandItem.java')
segment=wand[wand.index('if (state.is(ThaumcraftMod.TABLE.get()))'):wand.index('if (state.is(Blocks.CAULDRON))')]
req('zero-cost transform','transformFromTable' in segment and 'consumeTransformationCost' not in segment)
recipe=json.loads(txt('src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_wandrodgreatwood.json'))
req('Greatwood rod cost',recipe.get('aspects')=={'perditio':3})
req('English localization',json.loads(txt('src/main/resources/assets/thaumcraft/lang/en_us.json')).get('container.arcaneworkbench')=='Arcane Workbench')
req('Russian localization',json.loads(txt('src/main/resources/assets/thaumcraft/lang/ru_ru.json')).get('container.arcaneworkbench')=='Магический верстак')
gt=txt('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req('GameTest count/unique',len(methods)==248 and len(set(methods))==248,str(len(methods)))
for name in ('arcaneWorkbenchInventoryAndAutomationMatchOriginal','arcaneWorkbenchZeroCostAndCentivisScalingMatchOriginal','arcaneWorkbenchTableTransformationInstallsWand','arcaneWorkbenchGuiCoordinatesMatchTc4Original'):
 req('GameTest '+name,name in methods)
man=json.loads(txt('runtime_artifacts/runtime_test_manifest.template.json')); ids=[x['id'] for x in man['tests']]
req('manifest count/unique',man['version']=='11.64.31' and len(ids)==748 and len(set(ids))==748,f"{man['version']}/{len(ids)}")
for sid in ('gameplay.arcane_workbench_table_transform_wand','gameplay.arcane_workbench_table_transform_staff','gameplay.arcane_workbench_zero_aspect_cost','automation.arcane_workbench_wand_slot_only','persistence.arcane_workbench_inventory_nbt','visual.arcane_workbench_installed_wand','visual.arcane_workbench_gui_and_ghost','dedicated.arcane_workbench_server_authority'):
 req('scenario '+sid,sid in ids)
log=txt('TC4_11.64.31_FOCUSED_STATIC_CI_FINAL.log')
req('focused CI','FOCUSED STATIC CI v11.64.31: PASS (38/38)' in log)
req('JSON count','JSON resource validation: OK (2190 files)' in log)
req('recipe denominator','258/258 STATICALLY MAPPED' in log)
req('Java self-test','TC4ArcaneWorkbenchParitySelfTest: PASS' in txt('TC4_11.64.31_JAVA17_SELF_TEST.log'))
gradle=txt('TC4_11.64.31_GRADLE_BUILD_ATTEMPT.log')
req('Gradle command','./gradlew build --no-daemon --console=plain' in gradle)
req('Gradle honest failure','UnknownHostException: services.gradle.org' in gradle and 'EXIT_CODE=1' in gradle)
req('Java env','21.0.10' in gradle)
req('build status honest','BUILD VERIFIED: NO' in txt('TC4_11.64.31_BUILD_STATUS.txt') and 'JAR CREATED: NO' in txt('TC4_11.64.31_BUILD_STATUS.txt'))
req('start current','АКТУАЛЬНАЯ ВЕРСИЯ: 11.64.31' in txt('00_START_HERE_RU.txt'))
req('status current','# TC4 PORT STATUS V3 — v11.64.31' in txt('TC4_PORT_STATUS_V3.md'))
req('known boundary','## 11.64.31 Arcane Workbench runtime boundary' in txt('KNOWN_DEVIATIONS.md'))
plan=txt('TC4_11.64.31_FULL_CLOSURE_STATUS_AND_PLAN_RU.md')
req('strict closed count','19. **v11.64.31' in plan)
req('next wand base','Жезлы: rods и caps' in plan)
with zipfile.ZipFile(R/'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
 pref='Thaumcraft4-1.7.10-master/'
 for cur,orig in (
 ('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/gui/gui_arcaneworkbench.png','assets/thaumcraft/textures/gui/gui_arcaneworkbench.png'),
 ('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/worktable.png','assets/thaumcraft/textures/models/worktable.png')):
  req('resource '+cur,hashlib.sha256((R/cur).read_bytes()).digest()==hashlib.sha256(z.read(pref+orig)).digest())
forbidden=[]
for p in R.rglob('*'):
 if any(x in p.parts for x in ('build','.gradle','__pycache__')) or (p.is_file() and p.suffix in ('.class','.pyc')):
  forbidden.append(str(p.relative_to(R)))
req('clean tree',not forbidden,','.join(forbidden[:10]))
for name,cmd in [
 ('full guard',['python3','tools/tc4_116431_arcane_workbench_full_closure_guard.py']),
 ('parse guard',['python3','tools/java_parse_guard_116431.py']),
 ('JSON',['python3','tools/validate_json_resources.py']),
 ('manifest',['python3','tools/validate_runtime_manifest.py','--manifest','runtime_artifacts/runtime_test_manifest.template.json','--version','11.64.31','--template']),
 ('recipes',['python3','tools/tc4_recipe_registration_denominator_guard.py']),
 ('fertility regression',['python3','tools/tc4_116430_fertility_lamp_full_closure_guard.py']),
 ('research regression',['python3','tools/tc4_116427_research_system_full_closure_guard.py'])]:
 r=subprocess.run(cmd,cwd=R,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=300)
 req(name,r.returncode==0,r.stdout[-500:])
print(f'FINAL RECHECK v11.64.31: PASS ({len(checks)}/{len(checks)})')
