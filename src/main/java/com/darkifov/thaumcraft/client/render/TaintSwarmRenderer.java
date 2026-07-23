package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.entity.TaintSwarmEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

/** TC4 parity renderer: the swarm body is invisible; its cloud is emitted by the entity. */
public final class TaintSwarmRenderer extends EntityRenderer<TaintSwarmEntity> {
    public TaintSwarmRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(TaintSwarmEntity entity, float yaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        // Intentionally no body geometry, matching TC4's RenderTaintSwarm.
    }

    @Override
    public ResourceLocation getTextureLocation(TaintSwarmEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
