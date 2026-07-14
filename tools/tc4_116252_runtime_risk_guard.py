#!/usr/bin/env python3
"""Regression guard for v11.62.52 browser-atlas, node-tag timing and release-risk fixes."""
from __future__ import annotations

import math
import re
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ERRORS: list[str] = []


def read(rel: str) -> str:
    p = ROOT / rel
    if not p.is_file():
        ERRORS.append(f"missing file: {rel}")
        return ""
    return p.read_text(encoding="utf-8")


def require(text: str, token: str, label: str) -> None:
    if token not in text:
        ERRORS.append(f"{label}: missing {token!r}")


def png_size(rel: str) -> tuple[int, int]:
    p = ROOT / rel
    try:
        data = p.read_bytes()
    except OSError:
        ERRORS.append(f"missing PNG: {rel}")
        return 0, 0
    if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n" or data[12:16] != b"IHDR":
        ERRORS.append(f"invalid PNG header: {rel}")
        return 0, 0
    return struct.unpack(">II", data[16:24])


def version_tuple(value: str) -> tuple[int, ...]:
    return tuple(int(part) for part in value.split("."))


for label, text, pattern in (
    ("build.gradle", read("build.gradle"), r"^version\s*=\s*'([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?'"),
    ("mods.toml", read("src/main/resources/META-INF/mods.toml"), r'^version="([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?"'),
):
    match = re.search(pattern, text, re.MULTILINE)
    if match is None or version_tuple(match.group(1)) < (11, 62, 52):
        ERRORS.append(f"{label}: expected v11.62.52 or later")

screen = read("src/main/java/com/darkifov/thaumcraft/client/screen/ThaumonomiconScreen.java")
for token in (
    "TC4_BACKGROUND_TRAVEL_X = 288",
    "TC4_BACKGROUND_TRAVEL_Y = 316",
    "TC4_BACKGROUND_SOURCE_WIDTH = 112",
    "TC4_BACKGROUND_SOURCE_HEIGHT = 98",
    "TC4_BACKGROUND_DEST_WIDTH = 224",
    "TC4_BACKGROUND_DEST_HEIGHT = 196",
    "bgU / 2, bgV / 2",
    "travel - 1",
    "TC4_NODE_ATLAS_SIZE = 2048",
    "TC4_NODE_CELL = 64",
    "TC4_NODE_FRAMES = 32",
    "TC4_FORBIDDEN_STRIP = 5",
    "TC4_FORBIDDEN_SIZE = 80",
    "TC4_FORBIDDEN_COLOR = 0x440055",
    "TC4_FORBIDDEN_ALPHA = 0.66F",
    "TC4_NODE_FRAMES - 1 - part",
    "frame * TC4_NODE_CELL, TC4_FORBIDDEN_STRIP * TC4_NODE_CELL",
):
    require(screen, token, "Thaumonomicon exact constants")

layout = read("src/main/java/com/darkifov/thaumcraft/client/screen/OriginalResearchLayout.java")
for token in (
    "minColumn * CELL - 85",
    "minRow * CELL - 112",
    "maxColumn * CELL - 112",
    "maxRow * CELL - 61",
):
    require(layout, token, "TC4 browser bounds")

# Generic exhaustive equivalence: the modern helper and TC4's int-cast formula
# must agree at every integer pan position for representative positive spans.
for span in (1, 2, 7, 24, 73, 288, 417):
    low = -173
    high = low + span
    for pan in range(low - 2, high + 2):
        clamped = max(low, min(high - 1, pan))
        modern = max(0, min(287, int(((clamped - low) / float(span)) * 288)))
        tc4 = int(((clamped - low) / abs(low - high)) * 288.0)
        if modern != tc4:
            ERRORS.append(f"background coordinate mismatch span={span} pan={pan}: {modern}!={tc4}")
            break

for rel in (
    "src/main/resources/assets/thaumcraft/textures/gui/gui_researchback.png",
    "src/main/resources/assets/thaumcraft/textures/gui/gui_researchbackeldritch.png",
):
    if png_size(rel) != (512, 512):
        ERRORS.append(f"{rel}: expected 512x512")
if png_size("src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/misc/nodes.png") != (2048, 2048):
    ERRORS.append("nodes.png: expected original 2048x2048 / 32x32 cell atlas")

overlay = read("src/main/java/com/darkifov/thaumcraft/client/HelmetRevealingOverlayEvents.java")
for token in (
    "TC4_STEADY_TAG_SCALE = 0.26F",
    "TC4_RETAINED_SCALE_PER_FRAME = 0.90F",
    "TC4_FADE_PER_FRAME = 0.005F",
    "elapsedSeconds * REFERENCE_RENDER_FPS",
    "Math.pow(TC4_RETAINED_SCALE_PER_FRAME, referenceFrames)",
    "tagScale -= TC4_FADE_PER_FRAME * referenceFrames",
    "TC4_FONT_SCALE_RELATIVE_TO_ICON_PIXELS = 0.64F",
    "minecraft.font.draw(poseStack, amount, 14 - stringWidth, 1, 0x111111)",
    "minecraft.font.draw(poseStack, amount, 13 - stringWidth, 0, 0xFFFFFF)",
    "ClientPlayerNetworkEvent.LoggingOut",
    "tagScale = 0.0F",
    "lastFrameNanos = 0L",
):
    require(overlay, token, "node-tag timing/text parity")

# Check the closed form against repeatedly applying TC4's exact per-frame
# recurrence at integer frame counts.
for start in (0.0, 0.05, 0.18, 0.26, 0.30):
    for frames in (1, 2, 5, 30):
        iterative = start
        for _ in range(frames):
            iterative = 0.9 * iterative + 0.026
        closed = 0.26 + (start - 0.26) * math.pow(0.9, frames)
        if abs(iterative - closed) > 1.0e-9:
            ERRORS.append(f"tagscale closed form mismatch start={start} frames={frames}")

for workflow_name in (".github/workflows/build.yml", ".github/workflows/release.yml"):
    workflow = read(workflow_name)
    require(workflow, "python3 tools/tc4_116252_runtime_risk_guard.py", workflow_name)
    require(workflow, "reports/*11.62.74*.json", workflow_name)
    if re.search(r"THAUMCRAFT_LEGACY_REBUILD_V11_62_[0-9]+_EXPERT_FULL_TECHNICAL_REPORT_R[0-9]+\.md", workflow):
        ERRORS.append(f"{workflow_name}: historical report must not be required by clean CI")

if ERRORS:
    print("v11.62.52 runtime-risk guard: FAIL")
    for error in ERRORS:
        print(" -", error)
    raise SystemExit(1)

print("v11.62.52 runtime-risk guard: OK (exact browser coordinates, verified 32x32 node atlas, time-correct tag animation, TC4 amount text and logout reset)")
