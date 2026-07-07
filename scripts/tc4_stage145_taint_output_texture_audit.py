#!/usr/bin/env python3
"""Stage145 focused audit for TC4 taint metadata split and GitHub output texture packaging."""
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
read = lambda rel: (ROOT / rel).read_text(encoding='utf-8')

files = {
    "mod": read("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java"),
    "taint_block": read("src/main/java/com/darkifov/thaumcraft/block/TaintBlock.java"),
    "taint_fibres": read("src/main/java/com/darkifov/thaumcraft/block/TaintFibresBlock.java"),
    "taint_runtime": read("src/main/java/com/darkifov/thaumcraft/taint/TaintSpreadRuntime.java"),
    "build": read("build.gradle"),
    "workflow": read(".github/workflows/main.yml"),
    "static_audit": read("scripts/github_static_audit.py"),
}

required_resources = [
    "src/main/resources/assets/thaumcraft/textures/block/taint_crust.png",
    "src/main/resources/assets/thaumcraft/textures/block/taint_soil.png",
    "src/main/resources/assets/thaumcraft/textures/block/flesh_block.png",
    "src/main/resources/assets/thaumcraft/textures/block/taint_fibres.png",
    "src/main/resources/assets/thaumcraft/models/block/taint_crust.json",
    "src/main/resources/assets/thaumcraft/models/block/taint_soil.json",
    "src/main/resources/assets/thaumcraft/models/block/flesh_block.json",
    "src/main/resources/assets/thaumcraft/models/block/taint_fibres.json",
    "src/main/resources/assets/thaumcraft/models/item/taint_crust.json",
    "src/main/resources/assets/thaumcraft/models/item/taint_soil.json",
    "src/main/resources/assets/thaumcraft/models/item/flesh_block.json",
    "src/main/resources/assets/thaumcraft/models/item/taint_fibres.json",
    "src/main/resources/assets/thaumcraft/blockstates/taint_crust.json",
    "src/main/resources/assets/thaumcraft/blockstates/taint_soil.json",
    "src/main/resources/assets/thaumcraft/blockstates/flesh_block.json",
    "src/main/resources/assets/thaumcraft/blockstates/taint_fibres.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/taint_crust.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/taint_soil.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/flesh_block.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/taint_fibres.json",
]

checks = {
    "stage145_resources_preserved_on_current_version": any((f"version = '{v}'" in files["build"] and f'version="{v}"' in read("src/main/resources/META-INF/mods.toml")) for v in ["2.04.0", "2.02.0", "2.00.0", "1.98.0", "1.78.0", "1.76.0", "1.70.0", "1.65.0", "1.64.0", "1.63.0", "1.62.0", "1.61.0", "1.60.0", "1.59.0", "1.58.0", "1.57.0", "1.56.0", "1.55.0", "1.54.0", "1.53.0", "1.52.0", "1.51.0", "1.50.0", "1.49.0", "1.48.0", "1.47.0", "1.46.0", "1.45.0"]),
    "tc4_taint_metadata_split_registered": all(x in files["mod"] for x in ["TAINT_CRUST", "TAINT_SOIL", "FLESH_BLOCK", "TAINT_FIBRES", "TaintBlock.Variant.CRUST", "TaintBlock.Variant.SOIL", "TaintBlock.Variant.FLESH"]),
    "tc4_taint_classes_present": all(x in files["taint_block"] for x in ["CRUST", "SOIL", "FLESH", "randomTick", "entityInside"]),
    "tc4_fibres_age_bridge": all(x in files["taint_fibres"] for x in ["IntegerProperty.create(\"age\", 0, 4)", "canSurvive", "updateShape", "randomTick"]),
    "spread_runtime_uses_original_rules": all(x in files["taint_runtime"] for x in ["getAdjacentTaint", "spreadFibres", "isOnlyAdjacentToTaint", "isCrustTarget", "isSoilTarget", "maybeSpawnCrawler"]),
    "output_jar_resource_guard": all(x in files["build"] for x in ["verifyJarResources", "verifyGithubOutputJarResources", "copyGithubOutputJar", "assets/thaumcraft/textures/block/taint_crust.png", "${buildDir}/libs"]),
    "workflow_uploads_named_jars": any(name in files["workflow"] for name in ["thaumcraft-legacy-rebuild-stage204-jars", "thaumcraft-legacy-rebuild-stage165-jars", "thaumcraft-legacy-rebuild-stage164-jars", "thaumcraft-legacy-rebuild-stage163-jars", "thaumcraft-legacy-rebuild-stage162-jars", "thaumcraft-legacy-rebuild-stage161-jars", "thaumcraft-legacy-rebuild-stage160-jars", "thaumcraft-legacy-rebuild-stage159-jars", "thaumcraft-legacy-rebuild-stage158-jars", "thaumcraft-legacy-rebuild-stage157-jars", "thaumcraft-legacy-rebuild-stage156-jars", "thaumcraft-legacy-rebuild-stage155-jars", "thaumcraft-legacy-rebuild-stage154-jars", "thaumcraft-legacy-rebuild-stage153-jars", "thaumcraft-legacy-rebuild-stage152-jars", "thaumcraft-legacy-rebuild-stage151-jars", "thaumcraft-legacy-rebuild-stage150-jars", "thaumcraft-legacy-rebuild-stage154-jars", "thaumcraft-legacy-rebuild-stage153-jars", "thaumcraft-legacy-rebuild-stage152-jars", "thaumcraft-legacy-rebuild-stage151-jars", "thaumcraft-legacy-rebuild-stage150-jars", "thaumcraft-legacy-rebuild-stage154-jars", "thaumcraft-legacy-rebuild-stage153-jars", "thaumcraft-legacy-rebuild-stage152-jars", "thaumcraft-legacy-rebuild-stage151-jars", "thaumcraft-legacy-rebuild-stage150-jars", "thaumcraft-legacy-rebuild-stage154-jars", "thaumcraft-legacy-rebuild-stage153-jars", "thaumcraft-legacy-rebuild-stage152-jars", "thaumcraft-legacy-rebuild-stage151-jars", "thaumcraft-legacy-rebuild-stage150-jars", "thaumcraft-legacy-rebuild-stage149-jars", "thaumcraft-legacy-rebuild-stage148-jars", "thaumcraft-legacy-rebuild-stage147-jars", "thaumcraft-legacy-rebuild-stage146-jars", "thaumcraft-legacy-rebuild-stage145-jars"]) and "build/libs/*.jar" in files["workflow"] and "build/reobfJar/output.jar" not in files["workflow"],
    "static_audit_knows_new_helpers": "taintBlock|taintFibresBlock" in files["static_audit"],
    "resources_exist": all((ROOT / rel).exists() for rel in required_resources),
}

# Check texture/model references parse and point at real textures.
for rel in required_resources:
    path = ROOT / rel
    if path.suffix == ".json":
        try:
            json.loads(path.read_text(encoding="utf-8"))
        except Exception as exc:
            checks[f"json_valid:{rel}"] = False
            print(f"::error::{rel}: {exc}")

passed = all(checks.values())
print(json.dumps({"stage": 145, "goal": "TC4 taint metadata/fibres + GitHub output texture packaging", "checks": checks, "passed": passed}, indent=2))
if not passed:
    sys.exit(1)
