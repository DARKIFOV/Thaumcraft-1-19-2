package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AlchemicalCentrifugeBlockEntity;
import com.darkifov.thaumcraft.client.render.model.TC4CentrifugeModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Exact ModelCentrifuge geometry/UV renderer; only the inner assembly rotates in-world. */
public final class AlchemicalCentrifugeRenderer implements BlockEntityRenderer<AlchemicalCentrifugeBlockEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/models/centrifuge.png");
    private final TC4CentrifugeModel model;

    public AlchemicalCentrifugeRenderer(BlockEntityRendererProvider.Context context) {
        model = new TC4CentrifugeModel(context.bakeLayer(TC4CentrifugeModel.LAYER));
    }

    @Override
    public void render(AlchemicalCentrifugeBlockEntity tile, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderBoxes(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(tile.rotation()));
        model.renderSpinnyBit(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    public static void renderStandalone(TC4CentrifugeModel model, PoseStack poseStack,
                                        MultiBufferSource buffer, int packedLight, int packedOverlay,
                                        float rotationDegrees) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderBoxes(poseStack, consumer, packedLight, packedOverlay);
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(rotationDegrees));
        model.renderSpinnyBit(poseStack, consumer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
