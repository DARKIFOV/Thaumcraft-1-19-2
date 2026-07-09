package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Stage403-422 parity pass for original TC4 RenderItemThaumometer/ModelScanner.
 *
 * <p>This is still a Forge 1.19.2 renderer adapter, but it no longer draws the
 * old flat placeholder slab. The mesh below is a direct numeric transcription
 * of original scanner.obj: 24 vertices, 58 texture coordinates and 24 quads,
 * rendered with original models/scanner.png. No scan behavior or item progression
 * is changed here.</p>
 */
public final class ThaumometerItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation SCANNER = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/scanner.png");
    private static final float MODEL_SCALE = 0.34F;
    // Stage443-462 transform ledger: the mesh remains the numeric scanner.obj
    // transcription; only Forge item-context transforms may be adapted here.
    private static final float[] SCANNER_VERTICES = new float[] {
            -0.0000F, 0.0000F, -1.4000F, -0.0000F, 0.2000F, -1.4000F, 1.2124F, 0.2000F, -0.7000F, 1.2124F, 0.0000F, -0.7000F, -0.0000F, 0.2000F, -1.0000F, 0.8660F, 0.2000F, -0.5000F, -0.0000F, 0.0000F, -1.0000F, 0.8660F, 0.0000F, -0.5000F, 1.2124F, 0.2000F, 0.7000F, 1.2124F, 0.0000F, 0.7000F, 0.8660F, 0.2000F, 0.5000F, 0.8660F, 0.0000F, 0.5000F, -0.0000F, 0.2000F, 1.4000F, -0.0000F, 0.0000F, 1.4000F, -0.0000F, 0.2000F, 1.0000F, -0.0000F, 0.0000F, 1.0000F, -1.2124F, 0.2000F, 0.7000F, -1.2124F, 0.0000F, 0.7000F, -0.8660F, 0.2000F, 0.5000F, -0.8660F, 0.0000F, 0.5000F, -1.2124F, 0.2000F, -0.7000F, -1.2124F, 0.0000F, -0.7000F, -0.8660F, 0.2000F, -0.5000F, -0.8660F, 0.0000F, -0.5000F
    };
    private static final float[] SCANNER_UVS = new float[] {
            0.7694F, 0.0464F, 0.7694F, 0.0100F, 0.9900F, 0.0100F, 0.9900F, 0.0464F, 0.5195F, 0.2306F, 0.4467F, 0.2306F, 0.3557F, 0.0730F, 0.3921F, 0.0100F, 0.7064F, 0.0684F, 0.7064F, 0.1048F, 0.5488F, 0.1048F, 0.5488F, 0.0684F, 0.0828F, 0.7011F, 0.0100F, 0.7011F, 0.1374F, 0.4805F, 0.1738F, 0.5436F, 0.5488F, 0.1631F, 0.5488F, 0.1267F, 0.8035F, 0.1267F, 0.8035F, 0.1631F, 0.1738F, 0.0730F, 0.1374F, 0.0100F, 0.7308F, 0.1850F, 0.7308F, 0.2214F, 0.5489F, 0.2214F, 0.5489F, 0.1850F, 0.3921F, 0.4805F, 0.3557F, 0.5436F, 0.5488F, 0.0464F, 0.5488F, 0.0100F, 0.0828F, 0.2306F, 0.0100F, 0.2306F, 0.8640F, 0.0683F, 0.8640F, 0.1047F, 0.7064F, 0.1047F, 0.7064F, 0.0683F, 0.5195F, 0.7011F, 0.4467F, 0.7011F, 0.1738F, 0.3882F, 0.1374F, 0.4512F, 0.5489F, 0.1047F, 0.5489F, 0.0683F, 0.3921F, 0.9217F, 0.3557F, 0.8587F, 0.5489F, 0.1630F, 0.5489F, 0.1267F, 0.8036F, 0.1267F, 0.8036F, 0.1630F, 0.3557F, 0.3882F, 0.3921F, 0.4512F, 0.7307F, 0.1851F, 0.7307F, 0.2215F, 0.5488F, 0.2215F, 0.5488F, 0.1851F, 0.1374F, 0.9217F, 0.1738F, 0.8587F, 0.8639F, 0.0684F, 0.8639F, 0.1048F
    };
    private static final int[][] SCANNER_FACES = new int[][] {
            {0, 0, 1, 1, 2, 2, 3, 3},
            {1, 4, 4, 5, 5, 6, 2, 7},
            {4, 8, 6, 9, 7, 10, 5, 11},
            {6, 12, 0, 13, 3, 14, 7, 15},
            {3, 16, 2, 17, 8, 18, 9, 19},
            {2, 7, 5, 6, 10, 20, 8, 21},
            {5, 22, 7, 23, 11, 24, 10, 25},
            {7, 15, 3, 14, 9, 26, 11, 27},
            {9, 28, 8, 29, 12, 1, 13, 0},
            {8, 21, 10, 20, 14, 30, 12, 31},
            {10, 32, 11, 33, 15, 34, 14, 35},
            {11, 27, 9, 26, 13, 36, 15, 37},
            {13, 0, 12, 1, 16, 2, 17, 3},
            {12, 31, 14, 30, 18, 38, 16, 39},
            {14, 35, 15, 34, 19, 40, 18, 41},
            {15, 37, 13, 36, 17, 42, 19, 43},
            {17, 44, 16, 45, 20, 46, 21, 47},
            {16, 39, 18, 38, 22, 48, 20, 49},
            {18, 50, 19, 51, 23, 52, 22, 53},
            {19, 43, 17, 42, 21, 54, 23, 55},
            {21, 28, 20, 29, 1, 1, 0, 0},
            {20, 49, 22, 48, 4, 5, 1, 4},
            {22, 56, 23, 57, 6, 9, 4, 8},
            {23, 55, 21, 54, 0, 13, 6, 12}
    };
    private static ThaumometerItemRenderer INSTANCE;

    private ThaumometerItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static ThaumometerItemRenderer instance() {
        if (INSTANCE == null) {
            INSTANCE = new ThaumometerItemRenderer();
        }
        return INSTANCE;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        applyScannerTransform(transformType, poseStack);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(SCANNER));
        int light = Math.max(packedLight, 15728880);
        renderOriginalScannerObj(poseStack.last().pose(), consumer, light);
        poseStack.popPose();
    }

    private void applyScannerTransform(ItemTransforms.TransformType transformType, PoseStack poseStack) {
        if (transformType == ItemTransforms.TransformType.GUI) {
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-35.0F));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(25.0F));
            poseStack.scale(1.05F, 1.05F, 1.05F);
        } else if (transformType.firstPerson()) {
            poseStack.translate(0.15D, 0.11D, -0.02D);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(190.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-24.0F));
            poseStack.scale(0.78F, 0.78F, 0.78F);
        } else if (transformType == ItemTransforms.TransformType.GROUND) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(92.0F));
            poseStack.scale(0.50F, 0.50F, 0.50F);
        } else if (transformType == ItemTransforms.TransformType.FIXED) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            poseStack.scale(0.82F, 0.82F, 0.82F);
        } else {
            poseStack.translate(0.0D, 0.12D, 0.0D);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(188.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-18.0F));
            poseStack.scale(0.68F, 0.68F, 0.68F);
        }
    }

    private void renderOriginalScannerObj(Matrix4f matrix, VertexConsumer consumer, int light) {
        for (int[] face : SCANNER_FACES) {
            for (int corner = 0; corner < 4; corner++) {
                int vi = face[corner * 2];
                int ti = face[corner * 2 + 1];
                float ox = SCANNER_VERTICES[vi * 3];
                float oy = SCANNER_VERTICES[vi * 3 + 1];
                float oz = SCANNER_VERTICES[vi * 3 + 2];
                float u = SCANNER_UVS[ti * 2];
                float v = SCANNER_UVS[ti * 2 + 1];
                // Original obj lies on X/Z with Y thickness. Map it into the
                // Forge item plane without changing the source coordinates.
                vertex(matrix, consumer, ox * MODEL_SCALE, oz * MODEL_SCALE, (oy - 0.10F) * MODEL_SCALE, u, v, light);
            }
        }
    }

    private void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float u, float v, int light) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0.0F, 0.0F, 1.0F)
                .endVertex();
    }
}
