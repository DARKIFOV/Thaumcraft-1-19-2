package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Forge 1.19.2 adapter for the original TC4 armor-layout goggles texture.
 *
 * <p>The PNG is a 2x 64x32 armor atlas (128x64 physical pixels).  Earlier
 * rebuild code stretched arbitrary full-atlas strips over handmade planes,
 * sampling mostly transparent pixels.  This layer now uses the vanilla
 * humanoid head-cube UV net with the original logical 64x32 coordinates, which
 * is how ItemGoggles#getArmorTexture was consumed in TC4.</p>
 */
public final class TC4GogglesLayer extends RenderLayer<AbstractClientPlayer, net.minecraft.client.model.PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation GOGGLES_TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/goggles.png");
    private static final float TEX_W = 64.0F;
    private static final float TEX_H = 32.0F;

    public TC4GogglesLayer(RenderLayerParent<AbstractClientPlayer, net.minecraft.client.model.PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.is(ThaumcraftMod.GOGGLES_OF_REVEALING.get())
                && !head.is(ThaumcraftMod.HELMET_OF_REVEALING.get())) {
            return;
        }

        poseStack.pushPose();
        getParentModel().head.translateAndRotate(poseStack);
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(GOGGLES_TEXTURE));
        int light = Math.max(packedLight, 15728880);

        // Slightly inflated humanoid head cube.  Coordinates are local after
        // PlayerModel.head.translateAndRotate; UVs are the original armor net.
        float x0 = -0.2575F, x1 = 0.2575F;
        float y0 = -0.5075F, y1 = 0.0075F;
        float z0 = -0.2575F, z1 = 0.2575F;

        // right, front, left, back
        quadPx(matrix, consumer, x1,y0,z0, x1,y0,z1, x1,y1,z1, x1,y1,z0, 0,8,8,16, light);
        quadPx(matrix, consumer, x0,y0,z0, x1,y0,z0, x1,y1,z0, x0,y1,z0, 8,8,16,16, light);
        quadPx(matrix, consumer, x0,y0,z1, x0,y0,z0, x0,y1,z0, x0,y1,z1, 16,8,24,16, light);
        quadPx(matrix, consumer, x1,y0,z1, x0,y0,z1, x0,y1,z1, x1,y1,z1, 24,8,32,16, light);
        // top and bottom
        quadPx(matrix, consumer, x0,y0,z1, x1,y0,z1, x1,y0,z0, x0,y0,z0, 8,0,16,8, light);
        quadPx(matrix, consumer, x0,y1,z0, x1,y1,z0, x1,y1,z1, x0,y1,z1, 16,0,24,8, light);
        poseStack.popPose();
    }

    private static void quadPx(Matrix4f matrix, VertexConsumer consumer,
                               float x1, float y1, float z1,
                               float x2, float y2, float z2,
                               float x3, float y3, float z3,
                               float x4, float y4, float z4,
                               float u0px, float v0px, float u1px, float v1px, int light) {
        float u0 = u0px / TEX_W;
        float v0 = v0px / TEX_H;
        float u1 = u1px / TEX_W;
        float v1 = v1px / TEX_H;
        vertex(matrix, consumer, x1, y1, z1, u0, v1, light);
        vertex(matrix, consumer, x2, y2, z2, u1, v1, light);
        vertex(matrix, consumer, x3, y3, z3, u1, v0, light);
        vertex(matrix, consumer, x4, y4, z4, u0, v0, light);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer,
                               float x, float y, float z, float u, float v, int light) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0.0F, 0.0F, 1.0F)
                .endVertex();
    }
}
