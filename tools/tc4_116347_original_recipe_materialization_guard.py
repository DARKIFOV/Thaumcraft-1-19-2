#!/usr/bin/env python3
"""Static contract guard for v11.63.47 original smelting and infusion-enchantment materialization."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> int:
    require(any(f"version = '{v}'" in read("build.gradle") for v in ("11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.51", "11.63.52", "11.63.53", "11.63.54", "11.63.55")), "build.gradle version mismatch")
    require(any(f'version="{v}"' in read("src/main/resources/META-INF/mods.toml") for v in ("11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.51", "11.63.52", "11.63.53", "11.63.54", "11.63.55")), "mods.toml version mismatch")

    expected = {
        "tc4_smelting_4.json": ("thaumcraft:tc4_clusteriron", "minecraft:iron_ingot", 2, 1.0),
        "tc4_smelting_5.json": ("thaumcraft:tc4_clustercinnabar", "thaumcraft:quicksilver_drop", 2, 1.0),
        "tc4_smelting_6.json": ("thaumcraft:tc4_clustergold", "minecraft:gold_ingot", 2, 1.0),
        "tc4_smelting_7.json": ("thaumcraft:balanced_shard", "thaumcraft:tc4_dust", 1, 1.0),
        "tc4_smelting_8.json": ("thaumcraft:tc4_coin", "minecraft:gold_nugget", 1, 0.0),
    }
    for filename, (input_id, output_id, count, xp) in expected.items():
        data = json.loads(read(f"src/main/resources/data/thaumcraft/recipes/{filename}"))
        require(data["ingredient"]["item"] == input_id, f"{filename}: input mismatch")
        result = data["result"]
        actual_output = result["item"] if isinstance(result, dict) else result
        actual_count = result.get("count", 1) if isinstance(result, dict) else 1
        require(actual_output == output_id, f"{filename}: output mismatch")
        require(actual_count == count, f"{filename}: count mismatch")
        require(float(data["experience"]) == xp, f"{filename}: XP mismatch")
        require(data["cookingtime"] == 200, f"{filename}: cooking time mismatch")
    require(json.loads(read("src/main/resources/data/thaumcraft/recipes/tc4_smelting_4.json"))["type"] == "thaumcraft:counted_smelting", "cluster recipe must preserve count 2")
    require(json.loads(read("src/main/resources/data/thaumcraft/recipes/tc4_smelting_7.json"))["type"] == "minecraft:smelting", "single-output recipe should use vanilla smelting")

    serializer = read("src/main/java/com/darkifov/thaumcraft/recipe/CountedSmeltingRecipeSerializer.java")
    for token in ("new ItemStack(item, Math.max(1, count))", "buffer.writeItem(recipe.getResultItem())", "recipe.getExperience()", "recipe.getCookingTime()"):
        require(token in serializer, f"counted smelting serializer missing {token}")

    index = read("src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionEnchantmentIndex.java")
    require(index.count('add("InfEnch') == 24, "expected exactly 24 original infusion enchantment entries")
    require('"thaumcraft:repair"' in index and '"thaumcraft:haste"' in index, "custom enchantments missing")
    for key in ("InfEnch0", "InfEnch7", "InfEnch15", "InfEnch21"):
        require(f'add("{key}"' in index, f"missing {key}")

    adapter = read("src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionEnchantmentAdapter.java")
    for token in (
        "Math.max(1, enchantment.getMinCost(1) / 3)",
        "return base * (1 + current)",
        "return totalLevels / 2 + recipe.instability()",
        "mod += entry.getValue() * 0.1F",
        "enchantments.put(enchantment, next)",
        "EnchantmentHelper.setEnchantments(enchantments, central)",
    ):
        require(token in adapter, f"infusion enchantment formula missing: {token}")

    matrix = read("src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java")
    for token in (
        "TC4InfusionEnchantmentAdapter.scaledAspects",
        "TC4InfusionEnchantmentAdapter.calcXp",
        "TC4InfusionEnchantmentAdapter.calcInstability",
        "drainEnchantmentXp",
        "TC4InfusionEnchantmentAdapter.applyOutput",
    ):
        require(token in matrix, f"matrix enchantment runtime missing {token}")

    tests = read("src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java")
    for token in (
        "originalSmeltingRecipesMatchTC4",
        "infusionEnchantmentFamilyMatchesTC4",
        "Expected all 24 original infusion enchantment recipes",
        "Protection II XP cost does not match TC4",
        "Praecantatio scaling does not match TC4",
        "Max-level enchantment was incorrectly accepted",
    ):
        require(token in tests, f"GameTest contract missing {token}")
    require(tests.count("@GameTest(") >= 12, "expected at least 12 required GameTests")

    ledger = json.loads(read("src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_original_recipe_materialization_v11_63_47.json"))
    require(ledger["runtime_materialized_recipe_count"] == 189, "runtime materialization count mismatch")
    require(ledger["runtime_counts_by_kind"]["INFUSION_ENCHANTMENT"] == 24, "infusion enchantment ledger mismatch")
    require(ledger["runtime_counts_by_kind"]["SMELTING"] == 5, "smelting ledger mismatch")
    require(ledger["remaining_stage121_unresolved_recipe_count"] == 54, "unresolved count changed without evidence")

    manifest = json.loads(read("runtime_artifacts/runtime_test_manifest.template.json"))
    require(manifest["version"] in ("11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.52", "11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61"), "manifest version mismatch")
    require(len(manifest["tests"]) >= 330, f"expected at least 330 runtime scenarios, got {len(manifest['tests'])}")
    by_id = {case["id"]: case for case in manifest["tests"]}
    for case_id in (
        "recipes.tc4_original_smelting_exact_outputs",
        "infusion.tc4_enchantment_recipe_family",
        "infusion.tc4_enchantment_scaling_apply",
        "gametest.original_smelting_recipe_contract",
        "gametest.infusion_enchantment_recipe_contract",
    ):
        require(case_id in by_id, f"manifest missing {case_id}")
        require(by_id[case_id]["status"] == "NOT_TESTED", f"{case_id} must remain NOT_TESTED")

    for workflow in (".github/workflows/build.yml", ".github/workflows/release.yml"):
        wf = read(workflow)
        require("python3 tools/tc4_116347_original_recipe_materialization_guard.py" in wf, f"{workflow}: new guard missing")
        require(any(f"--version {v}" in wf for v in ("11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.51", "11.63.52", "11.63.53", "11.63.54", "11.63.55")), f"{workflow}: version mismatch")
        require("./gradlew runGameTestServer --stacktrace --no-daemon" in wf, f"{workflow}: GameTest task missing")

    print("TC4 v11.63.47 original smelting and infusion-enchantment materialization plus required GameTests guard: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
