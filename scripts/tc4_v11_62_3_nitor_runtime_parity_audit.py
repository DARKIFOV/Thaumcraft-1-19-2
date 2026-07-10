#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    p = ROOT / rel
    if not p.exists():
        raise SystemExit(f"missing required file: {rel}")
    return p.read_text(encoding='utf-8')

def assert_contains(rel, *needles):
    text = read(rel)
    for needle in needles:
        if needle not in text:
            raise SystemExit(f"{rel} missing required marker: {needle}")
    return text

# The player-facing TC4 resource item must place the internal light block.
assert_contains(
    "src/main/java/com/darkifov/thaumcraft/block/NitorItem.java",
    "class NitorItem extends Item",
    "public InteractionResult useOn(UseOnContext context)",
    "ThaumcraftMod.NITOR_LIGHT.get().defaultBlockState()",
    "level.setBlock(placePos, nitorState, 11)",
    "context.getItemInHand().shrink(1)",
    "InteractionResult.sidedSuccess"
)

# The placed Nitor block must behave like an airy TC4 magical flame/light source, not a solid cube.
assert_contains(
    "src/main/java/com/darkifov/thaumcraft/block/NitorLightBlock.java",
    "class NitorLightBlock extends Block",
    "getShape(",
    "propagatesSkylightDown",
    "getLightBlock",
    "animateTick",
    "ParticleTypes.FLAME",
    "ParticleTypes.END_ROD"
)

# Registry: nitor is a real special item; nitor_light is an internal block without a creative BlockItem.
thaumcraft_mod = read("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java")
for marker in [
    "new NitorItem(new Item.Properties().tab(THAUMCRAFT_TAB))",
    "NITOR_LIGHT = nitorLightBlock(\"nitor_light\"",
    "new NitorLightBlock(properties)",
    ".noCollission()",
    ".noOcclusion()",
    ".lightLevel(state -> 15)"
]:
    if marker not in thaumcraft_mod:
        raise SystemExit(f"ThaumcraftMod.java missing Nitor marker: {marker}")
if "ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(THAUMCRAFT_TAB)))" in thaumcraft_mod.split("private static RegistryObject<Block> nitorLightBlock", 1)[1].split("private static RegistryObject<Block> pillarBlock", 1)[0]:
    raise SystemExit("nitorLightBlock must not register a normal creative-tab BlockItem")

# Client render layer and cross model must make placed Nitor transparent/flame-like.
assert_contains(
    "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java",
    "ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.NITOR_LIGHT.get(), RenderType.cutout())"
)
assert_contains(
    "src/main/resources/assets/thaumcraft/models/block/nitor_light.json",
    '"parent": "minecraft:block/cross"',
    '"cross": "thaumcraft:item/nitor"'
)
assert_contains(
    "src/main/resources/assets/thaumcraft/blockstates/nitor_light.json",
    '"model": "thaumcraft:block/nitor_light"'
)

# Breaking placed Nitor returns the actual Nitor item. The old separate recipe/model must not exist.
assert_contains(
    "src/main/resources/data/thaumcraft/loot_tables/blocks/nitor_light.json",
    '"name": "thaumcraft:nitor"'
)
for rel in [
    "src/main/resources/data/thaumcraft/recipes/nitor_light.json",
    "src/main/resources/assets/thaumcraft/models/item/nitor_light.json"
]:
    if (ROOT / rel).exists():
        raise SystemExit(f"obsolete placeholder file must be removed: {rel}")

# GitHub artifact should upload only the playable reobfuscated jar.
assert_contains(
    ".github/workflows/main.yml",
    "build/libs/*-github.jar"
)
if "build/libs/*.jar" in read(".github/workflows/main.yml"):
    raise SystemExit("workflow still uploads every jar; expected only *-github.jar")

print("TC4 v11.62.3 Nitor runtime parity audit: OK")
