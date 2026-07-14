package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.HungryChestBlock;
import com.darkifov.thaumcraft.blockentity.HungryChestBlockEntity;
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

/** Exact ModelChest geometry using TC4's original chesthungry.png sheet. */
public final class HungryChestRenderer implements BlockEntityRenderer<HungryChestBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/chesthungry.png");

    public HungryChestRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HungryChestBlockEntity chest, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = chest.getBlockState().hasProperty(HungryChestBlock.FACING)
                ? chest.getBlockState().getValue(HungryChestBlock.FACING)
                : Direction.SOUTH;
        renderStandalone(facing, chest.lidAngle(partialTick), poseStack, buffer, packedLight);
    }

    public static void renderStandalone(Direction facing, float lidAngle, PoseStack poseStack,
                                        MultiBufferSource buffer, int packedLight) {
        float open = 1.0F - lidAngle;
        open = 1.0F - open * open * open;
        float lidRotation = -(open * ((float) Math.PI / 2.0F));

        poseStack.pushPose();
        // TileChestHungryRenderer's original transform.
        poseStack.translate(0.0D, 1.0D, 1.0D);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(rotationFor(facing)));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        // ModelChest.chestBelow: texture 0,19; addBox(1,0,1,14,10,14).
        renderModelBox(poseStack, consumer, 0, 19,
                1, 0, 1, 14, 10, 14,
                64, 64, packedLight);

        // ModelChest.chestLid: pivot(1,7,15), addBox(0,-5,-14,14,5,14).
        poseStack.pushPose();
        poseStack.translate(1.0F / 16.0F, 7.0F / 16.0F, 15.0F / 16.0F);
        poseStack.mulPose(Vector3f.XP.rotation(lidRotation));
        renderModelBox(poseStack, consumer, 0, 0,
                0, -5, -14, 14, 5, 14,
                64, 64, packedLight);
        poseStack.popPose();

        // ModelChest.chestKnob: pivot(8,7,15), addBox(-1,-2,-15,2,4,1).
        poseStack.pushPose();
        poseStack.translate(8.0F / 16.0F, 7.0F / 16.0F, 15.0F / 16.0F);
        poseStack.mulPose(Vector3f.XP.rotation(lidRotation));
        renderModelBox(poseStack, consumer, 0, 0,
                -1, -2, -15, 2, 4, 1,
                64, 64, packedLight);
        poseStack.popPose();
        poseStack.popPose();
    }

    private static float rotationFor(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> -90.0F;
            default -> 0.0F;
        };
    }

    private static void renderModelBox(PoseStack poseStack, VertexConsumer consumer,
                                       int textureU, int textureV,
                                       float boxX, float boxY, float boxZ,
                                       float width, float height, float depth,
                                       float textureWidth, float textureHeight,
                                       int light) {
        float px = 1.0F / 16.0F;
        float minX = boxX * px;
        float minY = boxY * px;
        float minZ = boxZ * px;
        float maxX = (boxX + width) * px;
        float maxY = (boxY + height) * px;
        float maxZ = (boxZ + depth) * px;

        float u = textureU;
        float v = textureV;
        float w = width;
        float h = height;
        float d = depth;
        PoseStack.Pose pose = poseStack.last();

        modelQuad(pose, consumer,
                maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ,
                (u + d + w) / textureWidth, (v + d) / textureHeight,
                (u + d + w + d) / textureWidth, (v + d + h) / textureHeight,
                1, 0, 0, light);
        modelQuad(pose, consumer,
                minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ,
                u / textureWidth, (v + d) / textureHeight,
                (u + d) / textureWidth, (v + d + h) / textureHeight,
                -1, 0, 0, light);
        modelQuad(pose, consumer,
                maxX, minY, maxZ, minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ,
                (u + d) / textureWidth, v / textureHeight,
                (u + d + w) / textureWidth, (v + d) / textureHeight,
                0, -1, 0, light);
        modelQuad(pose, consumer,
                maxX, maxY, minZ, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ,
                (u + d + w) / textureWidth, v / textureHeight,
                (u + d + w + w) / textureWidth, (v + d) / textureHeight,
                0, 1, 0, light);
        modelQuad(pose, consumer,
                minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ,
                (u + d) / textureWidth, (v + d) / textureHeight,
                (u + d + w) / textureWidth, (v + d + h) / textureHeight,
                0, 0, -1, light);
        modelQuad(pose, consumer,
                maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, minX, minY, maxZ,
                (u + d + w + d) / textureWidth, (v + d) / textureHeight,
                (u + d + w + d + w) / textureWidth, (v + d + h) / textureHeight,
                0, 0, 1, light);
    }

    private static void modelQuad(PoseStack.Pose pose, VertexConsumer consumer,
                                  float x1, float y1, float z1,
                                  float x2, float y2, float z2,
                                  float x3, float y3, float z3,
                                  float x4, float y4, float z4,
                                  float u0, float v0, float u1, float v1,
                                  float normalX, float normalY, float normalZ, int light) {
        vertex(pose.pose(), pose.normal(), consumer, x1, y1, z1, u0, v1, normalX, normalY, normalZ, light);
        vertex(pose.pose(), pose.normal(), consumer, x2, y2, z2, u1, v1, normalX, normalY, normalZ, light);
        vertex(pose.pose(), pose.normal(), consumer, x3, y3, z3, u1, v0, normalX, normalY, normalZ, light);
        vertex(pose.pose(), pose.normal(), consumer, x4, y4, z4, u0, v0, normalX, normalY, normalZ, light);
    }

    private static void vertex(Matrix4f matrix, Matrix3f normalMatrix, VertexConsumer consumer,
                               float x, float y, float z, float u, float v,
                               float normalX, float normalY, float normalZ, int light) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normalMatrix, normalX, normalY, normalZ)
                .endVertex();
    }
}
