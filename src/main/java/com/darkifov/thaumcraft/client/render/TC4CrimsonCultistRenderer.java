package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.render.model.TC4CrimsonCultistModel;
import com.darkifov.thaumcraft.entity.CrimsonCultistEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Replaces the four block-placeholder cultist renderers with TC4 humanoids. */
public final class TC4CrimsonCultistRenderer extends HumanoidMobRenderer<CrimsonCultistEntity, TC4CrimsonCultistModel> {
    private static final ResourceLocation SKIN = tex("cultist.png");
    private static final ResourceLocation ROBE = tex("cultist_robe_armor.png");
    private static final ResourceLocation KNIGHT = tex("cultist_plate_armor.png");
    private static final ResourceLocation LEADER = tex("cultist_leader_armor.png");

    public TC4CrimsonCultistRenderer(EntityRendererProvider.Context context) {
        super(context, new TC4CrimsonCultistModel(context.bakeLayer(TC4CrimsonCultistModel.BASE)), 0.5F);
        addLayer(new CultistArmorLayer(this, context));
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/models/" + name);
    }

    @Override
    protected void scale(CrimsonCultistEntity entity, PoseStack poseStack, float partialTickTime) {
        if (entity.role() == CrimsonCultistEntity.Role.LEADER) {
            poseStack.scale(1.25F, 1.25F, 1.25F);
        }
    }

    @Override
    public void render(CrimsonCultistEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        boolean ritual = entity.isRitualCasting();
        if (ritual) {
            float bob = Mth.sin((entity.tickCount + partialTicks + entity.getId() * 31.0F) / 9.0F) * 0.1F + 0.21F;
            poseStack.pushPose();
            poseStack.translate(0.0D, bob, 0.0D);
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            poseStack.popPose();
            return;
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CrimsonCultistEntity entity) {
        return SKIN;
    }

    private static final class CultistArmorLayer extends RenderLayer<CrimsonCultistEntity, TC4CrimsonCultistModel> {
        private final TC4CrimsonCultistModel robe;
        private final TC4CrimsonCultistModel knight;
        private final TC4CrimsonCultistModel leader;

        private CultistArmorLayer(TC4CrimsonCultistRenderer parent, EntityRendererProvider.Context context) {
            super(parent);
            robe = new TC4CrimsonCultistModel(context.bakeLayer(TC4CrimsonCultistModel.ROBE));
            knight = new TC4CrimsonCultistModel(context.bakeLayer(TC4CrimsonCultistModel.KNIGHT));
            leader = new TC4CrimsonCultistModel(context.bakeLayer(TC4CrimsonCultistModel.LEADER));
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, CrimsonCultistEntity entity,
                           float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                           float netHeadYaw, float headPitch) {
            TC4CrimsonCultistModel model;
            ResourceLocation texture;
            switch (entity.role()) {
                case CULTIST, CLERIC -> { model = robe; texture = ROBE; }
                case KNIGHT -> { model = knight; texture = KNIGHT; }
                case LEADER -> { model = leader; texture = LEADER; }
                default -> { return; }
            }
            getParentModel().copyPropertiesTo(model);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
            model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
