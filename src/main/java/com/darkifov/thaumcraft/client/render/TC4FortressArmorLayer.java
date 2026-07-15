package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.client.render.model.TC4FortressArmorModel;
import com.darkifov.thaumcraft.runic.TC4FortressArmorRuntime;
import com.darkifov.thaumcraft.runic.TC4FortressMaskRuntime;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
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

/** Exact-geometry fortress armor layer replacing the former vanilla-model approximation. */
public final class TC4FortressArmorLayer extends RenderLayer<AbstractClientPlayer, net.minecraft.client.model.PlayerModel<AbstractClientPlayer>> {
    public static final ResourceLocation FORTRESS_ARMOR = new ResourceLocation("thaumcraft", "textures/models/fortress_armor.png");
    private final TC4FortressArmorModel<AbstractClientPlayer> model;

    public TC4FortressArmorLayer(PlayerRenderer renderer) {
        this((RenderLayerParent<AbstractClientPlayer, net.minecraft.client.model.PlayerModel<AbstractClientPlayer>>) renderer);
    }

    public TC4FortressArmorLayer(RenderLayerParent<AbstractClientPlayer, net.minecraft.client.model.PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
        model = new TC4FortressArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(TC4FortressArmorModel.LAYER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        int pieces = countPieces(player);
        renderPiece(poseStack, buffer, packedLight, player, EquipmentSlot.HEAD, pieces,
                limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        renderPiece(poseStack, buffer, packedLight, player, EquipmentSlot.CHEST, pieces,
                limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        renderPiece(poseStack, buffer, packedLight, player, EquipmentSlot.LEGS, pieces,
                limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    private void renderPiece(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                             AbstractClientPlayer player, EquipmentSlot slot, int pieces,
                             float limbSwing, float limbSwingAmount, float ageInTicks,
                             float netHeadYaw, float headPitch) {
        ItemStack stack = player.getItemBySlot(slot);
        if (!TC4FortressArmorRuntime.isFortressPiece(stack)) {
            return;
        }
        getParentModel().copyPropertiesTo(model);
        model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        int mask = slot == EquipmentSlot.HEAD ? TC4FortressMaskRuntime.mask(stack) : -1;
        boolean goggles = slot == EquipmentSlot.HEAD && TC4FortressMaskRuntime.hasGoggles(stack);
        model.configure(slot, pieces, mask, goggles);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(FORTRESS_ARMOR));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static int countPieces(AbstractClientPlayer player) {
        int count = 0;
        if (TC4FortressArmorRuntime.isFortressPiece(player.getItemBySlot(EquipmentSlot.HEAD))) count++;
        if (TC4FortressArmorRuntime.isFortressPiece(player.getItemBySlot(EquipmentSlot.CHEST))) count++;
        if (TC4FortressArmorRuntime.isFortressPiece(player.getItemBySlot(EquipmentSlot.LEGS))) count++;
        return count;
    }
}
