#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    path = ROOT / rel
    if not path.exists():
        raise SystemExit(f"MISSING: {rel}")
    return path.read_text(encoding="utf-8")

def require(rel: str, *needles: str) -> None:
    data = read(rel)
    for needle in needles:
        if needle not in data:
            raise SystemExit(f"AUDIT FAIL: {rel} missing {needle!r}")

require("build.gradle", "version = '6.22.0'", "version = '6.02.0'")
require("src/main/resources/META-INF/mods.toml", 'version="6.22.0"', 'version="6.02.0"')
require(
    "src/main/java/com/darkifov/thaumcraft/research/TC4ResearchTableParity.java",
    "Stage603-622",
    "SLOT_SCRIBING_TOOLS_X = 14",
    "SLOT_RESEARCH_NOTE_X = 70",
    "COPY_ICON_X = 37",
    "ASPECT_GRID_X = 10",
    "ACTION_CREATE_NOTE = 0",
    "ACTION_COPY_COMPLETED_NOTE = 3",
    "isCopyAction",
)
require(
    "src/main/java/com/darkifov/thaumcraft/research/ResearchTableInventoryRuntime.java",
    "Stage603-622",
    "slot 0 is authoritative",
    "return Optional.empty();",
    "slot 1 is the original",
)
require(
    "src/main/java/com/darkifov/thaumcraft/menu/ResearchTableMenu.java",
    "TC4ResearchTableParity.SLOT_SCRIBING_TOOLS_X",
    "TC4ResearchTableParity.SLOT_RESEARCH_NOTE_X",
    "TC4ResearchTableParity.PLAYER_INVENTORY_X",
    "TC4ResearchTableParity.PLAYER_HOTBAR_Y",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java",
    "TC4ResearchTableParity.GUI_WIDTH",
    "ACTION_CREATE_NOTE",
    "ACTION_COMPLETE_SOLVED_NOTE",
    "ACTION_COPY_COMPLETED_NOTE",
    "COMBINE_ARROW_X",
    "PAGE_PREVIOUS_X",
)
require(
    "src/main/java/com/darkifov/thaumcraft/network/RequestResearchTableActionPacket.java",
    "TC4ResearchTableParity.ACTION_CREATE_NOTE",
    "TC4ResearchTableParity.ACTION_OPEN_NOTE",
    "TC4ResearchTableParity.ACTION_COMPLETE_SOLVED_NOTE",
    "TC4ResearchTableParity.isCopyAction",
)
require(
    "src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java",
    "Stage603-622",
    "hasInventoryItem(player, Items.PAPER)",
    "Stage603-622: preserve TC4 duplicate-copy semantics",
    "consumeInventoryItem(player, Items.INK_SAC)",
    "playOriginalResearchSound(\"write\"",
    "playOriginalResearchSound(\"learn\"",
    "TC4Sounds.event(key)",
)
recipes = list((ROOT / "src/main/resources/data/thaumcraft/recipes").glob("research_note*.json"))
if recipes:
    raise SystemExit("AUDIT FAIL: research notes became crafting-table recipes again: " + ", ".join(p.name for p in recipes))
if "return a sheet" in read("src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java"):
    raise SystemExit("AUDIT FAIL: old paper-return workaround returned; copy must pre-check materials")
require("docs/STAGE603_622_RESEARCH_TABLE_RECIPE_CLEANUP.md", "Research Table workflow", "not by a normal crafting-table JSON recipe")
require("STAGE603_622_TC4_RESEARCH_TABLE_DEEP_PARITY_REPORT.json", "Stage603-622", "not 100% original yet", "strict table-slot ownership")
require("docs/NEXT_CHAT_PROMPT_STAGE622.md", "Stage603–622", "Stage623–642")
workflow = read(".github/workflows/main.yml")
if "tc4_stage603_622_research_table_deep_parity_audit.py" not in workflow:
    raise SystemExit("AUDIT FAIL: workflow missing Stage603-622 audit")
print("Stage603-622 research table deep parity audit: OK")
