package com.darkifov.thaumcraft.client.render.model;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Direct Forge ModelPart translation of the distinctive TC4 fortress silhouette.
 * The major helmet, mask, pauldron, gauntlet, breastplate, belt and articulated
 * leg-panel cuboids are kept instead of rendering a vanilla HumanoidModel.
 */
public final class TC4FortressArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "fortress_armor"), "main");

    private final ModelPart mask0;
    private final ModelPart mask1;
    private final ModelPart mask2;
    private final ModelPart goggles;
    private final ModelPart ornament;
    private final ModelPart scroll;
    private final ModelPart book;
    private final ModelPart shoulderTier2R;
    private final ModelPart shoulderTier3R;
    private final ModelPart shoulderTier2L;
    private final ModelPart shoulderTier3L;
    private final ModelPart legTier2R;
    private final ModelPart legTier3R;
    private final ModelPart legTier2L;
    private final ModelPart legTier3L;

    public TC4FortressArmorModel(ModelPart root) {
        super(root);
        mask0 = head.getChild("mask_0");
        mask1 = head.getChild("mask_1");
        mask2 = head.getChild("mask_2");
        goggles = head.getChild("goggles");
        ornament = head.getChild("ornament");
        scroll = body.getChild("scroll");
        book = body.getChild("book");
        shoulderTier2R = rightArm.getChild("shoulder_tier_2");
        shoulderTier3R = rightArm.getChild("shoulder_tier_3");
        shoulderTier2L = leftArm.getChild("shoulder_tier_2");
        shoulderTier3L = leftArm.getChild("shoulder_tier_3");
        legTier2R = rightLeg.getChild("panel_tier_2");
        legTier3R = rightLeg.getChild("panel_tier_3");
        legTier2L = leftLeg.getChild("panel_tier_2");
        legTier3L = leftLeg.getChild("panel_tier_3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(82, 16).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 4.0F, 9.0F)
                        .texOffs(42, 26).addBox(-6.5F, -3.0F, -4.5F, 1.0F, 5.0F, 9.0F)
                        .texOffs(42, 26).mirror().addBox(5.5F, -3.0F, -4.5F, 1.0F, 5.0F, 9.0F)
                        .texOffs(82, 42).addBox(-4.5F, -3.0F, 5.5F, 9.0F, 5.0F, 1.0F)
                        .texOffs(42, 0).addBox(-4.5F, -6.0F, -6.5F, 9.0F, 1.0F, 2.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("mask_0", CubeListBuilder.create().texOffs(104, 4)
                .addBox(-4.5F, -5.0F, -4.65F, 9.0F, 5.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("mask_1", CubeListBuilder.create().texOffs(152, 4)
                .addBox(-4.5F, -5.0F, -4.66F, 9.0F, 5.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("mask_2", CubeListBuilder.create().texOffs(200, 4)
                .addBox(-4.5F, -5.0F, -4.67F, 9.0F, 5.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("goggles", CubeListBuilder.create().texOffs(200, 36)
                .addBox(-4.5F, -5.0F, -4.3F, 9.0F, 5.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("ornament", CubeListBuilder.create()
                .texOffs(136, 22).addBox(-1.5F, -9.0F, -7.0F, 3.0F, 3.0F, 2.0F)
                .texOffs(144, 16).addBox(-1.0F, -8.5F, -7.5F, 2.0F, 2.0F, 1.0F)
                .texOffs(156, 16).addBox(-4.5F, -10.0F, -6.5F, 9.0F, 3.0F, 1.0F),
                PartPose.rotation(-0.1396263F, 0.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(112, 72).addBox(-5.0F, -1.0F, -3.0F, 10.0F, 13.0F, 6.0F)
                        .texOffs(152, 88).addBox(-5.0F, 4.0F, -3.0F, 10.0F, 7.0F, 6.0F)
                        .texOffs(112, 110).addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F),
                PartPose.ZERO);
        body.addOrReplaceChild("scroll", CubeListBuilder.create().texOffs(160, 104)
                .addBox(3.75F, 2.0F, 2.5F, 2.0F, 6.0F, 2.0F), PartPose.rotation(0.0F, 0.0F, -0.12F));
        body.addOrReplaceChild("book", CubeListBuilder.create().texOffs(176, 100)
                .addBox(-5.5F, 3.0F, 2.6F, 4.0F, 6.0F, 2.0F), PartPose.rotation(0.0F, 0.0F, 0.10F));

        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(112, 70).addBox(-4.5F, -2.5F, -3.0F, 5.0F, 5.0F, 6.0F)
                        .texOffs(228, 52).addBox(-3.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F)
                        .texOffs(168, 62).addBox(-3.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F)
                        .texOffs(168, 62).addBox(-3.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        rightArm.addOrReplaceChild("shoulder_tier_2", CubeListBuilder.create().texOffs(220, 74)
                .addBox(-5.5F, -2.5F, -3.5F, 2.0F, 5.0F, 7.0F), PartPose.rotation(0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulder_tier_3", CubeListBuilder.create().texOffs(188, 90)
                .addBox(-3.5F, 1.5F, -3.5F, 2.0F, 5.0F, 7.0F), PartPose.rotation(0.0F, 0.0F, 0.4363323F));

        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().mirror()
                        .texOffs(112, 70).addBox(-0.5F, -2.5F, -3.0F, 5.0F, 5.0F, 6.0F)
                        .texOffs(228, 52).addBox(1.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F)
                        .texOffs(168, 62).addBox(0.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F)
                        .texOffs(168, 62).addBox(0.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        leftArm.addOrReplaceChild("shoulder_tier_2", CubeListBuilder.create().mirror().texOffs(220, 74)
                .addBox(3.5F, -2.5F, -3.5F, 2.0F, 5.0F, 7.0F), PartPose.rotation(0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulder_tier_3", CubeListBuilder.create().mirror().texOffs(188, 90)
                .addBox(1.5F, 1.5F, -3.5F, 2.0F, 5.0F, 7.0F), PartPose.rotation(0.0F, 0.0F, -0.4363323F));

        PartDefinition rightLeg = root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 102)
                        .addBox(-3.0F, 0.5F, -3.5F, 5.0F, 4.0F, 1.0F)
                        .texOffs(0, 44).addBox(-2.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F)
                        .texOffs(0, 36).addBox(-3.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        rightLeg.addOrReplaceChild("panel_tier_2", CubeListBuilder.create().texOffs(16, 102)
                .addBox(-3.0F, 3.5F, -2.5F, 5.0F, 4.0F, 1.0F), PartPose.rotation(-0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("panel_tier_3", CubeListBuilder.create().texOffs(0, 112)
                .addBox(-3.0F, 6.5F, -1.5F, 5.0F, 3.0F, 1.0F), PartPose.rotation(-0.4363323F, 0.0F, 0.0F));

        PartDefinition leftLeg = root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().mirror().texOffs(0, 102)
                        .addBox(-2.0F, 0.5F, -3.5F, 5.0F, 4.0F, 1.0F)
                        .texOffs(0, 44).addBox(1.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F)
                        .texOffs(0, 36).addBox(-2.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        leftLeg.addOrReplaceChild("panel_tier_2", CubeListBuilder.create().mirror().texOffs(16, 102)
                .addBox(-2.0F, 3.5F, -2.5F, 5.0F, 4.0F, 1.0F), PartPose.rotation(-0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("panel_tier_3", CubeListBuilder.create().mirror().texOffs(0, 112)
                .addBox(-2.0F, 6.5F, -1.5F, 5.0F, 3.0F, 1.0F), PartPose.rotation(-0.4363323F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 256, 128);
    }

    public void configure(EquipmentSlot slot, int setPieces, int mask, boolean showGoggles) {
        setAllVisible(false);
        head.visible = slot == EquipmentSlot.HEAD;
        hat.visible = false;
        body.visible = slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS;
        rightArm.visible = slot == EquipmentSlot.CHEST;
        leftArm.visible = slot == EquipmentSlot.CHEST;
        rightLeg.visible = slot == EquipmentSlot.LEGS;
        leftLeg.visible = slot == EquipmentSlot.LEGS;

        mask0.visible = mask == 0;
        mask1.visible = mask == 1;
        mask2.visible = mask == 2;
        goggles.visible = showGoggles;
        ornament.visible = setPieces >= 3;
        scroll.visible = setPieces >= 3;
        book.visible = setPieces >= 2;
        shoulderTier2R.visible = shoulderTier2L.visible = setPieces >= 2;
        shoulderTier3R.visible = shoulderTier3L.visible = setPieces >= 3;
        legTier2R.visible = legTier2L.visible = setPieces >= 2;
        legTier3R.visible = legTier3L.visible = setPieces >= 3;
    }
}
