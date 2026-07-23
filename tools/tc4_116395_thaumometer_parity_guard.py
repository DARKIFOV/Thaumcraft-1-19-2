#!/usr/bin/env python3
from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[1]

def req(cond,msg):
    if not cond: raise SystemExit('TC4 v11.63.95 Thaumometer parity guard: FAIL: '+msg)
def text(rel): return (ROOT/rel).read_text(encoding='utf-8')

build=text('build.gradle')
mods=text('src/main/resources/META-INF/mods.toml')
m=re.search(r"^version = '(\d+)\.(\d+)\.(\d+)'", build, re.M)
req(m is not None, 'build version parse')
current=tuple(map(int,m.groups()))
req(current >= (11,63,95), 'build version must be 11.63.95 or newer')
mm=re.search(r'(?m)^version="(\d+)\.(\d+)\.(\d+)"', mods)
req(mm is not None, 'mods.toml version parse')
req(tuple(map(int,mm.groups())) == current, 'mods.toml/build version agreement')
parity=text('src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerParity.java')
for token in (
 'CONTRACT_VERSION = "11.64.25"','USE_DURATION_TICKS = 25',
 'COMPLETION_REMAINING_TICKS = 5','ENTITY_SCAN_RANGE = 10.0D',
 'ENTITY_TARGET_EXPAND = 0.5D','CAMERA_TICK_INTERVAL = 2',
 'CAMERA_TICK_VOLUME = 0.20F','CAMERA_TICK_PITCH_BASE = 0.45F',
 'CAMERA_TICK_PITCH_RANDOM_SPAN = 0.10F','n==10',
 'mayStartHandheldScan','targetPriority'):
    req(token in parity,'missing parity token '+token)

item=text('src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java')
req('private static final int REQUIRED_STABLE_TICKS = 20' not in item,'stale local stable-tick literal')
for token in (
 'TC4ThaumometerParity.USE_DURATION_TICKS',
 'TC4ThaumometerParity.shouldPlayCameraTickAfterElapsed(elapsed)',
 'TC4ThaumometerParity.shouldCompleteAfterElapsed(elapsed)',
 'TC4ThaumometerParity.CAMERA_TICK_VOLUME',
 'TC4ThaumometerParity.CAMERA_TICK_PITCH_BASE',
 'TC4ThaumometerParity.CAMERA_TICK_PITCH_RANDOM_SPAN',
 '!isNewScanTarget(player,target)', '!isNewScanTarget(player,current)',
 'PlayerThaumData.hasScannedObject', 'PlayerThaumData.hasScannedEntity',
 'NodeScanData.hasScanned'):
    req(token in item,'production is not linked: '+token)

target=text('src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerTargeting.java')
req('TC4ThaumometerParity.ENTITY_SCAN_RANGE' in target,'target range not linked')
req('TC4ThaumometerParity.ENTITY_TARGET_EXPAND' in target,'target expansion not linked')
req(target.index('Entity entity = findEntity') < target.index('ScanTarget blockTarget = forBlock(player, hit.getBlockPos())'),
    'entity must be resolved before block target')

gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=101,f'expected at least 101 annotated GameTests, got {len(methods)}')
req(len(methods)==len(set(methods)),'GameTest names must be unique')
for name in (
 'thaumometerHoldDurationAndCompletionWindowMatchTc4',
 'thaumometerCameraTickCadenceMatchesTc4',
 'thaumometerNewTargetAndEntityPriorityMatchTc4'):
    req(name in methods,'missing GameTest '+name)

manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,63,95),'manifest version')
req(len(ids)>=429,f'expected at least 429 manifest scenarios, got {len(ids)}')
req(len(ids)==len(set(ids)),'manifest ids unique')
for id_ in (
 'gametest.thaumometer_hold_completion_contract',
 'gametest.thaumometer_camera_tick_cadence_contract',
 'gametest.thaumometer_new_target_entity_priority_contract'):
    req(id_ in ids,'missing manifest id '+id_)
print(f'TC4 v11.63.95 Thaumometer parity guard: PASS ({len(methods)} GameTests; {len(ids)} manifest scenarios; production-linked timing/target contracts)')
