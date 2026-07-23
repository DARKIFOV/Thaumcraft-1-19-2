#!/usr/bin/env python3
"""Static source/resource parity guard for v11.62.99 taint death conversion."""
from pathlib import Path
import hashlib, json, sys
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'src/main/java/com/darkifov/thaumcraft'
RES=ROOT/'src/main/resources/assets/thaumcraft'
OLD=Path('/mnt/data/tc_refs/tc4src/Thaumcraft4-1.7.10-master/assets/thaumcraft/textures/models')
checks=[]
def check(name, ok): checks.append((name,bool(ok)))
def text(p): return p.read_text(encoding='utf-8')

build=text(ROOT/'build.gradle'); mods=text(ROOT/'src/main/resources/META-INF/mods.toml')
mod=text(JAVA/'ThaumcraftMod.java'); client=text(JAVA/'client/ClientModEvents.java')
events=text(JAVA/'event/CommonEvents.java'); runtime=text(JAVA/'taint/TaintDeathConversionRuntime.java')
check('build_version', "version = '11.63.23'" in build)
check('mods_version', 'version="11.63.23"' in mods)
check('living_death_event_hook', 'LivingDeathEvent' in events and 'TaintDeathConversionRuntime.handle(event)' in events)
check('server_only', 'dead.level instanceof ServerLevel level' in runtime)
check('tainted_recursion_guard', 'dead instanceof TaintedMob' in runtime)
check('requires_taint_poison', 'dead.hasEffect(ThaumcraftMod.TAINT_POISON.get())' in runtime)
for key, vanilla in [('CREEPER','Creeper'),('SHEEP','Sheep'),('COW','Cow'),('PIG','Pig'),('CHICKEN','Chicken'),('VILLAGER','Villager')]:
    cls=''.join(x.title() for x in key.lower().split('_'))
    check('registry_'+key, f'ENTITY_TYPES.register("taint_{key.lower()}"' in mod)
    check('attribute_'+key, f'event.put(TAINT_{key}.get(), Taint{cls}Entity.createAttributes().build())' in mod)
    check('renderer_'+key, f'TAINT_{key}.get(), entityRenderer(Taint{cls}Renderer::new)' in client)
    check('conversion_'+key, f'dead instanceof {vanilla}' in runtime and f'ThaumcraftMod.TAINT_{key}.get().create(level)' in runtime)

check('fallback_thaumic_slime', 'ThaumcraftMod.THAUMIC_SLIME.get().create(level)' in runtime)
check('fallback_size_formula', '1.0F + Math.min(dead.getMaxHealth() / 10.0F, 6.0F)' in runtime)
check('fallback_set_tc4_size', 'slime.setTc4Size(Math.max(1, size), true)' in runtime)
check('position_yaw_copy', 'replacement.moveTo(dead.getX(), dead.getY(), dead.getZ(), dead.getYRot(), 0.0F)' in runtime)
check('spawn_replacement', 'level.addFreshEntity(replacement)' in runtime)

stats={
'TaintChickenEntity.java':['MAX_HEALTH, 8.0D','ATTACK_DAMAGE, 3.0D','MOVEMENT_SPEED, 0.40D','ARMOR, 2.0D','random.nextInt(4) == 0'],
'TaintCowEntity.java':['MAX_HEALTH, 40.0D','ATTACK_DAMAGE, 6.0D','MOVEMENT_SPEED, 0.27D','randomTaintResource'],
'TaintPigEntity.java':['MAX_HEALTH, 20.0D','ATTACK_DAMAGE, 4.0D','MOVEMENT_SPEED, 0.275D','ARMOR, 2.0D','random.nextInt(3) == 0'],
'TaintSheepEntity.java':['MAX_HEALTH, 20.0D','ATTACK_DAMAGE, 3.0D','MOVEMENT_SPEED, 0.25D','ARMOR, 1.0D','Items.SHEARS'],
'TaintVillagerEntity.java':['MAX_HEALTH, 30.0D','ATTACK_DAMAGE, 4.0D','MOVEMENT_SPEED, 0.30D','random.nextInt(13) < 1 + looting','TC4_RESEARCH_ITEMS.get("tc4_coin")'],
'TaintCreeperEntity.java':['MAX_HEALTH, 30.0D','ATTACK_DAMAGE, 2.0D','MOVEMENT_SPEED, 0.25D','FUSE_TIME = 30','1.5F, false, Explosion.BlockInteraction.NONE','TAINT_POISON.get(), 100','for (int i = 0; i < 10; i++)']}
for f,tokens in stats.items():
    src=text(JAVA/'entity'/f)
    check(f.replace('.java','')+'_marker','implements TaintedMob' in src)
    check(f.replace('.java','')+'_hostile_goal','MeleeAttackGoal' in src or f=='TaintCreeperEntity.java')
    for tok in tokens: check(f.replace('.java','')+'_'+tok[:22],tok in src)

creeper=text(JAVA/'entity/TaintCreeperEntity.java')
check('villager_is_monster', 'extends Monster implements TaintedMob' in text(JAVA/'entity/TaintVillagerEntity.java'))
check('villager_uses_villager_sounds', 'SoundEvents.VILLAGER_AMBIENT' in text(JAVA/'entity/TaintVillagerEntity.java'))
check('creeper_fuse_synched','EntityDataAccessor<Integer> FUSE' in creeper and 'EntityDataSerializers.INT' in creeper)
check('creeper_no_block_damage','Explosion.BlockInteraction.NONE' in creeper)
check('creeper_taint_columns','TaintSpreadRuntime.markTaintedColumn' in creeper and 'TaintFibresBlock.withAttachments' in creeper)
sheep_renderer=text(JAVA/'client/render/TaintSheepRenderer.java')
check('sheep_fur_layer','SheepFurModel' in sheep_renderer and 'sheep.isSheared()' in sheep_renderer and 'sheep_fur.png' in sheep_renderer)
creeper_renderer=text(JAVA/'client/render/TaintCreeperRenderer.java')
check('creeper_flash_scale','getTc4FlashIntensity' in creeper_renderer and 'power *= power' in creeper_renderer)
check('creeper_white_flash','getWhiteOverlayProgress' in creeper_renderer)

for name in ['chicken.png','cow.png','creeper.png','pig.png','sheep.png','sheep_fur.png','villager.png']:
    out=RES/'textures/models'/name
    check('texture_'+name,out.is_file())
    if out.is_file() and OLD.exists(): check('texture_exact_'+name, hashlib.sha256(out.read_bytes()).digest()==hashlib.sha256((OLD/name).read_bytes()).digest())
for locale in ['en_us','ru_ru']:
    data=json.loads(text(RES/'lang'/f'{locale}.json'))
    for name in ['taint_chicken','taint_cow','taint_creeper','taint_pig','taint_sheep','taint_villager']:
        check(locale+'_'+name, bool(data.get('entity.thaumcraft.'+name)))
manifest=json.loads(text(ROOT/'runtime_artifacts/runtime_test_manifest.template.json'))
check('manifest_version',manifest.get('version') in ('11.63.23','11.63.24','11.63.26','11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59','11.63.60','11.63.61'))
ids={x.get('id') for x in manifest.get('tests',[])}
for i in ['taint.death_conversion_six_vanilla_forms','taint.death_conversion_generic_slime_health_scaling','taint.converted_animals_hostile_ai_attributes_drops','taint.converted_sheep_shearing_and_fur_layer','taint.converted_creeper_fuse_poison_and_column_spread','taint.converted_entity_textures_renderers_and_sync']:
    check('runtime_'+i,i in ids)
check('manifest_76_cases',len(manifest.get('tests',[]))>=76)
failed=[n for n,o in checks if not o]
for n,o in checks: print(('PASS' if o else 'FAIL')+': '+n)
print(f'Taint death-conversion parity guard: {len(checks)-len(failed)}/{len(checks)}')
sys.exit(1 if failed else 0)
