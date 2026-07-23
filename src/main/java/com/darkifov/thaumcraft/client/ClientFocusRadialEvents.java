package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.wand.WandComponentData;
import com.darkifov.thaumcraft.wand.WandFocusRuntime;
import com.darkifov.thaumcraft.wand.WandManagerRuntime;
import com.darkifov.thaumcraft.wand.TC4WandFocusContract;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Modern client adapter for TC4 REHWandHandler's hold-F radial focus selector.
 * Shift+F removes the focus immediately; plain F releases the mouse, displays
 * the original radial textures and commits the hovered focus on key release.
 */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientFocusRadialEvents {
    private static final ResourceLocation RADIAL = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/misc/radial.png");
    private static final ResourceLocation RADIAL_2 = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/misc/radial2.png");

    private static boolean keyWasDown;
    private static boolean active;
    private static boolean mouseReleased;
    private static float scale;
    private static String hoveredKey;
    private static ItemStack hoveredStack = ItemStack.EMPTY;
    private static List<WandManagerRuntime.AvailableFocus> available = List.of();

    private ClientFocusRadialEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean keyDown = ClientWandArchitectKeybinds.KEY_CHANGE_WAND_FOCUS.isDown();

        if (minecraft.player == null || minecraft.level == null) {
            reset(minecraft, false);
            keyWasDown = keyDown;
            return;
        }

        if (keyDown && !keyWasDown) {
            // Drain KeyMapping's click counter: the radial owns F exclusively.
            while (ClientWandArchitectKeybinds.KEY_CHANGE_WAND_FOCUS.consumeClick()) {
                // no-op
            }
            ItemStack wand = minecraft.player.getMainHandItem();
            if (minecraft.screen == null && wand.getItem() instanceof WandItem && !WandComponentData.isSceptre(wand)) {
                if (minecraft.player.isShiftKeyDown()) {
                    ThaumcraftNetwork.requestFocusChangeFromClient(WandManagerRuntime.REMOVE);
                    reset(minecraft, false);
                } else {
                    available = WandManagerRuntime.availableFoci(minecraft.player);
                    hoveredKey = null;
                    hoveredStack = ItemStack.EMPTY;
                    active = true;
                    if (!available.isEmpty()) {
                        minecraft.mouseHandler.releaseMouse();
                        mouseReleased = true;
                    }
                }
            }
        }

        if (active && minecraft.screen != null) {
            reset(minecraft, false);
        } else if (!keyDown && keyWasDown && active) {
            String selected = hoveredKey;
            reset(minecraft, false);
            if (selected != null && !selected.isBlank()) {
                ThaumcraftNetwork.requestFocusChangeFromClient(selected);
            }
        }

        if (active) {
            scale = Math.min(1.0F, scale + 0.05F);
        } else {
            scale = Math.max(0.0F, scale - 0.05F);
            if (scale == 0.0F) {
                available = List.of();
                hoveredKey = null;
                hoveredStack = ItemStack.EMPTY;
            }
        }
        keyWasDown = keyDown;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type() || scale <= 0.0F) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }
        ItemStack wand = minecraft.player.getMainHandItem();
        if (!(wand.getItem() instanceof WandItem) || WandComponentData.isSceptre(wand)) {
            return;
        }

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        float width = TC4WandFocusContract.radialRadius(available.size());
        float rotation = (minecraft.player.tickCount % 720) / 2.0F + event.getPartialTick();
        PoseStack pose = event.getPoseStack();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        drawRotatingRing(pose, RADIAL, centerX, centerY, width * 2.75F * scale, rotation);
        drawRotatingRing(pose, RADIAL_2, centerX, centerY, width * 2.55F * scale, -rotation);

        ItemStack installed = WandFocusRuntime.getFocusStack(wand);
        if (!installed.isEmpty()) {
            minecraft.getItemRenderer().renderAndDecorateItem(installed, centerX - 8, centerY - 8);
        }

        hoveredKey = null;
        hoveredStack = ItemStack.EMPTY;
        if (!available.isEmpty()) {
            double mouseX = minecraft.mouseHandler.xpos() * screenWidth / minecraft.getWindow().getScreenWidth();
            double mouseY = minecraft.mouseHandler.ypos() * screenHeight / minecraft.getWindow().getScreenHeight();
            float currentRotation = TC4WandFocusContract.radialAngleDegrees(0, available.size(), scale);
            float slice = TC4WandFocusContract.radialSliceDegrees(available.size());
            for (WandManagerRuntime.AvailableFocus focus : available) {
                double radians = Math.toRadians(currentRotation);
                int iconX = centerX + (int) Math.round(Math.cos(radians) * width * scale);
                int iconY = centerY + (int) Math.round(Math.sin(radians) * width * scale);
                boolean hovered = active
                        && mouseX >= iconX - 10 && mouseX <= iconX + 10
                        && mouseY >= iconY - 10 && mouseY <= iconY + 10;
                if (hovered) {
                    hoveredKey = focus.sortKey();
                    hoveredStack = focus.stack();
                    pose.pushPose();
                    pose.translate(0.0D, 0.0D, 50.0D);
                    GuiComponent.fill(pose, iconX - 11, iconY - 11, iconX + 11, iconY + 11, 0x88FFFFFF);
                    pose.popPose();
                }
                minecraft.getItemRenderer().renderAndDecorateItem(focus.stack(), iconX - 8, iconY - 8);
                currentRotation += slice;
            }
        }

        ItemStack tooltipStack = hoveredStack;
        if (tooltipStack.isEmpty()) {
            double mouseX = minecraft.mouseHandler.xpos() * screenWidth / minecraft.getWindow().getScreenWidth();
            double mouseY = minecraft.mouseHandler.ypos() * screenHeight / minecraft.getWindow().getScreenHeight();
            if (!installed.isEmpty() && Math.abs(mouseX - centerX) <= 10.0D && Math.abs(mouseY - centerY) <= 10.0D) {
                tooltipStack = installed;
            }
        }
        if (!tooltipStack.isEmpty()) {
            Component title = tooltipStack.getHoverName();
            int titleWidth = minecraft.font.width(title);
            minecraft.font.drawShadow(pose, title, centerX - titleWidth / 2.0F, centerY + width + 20.0F, 0xFFFFFFFF);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static void drawRotatingRing(PoseStack pose, ResourceLocation texture, int centerX, int centerY,
                                         float renderedSize, float rotationDegrees) {
        if (renderedSize <= 0.0F) {
            return;
        }
        int size = Math.max(1, Math.round(renderedSize));
        RenderSystem.setShaderTexture(0, texture);
        pose.pushPose();
        pose.translate(centerX, centerY, 0.0D);
        pose.mulPose(Vector3f.ZP.rotationDegrees(rotationDegrees));
        GuiComponent.blit(pose, -size / 2, -size / 2, size, size, 0.0F, 0.0F, 256, 256, 256, 256);
        pose.popPose();
    }

    private static void reset(Minecraft minecraft, boolean clearScale) {
        active = false;
        hoveredKey = null;
        hoveredStack = ItemStack.EMPTY;
        if (mouseReleased) {
            if (minecraft.screen == null) {
                minecraft.mouseHandler.grabMouse();
            }
            mouseReleased = false;
        }
        if (clearScale) {
            scale = 0.0F;
            available = List.of();
        }
    }
}
