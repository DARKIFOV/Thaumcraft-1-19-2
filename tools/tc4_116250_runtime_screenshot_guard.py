#!/usr/bin/env python3
"""Regression guard for v11.62.50 fixes derived from the first real client screenshots."""
from __future__ import annotations

import json
import re
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


def forbid(text: str, token: str, label: str) -> None:
    if token in text:
        ERRORS.append(f"{label}: forbidden {token!r}")


def load_json(rel: str):
    try:
        return json.loads(read(rel))
    except Exception as exc:
        ERRORS.append(f"{rel}: invalid JSON: {exc}")
        return {}


def version_tuple(value: str) -> tuple[int, ...]:
    return tuple(int(x) for x in value.split("."))


for label, text, pattern in (
    ("build.gradle", read("build.gradle"), r"^version\s*=\s*'([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?'"),
    ("mods.toml", read("src/main/resources/META-INF/mods.toml"), r'^version="([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?"'),
):
    match = re.search(pattern, text, re.MULTILINE)
    if match is None or version_tuple(match.group(1)) < (11, 62, 50):
        ERRORS.append(f"{label}: expected v11.62.50 or later")

# Exact TC4 ModelTable/ModelArcaneWorkbench geometry and source atlases.
table = load_json("src/main/resources/assets/thaumcraft/models/block/table.json")
if table.get("textures", {}).get("atlas") != "thaumcraft:models/table":
    ERRORS.append("table model: expected original models/table atlas")
if len(table.get("elements", [])) != 4:
    ERRORS.append("table model: expected top, two legs and crossbar")
if [e.get("from") for e in table.get("elements", [])] != [[0, 12, 0], [10, 4, 6], [2, 4, 6], [0, 0, 4]]:
    ERRORS.append("table model: TC4 geometry drift")

workbench = load_json("src/main/resources/assets/thaumcraft/models/block/arcane_workbench.json")
workbench_textures = workbench.get("textures", {})
if workbench_textures.get("worktable") != "thaumcraft:original/thaumcraft4/models/worktable":
    ERRORS.append("arcane workbench: expected the original TC4 models/worktable atlas")
if len(workbench.get("elements", [])) != 6:
    ERRORS.append("arcane workbench: expected original tabletop, four legs and front ornament")
el0 = workbench.get("elements", [{}])[0] if workbench.get("elements") else {}
if el0.get("faces", {}).get("up", {}).get("texture") != "#worktable":
    ERRORS.append("arcane workbench: tabletop upper face must use the original #worktable atlas")

matrix_model = read("src/main/java/com/darkifov/thaumcraft/client/render/model/TC4InfusionMatrixModel.java")
for token in ("texOffs(0, 0)", "texOffs(0, 32)", "LayerDefinition.create(mesh, 64, 64)"):
    require(matrix_model, token, "TC4InfusionMatrixModel")
client_events = read("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java")
require(client_events, "registerLayerDefinition(TC4InfusionMatrixModel.LAYER", "ClientModEvents")

node_overlay = read("src/main/java/com/darkifov/thaumcraft/client/HelmetRevealingOverlayEvents.java")
for token in ("RenderLevelStageEvent", "AFTER_TRANSLUCENT_BLOCKS", "targetedNode(minecraft)"):
    require(node_overlay, token, "node goggles overlay")
if ("event.getCamera().rotation()" not in node_overlay
        and "event.getCamera().getYRot()" not in node_overlay
        and "Math.atan2(viewerDx, viewerDz)" not in node_overlay):
    ERRORS.append("node goggles overlay: missing camera-facing rotation")
forbid(node_overlay, 'textures/original/thaumcraft4/gui/hud.png', "node goggles overlay")

wand_renderer = read("src/main/java/com/darkifov/thaumcraft/client/render/WandItemRenderer.java")
for token in ("transformType.firstPerson()", "translate(0.50D, 1.50D, 0.50D)", "scale(1.00F, 1.10F, 1.00F)", "Vector3f.XP.rotationDegrees(180.0F)"):
    require(wand_renderer, token, "WandItemRenderer")
for token in ("MODEL_CENTER_Y", "left ? -0.11D : 0.11D"):
    forbid(wand_renderer, token, "WandItemRenderer")
node_renderer = read("src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java")
require(node_renderer, "boolean holdingWand", "AuraNodeRenderer drain beam")
forbid(node_renderer, "!player.isUsingItem()", "AuraNodeRenderer drain beam")

wand = read("src/main/java/com/darkifov/thaumcraft/block/WandItem.java")
require(wand, "private static final int NODE_TAP_INTERVAL = 5", "original node tapping rate")
require(wand, "player.getTicksUsingItem() % NODE_TAP_INTERVAL == 0", "original node tapping rate")

knowledge = read("src/main/java/com/darkifov/thaumcraft/research/PlayerAspectKnowledge.java")
for token in ("STARTER_PRIMAL_AMOUNT = 10", "StarterPrimalsSeeded", "STARTER_PRIMAL_AMOUNT - pool.get(aspect)"):
    require(knowledge, token, "starter primal aspects")
common = read("src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java")
require(common, "PlayerAspectKnowledge.copyFrom(oldPlayer, player)", "death clone persistence")

thaumometer = read("src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java")
require(thaumometer, "pendingTargetStillValid", "Thaumometer stable server scan")
packet = read("src/main/java/com/darkifov/thaumcraft/network/RequestThaumometerScanPacket.java")
forbid(packet, "TC4ThaumometerTargeting.target(player", "Thaumometer packet must not repeat exact server ray")

book = read("src/main/java/com/darkifov/thaumcraft/client/screen/TC4ResearchPageScreen.java")
for token in ("PAGE_WIDTH = 139", "renderPage(poseStack, leftPos, topPos", "renderPage(poseStack, leftPos + 152, topPos", "renderCompoundBlueprint", "resolveLegacyStack(expression)"):
    require(book, token, "TC4ResearchPageScreen")
for token in ("leftPos - 15", "leftPos + 137"):
    forbid(book, token, "TC4ResearchPageScreen shifted legacy columns")
bridge = read("src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeRuntimeBridge.java")
for token in ('"3", "4", "3"', '"IGNIS:70"', '"PERDITIO:70"'):
    require(bridge, token, "NodeJar compound blueprint")
resolver = read("src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java")
if resolver.find("resolveFunctionalBlockStack(compact)") > resolver.find("resolveLegacyExpression(expression)"):
    ERRORS.append("recipe item resolver: functional blocks must be resolved before generic aliases")

for workflow_name in (".github/workflows/build.yml", ".github/workflows/release.yml"):
    workflow = read(workflow_name)
    require(workflow, "python3 tools/tc4_116250_runtime_screenshot_guard.py", workflow_name)
    require(workflow, "reports/*11.62.74*.json", workflow_name)
    if re.search(r"THAUMCRAFT_LEGACY_REBUILD_V11_62_[0-9]+_EXPERT_FULL_TECHNICAL_REPORT_R[0-9]+\.md", workflow):
        ERRORS.append(f"{workflow_name}: historical report must not be required by clean CI")

if ERRORS:
    print("v11.62.50 runtime screenshot guard: FAIL")
    for error in ERRORS:
        print(" -", error)
    raise SystemExit(1)
print("v11.62.50 runtime screenshot guard: OK (node HUD/beam, first-person wand, TC4 tables/matrix, starter aspects, Thaumometer and book layout)")
