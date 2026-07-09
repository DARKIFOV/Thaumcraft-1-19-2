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

require("build.gradle", "version = '6.82.0'", "version = '6.62.0'")
require("src/main/resources/META-INF/mods.toml", 'version="6.82.0"', 'version="6.62.0"')
require(
    "src/main/java/com/darkifov/thaumcraft/research/TC4ResearchTableParity.java",
    "isNoteSlotHit",
    "isScribingToolsSlotHit",
    "isCopyIconHit",
    "isPreviousAspectPageHit",
    "isNextAspectPageHit",
    "isCombineArrowHit",
    "isAspectIconHit",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java",
    "TC4ResearchTableParity.isNoteSlotHit",
    "TC4ResearchTableParity.isPreviousAspectPageHit",
    "TC4ResearchTableParity.isNextAspectPageHit",
    "TC4ResearchTableParity.isCombineArrowHit",
    "TC4ResearchTableParity.isCopyIconHit",
    "Stage663-682: keep visual parity",
)
forbid(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java",
    "Right-click: prepare",
    "Right-click: open",
    "Shift-right-click: complete",
    "No ink in Scribing Tools",
    "Copy completed research note",
    "Copy appears only",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java",
    "TC4ResearchTableParity.isPreviousAspectPageHit",
    "TC4ResearchTableParity.isNextAspectPageHit",
    "TC4ResearchTableParity.isAspectIconHit",
    "!ClientResearchNoteData.solved()",
)
forbid(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java",
    "Previous aspects page",
    "Next aspects page",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableScreen.java",
    "TC4ResearchTableParity.isPreviousAspectPageHit",
    "TC4ResearchTableParity.isNextAspectPageHit",
    "TC4ResearchTableParity.isCombineArrowHit",
    "TC4ResearchTableParity.isAspectIconHit",
    "0xFFC08A32",
)
forbid(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableScreen.java",
    "Previous aspects page",
    "Next aspects page",
    "Select two TC4-discovered aspects",
    "AspectColor.dim",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ThaumonomiconScreen.java",
    "Stage663-682: keep the browser frame visually original",
    "popupUntil = 0L",
)
forbid(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ThaumonomiconScreen.java",
    '"Complete " + complete',
    '"Available " + available',
    "Forbidden knowledge / Warp",
    "Missing required research",
    "Research note requested",
    "Aspects: ",
    "click to open pages",
    "click to create research note",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java",
    "Original TC4 book pages do not display raw adapter page type strings",
    "Recipe page type is kept in data for gates/audits",
)
forbid(
    "src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java",
    "drawString(poseStack, font, pageTypeLabel(type)",
    "fill(poseStack, x, y, x + 190, y + 150",
    "0x22A06D2B",
    "0x22F5E0B8",
    "0x553F2612",
    "fill(poseStack, x, y, x + 16, y + 16, 0x33FFFFFF)",
)
recipes = [p.name for p in (ROOT / "src/main/resources/data/thaumcraft/recipes").glob("research_note*.json")]
if recipes:
    raise SystemExit("AUDIT FAIL: research note crafting recipes returned: " + ", ".join(recipes))
require("docs/STAGE663_682_RESEARCH_BOOK_PIXEL_PARITY.md", "Stage663-682", "not add new mechanics")
require("STAGE663_682_TC4_RESEARCH_BOOK_PIXEL_PARITY_REPORT.json", "Stage663-682", "6.82.0", "still_not_100_percent")
require("docs/NEXT_CHAT_PROMPT_STAGE682.md", "Stage663–682", "Stage683–702")
workflow = read(".github/workflows/main.yml")
if "tc4_stage663_682_research_book_pixel_parity_audit.py" not in workflow:
    raise SystemExit("AUDIT FAIL: workflow missing Stage663-682 audit")
print("Stage663-682 research/book pixel parity audit: OK")
