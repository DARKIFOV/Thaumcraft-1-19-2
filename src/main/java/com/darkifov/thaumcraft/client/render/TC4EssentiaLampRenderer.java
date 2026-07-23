package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TC4EssentiaLampBlock;
import com.darkifov.thaumcraft.blockentity.ArcaneBoreBaseBlockEntity;
import com.darkifov.thaumcraft.blockentity.TC4EssentiaLampBlockEntity;
import com.darkifov.thaumcraft.client.render.model.TC4ArcaneBoreModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** Exact shared TC4 TileArcaneLampRenderer nozzle path for Growth/Fertility lamps. */
public final class TC4EssentiaLampRenderer implements BlockEntityRenderer<TC4EssentiaLampBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/Bore.png");
    private final TC4ArcaneBoreModel model;

    public TC4EssentiaLampRenderer(BlockEntityRendererProvider.Context context) {
        model = new TC4ArcaneBoreModel(context.bakeLayer(TC4ArcaneBoreModel.BASE_LAYER));
    }

    @Override
    public void render(TC4EssentiaLampBlockEntity lamp, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        Direction facing = lamp.getBlockState().hasProperty(TC4EssentiaLampBlock.FACING)
                ? lamp.getBlockState().getValue(TC4EssentiaLampBlock.FACING) : Direction.DOWN;
        var consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        renderNozzle(pose, consumer, facing, .5D, 0.0D, .5D, light, overlay);
        if (lamp.getLevel() != null
                && lamp.getLevel().getBlockEntity(lamp.getBlockPos().relative(facing)) instanceof ArcaneBoreBaseBlockEntity) {
            Direction opposite = facing.getOpposite();
            renderNozzle(pose, consumer, opposite, .5D + facing.getStepX(), facing.getStepY(),
                    .5D + facing.getStepZ(), light, overlay);
        }
    }

    private void renderNozzle(PoseStack pose, com.mojang.blaze3d.vertex.VertexConsumer consumer,
                              Direction direction, double x, double y, double z, int light, int overlay) {
        pose.pushPose();
        pose.translate(x, y, z);
        switch (direction) {
            case DOWN -> { pose.translate(-.5D, .5D, 0); pose.mulPose(Vector3f.ZP.rotationDegrees(-90)); }
            case UP -> { pose.translate(.5D, .5D, 0); pose.mulPose(Vector3f.ZP.rotationDegrees(90)); }
            case NORTH -> pose.mulPose(Vector3f.YP.rotationDegrees(90));
            case SOUTH -> pose.mulPose(Vector3f.YP.rotationDegrees(-90));
            case WEST -> pose.mulPose(Vector3f.YP.rotationDegrees(180));
            case EAST -> { }
        }
        model.renderNozzle(pose, consumer, light, overlay == 0 ? OverlayTexture.NO_OVERLAY : overlay);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(TC4EssentiaLampBlockEntity lamp) {
        return true;
    }
}
