package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.TaintSheepEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class TaintSheepRenderer extends MobRenderer<TaintSheepEntity, SheepModel<TaintSheepEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/sheep.png");
    private static final ResourceLocation FUR = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/sheep_fur.png");
    public TaintSheepRenderer(EntityRendererProvider.Context context) {
        super(context, new SheepModel<>(context.bakeLayer(ModelLayers.SHEEP)), 0.7F);
        addLayer(new TaintFurLayer(this, new SheepFurModel<>(context.bakeLayer(ModelLayers.SHEEP_FUR))));
    }
    @Override public ResourceLocation getTextureLocation(TaintSheepEntity entity) { return TEXTURE; }

    private static final class TaintFurLayer extends RenderLayer<TaintSheepEntity, SheepModel<TaintSheepEntity>> {
        private final SheepFurModel<TaintSheepEntity> furModel;
        private TaintFurLayer(RenderLayerParent<TaintSheepEntity, SheepModel<TaintSheepEntity>> parent,
                              SheepFurModel<TaintSheepEntity> furModel) {
            super(parent);
            this.furModel = furModel;
        }
        @Override public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                                     TaintSheepEntity sheep, float limbSwing, float limbSwingAmount,
                                     float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (sheep.isSheared() || sheep.isInvisible()) return;
            getParentModel().copyPropertiesTo(furModel);
            furModel.prepareMobModel(sheep, limbSwing, limbSwingAmount, partialTick);
            furModel.setupAnim(sheep, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer vertex = buffers.getBuffer(RenderType.entityCutoutNoCull(FUR));
            furModel.renderToBuffer(poseStack, vertex, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
