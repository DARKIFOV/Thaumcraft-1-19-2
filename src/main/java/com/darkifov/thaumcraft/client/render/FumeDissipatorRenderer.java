package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.FumeDissipatorBlockEntity;
import com.darkifov.thaumcraft.client.render.model.TC4FluxScrubberModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Original TC4 obelisk_cap.obj Flux Scrubber renderer with the animated Tip group. */
public final class FumeDissipatorRenderer implements BlockEntityRenderer<FumeDissipatorBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/fluxscrubber.png");
    public FumeDissipatorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override public void render(FumeDissipatorBlockEntity scrubber, float partialTick, PoseStack pose,
                                 MultiBufferSource buffer, int light, int overlay) {
        Direction facing = scrubber.facing();
        pose.pushPose();
        pose.translate(0.5D, 0.5D, 0.5D);
        rotateToFacing(pose, facing);
        pose.translate(0.0D, 0.0D, -0.5D);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        renderMesh(pose, consumer, TC4FluxScrubberModel.CAP_TRIANGLES, light);
        long time = scrubber.getLevel() == null ? 0L : scrubber.getLevel().getGameTime();
        float q = time + partialTick + scrubber.animationSeed();
        float bob = Mth.sin(q / 8.0F) * 0.075F + 0.075F;
        pose.translate(0.0D, 0.0D, -bob);
        renderMesh(pose, consumer, TC4FluxScrubberModel.TIP_TRIANGLES, light);
        pose.popPose();
    }

    private static void rotateToFacing(PoseStack pose, Direction facing) {
        switch (facing) {
            case DOWN -> pose.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            case UP -> pose.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            case SOUTH -> pose.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            case WEST -> pose.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            case EAST -> pose.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
            default -> { }
        }
    }

    private static void renderMesh(PoseStack pose, VertexConsumer consumer, float[] data, int light) {
        PoseStack.Pose last = pose.last(); Matrix4f matrix = last.pose(); Matrix3f normal = last.normal();
        for (int i=0; i<data.length; i += TC4FluxScrubberModel.STRIDE * 3) {
            emit(matrix, normal, consumer, data, i, light);
            emit(matrix, normal, consumer, data, i + TC4FluxScrubberModel.STRIDE, light);
            emit(matrix, normal, consumer, data, i + TC4FluxScrubberModel.STRIDE * 2, light);
            emit(matrix, normal, consumer, data, i + TC4FluxScrubberModel.STRIDE * 2, light);
        }
    }
    private static void emit(Matrix4f matrix, Matrix3f normal, VertexConsumer consumer, float[] d, int i, int light) {
        consumer.vertex(matrix,d[i],d[i+1],d[i+2]).color(255,255,255,255).uv(d[i+3],d[i+4])
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal,d[i+5],d[i+6],d[i+7]).endVertex();
    }
}
