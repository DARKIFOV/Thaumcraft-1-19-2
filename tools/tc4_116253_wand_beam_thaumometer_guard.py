#!/usr/bin/env python3
"""Regression guard for v11.62.53 wand-drain beam and Thaumometer lighting parity."""
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


def reject(text: str, token: str, label: str) -> None:
    if token in text:
        ERRORS.append(f"{label}: obsolete token still present {token!r}")


def version_tuple(value: str) -> tuple[int, ...]:
    return tuple(int(part) for part in value.split("."))


for label, text, pattern in (
    ("build.gradle", read("build.gradle"), r"^version\s*=\s*'([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?'"),
    ("mods.toml", read("src/main/resources/META-INF/mods.toml"), r'^version="([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?"'),
):
    match = re.search(pattern, text, re.MULTILINE)
    if match is None or version_tuple(match.group(1)) < (11, 62, 53):
        ERRORS.append(f"{label}: expected v11.62.53 or later")

renderer = read("src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java")
for token in (
    "TC4_LINK_QUALITY = 16",
    "TC4_BEAM_WIDTH = 0.15F",
    "TC4_BEAM_SPEED = -0.02F",
    "new Vec3(-0.1D, -0.1D, 0.5D)",
    ".xRot(-pitch * ((float)Math.PI / 180.0F))",
    ".yRot(-yaw * ((float)Math.PI / 180.0F))",
    ".yRot(-useWave * 0.01F)",
    ".xRot(-useWave * 0.015F)",
    "Math.min(useTicks, 10) / 10.0F",
    "System.nanoTime() / 30_000_000L",
    "blocks * (TC4_LINK_QUALITY / 2.0F)",
    "startWorld.z % 16.0D",
    "startWorld.x % 16.0D",
    "startWorld.y % 16.0D",
    "renderOriginalFloatyLine",
    "current = (target + current*4) / 5",
    "blendColorOneTick",
    "VertexConsumerHelper.beamQuad",
):
    require(renderer, token, "AuraNodeRenderer exact beam")
for token in ("int segments = 12", "float width = 0.026F", "0.78F"):
    reject(renderer, token, "AuraNodeRenderer approximate beam")

node = read("src/main/java/com/darkifov/thaumcraft/blockentity/AuraNodeBlockEntity.java")
for token in (
    "public void clearWandDrain(Player drainer)",
    "lastDrainerEntityId = -1",
    "lastDrainGameTime = Long.MIN_VALUE / 4L",
):
    require(node, token, "AuraNodeBlockEntity drain lifecycle")

wand = read("src/main/java/com/darkifov/thaumcraft/block/WandItem.java")
for token in (
    "node.clearWandDrain(player)",
    "clearNodeDrain(player, wandStack)",
    "stopNodeUse(player, stack)",
):
    require(wand, token, "WandItem drain lifecycle")
for token in ("DustParticleOptions", "emitNodeTapFx", "sendParticles(dust"):
    reject(wand, token, "WandItem invented drain FX")

thaumometer = read("src/main/java/com/darkifov/thaumcraft/client/render/ThaumometerItemRenderer.java")
for token in (
    "int bodyLight = packedLight",
    "int screenLight = originalScannerScreenLight(packedLight)",
    "originalCoordinate = (int)(190.0F + Mth.sin(ticks - jitter) * 10.0F + 10.0F)",
    "int ambientBlock = (packedLight >> 4) & 15",
    "int ambientSky = (packedLight >> 20) & 15",
    "Math.round(originalCoordinate / 16.0F)",
    ".color(255,255,255,255)",
):
    require(thaumometer, token, "Thaumometer lighting")
for token in ("Math.max(packedLight, 15728880)", "210 * pulse"):
    reject(thaumometer, token, "Thaumometer obsolete fullbright")

for workflow_name in (".github/workflows/build.yml", ".github/workflows/release.yml"):
    workflow = read(workflow_name)
    require(workflow, "python3 tools/tc4_116253_wand_beam_thaumometer_guard.py", workflow_name)
    if not re.search(r"reports/\*11\.[0-9]+\.[0-9]+\*\.json", workflow):
        ERRORS.append(f"{workflow_name}: missing current-version generated-report glob")
    if re.search(r"THAUMCRAFT_LEGACY_REBUILD_V11_62_[0-9]+_EXPERT_FULL_TECHNICAL_REPORT_R[0-9]+\.md", workflow):
        ERRORS.append(f"{workflow_name}: historical report must not be required by clean CI")

if ERRORS:
    print("v11.62.53 wand/beam/Thaumometer guard: FAIL")
    for error in ERRORS:
        print(" -", error)
    raise SystemExit(1)

print("v11.62.53 wand/beam/Thaumometer guard: OK (exact TC4 floaty line, drain lifecycle, ambient scanner body and original glass lightmap)")
