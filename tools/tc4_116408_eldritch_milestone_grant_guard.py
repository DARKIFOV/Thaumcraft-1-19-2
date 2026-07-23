#!/usr/bin/env python3
"""v11.64.08 guard: TC4 Eldritch milestones ELDRITCHMINOR (actual warp > 25)
and ELDRITCHMAJOR (actual warp > 50) silently call grantResearch(10)/(20) with
NO chat line, matching WarpEvents.checkWarpEvent. The port previously showed
fabricated milestone chat literals and never granted research."""
from pathlib import Path
import json, re
R = Path(__file__).resolve().parents[1]
def t(p): return (R/p).read_text(encoding='utf-8')
def req(c, m):
    if not c: raise SystemExit('TC4 v11.64.08 eldritch milestone guard: FAIL: ' + m)
def vtuple(raw):
    m = re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']', raw)
    req(m is not None, 'version parse'); return tuple(map(int, m.groups()))

req(vtuple(t('build.gradle')) >= (11, 64, 8), 'build version >=11.64.08')
req(vtuple(t('src/main/resources/META-INF/mods.toml')) >= (11, 64, 8), 'mods version >=11.64.08')

# --- production: TC4EldritchProgression milestone parity ---
e = t('src/main/java/com/darkifov/thaumcraft/eldritch/TC4EldritchProgression.java')
req('ELDRITCH_MINOR_RESEARCH_GRANTS = 10' in e, 'MINOR grant magnitude constant (10) missing')
req('ELDRITCH_MAJOR_RESEARCH_GRANTS = 20' in e, 'MAJOR grant magnitude constant (20) missing')
# fabricated milestone chat literals must be gone
req('Something alien becomes visible' not in e, 'fabricated ELDRITCHMINOR chat literal still present')
req('The Eldritch tab opens fully' not in e, 'fabricated ELDRITCHMAJOR chat literal still present')
# faithful grantResearch port. v11.64.09+ delegates to the shared production
# helper so both ordinary warp events and Eldritch milestones use the same award
# and client-pool synchronization path.
req('private static void grantResearch(ServerPlayer player, int times)' in e, 'grantResearch port method missing')
if 'TC4WarpResearchGrant.grantAndSync(player, times)' in e:
    shared = t('src/main/java/com/darkifov/thaumcraft/warp/TC4WarpResearchGrant.java')
    req('1 + random.nextInt(times)' in shared, 'shared grantResearch amount formula drifted from TC4')
    req('PlayerAspectKnowledge.addPool(player, aspect, 1)' in shared, 'shared grantResearch must add +1 primal aspect pool per iteration')
    for asp in ('Aspect.AER','Aspect.TERRA','Aspect.IGNIS','Aspect.AQUA','Aspect.ORDO','Aspect.PERDITIO'):
        req(asp in shared, 'shared grantResearch primal set missing ' + asp)
else:
    req('1 + player.getRandom().nextInt(Math.max(1, times))' in e, 'grantResearch amount formula drifted from TC4 (1 + rand(times))')
    req('PlayerAspectKnowledge.addPool(player, aspect, 1)' in e, 'grantResearch must add +1 primal aspect pool per iteration')
    for asp in ('Aspect.AER','Aspect.TERRA','Aspect.IGNIS','Aspect.AQUA','Aspect.ORDO','Aspect.PERDITIO'):
        req(asp in e, 'grantResearch primal set missing ' + asp)
# MINOR block: silent unlock + grantResearch(10)
minor = e[e.find('actualWarp > ELDRITCH_MINOR_WARP'):e.find('actualWarp > ELDRITCH_MAJOR_WARP')]
req('unlock(player, "ELDRITCHMINOR", null)' in minor, 'ELDRITCHMINOR must unlock with no chat message (null)')
req('grantResearch(player, ELDRITCH_MINOR_RESEARCH_GRANTS)' in minor, 'ELDRITCHMINOR must call grantResearch(10) on first unlock')
# MAJOR block: silent unlock + grantResearch(20)
major = e[e.find('actualWarp > ELDRITCH_MAJOR_WARP'):e.find('"CRIMSON"')]
req('unlock(player, "ELDRITCHMAJOR", null)' in major, 'ELDRITCHMAJOR must unlock with no chat message (null)')
req('grantResearch(player, ELDRITCH_MAJOR_RESEARCH_GRANTS)' in major, 'ELDRITCHMAJOR must call grantResearch(20) on first unlock')

# --- parity contract class links to production constants ---
p = t('src/main/java/com/darkifov/thaumcraft/eldritch/TC4EldritchParity.java')
req('eldritchMilestoneGrantsMatchTc4' in p, 'TC4EldritchParity.eldritchMilestoneGrantsMatchTc4 missing')
req('ELDRITCH_MINOR_RESEARCH_GRANTS == 10' in p and 'ELDRITCH_MAJOR_RESEARCH_GRANTS == 20' in p, 'parity method must assert 10/20 magnitudes')

# --- GameTest present, forward-compatible count ---
g = t('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods = re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(', g, re.S)
req(len(methods) >= 141, 'expected at least 141 GameTests, got %d' % len(methods))
req(len(methods) == len(set(methods)), 'duplicate GameTest names')
req('eldritchMilestonesGrantResearchWithoutChat' in methods, 'missing eldritch milestone GameTest')
req('warpText8IsBathSaltsMilestoneNotBlurredVision' in methods, 'prior warp.text.8 GameTest disappeared')

# --- manifest scenario present, forward-compatible count ---
m = json.loads(t('runtime_artifacts/runtime_test_manifest.template.json'))
ids = [x['id'] for x in m['tests']]
req(tuple(map(int, m['version'].split('.'))) >= (11, 64, 8), 'manifest version >=11.64.08')
req(len(ids) >= 469 and len(ids) == len(set(ids)), 'expected at least 469 unique scenarios')
req('gametest.eldritch_milestone_grant_research' in ids, 'missing eldritch milestone manifest case')

# --- source evidence honesty ---
ev = json.loads(t('tools/data/tc4_eldritch_milestone_source_evidence_v11.64.08.json'))
req(ev['runtime_status'] == 'NOT_VERIFIED', 'runtime honesty')
req(ev['round'] == '11.64.08', 'evidence round')

print(f'TC4 v11.64.08 eldritch milestone guard: PASS ({len(methods)} GameTests; {len(ids)} scenarios; '
      f'MINOR/MAJOR grant research 10/20 silently, no fabricated chat)')
