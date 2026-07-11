#!/usr/bin/env python3
"""Parse every JSON resource shipped by the Forge mod."""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RESOURCE_ROOT = ROOT / "src" / "main" / "resources"
errors: list[str] = []
count = 0

for path in sorted(RESOURCE_ROOT.rglob("*.json")):
    count += 1
    try:
        with path.open("r", encoding="utf-8") as handle:
            json.load(handle)
    except Exception as exc:  # report the exact broken resource
        errors.append(f"{path.relative_to(ROOT)}: {exc}")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print(f"JSON resource validation: OK ({count} files)")
