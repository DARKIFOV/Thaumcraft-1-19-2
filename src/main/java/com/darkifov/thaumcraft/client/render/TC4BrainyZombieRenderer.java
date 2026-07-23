package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.BrainyZombieEntity;
import com.darkifov.thaumcraft.entity.GiantBrainyZombieEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Original RenderBrainyZombie texture and giant anger scaling. */
public final class TC4BrainyZombieRenderer<T extends BrainyZombieEntity>
        extends MobRenderer<T, ZombieModel<T>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/bzombie.png");

    public TC4BrainyZombieRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        if (entity instanceof GiantBrainyZombieEntity giant) {
            float scale = giant.getAnger();
            poseStack.scale(scale, scale, scale);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }
}
