#!/usr/bin/env python3
"""Verify the canonical copied TC4 4.2.3.5 texture bank byte-for-byte.

The source inventory was generated from the supplied 1.7.10 source archive and
is stored in the repository, so CI does not need the old mod checkout.
"""
from pathlib import Path
import hashlib
import json
import sys

root = Path(__file__).resolve().parents[1]
manifest_path = root / "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_1710_asset_inventory.json"
canonical = root / "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4"
entries = json.loads(manifest_path.read_text(encoding="utf-8"))
expected = {e["path"][len("textures/"):]: e for e in entries if e.get("path", "").startswith("textures/")}
errors = []
for rel, meta in sorted(expected.items()):
    path = canonical / rel
    if not path.is_file():
        errors.append(f"missing original texture: {rel}")
        continue
    digest = hashlib.sha1(path.read_bytes()).hexdigest()
    if digest != meta["sha1"]:
        errors.append(f"modified original texture: {rel} ({digest} != {meta['sha1']})")
extra = sorted(str(p.relative_to(canonical)).replace('\\','/') for p in canonical.rglob('*') if p.is_file() and str(p.relative_to(canonical)).replace('\\','/') not in expected)
if extra:
    errors.extend(f"unexpected file in canonical original bank: {x}" for x in extra)
if errors:
    print("TC4 original texture bank guard: FAIL")
    for error in errors[:80]:
        print(" -", error)
    if len(errors) > 80:
        print(f" - ... {len(errors)-80} more")
    sys.exit(1)
print(f"TC4 original texture bank guard: OK — {len(expected)} / {len(expected)} exact textures")
