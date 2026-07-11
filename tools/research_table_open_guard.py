#!/usr/bin/env python3
from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
block = (root / "src/main/java/com/darkifov/thaumcraft/block/ResearchTableBlock.java").read_text()
hooks = (root / "src/main/java/com/darkifov/thaumcraft/client/ClientHooks.java").read_text()
packet = (root / "src/main/java/com/darkifov/thaumcraft/network/OpenResearchTablePacket.java").read_text()
legacy_screen = root / "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableScreen.java"
errors = []
if "NetworkHooks.openScreen(serverPlayer, table" not in block:
    errors.append("ResearchTableBlock no longer opens its server-backed menu")
if "openResearchTable" in hooks or "ResearchTableScreen" in hooks:
    errors.append("ClientHooks can still open the duplicate unbound research screen")
if legacy_screen.exists():
    errors.append("duplicate ResearchTableScreen.java still exists")
if "Deliberate no-op" not in packet or "setScreen" in packet:
    errors.append("legacy packet can still open a client-only research screen")
if errors:
    print("Research Table open guard: FAIL")
    for error in errors:
        print(" -", error)
    sys.exit(1)
print("Research Table open guard: OK")
