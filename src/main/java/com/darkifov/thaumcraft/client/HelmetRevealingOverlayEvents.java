package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Forge 1.19.2 adapter for TC4 RenderEventHandler#drawTagsOnContainer.
 *
 * <p>The v11.62.50 renderer removed the incorrect central wand dial, but its
 * replacement still differed from TC4 in four visible ways: it used a fixed
 * scale, tilted with camera pitch, stacked later rows downwards and exposed
 * undiscovered aspect icons. This implementation ports the original tagscale
 * growth/decay and layout rules while keeping a modern world render event.</p>
 */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class HelmetRevealingOverlayEvents {
    private static final int ROW_SIZE = 5;
    private static final float MAX_TAG_SCALE = 0.30F;
    private static final float TC4_STEADY_TAG_SCALE = 0.26F;
    private static final float TC4_RETAINED_SCALE_PER_FRAME = 0.90F;
    private static final float TC4_FADE_PER_FRAME = 0.005F;
    private static final float REFERENCE_RENDER_FPS = 60.0F;
    private static final float MAX_ELAPSED_REFERENCE_FRAMES = 6.0F;
    private static final float TC4_FONT_SCALE_RELATIVE_TO_ICON_PIXELS = 0.64F;
    private static final ResourceLocation UNKNOWN_ASPECT = ResourceLocation.fromNamespaceAndPath(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/aspects/_unknown.png");

    private static float tagScale;
    private static long lastFrameNanos;

    private HelmetRevealingOverlayEvents() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        AuraNodeBlockEntity node = null;
        if (player != null && minecraft.level != null && !minecraft.options.hideGui
                && TC4RevealerHudAdapter.hasIngamePopupRevealer(player)) {
            node = TC4RevealerHudAdapter.targetedNode(minecraft);
        }

        updateTagScale(node);
        if (node == null || tagScale <= 0.0025F) {
            return;
        }

        List<AspectStack> aspects = TC4AuraNodeHudParity.sortedAspectsForHud(node.aspects().all()).stream()
                .filter(stack -> stack.amount() > 0)
                .toList();
        if (aspects.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        double x = node.getBlockPos().getX() + 0.5D - camera.x;
        // TC4 adds +0.4 when the space above is clear, then +0.5 block center
        // and finally tagscale*2 in the UP direction.
        double y = node.getBlockPos().getY() + 0.90D + tagScale * 2.0F - camera.y;
        double z = node.getBlockPos().getZ() + 0.5D - camera.z;

        float pixelScale = tagScale / 16.0F;
        float spacingPixels = 64.0F * tagScale; // 4 * tagScale^2 in world units
        float rowStepPixels = 16.8F;             // 1.05 * tagScale in world units

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        // Original drawTagsOnContainer derives yaw from viewer-to-container X/Z,
        // rotates around Y, then flips the quad 180 degrees around Z.  It never
        // follows camera pitch, so the aspect rows remain upright.
        float viewerDx = (float) (camera.x - (node.getBlockPos().getX() + 0.5D));
        float viewerDz = (float) (camera.z - (node.getBlockPos().getZ() + 0.5D));
        float viewerYaw = (float) (Math.atan2(viewerDx, viewerDz) * 180.0D / Math.PI);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(viewerYaw + 180.0F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        poseStack.scale(pixelScale, pixelScale, pixelScale);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        int row = 0;
        int indexInRow = 0;
        int left = aspects.size();
        for (AspectStack stack : aspects) {
            if (indexInRow >= ROW_SIZE) {
                row++;
                indexInRow = 0;
                left -= ROW_SIZE;
            }
            int inThisRow = Math.min(left, ROW_SIZE);
            float centerOffset = indexInRow - inThisRow / 2.0F + 0.5F;
            int iconX = Math.round(centerOffset * spacingPixels) - 8;
            // Positive GUI Y points down after the negative Y scale. TC4 raises
            // subsequent rows, therefore their GUI coordinate must be negative.
            int iconY = -Math.round(row * rowStepPixels);

            int color = stack.aspect().nativeColor();
            float red = ((color >> 16) & 255) / 255.0F;
            float green = ((color >> 8) & 255) / 255.0F;
            float blue = (color & 255) / 255.0F;
            ResourceLocation icon = ClientAspectData.knows(stack.aspect())
                    ? ResourceLocation.fromNamespaceAndPath(ThaumcraftMod.MOD_ID,
                    "textures/aspects/" + stack.aspect().id() + ".png")
                    : UNKNOWN_ASPECT;

            // Work around the icon centre exactly as TC4 does. Keeping the per-icon
            // transform local also lets the amount text use the original additional
            // 0.04 font scale instead of inheriting the oversized 1/16 GUI-pixel scale.
            poseStack.pushPose();
            poseStack.translate(iconX + 8.0F, iconY + 8.0F, 0.0F);
            RenderSystem.setShaderColor(red, green, blue, 0.75F);
            RenderSystem.setShaderTexture(0, icon);
            GuiComponent.blit(poseStack, -8, -8, 0, 0, 16, 16, 16, 16);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            String amount = Integer.toString(stack.amount());
            int stringWidth = minecraft.font.width(amount);
            poseStack.pushPose();
            poseStack.scale(TC4_FONT_SCALE_RELATIVE_TO_ICON_PIXELS,
                    TC4_FONT_SCALE_RELATIVE_TO_ICON_PIXELS,
                    TC4_FONT_SCALE_RELATIVE_TO_ICON_PIXELS);
            poseStack.translate(0.0D, 6.0D, -0.1D);
            minecraft.font.draw(poseStack, amount, 14 - stringWidth, 1, 0x111111);
            poseStack.translate(0.0D, 0.0D, -0.1D);
            minecraft.font.draw(poseStack, amount, 13 - stringWidth, 0, 0xFFFFFF);
            poseStack.popPose();
            poseStack.popPose();
            indexInRow++;
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    private static void updateTagScale(AuraNodeBlockEntity target) {
        long now = System.nanoTime();
        float referenceFrames;
        if (lastFrameNanos == 0L) {
            referenceFrames = 1.0F;
        } else {
            double elapsedSeconds = Math.max(0.0D, (now - lastFrameNanos) / 1_000_000_000.0D);
            referenceFrames = (float) Math.min(MAX_ELAPSED_REFERENCE_FRAMES,
                    elapsedSeconds * REFERENCE_RENDER_FPS);
        }
        lastFrameNanos = now;

        if (target != null) {
            // TC4 applies, in order, growth (0.031 - scale/10) and a 0.005
            // render-last decay. One reference frame is therefore exactly:
            //     next = 0.9 * current + 0.026
            // whose steady state is 0.26. Raising 0.9 to an elapsed-frame count
            // is the closed-form continuation of that recurrence, so animation
            // remains smooth at 30/60/144 FPS without changing the TC4 curve.
            double retained = Math.pow(TC4_RETAINED_SCALE_PER_FRAME, referenceFrames);
            tagScale = (float) (TC4_STEADY_TAG_SCALE
                    + (tagScale - TC4_STEADY_TAG_SCALE) * retained);
        } else {
            tagScale -= TC4_FADE_PER_FRAME * referenceFrames;
        }
        tagScale = Math.max(0.0F, Math.min(MAX_TAG_SCALE, tagScale));
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        // Static render state must never leak from one world/server into another.
        tagScale = 0.0F;
        lastFrameNanos = 0L;
    }
}
