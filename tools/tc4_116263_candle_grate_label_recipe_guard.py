#!/usr/bin/env python3
"""Static parity guard for the 11.62.63 candle, item-grate and normal-recipe follow-up."""
from __future__ import annotations

import argparse
import json
import re
from collections import Counter
from pathlib import Path


def text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def load(path: Path):
    return json.loads(text(path))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("root", nargs="?", default=".")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    errors: list[str] = []

    def require(condition: bool, message: str) -> None:
        if not condition:
            errors.append(message)

    require("version = '11.62.63'" in text(root / "build.gradle"), "project version is not 11.62.63")

    mod = text(root / "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java")
    candle = text(root / "src/main/java/com/darkifov/thaumcraft/block/TallowCandleBlock.java")
    stability = text(root / "src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionStabilityParity.java")
    grate = text(root / "src/main/java/com/darkifov/thaumcraft/block/ItemGrateBlock.java")
    grate_be = text(root / "src/main/java/com/darkifov/thaumcraft/blockentity/ItemGrateBlockEntity.java")
    note = text(root / "src/main/java/com/darkifov/thaumcraft/block/ResearchNoteItem.java")
    label = text(root / "src/main/java/com/darkifov/thaumcraft/block/JarLabelItem.java")
    label_recipe = text(root / "src/main/java/com/darkifov/thaumcraft/recipe/JarLabelRecipe.java")
    jei = text(root / "src/main/java/com/darkifov/thaumcraft/compat/jei/TC4JeiPlugin.java")

    # Candle parity: light, particles and actual matrix stabilizer participation.
    require("lightLevel(state -> 14)" in mod, "tallow candles are not light level 14")
    require("implements InfusionStabilizer" in candle, "tallow candle does not implement the stabilizer marker")
    require("ParticleTypes.SMOKE" in candle and "ParticleTypes.FLAME" in candle,
            "tallow candle is missing TC4 smoke/flame particles")
    require("block instanceof InfusionStabilizer" in stability,
            "infusion stability scan does not consume the candle stabilizer marker")
    candle_tag = load(root / "src/main/resources/data/minecraft/tags/blocks/candles.json")
    require(len(candle_tag.get("values", [])) == 16, "minecraft:candles tag does not contain all 16 tallow candles")

    # Item grate parity: state, redstone, collision and insertion-only upper capability.
    for token in ["extends BaseEntityBlock", "BooleanProperty.create(\"open\")", "hasNeighborSignal",
                  "instanceof ItemEntity", "new ItemGrateBlockEntity", "IRON_TRAPDOOR_OPEN",
                  "if (!level.isClientSide)"]:
        require(token in grate, f"item grate block missing contract token: {token}")
    for token in ["ForgeCapabilities.ITEM_HANDLER", "side == Direction.UP", "!isOpen()",
                  "entity.setDeltaMovement(0.0D, -0.1D, 0.0D)", "return ItemStack.EMPTY"]:
        require(token in grate_be, f"item grate block entity missing contract token: {token}")
    require("ITEM_GRATE_BLOCK_ENTITY" in mod, "item grate block entity is not registered")

    # Dynamic 48-aspect labels + reset: one serializer, 49 explicit JEI displays.
    require("JAR_LABEL_RECIPE" in mod and "SimpleRecipeSerializer<>(JarLabelRecipe::new)" in mod,
            "dynamic jar-label recipe serializer is not registered")
    require("amount == 8" in label_recipe and "validClear()" in label_recipe,
            "jar-label recipe does not preserve TC4 8-essentia assignment and reset")
    require("Aspect.values()" in label, "jar-label item does not expose all ported aspects")
    require("JAR_LABEL" in jei and "jarLabelRecipes()" in jei and "allAspectLabels()" in jei,
            "JEI jar-label category is not registered")
    aspect_source = text(root / "src/main/java/com/darkifov/thaumcraft/Aspect.java")
    aspect_count = len(re.findall(r"^\s*[A-Z_]+\(\"[a-z_]+\"", aspect_source, re.MULTILINE))
    require(aspect_count == 48, f"expected 48 TC4 aspects, found {aspect_count}")
    require("for (Aspect aspect : Aspect.values())" in jei and "recipes.add(new JarLabelJeiRecipe" in jei,
            "JEI does not generate one label recipe per aspect")

    # Knowledge-fragment recipe and metadata-42 behaviour.
    knowledge_recipe = root / "src/main/resources/data/thaumcraft/recipes/knowledge_fragments_to_unknown_note_original_tc4.json"
    require(knowledge_recipe.exists(), "KnowFrag recipe is missing")
    if knowledge_recipe.exists():
        data = load(knowledge_recipe)
        require(data.get("pattern") == ["KKK", "KKK", "KKK"], "KnowFrag is not a 3x3 fragment recipe")
        require(data.get("result", {}).get("item") == "thaumcraft:research_note", "KnowFrag output is not research_note")
    for token in ["TC4ResearchFlagPolicy.HIDDEN", "entry.aspects().isEmpty()",
                  "OriginalResearchProgression.parentsComplete", "TC4ResearchMetadataIndex.itemTriggers",
                  "new Random(player.level.getGameTime() / 50L)", "7 + player.level.random.nextInt(3)",
                  "TC4Sounds.event(\"erase\")", "TC4Sounds.event(\"write\")"]:
        require(token in note, f"unknown research-note parity missing: {token}")

    # Count modern recipe files without falsely requiring 135 separate JSONs.
    recipe_dir = root / "src/main/resources/data/thaumcraft/recipes"
    counts: Counter[str] = Counter()
    for path in recipe_dir.glob("*.json"):
        try:
            counts[load(path).get("type", "unknown")] += 1
        except Exception as exc:
            errors.append(f"invalid recipe JSON {path.name}: {exc}")
    standard = counts["minecraft:crafting_shaped"] + counts["minecraft:crafting_shapeless"]
    require(standard >= 126, f"only {standard} standard crafting recipes; expected at least 126")
    require(counts["thaumcraft:jar_label"] == 1, "dynamic jar-label recipe JSON is missing or duplicated")

    report = {
        "version": "11.62.63",
        "status": "PASS" if not errors else "FAIL",
        "errors": errors,
        "candle_variants": len(candle_tag.get("values", [])),
        "aspect_count": aspect_count,
        "dynamic_label_runtime_recipes": 1,
        "dynamic_label_jei_displays": aspect_count + 1,
        "standard_crafting_recipes": standard,
        "recipe_type_counts": dict(sorted(counts.items())),
        "normal_recipe_accounting": {
            "original_registrations_after_loop_expansion": 135,
            "original_dynamic_aspect_label_assignments": 48,
            "original_dynamic_label_reset": 1,
            "original_non_label_registrations": 86,
            "note": "Modern custom serializers collapse the 49 NBT label registrations into one runtime recipe; raw JSON counts are not a parity metric."
        }
    }
    output = root / "reports/tc4_116263_candle_grate_label_recipe_guard.json"
    output.parent.mkdir(exist_ok=True)
    output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    if errors:
        print("TC4 11.62.63 candle/grate/label/recipe guard: FAIL")
        for error in errors:
            print(" -", error)
        return 1
    print("TC4 11.62.63 candle/grate/label/recipe guard: PASS")
    print(f"Candles: 16; aspects/JEI label displays: {aspect_count}/{aspect_count + 1}; standard crafting recipes: {standard}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
