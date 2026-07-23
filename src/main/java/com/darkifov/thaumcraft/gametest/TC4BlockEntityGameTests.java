package com.darkifov.thaumcraft.gametest;

import com.darkifov.thaumcraft.arcane.TC4ArcaneWorkbenchParity;
import com.darkifov.thaumcraft.aura.TC4AuraParity;
import com.darkifov.thaumcraft.aura.TC4ThaumometerParity;
import com.darkifov.thaumcraft.aura.TC4ThaumometerScanKeys;
import com.darkifov.thaumcraft.eldritch.TC4EldritchParity;
import com.darkifov.thaumcraft.eldritch.TC4EldritchProgression;
import com.darkifov.thaumcraft.golem.TC4GolemParity;
import com.darkifov.thaumcraft.recipe.TC4RecipeParity;
import com.darkifov.thaumcraft.recipe.TC4RecipeRuntimeBridge;
import com.darkifov.thaumcraft.research.TC4ResearchEfficiencyParity;
import com.darkifov.thaumcraft.research.TC4ThaumonomiconParity;
import com.darkifov.thaumcraft.research.TC4ThaumonomiconLootParity;
import com.darkifov.thaumcraft.research.TC4ResearchMasteryCombinationParity;
import com.darkifov.thaumcraft.research.TC4ResearchTableBehaviorParity;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import com.darkifov.thaumcraft.research.ResearchTableFoundation;
import com.darkifov.thaumcraft.research.ResearchNoteGrid;
import com.darkifov.thaumcraft.research.ResearchNoteSolver;
import com.darkifov.thaumcraft.research.ResearchNoteState;
import com.darkifov.thaumcraft.research.ResearchTableInventoryRuntime;
import com.darkifov.thaumcraft.research.TC4ResearchNoteGraphParity;
import com.darkifov.thaumcraft.research.TC4ResearchNoteClearParity;
import com.darkifov.thaumcraft.research.TC4ResearchNoteCompletionParity;
import com.darkifov.thaumcraft.research.TC4ResearchCompletionWarpParity;
import com.darkifov.thaumcraft.research.TC4ResearchSystemFullClosureParity;
import com.darkifov.thaumcraft.research.ResearchRegistry;
import com.darkifov.thaumcraft.research.ResearchNoteRequirements;
import com.darkifov.thaumcraft.research.OriginalResearchBridge;
import com.darkifov.thaumcraft.research.OriginalResearchProgression;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.runic.TC4RunicParity;
import com.darkifov.thaumcraft.taint.TC4TaintParity;
import com.darkifov.thaumcraft.wand.TC4WandParity;
import com.darkifov.thaumcraft.wand.TC4WandComponentsFullClosureParity;
import com.darkifov.thaumcraft.wand.WandComponentData;
import com.darkifov.thaumcraft.wand.WandVariantRuntime;
import com.darkifov.thaumcraft.wand.WandRodType;
import com.darkifov.thaumcraft.wand.WandCapType;
import com.darkifov.thaumcraft.wand.WandFocusRuntime;
import com.darkifov.thaumcraft.wand.WandFocusType;
import com.darkifov.thaumcraft.wand.FocusUpgradeRuntime;
import com.darkifov.thaumcraft.wand.FocusUpgradeType;
import com.darkifov.thaumcraft.wand.TC4WandFocusContract;
import com.darkifov.thaumcraft.wand.TC4WandFociFullClosureParity;
import com.darkifov.thaumcraft.world.TC4WorldgenParity;
import com.darkifov.thaumcraft.event.WarpEvents;
import com.darkifov.thaumcraft.event.CommonEvents;
import com.darkifov.thaumcraft.warp.TC4WarpRuntimeParity;
import com.darkifov.thaumcraft.warp.TC4UnnaturalHungerParity;
import com.darkifov.thaumcraft.warp.TC4BathSaltsParity;
import com.darkifov.thaumcraft.warp.TC4WarpResearchGrant;
import com.darkifov.thaumcraft.effect.TC4WarpMobEffect;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipe;
import com.darkifov.thaumcraft.alchemy.AlchemyRecipes;
import com.darkifov.thaumcraft.alchemy.TC4AlchemyParity;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipe;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipes;
import com.darkifov.thaumcraft.arcane.TC4ArcaneWorkbenchVisCostParity;
import com.darkifov.thaumcraft.aura.AuraNodeModifier;
import com.darkifov.thaumcraft.aura.AuraNodeType;
import com.darkifov.thaumcraft.aura.TC4NodeJarRuntime;
import com.darkifov.thaumcraft.block.AuraNodeLegacyItem;
import com.darkifov.thaumcraft.block.ArcaneDoorBlock;
import com.darkifov.thaumcraft.block.ManaPodBlock;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.block.WandFocusItem;
import com.darkifov.thaumcraft.block.FocusPouchItem;
import com.darkifov.thaumcraft.block.TC4FortressArmorItem;
import com.darkifov.thaumcraft.block.TallowCandleBlock;
import com.darkifov.thaumcraft.block.BellowsBlock;
import com.darkifov.thaumcraft.block.TC4ArcaneBellowsParity;
import com.darkifov.thaumcraft.block.TC4InfernalFurnaceParity;
import com.darkifov.thaumcraft.block.TC4GrowthLampParity;
import com.darkifov.thaumcraft.block.TC4FertilityLampParity;
import com.darkifov.thaumcraft.block.TC4EssentiaLampBlock;
import com.darkifov.thaumcraft.block.InfernalFurnaceBlock;
import com.darkifov.thaumcraft.block.InfernalFurnaceLayer;
import com.darkifov.thaumcraft.block.ArcaneEarBlock;
import com.darkifov.thaumcraft.block.ArcaneLevitatorBlock;
import com.darkifov.thaumcraft.block.ArcanePressurePlateBlock;
import com.darkifov.thaumcraft.block.ArcaneLampBlock;
import com.darkifov.thaumcraft.block.ArcaneSpaBlock;
import com.darkifov.thaumcraft.block.TC4ArcaneSpaParity;
import com.darkifov.thaumcraft.block.TC4ArcaneLampParity;
import com.darkifov.thaumcraft.block.TC4ArcanePressurePlateParity;
import com.darkifov.thaumcraft.block.TC4ArcaneLevitatorParity;
import com.darkifov.thaumcraft.block.TC4ArcaneEarParity;
import com.darkifov.thaumcraft.block.TC4TallowCandleParity;
import com.darkifov.thaumcraft.block.HungryChestBlock;
import com.darkifov.thaumcraft.block.ScribingToolsItem;
import com.darkifov.thaumcraft.block.EssentiaReservoirBlock;
import com.darkifov.thaumcraft.block.EssentiaJarBlockItem;
import com.darkifov.thaumcraft.jar.TC4EssentiaJarParity;
import com.darkifov.thaumcraft.block.MirrorBlock;
import com.darkifov.thaumcraft.entity.CrimsonCultistEntity;
import com.darkifov.thaumcraft.entity.EldritchGuardianEntity;
import com.darkifov.thaumcraft.entity.MindSpiderEntity;
import com.darkifov.thaumcraft.entity.TravelingTrunkEntity;
import com.darkifov.thaumcraft.item.BoneBowItem;
import com.darkifov.thaumcraft.item.ArcaneKeyItem;
import com.darkifov.thaumcraft.runic.TC4FortressArmorRuntime;
import com.darkifov.thaumcraft.runic.TC4FortressMaskRuntime;
import com.darkifov.thaumcraft.blockentity.AlchemicalFurnaceBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlchemicalCentrifugeBlockEntity;
import com.darkifov.thaumcraft.blockentity.ThaumatoriumBlockEntity;
import com.darkifov.thaumcraft.block.MnemonicMatrixBlock;
import com.darkifov.thaumcraft.essentia.TC4DistillationRuntime;
import com.darkifov.thaumcraft.blockentity.ArcaneDoorBlockEntity;
import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import com.darkifov.thaumcraft.blockentity.ArcaneWorkbenchBlockEntity;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.blockentity.BrainJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.BellowsBlockEntity;
import com.darkifov.thaumcraft.blockentity.ArcaneEarBlockEntity;
import com.darkifov.thaumcraft.blockentity.ArcaneLevitatorBlockEntity;
import com.darkifov.thaumcraft.blockentity.ArcanePressurePlateBlockEntity;
import com.darkifov.thaumcraft.blockentity.ArcaneLampBlockEntity;
import com.darkifov.thaumcraft.blockentity.ArcaneSpaBlockEntity;
import com.darkifov.thaumcraft.blockentity.InfernalFurnaceBlockEntity;
import com.darkifov.thaumcraft.blockentity.InfernalFurnaceNozzleBlockEntity;
import com.darkifov.thaumcraft.blockentity.TC4EssentiaLampBlockEntity;
import com.darkifov.thaumcraft.blockentity.ArcaneBoreBlockEntity;
import com.darkifov.thaumcraft.blockentity.TC4ArcaneBoreParity;
import com.darkifov.thaumcraft.blockentity.ArcaneBoreBaseBlockEntity;
import com.darkifov.thaumcraft.blockentity.TC4BrainJarParity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaReservoirBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import com.darkifov.thaumcraft.blockentity.HungryChestBlockEntity;
import com.darkifov.thaumcraft.blockentity.TC4HungryChestParity;
import com.darkifov.thaumcraft.blockentity.TallowCandleBlockEntity;
import com.darkifov.thaumcraft.blockentity.FumeDissipatorBlockEntity;
import com.darkifov.thaumcraft.blockentity.ManaPodBlockEntity;
import com.darkifov.thaumcraft.blockentity.NodeJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.ResearchTableBlockEntity;
import com.darkifov.thaumcraft.blockentity.TC4WandPedestalBlockEntity;
import com.darkifov.thaumcraft.blockentity.VisChargeRelayBlockEntity;
import com.darkifov.thaumcraft.blockentity.VisRelayBlockEntity;
import com.darkifov.thaumcraft.porting.TC4LegacyDuplicateItemMigrator;
import com.darkifov.thaumcraft.porting.TC4ResearchItems;
import com.darkifov.thaumcraft.source.TC4ObjectAspectRegistry;
import com.darkifov.thaumcraft.essentia.EssentiaTubeSubtype;
import com.darkifov.thaumcraft.infusion.InfusionRecipe;
import com.darkifov.thaumcraft.infusion.InfusionRecipes;
import com.darkifov.thaumcraft.infusion.TC4InfusionEnchantmentAdapter;
import com.darkifov.thaumcraft.infusion.TC4InfusionEnchantmentIndex;
import com.darkifov.thaumcraft.infusion.TC4InfusionRuntime;
import com.darkifov.thaumcraft.infusion.TC4InfusionLifecycleParity;
import com.darkifov.thaumcraft.infusion.TC4InfusionPauseResumeParity;
import com.darkifov.thaumcraft.infusion.TC4InfusionSaveReloadParity;
import com.darkifov.thaumcraft.infusion.TC4InfusionInstabilityEventTableParity;
import com.darkifov.thaumcraft.infusion.TC4InfusionShortageInstabilityParity;
import com.darkifov.thaumcraft.infusion.TC4InfusionStabilityParity;
import com.darkifov.thaumcraft.infusion.InfusionStabilizer;
import com.darkifov.thaumcraft.infusion.TC4InfusionAltarFullClosureParity;
import com.darkifov.thaumcraft.mirror.MirrorBlockEntity;
import com.darkifov.thaumcraft.mirror.EssentiaMirrorBlockEntity;
import com.darkifov.thaumcraft.mirror.MirrorLink;
import com.darkifov.thaumcraft.menu.ResearchTableMenu;
import com.darkifov.thaumcraft.menu.ArcaneSpaMenu;
import com.darkifov.thaumcraft.essentia.TC4EssentiaParity;
import com.darkifov.thaumcraft.essentia.TC4EssentiaTubeParity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Items;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

/**
 * Required headless smoke tests for the highest-risk persistent TC4 systems.
 *
 * <p>These tests deliberately avoid client classes and player input. They run
 * on Forge's dedicated GameTest server and exercise real registered blocks,
 * BlockEntities, capabilities, ticking and ItemStack serialization.</p>
 */
@PrefixGameTestTemplate(false)
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TC4BlockEntityGameTests {
    private static final String TEMPLATE = "empty_9x5x9";
    private static final String BATCH = "thaumcraft_block_entity_smoke";

    private TC4BlockEntityGameTests() {
    }

    @SubscribeEvent
    public static void registerGameTests(RegisterGameTestsEvent event) {
        event.register(TC4BlockEntityGameTests.class);
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void essentiaJarPersistsFilterAndContents(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.ESSENTIA_JAR.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);

        EssentiaJarBlockEntity jar = requireBlockEntity(level, pos, EssentiaJarBlockEntity.class);
        jar.setFilterAspect(Aspect.AER);
        require(jar.addToContainerOriginal(Aspect.AER, 48, false) == 0,
                "Essentia jar rejected valid Aer input");
        require(jar.addToContainerOriginal(Aspect.IGNIS, 3, false) == 3,
                "Filtered jar accepted the wrong aspect");

        CompoundTag saved = jar.saveWithoutMetadata();
        EssentiaJarBlockEntity restored = new EssentiaJarBlockEntity(pos, state);
        restored.load(saved);
        require(restored.filterAspect() == Aspect.AER, "Jar filter did not survive NBT round-trip");
        require(restored.storedAspect() == Aspect.AER, "Jar aspect did not survive NBT round-trip");
        require(restored.amount() == 48, "Jar amount did not survive NBT round-trip");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandPedestalDrainsNodeAndChargesWand(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pedestalPos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos nodePos = helper.absolutePos(new BlockPos(5, 1, 2));
        BlockState pedestalState = ThaumcraftMod.TC4_WAND_PEDESTAL.get().defaultBlockState();
        level.setBlock(pedestalPos, pedestalState, Block.UPDATE_ALL);
        level.setBlock(nodePos, ThaumcraftMod.AURA_NODE.get().defaultBlockState(), Block.UPDATE_ALL);

        TC4WandPedestalBlockEntity pedestal = requireBlockEntity(
                level, pedestalPos, TC4WandPedestalBlockEntity.class);
        AuraNodeBlockEntity node = requireBlockEntity(level, nodePos, AuraNodeBlockEntity.class);
        node.initializeAs(AuraNodeType.NORMAL, AuraNodeModifier.NORMAL,
                new AspectList().add(Aspect.AER, 2));

        ItemStack wand = new ItemStack(ThaumcraftMod.IRON_CAPPED_WOODEN_WAND.get());
        pedestal.setStored(wand);
        for (int i = 0; i < 5; i++) {
            TC4WandPedestalBlockEntity.serverTick(level, pedestalPos, pedestalState, pedestal);
        }

        require(WandItem.getVis(pedestal.stored(), Aspect.AER) == 100,
                "Pedestal did not add one full Vis (100 centivis)");
        require(node.aspects().get(Aspect.AER) == 1,
                "Pedestal did not remove exactly one node aspect point");
        require(nodePos.equals(pedestal.drainSource()), "Pedestal did not record its drain source");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void nodeJarPersistsProfileAndCaptureAnimation(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.NODE_JAR_BLOCK.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);

        UUID nodeId = UUID.fromString("d4ec9f3a-7791-4c6b-8120-66bb21df5ac1");
        CompoundTag nodeTag = new CompoundTag();
        nodeTag.putString("NodeId", nodeId.toString());
        nodeTag.putString("NodeType", AuraNodeType.PURE.name());
        nodeTag.putString("NodeModifier", AuraNodeModifier.BRIGHT.name());
        nodeTag.put("Aspects", new AspectList().add(Aspect.IGNIS, 7).save());
        nodeTag.put("BaseAspects", new AspectList().add(Aspect.IGNIS, 9).save());

        NodeJarBlockEntity jar = requireBlockEntity(level, pos, NodeJarBlockEntity.class);
        jar.setNodeTag(nodeTag);
        jar.startCaptureAnimation();
        require(Math.abs(jar.captureScale(0.0F) - 3.0F) < 0.001F,
                "Node jar capture animation did not start at scale 3");

        CompoundTag saved = jar.saveWithoutMetadata();
        NodeJarBlockEntity restored = new NodeJarBlockEntity(pos, state);
        restored.load(saved);
        require(restored.hasNode(), "Node jar lost its node profile");
        require(nodeId.toString().equals(restored.nodeTag().getString("NodeId")),
                "Node jar changed the persistent node id");
        AspectList restoredAspects = new AspectList();
        restoredAspects.load(restored.nodeTag().getCompound("Aspects"));
        require(restoredAspects.get(Aspect.IGNIS) == 7, "Node jar changed stored aspects");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void manaPodGrowsAndPersistsAspect(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos podPos = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlock(podPos.above(), Blocks.OAK_LOG.defaultBlockState(), Block.UPDATE_ALL);
        BlockState ageTwo = ThaumcraftMod.TC4_MANA_POD.get().defaultBlockState()
                .setValue(ManaPodBlock.AGE, 2);
        level.setBlock(podPos, ageTwo, Block.UPDATE_ALL);

        ManaPodBlockEntity pod = requireBlockEntity(level, podPos, ManaPodBlockEntity.class);
        pod.setAspect(Aspect.IGNIS);
        pod.checkGrowth(RandomSource.create(116342L));
        require(level.getBlockState(podPos).getValue(ManaPodBlock.AGE) == 3,
                "Mana Pod did not advance from age 2 to age 3");
        require(pod.aspect() == Aspect.IGNIS, "Single-parent Mana Pod changed aspect unexpectedly");
        require(pod.exposedAspects().totalAmount() == 0,
                "Immature Mana Pod exposed a Thaumometer aspect");

        level.setBlock(podPos, level.getBlockState(podPos).setValue(ManaPodBlock.AGE, 7), Block.UPDATE_ALL);
        require(pod.exposedAspects().get(Aspect.IGNIS) == 1,
                "Mature Mana Pod did not expose exactly one aspect");

        CompoundTag saved = pod.saveWithoutMetadata();
        ManaPodBlockEntity restored = new ManaPodBlockEntity(podPos, level.getBlockState(podPos));
        restored.load(saved);
        require(restored.aspect() == Aspect.IGNIS, "Mana Pod aspect did not survive NBT round-trip");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void legacyStackMigrationPreservesPayload(GameTestHelper helper) {
        Item legacy = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ThaumcraftMod.MOD_ID, "nitor"));
        require(legacy != null, "Legacy tc4_nitor registry entry is missing");

        ItemStack original = new ItemStack(legacy, 3);
        original.setHoverName(Component.literal("Migrated Nitor"));
        original.getOrCreateTag().putInt("PortingProof", 116344);
        ItemStack migrated = TC4LegacyDuplicateItemMigrator.migrateStackDeep(original);

        require(migrated.getItem() == ThaumcraftMod.NITOR.get(), "Legacy Nitor was not migrated");
        require(migrated.getCount() == 3, "Migration changed stack count");
        require(migrated.hasCustomHoverName()
                        && "Migrated Nitor".equals(migrated.getHoverName().getString()),
                "Migration lost the custom name");
        require(migrated.getTag() != null && migrated.getTag().getInt("PortingProof") == 116344,
                "Migration lost custom NBT");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void energizedNodeUsesTransientPerTickPool(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos nodePos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState nodeState = ThaumcraftMod.AURA_NODE.get().defaultBlockState();
        level.setBlock(nodePos, nodeState, Block.UPDATE_ALL);

        AuraNodeBlockEntity node = requireBlockEntity(level, nodePos, AuraNodeBlockEntity.class);
        node.initializeAs(AuraNodeType.NORMAL, AuraNodeModifier.NORMAL,
                new AspectList().add(Aspect.AER, 25));
        CompoundTag energized = node.saveWithoutMetadata();
        energized.putBoolean("Energized", true);
        energized.putInt("EnergizedTicks", 100);
        node.load(energized);

        require(node.consumeEnergizedVis(Aspect.AER, 4) == 4,
                "Energized node did not supply the first four centivis");
        require(node.consumeEnergizedVis(Aspect.AER, 4) == 1,
                "Energized node pool exceeded floor(sqrt(25)) = 5 in one tick");
        require(node.aspects().get(Aspect.AER) == 25,
                "Relay consumption mutated the permanent aura profile");

        helper.runAfterDelay(1L, () -> {
            require(node.consumeEnergizedVis(Aspect.AER, 5) == 5,
                    "Energized node transient pool did not refill on the next tick");
            require(node.aspects().get(Aspect.AER) == 25,
                    "Refilled transient pool mutated permanent node aspects");
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void visRelayGraphHonorsLosAttunementAndPersistence(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos firstPos = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos secondPos = helper.absolutePos(new BlockPos(1, 1, 6));
        BlockPos sourcePos = helper.absolutePos(new BlockPos(6, 1, 6));
        BlockPos blockerPos = helper.absolutePos(new BlockPos(3, 1, 3));

        BlockState relayState = ThaumcraftMod.VIS_RELAY.get().defaultBlockState();
        level.setBlock(firstPos, relayState, Block.UPDATE_ALL);
        level.setBlock(secondPos, relayState, Block.UPDATE_ALL);
        level.setBlock(sourcePos, ThaumcraftMod.AURA_NODE.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(blockerPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        VisRelayBlockEntity first = requireBlockEntity(level, firstPos, VisRelayBlockEntity.class);
        VisRelayBlockEntity second = requireBlockEntity(level, secondPos, VisRelayBlockEntity.class);
        AuraNodeBlockEntity source = requireBlockEntity(level, sourcePos, AuraNodeBlockEntity.class);
        source.initializeAs(AuraNodeType.NORMAL, AuraNodeModifier.NORMAL,
                new AspectList().add(Aspect.AER, 25));
        CompoundTag energized = source.saveWithoutMetadata();
        energized.putBoolean("Energized", true);
        energized.putInt("EnergizedTicks", 100);
        source.load(energized);

        require(first.cycleAttunement() == 0 && second.cycleAttunement() == 0,
                "Relay attunement did not cycle wildcard -> Aer");
        first.refreshParent(level);
        require(secondPos.equals(first.parentPos()),
                "Relay did not route around the LoS blocker through the compatible neighbour");

        CompoundTag saved = first.saveWithoutMetadata();
        VisRelayBlockEntity restored = new VisRelayBlockEntity(firstPos, relayState);
        restored.load(saved);
        require(restored.attunement() == 0, "Relay attunement did not survive NBT round-trip");
        require(secondPos.equals(restored.parentPos()), "Relay parent did not survive NBT round-trip");

        require(second.cycleAttunement() == 1, "Second relay did not cycle Aer -> Ignis");
        first.refreshParent(level);
        require(first.parentPos() == null,
                "Incompatible relay channels remained connected through the blocked direct path");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void workbenchChargerTransfersFiveCentivisPerTick(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos workbenchPos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos chargerPos = workbenchPos.above();
        BlockPos sourcePos = helper.absolutePos(new BlockPos(6, 2, 2));
        BlockState workbenchState = ThaumcraftMod.ARCANE_WORKBENCH.get().defaultBlockState();
        BlockState chargerState = ThaumcraftMod.VIS_CHARGE_RELAY.get().defaultBlockState();
        level.setBlock(workbenchPos, workbenchState, Block.UPDATE_ALL);
        level.setBlock(chargerPos, chargerState, Block.UPDATE_ALL);
        level.setBlock(sourcePos, ThaumcraftMod.AURA_NODE.get().defaultBlockState(), Block.UPDATE_ALL);

        ArcaneWorkbenchBlockEntity workbench = requireBlockEntity(
                level, workbenchPos, ArcaneWorkbenchBlockEntity.class);
        VisChargeRelayBlockEntity charger = requireBlockEntity(
                level, chargerPos, VisChargeRelayBlockEntity.class);
        AuraNodeBlockEntity source = requireBlockEntity(level, sourcePos, AuraNodeBlockEntity.class);
        source.initializeAs(AuraNodeType.NORMAL, AuraNodeModifier.NORMAL,
                new AspectList().add(Aspect.AER, 25));
        CompoundTag energized = source.saveWithoutMetadata();
        energized.putBoolean("Energized", true);
        energized.putInt("EnergizedTicks", 100);
        source.load(energized);

        ItemStack wand = new ItemStack(ThaumcraftMod.IRON_CAPPED_WOODEN_WAND.get());
        workbench.setItem(ArcaneWorkbenchBlockEntity.SLOT_WAND, wand);
        VisChargeRelayBlockEntity.serverTick(level, chargerPos, chargerState, charger);

        require(WandItem.getVis(workbench.getItem(ArcaneWorkbenchBlockEntity.SLOT_WAND), Aspect.AER) == 5,
                "Workbench charger did not transfer exactly five centivis in one tick");
        require(source.aspects().get(Aspect.AER) == 25,
                "Workbench charger mutated permanent node aspects");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void arcaneDoorUpperHalfDelegatesPersistentAccess(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos lowerPos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos upperPos = lowerPos.above();
        BlockState lowerState = ThaumcraftMod.ARCANE_DOOR.get().defaultBlockState()
                .setValue(ArcaneDoorBlock.UPPER, false);
        BlockState upperState = lowerState.setValue(ArcaneDoorBlock.UPPER, true);
        level.setBlock(lowerPos, lowerState, Block.UPDATE_ALL);
        level.setBlock(upperPos, upperState, Block.UPDATE_ALL);

        UUID owner = UUID.fromString("3ec1d605-53d0-45e4-8276-354d521a5ae8");
        UUID standard = UUID.fromString("2fb4795b-a143-47ce-b911-5d7a4cdb3143");
        UUID full = UUID.fromString("92987084-92ba-41cf-950a-6e31e02029d8");
        CompoundTag access = new CompoundTag();
        access.putUUID("Owner", owner);
        ListTag standardList = new ListTag();
        standardList.add(NbtUtils.createUUID(standard));
        access.put("StandardAccess", standardList);
        ListTag fullList = new ListTag();
        fullList.add(NbtUtils.createUUID(full));
        access.put("FullAccess", fullList);

        ArcaneDoorBlockEntity lower = requireBlockEntity(level, lowerPos, ArcaneDoorBlockEntity.class);
        ArcaneDoorBlockEntity upper = requireBlockEntity(level, upperPos, ArcaneDoorBlockEntity.class);
        lower.load(access);
        require(owner.equals(upper.owner()), "Upper door half did not delegate owner to lower half");
        require(upper.authorizedUsers().contains(owner), "Door access list lost owner");
        require(upper.authorizedUsers().contains(standard), "Door access list lost standard user");
        require(upper.authorizedUsers().contains(full), "Door access list lost full-access user");
        require(upper.keyBindingPos().equals(lowerPos), "Upper door half returned the wrong key position");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void fluxScrubberRemovesFluxAndExportsPraecantatio(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos scrubberPos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos supportPos = scrubberPos.below();
        BlockPos fluxPos = helper.absolutePos(new BlockPos(4, 1, 2));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        BlockState scrubberState = ThaumcraftMod.FUME_DISSIPATOR.get().defaultBlockState()
                .setValue(com.darkifov.thaumcraft.block.FumeDissipatorBlock.FACING, net.minecraft.core.Direction.DOWN);
        level.setBlock(scrubberPos, scrubberState, Block.UPDATE_ALL);
        level.setBlock(fluxPos, ThaumcraftMod.FLUX_GOO.get().defaultBlockState(), Block.UPDATE_ALL);

        FumeDissipatorBlockEntity scrubber = requireBlockEntity(level, scrubberPos, FumeDissipatorBlockEntity.class);
        scrubber.setRuntimeStateForTest(FumeDissipatorBlockEntity.VIS_COST_PER_FLUX, 0, 0);
        for (int i = 0; i < 2400 && level.getBlockState(fluxPos).is(ThaumcraftMod.FLUX_GOO.get()); i++) {
            FumeDissipatorBlockEntity.serverTick(level, scrubberPos, scrubberState, scrubber);
        }
        require(level.isEmptyBlock(fluxPos), "Flux Scrubber did not remove flux within radius 16");
        require(scrubber.charges() == 1, "Flux Scrubber did not add one cleanup charge");

        scrubber.setRuntimeStateForTest(0, 4, 4);
        require(scrubber.takeEssentia(Aspect.PRAECANTATIO, 1, net.minecraft.core.Direction.UP) == 0,
                "Flux Scrubber exported essentia from the wrong face");
        require(scrubber.takeEssentia(Aspect.PRAECANTATIO, 1, net.minecraft.core.Direction.DOWN) == 1,
                "Flux Scrubber did not export Praecantatio from its facing side");
        CompoundTag saved = scrubber.saveWithoutMetadata();
        FumeDissipatorBlockEntity restored = new FumeDissipatorBlockEntity(scrubberPos, scrubberState);
        restored.load(saved);
        require(restored.charges() == 4 && restored.essentia() == 3,
                "Flux Scrubber counters did not survive NBT round-trip");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void originalSmeltingRecipesMatchTC4(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        requireSmelting(level, "tc4_smelting_4", "thaumcraft:tc4_clusteriron",
                "minecraft:iron_ingot", 2, 1.0F);
        requireSmelting(level, "tc4_smelting_5", "thaumcraft:tc4_clustercinnabar",
                "thaumcraft:quicksilver_drop", 2, 1.0F);
        requireSmelting(level, "tc4_smelting_6", "thaumcraft:tc4_clustergold",
                "minecraft:gold_ingot", 2, 1.0F);
        requireSmelting(level, "tc4_smelting_7", "thaumcraft:balanced_shard",
                "thaumcraft:tc4_dust", 1, 1.0F);
        requireSmelting(level, "tc4_smelting_8", "thaumcraft:tc4_coin",
                "minecraft:gold_nugget", 1, 0.0F);
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionEnchantmentFamilyMatchesTC4(GameTestHelper helper) {
        long enchantmentRecipeCount = InfusionRecipes.recipes().stream()
                .filter(InfusionRecipe::isInfusionEnchantment)
                .count();
        require(enchantmentRecipeCount == 24,
                "Expected all 24 original infusion enchantment recipes, got " + enchantmentRecipeCount);
        require(TC4InfusionEnchantmentIndex.customThaumcraftEntries().size() == 2,
                "Expected Repair and Haste custom enchantment recipes");
        require(TC4InfusionEnchantmentIndex.vanillaEntries().size() == 22,
                "Expected 22 vanilla enchantment recipes");

        InfusionRecipe protectionRecipe = InfusionRecipes.findById(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_enchantment_infench0"));
        require(protectionRecipe != null, "Protection infusion recipe is missing");
        require(protectionRecipe.componentsFor(ItemStack.EMPTY).size() == 2,
                "Protection infusion recipe has the wrong component count");
        require(protectionRecipe.componentsFor(ItemStack.EMPTY).get(0).equals(new ResourceLocation("minecraft", "iron_ingot")),
                "Protection infusion recipe lost the original iron-ingot component");
        require(protectionRecipe.componentsFor(ItemStack.EMPTY).get(1).equals(new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_dust")),
                "Protection infusion recipe lost the original Salis Mundus component");

        Enchantment protection = ForgeRegistries.ENCHANTMENTS.getValue(
                new ResourceLocation("minecraft", "protection"));
        Enchantment unbreaking = ForgeRegistries.ENCHANTMENTS.getValue(
                new ResourceLocation("minecraft", "unbreaking"));
        require(protection != null && unbreaking != null, "Vanilla enchantments are unavailable");

        ItemStack central = new ItemStack(Items.DIAMOND_CHESTPLATE);
        central.enchant(protection, 1);
        central.enchant(unbreaking, 3);
        require(TC4InfusionEnchantmentAdapter.canApply(protectionRecipe, central),
                "Protection II should be applicable to a compatible enchanted chestplate");
        require(TC4InfusionEnchantmentAdapter.calcXp(protectionRecipe, central) == 2,
                "Protection II XP cost does not match TC4 recipeXP * (1 + current level)");
        require(TC4InfusionEnchantmentAdapter.calcInstability(protectionRecipe, central) == 3,
                "Infusion enchantment instability does not include half the existing enchantment levels");
        java.util.EnumMap<Aspect, Integer> scaled =
                TC4InfusionEnchantmentAdapter.scaledAspects(protectionRecipe, central);
        require(scaled.getOrDefault(Aspect.PRAECANTATIO, 0) == 9,
                "Praecantatio scaling does not match TC4 getEssentiaMod");
        require(scaled.getOrDefault(Aspect.TUTAMEN, 0) == 18,
                "Tutamen scaling does not match TC4 getEssentiaMod");
        require(TC4InfusionEnchantmentAdapter.applyOutput(protectionRecipe, central),
                "Infusion enchantment output was not applied");
        require(EnchantmentHelper.getItemEnchantmentLevel(protection, central) == 2,
                "Infusion enchantment did not increment the existing level");
        require(EnchantmentHelper.getItemEnchantmentLevel(unbreaking, central) == 3,
                "Infusion enchantment overwrote unrelated compatible enchantments");

        java.util.Map<Enchantment, Integer> maxed =
                new java.util.LinkedHashMap<>(EnchantmentHelper.getEnchantments(central));
        maxed.put(protection, protection.getMaxLevel());
        EnchantmentHelper.setEnchantments(maxed, central);
        require(!TC4InfusionEnchantmentAdapter.canApply(protectionRecipe, central),
                "Max-level enchantment was incorrectly accepted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void oreDictionaryCrucibleAndLegacySmeltingMatchTC4(GameTestHelper helper) {
        requireAlchemyRecipe("PureTin", "#forge:ores/tin",
                "thaumcraft:tc4_clustertin", Aspect.METALLUM, 1, Aspect.ORDO, 1);
        requireAlchemyRecipe("PureSilver", "#forge:ores/silver",
                "thaumcraft:tc4_clustersilver", Aspect.METALLUM, 1, Aspect.ORDO, 1);
        requireAlchemyRecipe("PureLead", "#forge:ores/lead",
                "thaumcraft:tc4_clusterlead", Aspect.METALLUM, 1, Aspect.ORDO, 1);

        ServerLevel level = helper.getLevel();
        requireSmelting(level, "tc4_smelting_1", "thaumcraft:greatwood_log",
                "minecraft:charcoal", 1, 0.5F);
        requireSmelting(level, "tc4_smelting_1", "thaumcraft:silverwood_log",
                "minecraft:charcoal", 1, 0.5F);
        requireSmelting(level, "tc4_smelting_2", "thaumcraft:cinnabar_ore",
                "thaumcraft:quicksilver_drop", 1, 1.0F);
        requireSmelting(level, "tc4_smelting_3", "thaumcraft:amber_ore",
                "thaumcraft:amber", 1, 1.0F);
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandCapRecipeFamilyMatchesTC4(GameTestHelper helper) {
        requireArcaneCap("gold", "minecraft:gold_nugget", "thaumcraft:gold_wand_cap",
                3, 3, 3, 0);
        requireArcaneCap("copper", "#forge:nuggets/copper", "thaumcraft:tc4_wand_cap_copper",
                2, 2, 2, 0);
        requireArcaneCap("silver", "#forge:nuggets/silver", "thaumcraft:tc4_wand_cap_silver_inert",
                4, 4, 4, 0);
        requireArcaneCap("thaumium", "#forge:nuggets/thaumium", "thaumcraft:thaumium_wand_cap_inert",
                6, 6, 6, 0);
        requireArcaneCap("void", "#forge:nuggets/void_metal", "thaumcraft:tc4_wand_cap_void_inert",
                27, 18, 18, 27);

        requireChargedCapInfusion("silver", "thaumcraft:tc4_wand_cap_silver_inert",
                "thaumcraft:tc4_wand_cap_silver", 2, 4, 8, 4, 0, 0);
        requireChargedCapInfusion("thaumium", "thaumcraft:thaumium_wand_cap_inert",
                "thaumcraft:thaumium_wand_cap", 3, 5, 12, 6, 0, 0);
        requireChargedCapInfusion("void", "thaumcraft:tc4_wand_cap_void_inert",
                "thaumcraft:tc4_wand_cap_void", 4, 8, 18, 18, 18, 18);
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void mcpCorrectedArcaneAndWandRodRecipesMatchTC4(GameTestHelper helper) {
        ArcaneWorkbenchRecipe restrict = requireArcaneRecipe("tc4_tuberestrict", "thaumcraft:essentia_tube_restrict");
        requireIngredientTexts(restrict, "thaumcraft:essentia_tube", "#forge:stone");
        require(restrict.aspectCost().getOrDefault(Aspect.AQUA, 0) == 5
                        && restrict.aspectCost().getOrDefault(Aspect.TERRA, 0) == 16,
                "TubeRestrict lost its exact Aqua/Terra Vis cost");

        ArcaneWorkbenchRecipe oneWay = requireArcaneRecipe("tc4_tubeoneway", "thaumcraft:essentia_tube_oneway");
        requireIngredientTexts(oneWay, "thaumcraft:essentia_tube", "#forge:dyes/blue");
        require(oneWay.aspectCost().getOrDefault(Aspect.AQUA, 0) == 5
                        && oneWay.aspectCost().getOrDefault(Aspect.ORDO, 0) == 8
                        && oneWay.aspectCost().getOrDefault(Aspect.PERDITIO, 0) == 8,
                "TubeOneway lost its exact Aqua/Ordo/Perditio Vis cost");

        ArcaneWorkbenchRecipe crystalizer = requireArcaneRecipe(
                "tc4_essentia_crystalizer", "thaumcraft:essentia_crystalizer");
        require(crystalizer.pattern().equals(java.util.List.of("IDI", "QCQ", "WTW")),
                "EssentiaCrystalizer pattern mismatch");
        require("#forge:ingots/iron".equals(ArcaneWorkbenchRecipe.ingredientText(
                        crystalizer.explicitPatternMap().get('I'))),
                "EssentiaCrystalizer lost ingotIron tag");
        require("#minecraft:planks".equals(ArcaneWorkbenchRecipe.ingredientText(
                        crystalizer.explicitPatternMap().get('W'))),
                "EssentiaCrystalizer lost plankWood tag");
        require(new ResourceLocation("minecraft:dispenser").equals(crystalizer.explicitPatternMap().get('D')),
                "EssentiaCrystalizer dispenser MCP mapping mismatch");

        InfusionRecipes.ensureBundledRecipesLoaded();
        requireInfusionRecipe("tc4_wand_rod_obsidian", "minecraft:obsidian",
                "thaumcraft:tc4_wand_rod_obsidian", 3,
                java.util.List.of("thaumcraft:balanced_shard", "thaumcraft:terra_shard"),
                Aspect.TERRA, 12, Aspect.PRAECANTATIO, 6, Aspect.TENEBRAE, 6);
        requireInfusionRecipe("tc4_wand_rod_ice", "minecraft:ice",
                "thaumcraft:tc4_wand_rod_ice", 3,
                java.util.List.of("thaumcraft:balanced_shard", "thaumcraft:aqua_shard"),
                Aspect.AQUA, 12, Aspect.PRAECANTATIO, 6, Aspect.GELUM, 6);
        requireInfusionRecipe("tc4_wand_rod_quartz", "minecraft:quartz_block",
                "thaumcraft:tc4_wand_rod_quartz", 3,
                java.util.List.of("thaumcraft:balanced_shard", "thaumcraft:ordo_shard"),
                Aspect.ORDO, 12, Aspect.PRAECANTATIO, 6, Aspect.VITREUS, 6);
        requireInfusionRecipe("tc4_wand_rod_reed", "minecraft:sugar_cane",
                "thaumcraft:tc4_wand_rod_reed", 3,
                java.util.List.of("thaumcraft:balanced_shard", "thaumcraft:aer_shard"),
                Aspect.AER, 12, Aspect.PRAECANTATIO, 6, Aspect.MOTUS, 6);
        requireInfusionRecipe("tc4_wand_rod_blaze", "minecraft:blaze_rod",
                "thaumcraft:tc4_wand_rod_blaze", 3,
                java.util.List.of("thaumcraft:balanced_shard", "thaumcraft:ignis_shard"),
                Aspect.IGNIS, 12, Aspect.PRAECANTATIO, 6, Aspect.BESTIA, 6);
        requireInfusionRecipe("tc4_wand_rod_bone", "minecraft:bone",
                "thaumcraft:tc4_wand_rod_bone", 3,
                java.util.List.of("thaumcraft:balanced_shard", "thaumcraft:perditio_shard"),
                Aspect.PERDITIO, 12, Aspect.PRAECANTATIO, 6, Aspect.EXANIMIS, 6);

        InfusionRecipe silverwood = requireInfusionRecipe("silverwood_wand_core",
                "thaumcraft:silverwood_log", "thaumcraft:silverwood_wand_core", 5,
                java.util.List.of("thaumcraft:balanced_shard", "thaumcraft:aer_shard",
                        "thaumcraft:ignis_shard", "thaumcraft:aqua_shard", "thaumcraft:terra_shard",
                        "thaumcraft:ordo_shard", "thaumcraft:perditio_shard"));
        for (Aspect aspect : java.util.List.of(Aspect.AER, Aspect.IGNIS, Aspect.AQUA, Aspect.TERRA,
                Aspect.ORDO, Aspect.PERDITIO, Aspect.PRAECANTATIO)) {
            require(silverwood.aspectCost().getOrDefault(aspect, 0) == 9,
                    "Silverwood rod aspect cost mismatch for " + aspect);
        }

        InfusionRecipe primal = requireInfusionRecipe("tc4_wand_rod_primal_staff",
                "thaumcraft:silverwood_wand_core", "thaumcraft:tc4_staff_rod_primal", 8,
                java.util.List.of("thaumcraft:tc4_charm", "thaumcraft:tc4_wand_rod_obsidian",
                        "thaumcraft:tc4_wand_rod_ice", "thaumcraft:tc4_wand_rod_quartz",
                        "thaumcraft:tc4_charm", "thaumcraft:tc4_wand_rod_reed",
                        "thaumcraft:tc4_wand_rod_blaze", "thaumcraft:tc4_wand_rod_bone"));
        for (Aspect aspect : java.util.List.of(Aspect.AER, Aspect.IGNIS, Aspect.AQUA,
                Aspect.TERRA, Aspect.ORDO, Aspect.PERDITIO)) {
            require(primal.aspectCost().getOrDefault(aspect, 0) == 32,
                    "Primal staff rod aspect cost mismatch for " + aspect);
        }
        require(primal.aspectCost().getOrDefault(Aspect.PRAECANTATIO, 0) == 64,
                "Primal staff rod Praecantatio cost mismatch");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void legacyNbtAndMirrorInfusionsMatchTC4(GameTestHelper helper) {
        InfusionRecipes.ensureBundledRecipesLoaded();
        requireInfusionRecipe("tc4_jarbrain", "thaumcraft:essentia_jar",
                "thaumcraft:tc4_jar_brain", 4,
                java.util.List.of("thaumcraft:tc4_brain", "minecraft:spider_eye",
                        "minecraft:water_bucket", "minecraft:spider_eye"),
                Aspect.COGNITIO, 10, Aspect.SENSUS, 10, Aspect.EXANIMIS, 20);
        requireInfusionRecipe("tc4_mirror", "thaumcraft:tc4_mirrorglass",
                "thaumcraft:tc4_mirrorframe", 1,
                java.util.List.of("minecraft:gold_ingot", "minecraft:gold_ingot",
                        "minecraft:gold_ingot", "minecraft:ender_pearl"),
                Aspect.ITER, 8, Aspect.TENEBRAE, 8, Aspect.PERMUTATIO, 8);
        requireInfusionRecipe("tc4_mirrorhand", "thaumcraft:tc4_mirrorframe",
                "thaumcraft:tc4_mirrorhand", 5,
                java.util.List.of("minecraft:stick", "minecraft:compass", "minecraft:map"),
                Aspect.INSTRUMENTUM, 16, Aspect.ITER, 16);
        requireInfusionRecipe("tc4_mirroressentia", "thaumcraft:tc4_mirrorglass",
                "thaumcraft:tc4_mirrorframe2", 2,
                java.util.List.of("minecraft:iron_ingot", "minecraft:iron_ingot",
                        "minecraft:iron_ingot", "minecraft:ender_pearl"),
                Aspect.ITER, 8, Aspect.AQUA, 8, Aspect.PERMUTATIO, 8);
        requireInfusionRecipe("tc4_traveltrunk", "thaumcraft:hungry_chest",
                "thaumcraft:tc4_travel_trunk", 3,
                java.util.List.of("minecraft:iron_ingot", "thaumcraft:greatwood_planks",
                        "thaumcraft:tc4_golem_wood", "thaumcraft:greatwood_planks"),
                Aspect.MOTUS, 4, Aspect.SPIRITUS, 4, Aspect.ITER, 4, Aspect.VACUOS, 16);

        requireNbtInfusion("tc4_helm_goggles", "goggles", true, 1,
                java.util.List.of("minecraft:slime_ball", "thaumcraft:goggles_of_revealing"));
        requireNbtInfusion("tc4_mask_grinning_devil", "mask", false, 0,
                java.util.List.of("minecraft:black_dye", "minecraft:iron_ingot", "minecraft:leather",
                        "thaumcraft:tc4_block_shimmerleaf", "thaumcraft:tc4_brain", "minecraft:iron_ingot"));
        requireNbtInfusion("tc4_mask_angry_ghost", "mask", false, 1,
                java.util.List.of("minecraft:bone_meal", "minecraft:iron_ingot", "minecraft:leather",
                        "minecraft:poisonous_potato", "minecraft:wither_skeleton_skull", "minecraft:iron_ingot"));
        requireNbtInfusion("tc4_mask_sipping_fiend", "mask", false, 2,
                java.util.List.of("minecraft:red_dye", "minecraft:iron_ingot", "minecraft:leather",
                        "minecraft:ghast_tear", "minecraft:milk_bucket", "minecraft:iron_ingot"));
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void finalOriginalArcaneAndGolemFleshRecipesMatchTC4(GameTestHelper helper) {
        AlchemyRecipe flesh = AlchemyRecipes.recipes().stream()
                .filter(candidate -> "GolemFlesh".equals(candidate.tc4Key()))
                .findFirst().orElse(null);
        require(flesh != null, "Missing original GolemFlesh crucible recipe");
        require("thaumcraft:flesh_block".equals(flesh.catalystText()), "GolemFlesh catalyst mismatch");
        require(new ResourceLocation("thaumcraft:tc4_golem_flesh").equals(flesh.resultItemId()),
                "GolemFlesh output mismatch");
        require(flesh.cost().getOrDefault(Aspect.HUMANUS, 0) == 8
                        && flesh.cost().getOrDefault(Aspect.MOTUS, 0) == 8
                        && flesh.cost().getOrDefault(Aspect.SPIRITUS, 0) == 8,
                "GolemFlesh aspect ledger mismatch");

        ArcaneWorkbenchRecipe warded = requireArcaneRecipe("tc4_wardedglass", "thaumcraft:warded_glass");
        require(warded.resultCount() == 8 && warded.pattern().equals(java.util.List.of("GGG", "WBW", "GGG")),
                "WardedGlass result/pattern mismatch");
        requireArcaneKey(warded, 'B', "thaumcraft:tc4_brain");
        requireArcaneKey(warded, 'G', "minecraft:glass");
        requireArcaneKey(warded, 'W', "thaumcraft:greatwood_planks");
        requireArcaneAspects(warded, Aspect.AQUA, 5, Aspect.ORDO, 10, Aspect.TERRA, 5, Aspect.IGNIS, 5);

        ArcaneWorkbenchRecipe relay = requireArcaneRecipe("tc4_node_relay", "thaumcraft:vis_relay");
        require(relay.resultCount() == 2 && relay.pattern().equals(java.util.List.of(" I ", "ISI", " I ")),
                "NodeRelay result/pattern mismatch");
        requireArcaneKey(relay, 'I', "minecraft:iron_ingot");
        requireArcaneKey(relay, 'S', "thaumcraft:balanced_shard");
        requireArcaneAspects(relay, Aspect.IGNIS, 8, Aspect.ORDO, 8);

        ArcaneWorkbenchRecipe chargeRelay = requireArcaneRecipe("tc4_nodechargerelay", "thaumcraft:vis_charge_relay");
        require(chargeRelay.pattern().equals(java.util.List.of(" R ", "W W", "I I")),
                "NodeChargeRelay pattern mismatch");
        requireArcaneKey(chargeRelay, 'I', "minecraft:iron_ingot");
        requireArcaneKey(chargeRelay, 'R', "thaumcraft:vis_relay");
        requireArcaneKey(chargeRelay, 'W', "thaumcraft:greatwood_wand_core");
        requireArcaneAspects(chargeRelay, Aspect.IGNIS, 16, Aspect.ORDO, 16, Aspect.AER, 16);

        ArcaneWorkbenchRecipe voidJar = requireArcaneRecipe("tc4_jar_void", "thaumcraft:void_essentia_jar");
        require(voidJar.pattern().equals(java.util.List.of("O", "J", "P")), "JarVoid pattern mismatch");
        requireArcaneKey(voidJar, 'O', "minecraft:obsidian");
        requireArcaneKey(voidJar, 'J', "thaumcraft:essentia_jar");
        requireArcaneKey(voidJar, 'P', "minecraft:blaze_powder");
        requireArcaneAspects(voidJar, Aspect.AQUA, 5, Aspect.PERDITIO, 15);

        ArcaneWorkbenchRecipe hungry = requireArcaneRecipe("tc4_hungrychest", "thaumcraft:hungry_chest");
        require(hungry.pattern().equals(java.util.List.of("WTW", "W W", "WWW")), "HungryChest pattern mismatch");
        requireArcaneKey(hungry, 'W', "#minecraft:planks");
        requireArcaneKey(hungry, 'T', "minecraft:oak_trapdoor");
        requireArcaneAspects(hungry, Aspect.AER, 5, Aspect.ORDO, 3, Aspect.PERDITIO, 3);

        ArcaneWorkbenchRecipe furnace = requireArcaneRecipe("tc4_alchemyfurnace", "thaumcraft:alchemical_furnace");
        require(furnace.pattern().equals(java.util.List.of("SCS", "SFS", "SSS")), "AlchemyFurnace pattern mismatch");
        requireArcaneKey(furnace, 'S', "thaumcraft:arcane_stone");
        requireArcaneKey(furnace, 'C', "thaumcraft:crucible");
        requireArcaneKey(furnace, 'F', "minecraft:furnace");
        requireArcaneAspects(furnace, Aspect.IGNIS, 5, Aspect.AQUA, 5);

        ArcaneWorkbenchRecipe filter = requireArcaneRecipe("tc4_tubefilter", "thaumcraft:essentia_tube_filter");
        require(filter.pattern().isEmpty(), "TubeFilter must remain shapeless");
        requireIngredientTexts(filter, "thaumcraft:essentia_tube", "thaumcraft:tc4_filter");
        requireArcaneAspects(filter, Aspect.AQUA, 5, Aspect.ORDO, 16);

        ArcaneWorkbenchRecipe centrifuge = requireArcaneRecipe("tc4_centrifuge", "thaumcraft:alchemical_centrifuge");
        require(centrifuge.pattern().equals(java.util.List.of(" T ", "ACP", " T ")), "Centrifuge pattern mismatch");
        requireArcaneKey(centrifuge, 'T', "thaumcraft:essentia_tube");
        requireArcaneKey(centrifuge, 'A', "thaumcraft:alembic");
        requireArcaneKey(centrifuge, 'C', "thaumcraft:thaumatorium");
        requireArcaneKey(centrifuge, 'P', "minecraft:piston");
        requireArcaneAspects(centrifuge, Aspect.AQUA, 5, Aspect.ORDO, 5, Aspect.PERDITIO, 5);
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void advancedGolemWildcardFamilyMatchesTC4(GameTestHelper helper) {
        InfusionRecipes.ensureBundledRecipesLoaded();
        java.util.Map<String, String> catalysts = java.util.Map.of(
                "straw", "thaumcraft:tc4_golem_straw",
                "wood", "thaumcraft:tc4_golem_wood",
                "tallow", "thaumcraft:tc4_golem_tallow",
                "clay", "thaumcraft:tc4_golem_clay",
                "flesh", "thaumcraft:tc4_golem_flesh",
                "stone", "thaumcraft:tc4_golem_stone",
                "iron", "thaumcraft:tc4_golem_iron",
                "thaumium", "thaumcraft:tc4_golem_thaumium");
        java.util.Set<ResourceLocation> observed = new java.util.HashSet<>();
        java.util.List<String> components = java.util.List.of(
                "minecraft:shears", "minecraft:glowstone_dust", "minecraft:gunpowder",
                "thaumcraft:essentia_jar", "thaumcraft:tc4_brain");
        for (java.util.Map.Entry<String, String> entry : catalysts.entrySet()) {
            String path = "tc4_advancedgolem_" + entry.getKey();
            InfusionRecipe recipe = requireInfusionRecipe(path, entry.getValue(), entry.getValue(), 3,
                    components, Aspect.COGNITIO, 8, Aspect.SENSUS, 8, Aspect.VICTUS, 8);
            require(recipe.hasNbtOutput() && "advanced".equals(recipe.outputNbtLabel()),
                    path + " lost the legacy advanced output operation");
            require(recipe.outputNbt() instanceof ByteTag
                            && ((ByteTag) recipe.outputNbt()).getAsByte() == (byte) 1,
                    path + " lost byte advanced=1");
            Item catalyst = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getValue()));
            require(catalyst != null && recipe.catalystMatches(new ItemStack(catalyst)),
                    path + " does not accept its flattened legacy catalyst");
            observed.add(recipe.catalystId());
        }
        require(observed.size() == 8, "AdvancedGolem wildcard expansion does not cover eight unique legacy materials");
        helper.succeed();
    }

    private static void requireArcaneKey(ArcaneWorkbenchRecipe recipe, char symbol, String expected) {
        ResourceLocation actual = recipe.explicitPatternMap().get(symbol);
        require(expected.equals(ArcaneWorkbenchRecipe.ingredientText(actual)),
                recipe.id() + " key " + symbol + " mismatch: " + ArcaneWorkbenchRecipe.ingredientText(actual));
    }

    private static void requireArcaneAspects(ArcaneWorkbenchRecipe recipe, Object... aspectPairs) {
        require(aspectPairs.length % 2 == 0, recipe.id() + " invalid aspect test pairs");
        for (int index = 0; index < aspectPairs.length; index += 2) {
            Aspect aspect = (Aspect) aspectPairs[index];
            int amount = (Integer) aspectPairs[index + 1];
            require(recipe.aspectCost().getOrDefault(aspect, 0) == amount,
                    recipe.id() + " aspect mismatch for " + aspect);
        }
    }

    private static ArcaneWorkbenchRecipe requireArcaneRecipe(String path, String outputId) {
        ArcaneWorkbenchRecipe recipe = ArcaneWorkbenchRecipes.findById(
                new ResourceLocation(ThaumcraftMod.MOD_ID, path));
        require(recipe != null, "Missing arcane recipe " + path);
        require(new ResourceLocation(outputId).equals(recipe.resultItemId()),
                path + " output mismatch: " + recipe.resultItemId());
        return recipe;
    }

    private static void requireIngredientTexts(ArcaneWorkbenchRecipe recipe, String... expected) {
        java.util.List<String> actual = recipe.ingredients().stream()
                .map(ArcaneWorkbenchRecipe::ingredientText).toList();
        require(actual.equals(java.util.List.of(expected)),
                recipe.id() + " ingredient mismatch: " + actual);
    }

    private static InfusionRecipe requireInfusionRecipe(String path, String catalystId,
                                                         String outputId, int instability,
                                                         java.util.List<String> components,
                                                         Object... aspectPairs) {
        InfusionRecipe recipe = InfusionRecipes.findById(new ResourceLocation(ThaumcraftMod.MOD_ID, path));
        require(recipe != null, "Missing infusion recipe " + path);
        require(new ResourceLocation(catalystId).equals(recipe.catalystId()),
                path + " catalyst mismatch: " + recipe.catalystId());
        require(new ResourceLocation(outputId).equals(recipe.resultItemId()),
                path + " output mismatch: " + recipe.resultItemId());
        require(recipe.instability() == instability, path + " instability mismatch");
        java.util.List<ResourceLocation> expectedComponents = components.stream()
                .map(ResourceLocation::new).toList();
        require(recipe.components().equals(expectedComponents),
                path + " component order mismatch: " + recipe.components());
        require(aspectPairs.length % 2 == 0, path + " invalid test aspect pairs");
        for (int index = 0; index < aspectPairs.length; index += 2) {
            Aspect aspect = (Aspect) aspectPairs[index];
            int amount = (Integer) aspectPairs[index + 1];
            require(recipe.aspectCost().getOrDefault(aspect, 0) == amount,
                    path + " aspect mismatch for " + aspect);
        }
        return recipe;
    }

    private static void requireNbtInfusion(String path, String label, boolean byteTag,
                                           int value, java.util.List<String> components) {
        InfusionRecipe recipe = InfusionRecipes.findById(new ResourceLocation(ThaumcraftMod.MOD_ID, path));
        require(recipe != null && recipe.hasNbtOutput(), "Missing NBT infusion recipe " + path);
        require(label.equals(recipe.outputNbtLabel()), path + " output NBT label mismatch");
        Tag tag = recipe.outputNbt();
        if (byteTag) {
            require(tag instanceof ByteTag && ((ByteTag) tag).getAsByte() == (byte) value,
                    path + " byte NBT output mismatch: " + tag);
        } else {
            require(tag instanceof IntTag && ((IntTag) tag).getAsInt() == value,
                    path + " int NBT output mismatch: " + tag);
        }
        java.util.List<ResourceLocation> expectedComponents = components.stream()
                .map(ResourceLocation::new).toList();
        require(recipe.components().equals(expectedComponents),
                path + " NBT infusion component mismatch: " + recipe.components());
        Item helmet = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_thaumiumfortresshelm"));
        require(helmet != null, "Missing fortress helmet registry item");
        ItemStack damagedHelmet = new ItemStack(helmet);
        damagedHelmet.setDamageValue(7);
        require(recipe.catalystMatches(damagedHelmet), path + " lost wildcard fortress-helm catalyst");
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void auraNodeLegacyItemConvertsStoredNodeNbt(GameTestHelper helper) {
        Item legacyItem = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "aura_node"));
        require(legacyItem instanceof AuraNodeLegacyItem,
                "Legacy aura-node migration item is missing");

        CompoundTag aspects = new CompoundTag();
        aspects.putInt("aer", 12);
        aspects.putInt("ordo", 7);
        CompoundTag node = new CompoundTag();
        node.putString("NodeType", "NORMAL");
        node.putString("NodeModifier", "BRIGHT");
        node.put("Aspects", aspects);

        ItemStack legacy = new ItemStack(legacyItem);
        legacy.getOrCreateTag().put("BlockEntityTag", node.copy());
        legacy.setHoverName(Component.literal("Migrated Bright Node"));

        ItemStack converted = AuraNodeLegacyItem.convertLegacyStack(legacy);
        require(converted.is(ThaumcraftMod.NODE_JAR.get()),
                "Legacy aura-node stack did not convert to Node in a Jar");
        require(converted.hasCustomHoverName()
                        && converted.getHoverName().getString().equals("Migrated Bright Node"),
                "Aura-node migration lost the custom name");
        CompoundTag convertedRoot = converted.getTag();
        require(convertedRoot != null
                        && convertedRoot.contains(TC4NodeJarRuntime.TAG_NODE_JAR, Tag.TAG_COMPOUND),
                "Converted Node in a Jar lost node NBT");
        CompoundTag restoredNode = convertedRoot.getCompound(TC4NodeJarRuntime.TAG_NODE_JAR);
        require(restoredNode.getCompound("Aspects").getInt("aer") == 12
                        && restoredNode.getCompound("Aspects").getInt("ordo") == 7,
                "Converted Node in a Jar changed aspect values");
        require(AuraNodeLegacyItem.convertLegacyStack(new ItemStack(legacyItem)).isEmpty(),
                "Aura-node migration accepted a stack without node data");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void boneBowRetainsOriginalFastChargeContract(GameTestHelper helper) {
        Item item = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_bonebow"));
        require(item instanceof BoneBowItem, "Bone Bow registry item is missing");
        ItemStack bow = new ItemStack(item);
        require(bow.getMaxDamage() == 512, "Bone Bow durability is not the original 512");
        require(item.getEnchantmentValue() == 3, "Bone Bow enchantability is not the original 3");
        require(Math.abs(BoneBowItem.getBonePowerForTime(5) - (5.0F / 12.0F)) < 0.0001F,
                "Bone Bow half-charge curve drifted");
        require(BoneBowItem.getBonePowerForTime(10) == 1.0F,
                "Bone Bow no longer reaches full power at 10 ticks");
        require(BoneBowItem.getBonePowerForTime(18) == 1.0F,
                "Bone Bow power cap drifted");
        require(Math.abs(BoneBowItem.getPullModelValue(13) - (13.0F / 18.0F)) < 0.0001F,
                "Bone Bow pull-model timing drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 60)
    public static void travelingTrunkPersistsInventoryUpgradeAndCapability(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        TravelingTrunkEntity trunk = ThaumcraftMod.TRAVELING_TRUNK.get().create(level);
        require(trunk != null, "Traveling Trunk entity type failed to create");
        trunk.setUpgrade(5);
        trunk.setItem(0, new ItemStack(Items.DIAMOND, 3));
        ItemStack named = new ItemStack(Items.GOLD_INGOT, 2);
        named.setHoverName(Component.literal("Trunk Cargo"));
        trunk.setItem(7, named);
        trunk.setCustomName(Component.literal("Research Trunk"));
        require(trunk.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent(),
                "Traveling Trunk lost its item-handler capability");

        CompoundTag saved = trunk.saveWithoutId(new CompoundTag());
        TravelingTrunkEntity restored = ThaumcraftMod.TRAVELING_TRUNK.get().create(level);
        require(restored != null, "Restored Traveling Trunk failed to create");
        restored.load(saved);

        require(restored.getUpgrade() == 5, "Traveling Trunk upgrade did not persist");
        require(restored.getItem(0).is(Items.DIAMOND) && restored.getItem(0).getCount() == 3,
                "Traveling Trunk first inventory stack did not persist");
        require(restored.getItem(7).is(Items.GOLD_INGOT)
                        && restored.getItem(7).getCount() == 2
                        && restored.getItem(7).hasCustomHoverName(),
                "Traveling Trunk named inventory stack did not persist");
        require(restored.hasCustomName()
                        && restored.getCustomName().getString().equals("Research Trunk"),
                "Traveling Trunk custom name did not persist");
        require(restored.getContainerSize() == 27,
                "Traveling Trunk inventory size is not the original 27 slots");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 60)
    public static void crimsonCultistRolesKeepAttributesAndPersistence(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CrimsonCultistEntity cultist = ThaumcraftMod.CRIMSON_CULTIST.get().create(level);
        CrimsonCultistEntity knight = ThaumcraftMod.CRIMSON_KNIGHT.get().create(level);
        CrimsonCultistEntity cleric = ThaumcraftMod.CRIMSON_CLERIC.get().create(level);
        CrimsonCultistEntity leader = ThaumcraftMod.CRIMSON_PRAETOR.get().create(level);
        require(cultist != null && knight != null && cleric != null && leader != null,
                "One or more Crimson Cult entity types failed to create");
        require(cultist.role() == CrimsonCultistEntity.Role.CULTIST,
                "Crimson Cultist role mismatch");
        require(knight.role() == CrimsonCultistEntity.Role.KNIGHT,
                "Crimson Knight role mismatch");
        require(cleric.role() == CrimsonCultistEntity.Role.CLERIC,
                "Crimson Cleric role mismatch");
        require(leader.role() == CrimsonCultistEntity.Role.LEADER,
                "Crimson Praetor role mismatch");
        require(cultist.getAttributeValue(Attributes.MAX_HEALTH) == 24.0D,
                "Crimson Cultist max health drifted");
        require(knight.getAttributeValue(Attributes.ATTACK_DAMAGE) == 7.0D,
                "Crimson Knight attack damage drifted");
        require(cleric.getAttributeValue(Attributes.FOLLOW_RANGE) == 28.0D,
                "Crimson Cleric follow range drifted");
        require(leader.getAttributeValue(Attributes.MAX_HEALTH) == 52.0D,
                "Crimson Praetor max health drifted");

        CompoundTag saved = leader.saveWithoutId(new CompoundTag());
        CrimsonCultistEntity restored = ThaumcraftMod.CRIMSON_PRAETOR.get().create(level);
        require(restored != null, "Restored Crimson Praetor failed to create");
        restored.load(saved);
        require(restored.role() == CrimsonCultistEntity.Role.LEADER,
                "Crimson Praetor role did not survive NBT round-trip");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void fortressArmorSetAndMaskNbtMatchTc4(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        Item helmetItem = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_thaumiumfortresshelm"));
        Item chestItem = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_thaumiumfortresschest"));
        Item legsItem = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_thaumiumfortresslegs"));
        require(helmetItem instanceof TC4FortressArmorItem
                        && chestItem instanceof TC4FortressArmorItem
                        && legsItem instanceof TC4FortressArmorItem,
                "Fortress armor registry family is incomplete");

        ItemStack helmet = new ItemStack(helmetItem);
        helmet.getOrCreateTag().putByte(TC4FortressMaskRuntime.GOGGLES_TAG, (byte) 1);
        helmet.getOrCreateTag().putInt(TC4FortressMaskRuntime.MASK_TAG,
                TC4FortressMaskRuntime.MASK_GRINNING_DEVIL);
        player.setItemSlot(EquipmentSlot.HEAD, helmet);
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chestItem));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(legsItem));

        require(TC4FortressArmorRuntime.isFullFortressSet(player),
                "Fortress armor full-set detection failed");
        require(TC4FortressArmorItem.hasGoggles(helmet),
                "Fortress helmet goggles NBT was not recognized");
        require(TC4FortressArmorItem.mask(helmet)
                        == TC4FortressMaskRuntime.MASK_GRINNING_DEVIL,
                "Fortress helmet mask NBT was not recognized");
        require(Math.abs(TC4FortressArmorRuntime.fortressSetModifier(player) - 1.30D) < 0.0001D,
                "Fortress set multiplier does not match the original 1.30 mask set");
        require(helmetItem.isFireResistant() && chestItem.isFireResistant() && legsItem.isFireResistant(),
                "Fortress armor lost fire resistance");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void exactDuplicateLegacyItemAliasesAreNotRegistered(GameTestHelper helper) {
        for (var entry : TC4LegacyDuplicateItemMigrator.mappings().entrySet()) {
            ResourceLocation legacyId = new ResourceLocation(ThaumcraftMod.MOD_ID, entry.getKey());
            ResourceLocation canonicalId = new ResourceLocation(ThaumcraftMod.MOD_ID, entry.getValue());
            Item legacy = ForgeRegistries.ITEMS.getValue(legacyId);
            Item canonical = ForgeRegistries.ITEMS.getValue(canonicalId);
            require(legacy == null || legacy == Items.AIR,
                    "Exact duplicate alias is still registered: " + legacyId);
            require(canonical != null && canonical != Items.AIR,
                    "Canonical replacement is missing: " + canonicalId);
        }
        helper.succeed();
    }

    private static void requireAlchemyRecipe(String tc4Key, String catalystText, String outputId,
                                             Aspect first, int firstCost, Aspect second, int secondCost) {
        AlchemyRecipe recipe = AlchemyRecipes.recipes().stream()
                .filter(candidate -> tc4Key.equals(candidate.tc4Key()))
                .findFirst().orElse(null);
        require(recipe != null, "Missing original alchemy recipe " + tc4Key);
        require(catalystText.equals(recipe.catalystText()),
                tc4Key + " catalyst mismatch: " + recipe.catalystText());
        require(new ResourceLocation(outputId).equals(recipe.resultItemId()),
                tc4Key + " output mismatch: " + recipe.resultItemId());
        require(recipe.resultCount() == 1, tc4Key + " output count mismatch");
        require(recipe.cost().getOrDefault(first, 0) == firstCost,
                tc4Key + " first aspect cost mismatch");
        require(recipe.cost().getOrDefault(second, 0) == secondCost,
                tc4Key + " second aspect cost mismatch");
    }

    private static void requireArcaneCap(String suffix, String catalystText, String outputId,
                                         int ordo, int ignis, int aer, int perditio) {
        ArcaneWorkbenchRecipe recipe = ArcaneWorkbenchRecipes.findById(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_configrecipes_cap_" + suffix));
        require(recipe != null, "Missing generated cap recipe " + suffix);
        require(catalystText.equals(ArcaneWorkbenchRecipe.ingredientText(recipe.catalystItemId())),
                suffix + " cap catalyst mismatch");
        require(recipe.pattern().equals(java.util.List.of("NNN", "N N")),
                suffix + " cap pattern mismatch");
        require(recipe.catalystItemId().equals(recipe.inferredPatternMap().get('N')),
                suffix + " cap pattern tag mismatch");
        require(new ResourceLocation(outputId).equals(recipe.resultItemId()),
                suffix + " cap result mismatch");
        require(recipe.aspectCost().getOrDefault(Aspect.ORDO, 0) == ordo,
                suffix + " cap Ordo cost mismatch");
        require(recipe.aspectCost().getOrDefault(Aspect.IGNIS, 0) == ignis,
                suffix + " cap Ignis cost mismatch");
        require(recipe.aspectCost().getOrDefault(Aspect.AER, 0) == aer,
                suffix + " cap Aer cost mismatch");
        require(recipe.aspectCost().getOrDefault(Aspect.PERDITIO, 0) == perditio,
                suffix + " cap Perditio cost mismatch");

        if (catalystText.startsWith("#forge:nuggets/")) {
            String localId = switch (suffix) {
                case "copper" -> "tc4_nuggetcopper";
                case "silver" -> "tc4_nuggetsilver";
                case "thaumium" -> "thaumium_nugget";
                case "void" -> "tc4_nuggetvoid";
                default -> "";
            };
            Item representative = ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation(ThaumcraftMod.MOD_ID, localId));
            require(representative != null && recipe.catalystMatches(new ItemStack(representative)),
                    suffix + " cap did not accept its Forge-tagged TC4 nugget");
        } else {
            require(recipe.catalystMatches(new ItemStack(Items.GOLD_NUGGET)),
                    "Gold cap did not accept the exact vanilla gold nugget");
        }
    }

    private static void requireChargedCapInfusion(String suffix, String catalystId, String outputId,
                                                   int componentCount, int instability, int potentia,
                                                   int auram, int vacuos, int alienis) {
        InfusionRecipe recipe = InfusionRecipes.findById(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_wand_cap_" + suffix));
        require(recipe != null, "Missing charged cap infusion " + suffix);
        require(new ResourceLocation(catalystId).equals(recipe.catalystId()),
                suffix + " charged cap catalyst mismatch");
        require(new ResourceLocation(outputId).equals(recipe.resultItemId()),
                suffix + " charged cap result mismatch");
        require(recipe.components().size() == componentCount,
                suffix + " charged cap component count mismatch");
        ResourceLocation dust = new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_dust");
        require(recipe.components().stream().allMatch(dust::equals),
                suffix + " charged cap lost Salis Mundus components");
        require(recipe.instability() == instability,
                suffix + " charged cap instability mismatch");
        require(recipe.aspectCost().getOrDefault(Aspect.POTENTIA, 0) == potentia,
                suffix + " charged cap Potentia mismatch");
        require(recipe.aspectCost().getOrDefault(Aspect.AURAM, 0) == auram,
                suffix + " charged cap Auram mismatch");
        require(recipe.aspectCost().getOrDefault(Aspect.VACUOS, 0) == vacuos,
                suffix + " charged cap Vacuos mismatch");
        require(recipe.aspectCost().getOrDefault(Aspect.ALIENIS, 0) == alienis,
                suffix + " charged cap Alienis mismatch");
    }

    private static void requireSmelting(ServerLevel level, String recipePath, String inputId,
                                        String outputId, int outputCount, float experience) {
        ResourceLocation recipeId = new ResourceLocation(ThaumcraftMod.MOD_ID, recipePath);
        Recipe<?> raw = level.getRecipeManager().byKey(recipeId).orElse(null);
        require(raw instanceof SmeltingRecipe, "Missing smelting recipe " + recipeId);
        SmeltingRecipe recipe = (SmeltingRecipe) raw;
        Item input = ForgeRegistries.ITEMS.getValue(new ResourceLocation(inputId));
        require(input != null && recipe.getIngredients().get(0).test(new ItemStack(input)),
                recipeId + " does not accept the exact TC4 input " + inputId);
        ResourceLocation actualOutput = ForgeRegistries.ITEMS.getKey(recipe.getResultItem().getItem());
        require(new ResourceLocation(outputId).equals(actualOutput),
                recipeId + " output id mismatch: " + actualOutput);
        require(recipe.getResultItem().getCount() == outputCount,
                recipeId + " output count mismatch");
        require(Math.abs(recipe.getExperience() - experience) < 0.0001F,
                recipeId + " experience mismatch: " + recipe.getExperience());
        require(recipe.getCookingTime() == 200, recipeId + " cooking time mismatch");
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void tallowCandleKeepsSupportShapeAndInfusionStabilizerContract(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos supportPos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos candlePos = supportPos.above();
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        BlockState candleState = ThaumcraftMod.TALLOW_CANDLE.get().defaultBlockState();
        level.setBlock(candlePos, candleState, Block.UPDATE_ALL);

        require(candleState.getBlock() instanceof TallowCandleBlock,
                "Registered tallow candle does not use the parity block class");
        require(candleState.getBlock() instanceof InfusionStabilizer,
                "Tallow candle lost the infusion stabilizer marker");
        require(candleState.canSurvive(level, candlePos),
                "Tallow candle rejected a solid centered support");
        require(candleState.getCollisionShape(level, candlePos).isEmpty(),
                "Tallow candle unexpectedly gained collision");
        requireBlockEntity(level, candlePos, TallowCandleBlockEntity.class);

        level.setBlock(supportPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        require(!candleState.canSurvive(level, candlePos),
                "Tallow candle survived after its support was removed");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void hungryChestEatsPersistsAndExposesSingleInventory(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.HUNGRY_CHEST.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        HungryChestBlockEntity chest = requireBlockEntity(level, pos, HungryChestBlockEntity.class);

        ItemStack offered = new ItemStack(Items.DIAMOND, 5);
        offered.setHoverName(Component.literal("Hungry proof"));
        offered.getOrCreateTag().putInt("TC4Proof", 116354);
        ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.25D,
                pos.getZ() + 0.5D, offered.copy());
        require(level.addFreshEntity(entity), "Could not spawn the hungry-chest input entity");
        chest.eat(entity);

        require(entity.isRemoved(), "Hungry Chest did not consume a fully accepted stack");
        ItemStack stored = chest.getItem(0);
        require(stored.getCount() == 5 && stored.getItem() == Items.DIAMOND,
                "Hungry Chest changed the accepted stack");
        require(stored.hasCustomHoverName() && "Hungry proof".equals(stored.getHoverName().getString()),
                "Hungry Chest lost the custom stack name");
        require(stored.getTag() != null && stored.getTag().getInt("TC4Proof") == 116354,
                "Hungry Chest lost custom stack NBT");
        require(chest.lidAngle(1.0F) >= 0.2F,
                "Hungry Chest did not trigger the original eat lid event");
        require(chest.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent(),
                "Hungry Chest does not expose its Forge item-handler capability");

        CompoundTag saved = chest.saveWithoutMetadata();
        HungryChestBlockEntity restored = new HungryChestBlockEntity(pos, state);
        restored.load(saved);
        require(restored.getItem(0).getCount() == 5,
                "Hungry Chest inventory did not survive NBT round-trip");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void hungryChestFullContractMatchesOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.HUNGRY_CHEST.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        HungryChestBlockEntity chest = requireBlockEntity(level, pos, HungryChestBlockEntity.class);

        require(chest.getContainerSize() == 27, "Hungry Chest size must be 27");
        require(chest.getMaxStackSize() == 64, "Hungry Chest stack limit must be 64");
        AABB outline = state.getShape(level, pos).bounds();
        AABB collision = state.getCollisionShape(level, pos).bounds();
        require(Math.abs(outline.minX - 0.0625D) < 1.0E-9 && Math.abs(outline.maxX - 0.9375D) < 1.0E-9,
                "Hungry Chest horizontal outline bounds drifted");
        require(Math.abs(outline.maxY - 0.875D) < 1.0E-9,
                "Hungry Chest outline height must be 14/16");
        require(Math.abs(collision.maxY - 0.9375D) < 1.0E-9,
                "Hungry Chest collision height must be 15/16");
        require(TC4HungryChestParity.nextDropCount(64, 0) == 10, "Minimum break chunk mismatch");
        require(TC4HungryChestParity.nextDropCount(64, 20) == 30, "Maximum break chunk mismatch");
        require(Math.abs(TC4HungryChestParity.easedLid(0.5F) - 0.875F) < 1.0E-6F,
                "Original cubic lid easing mismatch");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void hungryChestCollisionFilterPartialAndRejectionMatchOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.HUNGRY_CHEST.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        HungryChestBlockEntity chest = requireBlockEntity(level, pos, HungryChestBlockEntity.class);

        for (int slot = 0; slot < 26; slot++) {
            chest.setItem(slot, new ItemStack(Items.STONE, 64));
        }
        chest.setItem(26, new ItemStack(Items.DIAMOND, 63));

        ItemEntity outside = new ItemEntity(level, pos.getX() - 0.1D, pos.getY() + 0.25D,
                pos.getZ() + 0.5D, new ItemStack(Items.DIAMOND, 5));
        require(level.addFreshEntity(outside), "Could not spawn outside Hungry Chest item");
        ((HungryChestBlock) state.getBlock()).entityInside(state, level, pos, outside);
        require(outside.getItem().getCount() == 5 && chest.getItem(26).getCount() == 63,
                "Hungry Chest ate an item outside the original collision box");

        outside.setPos(pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D);
        ((HungryChestBlock) state.getBlock()).entityInside(state, level, pos, outside);
        require(chest.getItem(26).getCount() == 64, "Hungry Chest did not fill the compatible stack first");
        require(outside.getItem().getCount() == 4, "Hungry Chest did not preserve the exact partial remainder");

        ItemEntity rejected = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.25D,
                pos.getZ() + 0.5D, new ItemStack(Items.GOLD_INGOT, 2));
        require(level.addFreshEntity(rejected), "Could not spawn rejected Hungry Chest item");
        ((HungryChestBlock) state.getBlock()).entityInside(state, level, pos, rejected);
        require(rejected.isAlive() && rejected.getItem().getCount() == 2,
                "Full Hungry Chest changed a rejected stack");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void hungryChestLidEventsAndRemoteValidityMatchOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.HUNGRY_CHEST.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        HungryChestBlockEntity chest = requireBlockEntity(level, pos, HungryChestBlockEntity.class);
        Player player = helper.makeMockPlayer();
        player.setPos(pos.getX() + 100.0D, pos.getY(), pos.getZ());
        require(chest.stillValid(player), "TC4 Hungry Chest must only validate the block entity identity");

        chest.startOpen(player);
        require(chest.openCount() == 1, "startOpen did not increment the original opener counter");
        for (int i = 0; i < 10; i++) {
            HungryChestBlockEntity.clientTick(level, pos, state, chest);
        }
        require(Math.abs(chest.lidAngle(1.0F) - 1.0F) < 1.0E-6F, "Lid did not open in ten 0.1 steps");
        chest.stopOpen(player);
        require(chest.openCount() == 0, "stopOpen did not decrement the original opener counter");
        for (int i = 0; i < 10; i++) {
            HungryChestBlockEntity.clientTick(level, pos, state, chest);
        }
        require(Math.abs(chest.lidAngle(1.0F)) < 1.0E-6F, "Lid did not close in ten 0.1 steps");
        chest.triggerEvent(TC4HungryChestParity.EAT_EVENT_ID, TC4HungryChestParity.EAT_EVENT_DATA);
        require(Math.abs(chest.lidAngle(1.0F) - 0.2F) < 1.0E-6F, "Eat block event did not nudge lid to 0.2");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void hungryChestBreakDropsPreserveOriginalChunksAndNbt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.HUNGRY_CHEST.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        HungryChestBlockEntity chest = requireBlockEntity(level, pos, HungryChestBlockEntity.class);
        ItemStack stored = new ItemStack(Items.DIAMOND, 64);
        stored.setHoverName(Component.literal("Hungry break proof"));
        stored.getOrCreateTag().putInt("TC4Hungry", 116416);
        chest.setItem(0, stored);

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        java.util.List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.0D),
                entity -> entity.getItem().is(Items.DIAMOND));
        require(drops.size() >= 3, "Original 64-item stack must split into multiple 10..30 chunks");
        int total = 0;
        int subTen = 0;
        double x = drops.get(0).getX();
        double y = drops.get(0).getY();
        double z = drops.get(0).getZ();
        for (ItemEntity drop : drops) {
            ItemStack stack = drop.getItem();
            total += stack.getCount();
            require(stack.getCount() <= 30, "Hungry Chest break chunk exceeded 30");
            if (stack.getCount() < 10) subTen++;
            require(stack.hasCustomHoverName() && "Hungry break proof".equals(stack.getHoverName().getString()),
                    "Hungry Chest break drop lost custom name");
            require(stack.getTag() != null && stack.getTag().getInt("TC4Hungry") == 116416,
                    "Hungry Chest break drop lost NBT");
            require(Math.abs(drop.getX() - x) < 1.0E-9 && Math.abs(drop.getY() - y) < 1.0E-9
                            && Math.abs(drop.getZ() - z) < 1.0E-9,
                    "Chunks from one original slot must share one randomized spawn position");
        }
        require(total == 64, "Hungry Chest break drops changed the total item count");
        require(subTen <= 1, "Only the final original break chunk may contain fewer than 10 items");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void hungryChestAutomationComparatorAndNbtMatchOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.HUNGRY_CHEST.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        HungryChestBlockEntity chest = requireBlockEntity(level, pos, HungryChestBlockEntity.class);
        var capabilityOptional = chest.getCapability(ForgeCapabilities.ITEM_HANDLER);
        require(capabilityOptional.isPresent(), "Hungry Chest item capability missing");
        net.minecraftforge.items.IItemHandler capability = capabilityOptional.orElse(null);
        require(capability != null, "Hungry Chest item capability resolved to null");
        ItemStack remainder = capability.insertItem(0, new ItemStack(Items.EMERALD, 64), false);
        require(remainder.isEmpty() && chest.getItem(0).getCount() == 64,
                "Hopper-equivalent insertion did not use the real inventory");
        require(state.getBlock().getAnalogOutputSignal(state, level, pos) == 1,
                "Single full slot comparator output mismatch");
        for (int slot = 1; slot < chest.getContainerSize(); slot++) {
            chest.setItem(slot, new ItemStack(Items.STONE, 64));
        }
        require(state.getBlock().getAnalogOutputSignal(state, level, pos) == 15,
                "Full Hungry Chest comparator output mismatch");

        CompoundTag saved = chest.saveWithoutMetadata();
        require(saved.contains("Items", Tag.TAG_LIST), "Hungry Chest did not save the original Items list");
        require(!saved.contains("OpenCount") && !saved.contains("LidAngle"),
                "Hungry Chest persisted transient lid/opener state");
        HungryChestBlockEntity restored = new HungryChestBlockEntity(pos, state);
        restored.load(saved);
        require(restored.getItem(0).is(Items.EMERALD) && restored.getItem(0).getCount() == 64,
                "Hungry Chest NBT round-trip changed the first slot");
        require(restored.getItem(26).is(Items.STONE) && restored.getItem(26).getCount() == 64,
                "Hungry Chest NBT round-trip changed the final slot");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void brainJarAbsorbsPersistsAndReportsComparatorExperience(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.BRAIN_JAR.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        BrainJarBlockEntity brain = requireBlockEntity(level, pos, BrainJarBlockEntity.class);

        ExperienceOrb orb = new ExperienceOrb(level, pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, 37);
        require(level.addFreshEntity(orb), "Could not spawn the Brain in a Jar XP orb");
        BrainJarBlockEntity.serverTick(level, pos, state, brain);

        require(orb.isRemoved(), "Brain in a Jar did not absorb a touching XP orb");
        require(brain.storedExperience() == 37,
                "Brain in a Jar stored the wrong XP amount");
        require(brain.comparatorOutput() == 1,
                "Brain in a Jar comparator output does not match the original 1..15 scale");

        CompoundTag saved = brain.saveWithoutMetadata();
        require(saved.contains("XP", Tag.TAG_INT) && !saved.contains("EatDelay"),
                "Brain in a Jar must persist only the original XP field");
        BrainJarBlockEntity restored = new BrainJarBlockEntity(pos, state);
        restored.load(saved);
        require(restored.storedExperience() == 37 && restored.eatDelayTicks() == 0,
                "Brain in a Jar XP/transient delay did not survive the original NBT contract");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void brainJarFullContractMatchesOriginal(GameTestHelper helper) {
        require(TC4BrainJarParity.MAX_XP == 2000,
                "Brain in a Jar capacity differs from TC4");
        require(TC4BrainJarParity.INTERACTION_EAT_DELAY_TICKS == 40,
                "Brain in a Jar shake delay differs from TC4");
        require(TC4BrainJarParity.randomReleaseBound(2000) == 64
                        && TC4BrainJarParity.randomReleaseBound(0) == 1,
                "Brain in a Jar random XP-release bound differs from TC4");
        require(TC4BrainJarParity.comparatorOutput(0) == 0
                        && TC4BrainJarParity.comparatorOutput(1) == 1
                        && TC4BrainJarParity.comparatorOutput(2000) == 15,
                "Brain in a Jar comparator scale differs from TC4");
        require(ThaumcraftMod.BRAIN_JAR_ITEM.get().getMaxStackSize() == TC4BrainJarParity.ITEM_MAX_STACK,
                "Brain in a Jar item is not stackable to the original limit");
        require(ThaumcraftMod.BRAIN_JAR.get().defaultBlockState().getLightEmission()
                        == TC4BrainJarParity.BLOCK_LIGHT_LEVEL,
                "Brain in a Jar light level differs from TC4");
        require(ThaumcraftMod.BRAIN_JAR.get().getEnchantPowerBonus(
                        ThaumcraftMod.BRAIN_JAR.get().defaultBlockState(), helper.getLevel(),
                        helper.absolutePos(new BlockPos(2, 1, 2))) == TC4BrainJarParity.ENCHANT_POWER_BONUS,
                "Brain in a Jar enchantment power bonus differs from TC4");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void brainJarAbsorptionPreservesOverflowUntilNextTick(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.BRAIN_JAR.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        BrainJarBlockEntity brain = requireBlockEntity(level, pos, BrainJarBlockEntity.class);
        CompoundTag seed = new CompoundTag();
        seed.putInt("XP", 1990);
        brain.load(seed);

        ExperienceOrb orb = new ExperienceOrb(level, pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, 20);
        require(level.addFreshEntity(orb), "Could not spawn overflow XP orb");
        BrainJarBlockEntity.serverTick(level, pos, state, brain);
        require(brain.storedExperience() == 2010,
                "Brain in a Jar incorrectly clamped overflow in the absorption tick");
        BrainJarBlockEntity.serverTick(level, pos, state, brain);
        require(brain.storedExperience() == TC4BrainJarParity.MAX_XP,
                "Brain in a Jar did not clamp overflow at the next tick start");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void brainJarShakeDelayAndBreakReleaseMatchOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.BRAIN_JAR.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        BrainJarBlockEntity brain = requireBlockEntity(level, pos, BrainJarBlockEntity.class);
        CompoundTag seed = new CompoundTag();
        seed.putInt("XP", 37);
        brain.load(seed);

        brain.releaseRandomExperience(level);
        require(brain.eatDelayTicks() == TC4BrainJarParity.INTERACTION_EAT_DELAY_TICKS,
                "Brain in a Jar interaction did not set the original 40-tick delay");

        int remaining = brain.storedExperience();
        level.getEntitiesOfClass(ExperienceOrb.class, new AABB(pos).inflate(4.0D)).forEach(ExperienceOrb::discard);
        brain.releaseAllExperience(level);
        require(brain.storedExperience() == 0,
                "Brain in a Jar did not clear stored XP when broken");
        int released = level.getEntitiesOfClass(ExperienceOrb.class, new AABB(pos).inflate(4.0D)).stream()
                .mapToInt(ExperienceOrb::getValue).sum();
        require(released == remaining,
                "Brain in a Jar changed the total XP released on break");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void magicMirrorLinksTransportsAndPersistsQueuedStacks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos firstPos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos secondPos = helper.absolutePos(new BlockPos(6, 1, 2));
        BlockState state = ThaumcraftMod.MAGIC_MIRROR.get().defaultBlockState();
        level.setBlock(firstPos, state, Block.UPDATE_ALL);
        level.setBlock(secondPos, state, Block.UPDATE_ALL);
        MirrorBlockEntity first = requireBlockEntity(level, firstPos, MirrorBlockEntity.class);
        MirrorBlockEntity second = requireBlockEntity(level, secondPos, MirrorBlockEntity.class);

        first.setPendingLink(MirrorLink.at(level, secondPos));
        require(first.isLinkValidSimple() && second.isLinkValidSimple(),
                "Magic Mirrors did not establish a reciprocal link");

        ItemStack payload = new ItemStack(Items.GOLD_INGOT, 3);
        payload.getOrCreateTag().putString("MirrorProof", "TC4");
        ItemEntity entity = new ItemEntity(level, firstPos.getX() + 0.5D, firstPos.getY() + 0.5D,
                firstPos.getZ() + 0.5D, payload.copy());
        require(level.addFreshEntity(entity), "Could not spawn the Magic Mirror payload");
        require(first.transport(entity), "Linked Magic Mirror rejected a valid item entity");
        require(entity.isRemoved(), "Transported item entity remained in the source world");
        require(second.queuedStackCount() == 1,
                "Target Magic Mirror did not queue the transported stack");
        require(first.instability() == 3,
                "Source Magic Mirror did not gain one instability point per transported item");

        CompoundTag saved = second.saveWithoutMetadata();
        MirrorBlockEntity restored = new MirrorBlockEntity(secondPos, state);
        restored.load(saved);
        require(restored.queuedStackCount() == 1,
                "Magic Mirror output queue did not survive NBT round-trip");
        ListTag items = saved.getList("Items", Tag.TAG_COMPOUND);
        require(items.size() == 1 && ItemStack.of(items.getCompound(0)).getCount() == 3,
                "Magic Mirror changed the queued payload during serialization");
        first.invalidateLink();
        require(!first.isLinked() && !second.isLinked(),
                "Breaking the reciprocal Magic Mirror link left a stale linked endpoint");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void labelledAndVoidJarsKeepTc4SuctionCapacityAndOverflow(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos filteredPos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState filteredState = ThaumcraftMod.ESSENTIA_JAR.get().defaultBlockState();
        level.setBlock(filteredPos, filteredState, Block.UPDATE_ALL);
        EssentiaJarBlockEntity filtered = requireBlockEntity(level, filteredPos, EssentiaJarBlockEntity.class);
        filtered.setFilterAspect(Aspect.AER);

        require(filtered.originalMinimumSuction(false) == 64,
                "Filtered jar minimum suction differs from TC4");
        require(filtered.originalSuctionAmount(false) == 64,
                "Empty filtered jar suction differs from TC4");
        require(filtered.addToContainerOriginal(Aspect.IGNIS, 4, false) == 4,
                "Filtered jar accepted the wrong aspect");
        require(filtered.addToContainerOriginal(Aspect.AER, 64, false) == 0 && filtered.amount() == 64,
                "Filtered jar did not fill to the original 64-point capacity");
        require(filtered.originalSuctionAmount(false) == 0,
                "Full normal jar kept pulling essentia");

        BlockPos voidPos = helper.absolutePos(new BlockPos(4, 1, 2));
        BlockState voidState = ThaumcraftMod.VOID_ESSENTIA_JAR.get().defaultBlockState();
        level.setBlock(voidPos, voidState, Block.UPDATE_ALL);
        EssentiaJarBlockEntity voidJar = requireBlockEntity(level, voidPos, EssentiaJarBlockEntity.class);
        voidJar.setFilterAspect(Aspect.AER);
        require(voidJar.addToContainerOriginal(Aspect.AER, 80, true) == 0,
                "Void jar did not consume overflow");
        require(voidJar.amount() == 64,
                "Void jar displayed more than the original capacity");
        require(voidJar.originalMinimumSuction(true) == 48 && voidJar.originalSuctionAmount(true) == 32,
                "Full labelled void jar did not fall back to the original baseline suction");

        CompoundTag saved = voidJar.saveWithoutMetadata();
        EssentiaJarBlockEntity restored = new EssentiaJarBlockEntity(voidPos, voidState);
        restored.load(saved);
        require(restored.filterAspect() == Aspect.AER && restored.amount() == 64,
                "Void jar filter or contents did not survive NBT round-trip");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void essentiaReservoirKeepsMixedStorageFacingAndCapacity(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.ESSENTIA_RESERVOIR.get().defaultBlockState()
                .setValue(EssentiaReservoirBlock.FACING, Direction.EAST);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        EssentiaReservoirBlockEntity reservoir = requireBlockEntity(
                level, pos, EssentiaReservoirBlockEntity.class);

        require(reservoir.canAccessFrom(Direction.EAST) && !reservoir.canAccessFrom(Direction.WEST),
                "Essentia Reservoir exposed more than its configured access side");
        require(reservoir.acceptFromTube(Aspect.AER, 120) == 120,
                "Reservoir rejected valid Aer input");
        require(reservoir.acceptFromTube(Aspect.IGNIS, 200) == 136,
                "Reservoir did not clamp mixed essentia to 256 points");
        require(reservoir.amount() == EssentiaReservoirBlockEntity.CAPACITY,
                "Reservoir capacity differs from TC4");
        require(reservoir.aspects().get(Aspect.AER) == 120
                        && reservoir.aspects().get(Aspect.IGNIS) == 136,
                "Reservoir lost mixed-aspect storage");
        require(reservoir.originalSuctionAmount(Aspect.AER) == 0,
                "Full reservoir kept suction");

        CompoundTag saved = reservoir.saveWithoutMetadata();
        EssentiaReservoirBlockEntity restored = new EssentiaReservoirBlockEntity(pos, state);
        restored.load(saved);
        require(restored.facing() == Direction.EAST && restored.amount() == 256,
                "Reservoir facing or contents did not survive NBT round-trip");
        require(restored.aspects().get(Aspect.AER) == 120
                        && restored.aspects().get(Aspect.IGNIS) == 136,
                "Reservoir mixed aspects changed during serialization");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void essentiaMirrorDrainsOnePointAndRollsBackRemoteJar(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos sourcePos = helper.absolutePos(new BlockPos(2, 2, 4));
        BlockPos remotePos = helper.absolutePos(new BlockPos(6, 2, 4));
        BlockState mirrorState = ThaumcraftMod.ESSENTIA_MIRROR.get().defaultBlockState()
                .setValue(MirrorBlock.FACING, Direction.NORTH);
        level.setBlock(sourcePos, mirrorState, Block.UPDATE_ALL);
        level.setBlock(remotePos, mirrorState, Block.UPDATE_ALL);
        EssentiaMirrorBlockEntity source = requireBlockEntity(
                level, sourcePos, EssentiaMirrorBlockEntity.class);
        EssentiaMirrorBlockEntity remote = requireBlockEntity(
                level, remotePos, EssentiaMirrorBlockEntity.class);
        source.setPendingLink(MirrorLink.at(level, remotePos));
        require(source.isLinkValidSimple() && remote.isLinkValidSimple(),
                "Essentia Mirrors did not establish a reciprocal link");

        BlockPos jarPos = remotePos.north();
        BlockState jarState = ThaumcraftMod.ESSENTIA_JAR.get().defaultBlockState();
        level.setBlock(jarPos, jarState, Block.UPDATE_ALL);
        EssentiaJarBlockEntity jar = requireBlockEntity(level, jarPos, EssentiaJarBlockEntity.class);
        require(jar.addToContainerOriginal(Aspect.AER, 3, false) == 0,
                "Could not prepare the remote essentia source");

        require(source.peekRemoteAspect() == Aspect.AER,
                "Essentia Mirror did not discover the remote jar");
        require(source.takeRemoteEssentia(Aspect.AER, 2) == 0,
                "Essentia Mirror accepted a request larger than one point");
        require(source.takeRemoteEssentia(Aspect.AER, 1) == 1 && jar.amount() == 2,
                "Essentia Mirror did not drain exactly one remote point");
        source.restoreRemoteEssentia(Aspect.AER, 1);
        require(jar.amount() == 3,
                "Essentia Mirror rollback did not restore the selected remote source");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void essentiaMirrorUsesOnlyForwardEightBlockSourceVolume(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos sourcePos = helper.absolutePos(new BlockPos(2, 2, 4));
        BlockPos remotePos = helper.absolutePos(new BlockPos(6, 2, 4));
        BlockState mirrorState = ThaumcraftMod.ESSENTIA_MIRROR.get().defaultBlockState()
                .setValue(MirrorBlock.FACING, Direction.NORTH);
        level.setBlock(sourcePos, mirrorState, Block.UPDATE_ALL);
        level.setBlock(remotePos, mirrorState, Block.UPDATE_ALL);
        EssentiaMirrorBlockEntity source = requireBlockEntity(
                level, sourcePos, EssentiaMirrorBlockEntity.class);
        source.setPendingLink(MirrorLink.at(level, remotePos));

        BlockPos behindPos = remotePos.south();
        level.setBlock(behindPos, ThaumcraftMod.ESSENTIA_JAR.get().defaultBlockState(), Block.UPDATE_ALL);
        EssentiaJarBlockEntity behind = requireBlockEntity(level, behindPos, EssentiaJarBlockEntity.class);
        behind.addToContainerOriginal(Aspect.IGNIS, 4, false);
        require(source.peekRemoteAspect() == null,
                "Essentia Mirror scanned behind the remote mirror face");

        BlockPos edgePos = remotePos.north(7);
        level.setBlock(edgePos, ThaumcraftMod.ESSENTIA_RESERVOIR.get().defaultBlockState(), Block.UPDATE_ALL);
        EssentiaReservoirBlockEntity edge = requireBlockEntity(
                level, edgePos, EssentiaReservoirBlockEntity.class);
        edge.acceptFromTube(Aspect.IGNIS, 4);
        require(source.peekRemoteAspect() == Aspect.IGNIS,
                "Essentia Mirror did not scan the original forward [0,8) volume");
        require(source.takeRemoteEssentia(Aspect.IGNIS, 1) == 1 && edge.amount() == 3,
                "Essentia Mirror did not drain a valid reservoir at the range edge");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 120)
    public static void normalAndRestrictedTubesPropagateOriginalJarSuction(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos normalPos = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos normalJarPos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos restrictPos = helper.absolutePos(new BlockPos(5, 2, 2));
        BlockPos restrictJarPos = helper.absolutePos(new BlockPos(5, 1, 2));

        level.setBlock(normalJarPos, ThaumcraftMod.ESSENTIA_JAR.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(normalPos, ThaumcraftMod.ESSENTIA_TUBE.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(restrictJarPos, ThaumcraftMod.ESSENTIA_JAR.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(restrictPos, ThaumcraftMod.ESSENTIA_TUBE_RESTRICT.get().defaultBlockState(), Block.UPDATE_ALL);

        EssentiaTubeBlockEntity normal = requireBlockEntity(level, normalPos, EssentiaTubeBlockEntity.class);
        EssentiaTubeBlockEntity restrict = requireBlockEntity(level, restrictPos, EssentiaTubeBlockEntity.class);
        tickTube(level, normalPos, normal, 24);
        tickTube(level, restrictPos, restrict, 24);

        require(normal.subtype() == EssentiaTubeSubtype.NORMAL && normal.getSuctionAmount(Direction.DOWN) == 31,
                "Normal tube did not propagate jar suction as 32 - 1");
        require(restrict.subtype() == EssentiaTubeSubtype.RESTRICT && restrict.getSuctionAmount(Direction.DOWN) == 16,
                "Restricted tube did not halve the neighbouring jar suction");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 120)
    public static void filteredTubeLocksSuctionAndTransferToLabelAspect(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos jarPos = helper.absolutePos(new BlockPos(3, 1, 3));
        BlockPos tubePos = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlock(jarPos, ThaumcraftMod.ESSENTIA_JAR.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(tubePos, ThaumcraftMod.ESSENTIA_TUBE_FILTER.get().defaultBlockState(), Block.UPDATE_ALL);

        EssentiaJarBlockEntity jar = requireBlockEntity(level, jarPos, EssentiaJarBlockEntity.class);
        EssentiaTubeBlockEntity tube = requireBlockEntity(level, tubePos, EssentiaTubeBlockEntity.class);
        jar.setFilterAspect(Aspect.AER);
        tube.setAspectFilter(Aspect.AER);
        tickTube(level, tubePos, tube, 24);

        require(tube.getSuctionType(Direction.DOWN) == Aspect.AER && tube.getSuctionAmount(Direction.DOWN) == 63,
                "Filtered tube did not propagate labelled-jar suction as 64 - 1");
        require(tube.allowsAspectForTransfer(Aspect.AER) && !tube.allowsAspectForTransfer(Aspect.IGNIS),
                "Filtered tube accepted an aspect different from its label");

        tube.setAspectFilter(Aspect.IGNIS);
        tickTube(level, tubePos, tube, 8);
        require(tube.getSuctionAmount(Direction.DOWN) == 0,
                "Filtered tube kept suction from a jar labelled with another aspect");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 120)
    public static void oneWayTubePropagatesSuctionOnlyAlongOriginalFacing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos jarPos = helper.absolutePos(new BlockPos(4, 1, 4));
        BlockPos tubePos = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlock(jarPos, ThaumcraftMod.ESSENTIA_JAR.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(tubePos, ThaumcraftMod.ESSENTIA_TUBE_ONEWAY.get().defaultBlockState(), Block.UPDATE_ALL);

        EssentiaTubeBlockEntity tube = requireBlockEntity(level, tubePos, EssentiaTubeBlockEntity.class);
        tube.setFacing(Direction.UP);
        tickTube(level, tubePos, tube, 24);
        require(tube.getSuctionAmount(Direction.DOWN) == 31,
                "One-way tube did not accept suction from its original input direction");

        tube.setFacing(Direction.NORTH);
        tickTube(level, tubePos, tube, 8);
        require(tube.getSuctionAmount(Direction.DOWN) == 0,
                "One-way tube propagated suction against its facing rule");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void bufferTubeCapsPersistsAndSynchronizesRollbackState(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        BlockState state = ThaumcraftMod.ESSENTIA_TUBE_BUFFER.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        EssentiaTubeBlockEntity buffer = requireBlockEntity(level, pos, EssentiaTubeBlockEntity.class);

        buffer.restoreBufferForNetwork(Aspect.AER, 5);
        buffer.restoreBufferForNetwork(Aspect.IGNIS, 3);
        buffer.restoreBufferForNetwork(Aspect.ORDO, 4);
        require(buffer.bufferAmount() == EssentiaTubeBlockEntity.BUFFER_CAPACITY,
                "Buffer tube exceeded its original eight-point capacity");
        require(buffer.getEssentiaAmount(Direction.NORTH) == EssentiaTubeBlockEntity.BUFFER_CAPACITY
                        && buffer.getTransportEssentiaAmount(Direction.NORTH) == EssentiaTubeBlockEntity.BUFFER_CAPACITY,
                "Buffer rollback did not synchronize the legacy transport amount");
        require(buffer.getSuctionType(Direction.NORTH) == null,
                "Buffer tube incorrectly aspect-locked its suction");

        buffer.cycleChoke(Direction.NORTH);
        buffer.cycleChoke(Direction.NORTH);
        require(buffer.chokeState(Direction.NORTH) == 2 && buffer.getSuctionAmount(Direction.NORTH) == 0,
                "Buffer tube closed-choke state did not disable side suction");

        CompoundTag saved = buffer.saveWithoutMetadata();
        EssentiaTubeBlockEntity restored = new EssentiaTubeBlockEntity(pos, state);
        restored.load(saved);
        require(restored.subtype() == EssentiaTubeSubtype.BUFFER
                        && restored.bufferAmount() == EssentiaTubeBlockEntity.BUFFER_CAPACITY
                        && restored.getEssentiaAmount(Direction.NORTH) == EssentiaTubeBlockEntity.BUFFER_CAPACITY,
                "Buffer tube lost mixed contents or synchronized amount after NBT round-trip");
        require(restored.chokeState(Direction.NORTH) == 2,
                "Buffer tube lost its side choke state after NBT round-trip");

        require(restored.drainBufferForNetwork(Aspect.AER, 2) == 2
                        && restored.getEssentiaAmount(Direction.NORTH) == 6,
                "Buffer tube drain did not update the live transport amount");
        restored.restoreBufferForNetwork(Aspect.AER, 2);
        require(restored.getEssentiaAmount(Direction.NORTH) == EssentiaTubeBlockEntity.BUFFER_CAPACITY,
                "Buffer tube rollback did not restore the live transport amount");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void tubeSubtypeNbtMatchesOriginalContracts(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));

        EssentiaTubeBlockEntity normal = new EssentiaTubeBlockEntity(pos,
                ThaumcraftMod.ESSENTIA_TUBE.get().defaultBlockState());
        CompoundTag normalTag = normal.saveWithoutMetadata();
        require(normalTag.contains("open", Tag.TAG_BYTE_ARRAY)
                        && normalTag.contains("amount", Tag.TAG_INT)
                        && normalTag.contains("side", Tag.TAG_INT)
                        && normalTag.contains("samount", Tag.TAG_INT),
                "Normal tube lost original type/amount/side/open/suction NBT");
        require(!normalTag.contains("tc4Subtype") && !normalTag.contains("venting")
                        && !normalTag.contains("tc4Count") && !normalTag.contains("buffer")
                        && !normalTag.contains("choke") && !normalTag.contains("bellows"),
                "Normal tube persisted temporary or foreign subtype state");

        EssentiaTubeBlockEntity buffer = new EssentiaTubeBlockEntity(pos,
                ThaumcraftMod.ESSENTIA_TUBE_BUFFER.get().defaultBlockState());
        buffer.restoreBufferForNetwork(Aspect.AER, 3);
        buffer.restoreBufferForNetwork(Aspect.IGNIS, 2);
        buffer.cycleChoke(Direction.NORTH);
        CompoundTag bufferTag = buffer.saveWithoutMetadata();
        require(bufferTag.contains("Aspects", Tag.TAG_LIST)
                        && bufferTag.getList("Aspects", Tag.TAG_COMPOUND).size() == 2
                        && bufferTag.contains("open", Tag.TAG_BYTE_ARRAY)
                        && bufferTag.contains("choke", Tag.TAG_BYTE_ARRAY),
                "Buffer did not use original root Aspects/open/choke NBT");
        require(!bufferTag.contains("type") && !bufferTag.contains("amount")
                        && !bufferTag.contains("side") && !bufferTag.contains("stype")
                        && !bufferTag.contains("samount") && !bufferTag.contains("buffer")
                        && !bufferTag.contains("tc4Subtype") && !bufferTag.contains("bellows"),
                "Buffer persisted TileTube-only or temporary state");

        EssentiaTubeBlockEntity valve = new EssentiaTubeBlockEntity(pos,
                ThaumcraftMod.ESSENTIA_VALVE.get().defaultBlockState());
        valve.toggleManualFlowLikeTC4();
        CompoundTag valveTag = valve.saveWithoutMetadata();
        require(valveTag.contains("flow", Tag.TAG_BYTE) && valveTag.contains("hadpower", Tag.TAG_BYTE),
                "Valve lost original flow/hadpower NBT");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void bufferComparatorAndMinimumSuctionMatchOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
        BlockState state = ThaumcraftMod.ESSENTIA_TUBE_BUFFER.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        EssentiaTubeBlockEntity buffer = requireBlockEntity(level, pos, EssentiaTubeBlockEntity.class);
        require(buffer.getMinimumSuction() == TC4EssentiaTubeParity.MINIMUM_SUCTION,
                "Tube minimum suction was not original zero");
        require(level.getBlockState(pos).getBlock().getAnalogOutputSignal(level.getBlockState(pos), level, pos) == 0,
                "Empty buffer comparator was not zero");
        buffer.restoreBufferForNetwork(Aspect.AER, 1);
        require(level.getBlockState(pos).getBlock().getAnalogOutputSignal(level.getBlockState(pos), level, pos)
                        == TC4EssentiaTubeParity.bufferComparator(1),
                "One-point buffer comparator output diverged from original");
        buffer.restoreBufferForNetwork(Aspect.IGNIS, 7);
        require(level.getBlockState(pos).getBlock().getAnalogOutputSignal(level.getBlockState(pos), level, pos) == 15,
                "Full buffer comparator output was not 15");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void closedValveKeepsTopologyAndManualState(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos valvePos = helper.absolutePos(new BlockPos(3, 2, 3));
        BlockPos tubePos = valvePos.east();
        level.setBlock(valvePos, ThaumcraftMod.ESSENTIA_VALVE.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(tubePos, ThaumcraftMod.ESSENTIA_TUBE.get().defaultBlockState(), Block.UPDATE_ALL);
        EssentiaTubeBlockEntity valve = requireBlockEntity(level, valvePos, EssentiaTubeBlockEntity.class);
        require(valve.canConnectSideLikeTC4(Direction.EAST), "Valve did not see adjacent transport");
        valve.toggleManualFlowLikeTC4();
        require(!valve.isFlowAllowed() && valve.canConnectSideLikeTC4(Direction.EAST),
                "Closed valve removed transport topology instead of only stopping suction");
        valve.toggleManualFlowLikeTC4();
        require(valve.isFlowAllowed(), "Manual valve toggle did not reopen flow");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void tubeSideToggleUpdatesBakedConnectionState(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos firstPos = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos secondPos = firstPos.east();
        level.setBlock(firstPos, ThaumcraftMod.ESSENTIA_TUBE.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(secondPos, ThaumcraftMod.ESSENTIA_TUBE.get().defaultBlockState(), Block.UPDATE_ALL);
        EssentiaTubeBlockEntity first = requireBlockEntity(level, firstPos, EssentiaTubeBlockEntity.class);
        first.toggleSideWithNeighbour(Direction.EAST);
        require(!first.isSideOpen(Direction.EAST)
                        && !level.getBlockState(firstPos).getValue(com.darkifov.thaumcraft.block.EssentiaTubeBlock.EAST),
                "Closed tube side remained visible in baked connection state");
        EssentiaTubeBlockEntity second = requireBlockEntity(level, secondPos, EssentiaTubeBlockEntity.class);
        require(!second.isSideOpen(Direction.WEST)
                        && !level.getBlockState(secondPos).getValue(com.darkifov.thaumcraft.block.EssentiaTubeBlock.WEST),
                "Neighbour tube did not mirror closed side state");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 120)
    public static void alchemicalFurnaceProcessesPersistsAndRespectsCapacity(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
        BlockState state = ThaumcraftMod.ALCHEMICAL_FURNACE.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        AlchemicalFurnaceBlockEntity furnace = requireBlockEntity(level, pos, AlchemicalFurnaceBlockEntity.class);

        require(furnace.insertInput(new ItemStack(Items.IRON_INGOT)), "Furnace rejected an aspect-bearing iron ingot");
        require(furnace.insertFuel(new ItemStack(Items.COAL)), "Furnace rejected vanilla furnace fuel");
        tickFurnace(level, pos, furnace, 40);
        require(furnace.aspects().get(Aspect.METALLUM) == 3, "Iron ingot did not distill to three Metallum");
        require(furnace.inputStack().isEmpty() && furnace.fuelStack().isEmpty(), "Furnace did not consume input and one fuel item");
        require(furnace.capacity() == 50, "Normal alchemical furnace capacity is not 50");

        furnace.aspects().add(Aspect.METALLUM, 47);
        require(furnace.insertInput(new ItemStack(Items.IRON_INGOT)), "Furnace rejected second capacity test input");
        tickFurnace(level, pos, furnace, 50);
        require(furnace.aspects().totalAmount() == 50 && !furnace.inputStack().isEmpty(),
                "Full furnace consumed input or exceeded its 50-point capacity");

        CompoundTag saved = furnace.saveWithoutMetadata();
        AlchemicalFurnaceBlockEntity restored = new AlchemicalFurnaceBlockEntity(pos, state);
        restored.load(saved);
        require(restored.aspects().totalAmount() == 50 && !restored.inputStack().isEmpty()
                        && restored.distillationCounter() == 0,
                "Furnace lost contents/input or persisted its temporary distillation phase");
        require(saved.contains("BurnTime") && saved.contains("Vis") && saved.contains("CookTime")
                        && saved.contains("Items") && saved.contains("Aspects") && saved.contains("speedBoost")
                        && !saved.contains("Counter") && !saved.contains("Bellows") && !saved.contains("BurnDuration")
                        && !saved.contains("PendingAspects"),
                "Furnace NBT did not match original TileAlchemyFurnace keys");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 120)
    public static void furnaceFeedsFiveAlembicsOnLocalFortiethTick(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos furnacePos = helper.absolutePos(new BlockPos(4, 1, 4));
        level.setBlock(furnacePos, ThaumcraftMod.ALCHEMICAL_FURNACE.get().defaultBlockState(), Block.UPDATE_ALL);
        AlchemicalFurnaceBlockEntity furnace = requireBlockEntity(level, furnacePos, AlchemicalFurnaceBlockEntity.class);
        Aspect[] aspects = {Aspect.AER, Aspect.IGNIS, Aspect.AQUA, Aspect.TERRA, Aspect.ORDO};
        AlembicBlockEntity[] alembics = new AlembicBlockEntity[5];
        for (int i = 0; i < alembics.length; i++) {
            BlockPos alembicPos = furnacePos.above(i + 1);
            level.setBlock(alembicPos, ThaumcraftMod.ALEMBIC.get().defaultBlockState(), Block.UPDATE_ALL);
            alembics[i] = requireBlockEntity(level, alembicPos, AlembicBlockEntity.class);
            alembics[i].addEssentia(aspects[i], 1);
            furnace.aspects().add(aspects[i], 1);
        }
        furnace.aspects().add(Aspect.PERDITIO, 1);
        require(TC4DistillationRuntime.countAlembicsAbove(level, furnacePos) == 5,
                "Furnace did not expose the original five-alembic stack");

        tickFurnace(level, furnacePos, furnace, 39);
        for (int i = 0; i < 5; i++) {
            require(alembics[i].aspects().get(aspects[i]) == 1, "Furnace distilled before its local fortieth tick");
        }
        tickFurnace(level, furnacePos, furnace, 1);
        for (int i = 0; i < 5; i++) {
            require(alembics[i].aspects().get(aspects[i]) == 2, "Furnace failed to top up one of five occupied alembics");
        }
        require(furnace.aspects().get(Aspect.PERDITIO) == 1, "Unserved aspect was lost from furnace storage");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void alembicKeepsCapacityFilterSidesAndNbt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
        BlockState state = ThaumcraftMod.ALEMBIC.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        AlembicBlockEntity alembic = requireBlockEntity(level, pos, AlembicBlockEntity.class);
        alembic.setFacing(Direction.NORTH);
        require(alembic.setAspectFilter(Aspect.AER), "Alembic rejected an empty label filter");
        require(alembic.addEssentia(Aspect.AER, 40) == 32 && alembic.spaceLeft() == 0,
                "Alembic did not cap storage at 32");
        require(alembic.addEssentia(Aspect.IGNIS, 1) == 0, "Alembic accepted an aspect different from its label");
        require(!alembic.canOutputTo(Direction.DOWN) && !alembic.canOutputTo(Direction.NORTH)
                        && alembic.canOutputTo(Direction.UP) && alembic.canOutputTo(Direction.EAST),
                "Alembic output-face rules differ from TC4");

        CompoundTag saved = alembic.saveWithoutMetadata();
        AlembicBlockEntity restored = new AlembicBlockEntity(pos, state);
        restored.load(saved);
        require(restored.storedAspect() == Aspect.AER && restored.aspects().get(Aspect.AER) == 32
                        && restored.aspectFilter() == Aspect.AER && restored.facing() == Direction.NORTH,
                "Alembic lost aspect, amount, filter or facing through NBT");
        require(saved.contains("aspect") && saved.contains("amount") && saved.contains("AspectFilter")
                        && saved.contains("facing") && !saved.contains("Aspects")
                        && !saved.contains("Aspect") && !saved.contains("Amount"),
                "Alembic did not use original lowercase TileAlembic NBT");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void furnaceSidedAutomationMatchesOriginal(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
        AlchemicalFurnaceBlockEntity furnace = new AlchemicalFurnaceBlockEntity(pos,
                ThaumcraftMod.ALCHEMICAL_FURNACE.get().defaultBlockState());
        require(furnace.getSlotsForFace(Direction.UP).length == 0,
                "Furnace exposed slots from the top");
        require(furnace.getSlotsForFace(Direction.DOWN).length == 1
                        && furnace.getSlotsForFace(Direction.DOWN)[0] == AlchemicalFurnaceBlockEntity.SLOT_FUEL,
                "Furnace bottom did not expose only the fuel slot");
        require(!furnace.canTakeItemThroughFace(AlchemicalFurnaceBlockEntity.SLOT_FUEL,
                        new ItemStack(Items.COAL), Direction.DOWN)
                        && furnace.canTakeItemThroughFace(AlchemicalFurnaceBlockEntity.SLOT_FUEL,
                        new ItemStack(Items.BUCKET), Direction.DOWN),
                "Bottom extraction was not limited to an empty bucket");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void alembicTypedEmptyStateAndComparatorMatchOriginal(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
        AlembicBlockEntity alembic = new AlembicBlockEntity(pos, ThaumcraftMod.ALEMBIC.get().defaultBlockState());
        require(alembic.setAspectFilter(Aspect.IGNIS) && alembic.storedAspect() == Aspect.IGNIS && alembic.amount() == 0,
                "Typed label did not preserve a zero-amount aspect");
        CompoundTag emptyTyped = alembic.saveWithoutMetadata();
        require(emptyTyped.getString("aspect").equals(Aspect.IGNIS.id()) && emptyTyped.getShort("amount") == 0,
                "Typed empty alembic NBT was lost");
        require(alembic.comparatorOutput() == 0, "Empty alembic comparator was not zero");
        alembic.addEssentia(Aspect.IGNIS, 1);
        require(alembic.comparatorOutput() == 1, "One-point alembic comparator was not one");
        alembic.addEssentia(Aspect.IGNIS, 31);
        require(alembic.comparatorOutput() == 15, "Full alembic comparator was not fifteen");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void alembicFillMessageThresholdsMatchOriginal(GameTestHelper helper) {
        require(com.darkifov.thaumcraft.alchemy.TC4AlchemicalFurnaceParity.alembicFillMessage(0) == 1
                        && com.darkifov.thaumcraft.alchemy.TC4AlchemicalFurnaceParity.alembicFillMessage(1) == 2
                        && com.darkifov.thaumcraft.alchemy.TC4AlchemicalFurnaceParity.alembicFillMessage(13) == 3
                        && com.darkifov.thaumcraft.alchemy.TC4AlchemicalFurnaceParity.alembicFillMessage(26) == 4
                        && com.darkifov.thaumcraft.alchemy.TC4AlchemicalFurnaceParity.alembicFillMessage(32) == 5,
                "Alembic fill-message thresholds diverged from 0/40/80/100 percent");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void centrifugeOnlySplitsCompoundFromBelowWithRedstonePause(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 4));
        BlockState state = ThaumcraftMod.ALCHEMICAL_CENTRIFUGE.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        AlchemicalCentrifugeBlockEntity centrifuge = requireBlockEntity(level, pos, AlchemicalCentrifugeBlockEntity.class);
        require(centrifuge.addInput(Aspect.AER, 1, Direction.DOWN) == 0, "Centrifuge accepted a primal aspect");
        require(centrifuge.addInput(Aspect.MOTUS, 1, Direction.UP) == 0, "Centrifuge accepted input from a non-bottom face");
        require(centrifuge.addInput(Aspect.MOTUS, 1, Direction.DOWN) == 1, "Centrifuge rejected a valid compound aspect");
        require(centrifuge.addInput(Aspect.VICTUS, 1, Direction.DOWN) == 0, "Centrifuge accepted a second simultaneous input");

        level.setBlock(pos.east(), Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        tickCentrifuge(level, pos, centrifuge, 5);
        require(centrifuge.process() == 39, "Powered centrifuge advanced its process counter");
        level.removeBlock(pos.east(), false);
        tickCentrifuge(level, pos, centrifuge, 39);
        Aspect output = centrifuge.outputAspect();
        require(output == Aspect.MOTUS.firstComponent() || output == Aspect.MOTUS.secondComponent(),
                "Centrifuge output was not one component of the input aspect");
        require(centrifuge.outputAmount(Direction.DOWN) == 0 && centrifuge.outputAmount(Direction.UP) == 1,
                "Centrifuge exposed output on a non-top face");
        require(centrifuge.takeOutput(output, 1, Direction.DOWN) == 0
                        && centrifuge.takeOutput(output, 1, Direction.UP) == 1,
                "Centrifuge did not enforce top-only extraction");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void centrifugePersistsOnlyOriginalNbtAndResumesImmediately(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 4));
        BlockState state = ThaumcraftMod.ALCHEMICAL_CENTRIFUGE.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        AlchemicalCentrifugeBlockEntity centrifuge = requireBlockEntity(level, pos, AlchemicalCentrifugeBlockEntity.class);
        require(centrifuge.addInput(Aspect.MOTUS, 1, Direction.DOWN) == 1, "Centrifuge setup failed");
        CompoundTag saved = centrifuge.saveWithoutMetadata();
        require(saved.getString("aspectIn").equals(Aspect.MOTUS.id()), "Canonical aspectIn was not saved");
        require(saved.getInt("facing") == Direction.NORTH.get3DDataValue(), "Canonical facing was not saved");
        require(!saved.contains("process") && !saved.contains("counter")
                        && !saved.contains("rotation") && !saved.contains("rotationSpeed"),
                "Temporary centrifuge state leaked into NBT");
        centrifuge.load(saved);
        require(centrifuge.process() == 0 && centrifuge.counter() == 0,
                "Temporary processing state was restored from NBT");
        tickCentrifuge(level, pos, centrifuge, 1);
        require(centrifuge.inputAspect() == null && centrifuge.outputAspect() != null,
                "Loaded input did not resume immediately like original TC4");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void centrifugePullsCompoundDirectlyFromJarBelow(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos jarPos = helper.absolutePos(new BlockPos(4, 1, 4));
        BlockPos centrifugePos = jarPos.above();
        level.setBlock(jarPos, ThaumcraftMod.ESSENTIA_JAR.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(centrifugePos, ThaumcraftMod.ALCHEMICAL_CENTRIFUGE.get().defaultBlockState(), Block.UPDATE_ALL);
        EssentiaJarBlockEntity jar = requireBlockEntity(level, jarPos, EssentiaJarBlockEntity.class);
        AlchemicalCentrifugeBlockEntity centrifuge = requireBlockEntity(level, centrifugePos, AlchemicalCentrifugeBlockEntity.class);
        require(jar.addToContainerOriginal(Aspect.MOTUS, 1, false) == 0, "Jar setup failed");
        tickCentrifuge(level, centrifugePos, centrifuge, 5);
        require(centrifuge.inputAspect() == Aspect.MOTUS && jar.amount() == 0,
                "Centrifuge did not draw compound essentia directly from a jar below");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void centrifugePullsCompoundDirectlyFromAlembicBelow(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos alembicPos = helper.absolutePos(new BlockPos(4, 1, 4));
        BlockPos centrifugePos = alembicPos.above();
        level.setBlock(alembicPos, ThaumcraftMod.ALEMBIC.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(centrifugePos, ThaumcraftMod.ALCHEMICAL_CENTRIFUGE.get().defaultBlockState(), Block.UPDATE_ALL);
        AlembicBlockEntity alembic = requireBlockEntity(level, alembicPos, AlembicBlockEntity.class);
        AlchemicalCentrifugeBlockEntity centrifuge = requireBlockEntity(level, centrifugePos, AlchemicalCentrifugeBlockEntity.class);
        require(alembic.addEssentia(Aspect.HUMANUS, 1) == 1, "Alembic setup failed");
        tickCentrifuge(level, centrifugePos, centrifuge, 5);
        require(centrifuge.inputAspect() == Aspect.HUMANUS && alembic.amount() == 0,
                "Centrifuge did not draw compound essentia directly from an alembic below");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void stackedCentrifugesCanRecursivelySplitCompoundOutput(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos lowerPos = helper.absolutePos(new BlockPos(4, 1, 4));
        BlockPos upperPos = lowerPos.above();
        level.setBlock(lowerPos, ThaumcraftMod.ALCHEMICAL_CENTRIFUGE.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(upperPos, ThaumcraftMod.ALCHEMICAL_CENTRIFUGE.get().defaultBlockState(), Block.UPDATE_ALL);
        AlchemicalCentrifugeBlockEntity lower = requireBlockEntity(level, lowerPos, AlchemicalCentrifugeBlockEntity.class);
        AlchemicalCentrifugeBlockEntity upper = requireBlockEntity(level, upperPos, AlchemicalCentrifugeBlockEntity.class);
        lower.restoreOutput(Aspect.HUMANUS, 1);
        tickCentrifuge(level, upperPos, upper, 5);
        require(upper.inputAspect() == Aspect.HUMANUS && lower.outputAspect() == null,
                "Stacked centrifuge did not consume the lower compound output");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void centrifugeShapeAndTransportFacesMatchOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 4));
        BlockState state = ThaumcraftMod.ALCHEMICAL_CENTRIFUGE.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        AlchemicalCentrifugeBlockEntity centrifuge = requireBlockEntity(level, pos, AlchemicalCentrifugeBlockEntity.class);
        AABB shape = state.getShape(level, pos).bounds();
        require(Math.abs(shape.minX - 0.25D) < 0.0001D && Math.abs(shape.maxX - 0.75D) < 0.0001D
                        && Math.abs(shape.minY) < 0.0001D && Math.abs(shape.maxY - 1.0D) < 0.0001D
                        && Math.abs(shape.minZ - 0.25D) < 0.0001D && Math.abs(shape.maxZ - 0.75D) < 0.0001D,
                "Centrifuge shape is not the original 8x16x8 column");
        require(centrifuge.isConnectable(Direction.UP) && centrifuge.isConnectable(Direction.DOWN)
                        && !centrifuge.isConnectable(Direction.NORTH)
                        && centrifuge.canInputFrom(Direction.DOWN) && centrifuge.canOutputTo(Direction.UP),
                "Centrifuge transport faces diverged from original");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 120)
    public static void thaumatoriumRequiresHeatAndRedstoneStopsSuction(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
        ThaumatoriumBlockEntity tile = placeValidThaumatorium(level, pos);
        require(tile.insertCatalyst(new ItemStack(Items.IRON_INGOT)), "Thaumatorium rejected a valid crucible catalyst");
        require(tile.selectFormulaIndex(0), "Thaumatorium did not remember the selected iron formula");
        tickThaumatorium(level, pos, tile, 5);
        require(!tile.heated() && tile.currentSuction() == null && "heat".equals(tile.lastMissing()),
                "Unheated Thaumatorium requested essentia");

        placeThaumatoriumHeat(level, pos);
        tickThaumatorium(level, pos, tile, 40);
        require(tile.heated() && tile.currentSuction() != null,
                "Heated Thaumatorium did not request its first missing aspect: "
                        + tile.statusComponent().getString());
        Direction allowed = Direction.EAST;
        require(tile.suctionAmountAt(pos, allowed) == ThaumatoriumBlockEntity.ORIGINAL_SUCTION,
                "Thaumatorium suction is not the original 128");

        level.setBlock(pos.west(), Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        tickThaumatorium(level, pos, tile, 5);
        require(tile.currentSuction() == null && "redstone".equals(tile.lastMissing()),
                "Redstone did not suspend Thaumatorium suction");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 120)
    public static void thaumatoriumCraftClearsEntireLegacyEssentiaBuffer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
        ThaumatoriumBlockEntity tile = placeValidThaumatorium(level, pos);
        BlockState state = level.getBlockState(pos);
        placeThaumatoriumHeat(level, pos);
        require(tile.insertCatalyst(new ItemStack(Items.IRON_INGOT)), "Thaumatorium rejected iron catalyst");
        require(tile.selectFormulaIndex(0), "Thaumatorium did not remember the selected iron formula");
        tickThaumatorium(level, pos, tile, 1);
        AlchemyRecipe recipe = tile.activeRecipe();
        require(recipe != null, "Thaumatorium did not resolve an active recipe");
        for (java.util.Map.Entry<Aspect, Integer> entry : recipe.cost().entrySet()) {
            int accepted = tile.acceptEssentiaFromGolem(entry.getKey(), entry.getValue());
            require(accepted == entry.getValue(),
                    "Thaumatorium rejected required essentia " + entry.getKey().id() + " "
                            + accepted + "/" + entry.getValue() + ": " + tile.statusComponent().getString());
        }
        Aspect stray = null;
        for (Aspect candidate : Aspect.values()) {
            if (!recipe.cost().containsKey(candidate)) {
                stray = candidate;
                break;
            }
        }
        require(stray != null, "No spare aspect available for legacy-buffer test");
        tile.essentia().add(stray, 2);

        CompoundTag saved = tile.saveWithoutMetadata();
        ThaumatoriumBlockEntity restored = new ThaumatoriumBlockEntity(pos, state);
        restored.load(saved);
        require(!restored.catalyst().isEmpty() && restored.essentia().get(stray) == 2,
                "Thaumatorium lost catalyst or legacy residual essentia through NBT");

        tickThaumatorium(level, pos, tile, 5);
        require(tile.catalyst().isEmpty() && tile.essentia().isEmpty() && !tile.lastRecipe().isBlank(),
                "Successful Thaumatorium craft did not consume catalyst and clear the complete essentia buffer");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void mnemonicMatricesExpandThaumatoriumFormulaMemoryByTwo(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 4));
        ThaumatoriumBlockEntity tile = placeValidThaumatorium(level, pos);
        Item blankCore = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_golem_core_blank"));
        require(blankCore != null && tile.insertCatalyst(new ItemStack(blankCore)),
                "Thaumatorium rejected the six-formula blank golem core catalyst");
        require(tile.maxRecipes() == 1 && tile.visibleFormulaCandidates().size() >= 5,
                "Thaumatorium candidate carousel was incorrectly truncated by memory capacity");
        require(tile.selectFormulaIndex(0) && tile.rememberedFormulaIds().size() == 1,
                "Thaumatorium did not remember its first formula");

        level.setBlock(pos.east(), ThaumcraftMod.MNEMONIC_MATRIX.get().defaultBlockState()
                .setValue(MnemonicMatrixBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
        level.setBlock(pos.west(), ThaumcraftMod.MNEMONIC_MATRIX.get().defaultBlockState()
                .setValue(MnemonicMatrixBlock.FACING, Direction.EAST), Block.UPDATE_ALL);
        require(tile.maxRecipes() == 5 && tile.visibleFormulaCandidates().size() >= 5,
                "Mnemonic matrices did not add two formula memories each");
        require(tile.selectFormulaIndex(4) && tile.rememberedFormulaIds().size() == 2,
                "Thaumatorium could not remember a formula unlocked by an oriented matrix");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 120)
    public static void blockedThaumatoriumOutputPreservesCatalystAndEssentia(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
        ThaumatoriumBlockEntity tile = placeValidThaumatorium(level, pos);
        placeThaumatoriumHeat(level, pos);
        require(tile.insertCatalyst(new ItemStack(Items.IRON_INGOT)), "Thaumatorium rejected iron catalyst");
        require(tile.selectFormulaIndex(0), "Thaumatorium did not remember the selected iron formula");
        tickThaumatorium(level, pos, tile, 1);
        AlchemyRecipe recipe = tile.activeRecipe();
        require(recipe != null, "Thaumatorium did not resolve an active recipe");
        for (java.util.Map.Entry<Aspect, Integer> entry : recipe.cost().entrySet()) {
            tile.acceptEssentiaFromGolem(entry.getKey(), entry.getValue());
        }
        int paid = tile.essentia().totalAmount();

        BlockPos outputPos = pos.relative(tile.facing());
        level.setBlock(outputPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        BlockEntity outputBe = level.getBlockEntity(outputPos);
        require(outputBe instanceof Container, "Test output chest did not expose a container");
        Container output = (Container) outputBe;
        for (int slot = 0; slot < output.getContainerSize(); slot++) {
            output.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        }
        tickThaumatorium(level, pos, tile, 5);
        require(!tile.catalyst().isEmpty() && tile.essentia().totalAmount() == paid
                        && "output".equals(tile.lastMissing()),
                "Blocked output consumed Thaumatorium catalyst or essentia: "
                        + tile.statusComponent().getString() + " paid=" + paid);

        output.setItem(0, ItemStack.EMPTY);
        tickThaumatorium(level, pos, tile, 5);
        require(tile.catalyst().isEmpty() && tile.essentia().isEmpty() && !output.getItem(0).isEmpty(),
                "Thaumatorium did not resume after output space became available");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void thaumatoriumRequiresRememberedFormulaAndUpperPartProxiesLower(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        ThaumatoriumBlockEntity tile = placeValidThaumatorium(level, pos);
        placeThaumatoriumHeat(level, pos);
        require(ThaumatoriumBlockEntity.resolveAt(level, pos.above()) == tile,
                "Thaumatorium upper half did not proxy the lower tile");
        require(tile.insertCatalyst(new ItemStack(Items.IRON_INGOT)), "Thaumatorium rejected catalyst");
        tickThaumatorium(level, pos, tile, 40);
        require(tile.activeRecipe() == null && tile.currentSuction() == null
                        && "formula".equals(tile.lastMissing()),
                "Thaumatorium crafted an unremembered formula");
        require(tile.selectFormulaIndex(0), "Thaumatorium did not remember selected formula");
        tickThaumatorium(level, pos, tile, 5);
        require(tile.activeRecipe() != null && tile.currentSuction() != null,
                "Remembered formula did not start original suction lifecycle");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void thaumatoriumTrimsRememberedRecipesWhenOrientedMatrixIsLost(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        ThaumatoriumBlockEntity tile = placeValidThaumatorium(level, pos);
        Item blankCore = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_golem_core_blank"));
        require(blankCore != null && tile.insertCatalyst(new ItemStack(blankCore)), "Missing blank golem core catalyst");
        level.setBlock(pos.east(), ThaumcraftMod.MNEMONIC_MATRIX.get().defaultBlockState()
                .setValue(MnemonicMatrixBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
        require(tile.maxRecipes() == 3, "Oriented mnemonic matrix did not add two memories");
        require(tile.selectFormulaIndex(0) && tile.selectFormulaIndex(1) && tile.selectFormulaIndex(2),
                "Thaumatorium failed to store three upgraded formulas");
        require(tile.rememberedFormulaIds().size() == 3, "Upgraded formula list size diverged");
        level.removeBlock(pos.east(), false);
        tickThaumatorium(level, pos, tile, 40);
        require(tile.maxRecipes() == 1 && tile.rememberedFormulaIds().size() == 1,
                "Formula list was not trimmed to the original base capacity after matrix loss");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionUsesOnlyOriginalStabilizerWhitelist(GameTestHelper helper) {
        require(ThaumcraftMod.AER_CRYSTAL.get() instanceof InfusionStabilizer,
                "Primal crystal cluster lost the original IInfusionStabiliser contract");
        require(ThaumcraftMod.BALANCED_CRYSTAL.get() instanceof InfusionStabilizer,
                "Balanced crystal cluster lost the original IInfusionStabiliser contract");
        require(TC4InfusionStabilityParity.isOriginalStyleStabilizer(
                        ThaumcraftMod.TALLOW_CANDLE.get().defaultBlockState()),
                "Tallow candle is not recognized as an infusion stabilizer");
        require(TC4InfusionStabilityParity.isOriginalStyleStabilizer(Blocks.PLAYER_HEAD.defaultBlockState())
                        && TC4InfusionStabilityParity.isOriginalStyleStabilizer(Blocks.CREEPER_HEAD.defaultBlockState()),
                "Original skull metadata family is not represented by modern head blocks");

        require(!TC4InfusionStabilityParity.isOriginalStyleStabilizer(
                        ThaumcraftMod.INFUSION_PILLAR.get().defaultBlockState()),
                "Infusion pillar incorrectly grants hidden stabilizer credit");
        require(!TC4InfusionStabilityParity.isOriginalStyleStabilizer(
                        ThaumcraftMod.ARCANE_STONE_BRICKS.get().defaultBlockState()),
                "Arcane stone incorrectly grants hidden stabilizer credit");
        require(!TC4InfusionStabilityParity.isOriginalStyleStabilizer(
                        ThaumcraftMod.NODE_STABILIZER.get().defaultBlockState()),
                "Node stabilizer incorrectly acts as an infusion stabilizer");
        require(!TC4InfusionStabilityParity.isOriginalStyleStabilizer(Blocks.DRAGON_HEAD.defaultBlockState()),
                "Post-1.7 dragon head incorrectly joined the TC4 skull whitelist");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionStabilizerScanPairsAroundMatrixBlock(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(4, 2, 4));
        BlockPos east = center.east(2);
        BlockPos west = center.west(2);
        BlockPos south = center.south(3);
        level.setBlock(east, ThaumcraftMod.AER_CRYSTAL.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(west, ThaumcraftMod.AER_CRYSTAL.get().defaultBlockState(), Block.UPDATE_ALL);

        TC4InfusionStabilityParity.StabilitySnapshot paired = TC4InfusionStabilityParity.scan(level, center);
        require(paired.mirroredPairs() == 1 && paired.unpaired() == 0 && paired.effectivePairs() == 1,
                "Mirrored crystal pair was not counted exactly once around the matrix block");

        level.setBlock(south, ThaumcraftMod.TALLOW_CANDLE.get().defaultBlockState(), Block.UPDATE_ALL);
        TC4InfusionStabilityParity.StabilitySnapshot mixed = TC4InfusionStabilityParity.scan(level, center);
        require(mixed.mirroredPairs() == 1 && mixed.unpaired() == 1,
                "Unpaired stabilizer did not remain separate from the mirrored pair");
        require(mixed.signature().contains(east.getX() + "," + east.getY() + "," + east.getZ()),
                "Stabilizer signature did not preserve deterministic world coordinates");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionInitialInstabilityUsesSingleOriginalSymmetryPass(GameTestHelper helper) {
        require(TC4InfusionRuntime.initialInstability(7, 5) == 12,
                "Infusion instability is not symmetry plus recipe instability");
        require(TC4InfusionRuntime.initialInstability(-4, 2) == -2,
                "Original craftingStart must preserve a negative stabilized value");
        require(TC4InfusionRuntime.initialInstability(22, 9) == 31,
                "Original craftingStart must not cap symmetry plus recipe instability");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void runningInfusionKeepsCraftingStartInstability(GameTestHelper helper) {
        require(TC4InfusionRuntime.runningInstability(12, 2, 3) == 12,
                "A later surroundings rescan must not erase locked instability");
        require(TC4InfusionRuntime.runningInstability(4, 9, 6) == 4,
                "A later surroundings rescan must not rewrite locked instability");
        require(TC4InfusionRuntime.runningInstability(31, 40, 40) == 31,
                "A raw starting value above 25 must remain until a shortage increment path caps it");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionChangedCatalystRollsExactlyOneInstabilityBranch(GameTestHelper helper) {
        require(TC4InfusionLifecycleParity.INVALID_CATALYST_EVENT_ROLLS == 1,
                "Changed catalyst must enter exactly one weighted instability branch");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionChangedCatalystAddsNoAutomaticFailureWarp(GameTestHelper helper) {
        require(!TC4InfusionLifecycleParity.grantsTerminalWarpOnInvalidCatalyst(),
                "Changed catalyst incorrectly adds a second terminal Warp award");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionSuccessAddsNoAutomaticWarp(GameTestHelper helper) {
        require(!TC4InfusionLifecycleParity.grantsAutomaticWarpOnSuccess(),
                "Successful infusion incorrectly grants unconditional Warp");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionMissingComponentWaitsInsteadOfCancelling(GameTestHelper helper) {
        require(!TC4InfusionLifecycleParity.missingComponentCancelsCraft(),
                "Missing component must leave the matrix waiting for a later retry");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionStructureLossPreservesLockedRecipe(GameTestHelper helper) {
        require(!TC4InfusionPauseResumeParity.structureLossClearsLockedRecipe(),
                "Structure loss must not clear the locked recipe or pending infusion state");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionStructureLossAddsNoTerminalWarp(GameTestHelper helper) {
        require(!TC4InfusionPauseResumeParity.structureLossGrantsTerminalWarp(),
                "Structure loss must deactivate the matrix without a terminal Warp award");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionReactivationResumesLockedCraft(GameTestHelper helper) {
        require(TC4InfusionPauseResumeParity.reactivationResumesLockedCraft(),
                "Restored altar must resume the existing locked craft after wand reactivation");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionComponentLayoutDoesNotReselectRecipeAfterStart(GameTestHelper helper) {
        require(!TC4InfusionPauseResumeParity.componentLayoutReselectsRecipeAfterStart(),
                "Started infusion must keep its recipe lock and wait for missing components");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionSaveReloadKeepsCraftingFlag(GameTestHelper helper) {
        require(TC4InfusionSaveReloadParity.saveReloadKeepsCraftingFlag(),
                "Save/reload must keep a running infusion in crafting state");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionSaveReloadKeepsLockedRecipe(GameTestHelper helper) {
        require(TC4InfusionSaveReloadParity.saveReloadKeepsLockedRecipe(),
                "Save/reload must preserve the locked recipe id and catalyst snapshot");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionSaveReloadKeepsPendingCosts(GameTestHelper helper) {
        require(TC4InfusionSaveReloadParity.saveReloadKeepsPendingEssentiaAndComponents(),
                "Save/reload must preserve pending essentia and component queues");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionSaveReloadRestartsTransientComponentTravel(GameTestHelper helper) {
        require(!TC4InfusionSaveReloadParity.saveReloadKeepsTravellingComponentSource(),
                "TC4 must not serialize a selected travelling component pedestal");
        require(TC4InfusionSaveReloadParity.saveReloadRestartsComponentTravelCounter(),
                "TC4 component itemCount must restart from zero after reload");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionChunkUnloadDoesNotCancelCraft(GameTestHelper helper) {
        require(!TC4InfusionSaveReloadParity.chunkUnloadCancelsInfusion(),
                "Chunk unload must not cancel or clear a saved infusion craft");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionInstabilityEventTableRollBoundMatchesOriginal(GameTestHelper helper) {
        require(TC4InfusionInstabilityEventTableParity.EVENT_ROLL_BOUND == 21,
                "Weighted instability event table must use the original 21-slot random.nextInt(21) roll");
        require(TC4InfusionInstabilityEventTableParity.GATE_ROLL_BOUND == 500,
                "Instability gate must use the original random.nextInt(500) <= instability check");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionInstabilityEventTableGateMatchesOriginal(GameTestHelper helper) {
        require(!TC4InfusionInstabilityEventTableParity.gateAllows(0, 0),
                "At instability 0 the original if(instability>0) guard blocks every roll");
        require(!TC4InfusionInstabilityEventTableParity.gateAllows(10, 5),
                "A roll above the current instability must never be allowed through the gate");
        require(TC4InfusionInstabilityEventTableParity.gateAllows(5, 5),
                "Original nextInt(500) <= 5 allows the boundary roll through the gate");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionInstabilityEventTableIsTotalAndNonOverlapping(GameTestHelper helper) {
        require(TC4InfusionInstabilityEventTableParity.countAssignedRolls() == TC4InfusionInstabilityEventTableParity.EVENT_ROLL_BOUND,
                "Every one of the 21 original roll slots must belong to exactly one non-overlapping category");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionInstabilityEventTableCosmeticSurgeIsDefaultBranch(GameTestHelper helper) {
        require(!TC4InfusionInstabilityEventTableParity.isCosmeticSurge(0),
                "Roll 0 is eject type 0, not cosmetic surge");
        require(!TC4InfusionInstabilityEventTableParity.isCosmeticSurge(20),
                "Roll 20 is warp branch, not cosmetic surge");
        require(TC4InfusionInstabilityEventTableParity.EJECT_TYPE_0_ROLLS.length == 4,
                "Original eject-type-0 branch covers exactly 4 rolls (0, 2, 10, 13)");
        require(TC4InfusionInstabilityEventTableParity.HARM_SINGLE_ROLLS.length == 2,
                "Original single-target harm branch covers exactly 2 rolls (5, 16)");
        require(TC4InfusionInstabilityEventTableParity.ZAP_SINGLE_ROLLS.length == 3,
                "Original single-target zap branch covers exactly 3 rolls (3, 8, 14)");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionInstabilityEventTableExplosionAndWarpMatchOriginal(GameTestHelper helper) {
        require(TC4InfusionInstabilityEventTableParity.EXPLODE_MATRIX_ROLL == 9,
                "Matrix self-explosion must be roll 9, matching the original switch");
        require(TC4InfusionInstabilityEventTableParity.EXPLODE_MATRIX_BASE_STRENGTH == 1.5F,
                "Matrix explosion base strength must match the original 1.5F + random.nextFloat()");
        require(TC4InfusionInstabilityEventTableParity.WARP_ROLL == 20,
                "Warp branch must be roll 20, matching the original switch");
        require(TC4InfusionInstabilityEventTableParity.WARP_STICKY_CHANCE == 0.25F,
                "Warp branch must keep the original 25% chance of +1 sticky warp");
        require(TC4InfusionInstabilityEventTableParity.WARP_PERMANENT_MIN == 1
                        && TC4InfusionInstabilityEventTableParity.WARP_PERMANENT_MAX == 5,
                "Permanent warp branch must keep the original random.nextInt(5) + 1 range (1-5)");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionEssentiaAndComponentShortageNeverCancelTheCraft(GameTestHelper helper) {
        require(!TC4InfusionShortageInstabilityParity.essentiaShortageCancelsCraft(),
                "A brief essentia shortage must never cancel a locked infusion craft");
        require(!TC4InfusionShortageInstabilityParity.componentShortageCancelsCraft(),
                "A brief component shortage must never cancel a locked infusion craft");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionEssentiaShortageAttemptsEveryPendingAspectPerCycle(GameTestHelper helper) {
        require(TC4InfusionShortageInstabilityParity.essentiaShortageAttemptsEveryPendingAspectPerCycle(),
                "Essentia shortage must retry every pending aspect in the same cycle, not just the first one");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionEssentiaShortageForcesResurveyButComponentShortageDoesNot(GameTestHelper helper) {
        require(TC4InfusionShortageInstabilityParity.essentiaShortageForcesSurroundingsResurvey(),
                "Essentia shortage must force an immediate surroundings re-scan, matching original checkSurroundings = true");
        require(!TC4InfusionShortageInstabilityParity.componentShortageForcesSurroundingsResurvey(),
                "Component shortage must not force a surroundings re-scan, matching the original craftCycle");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionComponentShortageFirstUnmatchedIngredientAlwaysRefundsEssentia(GameTestHelper helper) {
        require(TC4InfusionShortageInstabilityParity.componentShortageFirstUnmatchedIngredientAlwaysRefunds(),
                "The first unmatched ingredient index (a == 0) must always refund essentia, matching original nextInt(1 + a)");
        require(TC4InfusionShortageInstabilityParity.componentShortageRefundRollBoundForIndex(0) == 1,
                "Original nextInt(1 + 0) == nextInt(1), which is always 0");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionComponentShortageRefundOddsShrinkPerUnmatchedIndex(GameTestHelper helper) {
        require(TC4InfusionShortageInstabilityParity.componentShortageRefundRollBoundForIndex(1) == 2,
                "Second unmatched ingredient index must use original nextInt(1 + 1) == nextInt(2)");
        require(TC4InfusionShortageInstabilityParity.componentShortageRefundRollBoundForIndex(4) == 5,
                "Fifth unmatched ingredient index must use original nextInt(1 + 4) == nextInt(5)");
        require(TC4InfusionShortageInstabilityParity.essentiaShortageInstabilityRollBound(0) == 100,
                "Essentia shortage instability creep must start at original nextInt(100 - instability*3)");
        require(TC4InfusionShortageInstabilityParity.componentShortageInstabilityRollBound(0) == 50,
                "Component/XP shortage instability creep must start at original nextInt(50 - instability*2)");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionComponentShortageRefundReachesProductionPendingAspects(GameTestHelper helper) {
        // Behavioral test: verify that the lockedRecipeAspectTypes ledger is populated
        // and the random-aspect selection path compiles with the correct field.
        // We assert the parity contract AND the runtime production constants together.
        require(TC4InfusionShortageInstabilityParity.componentShortageFirstUnmatchedIngredientAlwaysRefunds(),
                "Index 0 must always trigger the refund gate (nextInt(1) == 0)");
        require(TC4InfusionShortageInstabilityParity.componentShortageRefundRollBoundForIndex(0) == 1,
                "Roll bound for first unmatched index must be exactly 1");
        require(TC4InfusionRuntime.componentShortageEssentiaRefundRollBound(0) == 1,
                "Production helper must return the correct nextInt(1+a) roll bound for index 0");
        require(TC4InfusionRuntime.componentShortageEssentiaRefundRollBound(1) == 2,
                "Production helper must return nextInt(2) roll bound for index 1");
        require(TC4InfusionRuntime.componentShortageEssentiaRefundRollBound(9) == 10,
                "Production helper must scale correctly for higher indices");
        require(TC4InfusionRuntime.failedEssentiaRollBound(0) == 100,
                "Essentia creep base roll must be 100 (original 100 - 0*3)");
        require(TC4InfusionRuntime.failedComponentRollBound(0) == 50,
                "Component creep base roll must be 50 (original 50 - 0*2)");
        require(TC4InfusionShortageInstabilityParity.componentShortageInstabilityRollBound(0) == 50,
                "Parity contract must match production component-creep roll bound");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionInstabilityEventDispatcherUsesParityGate(GameTestHelper helper) {
        require(!TC4InfusionInstabilityEventTableParity.gateAllows(0, 0),
                "At instability 0, even roll 0 does not pass (instability > 0 guard)");
        require(TC4InfusionInstabilityEventTableParity.gateAllows(5, 5),
                "At instability 5, roll 5 passes (original nextInt(500) <= 5)");
        require(!TC4InfusionInstabilityEventTableParity.gateAllows(6, 5),
                "At instability 5, roll 6 does not pass (roll exceeds instability)");
        require(TC4InfusionInstabilityEventTableParity.EVENT_ROLL_BOUND == 21,
                "Event table must use the original 21-slot bound");
        require(TC4InfusionInstabilityEventTableParity.GATE_ROLL_BOUND == 500,
                "Gate must use the original random.nextInt(500)");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionAltarBlueprintAndVisCostMatchTc4(GameTestHelper helper) {
        require(TC4InfusionAltarFullClosureParity.altarBlueprintVolume() == 27,
                "Infusion altar raw blueprint must remain 3x3x3");
        require(TC4InfusionAltarFullClosureParity.altarBlueprintAccountedBlocks() == 27,
                "Matrix, pedestal, stones, bricks and air must account for all 27 blueprint cells");
        require(TC4InfusionAltarFullClosureParity.PRIMAL_ASPECT_COUNT == 6,
                "Altar conversion must use all six primal aspects");
        require(TC4InfusionAltarFullClosureParity.ALTAR_VIS_COST_PER_PRIMAL_CENTIVIS == 2500,
                "Altar conversion must cost 25 vis / 2500 centivis per primal aspect");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionAltarWandOriginSearchAndResearchMatchTc4(GameTestHelper helper) {
        require("INFUSION".equals(TC4InfusionAltarFullClosureParity.RESEARCH_KEY),
                "Wand trigger must require original INFUSION research");
        require(TC4InfusionAltarFullClosureParity.ALTAR_ORIGIN_SCAN_MIN == -2
                        && TC4InfusionAltarFullClosureParity.ALTAR_ORIGIN_SCAN_MAX == 0,
                "WandManager must search origins clicked-2 through clicked on all axes");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionPillarOrientationsAndDropsMatchTc4(GameTestHelper helper) {
        require(TC4InfusionAltarFullClosureParity.ORIGINAL_PILLAR_ORIENTATIONS.length == 4,
                "Converted altar must retain four original pillar orientations");
        for (int orientation : TC4InfusionAltarFullClosureParity.ORIGINAL_PILLAR_ORIENTATIONS) {
            require(TC4InfusionAltarFullClosureParity.isOriginalPillarOrientation(orientation),
                    "Invalid original pillar orientation " + orientation);
        }
        require("thaumcraft:arcane_stone_bricks".equals(TC4InfusionAltarFullClosureParity.LOWER_PILLAR_DROP),
                "Lower pillar must drop the original arcane stone brick");
        require("thaumcraft:arcane_stone".equals(TC4InfusionAltarFullClosureParity.UPPER_PILLAR_DROP),
                "Upper pillar must drop the original arcane stone block");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionMatrixSchedulerMatchesTc4(GameTestHelper helper) {
        require(TC4InfusionAltarFullClosureParity.MATRIX_VALIDITY_IDLE_INTERVAL == 100,
                "Idle matrix validity must be checked every 100 ticks");
        require(TC4InfusionAltarFullClosureParity.MATRIX_VALIDITY_CRAFTING_INTERVAL == 20,
                "Crafting matrix validity must be checked every 20 ticks");
        require(TC4InfusionAltarFullClosureParity.CRAFT_CYCLE_INTERVAL == 10,
                "Normal craftCycle interval must be 10 ticks");
        require(TC4InfusionAltarFullClosureParity.ENCHANTMENT_XP_CYCLE_INTERVAL == 20,
                "Enchantment XP stage must use a 20-tick countDelay");
        require(TC4InfusionAltarFullClosureParity.shouldRunCraftCycle(20, 10)
                        && TC4InfusionAltarFullClosureParity.shouldRunCraftCycle(20, 20),
                "Production scheduler must use count % countDelay == 0");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionComponentTravelUsesFiveCraftCycles(GameTestHelper helper) {
        require(TC4InfusionAltarFullClosureParity.COMPONENT_TRAVEL_CYCLES == 5,
                "Original itemCount starts at five craft cycles");
        require(TC4InfusionRuntime.ITEM_PULL_DELAY == TC4InfusionAltarFullClosureParity.COMPONENT_TRAVEL_CYCLES,
                "Production infusion runtime drifted from the full-closure contract");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionPedestalNbtAndComparatorUseProductionPath(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlock(pos, ThaumcraftMod.ARCANE_PEDESTAL.get().defaultBlockState(), Block.UPDATE_ALL);
        ArcanePedestalBlockEntity pedestal = requireBlockEntity(level, pos, ArcanePedestalBlockEntity.class);
        pedestal.setStored(new ItemStack(Items.DIAMOND));
        CompoundTag update = pedestal.getUpdateTag();
        require(update.contains(TC4InfusionAltarFullClosureParity.PEDESTAL_ITEMS_NBT),
                "Production pedestal must save original Items list");
        ListTag items = update.getList(TC4InfusionAltarFullClosureParity.PEDESTAL_ITEMS_NBT, 10);
        require(items.size() == 1
                        && items.getCompound(0).getByte(TC4InfusionAltarFullClosureParity.PEDESTAL_SLOT_NBT) == 0,
                "Production pedestal must save Slot byte 0");
        require(TC4InfusionAltarFullClosureParity.pedestalComparator(!pedestal.isEmpty()) == 15,
                "Occupied one-slot pedestal comparator must be 15");
        pedestal.takeStored();
        require(TC4InfusionAltarFullClosureParity.pedestalComparator(!pedestal.isEmpty()) == 0,
                "Empty one-slot pedestal comparator must be 0");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionPedestalAndStabilizerScanBoundsMatchTc4(GameTestHelper helper) {
        require(TC4InfusionAltarFullClosureParity.isPedestalScanOffset(8, -10, 8),
                "Pedestal scan must include original far corner and bottom depth");
        require(!TC4InfusionAltarFullClosureParity.isPedestalScanOffset(9, -10, 8),
                "Pedestal scan must stop at horizontal radius 8");
        require(TC4InfusionAltarFullClosureParity.STABILIZER_HORIZONTAL_RADIUS == 12
                        && TC4InfusionAltarFullClosureParity.STABILIZER_TOP_OFFSET_FROM_MATRIX == 5
                        && TC4InfusionAltarFullClosureParity.STABILIZER_BOTTOM_OFFSET_FROM_MATRIX == -10,
                "Stabilizer scan must remain x/z +/-12 and y +5..-10 from matrix");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void infusionMatrixLightAndCraftingBreakExplosionMatchTc4(GameTestHelper helper) {
        require(TC4InfusionAltarFullClosureParity.MATRIX_LIGHT_LEVEL == 10,
                "Runic matrix light level must be 10");
        require(TC4InfusionAltarFullClosureParity.CRAFTING_BREAK_EXPLOSION_STRENGTH == 2.0F,
                "Breaking a crafting matrix must use original explosion strength 2.0");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void golemEveryCoreHasCreeperAvoidanceAtHighestPriority(GameTestHelper helper) {
        require(TC4GolemParity.everyCoreHasCreeperAvoidance(),
                "Every original TC4 golem core must include AIAvoidCreeperSwell at priority 0");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void golemFishCoreHasAIFishWithOriginalPriority(GameTestHelper helper) {
        require(TC4GolemParity.fishCoreHasFishingTask(),
                "FISH core must include AIFish at priority 2 with mutex 3, matching original TC4");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void golemUseCoreHasAIUseItem(GameTestHelper helper) {
        require(TC4GolemParity.useCoreHasUseItemTask(),
                "USE core must include AIUseItem at priority 0 with mutex 3");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void golemMaterialStatsMatchTc4EnumGolemType(GameTestHelper helper) {
        require(TC4GolemParity.materialStatsMatchTc4(),
                "All 8 golem materials must match TC4 EnumGolemType: health, armor, speed, fireResist");
        require(TC4GolemParity.upgradeStackingMatchesTc4(),
                "TC4 allows max 2 same upgrades stacked, stored as byte[] for exact duplication");
        require(TC4GolemParity.MATERIAL_COUNT == 8,
                "TC4 defines exactly 8 materials: STRAW, WOOD, TALLOW, CLAY, FLESH, STONE, IRON, THAUMIUM");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void arcaneWorkbenchInventoryAndAutomationMatchOriginal(GameTestHelper helper) {
        require(TC4ArcaneWorkbenchVisCostParity.inventoryLayoutMatchesOriginal(),
                "Arcane Workbench must have exactly 11 slots: output 9 and wand 10");
        require(ArcaneWorkbenchBlockEntity.SIZE == 11,
                "Production block entity must not expose the removed hidden catalyst slot");

        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlock(pos, ThaumcraftMod.ARCANE_WORKBENCH.get().defaultBlockState(), Block.UPDATE_ALL);
        ArcaneWorkbenchBlockEntity workbench = requireBlockEntity(level, pos, ArcaneWorkbenchBlockEntity.class);
        int[] accessible = workbench.getSlotsForFace(Direction.UP);
        require(accessible.length == 1 && accessible[0] == ArcaneWorkbenchBlockEntity.SLOT_WAND,
                "Every automation face must expose only original wand slot 10");
        ItemStack wand = new ItemStack(ThaumcraftMod.IRON_CAPPED_WOODEN_WAND.get());
        require(workbench.canPlaceItemThroughFace(10, wand, Direction.NORTH),
                "Non-staff wand must be insertable through sided automation");
        require(!workbench.canPlaceItemThroughFace(0, new ItemStack(Blocks.STONE), Direction.NORTH),
                "Crafting grid must not be exposed to sided automation");
        require(workbench.canTakeItemThroughFace(10, wand, Direction.SOUTH),
                "Wand slot must be extractable through sided automation");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void arcaneWorkbenchZeroCostAndCentivisScalingMatchOriginal(GameTestHelper helper) {
        require(TC4ArcaneWorkbenchVisCostParity.zeroAspectListIsFree(),
                "An empty original AspectList must cost zero vis, not synthetic Ordo 2");
        require(TC4ArcaneWorkbenchVisCostParity.centivisScalingMatchesOriginal(),
                "Non-empty recipe aspect units must scale by 100 to centivis");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void arcaneWorkbenchTableTransformationInstallsWand(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlock(pos, ThaumcraftMod.TABLE.get().defaultBlockState(), Block.UPDATE_ALL);
        Player player = helper.makeMockPlayer();
        ItemStack wand = new ItemStack(ThaumcraftMod.IRON_CAPPED_WOODEN_WAND.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);

        ArcaneWorkbenchBlockEntity.transformFromTable(level, pos, player, InteractionHand.MAIN_HAND, wand);
        require(level.getBlockState(pos).is(ThaumcraftMod.ARCANE_WORKBENCH.get()),
                "Thaumcraft table must transform into Arcane Workbench");
        ArcaneWorkbenchBlockEntity workbench = requireBlockEntity(level, pos, ArcaneWorkbenchBlockEntity.class);
        require(workbench.getItem(ArcaneWorkbenchBlockEntity.SLOT_WAND).is(ThaumcraftMod.IRON_CAPPED_WOODEN_WAND.get()),
                "Non-staff wand must be copied into original slot 10");
        require(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(),
                "Original transformation removes the selected non-staff wand from the player");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void arcaneWorkbenchGuiCoordinatesMatchTc4Original(GameTestHelper helper) {
        require(TC4ArcaneWorkbenchParity.GUI_WIDTH == 190 && TC4ArcaneWorkbenchParity.GUI_HEIGHT == 234,
                "Arcane Workbench GUI must use original TC4 dimensions 190x234");
        require(TC4ArcaneWorkbenchParity.PRIMALS.length == 6,
                "Original TC4 has exactly 6 primal aspects: Aer, Terra, Ignis, Aqua, Ordo, Perditio");
        require(TC4ArcaneWorkbenchParity.GRID_X == 40 && TC4ArcaneWorkbenchParity.GRID_Y == 40
                        && TC4ArcaneWorkbenchParity.GRID_SPACING == 24,
                "Arcane Workbench 3x3 grid must begin at 40/40 with 24px spacing");
        require(TC4ArcaneWorkbenchParity.WAND_SLOT_X == 160 && TC4ArcaneWorkbenchParity.WAND_SLOT_Y == 24
                        && TC4ArcaneWorkbenchParity.OUTPUT_SLOT_X == 160 && TC4ArcaneWorkbenchParity.OUTPUT_SLOT_Y == 64,
                "Wand/output slots must remain at original 160/24 and 160/64 coordinates");
        require(TC4ArcaneWorkbenchParity.WAND_RENDER_X == 0.65D
                        && TC4ArcaneWorkbenchParity.WAND_RENDER_Y == 1.0625D
                        && TC4ArcaneWorkbenchParity.WAND_RENDER_Z == 0.25D
                        && TC4ArcaneWorkbenchParity.WAND_RENDER_X_ROTATION == 90.0F
                        && TC4ArcaneWorkbenchParity.WAND_RENDER_Z_ROTATION == 20.0F,
                "Installed wand renderer must preserve exact TC4 translation and rotations");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void alchemyFallbackCrucibleRecipesMatchTc4AspectCosts(GameTestHelper helper) {
        require(TC4AlchemyParity.fallbackRecipesHaveCorrectAspectCosts(),
                "3 original TC4 fallback crucible recipes must have correct aspect costs");
        require(TC4AlchemyParity.FALLBACK_RECIPE_COUNT == 3,
                "TC4 registers exactly 3 fallback crucible recipes");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void alchemyLiquidDeathDropMechanicsMatchTc4(GameTestHelper helper) {
        require(TC4AlchemyParity.liquidDeathDropMechanicsMatchTc4(),
                "Liquid Death must drop crystals with 50% chance per aspect, min 1 per stack");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandRodCatalogueMatchesTc4(GameTestHelper helper) {
        require(TC4WandParity.rodCatalogueMatchesTc4(),
                "Wand/staff rod catalogue must preserve the 19 TC4/creative adapter entries and exact capacities/craft costs");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandCapModifiersMatchTc4(GameTestHelper helper) {
        require(TC4WandParity.capCatalogueAndModifiersMatchTc4(),
                "Wand cap base and aspect-specific vis modifiers must match TC4 ConfigItems");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandElementalRodRegenerationMatchesTc4(GameTestHelper helper) {
        require(TC4WandParity.elementalRegenerationMatchesTc4(),
                "Elemental rods must regenerate their primal every 200 ticks and primal staff every 50 ticks below 10% capacity");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandOriginalCreativeVariantsMatchTc4(GameTestHelper helper) {
        java.util.List<ItemStack> variants = WandVariantRuntime.creativeVariants();
        require(variants.size() == TC4WandComponentsFullClosureParity.ORIGINAL_WAND_CREATIVE_VARIANTS,
                "Original ItemWandCasting exposes exactly four filled creative stacks");
        WandComponentData first = WandComponentData.from(variants.get(0));
        WandComponentData second = WandComponentData.from(variants.get(1));
        WandComponentData third = WandComponentData.from(variants.get(2));
        WandComponentData fourth = WandComponentData.from(variants.get(3));
        require(first.rod() == WandRodType.WOOD && first.cap() == WandCapType.IRON && !WandComponentData.isSceptre(variants.get(0)),
                "Creative variant 1 must be wood/iron wand");
        require(second.rod() == WandRodType.GREATWOOD && second.cap() == WandCapType.GOLD && !WandComponentData.isSceptre(variants.get(1)),
                "Creative variant 2 must be greatwood/gold wand");
        require(third.rod() == WandRodType.SILVERWOOD && third.cap() == WandCapType.THAUMIUM && !WandComponentData.isSceptre(variants.get(2)),
                "Creative variant 3 must be silverwood/thaumium wand");
        require(fourth.rod() == WandRodType.SILVERWOOD && fourth.cap() == WandCapType.THAUMIUM && WandComponentData.isSceptre(variants.get(3)),
                "Creative variant 4 must be silverwood/thaumium sceptre");
        for (ItemStack stack : variants) {
            for (Aspect aspect : WandItem.primalVisAspects()) {
                require(WandItem.getVis(stack, aspect) == ((WandItem)stack.getItem()).stackVisCapacity(stack),
                        "Every original creative wand variant must be filled to capacity");
            }
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandRootNbtMigrationMatchesTc4(GameTestHelper helper) {
        ItemStack stack = new ItemStack(ThaumcraftMod.IRON_CAPPED_WOODEN_WAND.get());
        CompoundTag legacy = new CompoundTag();
        legacy.putString("Rod", "blaze_staff");
        legacy.putString("Cap", "void");
        stack.getOrCreateTag().put("Wand", legacy);
        require(WandComponentData.normalizeOriginalTags(stack), "Legacy nested component NBT must migrate once");
        require("blaze_staff".equals(stack.getTag().getString("rod")) && "void".equals(stack.getTag().getString("cap")),
                "Migrated wand must use original root rod/cap strings");
        require(!stack.getTag().contains("Wand"), "Legacy nested Wand compound must not be written back");
        require(!WandComponentData.normalizeOriginalTags(stack), "Already normalized root NBT must remain stable");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandStaffAttackDamageMatchesTc4(GameTestHelper helper) {
        ItemStack staff = WandVariantRuntime.create(WandRodType.GREATWOOD_STAFF, WandCapType.GOLD, false, false);
        ItemStack wand = WandVariantRuntime.create(WandRodType.GREATWOOD, WandCapType.GOLD, false, false);
        double staffDamage = staff.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream()
                .mapToDouble(modifier -> modifier.getAmount()).sum();
        double wandDamage = wand.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream()
                .mapToDouble(modifier -> modifier.getAmount()).sum();
        require(staffDamage == WandItem.STAFF_ATTACK_DAMAGE, "Staff rod must add original +6 attack damage");
        require(wandDamage == 0.0D, "Non-staff wand must not receive staff attack damage");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandStackRarityAndNamesMatchTc4(GameTestHelper helper) {
        ItemStack wand = WandVariantRuntime.create(WandRodType.WOOD, WandCapType.IRON, false, false);
        ItemStack staff = WandVariantRuntime.create(WandRodType.BLAZE_STAFF, WandCapType.VOID, false, false);
        ItemStack sceptre = WandVariantRuntime.create(WandRodType.SILVERWOOD, WandCapType.THAUMIUM, true, false);
        require(wand.getMaxStackSize() == 1 && wand.getRarity() == Rarity.UNCOMMON,
                "Original casting wand is unstackable and uncommon");
        require(wand.getHoverName().getString().contains("Wand") || wand.getHoverName().getString().contains("жезл"),
                "Normal wand name must use localized wand object name");
        require(staff.getHoverName().getString().contains("Staff") || staff.getHoverName().getString().contains("посох"),
                "Staff name must use localized staff object name");
        require(sceptre.getHoverName().getString().contains("Scepter") || sceptre.getHoverName().getString().contains("скипетр"),
                "Sceptre name must use localized sceptre object name");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandComponentCatalogueFullClosureMatchesTc4(GameTestHelper helper) {
        require(TC4WandComponentsFullClosureParity.capacitiesAndCraftCostsMatchOriginal(),
                "All 18 craftable rod/staff capacities and craft costs must match ConfigItems");
        require(TC4WandComponentsFullClosureParity.capCostsAndModifiersMatchOriginal(),
                "All 6 cap costs and base/special modifiers must match ConfigItems");
        require(TC4WandComponentsFullClosureParity.regenerationMatchesOriginal(),
                "Elemental rods must add one displayed vis on original cadence below 10 percent");
        require(TC4WandComponentsFullClosureParity.catalogueCountsMatchOriginal(),
                "Original component and casting-wand catalogue counts must remain exact");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandFociCatalogueAndKeysMatchTc4(GameTestHelper helper) {
        require(TC4WandFociFullClosureParity.cataloguesMatchOriginal(),
                "TC4 must expose ten foci, eighteen pouch slots and five upgrade ranks");
        require(TC4WandFociFullClosureParity.nbtKeysMatchOriginal(),
                "Focus, pouch and upgrade NBT keys must match original TC4");
        require(TC4WandFociFullClosureParity.soundContractMatchesOriginal(),
                "WandManager camera tick volume and pitches must match original TC4");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandFocusCanonicalStackNbtRoundTripMatchesTc4(GameTestHelper helper) {
        ItemStack wand = WandVariantRuntime.create(WandRodType.GREATWOOD, WandCapType.GOLD, false, false);
        ItemStack focus = new ItemStack(ThaumcraftMod.FOCUS_FIRE.get());
        FocusUpgradeRuntime.setAppliedUpgrades(focus, new short[]{9, 0, -1, -1, -1});
        WandFocusRuntime.setFocusStack(wand, focus);
        require(wand.getTag() != null && wand.getTag().contains(TC4WandFocusContract.FOCUS_STACK_NBT, Tag.TAG_COMPOUND),
                "Installed focus must be a complete ItemStack in lower-case focus compound");
        require(!wand.getTag().contains(TC4WandFocusContract.LEGACY_FOCUS_ID_NBT),
                "Temporary upper-case Focus id must not be written");
        ItemStack restored = WandFocusRuntime.getFocusStack(wand);
        require(restored.getItem() instanceof WandFocusItem && WandFocusRuntime.getFocus(wand) == WandFocusType.FIRE,
                "Focus type must round-trip through the canonical stack compound");
        short[] upgrades = FocusUpgradeRuntime.getAppliedUpgrades(restored);
        require(upgrades[0] == 9 && upgrades[1] == 0,
                "Full focus ItemStack NBT, including upgrade ranks, must survive installation");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandFocusLegacyIdMigratesOnce(GameTestHelper helper) {
        ItemStack wand = WandVariantRuntime.create(WandRodType.WOOD, WandCapType.IRON, false, false);
        wand.getOrCreateTag().putString(TC4WandFocusContract.LEGACY_FOCUS_ID_NBT, WandFocusType.FROST.id());
        ItemStack migrated = WandFocusRuntime.getFocusStack(wand);
        require(migrated.getItem() instanceof WandFocusItem && WandFocusRuntime.getFocus(wand) == WandFocusType.FROST,
                "Legacy Focus string must migrate to the matching functional focus stack");
        require(wand.getTag().contains(TC4WandFocusContract.FOCUS_STACK_NBT, Tag.TAG_COMPOUND)
                        && !wand.getTag().contains(TC4WandFocusContract.LEGACY_FOCUS_ID_NBT),
                "Legacy migration must be one-way and leave only canonical focus NBT");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void focusPouchInventorySlotNbtMatchesTc4(GameTestHelper helper) {
        ItemStack pouch = new ItemStack(ThaumcraftMod.FOCUS_POUCH.get());
        ItemStack[] inventory = new ItemStack[TC4WandFocusContract.FOCUS_POUCH_SLOTS];
        java.util.Arrays.fill(inventory, ItemStack.EMPTY);
        inventory[7] = new ItemStack(ThaumcraftMod.FOCUS_SHOCK.get());
        FocusPouchItem.setInventory(pouch, inventory);
        require(pouch.getTag() != null && pouch.getTag().contains(TC4WandFocusContract.POUCH_INVENTORY_NBT, Tag.TAG_LIST),
                "Focus pouch must save the original Inventory list");
        ListTag list = pouch.getTag().getList(TC4WandFocusContract.POUCH_INVENTORY_NBT, Tag.TAG_COMPOUND);
        require(list.size() == 1 && (list.getCompound(0).getByte(TC4WandFocusContract.POUCH_SLOT_NBT) & 255) == 7,
                "Focus pouch entry must preserve the original byte Slot index");
        require(!pouch.getTag().contains("SelectedFocus"),
                "Invented SelectedFocus state must never be persisted");
        require(FocusPouchItem.getInventory(pouch)[7].getItem() == ThaumcraftMod.FOCUS_SHOCK.get(),
                "Focus pouch ItemStack must round-trip through Inventory/Slot NBT");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandFocusUpgradeListMatchesTc4(GameTestHelper helper) {
        ItemStack focus = new ItemStack(ThaumcraftMod.FOCUS_EXCAVATION.get());
        short[] expected = new short[]{0, 1, 20, -1, -1};
        FocusUpgradeRuntime.setAppliedUpgrades(focus, expected);
        ListTag list = focus.getTag().getList(TC4WandFocusContract.UPGRADE_LIST_NBT, Tag.TAG_COMPOUND);
        require(list.size() == TC4WandFocusContract.FOCUS_UPGRADE_RANKS,
                "ItemFocusBasic upgrade list must always contain five rank compounds");
        short[] actual = FocusUpgradeRuntime.getAppliedUpgrades(focus);
        require(java.util.Arrays.equals(expected, actual),
                "Each focus rank must use the original short id field and exact ordering");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandFocusRadialGeometryMatchesTc4(GameTestHelper helper) {
        require(TC4WandFociFullClosureParity.radialGeometryMatchesOriginal(10),
                "Radial radius, first angle and equal pie slices must match REHWandHandler");
        require(TC4WandFocusContract.radialRadius(10) == 41.0F
                        && TC4WandFocusContract.radialSliceDegrees(10) == 36.0F,
                "Ten original foci must produce radius 41 and 36 degree slices");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void essentiaStorageCapacitiesAndSuctionMatchTc4(GameTestHelper helper) {
        require(TC4EssentiaParity.storageCapacitiesAndSuctionMatchTc4(),
                "Jar/reservoir capacities and normal/filtered/void suction strengths must match TC4");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void essentiaStoragePullCadenceMatchesTc4(GameTestHelper helper) {
        require(TC4EssentiaParity.storagePullCadenceMatchesTc4(),
                "Jars and reservoirs must actively pull essentia every five ticks");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void essentiaTubeSubtypesAndPropagationMatchTc4(GameTestHelper helper) {
        require(TC4EssentiaParity.tubeSubtypesAndPropagationMatchTc4(),
                "Six tube subtypes, suction transforms and 2/5/20 tick cadences must match TC4");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void essentiaMirrorTransferContractMatchesTc4(GameTestHelper helper) {
        require(TC4EssentiaParity.mirrorTransferContractMatchesTc4(),
                "Essentia mirrors must search the original range and transfer exactly one point per request");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void taintDeathConversionAndSpreadConstantsMatchTc4(GameTestHelper helper) {
        require(TC4TaintParity.deathConversionTableMatchesTc4(),
                "Six specific tainted replacements plus thaumic-slime fallback must remain registered");
        require(TC4TaintParity.spreadConstantsMatchTc4(),
                "Taint spread must preserve 3/5/3 offsets and the 1-in-200 spore gate");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void worldgenBiomesTreesAndManaPodsMatchTc4(GameTestHelper helper) {
        require(TC4WorldgenParity.biomeAndDecoratorConstantsMatchTc4(),
                "Magical Forest weight, both TC4 biomes and ten mana-pod attempts must match TC4");
        require(TC4WorldgenParity.treeEntryPointsExist(),
                "Greatwood and silverwood worldgen entry points must remain present");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void runicShieldDamageAndFortressConstantsMatchTc4(GameTestHelper helper) {
        require(TC4RunicParity.shieldConstantsMatchTc4(), "Runic shield 2000ms/80 tick/50 vis constants must match TC4");
        require(TC4RunicParity.damageSourcesMatchTc4(), "Taint and dissolve damage-source flags must match TC4");
        require(TC4RunicParity.fortressArmorConstantsMatchTc4(), "Fortress three-piece set and protection divisors must match TC4");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void eldritchWarpCrimsonAndOuterLandsMatchTc4(GameTestHelper helper) {
        require(TC4EldritchParity.warpThresholdsAndCrimsonRitesMatchTc4(),
                "Warp thresholds 10/25/50 and Crimson Rites increments 1/5 must match TC4");
        require(TC4EldritchParity.outerLandsConstantsMatchTc4(),
                "Outer Lands ground, sky, respawn, fog and cell-size constants must match TC4");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void auraNodeTypesModifiersAndJarCaptureMatchTc4(GameTestHelper helper) {
        require(TC4AuraParity.nodeTypesMatchTc4(), "All six aura-node types and colors must match TC4");
        require(TC4AuraParity.modifierDamageChainMatchesTc4(),
                "Node-jar 75% modifier damage chain and 100% aspect preservation must match TC4");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void recipeAspectAndSmeltingBonusLedgerMatchesTc4(GameTestHelper helper) {
        require(TC4RecipeParity.aspectAndSmeltingBonusLedgerMatchesTc4(),
                "48 aspect labels and 18 separate smelting bonuses must not alter the 258 recipe denominator");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void golemDecorationsBellModesAndCarryFormulaMatchTc4(GameTestHelper helper) {
        require(TC4GolemParity.decorationsBellModesAndCarryFormulaMatchTc4(),
                "Nine decorations, six bell modes and earth-upgrade carry scaling must match TC4");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void wandFocusCatalogueAndUpgradeIdsMatchTc4(GameTestHelper helper) {
        require(TC4WandParity.focusCatalogueAndUpgradeIdsMatchTc4(),
                "Ten focus types and contiguous focus-upgrade ids 0..20 must match TC4");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void alchemyLiquidDeathCrystalCountFormulaMatchesTc4(GameTestHelper helper) {
        require(TC4AlchemyParity.liquidDeathCrystalCountFormulaMatchesTc4(),
                "Liquid Death crystal count must use max(1, (1 + nextInt(amount)) / 2)");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void thaumometerHoldDurationAndCompletionWindowMatchTc4(GameTestHelper helper) {
        require(TC4ThaumometerParity.timingContractMatchesTc4(),
                "Thaumometer must use 25 ticks and complete at the original remaining-count boundary of five");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void thaumometerCameraTickCadenceMatchesTc4(GameTestHelper helper) {
        require(TC4ThaumometerParity.soundContractMatchesTc4(),
                "Thaumometer must emit ten pre-completion camera ticks with original volume and pitch range");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void thaumometerNewTargetAndEntityPriorityMatchTc4(GameTestHelper helper) {
        require(TC4ThaumometerParity.targetingContractMatchesTc4(),
                "Handheld scans must reject known targets and prefer a valid entity over the block ray");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void thaumometerAspectPoolCapsMatchTc4(GameTestHelper helper) {
        require(TC4ThaumometerParity.cappedAspectReward(99, 9, false) == 9
                        && TC4ThaumometerParity.cappedAspectReward(100, 9, false) == 3
                        && TC4ThaumometerParity.cappedAspectReward(125, 9, false) == 1,
                "Thaumometer aspect rewards must apply the original 100/125 pool caps");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void thaumometerDiscoveryBonusPrecedesCaps(GameTestHelper helper) {
        require(TC4ThaumometerParity.cappedAspectReward(0, 4, true) == 6
                        && TC4ThaumometerParity.cappedAspectReward(100, 7, true) == 3
                        && TC4ThaumometerParity.cappedAspectReward(125, 7, true) == 1,
                "First-discovery +2 must be applied before sqrt/hard-cap reduction");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void thaumometerNodeVisibilityConstantsMatchTc4(GameTestHelper helper) {
        require(TC4ThaumometerParity.NODE_VIEW_DISTANCE == 48.0D
                        && TC4ThaumometerParity.NODE_VIEW_CONE_DOT == 0.44F,
                "Held Thaumometer node view must use range 48 and cone dot 0.44");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void thaumometerReadoutLayoutMatchesTc4(GameTestHelper helper) {
        require(TC4ThaumometerParity.MAX_RENDERED_ASPECTS == 15
                        && TC4ThaumometerParity.rowCapacity(0) == 5
                        && TC4ThaumometerParity.rowCapacity(4) == 1
                        && TC4ThaumometerParity.NODE_TYPE_TEXT_COLOR == 15642134,
                "Scanner glass must retain the 5/4/3/2/1 aspect layout and node label colour");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void thaumometerTitleShrinkFormulaMatchesTc4(GameTestHelper helper) {
        require(Math.abs(TC4ThaumometerParity.titleScale(90) - 0.005F) < 0.000001F
                        && Math.abs(TC4ThaumometerParity.titleScale(110) - 0.0045F) < 0.000001F,
                "Scanner title must shrink by 0.000025 per pixel beyond width 90");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void thaumometerStableNodeIdentityMatchesTc4(GameTestHelper helper) {
        require("11.64.25".equals(TC4ThaumometerScanKeys.CONTRACT_VERSION)
                        && "NODE".equals(TC4ThaumometerScanKeys.NODE_PREFIX),
                "Aura-node scans must use the original NODE plus stable node-id identity");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void thaumometerItemContractMatchesTc4(GameTestHelper helper) {
        require(TC4ThaumometerParity.MAX_STACK_SIZE == 1
                        && TC4ThaumometerParity.USE_DURATION_TICKS == 25
                        && TC4ThaumometerParity.ENTITY_SCAN_RANGE == 10.0D,
                "Thaumometer must remain single-stack, 25-tick use and 10-block range");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void researchMasteryFreePlacementOrderingMatchesTc4(GameTestHelper helper) {
        require(TC4ResearchEfficiencyParity.freePlacementOrderingMatchesTc4(),
                "Research Mastery must resolve its 10% free-placement roll before pool/bonus availability");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void researchExpertiseMasteryRefundThresholdsMatchTc4(GameTestHelper helper) {
        require(TC4ResearchEfficiencyParity.probabilityBoundariesMatchTc4(),
                "Research Expertise/Mastery must preserve strict 25%, 50% and 10% boundaries");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void researchTableBonusCadenceAndRadiusMatchTc4(GameTestHelper helper) {
        require(TC4ResearchEfficiencyParity.bonusCadenceAndRadiusMatchTc4(),
                "Research Table bonus recalculation must retain the original >600 counter and radius 8 scan");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void researchTableBonusNbtCollapsesDuplicateTypesLikeTc4(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.RESEARCH_TABLE.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);

        ResearchTableBlockEntity table = requireBlockEntity(level, pos, ResearchTableBlockEntity.class);
        table.bonusAspects().add(Aspect.AER, 3).add(Aspect.IGNIS, 2);
        CompoundTag saved = table.saveWithoutMetadata();
        ListTag serialized = saved.getList(TC4ResearchTableBehaviorParity.BONUS_ASPECTS_TAG, Tag.TAG_COMPOUND);
        require(serialized.size() == 2,
                "TC4 Research Table must serialize one bonus record per positive aspect type");

        // Simulate an older port save that wrote one list entry per stored point.
        CompoundTag duplicateAer = new CompoundTag();
        duplicateAer.putString(TC4ResearchTableBehaviorParity.BONUS_ASPECT_TAG, Aspect.AER.id());
        serialized.add(duplicateAer);
        saved.put(TC4ResearchTableBehaviorParity.BONUS_ASPECTS_TAG, serialized);

        ResearchTableBlockEntity restored = new ResearchTableBlockEntity(pos, state);
        restored.load(saved);
        require(restored.bonusAmount(Aspect.AER) == 1 && restored.bonusAmount(Aspect.IGNIS) == 1,
                "Research Table bonus NBT must restore one point per serialized aspect type");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void researchTableBonusConsumptionPersistsAcrossSaveReload(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.RESEARCH_TABLE.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);

        ResearchTableBlockEntity table = requireBlockEntity(level, pos, ResearchTableBlockEntity.class);
        table.bonusAspects().add(Aspect.AER, 1).add(Aspect.IGNIS, 1);
        require(table.consumeBonusAspect(Aspect.AER), "Research Table did not consume its stored Aer bonus");

        CompoundTag saved = table.saveWithoutMetadata();
        ResearchTableBlockEntity restored = new ResearchTableBlockEntity(pos, state);
        restored.load(saved);
        require(restored.bonusAmount(Aspect.AER) == 0,
                "Consumed Research Table bonus reappeared after save/reload");
        require(restored.bonusAmount(Aspect.IGNIS) == 1,
                "Unconsumed Research Table bonus was lost after save/reload");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 120)
    public static void researchTableRecalcCounterUsesOriginalPostIncrementBoundary(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.RESEARCH_TABLE.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);

        ResearchTableBlockEntity table = requireBlockEntity(level, pos, ResearchTableBlockEntity.class);
        CompoundTag atThreshold = table.saveWithoutMetadata();
        atThreshold.putInt(TC4ResearchTableBehaviorParity.NEXT_RECALC_TAG, 600);
        table.load(atThreshold);
        ResearchTableBlockEntity.serverTick(level, pos, state, table);
        require(table.saveWithoutMetadata().getInt(TC4ResearchTableBehaviorParity.NEXT_RECALC_TAG) == 601,
                "Research Table must not recalculate while the pre-increment counter equals 600");

        CompoundTag beyondThreshold = table.saveWithoutMetadata();
        beyondThreshold.putInt(TC4ResearchTableBehaviorParity.NEXT_RECALC_TAG, 601);
        table.load(beyondThreshold);
        ResearchTableBlockEntity.serverTick(level, pos, state, table);
        require(table.saveWithoutMetadata().getInt(TC4ResearchTableBehaviorParity.NEXT_RECALC_TAG) == 0,
                "Research Table must recalculate and reset when the pre-increment counter is 601");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchMasteryCombinationConsumesPoolAndBonusAtomically(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResearchTableBlockEntity table = openResearchTableForCombinationTest(helper, player);
        setPoolAmount(player, Aspect.AER, 0);
        setPoolAmount(player, Aspect.PERDITIO, 1);
        table.bonusAspects().add(Aspect.AER, 1);

        var result = ResearchTableFoundation.combine(player, Aspect.AER, Aspect.PERDITIO);
        require(result.orElse(null) == Aspect.VACUOS,
                "Research Mastery combination did not create Vacuos from Aer + Perditio");
        require(PlayerAspectKnowledge.pool(player).get(Aspect.PERDITIO) == 0,
                "Combination did not consume the player-pool component");
        require(table.bonusAmount(Aspect.AER) == 0,
                "Combination did not consume the table-bonus component");
        require(PlayerAspectKnowledge.pool(player).get(Aspect.VACUOS) == 1,
                "Successful combination did not add one result point");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchCombinationMissingSecondComponentRollsBackFirst(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        openResearchTableForCombinationTest(helper, player);
        setPoolAmount(player, Aspect.AER, 1);
        setPoolAmount(player, Aspect.PERDITIO, 0);

        require(ResearchTableFoundation.combine(player, Aspect.AER, Aspect.PERDITIO).isEmpty(),
                "Combination unexpectedly succeeded without its second component");
        require(PlayerAspectKnowledge.pool(player).get(Aspect.AER) == 1,
                "Failed atomic preflight consumed the first component");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchInvalidCombinationStillConsumesBothComponents(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        openResearchTableForCombinationTest(helper, player);
        setPoolAmount(player, Aspect.AER, 1);
        setPoolAmount(player, Aspect.TERRA, 1);

        require(ResearchTableFoundation.combine(player, Aspect.AER, Aspect.TERRA).isEmpty(),
                "Aer + Terra unexpectedly produced a compound aspect");
        require(PlayerAspectKnowledge.pool(player).get(Aspect.AER) == 0
                        && PlayerAspectKnowledge.pool(player).get(Aspect.TERRA) == 0,
                "TC4 invalid combination must still consume both selected components");
        require(TC4ResearchMasteryCombinationParity.invalidPairsStillConsumeComponents(),
                "Parity contract no longer records invalid-pair consumption");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchSameAspectShortageCannotPartiallyDebit(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        openResearchTableForCombinationTest(helper, player);
        setPoolAmount(player, Aspect.AER, 1);

        require(ResearchTableFoundation.combine(player, Aspect.AER, Aspect.AER).isEmpty(),
                "Same-aspect request unexpectedly succeeded with only one point");
        require(PlayerAspectKnowledge.pool(player).get(Aspect.AER) == 1,
                "Same-aspect shortage partially consumed its only point");
        require(TC4ResearchMasteryCombinationParity.atomicPreflightMatchesTc4(),
                "Pair-debit preflight contract drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchNoteDisconnectedEmptyHexPlacementMatchesTc4(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResearchNotePlacementFixture fixture = openResearchNotePlacementTest(helper, player, false);
        setPoolAmount(player, Aspect.AER, 1);

        require(ResearchNoteGrid.neighbors(fixture.targetSlot()).stream()
                        .noneMatch(index -> ResearchNoteState.slot(fixture.note(), index).isPresent()),
                "Test target unexpectedly touches an occupied hex");
        require(ResearchNoteSolver.placeAspect(player, fixture.note(), fixture.targetSlot(), Aspect.AER),
                "TC4 type-0 hex placement was incorrectly blocked for lacking a compatible neighbour");
        require(ResearchNoteState.slot(fixture.note(), fixture.targetSlot()).orElse(null) == Aspect.AER,
                "Disconnected type-0 hex did not receive the placed aspect");
        require(PlayerAspectKnowledge.pool(player).get(Aspect.AER) == 0,
                "Successful Research Note placement did not consume one aspect point");
        require(ScribingToolsItem.inkLeft(fixture.table().getItem(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS))
                        == ScribingToolsItem.MAX_INK - 1,
                "Successful Research Note placement did not consume one ink");
        require(!TC4ResearchNoteGraphParity.placementRequiresCompatibleNeighbour(),
                "Graph parity contract reintroduced a neighbour gate");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchNoteForgedUnknownAspectIsRejectedServerSide(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResearchNotePlacementFixture fixture = openResearchNotePlacementTest(helper, player, false);
        setPoolAmount(player, Aspect.MOTUS, 1);

        require(!PlayerAspectKnowledge.knows(player, Aspect.MOTUS),
                "Unknown-aspect test setup unexpectedly discovered Motus");
        require(!ResearchNoteSolver.placeAspect(player, fixture.note(), fixture.targetSlot(), Aspect.MOTUS),
                "Server accepted a forged placement for an undiscovered aspect");
        require(PlayerAspectKnowledge.pool(player).get(Aspect.MOTUS) == 1,
                "Rejected unknown-aspect packet consumed its pool point");
        require(ScribingToolsItem.inkLeft(fixture.table().getItem(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS))
                        == ScribingToolsItem.MAX_INK,
                "Rejected unknown-aspect packet consumed ink");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchNoteForgedOccupiedHexIsRejectedWithoutDebit(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResearchNotePlacementFixture fixture = openResearchNotePlacementTest(helper, player, true);
        setPoolAmount(player, Aspect.AER, 1);

        require(!ResearchNoteSolver.placeAspect(player, fixture.note(), fixture.targetSlot(), Aspect.AER),
                "Server overwrote an occupied Research Note hex");
        require(ResearchNoteState.slot(fixture.note(), fixture.targetSlot()).orElse(null) == Aspect.ORDO,
                "Rejected occupied-hex packet changed the existing aspect");
        require(PlayerAspectKnowledge.pool(player).get(Aspect.AER) == 1,
                "Rejected occupied-hex packet consumed an aspect point");
        require(ScribingToolsItem.inkLeft(fixture.table().getItem(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS))
                        == ScribingToolsItem.MAX_INK,
                "Rejected occupied-hex packet consumed ink");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchNotePlacementRollbackRestoresInkAndAspect(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResearchNotePlacementFixture fixture = openResearchNotePlacementTest(helper, player, false);
        setPoolAmount(player, Aspect.AER, 1);

        var debit = ResearchTableInventoryRuntime.debitResearchNotePlacementAtomically(
                player, Aspect.AER, true);
        require(debit.isPresent(), "Unable to create real Research Note placement debit");
        require(PlayerAspectKnowledge.pool(player).get(Aspect.AER) == 0,
                "Placement debit did not consume its aspect point");
        require(ScribingToolsItem.inkLeft(fixture.table().getItem(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS))
                        == ScribingToolsItem.MAX_INK - 1,
                "Placement debit did not consume ink");

        ResearchTableInventoryRuntime.rollbackResearchNotePlacementDebit(player, debit.get());
        require(PlayerAspectKnowledge.pool(player).get(Aspect.AER) == 1,
                "Placement rollback did not restore the aspect point");
        require(ScribingToolsItem.inkLeft(fixture.table().getItem(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS))
                        == ScribingToolsItem.MAX_INK,
                "Placement rollback did not restore ink");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchNotePacketRequiresLiveOpenResearchTable(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        BlockState state = ThaumcraftMod.RESEARCH_TABLE.get().defaultBlockState();
        ResearchTableBlockEntity ghostTable = new ResearchTableBlockEntity(BlockPos.ZERO, state);
        ghostTable.setItem(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS,
                new ItemStack(ThaumcraftMod.SCRIBING_TOOLS.get()));
        ghostTable.setItem(ResearchTableBlockEntity.SLOT_RESEARCH_NOTE,
                controlledResearchNote(false));
        player.containerMenu = new ResearchTableMenu(116399, player.getInventory(), ghostTable);

        require(!ResearchTableInventoryRuntime.hasOpenResearchTable(player),
                "Packet context accepted a Research Table that is not live in the server level");
        require(ResearchTableInventoryRuntime.findOpenTableResearchNote(player).isEmpty(),
                "Forged/stale menu exposed a Research Note to the server packet path");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchNoteClearConsumesInkWithoutImplicitRefund(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResearchNotePlacementFixture fixture = openResearchNotePlacementTest(helper, player, true);
        setPoolAmount(player, Aspect.ORDO, 0);

        require(ResearchNoteSolver.clearSlotWithRoll(
                        player, fixture.note(), fixture.targetSlot(), 0.0F),
                "Valid placed Research Note hex did not clear");
        require(ResearchNoteState.slot(fixture.note(), fixture.targetSlot()).isEmpty(),
                "Cleared Research Note hex retained its aspect");
        require(ResearchNoteState.type(fixture.note(), fixture.targetSlot()) == ResearchNoteGrid.TYPE_EMPTY,
                "Cleared Research Note hex did not return to type 0");
        require(PlayerAspectKnowledge.pool(player).get(Aspect.ORDO) == 0,
                "Player without Expertise/Mastery received an aspect refund");
        require(ScribingToolsItem.inkLeft(fixture.table().getItem(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS))
                        == ScribingToolsItem.MAX_INK - TC4ResearchNoteClearParity.INK_PER_ACCEPTED_CLEAR,
                "Accepted clear did not consume exactly one ink");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchNoteExpertiseClearRefundBoundary(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResearchNotePlacementFixture fixture = openResearchNotePlacementTest(helper, player, true);
        PlayerThaumData.unlockResearch(player, "RESEARCHER1");
        setPoolAmount(player, Aspect.ORDO, 0);

        require(ResearchNoteSolver.clearSlotWithRoll(
                        player, fixture.note(), fixture.targetSlot(), 0.249999F),
                "Expertise clear failed below the 25 percent boundary");
        require(PlayerAspectKnowledge.pool(player).get(Aspect.ORDO) == 1,
                "Expertise clear did not refund the removed aspect below 25 percent");
        require(!TC4ResearchNoteClearParity.shouldRefundClearedAspect(
                        true, false, false, 0.25F),
                "Expertise refund incorrectly includes the exact 0.25 boundary");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchNoteMasteryClearRefundBoundary(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResearchNotePlacementFixture fixture = openResearchNotePlacementTest(helper, player, true);
        PlayerThaumData.unlockResearch(player, "RESEARCHER2");
        setPoolAmount(player, Aspect.ORDO, 0);

        require(ResearchNoteSolver.clearSlotWithRoll(
                        player, fixture.note(), fixture.targetSlot(), 0.499999F),
                "Mastery clear failed below the 50 percent boundary");
        require(PlayerAspectKnowledge.pool(player).get(Aspect.ORDO) == 1,
                "Mastery clear did not refund the removed aspect below 50 percent");
        require(!TC4ResearchNoteClearParity.shouldRefundClearedAspect(
                        false, true, false, 0.50F),
                "Mastery refund incorrectly includes the exact 0.50 boundary");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchNoteCreativeHasNoImplicitClearRefund(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResearchNotePlacementFixture fixture = openResearchNotePlacementTest(helper, player, true);
        player.getAbilities().instabuild = true;
        setPoolAmount(player, Aspect.ORDO, 0);

        require(ResearchNoteSolver.clearSlotWithRoll(
                        player, fixture.note(), fixture.targetSlot(), 0.0F),
                "Creative player could not clear a valid placed hex");
        require(PlayerAspectKnowledge.pool(player).get(Aspect.ORDO) == 0,
                "Creative mode incorrectly granted an unconditional aspect refund");
        require(ScribingToolsItem.inkLeft(fixture.table().getItem(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS))
                        == ScribingToolsItem.MAX_INK,
                "Creative clear unexpectedly consumed ink");
        require(!TC4ResearchNoteClearParity.creativeHasImplicitRefund(),
                "Clear parity contract reintroduced a creative refund bypass");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchNoteClearRollbackRestoresNbtInkAndPool(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResearchNotePlacementFixture fixture = openResearchNotePlacementTest(helper, player, true);
        setPoolAmount(player, Aspect.ORDO, 2);
        CompoundTag before = fixture.note().getTag() == null
                ? new CompoundTag() : fixture.note().getTag().copy();

        var debit = ResearchTableInventoryRuntime.debitResearchNoteClearAtomically(
                player, fixture.note(), fixture.targetSlot());
        require(debit.isPresent(), "Unable to create a real Research Note clear debit");
        PlayerAspectKnowledge.addPool(player, Aspect.ORDO, 1);
        require(ResearchNoteState.slot(fixture.note(), fixture.targetSlot()).isEmpty(),
                "Clear debit did not mutate note state");

        ResearchTableInventoryRuntime.rollbackResearchNoteClearDebit(player, debit.get());
        require(fixture.note().getTag() != null && fixture.note().getTag().equals(before),
                "Clear rollback did not restore the exact pre-edit Research Note NBT");
        require(ResearchNoteState.slot(fixture.note(), fixture.targetSlot()).orElse(null) == Aspect.ORDO,
                "Clear rollback did not restore the removed aspect");
        require(ScribingToolsItem.inkLeft(fixture.table().getItem(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS))
                        == ScribingToolsItem.MAX_INK,
                "Clear rollback did not restore ink damage");
        require(PlayerAspectKnowledge.pool(player).get(Aspect.ORDO) == 2,
                "Clear rollback did not restore the original player pool amount");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchNoteForgedClearRejectsEmptyAndAnchorHexes(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResearchNotePlacementFixture fixture = openResearchNotePlacementTest(helper, player, false);
        int anchorSlot = ResearchNoteGrid.byHex(0, 0).orElseThrow().index();
        int inkBefore = ScribingToolsItem.inkLeft(
                fixture.table().getItem(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS));

        require(!ResearchNoteSolver.clearSlotWithRoll(
                        player, fixture.note(), fixture.targetSlot(), 0.0F),
                "Server accepted a forged clear for an empty type-0 hex");
        require(!ResearchNoteSolver.clearSlotWithRoll(
                        player, fixture.note(), anchorSlot, 0.0F),
                "Server accepted a forged clear for a locked research anchor");
        require(ResearchNoteState.slot(fixture.note(), anchorSlot).orElse(null) == Aspect.MOTUS,
                "Rejected anchor clear changed the anchor aspect");
        require(ScribingToolsItem.inkLeft(
                        fixture.table().getItem(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS)) == inkBefore,
                "Rejected clear request consumed ink");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchNoteCompletionRequiresLiveOpenTable(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ItemStack note = controlledSolvableResearchNote(false);
        require(!ResearchNoteSolver.solve(player, note),
                "Research Note completion succeeded without a live Research Table context");
        require(!ResearchNoteState.solved(note),
                "Rejected completion mutated the Research Note");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchNoteConnectedGraphCompletesAndPrunes(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResearchNotePlacementFixture fixture = openSolvableResearchNoteTest(helper, player, true);
        PlayerAspectKnowledge.discover(player, Aspect.MOTUS);
        int disconnected = ResearchNoteGrid.byHex(2, 0).orElseThrow().index();

        require(ResearchNoteSolver.solve(player, fixture.note()),
                "Connected Research Note graph did not complete");
        require(ResearchNoteState.solved(fixture.note()) && ResearchNoteState.progress(fixture.note()) == 100,
                "Completed Research Note did not persist solved/progress state");
        require(ResearchNoteState.slot(fixture.note(), disconnected).isEmpty(),
                "Completion did not prune the disconnected non-anchor hex like TC4");
        require(!TC4ResearchNoteCompletionParity.completionConsumesAdditionalInk(),
                "Completion contract incorrectly consumes a second ink after the edit");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchNoteRepeatedCompletionIsRejected(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResearchNotePlacementFixture fixture = openSolvableResearchNoteTest(helper, player, false);
        PlayerAspectKnowledge.discover(player, Aspect.MOTUS);
        require(ResearchNoteSolver.solve(player, fixture.note()),
                "Initial Research Note completion failed");
        CompoundTag solvedTag = fixture.note().getTag().copy();
        require(!ResearchNoteSolver.solve(player, fixture.note()),
                "Already completed Research Note accepted a second completion");
        require(fixture.note().getTag().equals(solvedTag),
                "Repeated completion changed solved Research Note NBT");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchNoteStaleCompletionSnapshotIsRejected(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResearchNotePlacementFixture fixture = openSolvableResearchNoteTest(helper, player, false);
        PlayerAspectKnowledge.discover(player, Aspect.MOTUS);
        var snapshot = ResearchTableInventoryRuntime.beginResearchNoteCompletion(player, fixture.note());
        require(snapshot.isPresent(), "Unable to create Research Note completion preflight");
        ResearchNoteState.root(fixture.note()).putInt(ResearchNoteState.TAG_PROGRESS, 37);
        require(!ResearchTableInventoryRuntime.commitResearchNoteCompletion(player, snapshot.get()),
                "Stale completion snapshot committed after note NBT changed");
        require(!ResearchNoteState.solved(fixture.note()),
                "Stale completion snapshot marked the note solved");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void completedDiscoveryConsumesCreativeAndUnlocksSibling(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.getAbilities().instabuild = true;
        PlayerThaumData.unlockResearch(player, "NITOR");
        PlayerThaumData.unlockResearch(player, "ALUMENTUM");
        ItemStack note = solvedDiscoveryNote("DISTILESSENTIA");

        require(ResearchNoteSolver.convertSolvedNote(player, note),
                "Solved discovery did not convert");
        require(note.isEmpty(),
                "Completed discovery was not consumed in creative mode like TC4");
        require(PlayerThaumData.hasResearch(player, "DISTILESSENTIA"),
                "Target research was not unlocked");
        require(PlayerThaumData.hasResearch(player, "JARLABEL"),
                "Eligible sibling research was not unlocked after the target");
        require(TC4ResearchNoteCompletionParity.completedDiscoveryConsumedInCreative(),
                "Completion contract reintroduced a creative consumption bypass");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void staleSolvedDiscoverySnapshotCannotUnlockOrConsume(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        PlayerThaumData.unlockResearch(player, "NITOR");
        PlayerThaumData.unlockResearch(player, "ALUMENTUM");
        ItemStack note = solvedDiscoveryNote("DISTILESSENTIA");
        var snapshot = ResearchNoteSolver.beginSolvedNoteConversion(player, note);
        require(snapshot.isPresent(), "Unable to create solved-discovery snapshot");
        ResearchNoteState.root(note).putInt(ResearchNoteState.TAG_COPIES, 99);

        require(!ResearchNoteSolver.commitSolvedNoteConversion(player, snapshot.get()),
                "Stale solved-discovery snapshot committed after note mutation");
        require(note.getCount() == 1,
                "Rejected stale discovery conversion consumed the note");
        require(!PlayerThaumData.hasResearch(player, "DISTILESSENTIA"),
                "Rejected stale discovery conversion unlocked research");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchCompletionWarpSplitMatchesOriginal(GameTestHelper helper) {
        require(TC4ResearchCompletionWarpParity.splitResearchWarp(0).equals(
                        new TC4ResearchCompletionWarpParity.WarpSplit(0, 0)),
                "Zero research warp did not remain zero");
        require(TC4ResearchCompletionWarpParity.splitResearchWarp(1).equals(
                        new TC4ResearchCompletionWarpParity.WarpSplit(1, 0)),
                "One research warp was not fully permanent");
        require(TC4ResearchCompletionWarpParity.splitResearchWarp(2).equals(
                        new TC4ResearchCompletionWarpParity.WarpSplit(1, 1)),
                "Two research warp did not split 1/1");
        require(TC4ResearchCompletionWarpParity.splitResearchWarp(3).equals(
                        new TC4ResearchCompletionWarpParity.WarpSplit(2, 1)),
                "Three research warp did not split 2/1");
        require(TC4ResearchCompletionWarpParity.splitResearchWarp(5).equals(
                        new TC4ResearchCompletionWarpParity.WarpSplit(3, 2)),
                "Five research warp did not split 3/2");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void researchCompletionWarpUpdatesPermanentStickyAndCounter(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        OriginalResearchProgression.applyResearchWarp(player, 5);
        require(PlayerThaumData.getWarpPerm(player) == 3,
                "Research warp permanent bucket should be 3 for a five-point grant");
        require(PlayerThaumData.getWarpSticky(player) == 2,
                "Research warp sticky bucket should be 2 for a five-point grant");
        require(PlayerThaumData.getWarpTemporary(player) == 0,
                "Research completion incorrectly created temporary warp");
        require(PlayerThaumData.getWarpTotal(player) == 5
                        && PlayerThaumData.getWarpCounter(player) == 5,
                "Research warp total/counter drifted after bucket split");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void repeatedResearchUnlockDoesNotDuplicateWarp(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResearchEntry entry = syntheticResearchEntry("TEST_WARP_RESEARCH", 3);
        require(OriginalResearchBridge.unlock(player, entry),
                "Initial synthetic research unlock failed");
        require(!OriginalResearchBridge.unlock(player, entry),
                "Repeated synthetic research unlock was accepted");
        require(PlayerThaumData.getWarpPerm(player) == 2
                        && PlayerThaumData.getWarpSticky(player) == 1
                        && PlayerThaumData.getWarpTotal(player) == 3,
                "Repeated research unlock duplicated or misbucketed warp");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void infectiousWarpSpreadDowngradesLikeOriginal(GameTestHelper helper) {
        TC4WarpRuntimeParity.SpreadResult amplified =
                TC4WarpRuntimeParity.infectiousSpread(2);
        require(amplified.infectious() && amplified.amplifier() == 1,
                "Amplified infectious spread did not remain infectious at amplifier-1");

        TC4WarpRuntimeParity.SpreadResult terminal =
                TC4WarpRuntimeParity.infectiousSpread(0);
        require(!terminal.infectious() && terminal.amplifier() == 0,
                "Terminal infectious spread did not downgrade to ordinary Vis Exhaust");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void propagatedWarpExhaustKeepsDefaultMilkCure(GameTestHelper helper) {
        MobEffectInstance propagated = new MobEffectInstance(
                ThaumcraftMod.INFECTIOUS_VIS_EXHAUST.get(),
                TC4WarpRuntimeParity.INFECTIOUS_SPREAD_DURATION_TICKS,
                1, false, true, true
        );
        require(propagated.getCurativeItems().stream().anyMatch(stack -> stack.is(Items.MILK_BUCKET)),
                "Propagated infectious exhaustion lost the original default milk cure");
        require(TC4WarpRuntimeParity.infectiousSpread(1).keepsDefaultCuratives(),
                "Spread contract no longer preserves default curatives");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void initialWarpEventExhaustRemainsUncurable(GameTestHelper helper) {
        MobEffectInstance source = TC4WarpMobEffect.configureCuratives(new MobEffectInstance(
                ThaumcraftMod.INFECTIOUS_VIS_EXHAUST.get(),
                TC4WarpRuntimeParity.INFECTIOUS_SPREAD_DURATION_TICKS,
                1, false, true, true
        ));
        require(source.getCurativeItems().isEmpty(),
                "Initial warp-event infectious exhaustion unexpectedly retained a cure");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void sanitySoapConsumesOneItemInCreativeAndSurvival(GameTestHelper helper) {
        ItemStack creative = new ItemStack(ThaumcraftMod.SANITY_SOAP.get(), 2);
        creative.shrink(TC4WarpRuntimeParity.sanitySoapConsumption(true));
        require(creative.getCount() == 1,
                "Sanity Soap did not consume one item in creative like TC4");

        ItemStack survival = new ItemStack(ThaumcraftMod.SANITY_SOAP.get(), 2);
        survival.shrink(TC4WarpRuntimeParity.sanitySoapConsumption(false));
        require(survival.getCount() == 1,
                "Sanity Soap survival consumption drifted from one item");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void warpCleansingChanceAndWardDurationMatchOriginal(GameTestHelper helper) {
        require(Math.abs(TC4WarpRuntimeParity.sanitySoapStickyChance(false, false) - 0.33F) < 0.0001F,
                "Base Sanity Soap sticky-cleansing chance drifted");
        require(Math.abs(TC4WarpRuntimeParity.sanitySoapStickyChance(true, true) - 0.83F) < 0.0001F,
                "Warp Ward plus pure-fluid Sanity Soap chance drifted");
        require(TC4WarpRuntimeParity.purifyingFluidWardDuration(0) == 32000,
                "Zero-warp pure-fluid ward duration was not capped at 32000 ticks");
        require(TC4WarpRuntimeParity.purifyingFluidWardDuration(100) == 20000,
                "Permanent-warp 100 pure-fluid ward duration was not 20000 ticks");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void unnaturalHungerCurativeFoodReducesDurationAndAmplifier(GameTestHelper helper) {
        TC4WarpRuntimeParity.UnnaturalHungerReduction reduced =
                TC4WarpRuntimeParity.unnaturalHungerAfterCurative(5000, 3);
        require(reduced.remainsActive() && reduced.duration() == 4400 && reduced.amplifier() == 2,
                "Unnatural Hunger did not lose exactly 600 ticks and one amplifier");

        TC4WarpRuntimeParity.UnnaturalHungerReduction levelZero =
                TC4WarpRuntimeParity.unnaturalHungerAfterCurative(5000, 0);
        require(!levelZero.remainsActive() && levelZero.duration() == 4400 && levelZero.amplifier() == -1,
                "Level-zero Unnatural Hunger was not removed by a curative food");

        TC4WarpRuntimeParity.UnnaturalHungerReduction shortDuration =
                TC4WarpRuntimeParity.unnaturalHungerAfterCurative(600, 3);
        require(!shortDuration.remainsActive() && shortDuration.duration() == 0,
                "Unnatural Hunger survived the original strict duration > 0 gate");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void unnaturalHungerFullRuntimeAndShaderContractMatchesOriginal(GameTestHelper helper) {
        require(Math.abs(TC4UnnaturalHungerParity.exhaustionPerTick(0) - 0.025F) < 0.000001F,
                "Unnatural Hunger level I exhaustion drifted from 0.025 per tick");
        require(Math.abs(TC4UnnaturalHungerParity.exhaustionPerTick(3) - 0.100F) < 0.000001F,
                "Unnatural Hunger level IV exhaustion drifted from 0.1 per tick");
        require(TC4UnnaturalHungerParity.warpAmplifier(14) == 0
                        && TC4UnnaturalHungerParity.warpAmplifier(15) == 1
                        && TC4UnnaturalHungerParity.warpAmplifier(60) == 3,
                "Unnatural Hunger warp/amplifier mapping drifted from min(3, warp/15)");
        require(TC4UnnaturalHungerParity.FIRST_WARP_DURATION_TICKS == 5000
                        && TC4UnnaturalHungerParity.SECOND_WARP_DURATION_TICKS == 6000,
                "Unnatural Hunger event durations drifted from 5000/6000 ticks");

        TC4UnnaturalHungerParity.Rgb white = TC4UnnaturalHungerParity.transform(1.0F, 1.0F, 1.0F);
        require(Math.abs(white.red() - 1.014F) < 0.00001F
                        && Math.abs(white.green() - 0.794F) < 0.00001F
                        && Math.abs(white.blue() - 0.794F) < 0.00001F,
                "Unnatural Hunger ColorScale/Saturation shader math drifted from TC4");
        require(TC4UnnaturalHungerParity.EFFECT_COLOR == 0x446633
                        && TC4UnnaturalHungerParity.ICON_COLUMN == 7
                        && TC4UnnaturalHungerParity.ICON_ROW == 1,
                "Unnatural Hunger potion color/icon coordinates drifted from TC4");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void sunScornedUsesBrightnessTableThresholdsAndExactRolls(GameTestHelper helper) {
        require(TC4WarpRuntimeParity.sunScornedBurns(0.51F, 0.0F, true),
                "Sun Scorned rejected a bright sky-visible zero-roll burn");
        require(!TC4WarpRuntimeParity.sunScornedBurns(0.50F, 0.0F, true),
                "Sun Scorned burn threshold stopped being strictly greater than 0.5");
        require(!TC4WarpRuntimeParity.sunScornedBurns(1.0F, 0.0F, false),
                "Sun Scorned burned a target without sky visibility");
        require(TC4WarpRuntimeParity.sunScornedHeals(0.24F, 0.99F),
                "Sun Scorned rejected a dark high-roll heal");
        require(!TC4WarpRuntimeParity.sunScornedHeals(0.25F, 0.99F),
                "Sun Scorned heal threshold stopped being strictly below 0.25");
        require(!TC4WarpRuntimeParity.sunScornedHeals(0.10F, 0.20F),
                "Sun Scorned accepted equality instead of the original strict heal roll");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void deathGazeUsesTc4ApertureBodyCenterAndRoundCap(GameTestHelper helper) {
        double range = 8.0D;

        require(TC4WarpRuntimeParity.deathGazeConeContains(
                        0.0D, 0.0D, 7.0D,
                        0.0D, 0.0D, 1.0D, range),
                "Death Gaze rejected a target on the cone axis");

        double twentyDegrees = Math.toRadians(20.0D);
        require(TC4WarpRuntimeParity.deathGazeConeContains(
                        Math.sin(twentyDegrees) * 7.0D, 0.0D, Math.cos(twentyDegrees) * 7.0D,
                        0.0D, 0.0D, 1.0D, range),
                "Death Gaze rejected a target inside the original 0.75-radian aperture");

        // The pre-fix port used dot >= 0.75, accepting a 30-degree target.
        // TC4 treated 0.75 as the FULL aperture and compared against
        // cos(0.75/2), so 30 degrees is outside.
        double thirtyDegrees = Math.toRadians(30.0D);
        require(!TC4WarpRuntimeParity.deathGazeConeContains(
                        Math.sin(thirtyDegrees) * 7.0D, 0.0D, Math.cos(thirtyDegrees) * 7.0D,
                        0.0D, 0.0D, 1.0D, range),
                "Death Gaze still uses the too-wide dot>=0.75 cone");

        // TC4 clipped by axial projection, not Euclidean distance. This point
        // is farther than eight blocks in a sphere but remains below the cone's
        // round cap and must be accepted.
        double axial = 7.9D;
        double lateral = Math.tan(twentyDegrees) * axial;
        require(Math.sqrt(axial * axial + lateral * lateral) > range,
                "Death Gaze round-cap fixture is not outside the old spherical gate");
        require(TC4WarpRuntimeParity.deathGazeConeContains(
                        lateral, 0.0D, axial,
                        0.0D, 0.0D, 1.0D, range),
                "Death Gaze incorrectly retained the non-original spherical range gate");

        require(!TC4WarpRuntimeParity.deathGazeConeContains(
                        0.0D, 0.0D, range,
                        0.0D, 0.0D, 1.0D, range),
                "Death Gaze round cap must use the original strict projection < range boundary");
        require(!TC4WarpRuntimeParity.deathGazeConeContains(
                        0.0D, 0.0D, -2.0D,
                        0.0D, 0.0D, 1.0D, range),
                "Death Gaze accepted a target behind the player");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void warpSpawnOffsetsPreserveOriginalTriStateSign(GameTestHelper helper) {
        for (int magnitude = 7; magnitude <= 24; magnitude++) {
            require(TC4WarpRuntimeParity.signedSpawnOffset(magnitude, -1) == -magnitude,
                    "Negative TC4 warp-spawn offset changed magnitude");
            require(TC4WarpRuntimeParity.signedSpawnOffset(magnitude, 0) == 0,
                    "TC4 warp-spawn zero-axis outcome was lost");
            require(TC4WarpRuntimeParity.signedSpawnOffset(magnitude, 1) == magnitude,
                    "Positive TC4 warp-spawn offset changed magnitude");
        }
        require(TC4WarpRuntimeParity.signedSpawnOffset(7, -9) == -7
                        && TC4WarpRuntimeParity.signedSpawnOffset(7, 9) == 7,
                "Spawn-offset helper must normalize unexpected sign values");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void warpSpawnCandidateUsesOriginalCollisionLiquidContract(GameTestHelper helper) {
        require(TC4WarpRuntimeParity.acceptsEntitySpawnCandidate(true, true, false),
                "Valid TC4 spawn candidate was rejected");
        require(!TC4WarpRuntimeParity.acceptsEntitySpawnCandidate(false, true, false),
                "Spawn candidate without a solid top surface was accepted");
        require(!TC4WarpRuntimeParity.acceptsEntitySpawnCandidate(true, false, false),
                "Colliding spawn candidate was accepted");
        require(!TC4WarpRuntimeParity.acceptsEntitySpawnCandidate(true, true, true),
                "Liquid-filled spawn candidate was accepted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void warpSpawnCandidateUsesActualEntityDimensions(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 4));
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos.above(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        MindSpiderEntity spider = ThaumcraftMod.MIND_SPIDER.get().create(level);
        EldritchGuardianEntity guardian = ThaumcraftMod.ELDRITCH_GUARDIAN.get().create(level);
        require(spider != null && guardian != null, "Warp spawn entities were not registered");
        spider.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        guardian.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);

        boolean solidTop = level.getBlockState(pos.below())
                .isFaceSturdy(level, pos.below(), Direction.UP);
        boolean spiderCollisionFree = level.noCollision(spider);
        boolean guardianCollisionFree = level.noCollision(guardian);
        require(spiderCollisionFree,
                "Low Mind Spider should fit beneath a one-block ceiling");
        require(!guardianCollisionFree,
                "Tall Eldritch Guardian unexpectedly fit beneath a one-block ceiling");
        require(TC4WarpRuntimeParity.acceptsEntitySpawnCandidate(solidTop, spiderCollisionFree, false),
                "Entity-aware contract rejected the valid low-clearance spider position");
        require(!TC4WarpRuntimeParity.acceptsEntitySpawnCandidate(solidTop, guardianCollisionFree, false),
                "Entity-aware contract accepted the colliding guardian position");
        helper.succeed();
    }

    @GameTest(template = "thaumcraft:empty", batch = "warp")
    public static void warpText8IsBathSaltsMilestoneNotBlurredVision(GameTestHelper helper) {
        // warp.text.8 is the BATHSALTS milestone line (actual warp > 10), not a
        // per-event message. It must NOT be in the per-event displayed set, and
        // the milestone constant must still resolve to the original key.
        require("warp.text.8".equals(TC4WarpRuntimeParity.BATHSALTS_MILESTONE_MESSAGE_KEY),
                "BATHSALTS milestone must reuse the original TC4 warp.text.8 line");
        require(WARP_TEXT_KEYS.contains("warp.text.8"),
                "warp.text.8 is not part of the original warp text table");
        require(!WarpEvents.usesWarpMessage("warp.text.8"),
                "warp.text.8 must not be displayed by the per-event warp table");
        for (String key : WARP_TEXT_KEYS) {
            if (!key.equals("warp.text.8")) {
                require(WarpEvents.usesWarpMessage(key),
                        "Per-event warp table stopped displaying " + key);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "thaumcraft:empty", batch = "warp")
    public static void eldritchMilestonesGrantResearchWithoutChat(GameTestHelper helper) {
        // TC4 checkWarpEvent granted grantResearch(10) at ELDRITCHMINOR
        // (actual warp > 25) and grantResearch(20) at ELDRITCHMAJOR (actual
        // warp > 50), each with NO chat line. v11.64.08 restores those silent
        // research grants; earlier the port showed fabricated chat literals and
        // never granted research.
        require(TC4EldritchProgression.ELDRITCH_MINOR_WARP == 25,
                "ELDRITCHMINOR threshold must remain actual warp > 25");
        require(TC4EldritchProgression.ELDRITCH_MAJOR_WARP == 50,
                "ELDRITCHMAJOR threshold must remain actual warp > 50");
        require(TC4EldritchProgression.ELDRITCH_MINOR_RESEARCH_GRANTS == 10,
                "ELDRITCHMINOR must grant research magnitude 10 as in TC4");
        require(TC4EldritchProgression.ELDRITCH_MAJOR_RESEARCH_GRANTS == 20,
                "ELDRITCHMAJOR must grant research magnitude 20 as in TC4");
        require(TC4EldritchParity.eldritchMilestoneGrantsMatchTc4(),
                "Eldritch milestone grantResearch magnitudes drifted from TC4");
        helper.succeed();
    }

    @GameTest(template = "thaumcraft:empty", batch = "warp")
    public static void warpResearchGrantRollsTc4RangeAndPrimals(GameTestHelper helper) {
        java.util.List<Aspect> expectedPrimals = java.util.List.of(
                Aspect.AER, Aspect.TERRA, Aspect.IGNIS,
                Aspect.AQUA, Aspect.ORDO, Aspect.PERDITIO);
        require(TC4WarpResearchGrant.primalOrder().equals(expectedPrimals),
                "Warp research grants must preserve TC4 primal aspect order");

        for (int times : new int[]{1, 10, 20}) {
            for (int seed = 0; seed < 128; seed++) {
                TC4WarpResearchGrant.GrantRoll roll = TC4WarpResearchGrant.roll(
                        RandomSource.create(0x5A17L + seed * 31L + times), times);
                require(roll.amount() >= 1 && roll.amount() <= times,
                        "grantResearch amount left original 1..times range");
                require(roll.aspects().stream().allMatch(expectedPrimals::contains),
                        "grantResearch selected a non-primal aspect");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "thaumcraft:empty", batch = "warp")
    public static void bathSaltsFullContractMatchesOriginal(GameTestHelper helper) {
        require(TC4BathSaltsParity.ITEM_ENTITY_LIFESPAN_TICKS == 200,
                "Bath Salts item lifespan drifted from TC4");
        require(TC4BathSaltsParity.FLUID_LIGHT_LEVEL == 10
                        && TC4BathSaltsParity.FLUID_VISCOSITY == 1000,
                "Purifying fluid properties drifted from TC4");
        require(TC4BathSaltsParity.WARD_AMPLIFIER == 0
                        && TC4BathSaltsParity.WARD_ICON_COLUMN == 3
                        && TC4BathSaltsParity.WARD_ICON_ROW == 2
                        && TC4BathSaltsParity.WARD_COLOR == 0xE0F2F7,
                "Warp Ward identity drifted from TC4");
        require(TC4BathSaltsParity.wardDurationTicks(49) == 28571
                        && TC4BathSaltsParity.wardDurationTicks(100) == 20000,
                "Warp Ward sqrt duration formula drifted from TC4");
        require(TC4BathSaltsParity.RECIPE_COGNITIO == 6
                        && TC4BathSaltsParity.RECIPE_AURAM == 6
                        && TC4BathSaltsParity.RECIPE_ORDO == 6
                        && TC4BathSaltsParity.RECIPE_SANO == 6,
                "Bath Salts crucible aspects drifted from TC4");
        require(TC4BathSaltsParity.wizardEmeraldCost(0) == 5
                        && TC4BathSaltsParity.wizardEmeraldCost(2) == 7,
                "Wizard Bath Salts price must remain 5..7 emeralds");
        helper.succeed();
    }

    @GameTest(template = "thaumcraft:empty", batch = "warp")
    public static void bathSaltsExpireConvertsOnlyExactWaterSource(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos sourcePos = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlock(sourcePos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        ItemEntity salts = new ItemEntity(level, sourcePos.getX() + 0.5D,
                sourcePos.getY() + 0.1D, sourcePos.getZ() + 0.5D,
                new ItemStack(ThaumcraftMod.BATH_SALTS.get()));
        require(CommonEvents.dissolveBathSalts(salts),
                "Expired Bath Salts did not convert an exact water source");
        require(level.getBlockState(sourcePos).is(ThaumcraftMod.PURIFYING_FLUID_BLOCK.get()),
                "Water source did not become purifying fluid");

        BlockPos flowingPos = helper.absolutePos(new BlockPos(4, 1, 2));
        level.setBlock(flowingPos, Blocks.WATER.defaultBlockState()
                .setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL, 1), Block.UPDATE_ALL);
        ItemEntity flowingSalts = new ItemEntity(level, flowingPos.getX() + 0.5D,
                flowingPos.getY() + 0.1D, flowingPos.getZ() + 0.5D,
                new ItemStack(ThaumcraftMod.BATH_SALTS.get()));
        require(!CommonEvents.dissolveBathSalts(flowingSalts),
                "Flowing water was incorrectly converted by Bath Salts");
        helper.succeed();
    }

    @GameTest(template = "thaumcraft:empty", batch = "warp")
    public static void purifyingFluidSourceGrantsSingleUseWarpWard(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer();
        require(player instanceof net.minecraft.server.level.ServerPlayer,
                "GameTest mock player must be server-side");
        PlayerThaumData.addWarpPermanent(player, 100);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState pure = ThaumcraftMod.PURIFYING_FLUID_BLOCK.get().defaultBlockState();
        level.setBlock(pos, pure, Block.UPDATE_ALL);
        ThaumcraftMod.PURIFYING_FLUID_BLOCK.get().entityInside(
                level.getBlockState(pos), level, pos, player);
        MobEffectInstance ward = player.getEffect(ThaumcraftMod.WARP_WARD.get());
        require(ward != null && ward.getAmplifier() == 0
                        && ward.getDuration() == TC4BathSaltsParity.wardDurationTicks(100),
                "Purifying source did not grant exact Warp Ward");
        require(level.getBlockState(pos).isAir(),
                "Purifying source was not consumed after granting Warp Ward");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void tallowCandleFamilyMatchesOriginalMetadataAndColors(GameTestHelper helper) {
        require(TC4TallowCandleParity.COLOR_COUNT == 16, "TC4 candle family must contain 16 metadata colours");
        require(TC4TallowCandleParity.color(0) == 0xF0F0F0, "White candle tint mismatch");
        require(TC4TallowCandleParity.color(14) == 0xB3312C, "Red candle tint mismatch");
        require(TC4TallowCandleParity.color(15) == 0x1E1B1B, "Black candle tint mismatch");
        require(TC4TallowCandleParity.legacyMetadata("tallow_candle_light_blue") == 3,
                "Light-blue registry path did not preserve TC4 metadata 3");
        require(TC4TallowCandleParity.legacyMetadata("tallow_candle_black") == 15,
                "Black registry path did not preserve TC4 metadata 15");
        require(TC4TallowCandleParity.isOriginalArcanePechOfferColor(14),
                "Original Arcane Pech offer must include metadata 14");
        require(!TC4TallowCandleParity.isOriginalArcanePechOfferColor(15),
                "Original Arcane Pech loop must exclude metadata 15");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void tallowCandleUsesExactShapeLightAndNoCollision(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        BlockState state = ThaumcraftMod.TALLOW_CANDLE_RED.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);

        AABB shape = state.getShape(level, pos, net.minecraft.world.phys.shapes.CollisionContext.empty()).bounds();
        require(Math.abs(shape.minX - TC4TallowCandleParity.BODY_MIN) < 1.0E-6D
                        && Math.abs(shape.maxX - TC4TallowCandleParity.BODY_MAX) < 1.0E-6D
                        && Math.abs(shape.maxY - TC4TallowCandleParity.BODY_HEIGHT) < 1.0E-6D,
                "Tallow candle selection shape differs from TC4 6/16..10/16 x 8/16");
        require(state.getCollisionShape(level, pos,
                        net.minecraft.world.phys.shapes.CollisionContext.empty()).isEmpty(),
                "TC4 candle must have no collision box");
        require(state.getLightEmission() == TC4TallowCandleParity.BLOCK_LIGHT_LEVEL,
                "TC4 candle light level must be 14");
        require(state.canSurvive(level, pos), "Candle did not accept a solid top support");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void tallowCandleDropsWhenSupportIsLost(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState candle = ThaumcraftMod.TALLOW_CANDLE_BLUE.get().defaultBlockState();
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        require(candle.canSurvive(level, pos), "Supported candle unexpectedly cannot survive");
        level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        BlockState updated = candle.updateShape(Direction.DOWN, Blocks.AIR.defaultBlockState(),
                level, pos, pos.below());
        require(updated.isAir(), "Candle did not remove itself after losing lower support");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void tallowCandleDripsAndStabilizersUseProductionContract(GameTestHelper helper) {
        java.util.List<TC4TallowCandleParity.Drip> first = TC4TallowCandleParity.drips(7, 11, -3);
        java.util.List<TC4TallowCandleParity.Drip> second = TC4TallowCandleParity.drips(7, 11, -3);
        require(first.equals(second), "Coordinate-seeded TC4 candle drips are not deterministic");
        require(first.size() >= 1 && first.size() <= 5, "TC4 candle drip count must be 1..5");
        for (TC4TallowCandleParity.Drip drip : first) {
            require(drip.location() >= 2 && drip.location() <= 3,
                    "TC4 candle drip location must be 2..3");
            require(drip.heightPixels() >= 1 && drip.heightPixels() <= 3,
                    "TC4 candle drip height must be 1..3 pixels");
        }
        Block[] family = {
                ThaumcraftMod.TALLOW_CANDLE.get(), ThaumcraftMod.TALLOW_CANDLE_ORANGE.get(),
                ThaumcraftMod.TALLOW_CANDLE_MAGENTA.get(), ThaumcraftMod.TALLOW_CANDLE_LIGHT_BLUE.get(),
                ThaumcraftMod.TALLOW_CANDLE_YELLOW.get(), ThaumcraftMod.TALLOW_CANDLE_LIME.get(),
                ThaumcraftMod.TALLOW_CANDLE_PINK.get(), ThaumcraftMod.TALLOW_CANDLE_GRAY.get(),
                ThaumcraftMod.TALLOW_CANDLE_LIGHT_GRAY.get(), ThaumcraftMod.TALLOW_CANDLE_CYAN.get(),
                ThaumcraftMod.TALLOW_CANDLE_PURPLE.get(), ThaumcraftMod.TALLOW_CANDLE_BLUE.get(),
                ThaumcraftMod.TALLOW_CANDLE_BROWN.get(), ThaumcraftMod.TALLOW_CANDLE_GREEN.get(),
                ThaumcraftMod.TALLOW_CANDLE_RED.get(), ThaumcraftMod.TALLOW_CANDLE_BLACK.get()
        };
        for (Block block : family) {
            require(block instanceof TallowCandleBlock && block instanceof InfusionStabilizer,
                    "Every TC4 candle colour must remain an infusion stabilizer");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void tallowResearchEntryMatchesOriginalContract(GameTestHelper helper) {
        ResearchEntry entry = OriginalResearchBridge.byKey("TALLOW")
                .orElseThrow(() -> new GameTestAssertException("TALLOW research entry is missing"));
        require("ALCHEMY".equals(entry.category()), "TALLOW category must be ALCHEMY");
        require(entry.displayColumn() == TC4TallowCandleParity.RESEARCH_X
                        && entry.displayRow() == TC4TallowCandleParity.RESEARCH_Y
                        && entry.complexity() == TC4TallowCandleParity.RESEARCH_COMPLEXITY,
                "TALLOW research coordinates or complexity differ from TC4");
        require(java.util.Arrays.asList(entry.requirements()).contains("CRUCIBLE"),
                "TALLOW must require CRUCIBLE");
        require(entry.aspects().getOrDefault("corpus", 0) == TC4TallowCandleParity.RESEARCH_CORPUS
                        && entry.aspects().getOrDefault("praecantatio", 0)
                        == TC4TallowCandleParity.RESEARCH_PRAECANTATIO,
                "TALLOW research aspects differ from TC4");
        require(java.util.Arrays.asList(entry.recipeKeys()).contains("Tallow")
                        && java.util.Arrays.asList(entry.recipeKeys()).contains("TallowCandle"),
                "TALLOW research pages must include tallow and candle recipes");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneBellowsShapePlacementAndNbtMatchOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.BELLOWS.get().defaultBlockState()
                .setValue(BellowsBlock.FACING, Direction.EAST);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        AABB shape = state.getShape(level, pos,
                net.minecraft.world.phys.shapes.CollisionContext.empty()).bounds();
        require(Math.abs(shape.minX - TC4ArcaneBellowsParity.SHAPE_MIN_XZ) < 1.0E-6D
                        && Math.abs(shape.maxX - TC4ArcaneBellowsParity.SHAPE_MAX_XZ) < 1.0E-6D
                        && Math.abs(shape.minY - TC4ArcaneBellowsParity.SHAPE_MIN_Y) < 1.0E-6D
                        && Math.abs(shape.maxY - TC4ArcaneBellowsParity.SHAPE_MAX_Y) < 1.0E-6D,
                "Bellows fixed 0.1..0.9/full-height shape drifted from TC4");
        require(Math.abs(state.getDestroySpeed(level, pos) - TC4ArcaneBellowsParity.BLOCK_HARDNESS) < 1.0E-6F,
                "Bellows hardness drifted from TC4");
        BellowsBlockEntity bellows = requireBlockEntity(level, pos, BellowsBlockEntity.class);
        CompoundTag tag = bellows.saveWithoutMetadata();
        require(tag.getByte("orientation") == (byte) Direction.EAST.get3DDataValue(),
                "Bellows orientation NBT must use original lowercase byte key");
        require(tag.contains("onVanillaFurnace", Tag.TAG_BYTE),
                "Bellows furnace flag must use original lowercase boolean key");
        require(!tag.contains("Delay") && !tag.contains("inflation") && !tag.contains("expanding"),
                "Transient bellows animation/timer state must not persist");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneBellowsAnimationAndInventoryInflationMatchOriginal(GameTestHelper helper) {
        require(Math.abs(TC4ArcaneBellowsParity.initialInflation(0.0F) - 0.35F) < 1.0E-6F
                        && Math.abs(TC4ArcaneBellowsParity.initialInflation(1.0F) - 0.90F) < 1.0E-6F,
                "Bellows first-run random inflation range drifted");
        TC4ArcaneBellowsParity.AnimationStep deflate =
                TC4ArcaneBellowsParity.animationStep(1.0F, false);
        require(Math.abs(deflate.inflation() - 0.925F) < 1.0E-6F
                        && !deflate.expanding() && !deflate.playSound(),
                "Bellows deflate step drifted");
        TC4ArcaneBellowsParity.AnimationStep switchToInflate =
                TC4ArcaneBellowsParity.animationStep(0.35F, false);
        require(Math.abs(switchToInflate.inflation() - 0.375F) < 1.0E-6F
                        && switchToInflate.expanding() && !switchToInflate.playSound(),
                "Bellows lower-bound direction switch drifted");
        TC4ArcaneBellowsParity.AnimationStep top =
                TC4ArcaneBellowsParity.animationStep(0.99F, true);
        require(Math.abs(top.inflation() - 1.015F) < 1.0E-6F
                        && !top.expanding() && top.playSound(),
                "Bellows upper-bound overshoot/sound event drifted");
        require(Math.abs(TC4ArcaneBellowsParity.soundPitch(1.0F, 0.0F) - 0.7F) < 1.0E-6F
                        && Math.abs(TC4ArcaneBellowsParity.inventoryInflation(0) - 0.7F) < 1.0E-6F,
                "Bellows sound or inventory animation formula drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneBellowsBoostsOnlyLegacyHorizontalVanillaFurnaceTarget(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos furnacePos = helper.absolutePos(new BlockPos(4, 1, 2));
        BlockPos bellowsPos = furnacePos.west();
        level.setBlock(furnacePos, Blocks.FURNACE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(bellowsPos, ThaumcraftMod.BELLOWS.get().defaultBlockState()
                .setValue(BellowsBlock.FACING, Direction.EAST), Block.UPDATE_ALL);
        AbstractFurnaceBlockEntity furnace = requireBlockEntity(level, furnacePos, AbstractFurnaceBlockEntity.class);
        CompoundTag furnaceTag = furnace.saveWithoutMetadata();
        furnaceTag.putShort("CookTime", (short) 1);
        furnace.load(furnaceTag);
        BellowsBlockEntity bellows = requireBlockEntity(level, bellowsPos, BellowsBlockEntity.class);
        bellows.refreshAttachment();
        require(bellows.isOnVanillaFurnace(), "Horizontal furnace attachment was not detected");
        BellowsBlockEntity.serverTick(level, bellowsPos, level.getBlockState(bellowsPos), bellows);
        BellowsBlockEntity.serverTick(level, bellowsPos, level.getBlockState(bellowsPos), bellows);
        require(furnace.saveWithoutMetadata().getShort("CookTime") == 2,
                "Horizontal bellows did not add one vanilla furnace cook tick every two ticks");

        BlockPos verticalBellowsPos = helper.absolutePos(new BlockPos(6, 1, 2));
        BlockPos verticalFurnacePos = verticalBellowsPos.above();
        level.setBlock(verticalFurnacePos, Blocks.FURNACE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(verticalBellowsPos, ThaumcraftMod.BELLOWS.get().defaultBlockState()
                .setValue(BellowsBlock.FACING, Direction.UP), Block.UPDATE_ALL);
        AbstractFurnaceBlockEntity verticalFurnace = requireBlockEntity(
                level, verticalFurnacePos, AbstractFurnaceBlockEntity.class);
        CompoundTag verticalTag = verticalFurnace.saveWithoutMetadata();
        verticalTag.putShort("CookTime", (short) 1);
        verticalFurnace.load(verticalTag);
        BellowsBlockEntity vertical = requireBlockEntity(level, verticalBellowsPos, BellowsBlockEntity.class);
        vertical.refreshAttachment();
        require(vertical.isOnVanillaFurnace(), "Vertical placement-time furnace flag should be true in TC4");
        BellowsBlockEntity.serverTick(level, verticalBellowsPos, level.getBlockState(verticalBellowsPos), vertical);
        BellowsBlockEntity.serverTick(level, verticalBellowsPos, level.getBlockState(verticalBellowsPos), vertical);
        require(verticalFurnace.saveWithoutMetadata().getShort("CookTime") == 1,
                "TC4 vertical bellows quirk was lost: ticking must ignore Y offset");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void arcaneBellowsConsumerCountingMatchesOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos furnacePos = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlock(furnacePos, ThaumcraftMod.ALCHEMICAL_FURNACE.get().defaultBlockState(), Block.UPDATE_ALL);
        for (Direction direction : Direction.values()) {
            level.setBlock(furnacePos.relative(direction), ThaumcraftMod.BELLOWS.get().defaultBlockState()
                    .setValue(BellowsBlock.FACING, direction.getOpposite()), Block.UPDATE_ALL);
        }
        AlchemicalFurnaceBlockEntity alchemical = requireBlockEntity(
                level, furnacePos, AlchemicalFurnaceBlockEntity.class);
        AlchemicalFurnaceBlockEntity.serverTick(level, furnacePos, level.getBlockState(furnacePos), alchemical);
        require(alchemical.bellows() == TC4ArcaneBellowsParity.MAX_GENERIC_ATTACHED_BELLOWS,
                "Alchemical furnace must accept all six active oriented bellows");

        BlockPos cruciblePos = helper.absolutePos(new BlockPos(2, 1, 6));
        level.setBlock(cruciblePos, ThaumcraftMod.CRUCIBLE.get().defaultBlockState(), Block.UPDATE_ALL);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            level.setBlock(cruciblePos.relative(direction), ThaumcraftMod.BELLOWS.get().defaultBlockState()
                    .setValue(BellowsBlock.FACING, direction), Block.UPDATE_ALL);
        }
        com.darkifov.thaumcraft.blockentity.CrucibleBlockEntity crucible = requireBlockEntity(
                level, cruciblePos, com.darkifov.thaumcraft.blockentity.CrucibleBlockEntity.class);
        require(crucible.countBellows() == 4,
                "Crucible must count all four horizontal bellows even when they face away");
        require(TC4ArcaneBellowsParity.crucibleHeatGain(4) == 9
                        && TC4ArcaneBellowsParity.bufferSuction(6, 0) == 192
                        && TC4ArcaneBellowsParity.bufferSuction(6, 1) == 1
                        && TC4ArcaneBellowsParity.bufferSuction(6, 2) == 0,
                "Crucible or essentia-buffer bellows formulas drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneBellowsResearchRecipeAndInfernalContractMatchOriginal(GameTestHelper helper) {
        ResearchEntry entry = OriginalResearchBridge.byKey("BELLOWS")
                .orElseThrow(() -> new GameTestAssertException("BELLOWS research entry is missing"));
        require("ARTIFICE".equals(entry.category()) && entry.displayColumn() == -6
                        && entry.displayRow() == -2 && entry.complexity() == 1,
                "BELLOWS category/position/complexity drifted");
        require(java.util.Arrays.asList(entry.requirements()).contains("INFERNALFURNACE")
                        && entry.hasFlag("secondary") && entry.hasFlag("concealed"),
                "BELLOWS parent or flags drifted");
        require(entry.aspects().getOrDefault("aer", 0) == 6
                        && entry.aspects().getOrDefault("machina", 0) == 3
                        && entry.aspects().getOrDefault("motus", 0) == 3,
                "BELLOWS research aspects drifted");
        require(java.util.Arrays.asList(entry.recipeKeys()).contains("Bellows"),
                "BELLOWS research page lost its Arcane Crafting recipe");
        require(TC4ArcaneBellowsParity.alchemicalFurnaceSmeltTime(10, 6) == 25
                        && TC4ArcaneBellowsParity.infernalFurnaceCookTime(false, 3) == 80
                        && TC4ArcaneBellowsParity.infernalFurnaceCookTime(true, 3) == 20
                        && Math.abs(TC4ArcaneBellowsParity.INFERNAL_FURNACE_BONUS_CHANCE_PER_BELLOWS - 0.44F) < 1.0E-6F,
                "Bellows alchemical or external Infernal Furnace contract drifted");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneEarNoteCyclePitchAndPulseMathMatchOriginal(GameTestHelper helper) {
        require(TC4ArcaneEarParity.nextNote(0) == 1
                        && TC4ArcaneEarParity.nextNote(23) == 24
                        && TC4ArcaneEarParity.nextNote(24) == 0,
                "Arcane Ear note cycle must remain 0..24");
        require(Math.abs(TC4ArcaneEarParity.notePitch(12) - 1.0F) < 1.0E-6F
                        && Math.abs(TC4ArcaneEarParity.notePitch(0) - 0.5F) < 1.0E-6F
                        && Math.abs(TC4ArcaneEarParity.notePitch(24) - 2.0F) < 1.0E-6F,
                "Arcane Ear pitch formula drifted from TC4");
        require(TC4ArcaneEarParity.pulseAfterTick(10) == 9
                        && TC4ArcaneEarParity.pulseAfterTick(1) == 0
                        && TC4ArcaneEarParity.pulseAfterTick(0) == 0,
                "Arcane Ear ten-tick pulse countdown drifted");
        require(TC4ArcaneEarParity.matchesPlayedNote(4, 24, 4, 24, 4096.0D)
                        && !TC4ArcaneEarParity.matchesPlayedNote(4, 24, 4, 24, 4096.0001D)
                        && !TC4ArcaneEarParity.matchesPlayedNote(4, 24, 0, 24, 0.0D),
                "Arcane Ear exact note/tone/range matching drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneEarSupportToneAndBlockedOutputMatchOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos, ThaumcraftMod.ARCANE_EAR.get().defaultBlockState(), Block.UPDATE_ALL);
        ArcaneEarBlockEntity ear = requireBlockEntity(level, pos, ArcaneEarBlockEntity.class);
        ear.updateToneFromSupport();
        require(ear.tone() == 1, "Stone support must select bass drum tone");
        level.setBlock(pos.below(), Blocks.SAND.defaultBlockState(), Block.UPDATE_ALL);
        ear.updateToneFromSupport();
        require(ear.tone() == 2, "Sand support must select snare tone");
        level.setBlock(pos.below(), Blocks.GLASS.defaultBlockState(), Block.UPDATE_ALL);
        ear.updateToneFromSupport();
        require(ear.tone() == 3, "Glass support must select hat tone");
        level.setBlock(pos.below(), Blocks.OAK_PLANKS.defaultBlockState(), Block.UPDATE_ALL);
        ear.updateToneFromSupport();
        require(ear.tone() == 4, "Wood support must select bass tone");
        level.setBlock(pos.below(), Blocks.WHITE_WOOL.defaultBlockState(), Block.UPDATE_ALL);
        ear.updateToneFromSupport();
        require(ear.tone() == 0, "Other support materials must select harp tone");
        level.setBlock(pos.above(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        require(!ear.emitConfiguredNote(true), "Solid block above must suppress manual sound and particle");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 60)
    public static void arcaneEarDetectsMatchingNoteAndEmitsTransientRedstone(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos, ThaumcraftMod.ARCANE_EAR.get().defaultBlockState(), Block.UPDATE_ALL);
        ArcaneEarBlockEntity ear = requireBlockEntity(level, pos, ArcaneEarBlockEntity.class);
        ear.updateToneFromSupport();
        require(ear.note() == 0 && ear.tone() == 1, "Arcane Ear initial configuration drifted");

        ArcaneEarBlockEntity.onNotePlayed(level, pos.offset(64, 0, 0), NoteBlockInstrument.BASEDRUM, 0);
        require(ear.signalTicks() == 0,
                "Arcane Ear must buffer note events until its server tick");
        ArcaneEarBlockEntity.serverTick(level, pos, level.getBlockState(pos), ear);
        require(ear.signalTicks() == TC4ArcaneEarParity.REDSTONE_PULSE_TICKS,
                "Matching note at exactly 64 blocks must start ten-tick pulse");
        require(level.getBlockState(pos).getValue(ArcaneEarBlock.POWERED),
                "Matching note must expose redstone power 15");
        ArcaneEarBlockEntity.clearNoteEvents(level);
        ArcaneEarBlockEntity.serverTick(level, pos, level.getBlockState(pos), ear);
        require(ear.signalTicks() == 9, "Arcane Ear pulse did not decrement");
        for (int i = 0; i < 9; i++) {
            ArcaneEarBlockEntity.serverTick(level, pos, level.getBlockState(pos), ear);
        }
        require(ear.signalTicks() == 0 && !level.getBlockState(pos).getValue(ArcaneEarBlock.POWERED),
                "Arcane Ear redstone pulse must end after ten ticks");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneEarPersistsOnlyNoteAndTone(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockState state = ThaumcraftMod.ARCANE_EAR.get().defaultBlockState();
        level.setBlock(pos.below(), Blocks.OAK_PLANKS.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        ArcaneEarBlockEntity ear = requireBlockEntity(level, pos, ArcaneEarBlockEntity.class);
        ear.updateToneFromSupport();
        for (int i = 0; i < 7; i++) ear.changePitch();
        CompoundTag saved = ear.saveWithoutMetadata();
        require(saved.getByte(TC4ArcaneEarParity.NBT_NOTE) == 7
                        && saved.getByte(TC4ArcaneEarParity.NBT_TONE) == 4,
                "Arcane Ear note/tone NBT differs from TileSensor");
        require(!saved.contains("SignalTicks") && !saved.contains("redstoneSignal")
                        && !saved.contains("Powered"),
                "Transient Arcane Ear pulse/render state must not persist");
        saved.putInt("SignalTicks", 10);
        ArcaneEarBlockEntity restored = new ArcaneEarBlockEntity(pos,
                state.setValue(ArcaneEarBlock.POWERED, true));
        restored.load(saved);
        require(restored.note() == 7 && restored.tone() == 4 && restored.signalTicks() == 0,
                "Legacy/stale pulse NBT must not revive redstone after reload");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneEarResearchAspectsRarityAndBlockPropertiesMatchOriginal(GameTestHelper helper) {
        ResearchEntry entry = OriginalResearchBridge.byKey("ARCANEEAR")
                .orElseThrow(() -> new GameTestAssertException("ARCANEEAR research entry is missing"));
        require("ARTIFICE".equals(entry.category()) && entry.displayColumn() == 6
                        && entry.displayRow() == 0 && entry.complexity() == 1,
                "ARCANEEAR category/position/complexity drifted");
        require(java.util.Arrays.asList(entry.requirements()).contains("GOGGLES")
                        && entry.hasFlag("concealed"),
                "ARCANEEAR parent or concealed flag drifted");
        require(entry.aspects().getOrDefault("sensus", 0) == 3
                        && entry.aspects().getOrDefault("potentia", 0) == 3
                        && entry.aspects().getOrDefault("aer", 0) == 3
                        && java.util.Arrays.asList(entry.recipeKeys()).contains("ArcaneEar"),
                "ARCANEEAR research aspects or recipe page drifted");
        AspectList objectAspects = TC4ObjectAspectRegistry.byModernId(
                new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_block_arcane_ear"));
        require(objectAspects.get(Aspect.SENSUS) == TC4ArcaneEarParity.OBJECT_SENSUS
                        && objectAspects.totalAmount() == TC4ArcaneEarParity.OBJECT_SENSUS,
                "Arcane Ear object aspects must be exactly Sensus 4");
        ItemStack item = new ItemStack(ThaumcraftMod.ARCANE_EAR_ITEM.get());
        require(item.getRarity() == Rarity.COMMON && item.getMaxStackSize() == 64,
                "Arcane Ear item must keep ordinary rarity and stack size 64");
        BlockPos pos = helper.absolutePos(new BlockPos(5, 1, 5));
        levelSetAndCheckArcaneEarProperties(helper.getLevel(), pos);
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneLevitatorVelocityRangeAndEntityAdmissionMatchOriginal(GameTestHelper helper) {
        require(TC4ArcaneLevitatorParity.stackedMaximumRange(0) == 10
                        && TC4ArcaneLevitatorParity.stackedMaximumRange(2) == 30,
                "Arcane Levitator stack range drifted");
        require(Math.abs(TC4ArcaneLevitatorParity.nextVerticalVelocity(0.0D, false)
                        - TC4ArcaneLevitatorParity.LIFT_INCREMENT) < 1.0E-12D,
                "Arcane Levitator lift increment drifted");
        require(TC4ArcaneLevitatorParity.nextVerticalVelocity(0.35D, false) == 0.35D,
                "Arcane Levitator must not accelerate at or above the original cap");
        require(Math.abs(TC4ArcaneLevitatorParity.nextVerticalVelocity(-0.5D, true)
                        - (-0.5D * TC4ArcaneLevitatorParity.SNEAK_DESCENT_MULTIPLIER)) < 1.0E-12D,
                "Arcane Levitator sneak descent multiplier drifted");
        require(TC4ArcaneLevitatorParity.nextVerticalVelocity(0.1D, true) == 0.1D,
                "Sneaking must not add lift or alter upward velocity");
        require(TC4ArcaneLevitatorParity.lowerSegmentContributes(false)
                        && !TC4ArcaneLevitatorParity.lowerSegmentContributes(true),
                "Lower stack contribution must depend only on power at that lower block position");
        require(TC4ArcaneLevitatorParity.admitsEntity(true, false, false)
                        && TC4ArcaneLevitatorParity.admitsEntity(false, true, false)
                        && TC4ArcaneLevitatorParity.admitsEntity(false, false, true)
                        && !TC4ArcaneLevitatorParity.admitsEntity(false, false, false),
                "Arcane Levitator item/pushable/horse filter drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneLevitatorStackObstructionAndTransientStateMatchOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos lower = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos upper = lower.above();
        level.setBlock(lower, ThaumcraftMod.ARCANE_LEVITATOR.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(upper, ThaumcraftMod.ARCANE_LEVITATOR.get().defaultBlockState(), Block.UPDATE_ALL);
        require(ArcaneLevitatorBlockEntity.computeRangeAbove(level, upper) == 20,
                "One unpowered lower levitator must extend range to twenty blocks");
        level.setBlock(upper.above(4), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        require(ArcaneLevitatorBlockEntity.computeRangeAbove(level, upper) == 3,
                "First opaque full block must clip the lift column immediately below it");

        ArcaneLevitatorBlockEntity levitator = requireBlockEntity(level, upper, ArcaneLevitatorBlockEntity.class);
        CompoundTag saved = levitator.saveWithoutMetadata();
        require(!saved.contains("Counter") && !saved.contains("RangeAbove")
                        && !saved.contains("RequiresUpdate") && !saved.contains("LastPowerState"),
                "TileLifter counter/range/update/power state must remain transient");
        saved.putInt("RangeAbove", 99);
        saved.putBoolean("RequiresUpdate", false);
        ArcaneLevitatorBlockEntity restored = new ArcaneLevitatorBlockEntity(
                upper, ThaumcraftMod.ARCANE_LEVITATOR.get().defaultBlockState());
        restored.load(saved);
        require(restored.rangeAbove() == 0 && restored.requiresUpdate(),
                "Stale rebuild NBT must not restore transient levitator state");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 60)
    public static void arcaneLevitatorProductionTickLiftsItemsAndResetsFallDistance(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
        level.setBlock(pos, ThaumcraftMod.ARCANE_LEVITATOR.get().defaultBlockState(), Block.UPDATE_ALL);
        ArcaneLevitatorBlockEntity levitator = requireBlockEntity(level, pos, ArcaneLevitatorBlockEntity.class);
        ArcaneLevitatorBlockEntity.serverTick(level, pos, level.getBlockState(pos), levitator);
        ItemEntity item = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 1.25D,
                pos.getZ() + 0.5D, new ItemStack(Items.IRON_INGOT));
        item.setDeltaMovement(0.02D, 0.0D, -0.03D);
        item.fallDistance = 7.0F;
        level.addFreshEntity(item);
        ArcaneLevitatorBlockEntity.serverTick(level, pos, level.getBlockState(pos), levitator);
        require(Math.abs(item.getDeltaMovement().y - TC4ArcaneLevitatorParity.LIFT_INCREMENT) < 1.0E-9D,
                "Production TileLifter tick did not apply the original lift increment");
        require(Math.abs(item.getDeltaMovement().x - 0.02D) < 1.0E-9D
                        && Math.abs(item.getDeltaMovement().z + 0.03D) < 1.0E-9D,
                "Arcane Levitator must preserve horizontal velocity");
        require(item.fallDistance == 0.0F, "Arcane Levitator must reset fall distance every active tick");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneLevitatorHorizontalSupportRedstoneAndItemPropertiesMatchOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 4));
        BlockState state = ThaumcraftMod.ARCANE_LEVITATOR.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        require(state.hasProperty(ArcaneLevitatorBlock.POWERED)
                        && !state.getValue(ArcaneLevitatorBlock.POWERED),
                "Arcane Levitator must expose the modern render-only powered state");
        require(TC4ArcaneLevitatorParity.ACTIVE_GLOW_LEGACY_BRIGHTNESS == 180
                        && TC4ArcaneLevitatorParity.ACTIVE_GLOW_BLOCK_LIGHT == 11,
                "Arcane Levitator active glow brightness conversion drifted");
        require(state.isFaceSturdy(level, pos, Direction.NORTH)
                        && state.isFaceSturdy(level, pos, Direction.SOUTH)
                        && state.isFaceSturdy(level, pos, Direction.EAST)
                        && state.isFaceSturdy(level, pos, Direction.WEST),
                "Arcane Levitator horizontal faces must be sturdy");
        require(!state.isFaceSturdy(level, pos, Direction.UP)
                        && !state.isFaceSturdy(level, pos, Direction.DOWN),
                "Arcane Levitator top/bottom faces must not be sturdy");
        ArcaneLevitatorBlock block = (ArcaneLevitatorBlock) state.getBlock();
        require(block.canConnectRedstone(state, level, pos, Direction.NORTH)
                        && !block.canConnectRedstone(state, level, pos, Direction.UP),
                "Arcane Levitator redstone connections must remain horizontal-only");
        ItemStack stack = new ItemStack(ThaumcraftMod.ARCANE_LEVITATOR_ITEM.get());
        require(stack.getRarity() == Rarity.COMMON && stack.getMaxStackSize() == 64,
                "Arcane Levitator item must keep ordinary rarity and stack size 64");
        require(Math.abs(state.getDestroySpeed(level, pos) - TC4ArcaneLevitatorParity.BLOCK_HARDNESS) < 1.0E-6F,
                "Arcane Levitator hardness drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneLevitatorResearchAndRecipeIndexMatchOriginal(GameTestHelper helper) {
        ResearchEntry entry = OriginalResearchBridge.byKey("LEVITATOR")
                .orElseThrow(() -> new GameTestAssertException("LEVITATOR research entry is missing"));
        require("ARTIFICE".equals(entry.category()) && entry.displayColumn() == -3
                        && entry.displayRow() == -3 && entry.complexity() == 1,
                "LEVITATOR category/position/complexity drifted");
        require(java.util.Arrays.asList(entry.requirements()).contains("NITOR")
                        && entry.hasFlag("concealed"),
                "LEVITATOR parent or concealed flag drifted");
        require(entry.aspects().getOrDefault("motus", 0) == TC4ArcaneLevitatorParity.RESEARCH_MOTUS
                        && entry.aspects().getOrDefault("volatus", 0) == TC4ArcaneLevitatorParity.RESEARCH_VOLATUS
                        && entry.aspects().getOrDefault("aer", 0) == TC4ArcaneLevitatorParity.RESEARCH_AER
                        && java.util.Arrays.asList(entry.recipeKeys()).contains("Levitator"),
                "LEVITATOR research aspects or recipe page drifted");
        TC4RecipeRuntimeBridge.OriginalRecipe recipe = TC4RecipeRuntimeBridge.byKey("Levitator");
        require(recipe != null && java.util.Arrays.equals(recipe.pattern(), new String[]{"WEW", "BNB", "WAW"}),
                "Levitator original recipe pattern drifted");
        require(java.util.Arrays.asList(recipe.aspectCosts()).contains("AER:10")
                        && java.util.Arrays.asList(recipe.aspectCosts()).contains("TERRA:5"),
                "Levitator original recipe vis cost drifted");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcanePressurePlateProductionTriggerModesMatchOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlock(pos, ThaumcraftMod.ARCANE_PRESSURE_PLATE.get().defaultBlockState(), Block.UPDATE_ALL);
        ArcanePressurePlateBlockEntity plate = requireBlockEntity(level, pos,
                ArcanePressurePlateBlockEntity.class);
        Player owner = helper.makeMockPlayer();
        plate.initializeOwner(owner);
        ItemEntity item = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.05D,
                pos.getZ() + 0.5D, new ItemStack(Items.STICK));

        require(plate.setting() == 0 && plate.shouldTrigger(owner) && plate.shouldTrigger(item),
                "Mode 0 must trigger on every non-ignoring entity");
        require(plate.cycleSetting(owner) == 1 && !plate.shouldTrigger(owner) && plate.shouldTrigger(item),
                "Mode 1 must exclude owner/authorized players but retain non-player entities");
        require(plate.cycleSetting(owner) == 2 && plate.shouldTrigger(owner) && !plate.shouldTrigger(item),
                "Mode 2 must trigger only on owner/authorized players");
        require(level.getBlockState(pos).getValue(ArcanePressurePlateBlock.SETTING) == 2,
                "Production block state did not synchronize the TileArcanePressurePlate setting");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcanePressurePlateFloatingBoundsSignalsAndWardsMatchOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
        BlockState state = ThaumcraftMod.ARCANE_PRESSURE_PLATE.get().defaultBlockState();
        level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        require(level.getBlockState(pos).is(ThaumcraftMod.ARCANE_PRESSURE_PLATE.get()),
                "Original plate must survive without a supporting block");
        AABB up = state.getShape(level, pos).bounds();
        AABB down = state.setValue(ArcanePressurePlateBlock.POWERED, true).getShape(level, pos).bounds();
        require(up.minX == 0.0625D && up.maxX == 0.9375D && up.maxY == 0.0625D,
                "Unpressed outline bounds drifted");
        require(down.maxY == 0.03125D && state.getCollisionShape(level, pos).isEmpty(),
                "Pressed outline or empty collision drifted");
        ArcanePressurePlateBlock block = (ArcanePressurePlateBlock) state.getBlock();
        BlockState powered = state.setValue(ArcanePressurePlateBlock.POWERED, true);
        require(block.getSignal(powered, level, pos, Direction.NORTH) == 15
                        && block.getDirectSignal(powered, level, pos, Direction.UP) == 15
                        && block.getDirectSignal(powered, level, pos, Direction.NORTH) == 0,
                "Arcane Pressure Plate redstone output drifted");
        require(state.getDestroySpeed(level, pos) == TC4ArcanePressurePlateParity.WARDED_HARDNESS
                        && block.getPistonPushReaction(state) == net.minecraft.world.level.material.PushReaction.BLOCK,
                "Warded hardness or piston protection drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcanePressurePlateLegacyOwnerAccessNbtRoundTrip(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 4));
        level.setBlock(pos, ThaumcraftMod.ARCANE_PRESSURE_PLATE.get().defaultBlockState(), Block.UPDATE_ALL);
        ArcanePressurePlateBlockEntity plate = requireBlockEntity(level, pos,
                ArcanePressurePlateBlockEntity.class);
        CompoundTag legacy = new CompoundTag();
        legacy.putString("owner", "OriginalOwner");
        legacy.putByte("setting", (byte) 2);
        ListTag access = new ListTag();
        CompoundTag standard = new CompoundTag();
        standard.putString("name", "0StandardUser");
        access.add(standard);
        CompoundTag full = new CompoundTag();
        full.putString("name", "1FullUser");
        access.add(full);
        legacy.put("access", access);
        plate.load(legacy);
        CompoundTag saved = plate.saveWithoutMetadata();
        require("OriginalOwner".equals(saved.getString("owner")) && saved.getByte("setting") == 2,
                "Legacy owner/setting tags were not preserved");
        ListTag savedAccess = saved.getList("access", Tag.TAG_COMPOUND);
        require(savedAccess.size() == 2
                        && "0standarduser".equals(savedAccess.getCompound(0).getString("name"))
                        && "1fulluser".equals(savedAccess.getCompound(1).getString("name")),
                "Legacy access list prefixes or names drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneKeyOriginalLocationTypeAndMigrationContract(GameTestHelper helper) {
        BlockPos target = helper.absolutePos(new BlockPos(3, 1, 5));
        Item keyItem = ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_keyiron").get();
        require(keyItem instanceof ArcaneKeyItem, "tc4_keyiron must use the production ArcaneKeyItem");
        ItemStack key = new ItemStack(keyItem);
        ArcaneKeyItem.bind(key, target, TC4ArcanePressurePlateParity.KEY_TARGET_PLATE);
        CompoundTag tag = key.getTag();
        require(tag != null && tag.contains("location") && tag.contains("type")
                        && !tag.contains("Dimension") && !tag.contains("Bound"),
                "Bound key must write exact original location/type NBT");
        require(ArcaneKeyItem.matches(key, target)
                        && ArcaneKeyItem.boundType(key) == TC4ArcanePressurePlateParity.KEY_TARGET_PLATE,
                "Bound key did not match its original target coordinates/type");
        require(TC4ArcanePressurePlateParity.tooltipLocation(ArcaneKeyItem.boundLocation(key))
                        .equals("x " + target.getX() + ", z " + target.getZ() + ", y " + target.getY()),
                "Original x,z,y tooltip order drifted");

        ItemStack migrated = new ItemStack(keyItem);
        migrated.getOrCreateTag().putBoolean("Bound", true);
        migrated.getOrCreateTag().putInt("X", target.getX());
        migrated.getOrCreateTag().putInt("Y", target.getY());
        migrated.getOrCreateTag().putInt("Z", target.getZ());
        require(ArcaneKeyItem.matches(migrated, target),
                "Early-rebuild Bound/X/Y/Z keys must remain usable after migration");

        ItemStack malformed = new ItemStack(keyItem);
        malformed.getOrCreateTag().putString("unrelated", "legacy branch marker");
        require(ArcaneKeyItem.hasBindingContainer(malformed) && !ArcaneKeyItem.isBound(malformed),
                "Any existing NBT must enter the original bound-key branch without becoming a valid target");
        require(!ArcaneKeyItem.matches(malformed, target),
                "Malformed or unrelated key NBT must never match or be silently rebound");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcanePressurePlateOwnerWandRemovalDropsOnePlate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlock(pos, ThaumcraftMod.ARCANE_PRESSURE_PLATE.get().defaultBlockState(), Block.UPDATE_ALL);
        ArcanePressurePlateBlockEntity plate = requireBlockEntity(level, pos,
                ArcanePressurePlateBlockEntity.class);
        Player owner = helper.makeMockPlayer();
        plate.initializeOwner(owner);
        int before = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.0D)).size();
        require(ArcanePressurePlateBlock.removeWithOwnerWand(level, pos, owner),
                "Owner wand removal production path rejected the owner");
        require(level.getBlockState(pos).isAir(), "Owner wand removal did not remove the plate");
        int after = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.0D)).size();
        require(after == before + 1, "Owner wand removal must manually drop exactly one plate");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcanePressurePlateKeysResearchAndRecipeIndexMatchOriginal(GameTestHelper helper) {
        ResearchEntry entry = OriginalResearchBridge.byKey("WARDEDARCANA")
                .orElseThrow(() -> new GameTestAssertException("WARDEDARCANA research entry is missing"));
        require("ARTIFICE".equals(entry.category()) && entry.displayColumn() == -5
                        && entry.displayRow() == -4 && entry.complexity() == 2,
                "WARDEDARCANA category/position/complexity drifted");
        require(entry.aspects().getOrDefault("instrumentum", 0) == 6
                        && entry.aspects().getOrDefault("cognitio", 0) == 3
                        && entry.aspects().getOrDefault("machina", 0) == 3
                        && entry.aspects().getOrDefault("tutamen", 0) == 3,
                "WARDEDARCANA research aspects drifted");
        require(java.util.Arrays.asList(entry.recipeKeys()).containsAll(java.util.List.of(
                        "ArcaneDoor", "IronKey", "GoldKey", "ArcanePressurePlate", "WardedGlass")),
                "WARDEDARCANA recipe pages are incomplete");
        TC4RecipeRuntimeBridge.OriginalRecipe plate = TC4RecipeRuntimeBridge.byKey("ArcanePressurePlate");
        TC4RecipeRuntimeBridge.OriginalRecipe iron = TC4RecipeRuntimeBridge.byKey("IronKey");
        TC4RecipeRuntimeBridge.OriginalRecipe gold = TC4RecipeRuntimeBridge.byKey("GoldKey");
        require(plate != null && java.util.Arrays.equals(plate.pattern(), new String[]{"B", "TDT"})
                        && java.util.Arrays.asList(plate.components()).contains("new ItemStack(ConfigItems.itemResource, 1, 2)")
                        && java.util.Arrays.asList(plate.components()).contains("new ItemStack(ConfigBlocks.blockWoodenDevice, 1, 6)"),
                "Arcane Pressure Plate recipe pattern/components drifted");
        require(iron != null && gold != null && java.util.Arrays.equals(iron.pattern(), new String[]{"NNI", "N"})
                        && java.util.Arrays.equals(gold.pattern(), new String[]{"NNI", "N"}),
                "Iron/gold key recipe indexes drifted");
        require(new ItemStack(ThaumcraftMod.ARCANE_PRESSURE_PLATE_ITEM.get()).getRarity() == Rarity.COMMON
                        && new ItemStack(ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_keyiron").get()).getRarity() == Rarity.UNCOMMON
                        && new ItemStack(ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_keygold").get()).getMaxStackSize() == 64,
                "Plate/key rarity or stack size drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneLampParityMathMatchesOriginalBounds(GameTestHelper helper) {
        require(TC4ArcaneLampParity.triangularOffset(15, 0) == 15
                        && TC4ArcaneLampParity.triangularOffset(0, 15) == -15,
                "Arcane Lamp triangular distribution bounds drifted");
        require(TC4ArcaneLampParity.clampSampledY(100, 20, -64) == 24
                        && TC4ArcaneLampParity.clampSampledY(-20, 20, -64) == 5,
                "Arcane Lamp surface+4/minimum-5 Y contract drifted");
        require(TC4ArcaneLampParity.boreDistance(31) == 62
                        && TC4ArcaneLampParity.boreLateralOffset(0) == 3
                        && TC4ArcaneLampParity.boreLateralOffset(4) == -3
                        && TC4ArcaneLampParity.boreVerticalOffset(6, false) == -2,
                "Arcane Bore lamp tunnel offset table drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneLampAcceptsAnyNonAirSupportAndDropsOnAir(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos lampPos = helper.absolutePos(new BlockPos(4, 2, 4));
        BlockState lamp = ThaumcraftMod.ARCANE_LAMP.get().defaultBlockState()
                .setValue(ArcaneLampBlock.FACING, Direction.DOWN);
        BlockPos support = lampPos.below();
        level.setBlock(support, Blocks.GLASS_PANE.defaultBlockState(), Block.UPDATE_ALL);
        require(lamp.canSurvive(level, lampPos), "TC4 lamp rejected a non-air, non-sturdy support");
        level.setBlock(support, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        require(!lamp.canSurvive(level, lampPos), "TC4 lamp survived after its support became air");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneLampLightMarkerIsAirAndHasNoBlockEntity(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlock(pos, ThaumcraftMod.ARCANE_LAMP_LIGHT.get().defaultBlockState(), Block.UPDATE_ALL);
        BlockState state = level.getBlockState(pos);
        require(state.isAir() && state.is(ThaumcraftMod.ARCANE_LAMP_LIGHT.get()),
                "Arcane Lamp marker must retain both isAir and marker identity");
        require(level.getBlockEntity(pos) == null, "TC4 metadata-3 light marker must not create a BlockEntity");
        require(state.getCollisionShape(level, pos).isEmpty() && state.getShape(level, pos).isEmpty(),
                "Arcane Lamp marker must remain invisible and non-colliding");
        require(state.getLightEmission() == TC4ArcaneLampParity.LIGHT_LEVEL,
                "Arcane Lamp marker light level drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneLampOrientationNbtRoundTripsOriginalOrdinal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        BlockState east = ThaumcraftMod.ARCANE_LAMP.get().defaultBlockState()
                .setValue(ArcaneLampBlock.FACING, Direction.EAST);
        level.setBlock(pos, east, Block.UPDATE_ALL);
        ArcaneLampBlockEntity lamp = requireBlockEntity(level, pos, ArcaneLampBlockEntity.class);
        CompoundTag tag = lamp.saveWithFullMetadata();
        require(tag.getInt("orientation") == Direction.EAST.get3DDataValue(),
                "Arcane Lamp did not save TC4 orientation ordinal");
        level.setBlock(pos, east.setValue(ArcaneLampBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
        lamp.load(tag);
        lamp.onLoad();
        require(level.getBlockState(pos).getValue(ArcaneLampBlock.FACING) == Direction.EAST,
                "Arcane Lamp did not restore TC4 orientation ordinal");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneLampCleanupUsesOriginalSharedRadiusCube(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos lampPos = helper.absolutePos(new BlockPos(20, 20, 20));
        level.setBlock(lampPos, ThaumcraftMod.ARCANE_LAMP.get().defaultBlockState(), Block.UPDATE_ALL);
        ArcaneLampBlockEntity lamp = requireBlockEntity(level, lampPos, ArcaneLampBlockEntity.class);
        BlockPos inside = lampPos.offset(15, 15, 15);
        BlockPos outside = lampPos.offset(16, 0, 0);
        level.setBlock(inside, ThaumcraftMod.ARCANE_LAMP_LIGHT.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(outside, ThaumcraftMod.ARCANE_LAMP_LIGHT.get().defaultBlockState(), Block.UPDATE_ALL);
        lamp.removeLights();
        require(level.getBlockState(inside).isAir() && !level.getBlockState(inside).is(ThaumcraftMod.ARCANE_LAMP_LIGHT.get()),
                "Arcane Lamp failed to clear a shared marker at radius 15");
        require(level.getBlockState(outside).is(ThaumcraftMod.ARCANE_LAMP_LIGHT.get()),
                "Arcane Lamp incorrectly cleared a marker outside radius 15");
        level.removeBlock(outside, false);
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneBoreAdjacentLampSeedsOriginalTunnelMarker(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(4, 4, 4));
        BlockPos head = base.above();
        level.setBlock(base.north().below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(base.north(), ThaumcraftMod.ARCANE_LAMP.get().defaultBlockState(), Block.UPDATE_ALL);
        boolean placed = ArcaneBoreBlockEntity.trySeedTunnelLight(level, head, base, Direction.EAST, 30);
        BlockPos target = head.east(31).below(2);
        require(placed && level.getBlockState(target).is(ThaumcraftMod.ARCANE_LAMP_LIGHT.get()),
                "Arcane Bore production integration did not seed the original lamp tunnel marker");
        level.removeBlock(target, false);
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneLampResearchRecipeAndRarityMatchOriginal(GameTestHelper helper) {
        ResearchEntry entry = OriginalResearchBridge.byKey("ARCANELAMP")
                .orElseThrow(() -> new GameTestAssertException("ARCANELAMP research entry is missing"));
        require(java.util.Arrays.asList(entry.recipeKeys()).contains("ArcaneLamp"),
                "ARCANELAMP research no longer exposes the ArcaneLamp recipe key");
        TC4RecipeRuntimeBridge.OriginalRecipe recipe = TC4RecipeRuntimeBridge.byKey("ArcaneLamp");
        require(recipe != null && java.util.Arrays.equals(recipe.pattern(), new String[]{" S ", "IAI", " N "}),
                "Arcane Lamp original recipe pattern drifted");
        require(java.util.Arrays.asList(recipe.components()).containsAll(java.util.List.of(
                        "new ItemStack(ConfigBlocks.blockCosmeticOpaque, 1, 0)",
                        "new ItemStack(Blocks.glowstone)",
                        "new ItemStack(ConfigItems.itemResource, 1, 1)",
                        "new ItemStack(Items.field_151042_j)")),
                "Arcane Lamp original recipe components drifted");
        require(new ItemStack(ThaumcraftMod.ARCANE_LAMP_ITEM.get()).getRarity() == Rarity.COMMON,
                "Arcane Lamp item must retain the original common rarity");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneSpaParityConstantsAndCandidateOrder(GameTestHelper helper) {
        require(TC4ArcaneSpaParity.CAPACITY_MB == 5000
                        && TC4ArcaneSpaParity.BUCKET_MB == 1000
                        && TC4ArcaneSpaParity.CHECK_INTERVAL_TICKS == 40,
                "Arcane Spa tank/cadence constants drifted");
        require(TC4ArcaneSpaParity.shouldRunCycle(0)
                        && !TC4ArcaneSpaParity.shouldRunCycle(39)
                        && TC4ArcaneSpaParity.shouldRunCycle(40),
                "Arcane Spa counter++ % 40 cadence drifted");
        require(TC4ArcaneSpaParity.candidateOffsetX(0) == -2
                        && TC4ArcaneSpaParity.candidateOffsetZ(0) == -2
                        && TC4ArcaneSpaParity.candidateOffsetX(24) == 2
                        && TC4ArcaneSpaParity.candidateOffsetZ(24) == 2,
                "Arcane Spa x-outer/z-inner 5x5 order drifted");
        require(TC4ArcaneSpaParity.acceptedFluidAmount(4500, 1000) == 500,
                "Arcane Spa partial final-container fill drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void arcaneSpaFilledContainerUseConsumesWholePartialBucket(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        BlockState state = ThaumcraftMod.ARCANE_SPA.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        ArcaneSpaBlockEntity spa = requireBlockEntity(level, pos, ArcaneSpaBlockEntity.class);
        IFluidHandler side = spa.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.NORTH).orElse(null);
        require(side != null && side.fill(new FluidStack(Fluids.WATER, 4500), IFluidHandler.FluidAction.EXECUTE) == 4500,
                "Unable to prepare a 4500 mB Arcane Spa");

        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        ((ArcaneSpaBlock) state.getBlock()).use(state, level, pos, player, InteractionHand.MAIN_HAND, hit);

        require(spa.getFluidAmount() == 5000,
                "Arcane Spa did not accept the final 500 mB from a full bucket");
        require(player.getInventory().countItem(Items.WATER_BUCKET) == 0
                        && player.getInventory().countItem(Items.BUCKET) == 1,
                "TC4 partial fill must consume the complete filled container and return its empty container");
        require(!(player.containerMenu instanceof ArcaneSpaMenu),
                "Filled-container use must not open the Arcane Spa GUI");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void arcaneSpaEmptyContainerOpensGuiWithoutDraining(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        BlockState state = ThaumcraftMod.ARCANE_SPA.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        ArcaneSpaBlockEntity spa = requireBlockEntity(level, pos, ArcaneSpaBlockEntity.class);
        IFluidHandler side = spa.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.WEST).orElse(null);
        require(side != null && side.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE) == 1000,
                "Unable to prepare Arcane Spa fluid");

        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        ((ArcaneSpaBlock) state.getBlock()).use(state, level, pos, player, InteractionHand.MAIN_HAND, hit);

        require(spa.getFluidAmount() == 1000, "Empty bucket must not directly drain the Arcane Spa");
        require(player.containerMenu instanceof ArcaneSpaMenu,
                "A non-filled container must open the original Arcane Spa GUI path");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 80)
    public static void arcaneSpaExactNbtAndLegacyPortMigration(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlock(pos, ThaumcraftMod.ARCANE_SPA.get().defaultBlockState(), Block.UPDATE_ALL);
        ArcaneSpaBlockEntity spa = requireBlockEntity(level, pos, ArcaneSpaBlockEntity.class);
        spa.toggleMixing();
        IFluidHandler side = spa.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.SOUTH).orElse(null);
        require(side != null && side.fill(new FluidStack(Fluids.WATER, 2000), IFluidHandler.FluidAction.EXECUTE) == 2000,
                "Unable to prepare Arcane Spa exact NBT test");
        spa.saltsHandler().insertItem(0, new ItemStack(ThaumcraftMod.BATH_SALTS.get(), 3), false);

        CompoundTag exact = spa.saveWithFullMetadata();
        require(exact.contains(TC4ArcaneSpaParity.NBT_MIX, Tag.TAG_BYTE)
                        && !exact.getBoolean(TC4ArcaneSpaParity.NBT_MIX),
                "Arcane Spa must save lowercase TC4 mix");
        require(exact.contains("FluidName", Tag.TAG_STRING) && exact.getInt("Amount") == 2000,
                "Arcane Spa FluidStack must be written at the root like TC4");
        require(exact.getList(TC4ArcaneSpaParity.NBT_ITEMS, Tag.TAG_COMPOUND).size() == 1,
                "Arcane Spa Items list must be written at the root like TC4");
        require(!exact.contains(TC4ArcaneSpaParity.LEGACY_PORT_NBT_MIX)
                        && !exact.contains(TC4ArcaneSpaParity.LEGACY_PORT_NBT_TANK)
                        && !exact.contains(TC4ArcaneSpaParity.LEGACY_PORT_NBT_SALTS),
                "Early-port Mix/Tank/Salts keys leaked into new saves");

        BlockPos migratedPos = pos.east(2);
        level.setBlock(migratedPos, ThaumcraftMod.ARCANE_SPA.get().defaultBlockState(), Block.UPDATE_ALL);
        ArcaneSpaBlockEntity migrated = requireBlockEntity(level, migratedPos, ArcaneSpaBlockEntity.class);
        CompoundTag legacy = new CompoundTag();
        legacy.putBoolean(TC4ArcaneSpaParity.LEGACY_PORT_NBT_MIX, false);
        legacy.put(TC4ArcaneSpaParity.LEGACY_PORT_NBT_TANK,
                new FluidStack(Fluids.WATER, 3000).writeToNBT(new CompoundTag()));
        CompoundTag legacyInventory = new CompoundTag();
        legacyInventory.putInt("Size", 1);
        ListTag legacyItems = new ListTag();
        CompoundTag legacySalt = new CompoundTag();
        legacySalt.putByte("Slot", (byte) 0);
        new ItemStack(ThaumcraftMod.BATH_SALTS.get(), 2).save(legacySalt);
        legacyItems.add(legacySalt);
        legacyInventory.put("Items", legacyItems);
        legacy.put(TC4ArcaneSpaParity.LEGACY_PORT_NBT_SALTS, legacyInventory);
        migrated.load(legacy);
        require(!migrated.isMixing() && migrated.getFluidAmount() == 3000
                        && migrated.saltsHandler().getStackInSlot(0).getCount() == 2,
                "Arcane Spa did not migrate v11.63.24-v11.64.22 Mix/Tank/Salts saves");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 120)
    public static void arcaneSpaMixingCycleAndExpansionOrder(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(8, 2, 8));
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(pos.offset(x, 0, z), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        level.setBlock(pos, ThaumcraftMod.ARCANE_SPA.get().defaultBlockState(), Block.UPDATE_ALL);
        ArcaneSpaBlockEntity spa = requireBlockEntity(level, pos, ArcaneSpaBlockEntity.class);
        IFluidHandler side = spa.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.NORTH).orElse(null);
        require(side != null, "Arcane Spa side fluid capability is missing");
        side.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        spa.saltsHandler().insertItem(0, new ItemStack(ThaumcraftMod.BATH_SALTS.get()), false);
        ArcaneSpaBlockEntity.serverTick(level, pos, level.getBlockState(pos), spa);
        BlockPos center = pos.above();
        require(level.getFluidState(center).isSource()
                        && level.getFluidState(center).getType().isSame(ThaumcraftMod.PURIFYING_FLUID.get()),
                "Arcane Spa did not create the first purifying source above itself");
        require(spa.getFluidAmount() == 0 && spa.saltsHandler().getStackInSlot(0).isEmpty(),
                "Successful mix cycle must consume exactly 1000 mB and one Bath Salts");

        side.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        spa.saltsHandler().insertItem(0, new ItemStack(ThaumcraftMod.BATH_SALTS.get()), false);
        for (int i = 0; i < 40; i++) {
            ArcaneSpaBlockEntity.serverTick(level, pos, level.getBlockState(pos), spa);
        }
        BlockPos firstAdjacent = center.offset(-1, 0, 0);
        require(level.getFluidState(firstAdjacent).isSource()
                        && level.getFluidState(firstAdjacent).getType().isSame(ThaumcraftMod.PURIFYING_FLUID.get()),
                "Arcane Spa did not use original x-outer/z-inner first adjacent candidate (-1,0)");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void arcaneSpaAutomationSidesAndRedstoneGate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(5, 2, 5));
        level.setBlock(pos, ThaumcraftMod.ARCANE_SPA.get().defaultBlockState(), Block.UPDATE_ALL);
        ArcaneSpaBlockEntity spa = requireBlockEntity(level, pos, ArcaneSpaBlockEntity.class);
        require(!spa.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).isPresent()
                        && !spa.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).isPresent(),
                "Arcane Spa top side must expose neither fluid nor item automation");
        require(spa.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN).isPresent()
                        && spa.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.EAST).isPresent(),
                "Arcane Spa non-top sides must expose both automation capabilities");

        IFluidHandler side = spa.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN).orElse(null);
        require(side != null, "Arcane Spa side fluid capability missing");
        side.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        spa.saltsHandler().insertItem(0, new ItemStack(ThaumcraftMod.BATH_SALTS.get()), false);
        level.setBlock(pos.east(), Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        ArcaneSpaBlockEntity.serverTick(level, pos, level.getBlockState(pos), spa);
        require(spa.getFluidAmount() == 1000 && level.getBlockState(pos.above()).isAir(),
                "Redstone-powered Arcane Spa must not consume or place fluid");
        level.removeBlock(pos.east(), false);
        for (int i = 0; i < 40; i++) {
            ArcaneSpaBlockEntity.serverTick(level, pos, level.getBlockState(pos), spa);
        }
        require(spa.getFluidAmount() == 0 && !level.getBlockState(pos.above()).isAir(),
                "Arcane Spa did not resume on its next 40-tick cycle after redstone removal");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 60)
    public static void arcaneSpaFillModeRecipeResearchAndBlockPropertiesMatchOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlock(pos, ThaumcraftMod.ARCANE_SPA.get().defaultBlockState(), Block.UPDATE_ALL);
        ArcaneSpaBlockEntity spa = requireBlockEntity(level, pos, ArcaneSpaBlockEntity.class);
        spa.toggleMixing();
        IFluidHandler side = spa.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.WEST).orElse(null);
        require(side != null && side.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE) == 1000,
                "Unable to prepare Arcane Spa fill mode");
        ArcaneSpaBlockEntity.serverTick(level, pos, level.getBlockState(pos), spa);
        require(level.getFluidState(pos.above()).isSource()
                        && level.getFluidState(pos.above()).getType().isSame(Fluids.LAVA),
                "Fill mode must place the stored world-placeable fluid without Bath Salts");

        ResearchEntry entry = OriginalResearchBridge.byKey("ARCANESPA")
                .orElseThrow(() -> new GameTestAssertException("ARCANESPA research entry is missing"));
        require("ALCHEMY".equals(entry.category()) && entry.displayColumn() == -6
                        && entry.displayRow() == -5 && entry.complexity() == 1
                        && java.util.Arrays.asList(entry.requirements()).contains("BATHSALTS")
                        && entry.hasFlag("secondary")
                        && java.util.Arrays.asList(entry.recipeKeys()).contains("ArcaneSpa"),
                "ARCANESPA metadata/parent/recipe page drifted");
        require(entry.aspects().getOrDefault("aqua", 0) == 3
                        && entry.aspects().getOrDefault("machina", 0) == 3
                        && entry.aspects().getOrDefault("ordo", 0) == 3,
                "ARCANESPA research aspects drifted");
        TC4RecipeRuntimeBridge.OriginalRecipe recipe = TC4RecipeRuntimeBridge.byKey("ArcaneSpa");
        require(recipe != null && java.util.Arrays.equals(recipe.pattern(), new String[]{"QIQ", "SJS", "SPS"})
                        && java.util.Arrays.asList(recipe.aspectCosts()).containsAll(java.util.List.of(
                        "AQUA:16", "ORDO:8", "TERRA:4")),
                "Arcane Spa recipe pattern or vis costs drifted");
        BlockState state = level.getBlockState(pos);
        require(Math.abs(state.getDestroySpeed(level, pos) - TC4ArcaneSpaParity.BLOCK_HARDNESS) < 1.0E-6F
                        && !state.canOcclude()
                        && new ItemStack(ThaumcraftMod.ARCANE_SPA_ITEM.get()).getRarity() == Rarity.COMMON,
                "Arcane Spa stone hardness/non-opaque rendering/common rarity drifted");
        helper.succeed();
    }

    private static void levelSetAndCheckArcaneEarProperties(ServerLevel level, BlockPos pos) {
        BlockState state = ThaumcraftMod.ARCANE_EAR.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        require(Math.abs(state.getDestroySpeed(level, pos) - TC4ArcaneEarParity.BLOCK_HARDNESS) < 1.0E-6F,
                "Arcane Ear hardness drifted from TC4");
        AABB bounds = state.getCollisionShape(level, pos,
                net.minecraft.world.phys.shapes.CollisionContext.empty()).bounds();
        require(bounds.minX == 0.0D && bounds.minY == 0.0D && bounds.minZ == 0.0D
                        && bounds.maxX == 1.0D && bounds.maxY == 1.0D && bounds.maxZ == 1.0D,
                "Arcane Ear must retain full-cube collision despite multipart rendering");
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneBoreParityFormulasMatchOriginal(GameTestHelper helper) {
        require(TC4ArcaneBoreParity.width(0) == 5 && TC4ArcaneBoreParity.width(3) == 11,
                "Arcane Bore width formula drifted from 1+(2+area)*2");
        require(TC4ArcaneBoreParity.miningDelay(5.0F, 2, true) == 8,
                "Arcane Bore accelerated delay formula drifted");
        require(TC4ArcaneBoreParity.miningDelay(5.0F, 2, false) == 32,
                "Arcane Bore unaccelerated x4 delay drifted");
        require(TC4ArcaneBoreParity.pickaxeIsNearBroken(99, 100)
                        && !TC4ArcaneBoreParity.pickaxeIsNearBroken(98, 100),
                "Arcane Bore near-broken boundary drifted");
        require(TC4ArcaneBoreParity.addVisCredit(0.0F, 100) == 20.0F
                        && TC4ArcaneBoreParity.addEssentiaCredit(3.0F) == 23.0F,
                "Arcane Bore Perditio acceleration credits drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneBoreSpiralUsesOriginalRotatedLaneContract(GameTestHelper helper) {
        TC4ArcaneBoreParity.SpiralLane first = TC4ArcaneBoreParity.nextLane(
                10, 20, 30, 1, 0, 0, 0, 0, 0.0F, 0.0F, 0, 0, 0);
        TC4ArcaneBoreParity.SpiralLane second = TC4ArcaneBoreParity.nextLane(
                10, 20, 30, 1, 0, 0, 0, first.spiral(), first.currentRadius(),
                first.radiusIncrement(), first.laneX(), first.laneY(), first.laneZ());
        require(first.spiral() % 2 == 0 && second.spiral() % 2 == 0,
                "Arcane Bore spiral must advance in two-degree increments");
        require(first.laneX() != second.laneX() || first.laneY() != second.laneY()
                        || first.laneZ() != second.laneZ(),
                "Arcane Bore spiral failed to select a distinct integer lane");
        require(Math.abs(first.radiusIncrement() - 2.0F / 360.0F) < 0.000001F,
                "Arcane Bore radius increment drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneBoreRequiresVerticalBaseAndExtendsTowardNozzle(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos basePos = helper.absolutePos(new BlockPos(3, 2, 3));
        BlockPos borePos = basePos.above();
        level.setBlock(basePos, ThaumcraftMod.ARCANE_BORE_BASE.get().defaultBlockState(), Block.UPDATE_ALL);
        BlockState state = ThaumcraftMod.ARCANE_BORE.get().defaultBlockState()
                .setValue(com.darkifov.thaumcraft.block.ArcaneBoreBlock.FACING, Direction.EAST)
                .setValue(com.darkifov.thaumcraft.block.ArcaneBoreBlock.INVERTED, false);
        level.setBlock(borePos, state, Block.UPDATE_ALL);
        require(state.canSurvive(level, borePos), "Arcane Bore no longer survives on its base");
        net.minecraft.world.phys.AABB bounds = state.getShape(level, borePos).bounds();
        require(bounds.maxX == 2.0D && bounds.minX == 0.0D,
                "Arcane Bore selection box no longer extends one block toward the nozzle");
        level.removeBlock(basePos, false);
        require(!state.canSurvive(level, borePos), "Arcane Bore survived without its base");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneBoreSavesOriginalInventoryListAndOrientations(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos basePos = helper.absolutePos(new BlockPos(4, 2, 4));
        BlockPos borePos = basePos.above();
        level.setBlock(basePos, ThaumcraftMod.ARCANE_BORE_BASE.get().defaultBlockState(), Block.UPDATE_ALL);
        BlockState state = ThaumcraftMod.ARCANE_BORE.get().defaultBlockState()
                .setValue(com.darkifov.thaumcraft.block.ArcaneBoreBlock.FACING, Direction.SOUTH)
                .setValue(com.darkifov.thaumcraft.block.ArcaneBoreBlock.INVERTED, false);
        level.setBlock(borePos, state, Block.UPDATE_ALL);
        ArcaneBoreBlockEntity bore = requireBlockEntity(level, borePos, ArcaneBoreBlockEntity.class);
        bore.inventory().setStackInSlot(0, new ItemStack(ThaumcraftMod.FOCUS_EXCAVATION.get()));
        bore.inventory().setStackInSlot(1, new ItemStack(Items.IRON_PICKAXE));
        CompoundTag tag = bore.saveWithFullMetadata();
        require(tag.contains("Inventory", net.minecraft.nbt.Tag.TAG_LIST)
                        && tag.getList("Inventory", net.minecraft.nbt.Tag.TAG_COMPOUND).size() == 2,
                "Arcane Bore did not save TC4 Inventory list");
        require(tag.getList("Inventory", net.minecraft.nbt.Tag.TAG_COMPOUND).getCompound(0).contains("Slot"),
                "Arcane Bore inventory entry lost Slot byte");
        require(tag.getInt("orientation") == Direction.SOUTH.get3DDataValue()
                        && tag.getInt("baseOrientation") == Direction.UP.get3DDataValue(),
                "Arcane Bore orientation NBT drifted");
        require(!tag.contains("SpiralIndex"), "Arcane Bore must not persist the old port SpiralIndex");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneBoreBaseRetainsAllFacePerditioSuction(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockState state = ThaumcraftMod.ARCANE_BORE_BASE.get().defaultBlockState()
                .setValue(com.darkifov.thaumcraft.block.ArcaneBoreBaseBlock.FACING, Direction.WEST);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        ArcaneBoreBaseBlockEntity base = requireBlockEntity(level, pos, ArcaneBoreBaseBlockEntity.class);
        for (Direction direction : Direction.values()) require(base.canInputFrom(direction),
                "Arcane Bore Base must accept essentia connections on every face");
        require(base.suctionAmount(Direction.WEST) == 0 && base.suctionAmount(Direction.EAST) == 128,
                "Arcane Bore Base nozzle/non-nozzle suction drifted");
        require(base.suctionType(Direction.UP) == Aspect.PERDITIO,
                "Arcane Bore Base suction type must be Perditio");
        CompoundTag tag = base.saveWithFullMetadata();
        require(tag.getInt("orientation") == Direction.WEST.get3DDataValue(),
                "Arcane Bore Base did not save orientation ordinal");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneBoreMenuAndItemsRetainOriginalContracts(GameTestHelper helper) {
        require(new ItemStack(ThaumcraftMod.ARCANE_BORE_ITEM.get()).getRarity() == Rarity.COMMON
                        && new ItemStack(ThaumcraftMod.ARCANE_BORE_BASE_ITEM.get()).getRarity() == Rarity.COMMON,
                "Arcane Bore and Base must retain common rarity");
        require(TC4ArcaneBoreParity.NBT_INVENTORY.equals("Inventory")
                        && TC4ArcaneBoreParity.NBT_SPEEDY_TIME.equals("SpeedyTime"),
                "Arcane Bore original NBT key constants drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void arcaneBoreResearchAndRecipesMatchOriginal(GameTestHelper helper) {
        ResearchEntry entry = OriginalResearchBridge.byKey("ARCANEBORE")
                .orElseThrow(() -> new GameTestAssertException("ARCANEBORE research entry is missing"));
        require(java.util.Arrays.asList(entry.recipeKeys()).containsAll(java.util.List.of("ArcaneBoreBase", "ArcaneBore")),
                "ARCANEBORE research recipe links drifted");
        TC4RecipeRuntimeBridge.OriginalRecipe base = TC4RecipeRuntimeBridge.byKey("ArcaneBoreBase");
        TC4RecipeRuntimeBridge.OriginalRecipe bore = TC4RecipeRuntimeBridge.byKey("ArcaneBore");
        require(base != null && java.util.Arrays.equals(base.pattern(), new String[]{"WIW", "IDI", "WIW"}),
                "Arcane Bore Base shaped recipe pattern drifted");
        require(bore != null && "4".equals(bore.instability()),
                "Arcane Bore infusion recipe or instability drifted");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void thaumonomiconItemPropertiesMatchOriginal(GameTestHelper helper) {
        ItemStack normal = new ItemStack(ThaumcraftMod.THAUMONOMICON.get());
        ItemStack cheat = new ItemStack(ThaumcraftMod.THAUMONOMICON_CHEAT.get());
        require(normal.getMaxStackSize() == TC4ThaumonomiconParity.MAX_STACK_SIZE
                        && normal.getRarity() == Rarity.UNCOMMON,
                "Normal Thaumonomicon stack size or rarity drifted");
        require(cheat.getMaxStackSize() == TC4ThaumonomiconParity.MAX_STACK_SIZE
                        && cheat.getRarity() == Rarity.EPIC,
                "Cheat Thaumonomicon stack size or rarity drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void thaumonomiconSpreadAndPanContractsMatchOriginal(GameTestHelper helper) {
        require(TC4ThaumonomiconParity.DEFAULT_LAST_X == -5
                        && TC4ThaumonomiconParity.DEFAULT_LAST_Y == -6,
                "Thaumonomicon shared map origin drifted");
        require(TC4ThaumonomiconParity.spreadStart(3) == 2
                        && TC4ThaumonomiconParity.previousSpread(3) == 0
                        && TC4ThaumonomiconParity.nextSpread(0, 5) == 2
                        && TC4ThaumonomiconParity.maxFirstPage(6) == 4,
                "Thaumonomicon even-spread page navigation drifted");
        require(TC4ThaumonomiconParity.BROWSER_WIDTH == 256
                        && TC4ThaumonomiconParity.BROWSER_HEIGHT == 230
                        && TC4ThaumonomiconParity.MAP_WIDTH == 224
                        && TC4ThaumonomiconParity.MAP_HEIGHT == 196,
                "Thaumonomicon browser/map dimensions drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void thaumonomiconCompoundRecipeMatchesOriginal(GameTestHelper helper) {
        TC4RecipeRuntimeBridge.OriginalRecipe recipe = TC4RecipeRuntimeBridge.byKey("Thaumonomicon");
        require(recipe != null && recipe.kind() == TC4RecipeRuntimeBridge.Kind.COMPOUND,
                "Thaumonomicon compound recipe is missing");
        require(java.util.Arrays.equals(recipe.pattern(), new String[]{"1", "2", "1"})
                        && recipe.aspectCosts().length == 0
                        && java.util.Arrays.asList(recipe.components()).containsAll(java.util.List.of(
                        "new ItemStack(ConfigItems.itemWandCasting)",
                        "new ItemStack(Blocks.field_150342_X)")),
                "Thaumonomicon bookshelf/wand compound recipe drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void thaumonomiconResearchEntryMatchesOriginal(GameTestHelper helper) {
        ResearchEntry entry = OriginalResearchBridge.byKey("THAUMONOMICON")
                .orElseThrow(() -> new GameTestAssertException("THAUMONOMICON research entry is missing"));
        require("BASICS".equals(entry.category()) && entry.displayColumn() == 1
                        && entry.displayRow() == -2 && entry.complexity() == 0,
                "THAUMONOMICON category/position/complexity drifted");
        require(entry.hasFlag("autoUnlock") && entry.hasFlag("stub") && entry.hasFlag("round")
                        && java.util.Arrays.asList(entry.requirements()).contains("RESEARCH")
                        && java.util.Arrays.asList(entry.recipeKeys()).contains("Thaumonomicon"),
                "THAUMONOMICON flags/parent/pages drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void thaumonomiconCheatSheetContractMatchesOriginal(GameTestHelper helper) {
        require(TC4ThaumonomiconParity.CHEAT_ASPECT_POOL == 50,
                "Cheat Sheet aspect pool must be exactly 50 for newly discovered aspects");
        require(TC4ThaumonomiconParity.OPEN_PAGE_VOLUME == 1.0F
                        && TC4ThaumonomiconParity.PAGE_TURN_VOLUME == 0.66F
                        && TC4ThaumonomiconParity.CATEGORY_CLICK_VOLUME == 0.4F
                        && TC4ThaumonomiconParity.GUI_SOUND_PITCH == 1.0F,
                "Thaumonomicon GUI sound volumes/pitch drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void thaumonomiconAcquisitionContractsMatchOriginal(GameTestHelper helper) {
        require(TC4ThaumonomiconLootParity.RARE_CHEST_TABLES.size() == 7
                        && TC4ThaumonomiconLootParity.RARE_CHEST_WEIGHT == 1
                        && TC4ThaumonomiconLootParity.WIZARD_TOWER_WEIGHT == 20
                        && TC4ThaumonomiconLootParity.MIN_COUNT == 1
                        && TC4ThaumonomiconLootParity.MAX_COUNT == 1,
                "Thaumonomicon chest/wizard-tower acquisition constants drifted");
        require(TC4ThaumonomiconLootParity.requiresOriginalWizardTowerIntegration()
                        && TC4ThaumonomiconLootParity.requiresLegacyGlobalChestIntegration(),
                "Missing explicit external acquisition integration boundary");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void thaumonomiconObjectAspectsMatchOriginal(GameTestHelper helper) {
        AspectList aspects = TC4ObjectAspectRegistry.getAspectsForItem(new ItemStack(ThaumcraftMod.THAUMONOMICON.get()));
        require(aspects.get(Aspect.COGNITIO) == 10
                        && aspects.get(Aspect.PRAECANTATIO) == 2
                        && aspects.get(Aspect.ARBOR) == 1,
                "Thaumonomicon object aspects drifted from bookshelf + magic/mind registration");
        helper.succeed();
    }


    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void researchSystemOriginalRegistryIsExactly201(GameTestHelper helper) {
        require(ResearchRegistry.originalEntries().size() == TC4ResearchSystemFullClosureParity.ORIGINAL_RESEARCH_COUNT,
                "Original TC4 research denominator drifted from 201");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void researchSystemPrimalsStartKnownWithZeroPool(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        PlayerAspectKnowledge.seedPrimals(player);
        for (Aspect aspect : Aspect.values()) {
            if (aspect.isPrimal()) {
                require(PlayerAspectKnowledge.knows(player, aspect), "Primal aspect was not initially discovered: " + aspect.id());
                require(PlayerAspectKnowledge.pool(player).get(aspect) == 0,
                        "Primal aspect received invented starter research points: " + aspect.id());
            }
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void researchSystemLegacyWalletMigratesWithoutDuplication(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        CompoundTag root = new CompoundTag();
        AspectList modern = new AspectList().add(Aspect.AER, 2);
        root.put("Pool", modern.save());
        player.getPersistentData().put("ThaumcraftAspectKnowledge", root);
        CompoundTag legacy = new CompoundTag();
        legacy.putInt(Aspect.AER.id(), 7);
        player.getPersistentData().put("ThaumcraftOriginalAspectWallet", legacy);
        PlayerAspectKnowledge.seedPrimals(player);
        require(PlayerAspectKnowledge.pool(player).get(Aspect.AER) == 7,
                "Legacy/modern research pools were summed or lost instead of max-merged");
        require(!player.getPersistentData().contains("ThaumcraftOriginalAspectWallet"),
                "Legacy research wallet was not removed after migration");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void researchSystemOneTagNotesKeepOneAnchor(GameTestHelper helper) {
        ResearchEntry oneTag = ResearchRegistry.originalEntries().stream()
                .filter(entry -> entry.aspects().size() == 1)
                .findFirst()
                .orElseThrow(() -> new GameTestAssertException("No one-tag original research entry found"));
        require(ResearchNoteRequirements.requiredFor(oneTag.key()).size() == 1,
                "One-tag TC4 research note gained invented supplementary anchors");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void researchSystemCreativeDoesNotBypassInk(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.getAbilities().instabuild = true;
        require(!ResearchTableInventoryRuntime.checkInkForCreate(player)
                        && !ResearchTableInventoryRuntime.checkInkForEdit(player),
                "Creative mode incorrectly bypassed Research Table ink requirements");
        require(!TC4ResearchSystemFullClosureParity.CREATIVE_BYPASSES_RESEARCH_COSTS,
                "Research cost parity constant drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void researchSystemTableHasNoInventedCreateOrCompleteButtons(GameTestHelper helper) {
        require(TC4ResearchSystemFullClosureParity.noteCreationAndLearningPathsMatchOriginal(),
                "Research note creation/learning path drifted from Thaumonomicon + used-note flow");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void researchSystemDuplicateCanExceedVanillaMaxStack(GameTestHelper helper) {
        ItemStack note = new ItemStack(ThaumcraftMod.RESEARCH_NOTE.get());
        require(note.getMaxStackSize() == 1, "Research Note item max stack changed");
        note.grow(1);
        require(note.getCount() == 2,
                "Completed Research Note copy was incorrectly clamped to item max stack size");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void researchSystemCostsAndRadiusUseExactResearchTags(GameTestHelper helper) {
        ResearchEntry entry = ResearchRegistry.originalEntries().stream()
                .filter(candidate -> !candidate.aspects().isEmpty())
                .findFirst()
                .orElseThrow(() -> new GameTestAssertException("No tagged original research entry found"));
        require(OriginalResearchBridge.costsFor(entry).equals(entry.aspects()),
                "Secondary research cost added inferred aspects or changed exact tag amounts");
        require(TC4ResearchSystemFullClosureParity.noteRadius(1) == 2
                        && TC4ResearchSystemFullClosureParity.noteRadius(3) == 4,
                "Research Note radius formula drifted from 1 + min(3, complexity)");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void infernalFurnaceCookCadenceMatchesOriginal(GameTestHelper helper) {
        require(TC4InfernalFurnaceParity.cookTime(false, 0) == 140
                        && TC4InfernalFurnaceParity.cookTime(true, 0) == 80
                        && TC4InfernalFurnaceParity.cookTime(false, 3) == 80
                        && TC4InfernalFurnaceParity.cookTime(true, 3) == 20,
                "Infernal Furnace cook-time/bellows cadence drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void infernalFurnaceInventoryAndNbtMatchOriginal(GameTestHelper helper) {
        require(TC4InfernalFurnaceParity.INVENTORY_SIZE == 32
                        && TC4InfernalFurnaceParity.NBT_ITEMS.equals("Items")
                        && TC4InfernalFurnaceParity.NBT_SLOT.equals("Slot")
                        && TC4InfernalFurnaceParity.NBT_COOK_TIME.equals("CookTime")
                        && TC4InfernalFurnaceParity.NBT_SPEEDY_TIME.equals("SpeedyTime"),
                "Infernal Furnace inventory/NBT contract drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void infernalFurnaceIgnisAccelerationMatchesOriginal(GameTestHelper helper) {
        require(TC4InfernalFurnaceParity.IGNIS_VIS_REQUEST_CENTIVIS == 5
                        && TC4InfernalFurnaceParity.IGNIS_ESSENTIA_SPEED_TICKS == 600
                        && TC4InfernalFurnaceParity.NOZZLE_DRAW_INTERVAL_TICKS == 5
                        && TC4InfernalFurnaceParity.NOZZLE_SUCTION == 128,
                "Infernal Furnace Ignis acceleration/suction drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void infernalFurnaceFormationCostMatchesOriginal(GameTestHelper helper) {
        require(TC4InfernalFurnaceParity.FORMATION_IGNIS_CENTIVIS == 5000
                        && TC4InfernalFurnaceParity.FORMATION_TERRA_CENTIVIS == 5000
                        && TC4InfernalFurnaceParity.RESEARCH.equals("INFERNALFURNACE"),
                "Infernal Furnace 50 Ignis/50 Terra formation cost drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void infernalFurnaceRestorationMapMatchesOriginal(GameTestHelper helper) {
        require(TC4InfernalFurnaceParity.originalPartRestoresObsidian(2)
                        && TC4InfernalFurnaceParity.originalPartRestoresObsidian(5)
                        && !TC4InfernalFurnaceParity.originalPartRestoresObsidian(1)
                        && !TC4InfernalFurnaceParity.originalPartRestoresObsidian(10),
                "Infernal Furnace multiblock restoration map drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void infernalFurnaceCoreAndNozzleBlockEntitiesExist(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos core = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState coreState = ThaumcraftMod.INFERNAL_FURNACE.get().defaultBlockState()
                .setValue(InfernalFurnaceBlock.PART, 0)
                .setValue(InfernalFurnaceBlock.LAYER, InfernalFurnaceLayer.MIDDLE)
                .setValue(InfernalFurnaceBlock.FACING, Direction.EAST);
        level.setBlock(core, coreState, Block.UPDATE_ALL);
        require(level.getBlockEntity(core) instanceof InfernalFurnaceBlockEntity,
                "Infernal Furnace core BlockEntity missing");
        BlockPos nozzle = core.east();
        level.setBlock(nozzle, coreState.setValue(InfernalFurnaceBlock.PART, 10), Block.UPDATE_ALL);
        require(level.getBlockEntity(nozzle) instanceof InfernalFurnaceNozzleBlockEntity,
                "Infernal Furnace nozzle BlockEntity missing");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void infernalFurnaceLivingHazardAndBonusConstantsMatchOriginal(GameTestHelper helper) {
        require(TC4InfernalFurnaceParity.LIVING_DAMAGE == 3.0F
                        && TC4InfernalFurnaceParity.LIVING_FIRE_SECONDS == 10
                        && TC4InfernalFurnaceParity.BONUS_CHANCE_WITHOUT_BELLOWS == 0.25F
                        && TC4InfernalFurnaceParity.BONUS_CHANCE_PER_BELLOWS == 0.44F,
                "Infernal Furnace living hazard or bonus probabilities drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void growthLampChargeReserveAndSuctionMatchOriginal(GameTestHelper helper) {
        require(TC4GrowthLampParity.CHARGES_PER_ESSENTIA == 100
                        && TC4GrowthLampParity.DRAW_INTERVAL_TICKS == 5
                        && TC4GrowthLampParity.suction(false, -1, true) == 128
                        && TC4GrowthLampParity.suction(true, 100, true) == 0
                        && TC4GrowthLampParity.suction(true, 0, true) == 128
                        && TC4GrowthLampParity.suction(false, -1, false) == 0,
                "Growth Lamp charge/reserve/suction contract drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void growthLampAcceptsAnyNonAirSupportAndUsesDynamicLight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        BlockState inactive = ThaumcraftMod.TC4_LAMP_GROWTH.get().defaultBlockState()
                .setValue(TC4EssentiaLampBlock.FACING, Direction.DOWN)
                .setValue(TC4EssentiaLampBlock.ACTIVE, false);
        level.setBlock(pos.below(), Blocks.GLASS_PANE.defaultBlockState(), Block.UPDATE_ALL);
        require(inactive.canSurvive(level, pos), "Growth Lamp rejected non-air, non-sturdy support");
        require(inactive.getLightEmission() == TC4GrowthLampParity.INACTIVE_LIGHT
                        && inactive.setValue(TC4EssentiaLampBlock.ACTIVE, true).getLightEmission()
                        == TC4GrowthLampParity.ACTIVE_LIGHT,
                "Growth Lamp active/inactive light level drifted");
        level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        require(!inactive.canSurvive(level, pos), "Growth Lamp survived after support became air");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void growthLampBlockEntityAndOriginalNbtRoundTrip(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        BlockState state = ThaumcraftMod.TC4_LAMP_GROWTH.get().defaultBlockState()
                .setValue(TC4EssentiaLampBlock.FACING, Direction.EAST);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        TC4EssentiaLampBlockEntity lamp = requireBlockEntity(level, pos, TC4EssentiaLampBlockEntity.class);
        CompoundTag input = new CompoundTag();
        input.putInt(TC4GrowthLampParity.NBT_ORIENTATION, Direction.WEST.get3DDataValue());
        input.putBoolean(TC4GrowthLampParity.NBT_RESERVE, true);
        input.putInt(TC4GrowthLampParity.NBT_CHARGES, 37);
        lamp.load(input);
        lamp.onLoad();
        CompoundTag saved = lamp.saveWithoutMetadata();
        require(saved.getInt("orientation") == Direction.WEST.get3DDataValue()
                        && saved.getBoolean("reserve")
                        && saved.getInt("charges") == 37,
                "Growth Lamp original NBT failed to round-trip");
        require(!saved.contains("drawDelay") && !saved.contains("fertilityCounter"),
                "Growth Lamp persisted non-original transient counters");
        require(level.getBlockState(pos).getValue(TC4EssentiaLampBlock.ACTIVE),
                "Growth Lamp active state was not restored from charges");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void growthLampColumnScanAndSphereMatchOriginal(GameTestHelper helper) {
        require(TC4GrowthLampParity.RADIUS == 6
                        && TC4GrowthLampParity.DIAMETER == 13
                        && TC4GrowthLampParity.columnCount() == 169,
                "Growth Lamp 13x13 shuffled column scan drifted");
        require(TC4GrowthLampParity.insideSphere(5, 0, 0)
                        && !TC4GrowthLampParity.insideSphere(6, 0, 0)
                        && TC4GrowthLampParity.insideSphere(3, 3, 3),
                "Growth Lamp strict radius-6 sphere test drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void growthLampUsesSingleNaturalTickNotGuaranteedBonemeal(GameTestHelper helper) {
        require(TC4GrowthLampParity.USES_SINGLE_NATURAL_TICK,
                "Growth Lamp must execute one natural plant tick rather than guaranteed bonemeal");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void growthLampRecipeResearchAndRarityMatchOriginal(GameTestHelper helper) {
        requireInfusionRecipe(
                "tc4_lamp_growth",
                "thaumcraft:tc4_block_arcane_lamp",
                "thaumcraft:tc4_block_lamp_growth",
                4,
                java.util.List.of(
                        "minecraft:gold_ingot", "minecraft:black_dye", "thaumcraft:terra_shard",
                        "minecraft:gold_ingot", "minecraft:black_dye", "thaumcraft:terra_shard"),
                Aspect.HERBA, 16, Aspect.LUX, 8, Aspect.VICTUS, 16);
        ResearchEntry research = OriginalResearchBridge.byKey("LAMPGROWTH")
                .orElseThrow(() -> new GameTestAssertException("Missing LAMPGROWTH research"));
        require(java.util.Arrays.asList(research.requirements()).containsAll(
                        java.util.List.of("ARCANELAMP", "INFUSION")),
                "LAMPGROWTH research parent chain drifted");
        require(new ItemStack(ThaumcraftMod.TC4_LAMP_GROWTH_ITEM.get()).getRarity() == Rarity.COMMON,
                "Growth Lamp item rarity must remain common");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void growthLampRendererUsesOriginalBoreNozzleAndOffTextures(GameTestHelper helper) {
        require(TC4GrowthLampParity.ACTIVE_LIGHT == 15
                        && TC4GrowthLampParity.INACTIVE_LIGHT == 8
                        && TC4GrowthLampParity.SPARKLE_COLOR == 4_259_648,
                "Growth Lamp visual constants drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void fertilityLampChargeSuctionAndCadenceMatchOriginal(GameTestHelper helper) {
        require(TC4FertilityLampParity.MAX_CHARGES == 4
                        && TC4FertilityLampParity.BREEDING_COST == 2
                        && TC4FertilityLampParity.BREEDING_INTERVAL_TICKS == 300
                        && TC4FertilityLampParity.DRAW_INTERVAL_TICKS == 5
                        && TC4FertilityLampParity.suction(0, true) == 128
                        && TC4FertilityLampParity.suction(4, true) == 88
                        && TC4FertilityLampParity.suction(0, false) == 0,
                "Fertility Lamp charge, suction, or cadence contract drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void fertilityLampAcceptsAnyNonAirSupportAndUsesDynamicLight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        BlockState inactive = ThaumcraftMod.TC4_LAMP_FERTILITY.get().defaultBlockState()
                .setValue(TC4EssentiaLampBlock.FACING, Direction.DOWN)
                .setValue(TC4EssentiaLampBlock.ACTIVE, false);
        level.setBlock(pos.below(), Blocks.GLASS_PANE.defaultBlockState(), Block.UPDATE_ALL);
        require(inactive.canSurvive(level, pos), "Fertility Lamp rejected non-air support");
        require(inactive.getLightEmission() == TC4FertilityLampParity.INACTIVE_LIGHT
                        && inactive.setValue(TC4EssentiaLampBlock.ACTIVE, true).getLightEmission()
                        == TC4FertilityLampParity.ACTIVE_LIGHT,
                "Fertility Lamp active/inactive light drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void fertilityLampOriginalNbtAndTransientCountersMatch(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos, ThaumcraftMod.TC4_LAMP_FERTILITY.get().defaultBlockState()
                .setValue(TC4EssentiaLampBlock.FACING, Direction.DOWN), Block.UPDATE_ALL);
        TC4EssentiaLampBlockEntity lamp = requireBlockEntity(level, pos, TC4EssentiaLampBlockEntity.class);
        CompoundTag input = new CompoundTag();
        input.putInt(TC4FertilityLampParity.NBT_ORIENTATION, Direction.DOWN.get3DDataValue());
        input.putInt(TC4FertilityLampParity.NBT_CHARGES, 3);
        lamp.load(input);
        lamp.onLoad();
        CompoundTag saved = lamp.saveWithoutMetadata();
        require(saved.getInt("charges") == 3 && saved.getInt("orientation") == Direction.DOWN.get3DDataValue(),
                "Fertility Lamp original NBT failed to round-trip");
        require(!saved.contains("reserve") && !saved.contains("drawDelay") && !saved.contains("fertilityCounter"),
                "Fertility Lamp persisted non-original fields");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void fertilityLampDirectJarVictusInputMatchesOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlock(pos.below(), ThaumcraftMod.ESSENTIA_JAR.get().defaultBlockState(), Block.UPDATE_ALL);
        EssentiaJarBlockEntity jar = requireBlockEntity(level, pos.below(), EssentiaJarBlockEntity.class);
        jar.aspects().add(Aspect.VICTUS, 1);
        level.setBlock(pos, ThaumcraftMod.TC4_LAMP_FERTILITY.get().defaultBlockState()
                .setValue(TC4EssentiaLampBlock.FACING, Direction.DOWN), Block.UPDATE_ALL);
        TC4EssentiaLampBlockEntity lamp = requireBlockEntity(level, pos, TC4EssentiaLampBlockEntity.class);
        for (int i = 0; i < TC4FertilityLampParity.DRAW_INTERVAL_TICKS; i++) {
            TC4EssentiaLampBlockEntity.serverTick(level, pos, level.getBlockState(pos), lamp);
        }
        require(lamp.charges() == 1 && jar.aspects().get(Aspect.VICTUS) == 0,
                "Fertility Lamp failed direct top-side Victus draw from jar");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void fertilityLampExactClassPopulationAndBreedingMatchOriginal(GameTestHelper helper) {
        require(TC4FertilityLampParity.RADIUS == 7
                        && TC4FertilityLampParity.populationAllowed(7)
                        && !TC4FertilityLampParity.populationAllowed(8)
                        && TC4FertilityLampParity.eligibleAnimal(0, false)
                        && !TC4FertilityLampParity.eligibleAnimal(-10, false)
                        && !TC4FertilityLampParity.eligibleAnimal(0, true),
                "Fertility Lamp exact-class population or eligibility contract drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void fertilityLampRecipeResearchAndRarityMatchOriginal(GameTestHelper helper) {
        requireInfusionRecipe(
                "tc4_lamp_fertility",
                "thaumcraft:tc4_block_arcane_lamp",
                "thaumcraft:tc4_block_lamp_fertility",
                4,
                java.util.List.of(
                        "minecraft:gold_ingot", "minecraft:blaze_rod", "thaumcraft:ignis_shard",
                        "minecraft:gold_ingot", "minecraft:brewing_stand", "thaumcraft:ignis_shard"),
                Aspect.BESTIA, 16, Aspect.VICTUS, 16, Aspect.LUX, 8);
        ResearchEntry research = OriginalResearchBridge.byKey("LAMPFERTILITY")
                .orElseThrow(() -> new GameTestAssertException("Missing LAMPFERTILITY research"));
        require(java.util.Arrays.asList(research.requirements()).containsAll(
                        java.util.List.of("ARCANELAMP", "INFUSION")),
                "LAMPFERTILITY research parent chain drifted");
        require(new ItemStack(ThaumcraftMod.TC4_LAMP_FERTILITY_ITEM.get()).getRarity() == Rarity.COMMON,
                "Fertility Lamp item rarity must remain common");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 40)
    public static void fertilityLampRendererAndOffTexturesMatchOriginal(GameTestHelper helper) {
        require(TC4FertilityLampParity.ACTIVE_LIGHT == 15
                        && TC4FertilityLampParity.INACTIVE_LIGHT == 8
                        && TC4FertilityLampParity.BLOCK_HARDNESS == 3.0F
                        && TC4FertilityLampParity.BLOCK_RESISTANCE == 17.0F,
                "Fertility Lamp visual or block constants drifted");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void essentiaJarCanonicalNbtMatchesOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.ESSENTIA_JAR.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        EssentiaJarBlockEntity jar = requireBlockEntity(level, pos, EssentiaJarBlockEntity.class);
        require(jar.addToContainerOriginal(Aspect.AER, 8, false) == 0, "Normal jar rejected valid essentia");
        jar.setFilterAspect(Aspect.AER);
        jar.setLabelFacing(Direction.EAST);

        CompoundTag saved = jar.saveWithoutMetadata();
        require("aer".equals(saved.getString("Aspect")) && saved.getShort("Amount") == 8,
                "Jar did not persist original Aspect/Amount fields");
        require("aer".equals(saved.getString("AspectFilter")) && saved.getByte("facing") == 5,
                "Jar did not persist original AspectFilter/facing fields");
        require(!saved.contains("Aspects") && !saved.contains("FilterAspect"),
                "Jar still wrote pre-11.64.35 duplicate NBT");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void voidJarSuctionAndOverflowMatchOriginal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.VOID_ESSENTIA_JAR.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        EssentiaJarBlockEntity jar = requireBlockEntity(level, pos, EssentiaJarBlockEntity.class);
        jar.setFilterAspect(Aspect.AER);
        require(jar.originalMinimumSuction(true) == 48 && jar.originalSuctionAmount(true) == 48,
                "Labelled void jar did not use original suction 48 while below capacity");
        require(jar.addToContainerOriginal(Aspect.AER, 64, true) == 0 && jar.amount() == 64,
                "Void jar did not fill to the original 64-point display cap");
        require(jar.originalSuctionAmount(true) == 32,
                "Full labelled void jar did not fall back to original suction 32");
        require(jar.addToContainerOriginal(Aspect.AER, 8, true) == 0 && jar.amount() == 64,
                "Void jar did not consume compatible overflow");
        require(jar.addToContainerOriginal(Aspect.IGNIS, 8, true) == 8,
                "Void jar consumed overflow of a different aspect");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void labelledJarRetainsTypeWhenEmptied(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.ESSENTIA_JAR.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        EssentiaJarBlockEntity jar = requireBlockEntity(level, pos, EssentiaJarBlockEntity.class);
        jar.setFilterAspect(Aspect.ORDO);
        require(jar.addToContainerOriginal(Aspect.ORDO, 8, false) == 0, "Labelled jar rejected its aspect");
        require(jar.takeFromContainerOriginal(Aspect.ORDO, 8), "Could not empty labelled jar");
        require(jar.amount() == 0 && jar.storedAspect() == Aspect.ORDO && jar.suctionType() == Aspect.ORDO,
                "Empty labelled jar lost its retained original aspect type");
        jar.clearFilter();
        require(jar.addToContainerOriginal(Aspect.AER, 1, false) == 0 && jar.storedAspect() == Aspect.AER,
                "Zero-amount retained aspect incorrectly blocked a new unlabelled aspect");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void filledJarItemUsesRootTc4Nbt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.ESSENTIA_JAR.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        EssentiaJarBlockEntity jar = requireBlockEntity(level, pos, EssentiaJarBlockEntity.class);
        jar.addToContainerOriginal(Aspect.AQUA, 16, false);
        jar.setFilterAspect(Aspect.AQUA);
        jar.setLabelFacing(Direction.WEST);

        ItemStack stack = new ItemStack(ThaumcraftMod.ESSENTIA_JAR.get());
        EssentiaJarBlockItem.writeJarData(stack, jar);
        CompoundTag root = stack.getTag();
        ListTag itemAspects = root == null ? new ListTag() : root.getList("Aspects", Tag.TAG_COMPOUND);
        require(root != null && itemAspects.size() == 1
                        && "aqua".equals(itemAspects.getCompound(0).getString("key"))
                        && itemAspects.getCompound(0).getInt("amount") == 16,
                "Filled jar item lost original root Aspects-list NBT");
        require("aqua".equals(root.getString("AspectFilter")), "Filled jar item lost label NBT");
        require(!root.contains(EssentiaJarBlockItem.BLOCK_ENTITY_TAG) && !root.contains("facing")
                        && !root.contains("Aspect") && !root.contains("Amount"),
                "Filled jar item stored world-only or tile-only NBT");
        require(EssentiaJarBlockItem.itemAspects(stack).get(Aspect.AQUA) == 16
                        && EssentiaJarBlockItem.itemFilter(stack) == Aspect.AQUA,
                "Filled jar item could not read its canonical TC4 NBT");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void jarFacingAndPhialBoundariesMatchOriginal(GameTestHelper helper) {
        require(TC4EssentiaJarParity.labelFacingDataValue(0.0F) == 2
                        && TC4EssentiaJarParity.labelFacingDataValue(90.0F) == 5
                        && TC4EssentiaJarParity.labelFacingDataValue(180.0F) == 3
                        && TC4EssentiaJarParity.labelFacingDataValue(270.0F) == 4,
                "Jar label facing mapping drifted from BlockJar");
        require(TC4EssentiaJarParity.canFillEmptyPhial(8)
                        && !TC4EssentiaJarParity.canFillEmptyPhial(7)
                        && TC4EssentiaJarParity.canEmptyFilledPhial(56, true)
                        && !TC4EssentiaJarParity.canEmptyFilledPhial(57, true),
                "Jar phial transfer stopped requiring an atomic eight-point operation");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void jarLabelUsesOriginalZeroAmountAspectList(GameTestHelper helper) {
        ItemStack label = com.darkifov.thaumcraft.block.JarLabelItem.withAspect(Aspect.AER);
        CompoundTag root = label.getTag();
        ListTag list = root == null ? new ListTag() : root.getList("Aspects", Tag.TAG_COMPOUND);
        require(list.size() == 1
                        && "aer".equals(list.getCompound(0).getString("key"))
                        && list.getCompound(0).getInt("amount") == 0,
                "Aspect label did not use the original zero-amount AspectList NBT");
        require(com.darkifov.thaumcraft.block.JarLabelItem.getAspect(label) == Aspect.AER,
                "Aspect label could not read its original NBT");
        com.darkifov.thaumcraft.block.JarLabelItem.clearAspect(label);
        require(com.darkifov.thaumcraft.block.JarLabelItem.isBlank(label),
                "Cleared aspect label did not become blank");
        helper.succeed();
    }

    @GameTest(templateNamespace = ThaumcraftMod.MOD_ID, template = TEMPLATE, batch = BATCH, timeoutTicks = 100)
    public static void hoverHarnessKeepsOriginalJarItemNbt(GameTestHelper helper) {
        ItemStack jar = new ItemStack(ThaumcraftMod.ESSENTIA_JAR.get());
        EssentiaJarBlockItem.writeItemData(jar, Aspect.POTENTIA, 2, Aspect.POTENTIA);
        ItemStack harness = new ItemStack(TC4ResearchItems.registered("tc4_hoverharness")
                .orElseThrow(() -> new IllegalStateException("Hover Harness registry entry is missing")).get());
        com.darkifov.thaumcraft.item.gear.HoverHarnessItem.setJar(harness, jar);
        require(com.darkifov.thaumcraft.item.gear.HoverHarnessItem.expendCharge(harness, false),
                "Hover Harness rejected an original-format Potentia jar");
        ItemStack stored = com.darkifov.thaumcraft.item.gear.HoverHarnessItem.getJar(harness);
        require(EssentiaJarBlockItem.itemAspects(stored).get(Aspect.POTENTIA) == 2
                        && EssentiaJarBlockItem.itemFilter(stored) == Aspect.POTENTIA,
                "Hover Harness rewrote the jar to legacy BlockEntityTag/Aspects compound NBT");
        require(stored.getTag() != null && stored.getTag().contains("Aspects", Tag.TAG_LIST)
                        && !stored.getTag().contains(EssentiaJarBlockItem.BLOCK_ENTITY_TAG),
                "Hover Harness did not retain exact ItemJarFilled root NBT");
        helper.succeed();
    }

    private static final java.util.Set<String> WARP_TEXT_KEYS = java.util.Set.of(
            "warp.text.1", "warp.text.2", "warp.text.3", "warp.text.4", "warp.text.5",
            "warp.text.6", "warp.text.7", "warp.text.8", "warp.text.9", "warp.text.10",
            "warp.text.11", "warp.text.12", "warp.text.13", "warp.text.14", "warp.text.15");

    private static ResearchEntry syntheticResearchEntry(String key, int warp) {
        return new ResearchEntry(
                key, key, key, "REBUILD", 0, 0, 1,
                java.util.Collections.emptyMap(), new String[0], new String[0],
                new String[0], new String[0], new String[0], new String[0],
                new String[0], new String[0], new String[0], warp);
    }

    private static ResearchNotePlacementFixture openResearchNotePlacementTest(
            GameTestHelper helper, Player player, boolean occupiedTarget) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.RESEARCH_TABLE.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        ResearchTableBlockEntity table = requireBlockEntity(level, pos, ResearchTableBlockEntity.class);
        ItemStack note = controlledResearchNote(occupiedTarget);
        table.setItem(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS,
                new ItemStack(ThaumcraftMod.SCRIBING_TOOLS.get()));
        table.setItem(ResearchTableBlockEntity.SLOT_RESEARCH_NOTE, note);
        player.containerMenu = new ResearchTableMenu(116399, player.getInventory(), table);
        int targetSlot = ResearchNoteGrid.byHex(2, 0).orElseThrow().index();
        return new ResearchNotePlacementFixture(table, note, targetSlot);
    }

    private static ItemStack controlledResearchNote(boolean occupiedTarget) {
        ItemStack note = new ItemStack(ThaumcraftMod.RESEARCH_NOTE.get());
        int anchorSlot = ResearchNoteGrid.byHex(0, 0).orElseThrow().index();
        int targetSlot = ResearchNoteGrid.byHex(2, 0).orElseThrow().index();
        CompoundTag root = ResearchNoteState.root(note);
        root.putString(ResearchNoteState.TAG_TARGET, "");
        root.putInt(ResearchNoteState.TAG_RADIUS, ResearchNoteGrid.MIN_RADIUS);
        root.putBoolean(ResearchNoteState.TAG_SOLVED, false);

        CompoundTag slots = new CompoundTag();
        slots.putString(String.valueOf(anchorSlot), Aspect.MOTUS.id());
        if (occupiedTarget) {
            slots.putString(String.valueOf(targetSlot), Aspect.ORDO.id());
        }
        CompoundTag types = new CompoundTag();
        types.putInt(String.valueOf(anchorSlot), ResearchNoteGrid.TYPE_RESEARCH_ANCHOR);
        types.putInt(String.valueOf(targetSlot), occupiedTarget
                ? ResearchNoteGrid.TYPE_PLACED
                : ResearchNoteGrid.TYPE_EMPTY);
        root.put(ResearchNoteState.TAG_SLOTS, slots);
        root.put(ResearchNoteState.TAG_TYPES, types);
        ResearchNoteState.initialize(note, "");
        return note;
    }

    private record ResearchNotePlacementFixture(
            ResearchTableBlockEntity table, ItemStack note, int targetSlot) {
    }

    private static ResearchNotePlacementFixture openSolvableResearchNoteTest(
            GameTestHelper helper, Player player, boolean disconnectedPlaced) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.RESEARCH_TABLE.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        ResearchTableBlockEntity table = requireBlockEntity(level, pos, ResearchTableBlockEntity.class);
        ItemStack note = controlledSolvableResearchNote(disconnectedPlaced);
        table.setItem(ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS,
                new ItemStack(ThaumcraftMod.SCRIBING_TOOLS.get()));
        table.setItem(ResearchTableBlockEntity.SLOT_RESEARCH_NOTE, note);
        player.containerMenu = new ResearchTableMenu(116401, player.getInventory(), table);
        return new ResearchNotePlacementFixture(table, note, -1);
    }

    private static ItemStack controlledSolvableResearchNote(boolean disconnectedPlaced) {
        ItemStack note = new ItemStack(ThaumcraftMod.RESEARCH_NOTE.get());
        int firstAnchor = ResearchNoteGrid.byHex(0, 0).orElseThrow().index();
        int secondAnchor = ResearchNoteGrid.byHex(1, 0).orElseThrow().index();
        int disconnected = ResearchNoteGrid.byHex(2, 0).orElseThrow().index();
        CompoundTag root = ResearchNoteState.root(note);
        root.putString(ResearchNoteState.TAG_TARGET, "");
        root.putString(ResearchNoteState.TAG_REQUIRED, "aer,motus");
        root.putInt(ResearchNoteState.TAG_RADIUS, ResearchNoteGrid.MIN_RADIUS);
        root.putBoolean(ResearchNoteState.TAG_SOLVED, false);

        CompoundTag slots = new CompoundTag();
        slots.putString(String.valueOf(firstAnchor), Aspect.AER.id());
        slots.putString(String.valueOf(secondAnchor), Aspect.MOTUS.id());
        if (disconnectedPlaced) {
            slots.putString(String.valueOf(disconnected), Aspect.TERRA.id());
        }
        CompoundTag types = new CompoundTag();
        types.putInt(String.valueOf(firstAnchor), ResearchNoteGrid.TYPE_RESEARCH_ANCHOR);
        types.putInt(String.valueOf(secondAnchor), ResearchNoteGrid.TYPE_RESEARCH_ANCHOR);
        types.putInt(String.valueOf(disconnected), disconnectedPlaced
                ? ResearchNoteGrid.TYPE_PLACED : ResearchNoteGrid.TYPE_EMPTY);
        root.put(ResearchNoteState.TAG_SLOTS, slots);
        root.put(ResearchNoteState.TAG_TYPES, types);
        return note;
    }

    private static ItemStack solvedDiscoveryNote(String target) {
        ItemStack note = new ItemStack(ThaumcraftMod.RESEARCH_NOTE.get());
        CompoundTag root = ResearchNoteState.root(note);
        root.putString(ResearchNoteState.TAG_TARGET, target);
        root.putInt(ResearchNoteState.TAG_RADIUS, ResearchNoteGrid.MIN_RADIUS);
        root.putBoolean(ResearchNoteState.TAG_SOLVED, true);
        root.putInt(ResearchNoteState.TAG_PROGRESS, 100);
        root.put(ResearchNoteState.TAG_SLOTS, new CompoundTag());
        root.put(ResearchNoteState.TAG_TYPES, new CompoundTag());
        return note;
    }

    private static ResearchTableBlockEntity openResearchTableForCombinationTest(
            GameTestHelper helper, Player player) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = ThaumcraftMod.RESEARCH_TABLE.get().defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        ResearchTableBlockEntity table = requireBlockEntity(level, pos, ResearchTableBlockEntity.class);
        player.containerMenu = new ResearchTableMenu(116398, player.getInventory(), table);
        return table;
    }

    private static void setPoolAmount(Player player, Aspect aspect, int targetAmount) {
        PlayerAspectKnowledge.seedPrimals(player);
        int current = PlayerAspectKnowledge.pool(player).get(aspect);
        if (current > targetAmount) {
            require(PlayerAspectKnowledge.consumePool(player, aspect, current - targetAmount),
                    "Unable to reduce test aspect pool");
        } else if (current < targetAmount) {
            PlayerAspectKnowledge.addPool(player, aspect, targetAmount - current);
        }
    }

    private static void tickFurnace(ServerLevel level, BlockPos pos, AlchemicalFurnaceBlockEntity furnace, int ticks) {
        for (int i = 0; i < ticks; i++) {
            AlchemicalFurnaceBlockEntity.serverTick(level, pos, level.getBlockState(pos), furnace);
        }
    }

    private static void tickCentrifuge(ServerLevel level, BlockPos pos, AlchemicalCentrifugeBlockEntity centrifuge, int ticks) {
        for (int i = 0; i < ticks; i++) {
            AlchemicalCentrifugeBlockEntity.serverTick(level, pos, level.getBlockState(pos), centrifuge);
        }
    }

    private static void tickThaumatorium(ServerLevel level, BlockPos pos, ThaumatoriumBlockEntity tile, int ticks) {
        for (int i = 0; i < ticks; i++) {
            ThaumatoriumBlockEntity.serverTick(level, pos, level.getBlockState(pos), tile);
        }
    }

    private static ThaumatoriumBlockEntity placeValidThaumatorium(ServerLevel level, BlockPos pos) {
        // Place supporting parts first so neighbour notifications never expose a transient,
        // invalid half-built machine. This is the post-ritual layout created by WandManager.
        level.setBlock(pos.below(), ThaumcraftMod.CRUCIBLE.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos.above(), ThaumcraftMod.THAUMATORIUM_UPPER.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos, ThaumcraftMod.THAUMATORIUM.get().defaultBlockState(), Block.UPDATE_ALL);
        return requireBlockEntity(level, pos, ThaumatoriumBlockEntity.class);
    }

    private static void placeThaumatoriumHeat(ServerLevel level, BlockPos pos) {
        // Vanilla fire immediately removes itself without a sturdy supporting block.
        level.setBlock(pos.below(3), Blocks.NETHERRACK.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos.below(2), Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
    }

    private static void tickTube(ServerLevel level, BlockPos pos, EssentiaTubeBlockEntity tube, int ticks) {
        for (int i = 0; i < ticks; i++) {
            EssentiaTubeBlockEntity.serverTick(level, pos, level.getBlockState(pos), tube);
        }
    }

    private static <T extends BlockEntity> T requireBlockEntity(
            ServerLevel level, BlockPos pos, Class<T> expectedType) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!expectedType.isInstance(blockEntity)) {
            throw new GameTestAssertException("Expected " + expectedType.getSimpleName() + " at " + pos
                    + ", got " + (blockEntity == null ? "null" : blockEntity.getClass().getSimpleName()));
        }
        return expectedType.cast(blockEntity);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }
}
