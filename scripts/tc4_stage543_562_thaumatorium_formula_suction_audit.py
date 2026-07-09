#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    path = ROOT / rel
    if not path.exists():
        raise SystemExit(f"MISSING: {rel}")
    return path.read_text(encoding="utf-8")

def require(rel: str, *needles: str) -> None:
    data = read(rel)
    for needle in needles:
        if needle not in data:
            raise SystemExit(f"AUDIT FAIL: {rel} missing {needle!r}")

require("build.gradle", "version = '5.62.0'", "version = '5.42.0'")
require("src/main/resources/META-INF/mods.toml", 'version="5.62.0"', 'version="5.42.0"')
require(
    "src/main/java/com/darkifov/thaumcraft/blockentity/ThaumatoriumBlockEntity.java",
    "Stage503-562 TC4 Thaumatorium adapter",
    "selectFormulaIndex",
    "selectedFormulaIndex",
    "emitOriginalCraftEffects",
    'TC4Sounds.event("craftstart")',
    "ParticleTypes.WITCH",
)
require(
    "src/main/java/com/darkifov/thaumcraft/network/RequestThaumatoriumFormulaPacket.java",
    "original GuiThaumatorium hotzone adapter",
    "ThaumatoriumMenu",
    "selectFormulaIndex",
    'TC4Sounds.event("brain")',
)
require(
    "src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java",
    "RequestThaumatoriumFormulaPacket.class",
    "requestThaumatoriumFormulaFromClient",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/ThaumatoriumScreen.java",
    "Stage523-562 original GuiThaumatorium visual adapter",
    "mouseClicked",
    "FORMULA_SLOTS",
    "requestThaumatoriumFormulaFromClient",
    "recipe.resultItemId()",
)
require(
    "src/main/java/com/darkifov/thaumcraft/alchemy/AlchemyRecipe.java",
    "resultItemId()",
    "resultCount()",
)
require(
    "src/main/java/com/darkifov/thaumcraft/essentia/TC4EssentiaNetworkRuntime.java",
    "Shared Stage503-562 helper",
    "allowsOutputTo(direction.getOpposite())",
    "allowsInputFrom(direction)",
    "ContainerRef::sourcePriority",
    "EssentiaSuction.ALEMBIC_SOURCE_PRIORITY",
    "EssentiaSuction.RESERVOIR_SOURCE_PRIORITY",
    "EssentiaSuction.JAR_SOURCE_PRIORITY",
)
require(
    "STAGE543_562_TC4_THAUMATORIUM_FORMULA_SUCTION_REPORT.json",
    "Stage543-562",
    "RequestThaumatoriumFormulaPacket",
    "directional tube traversal",
)
require("docs/NEXT_CHAT_PROMPT_STAGE562.md", "Stage543-562", "Stage563-582")

print("Stage543-562 thaumatorium formula/suction audit: OK")
