#!/usr/bin/env python3
from pathlib import Path
import json, sys

ROOT = Path(__file__).resolve().parents[1]
errors=[]; checks=0

def need(path, *tokens):
    global checks
    p=ROOT/path
    if not p.is_file():
        errors.append(f"missing {path}"); return
    text=p.read_text(encoding="utf-8")
    for token in tokens:
        checks += 1
        if token not in text: errors.append(f"{path}: missing {token!r}")

def valid_json(path):
    global checks
    checks += 1
    try: json.loads((ROOT/path).read_text(encoding="utf-8"))
    except Exception as exc: errors.append(f"{path}: {exc}")

need("build.gradle", "version = '11.63.23'")
need("src/main/resources/META-INF/mods.toml", 'version="11.63.23"')
need("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java",
     'BRAIN_JAR = BLOCKS.register("tc4_jar_brain"',
     'BRAIN_JAR_ITEM = ITEMS.register("tc4_jar_brain"',
     'Map.entry("tc4_jar_brain", BRAIN_JAR_ITEM)',
     'BRAIN_JAR_BLOCK_ENTITY')
need("src/main/java/com/darkifov/thaumcraft/block/BrainJarBlock.java",
     "Block.box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D)",
     "return Shapes.block();", "return 2.0F;", "hasAnalogOutputSignal",
     "releaseRandomExperience", "releaseAllExperience", "ParticleTypes.EFFECT")
need("src/main/java/com/darkifov/thaumcraft/blockentity/BrainJarBlockEntity.java",
     "MAX_XP = 2000", "inflate(6.0D)", "LOCAL_ABSORB_BOX", "/ 7.0D",
     "* 0.15D", "* 0.33D", "eatDelay = 40", "nextInt(Math.min(xp + 1, 64))",
     'tag.putInt("XP", xp)', "ExperienceOrb.award", "* 14.0F", 'TC4Sounds.event("brain")',
     "private static float wrapRadians(float angle)", "angle %= twoPi",
     "wrapRadians(rotation - previousRotation)", "wrapRadians(desired - brain.rotation)")
brain=(ROOT/"src/main/java/com/darkifov/thaumcraft/blockentity/BrainJarBlockEntity.java").read_text()
checks += 1
if "Mth.wrapRadians" in brain: errors.append("Forge 1.19.2-incompatible Mth.wrapRadians call remains")
need("src/main/java/com/darkifov/thaumcraft/client/render/model/TC4BrainJarModel.java",
     "12.0F, 10.0F, 16.0F", "8.0F, 3.0F, 7.0F", "2.0F, 6.0F, 2.0F",
     "0.4089647F", "LayerDefinition.create(mesh, 128, 64)")
need("src/main/java/com/darkifov/thaumcraft/client/render/BrainJarRenderer.java",
     "brain2.png", "jarbrine.png", "/ 14.0F", "0.03F", "poseStack.scale(0.4F")
need("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java",
     "BRAIN_JAR_BLOCK_ENTITY", "BrainJarRenderer::new", "TC4BrainJarModel.LAYER",
     "ThaumcraftMod.BRAIN_JAR.get(), RenderType.translucent()")
need("src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java",
     "if (target.experienceLevel > 0)", "target.giveExperienceLevels(-1)")
matrix=(ROOT/"src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java").read_text()
checks += 2
if "target.experienceLevel > 0 || target.getAbilities().instabuild" in matrix: errors.append("creative XP bypass remains")
if "if (!target.getAbilities().instabuild)" in matrix: errors.append("creative damage/drain exemption remains")
need("src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java",
     "TileTubeValve only overrides setSuction", "return true;")
for path in [
 "src/main/resources/assets/thaumcraft/blockstates/tc4_jar_brain.json",
 "src/main/resources/assets/thaumcraft/models/block/tc4_jar_brain.json",
 "src/main/resources/assets/thaumcraft/models/item/tc4_jar_brain.json",
 "src/main/resources/data/thaumcraft/loot_tables/blocks/tc4_jar_brain.json"]: valid_json(path)
for lang in ["en_us","ru_ru"]:
    data=json.loads((ROOT/f"src/main/resources/assets/thaumcraft/lang/{lang}.json").read_text(encoding="utf-8"))
    checks += 2
    if "block.thaumcraft.tc4_jar_brain" not in data: errors.append(f"{lang}: block lang missing")
    if "item.thaumcraft.tc4_jar_brain" not in data: errors.append(f"{lang}: item lang missing")
if errors:
    print(f"TC4 11.63.10 Brain Jar runtime guard: FAIL ({len(errors)} problems)")
    for e in errors: print(" -",e)
    sys.exit(1)
print(f"TC4 11.63.10 Brain Jar runtime guard: PASS ({checks} checks)")
