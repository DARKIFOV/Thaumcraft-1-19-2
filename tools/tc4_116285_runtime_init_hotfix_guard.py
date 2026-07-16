#!/usr/bin/env python3
"""Regression guard for the 11.62.84 client initialization crash fixed in v11.62.89."""
from pathlib import Path
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

need("build.gradle", "version = '11.62.89'")
need("build.gradle", "net.minecraftforge:forge:1.19.2-43.5.2")
need("src/main/resources/META-INF/mods.toml", 'version="11.62.89"')

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
need("runtime_artifacts/runtime_test_manifest.template.json", '"version": "11.62.89"')
need("tools/validate_runtime_manifest.py", 'default="11.62.89"')

if errors:
    print(f"TC4 11.62.89 runtime init hotfix guard: FAIL ({len(errors)} problems; {checks} checks)")
    for error in errors:
        print(" -", error)
    sys.exit(1)

print(f"TC4 11.62.89 runtime init hotfix guard: PASS ({checks}/{checks} checks)")
