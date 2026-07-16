package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.menu.AlchemicalFurnaceMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Exact TC4 Alchemical Furnace GUI texture and gauge layout adapted to 1.19.2. */
public class AlchemicalFurnaceScreen extends AbstractContainerScreen<AlchemicalFurnaceMenu> {
    private static final ResourceLocation GUI =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/gui/gui_alchemyfurnace.png");

    public AlchemicalFurnaceScreen(AlchemicalFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI);
        RenderSystem.enableBlend();
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if (menu.isLit()) {
            int burn = menu.litProgress(20);
            blit(poseStack, leftPos + 80, topPos + 26 + 20 - burn,
                    176, 20 - burn, 16, burn);
        }

        int cook = menu.burnProgress(46);
        blit(poseStack, leftPos + 106, topPos + 13 + 46 - cook,
                216, 46 - cook, 9, cook);

        int contents = menu.essentiaProgress(48);
        blit(poseStack, leftPos + 61, topPos + 12 + 48 - contents,
                200, 48 - contents, 8, contents);

        // Glass frame over the essentia column.
        blit(poseStack, leftPos + 60, topPos + 8, 232, 0, 10, 55);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // The original TC4 GUI has no foreground labels.
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
