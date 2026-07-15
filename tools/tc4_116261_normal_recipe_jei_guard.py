#!/usr/bin/env python3
"""Static parity guard for the 11.62.61 TC4 normal-recipe/gear batch."""
from __future__ import annotations

import json
import re
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RECIPES = ROOT / "src/main/resources/data/thaumcraft/recipes"
ASSETS = ROOT / "src/main/resources/assets/thaumcraft"
REPORT_JSON = ROOT / "reports/tc4_normal_recipe_jei_audit_v11.62.61.json"
REPORT_MD = ROOT / "reports/TC4_NORMAL_RECIPE_JEI_AUDIT_V11_62_61.md"

EXPECTED: dict[str, tuple[str, str, int, list[str] | None]] = {
    "tc4_iron_nuggets_from_ingot": ("minecraft:crafting_shaped", "minecraft:iron_nugget", 9, ["#"]),
    "tc4_thaumium_nuggets_from_ingot": ("minecraft:crafting_shaped", "thaumcraft:thaumium_nugget", 9, ["#"]),
    "tc4_void_nuggets_from_ingot": ("minecraft:crafting_shaped", "thaumcraft:tc4_nuggetvoid", 9, ["#"]),
    "tc4_iron_ingot_from_nuggets": ("minecraft:crafting_shaped", "minecraft:iron_ingot", 1, ["###", "###", "###"]),
    "tc4_thaumium_ingot_from_nuggets": ("minecraft:crafting_shaped", "thaumcraft:thaumium_ingot", 1, ["###", "###", "###"]),
    "tc4_quicksilver_drop_from_nuggets": ("minecraft:crafting_shaped", "thaumcraft:quicksilver_drop", 1, ["###", "###", "###"]),
    "tc4_quicksilver_nuggets_from_drop": ("minecraft:crafting_shaped", "thaumcraft:tc4_nuggetquicksilver", 9, ["#"]),
    "tc4_void_ingot_from_nuggets": ("minecraft:crafting_shaped", "thaumcraft:void_metal_ingot", 1, ["###", "###", "###"]),
    "tc4_mundane_amulet": ("minecraft:crafting_shaped", "thaumcraft:tc4_bauble_amulet", 1, [" S ", "S S", " I "]),
    "tc4_mundane_ring": ("minecraft:crafting_shaped", "thaumcraft:tc4_bauble_ring", 1, [" N ", "N N", " N "]),
    "tc4_mundane_belt": ("minecraft:crafting_shaped", "thaumcraft:tc4_bauble_belt", 1, [" L ", "L L", " I "]),
    "tc4_triple_meat_treat_beef_chicken_pork": ("minecraft:crafting_shapeless", "thaumcraft:tc4_tripletreat", 1, None),
    "tc4_triple_meat_treat_beef_chicken_fish": ("minecraft:crafting_shapeless", "thaumcraft:tc4_tripletreat", 1, None),
    "tc4_triple_meat_treat_beef_fish_pork": ("minecraft:crafting_shapeless", "thaumcraft:tc4_tripletreat", 1, None),
    "tc4_triple_meat_treat_fish_chicken_pork": ("minecraft:crafting_shapeless", "thaumcraft:tc4_tripletreat", 1, None),
    "tc4_quicksilver_from_shimmerleaf": ("minecraft:crafting_shaped", "thaumcraft:quicksilver_drop", 1, ["#"]),
    "tc4_blaze_powder_from_cinderpearl": ("minecraft:crafting_shaped", "minecraft:blaze_powder", 1, ["#"]),
    "tc4_greatwood_planks": ("minecraft:crafting_shaped", "thaumcraft:greatwood_planks", 4, ["W"]),
    "tc4_silverwood_planks": ("minecraft:crafting_shaped", "thaumcraft:silverwood_planks", 4, ["W"]),
    "tc4_flesh_block": ("minecraft:crafting_shaped", "thaumcraft:flesh_block", 1, ["KKK", "KKK", "KKK"]),
    "jar_label": ("minecraft:crafting_shapeless", "thaumcraft:jar_label", 4, None),
}

ARMOR_PATTERNS = {
    "helm": ["III", "I I"],
    "chest": ["I I", "III", "III"],
    "legs": ["III", "I I", "I I"],
    "boots": ["I I", "I I"],
}
TOOL_PATTERNS = {
    "shovel": ["I", "S", "S"],
    "pick": ["III", " S ", " S "],
    "axe": ["II", "SI", "S "],
    "hoe": ["II", "S ", "S "],
    "sword": ["I", "I", "S"],
}
for material in ("thaumium", "void"):
    for part, pattern in {**ARMOR_PATTERNS, **TOOL_PATTERNS}.items():
        output_part = {
            "helm": "helm", "chest": "chest", "legs": "legs", "boots": "boots",
            "shovel": "shovel", "pick": "pick", "axe": "axe", "hoe": "hoe", "sword": "sword",
        }[part]
        EXPECTED[f"tc4_{material}_{part}"] = (
            "minecraft:crafting_shaped",
            f"thaumcraft:tc4_{material}{output_part}",
            1,
            pattern,
        )

LEGACY_WOOD_IDS = (
    "thaumcraft:tc4_block_greatwood_planks",
    "thaumcraft:tc4_block_silverwood_planks",
    "thaumcraft:tc4_block_greatwood_log",
    "thaumcraft:tc4_block_silverwood_log",
)
LEGACY_MATERIAL_IDS = (
    "thaumcraft:tc4_thaumiumingot",
    "thaumcraft:tc4_voidingot",
    "thaumcraft:tc4_nuggetthaumium",
    "thaumcraft:tc4_nuggetiron",
)
CUSTOM_RECIPE_DIRS = (
    ROOT / "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench",
    ROOT / "src/main/resources/data/thaumcraft/thaumcraft_alchemy",
    ROOT / "src/main/resources/data/thaumcraft/thaumcraft_infusion",
)


def read_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def main() -> int:
    problems: list[str] = []
    checks: dict[str, object] = {}

    build = (ROOT / "build.gradle").read_text(encoding="utf-8")
    checks["version_11_62_61"] = "version = '11.62.61'" in build
    if not checks["version_11_62_61"]:
        problems.append("build.gradle does not declare 11.62.61")

    recipe_results: dict[str, dict[str, object]] = {}
    for name, (kind, output, count, pattern) in EXPECTED.items():
        path = RECIPES / f"{name}.json"
        row: dict[str, object] = {"exists": path.is_file()}
        if not path.is_file():
            problems.append(f"missing recipe {name}")
            recipe_results[name] = row
            continue
        data = read_json(path)
        result = data.get("result", {})
        row.update({
            "type": data.get("type"),
            "output": result.get("item"),
            "count": result.get("count", 1),
            "pattern": data.get("pattern"),
            "tc4_source": data.get("tc4_source"),
        })
        if data.get("type") != kind:
            problems.append(f"{name}: type {data.get('type')} != {kind}")
        if result.get("item") != output or result.get("count", 1) != count:
            problems.append(f"{name}: wrong output {result}")
        if pattern is not None and data.get("pattern") != pattern:
            problems.append(f"{name}: wrong pattern {data.get('pattern')} != {pattern}")
        if not data.get("tc4_source"):
            problems.append(f"{name}: missing tc4_source anchor")
        recipe_results[name] = row
    checks["original_recipe_batch"] = recipe_results

    jar = read_json(RECIPES / "jar_label.json")
    jar_ingredients = jar.get("ingredients", [])
    paper_count = sum(x.get("item") == "minecraft:paper" for x in jar_ingredients if isinstance(x, dict))
    checks["jar_label_exact_shapeless"] = (
        jar.get("type") == "minecraft:crafting_shapeless"
        and paper_count == 4
        and {"tag": "forge:dyes/black"} in jar_ingredients
        and {"item": "minecraft:slime_ball"} in jar_ingredients
        and jar.get("result") == {"item": "thaumcraft:jar_label", "count": 4}
    )
    if not checks["jar_label_exact_shapeless"]:
        problems.append("jar_label does not match the original shapeless recipe")

    type_counts = Counter()
    for path in RECIPES.glob("*.json"):
        data = read_json(path)
        type_counts[data.get("type", "<missing>")] += 1
    checks["recipe_type_counts"] = dict(sorted(type_counts.items()))
    checks["jei_vanilla_crafting_visible_count"] = (
        type_counts["minecraft:crafting_shaped"] + type_counts["minecraft:crafting_shapeless"]
    )
    if checks["jei_vanilla_crafting_visible_count"] < len(EXPECTED):
        problems.append("vanilla crafting recipe count is unexpectedly low")

    legacy_hits: list[str] = []
    for directory in CUSTOM_RECIPE_DIRS:
        for path in directory.rglob("*.json"):
            text = path.read_text(encoding="utf-8")
            for old_id in LEGACY_WOOD_IDS:
                if old_id in text:
                    legacy_hits.append(f"{path.relative_to(ROOT)}: {old_id}")
    checks["legacy_wood_hits_in_active_custom_recipes"] = legacy_hits
    if legacy_hits:
        problems.extend(legacy_hits)

    legacy_material_hits: list[str] = []
    for directory in (RECIPES, *CUSTOM_RECIPE_DIRS):
        for path in directory.rglob("*.json"):
            text = path.read_text(encoding="utf-8")
            for old_id in LEGACY_MATERIAL_IDS:
                if old_id in text:
                    legacy_material_hits.append(f"{path.relative_to(ROOT)}: {old_id}")
    checks["legacy_material_hits_in_active_recipes"] = legacy_material_hits
    if legacy_material_hits:
        problems.extend(legacy_material_hits)

    resolver = (ROOT / "src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeItemResolver.java").read_text(encoding="utf-8")
    resolver_expectations = {
        "blockMagicalLog:1": "thaumcraft:silverwood_log",
        "blockWoodenDevice:6": "thaumcraft:greatwood_planks",
        "blockWoodenDevice:7": "thaumcraft:silverwood_planks",
        "itemResource:2": "thaumcraft:thaumium_ingot",
        "itemResource:16": "thaumcraft:void_metal_ingot",
        "itemNugget:0": "minecraft:iron_nugget",
        "itemNugget:6": "thaumcraft:thaumium_nugget",
    }
    resolver_ok = {}
    for source, target in resolver_expectations.items():
        ok = source in resolver and target in resolver
        resolver_ok[source] = ok
        if not ok:
            problems.append(f"resolver mapping missing: {source} -> {target}")
    checks["resolver_canonical_wood_and_materials"] = resolver_ok

    migrator = (ROOT / "src/main/java/com/darkifov/thaumcraft/porting/TC4LegacyDuplicateItemMigrator.java").read_text(encoding="utf-8")
    migration_expectations = {
        "tc4_thaumiumingot": "thaumium_ingot",
        "tc4_voidingot": "void_metal_ingot",
        "tc4_nuggetthaumium": "thaumium_nugget",
    }
    migration_ok = {}
    for source, target in migration_expectations.items():
        marker = f'map.put("{source}", "{target}")'
        migration_ok[source] = marker in migrator
        if marker not in migrator:
            problems.append(f"save migration mapping missing: {source} -> {target}")
    checks["legacy_material_save_migration"] = migration_ok

    research_items = (ROOT / "src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java").read_text(encoding="utf-8")
    functional_cases = {
        "tc4_thaumiumhelm": "TC4ThaumiumArmorItem",
        "tc4_thaumiumchest": "TC4ThaumiumArmorItem",
        "tc4_thaumiumlegs": "TC4ThaumiumArmorItem",
        "tc4_thaumiumboots": "TC4ThaumiumArmorItem",
        "tc4_thaumiumshovel": "TC4ThaumiumShovelItem",
        "tc4_thaumiumpick": "TC4ThaumiumPickaxeItem",
        "tc4_thaumiumaxe": "TC4ThaumiumAxeItem",
        "tc4_thaumiumhoe": "TC4ThaumiumHoeItem",
        "tc4_thaumiumsword": "TC4ThaumiumSwordItem",
        "tc4_voidhelm": "TC4VoidArmorItem",
        "tc4_voidchest": "TC4VoidArmorItem",
        "tc4_voidlegs": "TC4VoidArmorItem",
        "tc4_voidboots": "TC4VoidArmorItem",
        "tc4_voidshovel": "TC4VoidShovelItem",
        "tc4_voidpick": "TC4VoidPickaxeItem",
        "tc4_voidaxe": "TC4VoidAxeItem",
        "tc4_voidhoe": "TC4VoidHoeItem",
        "tc4_voidsword": "TC4VoidSwordItem",
    }
    functional_ok = {}
    for item_id, class_name in functional_cases.items():
        pattern = rf'case\s+"{re.escape(item_id)}"\s*->\s*new\s+{re.escape(class_name)}'
        ok = re.search(pattern, research_items) is not None
        functional_ok[item_id] = ok
        if not ok:
            problems.append(f"{item_id} is not registered as functional {class_name}")
    checks["functional_gear_registration"] = functional_ok

    gear_dir = ROOT / "src/main/java/com/darkifov/thaumcraft/item/gear"
    gear_sources = "\n".join(p.read_text(encoding="utf-8") for p in gear_dir.glob("*.java"))
    stat_markers = {
        "thaumium_uses_400": "return 400;",
        "thaumium_speed_7": "return 7.0F;",
        "thaumium_enchant_22": "return 22;",
        "void_uses_150": "return 150;",
        "void_speed_8": "return 8.0F;",
        "void_enchant_10": "return 10;",
        "void_repair_20_ticks": "holder.tickCount % 20",
        "void_weakness": "MobEffects.WEAKNESS",
        "void_sword_60_ticks": "applyWeakness(target, 60)",
        "void_tools_80_ticks": "applyWeakness(target, 80)",
        "uncommon_rarity": "return Rarity.UNCOMMON;",
        "runic_armor": "TC4RunicArmorHelper.appendTooltip",
        "thaumium_texture": "textures/models/thaumium_1.png",
        "void_texture": "textures/models/void_1.png",
    }
    checks["gear_stat_markers"] = {k: v in gear_sources for k, v in stat_markers.items()}
    for key, ok in checks["gear_stat_markers"].items():
        if not ok:
            problems.append(f"gear parity marker missing: {key}")

    texture_paths = [
        ASSETS / "textures/models/thaumium_1.png",
        ASSETS / "textures/models/thaumium_2.png",
        ASSETS / "textures/models/void_1.png",
        ASSETS / "textures/models/void_2.png",
    ]
    checks["armor_textures"] = {str(p.relative_to(ROOT)): p.is_file() for p in texture_paths}
    for p in texture_paths:
        if not p.is_file():
            problems.append(f"missing armor texture {p.relative_to(ROOT)}")

    output_models: dict[str, bool] = {}
    for _, (_, output, _, _) in EXPECTED.items():
        namespace, path = output.split(":", 1)
        if namespace != "thaumcraft":
            continue
        model = ASSETS / "models/item" / f"{path}.json"
        output_models[output] = model.is_file()
        if not model.is_file():
            problems.append(f"missing item model for recipe output {output}")
    checks["recipe_output_models"] = dict(sorted(output_models.items()))

    for lang in ("en_us.json", "ru_ru.json"):
        data = read_json(ASSETS / "lang" / lang)
        ok = "tc4.void_gear.self_repair" in data
        checks.setdefault("void_gear_localization", {})[lang] = ok
        if not ok:
            problems.append(f"missing void gear localization in {lang}")

    payload = {
        "version": "11.62.61",
        "status": "PASS" if not problems else "FAIL",
        "expected_original_recipe_entries": len(EXPECTED),
        "checks": checks,
        "problems": problems,
    }
    REPORT_JSON.parent.mkdir(parents=True, exist_ok=True)
    REPORT_JSON.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    md = [
        "# TC4 normal recipes, gear and JEI audit — 11.62.61",
        "",
        f"**Status:** {payload['status']}",
        "",
        f"- Original normal-recipe entries checked: **{len(EXPECTED)}** (38 new JSON files plus corrected Jar Label).",
        f"- Vanilla shaped recipes: **{type_counts['minecraft:crafting_shaped']}**.",
        f"- Vanilla shapeless recipes: **{type_counts['minecraft:crafting_shapeless']}**.",
        f"- Standard crafting recipes available to JEI's vanilla crafting category: **{checks['jei_vanilla_crafting_visible_count']}**.",
        f"- Functional Thaumium/Void gear registrations checked: **{len(functional_cases)}**.",
        f"- Legacy placeholder wood ids in active custom recipes: **{len(legacy_hits)}**.",
        f"- Legacy duplicate material ids in active recipes: **{len(legacy_material_hits)}**.",
        "",
        "## Functional parity included",
        "",
        "Thaumium and Void armor now use real ArmorItem implementations and the original armor textures. Thaumium tools use the TC4 material statistics. Void tools use the original material statistics, repair one durability point each second, apply Weakness on hit, and remain present in the warping-gear adapter. Active recipes now use canonical modern ingot/nugget carriers instead of save-migration aliases.",
        "",
        "## Problems",
        "",
    ]
    if problems:
        md.extend(f"- {problem}" for problem in problems)
    else:
        md.append("No static parity problems detected by this guard.")
    md.append("")
    REPORT_MD.write_text("\n".join(md), encoding="utf-8")

    print(f"TC4 11.62.61 normal recipe/JEI guard: {payload['status']}")
    print(f"Recipes checked: {len(EXPECTED)}; standard JEI crafting recipes: {checks['jei_vanilla_crafting_visible_count']}")
    if problems:
        for p in problems:
            print(f"ERROR: {p}")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
