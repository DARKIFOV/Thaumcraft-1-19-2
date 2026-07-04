package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.menu.PechTradeMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PechTradeScreen extends AbstractContainerScreen<PechTradeMenu> {
    public PechTradeScreen(PechTradeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
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
        fill(poseStack, leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xEE160F20);
        fill(poseStack, leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFFE8D4A7);
        fill(poseStack, leftPos + 8, topPos + 18, leftPos + imageWidth - 8, topPos + 92, 0x884B2E58);

        font.draw(poseStack, Component.literal("Торговля Печа").withStyle(ChatFormatting.GOLD), leftPos + 12, topPos + 10, 0xF2DFB2);
        font.draw(poseStack, "Выбери Tier жетона:", leftPos + 12, topPos + 25, 0xF2DFB2);
        font.draw(poseStack, "T1-T5 ищут жетон в инвентаре.", leftPos + 12, topPos + 86, 0x4A2A11);
        font.draw(poseStack, "Подарок берёт предмет из руки.", leftPos + 12, topPos + 96, 0x4A2A11);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
