#!/usr/bin/env python3
"""Regression guard for TC4 Infusion Matrix validLocation parity restored in v11.62.86."""
from pathlib import Path
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

altar = "src/main/java/com/darkifov/thaumcraft/infusion/InfusionAltarStructure.java"
need(altar, "boolean valid = strictTc4Location;")
need(altar, "state.is(ThaumcraftMod.INFUSION_PILLAR.get())")
forbid(altar, "state.is(ThaumcraftMod.ARCANE_STONE_BRICKS.get())")
forbid(altar, "strictTc4Location && hasComponents")
need(".github/workflows/build.yml", "Validate retained v11.62.86 Infusion valid-location parity")
need(".github/workflows/release.yml", "Validate retained v11.62.86 Infusion valid-location parity")

if errors:
    print(f"TC4 11.62.86 infusion valid-location guard: FAIL ({len(errors)} problems; {checks} checks)")
    for error in errors:
        print(" -", error)
    sys.exit(1)
print(f"TC4 11.62.86 infusion valid-location guard: PASS ({checks}/{checks} checks)")
