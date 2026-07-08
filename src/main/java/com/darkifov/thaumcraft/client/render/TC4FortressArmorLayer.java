package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.runic.TC4FortressArmorRuntime;
import com.darkifov.thaumcraft.runic.TC4FortressMaskRuntime;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Stage213 player layer that mirrors TC4 ModelFortressArmor texture selection.
 *
 * The original 1.7.10 model used fortress_armor.png for all pieces and added
 * separate runic_goggles/mask geometry when the helmet had the corresponding
 * NBT upgrades.  This 1.19.2 layer intentionally keeps vanilla humanoid bones
 * for compatibility while binding the original TC4 textures and visibility
 * branches.
 */
public class TC4FortressArmorLayer extends RenderLayer<AbstractClientPlayer, net.minecraft.client.model.PlayerModel<AbstractClientPlayer>> {
    public static final ResourceLocation FORTRESS_ARMOR = new ResourceLocation("thaumcraft", "textures/models/fortress_armor.png");
    public static final ResourceLocation RUNIC_GOGGLES = new ResourceLocation("thaumcraft", "textures/models/runic_goggles.png");

    private final HumanoidModel<AbstractClientPlayer> outerModel;
    private final HumanoidModel<AbstractClientPlayer> innerModel;

    public TC4FortressArmorLayer(PlayerRenderer renderer) {
        super(renderer);
        this.outerModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER));
        this.innerModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
    }

    public TC4FortressArmorLayer(RenderLayerParent<AbstractClientPlayer, net.minecraft.client.model.PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
        this.outerModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER));
        this.innerModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        renderFortressPiece(poseStack, buffer, packedLight, player, EquipmentSlot.HEAD, innerModel, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        renderFortressPiece(poseStack, buffer, packedLight, player, EquipmentSlot.CHEST, outerModel, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        renderFortressPiece(poseStack, buffer, packedLight, player, EquipmentSlot.LEGS, innerModel, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    private void renderFortressPiece(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, EquipmentSlot slot, HumanoidModel<AbstractClientPlayer> model, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack stack = player.getItemBySlot(slot);
        if (!TC4FortressArmorRuntime.isFortressPiece(stack)) {
            return;
        }
        getParentModel().copyPropertiesTo(model);
        model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        setPartVisibility(model, slot);
        VertexConsumer main = buffer.getBuffer(RenderType.entityCutoutNoCull(FORTRESS_ARMOR));
        model.renderToBuffer(poseStack, main, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        if (slot == EquipmentSlot.HEAD && TC4FortressMaskRuntime.hasGoggles(stack)) {
            VertexConsumer goggles = buffer.getBuffer(RenderType.entityTranslucent(RUNIC_GOGGLES));
            model.renderToBuffer(poseStack, goggles, packedLight, OverlayTexture.NO_OVERLAY, 0.6F, 0.9F, 1.0F, 0.85F);
        }
    }

    private static void setPartVisibility(HumanoidModel<?> model, EquipmentSlot slot) {
        model.setAllVisible(false);
        model.head.visible = slot == EquipmentSlot.HEAD;
        model.hat.visible = slot == EquipmentSlot.HEAD;
        model.body.visible = slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS;
        model.rightArm.visible = slot == EquipmentSlot.CHEST;
        model.leftArm.visible = slot == EquipmentSlot.CHEST;
        model.rightLeg.visible = slot == EquipmentSlot.LEGS;
        model.leftLeg.visible = slot == EquipmentSlot.LEGS;
    }
}
