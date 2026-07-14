#!/usr/bin/env python3
"""Regression guard for v11.62.51 research-tree, scan reward and node-tag parity fixes."""
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
        ERRORS.append(f"{label}: forbidden {token!r}")


def version_tuple(value: str) -> tuple[int, ...]:
    return tuple(int(part) for part in value.split("."))


for label, text, pattern in (
    ("build.gradle", read("build.gradle"), r"^version\s*=\s*'([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?'"),
    ("mods.toml", read("src/main/resources/META-INF/mods.toml"), r'^version="([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?"'),
):
    match = re.search(pattern, text, re.MULTILINE)
    if match is None or version_tuple(match.group(1)) < (11, 62, 51):
        ERRORS.append(f"{label}: expected v11.62.51 or later")

foundation = read("src/main/java/com/darkifov/thaumcraft/research/ResearchTableFoundation.java")
require(foundation, "PlayerAspectKnowledge.seedPrimals(player)", "research-table starter pool")
require(foundation, "FOUNDATION_POOL_SEEDED", "research-table save compatibility")
forbid(foundation, "PlayerAspectKnowledge.addPool(player, aspect, 3)", "research-table duplicate starter grant")

thaumometer = read("src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java")
for token in (
    "canUnderstandScan(player, aspects)",
    "firstPlayerScan ? absorbScannedAspects(player, aspects) : 0",
    "AspectList scanAspects = TC4AuraNodeScanParity.scanRewardAspects(node)",
    "firstNodeScan ? absorbScannedAspects(player, scanAspects) : 0",
    "!aspect.isPrimal() && !ResearchTableFoundation.componentsKnown(player, aspect)",
    "boolean discoveredNow = PlayerAspectKnowledge.discover(player, aspect)",
    "int reward = Math.max(0, entry.getValue()) + (discoveredNow ? 2 : 0)",
    "PlayerAspectKnowledge.addPool(player, aspect, reward)",
):
    require(thaumometer, token, "Thaumometer scan rewards")
forbid(thaumometer, "int reward = 1 + (discoveredNow ? 2 : 0)", "Thaumometer must use original @-scan magnitude")

node_scan = read("src/main/java/com/darkifov/thaumcraft/aura/TC4AuraNodeScanParity.java")
for token in (
    "Math.max(4, entry.getValue() / 10)",
    "case UNSTABLE -> rewards.add(Aspect.PERDITIO, 4)",
    "case HUNGRY -> rewards.add(Aspect.FAMES, 4)",
    "case TAINTED -> rewards.add(Aspect.VITIUM, 4)",
    "case PURE -> rewards.add(Aspect.SANO, 2).add(Aspect.ORDO, 2)",
    "case DARK -> rewards.add(Aspect.MORTUUS, 2).add(Aspect.TENEBRAE, 2)",
):
    require(node_scan, token, "TC4 node scan reward parity")

layout = read("src/main/java/com/darkifov/thaumcraft/client/screen/OriginalResearchLayout.java")
require(layout, "static boolean special(ResearchEntry entry)", "research flags")
require(layout, "TC4ResearchFlagPolicy.SPECIAL", "research special frame")
require(layout, "static boolean forbidden(ResearchEntry entry)", "research warp aura")
require(layout, "return entry.warp() > 0", "research warp aura")

screen = read("src/main/java/com/darkifov/thaumcraft/client/screen/ThaumonomiconScreen.java")
for token in (
    "bgU / 2, bgV / 2, TC4_BACKGROUND_SOURCE_WIDTH, TC4_BACKGROUND_SOURCE_HEIGHT",
    "TC4_BACKGROUND_DEST_WIDTH, TC4_BACKGROUND_DEST_HEIGHT, 512, 512",
    "Set<String> renderedSiblingLinks = new HashSet<>()",
    "containsKey(sibling.requirements(), entry.key())",
    "renderForbiddenWarp(poseStack, x + 11, y + 11)",
    "frame * TC4_NODE_CELL, TC4_FORBIDDEN_STRIP * TC4_NODE_CELL",
    "TC4_FORBIDDEN_COLOR = 0x440055",
    "TC4_FORBIDDEN_ALPHA = 0.66F",
    "travel - 1",
    "increments * 1.5F",
):
    require(screen, token, "Thaumonomicon browser parity")
forbid(screen, "return TC4ResearchFlagPolicy.has(entry, TC4ResearchFlagPolicy.SPECIAL) || entry.warp() > 0", "special/warp conflation")

textures = read("src/main/java/com/darkifov/thaumcraft/client/screen/OriginalGuiTextures.java")
require(textures, "int u, int v, int sourceWidth, int sourceHeight", "scaled tinted region helper")
require(textures, "blitOriginalScaledRegion(poseStack, x, y, texture, u, v, sourceWidth, sourceHeight", "scaled tinted region helper")

node_tags = read("src/main/java/com/darkifov/thaumcraft/client/HelmetRevealingOverlayEvents.java")
for token in (
    "MAX_TAG_SCALE = 0.30F",
    "UNKNOWN_ASPECT",
    "updateTagScale(node)",
    "ClientAspectData.knows(stack.aspect())",
    "Math.atan2(viewerDx, viewerDz)",
    "Vector3f.YP.rotationDegrees(viewerYaw + 180.0F)",
    "Vector3f.ZP.rotationDegrees(180.0F)",
    "TC4_STEADY_TAG_SCALE = 0.26F",
    "Math.pow(TC4_RETAINED_SCALE_PER_FRAME, referenceFrames)",
    "tagScale -= TC4_FADE_PER_FRAME * referenceFrames",
):
    require(node_tags, token, "TC4 world aspect tags")

for workflow_name in (".github/workflows/build.yml", ".github/workflows/release.yml"):
    workflow = read(workflow_name)
    require(workflow, "python3 tools/tc4_116251_research_scan_hud_guard.py", workflow_name)
    require(workflow, "reports/*11.62.74*.json", workflow_name)
    if re.search(r"THAUMCRAFT_LEGACY_REBUILD_V11_62_[0-9]+_EXPERT_FULL_TECHNICAL_REPORT_R[0-9]+\.md", workflow):
        ERRORS.append(f"{workflow_name}: historical report must not be required by clean CI")

if ERRORS:
    print("v11.62.51 research/scan/HUD guard: FAIL")
    for error in ERRORS:
        print(" -", error)
    raise SystemExit(1)

print("v11.62.51 research/scan/HUD guard: OK (single starter pool, TC4 @-scan rewards, map sampling, warp aura, sibling links and animated node tags)")
