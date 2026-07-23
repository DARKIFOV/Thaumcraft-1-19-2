#!/usr/bin/env python3
"""Static source/resource contract guard for v11.63.10 golem fishing bobber."""
from __future__ import annotations
import json, sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def text(rel):
    p=ROOT/rel
    return p.read_text(encoding='utf-8',errors='ignore') if p.is_file() else ''
def check(name,ok): checks.append((name,bool(ok)))
def contains(rel,*tokens):
    body=text(rel)
    for token in tokens: check(f'{rel}:{token[:72]}',token in body)

build=text('build.gradle'); mods=text('src/main/resources/META-INF/mods.toml')
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids={x.get('id') for x in manifest.get('tests',[])}
check('build_version',"version = '11.63.23'" in build)
check('mods_version','version="11.63.23"' in mods)
check('manifest_version',manifest.get('version') in ('11.63.23','11.63.24','11.63.26','11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61'))
check('manifest_count_at_least_124',len(manifest.get('tests',[]))>=124)

contains('src/main/java/com/darkifov/thaumcraft/entity/GolemBobberEntity.java',
         'extends Entity','SynchedEntityData.defineId','EntityDataSerializers.INT',
         'noCulling = true','setFisher(ThaumGolemEntity fisher)','fisherUuid = fisher.getUUID()',
         'double distance = Math.sqrt(dx * dx + dy * dy + dz * dz)',
         'dy * 0.1D + Math.sqrt(distance) * 0.08D','lifetime > 4000',
         'random.nextFloat() < 0.02F','ClipContext.Fluid.ANY','FluidTags.WATER',
         'private double waterFractionLikeTC4()','slice < 5','fraction += 0.20D',
         '0.04D * (waterFraction * 2.0D - 1.0D)','? 0.50D : 0.92D',
         'drag *= 0.90D','vertical *= 0.80D','random.nextFloat() * 0.2D',
         'FISHING_BOBBER_SPLASH','20 + random.nextInt(20)','tag.putUUID("Fisher"',
         'tag.putString("TC4Original", "EntityGolemBobber")',
         'NetworkHooks.getEntitySpawningPacket(this)')
check('no_1193_blockpos_containing','BlockPos.containing' not in text('src/main/java/com/darkifov/thaumcraft/entity/GolemBobberEntity.java'))
check('no_wrong_blocks_building_adapter','blocksBuilding = true' not in text('src/main/java/com/darkifov/thaumcraft/entity/GolemBobberEntity.java'))

contains('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java',
         'RegistryObject<EntityType<GolemBobberEntity>> GOLEM_BOBBER',
         'ENTITY_TYPES.register("golem_bobber"','GolemBobberEntity::new',
         '.sized(0.25F, 0.25F)','.clientTrackingRange(64)','.updateInterval(3)')
contains('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java',
         'ThaumcraftMod.GOLEM_BOBBER.get()','TC4GolemBobberRenderer::new')
contains('src/main/java/com/darkifov/thaumcraft/client/render/TC4GolemBobberRenderer.java',
         'textures/entity/fishing_hook.png','poseStack.scale(0.5F, 0.5F, 0.5F)',
         'fisher.yBodyRotO','fisher.yBodyRot','cos * 0.25D','sin * 0.70D',
         'RenderType.lines()','for (int i = 0; i < 16; i++)',
         '(t0 * t0 + t0) * 0.5F','color(0, 0, 0, 255)')

contains('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java',
         'private UUID fishingBobberUuid','private BlockPos fishingTarget',
         'private float fishingBiteQuality','private int fishingCooldown = 0',
         'new GolemBobberEntity(serverLevel, this, water)','serverLevel.addFreshEntity(bobber)',
         'fishingCooldown = 300 + random.nextInt(200)','AIFish:bobber',
         'private void tickFishingLifecycle()','if (fishingCooldown <= 0)',
         'AIFish:timeout','GolemOriginalRuntime.strength(material, originalUpgradeSlots) * 1.5E-4F',
         'private float fishingBiteQualityLikeTC4','quality += 3.0E-5F',
         'quality += 1.5E-5F','private void completeFishingCatch',
         'random.nextInt(10) < air','cookFishingCatch(catchStack)',
         'double distance = Math.sqrt(dx * dx + dy * dy + dz * dz)',
         'bobber.splashAndDiscard()','fishingCooldown = 0',
         'tag.putUUID("FishingBobber"','tag.putLong("FishingTarget"',
         'tag.putFloat("FishingBiteQuality"','fishingBobberResolveGrace = fishingBobberUuid == null ? 0 : 100',
         'coreType == GolemCoreType.FISH && !waiting && !pausedByGolemGui',
         'clearFishingBobber(true)')
contains('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java',
         'PotionUtils.setPotion','Potions.WATER','new ItemStack(Items.INK_SAC, 10)',
         'new ItemStack(Items.LILY_PAD)','int[] weights = {10, 10, 10, 10, 5, 2, 10, 5, 5, 10, 10}',
         'float junk = 0.10F - getUpgradeAmount(GolemUpgradeType.ENTROPY) * 0.025F',
         'float treasure = 0.05F + getUpgradeAmount(GolemUpgradeType.ORDER) * 0.0125F')
# Direct timer-only catches were the missing behavior: every loot roll must be in the reel path.
golem=text('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java')
check('single_loot_roll_callsite',golem.count('rollFishingCatchLikeTC4(water)')==1)
check('bobber_required_before_loot',golem.find('private void completeFishingCatch') < golem.find('rollFishingCatchLikeTC4(water)'))

for lang in ('en_us','ru_ru'):
    try:data=json.loads(text(f'src/main/resources/assets/thaumcraft/lang/{lang}.json'))
    except Exception:data={}
    check(f'lang:{lang}:golem_bobber',bool(data.get('entity.thaumcraft.golem_bobber')))
for tid in (
 'golems.fishing_bobber_entity_launch_owner_sync_and_64_block_tracking',
 'golems.fishing_bobber_original_300_499_tick_attempt_timeout',
 'golems.fishing_bobber_quality_strength_bite_probability',
 'golems.fishing_bobber_five_slice_buoyancy_collision_splash_and_lifetime',
 'golems.fishing_bobber_original_loot_air_fire_upgrades_and_reel_motion',
 'golems.fishing_bobber_line_renderer_save_reload_waiting_and_core_cleanup'):
    check('manifest:'+tid,tid in ids)
contains('KNOWN_DEVIATIONS.md','v11.63.09 — Golem fishing bobber runtime proof',
         'modern fishing-hook sprite','WeightedRandomFishable','multi-hour autonomous fishing')
contains('README.md','11.63.09 — Golem fishing bobber lifecycle parity',
         '300–499 tick active attempt window','five-slice buoyancy','sixteen-segment black line')
for wf in ('.github/workflows/build.yml','.github/workflows/release.yml'):
    body=text(wf)
    check('workflow_guard:'+wf,'tc4_116308_golem_fishing_bobber_parity_guard.py' in body)
    check('workflow_version:'+wf,'11.63.23' in body)
failed=[n for n,ok in checks if not ok]
for n,ok in checks: print(('PASS' if ok else 'FAIL')+' | '+n)
print(f'SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed')
if failed: sys.exit(1)
