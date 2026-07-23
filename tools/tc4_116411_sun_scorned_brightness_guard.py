#!/usr/bin/env python3
"""v11.64.11 guard: TC4 Sun Scorned brightness sampling and exact thresholds."""
from pathlib import Path
import json
import re
import zipfile

R = Path(__file__).resolve().parents[1]

def text(path):
    return (R / path).read_text(encoding='utf-8')

def req(condition, message):
    if not condition:
        raise SystemExit('TC4 v11.64.11 Sun Scorned brightness guard: FAIL: ' + message)

def version_tuple(raw):
    m = re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']', raw)
    req(m is not None, 'version parse')
    return tuple(map(int, m.groups()))

req(version_tuple(text('build.gradle')) >= (11, 64, 11), 'build version >= 11.64.11')
req(version_tuple(text('src/main/resources/META-INF/mods.toml')) >= (11, 64, 11), 'mods version >= 11.64.11')

parity = text('src/main/java/com/darkifov/thaumcraft/warp/TC4WarpRuntimeParity.java')
contract = re.search(r'CONTRACT_VERSION = "(\d+)\.(\d+)\.(\d+)"', parity)
req(contract and tuple(map(int, contract.groups())) >= (11, 64, 11), 'contract version >= 11.64.11')
for token in (
        'SUN_SCORNED_BURN_BRIGHTNESS = 0.5F',
        'SUN_SCORNED_HEAL_BRIGHTNESS = 0.25F',
        'SUN_SCORNED_RANDOM_SCALE = 30.0F',
        'SUN_SCORNED_BURN_OFFSET = 0.4F',
        'SUN_SCORNED_CHANCE_MULTIPLIER = 2.0F',
        'sunScornedBurns(',
        'brightness > SUN_SCORNED_BURN_BRIGHTNESS',
        'randomRoll * SUN_SCORNED_RANDOM_SCALE',
        'sunScornedHeals(',
        'brightness < SUN_SCORNED_HEAL_BRIGHTNESS',
        'randomRoll > brightness * SUN_SCORNED_CHANCE_MULTIPLIER'):
    req(token in parity, 'missing exact Sun Scorned contract token: ' + token)

production = text('src/main/java/com/darkifov/thaumcraft/effect/TC4WarpMobEffect.java')
start = production.index('private static void tickSunScorned')
end = production.index('private static void tickThaumarhia', start)
segment = production[start:end]
for token in (
        'float brightness = target.getLightLevelDependentMagicValue();',
        'brightness > TC4WarpRuntimeParity.SUN_SCORNED_BURN_BRIGHTNESS',
        'TC4WarpRuntimeParity.sunScornedBurns(',
        'target.level.canSeeSky(pos)',
        'target.setSecondsOnFire(4)',
        'brightness < TC4WarpRuntimeParity.SUN_SCORNED_HEAL_BRIGHTNESS',
        'TC4WarpRuntimeParity.sunScornedHeals(',
        'target.heal(1.0F)'):
    req(token in segment, 'production Sun Scorned wiring missing: ' + token)
req('getMaxLocalRawBrightness' not in segment, 'linear raw-light/15 sampling remains')
req('/ 15.0F' not in segment and '/15.0F' not in segment, 'raw light division remains')
# Keep the neutral band free of RNG consumption by nesting the draws inside the two branches.
first_random = segment.index('target.getRandom().nextFloat()')
bright_branch = segment.index('if (brightness > TC4WarpRuntimeParity.SUN_SCORNED_BURN_BRIGHTNESS)')
req(first_random > bright_branch, 'random draw moved before bright/dark admission')

with zipfile.ZipFile(R / 'reference/Thaumcraft4-1.7.10-4.2.3.5-source.zip') as z:
    name = next((n for n in z.namelist() if n.endswith('/thaumcraft/common/lib/potions/PotionSunScorned.java')), None)
    req(name is not None, 'original PotionSunScorned oracle missing')
    original = z.read(name).decode('utf-8', errors='replace')
for token in (
        'float f = target.func_70013_c(1.0F)',
        'f > 0.5F',
        'nextFloat() * 30.0F < (f - 0.4F) * 2.0F',
        'func_72937_j',
        'target.func_70015_d(4)',
        'f < 0.25F',
        'nextFloat() > f * 2.0F',
        'target.func_70691_i(1.0F)',
        'return par1 % 40 == 0'):
    req(token in original, 'original source evidence mismatch: ' + token)

gametest = text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods = re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(', gametest, re.S)
req(len(methods) >= 144, f'expected at least 144 GameTests, got {len(methods)}')
req(len(methods) == len(set(methods)), 'duplicate GameTest method names')
req('sunScornedUsesBrightnessTableThresholdsAndExactRolls' in methods, 'Sun Scorned GameTest missing')
for token in ('0.51F, 0.0F, true', '0.50F, 0.0F, true', '0.24F, 0.99F', '0.25F, 0.99F', '0.10F, 0.20F'):
    req(token in gametest, 'Sun Scorned boundary fixture missing: ' + token)

manifest = json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids = [case['id'] for case in manifest['tests']]
req(tuple(map(int, manifest['version'].split('.'))) >= (11, 64, 11), 'manifest version >= 11.64.11')
req(len(ids) >= 472 and len(ids) == len(set(ids)), 'expected at least 472 unique manifest scenarios')
req('gametest.sun_scorned_brightness_table_thresholds' in ids, 'Sun Scorned manifest scenario missing')

evidence = json.loads(text('tools/data/tc4_sun_scorned_brightness_source_evidence_v11.64.11.json'))
req(evidence['round'] == '11.64.11', 'evidence round mismatch')
req(evidence['runtime_status'] == 'NOT_VERIFIED', 'runtime honesty drifted')
req(evidence['build_status'] == 'NOT_OBTAINED', 'build honesty drifted')

print(f'TC4 v11.64.11 Sun Scorned brightness guard: PASS '
      f'({len(methods)} GameTests; {len(ids)} scenarios; brightness table + exact strict thresholds/rolls)')
