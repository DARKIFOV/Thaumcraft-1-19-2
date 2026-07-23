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

/** Modern baked equivalent of TC4 ModelManaPod's pod0 and pod2 cuboids. */
public final class TC4ManaPodModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "mana_pod"), "main");

    private final ModelPart pod0;
    private final ModelPart pod2;

    public TC4ManaPodModel(ModelPart root) {
        pod0 = root.getChild("pod0");
        pod2 = root.getChild("pod2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("pod0",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("pod2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-3.5F, 0.0F, -3.5F, 7.0F, 9.0F, 7.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    public void renderInner(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        pod0.render(poseStack, consumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void renderOuter(PoseStack poseStack, VertexConsumer consumer, int light, int overlay,
                            float red, float green, float blue, float alpha) {
        pod2.render(poseStack, consumer, light, overlay, red, green, blue, alpha);
    }
}
