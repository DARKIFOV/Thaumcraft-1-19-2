#!/usr/bin/env python3
"""Stage154 helper: groups remaining original TC4 parity work into safe bulk batches.

This script does not invent replacements. It reads existing source-mapping JSON
and reports which groups can be ported in larger batches and which must stay
unresolved until exact 1.7.10 -> 1.19.2 ids are known.
"""
from pathlib import Path
import json
from collections import Counter

ROOT = Path(__file__).resolve().parents[1]
MAP = ROOT / "src/main/resources/data/thaumcraft/tc4_source_mapping"
report = {
    "stage": 154,
    "rule": "accelerate only by batching exact TC4 data; never fabricate replacements",
    "recommended_bulk_batches": [
        {"stage_range": "155-156", "topic": "remaining recipe resolver/materialization", "strategy": "generate exact JSON where ids are resolved; keep ore-dict/unmapped entries unresolved"},
        {"stage_range": "157-159", "topic": "aspect object/entity database", "strategy": "bulk-generate exact AspectList tables and scanner links"},
        {"stage_range": "160-162", "topic": "wand focus parity", "strategy": "port multiple original focus behaviors per stage with dedicated audits"},
        {"stage_range": "163-166", "topic": "aura/node mechanics", "strategy": "port node type/modifier/vis math in one subsystem batch"},
        {"stage_range": "167-170", "topic": "golem AI/task parity", "strategy": "batch core tasks but keep behavior audits per task"}
    ]
}
for name in ["tc4_stage121_materialized_recipes.json", "tc4_stage121_unresolved_recipes.json"]:
    path = MAP / name
    if path.exists():
        data = json.loads(path.read_text(encoding="utf-8"))
        counts = Counter((entry.get("kind") or entry.get("tc4_kind") or "UNKNOWN") for entry in data if isinstance(entry, dict))
        report[name] = {"count": len(data), "counts_by_kind": dict(sorted(counts.items()))}
print(json.dumps(report, indent=2, ensure_ascii=False))
