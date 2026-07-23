#!/usr/bin/env python3
"""Regression guard for v11.62.46 audits and aura-node item parity fixes."""
from pathlib import Path
import json
import re

root = Path(__file__).resolve().parents[1]
errors = []

def require(path: str, token: str) -> None:
    file = root / path
    text = file.read_text(encoding="utf-8") if file.is_file() else ""
    if token not in text:
        errors.append(f"{path}: missing {token!r}")

build_text = (root / "build.gradle").read_text(encoding="utf-8")
mods_text = (root / "src/main/resources/META-INF/mods.toml").read_text(encoding="utf-8")
build_match = re.search(r"^version\s*=\s*'([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?'", build_text, re.MULTILINE)
mods_match = re.search(r'^version="([0-9]+(?:\.[0-9]+){2})(?:-[A-Za-z0-9.-]+)?"', mods_text, re.MULTILINE)
def version_tuple(value: str) -> tuple[int, ...]:
    return tuple(int(part) for part in value.split("."))
for label, match in (("build.gradle", build_match), ("mods.toml", mods_match)):
    if match is None or version_tuple(match.group(1)) < (11, 62, 46):
        errors.append(f"{label}: current version must preserve v11.62.46 or later")
require("src/main/java/com/darkifov/thaumcraft/client/render/NodeStabilizerRenderer.java", "tc4LightCoordinate")
require("src/main/java/com/darkifov/thaumcraft/client/render/NodeStabilizerRenderer.java", "TC4NodeRenderTypes.node(BUBBLE, true, false)")
require("src/main/java/com/darkifov/thaumcraft/client/render/NodeJarItemRenderer.java", "nanoTime / 40_000_000L + 1L")
require("src/main/java/com/darkifov/thaumcraft/client/render/NodeJarItemRenderer.java", "typeScale, 0xFFFFFFFF")
for tool in ("model_transform_audit.py", "bewlr_contract_audit.py", "aura_node_parity_audit.py"):
    if not (root / "tools" / tool).is_file():
        errors.append(f"tools/{tool}: missing")

models = root / "src/main/resources/assets/thaumcraft/models/item"
dynamic = []
for path in models.rglob("*.json"):
    data = json.loads(path.read_text(encoding="utf-8"))
    parent = data.get("parent", "")
    if parent in ("builtin/entity", "minecraft:builtin/entity"):
        dynamic.append(path.stem)
expected_dynamic = {
    "advanced_node_stabilizer",
    "alchemical_centrifuge",
    "alembic",
    "avaritia_creative_wand",
    "bellows",
    "essentia_jar",
    "filtered_essentia_jar",
    "greatwood_wand",
    "hungry_chest",
    "infusion_matrix",
    "iron_capped_wooden_wand",
    "node_jar",
    "node_stabilizer",
    "node_transducer",
    "silverwood_wand",
    "tallow_candle",
    "tallow_candle_orange",
    "tallow_candle_magenta",
    "tallow_candle_light_blue",
    "tallow_candle_yellow",
    "tallow_candle_lime",
    "tallow_candle_pink",
    "tallow_candle_gray",
    "tallow_candle_light_gray",
    "tallow_candle_cyan",
    "tallow_candle_purple",
    "tallow_candle_blue",
    "tallow_candle_brown",
    "tallow_candle_green",
    "tallow_candle_red",
    "tallow_candle_black",
    "tc4_block_banner",
    "tc4_jar_brain",
    "tc4_travel_trunk",
    "thaumometer",
    "vis_charge_relay",
    "vis_relay",
    "void_essentia_jar",
}
actual_dynamic = set(dynamic)
missing_dynamic = sorted(expected_dynamic - actual_dynamic)
unexpected_dynamic = sorted(actual_dynamic - expected_dynamic)
if missing_dynamic or unexpected_dynamic:
    errors.append(
        "builtin/entity model set drift: "
        f"missing={missing_dynamic}, unexpected={unexpected_dynamic}, actual={sorted(actual_dynamic)}"
    )

creative_model = models / "avaritia_creative_wand.json"
creative_data = json.loads(creative_model.read_text(encoding="utf-8")) if creative_model.is_file() else {}
if creative_data.get("display"):
    errors.append("avaritia_creative_wand.json: builtin/entity must not duplicate BEWLR TransformType display")
if creative_data.get("textures", {}).get("particle") != "thaumcraft:item/avaritia_creative_wand":
    errors.append("avaritia_creative_wand.json: missing canonical particle texture")

if errors:
    print("v11.62.46 audit guard: FAIL")
    for error in errors:
        print(" -", error)
    raise SystemExit(1)
print("v11.62.46 audit guard: OK (v11.62.46+ metadata, 38 registered BEWLR models, node jar/stabilizer parity, no duplicate creative-wand display)")
