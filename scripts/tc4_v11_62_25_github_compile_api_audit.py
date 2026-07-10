#!/usr/bin/env python3
"""Regression guard for v11.62.25 GitHub compile/API fixes."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel: str) -> str:
    path = ROOT / rel
    if not path.exists():
        errors.append(f"missing {rel}")
        return ""
    return path.read_text(encoding="utf-8", errors="replace")

def require(rel: str, *tokens: str) -> str:
    text = read(rel)
    for token in tokens:
        if token not in text:
            errors.append(f"{rel}: missing {token}")
    return text

build = require("build.gradle", "version = '11.62.25'", "dependsOn 'copyGithubOutputJar'")
mods = require("src/main/resources/META-INF/mods.toml", 'version="11.62.25"')
workflow = require(".github/workflows/main.yml", "tc4_v11_62_25_github_compile_api_audit.py", "v11.62.25-github-jar", "v11.62.25-build-reports")

screen = require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java",
    "import com.mojang.blaze3d.platform.Window;",
    "private void renderRecipeVisuals(",
)
firebat = require(
    "src/main/java/com/darkifov/thaumcraft/entity/TC4FireBatEntity.java",
    "public void addAdditionalSaveData(CompoundTag tag)",
    "public void readAdditionalSaveData(CompoundTag tag)",
)
golem = require(
    "src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java",
    "state.use(serverLevel, fake, InteractionHand.MAIN_HAND, hit)",
    "getMaterial().isReplaceable()",
    "LazyOptional<IFluidHandlerItem> capability",
)
require(
    "src/main/java/com/darkifov/thaumcraft/golem/GolemOriginalRuntime.java",
    "net.minecraft.util.RandomSource random",
)
require(
    "src/main/java/com/darkifov/thaumcraft/block/TemporaryHoleBlock.java",
    "(ServerLevel) tickerLevel",
    "state.getMaterial().isReplaceable()",
)
tree = require(
    "src/main/java/com/darkifov/thaumcraft/world/TC4TreeGenerator.java",
    "private static void makeGreatwoodButtressRootsLikeTC4(",
    "private static void makeGreatwoodCrownCapLikeTC4(",
)

for forbidden in [
    "import net.minecraft.client.Window;",
    "state.use(serverLevel, pos, fake, InteractionHand.MAIN_HAND, hit)",
    "LazyOptional<IFluidHandler> capability = filter.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)",
    "jar.finalizedBy('reobfJar')",
    "finalizedBy 'copyGithubOutputJar'",
]:
    corpus = "\n".join([build, screen, firebat, golem, tree, read("src/main/java/com/darkifov/thaumcraft/block/TemporaryHoleBlock.java")])
    if forbidden in corpus:
        errors.append(f"obsolete compile-risk token remains: {forbidden}")

if errors:
    print("TC4 v11.62.25 GitHub compile/API audit failed:")
    for error in errors:
        print(" -", error)
    sys.exit(1)
print("TC4 v11.62.25 GitHub compile/API audit: OK")
