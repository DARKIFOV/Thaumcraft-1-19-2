#!/usr/bin/env python3
"""Regression checks for the v11.62.45 Forge 1.19.2 parity fixes."""
from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
read = lambda rel: (root / rel).read_text(encoding="utf-8")
errors: list[str] = []

def require(text: str, token: str, label: str) -> None:
    if token not in text:
        errors.append(f"{label}: missing {token}")

def forbid(text: str, token: str, label: str) -> None:
    if token in text:
        errors.append(f"{label}: forbidden legacy token {token}")

packet = read("src/main/java/com/darkifov/thaumcraft/network/RequestCompleteSelectedResearchPacket.java")
for token in [
    "private final String researchKey",
    "buffer.writeUtf(packet.researchKey)",
    "OriginalResearchBridge.byKey(packet.researchKey)",
    "TC4ResearchFlagPolicy.isSecondary(entry)",
    "OriginalResearchBridge.completeWithAspectCost(player, entry)",
    "TC4ResearchNoteCreator.create(player, entry)",
]:
    require(packet, token, "research packet")
for token in ["selectedOrFirstAvailable(player)", "new RequestCompleteSelectedResearchPacket()"]:
    forbid(packet, token, "research packet")

network = read("src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java")
require(network, 'PROTOCOL_VERSION = "2"', "network")
require(network, "requestCompleteSelectedResearchFromClient(String researchKey)", "network")
require(network, "new RequestCompleteSelectedResearchPacket(researchKey)", "network")

layout = read("src/main/java/com/darkifov/thaumcraft/client/screen/OriginalResearchLayout.java")
require(layout, "return TC4ResearchFlagPolicy.isSecondary(entry);", "research layout")
require(layout, "return TC4ResearchFlagPolicy.has(entry, TC4ResearchFlagPolicy.ROUND);", "research layout")
forbid(layout, "|| secondary(entry)", "research layout")

book = read("src/main/java/com/darkifov/thaumcraft/client/screen/ThaumonomiconScreen.java")
for token in [
    'unlocked.contains("ELDRITCHMINOR")',
    "u = secondary ? 230 : 86",
    "float iconBrightness = complete ? 1.0F : available ? frameBrightness : 0.10F",
    "requestCompleteSelectedResearchFromClient(selected.key())",
    "Math.round(153.0F * (active ? 1.0F : phase))",
]:
    require(book, token, "Thaumonomicon")
for token in ["renderBrowserHeader", "requestSelectResearchFromClient(selected.key())", "0xAAFFE08A"]:
    forbid(book, token, "Thaumonomicon")

scanner = read("src/main/java/com/darkifov/thaumcraft/client/render/ThaumometerItemRenderer.java")
for token in [
    "ASPECT_RELATIVE_SCALE = 0.0075F / SCANNER_READOUT_SCALE",
    "int baseX = Math.min(5, remaining) * 8",
    "remaining -= Math.max(0, 5 - posY)",
    "poseStack.scale(ASPECT_RELATIVE_SCALE, ASPECT_RELATIVE_SCALE, 1.0F)",
]:
    require(scanner, token, "Thaumometer")
forbid(scanner, "float baseX = capacity * 8.0F", "Thaumometer")

node = read("src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java")
for token in [
    "long originalAnimationTime = nanoTime / 5_000_000L",
    "Math.floorMod(originalAnimationTime, rotationPeriod)",
    "viewer.getEyePosition(partialTick)",
    "viewer.getViewVector(partialTick)",
]:
    require(node, token, "aura node")
forbid(node, "nanoTime / 5_000_000.0F", "aura node")

wand = read("src/main/java/com/darkifov/thaumcraft/client/render/WandItemRenderer.java")
for token in [
    "private int originalBlockGlow",
    "int ambientBlock = (packedLight >> 4) & 15",
    "int ambientSky = (packedLight >> 20) & 15",
    "originalBlockGlow(packedLight, focusLightmapCoordinate)",
    "originalBlockGlow(packedLight, 200)",
]:
    require(wand, token, "wand")
forbid(wand, "Math.max(packedLight, 15728880)", "wand")

if errors:
    print("v11.62.45 parity guard: FAIL")
    for error in errors:
        print(" -", error)
    sys.exit(1)
print("v11.62.45 parity guard: OK (research transaction, book GUI, scanner, nodes, wand lighting)")
