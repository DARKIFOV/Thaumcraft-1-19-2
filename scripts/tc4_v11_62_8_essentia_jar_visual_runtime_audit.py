#!/usr/bin/env python3
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]

def read(path):
    p = ROOT / path
    if not p.exists():
        raise AssertionError(f"missing {path}")
    return p.read_text(encoding="utf-8")

def require(cond, msg):
    if not cond:
        raise AssertionError(msg)

build = read("build.gradle")
require("version = '11.62.8'" in build or "v11.62.8 essentia jar runtime parity marker" in build, "project version must carry v11.62.8 compatibility marker")
require("tc4_essentia_jar_visual_suction_label_reset" in build, "missing v11.62.8 parity marker")

client = read("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java")
for name in ("ESSENTIA_JAR", "FILTERED_ESSENTIA_JAR", "VOID_ESSENTIA_JAR"):
    require(f"ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.{name}.get(), RenderType.translucent())" in client,
            f"{name} must use translucent render layer")

be = read("src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaJarBlockEntity.java")
require("if (voidJar)" in be and "return filterAspect != null ? 48 : 32;" in be,
        "void jar must keep suction at/above capacity")
require("(!voidJar && jar.amount() >= jar.capacity())" in be,
        "serverTick must not stop void jar pulling when visually full")

renderer = read("src/main/java/com/darkifov/thaumcraft/client/render/EssentiaJarRenderer.java")
for token in (
        "renderLiquidColumn", "renderStoredAspectGhost", "aspectTexture", "textures/aspects/", 
        "ORIGINAL_LABEL_TEXTURE", "textures/original/thaumcraft4/models/label.png",
        "LIQUID_MIN_XZ", "TC4 label card"):
    require(token in renderer, f"jar renderer missing {token}")
require("RenderType.entityTranslucent(aspectTexture(filter))" in renderer,
        "label must render the real aspect icon, not a coloured square placeholder")

for name in ("essentia_jar", "filtered_essentia_jar"):
    model = json.loads(read(f"src/main/resources/assets/thaumcraft/models/block/{name}.json"))
    tex = model.get("textures", {})
    require(tex.get("side") == "thaumcraft:block/tc4/jar_side", f"{name} must use TC4 side texture")
    require(tex.get("top") == "thaumcraft:block/tc4/jar_top", f"{name} must use TC4 top texture")
    require(tex.get("bottom") == "thaumcraft:block/tc4/jar_bottom", f"{name} must use TC4 bottom texture")
    require(len(model.get("elements", [])) >= 6, f"{name} needs detailed TC4 jar silhouette elements")

void_model = json.loads(read("src/main/resources/assets/thaumcraft/models/block/void_essentia_jar.json"))
require(void_model["textures"].get("side") == "thaumcraft:block/tc4/jar_side_void", "void jar must use TC4 void side texture")
require(void_model["textures"].get("top") == "thaumcraft:block/tc4/jar_top_void", "void jar must use TC4 void top texture")

for name in ("essentia_jar", "filtered_essentia_jar", "void_essentia_jar"):
    item = json.loads(read(f"src/main/resources/assets/thaumcraft/models/item/{name}.json"))
    require(item.get("parent") == f"thaumcraft:block/{name}", f"{name} item model must follow block model")

print("tc4_v11_62_8_essentia_jar_visual_runtime_audit: OK")
