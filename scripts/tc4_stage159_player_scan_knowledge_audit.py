#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8")

errors: list[str] = []
player_data = read("src/main/java/com/darkifov/thaumcraft/data/PlayerThaumData.java")
thaumometer = read("src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java")
json_path = ROOT / "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_player_scan_knowledge_stage159.json"

for token in [
    'SCANNED_OBJECTS = "ScannedObjects"',
    'SCANNED_ENTITIES = "ScannedEntities"',
    'SCANNED_ASPECTS = "ScannedAspects"',
    'markScannedObject(Player player, String objectId)',
    'markScannedEntity(Player player, String entityId)',
    'recordScannedAspects(Player player, AspectList aspects)',
    'getScanKnowledgeCount(Player player)',
]:
    if token not in player_data:
        errors.append(f"PlayerThaumData missing Stage159 token: {token}")

for token in [
    'PlayerThaumData.markScannedObject(player, key)',
    'PlayerThaumData.markScannedEntity(player, modernId)',
    'PlayerThaumData.recordScannedAspects(player, aspects)',
    'addScannedBlock(stack, key); // Stage159: keep old per-item NBT as a compatibility mirror.',
    'addScannedEntity(stack, modernId); // Stage159: legacy per-thaumometer compatibility only.',
    'PlayerThaumData.getScanKnowledgeCount(player)',
]:
    if token not in thaumometer:
        errors.append(f"ThaumometerItem missing Stage159 token: {token}")

if not json_path.exists():
    errors.append("missing tc4_player_scan_knowledge_stage159.json")
else:
    data = json.loads(json_path.read_text(encoding="utf-8"))
    if data.get("stage") != 159:
        errors.append("Stage159 scan knowledge json has wrong stage")
    for key in ["ScannedObjects", "ScannedEntities", "ScannedAspects"]:
        if key not in data.get("player_lists", []):
            errors.append(f"Stage159 json missing player list {key}")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("Stage159 player scan knowledge audit: OK")
