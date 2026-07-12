package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.screen.ArcaneWorkbenchContainerScreen;
import com.darkifov.thaumcraft.client.screen.BottomlessPouchScreen;
import com.darkifov.thaumcraft.client.screen.FocusPouchScreen;
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
import com.darkifov.thaumcraft.client.render.ArcanePedestalRenderer;
import com.darkifov.thaumcraft.client.render.AspectOrbRenderer;
import com.darkifov.thaumcraft.client.render.CrucibleRenderer;
import com.darkifov.thaumcraft.client.render.AlembicRenderer;
import com.darkifov.thaumcraft.client.render.AlchemicalCentrifugeRenderer;
import com.darkifov.thaumcraft.client.render.EssentiaCrystalizerRenderer;
import com.darkifov.thaumcraft.client.render.AuraNodeRenderer;
import com.darkifov.thaumcraft.client.render.EssentiaJarRenderer;
import com.darkifov.thaumcraft.client.render.EssentiaReservoirRenderer;
import com.darkifov.thaumcraft.client.render.EssentiaTubeRenderer;
import com.darkifov.thaumcraft.client.render.InfusionMatrixRenderer;
import com.darkifov.thaumcraft.client.render.NodeStabilizerRenderer;
import com.darkifov.thaumcraft.client.render.NodeTransducerRenderer;
import com.darkifov.thaumcraft.client.render.FocalManipulatorRenderer;
import com.darkifov.thaumcraft.client.render.PechRenderer;
import com.darkifov.thaumcraft.client.render.ResearchTableRenderer;
import com.darkifov.thaumcraft.client.render.TaintCrawlerRenderer;
import com.darkifov.thaumcraft.client.render.TC4BlockMobRenderer;
import com.darkifov.thaumcraft.client.render.TC4FocusProjectileRenderer;
import com.darkifov.thaumcraft.client.render.TC4FireBatRenderer;
import com.darkifov.thaumcraft.client.render.TC4FrostShardRenderer;
import com.darkifov.thaumcraft.client.render.TC4EldritchOrbRenderer;
import com.darkifov.thaumcraft.client.render.TC4EldritchGuardianRenderer;
import com.darkifov.thaumcraft.client.render.TC4EldritchCrabRenderer;
import com.darkifov.thaumcraft.client.render.model.TC4EldritchBossLayerDefinitions;
import com.darkifov.thaumcraft.client.render.model.TC4FireBatModel;
import com.darkifov.thaumcraft.client.render.model.TC4InfusionMatrixModel;
import com.darkifov.thaumcraft.client.render.model.TC4ThaumGolemModel;
import com.darkifov.thaumcraft.client.render.model.TC4GolemAccessoriesModel;
import com.darkifov.thaumcraft.client.render.TC4EldritchWardenRenderer;
import com.darkifov.thaumcraft.client.render.TC4EldritchGolemRenderer;
import com.darkifov.thaumcraft.client.render.TC4CultistPortalRenderer;
import com.darkifov.thaumcraft.client.render.TC4TaintacleGiantRenderer;
import com.darkifov.thaumcraft.client.render.TC4TaintacleRenderer;
import com.darkifov.thaumcraft.client.render.TC4MindSpiderRenderer;
import com.darkifov.thaumcraft.client.render.TC4EldritchTileRenderer;
import com.darkifov.thaumcraft.client.render.TC4FortressArmorLayer;
import com.darkifov.thaumcraft.client.render.TC4GogglesLayer;
import com.darkifov.thaumcraft.client.render.ThaumGolemRenderer;
import com.darkifov.thaumcraft.client.render.WardedBlockRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraft.world.level.FoliageColor;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // v11.62.8 jar visual parity: TC4 jars use translucent glass and a
            // block-entity liquid/label overlay, not an opaque full cube.
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.ESSENTIA_JAR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.FILTERED_ESSENTIA_JAR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.VOID_ESSENTIA_JAR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.NITOR_LIGHT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.GREATWOOD_LEAVES.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.SILVERWOOD_LEAVES.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.GREATWOOD_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ThaumcraftMod.SILVERWOOD_SAPLING.get(), RenderType.cutout());
            BlockEntityRenderers.register(ThaumcraftMod.ARCANE_PEDESTAL_BLOCK_ENTITY.get(), ArcanePedestalRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.ALEMBIC_BLOCK_ENTITY.get(), AlembicRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.ALCHEMICAL_CENTRIFUGE_BLOCK_ENTITY.get(), AlchemicalCentrifugeRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.ESSENTIA_CRYSTALIZER_BLOCK_ENTITY.get(), EssentiaCrystalizerRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.AURA_NODE_BLOCK_ENTITY.get(), AuraNodeRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.ESSENTIA_JAR_BLOCK_ENTITY.get(), EssentiaJarRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.ESSENTIA_RESERVOIR_BLOCK_ENTITY.get(), EssentiaReservoirRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.ESSENTIA_TUBE_BLOCK_ENTITY.get(), EssentiaTubeRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.CRUCIBLE_BLOCK_ENTITY.get(), CrucibleRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.INFUSION_MATRIX_BLOCK_ENTITY.get(), InfusionMatrixRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.NODE_STABILIZER_BLOCK_ENTITY.get(), NodeStabilizerRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.NODE_TRANSDUCER_BLOCK_ENTITY.get(), NodeTransducerRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.RESEARCH_TABLE_BLOCK_ENTITY.get(), ResearchTableRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.FOCAL_MANIPULATOR_BLOCK_ENTITY.get(), FocalManipulatorRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.WARDED_BLOCK_ENTITY.get(), WardedBlockRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.ELDRITCH_CAP_BLOCK_ENTITY.get(), TC4EldritchTileRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.ELDRITCH_LOCK_BLOCK_ENTITY.get(), TC4EldritchTileRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.ELDRITCH_TRAP_BLOCK_ENTITY.get(), TC4EldritchTileRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.ELDRITCH_CRYSTAL_BLOCK_ENTITY.get(), TC4EldritchTileRenderer::new);
            EntityRenderers.register(ThaumcraftMod.ASPECT_ORB.get(), AspectOrbRenderer::new);
            EntityRenderers.register(ThaumcraftMod.THAUM_GOLEM.get(), ThaumGolemRenderer::new);
            EntityRenderers.register(ThaumcraftMod.TAINT_CRAWLER.get(), TaintCrawlerRenderer::new);
            EntityRenderers.register(ThaumcraftMod.PECH.get(), PechRenderer::new);
            EntityRenderers.register(ThaumcraftMod.ELDRITCH_GUARDIAN.get(), TC4EldritchGuardianRenderer::new);
            EntityRenderers.register(ThaumcraftMod.ELDRITCH_CRAB.get(), TC4EldritchCrabRenderer::new);
            EntityRenderers.register(ThaumcraftMod.MIND_SPIDER.get(), TC4MindSpiderRenderer::new);
            EntityRenderers.register(ThaumcraftMod.ELDRITCH_WARDEN.get(), TC4EldritchWardenRenderer::new);
            EntityRenderers.register(ThaumcraftMod.ELDRITCH_GOLEM.get(), TC4EldritchGolemRenderer::new);
            EntityRenderers.register(ThaumcraftMod.CRIMSON_CULTIST.get(), ctx -> new TC4BlockMobRenderer<>(ctx, () -> ThaumcraftMod.OBSIDIAN_TOTEM.get().defaultBlockState(), 0.42F, 1.45F));
            EntityRenderers.register(ThaumcraftMod.CRIMSON_KNIGHT.get(), ctx -> new TC4BlockMobRenderer<>(ctx, () -> ThaumcraftMod.OBSIDIAN_TILE.get().defaultBlockState(), 0.50F, 1.55F));
            EntityRenderers.register(ThaumcraftMod.CRIMSON_CLERIC.get(), ctx -> new TC4BlockMobRenderer<>(ctx, () -> ThaumcraftMod.NITOR_LIGHT.get().defaultBlockState(), 0.44F, 1.45F));
            EntityRenderers.register(ThaumcraftMod.CRIMSON_PRAETOR.get(), ctx -> new TC4BlockMobRenderer<>(ctx, () -> ThaumcraftMod.ELDRITCH_OBELISK.get().defaultBlockState(), 0.58F, 1.70F));
            EntityRenderers.register(ThaumcraftMod.CULTIST_PORTAL.get(), TC4CultistPortalRenderer::new);
            EntityRenderers.register(ThaumcraftMod.TAINTACLE.get(), ctx -> new TC4TaintacleRenderer<>(ctx));
            EntityRenderers.register(ThaumcraftMod.TAINTACLE_SMALL.get(), ctx -> new TC4TaintacleRenderer<>(ctx));
            EntityRenderers.register(ThaumcraftMod.TAINTACLE_GIANT.get(), TC4TaintacleGiantRenderer::new);
            EntityRenderers.register(ThaumcraftMod.FIREBAT.get(), TC4FireBatRenderer::new);
            EntityRenderers.register(ThaumcraftMod.FOCUS_PECH_BLAST.get(), ctx -> new TC4FocusProjectileRenderer<>(ctx));
            EntityRenderers.register(ThaumcraftMod.FOCUS_EMBER.get(), ctx -> new TC4FocusProjectileRenderer<>(ctx));
            EntityRenderers.register(ThaumcraftMod.FOCUS_FROST_SHARD.get(), TC4FrostShardRenderer::new);
            EntityRenderers.register(ThaumcraftMod.FOCUS_EXPLOSIVE_ORB.get(), ctx -> new TC4FocusProjectileRenderer<>(ctx));
            EntityRenderers.register(ThaumcraftMod.FOCUS_SHOCK_ORB.get(), ctx -> new TC4FocusProjectileRenderer<>(ctx));
            EntityRenderers.register(ThaumcraftMod.FOCUS_PRIMAL_ORB.get(), ctx -> new TC4FocusProjectileRenderer<>(ctx));
            EntityRenderers.register(ThaumcraftMod.ELDRITCH_ORB.get(), ctx -> new TC4EldritchOrbRenderer<>(ctx));
            EntityRenderers.register(ThaumcraftMod.GOLEM_ORB.get(), ctx -> new TC4EldritchOrbRenderer<>(ctx));
            MenuScreens.register(ThaumcraftMod.ARCANE_WORKBENCH_MENU.get(), ArcaneWorkbenchContainerScreen::new);
            MenuScreens.register(ThaumcraftMod.FOCAL_MANIPULATOR_MENU.get(), FocalManipulatorScreen::new);
            MenuScreens.register(ThaumcraftMod.RESEARCH_TABLE_MENU.get(), ResearchTableContainerScreen::new);
            MenuScreens.register(ThaumcraftMod.DECONSTRUCTION_TABLE_MENU.get(), DeconstructionTableScreen::new);
            MenuScreens.register(ThaumcraftMod.THAUMATORIUM_MENU.get(), ThaumatoriumScreen::new);
            MenuScreens.register(ThaumcraftMod.PECH_TRADE_MENU.get(), PechTradeScreen::new);
            MenuScreens.register(ThaumcraftMod.ESSENTIA_TERMINAL_MENU.get(), EssentiaTerminalScreen::new);
            MenuScreens.register(ThaumcraftMod.ESSENTIA_DRIVE_MENU.get(), EssentiaDriveScreen::new);
            MenuScreens.register(ThaumcraftMod.OSMOTIC_ENCHANTER_MENU.get(), OsmoticEnchanterScreen::new);
            MenuScreens.register(ThaumcraftMod.TRANSVECTOR_INTERFACE_MENU.get(), TransvectorInterfaceScreen::new);
            MenuScreens.register(ThaumcraftMod.BOTTOMLESS_POUCH_MENU.get(), BottomlessPouchScreen::new);
            MenuScreens.register(ThaumcraftMod.FOCUS_POUCH_MENU.get(), FocusPouchScreen::new);
            MenuScreens.register(ThaumcraftMod.GOLEM_MENU.get(), GolemScreen::new);
        });
    }


    private static final int TC4_GREATWOOD_LEAF_TINT = 0x4F7E38;
    private static final int TC4_SILVERWOOD_LEAF_TINT = 0xD9F0F0;

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        // v11.62.7 magic-tree parity: TC4 greatwood leaves are a greyscale
        // foliage mask and must be tinted on the client. Without this handler
        // the block renders pale/white in-game. Keep silverwood nearly white-blue
        // so its already-blue TC4 texture is preserved.
        event.register((state, level, pos, tintIndex) -> tintIndex == 0 ? TC4_GREATWOOD_LEAF_TINT : FoliageColor.getDefaultColor(),
                ThaumcraftMod.GREATWOOD_LEAVES.get());
        event.register((state, level, pos, tintIndex) -> tintIndex == 0 ? TC4_SILVERWOOD_LEAF_TINT : 0xFFFFFF,
                ThaumcraftMod.SILVERWOOD_LEAVES.get());
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? TC4_GREATWOOD_LEAF_TINT : 0xFFFFFF,
                ThaumcraftMod.GREATWOOD_LEAVES.get());
        event.register((stack, tintIndex) -> tintIndex == 0 ? TC4_SILVERWOOD_LEAF_TINT : 0xFFFFFF,
                ThaumcraftMod.SILVERWOOD_LEAVES.get());
        event.register((stack, tintIndex) -> tintIndex == 0 ? com.darkifov.thaumcraft.block.EssentiaCrystalItem.tint(stack) : 0xFFFFFF,
                ThaumcraftMod.ESSENTIA_CRYSTAL.get());
    }


    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TC4FireBatModel.LAYER, TC4FireBatModel::createBodyLayer);
        event.registerLayerDefinition(TC4InfusionMatrixModel.LAYER, TC4InfusionMatrixModel::createBodyLayer);
        event.registerLayerDefinition(TC4ThaumGolemModel.LAYER, TC4ThaumGolemModel::createBodyLayer);
        event.registerLayerDefinition(TC4GolemAccessoriesModel.LAYER, TC4GolemAccessoriesModel::createBodyLayer);
        event.registerLayerDefinition(TC4EldritchBossLayerDefinitions.ELDRITCH_GUARDIAN, TC4EldritchBossLayerDefinitions::createGuardianBodyLayer);
        event.registerLayerDefinition(TC4EldritchBossLayerDefinitions.ELDRITCH_WARDEN, TC4EldritchBossLayerDefinitions::createGuardianBodyLayer);
        event.registerLayerDefinition(TC4EldritchBossLayerDefinitions.ELDRITCH_GOLEM, TC4EldritchBossLayerDefinitions::createGolemBodyLayer);
        event.registerLayerDefinition(TC4EldritchBossLayerDefinitions.ELDRITCH_CRAB, TC4EldritchBossLayerDefinitions::createCrabBodyLayer);
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new TC4GogglesLayer(renderer));
                renderer.addLayer(new TC4FortressArmorLayer(renderer));
            }
        }
    }

}
