#!/usr/bin/env python3
"""Static guard for v11.62.96 original Alembic and Vis Relay OBJ/UV rendering."""
from __future__ import annotations

import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/com/darkifov/thaumcraft"
RES = ROOT / "src/main/resources/assets/thaumcraft"

checks: list[tuple[str, bool, str]] = []

def add(name: str, ok: bool, detail: str = "") -> None:
    checks.append((name, ok, detail))

def text(path: Path) -> str:
    return path.read_text(encoding="utf-8")

def same(a: Path, b: Path) -> bool:
    return a.exists() and b.exists() and hashlib.sha256(a.read_bytes()).digest() == hashlib.sha256(b.read_bytes()).digest()

build = text(ROOT / "build.gradle")
add("project version", "version = '11.62.96'" in build)

alembic_block = text(JAVA / "block/AlembicBlock.java")
alembic_renderer = text(JAVA / "client/render/AlembicRenderer.java")
alembic_model = text(JAVA / "client/render/model/TC4AlembicModel.java")
alembic_item = text(JAVA / "block/AlembicBlockItem.java")
alembic_item_renderer = text(JAVA / "client/render/AlembicItemRenderer.java")

add("Alembic uses animated BER path", "RenderShape.ENTITYBLOCK_ANIMATED" in alembic_block)
add("Alembic exact OBJ model groups", all(token in alembic_model for token in ["POT", "LEGS", "TUBEMAIN", "TUBESMALL", "PANEL", "renderAll"]))
add("Alembic OBJ Z-to-Y transform", "rotationDegrees(-90.0F)" in alembic_renderer)
add("Alembic original texture route", 'textures/models/alembic.png' in alembic_renderer)
add("Alembic fill retained", all(token in alembic_renderer for token in ["FILL_TEXTURE", "renderLiquidBox", "AlembicBlockEntity.CAPACITY"]))
add("Alembic BEWLR item", all(token in alembic_item for token in ["initializeClient", "AlembicItemRenderer", "getCustomRenderer"]) and "renderStandalone" in alembic_item_renderer)

relay_block = text(JAVA / "block/VisRelayBlock.java")
relay_be = text(JAVA / "blockentity/VisRelayBlockEntity.java")
relay_renderer = text(JAVA / "client/render/VisRelayRenderer.java")
relay_item = text(JAVA / "block/VisRelayBlockItem.java")
relay_model = text(JAVA / "client/render/model/TC4VisRelayModel.java")
mod = text(JAVA / "ThaumcraftMod.java")
client = text(JAVA / "client/ClientModEvents.java")

add("Vis Relay is animated block entity", all(token in relay_block for token in ["extends BaseEntityBlock", "newBlockEntity", "RenderShape.ENTITYBLOCK_ANIMATED"]))
add("Vis Relay block entity registered", all(token in mod for token in ["VIS_RELAY_BLOCK_ENTITY", "VisRelayBlockEntity::new", "VisRelayBlockItem"]))
add("Vis Relay renderer registered", all(token in client for token in ["VIS_RELAY_BLOCK_ENTITY", "VisRelayRenderer::new"]))
add("Vis Relay exact OBJ route", "VisChargeRelayRenderer.renderStandalone" in relay_renderer and "VIS_RELAY_BLOCK_ENTITY" in relay_be)
add("Vis Relay BEWLR item", all(token in relay_item for token in ["initializeClient", "VisChargeRelayItemRenderer", "getCustomRenderer"]))

# Both generated OBJ renderers must preserve triangles in a QUADS buffer.
for label, source in [("Alembic", alembic_model), ("Vis Relay", relay_model)]:
    add(f"{label} degenerate quad emission", all(token in source for token in ["i += 24", "i + 8", "i + 16", "emit(data, i + 16"]))

for name in ["alembic", "vis_relay"]:
    block_model = json.loads(text(RES / f"models/block/{name}.json"))
    item_model = json.loads(text(RES / f"models/item/{name}.json"))
    add(f"{name} block no cuboid placeholder", "elements" not in block_model and block_model.get("parent") != "minecraft:block/cube_all")
    add(f"{name} item builtin entity", item_model.get("parent") == "builtin/entity")

for asset in ["alembic.obj", "alembic.png", "vis_relay.obj", "vis_relay.png"]:
    active = RES / "textures/models" / asset
    original = RES / "textures/original/thaumcraft4/models" / asset
    add(f"byte-exact original {asset}", same(active, original))

problems = [{"name": n, "detail": d} for n, ok, d in checks if not ok]
report = {
    "version": "11.62.96",
    "status": "PASS" if not problems else "FAIL",
    "checks": [{"name": n, "status": "PASS" if ok else "FAIL", "detail": d} for n, ok, d in checks],
    "problems": problems,
    "limitations": [
        "Static source/resource contract only.",
        "Client lighting, culling, transforms and liquid depth require runtime screenshots."
    ],
}
reports = ROOT / "reports"
reports.mkdir(exist_ok=True)
out = reports / "tc4_116294_alembic_vis_relay_texture_uv_guard.json"
out.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(f"v11.62.96 Alembic/Vis Relay guard: {len(checks)-len(problems)}/{len(checks)} PASS")
print(out.relative_to(ROOT))
if problems:
    for p in problems:
        print(f"FAIL: {p['name']} {p['detail']}")
raise SystemExit(1 if problems else 0)
