#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8")

errors: list[str] = []
combos = read("src/main/java/com/darkifov/thaumcraft/AspectCombinationRegistry.java")
foundation = read("src/main/java/com/darkifov/thaumcraft/research/ResearchTableFoundation.java")
graph = read("src/main/java/com/darkifov/thaumcraft/research/ResearchAspectGraph.java")
json_path = ROOT / "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_aspect_decomposition_stage160.json"

for token in [
    'decompose(Aspect compound)',
    'isOriginalComponentPair(Aspect result, Aspect first, Aspect second)',
    'isOriginalDirectLink(Aspect first, Aspect second)',
]:
    if token not in combos:
        errors.append(f"AspectCombinationRegistry missing Stage160 token: {token}")

for token in [
    'componentsKnown(Player player, Aspect aspect)',
    'decompose(Player player, Aspect compound)',
    'AspectCombinationRegistry.isOriginalComponentPair(discovered, first, second)',
]:
    if token not in foundation:
        errors.append(f"ResearchTableFoundation missing Stage160 token: {token}")

if 'AspectCombinationRegistry.isOriginalDirectLink(first, second)' not in graph:
    errors.append("ResearchAspectGraph must use exact original direct links for note connections")

if not json_path.exists():
    errors.append("missing tc4_aspect_decomposition_stage160.json")
else:
    data = json.loads(json_path.read_text(encoding="utf-8"))
    if data.get("stage") != 160:
        errors.append("Stage160 decomposition json has wrong stage")
    if data.get("total_aspects") != 48:
        errors.append("Stage160 decomposition json should include 48 TC4 aspects")
    if data.get("compound_count") != 42:
        errors.append("Stage160 decomposition json should include 42 compound aspects")
    if set(data.get("primal_aspects", [])) != {"aer", "terra", "ignis", "aqua", "ordo", "perditio"}:
        errors.append("Stage160 decomposition json has wrong primal aspects")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("Stage160 research table aspect foundation audit: OK")
