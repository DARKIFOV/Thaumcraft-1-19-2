#!/usr/bin/env python3
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "src/main/resources/data/thaumcraft/tc4_source_mapping"
OBJECT_JSON = BASE / "tc4_modern_object_aspects_1192_runtime.json"
ENTITY_JSON = BASE / "tc4_modern_entity_aspects_1192_runtime.json"
STAGE157_JSON = BASE / "tc4_original_aspect_database_stage157.json"
OBJECT_REGISTRY = ROOT / "src/main/java/com/darkifov/thaumcraft/source/TC4ObjectAspectRegistry.java"
ENTITY_REGISTRY = ROOT / "src/main/java/com/darkifov/thaumcraft/source/TC4EntityAspectRegistry.java"

errors: list[str] = []

for path in [OBJECT_JSON, ENTITY_JSON, STAGE157_JSON, OBJECT_REGISTRY, ENTITY_REGISTRY]:
    if not path.exists():
        errors.append(f"missing Stage157 aspect parity file: {path.relative_to(ROOT)}")

if not errors:
    objects = json.loads(OBJECT_JSON.read_text(encoding="utf-8"))
    entities = json.loads(ENTITY_JSON.read_text(encoding="utf-8"))
    stage = json.loads(STAGE157_JSON.read_text(encoding="utf-8"))
    entity_java = ENTITY_REGISTRY.read_text(encoding="utf-8")
    object_java = OBJECT_REGISTRY.read_text(encoding="utf-8")

    if len(objects) < 190:
        errors.append(f"expected at least 190 strict object aspect entries, found {len(objects)}")
    if len(entities) < 67:
        errors.append(f"expected at least 67 strict entity aspect entries after Stage157, found {len(entities)}")
    if stage.get("source") != "thaumcraft/common/config/ConfigAspects.java":
        errors.append("Stage157 source should be original ConfigAspects.java")
    if stage.get("strict_policy", "").lower().find("unresolved") == -1:
        errors.append("Stage157 json must document unresolved/no-fake aspect policy")

    required_entities = {
        "minecraft:wither_skeleton",
        "thaumcraft:firebat",
        "thaumcraft:primal_orb",
        "thaumcraft:giant_brainy_zombie",
        "thaumcraft:crimson_knight",
        "thaumcraft:crimson_cleric",
        "thaumcraft:crimson_praetor",
    }
    for entity_id in sorted(required_entities):
        if entity_id not in entities:
            errors.append(f"missing Stage157 entity aspect mapping: {entity_id}")
        if f'exact("{entity_id}"' not in entity_java:
            errors.append(f"TC4EntityAspectRegistry missing exact Java mapping for {entity_id}")

    if "legacy(" not in object_java or "legacyAliasFor" not in object_java:
        errors.append("TC4ObjectAspectRegistry must keep legacy ore-dict aliases instead of fake modern ids")
    if "legacyScanTriggerId" not in entity_java:
        errors.append("TC4EntityAspectRegistry must expose legacy scan trigger aliases for Stage158")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("Stage157 object/entity aspect parity audit: OK")
