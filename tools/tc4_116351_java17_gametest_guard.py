#!/usr/bin/env python3
"""Regression guard for the v11.63.51 Forge API and energized-node fixture fixes."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


checks = {
    "project_version": any(f"version = '{v}'" in read("build.gradle") for v in ("11.63.51", "11.63.52", "11.63.53", "11.63.54", "11.63.55")),
    "mods_version": any(f'version="{v}"' in read("src/main/resources/META-INF/mods.toml") for v in ("11.63.51", "11.63.52", "11.63.53", "11.63.54", "11.63.55")),
    "scrubber_level_reader": "canSurvive(BlockState state, LevelReader level, BlockPos pos)" in read(
        "src/main/java/com/darkifov/thaumcraft/block/FumeDissipatorBlock.java"
    ),
    "door_place_context": "BlockPlaceContext placeContext = new BlockPlaceContext(context);" in read(
        "src/main/java/com/darkifov/thaumcraft/item/ArcaneDoorItem.java"
    ),
    "mana_bean_place_context": "BlockPlaceContext placeContext = new BlockPlaceContext(context);" in read(
        "src/main/java/com/darkifov/thaumcraft/item/simple/TC4ManaBeanItem.java"
    ),
    "energized_fixture_cooldown": read(
        "src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java"
    ).count('energized.putInt("EnergizedTicks", 100);') == 3,
}

failed = [name for name, passed in checks.items() if not passed]
if failed:
    raise SystemExit("TC4 v11.63.51 guard: FAIL: " + ", ".join(failed))

print(f"TC4 v11.63.51 Java 17/GameTest guard: PASS ({len(checks)}/{len(checks)})")
