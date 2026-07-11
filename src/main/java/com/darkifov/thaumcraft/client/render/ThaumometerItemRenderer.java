package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraNodeModifier;
import com.darkifov.thaumcraft.aura.AuraNodeType;
import com.darkifov.thaumcraft.aura.TC4AuraNodeScanParity;
import com.darkifov.thaumcraft.aura.TC4ThaumometerTargeting;
import com.darkifov.thaumcraft.block.ThaumometerItem;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.client.ClientScanData;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    private static final ResourceLocation SCANNER_SCREEN = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/scanscreen.png");
    private static final float MODEL_SCALE = 0.30F;
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
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(SCANNER));
        int light = Math.max(packedLight, 15728880);
        renderOriginalScannerObj(poseStack.last().pose(), consumer, light);
        renderScannerScreen(poseStack, buffer, light);
        renderOriginalScanReadout(stack, transformType, poseStack, buffer, light);
        poseStack.popPose();
    }

    private void applyScannerTransform(ItemTransforms.TransformType transformType, PoseStack poseStack) {
        if (transformType == ItemTransforms.TransformType.GUI) {
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-18.0F));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(52.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(226.0F));
            poseStack.scale(0.92F, 0.92F, 0.92F);
        } else if (transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
            // TC4 presents the scanner as a large two-handed instrument centred
            // in front of the camera.  The old 0.18 adapter scale reduced it to a
            // tiny item below the crosshair and made the scan glass unreadable.
            poseStack.translate(0.12D, -0.18D, -0.58D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(135.0F));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(2.0F));
            poseStack.scale(0.76F, 0.76F, 0.76F);
        } else if (transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
            poseStack.translate(-0.12D, -0.18D, -0.58D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-135.0F));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-2.0F));
            poseStack.scale(0.76F, 0.76F, 0.76F);
        } else if (transformType == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND) {
            poseStack.translate(0.18D, 0.16D, -0.08D);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(68.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-28.0F));
            poseStack.scale(0.72F, 0.72F, 0.72F);
        } else if (transformType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND) {
            poseStack.translate(-0.18D, 0.16D, -0.08D);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(68.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(28.0F));
            poseStack.scale(0.72F, 0.72F, 0.72F);
        } else if (transformType == ItemTransforms.TransformType.GROUND) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            poseStack.scale(0.48F, 0.48F, 0.48F);
        } else if (transformType == ItemTransforms.TransformType.FIXED) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            poseStack.scale(0.72F, 0.72F, 0.72F);
        } else {
            poseStack.scale(0.62F, 0.62F, 0.62F);
        }
    }

    private void renderScannerScreen(PoseStack poseStack, MultiBufferSource buffer, int light) {
        poseStack.pushPose();
        // scanner.obj is remapped into the XY plane; the scan screen sits just
        // above the centre opening, as in TC4 ItemThaumometerRenderer.
        poseStack.translate(0.0D, 0.0D, 0.036D);
        float pulse = 0.86F + 0.08F * (float)Math.sin((Minecraft.getInstance().level == null
                ? 0.0D : Minecraft.getInstance().level.getGameTime()) * 0.18D);
        float half = 0.215F;
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer screen = buffer.getBuffer(RenderType.entityTranslucent(SCANNER_SCREEN));
        screen.vertex(matrix, -half, -half, 0.0F).color(255,255,255,(int)(210 * pulse)).uv(0.0F,1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F,0.0F,1.0F).endVertex();
        screen.vertex(matrix, half, -half, 0.0F).color(255,255,255,(int)(210 * pulse)).uv(1.0F,1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F,0.0F,1.0F).endVertex();
        screen.vertex(matrix, half, half, 0.0F).color(255,255,255,(int)(210 * pulse)).uv(1.0F,0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F,0.0F,1.0F).endVertex();
        screen.vertex(matrix, -half, half, 0.0F).color(255,255,255,(int)(210 * pulse)).uv(0.0F,0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F,0.0F,1.0F).endVertex();
        poseStack.popPose();
    }

    /**
     * TC4 renders the actual scan result on the glass of the held Thaumometer.
     * Earlier rebuild versions duplicated that information as a modern HUD beside
     * the crosshair.  This adapter keeps the readout on the model itself: node name,
     * type/modifier and the triangular aspect list are rendered in the scanner plane.
     */
    private void renderOriginalScanReadout(ItemStack scannerStack, ItemTransforms.TransformType transformType,
                                           PoseStack poseStack, MultiBufferSource buffer, int light) {
        if (transformType != ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                && transformType != ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        TC4ThaumometerTargeting.ScanTarget target = TC4ThaumometerTargeting.find(
                minecraft.player, minecraft.getFrameTime());
        if (target == null || !target.isPresent()) {
            renderQuestionMark(poseStack, buffer, light);
            return;
        }

        boolean scanned = isScanned(scannerStack, target);
        List<AspectStack> aspects = scanned ? sortedAspects(target.aspects()) : List.of();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.041D);
        poseStack.scale(0.0040F, -0.0040F, 0.0040F);

        drawScannerTitle(poseStack, buffer, target.displayName(), light);

        if (target.kind() == TC4ThaumometerTargeting.Kind.NODE && target.node() != null) {
            AuraNodeBlockEntity node = target.node();
            if (scanned) {
                AuraNodeType nodeType = AuraNodeType.fromName(node.nodeType());
                AuraNodeModifier nodeModifier = AuraNodeModifier.fromName(node.nodeModifier());
                MutableComponent typeLine = Component.translatable(nodeType.translationKey());
                if (nodeModifier != AuraNodeModifier.NORMAL) {
                    typeLine.append(Component.literal(", "))
                            .append(Component.translatable(nodeModifier.translationKey()));
                }
                int typeWidth = minecraft.font.width(typeLine);
                minecraft.font.drawInBatch(typeLine, -typeWidth / 2.0F, -40.0F, 0xFFD7B8FF, false,
                        poseStack.last().pose(), buffer, false, 0, light);
            }
        }

        if (!scanned) {
            Component question = Component.literal("?");
            int width = minecraft.font.width(question);
            minecraft.font.drawInBatch(question, -width / 2.0F, -4.0F, 0xFFE7E7E7, false,
                    poseStack.last().pose(), buffer, false, 0, light);
            poseStack.popPose();
            return;
        }

        // Original ItemThaumometerRenderer uses a 5/4/3/2/1 triangular
        // arrangement and can display fifteen aspects.  Earlier rebuild code
        // used 4/3/2/1 and silently discarded the remaining five.
        int shown = Math.min(15, aspects.size());
        int index = 0;
        int row = 0;
        while (index < shown && row < 5) {
            int capacity = 5 - row;
            int rowCount = Math.min(capacity, shown - index);
            float baseX = capacity * 8.0F;
            for (int col = 0; col < rowCount; col++) {
                AspectStack aspect = aspects.get(index++);
                float x = -baseX + col * 16.0F;
                float y = -8.0F + row * 16.0F;
                drawAspectOnScanner(poseStack, buffer, aspect, x, y, light);
            }
            row++;
        }

        poseStack.popPose();
    }


    /**
     * Original ItemThaumometerRenderer starts at scale 0.005 and shrinks names
     * wider than 90 pixels. Keep that rule inside the common 0.004 scanner plane
     * so long translated block/entity names no longer spill outside the glass.
     */
    private void drawScannerTitle(PoseStack poseStack, MultiBufferSource buffer, Component title, int light) {
        Minecraft minecraft = Minecraft.getInstance();
        int width = minecraft.font.width(title);
        float originalScale = 0.005F;
        if (width > 90) {
            originalScale = Math.max(0.0024F, originalScale - 0.000025F * (width - 90));
        }
        float relativeScale = originalScale / 0.0040F;
        poseStack.pushPose();
        poseStack.translate(0.0D, -51.0D, 0.0D);
        poseStack.scale(relativeScale, relativeScale, 1.0F);
        minecraft.font.drawInBatch(title, -width / 2.0F, 0.0F, 0xFFF4F4F4, false,
                poseStack.last().pose(), buffer, false, 0, light);
        poseStack.popPose();
    }

    private boolean isScanned(ItemStack scannerStack, TC4ThaumometerTargeting.ScanTarget target) {
        return switch (target.kind()) {
            case NODE -> target.blockPos() != null
                    && (ClientScanData.hasNode(target.stableKey())
                    || ThaumometerItem.hasScannedNode(scannerStack, target.blockPos()));
            case ENTITY -> ClientScanData.hasEntity(target.stableKey())
                    || ThaumometerItem.hasScannedEntity(scannerStack, target.stableKey());
            case ITEM, BLOCK -> ClientScanData.hasObject(target.stableKey())
                    || ThaumometerItem.hasScannedBlock(scannerStack, target.stableKey());
            default -> false;
        };
    }

    private List<AspectStack> sortedAspects(AspectList aspects) {
        List<AspectStack> sorted = new ArrayList<>();
        if (aspects != null) {
            sorted.addAll(aspects.all());
        }
        // TC4 AspectList#getAspectsSorted orders tags alphabetically; it does
        // not reorder the scanner readout by amount.
        sorted.sort(Comparator.comparing(stack -> stack.aspect().id()));
        return sorted;
    }

    private void renderQuestionMark(PoseStack poseStack, MultiBufferSource buffer, int light) {
        Minecraft minecraft = Minecraft.getInstance();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.041D);
        poseStack.scale(0.0060F, -0.0060F, 0.0060F);
        Component question = Component.literal("?");
        int width = minecraft.font.width(question);
        minecraft.font.drawInBatch(question, -width / 2.0F, -4.0F, 0xFFE7E7E7, false,
                poseStack.last().pose(), buffer, false, 0, light);
        poseStack.popPose();
    }

    private void drawAspectOnScanner(PoseStack poseStack, MultiBufferSource buffer, AspectStack stack,
                                     float x, float y, int light) {
        ResourceLocation texture = new ResourceLocation(ThaumcraftMod.MOD_ID,
                "textures/aspects/" + stack.aspect().id() + ".png");
        int rgb = stack.aspect().nativeColor();
        int red = (rgb >> 16) & 255;
        int green = (rgb >> 8) & 255;
        int blue = rgb & 255;
        float size = 16.0F;
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer icon = buffer.getBuffer(RenderType.entityTranslucent(texture));
        icon.vertex(matrix, x, y + size, 0.0F).color(red, green, blue, 245).uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        icon.vertex(matrix, x + size, y + size, 0.0F).color(red, green, blue, 245).uv(1.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        icon.vertex(matrix, x + size, y, 0.0F).color(red, green, blue, 245).uv(1.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        icon.vertex(matrix, x, y, 0.0F).color(red, green, blue, 245).uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();

        String amount = String.valueOf(stack.amount());
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.font.drawInBatch(amount, x + 11.0F, y + 8.0F, 0xFFFFFFFF, true,
                matrix, buffer, false, 0, light);
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
