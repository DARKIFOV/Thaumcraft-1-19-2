#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    p = ROOT / rel
    if not p.exists():
        raise AssertionError(f"Missing required file: {rel}")
    return p.read_text(encoding="utf-8")

def require(text: str, needle: str, label: str):
    if needle not in text:
        raise AssertionError(f"Missing {label}: {needle}")

def require_re(text: str, pattern: str, label: str):
    if not re.search(pattern, text, re.MULTILINE | re.DOTALL):
        raise AssertionError(f"Missing {label}: /{pattern}/")

def main():
    build = read("build.gradle")
    mods = read("src/main/resources/META-INF/mods.toml")
    require_re(build, r"version = '2\.(0[89]|1[0-9])\.0'", "Stage208+ Gradle version")
    require_re(mods, r'version="2\.(0[89]|1[0-9])\.0"', "Stage208+ mods.toml version")

    renderer = read("src/main/java/com/darkifov/thaumcraft/client/render/InfusionMatrixRenderer.java")
    require(renderer, "textures/models/infuser.png", "original infuser model texture")
    require(renderer, "BlockEntityRenderer<InfusionMatrixBlockEntity>", "block entity renderer binding")
    require(renderer, "for (int a = 0; a < 2; a++)", "eight-cube a loop")
    require(renderer, "for (int b = 0; b < 2; b++)", "eight-cube b loop")
    require(renderer, "for (int c = 0; c < 2; c++)", "eight-cube c loop")
    require(renderer, "0.45F", "TC4 ModelCube scale")
    require(renderer, "0.25F", "TC4 cubelet offset")
    require(renderer, "35.0F * startUp", "startup pitch")
    require(renderer, "45.0F * startUp", "startup roll")
    require(renderer, "currentInstability", "active instability wobble")
    require(renderer, "RenderType.entityTranslucent", "overlay glow render type")
    require(renderer, "renderHalo", "crafting halo renderer")
    require(renderer, "RenderType.entityTranslucent(INFUSER_TEXTURE)", "crafting halo compatible translucent render type")

    client = read("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java")
    require(client, "InfusionMatrixRenderer", "client renderer registration import")
    require(client, "INFUSION_MATRIX_BLOCK_ENTITY", "matrix block entity renderer registration")

    block = read("src/main/java/com/darkifov/thaumcraft/block/InfusionMatrixBlock.java")
    require(block, "RenderShape.ENTITYBLOCK_ANIMATED", "animated render shape")

    model = read("src/main/resources/assets/thaumcraft/models/block/infusion_matrix.json")
    if "cube_all" in model:
        raise AssertionError("Infusion matrix block model still uses fake cube_all geometry")
    require(model, "thaumcraft:models/infuser", "infuser particle texture")

    recipe = read("src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipe.java")
    require(recipe, "recipeType", "recipeType field")
    require(recipe, "enchantmentId", "enchantment output id")
    require(recipe, "outputNbtLabel", "NBTBase output label")
    require(recipe, "enchantmentRecipe", "InfusionEnchantmentRecipe materializer")
    require(recipe, "isInfusionEnchantment", "recipeType==1 helper")
    require(recipe, "catalystWildcard", "central item wildcard for enchantment recipes")
    require(recipe, "componentMatches", "recipe-aware component matcher")

    adapter = read("src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionEnchantmentAdapter.java")
    require(adapter, "materializeRecipes", "enchantment runtime materialization")
    require(adapter, "canApply", "InfusionEnchantmentRecipe.matches adapter")
    require(adapter, "calcInstability", "TC4 enchantment instability scaling")
    require(adapter, "calcXp", "TC4 enchantment XP scaling")
    require(adapter, "scaledAspects", "TC4 getEssentiaMod aspect scaling")
    require(adapter, "applyOutput", "craftingFinish enchantment output")
    require(adapter, "EnchantmentHelper.setEnchantments", "increment enchantment output")

    recipes = read("src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipes.java")
    require(recipes, "TC4InfusionEnchantmentAdapter.materializeRecipes", "runtime enchantment recipe inclusion")

    helper = read("src/main/java/com/darkifov/thaumcraft/infusion/InfusionProcessHelper.java")
    require(helper, "recipe.componentMatches", "ItemStack-aware component matching path")
    require(helper, "getCraftingRemainingItem", "container item preservation")
    require(helper, "hasAspects(List<EssentiaJarBlockEntity> jars, Map<Aspect, Integer>", "scaled aspect availability check")

    matrix = read("src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java")
    require(matrix, "recipeXP", "recipe XP runtime field")
    require(matrix, "recipeType", "recipe type runtime field")
    require(matrix, "recipexp", "TC4 recipexp NBT alias")
    require(matrix, "recipetype", "TC4 recipetype NBT alias")
    require(matrix, "drainEnchantmentXp", "TC4 XP drain cycle")
    require(matrix, "giveExperienceLevels(-1)", "one-level XP drain")
    require(matrix, "DamageSource.MAGIC", "XP drain magic damage")
    require(matrix, "TC4InfusionEnchantmentAdapter.scaledAspects", "dynamic enchantment aspect cost")
    require(matrix, "TC4InfusionEnchantmentAdapter.applyOutput", "enchantment craftingFinish")
    require(matrix, "addTagElement", "NBTBase-style output attach")
    require(matrix, "findComponentPedestal(report.componentPedestals(), componentId, recipe)", "recipe-aware travelling component lookup")
    require(matrix, "consumeSingleComponent(report.componentPedestals(), travellingComponent, recipe)", "recipe-aware delayed component consume")

    report = read("STAGE208_TC4_INFUSION_RENDERER_ENCHANTMENT_PARITY_REPORT.json")
    require(report, '"stage": 208', "Stage208 report")
    require(report, "TileRunicMatrixRenderer", "source of truth report")
    require(report, "InfusionEnchantmentRecipe", "enchantment source report")

    prompt = read("docs/NEXT_CHAT_PROMPT_STAGE208.md")
    require(prompt, "Stage209", "next-stage prompt")
    require(prompt, "Thaumcraft4-1.7.10", "original source rule in prompt")

    print("Stage208 infusion renderer/enchantment parity audit OK")

if __name__ == "__main__":
    try:
        main()
    except AssertionError as exc:
        print(f"Stage208 audit failed: {exc}", file=sys.stderr)
        sys.exit(1)
