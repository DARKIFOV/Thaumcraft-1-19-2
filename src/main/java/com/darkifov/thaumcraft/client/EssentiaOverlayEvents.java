package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AlchemicalFurnaceBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.block.EssentiaValveBlock;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
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
public final class EssentiaOverlayEvents {
    private EssentiaOverlayEvents() {
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

        String line1 = null;
        String line2 = null;

        if (blockEntity instanceof AlchemicalFurnaceBlockEntity furnace) {
            int pct = furnace.burnDuration() <= 0 ? 0 : Math.min(100, furnace.burnProgress() * 100 / furnace.burnDuration());
            line1 = "Alchemical Furnace";
            line2 = "Stored " + furnace.aspects().totalAmount() + "/128 | Fuel " + furnace.fuelTime() + " | Burn " + pct + "%";
        } else if (blockEntity instanceof EssentiaJarBlockEntity jar) {
            String filter = jar.filterAspect() == null ? "none" : jar.filterAspect().displayName();
            boolean filtered = minecraft.level.getBlockState(pos).is(ThaumcraftMod.FILTERED_ESSENTIA_JAR.get());
            boolean voidJar = minecraft.level.getBlockState(pos).is(ThaumcraftMod.VOID_ESSENTIA_JAR.get());
            line1 = voidJar ? "Void Essentia Jar" : filtered ? "Filtered Essentia Jar" : "Essentia Jar";
            line2 = "Stored " + jar.aspects().totalAmount() + "/64 | Filter " + filter + " | Suction " + (voidJar ? "64" : filtered ? "48" : "32");
        } else if (blockEntity instanceof EssentiaTubeBlockEntity tube) {
            boolean valve = minecraft.level.getBlockState(pos).is(ThaumcraftMod.ESSENTIA_VALVE.get());
            line1 = valve ? "Essentia Valve" : "Essentia Tube";
            line2 = valve ? ("Network " + tube.networkSize() + " | " + (EssentiaValveBlock.isOpen(minecraft.level, pos) ? "OPEN" : "CLOSED")) : "Network " + tube.networkSize() + " | Suction N32/F48/V64";
        }

        if (line1 == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Font font = minecraft.font;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int x = screenWidth / 2 - 100;
        int y = 62;
        int w = 200;
        int h = 30;

        net.minecraft.client.gui.GuiComponent.fill(poseStack, x, y, x + w, y + h, 0xAA160F20);
        font.draw(poseStack, Component.literal(line1), x + 8, y + 6, 0xFFE8D4FF);
        font.draw(poseStack, Component.literal(line2), x + 8, y + 18, 0xFFBFAFEF);
    }
}
