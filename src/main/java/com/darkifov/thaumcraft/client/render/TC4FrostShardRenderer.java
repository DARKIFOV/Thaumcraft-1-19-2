package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.render.model.TC4FrostShardModel;
import com.darkifov.thaumcraft.entity.projectile.TC4FrostShardEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/** Modern renderer for the original TC4 RenderFrostShard orb.obj path. */
public class TC4FrostShardRenderer extends EntityRenderer<TC4FrostShardEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/blocks/frostshard.png");

    public TC4FrostShardRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(TC4FrostShardEntity shard, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, shard.yRotO, shard.getYRot())));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTicks, shard.xRotO, shard.getXRot())));

        RandomSource random = RandomSource.create(shard.getId());
        float base = shard.getVisualDamage() * 0.1F;
        poseStack.scale(base + random.nextFloat() * 0.1F,
                base + random.nextFloat() * 0.1F,
                base + random.nextFloat() * 0.1F);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        float[] data = TC4FrostShardModel.TRIANGLES;
        int triangleStride = TC4FrostShardModel.STRIDE * 3;
        for (int triangle = 0; triangle < data.length; triangle += triangleStride) {
            emitVertex(matrix, normal, consumer, data, triangle, packedLight);
            emitVertex(matrix, normal, consumer, data, triangle + TC4FrostShardModel.STRIDE, packedLight);
            emitVertex(matrix, normal, consumer, data, triangle + TC4FrostShardModel.STRIDE * 2, packedLight);
            // entityTranslucent uses quads. Repeating the final point turns the
            // original OBJ triangle into a degenerate quad without changing its face.
            emitVertex(matrix, normal, consumer, data, triangle + TC4FrostShardModel.STRIDE * 2, packedLight);
        }
        poseStack.popPose();
        super.render(shard, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void emitVertex(Matrix4f matrix, Matrix3f normal, VertexConsumer consumer, float[] data, int index, int light) {
        consumer.vertex(matrix, data[index], data[index + 1], data[index + 2])
                .color(255, 255, 255, 255)
                .uv(data[index + 3], data[index + 4])
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, data[index + 5], data[index + 6], data[index + 7])
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(TC4FrostShardEntity entity) {
        return TEXTURE;
    }
}
