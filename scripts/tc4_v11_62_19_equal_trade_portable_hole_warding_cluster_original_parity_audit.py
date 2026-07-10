#!/usr/bin/env python3
"""v11.62.19 Equal Trade, Portable Hole, Warding and counted-cluster parity audit."""
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []


def require(path: str, *tokens: str) -> str:
    p = ROOT / path
    if not p.is_file():
        errors.append(f"missing file: {path}")
        return ""
    text = p.read_text(encoding="utf-8")
    for token in tokens:
        if token not in text:
            errors.append(f"{path} missing {token!r}")
    return text


def load_json(path: str):
    p = ROOT / path
    try:
        return json.loads(p.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"invalid/missing JSON {path}: {exc}")
        return {}


require("build.gradle", "version = '11.62.19'", "version = '11.62.18'")
require("src/main/resources/META-INF/mods.toml", 'version="11.62.19"', 'version="11.62.18"')

runtime = require(
    "src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java",
    "case PORTABLE_HOLE -> castPortableHole(wandStack, level, player)",
    "case EQUAL_TRADE -> castEqualTrade(wandStack, level, player)",
    "case WARDING -> castWarding(wandStack, level, player)",
    "case PORTABLE_HOLE -> cost(Aspect.PERDITIO, 10, Aspect.AER, 10)",
    "cost(Aspect.PERDITIO, 6, Aspect.TERRA, 6, Aspect.ORDO, 6, Aspect.AER, 1, Aspect.IGNIS, 1, Aspect.AQUA, 1)",
    "cost(Aspect.PERDITIO, 5, Aspect.TERRA, 5, Aspect.ORDO, 5)",
    "case WARDING -> cost(Aspect.TERRA, 25, Aspect.ORDO, 25, Aspect.AQUA, 10)",
    "int maxDistance = 33 + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE) * 8",
    "int duration = 120 + focusUpgradeLevel(wandStack, FocusUpgradeType.EXTEND) * 60",
    "distance + 1, face, player",
    "scaledCost(focusVisCost(wandStack, WandFocusType.PORTABLE_HOLE, level.random), distance)",
    "SoundEvents.ENDERMAN_TELEPORT",
    "TC4OuterLandsDimensionAdapter.isOuterLands",
    "0x857B93",
    "EqualTradeSwapRuntime.enqueue",
    "3 + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE)",
    "public static boolean queueEqualTradeSwing(ServerPlayer player, InteractionHand hand, BlockPos target)",
    "0xFFE9CF",
    "now + 500L",
    "WardedBlockRuntime.unward(level, target, player)",
    "WardedBlockRuntime.rollbackWard(level, target, player)",
    "0xFCA000",
)
if "hardness > 20.0F" in runtime:
    errors.append("obsolete invented hardness cap returned in focus runtime")

trade = require(
    "src/main/java/com/darkifov/thaumcraft/wand/EqualTradeSwapRuntime.java",
    "Map<ServerLevel, ArrayDeque<SwapTask>> QUEUES",
    "while (!queue.isEmpty())",
    "if (trySwap(level, player, wand, task, queue, positions)) break",
    "WandFocusRuntime.getFocus(wand) != WandFocusType.EQUAL_TRADE",
    "current.equals(task.sourceState())",
    "current.hasBlockEntity()",
    "WardedBlockRuntime.isWarded(level, task.pos())",
    "level.mayInteract(player, task.pos())",
    "player.mayUseItemAt(task.pos(), Direction.UP, wand)",
    "BlockEvent.BreakEvent",
    "MinecraftForge.EVENT_BUS.post(breakEvent)",
    "syntheticHarvestTool(wand)",
    "Enchantments.SILK_TOUCH, 1",
    "Enchantments.BLOCK_FORTUNE, treasure",
    "player.getInventory().getItem(itemSlot).shrink(1)",
    "refundTargetItem(player, itemSlot, task.targetItem())",
    "consumeFocusVis(wand, player, WandFocusType.EQUAL_TRADE, cost)",
    "level.setBlock(task.pos(), current, Block.UPDATE_ALL)",
    "if (task.lifespan() > 0)",
    "for (int dx = -1; dx <= 1; dx++)",
    "for (int dy = -1; dy <= 1; dy++)",
    "for (int dz = -1; dz <= 1; dz++)",
    "!neighbour.equals(task.sourceState()) || !isExposed(level, next)",
    "return slot == -1 ? player.getOffhandItem()",
)
for forbidden in ("destroyBlock(task.pos()", "level.removeBlock(task.pos()"):
    if forbidden in trade:
        errors.append(f"Equal Trade contains non-transactional removal path: {forbidden}")

architect = require(
    "src/main/java/com/darkifov/thaumcraft/wand/FocusArchitectRuntime.java",
    "return 3 + enlarge * 2",
    "return 3 + enlarge",
    "equalTradeArchitectBlocks",
    "dir != side && dir != side.getOpposite()",
    "wardingArchitectBlocks",
    "insideOriginalBounds",
)

upgrades = require(
    "src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeRuntime.java",
    "case EQUAL_TRADE -> switch (rank)",
    "case 1, 2, 4 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE)",
    "case 3 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE, FocusUpgradeType.TREASURE, FocusUpgradeType.ARCHITECT)",
    "case 5 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE, FocusUpgradeType.SILK_TOUCH)",
    "case PORTABLE_HOLE -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE, FocusUpgradeType.EXTEND)",
    "case WARDING -> switch (rank)",
    "case 2 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.ARCHITECT)",
    "case 3, 4, 5 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE)",
    "isUpgradedWith(focusStack, FocusUpgradeType.ARCHITECT)",
)

hole_block = require(
    "src/main/java/com/darkifov/thaumcraft/block/TemporaryHoleBlock.java",
    'new ResourceLocation(ThaumcraftMod.MOD_ID, "portable_hole_blacklist")',
    "BlockEvent.BreakEvent",
    "MinecraftForge.EVENT_BUS.post(event)",
    "hole.initialize(old, duration, layers, clickedFace",
    "state.canBeReplaced()",
    "state.is(PORTABLE_HOLE_BLACKLIST)",
    "state.hasBlockEntity()",
    "WardedBlockRuntime.mayEdit(level, pos, owner)",
    "PushReaction.BLOCK",
    "canEntityDestroy",
    "canDropFromExplosion",
    "onBlockExploded",
    "Shapes.empty()",
    "RenderShape.INVISIBLE",
)

hole_be = require(
    "src/main/java/com/darkifov/thaumcraft/blockentity/TemporaryHoleBlockEntity.java",
    'tag.put("RememberedState", NbtUtils.writeBlockState(rememberedState))',
    'tag.putInt("Age", age)',
    'tag.putInt("Duration", duration)',
    'tag.putInt("RemainingLayers", remainingLayers)',
    'tag.putUUID("Owner", owner)',
    'tag.putInt("ClickedFace", clickedFace.get3DDataValue())',
    "if (!hole.expanded)",
    "if (hole.hasOwner() && hole.resolveOwner(level) == null)",
    "for (int first = -1; first <= 1; first++)",
    "for (int second = -1; second <= 1; second++)",
    "if (first == 0 && second == 0) continue",
    "pos.relative(clickedFace.getOpposite())",
    "remainingLayers - 1",
    "level.setBlock(pos, restore, Block.UPDATE_ALL)",
    "state.is(ThaumcraftMod.TEMPORARY_HOLE.get())",
    "state.is(ThaumcraftMod.WARDED_BLOCK.get())",
)

ward_runtime = require(
    "src/main/java/com/darkifov/thaumcraft/ward/WardedBlockRuntime.java",
    "ThreadLocal<Boolean> INTERNAL_WARD_MUTATION",
    "state.isSolidRender(level, pos)",
    "state.hasBlockEntity()",
    "BlockEvent.BreakEvent",
    "warded.initialize(remembered, player.getUUID())",
    "warded.isOwner(player.getUUID())",
    "player.getAbilities().instabuild",
    "rollbackWard",
    '"message.thaumcraft.warding.protected_owner"',
    '"message.thaumcraft.warding.protected_other"',
)

ward_be = require(
    "src/main/java/com/darkifov/thaumcraft/blockentity/WardedBlockEntity.java",
    'tag.put("RememberedState", NbtUtils.writeBlockState(rememberedState))',
    'tag.putUUID("Owner", owner)',
    "public boolean isOwner(UUID candidate)",
    "state.is(ThaumcraftMod.WARDED_BLOCK.get())",
    "state.is(ThaumcraftMod.TEMPORARY_HOLE.get())",
)

ward_block = require(
    "src/main/java/com/darkifov/thaumcraft/block/WardedBlock.java",
    "RenderShape.INVISIBLE",
    "original.getShape(level, pos, context)",
    "original.getCollisionShape(level, pos, context)",
    "original.getOcclusionShape(level, pos)",
    "getLightEmission(level, pos)",
    "getFriction(level, pos, entity)",
    "getSoundType(level, pos, entity)",
    "isLadder(level, pos, entity)",
    "canSustainPlant(level, pos, facing, plantable)",
    "getEnchantPowerBonus(level, pos)",
    "original.getSignal(level, pos, direction)",
    "original.getDirectSignal(level, pos, direction)",
    "canRedstoneConnectTo(level, pos, direction)",
    "PushReaction.BLOCK",
    "canEntityDestroy",
    "canDropFromExplosion",
    "onBlockExploded",
    "Intentionally do not forward block breaking",
)

require(
    "src/main/java/com/darkifov/thaumcraft/client/render/WardedBlockRenderer.java",
    "renderSingleBlock",
    "blockEntity.rememberedState()",
    "blockEntity.isOwner(player.getUUID())",
    "holdsWardingFocus(player)",
    "player.getOffhandItem()",
    "LevelRenderer.renderLineBox",
)

common = require(
    "src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java",
    "EqualTradeSwapRuntime.tick(level)",
    "onEqualTradeLeftClick",
    "event.setCanceled(true)",
    "WandFocusRuntime.queueEqualTradeSwing(player, event.getHand(), event.getPos())",
    "WardedBlockRuntime.isInternalWardMutation()",
    "WardedBlockRuntime.cancelIfProtected",
)

require(
    "src/main/java/com/darkifov/thaumcraft/network/RequestWandArchitectTogglePacket.java",
    "player.getMainHandItem()",
    "held = player.getOffhandItem()",
    "FocusArchitectRuntime.toggleMisc",
)
require(
    "src/main/java/com/darkifov/thaumcraft/network/RequestFocusChangePacket.java",
    "player.getMainHandItem()",
    "held = player.getOffhandItem()",
    "WandManagerRuntime.changeFocus",
)

mod = require(
    "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java",
    'RECIPE_SERIALIZERS.register("counted_smelting", CountedSmeltingRecipeSerializer::new)',
    'temporaryHoleBlock("temporary_hole"',
    'BLOCKS.register("warded_block"',
    'BLOCK_ENTITIES.register("temporary_hole"',
    'BLOCK_ENTITIES.register("warded_block"',
)

serializer = require(
    "src/main/java/com/darkifov/thaumcraft/recipe/CountedSmeltingRecipeSerializer.java",
    "implements RecipeSerializer<SmeltingRecipe>",
    'GsonHelper.getAsInt(object, "count", 1)',
    "new ItemStack(item, Math.max(1, count))",
    "buffer.writeItem(recipe.getResultItem())",
    "ItemStack result = buffer.readItem()",
)

expected_smelting = {
    "src/main/resources/data/thaumcraft/recipes/tc4_smelting_4.json": ("thaumcraft:tc4_clusteriron", "minecraft:iron_ingot", 2),
    "src/main/resources/data/thaumcraft/recipes/tc4_smelting_5.json": ("thaumcraft:tc4_clustercinnabar", "thaumcraft:quicksilver_drop", 2),
    "src/main/resources/data/thaumcraft/recipes/tc4_smelting_6.json": ("thaumcraft:tc4_clustergold", "minecraft:gold_ingot", 2),
    "src/main/resources/data/thaumcraft/recipes/tc4_smelting_cluster_copper.json": ("thaumcraft:tc4_clustercopper", "minecraft:copper_ingot", 2),
}
for path, (ingredient, result, count) in expected_smelting.items():
    data = load_json(path)
    if data.get("type") != "thaumcraft:counted_smelting":
        errors.append(f"{path} serializer mismatch")
    if data.get("ingredient", {}).get("item") != ingredient:
        errors.append(f"{path} ingredient mismatch")
    if data.get("result") != {"item": result, "count": count}:
        errors.append(f"{path} counted result mismatch: {data.get('result')!r}")

trade_recipe = load_json("src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focustrade.json")
trade_expected = {
    "research": "FOCUSTRADE",
    "pattern": ["CQE", "Q#Q", "CQE"],
    "key": {
        "#": "thaumcraft:quicksilver_drop",
        "Q": "minecraft:quartz",
        "C": "thaumcraft:balanced_shard",
        "E": "thaumcraft:balanced_shard",
    },
    "aspects": {"ORDO": 15, "PERDITIO": 15, "TERRA": 10},
}
for key, value in trade_expected.items():
    if trade_recipe.get(key) != value:
        errors.append(f"Equal Trade recipe {key} mismatch: {trade_recipe.get(key)!r}")

hole_recipe = load_json("src/main/resources/data/thaumcraft/thaumcraft_infusion/tc4_focusportablehole.json")
if hole_recipe.get("research") != "FOCUSPORTABLEHOLE" or hole_recipe.get("catalyst") != "minecraft:ender_eye":
    errors.append("Portable Hole recipe research/catalyst mismatch")
if hole_recipe.get("components") != [
    "minecraft:quartz", "thaumcraft:terra_shard", "minecraft:quartz",
    "thaumcraft:aer_shard", "minecraft:quartz", "thaumcraft:perditio_shard"
]:
    errors.append("Portable Hole component order mismatch")
if hole_recipe.get("aspects") != {"ITER": 25, "ALIENIS": 10, "PERMUTATIO": 10, "PERDITIO": 25} or hole_recipe.get("instability") != 3:
    errors.append("Portable Hole aspects/instability mismatch")

ward_recipe = load_json("src/main/resources/data/thaumcraft/thaumcraft_infusion/tc4_focuswarding.json")
if ward_recipe.get("research") != "FOCUSWARDING" or ward_recipe.get("catalyst") != "minecraft:fire_charge":
    errors.append("Warding recipe research/catalyst mismatch")
if ward_recipe.get("components") != [
    "thaumcraft:quicksilver_drop", "thaumcraft:terra_shard", "minecraft:quartz", "thaumcraft:ordo_shard",
    "thaumcraft:quicksilver_drop", "thaumcraft:terra_shard", "minecraft:quartz", "thaumcraft:ordo_shard"
]:
    errors.append("Warding component order mismatch")
if ward_recipe.get("aspects") != {"TERRA": 25, "TUTAMEN": 25, "ORDO": 25, "COGNITIO": 10} or ward_recipe.get("instability") != 4:
    errors.append("Warding aspects/instability mismatch")

blacklist = load_json("src/main/resources/data/thaumcraft/tags/blocks/portable_hole_blacklist.json")
for block in (
    "minecraft:bedrock", "minecraft:end_portal", "minecraft:end_gateway", "minecraft:nether_portal",
    "minecraft:command_block", "minecraft:moving_piston", "minecraft:piston_head",
    "thaumcraft:eldritch_portal", "thaumcraft:flux_goo", "thaumcraft:alchemical_furnace",
    "thaumcraft:warded_block",
):
    if block not in blacklist.get("values", []):
        errors.append(f"portable-hole blacklist missing {block}")

require(
    "src/main/resources/assets/thaumcraft/lang/ru_ru.json",
    '"tc.research_name.FOCUSTRADE"',
    '"tc.research_page.FOCUSTRADE.1"',
    '"tc.research_name.FOCUSPORTABLEHOLE"',
    '"tc.research_page.FOCUSPORTABLEHOLE.1"',
    '"tc.research_name.FOCUSWARDING"',
    '"tc.research_page.FOCUSWARDING.1"',
    '"message.thaumcraft.warding.protected_owner"',
    '"message.thaumcraft.warding.protected_other"',
)

mapping = require(
    "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_focus_trade_portable_hole_warding_clusters_original_parity_v11_62_19.json",
    '"one_successful_swap_per_level_tick": true',
    '"normal_lifespan": "3 + Enlarge"',
    '"max_depth": "33 + Enlarge * 8"',
    '"duration_ticks": "120 + Extend * 60"',
    '"owner_identity": "UUID replaces the original player-name hash"',
    '"serializer": "thaumcraft:counted_smelting"',
    '"tin cluster needs a canonical modded tin-ingot target"',
)
try:
    json.loads(mapping)
except Exception as exc:
    errors.append(f"invalid v11.62.19 source mapping JSON: {exc}")

for fake in (ROOT / "src/main/resources/data/thaumcraft/recipes").glob("*.json"):
    lower = fake.name.lower()
    if any(word in lower for word in ("equal_trade", "portable_hole", "warding", "focustrade", "focusportable", "focusward")):
        errors.append(f"obsolete normal crafting recipe remains: {fake.relative_to(ROOT)}")

require(
    ".github/workflows/main.yml",
    "tc4_v11_62_19_equal_trade_portable_hole_warding_cluster_original_parity_audit.py",
    "v11.62.19-github-jar",
    "v11.62.19-build-reports",
)
require("README.md", "v11.62.19", "Equal Trade", "Portable Hole", "Warding")

# Release reports and NEXT_CHAT_PROMPT are intentionally excluded from subsystem regression checks; only runtime code/resources are guarded.
if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)
print("TC4 v11.62.19 Equal Trade + Portable Hole + Warding + counted clusters audit: OK")
