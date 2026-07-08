package com.darkifov.thaumcraft.client.render.model;

import com.darkifov.thaumcraft.entity.EldritchCrabEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Stage220 baked ModelPart bridge for original ModelEldritchCrab. */
public class TC4EldritchCrabModel extends TC4BakedEldritchModel<EldritchCrabEntity> {
    public TC4EldritchCrabModel(ModelPart root) { super(root); }
    @Override
    public void setupAnim(EldritchCrabEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        ModelPart tailHelm = optional("TailHelm"); ModelPart tailBare = optional("TailBare");
        if (tailHelm != null) tailHelm.visible = entity.hasHelm(); if (tailBare != null) tailBare.visible = !entity.hasHelm();
        setLeg("RRLeg1", 0.0F, 0.2094395F, 0.4363323F); setLeg("RRLeg0", 0.0F, 0.2094395F, 0.4363323F);
        setLeg("RFLeg1", 0.0F, -0.2094395F, 0.4363323F); setLeg("RFLeg0", 0.0F, -0.2094395F, 0.4363323F);
        setLeg("LRLeg1", 0.0F, -0.2094395F, -0.4363323F); setLeg("LRLeg0", 0.0F, -0.2094395F, -0.4363323F);
        setLeg("LFLeg1", 0.0F, 0.2094395F, -0.4363323F); setLeg("LFLeg0", 0.0F, 0.2094395F, -0.4363323F);
        float f9 = -(Mth.cos(limbSwing * 0.6662F * 2.0F) * 0.4F) * limbSwingAmount;
        float f10 = -(Mth.cos(limbSwing * 0.6662F * 2.0F + Mth.PI) * 0.4F) * limbSwingAmount;
        addLegYawRoll("RRLeg1", f9); addLegYawRoll("RRLeg0", f9); addLegYawRoll("LRLeg1", -f9); addLegYawRoll("LRLeg0", -f9);
        addLegYawRoll("RFLeg1", f10); addLegYawRoll("RFLeg0", f10); addLegYawRoll("LFLeg1", -f10); addLegYawRoll("LFLeg0", -f10);
        if (tailBare != null) { tailBare.yRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.125F; tailBare.zRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount * 0.125F; }
        if (tailHelm != null) { tailHelm.yRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.125F; tailHelm.zRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount * 0.125F; }
        ModelPart rClaw2 = optional("RClaw2"); ModelPart lClaw2 = optional("LClaw2"); ModelPart rClaw1 = optional("RClaw1"); ModelPart lClaw1 = optional("LClaw1");
        if (rClaw2 != null) rClaw2.xRot = 0.3141593F - Mth.sin(entity.tickCount / 4.0F) * 0.25F;
        if (lClaw2 != null) lClaw2.xRot = 0.3141593F + Mth.sin(entity.tickCount / 4.1F) * 0.25F;
        if (rClaw1 != null) rClaw1.xRot = Mth.sin(entity.tickCount / 4.0F) * 0.125F;
        if (lClaw1 != null) lClaw1.xRot = -Mth.sin(entity.tickCount / 4.1F) * 0.125F;
    }
    private void setLeg(String name, float x, float y, float z) { ModelPart part = optional(name); if (part != null) { part.xRot = x; part.yRot = y; part.zRot = z; } }
    private void addLegYawRoll(String name, float delta) { ModelPart part = optional(name); if (part != null) { part.yRot += delta; part.zRot += delta; } }
}
