package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.menu.ArcaneSpaMenu;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

/** Client port of TC4 GuiSpa using the original 256x256 texture. */
public class ArcaneSpaScreen extends AbstractContainerScreen<ArcaneSpaMenu> {
    private static final ResourceLocation GUI =
            new ResourceLocation("thaumcraft", "textures/gui/gui_spa.png");
    private static final int TOGGLE_X = 89;
    private static final int TOGGLE_Y = 35;
    private static final int TOGGLE_SIZE = 8;
    private static final int TANK_X = 107;
    private static final int TANK_Y = 15;
    private static final int TANK_WIDTH = 8;
    private static final int TANK_HEIGHT = 48;

    public ArcaneSpaScreen(ArcaneSpaMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int iconV = menu.isMixing() ? 16 : 32;
        blit(poseStack, leftPos + TOGGLE_X, topPos + TOGGLE_Y,
                208, iconV, TOGGLE_SIZE, TOGGLE_SIZE);

        FluidStack fluid = menu.fluidStack();
        if (!fluid.isEmpty()) {
            // TC4 always tiled six complete 8x8 icons, then masked the empty top area.
            renderFluid(poseStack, fluid, leftPos + TANK_X, topPos + TANK_Y,
                    TANK_WIDTH, TANK_HEIGHT);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, GUI);
            int emptyMask = menu.emptyFluidMaskHeight(TANK_HEIGHT);
            if (emptyMask > 0) {
                blit(poseStack, leftPos + TANK_X, topPos + TANK_Y,
                        TANK_X, TANK_Y, 10, emptyMask);
            }
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI);
        blit(poseStack, leftPos + 106, topPos + 11, 232, 0, 10, 55);
        RenderSystem.disableBlend();
    }

    private static void renderFluid(PoseStack poseStack, FluidStack fluid,
                                    int x, int y, int width, int height) {
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation texture = extensions.getStillTexture();
        if (texture == null) {
            return;
        }

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
        int tint = extensions.getTintColor();
        float alpha = ((tint >> 24) & 0xFF) / 255.0F;
        float red = ((tint >> 16) & 0xFF) / 255.0F;
        float green = ((tint >> 8) & 0xFF) / 255.0F;
        float blue = (tint & 0xFF) / 255.0F;

        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShaderColor(red, green, blue, alpha);
        for (int offset = 0; offset < height; offset += 8) {
            int segment = Math.min(8, height - offset);
            blit(poseStack, x, y + offset, 0, width, segment, sprite);
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // Original GuiSpa intentionally draws no foreground labels.
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        if (inside(mouseX, mouseY, TANK_X - 3, 10, 10, 55)) {
            FluidStack fluid = menu.fluidStack();
            if (!fluid.isEmpty()) {
                renderComponentTooltip(poseStack, List.of(
                        fluid.getDisplayName(),
                        Component.literal(menu.fluidAmount() + " mb")
                ), mouseX, mouseY);
                return;
            }
        }
        if (inside(mouseX, mouseY, TOGGLE_X - 1, TOGGLE_Y - 1, 10, 10)) {
            renderComponentTooltip(poseStack, List.of(Component.translatable(
                    menu.isMixing() ? "text.spa.mix.true" : "text.spa.mix.false"
            )), mouseX, mouseY);
            return;
        }
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && inside(mouseX, mouseY, TOGGLE_X, TOGGLE_Y, TOGGLE_SIZE, TOGGLE_SIZE)) {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ArcaneSpaMenu.BUTTON_TOGGLE_MIX);
                if (minecraft.player != null) {
                    minecraft.player.playSound(TC4Sounds.event("cameraclack"), 0.4F, 1.0F);
                }
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        double localX = mouseX - leftPos;
        double localY = mouseY - topPos;
        return localX >= x && localY >= y && localX < x + width && localY < y + height;
    }
}
