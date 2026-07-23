#!/usr/bin/env python3
from pathlib import Path
import json,re
R=Path(__file__).resolve().parents[1]
def t(p): return (R/p).read_text(encoding='utf-8')
def req(c,m):
    if not c: raise SystemExit('TC4 v11.64.05 warp spawn collision guard: FAIL: '+m)
import re as _re
def _vt(raw):
    m=_re.search(r'(?m)^\s*version\s*=\s*["\']([0-9]+)\.([0-9]+)\.([0-9]+)',raw)
    return tuple(map(int,m.groups())) if m else (0,0,0)
req(_vt(t('build.gradle'))>=(11,64,5),'build version >=11.64.05')
req(_vt(t('src/main/resources/META-INF/mods.toml'))>=(11,64,5),'mods version >=11.64.05')
p=t('src/main/java/com/darkifov/thaumcraft/warp/TC4WarpRuntimeParity.java')
cm=_re.search(r'CONTRACT_VERSION = "([0-9]+)\.([0-9]+)\.([0-9]+)"',p)
req(cm is not None and tuple(map(int,cm.groups()))>=(11,64,5),'contract version >=11.64.05')
req('acceptsEntitySpawnCandidate' in p and 'solidTopSurface && collisionFree && !containsLiquid' in p,'pure entity-aware admission contract')
w=t('src/main/java/com/darkifov/thaumcraft/event/WarpEvents.java')
req('boolean collisionFree = level.noCollision(entity);' in w,'concrete entity collision check missing')
req('boolean containsLiquid = containsLiquid(level, entity.getBoundingBox());' in w,'entity AABB liquid scan missing')
req('TC4WarpRuntimeParity.acceptsEntitySpawnCandidate' in w,'production admission not source-linked')
segment=w[w.find('private static Optional<BlockPos> findSpawnAround'):w.find('private static int randomTc4AxisOffset')]
req('getBlockState(pos).isAir()' not in segment and 'getBlockState(pos.above()).isAir()' not in segment,'fixed two-air-block gate remains')
req('1.0E-7D' in segment and 'getFluidState(new BlockPos(x, y, z)).isEmpty()' in segment,'AABB liquid scan boundary contract missing')
g=t('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',g,re.S)
req(len(methods)>=139,'expected at least 139 GameTests, got %d'%len(methods))
req(len(methods)==len(set(methods)),'duplicate GameTest names')
for name in ('warpSpawnCandidateUsesOriginalCollisionLiquidContract','warpSpawnCandidateUsesActualEntityDimensions'):
    req(name in methods,'missing GameTest '+name)
m=json.loads(t('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in m['tests']]
req(tuple(map(int,m['version'].split('.')))>=(11,64,5),'manifest version >=11.64.05')
req(len(ids)>=467 and len(ids)==len(set(ids)),'expected at least 467 unique scenarios')
for case in ('gametest.warp_spawn_collision_liquid_contract','gametest.warp_spawn_entity_dimension_clearance'):
    req(case in ids,'missing manifest case '+case)
e=json.loads(t('tools/data/tc4_warp_spawn_collision_source_evidence_v11.64.05.json'))
req('does not require a hard-coded two-block air column' in e['confirmed_contract'],'source evidence collision contract')
req(e['runtime_status']=='NOT_VERIFIED','runtime honesty')
print(f'TC4 v11.64.05+ warp spawn collision guard: PASS ({len(methods)} GameTests; {len(ids)} scenarios; entity-aware collision/liquid admission production-linked)')
