from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def require(path, marker=None):
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing {path}")
        return ""
    text = p.read_text(encoding="utf-8", errors="ignore") if p.suffix.lower() in {".java", ".json", ".md", ".gradle"} or p.name == "build.gradle" else ""
    if marker and marker not in text:
        errors.append(f"{path}: missing marker {marker!r}")
    return text

build = require("build.gradle", "version = '5.22.0'")
require("docs/NEXT_CHAT_PROMPT_STAGE522.md", "Stage503–522")
require("STAGE503_522_TC4_ESSENTIA_THAUMATORIUM_RESERVOIR_REPORT.json", "TileEssentiaReservoir")

mod = require("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java")
for marker in [
    "ESSENTIA_RESERVOIR",
    "ADVANCED_ALCHEMICAL_FURNACE",
    "THAUMATORIUM",
    "ESSENTIA_RESERVOIR_BLOCK_ENTITY",
    "THAUMATORIUM_BLOCK_ENTITY",
    "EssentiaReservoirBlock",
    "ThaumatoriumBlock"
]:
    if marker not in mod:
        errors.append(f"ThaumcraftMod.java missing {marker}")

reservoir_block = require("src/main/java/com/darkifov/thaumcraft/block/EssentiaReservoirBlock.java", "BlockEssentiaReservoir")
reservoir_be = require("src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaReservoirBlockEntity.java", "CAPACITY = 256")
for marker in ["NBT_ASPECTS", "NBT_FACING", "canAccessFrom", "acceptFromTube", "removeEssentia", "ORIGINAL_RESERVOIR_SUCTION"]:
    if marker not in reservoir_be:
        errors.append(f"EssentiaReservoirBlockEntity.java missing {marker}")

thaumatorium_block = require("src/main/java/com/darkifov/thaumcraft/block/ThaumatoriumBlock.java", "TileThaumatorium")
thaumatorium_be = require("src/main/java/com/darkifov/thaumcraft/blockentity/ThaumatoriumBlockEntity.java", "TC4EssentiaNetworkRuntime")
for marker in ["AlchemyRecipes.findByCatalyst", "recipe.cost()", "Containers.dropItemStack", "ORIGINAL_CRAFT_INTERVAL_TICKS"]:
    if marker not in thaumatorium_be:
        errors.append(f"ThaumatoriumBlockEntity.java missing {marker}")

network = require("src/main/java/com/darkifov/thaumcraft/essentia/TC4EssentiaNetworkRuntime.java", "Thaumatorium")
for marker in ["EssentiaReservoirBlockEntity", "EssentiaJarBlockEntity", "AlembicBlockEntity", "drain", "available", "collectNetwork"]:
    if marker not in network:
        errors.append(f"TC4EssentiaNetworkRuntime.java missing {marker}")

connections = require("src/main/java/com/darkifov/thaumcraft/essentia/EssentiaTubeConnections.java")
if "EssentiaReservoirBlockEntity" not in connections or "ThaumatoriumBlockEntity" not in connections:
    errors.append("EssentiaTubeConnections must include reservoir and thaumatorium transport endpoints")
if "reservoir.canAccessFrom(direction.getOpposite())" not in connections:
    errors.append("EssentiaTubeConnections must guard reservoir access side")

resolver = require("src/main/java/com/darkifov/thaumcraft/essentia/EssentiaSuctionResolver.java")
if "return level.getBlockEntity(pos) instanceof EssentiaTubeBlockEntity" not in resolver:
    errors.append("EssentiaSuctionResolver must treat all BE-backed tube subtypes as tube-like")
if "RESERVOIR_SOURCE_PRIORITY" not in resolver:
    errors.append("EssentiaSuctionResolver must expose reservoir source pressure")

tube = require("src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java")
for marker in ["ReservoirSource", "ReservoirDestination", "JarSource", "source.pos()", "destination.container().accept"]:
    if marker not in tube:
        errors.append(f"EssentiaTubeBlockEntity.java missing {marker}")

recipe_resolver = require("src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeItemResolver.java")
for marker in [
    'BLOCK_META.put("blockEssentiaReservoir:*", "thaumcraft:essentia_reservoir")',
    'BLOCK_META.put("blockMetalDevice:3", "thaumcraft:advanced_alchemical_furnace")',
    'BLOCK_META.put("blockMetalDevice:10", "thaumcraft:thaumatorium")'
]:
    if marker not in recipe_resolver:
        errors.append(f"TC4RecipeItemResolver missing {marker}")

infusion = json.loads((ROOT / "src/main/resources/data/thaumcraft/thaumcraft_infusion/tc4_essentia_reservoir.json").read_text())
if infusion.get("result", {}).get("item") != "thaumcraft:essentia_reservoir":
    errors.append("tc4_essentia_reservoir infusion result must be runtime essentia_reservoir")
if infusion.get("catalyst") != "thaumcraft:essentia_tube":
    errors.append("tc4_essentia_reservoir catalyst must be runtime essentia_tube")
if any(x == "thaumcraft:tc4_block_essentia_jar" for x in infusion.get("components", [])):
    errors.append("tc4_essentia_reservoir components must not use tc4_block_essentia_jar mirror")

arcane = json.loads((ROOT / "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_advalchemyconstruct.json").read_text())
if arcane.get("result", {}).get("item") != "thaumcraft:advanced_alchemical_furnace":
    errors.append("tc4_advalchemyconstruct result must be runtime advanced_alchemical_furnace")

for path in [
    "src/main/resources/assets/thaumcraft/textures/block/essentiareservoir.png",
    "src/main/resources/assets/thaumcraft/textures/block/essentia_reservoir.png",
    "src/main/resources/assets/thaumcraft/textures/models/reservoir.obj",
    "src/main/resources/assets/thaumcraft/textures/models/reservoir.png",
    "src/main/resources/assets/thaumcraft/textures/models/thaumatorium.obj",
    "src/main/resources/assets/thaumcraft/textures/models/thaumatorium.png",
    "src/main/resources/assets/thaumcraft/textures/block/advanced_alchemical_furnace.png",
    "src/main/resources/assets/thaumcraft/blockstates/essentia_reservoir.json",
    "src/main/resources/assets/thaumcraft/blockstates/thaumatorium.json",
    "src/main/resources/assets/thaumcraft/blockstates/advanced_alchemical_furnace.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/essentia_reservoir.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/thaumatorium.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/advanced_alchemical_furnace.json"
]:
    if not (ROOT / path).exists():
        errors.append(f"missing resource {path}")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("Stage503-522 essentia/thaumatorium/reservoir audit: OK")
