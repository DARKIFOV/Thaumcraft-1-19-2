package com.darkifov.thaumcraft.client.render.model;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.darkifov.thaumcraft.golem.GolemCoreType;
import com.darkifov.thaumcraft.golem.GolemUpgradeType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Exact modern ModelPart translation of TC4 4.2.3.5 ModelGolem.
 * The original 128x128 UV layout and all six body-part cuboids are preserved.
 */
public final class TC4ThaumGolemModel extends HierarchicalModel<ThaumGolemEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "thaum_golem"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public TC4ThaumGolemModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        float offset = 30.0F;

        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -11.0F, -5.5F, 8.0F, 9.0F, 8.0F),
                PartPose.offset(0.0F, offset, -2.0F));

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 40).addBox(-8.0F, -2.0F, -6.0F, 16.0F, 12.0F, 11.0F)
                        .texOffs(0, 70).addBox(-4.5F, 10.0F, -3.0F, 9.0F, 5.0F, 6.0F, new net.minecraft.client.model.geom.builders.CubeDeformation(0.5F)),
                PartPose.offset(0.0F, offset, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(60, 21)
                        .addBox(-12.0F, -2.5F, -3.0F, 4.0F, 25.0F, 6.0F),
                PartPose.offset(0.0F, offset, 0.0F));

        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(60, 21).mirror()
                        .addBox(8.0F, -2.5F, -3.0F, 4.0F, 25.0F, 6.0F),
                PartPose.offset(0.0F, offset, 0.0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(37, 0)
                        .addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F),
                PartPose.offset(-4.0F, 18.0F + offset, 0.0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(37, 0).mirror()
                        .addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F),
                PartPose.offset(5.0F, 18.0F + offset, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(ThaumGolemEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        boolean inactive = entity.getCoreType() == GolemCoreType.BLANK || entity.isWaiting();
        head.yRot = inactive ? 0.0F : netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = inactive ? 0.57595867F : headPitch * Mth.DEG_TO_RAD;
        head.zRot = 0.0F;

        float wave = triangleWave(limbSwing, 13.0F);
        rightLeg.xRot = -1.5F * wave * limbSwingAmount;
        leftLeg.xRot = 1.5F * wave * limbSwingAmount;
        rightLeg.yRot = leftLeg.yRot = 0.0F;
        rightLeg.zRot = leftLeg.zRot = 0.0F;
        rightArm.yRot = leftArm.yRot = 0.0F;
        rightArm.zRot = leftArm.zRot = 0.0F;

        if (!entity.getCarriedForDisplay().isEmpty()) {
            rightArm.xRot = -1.0F;
            leftArm.xRot = -1.0F;
        } else {
            rightArm.xRot = (-0.2F + 1.5F * wave) * limbSwingAmount;
            leftArm.xRot = (-0.2F - 1.5F * wave) * limbSwingAmount;
        }

        if (entity.getCoreType() == GolemCoreType.ESSENTIA) {
            int carry = entity.getGolemMaterial().carryLimit(entity.getUpgradeAmount(GolemUpgradeType.EARTH));
            float twist = (1.0F - (0.5F + Math.min(64, carry) / 128.0F)) * 25.0F;
            leftArm.zRot = twist * Mth.DEG_TO_RAD;
            rightArm.zRot = -twist * Mth.DEG_TO_RAD;
        }
    }

    private static float triangleWave(float value, float period) {
        return (Math.abs(value % period - period * 0.5F) - period * 0.25F) / (period * 0.25F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
