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
parity = require("src/main/java/com/darkifov/thaumcraft/research/TC4ResearchTableParity.java")
require("src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_gui_research_table_visual_stage167.json")
for texture in [
    "src/main/resources/assets/thaumcraft/textures/gui/thaumcraft_core_original/guiresearchtable2.png",
    "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/gui/guiresearchtable2.png",
]:
    if not (ROOT / texture).exists():
        errors.append(f"missing original TC4 research table texture {texture}")

required_screen = [
    "RESEARCH_TABLE_TC4_ORIGINAL",
    "TC4ResearchTableParity.GUI_WIDTH",
    "TC4ResearchTableParity.GUI_HEIGHT",
    "TC4ResearchTableParity.COPY_ICON_X",
    "TC4ResearchTableParity.COPY_ICON_Y",
    "232, 200, 24, 24",
]
for needle in required_screen:
    if needle not in screen:
        errors.append(f"ResearchTableContainerScreen missing Stage167 visual parity marker: {needle}")

# v8.82 semantic audit: old Stage167 literal-token checks accepted overlay text.
# Original GuiResearchTable does not draw modern adapter text over the copied texture.
for forbidden in ['Component.literal("No ink', 'Component.literal("Right-click']:
    if forbidden in screen:
        errors.append(f"ResearchTableContainerScreen should not overlay modern adapter text: {forbidden}")

required_parity_constants = [
    "GUI_WIDTH = 255",
    "GUI_HEIGHT = 255",
    "SLOT_SCRIBING_TOOLS_X = 14",
    "SLOT_SCRIBING_TOOLS_Y = 10",
    "SLOT_RESEARCH_NOTE_X = 70",
    "SLOT_RESEARCH_NOTE_Y = 10",
    "COPY_ICON_X = 37",
    "COPY_ICON_Y = 5",
    "PLAYER_INVENTORY_X = 48",
    "PLAYER_INVENTORY_Y = 175",
    "PLAYER_HOTBAR_Y = 233",
]
for needle in required_parity_constants:
    if needle not in parity:
        errors.append(f"TC4ResearchTableParity missing original TC4 coordinate: {needle}")

for needle in [
    "ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS, TC4ResearchTableParity.SLOT_SCRIBING_TOOLS_X, TC4ResearchTableParity.SLOT_SCRIBING_TOOLS_Y",
    "ResearchTableBlockEntity.SLOT_RESEARCH_NOTE, TC4ResearchTableParity.SLOT_RESEARCH_NOTE_X, TC4ResearchTableParity.SLOT_RESEARCH_NOTE_Y",
    "TC4ResearchTableParity.PLAYER_INVENTORY_X + col * 18, TC4ResearchTableParity.PLAYER_INVENTORY_Y + row * 18",
    "TC4ResearchTableParity.PLAYER_INVENTORY_X + col * 18, TC4ResearchTableParity.PLAYER_HOTBAR_Y",
]:
    if needle not in menu:
        errors.append(f"ResearchTableMenu missing centralized original TC4 coordinate usage: {needle}")

if "thaumcraft_core_original/guiresearchtable2.png" not in textures:
    errors.append("OriginalGuiTextures does not expose TC4 guiresearchtable2 texture")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)
print("Stage167 GuiResearchTable visual parity audit: OK")
