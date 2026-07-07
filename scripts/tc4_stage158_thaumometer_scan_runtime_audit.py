#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "src/main/resources/data/thaumcraft/tc4_source_mapping"
SCAN_JSON = BASE / "tc4_thaumometer_scan_runtime_stage158.json"
THAUMOMETER = ROOT / "src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java"
ENTITY_REGISTRY = ROOT / "src/main/java/com/darkifov/thaumcraft/source/TC4EntityAspectRegistry.java"
PROGRESSION = ROOT / "src/main/java/com/darkifov/thaumcraft/research/OriginalResearchProgression.java"

errors: list[str] = []

for path in [SCAN_JSON, THAUMOMETER, ENTITY_REGISTRY, PROGRESSION]:
    if not path.exists():
        errors.append(f"missing Stage158 scan runtime file: {path.relative_to(ROOT)}")

if not errors:
    scan = json.loads(SCAN_JSON.read_text(encoding="utf-8"))
    thaumometer = THAUMOMETER.read_text(encoding="utf-8")
    entity_registry = ENTITY_REGISTRY.read_text(encoding="utf-8")

    for snippet in [
        "InteractionResultHolder<ItemStack> use",
        "findScannableEntity",
        "TC4EntityAspectRegistry.getAspectsForEntity",
        "TAG_SCANNED_ENTITIES",
        "OriginalResearchProgression.applyScanTriggers",
        "legacyScanTriggerId(target)",
    ]:
        if snippet not in thaumometer:
            errors.append(f"Thaumometer entity scan runtime missing snippet: {snippet}")

    required_triggers = {
        "minecraft:enderman": "Enderman",
        "thaumcraft:brainy_zombie": "Thaumcraft.BrainyZombie",
        "thaumcraft:firebat": "Thaumcraft.Firebat",
        "thaumcraft:primal_orb": "Thaumcraft.PrimalOrb",
    }
    triggers = scan.get("entity_scan_research_triggers", {})
    for modern, legacy in required_triggers.items():
        if triggers.get(modern) != legacy:
            errors.append(f"Stage158 scan json missing {modern}->{legacy}")
        if legacy not in entity_registry:
            errors.append(f"TC4EntityAspectRegistry missing legacy trigger string {legacy}")

    for key in ["FOCUSHELLBAT", "MIRROR", "JARBRAIN", "ROD_primal_staff"]:
        if key not in scan.get("research_keys_requiring_entity_scan", []):
            errors.append(f"Stage158 scan json missing research trigger key {key}")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("Stage158 Thaumometer scan runtime audit: OK")
