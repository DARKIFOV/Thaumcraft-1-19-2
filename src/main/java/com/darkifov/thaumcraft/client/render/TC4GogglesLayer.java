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
 * Stage403-422 player-layer adapter for original TC4 goggles of revealing.
 *
 * <p>TC4 used a custom head model with a front lens plate and side straps.
 * Forge 1.19.2 cannot consume the old ModelGoggles directly here, so this layer
 * keeps the real item/equipment behavior but renders a small multi-plane model
 * using the original models/goggles.png texture instead of the vanilla helmet
 * placeholder or a single flat billboard. Stage443-462 keeps the original
 * texture path and forbids generated vanilla helmet geometry fallback.</p>
 */
public final class TC4GogglesLayer extends RenderLayer<AbstractClientPlayer, net.minecraft.client.model.PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation GOGGLES_TEXTURE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/models/goggles.png");

    public TC4GogglesLayer(RenderLayerParent<AbstractClientPlayer, net.minecraft.client.model.PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.is(ThaumcraftMod.GOGGLES_OF_REVEALING.get())) {
            return;
        }
        poseStack.pushPose();
        getParentModel().head.translateAndRotate(poseStack);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(GOGGLES_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        int light = Math.max(packedLight, 15728880);

        // Front lens/bridge plane: mounted just in front of the player head.
        quad(matrix, consumer,
                -0.365F, -0.287F, -0.305F,
                 0.365F, -0.287F, -0.305F,
                 0.365F, -0.085F, -0.305F,
                -0.365F, -0.085F, -0.305F,
                0.000F, 0.000F, 1.000F, 0.500F, light);

        // Slightly raised dark lens rim from the upper strip of the same original texture.
        quad(matrix, consumer,
                -0.410F, -0.318F, -0.315F,
                 0.410F, -0.318F, -0.315F,
                 0.410F, -0.270F, -0.315F,
                -0.410F, -0.270F, -0.315F,
                0.000F, 0.500F, 1.000F, 0.625F, light);

        // Left and right side straps wrap backward along the head cube.
        quad(matrix, consumer,
                -0.410F, -0.300F, -0.285F,
                -0.410F, -0.300F,  0.235F,
                -0.410F, -0.245F,  0.235F,
                -0.410F, -0.245F, -0.285F,
                0.000F, 0.625F, 1.000F, 0.750F, light);
        quad(matrix, consumer,
                 0.410F, -0.300F,  0.235F,
                 0.410F, -0.300F, -0.285F,
                 0.410F, -0.245F, -0.285F,
                 0.410F, -0.245F,  0.235F,
                0.000F, 0.625F, 1.000F, 0.750F, light);

        // Back strap; very small but prevents the goggles from looking like a flat sticker.
        quad(matrix, consumer,
                 0.390F, -0.300F, 0.250F,
                -0.390F, -0.300F, 0.250F,
                -0.390F, -0.245F, 0.250F,
                 0.390F, -0.245F, 0.250F,
                0.000F, 0.750F, 1.000F, 0.875F, light);
        poseStack.popPose();
    }

    private static void quad(Matrix4f matrix, VertexConsumer consumer,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float u0, float v0, float u1, float v1, int light) {
        vertex(matrix, consumer, x1, y1, z1, u0, v1, light);
        vertex(matrix, consumer, x2, y2, z2, u1, v1, light);
        vertex(matrix, consumer, x3, y3, z3, u1, v0, light);
        vertex(matrix, consumer, x4, y4, z4, u0, v0, light);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float u, float v, int light) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0.0F, 0.0F, 1.0F)
                .endVertex();
    }
}
