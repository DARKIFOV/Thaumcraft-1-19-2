package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.render.model.TC4EldritchBossLayerDefinitions;
import com.darkifov.thaumcraft.client.render.model.TC4EldritchCrabModel;
import com.darkifov.thaumcraft.entity.EldritchCrabEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Stage220 renderer for original RenderEldritchCrab / textures/models/crab.png. */
public class TC4EldritchCrabRenderer extends MobRenderer<EldritchCrabEntity, TC4EldritchCrabModel> {
    private static final ResourceLocation CRAB = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/crab.png");
    public TC4EldritchCrabRenderer(EntityRendererProvider.Context context) { super(context, new TC4EldritchCrabModel(context.bakeLayer(TC4EldritchBossLayerDefinitions.ELDRITCH_CRAB)), 0.4F); }
    @Override public ResourceLocation getTextureLocation(EldritchCrabEntity entity) { return CRAB; }
}
