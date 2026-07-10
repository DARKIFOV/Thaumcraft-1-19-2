#!/usr/bin/env python3
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(path):
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing {path}")
        return ""
    return p.read_text(encoding="utf-8")

def require(path, token, label=None):
    text = read(path)
    if token not in text:
        errors.append(f"{path}: missing {label or token}")

def load_json(path):
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing {path}")
        return {}
    try:
        return json.loads(p.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"{path}: invalid json: {exc}")
        return {}

# Version marker must move forward without losing older compatibility comments.
require("build.gradle", "version = '11.62.7'", "project version 11.62.7")
require("src/main/resources/META-INF/mods.toml", 'version="11.62.7"', "mods.toml version 11.62.7")
require("build.gradle", "greatwood_silverwood_leaf_tint_and_shape_reset", "v11.62.7 marker")

client = read("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java")
for token in [
    "RegisterColorHandlersEvent.Block",
    "RegisterColorHandlersEvent.Item",
    "TC4_GREATWOOD_LEAF_TINT",
    "TC4_SILVERWOOD_LEAF_TINT",
    "ThaumcraftMod.GREATWOOD_LEAVES.get()",
    "ThaumcraftMod.SILVERWOOD_LEAVES.get()",
    "RenderType.cutoutMipped()",
    "ThaumcraftMod.GREATWOOD_SAPLING.get()",
    "ThaumcraftMod.SILVERWOOD_SAPLING.get()",
]:
    if token not in client:
        errors.append(f"ClientModEvents.java: missing {token}")

for name, tex in [("greatwood", "thaumcraft:block/tc4/greatwoodleaves"), ("silverwood", "thaumcraft:block/tc4/silverwoodleaves")]:
    model = load_json(f"src/main/resources/assets/thaumcraft/models/block/{name}_leaves.json")
    if model.get("parent") != "minecraft:block/leaves":
        errors.append(f"{name}_leaves model must use minecraft:block/leaves for cutout/tint parity")
    if model.get("textures", {}).get("all") != tex:
        errors.append(f"{name}_leaves model must use original TC4 texture {tex}")

for name, tex in [("greatwood", "thaumcraft:block/tc4/greatwoodsapling"), ("silverwood", "thaumcraft:block/tc4/silverwoodsapling")]:
    model = load_json(f"src/main/resources/assets/thaumcraft/models/block/{name}_sapling.json")
    if model.get("parent") != "minecraft:block/cross":
        errors.append(f"{name}_sapling model must use minecraft:block/cross")
    if model.get("textures", {}).get("cross") != tex:
        errors.append(f"{name}_sapling model must use original TC4 sapling texture {tex}")

tree = read("src/main/java/com/darkifov/thaumcraft/world/TC4TreeGenerator.java")
for token in [
    "makeGreatwoodButtressRootsLikeTC4",
    "makeGreatwoodCrownCapLikeTC4",
    "v11.62.7 runtime parity reset",
    "generateGreatwoodPassLikeTC4(level, tc4Base, heightLimit, 1.38D, random)",
    "two-pass audit marker",
]:
    if token not in tree:
        errors.append(f"TC4TreeGenerator.java: missing {token}")
# Ensure the dangerous second full tree is no longer an active statement.
active_second_pass = "        generateGreatwoodPassLikeTC4(level, new BlockPos(base.getX(), base.getY() + trunkHeight, base.getZ()), heightLimit, 1.66D, random);"
if active_second_pass in tree:
    errors.append("TC4TreeGenerator.java: second full greatwood pass is still active")
if "// generateGreatwoodPassLikeTC4(level, new BlockPos(base.getX(), base.getY() + trunkHeight, base.getZ()), heightLimit, 1.66D, random);" not in tree:
    errors.append("TC4TreeGenerator.java: compatibility marker for old second pass missing")

if errors:
    print("TC4 v11.62.7 magic tree visual/shape audit failed:")
    for e in errors:
        print(" -", e)
    sys.exit(1)
print("TC4 v11.62.7 magic tree visual/shape audit passed")
