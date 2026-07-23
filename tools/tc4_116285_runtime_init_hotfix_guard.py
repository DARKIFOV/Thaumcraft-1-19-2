#!/usr/bin/env python3
"""Regression guard for the 11.62.84 client initialization crash fixed in v11.63.10."""
from pathlib import Path
import json
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []
checks = 0

def read(rel: str) -> str:
    path = ROOT / rel
    if not path.is_file():
        errors.append(f"missing {rel}")
        return ""
    return path.read_text(encoding="utf-8", errors="ignore")

def need(rel: str, token: str) -> None:
    global checks
    checks += 1
    if token not in read(rel):
        errors.append(f"{rel}: missing {token!r}")

def forbid(rel: str, token: str) -> None:
    global checks
    checks += 1
    if token in read(rel):
        errors.append(f"{rel}: forbidden {token!r}")

need("build.gradle", "version = '11.63.23'")
need("build.gradle", "net.minecraftforge:forge:1.19.2-43.5.2")
need("src/main/resources/META-INF/mods.toml", 'version="11.63.23"')

bone_bow = "src/main/java/com/darkifov/thaumcraft/item/BoneBowItem.java"
need(bone_bow, "super(properties.durability(512));")
forbid(bone_bow, "super(properties.durability(512).stacksTo(1));")

checks += 1
bad_chains: list[str] = []
patterns = (
    re.compile(r"\.durability\s*\([^)]*\)\s*\.stacksTo\s*\("),
    re.compile(r"\.stacksTo\s*\([^)]*\)\s*\.durability\s*\("),
)
for path in (ROOT / "src/main/java").rglob("*.java"):
    text = path.read_text(encoding="utf-8", errors="ignore")
    if any(pattern.search(text) for pattern in patterns):
        bad_chains.append(str(path.relative_to(ROOT)))
if bad_chains:
    errors.append("damageable/explicit-stack Item.Properties chains remain: " + ", ".join(bad_chains))

need(".github/workflows/build.yml", "Validate retained v11.62.85 runtime initialization hotfix")
need(".github/workflows/release.yml", "Validate retained v11.62.85 runtime initialization hotfix")
manifest = json.loads(read("runtime_artifacts/runtime_test_manifest.template.json"))
assert manifest.get("version") in {"11.63.37", "11.63.38", "11.63.39", "11.63.40", "11.63.41", "11.63.42", "11.63.43", "11.63.44", "11.63.45", "11.63.46", "11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.52", "11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61"}
need("tools/validate_runtime_manifest.py", 'default="11.63.30"')

if errors:
    print(f"TC4 11.63.10 runtime init hotfix guard: FAIL ({len(errors)} problems; {checks} checks)")
    for error in errors:
        print(" -", error)
    sys.exit(1)

print(f"TC4 11.63.10 runtime init hotfix guard: PASS ({checks}/{checks} checks)")
