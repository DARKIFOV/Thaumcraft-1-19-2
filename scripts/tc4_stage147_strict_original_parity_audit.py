#!/usr/bin/env python3
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8", errors="ignore")

def exists(rel: str) -> bool:
    return (ROOT / rel).exists()

build = read("build.gradle")
mods = read("src/main/resources/META-INF/mods.toml")
workflow = read(".github/workflows/main.yml")
icons = read("src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchIconMap.java")
research = read("src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java")
status = read("docs/ORIGINAL_TC4_PORTING_STATUS.md") if exists("docs/ORIGINAL_TC4_PORTING_STATUS.md") else ""
prompt = read("docs/NEXT_CHAT_PROMPT_STAGE147.md") if exists("docs/NEXT_CHAT_PROMPT_STAGE147.md") else ""
readme = read("README.md")
github_upload = read("GITHUB_UPLOAD.md")

research_keys = re.findall(r'new ResearchEntry\(\s*\n\s*"([^"]+)"', research)
icon_keys = re.findall(r'map\.put\("([^"]+)"', icons)

expected_icon_snippets = {
    "ROD_ice": "textures/original/thaumcraft4/items/wand_rod_ice.png",
    "ROD_quartz": "textures/original/thaumcraft4/items/wand_rod_quartz.png",
    "ROD_bone": "textures/original/thaumcraft4/items/wand_rod_bone.png",
    "ROD_silverwood": "textures/original/thaumcraft4/items/wand_rod_silverwood.png",
    "ROD_reed_staff": "textures/original/thaumcraft4/items/wand_rod_reed.png",
    "ROD_blaze_staff": "textures/original/thaumcraft4/items/wand_rod_blaze.png",
    "ROD_obsidian_staff": "textures/original/thaumcraft4/items/wand_rod_obsidian.png",
    "ROD_ice_staff": "textures/original/thaumcraft4/items/wand_rod_ice.png",
    "ROD_quartz_staff": "textures/original/thaumcraft4/items/wand_rod_quartz.png",
    "ROD_bone_staff": "textures/original/thaumcraft4/items/wand_rod_bone.png",
    "ROD_silverwood_staff": "textures/original/thaumcraft4/items/wand_rod_silverwood.png",
    "CAP_void": "textures/original/thaumcraft4/items/wand_cap_void.png",
}

checks: dict[str, bool] = {
    "version_stage147_or_later": any((f"version = '{v}'" in build and f'version="{v}"' in mods) for v in ["1.94.0", "1.78.0", "1.76.0", "1.70.0", "1.65.0", "1.64.0", "1.63.0", "1.62.0", "1.61.0", "1.60.0", "1.59.0", "1.58.0", "1.57.0", "1.56.0", "1.55.0", "1.54.0", "1.47.0", "1.48.0", "1.49.0", "1.50.0", "1.53.0", "1.52.0", "1.51.0", "1.50.0", "1.53.0", "1.52.0", "1.51.0"]),
    "workflow_stage147_or_later": "tc4_stage147_strict_original_parity_audit.py" in workflow and any(name in workflow for name in ["thaumcraft-legacy-rebuild-stage194-jars", "thaumcraft-legacy-rebuild-stage165-jars", "thaumcraft-legacy-rebuild-stage164-jars", "thaumcraft-legacy-rebuild-stage163-jars", "thaumcraft-legacy-rebuild-stage194-jars", "thaumcraft-legacy-rebuild-stage165-jars", "thaumcraft-legacy-rebuild-stage164-jars", "thaumcraft-legacy-rebuild-stage161-jars", "thaumcraft-legacy-rebuild-stage160-jars", "thaumcraft-legacy-rebuild-stage159-jars", "thaumcraft-legacy-rebuild-stage158-jars", "thaumcraft-legacy-rebuild-stage155-jars", "thaumcraft-legacy-rebuild-stage154-jars", "thaumcraft-legacy-rebuild-stage147-jars", "thaumcraft-legacy-rebuild-stage148-jars", "thaumcraft-legacy-rebuild-stage149-jars", "thaumcraft-legacy-rebuild-stage150-jars", "thaumcraft-legacy-rebuild-stage153-jars", "thaumcraft-legacy-rebuild-stage153-jars", "thaumcraft-legacy-rebuild-stage152-jars", "thaumcraft-legacy-rebuild-stage151-jars"]),
    "github_ci_guard_stage147_or_later": "tc4_stage147_strict_original_parity_audit.py" in read("scripts/github_ci_guard.py") and any(v in read("scripts/github_ci_guard.py") for v in ["1.94.0", "1.78.0", "1.76.0", "1.70.0", "1.65.0", "1.64.0", "1.63.0", "1.62.0", "1.61.0", "1.60.0", "1.59.0", "1.58.0", "1.57.0", "1.56.0", "1.55.0", "1.54.0", "1.47.0", "1.48.0", "1.49.0", "1.50.0", "1.53.0", "1.52.0", "1.51.0", "1.50.0", "1.53.0", "1.52.0", "1.51.0"]),
    "original_research_key_count": len(research_keys) == 201,
    "original_research_keys_unique": len(research_keys) == len(set(research_keys)),
    "research_icon_map_nontrivial": len(icon_keys) >= 115,
    "strict_docs_present": exists("docs/ORIGINAL_TC4_PORTING_STATUS.md") and exists("docs/NEXT_CHAT_PROMPT_STAGE147.md"),
    "remaining_stage_estimate_written": any(v in status for v in ["8–31", "8-31", "10–33", "10-33", "20–45", "20-45", "22-47", "45-70", "44-69", "43-68", "42-67", "41-66", "40-65", "39-64", "38-63", "36-61", "34-59", "32-57", "30-55", "28-53", "26–51", "26-51"]),
    "continuation_prompt_written": "Продолжи перенос оригинального Thaumcraft 4" in prompt and "Stage147" in prompt,
    "no_new_original_language": "новый оригинал" not in (readme + github_upload + status + prompt).lower() and "new original" not in (readme + github_upload + status + prompt).lower(),
    "jar_guards_original_research_gui": all(entry in build for entry in [
        "assets/thaumcraft/textures/gui/gui_research.png",
        "assets/thaumcraft/textures/gui/gui_researchback.png",
        "assets/thaumcraft/textures/gui/gui_researchbackeldritch.png",
        "assets/thaumcraft/textures/misc/r_thaumaturgy.png",
        "assets/thaumcraft/textures/misc/r_crucible.png",
        "assets/thaumcraft/textures/misc/r_artifice.png",
        "assets/thaumcraft/textures/misc/r_golemancy.png",
        "assets/thaumcraft/textures/misc/r_eldritch.png",
        "assets/thaumcraft/textures/items/thaumonomiconcheat.png",
    ]),
    "jar_guards_corrected_rod_cap_icons": all(entry in build for entry in [
        "assets/thaumcraft/textures/original/thaumcraft4/items/wand_rod_ice.png",
        "assets/thaumcraft/textures/original/thaumcraft4/items/wand_rod_quartz.png",
        "assets/thaumcraft/textures/original/thaumcraft4/items/wand_rod_bone.png",
        "assets/thaumcraft/textures/original/thaumcraft4/items/wand_rod_silverwood.png",
        "assets/thaumcraft/textures/original/thaumcraft4/items/wand_cap_void.png",
    ]),
}

for key, texture in expected_icon_snippets.items():
    pattern = re.compile(r'map\.put\("' + re.escape(key) + r'",\s*new Entry\(new ResourceLocation\("thaumcraft",\s*"' + re.escape(texture) + r'"\)')
    checks[f"icon_{key}_exact"] = bool(pattern.search(icons))

passed = all(checks.values())
report = {
    "stage": 147,
    "goal": "strict original TC4 parity correction + continuation handoff",
    "research_key_count": len(research_keys),
    "research_icon_map_count": len(icon_keys),
    "remaining_stage_estimate": "45-70 stages after Stage147 for full exact original TC4 port",
    "checks": checks,
    "passed": passed,
}
print(json.dumps(report, indent=2, ensure_ascii=False))
if not passed:
    for name, ok in checks.items():
        if not ok:
            print(f"::error::Stage147 strict-original parity audit failed: {name}")
    sys.exit(1)
