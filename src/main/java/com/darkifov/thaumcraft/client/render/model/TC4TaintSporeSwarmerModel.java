package com.darkifov.thaumcraft.client.render.model;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.TaintSporeSwarmerEntity;
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

/** Modern layer-model port of TC4's two-cube ModelTaintSporeSwarmer. */
public final class TC4TaintSporeSwarmerModel extends EntityModel<TaintSporeSwarmerEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "taint_spore_swarmer"), "main");

    private final ModelPart core;
    private final ModelPart shell;
    private float displaySize;
    private float ageInTicks;

    public TC4TaintSporeSwarmerModel(ModelPart root) {
        core = root.getChild("core");
        shell = root.getChild("shell");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("core", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("shell", CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(TaintSporeSwarmerEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        displaySize = entity.displaySize;
        this.ageInTicks = ageInTicks;
        float intensity = entity.hurtTime > 0 ? 0.04F : 0.02F;
        core.xRot = intensity * Mth.sin(ageInTicks * 0.05F);
        core.zRot = intensity * Mth.sin(ageInTicks * 0.10F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        shell.render(poseStack, consumer, light, overlay, red, green, blue, alpha);

        float visibleSize = Math.max(0.01F, displaySize);
        float pulse = 0.025F * Mth.sin(ageInTicks * 0.075F);
        float horizontal = Math.max(0.01F, 0.07F * visibleSize + pulse);
        float vertical = Math.max(0.01F, 0.07F * visibleSize - pulse);
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.6D, 0.0D);
        poseStack.scale(horizontal, vertical, horizontal);
        poseStack.translate(0.0D, -0.5D, 0.0D);
        core.render(poseStack, consumer, 0xF000F0, overlay, red, green, blue, alpha);
        poseStack.popPose();
    }
}
