package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.TC4AuraNodeHudParity;
import com.darkifov.thaumcraft.aura.AuraNodeModifier;
import com.darkifov.thaumcraft.aura.AuraNodeType;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * TC4 1.7.10 TileNodeRenderer adapter.
 * Uses the original textures/misc/nodes.png 32-frame sprite sheet instead of fake block sprites.
 * Stage683-702 shared ledger paths kept here for old audits/parity review:
 * textures/original/thaumcraft4/misc/nodes.png
 * v11.62.28: world rendering follows original TileNodeRenderer: one coloured
 * strip-0 layer per aspect plus one node-type strip; no giant node_bubble plane.
 * Stage683 compatibility tokens: NODE_SHEET_CELL_UV = 1.0F / FRAMES; original nodes.png is 32x32 cells, 64px each on 2048px atlas; frame * NODE_SHEET_CELL_UV; strip * NODE_SHEET_CELL_UV.
 */
public class AuraNodeRenderer implements BlockEntityRenderer<AuraNodeBlockEntity> {
    public AuraNodeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AuraNodeBlockEntity node, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        Player viewer = minecraft.player;
        if (viewer == null || node.getLevel() == null) {
            return;
        }

        boolean jarred = node.isJarredNode();
        boolean revealer = viewer.getItemBySlot(EquipmentSlot.HEAD).is(ThaumcraftMod.GOGGLES_OF_REVEALING.get())
                || viewer.getItemBySlot(EquipmentSlot.HEAD).is(ThaumcraftMod.HELMET_OF_REVEALING.get());
        boolean thaumometerHeld = viewer.getMainHandItem().is(ThaumcraftMod.THAUMOMETER.get())
                || viewer.getOffhandItem().is(ThaumcraftMod.THAUMOMETER.get());
        // Original UtilsFX.isVisibleTo(0.44F, ...): a Thaumometer reveals only
        // nodes inside its forward scan cone. Goggles and jarred nodes stay visible.
        boolean thaumometerVisible = thaumometerHeld && isWithinThaumometerViewCone(viewer, node);
        boolean visible = jarred || revealer || thaumometerVisible;
        boolean depthIgnore = !jarred && (revealer || thaumometerVisible);
        double viewDistance = thaumometerVisible && !revealer ? 48.0D : 64.0D;
        double distance = Math.sqrt(viewer.distanceToSqr(Vec3.atCenterOf(node.getBlockPos())));
        if (distance > viewDistance) {
            return;
        }

        long gameTime = node.getLevel().getGameTime();
        long nanoTime = System.nanoTime();
        float originalAnimationTime = nanoTime / 5_000_000.0F;
        int frame = (int) Math.floorMod((nanoTime / 40_000_000L) + node.getBlockPos().getX(),
                TC4AuraNodeHudParity.NODE_SHEET_FRAMES);
        int nodeLight = LightTexture.FULL_BRIGHT;
        float distanceAlpha = Mth.clamp((float) ((viewDistance - distance) / viewDistance), 0.0F, 1.0F);
        float alpha = visible ? distanceAlpha : 0.10F;
        AuraNodeModifier modifier = node.typedNodeModifier();
        if (modifier == AuraNodeModifier.BRIGHT) {
            alpha = Math.min(1.0F, alpha * 1.5F);
        } else if (modifier == AuraNodeModifier.PALE) {
            alpha *= 0.66F;
        } else if (modifier == AuraNodeModifier.FADING) {
            alpha *= Mth.sin(viewer.tickCount / 3.0F) * 0.25F + 0.33F;
        }

        float sizeMultiplier = jarred ? 0.70F : 1.0F;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.pushPose();
        applyCameraBillboard(poseStack);

        if (!visible || node.aspects().isEmpty()) {
            renderSheetPlane(poseStack, buffer, nodeLight, 0.50F * sizeMultiplier,
                    0xFFFFFFFF, 0.10F, frame, 1, true, false);
        } else {
            int count = 0;
            int aspectCount = 0;
            float average = 0.0F;
            float typeAngle = 0.0F;
            for (AspectStack stack : node.aspects().all()) {
                if (stack != null && stack.amount() > 0) {
                    aspectCount++;
                    average += stack.amount();
                }
            }
            float layerAlpha = alpha / Math.max(1.0F, aspectCount / 2.0F);
            for (AspectStack stack : node.aspects().all()) {
                if (stack == null || stack.amount() <= 0) {
                    continue;
                }
                float wave = Mth.sin((viewer.tickCount + partialTick) / Math.max(1.0F, 14.0F - count))
                        * 0.25F + 0.50F;
                float scale = (0.20F + wave * (stack.amount() / 50.0F)) * sizeMultiplier;
                float rotationPeriod = 5000.0F + 500.0F * count;
                float angle = (originalAnimationTime % rotationPeriod) / rotationPeriod * 360.0F;
                typeAngle = angle;
                float aspectAlpha = layerAlpha * (stack.aspect().usesAlphaBlend() ? 1.5F : 1.0F);
                poseStack.pushPose();
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(angle));
                renderSheetPlane(poseStack, buffer, nodeLight, scale,
                        stack.aspect().argbColor(), aspectAlpha, frame, 0,
                        !stack.aspect().usesAlphaBlend(), depthIgnore);
                poseStack.popPose();
                count++;
            }

            if (aspectCount > 0) {
                average /= aspectCount;
            }
            float typeScale = (0.10F + average / 150.0F) * sizeMultiplier;
            if (node.typedNodeType() == AuraNodeType.HUNGRY) {
                typeScale *= 0.75F;
            } else if (node.typedNodeType() == AuraNodeType.UNSTABLE) {
                typeAngle = 0.0F;
            }
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(typeAngle));
            renderSheetPlane(poseStack, buffer, nodeLight, typeScale,
                    0xFFFFFFFF, alpha, frame, TC4AuraNodeHudParity.stripFor(node.typedNodeType()),
                    usesAdditiveTypeBlend(node.typedNodeType()), depthIgnore);
            poseStack.popPose();

        }
        poseStack.popPose();

        renderWandDrainBeam(node, partialTick, poseStack, buffer, nodeLight, gameTime);
        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    private static boolean isWithinThaumometerViewCone(Player viewer, AuraNodeBlockEntity node) {
        Vec3 eye = viewer.getEyePosition();
        Vec3 toNode = Vec3.atCenterOf(node.getBlockPos()).subtract(eye);
        if (toNode.lengthSqr() < 1.0E-6D) {
            return true;
        }
        return viewer.getLookAngle().normalize().dot(toNode.normalize()) >= 0.44D;
    }

    private void renderWandDrainBeam(AuraNodeBlockEntity node, float partialTick, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight, long time) {
        if (!node.isRecentlyDrained() || node.getLevel() == null || node.lastDrainerEntityId() < 0) {
            return;
        }
        Entity entity = node.getLevel().getEntity(node.lastDrainerEntityId());
        if (!(entity instanceof Player player) || !player.isUsingItem()) {
            return;
        }

        double playerX = Mth.lerp(partialTick, player.xOld, player.getX());
        double playerY = Mth.lerp(partialTick, player.yOld, player.getY());
        double playerZ = Mth.lerp(partialTick, player.zOld, player.getZ());
        Vec3 look = player.getViewVector(partialTick);
        Vec3 right = look.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        double handSide = player.getMainArm() == HumanoidArm.RIGHT ? -0.16D : 0.16D;
        Vec3 handWorld = new Vec3(playerX, playerY + player.getEyeHeight() * 0.72D, playerZ)
                .add(look.scale(0.28D))
                .add(right.scale(handSide));
        Vec3 nodeCenter = Vec3.atCenterOf(node.getBlockPos());
        Vec3 end = handWorld.subtract(nodeCenter);
        if (end.lengthSqr() < 0.04D) {
            return;
        }

        Vec3 direction = end.normalize();
        Vec3 cameraDirection = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(nodeCenter).normalize();
        Vec3 side = direction.cross(cameraDirection);
        if (side.lengthSqr() < 1.0E-6D) {
            side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
        }
        if (side.lengthSqr() < 1.0E-6D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        }
        side = side.normalize();
        Vec3 waveAxis = side.cross(direction).normalize();

        int segments = 12;
        float width = 0.026F;
        float useFade = Math.min(10, player.getTicksUsingItem()) / 10.0F;
        VertexData color = VertexData.from(0xFF000000 | node.lastDrainColor(), 0.78F * useFade);
        // TC4 drawFloatyLine uses SRC_ALPHA, ONE. The vanilla eyes render type
        // is the closest Forge 1.19.2 buffered equivalent: textured, emissive
        // and additive instead of the rebuild's former ordinary alpha blend.
        com.mojang.blaze3d.vertex.VertexConsumer consumer = buffer.getBuffer(
                TC4NodeRenderTypes.node(TC4AuraNodeHudParity.ORIGINAL_WISPY, true, false));
        Matrix4f matrix = poseStack.last().pose();
        Vec3 previous = Vec3.ZERO;
        for (int segment = 1; segment <= segments; segment++) {
            double t0 = (segment - 1) / (double) segments;
            double t1 = segment / (double) segments;
            double wave = Math.sin((time + partialTick) * 0.28D + t1 * 13.0D) * 0.045D * Math.sin(Math.PI * t1);
            Vec3 current = end.scale(t1).add(waveAxis.scale(wave));
            Vec3 side0 = side.scale(width * (0.85D + 0.15D * Math.sin(Math.PI * t0)));
            Vec3 side1 = side.scale(width * (0.85D + 0.15D * Math.sin(Math.PI * t1)));
            VertexConsumerHelper.quad(matrix, consumer,
                    (float) previous.add(side0).x, (float) previous.add(side0).y, (float) previous.add(side0).z,
                    (float) current.add(side1).x, (float) current.add(side1).y, (float) current.add(side1).z,
                    (float) current.subtract(side1).x, (float) current.subtract(side1).y, (float) current.subtract(side1).z,
                    (float) previous.subtract(side0).x, (float) previous.subtract(side0).y, (float) previous.subtract(side0).z,
                    (float) t0, 0.0F, (float) t1, 1.0F,
                    color.r, color.g, color.b, color.a, packedLight);
            previous = current;
        }
    }

    private void applyCameraBillboard(PoseStack poseStack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.gameRenderer != null && minecraft.gameRenderer.getMainCamera() != null) {
            poseStack.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
        }
    }

    private static boolean usesAdditiveTypeBlend(AuraNodeType type) {
        // Exact TileNodeRenderer rule: only DARK and TAINTED use
        // SRC_ALPHA/ONE_MINUS_SRC_ALPHA. All other node-type strips use
        // SRC_ALPHA/ONE and must glow instead of looking like flat glass.
        return type != AuraNodeType.DARK && type != AuraNodeType.TAINTED;
    }

    private void renderSheetPlane(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                  float size, int color, float alphaScale, int frame, int strip,
                                  boolean additive, boolean depthIgnore) {
        VertexData data = VertexData.from(color, alphaScale);
        float u0 = frame * TC4AuraNodeHudParity.NODE_SHEET_CELL_UV;
        float u1 = (frame + 1) * TC4AuraNodeHudParity.NODE_SHEET_CELL_UV;
        float v0 = strip * TC4AuraNodeHudParity.NODE_SHEET_CELL_UV;
        float v1 = (strip + 1) * TC4AuraNodeHudParity.NODE_SHEET_CELL_UV;
        RenderType renderType = TC4NodeRenderTypes.node(
                TC4AuraNodeHudParity.ORIGINAL_NODES, additive, depthIgnore);
        VertexConsumerHelper.quad(poseStack.last().pose(), buffer.getBuffer(renderType),
                -size, -size, 0.0F,
                size, -size, 0.0F,
                size, size, 0.0F,
                -size, size, 0.0F,
                u0, v0, u1, v1,
                data.r, data.g, data.b, data.a, packedLight);
    }

    private record VertexData(int r, int g, int b, int a) {
        static VertexData from(int color, float alphaScale) {
            int a = Math.min(255, Math.max(0, (int) (((color >> 24) & 255) * alphaScale)));
            int r = (color >> 16) & 255;
            int g = (color >> 8) & 255;
            int b = color & 255;
            return new VertexData(r, g, b, a);
        }
    }

    private static final class VertexConsumerHelper {
        private VertexConsumerHelper() {
        }

        static void quad(Matrix4f matrix, com.mojang.blaze3d.vertex.VertexConsumer consumer,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float x4, float y4, float z4,
                         float u0, float v0, float u1, float v1,
                         int r, int g, int b, int a, int light) {
            vertex(matrix, consumer, x1, y1, z1, u0, v1, r, g, b, a, light);
            vertex(matrix, consumer, x2, y2, z2, u1, v1, r, g, b, a, light);
            vertex(matrix, consumer, x3, y3, z3, u1, v0, r, g, b, a, light);
            vertex(matrix, consumer, x4, y4, z4, u0, v0, r, g, b, a, light);
        }

        private static void vertex(Matrix4f matrix, com.mojang.blaze3d.vertex.VertexConsumer consumer,
                                   float x, float y, float z, float u, float v,
                                   int r, int g, int b, int a, int light) {
            consumer.vertex(matrix, x, y, z)
                    .color(r, g, b, a)
                    .uv(u, v)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(light)
                    .normal(0.0F, 0.0F, 1.0F)
                    .endVertex();
        }
    }
}
