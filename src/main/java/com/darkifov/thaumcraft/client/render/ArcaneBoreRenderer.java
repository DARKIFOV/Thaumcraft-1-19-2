package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ArcaneBoreBlock;
import com.darkifov.thaumcraft.blockentity.ArcaneBoreBlockEntity;
import com.darkifov.thaumcraft.client.render.model.TC4ArcaneBoreCoreModel;
import com.darkifov.thaumcraft.client.render.model.TC4ArcaneBoreModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** TC4 TileArcaneBoreRenderer: separated mount/nozzle/emitter, triple vortex and jar core. */
public final class ArcaneBoreRenderer implements BlockEntityRenderer<ArcaneBoreBlockEntity> {
    private static final ResourceLocation BORE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/Bore.png");
    private static final ResourceLocation VORTEX = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/misc/vortex.png");
    private static final ResourceLocation JAR = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/jar.png");
    private static final ResourceLocation BEAM = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/misc/beam1.png");
    private final TC4ArcaneBoreModel model;
    private final TC4ArcaneBoreCoreModel core;

    public ArcaneBoreRenderer(BlockEntityRendererProvider.Context context) {
        model = new TC4ArcaneBoreModel(context.bakeLayer(TC4ArcaneBoreModel.BORE_LAYER));
        core = new TC4ArcaneBoreCoreModel(context.bakeLayer(TC4ArcaneBoreCoreModel.LAYER));
    }

    @Override public void render(ArcaneBoreBlockEntity bore, float partialTick, PoseStack pose,
                                 MultiBufferSource buffer, int light, int overlay) {
        Direction facing = bore.getBlockState().getValue(ArcaneBoreBlock.FACING);
        int packedOverlay = overlay == 0 ? OverlayTexture.NO_OVERLAY : overlay;
        VertexConsumer boreConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(BORE));
        pose.pushPose();
        pose.translate(.5, .5, .5);
        if (bore.clientTarget() != null) renderDigBeams(bore, partialTick, pose, buffer);
        orientToFacing(pose, facing);

        pose.pushPose();
        if (bore.getBlockState().getValue(ArcaneBoreBlock.INVERTED)) pose.mulPose(Vector3f.ZP.rotationDegrees(180));
        pose.translate(0, -.5, 0);
        model.renderBoreMount(pose, boreConsumer, light, packedOverlay);
        pose.popPose();

        pose.pushPose();
        pose.mulPose(Vector3f.ZP.rotationDegrees(90));
        pose.translate(0, -.5, 0);
        model.renderBoreNozzle(pose, boreConsumer, light, packedOverlay);
        pose.popPose();

        pose.pushPose();
        pose.mulPose(Vector3f.YP.rotationDegrees(bore.clientTopRotation()));
        pose.translate(0, .5, 0);
        model.renderEmitter(pose, boreConsumer, light, packedOverlay, bore.hasFocusForRender());
        pose.popPose();

        float rotation = ((Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime()) % 45L
                + partialTick) * 8.0F;
        renderVortex(pose, buffer, rotation, -.17F, .4F, 1.0F);
        renderVortex(pose, buffer, -rotation, -.21F, .3F, .8F);
        renderVortex(pose, buffer, rotation, -.25F, .2F, .8F);

        pose.pushPose();
        pose.mulPose(Vector3f.ZP.rotationDegrees(180));
        pose.translate(0, .3, 0);
        pose.scale(.6F, .6F, .6F);
        core.render(pose, buffer.getBuffer(RenderType.entityTranslucent(JAR)), light, packedOverlay);
        pose.popPose();
        pose.popPose();
    }

    private static void renderDigBeams(ArcaneBoreBlockEntity bore, float partialTick,
                                       PoseStack pose, MultiBufferSource buffer) {
        Vec3 end = Vec3.atCenterOf(bore.clientTarget()).subtract(Vec3.atCenterOf(bore.getBlockPos()));
        double length = end.length();
        if (length < 0.01D) return;
        Vec3 direction = end.scale(1.0D / length);
        Vec3 start = direction.scale(0.75D);
        Vec3 side = direction.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 1.0E-6D) side = direction.cross(new Vec3(1, 0, 0));
        side = side.normalize();
        Vec3 up = direction.cross(side).normalize();
        float ticks = (bore.getLevel() == null ? 0L : bore.getLevel().getGameTime()) + partialTick;
        VertexConsumer consumer = buffer.getBuffer(TC4NodeRenderTypes.node(BEAM, true, false));
        renderCrossedBeam(pose.last().pose(), consumer, start, end, side.scale(.055D), up.scale(.055D),
                length, ticks, 0x00FF66, 230, 0.0F);
        renderCrossedBeam(pose.last().pose(), consumer, start, end, side.scale(.035D), up.scale(.035D),
                length, -ticks, 0xFF88D5, 210, 0.3333F);
    }

    private static void renderCrossedBeam(Matrix4f matrix, VertexConsumer consumer, Vec3 start, Vec3 end,
                                          Vec3 side, Vec3 up, double length, float ticks,
                                          int color, int alpha, float uOffset) {
        int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
        float v0 = -ticks * .2F;
        float v1 = (float) length + v0;
        beamQuad(matrix, consumer, start.subtract(side), end.subtract(side), end.add(side), start.add(side),
                uOffset, uOffset + .3333F, v0, v1, r, g, b, alpha);
        beamQuad(matrix, consumer, start.subtract(up), end.subtract(up), end.add(up), start.add(up),
                uOffset, uOffset + .3333F, v0, v1, r, g, b, alpha);
    }

    private static void beamQuad(Matrix4f matrix, VertexConsumer consumer,
                                 Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                                 float u0, float u1, float v0, float v1,
                                 int r, int g, int bl, int alpha) {
        beamVertex(matrix, consumer, a, u0, v1, r, g, bl, alpha);
        beamVertex(matrix, consumer, b, u0, v0, r, g, bl, alpha);
        beamVertex(matrix, consumer, c, u1, v0, r, g, bl, alpha);
        beamVertex(matrix, consumer, d, u1, v1, r, g, bl, alpha);
    }

    private static void beamVertex(Matrix4f matrix, VertexConsumer consumer, Vec3 point,
                                   float u, float v, int r, int g, int b, int alpha) {
        consumer.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(r, g, b, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
    }

    private static void orientToFacing(PoseStack pose, Direction facing) {
        switch (facing) {
            case NORTH -> pose.mulPose(Vector3f.YP.rotationDegrees(90));
            case SOUTH -> pose.mulPose(Vector3f.YP.rotationDegrees(-90));
            case WEST -> pose.mulPose(Vector3f.YP.rotationDegrees(180));
            case UP -> pose.mulPose(Vector3f.ZP.rotationDegrees(-90));
            case DOWN -> pose.mulPose(Vector3f.ZP.rotationDegrees(90));
            default -> { }
        }
    }

    private static void renderVortex(PoseStack pose, MultiBufferSource buffer, float rotation,
                                     float y, float size, float alpha) {
        pose.pushPose();
        pose.translate(0, y, 0);
        pose.mulPose(Vector3f.XP.rotationDegrees(-90));
        pose.mulPose(Vector3f.ZP.rotationDegrees(rotation));
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(VORTEX));
        float half = size * .5F;
        int a = Math.round(alpha * 255.0F);
        vertex(pose.last(), consumer, -half, -half, 0, 0, 1, a);
        vertex(pose.last(), consumer, half, -half, 0, 1, 1, a);
        vertex(pose.last(), consumer, half, half, 0, 1, 0, a);
        vertex(pose.last(), consumer, -half, half, 0, 0, 0, a);
        pose.popPose();
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer,
                               float x, float y, float z, float u, float v, int alpha) {
        consumer.vertex(pose.pose(), x, y, z).color(255, 255, 255, alpha).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.normal(), 0, 0, 1).endVertex();
    }

    @Override public boolean shouldRenderOffScreen(ArcaneBoreBlockEntity blockEntity) { return true; }
}
