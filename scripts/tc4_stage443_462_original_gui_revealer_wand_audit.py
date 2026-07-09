#!/usr/bin/env python3
from pathlib import Path
import json
import hashlib

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(path: str) -> str:
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing file: {path}")
        return ""
    return p.read_text(encoding="utf-8", errors="ignore")

def require_contains(path: str, token: str, label: str) -> None:
    if token not in read(path):
        errors.append(f"{label}: missing token {token!r} in {path}")

def require_file(path: str, label: str = "file") -> Path:
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing {label}: {path}")
    elif p.is_file() and p.stat().st_size <= 0:
        errors.append(f"empty {label}: {path}")
    return p

def same(dst: str, src: str, label: str) -> None:
    dp = require_file(dst, label + " active")
    sp = require_file(src, label + " original")
    if dp.exists() and sp.exists():
        if hashlib.md5(dp.read_bytes()).hexdigest() != hashlib.md5(sp.read_bytes()).hexdigest():
            errors.append(f"{label}: active asset is not byte-identical to original TC4 asset")

build = read("build.gradle")
mods = read("src/main/resources/META-INF/mods.toml")
if "version = '4.62.0'" not in build:
    errors.append("Stage443-462 must set build.gradle project version to 4.62.0")
if "version = '4.42.0'" not in build:
    errors.append("Stage443-462 lost Stage423-442 compatibility marker in build.gradle")
if 'version="4.62.0"' not in mods:
    errors.append("Stage443-462 must set mods.toml version to 4.62.0")
if 'version="4.42.0"' not in mods:
    errors.append("Stage443-462 lost Stage423-442 compatibility marker in mods.toml")

hud = "src/main/java/com/darkifov/thaumcraft/client/TC4RevealerHudAdapter.java"
for token in [
    "Stage443-462 tightens IRevealer parity",
    "isHeadRevealerStack(player.getItemBySlot(EquipmentSlot.HEAD))",
    "isHandRevealerStack(player.getItemInHand(hand))",
    "stack.is(ThaumcraftMod.THAUMOMETER.get())",
    "NODE_FRAME_PIXELS = 64",
    "NODE_SHEET_PIXELS = 2048",
    "NODE_BUBBLE_PIXELS = 256",
]:
    require_contains(hud, token, "Stage443 revealer HUD")
old_revealer_shortcut = "for (InteractionHand hand : InteractionHand.values()) {\n            if (isRevealerStack(player.getItemInHand(hand)))"
if old_revealer_shortcut in read(hud):
    errors.append("Stage443 revealer drift: goggles/helmet can still reveal from hand through isRevealerStack")

arcane = "src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchContainerScreen.java"
for token in [
    "Stage443-462: original GuiArcaneWorkbench shows the primal aspect cost",
    "ClientArcaneRecipePage recipe = recipeForVisibleGrid();",
    "recipe = recipeForOutput();",
    "OriginalGuiTextures.ARCANE_WORKBENCH",
]:
    require_contains(arcane, token, "Stage443 Arcane Workbench GUI")

research = "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java"
for token in [
    "Stage443-462 coordinate ledger from original GuiResearchTable",
    "copy icon 37,5",
    "drawAspectSlot(poseStack, firstAspect, leftPos + 13, topPos + 139)",
    "drawAspectSlot(poseStack, secondAspect, leftPos + 71, topPos + 139)",
    "OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL",
]:
    require_contains(research, token, "Stage443 Research Table GUI")

node_renderer = "src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java"
for token in [
    "NODE_SHEET_CELL_UV = 1.0F / FRAMES",
    "original nodes.png is 32x32 cells, 64px each on 2048px atlas",
    "frame * NODE_SHEET_CELL_UV",
    "strip * NODE_SHEET_CELL_UV",
]:
    require_contains(node_renderer, token, "Stage443 Aura Node renderer")

for path, token, label in [
    ("src/main/java/com/darkifov/thaumcraft/client/render/ThaumometerItemRenderer.java", "Stage443-462 transform ledger", "Thaumometer renderer"),
    ("src/main/java/com/darkifov/thaumcraft/client/render/TC4GogglesLayer.java", "Stage443-462 keeps the original", "Goggles layer"),
    ("src/main/java/com/darkifov/thaumcraft/client/render/WandItemRenderer.java", "Stage443-462 wand visual parity", "Wand renderer"),
]:
    require_contains(path, token, label)

# Guard exact original assets used by the objects the user complained about.
for dst, src, label in [
    ("src/main/resources/assets/thaumcraft/textures/gui/thaumcraft_core_original/gui_arcaneworkbench.png", "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/gui/gui_arcaneworkbench.png", "Arcane Workbench GUI"),
    ("src/main/resources/assets/thaumcraft/textures/gui/thaumcraft_core_original/guiresearchtable2.png", "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/gui/guiresearchtable2.png", "Research Table GUI"),
    ("src/main/resources/assets/thaumcraft/textures/gui/thaumcraft_core_original/hud.png", "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/gui/hud.png", "Revealer HUD"),
    ("src/main/resources/assets/thaumcraft/textures/models/scanner.png", "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/models/scanner.png", "Thaumometer texture"),
    ("src/main/resources/assets/thaumcraft/textures/models/scanner.obj", "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/models/scanner.obj", "Thaumometer OBJ"),
    ("src/main/resources/assets/thaumcraft/textures/models/goggles.png", "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/models/goggles.png", "Goggles model texture"),
    ("src/main/resources/assets/thaumcraft/textures/models/wand.png", "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/models/wand.png", "Wand model texture"),
    ("src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/misc/nodes.png", "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/misc/nodes.png", "Aura node atlas"),
    ("src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/misc/node_bubble.png", "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/misc/node_bubble.png", "Aura node bubble"),
]:
    same(dst, src, label)

report = require_file("STAGE443_462_TC4_ORIGINAL_GUI_REVEALER_WAND_REPORT.json", "Stage443 report")
if report.exists():
    data = json.loads(report.read_text(encoding="utf-8"))
    if data.get("stage") != "443-462":
        errors.append("Stage443 report has wrong stage")
    if data.get("adds_new_mechanics") is not False:
        errors.append("Stage443 report must explicitly state no new mechanics")

workflow = read(".github/workflows/main.yml")
if "tc4_stage443_462_original_gui_revealer_wand_audit.py" not in workflow:
    errors.append("GitHub Actions workflow must run Stage443-462 audit")

if errors:
    print("Stage443-462 original GUI/revealer/wand audit failed:")
    for err in errors:
        print(" -", err)
    raise SystemExit(1)
print("Stage443-462 original GUI/revealer/wand audit: OK")
