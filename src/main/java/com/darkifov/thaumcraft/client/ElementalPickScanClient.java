package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/** Client-only five-second ore/fluid listening overlay for the Pickaxe of the Core. */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ElementalPickScanClient {
    private static List<ScanTarget> targets = List.of();
    private static long startTick;
    private static long endTick;

    private ElementalPickScanClient() {}

    public static void start(BlockPos origin, int radius, int durationTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        List<ScanTarget> found = new ArrayList<>();
        BlockPos min = origin.offset(-radius, -radius, -radius);
        BlockPos max = origin.offset(radius, radius, radius);
        for (BlockPos mutable : BlockPos.betweenClosed(min, max)) {
            BlockPos pos = mutable.immutable();
            double distanceSquared = pos.distSqr(origin);
            if (distanceSquared > radius * radius) {
                continue;
            }
            BlockState state = minecraft.level.getBlockState(pos);
            ScanKind kind = null;
            float aspectValue = 0.0F;
            if (state.getFluidState().is(FluidTags.WATER)) {
                kind = ScanKind.WATER;
            } else if (state.getFluidState().is(FluidTags.LAVA)) {
                kind = ScanKind.LAVA;
            } else if (state.is(Tags.Blocks.ORES)) {
                kind = ScanKind.ORE;
                aspectValue = AspectDatabase.getAspectsForBlock(state).totalAmount();
            }
            if (kind != null) {
                found.add(new ScanTarget(pos, kind, aspectValue, distanceSquared));
            }
        }
        targets = List.copyOf(found);
        startTick = minecraft.level.getGameTime();
        endTick = startTick + Math.max(1, durationTicks);
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || targets.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            clear();
            return;
        }
        long now = minecraft.level.getGameTime();
        if (now >= endTick) {
            clear();
            return;
        }

        float partial = event.getPartialTick();
        float elapsed = now + partial - startTick;
        float remaining = endTick - (now + partial);
        float fade = Math.min(1.0F, elapsed / 30.0F) * Math.min(1.0F, remaining / 5.0F);
        if (fade <= 0.0F) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        for (ScanTarget target : targets) {
            float distanceFade = Math.max(0.0F, 1.0F - (float) target.distanceSquared() / 64.0F);
            float strength = fade * distanceFade;
            if (target.kind() == ScanKind.ORE) {
                strength *= Math.min(1.5F, Math.max(0.15F, target.aspectValue() / 7.0F));
            }
            if (strength <= 0.02F) continue;
            float r = target.kind() == ScanKind.LAVA ? 1.0F : target.kind() == ScanKind.WATER ? 0.2F : 1.0F;
            float g = target.kind() == ScanKind.LAVA ? 0.28F : target.kind() == ScanKind.WATER ? 0.55F : 0.86F;
            float b = target.kind() == ScanKind.LAVA ? 0.05F : target.kind() == ScanKind.WATER ? 1.0F : 0.18F;
            AABB box = new AABB(target.pos()).inflate(0.003D);
            LevelRenderer.renderLineBox(poseStack, lines, box, r, g, b, Math.min(1.0F, strength));
        }
        buffers.endBatch(RenderType.lines());
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    private static void clear() {
        targets = List.of();
        startTick = 0L;
        endTick = 0L;
    }

    private enum ScanKind { WATER, LAVA, ORE }
    private record ScanTarget(BlockPos pos, ScanKind kind, float aspectValue, double distanceSquared) {}
}
