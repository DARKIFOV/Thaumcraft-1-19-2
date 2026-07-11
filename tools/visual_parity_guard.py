#!/usr/bin/env python3
"""Regression checks for the v11.62.36 TC4 visual restoration paths."""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []


def require(condition: bool, message: str) -> None:
    if not condition:
        errors.append(message)


resources = ROOT / "src/main/resources/assets/thaumcraft"
required_files = [
    resources / "textures/original/thaumcraft4/models/node_stabilizer.png",
    resources / "textures/original/thaumcraft4/models/node_stabilizer_over.png",
    resources / "textures/original/thaumcraft4/models/node_stabilizer.obj",
    resources / "textures/original/thaumcraft4/misc/node_bubble.png",
    resources / "textures/original/thaumcraft4/misc/nodes.png",
    resources / "textures/gui/gui_research.png",
    resources / "textures/original/thaumcraft4/misc/parchment3.png",
    resources / "textures/gui/thaumcraft_core_original/hex1.png",
    resources / "textures/gui/thaumcraft_core_original/hex2.png",
]
for path in required_files:
    require(path.is_file(), f"missing original TC4 visual resource: {path.relative_to(ROOT)}")

for item_id in ("node_stabilizer", "advanced_node_stabilizer"):
    model_path = resources / f"models/item/{item_id}.json"
    try:
        model = json.loads(model_path.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"invalid stabilizer item model {model_path.relative_to(ROOT)}: {exc}")
        continue
    require(model.get("parent") == "builtin/entity",
            f"{item_id} must use builtin/entity so its original OBJ item renderer runs")
    require("arcane_workbench_side" not in model_path.read_text(encoding="utf-8"),
            f"{item_id} still references the old Arcane Workbench placeholder texture")

stabilizer_renderer = (ROOT / "src/main/java/com/darkifov/thaumcraft/client/render/NodeStabilizerRenderer.java").read_text(encoding="utf-8")
require("TC4NodeStabilizerModel.LOCK_TRIANGLES" in stabilizer_renderer,
        "world stabilizer renderer is not using the embedded original lock geometry")
require("TC4NodeStabilizerModel.PISTON_TRIANGLES" in stabilizer_renderer,
        "world stabilizer renderer is not using the embedded original piston geometry")
require("renderStandalone" in stabilizer_renderer,
        "world/item stabilizer renderer no longer shares the original geometry path")

item_renderer = (ROOT / "src/main/java/com/darkifov/thaumcraft/client/render/NodeStabilizerItemRenderer.java").read_text(encoding="utf-8")
require("NodeStabilizerRenderer.renderStandalone" in item_renderer,
        "stabilizer item renderer does not use the original world geometry")

node_renderer = (ROOT / "src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java").read_text(encoding="utf-8")
require("TC4AuraNodeHudParity.ORIGINAL_NODES" in node_renderer,
        "aura node renderer is not sampling the original TC4 node atlas")
require("LightTexture.FULL_BRIGHT" in node_renderer,
        "aura node renderer lost its original full-bright translucent path")


research_screen = (ROOT / "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java").read_text(encoding="utf-8")
require("blitTc4ResearchParchment" in research_screen,
        "Main Research Table screen is missing TC4 parchment3 draw at x+94,y+8")
require("blitOriginalScaledTintedAlpha" in research_screen and "HEX1, 32, 32" in research_screen,
        "Integrated Research Table hexes are not sampling the complete 32x32 source textures")
require("0x0099CC" in research_screen and "drawTc4Connection" in research_screen,
        "Integrated Research Table links no longer use the original cyan TC4 line path")
require("0x22FFCC55" not in research_screen and "0x2244AA44" not in research_screen,
        "Integrated Research Table still contains modern rectangular debug overlays")
require("buildConnectionView" in research_screen and "ConnectionView" in research_screen,
        "Integrated research links are no longer anchored to the recursively connected TC4 solution path")
require("UNKNOWN_ASPECT" in research_screen and "ASPECT_BACK" in research_screen,
        "Integrated Research Table lost unknown-aspect and fixed-anchor orb rendering")
require("ACTION_SYNC_NOTE" in research_screen and "ACTION_OPEN_NOTE" not in research_screen,
        "Main Research Table still opens the rebuild-only second puzzle window")
require("requestPlaceResearchNoteAspectFromClient" in research_screen
        and "requestClearResearchNoteSlotFromClient" in research_screen,
        "Integrated parchment no longer supports original drag/place/clear interactions")


thaumometer_renderer = (ROOT / "src/main/java/com/darkifov/thaumcraft/client/render/ThaumometerItemRenderer.java").read_text(encoding="utf-8")
require("renderOriginalScanReadout" in thaumometer_renderer,
        "Thaumometer no longer renders scan data on the original scanner glass")
require("TC4ThaumometerTargeting.find" in thaumometer_renderer
        and "drawAspectOnScanner" in thaumometer_renderer
        and "ClientScanData" in thaumometer_renderer,
        "Thaumometer scanner readout is not driven by shared target and per-player scan data")
require("drawScannerTitle" in thaumometer_renderer and "width > 90" in thaumometer_renderer,
        "Thaumometer lost the original long-name scale-down rule")

scanner_targeting = (ROOT / "src/main/java/com/darkifov/thaumcraft/aura/TC4ThaumometerTargeting.java").read_text(encoding="utf-8")
require("nearestBlockDistance" in scanner_targeting and "Kind.ITEM" in scanner_targeting,
        "Shared scanner ray no longer blocks through-wall entities or dropped-item scans")
scanner_item = (ROOT / "src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java").read_text(encoding="utf-8")
require("elapsed >= REQUIRED_STABLE_TICKS" in scanner_item and "pendingMatches" in scanner_item,
        "Thaumometer no longer requires the original twenty stable scan ticks")
require("syncScanKnowledge" in scanner_item and "ItemEntity" in scanner_item,
        "Thaumometer scan completion no longer synchronizes player knowledge or dropped items")
common_events = (ROOT / "src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java").read_text(encoding="utf-8")
require("onThaumometerRightClickBlock" in common_events and "setUseBlock(Event.Result.DENY)" in common_events,
        "Interactive blocks can consume right-click before the Thaumometer scan")
require("onThaumometerEntityInteractSpecific" in common_events
        and "onThaumometerEntityInteract" in common_events
        and "setCancellationResult(InteractionResult.SUCCESS)" in common_events,
        "Interactive entities can replace the Thaumometer scan with their normal action")
scan_packet = ROOT / "src/main/java/com/darkifov/thaumcraft/network/ScanKnowledgeSyncPacket.java"
client_scan = ROOT / "src/main/java/com/darkifov/thaumcraft/client/ClientScanData.java"
require(scan_packet.is_file() and client_scan.is_file(),
        "Per-player scanner knowledge client mirror is missing")

arcane_screen = (ROOT / "src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchContainerScreen.java").read_text(encoding="utf-8")
require("TC4ArcaneWorkbenchParity.ASPECT_LOCS" in arcane_screen
        and "RenderSystem.defaultBlendFunc()" in arcane_screen,
        "Arcane Workbench GUI lost shared original coordinates or blended 190x234 background")

overlay_events = (ROOT / "src/main/java/com/darkifov/thaumcraft/client/HelmetRevealingOverlayEvents.java").read_text(encoding="utf-8")
require("hasIngamePopupRevealer" in overlay_events and "isRevealer(player)" not in overlay_events,
        "Thaumometer still duplicates its scanner readout as the external goggles HUD")

gui_helper = (ROOT / "src/main/java/com/darkifov/thaumcraft/client/screen/OriginalGuiTextures.java").read_text(encoding="utf-8")
require("blitTc4ResearchTableBackground" in gui_helper,
        "Research Table no longer uses the split original 255x167 + inventory-strip background")
require("blitTc4ResearchParchment" in gui_helper and "150, 150, 256, 256" in gui_helper,
        "Research parchment is not sampling TC4's original 150x150 region")
require("blitOriginalScaledTintedAlpha" in gui_helper,
        "GUI helper lost source-size-aware scaled alpha blitting")

require("isWithinThaumometerViewCone" in node_renderer and ">= 0.44D" in node_renderer,
        "Thaumometer node reveal no longer follows the original 0.44 view cone")
require("usesAlphaBlend()" in node_renderer and "typeAngle" in node_renderer,
        "Aura node aspect blend/type rotation parity is missing")
require("TC4NodeRenderTypes.node" in node_renderer
        and "usesAdditiveTypeBlend" in node_renderer,
        "Aura node layers lost TC4 additive-vs-alpha blend separation")
require("TC4NodeRenderTypes.node(TC4AuraNodeHudParity.ORIGINAL_WISPY, true, false)" in node_renderer,
        "Wand drain beam is no longer rendered with the original additive glow")

aspect_source = (ROOT / "src/main/java/com/darkifov/thaumcraft/Aspect.java").read_text(encoding="utf-8")
require("this == PERDITIO || this == VACUOS" in aspect_source,
        "TC4 aspect blend parity must keep Entropy/Void on blend 771")

require("overlayAlpha" in stabilizer_renderer and "170.0F * extension * pulse" in stabilizer_renderer,
        "Node stabilizer overlay is still forced opaque instead of following piston extension")

registry_guard = next((ROOT / "src/main/java").rglob("TC4RegistryGarbageGuard.java")).read_text(encoding="utf-8")
require('id.startsWith("tc4_")' in registry_guard and "return true;" in registry_guard,
        "tc4_* compatibility aliases are no longer unconditionally hidden")
require("filterCreativeItems" in registry_guard,
        "creative-tab exact duplicate filter is missing")
require('"aura_node"' in registry_guard,
        "non-original aura-node debug block item is visible in the creative tab")

for item_id in ("iron_capped_wooden_wand", "greatwood_wand", "silverwood_wand"):
    model_path = resources / f"models/item/{item_id}.json"
    model = json.loads(model_path.read_text(encoding="utf-8"))
    require(model.get("parent") == "minecraft:builtin/entity",
            f"{item_id} must stay on the BEWLR path")
    require("display" not in model,
            f"{item_id} still has JSON display transforms that double-transform WandItemRenderer")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("TC4 visual parity guard: OK")
