#!/usr/bin/env python3
"""Strict static audit for the v11.62.60 recipe/JEI parity batch."""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src/main/resources/data/thaumcraft"
ARCANE = DATA / "thaumcraft_arcane_workbench"
ALCHEMY = DATA / "thaumcraft_alchemy"
INFUSION = DATA / "thaumcraft_infusion"
REPORTS = ROOT / "reports"
REPORTS.mkdir(parents=True, exist_ok=True)

errors: list[str] = []
checks: list[dict[str, Any]] = []


def check(name: str, ok: bool, detail: str) -> None:
    checks.append({"name": name, "ok": bool(ok), "detail": detail})
    if not ok:
        errors.append(f"{name}: {detail}")


def load(path: Path) -> dict[str, Any]:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:  # noqa: BLE001 - exact file error is the audit result
        errors.append(f"invalid JSON {path.relative_to(ROOT)}: {exc}")
        return {}


def dotted(data: dict[str, Any], path: str) -> Any:
    value: Any = data
    for part in path.split("."):
        if not isinstance(value, dict) or part not in value:
            return None
        value = value[part]
    return value


def exact_recipe(directory: Path, filename: str, expected: dict[str, Any]) -> None:
    path = directory / filename
    check(f"recipe exists: {filename}", path.is_file(), str(path.relative_to(ROOT)))
    if not path.is_file():
        return
    data = load(path)
    for field, value in expected.items():
        actual = dotted(data, field)
        check(f"{filename}:{field}", actual == value, f"expected {value!r}, found {actual!r}")


# Java/runtime contracts.
alchemy_java = (ROOT / "src/main/java/com/darkifov/thaumcraft/alchemy/AlchemyRecipe.java").read_text(encoding="utf-8")
jei_java = (ROOT / "src/main/java/com/darkifov/thaumcraft/compat/jei/TC4JeiPlugin.java").read_text(encoding="utf-8")
arcane_index = (ROOT / "src/main/java/com/darkifov/thaumcraft/arcane/ArcaneWorkbenchRecipes.java").read_text(encoding="utf-8")
resolver = (ROOT / "src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeItemResolver.java").read_text(encoding="utf-8")
build = (ROOT / "build.gradle").read_text(encoding="utf-8")

check("version 11.62.60", "version = '11.62.60'" in build, "build.gradle version")
check("alchemy object tag parser", 'object.has("tag")' in alchemy_java, "JSON catalyst objects accept a tag")
check("alchemy hash tag parser", 'value.startsWith("#")' in alchemy_java, "string #namespace:path catalysts are accepted")
check("alchemy runtime tag match", "stack.is(TagKey.create" in alchemy_java, "tag catalyst matches real stacks")
check("JEI tag catalyst", ".addIngredients(recipe.catalystIngredient())" in jei_java, "JEI receives the complete Ingredient")
check("JEI original TC4 key", '"thaumcraft.jei.original_recipe"' in jei_java, "all custom categories draw the source recipe key")
check("arcane TC4-key dedup", '"tc4:" + recipe.tc4Key()' in arcane_index and "LinkedHashMap" in arcane_index,
      "generated exact wand recipes replace stale JSON copies")
check("itemResource 15 mapping", 'ITEM_META.put("itemResource:15", "thaumcraft:tc4_charm")' in resolver,
      "ConfigItems.itemResource meta 15")
check("compass stone mapping", 'ITEM_DIRECT.put("itemCompassStone", "thaumcraft:tc4_sinister_stone")' in resolver,
      "ConfigItems.itemCompassStone")

# New recipes.
exact_recipe(ALCHEMY, "tc4_pure_tin.json", {
    "tc4_key": "PureTin", "research": "PURETIN", "catalyst.tag": "forge:ores/tin",
    "aspects.METALLUM": 1, "aspects.ORDO": 1, "result.item": "thaumcraft:tc4_clustertin",
})
exact_recipe(ALCHEMY, "tc4_pure_silver.json", {
    "tc4_key": "PureSilver", "research": "PURESILVER", "catalyst.tag": "forge:ores/silver",
    "aspects.METALLUM": 1, "aspects.ORDO": 1, "result.item": "thaumcraft:tc4_clustersilver",
})
exact_recipe(ALCHEMY, "tc4_pure_lead.json", {
    "tc4_key": "PureLead", "research": "PURELEAD", "catalyst.tag": "forge:ores/lead",
    "aspects.METALLUM": 1, "aspects.ORDO": 1, "result.item": "thaumcraft:tc4_clusterlead",
})
exact_recipe(ARCANE, "tc4_node_relay.json", {
    "tc4_key": "NodeRelay", "research": "VISPOWER", "pattern": [" I ", "ISI", " I "],
    "key.S": "thaumcraft:balanced_shard", "aspects.IGNIS": 8, "aspects.ORDO": 8,
    "result.item": "thaumcraft:vis_relay", "result.count": 2,
})
exact_recipe(ARCANE, "tc4_jar_void.json", {
    "tc4_key": "JarVoid", "research": "JARVOID", "pattern": ["O", "J", "P"],
    "key.O": "minecraft:obsidian", "key.J": "thaumcraft:essentia_jar", "key.P": "minecraft:blaze_powder",
    "aspects.AQUA": 5, "aspects.PERDITIO": 15, "result.item": "thaumcraft:void_essentia_jar",
})

# Corrected recipes.
exact_recipe(ARCANE, "tc4_primalcharm.json", {
    "result.item": "thaumcraft:tc4_charm", "key.I": "minecraft:gold_ingot",
    "key.1": "thaumcraft:aer_shard", "key.2": "thaumcraft:ignis_shard", "key.3": "thaumcraft:aqua_shard",
    "key.4": "thaumcraft:terra_shard", "key.5": "thaumcraft:ordo_shard", "key.6": "thaumcraft:perditio_shard",
})
exact_recipe(ARCANE, "tc4_focusprimal.json", {
    "catalyst": "thaumcraft:tc4_charm", "key.#": "thaumcraft:tc4_charm",
})
exact_recipe(ARCANE, "tc4_essentia_crystalizer.json", {
    "key.Q": "thaumcraft:balanced_shard",
})
exact_recipe(INFUSION, "tc4_sin_stone.json", {
    "catalyst": "minecraft:flint", "result.item": "thaumcraft:tc4_sinister_stone",
})

staff_expected = {
    "tc4_wandrodgreatwoodstaff.json": 8,
    "tc4_wandrodobsidianstaff.json": 14,
    "tc4_wandrodsilverwoodstaff.json": 24,
    "tc4_wandrodicestaff.json": 14,
    "tc4_wandrodquartzstaff.json": 14,
    "tc4_wandrodreedstaff.json": 14,
    "tc4_wandrodblazestaff.json": 14,
    "tc4_wandrodbonestaff.json": 14,
}
for filename, cost in staff_expected.items():
    exact_recipe(ARCANE, filename, {
        "catalyst": "thaumcraft:tc4_charm", "key.S": "thaumcraft:tc4_charm",
        "pattern": ["  S", " G ", "G  "], "aspects.ORDO": cost,
    })

# No active recipe should still refer to known materializer placeholders.
forbidden = {
    "thaumcraft:warp_charm": "original itemResource:15 was replaced by an unrelated new item",
    "thaumcraft:primal_charm": "unregistered placeholder ID",
    "thaumcraft:tc4_compassstone": "unregistered placeholder ID",
    "thaumcraft:tc4_balanced_shard": "wrong duplicate shard ID",
}
active_files = sorted(ALCHEMY.glob("*.json")) + sorted(ARCANE.glob("*.json")) + sorted(INFUSION.glob("*.json"))
for bad, reason in forbidden.items():
    hits = [p.relative_to(ROOT).as_posix() for p in active_files if bad in p.read_text(encoding="utf-8")]
    check(f"no active {bad}", not hits, reason + (": " + ", ".join(hits) if hits else ""))

# Verify the changed runtime carriers have item models, including block items.
for item_id in [
    "tc4_clustertin", "tc4_clustersilver", "tc4_clusterlead", "tc4_charm",
    "tc4_sinister_stone", "vis_relay", "void_essentia_jar", "balanced_shard",
]:
    model = ROOT / f"src/main/resources/assets/thaumcraft/models/item/{item_id}.json"
    check(f"item model {item_id}", model.is_file(), str(model.relative_to(ROOT)))

# Every Thaumcraft item/block-item referenced by an active custom recipe must
# resolve to an item model. This catches typo IDs before a runtime blank slot.
model_ids = {path.stem for path in (ROOT / "src/main/resources/assets/thaumcraft/models/item").glob("*.json")}
recipe_refs: dict[str, list[str]] = {}
def collect_refs(value: Any, owner: Path) -> None:
    if isinstance(value, str) and value.startswith("thaumcraft:"):
        recipe_refs.setdefault(value.split(":", 1)[1], []).append(owner.relative_to(ROOT).as_posix())
    elif isinstance(value, list):
        for child in value:
            collect_refs(child, owner)
    elif isinstance(value, dict):
        for child in value.values():
            collect_refs(child, owner)
for path in active_files:
    collect_refs(load(path), path)
missing_models = {item_id: owners for item_id, owners in recipe_refs.items() if item_id not in model_ids}
check("all active Thaumcraft recipe IDs have item models", not missing_models,
      "; ".join(f"{item_id}: {', '.join(sorted(set(owners))[:3])}" for item_id, owners in sorted(missing_models.items()))
      if missing_models else f"{len(recipe_refs)} distinct Thaumcraft IDs resolved")

# Useful counts for release review.
counts: dict[str, Any] = {}
for name, directory in [("alchemy", ALCHEMY), ("arcane", ARCANE), ("infusion", INFUSION)]:
    files = sorted(directory.glob("*.json"))
    parsed = [load(path) for path in files]
    strict = [data for data in parsed if data.get("tc4_key")]
    counts[name] = {"json_files": len(files), "strict_tc4_recipes": len(strict)}

result = {
    "version": "11.62.60",
    "status": "PASS" if not errors else "FAIL",
    "counts": counts,
    "checks": checks,
    "errors": errors,
}
json_path = REPORTS / "tc4_recipe_jei_audit_v11.62.60.json"
json_path.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

lines = [
    "# TC4 recipe and JEI audit — v11.62.60",
    "",
    f"**Status: {result['status']}**",
    "",
    "## Recipe inventory",
    "",
]
for name, values in counts.items():
    lines.append(f"- {name}: **{values['json_files']}** JSON files, **{values['strict_tc4_recipes']}** source-keyed TC4 recipes")
lines += ["", "## Checks", ""]
for item in checks:
    lines.append(f"- {'PASS' if item['ok'] else 'FAIL'} — **{item['name']}**: {item['detail']}")
if errors:
    lines += ["", "## Blocking errors", ""] + [f"- {error}" for error in errors]
else:
    lines += [
        "", "## Result", "",
        "The new recipes are source-keyed, their corrected carrier IDs have item models, legacy ore catalysts are represented by modern item tags, and JEI receives the full tag ingredient instead of one placeholder stack.",
    ]
md_path = REPORTS / "TC4_RECIPE_JEI_AUDIT_V11_62_60.md"
md_path.write_text("\n".join(lines) + "\n", encoding="utf-8")

print(f"TC4 v11.62.60 recipe/JEI audit: {result['status']}")
for name, values in counts.items():
    print(f"  {name}: {values['json_files']} files / {values['strict_tc4_recipes']} strict")
print(json_path.relative_to(ROOT))
print(md_path.relative_to(ROOT))
if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)
