package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.entity.projectile.GolemDartEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Narrower TC4 dart silhouette using the vanilla arrow atlas. */
public final class TC4GolemDartRenderer extends ArrowRenderer<GolemDartEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("textures/entity/projectiles/arrow.png");

    public TC4GolemDartRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(GolemDartEntity entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.75F, 0.75F, 0.75F);
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(GolemDartEntity entity) {
        return TEXTURE;
    }
}
