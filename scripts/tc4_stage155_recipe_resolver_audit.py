#!/usr/bin/env python3
from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1]
errors = []
resolver = ROOT / "src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeItemResolver.java"
if not resolver.exists():
    errors.append("Missing TC4RecipeItemResolver.java")
else:
    text = resolver.read_text(encoding="utf-8")
    required = [
        'ITEM_META.put("itemBaubleBlanks:0", "thaumcraft:tc4_bauble_amulet")',
        'ITEM_META.put("itemBaubleBlanks:1", "thaumcraft:tc4_bauble_ring")',
        'ITEM_META.put("itemBaubleBlanks:2", "thaumcraft:tc4_bauble_belt")',
        'ITEM_DIRECT.put("itemFocusFire", "thaumcraft:tc4_focus_fire")',
        'ITEM_DIRECT.put("itemFocusPortableHole", "thaumcraft:tc4_focus_portablehole")',
        'VANILLA_BLOCKS.put("field_150359_w", "minecraft:glass")',
        'ORE_DICT_EXACT.put("gemEmerald", "minecraft:emerald")',
        'resolveLegacyRecipeExpression',
    ]
    for snippet in required:
        if snippet not in text:
            errors.append(f"Resolver missing exact Stage155 snippet: {snippet}")
    if "oreTin" in text or "oreSilver" in text or "oreLead" in text:
        errors.append("Ore entries like oreTin/oreSilver/oreLead must remain unresolved until an exact modern ore carrier exists")

items = ROOT / "src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java"
carriers = ROOT / "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_stage121_added_block_item_carriers.json"
if items.exists() and carriers.exists():
    item_text = items.read_text(encoding="utf-8")
    carrier_data = json.loads(carriers.read_text(encoding="utf-8"))
    missing = [entry["id"] for entry in carrier_data if f'e("{entry["id"]}"' not in item_text]
    if missing:
        errors.append("Stage121 block item carriers were not all registered in TC4ResearchItems: " + ", ".join(missing[:10]))
else:
    errors.append("Missing TC4ResearchItems.java or Stage121 block carrier map")

report = ROOT / "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_stage155_recipe_resolver_exact_pass.json"
if not report.exists():
    errors.append("Missing tc4_stage155_recipe_resolver_exact_pass.json")
else:
    data = json.loads(report.read_text(encoding="utf-8"))
    if data.get("stage") != 155:
        errors.append("Stage155 resolver report has wrong stage")
    if data.get("input_unresolved_stage121_count") != 54:
        errors.append("Stage155 should start from the 54 Stage121 unresolved recipes")
    if data.get("exact_materializable_after_stage155_count") != 10:
        errors.append("Stage155 should identify exactly 10 newly safe recipe materializations")
    entries = {entry.get("key"): entry for entry in data.get("entries", [])}
    for key in ["FocusFire", "FocusFrost", "FocusShock", "FocusTrade", "FocusExcavation", "FocusPrimal", "FocusPouch", "FocusHellbat", "FocusPortableHole", "FocusWarding"]:
        if not entries.get(key, {}).get("stage155_can_materialize_exact_now"):
            errors.append(f"{key} should be exact-materializable after Stage155")
    for key in ["WandCapSilver", "WandRodObsidian", "PureTin"]:
        if entries.get(key, {}).get("stage155_can_materialize_exact_now"):
            errors.append(f"{key} must remain unresolved; do not fake dynamic/ore-dict recipes")

if errors:
    print("Stage155 recipe resolver audit FAILED")
    for error in errors:
        print(" -", error)
    raise SystemExit(1)
print("Stage155 recipe resolver audit OK")
