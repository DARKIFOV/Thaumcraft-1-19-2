package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.BrainJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.TC4BrainJarParity;
import com.darkifov.thaumcraft.client.render.model.TC4BrainJarBrineModel;
import com.darkifov.thaumcraft.client.render.model.TC4BrainJarModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Exact TC4 TileJarRenderer brain and ModelJar brine transforms. */
public final class BrainJarRenderer implements BlockEntityRenderer<BrainJarBlockEntity> {
    private static final ResourceLocation BRAIN_TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/brain2.png");
    private static final ResourceLocation BRINE_TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/jarbrine.png");

    private final TC4BrainJarModel brainModel;
    private final TC4BrainJarBrineModel brineModel;

    public BrainJarRenderer(BlockEntityRendererProvider.Context context) {
        brainModel = new TC4BrainJarModel(context.bakeLayer(TC4BrainJarModel.LAYER));
        brineModel = new TC4BrainJarBrineModel(context.bakeLayer(TC4BrainJarBrineModel.LAYER));
    }

    @Override
    public void render(BrainJarBlockEntity brain, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int playerTicks = Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount;
        float bob = TC4BrainJarParity.bob(playerTicks);

        // TileJarRenderer: translate to block centre, lift 0.01 and invert model Y/Z.
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.01D, 0.5D);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));

        poseStack.pushPose();
        poseStack.translate(0.0D, TC4BrainJarParity.BRAIN_Y_OFFSET + bob, 0.0D);
        poseStack.mulPose(Vector3f.YP.rotation(brain.rotation(partialTick)));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
        poseStack.scale(TC4BrainJarParity.BRAIN_RENDER_SCALE,
                TC4BrainJarParity.BRAIN_RENDER_SCALE,
                TC4BrainJarParity.BRAIN_RENDER_SCALE);
        brainModel.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(BRAIN_TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        brineModel.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(BRINE_TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
