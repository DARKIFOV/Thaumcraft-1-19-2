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
    (build, "version = '11.62.5'", "project version not bumped to 11.62.5"),
    (build, "thaumonomicon_recipe_page_cards_complete", "missing v11.62.5 build marker"),
    (mods, 'version="11.62.5"', "mods.toml version not bumped to 11.62.5"),
    (screen, "PAGE_WIDTH", "missing hard page width clamp"),
    (screen, "PAGE_HEIGHT", "missing hard page height clamp"),
    (screen, "renderOriginalBookSeparator", "missing TC4 <LINE> separator renderer"),
    (screen, "<LINE", "markup parser must recognize TC4 <LINE> tags"),
    (screen, "renderCraftingBookPage", "missing crafting recipe book card"),
    (screen, "renderCrucibleBookPage", "missing crucible recipe book card"),
    (screen, "renderInfusionBookPage", "missing infusion recipe book card"),
    (screen, "renderInfusionComponentsRing", "missing infusion component ring layout"),
    (screen, "textures/item/tc4/", "legacy item IMG paths must map to TC4 sprites"),
    (workflow, "tc4_v11_62_5_thaumonomicon_recipe_cards_audit.py", "workflow missing v11.62.5 audit"),
]
for haystack, needle, message in required:
    if needle not in haystack:
        raise SystemExit(f"AUDIT FAIL: {message}")

for forbidden in [
    'drawRecipeField(poseStack',
    'compactExpressionWithTc4Item(recipe',
    'pageTypeLabel(type)',
    'drawString(poseStack, font, "[image]"',
]:
    if forbidden in screen:
        raise SystemExit(f"AUDIT FAIL: visible debug/placeholder renderer remains: {forbidden}")

print("TC4 v11.62.5 Thaumonomicon recipe cards/layout audit: OK")
