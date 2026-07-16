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

/** Exact six-part ModelCentrifuge geometry and 64x32 UV layout from TC4. */
public final class TC4CentrifugeModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "alchemical_centrifuge"), "main");

    private final ModelPart crossbar;
    private final ModelPart dingus1;
    private final ModelPart dingus2;
    private final ModelPart core;
    private final ModelPart top;
    private final ModelPart bottom;

    public TC4CentrifugeModel(ModelPart root) {
        crossbar = root.getChild("crossbar");
        dingus1 = root.getChild("dingus1");
        dingus2 = root.getChild("dingus2");
        core = root.getChild("core");
        top = root.getChild("top");
        bottom = root.getChild("bottom");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("crossbar",
                CubeListBuilder.create().mirror().texOffs(16, 0)
                        .addBox(-4.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F), PartPose.ZERO);
        root.addOrReplaceChild("dingus1",
                CubeListBuilder.create().mirror().texOffs(0, 16)
                        .addBox(4.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F), PartPose.ZERO);
        root.addOrReplaceChild("dingus2",
                CubeListBuilder.create().mirror().texOffs(0, 16)
                        .addBox(-8.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F), PartPose.ZERO);
        root.addOrReplaceChild("core",
                CubeListBuilder.create().mirror().texOffs(0, 0)
                        .addBox(-1.5F, -4.0F, -1.5F, 3.0F, 8.0F, 3.0F), PartPose.ZERO);
        root.addOrReplaceChild("top",
                CubeListBuilder.create().mirror().texOffs(20, 16)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 4.0F, 8.0F), PartPose.ZERO);
        root.addOrReplaceChild("bottom",
                CubeListBuilder.create().mirror().texOffs(20, 16)
                        .addBox(-4.0F, 4.0F, -4.0F, 8.0F, 4.0F, 8.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public void renderBoxes(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        top.render(poseStack, consumer, light, overlay);
        bottom.render(poseStack, consumer, light, overlay);
    }

    public void renderSpinnyBit(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        crossbar.render(poseStack, consumer, light, overlay);
        dingus1.render(poseStack, consumer, light, overlay);
        dingus2.render(poseStack, consumer, light, overlay);
        core.render(poseStack, consumer, light, overlay);
    }
}
