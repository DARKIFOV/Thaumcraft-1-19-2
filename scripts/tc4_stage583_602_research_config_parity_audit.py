#!/usr/bin/env python3
from pathlib import Path
import re

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

require("build.gradle", "version = '6.02.0'", "version = '5.82.0'")
require("src/main/resources/META-INF/mods.toml", 'version="6.02.0"', 'version="5.82.0"')
require(
    "src/main/java/com/darkifov/thaumcraft/research/TC4ResearchFlagPolicy.java",
    "Stage583-602",
    "AUTO_UNLOCK",
    "STUB",
    "HIDDEN",
    "LOST",
    "canCreateNormalResearchNote",
    "visibleInBook",
)
require(
    "src/main/java/com/darkifov/thaumcraft/research/OriginalResearchBridge.java",
    "TC4ResearchFlagPolicy.canCreateNormalResearchNote",
    "canSelectForResearchTable",
    "selectForResearchTable",
)
require(
    "src/main/java/com/darkifov/thaumcraft/network/RequestSelectResearchPacket.java",
    "selectForResearchTable",
    "cannot become a normal research-table note yet",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/OriginalResearchLayout.java",
    "Stage583-602",
    "TC4ResearchFlagPolicy.HIDDEN",
    "TC4ResearchFlagPolicy.LOST",
)
require(
    "src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java",
    "TAG_TC4_CATEGORY",
    "TAG_TC4_HIDDEN_PARENTS",
    "TAG_TC4_SIBLINGS",
    "TAG_TC4_FLAGS",
    "TAG_TC4_WARP",
    "putOriginalResearchMetadata",
)
require(
    "src/main/java/com/darkifov/thaumcraft/research/ResearchNoteRequirements.java",
    "supplementFromOriginalAspectComponents",
    "Optional<ResearchEntry> originalEntry",
)
# Guard against the old drift where the selector accepted any original key, including hidden/lost/stub.
if "boolean selected = OriginalResearchBridge.select(player, packet.researchKey);" in read("src/main/java/com/darkifov/thaumcraft/network/RequestSelectResearchPacket.java"):
    raise SystemExit("AUDIT FAIL: Research selector accepts every original key again")
# Static sanity: the bridge should still expose a large ConfigResearch port, not a tiny handmade list.
bridge = read("src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java")
entry_count = bridge.count("new ResearchEntry(")
if entry_count < 150:
    raise SystemExit(f"AUDIT FAIL: expected large ConfigResearch bridge, found only {entry_count} entries")
require(
    "STAGE583_602_TC4_RESEARCH_CONFIG_PARITY_REPORT.json",
    "Stage583-602",
    "ConfigResearch flags",
    "research-note NBT metadata",
    "not 100% original yet",
)
require("docs/NEXT_CHAT_PROMPT_STAGE602.md", "Stage583–602", "Stage603–622")
print("Stage583-602 research ConfigResearch parity audit: OK")
