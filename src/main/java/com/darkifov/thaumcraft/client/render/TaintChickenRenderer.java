package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.TaintChickenEntity;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class TaintChickenRenderer extends MobRenderer<TaintChickenEntity, ChickenModel<TaintChickenEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/chicken.png");
    public TaintChickenRenderer(EntityRendererProvider.Context context) {
        super(context, new ChickenModel<>(context.bakeLayer(ModelLayers.CHICKEN)), 0.3F);
    }
    @Override public ResourceLocation getTextureLocation(TaintChickenEntity entity) { return TEXTURE; }
}
