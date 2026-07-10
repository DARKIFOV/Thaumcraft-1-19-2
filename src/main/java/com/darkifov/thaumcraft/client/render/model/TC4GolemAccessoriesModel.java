package com.darkifov.thaumcraft.client.render.model;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.darkifov.thaumcraft.golem.GolemCoreType;
import com.darkifov.thaumcraft.golem.GolemDecorationType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Exact 128x128 geometry translation of TC4 ModelGolemAccessories. */
public final class TC4GolemAccessoriesModel extends HierarchicalModel<ThaumGolemEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftMod.MOD_ID, "thaum_golem_accessories"), "main");

    private final ModelPart root;
    private final ModelPart fez;
    private final ModelPart glasses;
    private final ModelPart hat;
    private final ModelPart hatRim;
    private final ModelPart bowtie;
    private final ModelPart dartgun;
    private final ModelPart mace;
    private final ModelPart visor;
    private final ModelPart plate;
    private final ModelPart plateLeft;
    private final ModelPart plateRight;
    private final ModelPart headJar;
    private final ModelPart headBrain;
    private final ModelPart evilHead;

    public TC4GolemAccessoriesModel(ModelPart root) {
        this.root = root;
        this.fez = root.getChild("fez");
        this.glasses = root.getChild("glasses");
        this.hat = root.getChild("hat");
        this.hatRim = root.getChild("hat_rim");
        this.bowtie = root.getChild("bowtie");
        this.dartgun = root.getChild("dartgun");
        this.mace = root.getChild("mace");
        this.visor = root.getChild("visor");
        this.plate = root.getChild("plate");
        this.plateLeft = root.getChild("plate_left");
        this.plateRight = root.getChild("plate_right");
        this.headJar = root.getChild("head_jar");
        this.headBrain = root.getChild("head_brain");
        this.evilHead = root.getChild("evil_head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        float offset = 30.0F;

        root.addOrReplaceChild("fez", CubeListBuilder.create().texOffs(0, 94)
                .addBox(-4.5F, -15.0F, -6.0F, 9.0F, 7.0F, 9.0F), PartPose.offset(0, offset, -2));
        root.addOrReplaceChild("plate", CubeListBuilder.create().texOffs(32, 40)
                .addBox(-6.5F, -1.0F, -7.0F, 13.0F, 12.0F, 13.0F), PartPose.offset(0, offset, 0));
        root.addOrReplaceChild("plate_left", CubeListBuilder.create().texOffs(0, 44)
                .addBox(-8.5F, -4.0F, -6.5F, 3.0F, 6.0F, 12.0F), PartPose.offset(0, offset, 0));
        root.addOrReplaceChild("plate_right", CubeListBuilder.create().texOffs(0, 44).mirror()
                .addBox(5.5F, -4.0F, -6.5F, 3.0F, 6.0F, 12.0F), PartPose.offset(0, offset, 0));
        root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 110)
                .addBox(-4.5F, -17.0F, -6.0F, 9.0F, 9.0F, 9.0F), PartPose.offset(0, offset, -2));
        root.addOrReplaceChild("glasses", CubeListBuilder.create().texOffs(0, 80)
                .addBox(-4.5F, -8.0F, -6.0F, 9.0F, 4.0F, 9.0F), PartPose.offset(0, offset, -2));
        root.addOrReplaceChild("visor", CubeListBuilder.create().texOffs(0, 70)
                .addBox(-5.0F, -8.0F, -6.0F, 10.0F, 5.0F, 5.0F), PartPose.offset(0, offset, -2));
        root.addOrReplaceChild("hat_rim", CubeListBuilder.create().texOffs(36, 114)
                .addBox(-6.5F, -9.0F, -8.0F, 13.0F, 1.0F, 13.0F), PartPose.offset(0, offset, -2));
        root.addOrReplaceChild("dartgun", CubeListBuilder.create().texOffs(80, 80)
                .addBox(7.9F, 7.5F, -3.5F, 6.0F, 16.0F, 7.0F), PartPose.offset(0, offset, 0));
        root.addOrReplaceChild("mace", CubeListBuilder.create().texOffs(80, 26)
                .addBox(-13.0F, 15.0F, -5.0F, 6.0F, 8.0F, 10.0F), PartPose.offset(0, offset, 0));
        root.addOrReplaceChild("bowtie", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.5F, -2.0F, -6.5F, 17.0F, 4.0F, 12.0F), PartPose.offset(0, offset, 0));
        root.addOrReplaceChild("head_jar", CubeListBuilder.create().texOffs(96, 56)
                .addBox(-4.0F, -15.0F, -5.5F, 8.0F, 4.0F, 8.0F), PartPose.offset(0, offset, -2));
        root.addOrReplaceChild("head_brain", CubeListBuilder.create().texOffs(96, 70)
                .addBox(-3.5F, -14.0F, -5.0F, 7.0F, 3.0F, 7.0F), PartPose.offset(0, offset, -2));
        root.addOrReplaceChild("evil_head", CubeListBuilder.create().texOffs(64, 65)
                .addBox(-4.0F, -9.0F, -5.5F, 8.0F, 7.0F, 8.0F), PartPose.offset(0, offset, -2));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(ThaumGolemEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        for (ModelPart part : new ModelPart[]{fez, glasses, hat, hatRim, bowtie, dartgun, mace, visor,
                plate, plateLeft, plateRight, headJar, headBrain, evilHead}) {
            part.visible = false;
        }
        fez.visible = entity.hasDecoration(GolemDecorationType.FEZ);
        glasses.visible = entity.hasDecoration(GolemDecorationType.GLASSES);
        hat.visible = entity.hasDecoration(GolemDecorationType.TOP_HAT);
        hatRim.visible = hat.visible;
        bowtie.visible = entity.hasDecoration(GolemDecorationType.BOWTIE);
        dartgun.visible = entity.hasDecoration(GolemDecorationType.DART_LAUNCHER);
        mace.visible = entity.hasDecoration(GolemDecorationType.MACE);
        visor.visible = entity.hasDecoration(GolemDecorationType.VISOR);
        plate.visible = entity.hasDecoration(GolemDecorationType.ARMOR);
        plateLeft.visible = plate.visible;
        plateRight.visible = plate.visible;
        headJar.visible = entity.isAdvancedGolem();
        headBrain.visible = entity.isAdvancedGolem();
        evilHead.visible = entity.isAdvancedGolem() && entity.getCoreType() != GolemCoreType.BLANK;

        boolean inactive = entity.getCoreType() == GolemCoreType.BLANK || entity.isWaiting();
        float yaw = inactive ? 0.0F : netHeadYaw * Mth.DEG_TO_RAD;
        float pitch = inactive ? 0.57595867F : headPitch * Mth.DEG_TO_RAD;
        for (ModelPart part : new ModelPart[]{fez, glasses, hat, hatRim, visor, headJar, headBrain, evilHead}) {
            part.yRot = yaw;
            part.xRot = pitch;
        }
        float wave = triangleWave(limbSwing, 13.0F);
        if (!entity.getCarriedForDisplay().isEmpty()) {
            dartgun.xRot = -1.0F;
            mace.xRot = -1.0F;
        } else {
            dartgun.xRot = (-0.2F - 1.5F * wave) * limbSwingAmount;
            mace.xRot = (-0.2F + 1.5F * wave) * limbSwingAmount;
        }
    }

    private static float triangleWave(float value, float period) {
        return (Math.abs(value % period - period * 0.5F) - period * 0.25F) / (period * 0.25F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
