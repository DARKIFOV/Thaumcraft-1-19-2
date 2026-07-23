#!/usr/bin/env python3
"""Static source/resource contract guard for v11.63.10 TC4 item-entity family parity."""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
checks: list[tuple[str, bool]] = []


def text(rel: str) -> str:
    path = ROOT / rel
    return path.read_text(encoding="utf-8", errors="ignore") if path.is_file() else ""


def check(name: str, ok: object) -> None:
    checks.append((name, bool(ok)))


def contains(rel: str, *tokens: str) -> None:
    body = text(rel)
    for token in tokens:
        check(f"{rel}:{token[:78]}", token in body)


build = text("build.gradle")
mods = text("src/main/resources/META-INF/mods.toml")
manifest = json.loads(text("runtime_artifacts/runtime_test_manifest.template.json"))
ids = {entry.get("id") for entry in manifest.get("tests", [])}

check("build_version", "version = '11.63.23'" in build)
check("mods_version", 'version="11.63.23"' in mods)
check("manifest_version", manifest.get("version") in ("11.63.23", "11.63.24", "11.63.26", "11.63.27", "11.63.28", "11.63.29", "11.63.30", "11.63.31", "11.63.32", "11.63.33", "11.63.36", "11.63.37", "11.63.38", "11.63.39", "11.63.40", "11.63.41", "11.63.42", "11.63.43", "11.63.44", "11.63.45", "11.63.46", "11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.52", "11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61"))
check("manifest_count_at_least_130", len(manifest.get("tests", [])) >= 130)

mod_rel = "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java"
contains(mod_rel,
    "import com.darkifov.thaumcraft.entity.SpecialItemEntity;",
    "import com.darkifov.thaumcraft.entity.PermanentItemEntity;",
    "import com.darkifov.thaumcraft.entity.FollowingItemEntity;",
    "RegistryObject<EntityType<SpecialItemEntity>> SPECIAL_ITEM",
    'ENTITY_TYPES.register("special_item"',
    "SpecialItemEntity::new",
    "RegistryObject<EntityType<PermanentItemEntity>> PERMANENT_ITEM",
    'ENTITY_TYPES.register("permanent_item"',
    "PermanentItemEntity::new",
    "RegistryObject<EntityType<FollowingItemEntity>> FOLLOWING_ITEM",
    'ENTITY_TYPES.register("following_item"',
    "FollowingItemEntity::new",
    ".sized(0.25F, 0.25F)",
    ".clientTrackingRange(4)",
    ".updateInterval(20)",
)
mod = text(mod_rel)
check("planned_entity_registry_50", len(re.findall(r"RegistryObject<EntityType<", mod)) == 50)

special_rel = "src/main/java/com/darkifov/thaumcraft/entity/SpecialItemEntity.java"
contains(special_rel,
    "extends ItemEntity",
    "public SpecialItemEntity(EntityType<? extends SpecialItemEntity> type, Level level)",
    "this(ThaumcraftMod.SPECIAL_ITEM.get(), level)",
    "setItem(stack.copy())",
    "random.nextDouble() * 360.0D",
    "(random.nextDouble() * 0.2D) - 0.1D",
    "0.2D",
    "if (motion.y > 0.0D)",
    "motion.y * 0.9D",
    "add(0.0D, 0.04D, 0.0D)",
    "super.tick()",
    "source.isExplosion()",
    "return false",
    "NetworkHooks.getEntitySpawningPacket(this)",
    'tag.putString("TC4Original", "EntitySpecialItem")',
)
special=text(special_rel)
check("special_no_plain_new_itementity", "new ItemEntity" not in special)
check("special_single_super_tick", special.count("super.tick();") == 1)

permanent_rel = "src/main/java/com/darkifov/thaumcraft/entity/PermanentItemEntity.java"
contains(permanent_rel,
    "extends SpecialItemEntity",
    "ThaumcraftMod.PERMANENT_ITEM.get()",
    "setUnlimitedLifetime()",
    "public void tick()",
    "public void readAdditionalSaveData(CompoundTag tag)",
    'tag.putBoolean("TC4PermanentItem", true)',
    'tag.putString("TC4Original", "EntityPermanentItem")',
)
permanent=text(permanent_rel)
check("permanent_lifetime_reasserted", permanent.count("setUnlimitedLifetime();") >= 4)

following_rel = "src/main/java/com/darkifov/thaumcraft/entity/FollowingItemEntity.java"
contains(following_rel,
    "extends SpecialItemEntity implements IEntityAdditionalSpawnData",
    "import net.minecraft.network.FriendlyByteBuf;",
    "import net.minecraftforge.entity.IEntityAdditionalSpawnData;",
    "SynchedEntityData.defineId(FollowingItemEntity.class, EntityDataSerializers.INT)",
    "DATA_TARGET_ID",
    "DATA_PARTICLE_TYPE",
    "private UUID targetUuid",
    "private Vec3 targetPosition = Vec3.ZERO",
    "private int homingTicks = 20",
    "private double followGravity = 0.04D",
    "ThaumcraftMod.FOLLOWING_ITEM.get()",
    "setTarget(target)",
    "setParticleType(particleType)",
    "entityData.define(DATA_TARGET_ID, -1)",
    "entityData.define(DATA_PARTICLE_TYPE, 3)",
    "target.getUUID()",
    "entityData.set(DATA_TARGET_ID, target.getId())",
    "serverLevel.getEntity(targetUuid)",
    "noPhysics = true",
    "setNoGravity(true)",
    "target.getBoundingBox().minY + target.getBbHeight() / 2.0D",
    "if (homingTicks > 1)",
    "homingTicks--",
    "if (distance > 0.5D)",
    "delta.scale(1.0D / (distance * homingTicks))",
    "getDeltaMovement().scale(0.1D)",
    "noPhysics = false",
    "setNoGravity(false)",
    "add(0.0D, -followGravity, 0.0D)",
    "ParticleTypes.BUBBLE_POP",
    "new DustParticleOptions",
    'tag.putUUID("Target", targetUuid)',
    'tag.putDouble("TargetX", targetPosition.x)',
    'tag.putInt("type", getParticleType())',
    'tag.putInt("HomingTicks", homingTicks)',
    'tag.putDouble("Gravity", followGravity)',
    'tag.putString("TC4Original", "EntityFollowingItem")',
    "public void writeSpawnData(FriendlyByteBuf buffer)",
    "buffer.writeBoolean(targetUuid != null)",
    "buffer.writeUUID(targetUuid)",
    "buffer.writeDouble(targetPosition.x)",
    "buffer.writeVarInt(Math.max(1, homingTicks))",
    "buffer.writeDouble(followGravity)",
    "public void readSpawnData(FriendlyByteBuf buffer)",
    "buffer.readBoolean() ? buffer.readUUID() : null",
    "new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())",
    "Math.max(1, buffer.readVarInt())",
    'tag.hasUUID("Target")',
    "beginFollowing()",
)
following=text(following_rel)
check("following_no_entity_reference_nbt", "putInt(\"TargetId\"" not in following)
check("following_spawn_packet_inherited", "getAddEntityPacket" not in following and "extends SpecialItemEntity" in following)
check("following_additional_spawn_data", "implements IEntityAdditionalSpawnData" in following and "writeSpawnData" in following and "readSpawnData" in following)

contains("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java",
    "ThaumcraftMod.SPECIAL_ITEM.get()",
    "ThaumcraftMod.PERMANENT_ITEM.get()",
    "ThaumcraftMod.FOLLOWING_ITEM.get()",
    "net.minecraft.client.renderer.entity.ItemEntityRenderer::new",
)

contains("src/main/java/com/darkifov/thaumcraft/block/WandItem.java",
    "import com.darkifov.thaumcraft.entity.SpecialItemEntity;",
    "if (state.is(Blocks.BOOKSHELF))",
    "SpecialItemEntity book = new SpecialItemEntity",
    "new ItemStack(ThaumcraftMod.THAUMONOMICON.get())",
    "book.setDeltaMovement(Vec3.ZERO)",
    "level.addFreshEntity(book)",
)

contains("src/main/java/com/darkifov/thaumcraft/blockentity/CrucibleBlockEntity.java",
    "import com.darkifov.thaumcraft.entity.SpecialItemEntity;",
    "while (!result.isEmpty())",
    "Math.min(result.getCount(), result.getMaxStackSize())",
    "SpecialItemEntity output = new SpecialItemEntity",
    "worldPosition.getY() + 0.71D",
    "output.setDeltaMovement(firstOutput ? 0.0D",
    "0.1D",
    "output.setPickUpDelay(10)",
    "level.addFreshEntity(output)",
    "if (itemEntity instanceof SpecialItemEntity)",
)

outer_rel="src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLootAdapter.java"
contains(outer_rel,
    "import com.darkifov.thaumcraft.entity.PermanentItemEntity;",
    "import com.darkifov.thaumcraft.entity.SpecialItemEntity;",
    "PermanentItemEntity entity = new PermanentItemEntity",
    "center.getY() + 1.5D",
    "entity.setDeltaMovement(0.0D, 0.0D, 0.0D)",
    "entity.setNoPickUpDelay()",
    "spawnSpecialBossDrop",
    "SpecialItemEntity entity = new SpecialItemEntity",
    "boss.getY() + boss.getBbHeight() / 2.0D",
    "entity.setPickUpDelay(10)",
    "entity.setDeltaMovement(0.0D, 0.1D, 0.0D)",
)
outer=text(outer_rel)
check("outer_no_plain_key_item_entity", "ItemEntity entity = new ItemEntity(level, center" not in outer)

contains("src/main/java/com/darkifov/thaumcraft/wand/EqualTradeSwapRuntime.java",
    "import com.darkifov.thaumcraft.entity.FollowingItemEntity;",
    "FollowingItemEntity entity = new FollowingItemEntity",
    "remainder, player, 5",
    "entity.setDefaultPickUpDelay()",
    "level.addFreshEntity(entity)",
)

for language in ("en_us", "ru_ru"):
    try:
        lang=json.loads(text(f"src/main/resources/assets/thaumcraft/lang/{language}.json"))
    except Exception:
        lang={}
    for key in ("entity.thaumcraft.special_item", "entity.thaumcraft.permanent_item", "entity.thaumcraft.following_item"):
        check(f"lang:{language}:{key}", bool(lang.get(key)))

for test_id in (
    "items.special_item_thaumonomicon_hover_and_explosion_immunity",
    "alchemy.special_item_crucible_output_motion_and_pickup_delay",
    "outer_lands.permanent_key_item_save_reload_and_pickup",
    "combat.special_boss_reward_explosion_survival",
    "tools.following_item_target_curve_arrival_and_collision_restore",
    "items.special_item_family_multiplayer_spawn_sync_and_nbt",
):
    check("manifest:"+test_id, test_id in ids)

contains("README.md",
    "11.63.10 — Special, permanent and following item entity parity",
    "50/50 types",
    "Equal Trade overflow pickup",
)
contains("KNOWN_DEVIATIONS.md",
    "v11.63.10 — Special/Permanent/Following item runtime proof",
    "RenderSpecialItem",
    "Elemental Axe",
    "Equal Trade overflow",
)
for workflow in (".github/workflows/build.yml", ".github/workflows/release.yml"):
    body=text(workflow)
    check("workflow_guard:"+workflow, "tc4_116310_special_item_family_parity_guard.py" in body)
    check("workflow_version:"+workflow, "11.63.23" in body)

failed=[name for name, ok in checks if not ok]
for name, ok in checks:
    print(("PASS" if ok else "FAIL")+" | "+name)
print(f"SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed")
if failed:
    sys.exit(1)
