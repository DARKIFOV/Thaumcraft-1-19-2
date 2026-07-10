package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectColor;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class EssentiaJarRenderer implements BlockEntityRenderer<EssentiaJarBlockEntity> {
    private static final ResourceLocation FILL_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/block/essentia_fill.png");
    private static final ResourceLocation ORIGINAL_LABEL_TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/label.png");

    // v11.62.10: matched to TC4 TileJarRenderer bounds:
    // renderBlocks.setRenderBounds(0.25, 0.0625, 0.25, 0.75, 0.0625 + amount/max * 0.625, 0.75).
    private static final float LIQUID_MIN_XZ = -0.250F;
    private static final float LIQUID_MAX_XZ = 0.250F;
    private static final float LIQUID_MIN_Y = 0.0625F;
    private static final float LIQUID_HEIGHT = 0.625F;

    public EssentiaJarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EssentiaJarBlockEntity jar, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Aspect aspect = jar.storedAspect();

        if (aspect != null && jar.amount() > 0) {
            float fill = jar.fillRatio();
            int color = AspectColor.argb(aspect, 156);
            float maxY = LIQUID_MIN_Y + LIQUID_HEIGHT * fill;

            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            renderLiquidColumn(poseStack, buffer.getBuffer(RenderType.entityTranslucent(FILL_TEXTURE)),
                    LIQUID_MIN_XZ, LIQUID_MIN_Y, LIQUID_MIN_XZ,
                    LIQUID_MAX_XZ, maxY, LIQUID_MAX_XZ,
                    color, packedLight);
            renderStoredAspectGhost(aspect, poseStack, buffer, packedLight);
            poseStack.popPose();
        }

        if (jar.hasFilter()) {
            renderJarLabel(jar.filterAspect(), jar.labelFacing(), poseStack, buffer, packedLight);
        }
    }


    private void renderJarLabel(Aspect filter, Direction facing, PoseStack poseStack, MultiBufferSource buffer, int light) {
        if (filter == null) {
            return;
        }
        poseStack.pushPose();
        Direction safeFacing = facing == null || !facing.getAxis().isHorizontal() ? Direction.NORTH : facing;
        switch (safeFacing) {
            case SOUTH -> {
                poseStack.translate(0.5D, 0.405D, 0.812D);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            }
            case EAST -> {
                poseStack.translate(0.812D, 0.405D, 0.5D);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(270.0F));
            }
            case WEST -> {
                poseStack.translate(0.188D, 0.405D, 0.5D);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            }
            default -> poseStack.translate(0.5D, 0.405D, 0.188D);
        }
        float crooked = crookedLabelRotation(filter, safeFacing, poseStack);
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(crooked));
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer label = buffer.getBuffer(RenderType.entityTranslucent(ORIGINAL_LABEL_TEXTURE));
        // TC4 label card: parchment paper label on the face of the jar.
        // Original TileJarRenderer rotates labels by a tiny hash-based amount when Config.crooked is enabled.
        quad(matrix, label,
                -0.225F, -0.160F, 0.0F,
                 0.225F, -0.160F, 0.0F,
                 0.225F,  0.160F, 0.0F,
                -0.225F,  0.160F, 0.0F,
                255, 255, 255, 242, light);

        VertexConsumer icon = buffer.getBuffer(RenderType.entityTranslucent(aspectTexture(filter)));
        quad(matrix, icon,
                -0.085F, -0.085F, 0.003F,
                 0.085F, -0.085F, 0.003F,
                 0.085F,  0.085F, 0.003F,
                -0.085F,  0.085F, 0.003F,
                255, 255, 255, 245, light);
        poseStack.popPose();
    }

    private float crookedLabelRotation(Aspect filter, Direction facing, PoseStack ignored) {
        String id = filter == null ? "" : filter.id();
        int face = facing == null ? Direction.NORTH.get3DDataValue() : facing.get3DDataValue();
        return Math.floorMod(id.hashCode() + face, 4) - 2.0F;
    }

    private void renderStoredAspectGhost(Aspect aspect, PoseStack poseStack, MultiBufferSource buffer, int light) {
        VertexConsumer icon = buffer.getBuffer(RenderType.entityTranslucent(aspectTexture(aspect)));
        // v11.62.8: subtle original-style aspect mark suspended inside the glass,
        // visible through the jar, instead of the old flat coloured square overlay.
        renderAspectGhostOnFace(Direction.NORTH, icon, poseStack, light);
        renderAspectGhostOnFace(Direction.SOUTH, icon, poseStack, light);
    }

    private void renderAspectGhostOnFace(Direction facing, VertexConsumer icon, PoseStack poseStack, int light) {
        poseStack.pushPose();
        if (facing == Direction.SOUTH) {
            poseStack.translate(0.0D, 0.48D, 0.265D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        } else {
            poseStack.translate(0.0D, 0.48D, -0.265D);
        }
        Matrix4f matrix = poseStack.last().pose();
        quad(matrix, icon,
                -0.115F, -0.115F, 0.0F,
                 0.115F, -0.115F, 0.0F,
                 0.115F,  0.115F, 0.0F,
                -0.115F,  0.115F, 0.0F,
                255, 255, 255, 138, light);
        poseStack.popPose();
    }

    private ResourceLocation aspectTexture(Aspect aspect) {
        String id = aspect == null ? "auram" : aspect.id();
        return new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + id + ".png");
    }

    private void renderLiquidColumn(PoseStack poseStack, VertexConsumer consumer,
                                    float minX, float minY, float minZ,
                                    float maxX, float maxY, float maxZ,
                                    int color, int light) {
        Matrix4f matrix = poseStack.last().pose();
        int a = (color >> 24) & 255;
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        // Top face and four translucent walls; no bottom face, so the fill reads as
        // essentia suspended in glass rather than a solid Minecraft cube.
        quad(matrix, consumer, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, Math.min(190, a + 24), light);
        quad(matrix, consumer, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, r, g, b, a, light);
        quad(matrix, consumer, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, maxX, minY, minZ, r, g, b, a, light);
        quad(matrix, consumer, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, r, g, b, a, light);
        quad(matrix, consumer, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, minX, minY, minZ, r, g, b, a, light);

        // A faint vertical shine, close to the old TC4 jarbrine overlay look.
        int shine = Math.min(128, a);
        quad(matrix, consumer,
                minX + 0.035F, minY + 0.02F, minZ - 0.002F,
                minX + 0.090F, minY + 0.02F, minZ - 0.002F,
                minX + 0.090F, maxY - 0.02F, minZ - 0.002F,
                minX + 0.035F, maxY - 0.02F, minZ - 0.002F,
                255, 255, 255, shine, light);
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
