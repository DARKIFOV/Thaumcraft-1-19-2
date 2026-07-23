#!/usr/bin/env python3
"""Static source/resource contract guard for v11.63.10 Inhabited Zombie and Eldritch biome."""
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
        check(f"{rel}:{token[:52]}", token in body)


build = text("build.gradle")
mods = text("src/main/resources/META-INF/mods.toml")
manifest = json.loads(text("runtime_artifacts/runtime_test_manifest.template.json"))
manifest_ids = {entry.get("id") for entry in manifest.get("tests", [])}

check("build_version", "version = '11.63.23'" in build)
check("mods_version", 'version="11.63.23"' in mods)
check("manifest_version", manifest.get("version") in ("11.63.23", "11.63.24", "11.63.26", "11.63.27", "11.63.27", "11.63.28", "11.63.29", "11.63.30", "11.63.31", "11.63.32", "11.63.33", "11.63.36", "11.63.37", "11.63.38", "11.63.39", "11.63.40", "11.63.41", "11.63.42", "11.63.43", "11.63.44", "11.63.45", "11.63.46", "11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.52", "11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61"))
check("manifest_count_at_least_106", len(manifest.get("tests", [])) >= 106)

contains("src/main/java/com/darkifov/thaumcraft/entity/InhabitedZombieEntity.java",
         "extends Zombie", "MAX_HEALTH, 30.0D", "ATTACK_DAMAGE, 5.0D",
         "SPAWN_REINFORCEMENTS_CHANCE, 0.0D",
         "new HurtByTargetGoal(this).setAlertOthers()",
         "NearestAttackableTargetGoal<>(this, CrimsonCultistEntity.class, true)",
         "Difficulty.HARD ? 0.9F : 0.6F", "return data;", "setDropChance(EquipmentSlot.HEAD, 0.0F)", "CRIMSON_PLATE_HELM",
         "CRIMSON_PLATE_CHEST", "CRIMSON_PLATE_LEGS",
         "BuiltInLootTables.EMPTY", "releasedCrab", "ELDRITCH_CRAB.get().create",
         "crab.setHelm(true)", "sendParticles(ParticleTypes.EXPLOSION", "deathTime = 19",
         'TC4Sounds.event("crabtalk")', "inflate(32.0D, 16.0D, 32.0D)",
         "Monster.checkMonsterSpawnRules")

contains("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java",
         'ENTITY_TYPES.register("inhabited_zombie"', ".sized(0.6F, 1.95F)",
         ".clientTrackingRange(64)", ".updateInterval(3)",
         "new ForgeSpawnEggItem(INHABITED_ZOMBIE, 0x557755, 0x550000",
         "SpawnPlacements.register(INHABITED_ZOMBIE.get()",
         "InhabitedZombieEntity::checkInhabitedZombieSpawnRules",
         "event.put(INHABITED_ZOMBIE.get(), InhabitedZombieEntity.createAttributes().build())")

contains("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java",
         "ThaumcraftMod.INHABITED_ZOMBIE.get()", "TC4InhabitedZombieRenderer")
contains("src/main/java/com/darkifov/thaumcraft/client/render/TC4InhabitedZombieRenderer.java",
         "extends ZombieRenderer", "czombie.png")
contains("src/main/java/com/darkifov/thaumcraft/block/TC4CrimsonPlateArmorItem.java",
         "entity instanceof InhabitedZombieEntity", "zombie_plate_armor.png",
         "cultist_plate_armor.png")

contains("src/main/java/com/darkifov/thaumcraft/world/TC4Biomes.java",
         'new ResourceLocation(ThaumcraftMod.MOD_ID, "eldritch")',
         'BIOMES.register("eldritch", TC4Biomes::createEldritch)',
         "Biome.Precipitation.NONE", ".skyColor(0x000000)",
         ".mobSpawnSettings(spawns.build())", ".generationSettings(generation.build())")

dimension_path = ROOT / "src/main/resources/data/thaumcraft/dimension/outer_lands.json"
try:
    dimension = json.loads(dimension_path.read_text(encoding="utf-8"))
except Exception:
    dimension = {}
check("outer_lands_uses_eldritch_biome",
      dimension.get("generator", {}).get("settings", {}).get("biome") == "thaumcraft:eldritch")
check("outer_lands_no_void_biome", "minecraft:the_void" not in json.dumps(dimension))

modifier_path = ROOT / "src/main/resources/data/thaumcraft/forge/biome_modifier/eldritch_spawns.json"
try:
    modifier = json.loads(modifier_path.read_text(encoding="utf-8"))
except Exception:
    modifier = {}
check("eldritch_modifier_type", modifier.get("type") == "forge:add_spawns")
check("eldritch_modifier_biome", modifier.get("biomes") == "thaumcraft:eldritch")
spawners = modifier.get("spawners", [])
check("eldritch_two_spawners", len(spawners) == 2)
spawn_by_type = {entry.get("type"): entry for entry in spawners}
for entity_id in ("thaumcraft:inhabited_zombie", "thaumcraft:eldritch_guardian"):
    spawn = spawn_by_type.get(entity_id, {})
    check(f"spawn:{entity_id}:present", bool(spawn))
    check(f"spawn:{entity_id}:weight_1", spawn.get("weight") == 1)
    check(f"spawn:{entity_id}:pack_1_1", spawn.get("minCount") == 1 and spawn.get("maxCount") == 1)

model_path = ROOT / "src/main/resources/assets/thaumcraft/models/item/inhabited_zombie_spawn_egg.json"
try:
    model = json.loads(model_path.read_text(encoding="utf-8"))
except Exception:
    model = {}
check("spawn_egg_model", model.get("parent") == "minecraft:item/template_spawn_egg")

for rel, expected in {
    "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/czombie.png":
        "60aae76435481da6f13a7391d0f1194b07356bfcf11c9f9a74b7d094227d08cb",
    "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/zombie_plate_armor.png":
        "4bcfe24c5007f042641f61844b3a76e19c9699ad1ff2cdf9c2a00ce16ee38148",
    "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/cultist_plate_armor.png":
        "e35bb492e7cdfc1ec06ee10918b3a443417b32849e1869b39b250aabe0d26fcd",
}.items():
    path = ROOT / rel
    check(f"texture:{Path(rel).name}:exists", path.is_file())
    check(f"texture:{Path(rel).name}:sha256",
          path.is_file() and hashlib.sha256(path.read_bytes()).hexdigest() == expected)

for lang_path, expected in {
    "src/main/resources/assets/thaumcraft/lang/en_us.json": {
        "entity.thaumcraft.inhabited_zombie": "Shambling Husk",
        "item.thaumcraft.inhabited_zombie_spawn_egg": "Shambling Husk Spawn Egg",
    },
    "src/main/resources/assets/thaumcraft/lang/ru_ru.json": {
        "entity.thaumcraft.inhabited_zombie": "Волочащаяся оболочка",
        "item.thaumcraft.inhabited_zombie_spawn_egg": "Яйцо призыва волочащейся оболочки",
    },
}.items():
    try:
        lang = json.loads(text(lang_path))
    except Exception:
        lang = {}
    for key, value in expected.items():
        check(f"lang:{lang_path}:{key}", lang.get(key) == value)

for tid in (
    "outer_lands.eldritch_biome_bootstrap_and_no_void_fallback",
    "mobs.inhabited_zombie_30_health_damage_and_no_reinforcements",
    "mobs.inhabited_zombie_cultist_armour_difficulty_chances",
    "mobs.inhabited_zombie_targets_cultists_and_respects_local_cap",
    "mobs.inhabited_zombie_death_releases_helmeted_eldritch_crab",
    "outer_lands.eldritch_biome_inhabited_guardian_equal_spawn_weights",
):
    check("manifest:" + tid, tid in manifest_ids)

for wf in (".github/workflows/build.yml", ".github/workflows/release.yml"):
    body = text(wf)
    check(f"workflow:{wf}:guard", "tc4_116305_inhabited_zombie_eldritch_biome_parity_guard.py" in body)
    check(f"workflow:{wf}:version", "11.63.23" in body)

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(("PASS" if ok else "FAIL") + " | " + name)
print(f"SUMMARY | {len(checks) - len(failed)}/{len(checks)} passed")
if failed:
    sys.exit(1)
