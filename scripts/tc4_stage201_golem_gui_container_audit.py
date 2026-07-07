#!/usr/bin/env python3
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
checks = [
    ("GolemMenu.java", "src/main/java/com/darkifov/thaumcraft/menu/GolemMenu.java", [
        "class GolemMenu extends AbstractContainerMenu",
        "currentScroll",
        "maxScroll",
        "clickMenuButton",
        "button == 66",
        "button == 67",
        "button >= 50 && button <= 57",
        "cycleGolemColor",
        "GolemVisibleSlot",
        "100 + a / 2 * 28",
        "16 + a % 2 * 31",
        "8 + j * 18, 84 + i * 18",
        "8 + i * 18, 142",
    ]),
    ("GolemInventoryContainer.java", "src/main/java/com/darkifov/thaumcraft/menu/GolemInventoryContainer.java", [
        "implements Container",
        "activeSlotCount",
        "getGolemInventoryStack",
        "setGolemInventoryStack",
    ]),
    ("GolemScreen.java", "src/main/java/com/darkifov/thaumcraft/client/screen/GolemScreen.java", [
        "textures/gui/thaumcraft_core_original/guigolem.png",
        "handleInventoryButtonClick(menu.containerId, 66)",
        "handleInventoryButtonClick(menu.containerId, 67)",
        "menu.golemColor",
        "menu.golemToggle",
    ]),
    ("ThaumcraftMod.java", "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java", [
        "GOLEM_MENU",
        "new GolemMenu(windowId, inv, data)",
    ]),
    ("ClientModEvents.java", "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java", [
        "MenuScreens.register(ThaumcraftMod.GOLEM_MENU.get(), GolemScreen::new)",
    ]),
    ("ThaumGolemEntity.java", "src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java", [
        "NetworkHooks.openScreen",
        "new GolemMenu(containerId, inventory, this)",
        "NBT_TOGGLES",
        "activeSlotCount",
        "setPausedByGolemGui",
        "cycleGolemColor",
    ]),
]

errors: list[str] = []
for label, rel, snippets in checks:
    path = ROOT / rel
    if not path.exists():
        errors.append(f"missing {label}: {rel}")
        continue
    text = path.read_text(encoding="utf-8")
    for snippet in snippets:
        if snippet not in text:
            errors.append(f"{label} missing snippet: {snippet}")

texture = ROOT / "src/main/resources/assets/thaumcraft/textures/gui/thaumcraft_core_original/guigolem.png"
if not texture.exists():
    errors.append("missing original guigolem texture resource")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)
print("TC4 Stage201 golem GUI/container audit: OK")
