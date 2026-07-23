#!/usr/bin/env python3
"""v11.64.10 guard: exact TC4 Death Gaze cone geometry and target admission."""
from pathlib import Path
import json
import re

R = Path(__file__).resolve().parents[1]

def t(path):
    return (R / path).read_text(encoding='utf-8')

def req(condition, message):
    if not condition:
        raise SystemExit('TC4 v11.64.10 Death Gaze cone guard: FAIL: ' + message)

def version_tuple(raw):
    m = re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']', raw)
    req(m is not None, 'version parse')
    return tuple(map(int, m.groups()))

req(version_tuple(t('build.gradle')) >= (11, 64, 10), 'build version >= 11.64.10')
req(version_tuple(t('src/main/resources/META-INF/mods.toml')) >= (11, 64, 10), 'mods version >= 11.64.10')

parity = t('src/main/java/com/darkifov/thaumcraft/warp/TC4WarpRuntimeParity.java')
contract = re.search(r'CONTRACT_VERSION = "(\d+)\.(\d+)\.(\d+)"', parity)
req(contract and tuple(map(int, contract.groups())) >= (11, 64, 10), 'contract version >= 11.64.10')
for token in (
        'DEATH_GAZE_APERTURE_RADIANS = 0.75D',
        'deathGazeConeContains(',
        'Math.cos(DEATH_GAZE_APERTURE_RADIANS / 2.0D)',
        'double axialProjection = dot / lookMagnitude',
        'return axialProjection < range;'):
    req(token in parity, 'missing exact cone token: ' + token)
req('cosine > Math.cos' in parity, 'original strict cosine boundary missing')

warp = t('src/main/java/com/darkifov/thaumcraft/event/WarpEvents.java')
start = warp.index('private static void checkDeathGaze')
end = warp.index('private static void grantResearch', start)
segment = warp[start:end]
for token in (
        'target -> target != player && target.isAlive()',
        '!target.isPickable()'):
    req(token in segment, 'production Death Gaze admission missing: ' + token)
for token in (
        'target.getBoundingBox().minY + target.getBbHeight() / 2.0D',
        'TC4WarpRuntimeParity.deathGazeConeContains('):
    req(token in warp, 'production Death Gaze geometry wiring missing: ' + token)
req('distanceToSqr' not in segment, 'non-original spherical distance gate remains')
req('target.getEyePosition()' not in warp, 'Death Gaze still aims at target eyes instead of body midpoint')
req('>= 0.75D' not in segment and '>= 0.75F' not in segment,
    'old too-wide direct dot>=0.75 comparison remains')

# Link to original oracle snippets embedded in the archive.
orig_warp = Path('/mnt/data/tc4_original/Thaumcraft4-1.7.10-master/thaumcraft/common/lib/WarpEvents.java')
orig_entity = Path('/mnt/data/tc4_original/Thaumcraft4-1.7.10-master/thaumcraft/common/lib/utils/EntityUtils.java')
orig_utils = Path('/mnt/data/tc4_original/Thaumcraft4-1.7.10-master/thaumcraft/common/lib/utils/Utils.java')
# The guard must also work after unpacking elsewhere, so source-text checks use
# the retained zip through Python's zipfile rather than relying on those temp paths.
import zipfile
with zipfile.ZipFile(R / 'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    names = z.namelist()
    def ztext(suffix):
        name = next((n for n in names if n.endswith(suffix)), None)
        req(name is not None, 'original oracle member missing: ' + suffix)
        return z.read(name).decode('utf-8', errors='replace')
    ow = ztext('/thaumcraft/common/lib/WarpEvents.java')
    oe = ztext('/thaumcraft/common/lib/utils/EntityUtils.java')
    ou = ztext('/thaumcraft/common/lib/utils/Utils.java')
req('isVisibleTo(0.75F, player, entity, range)' in ow, 'original Death Gaze aperture call mismatch')
req('ent2.field_70121_D.field_72338_b + ent2.field_70131_O / 2.0F' in oe,
    'original target body-midpoint evidence mismatch')
req('double halfAperture = aperture / 2.0F' in ou, 'original half-aperture evidence mismatch')
req('Math.cos(halfAperture)' in ou, 'original cosine evidence mismatch')
req('isUnderRoundCap' in ou and 'magn(axisVect)' in ou, 'original round-cap evidence mismatch')

gametest = t('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods = re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(', gametest, re.S)
req(len(methods) >= 143, f'expected at least 143 GameTests, got {len(methods)}')
req(len(methods) == len(set(methods)), 'duplicate GameTest method names')
req('deathGazeUsesTc4ApertureBodyCenterAndRoundCap' in methods, 'Death Gaze geometry GameTest missing')
req('Math.toRadians(30.0D)' in gametest and 'Math.tan(twentyDegrees) * axial' in gametest,
    'GameTest does not cover old wide cone and spherical-gate regression')

manifest = json.loads(t('runtime_artifacts/runtime_test_manifest.template.json'))
ids = [case['id'] for case in manifest['tests']]
req(tuple(map(int, manifest['version'].split('.'))) >= (11, 64, 10), 'manifest version >= 11.64.10')
req(len(ids) >= 471 and len(ids) == len(set(ids)), 'expected at least 471 unique manifest scenarios')
req('gametest.death_gaze_tc4_cone_geometry' in ids, 'Death Gaze manifest scenario missing')

evidence = json.loads(t('tools/data/tc4_death_gaze_cone_source_evidence_v11.64.10.json'))
req(evidence['round'] == '11.64.10', 'evidence round mismatch')
req(evidence['runtime_status'] == 'NOT_VERIFIED', 'runtime honesty drifted')
req(evidence['build_status'] == 'NOT_OBTAINED', 'build honesty drifted')

print(f'TC4 v11.64.10 Death Gaze cone guard: PASS '
      f'({len(methods)} GameTests; {len(ids)} scenarios; exact aperture/body midpoint/round cap/pickable gate)')
