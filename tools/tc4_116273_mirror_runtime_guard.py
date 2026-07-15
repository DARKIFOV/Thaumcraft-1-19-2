#!/usr/bin/env python3
"""Regression guard for v11.62.83 TC4 stationary/essentia/hand mirror runtime."""
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []
checks = 0

def exists(rel):
    global checks
    checks += 1
    if not (ROOT / rel).is_file():
        errors.append(f"missing file: {rel}")

def need(rel, token):
    global checks
    checks += 1
    p = ROOT / rel
    if not p.is_file():
        errors.append(f"missing file: {rel}")
        return
    text = p.read_text(encoding="utf-8")
    if token not in text:
        errors.append(f"{rel}: missing {token!r}")

# 15 file/resource contracts.
for rel in [
    "src/main/java/com/darkifov/thaumcraft/mirror/MirrorLink.java",
    "src/main/java/com/darkifov/thaumcraft/mirror/AbstractMirrorBlockEntity.java",
    "src/main/java/com/darkifov/thaumcraft/mirror/MirrorBlockEntity.java",
    "src/main/java/com/darkifov/thaumcraft/mirror/EssentiaMirrorBlockEntity.java",
    "src/main/java/com/darkifov/thaumcraft/block/MirrorBlock.java",
    "src/main/java/com/darkifov/thaumcraft/block/MirrorBlockItem.java",
    "src/main/java/com/darkifov/thaumcraft/block/HandMirrorItem.java",
    "src/main/java/com/darkifov/thaumcraft/menu/HandMirrorMenu.java",
    "src/main/java/com/darkifov/thaumcraft/client/screen/HandMirrorScreen.java",
    "src/main/resources/assets/thaumcraft/blockstates/tc4_mirrorframe.json",
    "src/main/resources/assets/thaumcraft/blockstates/tc4_mirrorframe2.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/tc4_mirrorframe.json",
    "src/main/resources/data/thaumcraft/loot_tables/blocks/tc4_mirrorframe2.json",
    "src/main/resources/assets/thaumcraft/textures/gui/guihandmirror.png",
    "src/main/resources/assets/thaumcraft/textures/block/tc4/mirrorpanetrans.png",
]: exists(rel)

# 12 release/registration contracts.
for rel, token in [
    ("build.gradle", "version = '11.62.83'"),
    ("src/main/resources/META-INF/mods.toml", 'version="11.62.83"'),
    ("README.md", "v11.62.73 Magic Mirror"),
    (".github/workflows/build.yml", "tc4_116273_mirror_runtime_guard.py"),
    (".github/workflows/release.yml", "tc4_116273_mirror_runtime_guard.py"),
    ("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java", 'BLOCKS.register("tc4_mirrorframe"'),
    ("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java", 'BLOCKS.register("tc4_mirrorframe2"'),
    ("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java", 'ITEMS.register("tc4_mirrorhand"'),
    ("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java", 'BLOCK_ENTITIES.register("tc4_mirror"'),
    ("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java", 'BLOCK_ENTITIES.register("tc4_mirror_essentia"'),
    ("src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java", 'MENUS.register("hand_mirror"'),
    ("src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java", "HAND_MIRROR_MENU"),
]: need(rel, token)

# 12 dimension-safe link/NBT contracts.
for token in [
    "record MirrorLink", "linkX", "linkY", "linkZ", "linkDim", "dimname",
    "ResourceKey.create(Registry.DIMENSION_REGISTRY", "legacyDimension", "Level.NETHER",
    "resolveLevel", "has(ItemStack", "clear(ItemStack",
]: need("src/main/java/com/darkifov/thaumcraft/mirror/MirrorLink.java", token)

# 15 reciprocal lifecycle/chunk-safety contracts.
for token in [
    "INITIAL_RETRY = 40", "MAX_RETRY = 600", "setPendingLink", "isLinkValidSimple",
    "isLinkValid()", "restoreLink()", "invalidateLink()", "retryInterval + 20",
    "acceptsPeer", "peer.linked = true", "peer.link = reciprocal", "targetLevel.hasChunkAt",
    "MirrorBlock.LINKED", 'tag.putBoolean("linked"', "ClientboundBlockEntityDataPacket.create",
    "dropLinkSnapshot", "linkForDrop()", "peer.retryInterval = INITIAL_RETRY",
]: need("src/main/java/com/darkifov/thaumcraft/mirror/AbstractMirrorBlockEntity.java", token)

# 22 regular mirror transport/instability/queue contracts.
for token in [
    "implements WorldlyContainer", "entity.hasPickUpDelay", "targetMirror.addStack",
    "addInstability(null, transported.getCount())", "outputStacks", 'tag.put("Items"',
    "mirrorTicks <= 20", "level.random.nextInt", "single.setCount(1)", "setPickUpDelay(20)",
    "normal.scale(0.15D)", "instability / 50", "mirrorTicks % 20", "Aspect.ORDO",
    "drainMachineVis", "addInstability(serverLevel, -1)", "getContainerSize() { return 1; }",
    "setItem(int slot", "target.addStack", "spawnOutFront", "canPlaceItemThroughFace",
    "tickLinkLifecycle", "ForgeCapabilities.ITEM_HANDLER", "LazyOptional<IItemHandler>",
    "insertItem(int slot", "routeInsertedStack", "itemCapability.cast()", "invalidateCaps()",
    "reviveCaps()",
]: need("src/main/java/com/darkifov/thaumcraft/mirror/MirrorBlockEntity.java", token)

# 22 essentia mirror parity contracts.
for token in [
    "RANGE = 8", "peekRemoteAspect", "takeRemoteEssentia", "amount != 1",
    "restoreRemoteEssentia", "findRemoteSource", "isLinkValid()", "targetLevel.hasChunkAt",
    "MirrorBlock.FACING", "for (int aa = -RANGE", "for (int bb = -RANGE",
    "for (int cc = 0; cc < RANGE", "orientedOffset", "direction.getStepY()",
    "direction.getStepX()", "EssentiaJarBlockEntity", "EssentiaReservoirBlockEntity",
    "AlembicBlockEntity", "EssentiaTubeBlockEntity", "AlchemicalFurnaceBlockEntity",
    "AlchemicalCentrifugeBlockEntity", "candidate instanceof EssentiaMirrorBlockEntity",
]: need("src/main/java/com/darkifov/thaumcraft/mirror/EssentiaMirrorBlockEntity.java", token)

# 18 six-direction block/state/drop contracts.
for token in [
    "BlockStateProperties.FACING", 'BooleanProperty.create("linked")', "Kind { ITEM, ESSENTIA }",
    "context.getClickedFace", "isFaceSturdy", "level.destroyBlock(pos, true)",
    "createTickerHelper", "MirrorBlockEntity::serverTick", "EssentiaMirrorBlockEntity::serverTick",
    "case DOWN -> DOWN", "case UP -> UP", "case NORTH -> NORTH", "case SOUTH -> SOUTH",
    "case WEST -> WEST", "case EAST -> EAST", "Shapes.empty()", "MirrorLink.read(stack)",
    "mirror.invalidateLink", "mirror.linkForDrop()", "dropLink.write(stack)",
]: need("src/main/java/com/darkifov/thaumcraft/block/MirrorBlock.java", token)

# 8 stationary item pairing contracts.
for token in [
    "mirror.isLinkValid()", "linkedStack.setCount(1)", "MirrorLink.at", "player.getInventory().add",
    "context.getItemInHand().shrink(1)", 'TC4Sounds.event("jar")', "isFoil", "linked_to",
]: need("src/main/java/com/darkifov/thaumcraft/block/MirrorBlockItem.java", token)

# 14 hand mirror binding/transport contracts.
for token in [
    "instanceof MirrorBlockEntity", "MirrorLink.at", "NetworkHooks.openScreen", "writeBoolean(mainHand)",
    "HandMirrorMenu", "transport(ItemStack mirror", "items.getItem() instanceof HandMirrorItem",
    "target.mirror().spawnDirect", "targetLevel.hasChunkAt", "MirrorLink.clear", 'TC4Sounds.event("zap")',
    "SoundEvents.ENDERMAN_TELEPORT", "isFoil", "tc.handmirrorlinkedto",
]: need("src/main/java/com/darkifov/thaumcraft/block/HandMirrorItem.java", token)

# 10 exact original menu contracts.
for token in [
    "new SimpleContainer(1)", "new Slot(input, 0, 80, 24)", "84 + row * 18", "142",
    "input.addListener", "HandMirrorItem.transport", "input.setItem(0, ItemStack.EMPTY)",
    "quickMoveStack", "instanceof HandMirrorItem", "input.removeItemNoUpdate(0)",
    "player.level.isClientSide",
]: need("src/main/java/com/darkifov/thaumcraft/menu/HandMirrorMenu.java", token)

# 5 original GUI contracts.
for token in [
    "textures/gui/guihandmirror.png", "imageWidth = 176", "imageHeight = 166",
    "blit(poseStack, leftPos, topPos", "renderLabels",
]: need("src/main/java/com/darkifov/thaumcraft/client/screen/HandMirrorScreen.java", token)

# 5 tube integration/rollback contracts.
for token in [
    "EssentiaMirrorBlockEntity", "mirror.peekRemoteAspect", "new MirrorSource", "mirror.takeRemoteEssentia",
    "mirror.restoreRemoteEssentia",
]: need("src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java", token)

# 5 aggregate resource contracts = 163 total.
checks += 1
for block in ("tc4_mirrorframe", "tc4_mirrorframe2"):
    data = json.loads((ROOT / f"src/main/resources/assets/thaumcraft/blockstates/{block}.json").read_text())
    if len(data.get("variants", {})) != 12:
        errors.append(f"{block}: expected 12 facing/linked variants")
checks += 1
models = list((ROOT / "src/main/resources/assets/thaumcraft/models/block").glob("tc4_mirrorframe*.json"))
if len(models) != 24:
    errors.append(f"expected 24 mirror block models, got {len(models)}")
checks += 1
for lang in ("en_us.json", "ru_ru.json"):
    data = json.loads((ROOT / "src/main/resources/assets/thaumcraft/lang" / lang).read_text())
    if "container.thaumcraft.hand_mirror" not in data or "block.thaumcraft.tc4_mirrorframe2" not in data:
        errors.append(f"{lang}: missing mirror translations")
checks += 1
if not (ROOT / "src/main/resources/assets/thaumcraft/models/item/tc4_mirrorhand.json").is_file():
    errors.append("missing hand mirror item model")
checks += 1
if "RenderType.translucent()" not in (ROOT / "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java").read_text():
    errors.append("missing translucent mirror render layer")

if checks != 176:
    errors.append(f"guard definition drift: expected 176 checks, got {checks}")

if errors:
    print(f"TC4 11.62.83 mirror runtime guard: FAIL ({len(errors)} problems; {checks} checks)")
    for error in errors:
        print(" -", error)
    sys.exit(1)
print(f"TC4 11.62.83 mirror runtime guard: PASS ({checks}/176 checks)")
