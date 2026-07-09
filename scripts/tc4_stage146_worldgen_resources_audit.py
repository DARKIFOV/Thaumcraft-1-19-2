#!/usr/bin/env python3
"""Stage146 focused audit for TC4 worldgen bridge and Greatwood/Silverwood output resources."""
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
read = lambda rel: (ROOT / rel).read_text(encoding="utf-8")

files = {
    "build": read("build.gradle"),
    "mods": read("src/main/resources/META-INF/mods.toml"),
    "mod": read("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java"),
    "worldgen": read("src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java"),
    "trees": read("src/main/java/com/darkifov/thaumcraft/world/TC4TreeGenerator.java"),
    "sapling": read("src/main/java/com/darkifov/thaumcraft/block/TC4SaplingBlock.java"),
    "events": read("src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java"),
    "workflow": read(".github/workflows/main.yml"),
    "static_audit": read("scripts/github_static_audit.py"),
}

required_resources = [
    "src/main/resources/assets/thaumcraft/textures/block/greatwood_log.png",
    "src/main/resources/assets/thaumcraft/textures/block/greatwood_log_top.png",
    "src/main/resources/assets/thaumcraft/textures/block/greatwood_leaves.png",
    "src/main/resources/assets/thaumcraft/textures/block/greatwood_sapling.png",
    "src/main/resources/assets/thaumcraft/textures/block/silverwood_log.png",
    "src/main/resources/assets/thaumcraft/textures/block/silverwood_log_top.png",
    "src/main/resources/assets/thaumcraft/textures/block/silverwood_leaves.png",
    "src/main/resources/assets/thaumcraft/textures/block/silverwood_sapling.png",
    "src/main/resources/assets/thaumcraft/models/block/greatwood_log.json",
    "src/main/resources/assets/thaumcraft/models/block/greatwood_leaves.json",
    "src/main/resources/assets/thaumcraft/models/block/greatwood_sapling.json",
    "src/main/resources/assets/thaumcraft/models/block/silverwood_log.json",
    "src/main/resources/assets/thaumcraft/models/block/silverwood_leaves.json",
    "src/main/resources/assets/thaumcraft/models/block/silverwood_sapling.json",
    "src/main/resources/assets/thaumcraft/blockstates/greatwood_log.json",
    "src/main/resources/assets/thaumcraft/blockstates/greatwood_leaves.json",
    "src/main/resources/assets/thaumcraft/blockstates/greatwood_sapling.json",
    "src/main/resources/assets/thaumcraft/blockstates/silverwood_log.json",
    "src/main/resources/assets/thaumcraft/blockstates/silverwood_leaves.json",
    "src/main/resources/assets/thaumcraft/blockstates/silverwood_sapling.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/greatwood_log.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/greatwood_leaves.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/greatwood_sapling.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/silverwood_log.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/silverwood_leaves.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/silverwood_sapling.json",
    "src/main/resources/data/minecraft/tags/blocks/logs.json",
    "src/main/resources/data/minecraft/tags/blocks/leaves.json",
    "src/main/resources/data/minecraft/tags/blocks/saplings.json",
]

checks = {
    "version_stage146": any((f"version = '{v}'" in files["build"] and f'version="{v}"' in files["mods"]) for v in ["2.04.0", "2.02.0", "2.00.0", "1.98.0", "1.78.0", "1.76.0", "1.70.0", "1.65.0", "1.64.0", "1.63.0", "1.62.0", "1.61.0", "1.60.0", "1.59.0", "1.58.0", "1.57.0", "1.56.0", "1.55.0", "1.54.0", "1.53.0", "1.52.0", "1.51.0", "1.50.0", "1.49.0", "1.48.0", "1.47.0", "1.46.0"]),
    "worldgen_runtime_hooked": "TC4WorldgenRuntime.generateNewChunk(level, chunk.getPos())" in files["events"] and "event.isNewChunk()" in files["events"] and "public static void generateNewChunk" in files["worldgen"],
    "tc4_generation_chances_present": all(x in files["worldgen"] for x in ["random.nextInt(60) == 3", "random.nextInt(25) == 7", "for (int i = 0; i < 18; i++)", "for (int i = 0; i < 20; i++)"]),
    "ore_generation_bridge": all(x in files["worldgen"] for x in ["CINNABAR_ORE", "AMBER_ORE", "randomInfusedCrystal", "tryPlaceOreBlob"]),
    "taint_pocket_bridge": "generateTaintPockets" in files["worldgen"] and "TaintSpreadRuntime.trySpreadNear" in files["worldgen"],
    "greatwood_silverwood_generators": all(x in files["trees"] for x in ["growGreatwood", "growSilverwood", "makeGreatwoodCrown", "makeSilverwoodCrown", "placeSilverwoodNode"]),
    "saplings_can_grow": all(x in files["sapling"] for x in ["BonemealableBlock", "randomTick", "performBonemeal", "TC4TreeGenerator.growGreatwood", "TC4TreeGenerator.growSilverwood"]),
    "saplings_registered_with_custom_class": "tc4SaplingBlock" in files["mod"] and "TC4SaplingBlock.Kind.GREATWOOD" in files["mod"] and "TC4SaplingBlock.Kind.SILVERWOOD" in files["mod"],
    "resources_exist": all((ROOT / rel).exists() for rel in required_resources),
    "jar_resource_guard_extended": all(x in files["build"] for x in ["assets/thaumcraft/textures/block/greatwood_log.png", "assets/thaumcraft/textures/block/silverwood_sapling.png", "data/minecraft/tags/blocks/logs.json"]),
    "workflow_stage146": "tc4_stage146_worldgen_resources_audit.py" in files["workflow"] and any(name in files["workflow"] for name in ["thaumcraft-legacy-rebuild-stage204-jars", "thaumcraft-legacy-rebuild-stage165-jars", "thaumcraft-legacy-rebuild-stage164-jars", "thaumcraft-legacy-rebuild-stage163-jars", "thaumcraft-legacy-rebuild-stage204-jars", "thaumcraft-legacy-rebuild-stage165-jars", "thaumcraft-legacy-rebuild-stage164-jars", "thaumcraft-legacy-rebuild-stage161-jars", "thaumcraft-legacy-rebuild-stage160-jars", "thaumcraft-legacy-rebuild-stage159-jars", "thaumcraft-legacy-rebuild-stage158-jars", "thaumcraft-legacy-rebuild-stage155-jars", "thaumcraft-legacy-rebuild-stage154-jars", "thaumcraft-legacy-rebuild-stage153-jars", "thaumcraft-legacy-rebuild-stage152-jars", "thaumcraft-legacy-rebuild-stage151-jars", "thaumcraft-legacy-rebuild-stage150-jars", "thaumcraft-legacy-rebuild-stage154-jars", "thaumcraft-legacy-rebuild-stage153-jars", "thaumcraft-legacy-rebuild-stage152-jars", "thaumcraft-legacy-rebuild-stage151-jars", "thaumcraft-legacy-rebuild-stage150-jars", "thaumcraft-legacy-rebuild-stage154-jars", "thaumcraft-legacy-rebuild-stage153-jars", "thaumcraft-legacy-rebuild-stage152-jars", "thaumcraft-legacy-rebuild-stage151-jars", "thaumcraft-legacy-rebuild-stage150-jars", "thaumcraft-legacy-rebuild-stage149-jars", "thaumcraft-legacy-rebuild-stage148-jars", "thaumcraft-legacy-rebuild-stage147-jars", "thaumcraft-legacy-rebuild-stage146-jars"]),
    "static_audit_tracks_sapling_helper": "tc4SaplingBlock" in files["static_audit"],
}

for rel in required_resources:
    path = ROOT / rel
    if path.suffix == ".json" and path.exists():
        try:
            json.loads(path.read_text(encoding="utf-8"))
        except Exception as exc:
            checks[f"json_valid:{rel}"] = False
            print(f"::error::{rel}: {exc}")

passed = all(checks.values())
print(json.dumps({"stage": 146, "goal": "TC4 worldgen + Greatwood/Silverwood output resources", "checks": checks, "passed": passed}, indent=2))
if not passed:
    sys.exit(1)
