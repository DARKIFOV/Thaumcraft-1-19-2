from pathlib import Path

root = Path(__file__).resolve().parents[1]
checks = {
    "item class": root / "src/main/java/com/darkifov/thaumcraft/item/TC4PrimordialPearlItem.java",
    "node class": root / "src/main/java/com/darkifov/thaumcraft/blockentity/AuraNodeBlockEntity.java",
    "registry": root / "src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java",
    "world package": root / "src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java",
}
for label, path in checks.items():
    assert path.is_file(), f"missing {label}: {path}"
item = checks["item class"].read_text(encoding="utf-8")
node = checks["node class"].read_text(encoding="utf-8")
registry = checks["registry"].read_text(encoding="utf-8")
main = (root / "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java").read_text(encoding="utf-8")
assert "PlayerThaumData.hasResearch" in item and '"PRIMNODE"' in item
assert "33" in item and "FLUX_GOO" in item and "FLUX_GAS" in item
assert "applyPrimordialPearl" in node and "AuraNodeModifier.BRIGHT" in node
assert 'case "tc4_eldritch_object_3" -> new TC4PrimordialPearlItem' in registry
assert 'specialItem("primordial_pearl"' in main and "new TC4PrimordialPearlItem" in main
print("TC4 v11.63.34 primordial pearl guard: PASS")
