package com.darkifov.thaumcraft.client.render.model;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Forge 1.19.2 port of vanilla 1.7.10 {@code ModelChest}, used verbatim by
 * TC4's {@code TileChestHungryRenderer}. The 64x64 cuboid unwrap is generated
 * by {@link CubeListBuilder}; hand-written UV quads caused the stretched and
 * mirrored texture visible in the regression screenshots.
 */
public final class TC4HungryChestModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "hungry_chest"), "main");

    private final ModelPart body;
    private final ModelPart lid;
    private final ModelPart knob;

    public TC4HungryChestModel(ModelPart root) {
        this.body = root.getChild("body");
        this.lid = root.getChild("lid");
        this.knob = root.getChild("knob");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ModelChest.chestBelow = texOffs(0,19).addBox(1,0,1,14,10,14)
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 19)
                        .addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F),
                PartPose.ZERO);

        // ModelChest.chestLid = texOffs(0,0).addBox(0,-5,-14,14,5,14)
        root.addOrReplaceChild("lid",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(0.0F, -5.0F, -14.0F, 14.0F, 5.0F, 14.0F),
                PartPose.offset(1.0F, 7.0F, 15.0F));

        // ModelChest.chestKnob = texOffs(0,0).addBox(-1,-2,-15,2,4,1)
        root.addOrReplaceChild("knob",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(8.0F, 7.0F, 15.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void setLidRotation(float xRotation) {
        lid.xRot = xRotation;
        knob.xRot = xRotation;
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        body.render(poseStack, consumer, light, overlay);
        lid.render(poseStack, consumer, light, overlay);
        knob.render(poseStack, consumer, light, overlay);
    }
}
