package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.render.model.TC4FireBatModel;
import com.darkifov.thaumcraft.entity.TC4FireBatEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Modern renderer preserving TC4 RenderFireBat scale, bobbing, textures and full-bright look. */
public class TC4FireBatRenderer extends MobRenderer<TC4FireBatEntity, TC4FireBatModel> {
    private static final ResourceLocation FIRE_TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/firebat.png");
    private static final ResourceLocation VAMPIRE_TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/vampirebat.png");

    public TC4FireBatRenderer(EntityRendererProvider.Context context) {
        super(context, new TC4FireBatModel(context.bakeLayer(TC4FireBatModel.LAYER)), 0.25F);
    }

    @Override
    protected void scale(TC4FireBatEntity entity, PoseStack poseStack, float partialTickTime) {
        float scale = entity.isDevil() || entity.isVampire() ? 0.60F : 0.35F;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    protected void setupRotations(TC4FireBatEntity entity, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTicks) {
        poseStack.translate(0.0D, Mth.cos(ageInTicks * 0.3F) * 0.1F, 0.0D);
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
    }

    @Override
    protected int getBlockLightLevel(TC4FireBatEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(TC4FireBatEntity entity) {
        return entity.isVampire() ? VAMPIRE_TEXTURE : FIRE_TEXTURE;
    }
}
