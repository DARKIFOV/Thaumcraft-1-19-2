package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import com.darkifov.thaumcraft.client.render.model.TC4AlembicModel;
import com.darkifov.thaumcraft.client.render.model.TC4ArcaneBoreModel;
import com.darkifov.thaumcraft.essentia.EssentiaTubeConnections;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

/** TC4 TileAlembicRenderer: conditional OBJ parts, facing label and non-tube transport nozzles. */
public class AlembicRenderer implements BlockEntityRenderer<AlembicBlockEntity> {
    private static final ResourceLocation MODEL_TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/alembic.png");
    private static final ResourceLocation LABEL_TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/label.png");
    private static final ResourceLocation BORE_TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/Bore.png");
    private final TC4ArcaneBoreModel boreModel;

    public AlembicRenderer(BlockEntityRendererProvider.Context context) {
        boreModel = new TC4ArcaneBoreModel(context.bakeLayer(TC4ArcaneBoreModel.BASE_LAYER));
    }

    @Override
    public void render(AlembicBlockEntity alembic, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        renderBody(alembic, pose, buffer, light, overlay);
        renderLabel(alembic.aspectFilter(), alembic.facing(), pose, buffer, light);
        renderConnectors(alembic, pose, buffer, light, overlay);
    }

    private static void renderBody(AlembicBlockEntity alembic, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        pose.pushPose();
        pose.translate(.5D, 0D, .5D);
        pose.mulPose(Vector3f.XP.rotationDegrees(-90F));
        rotateForFacing(pose, alembic.facing());
        VertexConsumer model = buffer.getBuffer(RenderType.entityCutoutNoCull(MODEL_TEXTURE));
        int ov = overlay == 0 ? OverlayTexture.NO_OVERLAY : overlay;
        if (alembic.aboveFurnace()) {
            TC4AlembicModel.renderTubeMain(pose.last(), model, light, ov);
            TC4AlembicModel.renderLegs(pose.last(), model, light, ov);
        } else if (alembic.aboveAlembic()) {
            TC4AlembicModel.renderTubeMain(pose.last(), model, light, ov);
            TC4AlembicModel.renderTubeSmall(pose.last(), model, light, ov);
        } else {
            TC4AlembicModel.renderLegs(pose.last(), model, light, ov);
        }
        TC4AlembicModel.renderPot(pose.last(), model, light, ov);
        TC4AlembicModel.renderPanel(pose.last(), model, light, ov);
        pose.popPose();
    }

    public static void renderStandalone(PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        pose.pushPose();
        pose.translate(.5D, 0D, .5D);
        pose.mulPose(Vector3f.XP.rotationDegrees(-90F));
        VertexConsumer model = buffer.getBuffer(RenderType.entityCutoutNoCull(MODEL_TEXTURE));
        int ov = overlay == 0 ? OverlayTexture.NO_OVERLAY : overlay;
        TC4AlembicModel.renderLegs(pose.last(), model, light, ov);
        TC4AlembicModel.renderPot(pose.last(), model, light, ov);
        TC4AlembicModel.renderPanel(pose.last(), model, light, ov);
        pose.popPose();
    }

    private static void rotateForFacing(PoseStack pose, Direction facing) {
        switch (facing) {
            case EAST -> pose.mulPose(Vector3f.ZP.rotationDegrees(180F));
            case SOUTH -> pose.mulPose(Vector3f.ZP.rotationDegrees(90F));
            case NORTH -> pose.mulPose(Vector3f.ZP.rotationDegrees(270F));
            default -> { }
        }
    }

    private void renderLabel(Aspect filter, Direction facing, PoseStack pose, MultiBufferSource buffer, int light) {
        if (filter == null) return;
        pose.pushPose();
        pose.translate(.5D, .468D, .5D);
        switch (facing) {
            case EAST -> { pose.translate(.409D, 0, 0); pose.mulPose(Vector3f.YP.rotationDegrees(-90F)); }
            case WEST -> { pose.translate(-.409D, 0, 0); pose.mulPose(Vector3f.YP.rotationDegrees(90F)); }
            case SOUTH -> { pose.translate(0, 0, .409D); pose.mulPose(Vector3f.YP.rotationDegrees(180F)); }
            default -> pose.translate(0, 0, -.409D);
        }
        Matrix4f matrix = pose.last().pose();
        quad(matrix, buffer.getBuffer(RenderType.entityTranslucent(LABEL_TEXTURE)), .135F, 255, 255, 255, light, 0F);
        ResourceLocation icon = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + filter.id() + ".png");
        quad(matrix, buffer.getBuffer(RenderType.entityTranslucent(icon)), .104F, 255, 255, 255, light, .003F);
        pose.popPose();
    }

    private void renderConnectors(AlembicBlockEntity alembic, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        if (alembic.getLevel() == null) return;
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(BORE_TEXTURE));
        for (Direction direction : Direction.values()) {
            if (!alembic.canOutputTo(direction)) continue;
            BlockEntity neighbor = alembic.getLevel().getBlockEntity(alembic.getBlockPos().relative(direction));
            if (neighbor == null || neighbor instanceof EssentiaTubeBlockEntity || !EssentiaTubeConnections.isTransportEndpoint(neighbor)) continue;
            pose.pushPose();
            pose.translate(.5D, 0D, .5D);
            orientNozzle(pose, direction);
            boreModel.renderNozzle(pose, consumer, light, overlay == 0 ? OverlayTexture.NO_OVERLAY : overlay);
            pose.popPose();
        }
    }

    private static void orientNozzle(PoseStack pose, Direction direction) {
        switch (direction) {
            case DOWN -> { pose.translate(-.5D, .5D, 0); pose.mulPose(Vector3f.ZP.rotationDegrees(-90)); }
            case UP -> { pose.translate(.5D, .5D, 0); pose.mulPose(Vector3f.ZP.rotationDegrees(90)); }
            case NORTH -> pose.mulPose(Vector3f.YP.rotationDegrees(90));
            case SOUTH -> pose.mulPose(Vector3f.YP.rotationDegrees(-90));
            case WEST -> pose.mulPose(Vector3f.YP.rotationDegrees(180));
            case EAST -> { }
        }
    }

    private static void quad(Matrix4f m, VertexConsumer c, float half, int r, int g, int b, int light, float z) {
        vertex(m,c,-half,-half,z,0,1,r,g,b,light); vertex(m,c,half,-half,z,1,1,r,g,b,light);
        vertex(m,c,half,half,z,1,0,r,g,b,light); vertex(m,c,-half,half,z,0,0,r,g,b,light);
    }
    private static void vertex(Matrix4f m, VertexConsumer c, float x,float y,float z,float u,float v,int r,int g,int b,int light) {
        c.vertex(m,x,y,z).color(r,g,b,255).uv(u,v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0,0,1).endVertex();
    }

    @Override public boolean shouldRenderOffScreen(AlembicBlockEntity alembic) { return true; }
}
