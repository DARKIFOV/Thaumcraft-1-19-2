#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors = []
report_path = ROOT / "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_stage156_bulk_recipe_materialization.json"
if not report_path.exists():
    errors.append("Missing tc4_stage156_bulk_recipe_materialization.json")
else:
    report = json.loads(report_path.read_text(encoding="utf-8"))
    if report.get("stage") != 156:
        errors.append("Stage156 materialization report has wrong stage")
    if report.get("created_recipe_count") != 10:
        errors.append("Stage156 should create exactly 10 safe bulk recipes")
    if report.get("created_by_kind", {}).get("ARCANE_SHAPED") != 7:
        errors.append("Stage156 should create 7 arcane focus recipes")
    if report.get("created_by_kind", {}).get("INFUSION") != 3:
        errors.append("Stage156 should create 3 infusion focus recipes")
    for rel in report.get("created_recipe_paths", []):
        path = ROOT / rel
        if not path.exists():
            errors.append(f"Created recipe path missing: {rel}")
            continue
        data = json.loads(path.read_text(encoding="utf-8"))
        for field in ["tc4_key", "tc4_kind", "tc4_source", "research", "result"]:
            if field not in data:
                errors.append(f"{rel} missing {field}")
        if data.get("tc4_key", "").startswith("WandCap") or data.get("tc4_key", "").startswith("WandRod"):
            errors.append(f"Dynamic WandCap/WandRod recipe should not be bulk-materialized in Stage156: {rel}")
        if data.get("tc4_key") == "FocusPouch" and data.get("catalyst") != "thaumcraft:tc4_bauble_belt":
            errors.append("FocusPouch must keep exact TC4 itemBaubleBlanks meta 2 -> tc4_bauble_belt")

required_arcane = ["tc4_focusfire.json", "tc4_focusfrost.json", "tc4_focusshock.json", "tc4_focustrade.json", "tc4_focusexcavation.json", "tc4_focusprimal.json", "tc4_focuspouch.json"]
for name in required_arcane:
    if not (ROOT / "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench" / name).exists():
        errors.append(f"Missing Stage156 arcane recipe {name}")
required_infusion = ["tc4_focushellbat.json", "tc4_focusportablehole.json", "tc4_focuswarding.json"]
for name in required_infusion:
    if not (ROOT / "src/main/resources/data/thaumcraft/thaumcraft_infusion" / name).exists():
        errors.append(f"Missing Stage156 infusion recipe {name}")

if errors:
    print("Stage156 bulk recipe materialization audit FAILED")
    for error in errors:
        print(" -", error)
    raise SystemExit(1)
print("Stage156 bulk recipe materialization audit OK")
