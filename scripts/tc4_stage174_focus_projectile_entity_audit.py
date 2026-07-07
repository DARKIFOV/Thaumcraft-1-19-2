#!/usr/bin/env python3
from pathlib import Path
import json, sys
ROOT=Path(__file__).resolve().parents[1]
errors=[]

def read(rel):
    p=ROOT/rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

mapping=ROOT/'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_focus_projectile_entities_stage174.json'
mod=read('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
runtime=read('src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java')
client=read('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
base=read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4FocusProjectileEntity.java')
renderer=read('src/main/java/com/darkifov/thaumcraft/client/render/TC4FocusProjectileRenderer.java')
workflow=read('.github/workflows/main.yml')
guard=read('scripts/github_ci_guard.py')
build=read('build.gradle')
mods=read('src/main/resources/META-INF/mods.toml')
if not mapping.exists():
    errors.append('missing tc4_focus_projectile_entities_stage174.json')
else:
    data=json.loads(mapping.read_text(encoding='utf-8'))
    ids={e['id'] for e in data.get('entities',[])}
    for eid in ['thaumcraft:focus_frost_shard','thaumcraft:focus_explosive_orb','thaumcraft:focus_shock_orb','thaumcraft:focus_primal_orb']:
        if eid not in ids:
            errors.append(f'missing projectile mapping {eid}')
for rel in ['TC4FocusProjectileEntity.java','TC4FrostShardEntity.java','TC4ExplosiveOrbEntity.java','TC4ShockOrbEntity.java','TC4PrimalOrbEntity.java']:
    if not (ROOT/'src/main/java/com/darkifov/thaumcraft/entity/projectile'/rel).exists():
        errors.append(f'missing projectile class {rel}')
for token in ['FOCUS_FROST_SHARD', 'FOCUS_EXPLOSIVE_ORB', 'FOCUS_SHOCK_ORB', 'FOCUS_PRIMAL_ORB']:
    if token not in mod:
        errors.append(f'ThaumcraftMod missing entity registry {token}')
    if token not in client:
        errors.append(f'ClientModEvents missing renderer for {token}')
for token in ['TC4FrostShardEntity', 'TC4ExplosiveOrbEntity', 'TC4ShockOrbEntity', 'TC4PrimalOrbEntity', 'shootProjectile']:
    if token not in runtime:
        errors.append(f'WandFocusRuntime missing projectile use {token}')
for token in ['NetworkHooks.getEntitySpawningPacket', 'onHitLiving', 'onHitBlockTC4']:
    if token not in base:
        errors.append(f'base projectile missing {token}')
if 'ProjectileUtil.getHitResult(this, this::canHitEntity)' not in base:
    errors.append('base projectile missing Forge 1.19.2 official hit-result adapter ProjectileUtil.getHitResult')
if 'TC4 focus projectile visual adapter' not in renderer and 'minimal projectile renderer placeholder' not in renderer:
    errors.append('projectile renderer must document either Stage174 placeholder or later TC4 visual adapter')
for token in ['tc4_stage174_focus_projectile_entity_audit.py','python scripts/tc4_stage174_focus_projectile_entity_audit.py','thaumcraft-legacy-rebuild-stage204-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing {token}')
if not (("version = '2.04.0'" in build or "version = '1.98.0'" in build or "version = '2.00.0'" in build) and ('version="2.04.0"' in mods or 'version="1.98.0"' in mods or 'version="2.00.0"' in mods)):
    errors.append('project version must be 1.98.0 or 2.00.0')
if errors:
    for e in errors:
        print('::error::'+e)
    sys.exit(1)
print('Stage174 focus projectile entity audit: OK')
