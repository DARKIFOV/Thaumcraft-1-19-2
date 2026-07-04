package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.menu.EssentiaTerminalMenu;
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

public class EssentiaTerminalScreen extends AbstractContainerScreen<EssentiaTerminalMenu> {
    private static final ResourceLocation ORIGINAL_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, \"textures/gui/essentia_terminal.png\");

    private int selectedAspectIndex = 0;

    public EssentiaTerminalScreen(EssentiaTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        
        this.imageWidth = 256;
        this.imageHeight = 256;imageWidth = 176;
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
        int x = leftPos + (imageWidth - 256) / 2;
        int y = topPos + (imageHeight - 256) / 2;
        OriginalGuiTextures.blitOriginal(poseStack, x, y, OriginalGuiTextures.ESSENTIA_TERMINAL, 256, 256);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
