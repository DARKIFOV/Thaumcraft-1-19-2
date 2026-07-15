package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.BrainJarBlockEntity;
import com.darkifov.thaumcraft.client.render.model.TC4BrainJarModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Brain and brine layer from TC4 TileJarRenderer; the glass jar itself is the block model. */
public final class BrainJarRenderer implements BlockEntityRenderer<BrainJarBlockEntity> {
    private static final ResourceLocation BRAIN_TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/brain2.png");
    private static final ResourceLocation BRINE_TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/jarbrine.png");
    private final TC4BrainJarModel model;

    public BrainJarRenderer(BlockEntityRendererProvider.Context context) {
        model = new TC4BrainJarModel(context.bakeLayer(TC4BrainJarModel.LAYER));
    }

    @Override
    public void render(BrainJarBlockEntity brain, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float bob = (float) Math.sin((Minecraft.getInstance().player == null ? 0 :
                Minecraft.getInstance().player.tickCount) / 14.0F) * 0.03F + 0.03F;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.57D + bob, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotation(brain.rotation(partialTick)));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
        poseStack.scale(0.4F, -0.4F, -0.4F);
        model.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(BRAIN_TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        poseStack.pushPose();
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer brine = buffer.getBuffer(RenderType.entityTranslucent(BRINE_TEXTURE));
        box(matrix, brine, 0.25F, 0.0625F, 0.25F, 0.75F, 0.6875F, 0.75F, packedLight);
        poseStack.popPose();
    }

    private static void box(Matrix4f matrix, VertexConsumer out, float x0, float y0, float z0,
                            float x1, float y1, float z1, int light) {
        quad(matrix,out,x0,y1,z0,x1,y1,z0,x1,y1,z1,x0,y1,z1,light);
        quad(matrix,out,x0,y0,z0,x0,y1,z0,x0,y1,z1,x0,y0,z1,light);
        quad(matrix,out,x1,y0,z1,x1,y1,z1,x1,y1,z0,x1,y0,z0,light);
        quad(matrix,out,x0,y0,z1,x0,y1,z1,x1,y1,z1,x1,y0,z1,light);
        quad(matrix,out,x1,y0,z0,x1,y1,z0,x0,y1,z0,x0,y0,z0,light);
    }

    private static void quad(Matrix4f m, VertexConsumer v,
                             float ax,float ay,float az,float bx,float by,float bz,
                             float cx,float cy,float cz,float dx,float dy,float dz,int light) {
        vertex(m,v,ax,ay,az,0,1,light); vertex(m,v,bx,by,bz,1,1,light);
        vertex(m,v,cx,cy,cz,1,0,light); vertex(m,v,dx,dy,dz,0,0,light);
    }

    private static void vertex(Matrix4f m, VertexConsumer v, float x,float y,float z,float u,float vv,int light) {
        v.vertex(m,x,y,z).color(255,255,255,190).uv(u,vv).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light).normal(0,1,0).endVertex();
    }
}
