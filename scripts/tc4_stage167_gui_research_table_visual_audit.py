#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def require(path: str, needle: str | None = None):
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing {path}")
        return ""
    text = p.read_text(encoding="utf-8", errors="ignore")
    if needle and needle not in text:
        errors.append(f"{path} missing {needle}")
    return text

screen = require("src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java")
menu = require("src/main/java/com/darkifov/thaumcraft/menu/ResearchTableMenu.java")
textures = require("src/main/java/com/darkifov/thaumcraft/client/screen/OriginalGuiTextures.java")
require("src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_gui_research_table_visual_stage167.json")
for texture in [
    "src/main/resources/assets/thaumcraft/textures/gui/thaumcraft_core_original/guiresearchtable2.png",
    "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/gui/guiresearchtable2.png",
]:
    if not (ROOT / texture).exists():
        errors.append(f"missing original TC4 research table texture {texture}")

required_screen = [
    "BG_WIDTH = 255",
    "BG_HEIGHT = 255",
    "RESEARCH_TABLE_TC4_ORIGINAL",
    "COPY_X = 37",
    "COPY_Y = 5",
    "232, 200, 24, 24",
    "No ink in Scribing Tools",
]
for needle in required_screen:
    if needle not in screen:
        errors.append(f"ResearchTableContainerScreen missing Stage167 visual parity marker: {needle}")

for needle in [
    "SLOT_SCRIBING_TOOLS, 14, 10",
    "SLOT_RESEARCH_NOTE, 70, 10",
    "48 + col * 18, 175 + row * 18",
    "48 + col * 18, 233",
]:
    if needle not in menu:
        errors.append(f"ResearchTableMenu missing original TC4 coordinate: {needle}")

if "thaumcraft_core_original/guiresearchtable2.png" not in textures:
    errors.append("OriginalGuiTextures does not expose TC4 guiresearchtable2 texture")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)
print("Stage167 GuiResearchTable visual parity audit: OK")
