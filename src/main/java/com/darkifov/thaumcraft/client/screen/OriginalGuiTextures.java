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
    static final ResourceLocation RESEARCH_PARCHMENT = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/misc/parchment3.png");
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

    /** Draws TC4's 255x255 research table exactly as the original two blits. */
    static void blitTc4ResearchTableBackground(PoseStack poseStack, int x, int y) {
        // GuiResearchTable 4.2.3.5: upper 255x167 region, then the lower
        // inventory strip begins at source row 166 and destination y+167.
        blitOriginalRegion(poseStack, x, y, RESEARCH_TABLE_TC4_ORIGINAL,
                0, 0, 255, 167, 256, 256);
        blitOriginalRegion(poseStack, x + 40, y + 167, RESEARCH_TABLE_TC4_ORIGINAL,
                0, 166, 184, 88, 256, 256);
    }

    /** TC4 GuiResearchTable.drawSheet: source 0,0; destination x+94,y+8; 150x150. */
    static void blitTc4ResearchParchment(PoseStack poseStack, int x, int y) {
        blitOriginalRegion(poseStack, x + 94, y + 8, RESEARCH_PARCHMENT,
                0, 0, 150, 150, 256, 256);
    }

    static void blitOriginalTinted(PoseStack poseStack, int x, int y, ResourceLocation texture, int width, int height, int rgb) {
        blitOriginalTintedAlpha(poseStack, x, y, texture, width, height, rgb, 1.0F);
    }

    static void blitOriginalTintedAlpha(PoseStack poseStack, int x, int y, ResourceLocation texture, int width, int height, int rgb, float alpha) {
        float red = ((rgb >> 16) & 255) / 255.0F;
        float green = ((rgb >> 8) & 255) / 255.0F;
        float blue = (rgb & 255) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(red, green, blue, Math.max(0.0F, Math.min(1.0F, alpha)));
        blitOriginal(poseStack, x, y, texture, width, height);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    static void blitOriginalRegion(PoseStack poseStack, int x, int y, ResourceLocation texture, int u, int v, int width, int height, int sheetWidth, int sheetHeight) {
        bind(texture);
        GuiComponent.blit(poseStack, x, y, u, v, width, height, sheetWidth, sheetHeight);
    }
    static void blitOriginalScaledRegion(PoseStack poseStack, int x, int y, ResourceLocation texture, int u, int v, int sourceWidth, int sourceHeight, int destWidth, int destHeight, int sheetWidth, int sheetHeight) {
        bind(texture);
        GuiComponent.blit(poseStack, x, y, destWidth, destHeight, (float) u, (float) v, sourceWidth, sourceHeight, sheetWidth, sheetHeight);
    }

    /** Draws the complete source image at a different destination size without corrupting its UVs. */
    static void blitOriginalScaled(PoseStack poseStack, int x, int y, ResourceLocation texture,
                                   int sourceWidth, int sourceHeight, int destWidth, int destHeight) {
        blitOriginalScaledRegion(poseStack, x, y, texture, 0, 0,
                sourceWidth, sourceHeight, destWidth, destHeight, sourceWidth, sourceHeight);
    }

    static void blitOriginalScaledTintedAlpha(PoseStack poseStack, int x, int y, ResourceLocation texture,
                                              int sourceWidth, int sourceHeight, int destWidth, int destHeight,
                                              int rgb, float alpha) {
        blitOriginalScaledTintedAlpha(poseStack, x, y, texture, 0, 0, sourceWidth, sourceHeight,
                destWidth, destHeight, sourceWidth, sourceHeight, rgb, alpha);
    }

    static void blitOriginalScaledTintedAlpha(PoseStack poseStack, int x, int y, ResourceLocation texture,
                                              int u, int v, int sourceWidth, int sourceHeight,
                                              int destWidth, int destHeight, int sheetWidth, int sheetHeight,
                                              int rgb, float alpha) {
        float red = ((rgb >> 16) & 255) / 255.0F;
        float green = ((rgb >> 8) & 255) / 255.0F;
        float blue = (rgb & 255) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(red, green, blue, Math.max(0.0F, Math.min(1.0F, alpha)));
        blitOriginalScaledRegion(poseStack, x, y, texture, u, v, sourceWidth, sourceHeight,
                destWidth, destHeight, sheetWidth, sheetHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

}
