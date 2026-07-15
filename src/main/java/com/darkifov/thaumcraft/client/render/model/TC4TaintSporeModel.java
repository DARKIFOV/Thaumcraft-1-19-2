package com.darkifov.thaumcraft.client.render.model;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.TaintSporeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Original two-cube 64x64 ModelTaintSpore geometry. */
public final class TC4TaintSporeModel extends EntityModel<TaintSporeEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "taint_spore"), "main");
    private final ModelPart cube;

    public TC4TaintSporeModel(ModelPart root) { this.cube = root.getChild("cube"); }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cube", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.0F, 2.0F, -6.0F, 12.0F, 12.0F, 12.0F)
                        .texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override public void setupAnim(TaintSporeEntity entity, float limbSwing, float limbSwingAmount,
                                     float ageInTicks, float netHeadYaw, float headPitch) {
        float intensity = entity.hurtTime > 0 ? 0.04F : 0.02F;
        cube.xRot = intensity * Mth.sin(ageInTicks * 0.05F);
        cube.zRot = intensity * Mth.sin(ageInTicks * 0.10F);
    }

    @Override public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                                         float red, float green, float blue, float alpha) {
        cube.render(poseStack, consumer, light, overlay, red, green, blue, alpha);
    }
}
