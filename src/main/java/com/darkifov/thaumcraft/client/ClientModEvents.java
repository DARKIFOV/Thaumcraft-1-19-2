package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.screen.ArcaneWorkbenchContainerScreen;
import com.darkifov.thaumcraft.client.screen.BottomlessPouchScreen;
import com.darkifov.thaumcraft.client.screen.EssentiaDriveScreen;
import com.darkifov.thaumcraft.client.screen.EssentiaTerminalScreen;
import com.darkifov.thaumcraft.client.screen.OsmoticEnchanterScreen;
import com.darkifov.thaumcraft.client.screen.PechTradeScreen;
import com.darkifov.thaumcraft.client.screen.TransvectorInterfaceScreen;
import com.darkifov.thaumcraft.client.render.ArcanePedestalRenderer;
import com.darkifov.thaumcraft.client.render.CrucibleRenderer;
import com.darkifov.thaumcraft.client.render.AlembicRenderer;
import com.darkifov.thaumcraft.client.render.AuraNodeRenderer;
import com.darkifov.thaumcraft.client.render.EssentiaJarRenderer;
import com.darkifov.thaumcraft.client.render.PechRenderer;
import com.darkifov.thaumcraft.client.render.TaintCrawlerRenderer;
import com.darkifov.thaumcraft.client.render.TC4BlockMobRenderer;
import com.darkifov.thaumcraft.client.render.ThaumGolemRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
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
            BlockEntityRenderers.register(ThaumcraftMod.ARCANE_PEDESTAL_BLOCK_ENTITY.get(), ArcanePedestalRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.ALEMBIC_BLOCK_ENTITY.get(), AlembicRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.AURA_NODE_BLOCK_ENTITY.get(), AuraNodeRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.ESSENTIA_JAR_BLOCK_ENTITY.get(), EssentiaJarRenderer::new);
            BlockEntityRenderers.register(ThaumcraftMod.CRUCIBLE_BLOCK_ENTITY.get(), CrucibleRenderer::new);
            EntityRenderers.register(ThaumcraftMod.THAUM_GOLEM.get(), ThaumGolemRenderer::new);
            EntityRenderers.register(ThaumcraftMod.TAINT_CRAWLER.get(), TaintCrawlerRenderer::new);
            EntityRenderers.register(ThaumcraftMod.PECH.get(), PechRenderer::new);
            EntityRenderers.register(ThaumcraftMod.ELDRITCH_GUARDIAN.get(), ctx -> new TC4BlockMobRenderer<>(ctx, () -> ThaumcraftMod.ELDRITCH_STONE.get().defaultBlockState(), 0.75F, 1.55F));
            EntityRenderers.register(ThaumcraftMod.CRIMSON_CULTIST.get(), ctx -> new TC4BlockMobRenderer<>(ctx, () -> ThaumcraftMod.OBSIDIAN_TOTEM.get().defaultBlockState(), 0.42F, 1.45F));
            EntityRenderers.register(ThaumcraftMod.CRIMSON_KNIGHT.get(), ctx -> new TC4BlockMobRenderer<>(ctx, () -> ThaumcraftMod.OBSIDIAN_TILE.get().defaultBlockState(), 0.50F, 1.55F));
            EntityRenderers.register(ThaumcraftMod.CRIMSON_CLERIC.get(), ctx -> new TC4BlockMobRenderer<>(ctx, () -> ThaumcraftMod.NITOR_LIGHT.get().defaultBlockState(), 0.44F, 1.45F));
            EntityRenderers.register(ThaumcraftMod.CRIMSON_PRAETOR.get(), ctx -> new TC4BlockMobRenderer<>(ctx, () -> ThaumcraftMod.ELDRITCH_OBELISK.get().defaultBlockState(), 0.58F, 1.70F));
            MenuScreens.register(ThaumcraftMod.ARCANE_WORKBENCH_MENU.get(), ArcaneWorkbenchContainerScreen::new);
            MenuScreens.register(ThaumcraftMod.PECH_TRADE_MENU.get(), PechTradeScreen::new);
            MenuScreens.register(ThaumcraftMod.ESSENTIA_TERMINAL_MENU.get(), EssentiaTerminalScreen::new);
            MenuScreens.register(ThaumcraftMod.ESSENTIA_DRIVE_MENU.get(), EssentiaDriveScreen::new);
            MenuScreens.register(ThaumcraftMod.OSMOTIC_ENCHANTER_MENU.get(), OsmoticEnchanterScreen::new);
            MenuScreens.register(ThaumcraftMod.TRANSVECTOR_INTERFACE_MENU.get(), TransvectorInterfaceScreen::new);
            MenuScreens.register(ThaumcraftMod.BOTTOMLESS_POUCH_MENU.get(), BottomlessPouchScreen::new);
        });
    }
}
