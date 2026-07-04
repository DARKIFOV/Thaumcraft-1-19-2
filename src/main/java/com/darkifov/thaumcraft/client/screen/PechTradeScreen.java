package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.menu.PechTradeMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PechTradeScreen extends AbstractContainerScreen<PechTradeMenu> {
    private static final ResourceLocation ORIGINAL_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, \"textures/gui/pech_trade.png\");

    public PechTradeScreen(PechTradeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        
        this.imageWidth = 256;
        this.imageHeight = 256;imageWidth = 176;
        imageHeight = 198;
        inventoryLabelY = 104;
    }

    @Override
    protected void init() {
        super.init();

        int buttonY = topPos + 36;

        for (int i = 1; i <= 5; i++) {
            final int tier = i;
            addRenderableWidget(new Button(leftPos + 10 + (i - 1) * 31, buttonY, 28, 18, Component.literal("T" + i), button -> {
                ThaumcraftNetwork.requestPechTradeFromClient(menu.pechEntityId(), tier);
            }));
        }

        addRenderableWidget(new Button(leftPos + 35, topPos + 62, 106, 18, Component.literal("Подарить предмет"), button -> {
            ThaumcraftNetwork.requestPechGiftFromClient(menu.pechEntityId());
        }));
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        int x = leftPos + (imageWidth - 256) / 2;
        int y = topPos + (imageHeight - 256) / 2;
        OriginalGuiTextures.blitOriginal(poseStack, x, y, OriginalGuiTextures.PECH_TRADE, 256, 256);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
