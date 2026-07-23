#!/usr/bin/env python3
"""v11.64.07 guard: warp.text.8 is the BATHSALTS milestone line, NOT the
blurred-vision event line. Corrects the v11.64.06 misattribution."""
from pathlib import Path
import json, re
R = Path(__file__).resolve().parents[1]
def t(p): return (R/p).read_text(encoding='utf-8')
def req(c, m):
    if not c: raise SystemExit('TC4 v11.64.07 warp message guard: FAIL: ' + m)
def vtuple(raw):
    m = re.search(r'(?m)^\s*version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']', raw)
    req(m is not None, 'version parse'); return tuple(map(int, m.groups()))

req(vtuple(t('build.gradle')) >= (11, 64, 7), 'build version >=11.64.07')
req(vtuple(t('src/main/resources/META-INF/mods.toml')) >= (11, 64, 7), 'mods version >=11.64.07')

# --- parity contract ---
p = t('src/main/java/com/darkifov/thaumcraft/warp/TC4WarpRuntimeParity.java')
mc = re.search(r'CONTRACT_VERSION = "(\d+)\.(\d+)\.(\d+)"', p)
req(mc is not None and tuple(map(int, mc.groups())) >= (11, 64, 7), 'contract version >=11.64.07')
req('public static final String BATHSALTS_MILESTONE_MESSAGE_KEY = "warp.text.8";' in p,
    'BATHSALTS milestone message key constant missing')
req('BLURRED_VISION_MESSAGE_KEY' not in p, 'stale BLURRED_VISION_MESSAGE_KEY constant still present')

# --- WarpEvents: blurred-vision branch must NOT show a chat line ---
w = t('src/main/java/com/darkifov/thaumcraft/event/WarpEvents.java')
seg = w[w.find('private static void applyOriginalEvent'):w.find('private static void addWarpEffect')]
branch = seg[seg.find('effectRoll <= 36'):seg.find('effectRoll <= 40')]
req('message(' not in branch, 'blurred-vision branch (effectRoll<=36) must not display any chat line')
req('BLURRED_VISION_MESSAGE_KEY' not in w, 'WarpEvents still references the reverted blurred-vision key')
# warp.text.8 must NOT be a per-event displayed message
req('public static boolean usesWarpMessage(String key)' in w, 'usesWarpMessage accessor missing')
dset = w[w.find('DISPLAYED_WARP_MESSAGES'):w.find('public static boolean usesWarpMessage')]
req('"warp.text.8"' not in dset, 'warp.text.8 must be absent from the per-event displayed set')
# every OTHER original warp line (1..7, 9..15) is still displayed by the event table
for n in list(range(1, 8)) + list(range(9, 16)):
    req(('"warp.text.%d"' % n) in dset, 'warp.text.%d missing from per-event displayed set' % n)

# --- BATHSALTS milestone: warp.text.8 shown once on first unlock ---
e = t('src/main/java/com/darkifov/thaumcraft/eldritch/TC4EldritchProgression.java')
req('actualWarp > BATHSALTS_WARP' in e, 'BATHSALTS milestone gate missing')
bath = e[e.find('actualWarp > BATHSALTS_WARP'):e.find('actualWarp > ELDRITCH_MINOR_WARP')]
req('Component.translatable(TC4WarpRuntimeParity.BATHSALTS_MILESTONE_MESSAGE_KEY)' in bath,
    'BATHSALTS milestone does not display the translatable warp.text.8 line')
req('ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC' in bath,
    'BATHSALTS milestone message lost the original dark-purple italic styling')
req('hadBathSalts' in bath, 'BATHSALTS one-shot guard (hadBathSalts) missing')
req('Your mind searches for a way to cleanse itself.' not in e,
    'fabricated BATHSALTS literal must be replaced by the original warp.text.8 key')

# --- lang oracle: original 1.7.10 warp.text.8 is the headache line ---
orig = t('src/main/resources/assets/thaumcraft/original_tc4_1710/lang/en_US.lang')
mo = re.search(r'(?m)^warp\.text\.8=(.*)$', orig)
req(mo is not None and 'headache' in mo.group(1).lower(), 'original 1.7.10 warp.text.8 oracle mismatch')
lang = json.loads(t('src/main/resources/assets/thaumcraft/lang/en_us.json'))
req(lang.get('warp.text.8', '').strip().lower().startswith('surely there must be a way to stop'),
    'en_us warp.text.8 text mismatch')

# --- GameTest present, forward-compatible count ---
g = t('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods = re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(', g, re.S)
req(len(methods) >= 140, 'expected at least 140 GameTests, got %d' % len(methods))
req(len(methods) == len(set(methods)), 'duplicate GameTest names')
req('warpText8IsBathSaltsMilestoneNotBlurredVision' in methods, 'missing corrected warp.text.8 GameTest')
req('warpBlurredVisionUsesOriginalHeadacheMessage' not in methods, 'stale v11.64.06 GameTest still present')

# --- manifest scenario present, forward-compatible count ---
m = json.loads(t('runtime_artifacts/runtime_test_manifest.template.json'))
ids = [x['id'] for x in m['tests']]
req(tuple(map(int, m['version'].split('.'))) >= (11, 64, 7), 'manifest version >=11.64.07')
req(len(ids) >= 468 and len(ids) == len(set(ids)), 'expected at least 468 unique scenarios')
req('gametest.warp_text8_bathsalts_milestone' in ids, 'missing corrected manifest case')
req('gametest.warp_blurred_vision_headache_message' not in ids, 'stale v11.64.06 manifest case still present')

# --- source evidence honesty ---
ev = json.loads(t('tools/data/tc4_warp_message_source_evidence_v11.64.07.json'))
req(ev['runtime_status'] == 'NOT_VERIFIED', 'runtime honesty')
req('warp.text.8' in ev['confirmed_contract'], 'source evidence contract')
req(ev.get('supersedes') == '11.64.06', 'evidence must record the corrected round')

print(f'TC4 v11.64.07 warp message guard: PASS ({len(methods)} GameTests; {len(ids)} scenarios; '
      f'warp.text.8 correctly placed on the BATHSALTS milestone, blurred-vision shows no line)')
