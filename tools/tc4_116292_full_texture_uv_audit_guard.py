#!/usr/bin/env python3
"""Regression guard retained in v11.63.10 full texture/UV audit and confirmed visual fixes."""
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
checks: list[tuple[str, bool]] = []

def text(rel: str) -> str:
    p = ROOT / rel
    return p.read_text(encoding="utf-8", errors="ignore") if p.is_file() else ""

def check(name: str, ok: bool) -> None:
    checks.append((name, bool(ok)))

build = text("build.gradle")
mods = text("src/main/resources/META-INF/mods.toml")
wisp = text("src/main/java/com/darkifov/thaumcraft/client/render/TC4WispRenderer.java")
furnace = text("src/main/java/com/darkifov/thaumcraft/client/screen/AlchemicalFurnaceScreen.java")
menu = text("src/main/java/com/darkifov/thaumcraft/menu/AlchemicalFurnaceMenu.java")
build_wf = text(".github/workflows/build.yml")
release_wf = text(".github/workflows/release.yml")

check("build version", "version = '11.63.23'" in build)
check("mods version", 'version="11.63.23"' in mods)
check("wisp original texture", '"textures/misc/wisp.png"' in wisp)
check("wisp original particles texture", '"textures/misc/particles.png"' in wisp and 'new ResourceLocation(ThaumcraftMod.MOD_ID' in wisp)
check("wisp 4x4 frame u", "(frame % 4) / 4.0F" in wisp and "coreU0 + 0.25F" in wisp)
check("wisp 4x4 frame v", "(frame / 4) / 4.0F" in wisp and "coreV0 + 0.25F" in wisp)
check("wisp halo row", "5.0F / 16.0F" in wisp and "6.0F / 16.0F" in wisp)
check("wisp original halo pulse", "0.4F + Mth.sin((entity.tickCount + partialTicks) / 10.0F) * 0.1F" in wisp)
check("wisp additive fullbright", "TC4NodeRenderTypes.node(texture, true, false)" in wisp and ".uv2(0xF000F0)" in wisp)
check("wisp original core scale", "WISP, 1.0F" in wisp)
check("wisp hurt tint", "entity.hurtTime > 0" in wisp and "green * 255 / 300" in wisp)
check("wisp no whole atlas calls", "0.0F, 0.0F, 1.0F, 1.0F" not in wisp)

check("furnace original GUI", 'new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/gui/gui_alchemyfurnace.png")' in furnace)
check("furnace no vanilla GUI", 'minecraft", "textures/gui/container/furnace.png' not in furnace)
check("furnace burn gauge", "leftPos + 80, topPos + 26 + 20 - burn" in furnace and "176, 20 - burn, 16, burn" in furnace)
check("furnace cook gauge", "leftPos + 106, topPos + 13 + 46 - cook" in furnace and "216, 46 - cook, 9, cook" in furnace)
check("furnace contents gauge", "leftPos + 61, topPos + 12 + 48 - contents" in furnace and "200, 48 - contents, 8, contents" in furnace)
check("furnace glass overlay", "leftPos + 60, topPos + 8, 232, 0, 10, 55" in furnace)
check("furnace original slots", "SLOT_INPUT, 80, 8" in menu and "SLOT_FUEL, 80, 48" in menu)
check("furnace essentia scale", "public int essentiaProgress(int pixels)" in menu)

baseline_path = ROOT / "audit_reports/baseline_11.62.91/audit_manifest.json"
post_path = ROOT / "audit_reports/postfix_11.62.92/audit_manifest.json"
try:
    baseline = json.loads(baseline_path.read_text(encoding="utf-8"))
    post = json.loads(post_path.read_text(encoding="utf-8"))
except Exception:
    baseline = {}
    post = {}
check("baseline exhaustive counts", baseline.get("counts", {}).get("original_png_mcmeta") == 921
      and baseline.get("counts", {}).get("json_models") == 988
      and baseline.get("counts", {}).get("registered_blocks") == 202)
check("baseline two confirmed mismatches", len(baseline.get("confirmed_mismatches", [])) == 2)
check("postfix exhaustive counts", post.get("counts", {}).get("original_exact_shipped") == 921
      and post.get("counts", {}).get("custom_renderer_files") == 70
      and post.get("counts", {}).get("gui_screens") == 25)
check("postfix confirmed source mismatches closed", post.get("confirmed_mismatches") == [])
check("audit CSVs present", all((ROOT / "audit_reports/postfix_11.62.92" / name).is_file() for name in (
    "texture_audit.csv", "model_audit.csv", "custom_renderer_audit.csv", "gui_audit.csv",
    "render_type_audit.csv", "item_context_audit.csv", "summary.md")))
check("audit tool present", (ROOT / "tools/full_texture_uv_audit.py").is_file())
check("build workflow", "tc4_116292_full_texture_uv_audit_guard.py" in build_wf)
check("release workflow", "tc4_116292_full_texture_uv_audit_guard.py" in release_wf)

failed = [name for name, ok in checks if not ok]
print(f"TC4 11.63.10 full texture/UV audit guard: {len(checks)-len(failed)}/{len(checks)} PASS")
if failed:
    for name in failed:
        print("FAIL:", name)
    sys.exit(1)
print("STATIC SOURCE/RESOURCE CONTRACT PASS; runtime visual parity remains NOT TESTED")
