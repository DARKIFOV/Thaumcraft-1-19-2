package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.item.TC4SanityCheckerItem;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Modern renderer for TC4 {@code ClientTickEventsFML#renderSanityHud}.
 *
 * <p>The original 20x76 frame from {@code hud.png} is drawn at (1,1). The
 * 48-pixel gauge is partitioned into temporary, sticky and permanent warp,
 * preserving the original violet tints and the 100+ warp cap marker.</p>
 */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SanityCheckerOverlayEvents {
    private static final int FRAME_X = 1;
    private static final int FRAME_Y = 1;
    private static final int BAR_X = 7;
    private static final int BAR_Y = 21;
    private static final int BAR_HEIGHT = 48;

    private SanityCheckerOverlayEvents() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }

        ItemStack held = minecraft.player.getMainHandItem();
        if (!(held.getItem() instanceof TC4SanityCheckerItem)) {
            return;
        }

        int permanent = Math.max(0, ClientResearchData.permanentWarp());
        int sticky = Math.max(0, ClientResearchData.stickyWarp());
        int temporary = Math.max(0, ClientResearchData.temporaryWarp());
        float total = permanent + sticky + temporary;
        float scale = 1.0F;
        if (total > 100.0F) {
            scale = 100.0F / total;
            total = 100.0F;
        }

        int gap = (int) ((100.0F - total) / 100.0F * BAR_HEIGHT);
        int temporaryHeight = Math.min(BAR_HEIGHT - gap, (int) (temporary / 100.0F * BAR_HEIGHT * scale));
        int stickyHeight = Math.min(BAR_HEIGHT - gap - temporaryHeight,
                (int) (sticky / 100.0F * BAR_HEIGHT * scale));
        int permanentHeight = permanent > 0
                ? Math.max(0, BAR_HEIGHT - gap - temporaryHeight - stickyHeight)
                : 0;

        PoseStack poseStack = event.getPoseStack();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TC4AuraNodeHudParity.ORIGINAL_HUD);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        GuiComponent.blit(poseStack, FRAME_X, FRAME_Y, 152, 0, 20, 76, 256, 256);
        drawBar(poseStack, gap, temporaryHeight, 1.0F, 0.5F, 1.0F);
        drawBar(poseStack, gap + temporaryHeight, stickyHeight, 0.75F, 0.0F, 0.75F);
        drawBar(poseStack, gap + temporaryHeight + stickyHeight, permanentHeight, 0.5F, 0.0F, 0.5F);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(poseStack, FRAME_X, FRAME_Y, 176, 0, 20, 76, 256, 256);
        if (total >= 100.0F) {
            GuiComponent.blit(poseStack, FRAME_X, FRAME_Y, 216, 0, 20, 16, 256, 256);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static void drawBar(PoseStack poseStack, int sourceOffset, int height,
                                float red, float green, float blue) {
        if (height <= 0) {
            return;
        }
        RenderSystem.setShaderColor(red, green, blue, 1.0F);
        GuiComponent.blit(poseStack, BAR_X, BAR_Y + sourceOffset,
                200, sourceOffset, 8, height, 256, 256);
    }
}
