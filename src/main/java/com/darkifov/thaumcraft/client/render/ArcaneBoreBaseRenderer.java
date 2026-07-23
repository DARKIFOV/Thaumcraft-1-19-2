package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ArcaneBoreBaseBlock;
import com.darkifov.thaumcraft.blockentity.ArcaneBoreBaseBlockEntity;
import com.darkifov.thaumcraft.client.render.model.TC4ArcaneBoreModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** Exact TC4 base renderer: stationary body and independently directed output nozzle. */
public final class ArcaneBoreBaseRenderer implements BlockEntityRenderer<ArcaneBoreBaseBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/Bore.png");
    private final TC4ArcaneBoreModel model;
    public ArcaneBoreBaseRenderer(BlockEntityRendererProvider.Context context) {
        model = new TC4ArcaneBoreModel(context.bakeLayer(TC4ArcaneBoreModel.BASE_LAYER));
    }
    @Override public void render(ArcaneBoreBaseBlockEntity base, float partialTick, PoseStack pose,
                                 MultiBufferSource buffer, int light, int overlay) {
        Direction facing = base.getBlockState().getValue(ArcaneBoreBaseBlock.FACING);
        int packedOverlay = overlay == 0 ? OverlayTexture.NO_OVERLAY : overlay;
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        pose.pushPose();
        pose.translate(.5, 0, .5);
        model.renderBaseBody(pose, consumer, light, packedOverlay);
        pose.pushPose();
        pose.mulPose(Vector3f.YP.rotationDegrees(switch (facing) {
            case NORTH -> 90; case SOUTH -> 270; case WEST -> 180; default -> 0;
        }));
        model.renderNozzle(pose, consumer, light, packedOverlay);
        pose.popPose();
        pose.popPose();
    }
}
