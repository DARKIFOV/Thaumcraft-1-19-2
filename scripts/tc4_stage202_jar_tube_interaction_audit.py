#!/usr/bin/env python3
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
checks = [
    ("JarTubeInteractionRuntime.java", "src/main/java/com/darkifov/thaumcraft/jar/JarTubeInteractionRuntime.java", [
        "NBT_ASPECT_FILTER",
        "applyLabelToJar",
        "applyFilterToTube",
        "aspectFromPhial",
        "setFilterAspect(aspect)",
        "tube.setAspectFilter(null)",
    ]),
    ("EssentiaJarBlock.java", "src/main/java/com/darkifov/thaumcraft/block/EssentiaJarBlock.java", [
        "held.getItem() instanceof JarLabelItem",
        "JarTubeInteractionRuntime.applyLabelToJar",
        "isVoidJar(state)",
    ]),
    ("JarLabelItem.java", "src/main/java/com/darkifov/thaumcraft/block/JarLabelItem.java", [
        "JarTubeInteractionRuntime.applyLabelToJar",
        "InteractionHand.MAIN_HAND",
    ]),
    ("EssentiaTubeBlock.java", "src/main/java/com/darkifov/thaumcraft/block/EssentiaTubeBlock.java", [
        "JarTubeInteractionRuntime.applyFilterToTube",
        "player.getItemInHand(hand).getItem() instanceof JarLabelItem",
        "player.getItemInHand(hand).getItem() instanceof EssentiaPhialItem",
    ]),
    ("EssentiaJarBlockEntity.java", "src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaJarBlockEntity.java", [
        "AspectFilter",
        "Amount",
        "facing",
        "filterAspect",
    ]),
    ("EssentiaTubeBlockEntity.java", "src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java", [
        "setAspectFilter",
        "AspectFilter",
        "subtype",
    ]),
]

errors: list[str] = []
for label, rel, snippets in checks:
    path = ROOT / rel
    if not path.exists():
        errors.append(f"missing {label}: {rel}")
        continue
    text = path.read_text(encoding="utf-8")
    for snippet in snippets:
        if snippet not in text:
            errors.append(f"{label} missing snippet: {snippet}")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)
print("TC4 Stage202 jar/tube interaction audit: OK")
