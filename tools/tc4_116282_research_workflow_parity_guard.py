#!/usr/bin/env python3
"""Static regression guard for v11.62.91 TC4 research-note/table workflow parity."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []
checks = 0


def text(rel: str) -> str:
    path = ROOT / rel
    if not path.is_file():
        errors.append(f"missing file: {rel}")
        return ""
    return path.read_text(encoding="utf-8")


def require(label: str, condition: bool) -> None:
    global checks
    checks += 1
    if not condition:
        errors.append(label)


build = text("build.gradle")
mods = text("src/main/resources/META-INF/mods.toml")
note_item = text("src/main/java/com/darkifov/thaumcraft/block/ResearchNoteItem.java")
creator = text("src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteCreator.java")
complete_packet = text("src/main/java/com/darkifov/thaumcraft/network/RequestCompleteSelectedResearchPacket.java")
table_packet = text("src/main/java/com/darkifov/thaumcraft/network/RequestResearchTableActionPacket.java")
table_screen = text("src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java")
table_be = text("src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java")
bonus = text("src/main/java/com/darkifov/thaumcraft/research/ResearchTableBonusRuntime.java")
manifest_text = text("runtime_artifacts/runtime_test_manifest.template.json")

require("build version 11.62.91", "version = '11.62.91'" in build)
require("mods version 11.62.91", 'version="11.62.91"' in mods)

# Unfinished notes must not open a freehand puzzle; completed notes still learn on use.
require("research note retains solved-note conversion", "ResearchNoteSolver.convertSolvedNote(player, stack)" in note_item)
require("unfinished note explicitly requires Research Table", "Place this research note in a Research Table" in note_item)
require("research note item no longer opens puzzle directly", "ThaumcraftNetwork.openResearchNote" not in note_item)

# Clicking an available primary entry creates the inventory note only.
require("Thaumonomicon request creates targeted note", "TC4ResearchNoteCreator.create(player, entry)" in complete_packet)
require("Thaumonomicon request does not open note GUI", "ThaumcraftNetwork.openResearchNote(player, note)" not in complete_packet)
require("note creation consumes original paper path", "paper.shrink(1)" in creator)
require("note creation consumes scribing-tool ink", "ScribingToolsItem.consumeInk(tools, INK_COST)" in creator)
require("note creation plays original learn sound", 'TC4Sounds.event("learn")' in creator and "0.75F, 1.0F" in creator)

# The table note slot must remain a physical container slot, not a rebuild action button.
require("screen documents physical note-slot handling", "The note slot is a real container slot in TC4" in table_screen)
require("screen no longer requests table-side note creation", "ACTION_CREATE_NOTE" not in table_screen)
require("screen no longer learns discovery through shift-click", "ACTION_COMPLETE_SOLVED_NOTE" not in table_screen)
require("legacy create action is rejected with Thaumonomicon guidance", "Create research notes from an available Thaumonomicon entry" in table_packet)
require("legacy complete action is rejected with item-use guidance", "Take the completed discovery and use it" in table_packet)

# Compile and persistence regressions.
require("no duplicate unreachable return false", "return false;\n            return false;" not in table_be)
require("fixed-size inventory clear loop", "for (int i = 0; i < items.size(); i++)" in table_be)
require("load resets fixed-size inventory", table_be.count("items.set(i, ItemStack.EMPTY)") >= 2)

# Now-functional Brain in a Jar must participate in the original random bonus source.
require("brain jar is research bonus source", "ThaumcraftMod.BRAIN_JAR.get()" in bonus)
require("brain jar uses original 1/200 chance", "? 200 : 0" in bonus)
require("bookshelf retains original 1/300 chance", "state.is(Blocks.BOOKSHELF) ? 300" in bonus)

try:
    manifest = json.loads(manifest_text)
except json.JSONDecodeError as exc:
    errors.append(f"runtime manifest invalid JSON: {exc}")
    manifest = {}

require("runtime manifest version 11.62.91", manifest.get("version") == "11.62.91")
test_ids = {entry.get("id") for entry in manifest.get("tests", []) if isinstance(entry, dict)}
for test_id in (
    "research.note_creation_thaumonomicon_inventory_only",
    "research.unfinished_note_requires_table_completed_note_item_use",
    "research.table_bonus_bookshelf_brain_jar_persistence",
):
    require(f"runtime test present: {test_id}", test_id in test_ids)

if errors:
    print(f"TC4 11.62.91 research workflow parity guard: FAIL ({len(errors)} problems; {checks} checks)")
    for problem in errors:
        print(f" - {problem}")
    raise SystemExit(1)

print(f"TC4 11.62.91 research workflow parity guard: PASS ({checks}/{checks} checks)")
