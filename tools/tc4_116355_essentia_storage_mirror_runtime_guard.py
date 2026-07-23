#!/usr/bin/env python3
"""v11.63.55 essentia storage and Essentia Mirror runtime contract guard."""
from __future__ import annotations
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')
def req(c,m):
    if not c: raise SystemExit('TC4 v11.63.55 essentia runtime guard: FAIL: '+m)
def main():
    req("version = '11." in read('build.gradle'),'build version')
    req('version="11.' in read('src/main/resources/META-INF/mods.toml'),'mods version')
    tests=read('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
    required={
      'labelledAndVoidJarsKeepTc4SuctionCapacityAndOverflow':('originalMinimumSuction','originalSuctionAmount','VOID_ESSENTIA_JAR','TC4'),
      'essentiaReservoirKeepsMixedStorageFacingAndCapacity':('EssentiaReservoirBlock.FACING','Direction.EAST','CAPACITY','mixed'),
      'essentiaMirrorDrainsOnePointAndRollsBackRemoteJar':('takeRemoteEssentia(Aspect.AER, 2)','restoreRemoteEssentia','MirrorLink.at','jar.amount() == 3'),
      'essentiaMirrorUsesOnlyForwardEightBlockSourceVolume':('remotePos.south()','remotePos.north(7)','peekRemoteAspect() == null','edge.amount() == 3'),
    }
    for method,tokens in required.items():
      req(method in tests,'missing GameTest '+method)
      for token in tokens: req(token in tests,method+': missing '+token)
    req(tests.count('@GameTest(')>=32,'expected at least 32 GameTests')
    mirror=read('src/main/java/com/darkifov/thaumcraft/mirror/EssentiaMirrorBlockEntity.java')
    for token in ('public static final int RANGE = 8','amount != TRANSFER_UNIT','restoreRemoteEssentia','for (int cc = 0; cc < RANGE; cc++)'):
      req(token in mirror,'Essentia Mirror source contract '+token)
    jar=read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaJarBlockEntity.java')
    for token in ('TC4EssentiaJarParity.CAPACITY','TC4EssentiaJarParity.minimumSuction','TC4EssentiaJarParity.suctionAmount'):
      req(token in jar,'jar contract '+token)
    reservoir=read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaReservoirBlockEntity.java')
    for token in ('public static final int CAPACITY = 256','ORIGINAL_RESERVOIR_SUCTION = 24','sideFromReservoir == facing'):
      req(token in reservoir,'reservoir contract '+token)
    manifest=json.loads(read('runtime_artifacts/runtime_test_manifest.template.json'))
    req(str(manifest.get('version','')).startswith('11.'),'manifest version')
    req(len(manifest.get('tests',[]))>=360,'manifest scenario count')
    ids={x.get('id') for x in manifest['tests']}
    expected={'gametest.filtered_void_jar_suction_overflow_contract','gametest.essentia_reservoir_mixed_capacity_contract','gametest.essentia_mirror_remote_drain_rollback_contract','gametest.essentia_mirror_forward_range_contract'}
    req(expected<=ids,'missing new runtime scenarios')
    for wf in ('.github/workflows/build.yml','.github/workflows/release.yml'):
      text=read(wf); req('--version 11.' in text,wf+' manifest version'); req('tc4_116355_essentia_storage_mirror_runtime_guard.py' in text,wf+' guard wiring')
    print('TC4 v11.63.55 essentia runtime guard: PASS (32 GameTests, 360 scenarios)')
if __name__=='__main__': main()
