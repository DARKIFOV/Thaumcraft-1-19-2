#!/usr/bin/env python3
from pathlib import Path
import json,re
R=Path(__file__).resolve().parents[1]
def t(p): return (R/p).read_text(encoding='utf-8')
def req(c,m):
    if not c: raise SystemExit('TC4 v11.64.04 warp spawn guard: FAIL: '+m)
req(re.search(r"version = '11\.64\.(?:0[4-9]|[1-9][0-9])'", t('build.gradle')) is not None,'build version >=11.64.04')
req(re.search(r'version="11\.64\.(?:0[4-9]|[1-9][0-9])"', t('src/main/resources/META-INF/mods.toml')) is not None,'mods version >=11.64.04')
p=t('src/main/java/com/darkifov/thaumcraft/warp/TC4WarpRuntimeParity.java')
req(re.search(r'CONTRACT_VERSION = "11\.64\.(?:0[4-9]|[1-9][0-9])"', p) is not None,'contract version >=11.64.04')
req('signedSpawnOffset(int magnitude, int signRoll)' in p,'pure tri-state offset contract')
req('Integer.compare(signRoll, 0)' in p,'sign normalization')
w=t('src/main/java/com/darkifov/thaumcraft/event/WarpEvents.java')
req('int signRoll = Mth.nextInt(player.getRandom(), -1, 1);' in w,'inclusive TC4 tri-state sign roll')
req('TC4WarpRuntimeParity.signedSpawnOffset(magnitude, signRoll)' in w,'production wiring')
req('player.getRandom().nextBoolean()' not in w[w.find('private static int randomTc4AxisOffset'):],'boolean sign regression remains')
g=t('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',g,re.S)
req(len(methods)>=137,'expected at least 137 GameTests, got %d'%len(methods))
req(len(methods)==len(set(methods)),'duplicate GameTest names')
req('warpSpawnOffsetsPreserveOriginalTriStateSign' in methods,'missing tri-state behavior GameTest')
req('warpSpawnOffsetsKeepOriginalSignedMinimum' not in methods,'rejected non-zero GameTest remains')
m=json.loads(t('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in m['tests']]
req(re.fullmatch(r'11\.64\.(?:0[4-9]|[1-9][0-9])', m['version']) is not None,'manifest version >=11.64.04')
req(len(ids)>=465 and len(ids)==len(set(ids)),'expected at least 465 unique scenarios')
req('gametest.warp_spawn_signed_tristate_offsets' in ids,'missing manifest case')
req('gametest.warp_spawn_signed_nonzero_offsets' not in ids,'rejected manifest case remains')
e=json.loads(t('tools/data/tc4_warp_spawn_offset_source_evidence_v11.64.04.json'))
req('inclusive random integer in [-1,1]' in e['confirmed_contract'],'source evidence tri-state contract')
req(e['runtime_status']=='NOT_VERIFIED','runtime honesty')
print(f'TC4 v11.64.04+ warp spawn guard: PASS ({len(methods)} GameTests; {len(ids)} scenarios; original -1/0/+1 offsets production-linked)')
