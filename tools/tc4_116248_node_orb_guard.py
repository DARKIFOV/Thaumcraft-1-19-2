#!/usr/bin/env python3
"""Regression guard for v11.62.48 aura-node scheduler and Aspect Orb parity."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ERRORS: list[str] = []


def read(rel: str) -> str:
    path = ROOT / rel
    if not path.is_file():
        ERRORS.append(f"missing file: {rel}")
        return ""
    return path.read_text(encoding="utf-8")


def require(text: str, token: str, label: str) -> None:
    if token not in text:
        ERRORS.append(f"{label}: missing {token!r}")


def forbid(text: str, token: str, label: str) -> None:
    if token in text:
        ERRORS.append(f"{label}: forbidden token {token!r}")


def version_tuple(value: str) -> tuple[int, ...]:
    return tuple(int(part) for part in value.split("."))


build = read("build.gradle")
mods = read("src/main/resources/META-INF/mods.toml")
for label, text, pattern in (
    ("build.gradle", build, r"^version\s*=\s*'([0-9.]+)'"),
    ("mods.toml", mods, r'^version="([0-9.]+)"'),
):
    match = re.search(pattern, text, re.MULTILINE)
    if match is None or version_tuple(match.group(1)) < (11, 62, 48):
        ERRORS.append(f"{label}: expected v11.62.48 or later")

node = read("src/main/java/com/darkifov/thaumcraft/blockentity/AuraNodeBlockEntity.java")
for token in (
    "private long nodeTick",
    "node.nodeTick++",
    "node.nodeTick % regeneration == 0L",
    "private void tickNodeStability(Level level)",
    "new AspectOrbEntity(",
    "serverLevel.addFreshEntity(orb)",
    "10_000 / lock",
    "12_500 / lock",
    "nodeType = AuraNodeType.NORMAL.name()",
    "nodeModifier = AuraNodeModifier.PALE.name()",
):
    require(node, token, "AuraNodeBlockEntity")
for token in (
    "private void tickUnstable",
    "level.getGameTime() % regeneration",
    "aspects.add(to, 1)",
    'tag.putLong("NodeTick"',
    'tag.getLong("NodeTick")',
):
    forbid(node, token, "AuraNodeBlockEntity")

orb = read("src/main/java/com/darkifov/thaumcraft/entity/AspectOrbEntity.java")
for token in (
    "public static final int MAX_AGE = 150",
    "private static final double SEEK_RANGE = 8.0D",
    "getBoundingBox().inflate(SEEK_RANGE)",
    "findHotbarWandWithRoom",
    "current < wand.stackVisCapacity(stack)",
    "WandItem.addVis(wand, getAspect(), getAspectValue())",
    "player.takeXpDelay > 0",
    "player.takeXpDelay = 2",
    "NetworkHooks.getEntitySpawningPacket(this)",
    'tag.putString("Aspect", getAspect().id())',
):
    require(orb, token, "AspectOrbEntity")

renderer = read("src/main/java/com/darkifov/thaumcraft/client/render/AspectOrbRenderer.java")
for token in (
    '"textures/original/thaumcraft4/misc/particles.png"',
    "System.nanoTime() / 25_000_000L % 16L",
    "float v0 = 0.5F",
    "float v1 = 0.5625F",
    "AspectOrbEntity.MAX_AGE - orb.orbAge()",
    "!aspect.usesAlphaBlend()",
):
    require(renderer, token, "AspectOrbRenderer")

mod = read("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java")
for token in (
    "RegistryObject<EntityType<AspectOrbEntity>> ASPECT_ORB",
    'ENTITY_TYPES.register("aspect_orb"',
    ".sized(0.125F, 0.125F)",
    ".updateInterval(1)",
):
    require(mod, token, "ThaumcraftMod")

client = read("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java")
require(client, "EntityRenderers.register(ThaumcraftMod.ASPECT_ORB.get(), AspectOrbRenderer::new)", "ClientModEvents")

for workflow_name in (".github/workflows/build.yml", ".github/workflows/release.yml"):
    workflow = read(workflow_name)
    require(workflow, "python3 tools/tc4_116248_node_orb_guard.py", workflow_name)
    if "THAUMCRAFT_V11_62_48_FULL_REPORT.md" not in workflow and "THAUMCRAFT_V11_62_49_FULL_REPORT.md" not in workflow:
        ERRORS.append(f"{workflow_name}: missing consolidated release report")

if ERRORS:
    print("v11.62.48 node/orb guard: FAIL")
    for error in ERRORS:
        print(" -", error)
    raise SystemExit(1)

print(
    "v11.62.48 node/orb guard: OK "
    "(local per-node scheduler, TC4 reload phase, unstable-node Aspect Orb emission, "
    "hotbar wand pickup, TC4 particle-sheet renderer)"
)
