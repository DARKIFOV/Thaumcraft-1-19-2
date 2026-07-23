package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.VisChargeRelayBlockEntity;
import com.darkifov.thaumcraft.client.render.model.TC4VisRelayModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Original TC4 {@code vis_relay.obj} renderer used by the Arcane Workbench
 * charger. The supports and floating ring retain the source transforms while
 * the crystal receives the source five-tick aspect-colour/light pulse.
 */
public final class VisChargeRelayRenderer implements BlockEntityRenderer<VisChargeRelayBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftMod.MOD_ID, "textures/models/vis_relay.png");

    public VisChargeRelayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(VisChargeRelayBlockEntity relay, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderStandalone(relay.pulseAspect(), relay.pulseStrength(partialTick),
                poseStack, buffer, packedLight);
        TC4VisRelayBeamRenderer.render(relay, partialTick, poseStack, buffer);
    }

    public static void renderStandalone(Aspect pulseAspect, float pulseStrength,
                                        PoseStack poseStack, MultiBufferSource buffer,
                                        int packedLight) {
        VertexConsumer solid = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        // Equivalent to the TC4 GL sequence: X -90, X +180, Z +45.
        poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(45.0F));

        TC4VisRelayModel.renderRingFloat(poseStack.last(), solid, packedLight,
                OverlayTexture.NO_OVERLAY);

        poseStack.pushPose();
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
        poseStack.translate(0.0D, 0.0D, 0.5D);
        for (int side = 0; side < 4; side++) {
            TC4VisRelayModel.renderSupport(poseStack.last(), solid, packedLight,
                    OverlayTexture.NO_OVERLAY);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
        }
        poseStack.popPose();

        int rgb = pulseAspect == null ? 0xFFFFFF : relayColor(pulseAspect);
        float pulse = Math.max(0.0F, Math.min(1.0F, pulseStrength));
        int red = brighten((rgb >> 16) & 255, pulse);
        int green = brighten((rgb >> 8) & 255, pulse);
        int blue = brighten(rgb & 255, pulse);
        int crystalLight = pulse > 0.0F ? LightTexture.FULL_BRIGHT : packedLight;
        VertexConsumer glow = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        TC4VisRelayModel.renderCrystal(poseStack.last(), glow, crystalLight,
                OverlayTexture.NO_OVERLAY, red, green, blue, 230);
        poseStack.popPose();
    }

    private static int brighten(int component, float pulse) {
        // TC4 divided pulse colours by 200, allowing a small over-bright glow.
        int lifted = Math.round(component * (1.0F + 0.22F * pulse));
        return Math.min(255, Math.max(0, lifted));
    }

    private static int relayColor(Aspect aspect) {
        return switch (aspect) {
            case AER -> 0xFFFF7E;
            case IGNIS -> 0xFF3C01;
            case AQUA -> 0x0090FF;
            case TERRA -> 0x00A000;
            case ORDO -> 0xEECFFF;
            case PERDITIO -> 0x555577;
            default -> aspect.nativeColor();
        };
    }
}
