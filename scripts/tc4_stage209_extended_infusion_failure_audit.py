#!/usr/bin/env python3
from pathlib import Path
import json
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
    require_re(build, r"version = '2\.(09|1[0-9])\.0'", "Stage209+ Gradle version")
    require_re(mods, r'version="2\.(09|1[0-9])\.0"', "Stage209+ mods.toml version")

    matrix = read("src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java")
    require(matrix, "cycleDelay > 0", "craftCycle delay gate before instability")
    require(matrix, "TC4 rolls instability inside craftCycle", "craftCycle instability cadence comment")
    require(matrix, "InfusionProcessHelper.instabilityEvent(level, worldPosition", "runtime instability event bridge")
    require(matrix, "cycleDelay = TC4InfusionRuntime.CRAFT_CYCLE_DELAY", "instability consumes a cycle")
    require(matrix, "Catalyst changed during infusion", "invalid catalyst failure path")
    require(matrix, "sendInfusionSourceFromEntity", "XP drain PacketFXInfusionSource entity path")

    helper = read("src/main/java/com/darkifov/thaumcraft/infusion/InfusionProcessHelper.java")
    require(helper, "return InfusionInstabilityEvents.maybeTrigger", "instability boolean return")
    require(helper, "ThaumcraftNetwork.sendInfusionSource", "PacketFXInfusionSource bridge")

    events = read("src/main/java/com/darkifov/thaumcraft/infusion/InfusionInstabilityEvents.java")
    for roll in ["case 0, 2, 10, 13", "case 6, 17", "case 1, 11", "case 3, 8, 14", "case 5, 16", "case 12", "case 19", "case 7", "case 4, 15", "case 18", "case 9", "case 20"]:
        require(events, roll, f"TC4 instability event table {roll}")
    require(events, "ThaumcraftMod.FLUX_GOO", "TC4 flux goo placement")
    require(events, "ThaumcraftMod.FLUX_GAS", "TC4 flux gas placement")
    require(events, "ThaumcraftNetwork.sendBlockZap", "PacketFXBlockZap bridge")
    require(events, "EVENT_ROLL_BOUND = 21", "TC4 0..20 event bound")
    require(events, "VALIDITY_INSTABILITY_ROLL", "TC4 nextInt(500) instability gate")

    matcher = read("src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionItemMatcher.java")
    require(matcher, "WILDCARD_DAMAGE = 32767", "TC4 wildcard damage sentinel")
    require(matcher, "CompoundTag actualTag", "NBT equality check")
    require(matcher, "getDamageValue() == expectedDamage", "damage equality check")

    recipe = read("src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipe.java")
    require(recipe, "ComponentSpec", "component metadata specs")
    require(recipe, "readDamage", "JSON damage/meta parser")
    require(recipe, "TagParser.parseTag", "JSON NBT parser")
    require(recipe, "TC4InfusionItemMatcher.catalystMatches", "catalyst matcher bridge")
    require(recipe, "TC4InfusionItemMatcher.matches", "component matcher bridge")

    network = read("src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java")
    require(network, "PacketFXInfusionSource.class", "PacketFXInfusionSource registration")
    require(network, "PacketFXBlockZap.class", "PacketFXBlockZap registration")
    require(network, "sendInfusionSource", "source FX sender")
    require(network, "sendBlockZap", "zap FX sender")
    read("src/main/java/com/darkifov/thaumcraft/network/PacketFXInfusionSource.java")
    read("src/main/java/com/darkifov/thaumcraft/network/PacketFXBlockZap.java")
    read("src/main/java/com/darkifov/thaumcraft/client/fx/TC4ClientInfusionFx.java")

    goo = read("src/main/java/com/darkifov/thaumcraft/block/FluxGooBlock.java")
    gas = read("src/main/java/com/darkifov/thaumcraft/block/FluxGasBlock.java")
    require(goo, "Stage209 narrows", "flux goo Stage209 parity note")
    require(goo, "multiply(0.65D", "TC4 goo movement damping")
    require(goo, "random.nextInt(30)", "TC4 goo finite decay cadence")
    require(gas, "Stage209 narrows", "flux gas Stage209 parity note")
    require(gas, "level.random.nextInt(10)", "TC4 gas contact roll")
    require(gas, "level.removeBlock(pos, false)", "finite gas removal")

    report = json.loads(read("STAGE209_TC4_EXTENDED_INFUSION_FAILURE_PARITY_REPORT.json"))
    if report.get("stage") != 209:
        raise AssertionError("Stage209 report has wrong stage")
    require(read("docs/NEXT_CHAT_PROMPT_STAGE209.md"), "Stage210", "next-stage prompt")
    require(read("docs/TC4_EXTENDED_INFUSION_FAILURE_PARITY_STAGE209.md"), "TileInfusionMatrix", "stage doc original source reference")

    print("Stage209 extended infusion/failure parity audit OK")

if __name__ == "__main__":
    try:
        main()
    except AssertionError as exc:
        print(f"Stage209 audit failed: {exc}", file=sys.stderr)
        sys.exit(1)
