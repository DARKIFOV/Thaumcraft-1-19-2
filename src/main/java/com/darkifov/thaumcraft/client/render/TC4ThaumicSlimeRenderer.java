package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.TC4ThaumicSlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Slime;

/** Uses the vanilla two-layer slime geometry with TC4's original tslime texture. */
public class TC4ThaumicSlimeRenderer extends SlimeRenderer {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/tslime.png");

    public TC4ThaumicSlimeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void scale(Slime entity, PoseStack poseStack, float partialTickTime) {
        if (entity instanceof TC4ThaumicSlimeEntity slime) {
            float scale = 0.6F * (float) Math.sqrt(Math.max(1, slime.getTc4Size()));
            poseStack.scale(scale, scale, scale);
            return;
        }
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(Slime entity) {
        return TEXTURE;
    }
}
