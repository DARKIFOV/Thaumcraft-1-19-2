#!/usr/bin/env python3
from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1]
errors = []

index = ROOT / "src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionEnchantmentIndex.java"
if not index.exists():
    errors.append("Missing TC4InfusionEnchantmentIndex.java")
else:
    text = index.read_text(encoding="utf-8")
    keys = re.findall(r'add\("(InfEnch(?:Repair|Haste|\d+))"', text)
    if len(keys) != 24:
        errors.append(f"Expected 24 original TC4 infusion enchantment entries, found {len(keys)}")
    if len(set(keys)) != 24:
        errors.append("Infusion enchantment keys are not unique")
    for required in ["InfEnchRepair", "InfEnchHaste", "InfEnch0", "InfEnch21"]:
        if required not in keys:
            errors.append(f"Missing original infusion enchantment key {required}")
    if "customThaumcraftEnchantment" not in text:
        errors.append("Index must keep Thaumcraft custom enchantments marked separately, not fake them as vanilla")
    if "thaumcraft:repair" not in text or "thaumcraft:haste" not in text:
        errors.append("Custom TC4 enchantment ids repair/haste must be preserved as thaumcraft ids")
    if "minecraft:unbreaking" not in text or "minecraft:infinity" not in text:
        errors.append("Vanilla modern enchantment ids were not mapped")

mapping_dir = ROOT / "src/main/resources/data/thaumcraft/tc4_source_mapping"
files = sorted(mapping_dir.glob("tc4_infusion_enchantment_*.json"))
if len(files) != 24:
    errors.append(f"Expected 24 tc4_infusion_enchantment_*.json source files, found {len(files)}")
for f in files:
    try:
        data = json.loads(f.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"Invalid JSON {f}: {exc}")
        continue
    for field in ["tc4_key", "research", "tc4_kind", "result_expression", "aspects", "instability"]:
        if field not in data:
            errors.append(f"{f.name} missing {field}")
    if data.get("tc4_kind") != "INFUSION_ENCHANTMENT":
        errors.append(f"{f.name} is not tagged INFUSION_ENCHANTMENT")
    if data.get("research") != "INFUSIONENCHANTMENT":
        errors.append(f"{f.name} must keep INFUSIONENCHANTMENT research gate")

recipes_dir = ROOT / "src/main/resources/data/thaumcraft/recipes"
fake_json = []
for f in recipes_dir.glob("*.json"):
    txt = f.read_text(encoding="utf-8", errors="ignore")
    if "INFUSION_ENCHANTMENT" in txt and "tc4_kind" in txt:
        fake_json.append(str(f.relative_to(ROOT)))
if fake_json:
    errors.append("Infusion enchantments must not be fake item recipe JSON files: " + ", ".join(fake_json[:10]))

bridge = ROOT / "src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeRuntimeBridge.java"
if bridge.exists():
    btxt = bridge.read_text(encoding="utf-8")
    if btxt.count("Kind.INFUSION_ENCHANTMENT") < 24:
        errors.append("TC4RecipeRuntimeBridge should retain all 24 original INFUSION_ENCHANTMENT entries")
else:
    errors.append("Missing TC4RecipeRuntimeBridge.java")

if errors:
    print("Stage154 infusion enchantment parity audit FAILED")
    for e in errors:
        print(" -", e)
    raise SystemExit(1)
print("Stage154 infusion enchantment parity audit OK")
