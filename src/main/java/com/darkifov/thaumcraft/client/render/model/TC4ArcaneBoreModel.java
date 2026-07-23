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

/** Exact cuboid/UV dimensions from TC4 ModelBore, ModelBoreEmit and ModelBoreBase. */
public final class TC4ArcaneBoreModel {
    public static final ModelLayerLocation BORE_LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "arcane_bore"), "main");
    public static final ModelLayerLocation BASE_LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "arcane_bore_base"), "main");
    private final ModelPart root;

    public TC4ArcaneBoreModel(ModelPart root) { this.root = root; }

    public static LayerDefinition createBoreLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 32)
                .addBox(-6, 0, -6, 12, 2, 12), PartPose.offset(0, 14, 0));
        r.addOrReplaceChild("side1", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-2, 2, -5.5F, 4, 8, 1), PartPose.offset(0, 6, 0));
        r.addOrReplaceChild("side2", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-2, 2, 4.5F, 4, 8, 1), PartPose.offset(0, 6, 0));
        r.addOrReplaceChild("crossbar", CubeListBuilder.create().texOffs(0, 48)
                .addBox(-1, -1, -6, 2, 2, 12), PartPose.offset(0, 8, 0));
        r.addOrReplaceChild("front", CubeListBuilder.create().texOffs(30, 14)
                .addBox(4, -2.5F, -2.5F, 4, 5, 5), PartPose.offset(0, 8, 0));
        r.addOrReplaceChild("middle", CubeListBuilder.create().texOffs(0, 14)
                .addBox(-2, -4, -4, 6, 8, 8), PartPose.offset(0, 8, 0));
        r.addOrReplaceChild("emitter_rod", CubeListBuilder.create().texOffs(56, 0)
                .addBox(-1, 1, -1, 2, 11, 2), PartPose.offset(0, 1, 0));
        r.addOrReplaceChild("emitter_knob", CubeListBuilder.create().texOffs(66, 0)
                .addBox(-2, 12, -2, 4, 4, 4), PartPose.ZERO);
        r.addOrReplaceChild("emitter_cross1", CubeListBuilder.create().texOffs(56, 16)
                .addBox(-2, 0, -2, 4, 1, 4), PartPose.offset(0, 8, 0));
        r.addOrReplaceChild("emitter_cross2", CubeListBuilder.create().texOffs(56, 24)
                .addBox(-3, 4, -3, 6, 1, 6), PartPose.ZERO);
        r.addOrReplaceChild("emitter_cross3", CubeListBuilder.create().texOffs(56, 16)
                .addBox(-2, 0, -2, 4, 1, 4), PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 64);
    }

    public static LayerDefinition createBaseLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("base1", CubeListBuilder.create().texOffs(64, 24)
                .addBox(-8, 0, -8, 16, 2, 16), PartPose.offset(0, 0, 0));
        r.addOrReplaceChild("base2", CubeListBuilder.create().texOffs(64, 24)
                .addBox(-8, 0, -8, 16, 2, 16), PartPose.offset(0, 14, 0));
        r.addOrReplaceChild("mid", CubeListBuilder.create().texOffs(84, 42)
                .addBox(-2.5F, 0, -2.5F, 5, 12, 5), PartPose.offset(0, 2, 0));
        for (int i = 0; i < 4; i++) {
            int x = i == 0 || i == 1 ? -5 : 5;
            int z = i == 0 || i == 3 ? -5 : 5;
            r.addOrReplaceChild("pillar" + i, CubeListBuilder.create().texOffs(64, 42)
                    .addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(x, 2, z));
        }
        r.addOrReplaceChild("nozzle1", CubeListBuilder.create().texOffs(106, 42)
                .addBox(2.5F, -2, -2, 5, 4, 4), PartPose.offset(0, 8, 0));
        r.addOrReplaceChild("nozzle2", CubeListBuilder.create().texOffs(106, 51)
                .addBox(7, -2.5F, -2.5F, 1, 5, 5), PartPose.offset(0, 8, 0));
        return LayerDefinition.create(mesh, 128, 64);
    }

    public void render(PoseStack pose, VertexConsumer consumer, int light, int overlay) {
        root.render(pose, consumer, light, overlay);
    }

    public void renderBoreMount(PoseStack pose, VertexConsumer consumer, int light, int overlay) {
        root.getChild("base").render(pose, consumer, light, overlay);
        root.getChild("side1").render(pose, consumer, light, overlay);
        root.getChild("side2").render(pose, consumer, light, overlay);
        root.getChild("crossbar").render(pose, consumer, light, overlay);
    }

    public void renderBoreNozzle(PoseStack pose, VertexConsumer consumer, int light, int overlay) {
        root.getChild("front").render(pose, consumer, light, overlay);
        root.getChild("middle").render(pose, consumer, light, overlay);
    }

    public void renderEmitter(PoseStack pose, VertexConsumer consumer, int light, int overlay, boolean focus) {
        if (focus) root.getChild("emitter_knob").render(pose, consumer, light, overlay);
        root.getChild("emitter_cross1").render(pose, consumer, light, overlay);
        root.getChild("emitter_cross2").render(pose, consumer, light, overlay);
        root.getChild("emitter_cross3").render(pose, consumer, light, overlay);
        root.getChild("emitter_rod").render(pose, consumer, light, overlay);
    }

    public void renderBaseBody(PoseStack pose, VertexConsumer consumer, int light, int overlay) {
        root.getChild("base1").render(pose, consumer, light, overlay);
        root.getChild("base2").render(pose, consumer, light, overlay);
        root.getChild("mid").render(pose, consumer, light, overlay);
        for (int i = 0; i < 4; i++) root.getChild("pillar" + i).render(pose, consumer, light, overlay);
    }

    /** Exact ModelBoreBase nozzle pair reused by the original Arcane Lamp TESR. */
    public void renderNozzle(PoseStack pose, VertexConsumer consumer, int light, int overlay) {
        root.getChild("nozzle1").render(pose, consumer, light, overlay);
        root.getChild("nozzle2").render(pose, consumer, light, overlay);
    }
}
