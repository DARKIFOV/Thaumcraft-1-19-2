package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

final class OriginalGuiTextures {
    private OriginalGuiTextures() {
    }

    static final ResourceLocation THAUMONOMICON = tex("gui_researchbook.png");
    static final ResourceLocation RESEARCH_TABLE = tex("guiresearchtable2.png");
    static final ResourceLocation RESEARCH_TABLE_TC4_ORIGINAL = tex("thaumcraft_core_original/guiresearchtable2.png");
    static final ResourceLocation RESEARCH_TABLE_OVERLAY = tex("research_table_overlay.png");
    static final ResourceLocation HEX1 = tex("thaumcraft_core_original/hex1.png");
    static final ResourceLocation HEX2 = tex("thaumcraft_core_original/hex2.png");
    static final ResourceLocation ARCANE_WORKBENCH = tex("thaumcraft_core_original/gui_arcaneworkbench.png");
    static final ResourceLocation PECH_TRADE = tex("pech_trade.png");
    static final ResourceLocation ESSENTIA_TERMINAL = tex("essentia_terminal.png");
    static final ResourceLocation ESSENTIA_DRIVE = tex("essentia_drive.png");
    static final ResourceLocation OSMOTIC_ENCHANTER = tex("osmotic_enchanter.png");
    static final ResourceLocation TRANSVECTOR_INTERFACE = tex("summon_tablet.png");
    static final ResourceLocation BOTTOMLESS_POUCH = tex("bottomless_pouch.png");

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/gui/" + name);
    }

    static void bind(ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
    }

    static void blitOriginal(PoseStack poseStack, int x, int y, ResourceLocation texture, int width, int height) {
        bind(texture);
        GuiComponent.blit(poseStack, x, y, 0, 0, width, height, width, height);
    }

    static void blitOriginalRegion(PoseStack poseStack, int x, int y, ResourceLocation texture, int u, int v, int width, int height, int sheetWidth, int sheetHeight) {
        bind(texture);
        GuiComponent.blit(poseStack, x, y, u, v, width, height, sheetWidth, sheetHeight);
    }
}
