package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(modid = ThaumcraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class HelmetRevealingOverlayEvents {
    private HelmetRevealingOverlayEvents() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || minecraft.options.hideGui) {
            return;
        }

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        boolean revealer = helmet.is(ThaumcraftMod.HELMET_OF_REVEALING.get()) || helmet.is(ThaumcraftMod.GOGGLES_OF_REVEALING.get());
        if (!revealer || minecraft.level == null || !(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        if (!(minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof AuraNodeBlockEntity node)) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        int x = minecraft.getWindow().getGuiScaledWidth() / 2 + 12;
        int y = minecraft.getWindow().getGuiScaledHeight() / 2 + 10;
        renderNodeHud(minecraft, poseStack, node, x, y);
    }

    private static void renderNodeHud(Minecraft minecraft, PoseStack poseStack, AuraNodeBlockEntity node, int x, int y) {
        String header = "§bAura Node";
        if (!"NORMAL".equals(node.nodeModifier())) {
            header += " §7" + prettify(node.nodeModifier());
        }
        if (!"NORMAL".equals(node.nodeType())) {
            header += " §7" + prettify(node.nodeType());
        }
        minecraft.font.draw(poseStack, Component.literal(header), x, y, 0xE0E8FFFF);

        int total = node.aspects().totalAmount();
        int base = Math.max(total, node.baseAspects().totalAmount());
        minecraft.font.draw(poseStack, Component.literal("§7Vis: §f" + total + "§7/" + base), x, y + 10, 0xE0FFFFFF);

        List<AspectStack> aspects = new ArrayList<>(node.aspects().all());
        aspects.sort(Comparator.comparingInt(AspectStack::amount).reversed());
        int line = 0;
        StringBuilder row = new StringBuilder();
        for (AspectStack stack : aspects) {
            if (stack.amount() <= 0) {
                continue;
            }
            String part = stack.aspect().displayName() + " " + stack.amount();
            if (row.length() + part.length() > 28) {
                minecraft.font.draw(poseStack, Component.literal("§f" + row), x, y + 22 + line * 10, stack.aspect().argbColor());
                row.setLength(0);
                line++;
            }
            if (row.length() > 0) {
                row.append("  ");
            }
            row.append(part);
            if (line >= 3) {
                break;
            }
        }
        if (row.length() > 0 && line < 4) {
            minecraft.font.draw(poseStack, Component.literal("§f" + row), x, y + 22 + line * 10, 0xE0FFFFFF);
            line++;
        }

        minecraft.font.draw(poseStack, Component.literal("§8TC4 revealer adapter: no fake research/warp HUD"), x, y + 22 + line * 10, 0xA0C8C8C8);
    }

    private static String prettify(String value) {
        if (value == null || value.isBlank()) {
            return "Normal";
        }
        String lower = value.toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
