package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.VisRelayBlockEntity;
import com.darkifov.thaumcraft.client.TC4RevealerHudAdapter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Numeric 1.19.2 adapter for TC4 FXBeamPower (beam1.png, crossed additive quads). */
final class TC4VisRelayBeamRenderer {
    private static final ResourceLocation BEAM = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/misc/beam1.png");
    private static final double HALF_WIDTH = 0.15D * 0.7D;

    private TC4VisRelayBeamRenderer() {
    }

    static void render(VisRelayBlockEntity relay, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        BlockPos parent = relay.parentPos();
        if (parent == null) return;

        BlockPos origin = relay.getBlockPos();
        Vec3 start = new Vec3(0.5D, 0.5D, 0.5D);
        Vec3 end = new Vec3(parent.getX() - origin.getX() + 0.5D,
                parent.getY() - origin.getY() + 0.5D,
                parent.getZ() - origin.getZ() + 0.5D);
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length < 1.0E-5D) return;

        Vec3 direction = delta.scale(1.0D / length);
        Vec3 side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 1.0E-5D) side = direction.cross(new Vec3(1.0D, 0.0D, 0.0D));
        side = side.normalize().scale(HALF_WIDTH);
        Vec3 up = direction.cross(side).normalize().scale(HALF_WIDTH);

        float pulse = relay.pulseStrength(partialTick);
        float baseOpacity = pulse > 0.0F ? 0.8F : 0.3F;
        Minecraft minecraft = Minecraft.getInstance();
        float reveal = minecraft.player != null && TC4RevealerHudAdapter.isRevealer(minecraft.player) ? 1.0F : 0.1F;
        int alpha = Mth.clamp(Math.round(baseOpacity * reveal * 255.0F), 1, 255);
        int color = relay.beamColor(partialTick);
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;

        float ticks = (relay.getLevel() == null ? 0L : relay.getLevel().getGameTime()) + partialTick;
        float scroll = -ticks * 0.2F - Mth.floor(-ticks * 0.1F);
        float v0 = -1.0F + scroll;
        float v1 = (float)length + v0;

        VertexConsumer consumer = buffer.getBuffer(TC4NodeRenderTypes.node(BEAM, true, false));
        Matrix4f matrix = poseStack.last().pose();
        quad(matrix, consumer, start.subtract(side), end.subtract(side), end.add(side), start.add(side),
                v0, v1, red, green, blue, alpha);
        quad(matrix, consumer, start.subtract(up), end.subtract(up), end.add(up), start.add(up),
                v0 + 1.0F / 3.0F, v1 + 1.0F / 3.0F, red, green, blue, alpha);
    }

    private static void quad(Matrix4f matrix, VertexConsumer consumer,
                             Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4,
                             float v0, float v1, int r, int g, int b, int a) {
        vertex(matrix, consumer, p1, 1.0F, v1, r, g, b, a);
        vertex(matrix, consumer, p2, 1.0F, v0, r, g, b, a);
        vertex(matrix, consumer, p3, 0.0F, v0, r, g, b, a);
        vertex(matrix, consumer, p4, 0.0F, v1, r, g, b, a);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, Vec3 point,
                               float u, float v, int r, int g, int b, int a) {
        consumer.vertex(matrix, (float)point.x, (float)point.y, (float)point.z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
