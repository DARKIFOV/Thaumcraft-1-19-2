#!/usr/bin/env python3
"""Static source/resource contract guard for v11.63.07 Primal Arrows and golem dart."""
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
    for token in tokens: check(f'{rel}:{token[:64]}',token in body)

build=text('build.gradle'); mods=text('src/main/resources/META-INF/mods.toml')
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids={x.get('id') for x in manifest.get('tests',[])}
check('build_version',"version = '11.63.23'" in build)
check('mods_version','version="11.63.23"' in mods)
check('manifest_version',manifest.get('version') in ('11.63.23','11.63.24','11.63.26','11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61'))
check('manifest_count_at_least_124',len(manifest.get('tests',[]))>=124)

contains('src/main/java/com/darkifov/thaumcraft/item/PrimalArrowItem.java',
         'extends ArrowItem','private final int primalType','Math.max(0, Math.min(5, primalType))',
         'public AbstractArrow createArrow','new PrimalArrowEntity(level, shooter, primalType)')
contains('src/main/java/com/darkifov/thaumcraft/entity/projectile/PrimalArrowEntity.java',
         'extends Arrow','SynchedEntityData.defineId','EntityDataSerializers.BYTE',
         'setBaseDamage(2.1D)','case 3 -> setBaseDamage(getBaseDamage() * 1.5D)',
         'case 4, 5 -> setBaseDamage(getBaseDamage() * 0.8D)',
         'target.setSecondsOnFire(isOnFire() ? 10 : 5)',
         'MobEffects.MOVEMENT_SLOWDOWN, 200, 4','MobEffects.POISON, 200, 4',
         'MobEffects.WEAKNESS, 100, 0','instanceof net.minecraft.world.entity.monster.EnderMan',
         'DustParticleOptions','COLORS[Math.min(getPrimalType()',
         'tc4_el_arrow_air','tc4_el_arrow_fire','tc4_el_arrow_water','tc4_el_arrow_earth',
         'tc4_el_arrow_order','tc4_el_arrow_entropy','TC4PrimalType',
         'NetworkHooks.getEntitySpawningPacket(this)')
contains('src/main/java/com/darkifov/thaumcraft/entity/projectile/GolemDartEntity.java',
         'extends Arrow','pickup = Pickup.DISALLOWED','for (int i = 0; i < 5; i++)',
         'ParticleTypes.SMOKE','return ItemStack.EMPTY','TC4Original','EntityDart',
         'NetworkHooks.getEntitySpawningPacket(this)')
contains('src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java',
         'case "tc4_el_arrow_air" -> new PrimalArrowItem(functionalProperties, 0)',
         'case "tc4_el_arrow_fire" -> new PrimalArrowItem(functionalProperties, 1)',
         'case "tc4_el_arrow_water" -> new PrimalArrowItem(functionalProperties, 2)',
         'case "tc4_el_arrow_earth" -> new PrimalArrowItem(functionalProperties, 3)',
         'case "tc4_el_arrow_order" -> new PrimalArrowItem(functionalProperties, 4)',
         'case "tc4_el_arrow_entropy" -> new PrimalArrowItem(functionalProperties, 5)')
contains('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java',
         'ENTITY_TYPES.register("primal_arrow"','PrimalArrowEntity::new',
         'ENTITY_TYPES.register("golem_dart"','GolemDartEntity::new',
         '.sized(0.5F, 0.5F)','.clientTrackingRange(64)','.updateInterval(20)')
contains('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java',
         'GolemDartEntity dart = new GolemDartEntity(level, this)',
         'dart.shoot(dx, dy + horizontal * 0.2D, dz, 1.6F',
         'dart.setBaseDamage(getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4D)',
         'level.addFreshEntity(dart)')
check('golem_no_vanilla_arrow_spawn','Arrow dart = new Arrow(level, this)' not in text('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java'))
contains('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java',
         'ThaumcraftMod.PRIMAL_ARROW.get()','TC4PrimalArrowRenderer::new',
         'ThaumcraftMod.GOLEM_DART.get()','TC4GolemDartRenderer::new')
contains('src/main/java/com/darkifov/thaumcraft/client/render/TC4PrimalArrowRenderer.java',
         'extends ArrowRenderer<PrimalArrowEntity>','textures/entity/projectiles/arrow.png')
contains('src/main/java/com/darkifov/thaumcraft/client/render/TC4GolemDartRenderer.java',
         'extends ArrowRenderer<GolemDartEntity>','poseStack.scale(0.75F, 0.75F, 0.75F)',
         'textures/entity/projectiles/arrow.png')
contains('src/main/java/com/darkifov/thaumcraft/item/BoneBowItem.java',
         'projectile.getItem() instanceof ArrowItem','arrowItem.createArrow(level, projectile, player)')

items=('air','fire','water','earth','order','entropy')
for name in items:
    rel=f'src/main/resources/assets/thaumcraft/textures/item/tc4/el_arrow_{name}.png'
    orig=f'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/el_arrow_{name}.png'
    a,b=ROOT/rel,ROOT/orig
    check('texture_exists:'+name,a.is_file())
    check('original_texture_exists:'+name,b.is_file())
    check('texture_byte_exact:'+name,a.is_file() and b.is_file() and hashlib.sha256(a.read_bytes()).digest()==hashlib.sha256(b.read_bytes()).digest())
    try:model=json.loads(text(f'src/main/resources/assets/thaumcraft/models/item/tc4_el_arrow_{name}.json'))
    except Exception:model={}
    check('model_parent:'+name,model.get('parent')=='item/generated')
    check('model_texture:'+name,model.get('textures',{}).get('layer0')==f'thaumcraft:item/tc4/el_arrow_{name}')

for lang in ('en_us','ru_ru'):
    try:data=json.loads(text(f'src/main/resources/assets/thaumcraft/lang/{lang}.json'))
    except Exception:data={}
    for name in items:
        check(f'lang:{lang}:arrow:{name}',bool(data.get(f'item.thaumcraft.tc4_el_arrow_{name}')))
    for key in ('entity.thaumcraft.primal_arrow','entity.thaumcraft.golem_dart'):
        check(f'lang:{lang}:{key}',bool(data.get(key)))
check('english_placeholder_removed','TC4 El Arrow' not in text('src/main/resources/assets/thaumcraft/lang/en_us.json'))

for tid in (
 'combat.primal_arrow_six_items_vanilla_and_bone_bow_creation',
 'combat.primal_arrow_base_earth_order_entropy_damage_curve',
 'combat.primal_arrow_fire_duration_and_enderman_exception',
 'combat.primal_arrow_water_order_entropy_status_effects',
 'combat.primal_arrow_type_sync_nbt_pickup_and_coloured_trail',
 'golems.dart_launcher_custom_projectile_smoke_damage_and_no_pickup'):
    check('manifest:'+tid,tid in ids)
contains('KNOWN_DEVIATIONS.md','Primal Arrow and golem dart runtime proof','v11.63.07','coloured particle trail')
contains('README.md','11.63.07 — Primal Arrow and golem dart projectile parity','six inert primal-arrow research items','dedicated non-pickup dart entity')
for wf in ('.github/workflows/build.yml','.github/workflows/release.yml'):
    body=text(wf)
    check('workflow_guard:'+wf,'tc4_116307_primal_arrow_dart_parity_guard.py' in body)
    check('workflow_version:'+wf,'11.63.23' in body)
failed=[n for n,ok in checks if not ok]
for n,ok in checks: print(('PASS' if ok else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed')
if failed: sys.exit(1)
