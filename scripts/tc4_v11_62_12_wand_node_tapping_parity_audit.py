#!/usr/bin/env python3
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []


def read(path: str) -> str:
    file = ROOT / path
    if not file.exists():
        errors.append(f"missing file: {path}")
        return ""
    return file.read_text(encoding="utf-8")


def must(path: str, *needles: str) -> None:
    text = read(path)
    for needle in needles:
        if needle not in text:
            errors.append(f"{path} missing {needle!r}")


def must_not(path: str, *needles: str) -> None:
    text = read(path)
    for needle in needles:
        if needle in text:
            errors.append(f"{path} must not contain {needle!r}")


def must_exist(path: str) -> None:
    if not (ROOT / path).exists():
        errors.append(f"missing file: {path}")


must(
    "build.gradle",
    "version = '11.62.12'",
    "tc4_wand_node_continuous_tap_root_vis_hud_recharge",
)
must(
    "src/main/resources/META-INF/mods.toml",
    'version="11.62.12"',
    "tc4_wand_node_continuous_tap_root_vis_hud_recharge",
)

wand = "src/main/java/com/darkifov/thaumcraft/block/WandItem.java"
must(
    wand,
    'TAG_NODE_X = "IIUX"',
    'TAG_NODE_Y = "IIUY"',
    'TAG_NODE_Z = "IIUZ"',
    "NODE_TAP_INTERVAL = 5",
    "root.contains(aspect.id())",
    "root.putInt(aspect.id(), value)",
    "migrateLegacyVisStorage",
    'PlayerThaumData.hasResearch(player, "NODETAPPER1")',
    'PlayerThaumData.hasResearch(player, "NODETAPPER2")',
    'PlayerThaumData.hasResearch(player, "NODEPRESERVE")',
    "components.rod() != WandRodType.WOOD",
    "components.cap() != WandCapType.IRON",
    "!player.isShiftKeyDown()",
    "player.getTicksUsingItem() % NODE_TAP_INTERVAL == 0",
    "playerStillTargetsNode",
    "node.drainToWand(aspect, requested)",
    "addVis(wandStack, aspect, drained)",
    "node.markWandDrain(aspect, player)",
    "beginNodeUse(wandStack, level, pos)",
    "player.startUsingItem(context.getHand())",
    "TC4 keeps the use action active when the wand is full",
)
must_not(
    wand,
    "The wand is full or the node has no tappable primal vis.",
)

node_block = "src/main/java/com/darkifov/thaumcraft/block/AuraNodeBlock.java"
must(
    node_block,
    "wandItem.beginNodeUse(stack, level, pos)",
    "player.startUsingItem(hand)",
)
must_not(
    node_block,
    "chargeFromNode(stack, node)",
)

node = "src/main/java/com/darkifov/thaumcraft/blockentity/AuraNodeBlockEntity.java"
must(
    node,
    "case BRIGHT -> 400",
    "case PALE -> 900",
    "case FADING -> 0",
    "default -> 600",
    "interval *= 20",
    "interval *= 2",
    "regenerateOneMissingAspect",
    "applyCatchUpRecharge",
    "regeneration * 75L",
    'tag.putLong("LastActiveMillis", lastActiveMillis)',
    "level.getGameTime() % 1200L == 0L",
    "new java.util.ArrayList<>(baseAspects.entries().keySet())",
    "aspects.removeUpTo(aspect, amount)",
)
must_not(
    node,
    "brightMultiplier",
    "stability = Math.max(0, stability - removed)",
)

hud = "src/main/java/com/darkifov/thaumcraft/client/WandVisOverlayEvents.java"
must(
    hud,
    "original TC4 casting-wand vis dial",
    "TC4AuraNodeHudParity.ORIGINAL_HUD",
    "30.0F * amount / capacity",
    "-15.0F + index * 24.0F",
    "GuiComponent.blit(poseStack, -4, 35 - fill, 104, 0, 8, fill, 256, 256)",
    "GuiComponent.blit(poseStack, -8, -3, 72, 0, 16, 42, 256, 256)",
    "minecraft.player.isShiftKeyDown()",
    "OLD_VIS",
    "120, 0, 8, 8",
    "128, 0, 8, 8",
    "136, 0, 8, 8",
    "WandItem.modifiedVisCost",
)

must(
    "src/main/java/com/darkifov/thaumcraft/client/TC4AuraNodeHudParity.java",
    "ORIGINAL_WISPY",
    "textures/misc/wispy.png",
)
must_exist("src/main/resources/assets/thaumcraft/textures/misc/wispy.png")

must(
    "src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java",
    "node.isRecentlyDrained()",
    "node.lastDrainColor()",
    "node.lastDrainerEntityId()",
    "renderWandDrainBeam",
    "TC4AuraNodeHudParity.ORIGINAL_WISPY",
)

workflow = ".github/workflows/main.yml"
must(
    workflow,
    "tc4_v11_62_12_wand_node_tapping_parity_audit.py",
    "build/libs/*-github.jar",
    "v11.62.12-github-jar",
    "v11.62.12-build-reports",
)
must_not(workflow, "build/libs/*.jar\n")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("TC4 v11.62.12 wand/node tapping parity audit: OK")
