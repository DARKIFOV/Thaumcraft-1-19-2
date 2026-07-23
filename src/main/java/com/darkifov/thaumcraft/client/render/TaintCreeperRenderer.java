package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.TaintCreeperEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class TaintCreeperRenderer extends MobRenderer<TaintCreeperEntity, CreeperModel<TaintCreeperEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/creeper.png");
    public TaintCreeperRenderer(EntityRendererProvider.Context context) {
        super(context, new CreeperModel<>(context.bakeLayer(ModelLayers.CREEPER)), 0.5F);
    }
    @Override protected void scale(TaintCreeperEntity entity, PoseStack poseStack, float partialTick) {
        float flash = Mth.clamp(entity.getTc4FlashIntensity(partialTick), 0.0F, 1.0F);
        float wobble = 1.0F + Mth.sin(flash * 100.0F) * flash * 0.01F;
        float power = flash * flash;
        power *= power;
        float xz = (1.0F + power * 0.4F) * wobble;
        float y = (1.0F + power * 0.1F) / wobble;
        poseStack.scale(xz, y, xz);
    }
    @Override protected float getWhiteOverlayProgress(TaintCreeperEntity entity, float partialTick) {
        float flash = entity.getTc4FlashIntensity(partialTick);
        return (int) (flash * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(flash * 0.2F, 0.0F, 1.0F);
    }
    @Override public ResourceLocation getTextureLocation(TaintCreeperEntity entity) { return TEXTURE; }
}
