package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.TransvectorInterfaceBlockEntity;
import com.darkifov.thaumcraft.menu.TransvectorInterfaceMenu;
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

public class TransvectorInterfaceScreen extends AbstractContainerScreen<TransvectorInterfaceMenu> {
    private static final ResourceLocation ORIGINAL_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, \"textures/gui/transvector_interface.png\");

    public TransvectorInterfaceScreen(TransvectorInterfaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        
        this.imageWidth = 256;
        this.imageHeight = 256;imageWidth = 176;
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
        int x = leftPos + (imageWidth - 256) / 2;
        int y = topPos + (imageHeight - 256) / 2;
        OriginalGuiTextures.blitOriginal(poseStack, x, y, OriginalGuiTextures.TRANSVECTOR_INTERFACE, 256, 256);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
