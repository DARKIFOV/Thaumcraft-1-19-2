package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.TC4RevealerHudAdapter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Forge 1.19.2 billboard adapter for TC4 RenderEventHandler#drawTagsOnContainer. */
public final class RevealerAspectTagRenderer {
    private RevealerAspectTagRenderer() {
    }

    public static void renderSingle(BlockPos pos, Aspect aspect, int amount, double localY,
                                    PoseStack poseStack, MultiBufferSource buffer) {
        Minecraft minecraft = Minecraft.getInstance();
        if (pos == null || aspect == null || amount <= 0 || minecraft.player == null
                || minecraft.level == null || minecraft.hitResult == null
                || !TC4RevealerHudAdapter.hasIngamePopupRevealer(minecraft.player)) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hit)
                || minecraft.hitResult.getType() != HitResult.Type.BLOCK
                || !hit.getBlockPos().equals(pos)) {
            return;
        }
        if (minecraft.player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 256.0D) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, localY, 0.5D);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        // Minecraft's font and GUI quads face -Z in world billboards.
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        Matrix4f matrix = poseStack.last().pose();
        ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID,
                "textures/aspects/" + aspect.id() + ".png");
        VertexConsumer icon = buffer.getBuffer(RenderType.entityTranslucent(texture));
        int rgb = aspect.nativeColor();
        int red = (rgb >> 16) & 255;
        int green = (rgb >> 8) & 255;
        int blue = rgb & 255;
        float x0 = -8.0F;
        float y0 = -8.0F;
        float x1 = 8.0F;
        float y1 = 8.0F;
        vertex(matrix, icon, x0, y1, 0.0F, 0.0F, 1.0F, red, green, blue);
        vertex(matrix, icon, x1, y1, 0.0F, 1.0F, 1.0F, red, green, blue);
        vertex(matrix, icon, x1, y0, 0.0F, 1.0F, 0.0F, red, green, blue);
        vertex(matrix, icon, x0, y0, 0.0F, 0.0F, 0.0F, red, green, blue);

        Font font = minecraft.font;
        Component count = Component.literal(String.valueOf(amount));
        float textX = 10.0F;
        float textY = -font.lineHeight / 2.0F + 1.0F;
        font.drawInBatch(count, textX, textY, 0xFFF5EEFF, false,
                matrix, buffer, true, 0, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z,
                               float u, float v, int red, int green, int blue) {
        consumer.vertex(matrix, x, y, z)
                .color(red, green, blue, 245)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0.0F, 0.0F, 1.0F)
                .endVertex();
    }
}
