package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.render.model.TC4TaintSporeModel;
import com.darkifov.thaumcraft.entity.TaintSporeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Pulsing full-bright TC4 taint spore renderer. */
public final class TaintSporeRenderer extends MobRenderer<TaintSporeEntity, TC4TaintSporeModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/taint_spore.png");

    public TaintSporeRenderer(EntityRendererProvider.Context context) {
        super(context, new TC4TaintSporeModel(context.bakeLayer(TC4TaintSporeModel.LAYER)), 0.25F);
    }

    @Override protected void scale(TaintSporeEntity entity, PoseStack poseStack, float partialTickTime) {
        float display = entity.displaySize;
        if (display < entity.getSporeSize()) display += 0.02F * partialTickTime;
        float base = 0.12F * display;
        float pulse = 0.025F * Mth.sin((entity.tickCount + partialTickTime) * 0.075F);
        poseStack.scale(base + pulse, base - pulse, base + pulse);
    }

    @Override protected int getBlockLightLevel(TaintSporeEntity entity, BlockPos pos) { return 15; }
    @Override public ResourceLocation getTextureLocation(TaintSporeEntity entity) { return TEXTURE; }
}
