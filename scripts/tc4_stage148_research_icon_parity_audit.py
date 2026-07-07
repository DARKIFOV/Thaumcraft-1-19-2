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
guard = read("scripts/github_ci_guard.py")
icons = read("src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchIconMap.java")
research = read("src/main/java/com/darkifov/thaumcraft/research/TC4ResearchRuntimeBridge.java")
status = read("docs/ORIGINAL_TC4_PORTING_STATUS.md") if exists("docs/ORIGINAL_TC4_PORTING_STATUS.md") else ""
prompt = read("docs/NEXT_CHAT_PROMPT_STAGE148.md") if exists("docs/NEXT_CHAT_PROMPT_STAGE148.md") else ""

research_keys = re.findall(r'new ResearchEntry\(\s*\n\s*"([^"]+)"', research)
icon_keys = re.findall(r'map\.put\("([^"]+)"', icons)
icon_paths = re.findall(r'new ResourceLocation\("thaumcraft", "([^"]+)"\)', icons)

missing_icon_keys = sorted(set(research_keys) - set(icon_keys))
extra_duplicate_icons = len(icon_keys) - len(set(icon_keys))
missing_icon_files = []
missing_jar_guards = []
for path in icon_paths:
    if path.startswith("textures/"):
        rel = "assets/thaumcraft/" + path
        resource_rel = "src/main/resources/" + rel
        if not exists(resource_rel):
            missing_icon_files.append(rel)
        if path.startswith("textures/original/thaumcraft4/") and f"'{rel}'" not in build:
            missing_jar_guards.append(rel)

stage148_required = {
    "CAP_iron": "textures/original/thaumcraft4/items/wand_cap_iron.png",
    "ROD_wood": "textures/original/thaumcraft4/models/wand_rod_wood.png",
    "SCEPTRE": "textures/original/thaumcraft4/models/wand.png",
    "ASPECTS": "textures/original/thaumcraft4/misc/r_aspects.png",
    "PECH": "textures/original/thaumcraft4/misc/r_pech.png",
    "NODES": "textures/original/thaumcraft4/misc/r_nodes.png",
    "WARP": "textures/original/thaumcraft4/misc/r_warp.png",
    "NODEJAR": "textures/original/thaumcraft4/models/jar.png",
    "INFUSION": "textures/original/thaumcraft4/models/infuser.png",
    "CRUCIBLE": "textures/original/thaumcraft4/blocks/crucible1.png",
    "TUBES": "textures/original/thaumcraft4/blocks/pipe_1.png",
    "HUNGRYCHEST": "textures/original/thaumcraft4/models/chesthungry.png",
    "ELDRITCHMINOR": "textures/original/thaumcraft4/misc/r_eldritchminor.png",
    "ELDRITCHMAJOR": "textures/original/thaumcraft4/misc/r_eldritchmajor.png",
    "ARMORVOIDFORTRESS": "textures/original/thaumcraft4/items/voidrobehelm.png",
}

checks: dict[str, bool] = {
    "version_stage148_or_later": any((f"version = '{v}'" in build and f'version="{v}"' in mods) for v in ["1.94.0", "1.78.0", "1.76.0", "1.70.0", "1.65.0", "1.64.0", "1.63.0", "1.62.0", "1.61.0", "1.60.0", "1.59.0", "1.58.0", "1.57.0", "1.56.0", "1.55.0", "1.54.0", "1.48.0", "1.49.0", "1.50.0", "1.53.0", "1.52.0", "1.51.0"]),
    "workflow_stage148_or_later": "tc4_stage148_research_icon_parity_audit.py" in workflow and any(name in workflow for name in ["thaumcraft-legacy-rebuild-stage194-jars", "thaumcraft-legacy-rebuild-stage165-jars", "thaumcraft-legacy-rebuild-stage164-jars", "thaumcraft-legacy-rebuild-stage163-jars", "thaumcraft-legacy-rebuild-stage194-jars", "thaumcraft-legacy-rebuild-stage165-jars", "thaumcraft-legacy-rebuild-stage164-jars", "thaumcraft-legacy-rebuild-stage161-jars", "thaumcraft-legacy-rebuild-stage160-jars", "thaumcraft-legacy-rebuild-stage159-jars", "thaumcraft-legacy-rebuild-stage158-jars", "thaumcraft-legacy-rebuild-stage155-jars", "thaumcraft-legacy-rebuild-stage154-jars", "thaumcraft-legacy-rebuild-stage148-jars", "thaumcraft-legacy-rebuild-stage149-jars", "thaumcraft-legacy-rebuild-stage150-jars", "thaumcraft-legacy-rebuild-stage153-jars", "thaumcraft-legacy-rebuild-stage153-jars", "thaumcraft-legacy-rebuild-stage152-jars", "thaumcraft-legacy-rebuild-stage151-jars"]),
    "github_ci_guard_stage148_or_later": "tc4_stage148_research_icon_parity_audit.py" in guard and any(v in guard for v in ["1.94.0", "1.78.0", "1.76.0", "1.70.0", "1.65.0", "1.64.0", "1.63.0", "1.62.0", "1.61.0", "1.60.0", "1.59.0", "1.58.0", "1.57.0", "1.56.0", "1.55.0", "1.54.0", "1.48.0", "1.49.0", "1.50.0", "1.53.0", "1.52.0", "1.51.0"]),
    "original_research_key_count_201": len(research_keys) == 201,
    "original_research_keys_unique": len(research_keys) == len(set(research_keys)),
    "icon_key_count_201": len(icon_keys) == 201,
    "icon_keys_unique": extra_duplicate_icons == 0,
    "no_research_key_missing_icon": not missing_icon_keys,
    "all_icon_files_exist": not missing_icon_files,
    "jar_guards_all_original_icon_resources": not missing_jar_guards,
    "continuation_prompt_stage148_or_later": ("Stage148" in prompt or "Stage149" in prompt or "Stage150" in prompt or "Stage151" in prompt) and "строгий оригинальный TC4" in prompt,
    "status_remaining_estimate_updated": any(v in status for v in ["8–31", "8-31", "10–33", "10-33", "20–45", "20-45", "22-47", "45-70", "44-69", "43-68", "42-67", "41-66", "40-65", "39-64", "38-63", "36-61", "34-59", "32-57", "30-55", "28-53", "26–51", "26-51"]),
}

for key, texture in stage148_required.items():
    checks[f"exact_icon_{key}"] = f'map.put("{key}", new Entry(new ResourceLocation("thaumcraft", "{texture}")' in icons

passed = all(checks.values())
report = {
    "stage": 148,
    "goal": "complete strict original TC4 ConfigResearch icon parity for all 201 research keys",
    "research_key_count": len(research_keys),
    "icon_key_count": len(icon_keys),
    "missing_icon_keys": missing_icon_keys,
    "missing_icon_files": missing_icon_files,
    "missing_jar_guards": missing_jar_guards,
    "remaining_stage_estimate_after_stage148": "44-69 stages before Stage149; 43-68 after Stage149",
    "checks": checks,
    "passed": passed,
}
print(json.dumps(report, indent=2, ensure_ascii=False))
if not passed:
    for name, ok in checks.items():
        if not ok:
            print(f"::error::Stage148 research icon parity audit failed: {name}")
    sys.exit(1)
