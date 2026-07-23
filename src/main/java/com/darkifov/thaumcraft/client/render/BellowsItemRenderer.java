package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TC4ArcaneBellowsParity;
import com.darkifov.thaumcraft.client.render.model.TC4BellowsModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Exact TC4 inventory render path for the animated five-part bellows model. */
public final class BellowsItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/models/bellows.png");
    private static BellowsItemRenderer instance;
    private TC4BellowsModel model;

    private BellowsItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static BellowsItemRenderer instance() {
        if (instance == null) {
            instance = new BellowsItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType,
                             PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        if (model == null) {
            model = new TC4BellowsModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(TC4BellowsModel.FRAME_LAYER),
                    Minecraft.getInstance().getEntityModels().bakeLayer(TC4BellowsModel.BAG_LAYER));
        }

        Player player = Minecraft.getInstance().player;
        float inflation = TC4ArcaneBellowsParity.inventoryInflation(player == null ? 0 : player.tickCount);

        poseStack.pushPose();
        // BlockWoodenDeviceRenderer inventory transform, followed by the TESR's
        // orientation=2 (north) transform. It was identical in every display context.
        poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        poseStack.translate(0.5D, -0.5D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        model.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                packedLight, packedOverlay == 0 ? OverlayTexture.NO_OVERLAY : packedOverlay, inflation);
        poseStack.popPose();
    }
}
