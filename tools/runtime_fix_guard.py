#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
java = root / "src/main/java/com/darkifov/thaumcraft"
assets = root / "src/main/resources/assets/thaumcraft"
problems = []

wand = (java / "client/render/WandItemRenderer.java").read_text()
for token in [
    "translate(0.50D, 1.50D, 0.50D)",
    "scale(1.00F, 1.10F, 1.00F)",
    "Vector3f.XP.rotationDegrees(180.0F)",
]:
    if token not in wand:
        problems.append(f"held wand original transform missing: {token}")
for forbidden in ["MODEL_CENTER_Y", "left ? -0.11D : 0.11D"]:
    if forbidden in wand:
        problems.append(f"held wand screenshot regression returned: {forbidden}")

node = (java / "client/render/AuraNodeRenderer.java").read_text()
node_types = (java / "client/render/TC4NodeRenderTypes.java").read_text()
for token in ["TC4NodeRenderTypes.node", "LEQUAL_DEPTH_TEST", "NO_DEPTH_TEST", "ADDITIVE_TRANSPARENCY", "COLOR_WRITE"]:
    if token not in node + node_types:
        problems.append(f"standalone node render path missing: {token}")
if "RenderType.eyes(TC4AuraNodeHudParity.ORIGINAL_NODES)" in node:
    problems.append("aura node still uses the entity-eyes equal-depth render type")

meter = (java / "block/ThaumometerItem.java").read_text()
events = (java / "event/CommonEvents.java").read_text()
for token in ["TAG_PENDING_SCAN_START", "serverTickPendingScan", "REQUIRED_STABLE_TICKS = 20"]:
    if token not in meter:
        problems.append(f"Thaumometer stable server tick missing: {token}")
if "TickEvent.PlayerTickEvent" not in events or "serverTickPendingScan" not in events:
    problems.append("Thaumometer Forge interaction fallback is not wired to player ticks")

mod = (java / "ThaumcraftMod.java").read_text()
cheat = java / "block/CreativeThaumonomiconItem.java"
if "THAUMONOMICON_CHEAT" not in mod or not cheat.exists():
    problems.append("functional complete-knowledge Thaumonomicon is not registered")
elif "ResearchRegistry.entries()" not in cheat.read_text():
    problems.append("complete-knowledge Thaumonomicon does not unlock the registry")
if not (assets / "models/item/thaumonomicon_cheat.json").exists():
    problems.append("complete-knowledge Thaumonomicon model is missing")

nitor = (java / "block/NitorLightBlock.java").read_text()
if "RenderShape.INVISIBLE" not in nitor:
    problems.append("Nitor returned to a visible crossed-block model")
if "ParticleTypes.FLAME" in nitor:
    problems.append("Nitor still uses the vanilla flame placeholder")

if problems:
    print("Runtime fix guard: FAILED")
    for problem in problems:
        print(" -", problem)
    raise SystemExit(1)
print("Runtime fix guard: OK (wand, node, Thaumometer, research cheat book, Nitor)")
