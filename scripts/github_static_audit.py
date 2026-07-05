#!/usr/bin/env python3
from __future__ import annotations

import json
import re
import struct
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/thaumcraft"
DATA = ROOT / "src/main/resources/data/thaumcraft"
JAVA_MAIN = ROOT / "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java"


def fail(message: str) -> None:
    print(f"::error::{message}")
    raise SystemExit(1)


def warn(message: str) -> None:
    print(f"::warning::{message}")


def read_png_size(path: Path) -> tuple[int, int]:
    data = path.read_bytes()
    if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("invalid PNG signature")
    if data[12:16] != b"IHDR":
        raise ValueError("missing PNG IHDR")
    width, height = struct.unpack(">II", data[16:24])
    return width, height


def json_audit() -> list[str]:
    errors: list[str] = []
    for path in ROOT.rglob("*.json"):
        try:
            json.loads(path.read_text(encoding="utf-8"))
        except Exception as exc:
            errors.append(f"{path.relative_to(ROOT)}: {exc}")
    return errors


def png_audit() -> list[str]:
    errors: list[str] = []
    for path in (ROOT / "src/main/resources").rglob("*.png"):
        rel = path.relative_to(ROOT).as_posix()
        try:
            width, height = read_png_size(path)
        except Exception as exc:
            errors.append(f"{rel}: {exc}")
            continue

        is_original_bank = "/textures/original/" in rel

        if width <= 0 or height <= 0:
            errors.append(f"{rel}: zero dimensions {width}x{height}")
        if not is_original_bank and width < 8 or not is_original_bank and height < 8:
            errors.append(f"{rel}: too small {width}x{height}")
        if width > 4096 or height > 4096:
            errors.append(f"{rel}: too large {width}x{height}")

        if not is_original_bank and ("/textures/item/" in rel or "/textures/block/" in rel) and (width % 16 != 0 or height % 16 != 0):
            errors.append(f"{rel}: item/block texture size is not multiple of 16: {width}x{height}")

    return errors


def collect_registry_ids() -> tuple[set[str], set[str]]:
    text = JAVA_MAIN.read_text(encoding="utf-8")

    item_pattern = re.compile(
        r'\b(?:item|specialItem|extrasFocus|pechToken|ttParityItem|tceParityItem)\("([a-z0-9_]+)"'
    )
    block_pattern = re.compile(
        r'\b(?:block|crucibleBlock|arcaneWorkbenchBlock|researchTableBlock|auraNodeBlock|'
        r'nodeStabilizerBlock|matrixAuxiliaryBlock|arcanePedestalBlock|infusionMatrixBlock|'
        r'thaumicEnergisticsDeviceBlock|thaumicTinkererDeviceBlock|transvectorInterfaceBlock|'
        r'etherealPlatformBlock|fumeDissipatorBlock|essentiaDriveBlock|pedestalBlock|'
        r'eldritchAltarBlock|eldritchPortalBlock|essentiaJarBlock|filteredEssentiaJarBlock|'
        r'voidEssentiaJarBlock|essentiaTubeBlock|essentiaValveBlock|alchemicalFurnaceBlock|'
        r'extrasElementBlock|ttParityBlock|tceParityBlock)\("([a-z0-9_]+)"'
    )

    return set(item_pattern.findall(text)), set(block_pattern.findall(text))


def registry_resource_audit() -> list[str]:
    errors: list[str] = []
    item_ids, block_ids = collect_registry_ids()

    for item_id in sorted(item_ids):
        if not (ASSETS / f"models/item/{item_id}.json").exists():
            errors.append(f"missing item model: {item_id}")
        if not (ASSETS / f"textures/item/{item_id}.png").exists():
            errors.append(f"missing item texture: {item_id}")

    for block_id in sorted(block_ids):
        if not (ASSETS / f"models/block/{block_id}.json").exists():
            errors.append(f"missing block model: {block_id}")
        if not (ASSETS / f"models/item/{block_id}.json").exists():
            errors.append(f"missing block item model: {block_id}")
        if not (ASSETS / f"blockstates/{block_id}.json").exists():
            errors.append(f"missing blockstate: {block_id}")
        if not (ASSETS / f"textures/block/{block_id}.png").exists():
            errors.append(f"missing block texture: {block_id}")
        if not (DATA / f"loot_tables/blocks/{block_id}.json").exists():
            errors.append(f"missing block loot table: {block_id}")

    print(f"Registry ids: items={len(item_ids)}, blocks={len(block_ids)}")
    return errors


def model_reference_audit() -> list[str]:
    errors: list[str] = []

    for path in (ASSETS / "models").rglob("*.json"):
        rel = path.relative_to(ROOT).as_posix()
        try:
            obj = json.loads(path.read_text(encoding="utf-8"))
        except Exception:
            continue

        parent = obj.get("parent")
        if isinstance(parent, str) and parent.startswith("thaumcraft:"):
            parent_path = ASSETS / "models" / f"{parent.split(':', 1)[1]}.json"
            if not parent_path.exists():
                errors.append(f"{rel}: missing parent model {parent}")

        textures = obj.get("textures", {})
        if isinstance(textures, dict):
            for key, value in textures.items():
                if not isinstance(value, str) or value.startswith("#"):
                    continue
                if ":" in value:
                    namespace, texture_path = value.split(":", 1)
                    if namespace != "thaumcraft":
                        continue
                else:
                    texture_path = value
                if not (ASSETS / "textures" / f"{texture_path}.png").exists():
                    errors.append(f"{rel}: missing texture ref {key}={value}")

    return errors


def gui_audit() -> list[str]:
    errors: list[str] = []

    gui_dir = ASSETS / "textures/gui"
    if not gui_dir.exists():
        errors.append("missing textures/gui directory")
        return errors

    required_gui = [
        "thaumonomicon.png",
        "arcane_workbench.png",
        "pech_trade.png",
        "essentia_terminal.png",
        "essentia_drive.png",
        "osmotic_enchanter.png",
        "transvector_interface.png",
        "bottomless_pouch.png",
    ]

    for name in required_gui:
        path = gui_dir / name
        if not path.exists():
            errors.append(f"missing GUI texture: {name}")
            continue
        try:
            read_png_size(path)
        except Exception as exc:
            errors.append(f"bad GUI texture {name}: {exc}")

    return errors


def java_brace_audit() -> list[str]:
    errors: list[str] = []

    for path in (ROOT / "src/main/java").rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        balance = 0
        for ch in text:
            if ch == "{":
                balance += 1
            elif ch == "}":
                balance -= 1
            if balance < 0:
                errors.append(f"{path.relative_to(ROOT)}: negative brace balance")
                break
        if balance != 0:
            errors.append(f"{path.relative_to(ROOT)}: brace balance {balance}")

    return errors



def compile_helper_presence_audit() -> list[str]:
    errors: list[str] = []
    text = JAVA_MAIN.read_text(encoding="utf-8")
    required_helpers = [
        "private static RegistryObject<Item> ttParityItem",
        "private static RegistryObject<Item> tceParityItem",
        "private static RegistryObject<Block> ttParityBlock",
        "private static RegistryObject<Block> tceParityBlock",
    ]
    for helper in required_helpers:
        if helper not in text:
            errors.append(f"missing compile helper: {helper}")
    return errors



def compile_api_risk_audit() -> list[str]:
    errors: list[str] = []
    forbidden_patterns = {
        "Container.stillValidBlockEntity(": "1.19.2 has no Container.stillValidBlockEntity helper here",
        ".serverLevel()": "ServerPlayer.serverLevel() was unavailable in the GitHub compile environment",
        ".parents()": "ResearchEntry exposes requirements(), not parents()",
        ".setHint(": "EditBox#setHint was unavailable in the GitHub compile environment",
    }

    for path in (ROOT / "src/main/java").rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        rel = path.relative_to(ROOT).as_posix()
        for pattern, reason in forbidden_patterns.items():
            if pattern in text:
                errors.append(f"{rel}: forbidden compile-risk pattern {pattern} — {reason}")

    return errors



def original_asset_restoration_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "textures/gui/thaumonomicon.png",
        "textures/gui/research_table.png",
        "textures/gui/arcane_workbench.png",
        "textures/gui/pech_trade.png",
        "textures/item/iron_capped_wooden_wand.png",
        "textures/item/greatwood_wand.png",
        "textures/item/infused_scribing_tools.png",
        "textures/item/goggles_of_revealing.png",
        "textures/block/research_table.png",
        "textures/block/arcane_workbench.png",
        "textures/original/thaumcraft4/gui/gui_researchbook.png",
        "textures/original/thaumcraft4/gui/guiresearchtable2.png",
        "textures/original/thaumcraft4/gui/gui_arcaneworkbench.png",
    ]
    for rel in required:
        path = ASSETS / rel
        if not path.exists():
            errors.append(f"missing restored original asset: {rel}")
    return errors



def stage90_deepening_audit() -> list[str]:
    errors: list[str] = []
    required_files = [
        "sounds.json",
        "sounds/wand1.ogg",
        "sounds/page1.ogg",
        "sounds/infuser.ogg",
        "sounds/jar1.ogg",
        "textures/item/tt_dark_quartz.png",
        "textures/item/tce_info_book.png",
        "textures/block/jar_top.png",
        "textures/block/jar_bottom.png",
        "textures/block/pedestal_top.png",
        "lang/original_legacy/en_US.lang",
    ]
    for rel in required_files:
        if not (ASSETS / rel).exists():
            errors.append(f"missing Stage90 deepening file: {rel}")

    # Stage96 update: wands are allowed to be 3D element models instead of 2D handheld.
    for model_id in ["iron_capped_wooden_wand", "greatwood_wand", "silverwood_wand", "focus_fire"]:
        path = ASSETS / "models" / "item" / f"{model_id}.json"
        if path.exists():
            try:
                obj = json.loads(path.read_text(encoding="utf-8"))
                if model_id.endswith("_wand") or model_id in ["iron_capped_wooden_wand", "greatwood_wand", "silverwood_wand"]:
                    if "elements" not in obj and obj.get("parent") not in ("minecraft:item/handheld", "minecraft:builtin/entity"):
                        errors.append(f"{model_id} item model is neither 3D elements, handheld nor custom builtin/entity")
                elif obj.get("parent") != "minecraft:item/handheld":
                    errors.append(f"{model_id} item model is not handheld")
            except Exception as exc:
                errors.append(f"{model_id} model read failed: {exc}")
    return errors



def stage91_strict_gui_audit() -> list[str]:
    errors: list[str] = []
    required_java = [
        "src/main/java/com/darkifov/thaumcraft/client/screen/OriginalGuiTextures.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/ThaumonomiconScreen.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableScreen.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchScreen.java",
    ]
    for rel in required_java:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage91 Java file: {rel}")

    # Reject the broken old pattern: stretched 256x arbitrary GUI blit on original backgrounds.
    for path in (ROOT / "src/main/java/com/darkifov/thaumcraft/client/screen").glob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        rel = path.relative_to(ROOT).as_posix()
        if "blit(poseStack, panelX, panelY, 0, 0, 256, 212)" in text:
            errors.append(f"{rel}: contains old stretched arcane GUI blit")
        if "blit(poseStack, leftPos, topPos, 0, 0, Math.min(imageWidth, 256), Math.min(imageHeight, 256))" in text:
            errors.append(f"{rel}: contains old generic min-size GUI blit")

    # Stage96 update: wands should now be true 3D element models using rod/cap textures.
    for model_id in ["iron_capped_wooden_wand", "greatwood_wand", "silverwood_wand"]:
        path = ASSETS / "models" / "item" / f"{model_id}.json"
        if not path.exists():
            errors.append(f"missing wand model: {model_id}")
            continue
        try:
            obj = json.loads(path.read_text(encoding="utf-8"))
            textures = obj.get("textures", {})
            if obj.get("parent") == "minecraft:builtin/entity":
                pass
            else:
                if "elements" not in obj:
                    errors.append(f"{model_id}: not a Stage96/98 3D/custom wand model")
                if "rod" not in textures or "cap" not in textures:
                    errors.append(f"{model_id}: missing rod/cap texture channels")
        except Exception as exc:
            errors.append(f"{model_id}: invalid json {exc}")

    return errors



def stage92_behavior_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "src/main/java/com/darkifov/thaumcraft/client/screen/OriginalResearchCategory.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/OriginalResearchLayout.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/ThaumonomiconScreen.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableScreen.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchScreen.java",
        "STAGE92_ORIGINAL_RESEARCH_BEHAVIOR_REPORT.json",
    ]
    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage92 behavior file: {rel}")

    screen_checks = {
        "ThaumonomiconScreen.java": ["OriginalResearchLayout.entriesFor", "mouseClicked", "renderResearchTree", "renderSelectedPage"],
        "ResearchTableScreen.java": ["Aspect.values().length", "AspectCombinationRegistry.count", "textures/aspects/", "mouseClicked"],
        "ArcaneWorkbenchScreen.java": ["renderOriginalLikeCraftingOverlay", "renderVisAndPrimalCosts", "PRIMAL"],
    }

    for file_name, tokens in screen_checks.items():
        path = ROOT / "src/main/java/com/darkifov/thaumcraft/client/screen" / file_name
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{file_name}: missing behavior token {token}")

    if (ROOT / ".github/ISSUE_TEMPLATE").exists():
        errors.append("clean package regression: .github/ISSUE_TEMPLATE should not be present")

    return errors



def stage93_backend_bridge_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "src/main/java/com/darkifov/thaumcraft/research/OriginalAspectWallet.java",
        "src/main/java/com/darkifov/thaumcraft/research/OriginalResearchBridge.java",
        "src/main/java/com/darkifov/thaumcraft/research/OriginalArcaneCostBridge.java",
        "STAGE93_ORIGINAL_BACKEND_BEHAVIOR_BRIDGE_REPORT.json",
        "src/main/resources/assets/thaumcraft/textures/item/research_note_complete.png",
        "src/main/resources/assets/thaumcraft/textures/block/essentia_jar_aer.png",
        "src/main/resources/assets/thaumcraft/textures/block/aura_node_aer.png",
    ]
    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage93 backend bridge file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/research/OriginalAspectWallet.java": ["getPersistentData", "seedIfEmpty", "consume"],
        "src/main/java/com/darkifov/thaumcraft/research/OriginalResearchBridge.java": ["canUnlock", "completeWithAspectCost", "requirements"],
        "src/main/java/com/darkifov/thaumcraft/research/OriginalArcaneCostBridge.java": ["visCostFor", "primalCostFor", "canCraft"],
    }

    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing token {token}")

    return errors



def stage94_selected_render_bridge_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "src/main/java/com/darkifov/thaumcraft/research/OriginalResearchSelection.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/OriginalClientResearchSelection.java",
        "src/main/java/com/darkifov/thaumcraft/client/OriginalVisualStateBridge.java",
        "STAGE94_SELECTED_RESEARCH_RENDER_BRIDGE_REPORT.json",
        "src/main/resources/assets/thaumcraft/textures/item/research_note_selected.png",
        "src/main/resources/assets/thaumcraft/textures/item/research_note_locked.png",
        "src/main/resources/assets/thaumcraft/textures/item/research_note_available.png",
        "src/main/resources/assets/thaumcraft/textures/item/research_note_complete.png",
        "src/main/resources/assets/thaumcraft/models/block/essentia_jar_aer.json",
        "src/main/resources/assets/thaumcraft/models/block/aura_node_aer.json",
    ]
    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage94 selected/render bridge file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/research/OriginalResearchSelection.java": ["SelectedResearchKey", "getEntry", "clear"],
        "src/main/java/com/darkifov/thaumcraft/research/OriginalResearchBridge.java": ["selectedOrFirstAvailable", "completeSelectedOrFirst", "statusFor"],
        "src/main/java/com/darkifov/thaumcraft/client/screen/ThaumonomiconScreen.java": ["OriginalClientResearchSelection.set", "Selected for Research Note"],
        "src/main/java/com/darkifov/thaumcraft/client/OriginalVisualStateBridge.java": ["jarModel", "nodeModel", "researchNoteModel"],
    }

    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing token {token}")

    return errors



def stage95_networked_research_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "src/main/java/com/darkifov/thaumcraft/network/RequestSelectResearchPacket.java",
        "src/main/java/com/darkifov/thaumcraft/network/RequestCompleteSelectedResearchPacket.java",
        "STAGE95_NETWORKED_SELECTED_RESEARCH_REPORT.json",
    ]
    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage95 networked research file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java": [
            "RequestSelectResearchPacket.class",
            "RequestCompleteSelectedResearchPacket.class",
            "requestSelectResearchFromClient",
            "requestCompleteSelectedResearchFromClient"
        ],
        "src/main/java/com/darkifov/thaumcraft/client/screen/ThaumonomiconScreen.java": [
            "ThaumcraftNetwork.requestSelectResearchFromClient",
            "ThaumcraftNetwork.requestCompleteSelectedResearchFromClient",
            "ClientResearchData.research",
            "Complete Selected"
        ],
        "src/main/java/com/darkifov/thaumcraft/network/RequestResearchUnlockPacket.java": [
            "OriginalResearchBridge.selectedOrFirstAvailable",
            "OriginalResearchBridge.completeWithAspectCost"
        ],
        "src/main/java/com/darkifov/thaumcraft/network/RequestSelectResearchPacket.java": [
            "writeUtf",
            "OriginalResearchBridge.select",
            "ThaumcraftNetwork.syncResearch"
        ],
        "src/main/java/com/darkifov/thaumcraft/network/RequestCompleteSelectedResearchPacket.java": [
            "selectedOrFirstAvailable",
            "findResearchPoint",
            "completeWithAspectCost"
        ],
    }

    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing token {token}")

    return errors



def stage96_real_parity_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "REAL_MINECRAFT_PARITY_AUDIT_STAGE96.md",
        "REAL_MINECRAFT_PARITY_AUDIT_STAGE96.json",
        "src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java",
        "src/main/resources/assets/thaumcraft/textures/block/aura_node_sprite.png",
        "src/main/resources/assets/thaumcraft/textures/item/aura_node_debug.png",
    ]
    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage96 real parity file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/block/AuraNodeBlock.java": [
            "RenderShape.INVISIBLE",
            "Shapes.empty",
            "AURA_NODE_SELECTION_SHAPE"
        ],
        "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java": [
            "AuraNodeRenderer"
        ],
        "src/main/java/com/darkifov/thaumcraft/block/ThaumicEnergisticsDeviceBlock.java": [
            "PART_LIKE_SHAPE",
            "isPartLike"
        ],
        "src/main/java/com/darkifov/thaumcraft/block/EssentiaJarBlock.java": [
            "JAR_SHAPE"
        ],
    }

    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing Stage96 token {token}")

    # Reject the specific old bad patterns for the corrected objects.
    bad_model_patterns = {
        "src/main/resources/assets/thaumcraft/models/block/aura_node.json": ["minecraft:block/cube_all", "minecraft:block/cube"],
        "src/main/resources/assets/thaumcraft/models/block/essentia_import_bus.json": ["minecraft:block/cube_all"],
        "src/main/resources/assets/thaumcraft/models/block/essentia_export_bus.json": ["minecraft:block/cube_all"],
        "src/main/resources/assets/thaumcraft/models/block/essentia_storage_bus.json": ["minecraft:block/cube_all"],
        "src/main/resources/assets/thaumcraft/models/item/iron_capped_wooden_wand.json": ["minecraft:item/generated", "minecraft:item/handheld"],
    }

    for rel, bad_tokens in bad_model_patterns.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in bad_tokens:
            if token in text:
                errors.append(f"{rel}: still contains old bad pattern {token}")

    return errors



def stage97_aura_node_thaumometer_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "STAGE97_AURA_NODE_THAUMOMETER_PARITY_REPORT.json",
        "src/main/java/com/darkifov/thaumcraft/aura/AuraNodeType.java",
        "src/main/java/com/darkifov/thaumcraft/aura/AuraNodeScan.java",
        "src/main/java/com/darkifov/thaumcraft/item/ThaumometerItem.java",
        "src/main/resources/assets/thaumcraft/textures/item/thaumometer.png",
        "src/main/resources/assets/thaumcraft/models/item/thaumometer.json",
        "src/main/resources/assets/thaumcraft/textures/block/aura_node_sprite_normal.png",
        "src/main/resources/assets/thaumcraft/textures/block/aura_node_sprite_pure.png",
        "src/main/resources/assets/thaumcraft/textures/block/aura_node_sprite_tainted.png",
    ]
    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage97 aura/thaumometer file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/blockentity/AuraNodeBlockEntity.java": [
            "AuraNodeType",
            "stability",
            "scanned",
            "markScanned",
            "visualSize"
        ],
        "src/main/java/com/darkifov/thaumcraft/block/AuraNodeBlock.java": [
            "AuraNodeScan.sendScan",
            "DustParticleOptions",
            "thaumometer"
        ],
        "src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java": [
            "typedNodeType",
            "scanned",
            "stabilityPulse"
        ],
        "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java": [
            "THAUMOMETER",
            "ThaumometerItem"
        ],
    }

    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing Stage97 token {token}")

    return errors



def stage98_wand_renderer_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "NO_COSTYL_POLICY_STAGE98.json",
        "STAGE98_WAND_ORIGINAL_COMPONENT_RENDERER_REPORT.json",
        "src/main/java/com/darkifov/thaumcraft/wand/WandRodType.java",
        "src/main/java/com/darkifov/thaumcraft/wand/WandCapType.java",
        "src/main/java/com/darkifov/thaumcraft/wand/WandComponentData.java",
        "src/main/java/com/darkifov/thaumcraft/client/render/WandItemRenderer.java",
        "src/main/resources/assets/thaumcraft/textures/entity/wand/rod_wood.png",
        "src/main/resources/assets/thaumcraft/textures/entity/wand/rod_greatwood.png",
        "src/main/resources/assets/thaumcraft/textures/entity/wand/rod_silverwood.png",
        "src/main/resources/assets/thaumcraft/textures/entity/wand/cap_iron.png",
        "src/main/resources/assets/thaumcraft/textures/entity/wand/cap_gold.png",
        "src/main/resources/assets/thaumcraft/textures/entity/wand/cap_thaumium.png",
    ]
    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage98 wand renderer file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/block/WandItem.java": [
            "initializeClient",
            "IClientItemExtensions",
            "WandItemRenderer.instance",
            "defaultRod",
            "defaultCap",
            "WandComponentData"
        ],
        "src/main/java/com/darkifov/thaumcraft/client/render/WandItemRenderer.java": [
            "renderByItem",
            "WandComponentData.from",
            "RenderType.entityCutoutNoCull",
            "rodTexture",
            "capTexture"
        ],
        "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java": [
            "WandRodType.WOOD",
            "WandRodType.GREATWOOD",
            "WandRodType.SILVERWOOD",
            "WandCapType.IRON",
            "WandCapType.GOLD",
            "WandCapType.THAUMIUM"
        ],
    }

    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing Stage98 token {token}")

    for model_id in ["iron_capped_wooden_wand", "greatwood_wand", "silverwood_wand"]:
        path = ASSETS / "models" / "item" / f"{model_id}.json"
        if not path.exists():
            errors.append(f"missing wand model {model_id}")
            continue
        try:
            obj = json.loads(path.read_text(encoding="utf-8"))
            if obj.get("parent") != "minecraft:builtin/entity":
                errors.append(f"{model_id}: must use builtin/entity custom renderer model, got {obj.get('parent')}")
        except Exception as exc:
            errors.append(f"{model_id}: invalid json {exc}")

    return errors



def stage99_avaritia_creative_wand_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "STAGE99_AVARITIA_CREATIVE_WAND_INFINITE_VIS_REPORT.json",
        "src/main/java/com/darkifov/thaumcraft/block/AvaritiaCreativeWandItem.java",
        "src/main/resources/assets/thaumcraft/models/item/avaritia_creative_wand.json",
        "src/main/resources/assets/thaumcraft/textures/item/avaritia_creative_wand.png",
        "src/main/resources/assets/thaumcraft/textures/entity/wand/rod_creative_infinity.png",
        "src/main/resources/assets/thaumcraft/textures/entity/wand/cap_infinity.png",
    ]

    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage99 creative wand file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/block/WandItem.java": [
            "INFINITE_VIS_DISPLAY",
            "hasInfiniteVis",
            "isInfiniteVis",
            "∞ infinite primal vis"
        ],
        "src/main/java/com/darkifov/thaumcraft/block/AvaritiaCreativeWandItem.java": [
            "extends WandItem",
            "isInfiniteVis",
            "WandRodType.CREATIVE",
            "WandCapType.INFINITY",
            "Infinite primal vis"
        ],
        "src/main/java/com/darkifov/thaumcraft/wand/WandRodType.java": [
            "CREATIVE",
            "rod_creative_infinity"
        ],
        "src/main/java/com/darkifov/thaumcraft/wand/WandCapType.java": [
            "INFINITY",
            "cap_infinity",
            "0.0F"
        ],
        "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java": [
            "AVARITIA_CREATIVE_WAND",
            "avaritia_creative_wand",
            "AvaritiaCreativeWandItem"
        ],
    }

    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing Stage99 token {token}")

    model_path = ASSETS / "models" / "item" / "avaritia_creative_wand.json"
    if model_path.exists():
        try:
            obj = json.loads(model_path.read_text(encoding="utf-8"))
            if obj.get("parent") != "minecraft:builtin/entity":
                errors.append("avaritia_creative_wand must use minecraft:builtin/entity custom renderer model")
        except Exception as exc:
            errors.append(f"avaritia_creative_wand model invalid json: {exc}")

    lang_path = ASSETS / "lang" / "ru_ru.json"
    if lang_path.exists():
        lang = lang_path.read_text(encoding="utf-8", errors="ignore")
        if "Креативный жезл Аваритии" not in lang:
            errors.append("ru_ru language missing Avaritia creative wand name")

    return errors



def stage100_aspect_research_foundation_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "STAGE100_ORIGINAL_ASPECT_RESEARCH_FOUNDATION_REPORT.json",
        "src/main/java/com/darkifov/thaumcraft/Aspect.java",
        "src/main/java/com/darkifov/thaumcraft/AspectStack.java",
        "src/main/java/com/darkifov/thaumcraft/AspectCombinationRegistry.java",
        "src/main/java/com/darkifov/thaumcraft/research/PlayerAspectKnowledge.java",
        "src/main/java/com/darkifov/thaumcraft/research/ResearchTableFoundation.java",
        "src/main/resources/data/thaumcraft/tc4_aspect_foundation.json",
        "src/main/resources/assets/thaumcraft/textures/aspects/aer.png",
        "src/main/resources/assets/thaumcraft/textures/aspects/vinculum.png",
        "src/main/resources/assets/thaumcraft/textures/item/aspect_vinculum.png",
    ]

    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage100 foundation file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/Aspect.java": [
            "AER(",
            "VINCULUM(",
            "colorValue",
            "componentA",
            "byId",
        ],
        "src/main/java/com/darkifov/thaumcraft/AspectCombinationRegistry.java": [
            "register(Aspect.AER, Aspect.PERDITIO, Aspect.VACUOS)",
            "register(Aspect.MOTUS, Aspect.PERDITIO, Aspect.VINCULUM)",
            "combine",
        ],
        "src/main/java/com/darkifov/thaumcraft/research/PlayerAspectKnowledge.java": [
            "seedPrimals",
            "knownAspects",
            "consumePool",
        ],
        "src/main/java/com/darkifov/thaumcraft/research/ResearchTableFoundation.java": [
            "combine",
            "AspectCombinationRegistry.combine",
            "Discovered aspect",
        ],
        "src/main/java/com/darkifov/thaumcraft/block/ResearchTableBlock.java": [
            "ScribingToolsItem.consumeInk",
            "ResearchTableFoundation.seed",
            "TheoryProgress",
        ],
        "src/main/java/com/darkifov/thaumcraft/block/ScribingToolsItem.java": [
            "MAX_INK",
            "consumeInk",
            "Ink:",
        ],
        "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableScreen.java": [
            "Aspect.values().length",
            "AspectCombinationRegistry.count",
            "textures/aspects/",
        ],
    }

    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing Stage100 token {token}")

    try:
        text = (ROOT / "src/main/java/com/darkifov/thaumcraft/Aspect.java").read_text(encoding="utf-8")
        if text.count("(") < 48:
            errors.append("Aspect enum appears incomplete")
    except Exception:
        pass

    return errors



def stage101_networked_research_table_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "STAGE101_NETWORKED_RESEARCH_TABLE_ASPECT_DISCOVERY_REPORT.json",
        "src/main/java/com/darkifov/thaumcraft/client/ClientAspectData.java",
        "src/main/java/com/darkifov/thaumcraft/network/AspectKnowledgeSyncPacket.java",
        "src/main/java/com/darkifov/thaumcraft/network/RequestCombineAspectsPacket.java",
    ]

    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage101 networked research table file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/client/ClientAspectData.java": [
            "knownCount",
            "poolView",
            "seedPrimals"
        ],
        "src/main/java/com/darkifov/thaumcraft/network/AspectKnowledgeSyncPacket.java": [
            "ClientAspectData.set",
            "writeVarInt",
            "pool"
        ],
        "src/main/java/com/darkifov/thaumcraft/network/RequestCombineAspectsPacket.java": [
            "ResearchTableFoundation.combine",
            "syncAspectKnowledge",
            "syncResearch"
        ],
        "src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java": [
            "AspectKnowledgeSyncPacket.class",
            "RequestCombineAspectsPacket.class",
            "syncAspectKnowledge",
            "requestCombineAspectsFromClient"
        ],
        "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableScreen.java": [
            "ClientAspectData.knows",
            "ClientAspectData.pool",
            "ThaumcraftNetwork.requestCombineAspectsFromClient",
            "Combination sent"
        ],
        "src/main/java/com/darkifov/thaumcraft/research/ResearchTableFoundation.java": [
            "Not enough aspect notes",
            "consumePool",
            "player.getAbilities().instabuild"
        ],
        "src/main/java/com/darkifov/thaumcraft/research/PlayerAspectKnowledge.java": [
            "knownAspectIds",
            "poolAmounts"
        ],
    }

    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing Stage101 token {token}")

    return errors



def stage102_research_note_minigame_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "STAGE102_RESEARCH_NOTE_MINIGAME_FOUNDATION_REPORT.json",
        "src/main/java/com/darkifov/thaumcraft/research/ResearchNoteGrid.java",
        "src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java",
        "src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java",
        "src/main/java/com/darkifov/thaumcraft/client/ClientResearchNoteData.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java",
        "src/main/java/com/darkifov/thaumcraft/network/ResearchNoteSyncPacket.java",
        "src/main/java/com/darkifov/thaumcraft/network/OpenResearchNotePacket.java",
        "src/main/java/com/darkifov/thaumcraft/network/RequestPlaceResearchNoteAspectPacket.java",
        "src/main/java/com/darkifov/thaumcraft/network/RequestSolveResearchNotePacket.java",
    ]

    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage102 research note file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java": [
            "TAG_ROOT",
            "place",
            "canLink",
            "isSolved",
            "markSolved"
        ],
        "src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java": [
            "PlayerAspectKnowledge.consumePool",
            "ResearchNoteState.place",
            "ResearchNoteState.isSolved"
        ],
        "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java": [
            "requestPlaceResearchNoteAspectFromClient",
            "requestSolveResearchNoteFromClient",
            "ClientResearchNoteData"
        ],
        "src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java": [
            "ResearchNoteSyncPacket.class",
            "OpenResearchNotePacket.class",
            "RequestPlaceResearchNoteAspectPacket.class",
            "RequestSolveResearchNotePacket.class",
            "syncResearchNote",
            "openResearchNote"
        ],
        "src/main/java/com/darkifov/thaumcraft/block/ResearchNoteItem.java": [
            "ResearchNoteState.initialize",
            "openResearchNote",
            "Shift-right-click"
        ],
    }

    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing Stage102 token {token}")

    return errors



def stage103_research_note_target_rules_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "STAGE103_RESEARCH_NOTE_TARGET_RULES_REPORT.json",
        "src/main/java/com/darkifov/thaumcraft/research/ResearchAspectGraph.java",
        "src/main/java/com/darkifov/thaumcraft/research/ResearchNoteRequirements.java",
    ]
    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage103 research note target rules file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/research/ResearchAspectGraph.java": [
            "distance",
            "canConnect",
            "AspectCombinationRegistry.combine",
        ],
        "src/main/java/com/darkifov/thaumcraft/research/ResearchNoteRequirements.java": [
            "requiredFor",
            "wand",
            "alchemy",
            "golem",
            "eldritch"
        ],
        "src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java": [
            "TAG_REQUIRED",
            "requiredAspects",
            "ResearchNoteRequirements.startFor",
            "ResearchAspectGraph.canConnect",
            "hasAllRequired"
        ],
        "src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java": [
            "missing required aspects",
            "hasAllRequired"
        ],
        "src/main/java/com/darkifov/thaumcraft/block/ResearchTableBlock.java": [
            "selectedOrFirstAvailable",
            "targetResearch.key"
        ],
        "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java": [
            "ResearchAspectGraph.distance",
            "ResearchNoteRequirements.requiredFor",
            "Required aspects are marked"
        ],
    }

    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing Stage103 token {token}")

    return errors



def stage104_native_aspect_colors_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "STAGE104_NATIVE_ASPECT_COLORS_REPORT.json",
        "src/main/java/com/darkifov/thaumcraft/AspectColor.java",
        "src/main/resources/data/thaumcraft/tc4_aspect_native_colors.json",
        "src/main/resources/assets/thaumcraft/textures/gui/aspect_native_color_swatch.png",
    ]
    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage104 native aspect colors file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/Aspect.java": [
            "nativeColor",
            "argbColor",
            "textColor"
        ],
        "src/main/java/com/darkifov/thaumcraft/AspectColor.java": [
            "argb",
            "dim",
            "mix",
            "readableText",
            "hex"
        ],
        "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableScreen.java": [
            "AspectColor.argb",
            "AspectColor.dim"
        ],
        "src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java": [
            "AspectColor.argb",
            "AspectColor.mix",
            "AspectColor.dim"
        ],
        "src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java": [
            "aspect.textColor"
        ],
        "src/main/java/com/darkifov/thaumcraft/research/ResearchTableFoundation.java": [
            "discovered.textColor"
        ],
    }

    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing Stage104 token {token}")

    try:
        data = json.loads((ROOT / "src/main/resources/data/thaumcraft/tc4_aspect_native_colors.json").read_text(encoding="utf-8"))
        if data.get("count") != 48:
            errors.append("native aspect color export must contain 48 aspects")
        if not any(entry.get("id") == "praecantatio" and entry.get("rgb_hex") for entry in data.get("colors", [])):
            errors.append("native color export missing praecantatio")
    except Exception as exc:
        errors.append(f"native aspect color export invalid: {exc}")

    return errors



def stage105_essentia_jar_aura_color_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "STAGE105_ESSENTIA_JAR_AURA_NATIVE_COLOR_RENDERER_REPORT.json",
        "src/main/java/com/darkifov/thaumcraft/AspectVisuals.java",
        "src/main/java/com/darkifov/thaumcraft/client/render/EssentiaJarRenderer.java",
        "src/main/resources/assets/thaumcraft/textures/block/essentia_fill.png",
    ]
    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage105 jar/aura renderer file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/AspectVisuals.java": [
            "dominant",
            "blendedColor",
            "fillRatio"
        ],
        "src/main/java/com/darkifov/thaumcraft/AspectList.java": [
            "aspect.textColor"
        ],
        "src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java": [
            "AspectVisuals.blendedColor",
            "specialTypeTint"
        ],
        "src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaJarBlockEntity.java": [
            "storedAspect",
            "amount()",
            "capacity()",
            "fillRatio"
        ],
        "src/main/java/com/darkifov/thaumcraft/client/render/EssentiaJarRenderer.java": [
            "EssentiaJarBlockEntity",
            "AspectColor.argb",
            "RenderType.entityTranslucent",
            "essentia_fill.png"
        ],
        "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java": [
            "EssentiaJarRenderer",
            "ESSENTIA_JAR_BLOCK_ENTITY"
        ],
        "src/main/java/com/darkifov/thaumcraft/block/EssentiaJarBlock.java": [
            "first.textColor",
            "heldAspect.textColor",
            "jar.amount()",
            "jar.capacity()"
        ],
    }

    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing Stage105 token {token}")

    return errors



def stage106_essentia_transport_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "STAGE106_ESSENTIA_TRANSPORT_ALEMBIC_SUCTION_REPORT.json",
        "src/main/java/com/darkifov/thaumcraft/essentia/EssentiaSuction.java",
        "src/main/java/com/darkifov/thaumcraft/client/render/AlembicRenderer.java",
    ]
    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage106 essentia transport file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/essentia/EssentiaSuction.java": [
            "JAR_NORMAL",
            "JAR_FILTERED",
            "JAR_VOID",
            "ALEMBIC_SOURCE_PRIORITY",
            "FURNACE_SOURCE_PRIORITY"
        ],
        "src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java": [
            "findBestSource",
            "AlembicSource",
            "FurnaceSource",
            "findBestDestinationJar",
            "lastMovedAspect",
            "DustParticleOptions"
        ],
        "src/main/java/com/darkifov/thaumcraft/block/EssentiaTubeBlock.java": [
            "lastSourceCount",
            "lastDestinationCount",
            "lastMovedAspect"
        ],
        "src/main/java/com/darkifov/thaumcraft/client/render/AlembicRenderer.java": [
            "AlembicBlockEntity",
            "AspectColor.argb",
            "RenderType.entityTranslucent",
            "essentia_fill.png"
        ],
        "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java": [
            "AlembicRenderer",
            "ALEMBIC_BLOCK_ENTITY"
        ],
    }

    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing Stage106 token {token}")

    return errors


def stage107_directional_essentia_tubes_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "STAGE107_DIRECTIONAL_ESSENTIA_TUBES_REPORT.json",
        "src/main/java/com/darkifov/thaumcraft/essentia/EssentiaTubeConnections.java",
        "src/main/java/com/darkifov/thaumcraft/essentia/EssentiaSuctionPath.java",
        "src/main/resources/assets/thaumcraft/models/block/essentia_tube_center.json",
        "src/main/resources/assets/thaumcraft/models/block/essentia_tube_arm_north.json",
        "src/main/resources/assets/thaumcraft/blockstates/essentia_tube.json",
    ]
    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage107 directional tube file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/essentia/EssentiaTubeConnections.java": [
            "canConnect",
            "EssentiaTubeBlockEntity",
            "EssentiaJarBlockEntity",
            "AlembicBlockEntity",
            "summary"
        ],
        "src/main/java/com/darkifov/thaumcraft/block/EssentiaTubeBlock.java": [
            "BooleanProperty",
            "NORTH",
            "SOUTH",
            "UP",
            "DOWN",
            "updateConnections",
            "getStateForPlacement",
            "Connected sides"
        ],
        "src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java": [
            "EssentiaTubeConnections",
            "connectedTransportNeighbor",
            "Directional tube pass"
        ],
    }
    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing Stage107 token {token}")

    try:
        state = json.loads((ASSETS / "blockstates" / "essentia_tube.json").read_text(encoding="utf-8"))
        if "multipart" not in state:
            errors.append("essentia_tube blockstate must use multipart")
    except Exception as exc:
        errors.append(f"essentia_tube blockstate invalid: {exc}")

    return errors



def stage108_tube_suction_backflow_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "STAGE108_TUBE_SUCTION_BACKFLOW_TEST_READY_REPORT.json",
        "TEST_UPLOAD_CHECKLIST_STAGE108.md",
        "src/main/java/com/darkifov/thaumcraft/essentia/EssentiaSuctionResolver.java",
        "src/main/java/com/darkifov/thaumcraft/essentia/EssentiaBackflowResult.java",
    ]

    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage108 suction/backflow file: {rel}")

    token_checks = {
        "src/main/java/com/darkifov/thaumcraft/block/EssentiaTubeBlock.java": [
            "public static final BooleanProperty NORTH",
            "CORE_SHAPE",
            "Block.box",
            "connectedSidesDiagnostic",
            "Winning suction",
            "Backflow"
        ],
        "src/main/java/com/darkifov/thaumcraft/essentia/EssentiaSuctionResolver.java": [
            "sideAllows",
            "sourcePressure",
            "destinationSuction",
            "competingDestinations",
            "filteredJar"
        ],
        "src/main/java/com/darkifov/thaumcraft/essentia/EssentiaBackflowResult.java": [
            "canMove",
            "netPull",
            "backflowBlocked"
        ],
        "src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java": [
            "EssentiaSuctionResolver.sideAllows",
            "EssentiaBackflowResult",
            "lastConflictCount",
            "lastWinningSuction",
            "lastBackflowBlocked"
        ],
    }

    for rel, tokens in token_checks.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8", errors="ignore") if path.exists() else ""
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: missing Stage108 token {token}")

    return errors



def stage109_compile_syntax_fix_audit() -> list[str]:
    errors: list[str] = []
    required = [
        "STAGE109_COMPILE_SYNTAX_FIX_REPORT.json",
        "scripts/java_syntax_guard.py",
    ]

    for rel in required:
        if not (ROOT / rel).exists():
            errors.append(f"missing Stage109 compile syntax file: {rel}")

    for path in (ROOT / "src/main/java").rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        rel = path.relative_to(ROOT)

        if '\\"' in text:
            errors.append(f"{rel}: contains escaped quote syntax leak")

        if "\\n        " in text or "\\n    " in text:
            errors.append(f"{rel}: contains literal backslash-n syntax leak")

    fixed_targets = [
        "src/main/java/com/darkifov/thaumcraft/client/screen/OsmoticEnchanterScreen.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/EssentiaDriveScreen.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/TransvectorInterfaceScreen.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/PechTradeScreen.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchContainerScreen.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchScreen.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/EssentiaTerminalScreen.java",
        "src/main/java/com/darkifov/thaumcraft/client/screen/BottomlessPouchScreen.java",
    ]

    for rel in fixed_targets:
        path = ROOT / rel
        if not path.exists():
            errors.append(f"expected Stage109 fixed target missing: {rel}")

    return errors


def main() -> None:
    checks = {
        "JSON": json_audit(),
        "PNG": png_audit(),
        "Registry resources": registry_resource_audit(),
        "Model references": model_reference_audit(),
        "GUI": gui_audit(),
        "Java braces": java_brace_audit(),
        "Compile helpers": compile_helper_presence_audit(),
        "Compile API risks": compile_api_risk_audit(),
        "Original asset restoration": original_asset_restoration_audit(),
        "Stage90 deepening": stage90_deepening_audit(),
        "Stage91 strict GUI": stage91_strict_gui_audit(),
        "Stage92 behavior": stage92_behavior_audit(),
        "Stage93 backend bridge": stage93_backend_bridge_audit(),
        "Stage94 selected/render bridge": stage94_selected_render_bridge_audit(),
        "Stage95 networked research": stage95_networked_research_audit(),
        "Stage96 real parity": stage96_real_parity_audit(),
        "Stage97 aura thaumometer": stage97_aura_node_thaumometer_audit(),
        "Stage98 wand renderer": stage98_wand_renderer_audit(),
        "Stage99 Avaritia creative wand": stage99_avaritia_creative_wand_audit(),
        "Stage100 aspect research foundation": stage100_aspect_research_foundation_audit(),
        "Stage101 networked research table": stage101_networked_research_table_audit(),
        "Stage102 research note minigame": stage102_research_note_minigame_audit(),
        "Stage103 research note target rules": stage103_research_note_target_rules_audit(),
        "Stage104 native aspect colors": stage104_native_aspect_colors_audit(),
        "Stage105 essentia jar aura color": stage105_essentia_jar_aura_color_audit(),
        "Stage106 essentia transport": stage106_essentia_transport_audit(),
        "Stage107 directional essentia tubes": stage107_directional_essentia_tubes_audit(),
        "Stage108 tube suction backflow": stage108_tube_suction_backflow_audit(),
        "Stage109 compile syntax fix": stage109_compile_syntax_fix_audit(),
    }

    total_errors = 0
    for name, errors in checks.items():
        if errors:
            total_errors += len(errors)
            print(f"\n{name}: FAILED ({len(errors)} errors)")
            for error in errors[:100]:
                print(f"  - {error}")
        else:
            print(f"{name}: OK")

    if total_errors:
        fail(f"Static audit failed with {total_errors} errors")

    print("\nStatic audit passed successfully.")


if __name__ == "__main__":
    main()
