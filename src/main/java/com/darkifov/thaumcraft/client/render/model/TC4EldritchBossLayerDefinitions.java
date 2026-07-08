package com.darkifov.thaumcraft.client.render.model;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Stage219 1.19.2 ModelPart/LayerDefinition port anchor for TC4
 * ModelEldritchGuardian and ModelEldritchGolem.
 *
 * <p>The field names and cube dimensions are copied from the original 1.7.10
 * constructors for the high-impact visible parts.  They are registered with the
 * client layer event so later stages can move the renderers from the current
 * low-level VertexConsumer bridge to baked ModelPart rendering without changing
 * the layer contract.</p>
 */
// Stage219 marker: ModelEldritchGuardian -> LayerDefinition, ModelEldritchGolem -> LayerDefinition, texture 128x64
public final class TC4EldritchBossLayerDefinitions {
    public static final ModelLayerLocation ELDRITCH_GUARDIAN = new ModelLayerLocation(new ResourceLocation(ThaumcraftMod.MOD_ID, "eldritch_guardian"), "main");
    public static final ModelLayerLocation ELDRITCH_WARDEN = new ModelLayerLocation(new ResourceLocation(ThaumcraftMod.MOD_ID, "eldritch_warden"), "main");
    public static final ModelLayerLocation ELDRITCH_GOLEM = new ModelLayerLocation(new ResourceLocation(ThaumcraftMod.MOD_ID, "eldritch_golem"), "main");
    public static final ModelLayerLocation ELDRITCH_CRAB = new ModelLayerLocation(new ResourceLocation(ThaumcraftMod.MOD_ID, "eldritch_crab"), "main");

    private TC4EldritchBossLayerDefinitions() {
    }

    public static LayerDefinition createGuardianBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("HoodEye", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -14.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        root.addOrReplaceChild("Hood1", CubeListBuilder.create().texOffs(40, 12).addBox(-4.0F, -14.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        root.addOrReplaceChild("Hood2", CubeListBuilder.create().texOffs(36, 28).addBox(-3.5F, -14.7F, 2.0F, 7.0F, 7.0F, 3.0F), PartPose.rotation(-0.2268928F, 0.0F, 0.0F));
        root.addOrReplaceChild("Hood3", CubeListBuilder.create().texOffs(22, 19).addBox(-3.0F, -15.0F, 2.5F, 6.0F, 6.0F, 3.0F), PartPose.rotation(-0.3490659F, 0.0F, 0.0F));
        root.addOrReplaceChild("Hood4", CubeListBuilder.create().texOffs(40, 4).addBox(-2.5F, -15.7F, 3.5F, 5.0F, 5.0F, 3.0F), PartPose.rotation(-0.5759587F, 0.0F, 0.0F));
        root.addOrReplaceChild("Chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 7.0F, 2.0F), PartPose.ZERO);
        root.addOrReplaceChild("Backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, -5.0F, 2.0F, 8.0F, 11.0F, 2.0F), PartPose.ZERO);
        root.addOrReplaceChild("BeltR", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, -2.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
        root.addOrReplaceChild("BeltL", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, -2.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
        root.addOrReplaceChild("Mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 2.0F, -3.0F, 8.0F, 4.0F, 1.0F), PartPose.ZERO);
        root.addOrReplaceChild("ShoulderR", CubeListBuilder.create().texOffs(56, 35).addBox(-8.5F, -6.5F, -2.5F, 5.0F, 5.0F, 5.0F), PartPose.rotation(-0.3665191F, 0.122173F, 0.0349066F));
        root.addOrReplaceChild("ShoulderL", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(3.5F, -6.5F, -2.5F, 5.0F, 5.0F, 5.0F), PartPose.rotation(-0.3665191F, -0.122173F, -0.0349066F));
        root.addOrReplaceChild("ArmR1", CubeListBuilder.create().texOffs(72, 8).addBox(-8.0F, -1.5F, -1.5F, 4.0F, 10.0F, 5.0F), PartPose.rotation(-0.9599311F, 0.1047198F, 0.1919862F));
        root.addOrReplaceChild("ArmL1", CubeListBuilder.create().texOffs(72, 8).mirror().addBox(4.0F, -1.5F, -1.5F, 4.0F, 10.0F, 5.0F), PartPose.rotation(-0.9599311F, -0.1047198F, -0.1919862F));
        root.addOrReplaceChild("LegpanelC1", CubeListBuilder.create().texOffs(96, 0).addBox(-3.0F, 5.0F, -3.0F, 6.0F, 5.0F, 1.0F), PartPose.ZERO);
        root.addOrReplaceChild("LegpanelC2", CubeListBuilder.create().texOffs(96, 0).addBox(-3.0F, 9.0F, -3.0F, 6.0F, 5.0F, 1.0F), PartPose.ZERO);
        root.addOrReplaceChild("LegpanelC3", CubeListBuilder.create().texOffs(96, 0).addBox(-3.0F, 13.0F, -3.0F, 6.0F, 5.0F, 1.0F), PartPose.ZERO);
        // Stage273-282: complete another visible ring from TC4 ModelEldritchGuardian.
        root.addOrReplaceChild("MbeltL", CubeListBuilder.create().texOffs(76, 44).mirror().addBox(4.0F, 2.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
        root.addOrReplaceChild("MbeltR", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 2.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO);
        root.addOrReplaceChild("ArmR2", CubeListBuilder.create().texOffs(72, 23).addBox(-8.0F, 8.0F, -2.0F, 4.0F, 9.0F, 4.0F), PartPose.rotation(-0.2094395F, 0.0F, 0.122173F));
        root.addOrReplaceChild("ArmL2", CubeListBuilder.create().texOffs(72, 23).mirror().addBox(4.0F, 8.0F, -2.0F, 4.0F, 9.0F, 4.0F), PartPose.rotation(-0.2094395F, 0.0F, -0.122173F));
        root.addOrReplaceChild("SidepanelR1", CubeListBuilder.create().texOffs(110, 0).addBox(-5.0F, 5.0F, -2.5F, 2.0F, 7.0F, 1.0F), PartPose.rotation(0.0F, 0.0F, 0.0872665F));
        root.addOrReplaceChild("SidepanelL1", CubeListBuilder.create().texOffs(110, 0).mirror().addBox(3.0F, 5.0F, -2.5F, 2.0F, 7.0F, 1.0F), PartPose.rotation(0.0F, 0.0F, -0.0872665F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    public static LayerDefinition createGolemBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("Torso", CubeListBuilder.create().texOffs(34, 45).addBox(-5.0F, 2.5F, -5.5F, 10.0F, 10.0F, 6.0F), PartPose.rotation(0.1745329F, 0.0F, 0.0F));
        root.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(47, 12).addBox(-3.5F, -1.5F, -6.3F, 7.0F, 7.0F, 5.0F), PartPose.rotation(-0.1047198F, 0.0F, 0.0F));
        root.addOrReplaceChild("Head2", CubeListBuilder.create().texOffs(26, 16).addBox(-2.0F, -2.0F, -7.0F, 4.0F, 4.0F, 4.0F), PartPose.rotation(-0.1047198F, 0.0F, 0.0F));
        root.addOrReplaceChild("CollarL", CubeListBuilder.create().texOffs(75, 50).addBox(3.5F, -0.5F, -9.5F, 1.0F, 4.0F, 10.0F), PartPose.rotation(0.837758F, 0.0F, 0.0F));
        root.addOrReplaceChild("CollarR", CubeListBuilder.create().texOffs(67, 50).addBox(-4.5F, -0.5F, -9.5F, 1.0F, 4.0F, 10.0F), PartPose.rotation(0.837758F, 0.0F, 0.0F));
        root.addOrReplaceChild("ArmR", CubeListBuilder.create().texOffs(78, 32).addBox(-8.5F, 4.5F, -4.0F, 4.0F, 13.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, 0.1047198F));
        root.addOrReplaceChild("ArmL", CubeListBuilder.create().texOffs(78, 32).mirror().addBox(4.5F, 4.5F, -4.0F, 4.0F, 13.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, -0.1047198F));
        root.addOrReplaceChild("LegR", CubeListBuilder.create().texOffs(79, 19).addBox(-4.5F, 15.0F, -2.0F, 4.0F, 9.0F, 4.0F), PartPose.ZERO);
        root.addOrReplaceChild("LegL", CubeListBuilder.create().texOffs(79, 19).mirror().addBox(0.5F, 15.0F, -2.0F, 4.0F, 9.0F, 4.0F), PartPose.ZERO);
        root.addOrReplaceChild("Frontcloth0", CubeListBuilder.create().texOffs(114, 52).addBox(-3.0F, 3.2F, -6.0F, 6.0F, 10.0F, 1.0F), PartPose.rotation(0.1745329F, 0.0F, 0.0F));
        root.addOrReplaceChild("HeadlessVent", CubeListBuilder.create().texOffs(22, 0).addBox(-3.5F, 0.0F, -8.5F, 7.0F, 1.0F, 8.0F), PartPose.rotation(0.837758F, 0.0F, 0.0F));
        // Stage273-282: add TC4 cloak/collar/waist pieces that were still missing from the baked tree.
        root.addOrReplaceChild("Cloak1", CubeListBuilder.create().texOffs(0, 47).addBox(-5.0F, 1.5F, 1.5F, 10.0F, 12.0F, 1.0F), PartPose.rotation(0.1396263F, 0.0F, 0.0F));
        root.addOrReplaceChild("Cloak2", CubeListBuilder.create().texOffs(0, 59).addBox(-5.0F, 13.5F, -0.8F, 10.0F, 4.0F, 1.0F), PartPose.rotation(0.3069452F, 0.0F, 0.0F));
        root.addOrReplaceChild("Cloak3", CubeListBuilder.create().texOffs(0, 37).addBox(-5.0F, 17.5F, -3.3F, 10.0F, 4.0F, 1.0F), PartPose.rotation(0.4465716F, 0.0F, 0.0F));
        root.addOrReplaceChild("CollarF", CubeListBuilder.create().texOffs(77, 59).addBox(-3.5F, -0.5F, -9.5F, 7.0F, 4.0F, 1.0F), PartPose.rotation(0.837758F, 0.0F, 0.0F));
        root.addOrReplaceChild("CollarB", CubeListBuilder.create().texOffs(77, 59).addBox(-3.5F, -0.5F, -0.5F, 7.0F, 4.0F, 1.0F), PartPose.rotation(0.837758F, 0.0F, 0.0F));
        root.addOrReplaceChild("Frontcloth1", CubeListBuilder.create().texOffs(114, 39).addBox(-3.0F, 13.5F, -5.0F, 6.0F, 6.0F, 1.0F), PartPose.rotation(-0.1047198F, 0.0F, 0.0F));
        root.addOrReplaceChild("Frontcloth2", CubeListBuilder.create().texOffs(114, 47).addBox(-3.0F, 19.5F, -3.0F, 6.0F, 3.0F, 1.0F), PartPose.rotation(-0.3316126F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    public static LayerDefinition createCrabBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Stage220: original ModelEldritchCrab names/atlas; TailHelm/TailBare mirror `hasHelm()`.
        root.addOrReplaceChild("TailHelm", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -4.5F, -0.4F, 9.0F, 9.0F, 9.0F), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.1047198F, 0.0F, 0.0F));
        root.addOrReplaceChild("TailBare", CubeListBuilder.create().texOffs(64, 0).addBox(-4.0F, -4.0F, -0.4F, 8.0F, 8.0F, 8.0F), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.1047198F, 0.0F, 0.0F));
        root.addOrReplaceChild("Torso", CubeListBuilder.create().texOffs(0, 18).addBox(-3.5F, -3.5F, -6.066667F, 7.0F, 7.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.0523599F, 0.0F, 0.0F));
        root.addOrReplaceChild("Head0", CubeListBuilder.create().texOffs(0, 31).addBox(-2.5F, -2.0F, -8.066667F, 5.0F, 5.0F, 2.0F), PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("Head1", CubeListBuilder.create().texOffs(0, 38).addBox(-2.0F, -1.5F, -9.066667F, 4.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("RArm", CubeListBuilder.create().texOffs(44, 4).addBox(-1.0F, -1.0F, -5.066667F, 2.0F, 2.0F, 6.0F), PartPose.offsetAndRotation(-3.0F, 17.0F, -4.0F, 0.0F, 0.7504916F, 0.0F));
        root.addOrReplaceChild("LArm", CubeListBuilder.create().texOffs(44, 4).mirror().addBox(-1.0F, -1.0F, -4.066667F, 2.0F, 2.0F, 6.0F), PartPose.offsetAndRotation(4.0F, 17.0F, -5.0F, 0.0F, -0.7504916F, 0.0F));
        root.addOrReplaceChild("RClaw0", CubeListBuilder.create().texOffs(0, 55).addBox(-2.0F, -2.5F, -3.066667F, 4.0F, 5.0F, 3.0F), PartPose.offset(-6.0F, 17.0F, -7.0F));
        root.addOrReplaceChild("RClaw1", CubeListBuilder.create().texOffs(0, 47).addBox(-2.0F, -1.0F, -5.066667F, 4.0F, 3.0F, 5.0F), PartPose.offset(-6.0F, 15.5F, -10.0F));
        root.addOrReplaceChild("RClaw2", CubeListBuilder.create().texOffs(14, 54).addBox(-1.5F, -1.0F, -4.066667F, 3.0F, 2.0F, 5.0F), PartPose.offsetAndRotation(-6.0F, 18.5F, -10.0F, 0.3141593F, 0.0F, 0.0F));
        root.addOrReplaceChild("LClaw0", CubeListBuilder.create().texOffs(0, 55).mirror().addBox(-2.0F, -2.5F, -3.066667F, 4.0F, 5.0F, 3.0F), PartPose.offset(6.0F, 17.0F, -7.0F));
        root.addOrReplaceChild("LClaw1", CubeListBuilder.create().texOffs(0, 47).mirror().addBox(-2.0F, -1.0F, -5.066667F, 4.0F, 3.0F, 5.0F), PartPose.offset(6.0F, 15.5F, -10.0F));
        root.addOrReplaceChild("LClaw2", CubeListBuilder.create().texOffs(14, 54).mirror().addBox(-1.5F, -1.0F, -4.066667F, 3.0F, 2.0F, 5.0F), PartPose.offsetAndRotation(6.0F, 18.5F, -10.0F, 0.3141593F, 0.0F, 0.0F));
        root.addOrReplaceChild("RRLeg0", CubeListBuilder.create().texOffs(36, 0).addBox(-4.5F, -1.0F, -0.9F, 6.0F, 2.0F, 2.0F), PartPose.offset(-4.0F, 20.0F, -1.5F));
        root.addOrReplaceChild("RRLeg1", CubeListBuilder.create().texOffs(36, 4).addBox(-4.5F, 1.0F, -0.9F, 2.0F, 5.0F, 2.0F), PartPose.offset(-4.0F, 20.0F, -1.5F));
        root.addOrReplaceChild("RFLeg0", CubeListBuilder.create().texOffs(36, 0).addBox(-5.0F, -1.0F, -1.066667F, 6.0F, 2.0F, 2.0F), PartPose.offset(-4.0F, 20.0F, -3.5F));
        root.addOrReplaceChild("RFLeg1", CubeListBuilder.create().texOffs(36, 4).addBox(-5.0F, 1.0F, -1.066667F, 2.0F, 5.0F, 2.0F), PartPose.offset(-4.0F, 20.0F, -3.5F));
        root.addOrReplaceChild("LRLeg0", CubeListBuilder.create().texOffs(36, 0).mirror().addBox(-1.5F, -1.0F, -0.9F, 6.0F, 2.0F, 2.0F), PartPose.offset(4.0F, 20.0F, -1.5F));
        root.addOrReplaceChild("LRLeg1", CubeListBuilder.create().texOffs(36, 4).mirror().addBox(2.5F, 1.0F, -0.9F, 2.0F, 5.0F, 2.0F), PartPose.offset(4.0F, 20.0F, -1.5F));
        root.addOrReplaceChild("LFLeg0", CubeListBuilder.create().texOffs(36, 0).mirror().addBox(-1.0F, -1.0F, -1.066667F, 6.0F, 2.0F, 2.0F), PartPose.offset(4.0F, 20.0F, -3.5F));
        root.addOrReplaceChild("LFLeg1", CubeListBuilder.create().texOffs(36, 4).mirror().addBox(3.0F, 1.0F, -1.066667F, 2.0F, 5.0F, 2.0F), PartPose.offset(4.0F, 20.0F, -3.5F));
        return LayerDefinition.create(mesh, 128, 64);
    }

}
