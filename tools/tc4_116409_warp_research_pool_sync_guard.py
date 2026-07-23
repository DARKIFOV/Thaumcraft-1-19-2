#!/usr/bin/env python3
"""v11.64.09 guard: TC4 warp grantResearch awards must use the exact six
primal aspects, the 1 + rand(times) range, and immediately synchronize the
changed aspect pool to the affected client from every production path."""
from pathlib import Path
import json
import re

R = Path(__file__).resolve().parents[1]

def t(path):
    return (R / path).read_text(encoding='utf-8')

def req(condition, message):
    if not condition:
        raise SystemExit('TC4 v11.64.09 warp research pool sync guard: FAIL: ' + message)

def version_tuple(raw):
    m = re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']', raw)
    req(m is not None, 'version parse')
    return tuple(map(int, m.groups()))

req(version_tuple(t('build.gradle')) >= (11, 64, 9), 'build version >= 11.64.09')
req(version_tuple(t('src/main/resources/META-INF/mods.toml')) >= (11, 64, 9), 'mods version >= 11.64.09')

shared = t('src/main/java/com/darkifov/thaumcraft/warp/TC4WarpResearchGrant.java')
req('CONTRACT_VERSION = "11.64.09"' in shared, 'shared grant contract version missing')
req('int amount = 1 + random.nextInt(times)' in shared, 'TC4 1 + rand(times) formula missing')
req('if (times <= 0)' in shared, 'invalid non-positive times are not rejected')
for aspect in ('Aspect.AER', 'Aspect.TERRA', 'Aspect.IGNIS', 'Aspect.AQUA', 'Aspect.ORDO', 'Aspect.PERDITIO'):
    req(aspect in shared, 'primal order missing ' + aspect)
req('PlayerAspectKnowledge.addPool(player, aspect, 1)' in shared, 'server aspect pool mutation missing')
req('ThaumcraftNetwork.syncAspectKnowledge(player)' in shared, 'post-grant client pool sync missing')
req(shared.find('PlayerAspectKnowledge.addPool(player, aspect, 1)') < shared.find('ThaumcraftNetwork.syncAspectKnowledge(player)'),
    'client sync must happen after all pool mutations')
req('new AspectKnowledgeSyncPacket(PlayerAspectKnowledge.knownAspectIds(player), PlayerAspectKnowledge.poolAmounts(player))'
    in t('src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java'),
    'syncAspectKnowledge no longer sends the complete known/pool snapshot')

warp = t('src/main/java/com/darkifov/thaumcraft/event/WarpEvents.java')
eldritch = t('src/main/java/com/darkifov/thaumcraft/eldritch/TC4EldritchProgression.java')
for name, production in (('WarpEvents', warp), ('TC4EldritchProgression', eldritch)):
    req('TC4WarpResearchGrant.grantAndSync(player, times)' in production,
        name + ' does not delegate grantResearch to the synchronized shared production path')

# The test exercises the actual roll method used by production, not a duplicate
# constants-only implementation.
gametest = t('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods = re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(', gametest, re.S)
req(len(methods) >= 142, f'expected at least 142 GameTests, got {len(methods)}')
req(len(methods) == len(set(methods)), 'duplicate GameTest method names')
req('warpResearchGrantRollsTc4RangeAndPrimals' in methods, 'new warp research grant GameTest missing')
req('TC4WarpResearchGrant.roll(' in gametest, 'GameTest is not linked to the production roll method')

manifest = json.loads(t('runtime_artifacts/runtime_test_manifest.template.json'))
ids = [case['id'] for case in manifest['tests']]
req(tuple(map(int, manifest['version'].split('.'))) >= (11, 64, 9), 'manifest version >= 11.64.09')
req(len(ids) >= 470 and len(ids) == len(set(ids)), 'expected at least 470 unique manifest scenarios')
req('gametest.warp_research_grant_pool_sync' in ids, 'warp research pool sync manifest scenario missing')

evidence = json.loads(t('tools/data/tc4_warp_research_pool_sync_source_evidence_v11.64.09.json'))
req(evidence['round'] == '11.64.09', 'evidence round mismatch')
req(evidence['runtime_status'] == 'NOT_VERIFIED', 'runtime honesty drifted')
req(evidence['build_status'] == 'NOT_OBTAINED', 'build honesty drifted')

print(f'TC4 v11.64.09 warp research pool sync guard: PASS '
      f'({len(methods)} GameTests; {len(ids)} scenarios; exact primal roll + aggregate client pool sync)')
