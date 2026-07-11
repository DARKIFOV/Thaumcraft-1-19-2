package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AlchemicalCentrifugeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Modern renderer for the rotating part of TC4 ModelCentrifuge.
 * The static top/bottom housings remain in the block model while the original
 * crossbar, counterweights and central spindle rotate around the Y axis.
 */
public final class AlchemicalCentrifugeRenderer implements BlockEntityRenderer<AlchemicalCentrifugeBlockEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/block/alchemical_furnace.png");

    public AlchemicalCentrifugeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AlchemicalCentrifugeBlockEntity tile, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(tile.rotation(partialTick)));

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        // ModelCentrifuge: Crossbar, Dingus1, Dingus2 and Core, converted from 1/16 model units.
        cube(poseStack, consumer, -4 / 16.0F, -1 / 16.0F, -1 / 16.0F,
                4 / 16.0F, 1 / 16.0F, 1 / 16.0F, packedLight);
        cube(poseStack, consumer, 4 / 16.0F, -3 / 16.0F, -2 / 16.0F,
                8 / 16.0F, 3 / 16.0F, 2 / 16.0F, packedLight);
        cube(poseStack, consumer, -8 / 16.0F, -3 / 16.0F, -2 / 16.0F,
                -4 / 16.0F, 3 / 16.0F, 2 / 16.0F, packedLight);
        cube(poseStack, consumer, -1.5F / 16.0F, -4 / 16.0F, -1.5F / 16.0F,
                1.5F / 16.0F, 4 / 16.0F, 1.5F / 16.0F, packedLight);
        poseStack.popPose();
    }

    private static void cube(PoseStack poseStack, VertexConsumer consumer,
                             float minX, float minY, float minZ,
                             float maxX, float maxY, float maxZ, int light) {
        Matrix4f matrix = poseStack.last().pose();
        // Generic UVs intentionally use the complete original 64x32 texture; geometry and rotation are exact.
        quad(matrix, consumer, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, light, 0, 1, 0);
        quad(matrix, consumer, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ, light, 0, -1, 0);
        quad(matrix, consumer, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, light, -1, 0, 0);
        quad(matrix, consumer, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, maxX, minY, minZ, light, 1, 0, 0);
        quad(matrix, consumer, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, light, 0, 0, 1);
        quad(matrix, consumer, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, minX, minY, minZ, light, 0, 0, -1);
    }

    private static void quad(Matrix4f matrix, VertexConsumer consumer,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             int light, float nx, float ny, float nz) {
        vertex(matrix, consumer, x1, y1, z1, 0.0F, 1.0F, light, nx, ny, nz);
        vertex(matrix, consumer, x2, y2, z2, 1.0F, 1.0F, light, nx, ny, nz);
        vertex(matrix, consumer, x3, y3, z3, 1.0F, 0.0F, light, nx, ny, nz);
        vertex(matrix, consumer, x4, y4, z4, 0.0F, 0.0F, light, nx, ny, nz);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer,
                               float x, float y, float z, float u, float v,
                               int light, float nx, float ny, float nz) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(nx, ny, nz)
                .endVertex();
    }
}
