#!/usr/bin/env python3
"""Regression guard for v11.62.54 original wand HUD and focus-cycle parity."""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel: str) -> str:
    p = ROOT / rel
    if not p.is_file():
        errors.append(f"missing {rel}")
        return ""
    return p.read_text(encoding="utf-8")

def need(text: str, token: str, label: str) -> None:
    if token not in text:
        errors.append(f"{label}: missing {token!r}")

def forbid(text: str, token: str, label: str) -> None:
    if token in text:
        errors.append(f"{label}: forbidden {token!r}")

build = read("build.gradle")
mods = read("src/main/resources/META-INF/mods.toml")
hud = read("src/main/java/com/darkifov/thaumcraft/client/WandVisOverlayEvents.java")
config = read("src/main/java/com/darkifov/thaumcraft/config/ThaumcraftConfig.java")
manager = read("src/main/java/com/darkifov/thaumcraft/wand/WandManagerRuntime.java")
pouch = read("src/main/java/com/darkifov/thaumcraft/block/FocusPouchItem.java")

def current_version(text: str, pattern: str, label: str) -> tuple[int, ...] | None:
    match = re.search(pattern, text, re.MULTILINE)
    if match is None:
        errors.append(f"{label}: current semantic version not found")
        return None
    return tuple(int(part) for part in match.group(1).split("."))

build_version = current_version(
    build,
    r"^version\s*=\s*'([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?'",
    "build",
)
mods_version = current_version(
    mods,
    r'^version="([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?"',
    "mods",
)
for label, version in (("build", build_version), ("mods", mods_version)):
    if version is not None and version < (11, 62, 54):
        errors.append(f"{label}: expected v11.62.54 or later")

# Original Config.dialBottom default and 32x32 corner placement.
need(config, "WAND_DIAL_BOTTOM", "config")
need(config, 'define("wandDialBottom", false)', "config")
need(hud, "boolean dialBottom = ThaumcraftConfig.WAND_DIAL_BOTTOM.get()", "hud")
need(hud, "int dialX = 0", "hud")
need(hud, "getGuiScaledHeight() - 32", "hud")
need(hud, "poseStack.scale(0.5F, 0.5F, 1.0F)", "hud")
need(hud, "GuiComponent.blit(poseStack, 0, 0, 0, 0, 64, 64, 256, 256)", "hud")
need(hud, "if (!dialBottom)", "hud")
need(hud, "Integer.toString(amount / 100)", "hud")
need(hud, "renderFocusOrTradeIcon", "hud")
need(hud, "FocusArchitectRuntime.pickedBlock", "hud")
need(hud, "getCooldownPercent", "hud")
need(hud, "ClientPlayerNetworkEvent.LoggingOut", "hud")
forbid(hud, "thaumcraft.hud.wand.vis", "hud invented total label")
forbid(hud, "thaumcraft.hud.wand.focus", "hud invented focus label")

# Exact TreeMap key semantics and transactional swap/rollback.
need(manager, "foci.put(FocusPouchItem.sortingHelper(stack)", "focus manager")
need(manager, "FocusLocation selectedLocation = foci.get(key)", "focus manager")
need(manager, "foci.higherEntry(key)", "focus manager")
need(manager, "foci.firstEntry().getValue()", "focus manager")
need(manager, "tryStoreFocus(player, installed)", "focus manager")
need(manager, "selectedLocation.putBack(player, next)", "focus manager")
need(manager, "removeCurrentFocus", "focus manager")
forbid(manager, "uniqueKey(", "focus manager")
forbid(manager, "#%04d", "focus manager")
forbid(manager, "Equipped ", "focus manager non-original chat")
need(pouch, "putFocusAt", "focus pouch")
need(pouch, "slot < 0 || slot >= inv.length", "focus pouch")

if errors:
    print("v11.62.54 focus HUD/cycle guard: FAIL")
    for error in errors:
        print(" -", error)
    sys.exit(1)
print("v11.62.54 focus HUD/cycle guard: OK (original 32x32 dial, focus/trade icon, cooldown, TreeMap cycle and transactional rollback)")
