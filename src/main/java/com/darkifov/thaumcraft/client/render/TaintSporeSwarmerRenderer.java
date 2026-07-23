package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.render.model.TC4TaintSporeSwarmerModel;
import com.darkifov.thaumcraft.entity.TaintSporeSwarmerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/** Full-bright TC4 Spore Swarmer renderer using the original taint-spore texture. */
public final class TaintSporeSwarmerRenderer
        extends MobRenderer<TaintSporeSwarmerEntity, TC4TaintSporeSwarmerModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/models/taint_spore.png");

    public TaintSporeSwarmerRenderer(EntityRendererProvider.Context context) {
        super(context, new TC4TaintSporeSwarmerModel(
                context.bakeLayer(TC4TaintSporeSwarmerModel.LAYER)), 0.25F);
    }

    @Override
    protected int getBlockLightLevel(TaintSporeSwarmerEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(TaintSporeSwarmerEntity entity) {
        return TEXTURE;
    }
}
