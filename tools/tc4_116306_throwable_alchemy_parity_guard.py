#!/usr/bin/env python3
"""Static source/resource contract guard for v11.63.06 throwable Alumentum and Bottled Taint."""
from __future__ import annotations
import hashlib, json, sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def text(rel):
    p=ROOT/rel
    return p.read_text(encoding='utf-8',errors='ignore') if p.is_file() else ''
def check(name,ok): checks.append((name,bool(ok)))
def contains(rel,*tokens):
    body=text(rel)
    for token in tokens: check(f'{rel}:{token[:54]}',token in body)

build=text('build.gradle'); mods=text('src/main/resources/META-INF/mods.toml')
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids={x.get('id') for x in manifest.get('tests',[])}
check('build_version',"version = '11.63.23'" in build)
check('mods_version','version="11.63.23"' in mods)
check('manifest_version',manifest.get('version') in ('11.63.23','11.63.24','11.63.26','11.63.27','11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61'))
check('manifest_count_at_least_106',len(manifest.get('tests',[]))>=106)

contains('src/main/java/com/darkifov/thaumcraft/entity/projectile/AlumentumProjectileEntity.java',
         'extends ThrowableItemProjectile','super(type, owner, level)','super(type, x, y, z, level)',
         'tc4_alumentum','for (int i = 0; i < 3; i++)','ParticleTypes.SOUL_FIRE_FLAME',
         'ParticleTypes.ELECTRIC_SPARK','RULE_MOBGRIEFING','1.66F',
         'Explosion.BlockInteraction.DESTROY','Explosion.BlockInteraction.NONE','discard()',
         'public void addAdditionalSaveData','NetworkHooks.getEntitySpawningPacket')
contains('src/main/java/com/darkifov/thaumcraft/entity/projectile/BottleTaintProjectileEntity.java',
         'extends ThrowableItemProjectile','tc4_bottle_taint','return 0.05F','inflate(5.0D)',
         'living instanceof TaintedMob','living.getMobType() == MobType.UNDEAD',
         'TAINT_POISON.get(), 100, 0','for (int i = 0; i < 10; i++)',
         '* 5.0F','random.nextBoolean()','TaintSpreadRuntime.isColumnTainted',
         'new BlockPos(Mth.floor(getX()), Mth.floor(getY()), Mth.floor(getZ()))','TaintSpreadRuntime.markTaintedColumn','TAINT_FIBRES.get().defaultBlockState()',
         'setValue(TaintFibresBlock.AGE, 0)','ParticleTypes.WITCH','ParticleTypes.ITEM_SLIME',
         'public void addAdditionalSaveData','discard()','NetworkHooks.getEntitySpawningPacket')
contains('src/main/java/com/darkifov/thaumcraft/item/AlumentumItem.java',
         'extends TC4ResearchComponentItem','SoundEvents.ARROW_SHOOT','SoundSource.NEUTRAL','0.3F',
         'ALUMENTUM_PROJECTILE.get()','shootFromRotation','0.75F, 1.0F','instabuild','stack.shrink(1)',
         'InteractionResultHolder.sidedSuccess')
contains('src/main/java/com/darkifov/thaumcraft/item/BottleTaintItem.java',
         'extends TC4ResearchComponentItem','SoundEvents.ARROW_SHOOT','0.5F',
         'BOTTLE_TAINT_PROJECTILE.get()','shootFromRotation','-20.0F, 0.5F, 1.0F',
         'instabuild','stack.shrink(1)','InteractionResultHolder.sidedSuccess')
contains('src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java',
         'case "tc4_alumentum" -> new AlumentumItem(functionalProperties',
         'case "tc4_bottle_taint" -> new BottleTaintItem(functionalProperties.stacksTo(8)')
contains('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java',
         'ENTITY_TYPES.register("alumentum_projectile"','ENTITY_TYPES.register("bottle_taint_projectile"',
         '.sized(0.25F, 0.25F)','.clientTrackingRange(8)','.updateInterval(10)',
         'AbstractProjectileDispenseBehavior','ALUMENTUM_PROJECTILE.get(), level, position.x()',
         'BOTTLE_TAINT_PROJECTILE.get(), level, position.x()','protected float getPower()','return 0.75F','return 0.5F')
contains('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java',
         'ThaumcraftMod.ALUMENTUM_PROJECTILE.get()','ThrownItemRenderer<>(ctx, 1.0F, true)',
         'ThaumcraftMod.BOTTLE_TAINT_PROJECTILE.get()','ThrownItemRenderer<>(ctx, 0.8F, false)')

for rel,sha in {
 'src/main/resources/assets/thaumcraft/textures/item/tc4/alumentum.png':'74bb2fb540c319f23142f32c5f936b38dd9186f49473788107e82ac9f18c43f9',
 'src/main/resources/assets/thaumcraft/textures/item/tc4/bottle_taint.png':'a99ca9346839431821e0a121f7f2ebe72aefa32f47822c6ee347c9bf2902825d',
 'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/alumentum.png':'74bb2fb540c319f23142f32c5f936b38dd9186f49473788107e82ac9f18c43f9',
 'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/bottle_taint.png':'a99ca9346839431821e0a121f7f2ebe72aefa32f47822c6ee347c9bf2902825d',
}.items():
    p=ROOT/rel; check('texture_exists:'+rel,p.is_file()); check('texture_sha:'+rel,p.is_file() and hashlib.sha256(p.read_bytes()).hexdigest()==sha)
for item,tex in [('tc4_alumentum','thaumcraft:item/tc4/alumentum'),('tc4_bottle_taint','thaumcraft:item/tc4/bottle_taint')]:
    try: model=json.loads(text(f'src/main/resources/assets/thaumcraft/models/item/{item}.json'))
    except Exception: model={}
    check('model_parent:'+item,model.get('parent')=='item/generated')
    check('model_texture:'+item,model.get('textures',{}).get('layer0')==tex)
for lang in ('en_us','ru_ru'):
    try: data=json.loads(text(f'src/main/resources/assets/thaumcraft/lang/{lang}.json'))
    except Exception: data={}
    for key in ('item.thaumcraft.tc4_alumentum','item.thaumcraft.tc4_bottle_taint',
                'entity.thaumcraft.alumentum_projectile','entity.thaumcraft.bottle_taint_projectile'):
        check(f'lang:{lang}:{key}',bool(data.get(key)))
for tid in (
 'alchemy.alumentum_right_click_sound_velocity_and_consumption',
 'alchemy.alumentum_impact_explosion_strength_and_mobgriefing',
 'alchemy.alumentum_dispenser_projectile_path',
 'taint.bottled_taint_stack_limit_sound_velocity_and_consumption',
 'taint.bottled_taint_radius_poison_and_entity_exclusions',
 'taint.bottled_taint_columns_fibres_particles_and_dispenser'):
    check('manifest:'+tid,tid in ids)
contains('src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java','AlumentumItem','BottleTaintItem')
for wf in ('.github/workflows/build.yml','.github/workflows/release.yml'):
    body=text(wf)
    check('workflow_guard:'+wf,'tc4_116306_throwable_alchemy_parity_guard.py' in body)
    check('workflow_version:'+wf,'11.63.23' in body)
failed=[n for n,ok in checks if not ok]
for n,ok in checks: print(('PASS' if ok else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed')
if failed: sys.exit(1)
