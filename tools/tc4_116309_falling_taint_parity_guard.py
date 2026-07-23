#!/usr/bin/env python3
"""Static source/resource contract guard for v11.63.10 falling-taint parity."""
from __future__ import annotations

import json
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
        check(f"{rel}:{token[:76]}", token in body)


build = text("build.gradle")
mods = text("src/main/resources/META-INF/mods.toml")
manifest = json.loads(text("runtime_artifacts/runtime_test_manifest.template.json"))
ids = {entry.get("id") for entry in manifest.get("tests", [])}

check("build_version", "version = '11.63.23'" in build)
check("mods_version", 'version="11.63.23"' in mods)
check("manifest_version", manifest.get("version") in ("11.63.23", "11.63.24", "11.63.26", "11.63.27", "11.63.28", "11.63.29", "11.63.30", "11.63.31", "11.63.32", "11.63.33", "11.63.36", "11.63.37", "11.63.38", "11.63.39", "11.63.40", "11.63.41", "11.63.42", "11.63.43", "11.63.44", "11.63.45", "11.63.46", "11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.52", "11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61"))
check("manifest_count_at_least_124", len(manifest.get("tests", [])) >= 124)

contains(
    "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java",
    "import com.darkifov.thaumcraft.entity.FallingTaintEntity;",
    "RegistryObject<EntityType<FallingTaintEntity>> FALLING_TAINT",
    'ENTITY_TYPES.register("falling_taint"',
    "FallingTaintEntity::new",
    ".sized(0.98F, 0.98F)",
    ".clientTrackingRange(4)",
    ".updateInterval(3)",
    '.build(MOD_ID + ":falling_taint")',
)

entity_rel = "src/main/java/com/darkifov/thaumcraft/entity/FallingTaintEntity.java"
contains(
    entity_rel,
    "extends Entity",
    "SynchedEntityData.defineId(FallingTaintEntity.class, EntityDataSerializers.INT)",
    "Block.getId(Blocks.AIR.defaultBlockState())",
    "Block.stateById(entityData.get(DATA_BLOCK_STATE))",
    "blocksBuilding = true",
    "this.sourcePos = sourcePos.immutable()",
    "setPos(visualPos.getX() + 0.5D, visualPos.getY(), visualPos.getZ() + 0.5D)",
    "fallTime++",
    "if (!level.isClientSide && fallTime == 1)",
    "if (!source.is(carried.getBlock()))",
    "level.removeBlock(sourcePos, false)",
    "getDeltaMovement().add(0.0D, -0.04D, 0.0D)",
    "move(MoverType.SELF, getDeltaMovement())",
    "multiply(0.98D, 0.98D, 0.98D)",
    "velocity.x * 0.7D",
    "velocity.y * -0.5D",
    'TC4Sounds.event("gore")',
    "0.5F, (random.nextFloat() - random.nextFloat()) * 0.16F + 0.8F",
    "canPlaceAt(landingPos)",
    "!TaintSpreadRuntime.canTaintFallBelow(level, landingPos.below())",
    "level.setBlock(landingPos, carried, 3)",
    "TaintSpreadRuntime.markTaintedColumn(server, landingPos)",
    "fallTime > 600",
    "state.getMaterial().isReplaceable()",
    "state.getFluidState().is(FluidTags.LAVA)",
    "spawnTaintLandingParticles()",
    "for (int i = 0; i < 10; i++)",
    'tag.putInt("BlockState"',
    'tag.putInt("Time"',
    'tag.putInt("OldX"',
    'tag.putInt("OldY"',
    'tag.putInt("OldZ"',
    "public boolean isAttackable()",
    "public boolean displayFireAnimation()",
    "NetworkHooks.getEntitySpawningPacket(this)",
)
entity = text(entity_rel)
check("no_fall_damage_path", "causeFallDamage" not in entity and "fallDistance" not in entity)
check("server_removes_source_only_once", entity.count("level.removeBlock(sourcePos, false)") == 1)
check("no_vanilla_falling_block_import_or_construction", "import net.minecraft.world.entity.item.FallingBlockEntity" not in entity and "new FallingBlockEntity" not in entity)

runtime_rel = "src/main/java/com/darkifov/thaumcraft/taint/TaintSpreadRuntime.java"
contains(
    runtime_rel,
    "import com.darkifov.thaumcraft.entity.FallingTaintEntity;",
    "if (variant == TaintBlock.Variant.CRUST && tryToFall(level, pos, random)) return;",
    "new FallingTaintEntity(level, pos, state, pos)",
    "new FallingTaintEntity(level, lateral, state, pos)",
    "if (!level.isEmptyBlock(pos.above())) return false",
    "for (int depth = 0; depth < 4; depth++)",
    "public static boolean canTaintFallBelow(LevelReader level, BlockPos pos)",
    "BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))",
    "level.getBlockState(check).is(BlockTags.LOGS)",
    "state.is(ThaumcraftMod.TAINT_FIBRES.get())",
    "state.getFluidState().is(FluidTags.WATER)",
)
runtime = text(runtime_rel)
check("vanilla_falling_block_removed", "FallingBlockEntity" not in runtime)
check("lateral_source_not_pre_removed", "level.removeBlock(pos, false)" not in runtime[runtime.find("private static boolean tryToFall"):runtime.find("public static boolean canTaintFallBelow")])
check("lava_not_fall_through", "|| !state.getFluidState().isEmpty()" not in runtime)

renderer_rel = "src/main/java/com/darkifov/thaumcraft/client/render/TC4FallingTaintRenderer.java"
contains(
    renderer_rel,
    "extends EntityRenderer<FallingTaintEntity>",
    "shadowRadius = 0.5F",
    "!entity.level.getBlockState(entity.blockPosition()).is(state.getBlock())",
    "poseStack.translate(-0.5D, 0.0D, -0.5D)",
    "renderSingleBlock(",
    "LightTexture.FULL_BRIGHT",
    "OverlayTexture.NO_OVERLAY",
    "InventoryMenu.BLOCK_ATLAS",
)
contains(
    "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java",
    "import com.darkifov.thaumcraft.client.render.TC4FallingTaintRenderer;",
    "ThaumcraftMod.FALLING_TAINT.get()",
    "TC4FallingTaintRenderer::new",
)

for language in ("en_us", "ru_ru"):
    try:
        lang = json.loads(text(f"src/main/resources/assets/thaumcraft/lang/{language}.json"))
    except Exception:
        lang = {}
    check(f"lang:{language}:falling_taint", bool(lang.get("entity.thaumcraft.falling_taint")))

for test_id in (
    "taint.falling_taint_source_removed_on_first_server_tick",
    "taint.falling_taint_direct_and_lateral_crust_collapse",
    "taint.falling_taint_gravity_drag_landing_and_600_tick_timeout",
    "taint.falling_taint_replaceable_target_log_support_and_no_fall_damage",
    "taint.falling_taint_blockstate_nbt_spawn_sync_and_fullbright_renderer",
    "taint.falling_taint_flux_goo_fire_lava_gore_and_particle_paths",
):
    check("manifest:" + test_id, test_id in ids)

contains(
    "KNOWN_DEVIATIONS.md",
    "v11.63.09 — Falling Taint runtime proof",
    "Flux Goo metadata",
    "dedicated-server chunk-boundary",
)
contains(
    "README.md",
    "11.63.09 — Falling Taint and crust-collapse parity",
    "first server tick",
    "600-tick safety timeout",
)
for workflow in (".github/workflows/build.yml", ".github/workflows/release.yml"):
    body = text(workflow)
    check("workflow_guard:" + workflow, "tc4_116309_falling_taint_parity_guard.py" in body)
    check("workflow_version:" + workflow, "11.63.23" in body)

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(("PASS" if ok else "FAIL") + " | " + name)
print(f"SUMMARY | {len(checks) - len(failed)}/{len(checks)} passed")
if failed:
    sys.exit(1)
