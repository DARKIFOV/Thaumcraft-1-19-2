package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.MindSpiderEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleTypes;

/** Dedicated Stage283-302 MindSpider renderer replacing the Stage273 block placeholder. */
public class TC4MindSpiderRenderer extends TC4BlockMobRenderer<MindSpiderEntity> {
    public TC4MindSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, () -> ThaumcraftMod.TAINT_FIBRES.get().defaultBlockState(), 0.16F, 0.16F);
    }

    @Override
    public void render(MindSpiderEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.isHarmless()) {
            var viewer = Minecraft.getInstance().player;
            if (viewer == null || !viewer.getGameProfile().getName().equals(entity.getViewer())) {
                return;
            }
        }

        poseStack.pushPose();
        if (entity.isHarmless()) {
            poseStack.scale(0.72F, 0.72F, 0.72F);
        }
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
        if (entity.level.isClientSide && entity.tickCount % 7 == 0) {
            entity.level.addParticle(ParticleTypes.PORTAL, entity.getRandomX(0.15D), entity.getY() + 0.05D, entity.getRandomZ(0.15D), 0.0D, 0.0D, 0.0D);
        }
    }
}
