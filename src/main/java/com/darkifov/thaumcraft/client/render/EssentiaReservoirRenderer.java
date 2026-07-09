package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EssentiaReservoirBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/** Stage503-522 TileEssentiaReservoirRenderer adapter: original reservoir model texture + internal essentia tint. */
public class EssentiaReservoirRenderer implements BlockEntityRenderer<EssentiaReservoirBlockEntity> {
    private static final ResourceLocation FILL_TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/block/essentia_fill.png");

    public EssentiaReservoirRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EssentiaReservoirBlockEntity reservoir, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (reservoir.amount() <= 0) {
            return;
        }
        int index = 0;
        int total = Math.max(1, reservoir.amount());
        for (Map.Entry<Aspect, Integer> entry : reservoir.aspects().entries().entrySet()) {
            if (entry.getKey() == null || entry.getValue() <= 0) {
                continue;
            }
            float height = Math.max(0.02F, 0.72F * entry.getValue() / total);
            float minY = 0.12F + index * 0.035F;
            float maxY = Math.min(0.88F, minY + height);
            int color = AspectColor.argb(entry.getKey(), 120);
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            renderLiquidBox(poseStack, buffer.getBuffer(RenderType.entityTranslucent(FILL_TEXTURE)),
                    -0.34F, minY, -0.34F, 0.34F, maxY, 0.34F, color, packedLight);
            poseStack.popPose();
            index++;
            if (index >= 6) {
                break;
            }
        }
    }

    private void renderLiquidBox(PoseStack poseStack, VertexConsumer consumer,
                                 float minX, float minY, float minZ,
                                 float maxX, float maxY, float maxZ,
                                 int color, int light) {
        Matrix4f matrix = poseStack.last().pose();
        int a = (color >> 24) & 255;
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        quad(matrix, consumer, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a, light);
        quad(matrix, consumer, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, r, g, b, a, light);
        quad(matrix, consumer, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, maxX, minY, minZ, r, g, b, a, light);
        quad(matrix, consumer, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, r, g, b, a, light);
        quad(matrix, consumer, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, minX, minY, minZ, r, g, b, a, light);
    }

    private void quad(Matrix4f matrix, VertexConsumer consumer,
                      float x1, float y1, float z1,
                      float x2, float y2, float z2,
                      float x3, float y3, float z3,
                      float x4, float y4, float z4,
                      int r, int g, int b, int a, int light) {
        vertex(matrix, consumer, x1, y1, z1, 0.0F, 1.0F, r, g, b, a, light);
        vertex(matrix, consumer, x2, y2, z2, 1.0F, 1.0F, r, g, b, a, light);
        vertex(matrix, consumer, x3, y3, z3, 1.0F, 0.0F, r, g, b, a, light);
        vertex(matrix, consumer, x4, y4, z4, 0.0F, 0.0F, r, g, b, a, light);
    }

    private void vertex(Matrix4f matrix, VertexConsumer consumer,
                        float x, float y, float z, float u, float v,
                        int r, int g, int b, int a, int light) {
        consumer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
