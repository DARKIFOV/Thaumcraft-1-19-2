#!/usr/bin/env python3
"""Release guard for v11.62.66 arcane count and infusion enchantment port."""
from __future__ import annotations

import argparse
import json
import pathlib
import re
import sys


def check(name: str, condition: bool, details: str, failures: list[dict], checks: list[dict]) -> None:
    row = {"name": name, "passed": bool(condition), "details": details}
    checks.append(row)
    if not condition:
        failures.append(row)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    ap.add_argument("--version", default="11.62.66")
    ap.add_argument("--json-out", default="reports/tc4_116266_arcane_enchantment_guard.json")
    args = ap.parse_args()

    root = pathlib.Path(args.root).resolve()
    checks: list[dict] = []
    failures: list[dict] = []

    build = (root / "build.gradle").read_text(encoding="utf-8")
    mod = (root / "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java").read_text(encoding="utf-8")
    repair = (root / "src/main/java/com/darkifov/thaumcraft/enchantment/TC4RepairEnchantment.java").read_text(encoding="utf-8")
    haste = (root / "src/main/java/com/darkifov/thaumcraft/enchantment/TC4HasteEnchantment.java").read_text(encoding="utf-8")
    events = (root / "src/main/java/com/darkifov/thaumcraft/enchantment/TC4EnchantmentEvents.java").read_text(encoding="utf-8")
    jei = (root / "src/main/java/com/darkifov/thaumcraft/compat/jei/TC4JeiPlugin.java").read_text(encoding="utf-8")
    adapter = (root / "src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionEnchantmentAdapter.java").read_text(encoding="utf-8")
    index = (root / "src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionEnchantmentIndex.java").read_text(encoding="utf-8")
    repair_tag_path = root / "src/main/resources/data/thaumcraft/tags/items/repairable.json"
    repair_tag = json.loads(repair_tag_path.read_text(encoding="utf-8"))
    en = json.loads((root / "src/main/resources/assets/thaumcraft/lang/en_us.json").read_text(encoding="utf-8"))
    ru = json.loads((root / "src/main/resources/assets/thaumcraft/lang/ru_ru.json").read_text(encoding="utf-8"))
    arcane_report = json.loads((root / "reports/tc4_arcane_recipe_full_mapping_v11.62.66.json").read_text(encoding="utf-8"))
    infusion_report = json.loads((root / "reports/tc4_infusion_enchantment_coverage_v11.62.66.json").read_text(encoding="utf-8"))

    check("version", f"version = '{args.version}'" in build, args.version, failures, checks)
    check("arcane_literal_shaped", arcane_report.get("literal_shaped_call_sites") == 84, "84 literal shaped call sites", failures, checks)
    check("arcane_expanded_shaped", arcane_report.get("expanded_shaped_registrations") == 104, "104 shaped registrations after loops", failures, checks)
    check("arcane_shapeless", arcane_report.get("expanded_shapeless_registrations") == 5, "5 shapeless registrations", failures, checks)
    check("arcane_total", arcane_report.get("expanded_arcane_workbench_registrations_total") == 109, "109 total registrations", failures, checks)
    check("arcane_full_mapping", arcane_report.get("mapped") == 109 and not arcane_report.get("missing"), "109/109 mapped", failures, checks)

    check("enchantment_deferred_register", "DeferredRegister<Enchantment> ENCHANTMENTS" in mod and "ENCHANTMENTS.register(modBus)" in mod, "Forge enchantment registry attached", failures, checks)
    check("repair_registered", 'ENCHANTMENTS.register("repair", TC4RepairEnchantment::new)' in mod, "thaumcraft:repair", failures, checks)
    check("haste_registered", 'ENCHANTMENTS.register("haste", TC4HasteEnchantment::new)' in mod, "thaumcraft:haste", failures, checks)

    repair_contract = all(token in repair for token in [
        "20 + (level - 1) * 10", "super.getMinCost(level) + 50", "return 2;",
        "stack.is(REPAIRABLE)", "other != Enchantments.UNBREAKING",
    ])
    check("repair_enchantment_contract", repair_contract, "costs, cap, tag applicability, Unbreaking conflict", failures, checks)
    haste_contract = all(token in haste for token in [
        "15 + (level - 1) * 9", "super.getMinCost(level) + 50", "return 3;",
        "EquipmentSlot.FEET", "tc4_hoverharness",
    ])
    check("haste_enchantment_contract", haste_contract, "costs, cap, boots/harness applicability", failures, checks)

    repair_runtime = all(token in events for token in [
        "% 40 == 0", "Math.sqrt(entry.getValue() * 2.0D)", "consumeInventoryVis",
        "getContainerSize() - 1; i >= 0; i--", "boolean enough = true",
        "if (!isHoverHarness(stack))", "stack.getDamageValue() - level",
    ])
    check("repair_runtime_source", repair_runtime, "40 ticks, primal formula, vis drain, repair amount", failures, checks)
    haste_runtime = all(token in events for token in [
        "level * 0.015D", "!player.isOnGround()", "player.isInWater()", "player.zza <= 0.0F",
    ])
    check("haste_runtime_source", haste_runtime, "forward impulse and reductions", failures, checks)

    tag_values = repair_tag.get("values", [])
    check("repairable_tag_size", len(tag_values) >= 45, f"{len(tag_values)} tagged ids", failures, checks)
    check("repairable_tag_no_duplicates", len(tag_values) == len(set(tag_values)), "unique entries", failures, checks)
    check("repairable_tag_core_items", all(item in tag_values for item in [
        "thaumcraft:goggles_of_revealing", "thaumcraft:tc4_hoverharness",
        "thaumcraft:tc4_thaumiumpick", "thaumcraft:tc4_voidsword",
        "thaumcraft:tc4_primal_crusher",
    ]), "core IRepairable families present", failures, checks)

    item_model_dir = root / "src/main/resources/assets/thaumcraft/models/item"
    model_ids = {path.stem for path in item_model_dir.glob("*.json")}
    missing_tag_models = sorted(
        value.split(":", 1)[1]
        for value in tag_values
        if value.startswith("thaumcraft:") and value.split(":", 1)[1] not in model_ids
    )
    check("repairable_tag_models", not missing_tag_models, f"missing models: {missing_tag_models}", failures, checks)

    index_count = len(re.findall(r'\badd\("InfEnch(?:Repair|Haste|\d+)"', index))
    check("infusion_enchantment_index_count", index_count == 24, f"{index_count}/24 entries", failures, checks)
    check("infusion_mapping_report", infusion_report.get("mapped_count") == 24 and infusion_report.get("status") == "STATIC_MAPPING_COMPLETE", "24/24 exact static map", failures, checks)
    check("infusion_custom_target_check", "central.getItem().isEnchantable(central)" in adapter and "central.getItem().canApplyAtEnchantingTable(" not in adapter, "TC4 general item-enchantability check", failures, checks)
    check("jei_enchantment_inputs", "infusionEnchantmentExamples" in jei, "representative central items", failures, checks)
    check("jei_enchantment_outputs", "infusionEnchantmentOutputs" in jei and "EnchantmentHelper.setEnchantments" in jei, "visibly enchanted result stacks", failures, checks)

    lang_keys = ["enchantment.thaumcraft.haste", "enchantment.thaumcraft.repair", "thaumcraft.jei.infusion_enchantment_output"]
    check("english_lang_keys", all(key in en for key in lang_keys), "all new English keys", failures, checks)
    check("russian_lang_keys", all(key in ru for key in lang_keys), "all new Russian keys", failures, checks)

    result = {
        "version": args.version,
        "status": "PASS" if not failures else "FAIL",
        "passed": len(checks) - len(failures),
        "total": len(checks),
        "checks": checks,
        "failures": failures,
        "scope": "static source/resource guard only; not compile or runtime proof",
    }
    out = pathlib.Path(args.json_out)
    if not out.is_absolute():
        out = root / out
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0 if not failures else 1


if __name__ == "__main__":
    sys.exit(main())
