#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

focus = read('src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')

for token in [
    'playsOwnActivationSound',
    'case FROST, PORTABLE_HOLE, PRIMAL -> true',
    'FocusUpgradeType.FIREBALL',
    'FocusUpgradeType.EARTH_SHOCK',
    'level.levelEvent(null, 1009, player.blockPosition(), 0)',
    'HitBundle hit = ray(level, player, 17.0D)',
    'TC4Sounds.event("ice"), SoundSource.PLAYERS, 0.4F, 1.0F + level.random.nextFloat() * 0.1F',
    'TC4Sounds.event("ice"), SoundSource.PLAYERS, 0.3F, 0.8F + level.random.nextFloat() * 0.1F',
    '3 + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE) * 2',
    'WARDING_DELAY',
    'now + 500L',
    'System.currentTimeMillis() / 200L',
    'new java.util.Random',
]:
    if token not in focus:
        errors.append(f'WandFocusRuntime missing Stage184 behavior token {token}')

if 'level.playSound(null, player.blockPosition(), soundFor(type)' not in focus:
    errors.append('generic focus sound fallback missing')
if 'if (!playsOwnActivationSound(wandStack, type))' not in focus:
    errors.append('generic focus sound fallback is not gated by original own-sound logic')

for token in ['tc4_stage184_remaining_focus_behavior_audit.py', 'python scripts/tc4_stage184_remaining_focus_behavior_audit.py', 'thaumcraft-legacy-rebuild-stage194-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing Stage184 token {token}')

if "version = '1.94.0'" not in build or 'version="1.94.0"' not in mods:
    errors.append('project version must be 1.94.0')

if errors:
    for e in errors:
        print('::error::' + e)
    sys.exit(1)
print('Stage184 remaining focus behavior audit: OK')
