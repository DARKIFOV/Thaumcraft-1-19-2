#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(path: str) -> str:
    p = ROOT / path
    if not p.exists():
        raise SystemExit(f"AUDIT FAIL: missing {path}")
    return p.read_text(encoding="utf-8", errors="replace")

screen = read("src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java")
build = read("build.gradle")
mods = read("src/main/resources/META-INF/mods.toml")
workflow = read(".github/workflows/main.yml")

required = [
    (build, "version = '11.62.6'", "project version not bumped to 11.62.6"),
    (build, "thaumonomicon_page_tooltips_and_scissor_complete", "missing v11.62.6 build marker"),
    (mods, 'version="11.62.6"', "mods.toml version not bumped to 11.62.6"),
    (mods, "thaumonomicon_page_tooltips_and_scissor_complete", "missing v11.62.6 mods marker"),
    (screen, "withPageScissor", "missing physical page scissor wrapper"),
    (screen, "RenderSystem.enableScissor", "missing GL scissor enable"),
    (screen, "RenderSystem.disableScissor", "missing GL scissor disable"),
    (screen, "BookHoverRegion", "missing book hover hitbox registry"),
    (screen, "bookHoverRegions.clear", "hover hitboxes must reset every frame"),
    (screen, "registerItemHover", "missing item hover registration"),
    (screen, "registerAspectHover", "missing aspect hover registration"),
    (screen, "renderBookHoverTooltip", "missing book hover tooltip renderer"),
    (screen, "renderTooltip(poseStack, region.stack()", "missing vanilla ItemStack tooltip call"),
    (screen, "renderComponentTooltip(poseStack, region.lines()", "missing aspect text tooltip call"),
    (workflow, "tc4_v11_62_6_thaumonomicon_tooltip_scissor_audit.py", "workflow missing v11.62.6 audit"),
]

for haystack, needle, message in required:
    if needle not in haystack:
        raise SystemExit(f"AUDIT FAIL: {message}")

for forbidden in [
    'drawString(poseStack, font, "[image]"',
    'path: |\n            build/libs/*.jar',
]:
    if forbidden in screen + workflow:
        raise SystemExit(f"AUDIT FAIL: forbidden old placeholder/upload remains: {forbidden}")

print("TC4 v11.62.6 Thaumonomicon tooltip/scissor audit: OK")
