#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

base = read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4FocusProjectileEntity.java')
frost = read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4FrostShardEntity.java')
explosive = read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4ExplosiveOrbEntity.java')
shock = read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4ShockOrbEntity.java')
primal = read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4PrimalOrbEntity.java')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')

for token in [
    'tc4Gravity()',
    'shouldDiscardAfterEntityHit',
    'setDeltaMovement(getDeltaMovement().add(0.0D, -tc4Gravity(), 0.0D))',
]:
    if token not in base:
        errors.append(f'base projectile missing Stage178 gravity/lifecycle token {token}')

for token in [
    'return fragile ? 0.015D : 0.05D',
    'bounceLimit-- > 0',
    '0.66D * bounce',
    'hit.getDirection().getAxis()',
    'return fragile || bounceLimit-- <= 0',
    'ParticleTypes.SNOWFLAKE',
]:
    if token not in frost:
        errors.append(f'frost shard missing original bounce/frost token {token}')

for token in [
    'return 0.01D',
    'strength * 1.5F',
    'DamageSource.ON_FIRE',
    'Explosion.BlockInteraction.NONE',
    'maxLife = 500',
]:
    if token not in explosive:
        errors.append(f'explosive orb missing original behavior token {token}')

for token in [
    'return 0.05D',
    'level.getEntities(this, getBoundingBox().inflate(area)',
    'canSeeFromOrb',
    'ClipContext.Block.COLLIDER',
    'entity.hurt(DamageSource.LIGHTNING_BOLT, damage)',
    'TC4Sounds.event("shock")',
]:
    if token not in shock:
        errors.append(f'shock orb missing original AoE/LOS token {token}')

for token in [
    'return 0.001D',
    'new java.util.Random(getId() + life)',
    'distanceToSqr(target)',
    'Mth.clamp',
    'float specialChance = isInWaterOrBubble() ? 10.0F : 1.0F',
    'float strength = isInWaterOrBubble() ? 4.0F : 2.0F',
    'Explosion.BlockInteraction.BREAK',
    'taintSplosion',
    'ThaumcraftMod.AURA_NODE',
    'ThaumcraftMod.TAINT_FIBRES',
    'maxLife = 5000',
]:
    if token not in primal:
        errors.append(f'primal orb missing original behavior token {token}')

for forbidden in ['applyPrimal(', 'primalBlock(']:
    if forbidden in base:
        errors.append(f'base projectile still contains non-original Stage174/176 primal adapter {forbidden}')

for token in ['tc4_stage178_projectile_behavior_audit.py', 'python scripts/tc4_stage178_projectile_behavior_audit.py', 'thaumcraft-legacy-rebuild-stage194-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing {token}')

if errors:
    for e in errors:
        print('::error::' + e)
    sys.exit(1)
print('Stage178 projectile behavior audit: OK')
