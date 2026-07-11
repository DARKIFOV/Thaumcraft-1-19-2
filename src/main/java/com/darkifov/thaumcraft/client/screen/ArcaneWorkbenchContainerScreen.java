package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.arcane.TC4ArcaneWorkbenchParity;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.menu.ArcaneWorkbenchMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * v11.62.11 strict port of TC4 GuiArcaneWorkbench.
 *
 * The server menu synchronizes the exact matched recipe, modified primal costs,
 * affordability and ghost result. The client no longer tries to infer a recipe
 * from loose item ids, which was the main source of wrong icons/costs and stale
 * output in the previous rebuild.
 */
public class ArcaneWorkbenchContainerScreen extends AbstractContainerScreen<ArcaneWorkbenchMenu> {
    public ArcaneWorkbenchContainerScreen(ArcaneWorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = TC4ArcaneWorkbenchParity.GUI_WIDTH;
        imageHeight = TC4ArcaneWorkbenchParity.GUI_HEIGHT;
        inventoryLabelY = TC4ArcaneWorkbenchParity.PLAYER_INV_Y;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        // Original GuiArcaneWorkbench explicitly enabled blending around the
        // complete 190x234 source blit. This matters for the soft transparent
        // edge pixels in the frame and prevents the modern black fringe.
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        OriginalGuiTextures.blitOriginalRegion(
                poseStack,
                leftPos,
                topPos,
                OriginalGuiTextures.ARCANE_WORKBENCH,
                0,
                0,
                imageWidth,
                imageHeight,
                256,
                256
        );
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // Original GuiArcaneWorkbench has no foreground title labels.
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderOriginalAspectCosts(poseStack);
        renderOriginalInsufficientVisGhost(poseStack);
        renderTooltip(poseStack, mouseX, mouseY);
        renderOriginalAspectHover(poseStack, mouseX, mouseY);
        renderGhostResultHover(poseStack, mouseX, mouseY);
    }

    private void renderOriginalAspectCosts(PoseStack poseStack) {
        if (!menu.hasArcaneRecipe()) {
            return;
        }

        ItemStack wand = menu.getSlot(ArcaneWorkbenchMenu.MENU_SLOT_WAND).getItem();
        int ticks = Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount;

        for (int i = 0; i < TC4ArcaneWorkbenchParity.PRIMALS.length; i++) {
            Aspect aspect = TC4ArcaneWorkbenchParity.PRIMALS[i];
            int amount = menu.arcaneCost(aspect);
            if (amount <= 0) {
                continue;
            }

            boolean enough = wand.getItem() instanceof WandItem
                    && (WandItem.hasInfiniteVis(wand) || WandItem.getVis(wand, aspect) >= amount);
            float alpha = enough ? 1.0F : 0.3F + Mth.sin((ticks + i * 10) / 2.0F) * 0.2F;
            int x = leftPos + TC4ArcaneWorkbenchParity.ASPECT_LOCS[i][0] - 8;
            int y = topPos + TC4ArcaneWorkbenchParity.ASPECT_LOCS[i][1] - 8;
            ResourceLocation texture = new ResourceLocation(
                    ThaumcraftMod.MOD_ID,
                    "textures/aspects/" + aspect.id() + ".png"
            );

            OriginalGuiTextures.blitOriginalTintedAlpha(poseStack, x, y, texture, 16, 16,
                    aspect.nativeColor(), Mth.clamp(alpha, 0.1F, 1.0F));

            int textColor = enough ? 0xFFFFFF : 0x9A6A62;
            drawString(poseStack, font, Component.literal(WandItem.formatVis(amount)), x + 11, y + 9, textColor);
        }
    }

    /**
     * Original TC4 leaves the real output slot empty when the wand cannot pay,
     * then draws a dim non-collectible recipe result and the tiny warning text.
     */
    private void renderOriginalInsufficientVisGhost(PoseStack poseStack) {
        if (!menu.hasArcaneRecipe() || menu.canAffordArcaneRecipe()) {
            return;
        }

        ItemStack wand = menu.getSlot(ArcaneWorkbenchMenu.MENU_SLOT_WAND).getItem();
        if (!(wand.getItem() instanceof WandItem)) {
            return;
        }

        ItemStack ghost = menu.ghostArcaneResult();
        if (ghost.isEmpty()) {
            return;
        }

        int x = leftPos + TC4ArcaneWorkbenchParity.OUTPUT_SLOT_X;
        int y = topPos + TC4ArcaneWorkbenchParity.OUTPUT_SLOT_Y;
        // GuiArcaneWorkbench used GL color (0.33, 0.33, 0.33, 0.66) for the
        // unaffordable result. Tint the item itself instead of covering it with
        // the previous rebuild's opaque debug rectangle.
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(0.33F, 0.33F, 0.33F, 0.66F);
        itemRenderer.renderAndDecorateItem(ghost, x, y);
        itemRenderer.renderGuiItemDecorations(font, ghost, x, y);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        poseStack.pushPose();
        poseStack.translate(leftPos + 168.0F, topPos + 46.0F, 300.0F);
        poseStack.scale(0.5F, 0.5F, 1.0F);
        Component warning = Component.literal("Insufficient vis");
        int width = font.width(warning);
        drawString(poseStack, font, warning, -width / 2, 0, 0xEE6E6E);
        poseStack.popPose();
    }

    private void renderOriginalAspectHover(PoseStack poseStack, int mouseX, int mouseY) {
        if (!menu.hasArcaneRecipe()) {
            return;
        }

        Aspect hovered = aspectAtOriginalArcaneLoc(mouseX - leftPos, mouseY - topPos);
        if (hovered == null) {
            return;
        }

        int amount = menu.arcaneCost(hovered);
        if (amount <= 0) {
            return;
        }

        ItemStack wand = menu.getSlot(ArcaneWorkbenchMenu.MENU_SLOT_WAND).getItem();
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(hovered.displayName() + " " + WandItem.formatVis(amount)).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (wand.getItem() instanceof WandItem && !WandItem.hasInfiniteVis(wand)) {
            int stored = WandItem.getVis(wand, hovered);
            lines.add(Component.literal(WandItem.formatVis(stored) + " / " + WandItem.formatVis(amount)).withStyle(
                    stored >= amount ? ChatFormatting.DARK_PURPLE : ChatFormatting.RED
            ));
        }
        renderComponentTooltip(poseStack, lines, mouseX, mouseY);
    }

    private void renderGhostResultHover(PoseStack poseStack, int mouseX, int mouseY) {
        if (!menu.hasArcaneRecipe() || menu.canAffordArcaneRecipe()) {
            return;
        }
        int localX = mouseX - leftPos;
        int localY = mouseY - topPos;
        if (!TC4ArcaneWorkbenchParity.insideOutputSlot(localX, localY)) {
            return;
        }
        ItemStack ghost = menu.ghostArcaneResult();
        if (!ghost.isEmpty()) {
            renderTooltip(poseStack, ghost, mouseX, mouseY);
        }
    }

    private Aspect aspectAtOriginalArcaneLoc(int mouseX, int mouseY) {
        return TC4ArcaneWorkbenchParity.aspectAt(mouseX, mouseY);
    }
}
