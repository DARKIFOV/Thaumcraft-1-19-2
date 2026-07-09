#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(path: str) -> str:
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing file: {path}")
        return ""
    return p.read_text(encoding="utf-8", errors="ignore")

def require_file(path: str):
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing file: {path}")
    elif p.is_file() and p.stat().st_size <= 0:
        errors.append(f"empty file: {path}")
    return p

def require_contains(path: str, token: str, label: str):
    if token not in read(path):
        errors.append(f"{label}: missing token {token!r} in {path}")

build = read("build.gradle")
mods = read("src/main/resources/META-INF/mods.toml")
if "version = '4.82.0'" not in build:
    errors.append("Stage463-482 must set build.gradle version to 4.82.0")
if "version = '4.62.0'" not in build:
    errors.append("Stage463-482 lost Stage443-462 compatibility marker in build.gradle")
if 'version="4.82.0"' not in mods:
    errors.append("Stage463-482 must set mods.toml version to 4.82.0")
if 'version="4.62.0"' not in mods:
    errors.append("Stage463-482 lost Stage443-462 compatibility marker in mods.toml")

component = "src/main/java/com/darkifov/thaumcraft/item/TC4ResearchComponentItem.java"
for token in [
    "isCreativeThaumonomiconCheat()",
    '"thaumonomiconcheat".equals(legacyTexture)',
    "grantAllResearch(Player player)",
    "ResearchRegistry.entries()",
    "PlayerThaumData.unlockResearch(player, entry.key())",
    "OriginalResearchProgression.applyUnlockSideEffects(player, entry)",
    "ThaumcraftNetwork.syncResearch(serverPlayer)",
    "ThaumcraftNetwork.syncAspectKnowledge(serverPlayer)",
    "No recipes are added; original progression remains untouched unless you use it.",
]:
    require_contains(component, token, "Creative Thaumonomicon behavior")

research_items = read("src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java")
if 'e("tc4_thaumonomiconcheat", "thaumonomiconcheat"' not in research_items:
    errors.append("TC4ResearchItems must keep the original tc4_thaumonomiconcheat mirror entry")

mod = read("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java")
if 'CREATIVE_RESEARCH_BOOK' in mod or 'creative_research_book' in mod:
    errors.append("Do not add a duplicate creative_research_book registry id; use existing tc4_thaumonomiconcheat mirror")

require_file("src/main/resources/assets/thaumcraft/models/item/tc4_thaumonomiconcheat.json")
require_file("src/main/resources/assets/thaumcraft/textures/item/tc4/thaumonomiconcheat.png")
require_contains("src/main/resources/assets/thaumcraft/lang/en_us.json", '"item.thaumcraft.tc4_thaumonomiconcheat": "Creative Thaumonomicon"', "English lang")
require_contains("src/main/resources/assets/thaumcraft/lang/ru_ru.json", '"item.thaumcraft.tc4_thaumonomiconcheat": "Творческий таумономикон"', "Russian lang")

report = require_file("STAGE463_482_TC4_PARITY_AUDIT_CREATIVE_RESEARCH_REPORT.json")
if report.exists():
    data = json.loads(report.read_text(encoding="utf-8"))
    if data.get("stage") != "463-482":
        errors.append("Stage463-482 report has wrong stage")
    book = data.get("user_requested_full_research_book", {})
    if book.get("registry_id") != "thaumcraft:tc4_thaumonomiconcheat":
        errors.append("Stage463-482 report must identify thaumcraft:tc4_thaumonomiconcheat")
    if book.get("recipe_added") is not False:
        errors.append("Creative research book must not add a recipe")
    if book.get("new_duplicate_book_registered") is not False:
        errors.append("Creative research book must not be duplicated")
    if "estimated_strict_tc4_parity_percent" not in data.get("parity_assessment", {}):
        errors.append("Stage463-482 report must include honest parity estimate")

require_file("docs/NEXT_CHAT_PROMPT_STAGE482.md")
workflow = read(".github/workflows/main.yml")
if "tc4_stage463_482_parity_audit_creative_research_audit.py" not in workflow:
    errors.append("GitHub Actions workflow must run Stage463-482 audit")

if errors:
    print("Stage463-482 parity audit/creative research audit failed:")
    for error in errors:
        print(" -", error)
    raise SystemExit(1)
print("Stage463-482 parity audit/creative research audit: OK")
