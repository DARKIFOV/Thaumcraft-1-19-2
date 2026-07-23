package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.item.ElementalShovelItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Sneak-only line-box preview replacing TC4's IArchitect overlay. */
@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ElementalShovelPreviewClient {
    private ElementalShovelPreviewClient() {}

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !minecraft.player.isShiftKeyDown()
                || !(minecraft.hitResult instanceof BlockHitResult hit)
                || minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        ItemStack stack = heldShovel(minecraft);
        if (stack.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        for (var pos : ElementalShovelItem.previewPositions(minecraft.player, hit, stack)) {
            LevelRenderer.renderLineBox(poseStack, lines, new AABB(pos).inflate(0.003D),
                    0.55F, 0.35F, 0.12F, 0.85F);
        }
        buffers.endBatch(RenderType.lines());
        poseStack.popPose();
    }

    private static ItemStack heldShovel(Minecraft minecraft) {
        ItemStack main = minecraft.player.getMainHandItem();
        if (main.getItem() instanceof ElementalShovelItem) return main;
        ItemStack off = minecraft.player.getOffhandItem();
        return off.getItem() instanceof ElementalShovelItem ? off : ItemStack.EMPTY;
    }
}
