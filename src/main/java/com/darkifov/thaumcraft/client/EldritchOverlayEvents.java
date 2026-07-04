package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EldritchPortalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EldritchOverlayEvents {
    private EldritchOverlayEvents() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null || minecraft.hitResult == null) {
            return;
        }

        if (minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = ((BlockHitResult) minecraft.hitResult).getBlockPos();
        BlockEntity blockEntity = minecraft.level.getBlockEntity(pos);

        if (!(blockEntity instanceof EldritchPortalBlockEntity portal)) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Font font = minecraft.font;
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int x = screenWidth / 2 - 105;
        int y = 96;
        int w = 210;
        int h = 34;

        net.minecraft.client.gui.GuiComponent.fill(poseStack, x, y, x + w, y + h, 0xAA12081E);
        String line1 = portal.encounterActive() ? "Eldritch Arena Active" : "Eldritch Portal";
        String line2 = "Stability " + portal.stability() + " | Cooldown " + portal.cooldown() + " | Wave " + portal.wave();

        font.draw(poseStack, Component.literal(line1), x + 8, y + 7, 0xFFE8C6FF);
        font.draw(poseStack, Component.literal(line2), x + 8, y + 20, 0xFFBFAFEF);
    }
}
