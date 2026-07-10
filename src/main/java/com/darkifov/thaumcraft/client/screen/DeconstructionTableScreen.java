package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.menu.DeconstructionTableMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DeconstructionTableScreen extends AbstractContainerScreen<DeconstructionTableMenu> {
    private static final ResourceLocation GUI = new ResourceLocation("thaumcraft", "textures/gui/gui_decontable.png");

    public DeconstructionTableScreen(DeconstructionTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int progress = menu.scaledBreakTime(46);
        if (progress > 0) blit(poseStack, leftPos + 93, topPos + 15 + 46 - progress, 176, 46 - progress, 9, progress);
        Aspect aspect = menu.outputAspect();
        if (aspect != null) {
            ResourceLocation icon = new ResourceLocation("thaumcraft", "textures/aspects/" + aspect.id() + ".png");
            OriginalGuiTextures.blitOriginal(poseStack, leftPos + 64, topPos + 48, icon, 16, 16);
        }
    }

    @Override protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) { }

    @Override public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        Aspect aspect = menu.outputAspect();
        if (aspect != null && mouseX >= leftPos + 64 && mouseX < leftPos + 80 && mouseY >= topPos + 48 && mouseY < topPos + 64) {
            renderTooltip(poseStack, Component.literal(aspect.displayName()), mouseX, mouseY);
        } else {
            renderTooltip(poseStack, mouseX, mouseY);
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && menu.outputAspect() != null
                && mouseX >= leftPos + 64 && mouseX < leftPos + 80
                && mouseY >= topPos + 48 && mouseY < topPos + 64) {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
