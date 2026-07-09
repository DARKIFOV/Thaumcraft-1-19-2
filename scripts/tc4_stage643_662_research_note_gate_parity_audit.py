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

require("build.gradle", "version = '6.62.0'", "version = '6.42.0'")
require("src/main/resources/META-INF/mods.toml", 'version="6.62.0"', 'version="6.42.0"')
require(
    "src/main/java/com/darkifov/thaumcraft/research/TC4ResearchTableParity.java",
    "NOTE_HEX_DRAW_W = 20",
    "NOTE_HEX_DRAW_H = 18",
    "NOTE_HEX_HIT_RADIUS_SQ = 144.0D",
)
require(
    "src/main/java/com/darkifov/thaumcraft/research/ResearchNoteGrid.java",
    "TC4ResearchTableParity.NOTE_HEX_HIT_RADIUS_SQ",
)
require(
    "src/main/java/com/darkifov/thaumcraft/research/ResearchTableInventoryRuntime.java",
    "hasOpenResearchTable",
    "findOpenTableResearchNote",
    "findOpenTableScribingTools",
)
for packet in [
    "src/main/java/com/darkifov/thaumcraft/network/RequestPlaceResearchNoteAspectPacket.java",
    "src/main/java/com/darkifov/thaumcraft/network/RequestClearResearchNoteSlotPacket.java",
    "src/main/java/com/darkifov/thaumcraft/network/RequestSolveResearchNotePacket.java",
]:
    require(packet, "findOpenTableResearchNote", "Open the Research Table and place a Research Note in slot 1.")
    forbid(packet, "findHeldResearchNote(player).orElse", "Hold a Research Note")
require(
    "src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java",
    "This research note is already complete.",
    "ResearchNoteState.solved(note)",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java",
    "TC4ResearchTableParity.NOTE_HEX_DRAW_W",
    "TC4ResearchTableParity.NOTE_HEX_DRAW_H",
)
forbid(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java",
    "AspectColor.argb(draggedAspect",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java",
    "Stage643-662: warp remains preserved",
)
forbid(
    "src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java",
    '"Warp: " + entry.warp()',
)
recipes = [p.name for p in (ROOT / "src/main/resources/data/thaumcraft/recipes").glob("research_note*.json")]
if recipes:
    raise SystemExit("AUDIT FAIL: research note crafting recipes returned: " + ", ".join(recipes))
require("docs/STAGE643_662_RESEARCH_NOTE_GATE_PARITY.md", "Stage643-662", "open Research Table slot 1")
require("STAGE643_662_TC4_RESEARCH_NOTE_GATE_PARITY_REPORT.json", "Stage643-662", "6.62.0", "still_not_100_percent")
require("docs/NEXT_CHAT_PROMPT_STAGE662.md", "Stage643–662", "Stage663–682")
workflow = read(".github/workflows/main.yml")
if "tc4_stage643_662_research_note_gate_parity_audit.py" not in workflow:
    raise SystemExit("AUDIT FAIL: workflow missing Stage643-662 audit")
print("Stage643-662 research note gate parity audit: OK")
