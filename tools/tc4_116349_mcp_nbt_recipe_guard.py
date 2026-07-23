#!/usr/bin/env python3
"""Static contract guard for v11.63.49 MCP, OreDictionary and NBT recipe corrections."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ARCANE = "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/"
INFUSION = "src/main/resources/data/thaumcraft/thaumcraft_infusion/"


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def load(path: str):
    return json.loads(read(path))


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def check_infusion(filename: str, catalyst: str, components: list[str], aspects: dict[str, int], instability: int) -> None:
    data = load(INFUSION + filename)
    require(data["catalyst"] == catalyst, f"{filename}: catalyst mismatch")
    require(data["components"] == components, f"{filename}: ordered component mismatch")
    require(data["aspects"] == aspects, f"{filename}: aspect mismatch")
    require(data["instability"] == instability, f"{filename}: instability mismatch")
    require(data.get("v11_63_49_exact_source") is True, f"{filename}: exact-source marker missing")


def main() -> int:
    require("version = '11.63.49'" in read("build.gradle"), "build.gradle version mismatch")
    require('version="11.63.49"' in read("src/main/resources/META-INF/mods.toml"), "mods.toml version mismatch")

    restrict = load(ARCANE + "tc4_tuberestrict.json")
    require(restrict["ingredients"] == ["thaumcraft:essentia_tube", "#forge:stone"],
            "TubeRestrict lost OreDictionary stone tag")
    one_way = load(ARCANE + "tc4_tubeoneway.json")
    require(one_way["ingredients"] == ["thaumcraft:essentia_tube", "#forge:dyes/blue"],
            "TubeOneway lost dyeBlue tag")
    crystalizer = load(ARCANE + "tc4_essentia_crystalizer.json")
    require(crystalizer["pattern"] == ["IDI", "QCQ", "WTW"], "crystalizer pattern mismatch")
    require(crystalizer["key"]["I"] == "#forge:ingots/iron", "crystalizer ingotIron mismatch")
    require(crystalizer["key"]["W"] == "#minecraft:planks", "crystalizer plankWood mismatch")
    require(crystalizer["key"]["D"] == "minecraft:dispenser", "crystalizer dispenser mismatch")
    require(crystalizer["aspects"] == {"AQUA": 5, "TERRA": 15, "ORDO": 5},
            "crystalizer Vis mismatch")

    check_infusion("tc4_wand_rod_obsidian.json", "minecraft:obsidian",
                   ["thaumcraft:balanced_shard", "thaumcraft:terra_shard"],
                   {"TERRA": 12, "PRAECANTATIO": 6, "TENEBRAE": 6}, 3)
    check_infusion("tc4_wand_rod_ice.json", "minecraft:ice",
                   ["thaumcraft:balanced_shard", "thaumcraft:aqua_shard"],
                   {"AQUA": 12, "PRAECANTATIO": 6, "GELUM": 6}, 3)
    check_infusion("tc4_wand_rod_quartz.json", "minecraft:quartz_block",
                   ["thaumcraft:balanced_shard", "thaumcraft:ordo_shard"],
                   {"ORDO": 12, "PRAECANTATIO": 6, "VITREUS": 6}, 3)
    check_infusion("tc4_wand_rod_reed.json", "minecraft:sugar_cane",
                   ["thaumcraft:balanced_shard", "thaumcraft:aer_shard"],
                   {"AER": 12, "PRAECANTATIO": 6, "MOTUS": 6}, 3)
    check_infusion("tc4_wand_rod_blaze.json", "minecraft:blaze_rod",
                   ["thaumcraft:balanced_shard", "thaumcraft:ignis_shard"],
                   {"IGNIS": 12, "PRAECANTATIO": 6, "BESTIA": 6}, 3)
    check_infusion("tc4_wand_rod_bone.json", "minecraft:bone",
                   ["thaumcraft:balanced_shard", "thaumcraft:perditio_shard"],
                   {"PERDITIO": 12, "PRAECANTATIO": 6, "EXANIMIS": 6}, 3)

    jar = load(INFUSION + "tc4_jarbrain.json")
    require(jar["components"] == ["thaumcraft:tc4_brain", "minecraft:spider_eye",
                                  "minecraft:water_bucket", "minecraft:spider_eye"],
            "JarBrain MCP field mismatch")
    trunk = load(INFUSION + "tc4_traveltrunk.json")
    require(trunk["components"][0] == "minecraft:iron_ingot", "TravelTrunk field_151042_j mismatch")
    hand = load(INFUSION + "tc4_mirrorhand.json")
    require(hand["components"] == ["minecraft:stick", "minecraft:compass", "minecraft:map"],
            "MirrorHand field mapping mismatch")
    essentia = load(INFUSION + "tc4_mirroressentia.json")
    require(essentia["components"] == ["minecraft:iron_ingot"] * 3 + ["minecraft:ender_pearl"],
            "MirrorEssentia field mapping mismatch")

    mask_components = {
        "tc4_mask_grinning_devil.json": ["minecraft:black_dye", "minecraft:iron_ingot",
            "minecraft:leather", "thaumcraft:tc4_block_shimmerleaf", "thaumcraft:tc4_brain", "minecraft:iron_ingot"],
        "tc4_mask_angry_ghost.json": ["minecraft:bone_meal", "minecraft:iron_ingot",
            "minecraft:leather", "minecraft:poisonous_potato", "minecraft:wither_skeleton_skull", "minecraft:iron_ingot"],
        "tc4_mask_sipping_fiend.json": ["minecraft:red_dye", "minecraft:iron_ingot",
            "minecraft:leather", "minecraft:ghast_tear", "minecraft:milk_bucket", "minecraft:iron_ingot"],
    }
    for index, (filename, expected) in enumerate(mask_components.items()):
        data = load(INFUSION + filename)
        require(data["components"] == expected, f"{filename}: component mismatch")
        require(data["result"]["item"] == "minecraft:air", f"{filename}: NBT-only result marker mismatch")
        require(data["result"]["output_nbt_label"] == "mask", f"{filename}: NBT label mismatch")
        require(data["result"]["output_nbt_type"] == "int", f"{filename}: NBT type mismatch")
        require(data["result"]["output_nbt_value"] == index, f"{filename}: NBT value mismatch")

    goggles = load(INFUSION + "tc4_helm_goggles.json")
    require(goggles["result"]["output_nbt_label"] == "goggles", "HelmGoggles NBT label mismatch")
    require(goggles["result"]["output_nbt_type"] == "byte", "HelmGoggles NBT type mismatch")
    require(goggles["result"]["output_nbt_value"] == 1, "HelmGoggles NBT value mismatch")

    infusion_runtime = read("src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipe.java")
    matrix_runtime = read("src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java")
    for token in ("public boolean hasNbtOutput()", "public String outputNbtLabel()", "public Tag outputNbt()"):
        require(token in infusion_runtime, f"NBT recipe runtime missing {token}")
    for token in ("recipe.hasNbtOutput()", "central.addTagElement(recipe.outputNbtLabel(), recipe.outputNbt())"):
        require(token in matrix_runtime, f"NBT central-output runtime missing {token}")

    tests = read("src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java")
    for token in ("mcpCorrectedArcaneAndWandRodRecipesMatchTC4", "legacyNbtAndMirrorInfusionsMatchTC4",
                  '"minecraft:ice"', '"minecraft:water_bucket"', '"minecraft:wither_skeleton_skull"',
                  "requireNbtInfusion"):
        require(token in tests, f"GameTest contract missing {token}")
    require(tests.count("@GameTest(") >= 16, "expected at least 16 required GameTests")

    ledger = load("src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_mcp_nbt_recipe_closure_v11_63_49.json")
    require(ledger["v11_63_49_promoted_count"] == 20, "promoted count mismatch")
    require(ledger["exact_runtime_source_record_count"] == 233, "exact source count mismatch")
    require(ledger["remaining_unresolved_count"] == 10, "remaining count mismatch")
    require(len(ledger["remaining_unresolved_keys"]) == 10, "remaining key list mismatch")
    for key in ("EssentiaCrystalizer", "WandRodIce", "JarBrain", "MaskAngryGhost"):
        require(key not in ledger["remaining_unresolved_keys"], f"{key} remained unresolved")
    require("AdvancedGolem" in ledger["remaining_unresolved_keys"], "AdvancedGolem ambiguity was hidden")

    manifest = load("runtime_artifacts/runtime_test_manifest.template.json")
    require(manifest["version"] in {"11.63.49", "11.63.50", "11.63.52", "11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61"}, "manifest version mismatch")
    require(len(manifest["tests"]) >= 342, f"expected at least 342 scenarios, got {len(manifest['tests'])}")
    cases = {case["id"]: case for case in manifest["tests"]}
    for case_id in ("recipes.legacy_oredict_misc_tags", "recipes.wand_rod_infusion_family",
                    "infusion.legacy_nbt_output_family", "infusion.mirror_brain_trunk_field_mapping",
                    "gametest.mcp_arcane_wand_recipe_contract", "gametest.legacy_nbt_mirror_recipe_contract"):
        require(case_id in cases, f"manifest missing {case_id}")
        require(cases[case_id]["status"] == "NOT_TESTED", f"{case_id} must remain NOT_TESTED")

    for workflow in (".github/workflows/build.yml", ".github/workflows/release.yml"):
        wf = read(workflow)
        require("python3 tools/tc4_116349_mcp_nbt_recipe_guard.py" in wf, f"{workflow}: new guard missing")
        require("--version 11.63.49" in wf or "--version 11.63.50" in wf or "--version 11.63.51" in wf or "--version 11.63.52" in wf or "--version 11.63.53" in wf or "--version 11.63.54" in wf or "--version 11.63.55" in wf, f"{workflow}: version mismatch")
        require("./gradlew runGameTestServer --stacktrace --no-daemon" in wf, f"{workflow}: GameTest task missing")

    print("TC4 v11.63.49 MCP/OreDictionary/NBT recipe correction guard: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
