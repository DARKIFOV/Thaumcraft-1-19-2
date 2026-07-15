package com.darkifov.thaumcraft.client.render.model;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.TravelingTrunkEntity;
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

/** TC4 ModelTrunk chest geometry plus animated feet for entity movement. */
public final class TC4TravelingTrunkModel extends HierarchicalModel<TravelingTrunkEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "traveling_trunk"), "main");
    private final ModelPart root;
    private final ModelPart lid;
    private final ModelPart frontLeft;
    private final ModelPart frontRight;
    private final ModelPart backLeft;
    private final ModelPart backRight;

    public TC4TravelingTrunkModel(ModelPart root) {
        this.root = root;
        lid = root.getChild("lid");
        frontLeft = root.getChild("front_left");
        frontRight = root.getChild("front_right");
        backLeft = root.getChild("back_left");
        backRight = root.getChild("back_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 19)
                .addBox(-7.0F, -5.0F, -7.0F, 14.0F, 10.0F, 14.0F), PartPose.offset(0.0F, 17.0F, 0.0F));
        PartDefinition lid = root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-7.0F, -5.0F, -14.0F, 14.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 12.0F, 7.0F));
        lid.addOrReplaceChild("knob", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 1.0F), PartPose.ZERO);
        CubeListBuilder leg = CubeListBuilder.create().texOffs(48, 43).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F);
        root.addOrReplaceChild("front_left", leg, PartPose.offset(4.5F, 21.0F, -4.5F));
        root.addOrReplaceChild("front_right", leg, PartPose.offset(-4.5F, 21.0F, -4.5F));
        root.addOrReplaceChild("back_left", leg, PartPose.offset(4.5F, 21.0F, 4.5F));
        root.addOrReplaceChild("back_right", leg, PartPose.offset(-4.5F, 21.0F, 4.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override public ModelPart root() { return root; }

    @Override
    public void setupAnim(TravelingTrunkEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float open = entity == null ? 0.0F : entity.getLid(ageInTicks - entity.tickCount);
        float eased = 1.0F - open;
        eased = 1.0F - eased * eased * eased;
        lid.xRot = -(eased * Mth.HALF_PI);
        float swing = Mth.cos(limbSwing * 0.6662F) * 1.1F * limbSwingAmount;
        frontLeft.xRot = backRight.xRot = swing;
        frontRight.xRot = backLeft.xRot = -swing;
    }

    public void setupItem() {
        lid.xRot = 0.0F;
        frontLeft.xRot = frontRight.xRot = backLeft.xRot = backRight.xRot = 0.0F;
    }
}
