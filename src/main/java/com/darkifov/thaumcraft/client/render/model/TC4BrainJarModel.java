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

/** Original TC4 ModelBrain geometry and 128x64 UV layout. */
public final class TC4BrainJarModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "brain_jar"), "main");

    private final ModelPart root;

    public TC4BrainJarModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("brain",
                CubeListBuilder.create().mirror().texOffs(0, 0)
                        .addBox(-6.0F, -5.0F, -8.0F, 12.0F, 10.0F, 16.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("base",
                CubeListBuilder.create().mirror().texOffs(64, 0)
                        .addBox(-4.0F, 5.0F, 0.0F, 8.0F, 3.0F, 7.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("stem",
                CubeListBuilder.create().mirror().texOffs(0, 32)
                        .addBox(-1.0F, 5.0F, -2.0F, 2.0F, 6.0F, 2.0F),
                PartPose.rotation(0.4089647F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        root.render(poseStack, consumer, light, overlay);
    }
}
