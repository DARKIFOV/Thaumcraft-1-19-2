#!/usr/bin/env python3
"""Regression guard for the serializable Magical Forest multi-noise install."""
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []


def require(condition: bool, message: str) -> None:
    if not condition:
        errors.append(message)


installer_path = ROOT / "src/main/java/com/darkifov/thaumcraft/world/MagicalForestWorldgenInstaller.java"
diagnostics_path = ROOT / "src/main/java/com/darkifov/thaumcraft/world/MagicalForestWorldgenDiagnostics.java"
biomes_path = ROOT / "src/main/java/com/darkifov/thaumcraft/world/TC4Biomes.java"
at_path = ROOT / "src/main/resources/META-INF/accesstransformer.cfg"

for path in (installer_path, diagnostics_path, biomes_path, at_path):
    require(path.is_file(), f"missing Magical Forest resource: {path.relative_to(ROOT)}")

if not errors:
    installer = installer_path.read_text(encoding="utf-8")
    diagnostics = diagnostics_path.read_text(encoding="utf-8")
    biomes = biomes_path.read_text(encoding="utf-8")
    access = at_path.read_text(encoding="utf-8")

    require("MultiNoiseBiomeSource" in installer, "installer no longer uses the vanilla serializable source")
    require("source.parameters.values()" in installer, "installer no longer preserves the climate table")
    require("Biomes.FLOWER_FOREST" in installer, "rare forest climate replacement was removed")
    require("preservedFlowerForest" in installer and "flowerForestPoint" in installer,
            "installer deletes Flower Forest instead of splitting compatible climate points")
    require("new Climate.ParameterList" in installer, "installer no longer rebuilds climate parameters")
    require("generator.biomeSource = replacement" in installer, "live generator source is not replaced")
    require("possibleBiomes().contains(magicalForest)" in installer,
            "installer does not verify /locate visibility")
    require("ServerStartingEvent" in installer and "ServerStartedEvent" in installer,
            "installer lost its early event or fallback event")
    require("MagicalForestWorldgenInstaller.ensureInstalled" in diagnostics,
            "diagnostic no longer validates installation")
    require("sourcePossible" in diagnostics and "possibleBiomes" in diagnostics,
            "diagnostic no longer proves /locate visibility")
    require("runtime wrapper" not in diagnostics.lower(),
            "diagnostic still expects the removed non-serializable wrapper")
    require("f_62137_" in access and "f_48435_" in access,
            "required narrow access-transformer entries are missing")
    require("alone are deliberately not treated as proof" in biomes,
            "TC4Biomes again treats Forge compatibility lists as generation proof")
    require("ORIGINAL_MAGICAL_FOREST_WEIGHT = 5" in biomes,
            "Magical Forest lost the original TC4 default biome weight")
    require("BiomeManager.BiomeType.WARM" in biomes and "BiomeManager.BiomeType.COOL" in biomes,
            "Magical Forest must be registered in both original WARM and COOL pools")
    for token in (".temperature(0.7F)", ".downfall(0.6F)", ".waterColor(0x0077EE)",
                  ".grassColorOverride(0x55FF81)", ".foliageColorOverride(0x66FFC5)"):
        require(token in biomes, f"Magical Forest original climate/colour token missing: {token}")

    spawns_path = ROOT / "src/main/resources/data/thaumcraft/forge/biome_modifier/magical_forest_spawns.json"
    require(spawns_path.is_file(), "missing original Magical Forest spawn biome modifier")
    if spawns_path.is_file():
        spawns = spawns_path.read_text(encoding="utf-8")
        for token in ('"minecraft:wolf"', '"minecraft:horse"', '"minecraft:witch"',
                      '"minecraft:enderman"', '"thaumcraft:pech"', '"thaumcraft:wisp"'):
            require(token in spawns, f"Magical Forest spawn entry missing: {token}")

for namespace, tag_name in (
    ("forge", "is_overworld"),
    ("forge", "is_forest"),
    ("minecraft", "is_overworld"),
    ("minecraft", "is_forest"),
):
    path = ROOT / f"src/main/resources/data/{namespace}/tags/worldgen/biome/{tag_name}.json"
    require(path.is_file(), f"missing biome tag {path.relative_to(ROOT)}")
    if path.is_file():
        require("thaumcraft:magical_forest" in path.read_text(encoding="utf-8"),
                f"Magical Forest missing from {namespace}:{tag_name}")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("Magical Forest worldgen guard: OK (serializable multi-noise source and locate validation)")
