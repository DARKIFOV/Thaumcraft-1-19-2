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
 * Exact Forge 1.19.2 ModelPart port of TC4 ModelCube(0) and ModelCube(32).
 *
 * <p>The previous renderer mapped one quarter of infuser.png onto every cube
 * face. ModelRenderer actually uses the standard cuboid unwrap on a 64x64
 * atlas, with the base at V=0 and the active overlay at V=32.</p>
 */
public final class TC4InfusionMatrixModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "infusion_matrix_cube"), "main");

    private final ModelPart base;
    private final ModelPart overlay;

    public TC4InfusionMatrixModel(ModelPart root) {
        this.base = root.getChild("base");
        this.overlay = root.getChild("overlay");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("base",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("overlay",
                CubeListBuilder.create().texOffs(0, 32).mirror()
                        .addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    public void renderBase(PoseStack poseStack, VertexConsumer consumer, int light, int overlayLight) {
        base.render(poseStack, consumer, light, overlayLight, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void renderOverlay(PoseStack poseStack, VertexConsumer consumer, int light, int overlayLight,
                              float red, float green, float blue, float alpha) {
        overlay.render(poseStack, consumer, light, overlayLight, red, green, blue, alpha);
    }
}
