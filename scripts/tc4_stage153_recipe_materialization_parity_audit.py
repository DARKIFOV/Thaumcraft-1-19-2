#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path
from collections import Counter

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
MAP = RES / "data/thaumcraft/tc4_source_mapping"
INFUSION_DIR = RES / "data/thaumcraft/thaumcraft_infusion"
ARCANE_DIR = RES / "data/thaumcraft/thaumcraft_arcane_workbench"
ALCHEMY_DIR = RES / "data/thaumcraft/thaumcraft_alchemy"

EXPECTED_STAGE153_INFUSION_KEYS = {
    "CoreLumber",
    "CoreFishing",
    "CoreUse",
    "ArcaneBore",
    "LampGrowth",
    "LampFertility",
    "ThaumiumFortressHelm",
    "ThaumiumFortressChest",
    "ThaumiumFortressLegs",
    "VoidRobeHelm",
    "VoidRobeChest",
    "VoidRobeLegs",
    "SanityCheck",
    "EssentiaReservoir",
    "SinStone",
    "PrimalCrusher",
    "EldritchEye",
}

errors: list[str] = []


def load_json(path: Path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:  # pragma: no cover
        errors.append(f"failed to read json {path}: {exc}")
        return None


manifest_path = MAP / "tc4_original_recipe_materialization_stage153.json"
manifest = load_json(manifest_path)
if not isinstance(manifest, dict):
    errors.append("missing Stage153 materialization manifest")
else:
    if manifest.get("stage") != 153:
        errors.append("Stage153 manifest has wrong stage")
    if manifest.get("version") != "1.53.0":
        errors.append("Stage153 manifest has wrong version")
    if manifest.get("stage153_created_infusion_json_count") != 17:
        errors.append("Stage153 should create 17 exact original infusion recipe JSON files")
    if manifest.get("runtime_tc4_custom_recipe_count_after_stage153") != 160:
        errors.append("Stage153 should expose 160 strict TC4 custom runtime recipes after materialization")
    if manifest.get("materialized_but_not_custom_runtime_count") != 29:
        errors.append("Stage153 should leave exactly 29 materialized recipes outside current custom runtime: 24 infusion enchantments + 5 smelting")
    counts = manifest.get("materialized_but_not_custom_runtime_counts_by_kind", {})
    if counts.get("INFUSION_ENCHANTMENT") != 24 or counts.get("SMELTING") != 5:
        errors.append("Stage153 non-runtime materialized counts must be 24 INFUSION_ENCHANTMENT and 5 SMELTING")

runtime_keys: set[str] = set()
keys_by_kind: Counter[str] = Counter()
non_original_count = 0
for directory in (ALCHEMY_DIR, ARCANE_DIR, INFUSION_DIR):
    if not directory.exists():
        errors.append(f"missing recipe directory: {directory}")
        continue
    for path in directory.glob("*.json"):
        data = load_json(path)
        if not isinstance(data, dict):
            continue
        key = data.get("tc4_key", "")
        if key:
            runtime_keys.add(key)
            keys_by_kind[data.get("tc4_kind") or data.get("kind") or "UNKNOWN"] += 1
        else:
            non_original_count += 1

missing = EXPECTED_STAGE153_INFUSION_KEYS - runtime_keys
if missing:
    errors.append("missing Stage153 infusion materializations: " + ", ".join(sorted(missing)))

if keys_by_kind.get("INFUSION", 0) < 39:
    errors.append(f"expected at least 39 strict TC4 infusion runtime recipes, found {keys_by_kind.get('INFUSION')}")
if keys_by_kind.get("CRUCIBLE") != 50:
    errors.append(f"expected 50 strict TC4 crucible runtime recipes, found {keys_by_kind.get('CRUCIBLE')}")
if keys_by_kind.get("ARCANE_SHAPED", 0) < 69:
    errors.append(f"expected at least 69 strict TC4 arcane shaped runtime recipes, found {keys_by_kind.get('ARCANE_SHAPED')}")
if keys_by_kind.get("ARCANE_SHAPELESS") != 2:
    errors.append(f"expected 2 strict TC4 arcane shapeless runtime recipes, found {keys_by_kind.get('ARCANE_SHAPELESS')}")

stage121 = load_json(MAP / "tc4_stage121_materialized_recipes.json")
if isinstance(stage121, list):
    materialized_keys = {entry.get("tc4_key") or entry.get("key") for entry in stage121 if entry.get("tc4_key") or entry.get("key")}
    expected_non_runtime = {entry.get("tc4_key") or entry.get("key") for entry in stage121 if (entry.get("tc4_key") or entry.get("key")) not in runtime_keys}
    if len(materialized_keys) != 189:
        errors.append(f"Stage121 materialized source should still have 189 recipes, found {len(materialized_keys)}")
    if len(expected_non_runtime) > 29:
        errors.append(f"Stage153 or later should leave at most 29 Stage121 materialized keys outside custom runtime, found {len(expected_non_runtime)}")
else:
    errors.append("missing Stage121 materialized recipe source")

# The managers must isolate strict-original recipes instead of letting old fallback/addon JSON pollute the TC4 runtime path.
for rel, token in {
    "src/main/java/com/darkifov/thaumcraft/arcane/ArcaneWorkbenchRecipes.java": "STRICT_ORIGINAL",
    "src/main/java/com/darkifov/thaumcraft/alchemy/AlchemyRecipes.java": "STRICT_ORIGINAL_RECIPES",
    "src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipes.java": "STRICT_ORIGINAL",
}.items():
    text = (ROOT / rel).read_text(encoding="utf-8")
    if token not in text or "nonOriginalRecipes" not in text or "tc4Key().isBlank()" not in text:
        errors.append(f"{rel} does not isolate strict original TC4 recipes from non-original placeholders")

matrix_text = (ROOT / "src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java").read_text(encoding="utf-8")
if "findMatchingOriginalInfusionRecipe" not in matrix_text:
    errors.append("Infusion matrix must select original TC4 recipes by catalyst + component pedestals")
if "InfusionProcessHelper.hasComponents(componentPedestals, recipe)" not in matrix_text:
    errors.append("Stage153 infusion lookup must check component pedestals before selecting a recipe")
if "InfusionRecipes.find(catalystPedestal.stored())" in matrix_text:
    errors.append("Infusion matrix still uses old catalyst-only recipe selection")

build_text = (ROOT / "build.gradle").read_text(encoding="utf-8")
mods_text = (ROOT / "src/main/resources/META-INF/mods.toml").read_text(encoding="utf-8")
workflow_text = (ROOT / ".github/workflows/main.yml").read_text(encoding="utf-8")
if not any(f"version = '{v}'" in build_text for v in ["1.94.0", "1.78.0", "1.76.0", "1.70.0", "1.65.0", "1.64.0", "1.63.0", "1.62.0", "1.61.0", "1.60.0", "1.59.0", "1.58.0", "1.57.0", "1.56.0", "1.55.0", "1.54.0", "1.53.0"]):
    errors.append("build.gradle must be Stage153 or later")
if not any(f'version="{v}"' in mods_text for v in ["1.94.0", "1.78.0", "1.76.0", "1.70.0", "1.65.0", "1.64.0", "1.63.0", "1.62.0", "1.61.0", "1.60.0", "1.59.0", "1.58.0", "1.57.0", "1.56.0", "1.55.0", "1.54.0", "1.53.0"]):
    errors.append("mods.toml must be Stage153 or later")
if "tc4_original_recipe_materialization_stage153.json" not in build_text:
    errors.append("verifyJarResources must include the Stage153 materialization manifest")
if "tc4_stage153_recipe_materialization_parity_audit.py" not in workflow_text:
    errors.append("GitHub workflow must run Stage153 recipe materialization audit")
if not any(name in workflow_text for name in ["thaumcraft-legacy-rebuild-stage194-jars", "thaumcraft-legacy-rebuild-stage165-jars", "thaumcraft-legacy-rebuild-stage164-jars", "thaumcraft-legacy-rebuild-stage163-jars", "thaumcraft-legacy-rebuild-stage194-jars", "thaumcraft-legacy-rebuild-stage165-jars", "thaumcraft-legacy-rebuild-stage164-jars", "thaumcraft-legacy-rebuild-stage161-jars", "thaumcraft-legacy-rebuild-stage160-jars", "thaumcraft-legacy-rebuild-stage159-jars", "thaumcraft-legacy-rebuild-stage158-jars", "thaumcraft-legacy-rebuild-stage155-jars", "thaumcraft-legacy-rebuild-stage154-jars", "thaumcraft-legacy-rebuild-stage153-jars"]):
    errors.append("GitHub workflow artifact name must be Stage153 or later")

report = {
    "stage": 153,
    "version": "1.53.0",
    "strict_runtime_keys": len(runtime_keys),
    "strict_runtime_counts_by_kind": dict(keys_by_kind),
    "stage153_expected_infusion_keys": len(EXPECTED_STAGE153_INFUSION_KEYS),
    "non_original_placeholder_json_count": non_original_count,
    "errors": errors,
}
(ROOT / "STAGE153_RECIPE_MATERIALIZATION_PARITY_REPORT.json").write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("Stage153 recipe materialization parity audit: OK")
