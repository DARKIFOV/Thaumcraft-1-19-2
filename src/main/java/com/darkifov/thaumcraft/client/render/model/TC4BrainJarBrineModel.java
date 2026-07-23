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

/** Pixel/UV exact port of ModelJar.Brine (64x32). */
public final class TC4BrainJarBrineModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "brain_jar_brine"), "main");

    private final ModelPart brine;

    public TC4BrainJarBrineModel(ModelPart root) {
        this.brine = root.getChild("brine");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("brine",
                CubeListBuilder.create().mirror().texOffs(0, 0)
                        .addBox(-4.0F, -11.0F, -4.0F, 8.0F, 10.0F, 8.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        brine.render(poseStack, consumer, light, overlay);
    }
}
