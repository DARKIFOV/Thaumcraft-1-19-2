package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.render.model.TC4GolemAccessoriesModel;
import com.darkifov.thaumcraft.client.render.model.TC4ThaumGolemModel;
import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.darkifov.thaumcraft.golem.GolemMaterial;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * TC4 RenderGolemBase parity renderer: original body model/material textures,
 * accessory pass, damage pass and carried-stack pass.
 */
public final class ThaumGolemRenderer extends MobRenderer<ThaumGolemEntity, TC4ThaumGolemModel> {
    private static final ResourceLocation STRAW = texture("golem_straw");
    private static final ResourceLocation WOOD = texture("golem_wood");
    private static final ResourceLocation TALLOW = texture("golem_tallow");
    private static final ResourceLocation CLAY = texture("golem_clay");
    private static final ResourceLocation FLESH = texture("golem_flesh");
    private static final ResourceLocation STONE = texture("golem_stone");
    private static final ResourceLocation IRON = texture("golem_iron");
    private static final ResourceLocation THAUMIUM = texture("golem_thaumium");

    public ThaumGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new TC4ThaumGolemModel(context.bakeLayer(TC4ThaumGolemModel.LAYER)), 0.25F);
        addLayer(new TC4GolemAccessoriesLayer(this,
                new TC4GolemAccessoriesModel(context.bakeLayer(TC4GolemAccessoriesModel.LAYER))));
        addLayer(new TC4GolemDamageLayer(this,
                new TC4ThaumGolemModel(context.bakeLayer(TC4ThaumGolemModel.LAYER))));
        addLayer(new TC4GolemCarriedItemLayer(this));
    }

    @Override
    protected void scale(ThaumGolemEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.4F, 0.4F, 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(ThaumGolemEntity entity) {
        GolemMaterial material = entity.getGolemMaterial();
        return switch (material) {
            case STRAW -> STRAW;
            case WOOD -> WOOD;
            case TALLOW -> TALLOW;
            case CLAY -> CLAY;
            case FLESH -> FLESH;
            case STONE -> STONE;
            case IRON -> IRON;
            case THAUMIUM -> THAUMIUM;
        };
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/" + name + ".png");
    }
}
