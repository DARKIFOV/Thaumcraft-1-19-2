package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ManaPodBlock;
import com.darkifov.thaumcraft.blockentity.ManaPodBlockEntity;
import com.darkifov.thaumcraft.client.render.model.TC4ManaPodModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.awt.Color;

/** Aspect-coloured TESR parity for TC4 TileManaPodRenderer. */
public final class ManaPodRenderer implements BlockEntityRenderer<ManaPodBlockEntity> {
    private static final ResourceLocation POD_INNER = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/models/manapod_0.png");
    private static final ResourceLocation POD_OUTER = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/models/manapod_2.png");
    private static final float BASE_RED = 37.0F / 255.0F;
    private static final float BASE_GREEN = 157.0F / 255.0F;
    private static final float BASE_BLUE = 117.0F / 255.0F;

    private final TC4ManaPodModel model;

    public ManaPodRenderer(BlockEntityRendererProvider.Context context) {
        model = new TC4ManaPodModel(context.bakeLayer(TC4ManaPodModel.LAYER));
    }

    @Override
    public void render(ManaPodBlockEntity pod, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockState state = pod.getBlockState();
        if (!state.hasProperty(ManaPodBlock.AGE)) {
            return;
        }
        int age = state.getValue(ManaPodBlock.AGE);
        if (age <= 1) {
            return;
        }

        Aspect aspect = pod.aspect() == null ? Aspect.HERBA : pod.aspect();
        Color color = new Color(aspect.nativeColor());
        float aspectRed = color.getRed() / 255.0F;
        float aspectGreen = color.getGreen() / 255.0F;
        float aspectBlue = color.getBlue() / 255.0F;
        float red;
        float green;
        float blue;
        if (age == 7) {
            red = aspectRed;
            green = aspectGreen;
            blue = aspectBlue;
        } else {
            float mix = age - 2.0F;
            red = (BASE_RED + aspectRed * mix) / (mix + 1.0F);
            green = (BASE_GREEN + aspectGreen * mix) / (mix + 1.0F);
            blue = (BASE_BLUE + aspectBlue * mix) / (mix + 1.0F);
        }

        long gameTime = pod.getLevel() == null ? 0L : pod.getLevel().getGameTime();
        float pulse = Mth.sin((gameTime + partialTick + Math.floorMod(pod.hashCode(), 100)) / 8.0F) * 0.1F + 0.9F;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.75D, 0.5D);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));

        if (age > 2) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.1D, 0.0D);
            float innerScale = 0.125F * age * pulse;
            poseStack.scale(innerScale, innerScale, innerScale);
            VertexConsumer inner = buffers.getBuffer(RenderType.entityTranslucent(POD_INNER));
            model.renderInner(poseStack, inner, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }

        float outerScale = 0.15F * age;
        poseStack.scale(outerScale, outerScale, outerScale);
        VertexConsumer outer = buffers.getBuffer(RenderType.entityTranslucent(POD_OUTER));
        model.renderOuter(poseStack, outer, packedLight, OverlayTexture.NO_OVERLAY, red, green, blue, 0.9F);
        poseStack.popPose();
    }
}
