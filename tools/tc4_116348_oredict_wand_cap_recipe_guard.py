#!/usr/bin/env python3
"""Static contract guard for v11.63.48 OreDictionary/tag and wand-cap recipe closure."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def load(path: str):
    return json.loads(read(path))


def main() -> int:
    require(any(f"version = '{v}'" in read("build.gradle") for v in ("11.63.48", "11.63.49", "11.63.50")), "build.gradle version mismatch")
    require(any(f'version="{v}"' in read("src/main/resources/META-INF/mods.toml") for v in ("11.63.48", "11.63.49", "11.63.50")), "mods.toml version mismatch")

    arcane = read("src/main/java/com/darkifov/thaumcraft/arcane/ArcaneWorkbenchRecipe.java")
    for token in (
        "return ingredientMatches(catalystId, stack);",
        "public static ResourceLocation tagIngredient(ResourceLocation tagId)",
        "ResourceLocation catalyst = parseIngredient(json.get(\"catalyst\").getAsString());",
        "TAG_SENTINEL_NAMESPACE",
    ):
        require(token in arcane, f"arcane tag-catalyst runtime missing {token}")

    index = read("src/main/java/com/darkifov/thaumcraft/wand/TC4ConfigRecipesWandIndex.java")
    expected_tags = {
        'WandCapCopper': 'tag("forge", "nuggets/copper")',
        'WandCapSilverInert': 'tag("forge", "nuggets/silver")',
        'WandCapThaumiumInert': 'tag("forge", "nuggets/thaumium")',
        'WandCapVoidInert': 'tag("forge", "nuggets/void_metal")',
    }
    for key, tag in expected_tags.items():
        require(key in index and tag in index, f"{key} lost exact Forge tag mapping")
    for token in (
        'addCapRecipe(recipes, "gold", "WandCapGold", WandCapType.GOLD, item("minecraft", "gold_nugget")',
        ".require(Aspect.PERDITIO, cost * 3)",
        ".require(Aspect.ORDO, cost * 3)",
        ".require(Aspect.IGNIS, cost * 2)",
        ".require(Aspect.AER, cost * 2)",
    ):
        require(token in index, f"wand-cap formula missing {token}")

    pure = {
        "tc4_pure_tin.json": ("PureTin", "forge:ores/tin", "thaumcraft:tc4_clustertin"),
        "tc4_pure_silver.json": ("PureSilver", "forge:ores/silver", "thaumcraft:tc4_clustersilver"),
        "tc4_pure_lead.json": ("PureLead", "forge:ores/lead", "thaumcraft:tc4_clusterlead"),
    }
    for filename, (key, tag, result) in pure.items():
        data = load(f"src/main/resources/data/thaumcraft/thaumcraft_alchemy/{filename}")
        require(data["tc4_key"] == key, f"{filename}: key mismatch")
        require(data["catalyst"]["tag"] == tag, f"{filename}: tag mismatch")
        require(data["aspects"] == {"METALLUM": 1, "ORDO": 1}, f"{filename}: aspect mismatch")
        require(data["result"] == {"item": result, "count": 1}, f"{filename}: result mismatch")

    smelting = {
        "tc4_smelting_1.json": ("tag", "thaumcraft:magical_logs", "minecraft:charcoal", 0.5),
        "tc4_smelting_2.json": ("item", "thaumcraft:cinnabar_ore", "thaumcraft:quicksilver_drop", 1.0),
        "tc4_smelting_3.json": ("item", "thaumcraft:amber_ore", "thaumcraft:amber", 1.0),
    }
    for filename, (ingredient_kind, ingredient, output, xp) in smelting.items():
        data = load(f"src/main/resources/data/thaumcraft/recipes/{filename}")
        require(data["type"] == "minecraft:smelting", f"{filename}: type mismatch")
        require(data["ingredient"] == {ingredient_kind: ingredient}, f"{filename}: input mismatch")
        require(data["result"] == output, f"{filename}: output mismatch")
        require(float(data["experience"]) == xp and data["cookingtime"] == 200,
                f"{filename}: XP/time mismatch")

    charged = {
        "silver": (2, 4, {"POTENTIA": 8, "AURAM": 4}),
        "thaumium": (3, 5, {"POTENTIA": 12, "AURAM": 6}),
        "void": (4, 8, {"POTENTIA": 18, "VACUOS": 18, "ALIENIS": 18, "AURAM": 18}),
    }
    for suffix, (component_count, instability, aspects) in charged.items():
        data = load(f"src/main/resources/data/thaumcraft/thaumcraft_infusion/tc4_wand_cap_{suffix}.json")
        require(len(data["components"]) == component_count, f"{suffix}: component count mismatch")
        require(set(data["components"]) == {"thaumcraft:tc4_dust"}, f"{suffix}: component mismatch")
        require(data["instability"] == instability, f"{suffix}: instability mismatch")
        require(data["aspects"] == aspects, f"{suffix}: aspects mismatch")

    tests = read("src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java")
    for token in (
        "oreDictionaryCrucibleAndLegacySmeltingMatchTC4",
        "wandCapRecipeFamilyMatchesTC4",
        '"#forge:ores/tin"',
        '"#forge:nuggets/void_metal"',
        '"thaumcraft:quicksilver_drop"',
        "requireChargedCapInfusion",
    ):
        require(token in tests, f"GameTest contract missing {token}")
    require(tests.count("@GameTest(") >= 14, "expected at least 14 required GameTests")

    ledger = load("src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_oredict_wand_cap_recipe_closure_v11_63_48.json")
    require(ledger["v11_63_48_promoted_count"] == 14, "promoted count mismatch")
    require(ledger["exact_runtime_source_record_count"] == 213, "exact source count mismatch")
    require(ledger["remaining_unresolved_count"] == 30, "remaining unresolved count mismatch")
    require(len(ledger["remaining_unresolved_keys"]) == 30, "remaining key list mismatch")
    require("Smelting_1" not in ledger["remaining_unresolved_keys"], "smelting remained unresolved")
    require("WandCapVoid" not in ledger["remaining_unresolved_keys"], "charged void cap remained unresolved")

    manifest = load("runtime_artifacts/runtime_test_manifest.template.json")
    require(manifest["version"] in ("11.63.48", "11.63.49", "11.63.50", "11.63.52", "11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61"), "manifest version mismatch")
    require(len(manifest["tests"]) >= 336, f"expected at least 336 scenarios, got {len(manifest['tests'])}")
    by_id = {case["id"]: case for case in manifest["tests"]}
    for case_id in (
        "recipes.oredict_pure_metals_tag_catalysts",
        "recipes.wand_cap_oredict_arcane_family",
        "recipes.wand_cap_charged_infusion_family",
        "recipes.legacy_custom_ore_smelting_inputs",
        "gametest.oredict_and_smelting_recipe_contract",
        "gametest.wand_cap_recipe_family_contract",
    ):
        require(case_id in by_id, f"manifest missing {case_id}")
        require(by_id[case_id]["status"] == "NOT_TESTED", f"{case_id} must remain NOT_TESTED")

    for workflow in (".github/workflows/build.yml", ".github/workflows/release.yml"):
        wf = read(workflow)
        require("python3 tools/tc4_116348_oredict_wand_cap_recipe_guard.py" in wf,
                f"{workflow}: new guard missing")
        require("--version 11.63.48" in wf or "--version 11.63.49" in wf or "--version 11.63.50" in wf or "--version 11.63.51" in wf or "--version 11.63.52" in wf or "--version 11.63.53" in wf or "--version 11.63.54" in wf or "--version 11.63.55" in wf, f"{workflow}: version mismatch")
        require("./gradlew runGameTestServer --stacktrace --no-daemon" in wf,
                f"{workflow}: GameTest task missing")

    print("TC4 v11.63.48 OreDictionary/tag, legacy smelting and wand-cap recipe closure guard: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
