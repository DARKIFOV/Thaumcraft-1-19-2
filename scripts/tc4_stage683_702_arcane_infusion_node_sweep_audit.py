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

def forbid(rel: str, *needles: str) -> None:
    data = read(rel)
    for needle in needles:
        if needle in data:
            raise SystemExit(f"AUDIT FAIL: {rel} still contains forbidden drift {needle!r}")

require("build.gradle", "version = '7.02.0'", "version = '6.82.0'")
require("src/main/resources/META-INF/mods.toml", 'version="7.02.0"', 'version="6.82.0"')
require(
    "src/main/java/com/darkifov/thaumcraft/arcane/TC4ArcaneWorkbenchParity.java",
    "Stage683-702",
    "GUI_WIDTH = 190",
    "GUI_HEIGHT = 234",
    "GRID_SPACING = 24",
    "OUTPUT_SLOT_X = 160",
    "WAND_SLOT_X = 160",
    "ASPECT_LOCS",
)
require(
    "src/main/java/com/darkifov/thaumcraft/menu/ArcaneWorkbenchMenu.java",
    "TC4ArcaneWorkbenchParity.OUTPUT_SLOT_X",
    "TC4ArcaneWorkbenchParity.WAND_SLOT_X",
    "TC4ArcaneWorkbenchParity.gridSlotX",
    "arcaneWorkbench.canTakeOutput(player)",
)
require(
    "src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java",
    "findMatchingArcaneRecipe(player, false, false)",
    "public boolean canTakeOutput",
    "findMatchingArcaneRecipe(player, false, true)",
    "findMatchingArcaneRecipe(Player player, boolean message, boolean requireVis)",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchContainerScreen.java",
    "TC4ArcaneWorkbenchParity.GUI_WIDTH",
    "TC4ArcaneWorkbenchParity.ASPECT_LOCS",
    "original TC4 does not draw a modern text warning",
)
forbid(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchContainerScreen.java",
    "Insufficient vis",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/TC4AuraNodeHudParity.java",
    "NODE_SHEET_FRAMES = 32",
    "NODE_FRAME_PIXELS = 64",
    "NODE_SHEET_PIXELS = 2048",
    "NODE_BUBBLE_PIXELS = 256",
    "stripFor(AuraNodeType type)",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java",
    "TC4AuraNodeHudParity.NODE_SHEET_FRAMES",
    "TC4AuraNodeHudParity.stripFor(node.typedNodeType())",
    "TC4AuraNodeHudParity.ORIGINAL_NODE_BUBBLE",
    "TC4AuraNodeHudParity.ORIGINAL_NODES",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/TC4RevealerHudAdapter.java",
    "TC4AuraNodeHudParity.ORIGINAL_HUD",
    "TC4AuraNodeHudParity.stripFor(node.nodeType())",
    "TC4AuraNodeHudParity.NODE_BUBBLE_PIXELS",
    "TC4AuraNodeHudParity.NODE_SHEET_PIXELS",
)
require(
    "src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java",
    "recipeingredients",
    "recipeessentia",
    "recipeinstability",
    "serializePendingAspects",
)
require("docs/STAGE683_702_ARCANE_INFUSION_NODE_SWEEP.md", "Stage683-702", "not add new mechanics")
require("STAGE683_702_TC4_ARCANE_INFUSION_NODE_SWEEP_REPORT.json", "Stage683-702", "7.02.0", "still_not_100_percent")
require("docs/NEXT_CHAT_PROMPT_STAGE702.md", "Stage683–702", "Stage703–722")
workflow = read(".github/workflows/main.yml")
if "tc4_stage683_702_arcane_infusion_node_sweep_audit.py" not in workflow:
    raise SystemExit("AUDIT FAIL: workflow missing Stage683-702 audit")
print("Stage683-702 arcane/infusion/node sweep audit: OK")
