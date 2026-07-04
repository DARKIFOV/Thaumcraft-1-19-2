package com.darkifov.thaumcraft.thaumicenergistics;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EssentiaDriveBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public final class ThaumicAeGrid {
    public static final int DEFAULT_RADIUS = 24;
    public static final int CHANNELS_PER_CONTROLLER = 32;
    public static final int CHANNELS_PER_CABLE_ONLY_NETWORK = 8;
    public static final int ENERGY_PER_ACCEPTOR = 4096;
    public static final int PASSIVE_CONTROLLER_ENERGY = 1024;

    private ThaumicAeGrid() {
    }

    public static ThaumicAeGridReport scan(Level level, BlockPos center) {
        int controllers = 0;
        int cables = 0;
        int machines = 0;
        int energyAcceptors = 0;
        int craftingCpus = 0;

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-DEFAULT_RADIUS, -DEFAULT_RADIUS, -DEFAULT_RADIUS), center.offset(DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS))) {
            Block block = level.getBlockState(pos).getBlock();

            if (block == ThaumcraftMod.THAUMIC_ME_CONTROLLER.get()) {
                controllers++;
                continue;
            }

            if (block == ThaumcraftMod.THAUMIC_ME_CABLE.get()) {
                cables++;
                continue;
            }

            if (block == ThaumcraftMod.THAUMIC_ENERGY_ACCEPTOR.get()) {
                energyAcceptors++;
                machines++;
                continue;
            }

            if (block == ThaumcraftMod.THAUMIC_CRAFTING_CPU.get()) {
                craftingCpus++;
                machines++;
                continue;
            }

            if (block == ThaumcraftMod.ESSENTIA_TERMINAL.get()
                    || block == ThaumcraftMod.ESSENTIA_DRIVE.get()
                    || block == ThaumcraftMod.ESSENTIA_INTERFACE.get()
                    || block == ThaumcraftMod.ESSENTIA_IMPORT_BUS.get()
                    || block == ThaumcraftMod.ESSENTIA_EXPORT_BUS.get()
                    || block == ThaumcraftMod.ESSENTIA_STORAGE_BUS.get()
                    || block == ThaumcraftMod.ESSENTIA_STORAGE_MONITOR.get()
                    || block == ThaumcraftMod.ARCANE_ASSEMBLER.get()
                    || block == ThaumcraftMod.ARCANE_PATTERN_ENCODER.get()
                    || block == ThaumcraftMod.ARCANE_PATTERN_PROVIDER.get()
                    || block == ThaumcraftMod.ESSENTIA_PROVIDER.get()
                    || block == ThaumcraftMod.INFUSION_PROVIDER.get()
                    || block == ThaumcraftMod.KNOWLEDGE_INSCRIBER.get()
                    || block == ThaumcraftMod.ARCANE_CRAFTING_TERMINAL.get()
                    || block == ThaumcraftMod.ESSENTIA_LEVEL_EMITTER.get()
                    || block == ThaumcraftMod.ESSENTIA_CONVERSION_MONITOR.get()
                    || block == ThaumcraftMod.VIS_INTERFACE.get()) {
                machines++;
            }
        }

        int channelBudget = controllers > 0
                ? controllers * CHANNELS_PER_CONTROLLER
                : Math.max(CHANNELS_PER_CABLE_ONLY_NETWORK, cables);
        int usedChannels = Math.min(channelBudget, Math.max(0, machines));
        int energyCapacity = controllers * PASSIVE_CONTROLLER_ENERGY + energyAcceptors * ENERGY_PER_ACCEPTOR;
        int stored = energyCapacity;

        Map<Aspect, Integer> essentia = ThaumicEnergisticsNetwork.totals(level, center);
        return new ThaumicAeGridReport(controllers, cables, machines, usedChannels, channelBudget, stored, energyCapacity, craftingCpus, essentia);
    }

    public static boolean canScheduleCraft(Level level, BlockPos center, Player player) {
        ThaumicAeGridReport report = scan(level, center);
        if (!report.hasController()) {
            player.displayClientMessage(Component.literal("AE2 parity grid: нужен Thaumic ME Controller.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (!report.hasEnergy()) {
            player.displayClientMessage(Component.literal("AE2 parity grid: нет энергии. Поставь Thaumic Energy Acceptor.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (!report.hasFreeChannels() && report.channelsTotal() > 0) {
            player.displayClientMessage(Component.literal("AE2 parity grid: каналы заняты " + report.channelsUsed() + "/" + report.channelsTotal()).withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (!report.hasCraftingCpu()) {
            player.displayClientMessage(Component.literal("AE2 parity grid: нужен Thaumic Crafting CPU.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        return true;
    }

    public static void sendStatus(Level level, BlockPos center, Player player) {
        ThaumicAeGridReport report = scan(level, center);

        player.displayClientMessage(Component.literal("Thaumic AE Grid | " + report.summary()).withStyle(ChatFormatting.AQUA), false);

        if (!report.hasController()) {
            player.displayClientMessage(Component.literal("Нет Controller: сеть работает как small cable-only grid.").withStyle(ChatFormatting.GRAY), false);
        }

        if (!report.hasCraftingCpu()) {
            player.displayClientMessage(Component.literal("Нет Crafting CPU: автокрафт работает только в простом режиме Assembler.").withStyle(ChatFormatting.YELLOW), false);
        }

        for (Map.Entry<Aspect, Integer> entry : report.essentia().entrySet()) {
            player.displayClientMessage(Component.literal(entry.getKey().displayName() + ": " + entry.getValue()).withStyle(entry.getKey().color()), false);
        }
    }

    public static int injectEnergyFromNitor(Level level, BlockPos center, int amount) {
        // Standalone rebuild: energy is virtual and controller/acceptor capacity-based.
        // Method is kept as an AE2 API replacement seam for future real FE/AE power integration.
        return Math.max(0, amount);
    }
}
