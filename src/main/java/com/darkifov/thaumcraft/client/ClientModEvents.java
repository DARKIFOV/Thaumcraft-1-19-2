package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TC4TallowCandleParity;
import com.darkifov.thaumcraft.block.TC4ArcaneLevitatorParity;
import com.darkifov.thaumcraft.block.TC4ArcaneLevitatorEffectsBridge;
import com.darkifov.thaumcraft.client.screen.AlchemicalFurnaceScreen;
import com.darkifov.thaumcraft.client.screen.ArcaneWorkbenchContainerScreen;
import com.darkifov.thaumcraft.client.screen.ArcaneSpaScreen;
import com.darkifov.thaumcraft.client.screen.ArcaneBoreScreen;
import com.darkifov.thaumcraft.client.screen.BottomlessPouchScreen;
import com.darkifov.thaumcraft.client.screen.HandMirrorScreen;
import com.darkifov.thaumcraft.client.screen.FocusPouchScreen;
import com.darkifov.thaumcraft.client.screen.HoverHarnessScreen;
import com.darkifov.thaumcraft.client.screen.FocalManipulatorScreen;
import com.darkifov.thaumcraft.client.screen.GolemScreen;
import com.darkifov.thaumcraft.client.screen.EssentiaDriveScreen;
import com.darkifov.thaumcraft.client.screen.EssentiaTerminalScreen;
import com.darkifov.thaumcraft.client.screen.OsmoticEnchanterScreen;
import com.darkifov.thaumcraft.client.screen.PechTradeScreen;
import com.darkifov.thaumcraft.client.screen.ResearchTableContainerScreen;
import com.darkifov.thaumcraft.client.screen.DeconstructionTableScreen;
import com.darkifov.thaumcraft.client.screen.TransvectorInterfaceScreen;
import com.darkifov.thaumcraft.client.screen.ThaumatoriumScreen;
import com.darkifov.thaumcraft.client.fx.TC4PurifyingBubbleParticle;
import com.darkifov.thaumcraft.client.fx.TC4BrainJarFullParticle;
import com.darkifov.thaumcraft.client.fx.TC4ArcaneLevitatorSparkleParticle;
import com.darkifov.thaumcraft.client.fx.TC4ArcaneLevitatorClientEffects;
import com.darkifov.thaumcraft.client.render.ArcanePedestalRenderer;
import com.darkifov.thaumcraft.client.render.ArcaneWorkbenchRenderer;
import com.darkifov.thaumcraft.client.render.TC4WandPedestalRenderer;
import com.darkifov.thaumcraft.client.render.ManaPodRenderer;
import com.darkifov.thaumcraft.client.render.ArcaneBoreRenderer;
import com.darkifov.thaumcraft.client.render.ArcaneBoreBaseRenderer;
import com.darkifov.thaumcraft.client.render.ArcaneLampRenderer;
import com.darkifov.thaumcraft.client.render.TC4EssentiaLampRenderer;
import com.darkifov.thaumcraft.client.render.AspectOrbRenderer;
import com.darkifov.thaumcraft.client.render.CrucibleRenderer;
import com.darkifov.thaumcraft.client.render.AlembicRenderer;
import com.darkifov.thaumcraft.client.render.AlchemicalCentrifugeRenderer;
import com.darkifov.thaumcraft.client.render.ThaumatoriumRenderer;
import com.darkifov.thaumcraft.client.render.EssentiaCrystalizerRenderer;
import com.darkifov.thaumcraft.client.render.AuraNodeRenderer;
import com.darkifov.thaumcraft.client.render.NodeJarRenderer;
import com.darkifov.thaumcraft.client.render.EssentiaJarRenderer;
import com.darkifov.thaumcraft.client.render.BrainJarRenderer;
import com.darkifov.thaumcraft.client.render.BellowsRenderer;
import com.darkifov.thaumcraft.client.render.EssentiaReservoirRenderer;
import com.darkifov.thaumcraft.client.render.EssentiaTubeRenderer;
import com.darkifov.thaumcraft.client.render.InfusionMatrixRenderer;
import com.darkifov.thaumcraft.client.render.NodeStabilizerRenderer;
import com.darkifov.thaumcraft.client.render.NodeTransducerRenderer;
import com.darkifov.thaumcraft.client.render.FocalManipulatorRenderer;
import com.darkifov.thaumcraft.client.render.FumeDissipatorRenderer;
import com.darkifov.thaumcraft.client.render.PechRenderer;
import com.darkifov.thaumcraft.client.render.ResearchTableRenderer;
import com.darkifov.thaumcraft.client.render.HungryChestRenderer;
import com.darkifov.thaumcraft.client.render.TallowCandleRenderer;
import com.darkifov.thaumcraft.client.render.TC4BannerRenderer;
import com.darkifov.thaumcraft.client.render.VisChargeRelayRenderer;
import com.darkifov.thaumcraft.client.render.VisRelayRenderer;
import com.darkifov.thaumcraft.client.render.WardedGlassRenderer;
import com.darkifov.thaumcraft.client.render.TaintCrawlerRenderer;
import com.darkifov.thaumcraft.client.render.TaintChickenRenderer;
import com.darkifov.thaumcraft.client.render.TaintCowRenderer;
import com.darkifov.thaumcraft.client.render.TaintCreeperRenderer;
import com.darkifov.thaumcraft.client.render.TaintPigRenderer;
import com.darkifov.thaumcraft.client.render.TaintSheepRenderer;
import com.darkifov.thaumcraft.client.render.TaintVillagerRenderer;
import com.darkifov.thaumcraft.client.render.TaintSporeRenderer;
import com.darkifov.thaumcraft.client.render.TaintSporeSwarmerRenderer;
import com.darkifov.thaumcraft.client.render.TaintSwarmRenderer;
import com.darkifov.thaumcraft.client.render.TC4BlockMobRenderer;
import com.darkifov.thaumcraft.client.render.TC4BrainyZombieRenderer;
import com.darkifov.thaumcraft.client.render.TC4CrimsonCultistRenderer;
import com.darkifov.thaumcraft.client.render.TC4FocusProjectileRenderer;
import com.darkifov.thaumcraft.client.render.TC4FallingTaintRenderer;
import com.darkifov.thaumcraft.client.render.TC4GolemBobberRenderer;
import com.darkifov.thaumcraft.client.render.TC4InhabitedZombieRenderer;
import com.darkifov.thaumcraft.client.render.TC4PrimalArrowRenderer;
import com.darkifov.thaumcraft.client.render.TC4GolemDartRenderer;
import com.darkifov.thaumcraft.client.render.TC4FireBatRenderer;
import com.darkifov.thaumcraft.client.render.TC4WispRenderer;
import com.darkifov.thaumcraft.client.render.TC4ThaumicSlimeRenderer;
import com.darkifov.thaumcraft.client.render.TC4FrostShardRenderer;
import com.darkifov.thaumcraft.client.render.TC4EldritchOrbRenderer;
import com.darkifov.thaumcraft.client.render.TC4EldritchGuardianRenderer;
import com.darkifov.thaumcraft.client.render.TC4EldritchCrabRenderer;
import com.darkifov.thaumcraft.client.render.model.TC4EldritchBossLayerDefinitions;
import com.darkifov.thaumcraft.client.render.model.TC4FireBatModel;
import com.darkifov.thaumcraft.client.render.model.TC4ManaPodModel;
import com.darkifov.thaumcraft.client.render.model.TC4InfusionMatrixModel;
import com.darkifov.thaumcraft.client.render.model.TC4HungryChestModel;
import com.darkifov.thaumcraft.client.render.model.TC4BrainJarModel;
import com.darkifov.thaumcraft.client.render.model.TC4BrainJarBrineModel;
import com.darkifov.thaumcraft.client.render.model.TC4BellowsModel;
import com.darkifov.thaumcraft.client.render.model.TC4ArcaneBoreModel;
import com.darkifov.thaumcraft.client.render.model.TC4ArcaneBoreCoreModel;
import com.darkifov.thaumcraft.client.render.model.TC4CentrifugeModel;
import com.darkifov.thaumcraft.client.render.model.TC4ThaumGolemModel;
import com.darkifov.thaumcraft.client.render.model.TC4GolemAccessoriesModel;
import com.darkifov.thaumcraft.client.render.model.TC4CrimsonCultistModel;
import com.darkifov.thaumcraft.client.render.model.TC4FortressArmorModel;
import com.darkifov.thaumcraft.client.render.model.TC4HoverHarnessArmorModel;
import com.darkifov.thaumcraft.client.render.model.TC4TravelingTrunkModel;
import com.darkifov.thaumcraft.client.render.model.TC4TaintSporeModel;
import com.darkifov.thaumcraft.client.render.model.TC4TaintSporeSwarmerModel;
import com.darkifov.thaumcraft.client.render.TC4EldritchWardenRenderer;
import com.darkifov.thaumcraft.client.render.TC4EldritchGolemRenderer;
import com.darkifov.thaumcraft.client.render.TC4CultistPortalRenderer;
import com.darkifov.thaumcraft.client.render.TC4TaintacleGiantRenderer;
import com.darkifov.thaumcraft.client.render.TC4TaintacleRenderer;
import com.darkifov.thaumcraft.client.render.TC4MindSpiderRenderer;
import com.darkifov.thaumcraft.client.render.TC4EldritchTileRenderer;
import com.darkifov.thaumcraft.client.render.TC4FortressArmorLayer;
import com.darkifov.thaumcraft.client.render.TC4GogglesLayer;
import com.darkifov.thaumcraft.client.render.TC4HoverHarnessClientExtension;
import com.darkifov.thaumcraft.client.render.TC4HoverHarnessLayer;
import com.darkifov.thaumcraft.client.render.ThaumGolemRenderer;
import com.darkifov.thaumcraft.client.render.TravelingTrunkRenderer;
import com.darkifov.thaumcraft.client.render.WardedBlockRenderer;
import com.darkifov.thaumcraft.client.render.EldritchPortalRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import java.util.function.Function;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }


    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.register(ThaumcraftMod.PURIFYING_BUBBLE_PARTICLE.get(),
                TC4PurifyingBubbleParticle.Provider::new);
        event.register(ThaumcraftMod.BRAIN_JAR_FULL_PARTICLE.get(),
                TC4BrainJarFullParticle.Provider::new);
        event.register(ThaumcraftMod.ARCANE_LEVITATOR_SPARKLE_PARTICLE.get(),
                TC4ArcaneLevitatorSparkleParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager ->
                UnnaturalHungerPostEffect.invalidateResources());
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            TC4ArcaneLevitatorEffectsBridge.install(TC4ArcaneLevitatorClientEffects::spawn);
            ItemProperties.register(ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_bonebow").get(),
                    new ResourceLocation(ThaumcraftMod.MOD_ID, "pulling"),
                    (stack, level, living, seed) -> living != null && living.isUsingItem()
                            && living.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_bonebow").get(),
                    new ResourceLocation(ThaumcraftMod.MOD_ID, "pull"),
                    (stack, level, living, seed) -> living == null ? 0.0F
                            : com.darkifov.thaumcraft.item.BoneBowItem.getPullModelValue(
                                    stack.getUseDuration() - living.getUseItemRemainingTicks()));
            ItemProperties.register(ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_sinister_stone").get(),
                    new ResourceLocation(ThaumcraftMod.MOD_ID, "active"),
                    (stack, level, living, seed) -> com.darkifov.thaumcraft.item.TC4SinisterStoneItem.modelActive(stack));
            ItemProperties.register(ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_sinister_stone_active").get(),
                    new ResourceLocation(ThaumcraftMod.MOD_ID, "active"),
                    (stack, level, living, seed) -> 1.0F);

            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ARCANE_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ARCANE_LEVITATOR.get(), RenderType.cutout());

            // v11.62.8 jar visual parity: TC4 jars use translucent glass and a
            // block-entity liquid/label overlay, not an opaque full cube.
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ESSENTIA_JAR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.BRAIN_JAR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.MAGIC_MIRROR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ESSENTIA_MIRROR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.FILTERED_ESSENTIA_JAR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.VOID_ESSENTIA_JAR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.NITOR_LIGHT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.CRUCIBLE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.GREATWOOD_LEAVES.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.SILVERWOOD_LEAVES.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.GREATWOOD_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.SILVERWOOD_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TC4_SHIMMERLEAF.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TC4_CINDERPEARL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TC4_ETHEREAL_BLOOM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TC4_VISHROOM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TC4_MANA_POD.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TAINT_FIBRES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ITEM_GRATE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.WARDED_GLASS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.PURIFYING_FLUID.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.FLOWING_PURIFYING_FLUID.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.LIQUID_DEATH_FLUID.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.FLOWING_LIQUID_DEATH_FLUID.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.VIS_CHARGE_RELAY.get(), RenderType.cutout());

            // v11.63.10: explicit fallback layers mirror model JSON render_type entries.
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ADVANCED_ALCHEMICAL_FURNACE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.AER_CRYSTAL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.AMBER_BRICKS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.AQUA_CRYSTAL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ARCANE_CRAFTING_TERMINAL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.BELLOWS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ELDRITCH_PORTAL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ESSENTIA_CONVERSION_MONITOR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ESSENTIA_EXPORT_BUS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ESSENTIA_IMPORT_BUS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ESSENTIA_LEVEL_EMITTER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ESSENTIA_STORAGE_BUS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ESSENTIA_STORAGE_MONITOR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ESSENTIA_TERMINAL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.EXTRAS_LIGHT_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.EXTRAS_WATER_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.FLUX_GAS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.FLUX_GOO.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.FUME_DISSIPATOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.GOLEM_SEAL_COLLECT_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.IGNIS_CRYSTAL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.MATRIX_ACCELERATOR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.MATRIX_STABILIZER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.MNEMONIC_MATRIX.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.NITOR_LIGHT.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ORDO_CRYSTAL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.PERDITIO_CRYSTAL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TAINT_FIBRES.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.BALANCED_CRYSTAL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TCE_CACTUS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TCE_WARDED_GLASS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TERRA_CRYSTAL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.THAUMIC_CRAFTING_CPU.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.THAUMIC_ME_CABLE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.THAUMIC_ME_CONTROLLER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TT_ENCHANTER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TT_FIRE_AIR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TT_FIRE_CHAOS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TT_FIRE_EARTH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TT_FIRE_ORDER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TT_FIRE_WATER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TT_FUNNEL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TT_GASEOUS_LIGHT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TT_GASEOUS_SHADOW.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TT_MOB_MAGNET.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TT_NITOR_GAS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TT_REPAIRER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TT_WARP_GATE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.VIS_INTERFACE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE_ORANGE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE_MAGENTA.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE_LIGHT_BLUE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE_YELLOW.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE_LIME.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE_PINK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE_GRAY.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE_LIGHT_GRAY.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE_CYAN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE_PURPLE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE_BLUE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE_BROWN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE_GREEN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE_RED.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.TALLOW_CANDLE_BLACK.get(), RenderType.cutout());
            BlockEntityRenderers.register(ThaumcraftMod.ARCANE_WORKBENCH_BLOCK_ENTITY.get(), blockEntityRenderer(ArcaneWorkbenchRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.ARCANE_PEDESTAL_BLOCK_ENTITY.get(), blockEntityRenderer(ArcanePedestalRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.TC4_WAND_PEDESTAL_BLOCK_ENTITY.get(), blockEntityRenderer(TC4WandPedestalRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.MANA_POD_BLOCK_ENTITY.get(), blockEntityRenderer(ManaPodRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.BELLOWS_BLOCK_ENTITY.get(), blockEntityRenderer(BellowsRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.ARCANE_BORE_BLOCK_ENTITY.get(), blockEntityRenderer(ArcaneBoreRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.ARCANE_BORE_BASE_BLOCK_ENTITY.get(), blockEntityRenderer(ArcaneBoreBaseRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.ARCANE_LAMP_BLOCK_ENTITY.get(), blockEntityRenderer(ArcaneLampRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.TC4_ESSENTIA_LAMP_BLOCK_ENTITY.get(), blockEntityRenderer(TC4EssentiaLampRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.HUNGRY_CHEST_BLOCK_ENTITY.get(), blockEntityRenderer(HungryChestRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.TALLOW_CANDLE_BLOCK_ENTITY.get(), blockEntityRenderer(TallowCandleRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.TC4_BANNER_BLOCK_ENTITY.get(), blockEntityRenderer(TC4BannerRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.FUME_DISSIPATOR_BLOCK_ENTITY.get(), blockEntityRenderer(FumeDissipatorRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.VIS_RELAY_BLOCK_ENTITY.get(), blockEntityRenderer(VisRelayRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.VIS_CHARGE_RELAY_BLOCK_ENTITY.get(), blockEntityRenderer(VisChargeRelayRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.WARDED_GLASS_BLOCK_ENTITY.get(), blockEntityRenderer(WardedGlassRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.ALEMBIC_BLOCK_ENTITY.get(), blockEntityRenderer(AlembicRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.ALCHEMICAL_CENTRIFUGE_BLOCK_ENTITY.get(), blockEntityRenderer(AlchemicalCentrifugeRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.THAUMATORIUM_BLOCK_ENTITY.get(), blockEntityRenderer(ThaumatoriumRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.ESSENTIA_CRYSTALIZER_BLOCK_ENTITY.get(), blockEntityRenderer(EssentiaCrystalizerRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.AURA_NODE_BLOCK_ENTITY.get(), blockEntityRenderer(AuraNodeRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.NODE_JAR_BLOCK_ENTITY.get(), blockEntityRenderer(NodeJarRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.ESSENTIA_JAR_BLOCK_ENTITY.get(), blockEntityRenderer(EssentiaJarRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.BRAIN_JAR_BLOCK_ENTITY.get(), blockEntityRenderer(BrainJarRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.ESSENTIA_RESERVOIR_BLOCK_ENTITY.get(), blockEntityRenderer(EssentiaReservoirRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.ESSENTIA_TUBE_BLOCK_ENTITY.get(), blockEntityRenderer(EssentiaTubeRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.CRUCIBLE_BLOCK_ENTITY.get(), blockEntityRenderer(CrucibleRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.INFUSION_MATRIX_BLOCK_ENTITY.get(), blockEntityRenderer(InfusionMatrixRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.NODE_STABILIZER_BLOCK_ENTITY.get(), blockEntityRenderer(NodeStabilizerRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.NODE_TRANSDUCER_BLOCK_ENTITY.get(), blockEntityRenderer(NodeTransducerRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.RESEARCH_TABLE_BLOCK_ENTITY.get(), blockEntityRenderer(ResearchTableRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.FOCAL_MANIPULATOR_BLOCK_ENTITY.get(), blockEntityRenderer(FocalManipulatorRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.WARDED_BLOCK_ENTITY.get(), blockEntityRenderer(WardedBlockRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.ELDRITCH_PORTAL_BLOCK_ENTITY.get(), blockEntityRenderer(EldritchPortalRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.ELDRITCH_CAP_BLOCK_ENTITY.get(), blockEntityRenderer(TC4EldritchTileRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.ELDRITCH_LOCK_BLOCK_ENTITY.get(), blockEntityRenderer(TC4EldritchTileRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.ELDRITCH_TRAP_BLOCK_ENTITY.get(), blockEntityRenderer(TC4EldritchTileRenderer::new));
            BlockEntityRenderers.register(ThaumcraftMod.ELDRITCH_CRYSTAL_BLOCK_ENTITY.get(), blockEntityRenderer(TC4EldritchTileRenderer::new));
            EntityRenderers.register(ThaumcraftMod.SPECIAL_ITEM.get(),
                    entityRenderer(net.minecraft.client.renderer.entity.ItemEntityRenderer::new));
            EntityRenderers.register(ThaumcraftMod.PERMANENT_ITEM.get(),
                    entityRenderer(net.minecraft.client.renderer.entity.ItemEntityRenderer::new));
            EntityRenderers.register(ThaumcraftMod.FOLLOWING_ITEM.get(),
                    entityRenderer(net.minecraft.client.renderer.entity.ItemEntityRenderer::new));
            EntityRenderers.register(ThaumcraftMod.ASPECT_ORB.get(), entityRenderer(AspectOrbRenderer::new));
            EntityRenderers.register(ThaumcraftMod.TRAVELING_TRUNK.get(), entityRenderer(TravelingTrunkRenderer::new));
            EntityRenderers.register(ThaumcraftMod.THAUM_GOLEM.get(), entityRenderer(ThaumGolemRenderer::new));
            EntityRenderers.register(ThaumcraftMod.TAINT_CRAWLER.get(), entityRenderer(TaintCrawlerRenderer::new));
            EntityRenderers.register(ThaumcraftMod.TAINT_CHICKEN.get(), entityRenderer(TaintChickenRenderer::new));
            EntityRenderers.register(ThaumcraftMod.TAINT_COW.get(), entityRenderer(TaintCowRenderer::new));
            EntityRenderers.register(ThaumcraftMod.TAINT_CREEPER.get(), entityRenderer(TaintCreeperRenderer::new));
            EntityRenderers.register(ThaumcraftMod.TAINT_PIG.get(), entityRenderer(TaintPigRenderer::new));
            EntityRenderers.register(ThaumcraftMod.TAINT_SHEEP.get(), entityRenderer(TaintSheepRenderer::new));
            EntityRenderers.register(ThaumcraftMod.TAINT_VILLAGER.get(), entityRenderer(TaintVillagerRenderer::new));
            EntityRenderers.register(ThaumcraftMod.TAINT_SPORE.get(), entityRenderer(TaintSporeRenderer::new));
            EntityRenderers.register(ThaumcraftMod.TAINT_SWARMER.get(), entityRenderer(TaintSporeSwarmerRenderer::new));
            EntityRenderers.register(ThaumcraftMod.TAINT_SWARM.get(), entityRenderer(TaintSwarmRenderer::new));
            EntityRenderers.register(ThaumcraftMod.FALLING_TAINT.get(), entityRenderer(TC4FallingTaintRenderer::new));
            EntityRenderers.register(ThaumcraftMod.PECH.get(), entityRenderer(PechRenderer::new));
            EntityRenderers.register(ThaumcraftMod.ELDRITCH_GUARDIAN.get(), entityRenderer(TC4EldritchGuardianRenderer::new));
            EntityRenderers.register(ThaumcraftMod.ELDRITCH_CRAB.get(), entityRenderer(TC4EldritchCrabRenderer::new));
            EntityRenderers.register(ThaumcraftMod.MIND_SPIDER.get(), entityRenderer(TC4MindSpiderRenderer::new));
            EntityRenderers.register(ThaumcraftMod.ELDRITCH_WARDEN.get(), entityRenderer(TC4EldritchWardenRenderer::new));
            EntityRenderers.register(ThaumcraftMod.ELDRITCH_GOLEM.get(), entityRenderer(TC4EldritchGolemRenderer::new));
            EntityRenderers.register(ThaumcraftMod.CRIMSON_CULTIST.get(), entityRenderer(TC4CrimsonCultistRenderer::new));
            EntityRenderers.register(ThaumcraftMod.CRIMSON_KNIGHT.get(), entityRenderer(TC4CrimsonCultistRenderer::new));
            EntityRenderers.register(ThaumcraftMod.CRIMSON_CLERIC.get(), entityRenderer(TC4CrimsonCultistRenderer::new));
            EntityRenderers.register(ThaumcraftMod.CRIMSON_PRAETOR.get(), entityRenderer(TC4CrimsonCultistRenderer::new));
            EntityRenderers.register(ThaumcraftMod.CULTIST_PORTAL.get(), entityRenderer(TC4CultistPortalRenderer::new));
            EntityRenderers.register(ThaumcraftMod.TAINTACLE.get(), entityRenderer(ctx -> new TC4TaintacleRenderer<>(ctx)));
            EntityRenderers.register(ThaumcraftMod.TAINTACLE_SMALL.get(), entityRenderer(ctx -> new TC4TaintacleRenderer<>(ctx)));
            EntityRenderers.register(ThaumcraftMod.TAINTACLE_GIANT.get(), entityRenderer(TC4TaintacleGiantRenderer::new));
            EntityRenderers.register(ThaumcraftMod.FIREBAT.get(), entityRenderer(TC4FireBatRenderer::new));
            EntityRenderers.register(ThaumcraftMod.WISP.get(), entityRenderer(TC4WispRenderer::new));
            EntityRenderers.register(ThaumcraftMod.THAUMIC_SLIME.get(), entityRenderer(context ->
                    castEntityRenderer(new TC4ThaumicSlimeRenderer(context))));
            EntityRenderers.register(ThaumcraftMod.BRAINY_ZOMBIE.get(),
                    entityRenderer(ctx -> new TC4BrainyZombieRenderer<com.darkifov.thaumcraft.entity.BrainyZombieEntity>(ctx)));
            EntityRenderers.register(ThaumcraftMod.GIANT_BRAINY_ZOMBIE.get(),
                    entityRenderer(ctx -> new TC4BrainyZombieRenderer<com.darkifov.thaumcraft.entity.GiantBrainyZombieEntity>(ctx)));
            EntityRenderers.register(ThaumcraftMod.INHABITED_ZOMBIE.get(),
                    entityRenderer(ctx -> castEntityRenderer(new TC4InhabitedZombieRenderer(ctx))));
            EntityRenderers.register(ThaumcraftMod.ALUMENTUM_PROJECTILE.get(),
                    entityRenderer(ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 1.0F, true)));
            EntityRenderers.register(ThaumcraftMod.BOTTLE_TAINT_PROJECTILE.get(),
                    entityRenderer(ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 0.8F, false)));
            EntityRenderers.register(ThaumcraftMod.PRIMAL_ARROW.get(),
                    entityRenderer(TC4PrimalArrowRenderer::new));
            EntityRenderers.register(ThaumcraftMod.GOLEM_DART.get(),
                    entityRenderer(TC4GolemDartRenderer::new));
            EntityRenderers.register(ThaumcraftMod.GOLEM_BOBBER.get(),
                    entityRenderer(TC4GolemBobberRenderer::new));
            EntityRenderers.register(ThaumcraftMod.FOCUS_PECH_BLAST.get(), entityRenderer(ctx -> new TC4FocusProjectileRenderer<>(ctx)));
            EntityRenderers.register(ThaumcraftMod.FOCUS_EMBER.get(), entityRenderer(ctx -> new TC4FocusProjectileRenderer<>(ctx)));
            EntityRenderers.register(ThaumcraftMod.FOCUS_FROST_SHARD.get(), entityRenderer(TC4FrostShardRenderer::new));
            EntityRenderers.register(ThaumcraftMod.FOCUS_EXPLOSIVE_ORB.get(), entityRenderer(ctx -> new TC4FocusProjectileRenderer<>(ctx)));
            EntityRenderers.register(ThaumcraftMod.FOCUS_SHOCK_ORB.get(), entityRenderer(ctx -> new TC4FocusProjectileRenderer<>(ctx)));
            EntityRenderers.register(ThaumcraftMod.FOCUS_PRIMAL_ORB.get(), entityRenderer(ctx -> new TC4FocusProjectileRenderer<>(ctx)));
            EntityRenderers.register(ThaumcraftMod.ELDRITCH_ORB.get(), entityRenderer(ctx -> new TC4EldritchOrbRenderer<>(ctx)));
            EntityRenderers.register(ThaumcraftMod.GOLEM_ORB.get(), entityRenderer(ctx -> new TC4EldritchOrbRenderer<>(ctx)));
            MenuScreens.register(ThaumcraftMod.ALCHEMICAL_FURNACE_MENU.get(), screenConstructor(AlchemicalFurnaceScreen::new));
            MenuScreens.register(ThaumcraftMod.ARCANE_WORKBENCH_MENU.get(), screenConstructor(ArcaneWorkbenchContainerScreen::new));
            MenuScreens.register(ThaumcraftMod.ARCANE_SPA_MENU.get(), screenConstructor(ArcaneSpaScreen::new));
            MenuScreens.register(ThaumcraftMod.ARCANE_BORE_MENU.get(), screenConstructor(ArcaneBoreScreen::new));
            MenuScreens.register(ThaumcraftMod.FOCAL_MANIPULATOR_MENU.get(), screenConstructor(FocalManipulatorScreen::new));
            MenuScreens.register(ThaumcraftMod.RESEARCH_TABLE_MENU.get(), screenConstructor(ResearchTableContainerScreen::new));
            MenuScreens.register(ThaumcraftMod.DECONSTRUCTION_TABLE_MENU.get(), screenConstructor(DeconstructionTableScreen::new));
            MenuScreens.register(ThaumcraftMod.THAUMATORIUM_MENU.get(), screenConstructor(ThaumatoriumScreen::new));
            MenuScreens.register(ThaumcraftMod.PECH_TRADE_MENU.get(), screenConstructor(PechTradeScreen::new));
            MenuScreens.register(ThaumcraftMod.ESSENTIA_TERMINAL_MENU.get(), screenConstructor(EssentiaTerminalScreen::new));
            MenuScreens.register(ThaumcraftMod.ESSENTIA_DRIVE_MENU.get(), screenConstructor(EssentiaDriveScreen::new));
            MenuScreens.register(ThaumcraftMod.OSMOTIC_ENCHANTER_MENU.get(), screenConstructor(OsmoticEnchanterScreen::new));
            MenuScreens.register(ThaumcraftMod.TRANSVECTOR_INTERFACE_MENU.get(), screenConstructor(TransvectorInterfaceScreen::new));
            MenuScreens.register(ThaumcraftMod.BOTTOMLESS_POUCH_MENU.get(), screenConstructor(BottomlessPouchScreen::new));
            MenuScreens.register(ThaumcraftMod.HAND_MIRROR_MENU.get(), screenConstructor(HandMirrorScreen::new));
            MenuScreens.register(ThaumcraftMod.FOCUS_POUCH_MENU.get(), screenConstructor(FocusPouchScreen::new));
            MenuScreens.register(ThaumcraftMod.HOVER_HARNESS_MENU.get(), screenConstructor(HoverHarnessScreen::new));
            MenuScreens.register(ThaumcraftMod.GOLEM_MENU.get(), screenConstructor(GolemScreen::new));
        });
    }


    /**
     * Keeps Minecraft's obfuscated renderer-provider SAM out of invokedynamic.
     *
     * <p>The emergency production patcher used before a full ForgeGradle build
     * remapped ordinary method declarations and references but not the name of
     * an invokedynamic call site. A direct {@code Renderer::new} therefore
     * generated a lambda that implemented {@code create} while the SRG runtime
     * expected {@code m_173570_}, causing an {@link AbstractMethodError} during
     * the initial resource reload. The anonymous adapter has a real override,
     * which both ForgeGradle and the fallback remapper can rename safely. The
     * inner factory is a JDK {@link Function}; its {@code apply} method is not
     * obfuscated.</p>
     */
    private static <T extends BlockEntity> BlockEntityRendererProvider<T> blockEntityRenderer(
            Function<BlockEntityRendererProvider.Context, ? extends BlockEntityRenderer<T>> factory) {
        return new BlockEntityRendererProvider<>() {
            @Override
            public BlockEntityRenderer<T> create(BlockEntityRendererProvider.Context context) {
                return factory.apply(context);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> EntityRenderer<T> castEntityRenderer(EntityRenderer<?> renderer) {
        return (EntityRenderer<T>) renderer;
    }

    private static <T extends Entity> EntityRendererProvider<T> entityRenderer(
            Function<EntityRendererProvider.Context, ? extends EntityRenderer<T>> factory) {
        return new EntityRendererProvider<>() {
            @Override
            public EntityRenderer<T> create(EntityRendererProvider.Context context) {
                return factory.apply(context);
            }
        };
    }

    @FunctionalInterface
    private interface ScreenFactory<M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> {
        S create(M menu, Inventory inventory, Component title);
    }

    private static <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>>
    MenuScreens.ScreenConstructor<M, S> screenConstructor(ScreenFactory<M, S> factory) {
        return new MenuScreens.ScreenConstructor<>() {
            @Override
            public S create(M menu, Inventory inventory, Component title) {
                return factory.create(menu, inventory, title);
            }
        };
    }

    @FunctionalInterface
    private interface BlockColorFactory {
        int color(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex);
    }

    private static BlockColor blockColor(BlockColorFactory factory) {
        return new BlockColor() {
            @Override
            public int getColor(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) {
                return factory.color(state, level, pos, tintIndex);
            }
        };
    }

    @FunctionalInterface
    private interface ItemColorFactory {
        int color(ItemStack stack, int tintIndex);
    }

    private static ItemColor itemColor(ItemColorFactory factory) {
        return new ItemColor() {
            @Override
            public int getColor(ItemStack stack, int tintIndex) {
                return factory.color(stack, tintIndex);
            }
        };
    }


    private static final int TC4_GREATWOOD_LEAF_TINT = 0x4F7E38;
    private static final int TC4_SILVERWOOD_LEAF_TINT = 0xD9F0F0;

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        // v11.62.7 magic-tree parity: TC4 greatwood leaves are a greyscale
        // foliage mask and must be tinted on the client. Without this handler
        // the block renders pale/white in-game. Keep silverwood nearly white-blue
        // so its already-blue TC4 texture is preserved.
        event.register(blockColor((state, level, pos, tintIndex) -> tintIndex == 0 ? TC4_GREATWOOD_LEAF_TINT : FoliageColor.getDefaultColor()),
                ThaumcraftMod.GREATWOOD_LEAVES.get());
        event.register(blockColor((state, level, pos, tintIndex) -> tintIndex == 0 ? TC4_SILVERWOOD_LEAF_TINT : 0xFFFFFF),
                ThaumcraftMod.SILVERWOOD_LEAVES.get());
        event.register(blockColor((state, level, pos, tintIndex) -> switch (tintIndex) {
                    case 0 -> TC4ArcaneLevitatorParity.WORLD_TOP_GLOW;
                    case 1 -> TC4ArcaneLevitatorParity.WORLD_SIDE_GLOW;
                    default -> 0xFFFFFF;
                }), ThaumcraftMod.ARCANE_LEVITATOR.get());
        event.register(blockColor((state, level, pos, tintIndex) -> {
                    if (tintIndex != 0) {
                        return 0xFFFFFF;
                    }
                    net.minecraft.resources.ResourceLocation key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                    return TC4TallowCandleParity.color(TC4TallowCandleParity.legacyMetadata(
                            key == null ? null : key.getPath()));
                }),
                ThaumcraftMod.TALLOW_CANDLE.get(), ThaumcraftMod.TALLOW_CANDLE_ORANGE.get(),
                ThaumcraftMod.TALLOW_CANDLE_MAGENTA.get(), ThaumcraftMod.TALLOW_CANDLE_LIGHT_BLUE.get(),
                ThaumcraftMod.TALLOW_CANDLE_YELLOW.get(), ThaumcraftMod.TALLOW_CANDLE_LIME.get(),
                ThaumcraftMod.TALLOW_CANDLE_PINK.get(), ThaumcraftMod.TALLOW_CANDLE_GRAY.get(),
                ThaumcraftMod.TALLOW_CANDLE_LIGHT_GRAY.get(), ThaumcraftMod.TALLOW_CANDLE_CYAN.get(),
                ThaumcraftMod.TALLOW_CANDLE_PURPLE.get(), ThaumcraftMod.TALLOW_CANDLE_BLUE.get(),
                ThaumcraftMod.TALLOW_CANDLE_BROWN.get(), ThaumcraftMod.TALLOW_CANDLE_GREEN.get(),
                ThaumcraftMod.TALLOW_CANDLE_RED.get(), ThaumcraftMod.TALLOW_CANDLE_BLACK.get());
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(itemColor((stack, tintIndex) -> tintIndex == 0 ? TC4_GREATWOOD_LEAF_TINT : 0xFFFFFF),
                ThaumcraftMod.GREATWOOD_LEAVES.get());
        event.register(itemColor((stack, tintIndex) -> tintIndex == 0 ? TC4_SILVERWOOD_LEAF_TINT : 0xFFFFFF),
                ThaumcraftMod.SILVERWOOD_LEAVES.get());
        event.register(itemColor((stack, tintIndex) -> switch (tintIndex) {
                    case 0 -> TC4ArcaneLevitatorParity.INVENTORY_TOP_GLOW;
                    case 1 -> TC4ArcaneLevitatorParity.INVENTORY_SIDE_GLOW;
                    default -> 0xFFFFFF;
                }), ThaumcraftMod.ARCANE_LEVITATOR_ITEM.get());
        event.register(itemColor((stack, tintIndex) -> {
                    if (tintIndex != 0) {
                        return 0xFFFFFF;
                    }
                    net.minecraft.resources.ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    return TC4TallowCandleParity.color(TC4TallowCandleParity.legacyMetadata(
                            key == null ? null : key.getPath()));
                }),
                ThaumcraftMod.TALLOW_CANDLE.get(), ThaumcraftMod.TALLOW_CANDLE_ORANGE.get(),
                ThaumcraftMod.TALLOW_CANDLE_MAGENTA.get(), ThaumcraftMod.TALLOW_CANDLE_LIGHT_BLUE.get(),
                ThaumcraftMod.TALLOW_CANDLE_YELLOW.get(), ThaumcraftMod.TALLOW_CANDLE_LIME.get(),
                ThaumcraftMod.TALLOW_CANDLE_PINK.get(), ThaumcraftMod.TALLOW_CANDLE_GRAY.get(),
                ThaumcraftMod.TALLOW_CANDLE_LIGHT_GRAY.get(), ThaumcraftMod.TALLOW_CANDLE_CYAN.get(),
                ThaumcraftMod.TALLOW_CANDLE_PURPLE.get(), ThaumcraftMod.TALLOW_CANDLE_BLUE.get(),
                ThaumcraftMod.TALLOW_CANDLE_BROWN.get(), ThaumcraftMod.TALLOW_CANDLE_GREEN.get(),
                ThaumcraftMod.TALLOW_CANDLE_RED.get(), ThaumcraftMod.TALLOW_CANDLE_BLACK.get());
        event.register(itemColor((stack, tintIndex) -> tintIndex == 0 ? com.darkifov.thaumcraft.block.EssentiaCrystalItem.tint(stack) : 0xFFFFFF),
                ThaumcraftMod.ESSENTIA_CRYSTAL.get());
        event.register(itemColor((stack, tintIndex) -> tintIndex == 1
                        ? com.darkifov.thaumcraft.item.WispEssenceItem.tint(stack) : 0xFFFFFF),
                ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_wispessence").get());
        event.register(itemColor(com.darkifov.thaumcraft.item.TC4EssenceItem::tint),
                ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_essence").get());
        event.register(itemColor((stack, tintIndex) -> tintIndex == 0
                        ? com.darkifov.thaumcraft.item.simple.TC4ManaBeanItem.tint(stack) : 0xFFFFFF),
                ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_mana_bean").get());
    }


    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TC4FireBatModel.LAYER, TC4FireBatModel::createBodyLayer);
        event.registerLayerDefinition(TC4ManaPodModel.LAYER, TC4ManaPodModel::createBodyLayer);
        event.registerLayerDefinition(TC4InfusionMatrixModel.LAYER, TC4InfusionMatrixModel::createBodyLayer);
        event.registerLayerDefinition(TC4HungryChestModel.LAYER, TC4HungryChestModel::createBodyLayer);
        event.registerLayerDefinition(TC4BrainJarModel.LAYER, TC4BrainJarModel::createBodyLayer);
        event.registerLayerDefinition(TC4BrainJarBrineModel.LAYER, TC4BrainJarBrineModel::createBodyLayer);
        event.registerLayerDefinition(TC4BellowsModel.FRAME_LAYER, TC4BellowsModel::createFrameLayer);
        event.registerLayerDefinition(TC4BellowsModel.BAG_LAYER, TC4BellowsModel::createBagLayer);
        event.registerLayerDefinition(TC4ArcaneBoreModel.BORE_LAYER, TC4ArcaneBoreModel::createBoreLayer);
        event.registerLayerDefinition(TC4ArcaneBoreModel.BASE_LAYER, TC4ArcaneBoreModel::createBaseLayer);
        event.registerLayerDefinition(TC4ArcaneBoreCoreModel.LAYER, TC4ArcaneBoreCoreModel::createLayer);
        event.registerLayerDefinition(TC4CentrifugeModel.LAYER, TC4CentrifugeModel::createBodyLayer);
        event.registerLayerDefinition(TC4ThaumGolemModel.LAYER, TC4ThaumGolemModel::createBodyLayer);
        event.registerLayerDefinition(TC4GolemAccessoriesModel.LAYER, TC4GolemAccessoriesModel::createBodyLayer);
        event.registerLayerDefinition(TC4CrimsonCultistModel.BASE, TC4CrimsonCultistModel::createBaseLayer);
        event.registerLayerDefinition(TC4CrimsonCultistModel.ROBE, TC4CrimsonCultistModel::createRobeLayer);
        event.registerLayerDefinition(TC4CrimsonCultistModel.KNIGHT, TC4CrimsonCultistModel::createKnightLayer);
        event.registerLayerDefinition(TC4CrimsonCultistModel.LEADER, TC4CrimsonCultistModel::createLeaderLayer);
        event.registerLayerDefinition(TC4FortressArmorModel.LAYER, TC4FortressArmorModel::createBodyLayer);
        event.registerLayerDefinition(TC4HoverHarnessArmorModel.LAYER, TC4HoverHarnessArmorModel::createBodyLayer);
        event.registerLayerDefinition(TC4TravelingTrunkModel.LAYER, TC4TravelingTrunkModel::createBodyLayer);
        event.registerLayerDefinition(TC4TaintSporeModel.LAYER, TC4TaintSporeModel::createBodyLayer);
        event.registerLayerDefinition(TC4TaintSporeSwarmerModel.LAYER, TC4TaintSporeSwarmerModel::createBodyLayer);
        event.registerLayerDefinition(TC4EldritchBossLayerDefinitions.ELDRITCH_GUARDIAN, TC4EldritchBossLayerDefinitions::createGuardianBodyLayer);
        event.registerLayerDefinition(TC4EldritchBossLayerDefinitions.ELDRITCH_WARDEN, TC4EldritchBossLayerDefinitions::createGuardianBodyLayer);
        event.registerLayerDefinition(TC4EldritchBossLayerDefinitions.ELDRITCH_GOLEM, TC4EldritchBossLayerDefinitions::createGolemBodyLayer);
        event.registerLayerDefinition(TC4EldritchBossLayerDefinitions.ELDRITCH_CRAB, TC4EldritchBossLayerDefinitions::createCrabBodyLayer);
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        TC4HoverHarnessClientExtension.bake(event.getEntityModels());
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new TC4GogglesLayer(renderer));
                renderer.addLayer(new TC4FortressArmorLayer(renderer));
                renderer.addLayer(new TC4HoverHarnessLayer(renderer));
            }
        }
    }

}
