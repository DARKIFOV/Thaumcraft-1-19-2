package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.render.model.TC4TravelingTrunkModel;
import com.darkifov.thaumcraft.entity.TravelingTrunkEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class TravelingTrunkRenderer extends MobRenderer<TravelingTrunkEntity, TC4TravelingTrunkModel> {
    private static final ResourceLocation NORMAL = texture("trunk.png");
    private static final ResourceLocation ANGRY = texture("trunkangry.png");

    public TravelingTrunkRenderer(EntityRendererProvider.Context context) {
        super(context, new TC4TravelingTrunkModel(context.bakeLayer(TC4TravelingTrunkModel.LAYER)), 0.5F);
    }

    private static ResourceLocation texture(String file) {
        return new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/" + file);
    }

    @Override
    protected void scale(TravelingTrunkEntity entity, PoseStack poseStack, float partialTickTime) {
        float bounce = entity.isOnGround() ? 1.0F : 0.92F;
        poseStack.scale(0.86F / bounce, 0.72F * bounce, 0.86F / bounce);
    }

    @Override
    public ResourceLocation getTextureLocation(TravelingTrunkEntity entity) {
        return entity.getAnger() > 0 ? ANGRY : NORMAL;
    }
}
