package com.darkifov.thaumcraft.client.render.model;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.CrimsonCultistEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Forge 1.19.2 model family for the four TC4 crimson cultist roles.
 *
 * <p>The previous port rendered unrelated blocks.  These layers restore a
 * humanoid base and role-specific robe/plate silhouettes using the original
 * TC4 texture sheets.  UV coordinates are doubled because the bundled armor
 * sheets are the 256x128 high-resolution copies of the original 128x64 maps.</p>
 */
public final class TC4CrimsonCultistModel extends HumanoidModel<CrimsonCultistEntity> {
    public enum Variant { BASE, ROBE, KNIGHT, LEADER }

    public static final ModelLayerLocation BASE = layer("crimson_cultist", "base");
    public static final ModelLayerLocation ROBE = layer("crimson_cultist", "robe");
    public static final ModelLayerLocation KNIGHT = layer("crimson_cultist", "knight");
    public static final ModelLayerLocation LEADER = layer("crimson_cultist", "leader");

    public TC4CrimsonCultistModel(ModelPart root) {
        super(root);
    }

    private static ModelLayerLocation layer(String path, String part) {
        return new ModelLayerLocation(new ResourceLocation(ThaumcraftMod.MOD_ID, path), part);
    }

    public static LayerDefinition createBaseLayer() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32);
    }

    public static LayerDefinition createRobeLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.35F), 0.0F);
        PartDefinition root = mesh.getRoot();
        root.getChild("head").addOrReplaceChild("hood",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.75F, -8.75F, -4.75F, 9.5F, 9.5F, 9.5F),
                PartPose.ZERO);
        root.getChild("body").addOrReplaceChild("robe_skirt",
                CubeListBuilder.create().texOffs(0, 64)
                        .addBox(-5.0F, 9.0F, -2.75F, 10.0F, 11.0F, 5.5F),
                PartPose.ZERO);
        root.getChild("right_arm").addOrReplaceChild("wide_sleeve",
                CubeListBuilder.create().texOffs(32, 64)
                        .addBox(-3.75F, 5.0F, -2.75F, 4.5F, 7.0F, 5.5F),
                PartPose.ZERO);
        root.getChild("left_arm").addOrReplaceChild("wide_sleeve",
                CubeListBuilder.create().texOffs(32, 64).mirror()
                        .addBox(-0.75F, 5.0F, -2.75F, 4.5F, 7.0F, 5.5F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 256, 128);
    }

    public static LayerDefinition createKnightLayer() {
        return createPlateLayer(false);
    }

    public static LayerDefinition createLeaderLayer() {
        return createPlateLayer(true);
    }

    private static LayerDefinition createPlateLayer(boolean leader) {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.45F), 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        head.addOrReplaceChild("helmet",
                CubeListBuilder.create().texOffs(82, 16)
                        .addBox(-4.5F, -9.0F, -4.5F, 9.0F, 5.0F, 9.0F)
                        .texOffs(42, 26).addBox(-5.5F, -4.0F, -4.5F, 1.0F, 6.0F, 9.0F)
                        .texOffs(42, 26).mirror().addBox(4.5F, -4.0F, -4.5F, 1.0F, 6.0F, 9.0F),
                PartPose.ZERO);
        if (leader) {
            head.addOrReplaceChild("crest",
                    CubeListBuilder.create().texOffs(136, 16)
                            .addBox(-1.0F, -12.0F, -1.0F, 2.0F, 5.0F, 7.0F),
                    PartPose.ZERO);
        }
        root.getChild("body").addOrReplaceChild("breastplate",
                CubeListBuilder.create().texOffs(72, 56)
                        .addBox(-5.0F, -0.5F, -3.0F, 10.0F, 12.5F, 6.0F)
                        .texOffs(112, 88).addBox(-5.25F, 7.5F, -3.25F, 10.5F, 4.0F, 6.5F),
                PartPose.ZERO);
        root.getChild("right_arm").addOrReplaceChild("pauldron",
                CubeListBuilder.create().texOffs(112, 70)
                        .addBox(-4.75F, -3.0F, -3.25F, leader ? 6.0F : 5.5F, 5.5F, 6.5F)
                        .texOffs(168, 52).addBox(-3.25F, 3.5F, -2.75F, 4.0F, 6.5F, 5.5F),
                PartPose.ZERO);
        root.getChild("left_arm").addOrReplaceChild("pauldron",
                CubeListBuilder.create().texOffs(112, 70).mirror()
                        .addBox(-1.25F, -3.0F, -3.25F, leader ? 6.0F : 5.5F, 5.5F, 6.5F)
                        .texOffs(168, 52).mirror().addBox(-0.75F, 3.5F, -2.75F, 4.0F, 6.5F, 5.5F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 256, 128);
    }
}
