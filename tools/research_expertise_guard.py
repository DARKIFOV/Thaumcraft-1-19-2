#!/usr/bin/env python3
"""Static regression guard for TC4 research connections and expertise perks."""
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []


def require(path: str, token: str, message: str) -> None:
    text = (ROOT / path).read_text(encoding="utf-8")
    if token not in text:
        errors.append(message)


def forbid(path: str, token: str, message: str) -> None:
    text = (ROOT / path).read_text(encoding="utf-8")
    if token in text:
        errors.append(message)


registry = "src/main/java/com/darkifov/thaumcraft/AspectCombinationRegistry.java"
solver = "src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java"
screen = "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java"
legacy_screen = "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java"

require(registry,
        "if (first == second) {\n            return false;",
        "identical aspects must not form a direct research link")
forbid(registry,
       "if (first == second) {\n            return true;",
       "legacy identical-aspect connection is still enabled")
require(solver,
        "ResearchNoteState.touchesCompatibleNeighbor(note, slot, aspect)",
        "server does not require a compatible occupied neighbour")
require(solver,
        'PlayerThaumData.hasResearch(player, "RESEARCHER2")',
        "Research Mastery check is missing")
require(solver,
        "player.getRandom().nextFloat() < 0.10F",
        "Research Mastery 10% free-placement chance is missing")
require(solver,
        'PlayerThaumData.hasResearch(player, "RESEARCHER1")',
        "Research Expertise 25% refund check is missing")
require(solver,
        "return roll < 0.50F;",
        "Research Mastery 50% removal refund is missing")
require(solver,
        "return roll < 0.25F;",
        "Research Expertise 25% removal refund is missing")
require(screen,
        "thaumcraft.gui.research.expertise_components",
        "Expertise component hint is missing from the table tooltip")
require(screen,
        "hasShiftDown() && hasResearchMastery()",
        "Mastery shift-click combination shortcut is missing")
require(screen,
        "touchesCompatibleClientNeighbor(slot, aspect)",
        "main research screen can preview disconnected placements")
require(legacy_screen,
        "ResearchAspectGraph.canConnect(aspect, other)",
        "legacy note screen can bypass direct-link placement validation")

for lang in ("en_us", "ru_ru"):
    path = ROOT / f"src/main/resources/assets/thaumcraft/lang/{lang}.json"
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:  # pragma: no cover - CI diagnostics
        errors.append(f"{lang}.json is invalid: {exc}")
        continue
    for key in (
        "thaumcraft.gui.research.expertise_components",
        "thaumcraft.gui.research.mastery_shift",
        "thaumcraft.message.research.expertise_saved",
    ):
        if key not in data:
            errors.append(f"{lang}.json is missing {key}")

if errors:
    print("Research connection / expertise guard: FAILED")
    for error in errors:
        print(f" - {error}")
    sys.exit(1)

print("Research connection / expertise guard: OK")
