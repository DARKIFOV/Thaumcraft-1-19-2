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
            raise SystemExit(f"AUDIT FAIL: {rel} still contains forbidden visible/debug text {needle!r}")

require("build.gradle", "version = '6.42.0'", "version = '6.22.0'")
require("src/main/resources/META-INF/mods.toml", 'version="6.42.0"', 'version="6.22.0"')
require(
    "src/main/java/com/darkifov/thaumcraft/block/ScribingToolsItem.java",
    "Stage623-642",
    "TC4_ORIGINAL_REFILL_RECIPE_ID",
    "scribing_tools_refill_original_tc4_style",
    "refillFromInkSac",
    "craft with an ink sac",
)
require(
    "src/main/resources/data/thaumcraft/recipes/scribing_tools_refill_original_tc4_style.json",
    "thaumcraft:scribing_tools",
    "minecraft:ink_sac",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java",
    "TC4ResearchTableParity.ASPECT_GRID_X",
    "TC4ResearchTableParity.ASPECT_GRID_COLUMNS",
    "TC4ResearchTableParity.ASPECT_GRID_STEP",
    "TC4ResearchTableParity.PAGE_PREVIOUS_X",
    "TC4ResearchTableParity.PAGE_NEXT_X",
)
forbid(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java",
    "TC4 hex ",
    "Drag an aspect onto",
    "Placement sent.",
    "Dropped outside the TC4 research hex grid.",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java",
    "Stage623-642: original Thaumonomicon pages do not print raw",
    "Stage623-642: visible Thaumonomicon recipe pages should look like",
    "Stage623-642: do not paint inferred pattern letters",
)
forbid(
    "src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java",
    "TC4 page key: ",
    "No matching TC4 ConfigRecipes entry yet",
    "missing recipe key",
    "Original TC4 ItemStack Page",
    "drawCenteredString(poseStack, font, Component.literal(recipe.key())",
)
recipes = [p.name for p in (ROOT / "src/main/resources/data/thaumcraft/recipes").glob("research_note*.json")]
if recipes:
    raise SystemExit("AUDIT FAIL: research note crafting recipes returned: " + ", ".join(recipes))
require("docs/STAGE623_642_RESEARCH_VISUAL_SCRIBING_PARITY.md", "Stage623-642", "Scribing Tools refill", "not a new right-click ability")
require("STAGE623_642_TC4_RESEARCH_VISUAL_SCRIBING_REPORT.json", "Stage623-642", "6.42.0", "still_not_100_percent")
require("docs/NEXT_CHAT_PROMPT_STAGE642.md", "Stage623–642", "Research Table / Research Note")
workflow = read(".github/workflows/main.yml")
if "tc4_stage623_642_research_visual_scribing_audit.py" not in workflow:
    raise SystemExit("AUDIT FAIL: workflow missing Stage623-642 audit")
print("Stage623-642 research visual/scribing parity audit: OK")
