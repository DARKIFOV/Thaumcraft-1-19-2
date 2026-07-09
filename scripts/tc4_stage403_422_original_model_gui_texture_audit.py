#!/usr/bin/env python3
from pathlib import Path
import hashlib

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(path: str) -> str:
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing file: {path}")
        return ""
    return p.read_text(encoding="utf-8")

def require_file(path: str, label: str = "file") -> Path:
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing {label}: {path}")
    elif p.is_file() and p.stat().st_size <= 0:
        errors.append(f"empty {label}: {path}")
    return p

def require_contains(path: str, token: str, label: str) -> None:
    if token not in read(path):
        errors.append(f"{label}: missing token {token!r} in {path}")

def require_same(dst: str, src: str, label: str) -> None:
    dp = require_file(dst, label + " active")
    sp = require_file(src, label + " original")
    if dp.exists() and sp.exists():
        if hashlib.md5(dp.read_bytes()).hexdigest() != hashlib.md5(sp.read_bytes()).hexdigest():
            errors.append(f"{label}: active texture is not byte-identical to original TC4 asset: {dst} != {src}")

build = read("build.gradle")
mods = read("src/main/resources/META-INF/mods.toml")
if "version = '4.22.0'" not in build:
    errors.append("Stage403-422 must set build.gradle project version to 4.22.0")
if 'version="4.22.0"' not in mods:
    errors.append("Stage403-422 must set mods.toml version to 4.22.0")
if "version = '4.02.0'" not in build or 'version="4.02.0"' not in mods:
    errors.append("Stage403-422 must preserve Stage383-402 compatibility version markers")

# The Thaumometer must be backed by the original scanner OBJ numbers, not by the previous flat slab adapter.
thaumometer = "src/main/java/com/darkifov/thaumcraft/client/render/ThaumometerItemRenderer.java"
for token in [
    "Stage403-422 parity pass",
    "SCANNER_VERTICES",
    "SCANNER_UVS",
    "SCANNER_FACES",
    "original scanner.obj: 24 vertices, 58 texture coordinates and 24 quads",
    "textures/original/thaumcraft4/models/scanner.png",
    "renderOriginalScannerObj",
]:
    require_contains(thaumometer, token, "Thaumometer original OBJ renderer")
if "renderScannerQuad" in read(thaumometer) or "renderThickness" in read(thaumometer):
    errors.append("Thaumometer renderer still contains the previous flat scanner quad/thickness placeholder")

# Goggles must be a multi-plane head layer using original goggles.png, not a single flat sticker.
goggles = "src/main/java/com/darkifov/thaumcraft/client/render/TC4GogglesLayer.java"
for token in [
    "Stage403-422 player-layer adapter",
    "textures/original/thaumcraft4/models/goggles.png",
    "Left and right side straps wrap backward",
    "Back strap",
]:
    require_contains(goggles, token, "Goggles original layer")
if read(goggles).count("quad(matrix, consumer") < 5:
    errors.append("Goggles layer must render front/rim/side/back planes, not one flat quad")

# Aura node HUD must draw the actual original node sheet and bubble inside the revealer HUD.
hud = "src/main/java/com/darkifov/thaumcraft/client/TC4RevealerHudAdapter.java"
for token in [
    "ORIGINAL_NODES",
    "ORIGINAL_NODE_BUBBLE",
    "drawOriginalNodeSprite",
    "NODE_SHEET_FRAMES = 32",
    "ORIGINAL_NODE_BUBBLE",
]:
    require_contains(hud, token, "Aura node original HUD")

hud_source = read(hud)
if "GuiComponent.blit(poseStack, x, y, u, v, 32, 32, 2048, 2048)" not in hud_source and "GuiComponent.blit(poseStack, x, y, 32, 32, (float) u, (float) v, 64, 64, 2048, 2048)" not in hud_source:
    errors.append("Aura node original HUD lost nodes.png draw call: expected Stage403 crop call or Stage423 full-frame scaled call")

# In-world aura node renderer must not add fake aspect-icon orbitals in this visual parity pass.
aura = read("src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java")
if "renderAspectWisps(node" in aura:
    errors.append("AuraNodeRenderer still calls fake aspect-icon orbitals; Stage403-422 restricts visible node to original nodes.png/node_bubble.png layers")
for token in ["textures/original/thaumcraft4/misc/nodes.png", "textures/original/thaumcraft4/misc/node_bubble.png"]:
    if token not in aura:
        errors.append(f"AuraNodeRenderer lost original texture binding {token}")

# Key active GUI/assets must be byte-identical to original TC4 assets.
texture_pairs = {
    "src/main/resources/assets/thaumcraft/textures/gui/gui_arcaneworkbench.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/gui/gui_arcaneworkbench.png",
    "src/main/resources/assets/thaumcraft/textures/gui/guiresearchtable2.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/gui/guiresearchtable2.png",
    "src/main/resources/assets/thaumcraft/textures/gui/hud.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/gui/hud.png",
    "src/main/resources/assets/thaumcraft/textures/gui/gui_researchbook.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/gui/gui_researchbook.png",
    "src/main/resources/assets/thaumcraft/textures/item/goggles_of_revealing.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/gogglesrevealing.png",
    "src/main/resources/assets/thaumcraft/textures/item/thaumometer.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/scanner.png",
    "src/main/resources/assets/thaumcraft/textures/item/scribing_tools.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/inkwell.png",
    "src/main/resources/assets/thaumcraft/textures/item/research_note.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/researchnotes.png",
    "src/main/resources/assets/thaumcraft/textures/item/quicksilver_drop.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/quicksilver.png",
    "src/main/resources/assets/thaumcraft/textures/item/essentia_phial.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/phial.png",
    "src/main/resources/assets/thaumcraft/textures/item/boots_of_the_traveller.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/bootstraveler.png",
    "src/main/resources/assets/thaumcraft/textures/item/gold_wand_cap.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/wand_cap_gold.png",
    "src/main/resources/assets/thaumcraft/textures/item/iron_wand_cap.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/wand_cap_iron.png",
    "src/main/resources/assets/thaumcraft/textures/item/thaumium_wand_cap.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/wand_cap_thaumium.png",
    "src/main/resources/assets/thaumcraft/textures/item/wooden_wand_core.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/wand_rod_wood.png",
    "src/main/resources/assets/thaumcraft/textures/item/greatwood_wand_core.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/wand_rod_greatwood.png",
    "src/main/resources/assets/thaumcraft/textures/item/silverwood_wand_core.png": "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/items/wand_rod_silverwood.png",
}
for dst, src in texture_pairs.items():
    require_same(dst, src, dst.rsplit('/', 1)[-1])

workflow = read(".github/workflows/main.yml")
if "tc4_stage403_422_original_model_gui_texture_audit.py" not in workflow:
    errors.append("GitHub Actions workflow must run Stage403-422 audit")

if errors:
    print("Stage403-422 original model/gui/texture audit failed:")
    for err in errors:
        print(" -", err)
    raise SystemExit(1)
print("Stage403-422 original model/gui/texture audit: OK")
