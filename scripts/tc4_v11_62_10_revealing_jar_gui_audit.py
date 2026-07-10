#!/usr/bin/env python3
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []


def must(path: str, *needles: str) -> None:
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing file: {path}")
        return
    text = p.read_text(encoding="utf-8")
    for needle in needles:
        if needle not in text:
            errors.append(f"{path} missing {needle!r}")

must("build.gradle", "version = '11.62.10'", "tc4_revealing_jar_gui_original_hud_reset")
must("src/main/resources/META-INF/mods.toml", 'version="11.62.10"', "tc4_revealing_jar_gui_original_hud_reset")
must("src/main/java/com/darkifov/thaumcraft/client/TC4RevealerHudAdapter.java",
     "hasIngamePopupRevealer", "isHeadRevealerStack", "ItemGoggles.showIngamePopups parity")
must("src/main/java/com/darkifov/thaumcraft/client/EssentiaOverlayEvents.java",
     "TC4 IGoggles.showIngamePopups parity pass",
     "TC4RevealerHudAdapter.hasIngamePopupRevealer",
     "TC4AuraNodeHudParity.ORIGINAL_HUD",
     "Void Jar", "Filtered Jar", "Warded Jar",
     "Essentia Valve", "Essentia Tube", "Alchemical Furnace",
     "textures/aspects/")
must("src/main/java/com/darkifov/thaumcraft/client/render/EssentiaJarRenderer.java",
     "TileJarRenderer bounds", "LIQUID_MIN_Y = 0.0625F", "LIQUID_HEIGHT = 0.625F", "crookedLabelRotation")
must("src/main/resources/assets/thaumcraft/models/item/goggles_of_revealing.json",
     "thaumcraft:item/tc4/gogglesrevealing")
must(".github/workflows/main.yml", "tc4_v11_62_10_revealing_jar_gui_audit.py", "build/libs/*-github.jar")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)
print("TC4 v11.62.10 revealing/jar GUI parity audit: OK")
