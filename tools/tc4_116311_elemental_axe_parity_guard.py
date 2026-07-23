#!/usr/bin/env python3
"""Static source/resource contract guard for v11.63.23 Axe of the Stream parity."""
from __future__ import annotations

import hashlib
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
        check(f"{rel}:{token[:82]}", token in body)


build = text("build.gradle")
mods = text("src/main/resources/META-INF/mods.toml")
manifest = json.loads(text("runtime_artifacts/runtime_test_manifest.template.json"))
ids = {entry.get("id") for entry in manifest.get("tests", [])}

check("build_version", "version = '11.63.23'" in build)
check("mods_version", 'version="11.63.23"' in mods)
check("manifest_version", manifest.get("version") in ("11.63.23", "11.63.24", "11.63.26", "11.63.27", "11.63.28", "11.63.29", "11.63.30", "11.63.31", "11.63.32", "11.63.33", "11.63.36", "11.63.37", "11.63.38", "11.63.39", "11.63.40", "11.63.41", "11.63.42", "11.63.43", "11.63.44", "11.63.45", "11.63.46", "11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.52", "11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61"))
check("manifest_count_at_least_136", len(manifest.get("tests", [])) >= 136)

registry_rel = "src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java"
contains(
    registry_rel,
    "import com.darkifov.thaumcraft.item.ElementalAxeItem;",
    'case "tc4_elementalaxe" -> new ElementalAxeItem(functionalProperties);',
)
registry = text(registry_rel)
check("elemental_axe_single_registration", registry.count('case "tc4_elementalaxe"') == 1)

# Exact TC4 material values from ThaumcraftApi.toolMatElemental.
tier_rel = "src/main/java/com/darkifov/thaumcraft/item/gear/TC4ElementalToolTier.java"
contains(
    tier_rel,
    "public enum TC4ElementalToolTier implements Tier",
    "INSTANCE;",
    "return 1500;",
    "return 10.0F;",
    "return 3.0F;",
    "return 3;",
    "return 18;",
    "Ingredient.of(ThaumcraftMod.THAUMIUM_INGOT.get())",
)
tier = text(tier_rel)
check("tier_five_overrides", len(re.findall(r"public (?:int|float|Ingredient) get", tier)) >= 6)

axe_rel = "src/main/java/com/darkifov/thaumcraft/item/ElementalAxeItem.java"
contains(
    axe_rel,
    "public final class ElementalAxeItem extends AxeItem",
    "ThreadLocal<Boolean> INTERNAL_HARVEST",
    "new DustParticleOptions(new Vector3f(0.33F, 0.33F, 1.0F), 0.65F)",
    "super(TC4ElementalToolTier.INSTANCE, 3.0F, -3.0F, properties.stacksTo(1))",
    "return Rarity.RARE;",
    "return UseAnim.BOW;",
    "return 72_000;",
    "player.startUsingItem(hand);",
    "InteractionResultHolder.consume(stack)",
    "player.getBoundingBox().inflate(10.0D)",
    "item instanceof FollowingItemEntity following",
    "following.getTarget() == null",
    "away.scale(0.3D / distance)",
    "Mth.clamp(next.x, -0.35D, 0.35D)",
    "Mth.clamp(next.y, -0.35D, 0.35D)",
    "Mth.clamp(next.z, -0.35D, 0.35D)",
    "item.hasImpulse = true;",
    "serverLevel.sendParticles(STREAM_PARTICLE",
    "public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player)",
    "if (INTERNAL_HARVEST.get())",
    "player.isShiftKeyDown()",
    "sourceState.is(BlockTags.LOGS)",
    "if (level.isClientSide)",
    "findFurthestConnectedLog(serverLevel, pos, sourceState.getBlock())",
    "preExistingDrops.add(item.getUUID())",
    "INTERNAL_HARVEST.set(true)",
    "serverPlayer.gameMode.destroyBlock(target)",
    "INTERNAL_HARVEST.remove()",
    "convertNewDrops(serverLevel, target, serverPlayer, preExistingDrops)",
    "scheduleNeighbourTicks(serverLevel, target)",
    'TC4Sounds.event("bubble")',
    "SoundSource.PLAYERS, 0.15F, 1.0F",
    "BlockPos current = origin.immutable()",
    "double lastDistance = 0.0D",
    "for (int dx = -2; dx <= 2; dx++)",
    "for (int dy = 2; dy >= -2; dy--)",
    "for (int dz = -2; dz <= 2; dz++)",
    "Math.abs(candidate.getX() - origin.getX()) > 24",
    "Math.abs(candidate.getY() - origin.getY()) > 48",
    "Math.abs(candidate.getZ() - origin.getZ()) > 24",
    "state.getBlock() != sourceBlock",
    "!state.is(BlockTags.LOGS)",
    "state.getDestroySpeed(level, candidate) < 0.0F",
    "candidate.distSqr(origin)",
    "distance > lastDistance",
    "continue search;",
    "!preExistingDrops.contains(item.getUUID())",
    "!(item instanceof FollowingItemEntity)",
    "new FollowingItemEntity(level",
    "drop.getItem().copy(), player, 10",
    "following.setDeltaMovement(drop.getDeltaMovement())",
    "following.setPickUpDelay(10)",
    "level.addFreshEntity(following)",
    "drop.discard()",
    "for (int dx = -3; dx <= 3; dx++)",
    "150 + level.random.nextInt(150)",
)
axe = text(axe_rel)
check("axe_no_manual_stack_damage", "hurtAndBreak(" not in axe and ".setDamageValue(" not in axe)
check("axe_no_direct_block_remove", "removeBlock(" not in axe and "destroyBlock(target)" in axe)
check("axe_server_authoritative_motion", "if (!level.isClientSide)" in axe)
check("axe_single_nested_destroy", axe.count("gameMode.destroyBlock(target)") == 1)
check("axe_threadlocal_cleanup_finally", "finally" in axe and "INTERNAL_HARVEST.remove()" in axe)
check("axe_same_block_filter", axe.count("state.getBlock() != sourceBlock") == 1)
check("axe_following_particle_type_10", "drop.getItem().copy(), player, 10" in axe)
check("axe_newborn_drop_filter", "preExistingDrops" in axe and "HashSet" in axe)

model_rel = "src/main/resources/assets/thaumcraft/models/item/tc4_elementalaxe.json"
try:
    model = json.loads(text(model_rel))
except Exception:
    model = {}
check("item_model_generated", model.get("parent") == "item/generated")
check("item_model_texture", model.get("textures", {}).get("layer0") == "thaumcraft:item/tc4/elementalaxe")

texture = ROOT / "src/main/resources/assets/thaumcraft/textures/item/tc4/elementalaxe.png"
check("texture_exists", texture.is_file())
if texture.is_file():
    check("texture_byte_exact_tc4", hashlib.sha256(texture.read_bytes()).hexdigest() == "4a948ae34d01732c233ef3bfd867f550f2e8917920c160ed1f8206aa2ec0ae71")

for language, expected in (("en_us", "Axe of the Stream"), ("ru_ru", "Топор Потока")):
    try:
        lang = json.loads(text(f"src/main/resources/assets/thaumcraft/lang/{language}.json"))
    except Exception:
        lang = {}
    check(f"lang:{language}:elementalaxe", lang.get("item.thaumcraft.tc4_elementalaxe") == expected)

for test_id in (
    "tools.elemental_axe_material_rarity_repair_and_use_animation",
    "tools.elemental_axe_item_attraction_radius_acceleration_and_cap",
    "tools.elemental_axe_active_following_item_exclusion_and_particles",
    "tools.elemental_axe_furthest_connected_log_harvest_and_bounds",
    "tools.elemental_axe_following_drop_conversion_durability_and_events",
    "tools.elemental_axe_sneak_bypass_leaf_updates_sound_and_save_reload",
):
    check("manifest:" + test_id, test_id in ids)

contains(
    "README.md",
    "11.63.11 — Axe of the Stream gameplay parity",
    "0.3 acceleration",
    "ServerPlayerGameMode.destroyBlock",
    "FollowingItemEntity",
)
contains(
    "KNOWN_DEVIATIONS.md",
    "v11.63.11 — Axe of the Stream runtime proof",
    "minecraft:logs",
    "PacketFXBlockBubble",
)
for workflow in (".github/workflows/build.yml", ".github/workflows/release.yml"):
    body = text(workflow)
    check("workflow_guard:" + workflow, "tc4_116311_elemental_axe_parity_guard.py" in body)
    check("workflow_version:" + workflow, "11.63.23" in body)

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(("PASS" if ok else "FAIL") + " | " + name)
print(f"SUMMARY | {len(checks) - len(failed)}/{len(checks)} passed")
if failed:
    sys.exit(1)
