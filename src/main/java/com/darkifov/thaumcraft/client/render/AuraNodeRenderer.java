package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.TC4AuraNodeHudParity;
import com.darkifov.thaumcraft.aura.AuraNodeModifier;
import com.darkifov.thaumcraft.aura.AuraNodeType;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.block.WandItem;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

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
    /** Original Config.golemLinkQuality default; UtilsFX.drawFloatyLine uses half of it. */
    private static final int TC4_LINK_QUALITY = 16;
    private static final float TC4_BEAM_WIDTH = 0.15F;
    private static final float TC4_BEAM_SPEED = -0.02F;
    private final Map<BlockPos, BeamColorState> beamColors = new HashMap<>();

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
        boolean thaumometerVisible = thaumometerHeld && isWithinThaumometerViewCone(viewer, node, partialTick);
        boolean visible = jarred || revealer || thaumometerVisible;
        boolean depthIgnore = !jarred && (revealer || thaumometerVisible);
        double viewDistance = thaumometerVisible && !revealer ? 48.0D : 64.0D;
        double distance = Math.sqrt(viewer.distanceToSqr(Vec3.atCenterOf(node.getBlockPos())));
        if (distance > viewDistance) {
            return;
        }

        long gameTime = node.getLevel().getGameTime();
        long nanoTime = System.nanoTime();
        long originalAnimationTime = nanoTime / 5_000_000L;
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
                long rotationPeriod = 5000L + 500L * count;
                float angle = Math.floorMod(originalAnimationTime, rotationPeriod)
                        / (float) rotationPeriod * 360.0F;
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

    private static boolean isWithinThaumometerViewCone(Player viewer, AuraNodeBlockEntity node, float partialTick) {
        Vec3 eye = viewer.getEyePosition(partialTick);
        Vec3 toNode = Vec3.atCenterOf(node.getBlockPos()).subtract(eye);
        if (toNode.lengthSqr() < 1.0E-6D) {
            return true;
        }
        return viewer.getViewVector(partialTick).normalize().dot(toNode.normalize()) >= 0.44D;
    }

    private void renderWandDrainBeam(AuraNodeBlockEntity node, float partialTick, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight, long gameTime) {
        BlockPos nodePos = node.getBlockPos();
        if (!node.isRecentlyDrained() || node.getLevel() == null || node.lastDrainerEntityId() < 0) {
            beamColors.remove(nodePos);
            return;
        }
        Entity entity = node.getLevel().getEntity(node.lastDrainerEntityId());
        if (!(entity instanceof Player player)) {
            beamColors.remove(nodePos);
            return;
        }

        // The server now clears the node's drain state on release, failed taps,
        // and look-away. Holding a wand remains a network-latency fallback for
        // remote clients where the vanilla use flag can arrive a frame later.
        boolean holdingWand = player.getMainHandItem().getItem() instanceof WandItem
                || player.getOffhandItem().getItem() instanceof WandItem;
        if (!holdingWand) {
            beamColors.remove(nodePos);
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        int useTicks = Math.max(0, player.getTicksUsingItem());
        float useWave = Mth.sin(useTicks / 10.0F) * 10.0F;
        float pitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());
        float yaw = Mth.lerp(partialTick, player.yRotO, player.getYRot());

        // Exact TileNodeRenderer hand vector: Vec3(-0.1,-0.1,0.5), then the
        // interpolated pitch/yaw and the small use-count wobble rotations.
        Vec3 handOffset = new Vec3(-0.1D, -0.1D, 0.5D)
                .xRot(-pitch * ((float)Math.PI / 180.0F))
                .yRot(-yaw * ((float)Math.PI / 180.0F))
                .yRot(-useWave * 0.01F)
                .xRot(-useWave * 0.015F);
        double playerX = Mth.lerp(partialTick, player.xOld, player.getX());
        double playerY = Mth.lerp(partialTick, player.yOld, player.getY());
        double playerZ = Mth.lerp(partialTick, player.zOld, player.getZ());
        double remoteEyeHeight = player == minecraft.player ? 0.0D : player.getEyeHeight();
        Vec3 startWorld = new Vec3(playerX, playerY + remoteEyeHeight, playerZ).add(handOffset);
        Vec3 endWorld = Vec3.atCenterOf(nodePos);

        float distanceFactor = useTicks > 0 ? Math.min(useTicks, 10) / 10.0F : 1.0F;
        int beamColor = smoothBeamColor(nodePos, node.lastDrainColor(), gameTime);
        renderOriginalFloatyLine(startWorld, endWorld, distanceFactor, beamColor,
                poseStack, buffer, packedLight);
    }

    /**
     * Tick-based continuation of TileNode's client colour recurrence:
     * current = (target + current*4) / 5. It deliberately advances by game
     * ticks rather than render frames so 30/60/144 FPS produce the same beam.
     */
    private int smoothBeamColor(BlockPos pos, int target, long gameTime) {
        BeamColorState state = beamColors.computeIfAbsent(pos.immutable(),
                ignored -> new BeamColorState(0xFFFFFF, gameTime));
        int elapsed = (int) Mth.clamp(gameTime - state.lastTick, 0L, 20L);
        for (int i = 0; i < elapsed; i++) {
            state.color = blendColorOneTick(target, state.color);
        }
        state.lastTick = gameTime;
        return state.color;
    }

    private static int blendColorOneTick(int target, int current) {
        int r = (((target >> 16) & 255) + ((current >> 16) & 255) * 4) / 5;
        int g = (((target >> 8) & 255) + ((current >> 8) & 255) * 4) / 5;
        int b = ((target & 255) + (current & 255) * 4) / 5;
        return (r << 16) | (g << 8) | b;
    }

    /** Numeric Forge adapter for UtilsFX.drawFloatyLine used by TileNodeRenderer. */
    private void renderOriginalFloatyLine(Vec3 startWorld, Vec3 endWorld, float distanceFactor,
                                          int color, PoseStack poseStack, MultiBufferSource buffer,
                                          int packedLight) {
        Vec3 delta = startWorld.subtract(endWorld);
        float dist = (float) delta.length();
        if (dist < 1.0E-4F || distanceFactor <= 0.0F) {
            return;
        }

        float blocks = Math.round(dist);
        float length = Math.max(1.0F, blocks * (TC4_LINK_QUALITY / 2.0F));
        int steps = Math.max(1, Mth.floor(length * Mth.clamp(distanceFactor, 0.0F, 1.0F)));
        float time = (float) (System.nanoTime() / 30_000_000L);
        float phase = (time % 32767.0F) / 5.0F;
        float qualityHalf = TC4_LINK_QUALITY / 2.0F;
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        com.mojang.blaze3d.vertex.VertexConsumer consumer = buffer.getBuffer(
                TC4NodeRenderTypes.node(TC4AuraNodeHudParity.ORIGINAL_WISPY, true, false));
        Matrix4f matrix = poseStack.last().pose();
        Vec3 previous = null;
        float previousU = 0.0F;
        float previousAlpha = 0.0F;

        for (int i = 0; i <= steps; i++) {
            float f2 = i / length;
            float f3 = 1.0F - Math.abs(i - length / 2.0F) / (length / 2.0F);
            f3 = Mth.clamp(f3, 0.0F, 1.0F);
            double waveBase = dist * (1.0F - f2) * qualityHalf - phase;
            double dx = delta.x + Mth.sin((float) ((startWorld.z % 16.0D + waveBase) / 4.0D)) * 0.5F * f3;
            double dy = delta.y + Mth.sin((float) ((startWorld.x % 16.0D + waveBase) / 3.0D)) * 0.5F * f3;
            double dz = delta.z + Mth.sin((float) ((startWorld.y % 16.0D + waveBase) / 2.0D)) * 0.5F * f3;
            Vec3 current = new Vec3(dx * f2, dy * f2, dz * f2);
            float currentU = (1.0F - f2) * dist - time * TC4_BEAM_SPEED;

            if (previous != null) {
                VertexConsumerHelper.beamQuad(matrix, consumer,
                        previous.add(0.0D, -TC4_BEAM_WIDTH, 0.0D),
                        current.add(0.0D, -TC4_BEAM_WIDTH, 0.0D),
                        current.add(0.0D, TC4_BEAM_WIDTH, 0.0D),
                        previous.add(0.0D, TC4_BEAM_WIDTH, 0.0D),
                        previousU, currentU, previousAlpha, f3, r, g, b, packedLight);
                VertexConsumerHelper.beamQuad(matrix, consumer,
                        previous.add(-TC4_BEAM_WIDTH, 0.0D, 0.0D),
                        current.add(-TC4_BEAM_WIDTH, 0.0D, 0.0D),
                        current.add(TC4_BEAM_WIDTH, 0.0D, 0.0D),
                        previous.add(TC4_BEAM_WIDTH, 0.0D, 0.0D),
                        previousU, currentU, previousAlpha, f3, r, g, b, packedLight);
            }
            previous = current;
            previousU = currentU;
            previousAlpha = f3;
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


    private static final class BeamColorState {
        private int color;
        private long lastTick;

        private BeamColorState(int color, long lastTick) {
            this.color = color;
            this.lastTick = lastTick;
        }
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

        static void beamQuad(Matrix4f matrix, com.mojang.blaze3d.vertex.VertexConsumer consumer,
                             Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4,
                             float u0, float u1, float alpha0, float alpha1,
                             int r, int g, int b, int light) {
            int a0 = Mth.clamp((int) (alpha0 * 255.0F), 0, 255);
            int a1 = Mth.clamp((int) (alpha1 * 255.0F), 0, 255);
            vertex(matrix, consumer, (float)p1.x, (float)p1.y, (float)p1.z, u0, 1.0F, r, g, b, a0, light);
            vertex(matrix, consumer, (float)p2.x, (float)p2.y, (float)p2.z, u1, 1.0F, r, g, b, a1, light);
            vertex(matrix, consumer, (float)p3.x, (float)p3.y, (float)p3.z, u1, 0.0F, r, g, b, a1, light);
            vertex(matrix, consumer, (float)p4.x, (float)p4.y, (float)p4.z, u0, 0.0F, r, g, b, a0, light);
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
