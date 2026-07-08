#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]


def read(rel: str) -> str:
    p = ROOT / rel
    if not p.exists():
        raise AssertionError(f"missing file: {rel}")
    return p.read_text(encoding="utf-8")


def require(text: str, needle: str, label: str) -> None:
    if needle not in text:
        raise AssertionError(f"missing {label}: {needle}")


def require_re(text: str, pattern: str, label: str) -> None:
    if not re.search(pattern, text, re.MULTILINE | re.DOTALL):
        raise AssertionError(f"missing {label}: /{pattern}/")


def main() -> None:
    build = read("build.gradle")
    mods = read("src/main/resources/META-INF/mods.toml")
    require(build, "mappings channel: 'official', version: '1.19.2'", "Minecraft 1.19.2 mappings")
    require(build, "net.minecraftforge:forge:1.19.2-43", "Forge 1.19.2 dependency")
    require_re(build, r"version = '2\.(10|11|12|13|14|15|16)\.0'", "Stage210+ Gradle version")
    require_re(mods, r'version="2\.(10|11|12|13|14|15|16)\.0"', "Stage210+ mods.toml version")

    runic = read("src/main/java/com/darkifov/thaumcraft/infusion/TC4RunicArmorHelper.java")
    require(runic, 'HARDEN_TAG = "RS.HARDEN"', "TC4 RS.HARDEN tag")
    for id_, charge in {
        "tc4_runic_amulet": 8,
        "tc4_runic_amulet_emergency": 7,
        "tc4_runic_girdle": 10,
        "tc4_runic_girdle_kinetic": 9,
        "tc4_runic_ring_lesser": 1,
        "tc4_runic_ring": 5,
        "tc4_runic_ring_charged": 4,
        "tc4_runic_ring_regen": 4,
    }.items():
        require(runic, f'Map.entry("thaumcraft:{id_}", {charge})', f"base runic charge {id_}")
    require(runic, "getHardening", "EventHandlerRunic.getHardening port")
    require(runic, "getFinalCharge", "EventHandlerRunic.getFinalCharge port")
    require(runic, "putByte(HARDEN_TAG", "RS.HARDEN byte output")
    require(runic, "voidrobeboots", "void robe boots augmentable")

    adapter = read("src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionRunicAugmentAdapter.java")
    require(adapter, "INFUSION_RUNIC_AUGMENT", "runtime recipe kind")
    require(adapter, "RUNICAUGMENTATION", "research gate")
    require(adapter, "minecraft", "iron_ingot",)
    require(adapter, "thaumcraft", "tc4_dust",)
    require(adapter, "for (int i = 0; i < finalCharge; i++)", "extra salis mundus per final charge")
    require(adapter, "32.0D * Math.pow(2.0D, finalCharge)", "TC4 exponential essentia formula")
    require(adapter, "Aspect.TUTAMEN", "ARMOR/TUTAMEN aspect")
    require(adapter, "Aspect.PRAECANTATIO", "MAGIC/PRAECANTATIO aspect")
    require(adapter, "Aspect.POTENTIA", "ENERGY/POTENTIA aspect")
    require(adapter, "5 + Math.max(0, TC4RunicArmorHelper.getFinalCharge", "TC4 instability formula")
    require(adapter, "TC4RunicArmorHelper.addHardening", "output hardening increment")

    recipe = read("src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipe.java")
    require(recipe, "runicAugmentRecipe", "runic augment recipe factory")
    require(recipe, "isRunicAugmentRecipe", "runic recipe discriminator")
    require(recipe, "componentsFor(ItemStack catalyst)", "dynamic catalyst-dependent components")
    require(recipe, "aspectCostFor(ItemStack catalyst)", "dynamic catalyst-dependent aspects")
    require(recipe, "instabilityFor(ItemStack catalyst)", "dynamic catalyst-dependent instability")
    require(recipe, "TC4InfusionRunicAugmentAdapter.canApply", "runic catalyst matcher")

    recipes = read("src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipes.java")
    require(recipes, "RUNIC_RUNTIME", "runtime runic recipe list")
    require(recipes, "TC4InfusionRunicAugmentAdapter.materializeRecipe()", "runic runtime recipe registration")
    require(recipes, "combined.addAll(runicRuntimeRecipes())", "runtime recipe included in lookup")

    helper = read("src/main/java/com/darkifov/thaumcraft/infusion/InfusionProcessHelper.java")
    require(helper, "hasComponents(pedestals, recipe, ItemStack.EMPTY)", "legacy overload preserved")
    require(helper, "recipe.componentsFor(catalyst)", "component matching uses dynamic runic components")
    require(helper, "componentText(recipe, ItemStack.EMPTY)", "component text overload preserved")
    require(helper, "recipe.aspectCostFor(catalyst)", "aspect text uses dynamic runic aspects")

    matrix = read("src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java")
    require(matrix, "TC4InfusionRunicAugmentAdapter", "matrix runic adapter import")
    require(matrix, "InfusionProcessHelper.hasComponents(report.componentPedestals(), recipe, catalystPedestal.stored())", "start uses catalyst-dependent component matching")
    require(matrix, "recipe.aspectCostFor(catalystPedestal.stored())", "start uses catalyst-dependent aspects")
    require(matrix, "recipe.instabilityFor(catalystPedestal.stored())", "start uses catalyst-dependent instability")
    require(matrix, "recipe.componentsFor(catalystPedestal.stored())", "start stores dynamic pending components")
    require(matrix, "TC4InfusionRunicAugmentAdapter.applyOutput", "finish applies RS.HARDEN output")
    require(matrix, "failedComponentRollBound(recipeInstability)", "failure uses dynamic recipe instability")
    require(matrix, "failedEssentiaRollBound(recipeInstability)", "essentia failure uses dynamic recipe instability")

    item = read("src/main/java/com/darkifov/thaumcraft/item/TC4ResearchComponentItem.java")
    require(item, "TC4RunicArmorHelper.appendTooltip", "runic shield tooltip bridge")

    # Guard against accidentally reintroducing 1.7.10 runtime-only imports in the newly touched Java files.
    touched = [
        "src/main/java/com/darkifov/thaumcraft/infusion/TC4RunicArmorHelper.java",
        "src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionRunicAugmentAdapter.java",
        "src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipe.java",
        "src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipes.java",
        "src/main/java/com/darkifov/thaumcraft/infusion/InfusionProcessHelper.java",
        "src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java",
        "src/main/java/com/darkifov/thaumcraft/item/TC4ResearchComponentItem.java",
    ]
    forbidden = ["net.minecraft.item.ItemStack", "net.minecraft.nbt.NBTTag", "thaumcraft.api.IRunicArmor", "func_77983_a", "field_77990_d"]
    for rel in touched:
        text = read(rel)
        for needle in forbidden:
            if needle in text:
                raise AssertionError(f"1.19.2 guard failed: {needle} appears in {rel}")

    print("Stage210 runic augmentation + 1.19.2 compatibility audit OK")


if __name__ == "__main__":
    try:
        main()
    except AssertionError as exc:
        print(f"Stage210 audit failed: {exc}", file=sys.stderr)
        sys.exit(1)
