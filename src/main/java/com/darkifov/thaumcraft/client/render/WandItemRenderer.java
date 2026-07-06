package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.wand.WandComponentData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class WandItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static WandItemRenderer INSTANCE;

    private WandItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static WandItemRenderer instance() {
        if (INSTANCE == null) {
            INSTANCE = new WandItemRenderer();
        }

        return INSTANCE;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        WandComponentData data = WandComponentData.from(stack);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);

        if (transformType == ItemTransforms.TransformType.GUI) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(28.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(225.0F));
            poseStack.scale(0.92F, 0.92F, 0.92F);
        } else if (transformType.firstPerson()) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-38.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-32.0F));
            poseStack.scale(0.88F, 0.88F, 0.88F);
        } else {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-35.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-28.0F));
            poseStack.scale(0.82F, 0.82F, 0.82F);
        }

        ResourceLocation rodTexture = wandTexture(data.rod().rendererTexture());
        ResourceLocation capTexture = wandTexture(data.cap().rendererTexture());

        VertexConsumer rodConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(rodTexture));
        VertexConsumer capConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(capTexture));

        int light = data.rod().glowing() ? Math.max(packedLight, 15728880) : packedLight;
        float rodHalf = data.rod().staff() ? 0.78F : 0.60F;
        float capInner = data.rod().staff() ? 0.76F : 0.58F;
        float capOuter = data.rod().staff() ? 0.92F : 0.72F;
        float thickness = data.rod().staff() ? 0.065F : 0.055F;

        // TC4 intent: one rod core plus two separate caps. Staff rods are longer and thicker.
        renderBox(poseStack, rodConsumer, -thickness, -rodHalf, -thickness, thickness, rodHalf, thickness, light, 255);
        renderBox(poseStack, capConsumer, -0.095F, -capOuter, -0.095F, 0.095F, -capInner, 0.095F, light, 255);
        renderBox(poseStack, capConsumer, -0.095F, capInner, -0.095F, 0.095F, capOuter, 0.095F, light, 255);

        poseStack.popPose();
    }

    private ResourceLocation wandTexture(String name) {
        return new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/entity/wand/" + name + ".png");
    }

    private void renderBox(PoseStack poseStack, VertexConsumer consumer,
                           float minX, float minY, float minZ,
                           float maxX, float maxY, float maxZ,
                           int light, int alpha) {
        Matrix4f matrix = poseStack.last().pose();

        // north/south
        quad(matrix, consumer, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, light, alpha);
        quad(matrix, consumer, maxX, minY, maxZ, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, light, alpha);

        // east/west
        quad(matrix, consumer, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, light, alpha);
        quad(matrix, consumer, minX, minY, maxZ, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, light, alpha);

        // up/down
        quad(matrix, consumer, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, light, alpha);
        quad(matrix, consumer, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ, light, alpha);
    }

    private void quad(Matrix4f matrix, VertexConsumer consumer,
                      float x1, float y1, float z1,
                      float x2, float y2, float z2,
                      float x3, float y3, float z3,
                      float x4, float y4, float z4,
                      int light, int alpha) {
        vertex(matrix, consumer, x1, y1, z1, 0.0F, 1.0F, light, alpha);
        vertex(matrix, consumer, x2, y2, z2, 1.0F, 1.0F, light, alpha);
        vertex(matrix, consumer, x3, y3, z3, 1.0F, 0.0F, light, alpha);
        vertex(matrix, consumer, x4, y4, z4, 0.0F, 0.0F, light, alpha);
    }

    private void vertex(Matrix4f matrix, VertexConsumer consumer,
                        float x, float y, float z,
                        float u, float v, int light, int alpha) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0.0F, 0.0F, 1.0F)
                .endVertex();
    }
}
