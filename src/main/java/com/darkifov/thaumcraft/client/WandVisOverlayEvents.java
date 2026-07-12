package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.wand.WandFocusRuntime;
import com.darkifov.thaumcraft.wand.WandFocusType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * v11.62.12: original TC4 casting-wand vis dial.
 *
 * Reference: ClientTickEventsFML#renderCastingWandHud in 4.2.3.5.  The old HUD
 * draws the six primal reservoirs from textures/gui/hud.png around a 32px dial,
 * and exposes exact values while the player is sneaking.  The modern port keeps
 * displayed vis units (25/50/100 capacity) while preserving the original layout.
 */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WandVisOverlayEvents {
    private static final int HOTBAR_SLOTS = 9;
    private static final int[][] OLD_VIS = new int[HOTBAR_SLOTS][6];
    private static final boolean[] OLD_VIS_INITIALIZED = new boolean[HOTBAR_SLOTS];
    private static long nextOldVisSyncMillis;

    private WandVisOverlayEvents() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }

        ItemStack wandStack = minecraft.player.getMainHandItem();
        if (!(wandStack.getItem() instanceof WandItem wandItem)) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        int capacity = Math.max(1, wandItem.stackVisCapacity(wandStack));
        Aspect[] primals = WandItem.primalVisAspects();
        int hotbarSlot = Math.max(0, Math.min(HOTBAR_SLOTS - 1, minecraft.player.getInventory().selected));
        long now = System.currentTimeMillis();
        if (!OLD_VIS_INITIALIZED[hotbarSlot]) {
            snapshotVis(hotbarSlot, primals, wandStack);
            OLD_VIS_INITIALIZED[hotbarSlot] = true;
            nextOldVisSyncMillis = now + 1000L;
        } else if (now >= nextOldVisSyncMillis) {
            snapshotVis(hotbarSlot, primals, wandStack);
            nextOldVisSyncMillis = now + 1000L;
        }

        WandFocusType focus = WandFocusRuntime.getFocus(wandStack);
        var focusCost = focus == null ? null : WandFocusRuntime.focusVisCost(wandStack, focus, minecraft.player.getRandom());

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TC4AuraNodeHudParity.ORIGINAL_HUD);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int hudX = 8;
        int hudY = Math.max(8, minecraft.getWindow().getGuiScaledHeight() - 70);
        poseStack.pushPose();
        poseStack.translate(hudX, hudY, 200.0D);
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 1.0F);
        GuiComponent.blit(poseStack, 0, 0, 0, 0, 64, 64, 256, 256);
        poseStack.popPose();

        poseStack.translate(16.0D, 16.0D, 0.0D);
        for (int index = 0; index < primals.length; index++) {
            Aspect aspect = primals[index];
            int amount = Math.min(capacity, Math.max(0, WandItem.getVis(wandStack, aspect)));
            int fill = (int) (30.0F * amount / capacity);

            poseStack.pushPose();
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-15.0F + index * 24.0F));
            poseStack.translate(0.0D, -32.0D, 0.0D);
            poseStack.scale(0.5F, 0.5F, 1.0F);

            if (fill > 0) {
                int color = aspect.nativeColor();
                RenderSystem.setShaderColor(((color >> 16) & 255) / 255.0F, ((color >> 8) & 255) / 255.0F,
                        (color & 255) / 255.0F, 0.80F);
                GuiComponent.blit(poseStack, -4, 35 - fill, 104, 0, 8, fill, 256, 256);
            }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(poseStack, -8, -3, 72, 0, 16, 42, 256, 256);

            int markerOffset = 0;
            int baseFocusCost = focusCost == null ? 0 : focusCost.get(aspect);
            if (baseFocusCost > 0) {
                GuiComponent.blit(poseStack, -4, -8, 136, 0, 8, 8, 256, 256);
                markerOffset = 8;
            }

            int oldAmount = OLD_VIS[hotbarSlot][index];
            if (oldAmount > amount) {
                GuiComponent.blit(poseStack, -4, -8 - markerOffset, 128, 0, 8, 8, 256, 256);
            } else if (oldAmount < amount) {
                GuiComponent.blit(poseStack, -4, -8 - markerOffset, 120, 0, 8, 8, 256, 256);
            }

            if (minecraft.player.isShiftKeyDown()) {
                poseStack.pushPose();
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(-90.0F));
                minecraft.font.draw(poseStack, Component.literal(WandItem.formatVis(amount)), -32.0F, -4.0F, 0xFFFFFFFF);
                if (baseFocusCost > 0) {
                    int modifiedCost = WandItem.modifiedVisCost(wandStack, minecraft.player, aspect, baseFocusCost, false);
                    minecraft.font.draw(poseStack, Component.literal(WandItem.formatVis(modifiedCost)), 8.0F, -4.0F, 0xFFFFFFFF);
                }
                poseStack.popPose();
                // Font rendering changes the bound texture; TC4 explicitly binds hud.png again.
                RenderSystem.setShaderTexture(0, TC4AuraNodeHudParity.ORIGINAL_HUD);
            }
            poseStack.popPose();
        }
        poseStack.popPose();

        if (minecraft.player.isShiftKeyDown()) {
            int total = 0;
            for (Aspect aspect : primals) {
                total += Math.max(0, WandItem.getVis(wandStack, aspect));
            }
            minecraft.font.draw(poseStack,
                    Component.translatable("thaumcraft.hud.wand.vis", WandItem.formatVis(total), WandItem.formatVis(capacity * primals.length)),
                    hudX + 36.0F, hudY + 12.0F, 0xFFFFFFFF);
            if (focus != null) {
                minecraft.font.draw(poseStack,
                        Component.translatable("thaumcraft.hud.wand.focus", Component.translatable(focus.translationKey())),
                        hudX + 36.0F, hudY + 23.0F, 0xFFE6D5FF);
            }
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static void snapshotVis(int hotbarSlot, Aspect[] primals, ItemStack wandStack) {
        for (int index = 0; index < primals.length && index < OLD_VIS[hotbarSlot].length; index++) {
            OLD_VIS[hotbarSlot][index] = WandItem.getVis(wandStack, primals[index]);
        }
    }
}
