package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.blockentity.TransvectorInterfaceBlockEntity;
import com.darkifov.thaumcraft.menu.TransvectorInterfaceMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TransvectorInterfaceScreen extends AbstractContainerScreen<TransvectorInterfaceMenu> {
    public TransvectorInterfaceScreen(TransvectorInterfaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 218;
        inventoryLabelY = 120;
    }

    @Override
    protected void init() {
        super.init();

        int x1 = leftPos + 8;
        int x2 = leftPos + 91;
        int y = topPos + 48;

        addRenderableWidget(new Button(x1, y, 78, 18, Component.literal("Статус"), button -> {
            ThaumcraftNetwork.requestTransvectorAction(menu.pos(), TransvectorInterfaceBlockEntity.ACTION_STATUS);
        }));

        addRenderableWidget(new Button(x2, y, 78, 18, Component.literal("Инспект"), button -> {
            ThaumcraftNetwork.requestTransvectorAction(menu.pos(), TransvectorInterfaceBlockEntity.ACTION_INSPECT);
        }));

        addRenderableWidget(new Button(x1, y + 21, 78, 18, Component.literal("Deep status"), button -> {
            ThaumcraftNetwork.requestTransvectorAction(menu.pos(), TransvectorInterfaceBlockEntity.ACTION_DEEP_STATUS);
        }));

        addRenderableWidget(new Button(x2, y + 21, 78, 18, Component.literal("Очистить"), button -> {
            ThaumcraftNetwork.requestTransvectorClear(menu.pos());
        }));

        addRenderableWidget(new Button(x1, y + 44, 78, 18, Component.literal("Jar → Cell"), button -> {
            ThaumcraftNetwork.requestTransvectorAction(menu.pos(), TransvectorInterfaceBlockEntity.ACTION_PULL_JAR_TO_CELL);
        }));

        addRenderableWidget(new Button(x2, y + 44, 78, 18, Component.literal("Cell → Jar"), button -> {
            ThaumcraftNetwork.requestTransvectorAction(menu.pos(), TransvectorInterfaceBlockEntity.ACTION_PUSH_CELL_TO_JAR);
        }));

        addRenderableWidget(new Button(x1, y + 66, 78, 18, Component.literal("Drive → Cell"), button -> {
            ThaumcraftNetwork.requestTransvectorAction(menu.pos(), TransvectorInterfaceBlockEntity.ACTION_PULL_DRIVE_TO_CELL);
        }));

        addRenderableWidget(new Button(x2, y + 66, 78, 18, Component.literal("Cell → Drive"), button -> {
            ThaumcraftNetwork.requestTransvectorAction(menu.pos(), TransvectorInterfaceBlockEntity.ACTION_PUSH_CELL_TO_DRIVE);
        }));
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        fill(poseStack, leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xEE100F22);
        fill(poseStack, leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF20152F);
        fill(poseStack, leftPos + 6, topPos + 18, leftPos + imageWidth - 6, topPos + 112, 0x884C2FA8);

        font.draw(poseStack, Component.literal("Transvector Interface").withStyle(ChatFormatting.LIGHT_PURPLE), leftPos + 12, topPos + 9, 0xF2DFB2);
        font.draw(poseStack, "Remote actions cost 4 Praecantatio vis.", leftPos + 12, topPos + 24, 0xE8D4A7);
        font.draw(poseStack, "Для transfer держи Digital Cell в руке.", leftPos + 12, topPos + 36, 0xCFEAFF);
        font.draw(poseStack, "Shift+ПКМ по блоку = быстрый inspect.", leftPos + 12, topPos + 112, 0xCFEAFF);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
