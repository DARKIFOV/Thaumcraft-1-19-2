#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(path: str) -> str:
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing {path}")
        return ""
    return p.read_text(encoding="utf-8", errors="ignore")

be = read("src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java")
state = read("src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java")
packet = read("src/main/java/com/darkifov/thaumcraft/network/RequestResearchTableActionPacket.java")
note_item = read("src/main/java/com/darkifov/thaumcraft/block/ResearchNoteItem.java")
require_json = ROOT / "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_research_dupe_copy_stage168.json"
if not require_json.exists():
    errors.append("missing tc4_research_dupe_copy_stage168.json")

for needle in [
    "Items.INK_SAC",
    "consumeInventoryItem(player, Items.PAPER)",
    "consumeInventoryItem(player, Items.INK_SAC)",
    "hasCopyAspectCost",
    "consumeCopyAspectCost",
    "entry.aspects()",
    "ResearchNoteState.incrementCopyCount(note)",
    "note.grow(1)",
]:
    if needle not in be:
        errors.append(f"ResearchTableBlockEntity missing original duplicate parity behavior: {needle}")

if "consumeInk(ResearchTableInventoryRuntime.INK_PER_NOTE_CREATE, player)" in be[be.find("copyCompletedResearchNote"):be.find("private boolean hasCopyAspectCost")]:
    errors.append("copyCompletedResearchNote still consumes scribing-tool ink instead of original paper + ink sac copy cost")

for needle in ["TAG_COPIES", "copyCount", "incrementCopyCount", "stack.getOrCreateTag().putInt(TAG_COPIES"]:
    if needle not in state:
        errors.append(f"ResearchNoteState missing copy-count compatibility marker: {needle}")

if "packet.action == 3 || packet.action == 5" not in packet:
    errors.append("RequestResearchTableActionPacket does not accept original container copy action id 5")
if "Copies made" not in note_item:
    errors.append("ResearchNoteItem tooltip does not expose copies counter")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)
print("Stage168 RESEARCHDUPE copy parity audit: OK")
