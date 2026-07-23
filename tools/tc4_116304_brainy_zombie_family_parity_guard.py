#!/usr/bin/env python3
"""Static source/resource contract guard for v11.63.10 Brainy Zombie family."""
from __future__ import annotations

import hashlib
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
checks: list[tuple[str, bool]] = []


def text(rel: str) -> str:
    path = ROOT / rel
    return path.read_text(encoding="utf-8", errors="ignore") if path.is_file() else ""


def check(name: str, condition: bool) -> None:
    checks.append((name, bool(condition)))


def contains(rel: str, *tokens: str) -> None:
    body = text(rel)
    for token in tokens:
        check(f"{rel}:{token[:48]}", token in body)


build = text("build.gradle")
mods = text("src/main/resources/META-INF/mods.toml")
mod = text("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java")
client = text("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java")
brainy = text("src/main/java/com/darkifov/thaumcraft/entity/BrainyZombieEntity.java")
giant = text("src/main/java/com/darkifov/thaumcraft/entity/GiantBrainyZombieEntity.java")
renderer = text("src/main/java/com/darkifov/thaumcraft/client/render/TC4BrainyZombieRenderer.java")
aspects = text("src/main/java/com/darkifov/thaumcraft/source/TC4EntityAspectRegistry.java")
manifest = json.loads(text("runtime_artifacts/runtime_test_manifest.template.json"))
manifest_ids = {entry.get("id") for entry in manifest.get("tests", [])}

check("build_version", "version = '11.63.23'" in build)
check("mods_version", 'version="11.63.23"' in mods)
check("manifest_version", manifest.get("version") in ("11.63.23", "11.63.24", "11.63.26", "11.63.27", "11.63.27", "11.63.28", "11.63.29", "11.63.30", "11.63.31", "11.63.32", "11.63.33", "11.63.36", "11.63.37", "11.63.38", "11.63.39", "11.63.40", "11.63.41", "11.63.42", "11.63.43", "11.63.44", "11.63.45", "11.63.46", "11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.52", "11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61"))
check("manifest_count_at_least_106", len(manifest.get("tests", [])) >= 106)

contains("src/main/java/com/darkifov/thaumcraft/entity/BrainyZombieEntity.java",
         "extends Zombie", "MAX_HEALTH, 25.0D", "ATTACK_DAMAGE, 5.0D",
         "ARMOR, 3.0D", "SPAWN_REINFORCEMENTS_CHANCE, 0.0D", "BuiltInLootTables.EMPTY",
         "roll < 3", "random.nextBoolean()", "Items.ROTTEN_FLESH",
         "random.nextInt(10) - looting <= 4", 'TC4_RESEARCH_ITEMS.get("tc4_brain")')

contains("src/main/java/com/darkifov/thaumcraft/entity/GiantBrainyZombieEntity.java",
         "extends BrainyZombieEntity", "xpReward = 15", "MAX_HEALTH, 60.0D",
         "ATTACK_DAMAGE, 7.0D", "LeapAtTargetGoal(this, 0.4F)",
         "EntityDataSerializers.FLOAT", "entityData.define(ANGER, 1.0F)",
         "Math.max(1.0F, Math.min(2.0F, anger))", "getAnger() - 0.002F",
         "getAnger() + 0.1F", "7.0D + (getAnger() - 1.0F) * 5.0D",
         "1.2F + safeAnger", "onSyncedDataUpdated", 'tag.putFloat("TC4Anger"',
         "roll < 12", "Items.ROTTEN_FLESH, 2", "random.nextInt(200) - looting < 5",
         "ThaumcraftMod.THAUMIUM_INGOT.get()", "Items.CARROT", "Items.POTATO",
         "ThaumcraftMod.AMBER.get()")
check("brainy_uses_vanilla_zombie_xp", "xpReward =" not in brainy)
check("giant_no_invented_knockback_attribute", "KNOCKBACK_RESISTANCE" not in giant)

contains("src/main/java/com/darkifov/thaumcraft/client/render/TC4BrainyZombieRenderer.java",
         "ZombieModel", "ModelLayers.ZOMBIE", "bzombie.png", "giant.getAnger()",
         "poseStack.scale(scale, scale, scale)")

contains("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java",
         'ENTITY_TYPES.register("brainy_zombie"', 'ENTITY_TYPES.register("giant_brainy_zombie"',
         ".sized(0.6F, 1.95F)", ".clientTrackingRange(64)", ".updateInterval(3)", "new ForgeSpawnEggItem(BRAINY_ZOMBIE, 0xFFBFFF, 0x008000",
         "new ForgeSpawnEggItem(GIANT_BRAINY_ZOMBIE, 0xFFBFFF, 0x004000",
         "SpawnPlacements.register(BRAINY_ZOMBIE.get()", "SpawnPlacements.register(GIANT_BRAINY_ZOMBIE.get()",
         "Monster::checkMonsterSpawnRules", "event.put(BRAINY_ZOMBIE.get()",
         "event.put(GIANT_BRAINY_ZOMBIE.get()")

contains("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java",
         "ThaumcraftMod.BRAINY_ZOMBIE.get()", "ThaumcraftMod.GIANT_BRAINY_ZOMBIE.get()",
         "TC4BrainyZombieRenderer")

biome_path = ROOT / "src/main/resources/data/thaumcraft/forge/biome_modifier/add_brainy_zombies.json"
try:
    biome = json.loads(biome_path.read_text(encoding="utf-8"))
except Exception:
    biome = {}
check("biome_modifier_type", biome.get("type") == "forge:add_spawns")
check("biome_modifier_overworld_tag", biome.get("biomes") == "#minecraft:is_overworld")
spawners = biome.get("spawners", [])
check("biome_modifier_one_spawner", len(spawners) == 1)
if spawners:
    spawn = spawners[0]
    check("brainy_spawn_type", spawn.get("type") == "thaumcraft:brainy_zombie")
    check("brainy_spawn_weight_10", spawn.get("weight") == 10)
    check("brainy_spawn_pack_1_1", spawn.get("minCount") == 1 and spawn.get("maxCount") == 1)

for name in ("brainy_zombie_spawn_egg", "giant_brainy_zombie_spawn_egg"):
    path = ROOT / f"src/main/resources/assets/thaumcraft/models/item/{name}.json"
    try:
        model = json.loads(path.read_text(encoding="utf-8"))
    except Exception:
        model = {}
    check(f"{name}_template", model.get("parent") == "minecraft:item/template_spawn_egg")

texture = ROOT / "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/bzombie.png"
check("original_bzombie_texture_exists", texture.is_file())
check("original_bzombie_texture_sha256",
      texture.is_file() and hashlib.sha256(texture.read_bytes()).hexdigest() ==
      "802e3a399b6bd9466fb2282605a081635dd8e4fe2328604ee53ee94c90ac92c7")

for lang_path, expected in {
    "src/main/resources/assets/thaumcraft/lang/en_us.json": {
        "entity.thaumcraft.brainy_zombie": "Angry Zombie",
        "entity.thaumcraft.giant_brainy_zombie": "Furious Zombie",
        "item.thaumcraft.brainy_zombie_spawn_egg": "Angry Zombie Spawn Egg",
        "item.thaumcraft.giant_brainy_zombie_spawn_egg": "Furious Zombie Spawn Egg",
    },
    "src/main/resources/assets/thaumcraft/lang/ru_ru.json": {
        "entity.thaumcraft.brainy_zombie": "Злой зомби",
        "entity.thaumcraft.giant_brainy_zombie": "Яростный зомби",
        "item.thaumcraft.brainy_zombie_spawn_egg": "Яйцо призыва злого зомби",
        "item.thaumcraft.giant_brainy_zombie_spawn_egg": "Яйцо призыва яростного зомби",
    },
}.items():
    try:
        lang = json.loads(text(lang_path))
    except Exception:
        lang = {}
    for key, value in expected.items():
        check(f"lang:{lang_path}:{key}", lang.get(key) == value)

for token in (
    'exact("thaumcraft:brainy_zombie", aspects(Aspect.EXANIMIS, 3, Aspect.HUMANUS, 1, Aspect.COGNITIO, 1, Aspect.TERRA, 1))',
    'exact("thaumcraft:giant_brainy_zombie", aspects(Aspect.EXANIMIS, 4, Aspect.HUMANUS, 2, Aspect.COGNITIO, 1, Aspect.TERRA, 2))',
    'case "thaumcraft:brainy_zombie" -> "Thaumcraft.BrainyZombie"',
    'case "thaumcraft:giant_brainy_zombie" -> "Thaumcraft.GiantBrainyZombie"',
):
    check("aspect_scan:" + token[:44], token in aspects)

for tid in (
    "mobs.brainy_zombie_overworld_spawn_weight_10",
    "mobs.brainy_zombie_attributes_armor_and_reinforcements",
    "mobs.brainy_zombie_brain_and_rotten_flesh_drops",
    "mobs.giant_brainy_zombie_anger_decay_scale_and_collision",
    "mobs.giant_brainy_zombie_attack_curve_leap_and_rare_drops",
    "research.jarbrain_entity_scan_triggers",
):
    check("manifest:" + tid, tid in manifest_ids)

for wf in (".github/workflows/build.yml", ".github/workflows/release.yml"):
    body = text(wf)
    check(f"workflow:{wf}:guard", "tc4_116304_brainy_zombie_family_parity_guard.py" in body)
    check(f"workflow:{wf}:version", "11.63.23" in body)

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(("PASS" if ok else "FAIL") + " | " + name)
print(f"SUMMARY | {len(checks) - len(failed)}/{len(checks)} passed")
if failed:
    sys.exit(1)
