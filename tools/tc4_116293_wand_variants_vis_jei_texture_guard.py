#!/usr/bin/env python3
"""Regression guard for v11.62.96 wand capacity, subtype catalogue, JEI and BEWLR texture identity."""
from pathlib import Path
import hashlib
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
checks: list[tuple[str, bool]] = []

def text(rel: str) -> str:
    p = ROOT / rel
    return p.read_text(encoding="utf-8", errors="ignore") if p.is_file() else ""

def check(name: str, condition: bool) -> None:
    checks.append((name, bool(condition)))

build = text("build.gradle")
mods = text("src/main/resources/META-INF/mods.toml")
item = text("src/main/java/com/darkifov/thaumcraft/block/WandItem.java")
data = text("src/main/java/com/darkifov/thaumcraft/wand/WandComponentData.java")
variants = text("src/main/java/com/darkifov/thaumcraft/wand/WandVariantRuntime.java")
crafting = text("src/main/java/com/darkifov/thaumcraft/wand/WandCraftingRuntime.java")
jei = text("src/main/java/com/darkifov/thaumcraft/compat/jei/TC4JeiPlugin.java")
mod = text("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java")
renderer = text("src/main/java/com/darkifov/thaumcraft/client/render/WandItemRenderer.java")
rods = text("src/main/java/com/darkifov/thaumcraft/wand/WandRodType.java")
caps = text("src/main/java/com/darkifov/thaumcraft/wand/WandCapType.java")
build_wf = text(".github/workflows/build.yml")
release_wf = text(".github/workflows/release.yml")

check("build version", "version = '11.62.96'" in build)
check("mods version", 'version="11.62.96"' in mods)
check("original capacity formula", "rod.baseCapacity() * multiplier" in data and "isSceptre(stack) ? 150 : 100" in data)
check("wood/greatwood/silverwood capacities", 'WOOD("wood", "wand_rod_wood", 25' in rods
      and 'GREATWOOD("greatwood", "wand_rod_greatwood", 50' in rods
      and 'SILVERWOOD("silverwood", "wand_rod_silverwood", 100' in rods)
check("root rod and cap NBT", "root.putString(ORIGINAL_TAG_ROD" in data and "root.putString(ORIGINAL_TAG_CAP" in data)
check("creative variant matrix", "CRAFTABLE_ROD_COUNT = 18" in variants
      and "CRAFTABLE_CAP_COUNT = 6" in variants
      and "WAND_AND_STAFF_VARIANT_COUNT" in variants
      and "CREATIVE_VARIANT_COUNT" in variants)
check("exclude infinity-only components", "rod == WandRodType.CREATIVE" in variants and "cap == WandCapType.INFINITY" in variants)
check("canonical sceptre", "WandRodType.SILVERWOOD, WandCapType.THAUMIUM, true, true" in variants)
check("creative variants filled to own capacity", "WandItem.fillToCapacity(stack)" in variants
      and "int capacity = wandItem.stackVisCapacity(stack)" in item
      and "storeVisRaw(stack, aspect, capacity)" in item)
check("creative tab replaces legacy aliases", "items.removeIf" in mod
      and "IRON_CAPPED_WOODEN_WAND.get()" in mod
      and "GREATWOOD_WAND.get()" in mod
      and "SILVERWOOD_WAND.get()" in mod
      and "items.addAll(WandVariantRuntime.creativeVariants())" in mod)
check("runtime assembly result carries components", "public static ItemStack resultFor(ArcaneWorkbenchRecipe recipe)" in crafting
      and "WandVariantRuntime.create(" in crafting
      and "assembly.rod()" in crafting and "assembly.cap()" in crafting)
check("JEI uses dynamic assembly output", "WandCraftingRuntime.isGeneratedAssembly(recipe)" in jei
      and "WandCraftingRuntime.resultFor(recipe)" in jei
      and ": recipe.result()" in jei)
check("JEI preserves component subtypes without vis fragmentation", "registerItemSubtypes(ISubtypeRegistration registration)" in jei
      and "registration.registerSubtypeInterpreter(" in jei
      and "WandVariantRuntime.subtypeKey(stack)" in jei
      and "registration.useNbtForSubtypes" not in jei)
check("BEWLR component texture identity", "WandComponentData data = WandComponentData.from(stack)" in renderer
      and "data.rod().rendererTexture()" in renderer
      and "data.cap().rendererTexture()" in renderer
      and '"textures/entity/wand/" + name + ".png"' in renderer)
check("build workflow guard", "tc4_116293_wand_variants_vis_jei_texture_guard.py" in build_wf)
check("release workflow guard", "tc4_116293_wand_variants_vis_jei_texture_guard.py" in release_wf)

# Every active rod/cap texture used by the renderer must exist and match the
# original TC4 model texture stored in the source archive byte-for-byte.
texture_names = set(re.findall(r'"(wand_(?:rod|cap)_[a-z0-9_]+|rod_creative_infinity|cap_infinity)"', rods + caps))
# Enum constructor second fields are more reliable than the permissive regex.
texture_names = set(re.findall(r'^\s*[A-Z0-9_]+\("[^"]+",\s*"([^"]+)"', rods + "\n" + caps, re.M))
texture_names.discard("rod_creative_infinity")
texture_names.discard("cap_infinity")
texture_failures: list[str] = []
for name in sorted(texture_names):
    active = ROOT / "src/main/resources/assets/thaumcraft/textures/entity/wand" / f"{name}.png"
    original = ROOT / "src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models" / f"{name}.png"
    if not active.is_file() or not original.is_file():
        texture_failures.append(f"missing:{name}")
        continue
    if hashlib.sha256(active.read_bytes()).digest() != hashlib.sha256(original.read_bytes()).digest():
        texture_failures.append(f"different:{name}")
check("all renderer wand textures byte-exact original", bool(texture_names) and not texture_failures)

failed = [name for name, ok in checks if not ok]
print(f"TC4 11.62.96 wand variants/vis/JEI/texture guard: {len(checks)-len(failed)}/{len(checks)} PASS")
if texture_failures:
    for failure in texture_failures:
        print("TEXTURE:", failure)
if failed:
    for name in failed:
        print("FAIL:", name)
    sys.exit(1)
print("STATIC SOURCE/RESOURCE CONTRACT PASS; creative and JEI runtime rendering still require client verification")
