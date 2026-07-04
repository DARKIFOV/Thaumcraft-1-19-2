package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.menu.EssentiaTerminalMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EssentiaTerminalScreen extends AbstractContainerScreen<EssentiaTerminalMenu> {
    private int selectedAspectIndex = 0;

    public EssentiaTerminalScreen(EssentiaTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 208;
        inventoryLabelY = 114;
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(new Button(leftPos + 12, topPos + 70, 72, 18, Component.literal("Скан jar"), button -> {
            ThaumcraftNetwork.requestEssentiaTerminalScan(menu.pos());
        }));

        addRenderableWidget(new Button(leftPos + 92, topPos + 70, 72, 18, Component.literal("Скан ячеек"), button -> {
            ThaumcraftNetwork.requestEssentiaInventoryScan(menu.pos());
        }));

        addRenderableWidget(new Button(leftPos + 12, topPos + 92, 24, 18, Component.literal("<"), button -> {
            selectedAspectIndex = Math.floorMod(selectedAspectIndex - 1, Aspect.values().length);
        }));

        addRenderableWidget(new Button(leftPos + 140, topPos + 92, 24, 18, Component.literal(">"), button -> {
            selectedAspectIndex = Math.floorMod(selectedAspectIndex + 1, Aspect.values().length);
        }));

        addRenderableWidget(new Button(leftPos + 44, topPos + 92, 88, 18, Component.literal("Скан aspect"), button -> {
            ThaumcraftNetwork.requestEssentiaTerminalFilteredScan(menu.pos(), selectedAspectIndex);
        }));
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        fill(poseStack, leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xEE100F22);
        fill(poseStack, leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF142236);
        fill(poseStack, leftPos + 8, topPos + 18, leftPos + imageWidth - 8, topPos + 112, 0x8820B8FF);

        Aspect aspect = Aspect.values()[Math.floorMod(selectedAspectIndex, Aspect.values().length)];

        font.draw(poseStack, Component.literal("Essentia Terminal").withStyle(ChatFormatting.AQUA), leftPos + 12, topPos + 10, 0xBFEFFF);
        font.draw(poseStack, "Drive / jar / cell scanner", leftPos + 12, topPos + 25, 0xE8D4A7);
        font.draw(poseStack, "Filter:", leftPos + 12, topPos + 48, 0xF2DFB2);
        font.draw(poseStack, aspect.displayName(), leftPos + 58, topPos + 48, 0xFFFFFF);
        font.draw(poseStack, "Кнопки ниже сканируют выбранный aspect.", leftPos + 12, topPos + 59, 0xCFEAFF);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
