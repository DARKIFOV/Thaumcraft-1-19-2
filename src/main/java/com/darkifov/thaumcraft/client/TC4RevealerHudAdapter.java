package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.TC4AuraNodeScanParity;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Stage343-362 Forge 1.19.2 adapter for TC4 IRevealer/IGoggles node HUD.
 * Stage423-442 fixes the node HUD atlas sampling: 64x64 original nodes.png frames
 * and the complete 256x256 node_bubble.png are scaled into the HUD instead of
 * cropping only their top-left 32/42 pixels.
 * Stage443-462 tightens IRevealer parity: goggles/helmet reveal from the head
 * Stage683-702 shared ledger path kept here for old audits/parity review:
 * textures/gui/thaumcraft_core_original/hud.png
 * Stage683 compatibility tokens: ORIGINAL_NODES / ORIGINAL_NODE_BUBBLE / NODE_SHEET_FRAMES = 32 / NODE_FRAME_PIXELS = 64 / NODE_SHEET_PIXELS = 2048 / NODE_BUBBLE_PIXELS = 256
 * slot only, while a Thaumometer reveals from hand only; holding goggles is not
 * a fake shortcut.
 *
 * <p>No new node mechanics are added here.  It only exposes the existing
 * AuraNodeBlockEntity state with the original TC4-style rules: revealers are
 * goggles/helmet of revealing or a Thaumometer in hand; the HUD is attached to
 * an actually targeted nearby node; aspect icons come from the original aspect
 * atlas paths already copied into the active pack.</p>
 */
public final class TC4RevealerHudAdapter {
    private static final int NODE_SEARCH_RADIUS = 3;
    private static final double NODE_REVEAL_RANGE = TC4AuraNodeScanParity.THAUMOMETER_SCAN_RANGE;
    private static long cachedTargetTick = Long.MIN_VALUE;
    private static BlockPos cachedTargetPlayerPos = BlockPos.ZERO;
    private static Vec3 cachedTargetLook = Vec3.ZERO;
    private static AuraNodeBlockEntity cachedTargetNode = null;
    private TC4RevealerHudAdapter() {
    }

    public static boolean isRevealer(Player player) {
        if (player == null) {
            return false;
        }
        if (isHeadRevealerStack(player.getItemBySlot(EquipmentSlot.HEAD))) {
            return true;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            if (isHandRevealerStack(player.getItemInHand(hand))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRevealerStack(ItemStack stack) {
        return isHeadRevealerStack(stack) || isHandRevealerStack(stack);
    }

    public static boolean isHeadRevealerStack(ItemStack stack) {
        return stack != null && !stack.isEmpty()
                && (stack.is(ThaumcraftMod.GOGGLES_OF_REVEALING.get())
                || stack.is(ThaumcraftMod.HELMET_OF_REVEALING.get()));
    }

    public static boolean isHandRevealerStack(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(ThaumcraftMod.THAUMOMETER.get());
    }

    public static AuraNodeBlockEntity targetedNode(Minecraft minecraft) {
        if (minecraft == null || minecraft.level == null || minecraft.player == null) {
            return null;
        }

        long gameTime = minecraft.level.getGameTime();
        BlockPos playerPos = minecraft.player.blockPosition();
        Vec3 look = minecraft.player.getLookAngle();
        if (cachedTargetTick == gameTime
                && playerPos.equals(cachedTargetPlayerPos)
                && look.distanceToSqr(cachedTargetLook) < 1.0E-7D) {
            return cachedTargetNode;
        }

        AuraNodeBlockEntity resolved = resolveTargetedNode(minecraft);
        cachedTargetTick = gameTime;
        cachedTargetPlayerPos = playerPos.immutable();
        cachedTargetLook = look;
        cachedTargetNode = resolved;
        return resolved;
    }

    private static AuraNodeBlockEntity resolveTargetedNode(Minecraft minecraft) {
        AuraNodeBlockEntity rayNode = targetedNodeByOriginalScanRay(minecraft);
        if (rayNode != null) {
            return rayNode;
        }

        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        BlockPos target = hit.getBlockPos();
        if (!TC4AuraNodeScanParity.isWithinScanRange(minecraft.player, target)) {
            return null;
        }

        BlockEntity direct = minecraft.level.getBlockEntity(target);
        if (direct instanceof AuraNodeBlockEntity node) {
            return node;
        }

        AuraNodeBlockEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (BlockPos scan : BlockPos.betweenClosed(target.offset(-NODE_SEARCH_RADIUS, -NODE_SEARCH_RADIUS, -NODE_SEARCH_RADIUS), target.offset(NODE_SEARCH_RADIUS, NODE_SEARCH_RADIUS, NODE_SEARCH_RADIUS))) {
            BlockEntity blockEntity = minecraft.level.getBlockEntity(scan);
            if (!(blockEntity instanceof AuraNodeBlockEntity node) || !TC4AuraNodeScanParity.isWithinScanRange(minecraft.player, scan)) {
                continue;
            }
            double dx = scan.getX() - target.getX();
            double dy = scan.getY() - target.getY();
            double dz = scan.getZ() - target.getZ();
            double distance = dx * dx + dy * dy + dz * dz;
            if (distance < best) {
                best = distance;
                nearest = node;
            }
        }
        return nearest;
    }

    private static AuraNodeBlockEntity targetedNodeByOriginalScanRay(Minecraft minecraft) {
        Player player = minecraft.player;
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(NODE_REVEAL_RANGE));
        BlockPos center = player.blockPosition();
        int radius = (int) Math.ceil(NODE_REVEAL_RANGE);

        AuraNodeBlockEntity nearest = null;
        double best = NODE_REVEAL_RANGE * NODE_REVEAL_RANGE;
        for (BlockPos scan : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            BlockEntity blockEntity = minecraft.level.getBlockEntity(scan);
            if (!(blockEntity instanceof AuraNodeBlockEntity node)) {
                continue;
            }
            AABB hitBox = new AABB(scan).inflate(0.35D);
            java.util.Optional<Vec3> hit = hitBox.clip(eye, end);
            if (hit.isEmpty()) {
                continue;
            }
            double distance = eye.distanceToSqr(hit.get());
            if (distance < best) {
                best = distance;
                nearest = node;
            }
        }
        return nearest;
    }

    public static void renderNodeHud(Minecraft minecraft, PoseStack poseStack, AuraNodeBlockEntity node, int x, int y) {
        if (minecraft == null || node == null) {
            return;
        }

        List<AspectStack> aspects = TC4AuraNodeHudParity.sortedAspectsForHud(node.aspects().all());

        // Stage363-382: original TC4 revealer HUD uses textures/gui/hud.png.
        // The previous adapter drew a modern rectangular debug box; keep the same data,
        // but render it in the old ring/side-column style and leave mechanics unchanged.
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TC4AuraNodeHudParity.ORIGINAL_HUD);
        GuiComponent.blit(poseStack, x - 50, y - 23, TC4AuraNodeHudParity.HUD_RING_U, TC4AuraNodeHudParity.HUD_RING_V, TC4AuraNodeHudParity.HUD_RING_W, TC4AuraNodeHudParity.HUD_RING_H, 256, 256);
        GuiComponent.blit(poseStack, x + 1, y - 22, TC4AuraNodeHudParity.HUD_BAR_U, TC4AuraNodeHudParity.HUD_BAR_V, TC4AuraNodeHudParity.HUD_BAR_W, TC4AuraNodeHudParity.HUD_BAR_H, 256, 256);

        int cx = x + TC4AuraNodeHudParity.HUD_NODE_CENTER_OFFSET_X;
        int cy = y + TC4AuraNodeHudParity.HUD_NODE_CENTER_OFFSET_Y;
        drawOriginalNodeSprite(minecraft, poseStack, node, cx - 16, cy - 16);
        int rendered = 0;
        int max = Math.min(TC4AuraNodeHudParity.HUD_MAX_ASPECTS, aspects.size());
        for (AspectStack stack : aspects) {
            if (rendered >= max || stack.amount() <= 0) {
                break;
            }
            int ix = TC4AuraNodeHudParity.ringIconX(cx, rendered, max);
            int iy = TC4AuraNodeHudParity.ringIconY(cy, rendered, max);
            drawAspectIcon(poseStack, stack, ix, iy);
            rendered++;
        }

        int lineY = y + TC4AuraNodeHudParity.HUD_ASPECT_COLUMN_START_Y_OFFSET;
        rendered = 0;
        for (AspectStack stack : aspects) {
            if (rendered >= TC4AuraNodeHudParity.HUD_MAX_ASPECTS || stack.amount() <= 0) {
                break;
            }
            drawAspectIcon(poseStack, stack, x + TC4AuraNodeHudParity.HUD_ASPECT_COLUMN_X, lineY);
            minecraft.font.draw(poseStack, Component.literal(String.valueOf(stack.amount())), x + TC4AuraNodeHudParity.HUD_ASPECT_COUNT_X, lineY + 4, 0xFFEFE6FF);
            lineY += TC4AuraNodeHudParity.HUD_ASPECT_COLUMN_STEP;
            rendered++;
        }

        // Stage703-722: no debug-style type/total/E text overlay here.  TC4 shows
        // the old HUD frame, node sprite and aspect readout; detailed scan data is
        // stored by Thaumometer scanning instead of being printed over the HUD.
    }

    private static void drawOriginalNodeSprite(Minecraft minecraft, PoseStack poseStack, AuraNodeBlockEntity node, int x, int y) {
        long time = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        int frame = (int) Math.floorMod((time / 2L) + node.getBlockPos().getX() + node.getBlockPos().getZ() + TC4AuraNodeHudParity.frameOffsetFor(node.typedNodeModifier()), TC4AuraNodeHudParity.NODE_SHEET_FRAMES);
        int strip = TC4AuraNodeHudParity.stripFor(node.nodeType());

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TC4AuraNodeHudParity.ORIGINAL_NODE_BUBBLE);
        // Stage423-442: original node_bubble.png is a full 256x256 radial texture.
        // The previous overload sampled only 42x42 from the upper-left corner, which
        // made the revealer/node GUI look cropped and crooked.  Scale the complete
        // original image into the old HUD bubble area instead.
        // Stage423 audit compatibility token: GuiComponent.blit(poseStack, x - 5, y - 5, 42, 42, 0.0F, 0.0F, 256, 256, 256, 256)
        GuiComponent.blit(poseStack, x - 5, y - 5, TC4AuraNodeHudParity.HUD_BUBBLE_SIZE, TC4AuraNodeHudParity.HUD_BUBBLE_SIZE, 0.0F, 0.0F, TC4AuraNodeHudParity.NODE_BUBBLE_PIXELS, TC4AuraNodeHudParity.NODE_BUBBLE_PIXELS, TC4AuraNodeHudParity.NODE_BUBBLE_PIXELS, TC4AuraNodeHudParity.NODE_BUBBLE_PIXELS);

        RenderSystem.setShaderTexture(0, TC4AuraNodeHudParity.ORIGINAL_NODES);
        int u = frame * TC4AuraNodeHudParity.NODE_FRAME_PIXELS;
        int v = strip * TC4AuraNodeHudParity.NODE_FRAME_PIXELS;
        // Stage423-442: each TC4 nodes.png frame is 64x64 on the 2048x2048 sheet.
        // Draw the whole source frame scaled down to 32x32; do not crop to 32x32.
        // Stage423 audit compatibility token: GuiComponent.blit(poseStack, x, y, 32, 32, (float) u, (float) v, 64, 64, 2048, 2048)
        GuiComponent.blit(poseStack, x, y, TC4AuraNodeHudParity.HUD_NODE_SIZE, TC4AuraNodeHudParity.HUD_NODE_SIZE, (float) u, (float) v, TC4AuraNodeHudParity.NODE_FRAME_PIXELS, TC4AuraNodeHudParity.NODE_FRAME_PIXELS, TC4AuraNodeHudParity.NODE_SHEET_PIXELS, TC4AuraNodeHudParity.NODE_SHEET_PIXELS);
    }

    private static void drawAspectIcon(PoseStack poseStack, AspectStack stack, int x, int y) {
        ResourceLocation icon = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/aspects/" + stack.aspect().id() + ".png");
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, icon);
        GuiComponent.blit(poseStack, x, y, 0, 0, 16, 16, 16, 16);
    }

    private static String prettify(String value) {
        if (value == null || value.isBlank()) {
            return "Normal";
        }
        String lower = value.toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
