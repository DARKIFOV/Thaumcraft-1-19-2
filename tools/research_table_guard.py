#!/usr/bin/env python3
"""Fail CI if the TC4 research puzzle escapes the Research Table container again."""
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []


def require(condition: bool, message: str) -> None:
    if not condition:
        errors.append(message)


screen_path = ROOT / "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java"
parity_path = ROOT / "src/main/java/com/darkifov/thaumcraft/research/TC4ResearchTableParity.java"
packet_path = ROOT / "src/main/java/com/darkifov/thaumcraft/network/RequestResearchTableActionPacket.java"
tile_path = ROOT / "src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java"
network_path = ROOT / "src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java"

for path in (screen_path, parity_path, packet_path, tile_path, network_path):
    require(path.is_file(), f"missing research integration source: {path.relative_to(ROOT)}")

if not errors:
    screen = screen_path.read_text(encoding="utf-8")
    parity = parity_path.read_text(encoding="utf-8")
    packet = packet_path.read_text(encoding="utf-8")
    tile = tile_path.read_text(encoding="utf-8")
    network = network_path.read_text(encoding="utf-8")

    require("blitTc4ResearchTableBackground" in screen,
            "main Research Table no longer uses the original TC4 background")
    require("blitTc4ResearchParchment" in screen,
            "parchment is no longer rendered inside the main Research Table")
    require("ResearchNoteGrid.slots()" in screen and "ResearchNoteGrid.hitTest" in screen,
            "integrated hex grid rendering or interaction is missing")
    require("buildConnectionView" in screen and "ArrayDeque" in screen,
            "anchor-rooted TC4 connection traversal is missing")
    require("requestPlaceResearchNoteAspectFromClient" in screen,
            "dragging an aspect onto the integrated parchment no longer sends placement")
    require("requestClearResearchNoteSlotFromClient" in screen,
            "right-click clear on the integrated parchment is missing")
    require("ACTION_SYNC_NOTE" in screen and "ACTION_OPEN_NOTE" not in screen,
            "main table still depends on the rebuild-only second research screen")
    require("containerTick" in screen and "noteSignature" in screen,
            "live note insertion/removal is no longer synchronized while the table stays open")
    require("ACTION_SYNC_NOTE = 4" in parity,
            "dedicated note-NBT synchronization action changed or disappeared")
    require("ACTION_SYNC_NOTE" in packet and "syncResearchNote(player)" in packet,
            "server action packet no longer handles note-only synchronization")
    require("void syncResearchNote(ServerPlayer player)" in tile,
            "Research Table block entity lost its note synchronization entry point")
    require("ResearchNoteGrid.MIN_RADIUS" in network and "ResearchNoteSyncPacket" in network,
            "empty-note clearing packet is missing; stale puzzles could remain client-side")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("Research Table integration guard: OK")
