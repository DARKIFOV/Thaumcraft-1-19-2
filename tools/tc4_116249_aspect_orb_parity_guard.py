#!/usr/bin/env python3
"""Regression guard for v11.62.49 Aspect Orb renderer and entity parity."""
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
    ("build.gradle", build, r"^version\s*=\s*'([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?'"),
    ("mods.toml", mods, r'^version="([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?"'),
):
    match = re.search(pattern, text, re.MULTILINE)
    if match is None or version_tuple(match.group(1)) < (11, 62, 49):
        ERRORS.append(f"{label}: expected v11.62.49 or later")

renderer = read("src/main/java/com/darkifov/thaumcraft/client/render/AspectOrbRenderer.java")
for token in (
    "float remaining = (float) (AspectOrbEntity.MAX_AGE - orb.orbAge())",
    "0.1F + 0.3F * remaining / (float) AspectOrbEntity.MAX_AGE",
    "int blockLightCoordinate = packedLight & 0xFFFF",
    "int skyLightCoordinate = packedLight & 0xFFFF0000",
    "Math.min(240, blockLightCoordinate + 120) | skyLightCoordinate",
    "TC4NodeRenderTypes.node(PARTICLES, !aspect.usesAlphaBlend(), false)",
):
    require(renderer, token, "AspectOrbRenderer")
for token in (
    "((AspectOrbEntity.MAX_AGE - orb.orbAge()) / AspectOrbEntity.MAX_AGE)",
    "ambientBlock + 8",
    "LightTexture.pack",
    "TC4NodeRenderTypes.node(PARTICLES, true, false)",
):
    forbid(renderer, token, "AspectOrbRenderer")

orb = read("src/main/java/com/darkifov/thaumcraft/entity/AspectOrbEntity.java")
for token in (
    "private int orbCooldown",
    "if (orbCooldown > 0)",
    "player.takeXpDelay > 0",
    "player.takeXpDelay = 2",
    "if (isInLava())",
    "moveTowardsClosestSpace(getX(), (bounds.minY + bounds.maxY) * 0.5D, getZ())",
    "if (orbAge >= MAX_AGE)",
):
    require(orb, token, "AspectOrbEntity")
for token in (
    "if (isInWater())",
    'tag.putInt("Cooldown"',
    'tag.getInt("Cooldown")',
):
    forbid(orb, token, "AspectOrbEntity")

aspect = read("src/main/java/com/darkifov/thaumcraft/Aspect.java")
for token in (
    "return this == PERDITIO || this == VACUOS",
    "public boolean usesAlphaBlend()",
):
    require(aspect, token, "Aspect blend contract")

for workflow_name in (".github/workflows/build.yml", ".github/workflows/release.yml"):
    workflow = read(workflow_name)
    require(workflow, "python3 tools/tc4_116249_aspect_orb_parity_guard.py", workflow_name)
    require(workflow, "reports/*11.62.74*.json", workflow_name)
    if re.search(r"THAUMCRAFT_LEGACY_REBUILD_V11_62_[0-9]+_EXPERT_FULL_TECHNICAL_REPORT_R[0-9]+\.md", workflow):
        ERRORS.append(f"{workflow_name}: historical report must not be required by clean CI")

if ERRORS:
    print("v11.62.49 Aspect Orb parity guard: FAIL")
    for error in ERRORS:
        print(" -", error)
    raise SystemExit(1)

print(
    "v11.62.49 Aspect Orb parity guard: OK "
    "(smooth lifetime scale, exact +120 raw lightmap, lava physics, "
    "dual TC4 pickup gates and aspect-specific blending)"
)
