#!/usr/bin/env python3
from pathlib import Path
import hashlib
import json

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

def same(dst: str, src: str, label: str) -> None:
    dp = require_file(dst, label + " active")
    sp = require_file(src, label + " original")
    if dp.exists() and sp.exists():
        if hashlib.md5(dp.read_bytes()).hexdigest() != hashlib.md5(sp.read_bytes()).hexdigest():
            errors.append(f"{label}: active asset is not byte-identical to original TC4 asset: {dst} != {src}")

build = read("build.gradle")
mods = read("src/main/resources/META-INF/mods.toml")
if "version = '4.42.0'" not in build:
    errors.append("Stage423-442 must set build.gradle project version to 4.42.0")
if 'version="4.42.0"' not in mods:
    errors.append("Stage423-442 must set mods.toml version to 4.42.0")
for marker in ["version = '4.22.0'", "version = '4.02.0'", "version = '3.82.0'"]:
    if marker not in build:
        errors.append(f"build.gradle lost compatibility marker {marker}")
for marker in ['version="4.22.0"', 'version="4.02.0"', 'version="3.82.0"']:
    if marker not in mods:
        errors.append(f"mods.toml lost compatibility marker {marker}")

hud = "src/main/java/com/darkifov/thaumcraft/client/TC4RevealerHudAdapter.java"
for token in [
    "Stage423-442 fixes the node HUD atlas sampling",
    "complete 256x256 node_bubble.png",
    "GuiComponent.blit(poseStack, x - 5, y - 5, 42, 42, 0.0F, 0.0F, 256, 256, 256, 256)",
    "GuiComponent.blit(poseStack, x, y, 32, 32, (float) u, (float) v, 64, 64, 2048, 2048)",
]:
    require_contains(hud, token, "Stage423 HUD full-atlas sampling")
old_crops = [
    "GuiComponent.blit(poseStack, x - 5, y - 5, 0, 0, 42, 42, 256, 256)",
    "GuiComponent.blit(poseStack, x, y, u, v, 32, 32, 2048, 2048)",
]
for token in old_crops:
    if token in read(hud):
        errors.append(f"Stage423 HUD still crops original texture instead of scaling full source: {token}")

aura = "src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java"
for token in [
    "Stage423-442 removes the leftover crossing-plane depth adapter",
    "do not render crossing billboard adapters either",
    "stays restricted to original nodes.png + node_bubble.png camera-facing layers",
]:
    require_contains(aura, token, "Stage423 in-world node renderer")
for forbidden in ["poseStack.mulPose(Vector3f.YP.rotationDegrees(rotation));", "poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));"]:
    if forbidden in read(aura):
        errors.append(f"AuraNodeRenderer still has non-original crossing-plane adapter transform: {forbidden}")

# Every original core TC4 GUI texture must be mirrored by exact filename into active textures/gui.
for p in (ROOT / "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/gui").iterdir():
    if p.is_file() and p.suffix == ".png":
        same(f"src/main/resources/assets/thaumcraft/textures/gui/{p.name}", str(p.relative_to(ROOT)), f"core GUI {p.name}")

# Model/runtime assets used by Thaumometer, Goggles and Wands must be active and byte-identical.
for name in ["scanner.png", "scanner.obj", "goggles.png", "wand.png", "wand_cap_iron.png", "wand_cap_gold.png", "wand_cap_thaumium.png", "wand_cap_copper.png", "wand_cap_silver.png", "wand_cap_void.png", "wand_rod_wood.png", "wand_rod_greatwood.png", "wand_rod_silverwood.png", "wand_rod_obsidian.png", "wand_rod_blaze.png", "wand_rod_ice.png", "wand_rod_quartz.png", "wand_rod_bone.png", "wand_rod_reed.png", "wand_rod_primal.png"]:
    same(f"src/main/resources/assets/thaumcraft/textures/models/{name}", f"src/main/resources/assets/thaumcraft/original_tc4_1710/textures/models/{name}", f"active model {name}")

for dst, src in {
    "src/main/resources/assets/thaumcraft/textures/item/iron_capped_wooden_wand.png": "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/models/wand.png",
    "src/main/resources/assets/thaumcraft/textures/item/greatwood_wand.png": "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/models/wand.png",
    "src/main/resources/assets/thaumcraft/textures/item/silverwood_wand.png": "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/models/wand.png",
    "src/main/resources/assets/thaumcraft/textures/item/thaumometer.png": "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/models/scanner.png",
    "src/main/resources/assets/thaumcraft/textures/item/goggles_of_revealing.png": "src/main/resources/assets/thaumcraft/original_tc4_1710/textures/items/gogglesrevealing.png",
}.items():
    same(dst, src, Path(dst).name)

for name in ["wand.png", "wand_rod_wood.png", "wand_rod_greatwood.png", "wand_rod_silverwood.png", "wand_rod_obsidian.png", "wand_rod_blaze.png", "wand_rod_ice.png", "wand_rod_quartz.png", "wand_rod_bone.png", "wand_rod_reed.png", "wand_rod_primal.png", "wand_cap_iron.png", "wand_cap_gold.png", "wand_cap_thaumium.png", "wand_cap_copper.png", "wand_cap_silver.png", "wand_cap_void.png"]:
    same(f"src/main/resources/assets/thaumcraft/textures/entity/wand/{name}", f"src/main/resources/assets/thaumcraft/original_tc4_1710/textures/models/{name}", f"entity wand {name}")

manifest = require_file("docs/STAGE423_442_ORIGINAL_TEXTURE_MIRROR_MANIFEST.json", "texture mirror manifest")
if manifest.exists():
    data = json.loads(manifest.read_text(encoding="utf-8"))
    if len(data) < 320:
        errors.append(f"Stage423-442 texture mirror manifest is too small: expected >=320 entries, got {len(data)}")

workflow = read(".github/workflows/main.yml")
if "tc4_stage423_442_original_visual_atlas_audit.py" not in workflow:
    errors.append("GitHub Actions workflow must run Stage423-442 audit")

if errors:
    print("Stage423-442 original visual/atlas audit failed:")
    for err in errors:
        print(" -", err)
    raise SystemExit(1)
print("Stage423-442 original visual/atlas audit: OK")
