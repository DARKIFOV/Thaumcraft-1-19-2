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

/** Exact ModelBellows cuboids and per-part UV normalization from TC4 4.2.3.5. */
public final class TC4BellowsModel {
    public static final ModelLayerLocation FRAME_LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "bellows_frame"), "main");
    public static final ModelLayerLocation BAG_LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "bellows_bag"), "main");

    private final ModelPart bottomPlank;
    private final ModelPart middlePlank;
    private final ModelPart topPlank;
    private final ModelPart nozzle;
    private final ModelPart bag;

    public TC4BellowsModel(ModelPart frameRoot, ModelPart bagRoot) {
        bottomPlank = frameRoot.getChild("bottom_plank");
        middlePlank = frameRoot.getChild("middle_plank");
        topPlank = frameRoot.getChild("top_plank");
        nozzle = frameRoot.getChild("nozzle");
        bag = bagRoot.getChild("bag");
    }

    public static LayerDefinition createFrameLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bottom_plank",
                CubeListBuilder.create().mirror().texOffs(0, 0)
                        .addBox(-6.0F, 0.0F, -6.0F, 12.0F, 2.0F, 12.0F),
                PartPose.offset(0.0F, 22.0F, 0.0F));
        root.addOrReplaceChild("middle_plank",
                CubeListBuilder.create().mirror().texOffs(0, 0)
                        .addBox(-6.0F, -1.0F, -6.0F, 12.0F, 2.0F, 12.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("top_plank",
                CubeListBuilder.create().mirror().texOffs(0, 0)
                        .addBox(-6.0F, 0.0F, -6.0F, 12.0F, 2.0F, 12.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("nozzle",
                CubeListBuilder.create().mirror().texOffs(0, 36)
                        .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, 16.0F, 6.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    public static LayerDefinition createBagLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("bag",
                CubeListBuilder.create().mirror().texOffs(48, 0)
                        .addBox(-10.0F, -12.03333F, -10.0F, 20.0F, 24.0F, 20.0F),
                PartPose.offset(0.0F, 0.5F, 0.0F));
        // ModelBellows explicitly called bag.setTextureSize(64, 32), unlike the frame's 128x64.
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** Original TileBellowsRenderer transforms at a neutral preview inflation. */
    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, float inflation) {
        float bounded = inflation;
        float separation = 0.125F + bounded * 0.875F;

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);
        poseStack.pushPose();
        poseStack.scale(0.5F, (bounded + 0.1F) / 2.0F, 0.5F);
        bag.render(poseStack, consumer, light, overlay);
        poseStack.popPose();

        poseStack.translate(0.0D, -1.0D, 0.0D);
        poseStack.pushPose();
        poseStack.translate(0.0D, -separation / 2.0F + 0.5F, 0.0D);
        topPlank.render(poseStack, consumer, light, overlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, separation / 2.0F - 0.5F, 0.0D);
        bottomPlank.render(poseStack, consumer, light, overlay);
        poseStack.popPose();
        middlePlank.render(poseStack, consumer, light, overlay);
        nozzle.render(poseStack, consumer, light, overlay);
        poseStack.popPose();
    }
}
