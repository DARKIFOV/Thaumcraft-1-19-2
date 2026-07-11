#!/usr/bin/env python3
"""Fail CI if the Forge 1.19.2 project gains a NeoForge/TerraBlender dependency."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BUILD = (ROOT / "build.gradle").read_text(encoding="utf-8")
SETTINGS = (ROOT / "settings.gradle").read_text(encoding="utf-8")
MODS = (ROOT / "src/main/resources/META-INF/mods.toml").read_text(encoding="utf-8")

errors: list[str] = []
required = {
    "ForgeGradle plugin": "id 'net.minecraftforge.gradle'" in BUILD,
    "Forge 1.19.2 dependency": "net.minecraftforge:forge:1.19.2-43.5.2" in BUILD,
    "Forge Maven": "https://maven.minecraftforge.net/" in SETTINGS,
    "Forge loader range": 'loaderVersion="[43,)"' in MODS,
    "Forge dependency declaration": 'modId="forge"' in MODS,
}
for label, valid in required.items():
    if not valid:
        errors.append(f"missing required {label}")

forbidden_patterns = {
    "TerraBlender": r"terrablender|glitchfiend",
    "NeoForge": r"neoforge|net\.neoforged",
}
scan_files = [
    ROOT / "build.gradle",
    ROOT / "settings.gradle",
    ROOT / "src/main/resources/META-INF/mods.toml",
]
scan_files.extend((ROOT / "src/main/java").rglob("*.java"))
for path in scan_files:
    text = path.read_text(encoding="utf-8", errors="ignore")
    for label, pattern in forbidden_patterns.items():
        if re.search(pattern, text, re.IGNORECASE):
            errors.append(f"{path.relative_to(ROOT)} contains forbidden {label} reference")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("Forge-only guard: OK (Forge 1.19.2 / 43.5.2; no NeoForge or TerraBlender)")
