package com.darkifov.thaumcraft.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/** Stage220 baked ModelPart bridge for TC4 Eldritch Guardian/Warden/Golem models. */
public class TC4BakedEldritchModel<T extends Entity> extends EntityModel<T> {
    protected final ModelPart root;
    public TC4BakedEldritchModel(ModelPart root) { this.root = root; }
    @Override public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}
    @Override public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) { root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha); }
    public ModelPart optional(String name) { try { return root.getChild(name); } catch (RuntimeException ignored) { return null; } }
    public void resetArmLift(float leftLift, float rightLift) {
        ModelPart armL = optional("ArmL1"); ModelPart armR = optional("ArmR1");
        if (armL != null) armL.xRot = -0.9599311F - leftLift * 0.46F;
        if (armR != null) armR.xRot = -0.9599311F - rightLift * 0.46F;
    }
    public void animateGolem(float limbSwing, float limbSwingAmount, boolean headless) {
        ModelPart head = optional("Head"); ModelPart head2 = optional("Head2"); ModelPart vent = optional("HeadlessVent");
        if (head != null) head.visible = !headless; if (head2 != null) head2.visible = !headless; if (vent != null) vent.visible = headless;
        float sway = Mth.cos(limbSwing * 0.6662F) * 0.18F * limbSwingAmount;
        ModelPart left = optional("ArmL"); ModelPart right = optional("ArmR");
        if (left != null) left.xRot = sway; if (right != null) right.xRot = -sway;
    }
}
