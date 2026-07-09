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

require("build.gradle", "version = '5.82.0'", "version = '5.62.0'")
require("src/main/resources/META-INF/mods.toml", 'version="5.82.0"', 'version="5.62.0"')
require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java",
    "Stage563-582",
    "NOTE_SLOT_X = 70",
    "NOTE_SLOT_Y = 10",
    "button == 1 && inOriginalNoteSlot",
    "requestResearchTableActionFromClient(menu.blockPos(), 0)",
    "requestResearchTableActionFromClient(menu.blockPos(), 1)",
    "requestResearchTableActionFromClient(menu.blockPos(), 2)",
    "Left-click remains vanilla slot pickup/move behavior",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java",
    "ASPECTS_PER_PAGE = 25",
    "renderAspectPageArrows",
    "leftPos + 10 + (local % 5) * 18",
    "topPos + 40 + (local / 5) * 18",
    "No opaque modern button squares",
)
# Guard against the most visible previous drift: fake plus marker and direct opaque palette button squares.
note_screen = read("src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java")
if 'Component.literal("+")' in note_screen:
    raise SystemExit("AUDIT FAIL: fake plus marker returned to ResearchNoteScreen")
if "0xAA1D140C" in note_screen and "renderAspectPalette" in note_screen:
    raise SystemExit("AUDIT FAIL: old opaque palette square color returned to ResearchNoteScreen")
require(
    "STAGE563_582_TC4_RESEARCH_TABLE_NOTE_PARITY_REPORT.json",
    "Stage563-582",
    "not 100% original yet",
    "right-click slot 1 prepares or opens the note",
    "ConfigResearch parity audit",
)
require("docs/NEXT_CHAT_PROMPT_STAGE582.md", "Stage563–582", "Stage583–602")
print("Stage563-582 research table/note parity audit: OK")
