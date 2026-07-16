package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.render.model.TC4BrainJarModel;
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

/** Real jar geometry plus original brain/brine layers in all item display contexts. */
public final class BrainJarItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation BRAIN_TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/brain2.png");
    private static final ResourceLocation BRINE_TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/jarbrine.png");
    private static BrainJarItemRenderer instance;
    private TC4BrainJarModel model;

    private BrainJarItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static BrainJarItemRenderer instance() {
        if (instance == null) {
            instance = new BrainJarItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        if (model == null) {
            model = new TC4BrainJarModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(TC4BrainJarModel.LAYER));
        }
        poseStack.pushPose();
        applyTransform(transformType, poseStack);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                ThaumcraftMod.BRAIN_JAR.get().defaultBlockState(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.60D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
        poseStack.scale(0.4F, -0.4F, -0.4F);
        model.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(BRAIN_TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer brine = buffer.getBuffer(RenderType.entityTranslucent(BRINE_TEXTURE));
        box(matrix, brine, 0.25F, 0.0625F, 0.25F, 0.75F, 0.6875F, 0.75F, packedLight);
        poseStack.popPose();
    }

    private static void applyTransform(ItemTransforms.TransformType type, PoseStack poseStack) {
        if (type == ItemTransforms.TransformType.GUI) {
            poseStack.translate(0.50D, 0.48D, 0.50D);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(225.0F));
            poseStack.scale(0.82F, 0.82F, 0.82F);
            poseStack.translate(-0.50D, -0.50D, -0.50D);
        } else if (type == ItemTransforms.TransformType.GROUND) {
            poseStack.translate(0.50D, 0.12D, 0.50D);
            poseStack.scale(0.55F, 0.55F, 0.55F);
            poseStack.translate(-0.50D, 0.0D, -0.50D);
        } else if (type == ItemTransforms.TransformType.FIXED) {
            poseStack.translate(0.50D, 0.50D, 0.50D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            poseStack.scale(0.80F, 0.80F, 0.80F);
            poseStack.translate(-0.50D, -0.50D, -0.50D);
        } else if (type.firstPerson()) {
            poseStack.translate(0.38D, 0.18D, 0.22D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(45.0F));
            poseStack.scale(0.62F, 0.62F, 0.62F);
        } else {
            poseStack.translate(0.38D, 0.16D, 0.22D);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(45.0F));
            poseStack.scale(0.58F, 0.58F, 0.58F);
        }
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
