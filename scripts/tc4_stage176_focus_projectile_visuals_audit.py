#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

renderer = read('src/main/java/com/darkifov/thaumcraft/client/render/TC4FocusProjectileRenderer.java')
explosive = read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4ExplosiveOrbEntity.java')
frost = read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4FrostShardEntity.java')
shock = read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4ShockOrbEntity.java')
primal = read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4PrimalOrbEntity.java')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')

for token in [
    'TC4 focus projectile visual adapter',
    'textures/original/thaumcraft4/blocks/frostshard.png',
    'textures/original/thaumcraft4/misc/particles.png',
    'textures/original/thaumcraft4/misc/particles2.png',
    'TC4FrostShardEntity',
    'TC4ExplosiveOrbEntity',
    'TC4ShockOrbEntity',
    'TC4PrimalOrbEntity',
    'entityRenderDispatcher.cameraOrientation()',
    'entity.tickCount % 4',
    'entity.tickCount % 6',
    'entity.tickCount % 13',
    '0.8125F',
    '0.875F',
    '0.125F',
    'Aspect.AER.argbColor()',
]:
    if token not in renderer:
        errors.append(f'renderer missing TC4 visual parity token {token}')

if '// Renderer parity will be handled' in renderer or 'blank.png' in renderer:
    errors.append('projectile renderer still contains Stage174 placeholder/no-op texture path')

projectiles = {
    'explosive': explosive,
    'frost': frost,
    'shock': shock,
    'primal': primal,
}
required = {
    'explosive': ['private float strength = 1.0F', 'ParticleTypes.FLAME', 'ParticleTypes.SMOKE', 'maxLife = 500'],
    'frost': ['ParticleTypes.SNOWFLAKE', 'TC4Sounds.event("ice")', 'new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, frosty - 1)', 'if (frosty > 0)', 'maxLife = 500'],
    'shock': ['private float area = 4.0F', 'damage = 5.0F', 'ParticleTypes.ELECTRIC_SPARK', 'TC4Sounds.event("shock")', 'server.sendParticles', 'maxLife = 500'],
    'primal': ['maxLife = 5000', 'life > 20', 'inflate(16.0D)', 'dx * 0.2D', 'dy * 0.2D', 'dz * 0.2D', 'Mth.clamp', 'ParticleTypes.ENCHANT', 'ParticleTypes.WITCH'],
}
for name, tokens in required.items():
    text = projectiles[name]
    for token in tokens:
        if token not in text:
            errors.append(f'{name} projectile missing token {token}')

if 'Blocks.SNOW' in frost:
    errors.append('FrostShard must not place snow on hit; original TC4 renderer/effect path does not create snow blocks')

for token in ['tc4_stage176_focus_projectile_visuals_audit.py', 'python scripts/tc4_stage176_focus_projectile_visuals_audit.py', 'thaumcraft-legacy-rebuild-stage204-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing {token}')

if not (("version = '2.04.0'" in build or "version = '1.98.0'" in build or "version = '2.00.0'" in build) and ('version="2.04.0"' in mods or 'version="1.98.0"' in mods or 'version="2.00.0"' in mods)):
    errors.append('project version must be 1.98.0 or 2.00.0')

if errors:
    for e in errors:
        print('::error::' + e)
    sys.exit(1)
print('Stage176 focus projectile visuals audit: OK')
