package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TC4BannerBlock;
import com.darkifov.thaumcraft.block.TC4BannerBlockEntity;
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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/** Original ModelBanner geometry, texture sheet, colour palette and cloth sway. */
public final class TC4BannerRenderer implements BlockEntityRenderer<TC4BannerBlockEntity> {
    private static final ResourceLocation BLANK = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/models/banner_blank.png");
    private static final ResourceLocation CULTIST = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/models/banner_cultist.png");

    /** TC4 Utils.colors, indexed by legacy wool metadata. */
    private static final int[] COLORS = {
            15790320, 15435844, 12801229, 6719955,
            14602026, 4312372, 14188952, 4408131,
            10526880, 2651799, 8073150, 2437522,
            5320730, 3887386, 11743532, 1973019
    };

    public TC4BannerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TC4BannerBlockEntity banner, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = banner.getBlockState();
        boolean wall = state.hasProperty(TC4BannerBlock.WALL) && state.getValue(TC4BannerBlock.WALL);
        int rotation = state.hasProperty(TC4BannerBlock.ROTATION) ? state.getValue(TC4BannerBlock.ROTATION) : 0;
        long time = banner.getLevel() == null ? 0L : banner.getLevel().getGameTime();
        renderStandalone(banner.color(), banner.aspect(), wall, rotation,
                banner.getBlockPos(), time, partialTick, poseStack, buffer, packedLight);
    }

    public static void renderStandalone(int color, Aspect aspect, boolean wall, int rotation,
                                        BlockPos seedPos, long gameTime, float partialTick,
                                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ResourceLocation texture = color < 0 && aspect == null ? CULTIST : BLANK;
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.5D, 0.5D);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Vector3f.YP.rotationDegrees((rotation & 15) * 22.5F));

        if (!wall) {
            poseStack.pushPose();
            poseStack.translate(-1.0F / 16.0F, -7.0F / 16.0F, -2.0F / 16.0F);
            renderModelBox(poseStack, consumer, 62, 0,
                    0, 0, -1, 2, 31, 2,
                    128, 64, packedLight, 255, 255, 255, 255);
            poseStack.popPose();
        } else {
            poseStack.translate(0.0D, 0.0D, -0.4125D);
        }

        // Wooden cross-beam.
        renderModelBox(poseStack, consumer, 30, 0,
                -7, -7, -1, 14, 2, 2,
                128, 64, packedLight, 255, 255, 255, 255);

        int rgb = color >= 0 && color < COLORS.length ? COLORS[color] : 0xFFFFFF;
        int red = (rgb >> 16) & 255;
        int green = (rgb >> 8) & 255;
        int blue = rgb & 255;

        // Fabric tabs.
        renderModelBox(poseStack, consumer, 0, 29,
                -5, -7.5F, -1.5F, 2, 3, 3,
                128, 64, packedLight, red, green, blue, 255);
        renderModelBox(poseStack, consumer, 0, 29,
                3, -7.5F, -1.5F, 2, 3, 3,
                128, 64, packedLight, red, green, blue, 255);

        float seed = seedPos.getX() * 7.0F + seedPos.getY() * 9.0F + seedPos.getZ() * 13.0F;
        float phase = seed + gameTime + partialTick;
        float sway = (0.005F + 0.005F * (float) Math.cos(phase * Math.PI * 0.02F)) * (float) Math.PI;

        poseStack.pushPose();
        poseStack.translate(0.0F, -5.0F / 16.0F, 0.0F);
        poseStack.mulPose(Vector3f.XP.rotation(sway));
        renderModelBox(poseStack, consumer, 0, 0,
                -7, 0, -0.5F, 14, 28, 1,
                128, 64, packedLight, red, green, blue, 255);

        if (aspect != null) {
            renderAspect(aspect, poseStack, buffer, packedLight);
        }
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderAspect(Aspect aspect, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight) {
        ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID,
                "textures/aspects/" + aspect.id() + ".png");
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        PoseStack.Pose pose = poseStack.last();
        float x0 = -0.30F;
        float x1 = 0.30F;
        float y0 = 0.25F;
        float y1 = 0.85F;
        float z = 0.0505F;
        quad(pose, consumer,
                x0, y1, z, x1, y1, z, x1, y0, z, x0, y0, z,
                0, 0, 1, 1, 0, 0, 1, packedLight,
                255, 255, 255, 235);
    }

    private static void renderModelBox(PoseStack poseStack, VertexConsumer consumer,
                                       int textureU, int textureV,
                                       float boxX, float boxY, float boxZ,
                                       float width, float height, float depth,
                                       float textureWidth, float textureHeight,
                                       int light, int red, int green, int blue, int alpha) {
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

        quad(pose, consumer,
                maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ,
                (u + d + w) / textureWidth, (v + d) / textureHeight,
                (u + d + w + d) / textureWidth, (v + d + h) / textureHeight,
                1, 0, 0, light, red, green, blue, alpha);
        quad(pose, consumer,
                minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ,
                u / textureWidth, (v + d) / textureHeight,
                (u + d) / textureWidth, (v + d + h) / textureHeight,
                -1, 0, 0, light, red, green, blue, alpha);
        quad(pose, consumer,
                maxX, minY, maxZ, minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ,
                (u + d) / textureWidth, v / textureHeight,
                (u + d + w) / textureWidth, (v + d) / textureHeight,
                0, -1, 0, light, red, green, blue, alpha);
        quad(pose, consumer,
                maxX, maxY, minZ, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ,
                (u + d + w) / textureWidth, v / textureHeight,
                (u + d + w + w) / textureWidth, (v + d) / textureHeight,
                0, 1, 0, light, red, green, blue, alpha);
        quad(pose, consumer,
                minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ,
                (u + d) / textureWidth, (v + d) / textureHeight,
                (u + d + w) / textureWidth, (v + d + h) / textureHeight,
                0, 0, -1, light, red, green, blue, alpha);
        quad(pose, consumer,
                maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, minX, minY, maxZ,
                (u + d + w + d) / textureWidth, (v + d) / textureHeight,
                (u + d + w + d + w) / textureWidth, (v + d + h) / textureHeight,
                0, 0, 1, light, red, green, blue, alpha);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer consumer,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float u0, float v0, float u1, float v1,
                             float normalX, float normalY, float normalZ, int light,
                             int red, int green, int blue, int alpha) {
        vertex(pose.pose(), pose.normal(), consumer, x1, y1, z1, u0, v1,
                normalX, normalY, normalZ, light, red, green, blue, alpha);
        vertex(pose.pose(), pose.normal(), consumer, x2, y2, z2, u1, v1,
                normalX, normalY, normalZ, light, red, green, blue, alpha);
        vertex(pose.pose(), pose.normal(), consumer, x3, y3, z3, u1, v0,
                normalX, normalY, normalZ, light, red, green, blue, alpha);
        vertex(pose.pose(), pose.normal(), consumer, x4, y4, z4, u0, v0,
                normalX, normalY, normalZ, light, red, green, blue, alpha);
    }

    private static void vertex(Matrix4f matrix, Matrix3f normalMatrix, VertexConsumer consumer,
                               float x, float y, float z, float u, float v,
                               float normalX, float normalY, float normalZ, int light,
                               int red, int green, int blue, int alpha) {
        consumer.vertex(matrix, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normalMatrix, normalX, normalY, normalZ)
                .endVertex();
    }
}
