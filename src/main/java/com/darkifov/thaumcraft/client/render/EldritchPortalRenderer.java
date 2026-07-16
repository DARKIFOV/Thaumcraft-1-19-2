package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EldritchPortalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** TC4 animated, camera-facing Eldritch Portal sheet renderer. */
public final class EldritchPortalRenderer implements BlockEntityRenderer<EldritchPortalBlockEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/misc/eldritch_portal.png");

    public EldritchPortalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EldritchPortalBlockEntity portal, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        long time = portal.getLevel() == null ? 0L : portal.getLevel().getGameTime();
        int frame = Math.floorMod((int) (time / 3L), 16);
        float u0 = frame / 16.0F;
        float u1 = (frame + 1) / 16.0F;
        float pulse = 0.93F + Mth.sin((time + partialTick) * 0.075F) * 0.07F;
        float active = portal.encounterActive() ? 1.35F : 1.0F;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.75D, 0.5D);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        poseStack.scale(active * pulse, active * pulse, active * pulse);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        float half = 0.78F;
        vertex(matrix, consumer, -half, -half, u1, 1.0F);
        vertex(matrix, consumer, half, -half, u0, 1.0F);
        vertex(matrix, consumer, half, half, u0, 0.0F);
        vertex(matrix, consumer, -half, half, u1, 0.0F);
        poseStack.popPose();
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer,
                               float x, float y, float u, float v) {
        consumer.vertex(matrix, x, y, 0.0F)
                .color(255, 255, 255, 230)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0.0F, 0.0F, 1.0F)
                .endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(EldritchPortalBlockEntity portal) {
        return true;
    }
}
