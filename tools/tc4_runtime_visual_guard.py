#!/usr/bin/env python3
from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
read = lambda rel: (root / rel).read_text(encoding="utf-8")
errors = []

thaumometer = read("src/main/java/com/darkifov/thaumcraft/client/render/ThaumometerItemRenderer.java")
for token in ["Math.min(15, aspects.size())", "int posY = 0", "remaining -= Math.max(0, 5 - posY)", "ASPECT_RELATIVE_SCALE = 0.0075F / SCANNER_READOUT_SCALE", "float size = 16.0F", "Comparator.comparing(stack -> stack.aspect().id())"]:
    if token not in thaumometer:
        errors.append(f"Thaumometer original readout token missing: {token}")
for forbidden in ["drawNodeOnScanner(", "ORIGINAL_NODE_BUBBLE", "comparingInt(AspectStack::amount).reversed()", "Math.min(10, aspects.size())"]:
    if forbidden in thaumometer:
        errors.append(f"Thaumometer still contains non-original scanner overlay/order: {forbidden}")

node = read("src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java")
if "float sizeMultiplier = jarred ? 0.70F : 1.0F;" not in node:
    errors.append("Aura node jar/world size ledger drifted")
if "node.isEnergized() ? 1.10F" in node:
    errors.append("Aura node still has invented energized size multiplier")
if "renderSheetPlane(poseStack, buffer, nodeLight, Math.max(0.20F, typeScale * 0.82F)" in node:
    errors.append("Aura node still draws invented drain-color sprite overlay")
if "ORIGINAL_WISPY" not in node:
    errors.append("Aura node drain beam no longer uses original wispy texture")

wand = read("src/main/java/com/darkifov/thaumcraft/client/render/WandItemRenderer.java")
for token in ["renderOriginalTC4SceptreRunes", "36.0F * rot + ticks", "RenderType.eyes(ORIGINAL_SCRIPT)", "Mth.sin((ticks + rune * 5.0F) / 5.0F)"]:
    if token not in wand:
        errors.append(f"Wand original rune behavior missing: {token}")

goggles_path = root / "src/main/java/com/darkifov/thaumcraft/client/render/TC4GogglesLayer.java"
goggles = goggles_path.read_text(encoding="utf-8")
for token in ["TEX_W = 64.0F", "TEX_H = 32.0F", "quadPx", "getParentModel().head.translateAndRotate"]:
    if token not in goggles:
        errors.append(f"Goggles armor-atlas adapter missing: {token}")
if "Front lens/bridge plane" in goggles or "0.000F, 0.625F, 1.000F" in goggles:
    errors.append("Goggles still use stretched arbitrary atlas planes")

page = read("src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java")
browser = read("src/main/java/com/darkifov/thaumcraft/client/screen/ThaumonomiconScreen.java")
if "textures/original/thaumcraft4/gui/gui_researchbook.png" not in page:
    errors.append("Thaumonomicon page does not bind canonical original research book")
if "textures/original/thaumcraft4/gui/gui_research.png" not in browser:
    errors.append("Thaumonomicon browser does not bind canonical original research GUI")
if page.count("import net.minecraft.client.resources.language.I18n;") != 1:
    errors.append("TC4ResearchPageScreen has duplicate/missing I18n import")

research = read("src/main/java/com/darkifov/thaumcraft/research/TC4ResearchTableParity.java")
for token in ["SLOT_SCRIBING_TOOLS_X = 14", "SLOT_SCRIBING_TOOLS_Y = 10", "SLOT_RESEARCH_NOTE_X = 70", "SLOT_RESEARCH_NOTE_Y = 10", "PLAYER_INVENTORY_X = 48", "PLAYER_INVENTORY_Y = 175", "PLAYER_HOTBAR_Y = 233"]:
    if token not in research:
        errors.append(f"Research table original slot coordinate drift: {token}")
arcane = read("src/main/java/com/darkifov/thaumcraft/arcane/TC4ArcaneWorkbenchParity.java")
for token in ["GUI_WIDTH = 190", "GUI_HEIGHT = 234", "GRID_X = 40", "GRID_Y = 40", "WAND_SLOT_X = 160", "WAND_SLOT_Y = 24", "OUTPUT_SLOT_X = 160", "OUTPUT_SLOT_Y = 64"]:
    if token not in arcane:
        errors.append(f"Arcane workbench original coordinate drift: {token}")

if errors:
    print("TC4 runtime visual parity guard: FAIL")
    for error in errors:
        print(" -", error)
    sys.exit(1)
print("TC4 runtime visual parity guard: OK")
