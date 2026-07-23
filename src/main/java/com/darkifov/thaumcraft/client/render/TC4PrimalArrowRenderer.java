package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.entity.projectile.PrimalArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Vanilla arrow geometry plus the colored TC4 aura emitted by the entity. */
public final class TC4PrimalArrowRenderer extends ArrowRenderer<PrimalArrowEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("textures/entity/projectiles/arrow.png");

    public TC4PrimalArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(PrimalArrowEntity entity) {
        return TEXTURE;
    }
}
