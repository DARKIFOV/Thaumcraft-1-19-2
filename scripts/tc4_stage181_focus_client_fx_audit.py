#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

focus = read('src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java')
bridge = read('src/main/java/com/darkifov/thaumcraft/porting/TC4ClientFocusFxBridge.java')
fx = read('src/main/java/com/darkifov/thaumcraft/client/fx/TC4ClientFocusFx.java')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')

for token in [
    'TC4ClientFocusFxBridge.sparkleCloud(level, hit.end(), spread, 5, 2)',
    'TC4ClientFocusFxBridge.shockLightning(level, player, hit.end(), true)',
    'TC4ClientFocusFxBridge.beamCont(level, player, end, 2, 65382, false, impact > 0 ? 2.0F : 0.0F, impact)',
    'TC4ClientFocusFxBridge.excavateFX(level, target, player, progress)',
    'int progress = Math.min(9, Math.max(0, (int) (bc / hardness * 9.0F)))',
]:
    if token not in focus:
        errors.append(f'WandFocusRuntime missing Stage181 client FX call {token}')

for token in [
    'Original TC4 invoked client-only proxy methods directly from focus classes',
    'beamCont(Level level, Player player, Vec3 target, int type, int color, boolean reverse, float endMod, int impact)',
    'sparkleCloud(Level level, Vec3 pos, float spread, int count, int colorType)',
    'shockLightning(Level level, Player player, Vec3 target, boolean offset)',
    'excavateFX(Level level, BlockPos pos, Player player, int progress)',
    'DistExecutor.unsafeRunWhenOn(Dist.CLIENT',
    'TC4ClientFocusFx.beamCont',
    'TC4ClientFocusFx.sparkleCloud',
    'TC4ClientFocusFx.shockLightning',
    'TC4ClientFocusFx.excavateFX',
]:
    if token not in bridge:
        errors.append(f'TC4ClientFocusFxBridge missing original proxy adapter token {token}')

for token in [
    'FXBeamWand, FXLightningBolt',
    'beamCont(Player player, Vec3 target, int type, int color, boolean reverse, float endMod, int impact)',
    'new DustParticleOptions(new Vector3f(rgb[0], rgb[1], rgb[2])',
    'ParticleTypes.ELECTRIC_SPARK',
    'ParticleTypes.ENCHANT',
    'int segments = 6; // original FXLightningBolt(world, ..., 6, 0.5F, 8)',
    'renderSegment(previous, current, 0x99CCFF, 5)',
    'mc.levelRenderer.destroyBlockProgress(player.getId(), pos',
    'wandTip(Player player)',
]:
    if token not in fx:
        errors.append(f'TC4ClientFocusFx missing Stage181 token {token}')

for token in ['tc4_stage181_focus_client_fx_audit.py', 'python scripts/tc4_stage181_focus_client_fx_audit.py', 'thaumcraft-legacy-rebuild-stage204-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing Stage181 token {token}')

if not (("version = '2.04.0'" in build or "version = '1.98.0'" in build or "version = '2.00.0'" in build) and ('version="2.04.0"' in mods or 'version="1.98.0"' in mods or 'version="2.00.0"' in mods)):
    errors.append('project version must be 1.98.0 or 2.00.0')

if errors:
    for e in errors:
        print('::error::' + e)
    sys.exit(1)
print('Stage181 focus client FX audit: OK')
