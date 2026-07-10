#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(path: str) -> str:
    p = ROOT / path
    if not p.exists():
        raise SystemExit(f"AUDIT FAIL: missing {path}")
    return p.read_text(encoding="utf-8", errors="replace")

screen = read("src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java")
text = read("src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchText.java")
build = read("build.gradle")
workflow = read(".github/workflows/main.yml")

required = [
    (build, "version = '11.62.4'", "version not bumped to 11.62.4"),
    (build, "thaumonomicon_page_layout_reset_complete", "missing v11.62.4 build marker"),
    (text, "rawPageText", "TC4ResearchText must expose raw page text for IMG renderer"),
    (screen, "renderOriginalBookMarkup", "missing ordered TC4 markup renderer"),
    (screen, "renderOriginalBookImageTag", "missing IMG tag renderer"),
    (screen, "OriginalImageSpec", "missing original IMG spec parser"),
    (screen, "remapLegacyTexturePath", "missing old textures/items path remapper"),
    (screen, "quicksilver_drop", "missing quicksilver legacy texture remap"),
    (screen, "v11.62.4 Thaumonomicon reset", "missing v11.62.4 implementation marker"),
    (workflow, "tc4_v11_62_4_thaumonomicon_markup_audit.py", "workflow missing v11.62.4 audit"),
]
for haystack, needle, message in required:
    if needle not in haystack:
        raise SystemExit(f"AUDIT FAIL: {message}")

for forbidden in [
    'text = text.replaceAll("(?i)<IMG>.*?</IMG>", "\\n[image]\\n")',
    'drawString(poseStack, font, "[image]"',
]:
    if forbidden in text or forbidden in screen:
        raise SystemExit(f"AUDIT FAIL: forbidden broken Thaumonomicon image placeholder remains: {forbidden}")

print("TC4 v11.62.4 Thaumonomicon markup/layout audit: OK")
