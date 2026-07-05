package com.darkifov.thaumcraft.thaumicenergistics;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.blockentity.EssentiaDriveBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ThaumicEnergisticsNetwork {
    public static final int DEFAULT_RADIUS = 16;

    private ThaumicEnergisticsNetwork() {
    }

    public static List<EssentiaDriveBlockEntity> drives(Level level, BlockPos center) {
        return drives(level, center, DEFAULT_RADIUS);
    }

    public static List<EssentiaDriveBlockEntity> drives(Level level, BlockPos center, int radius) {
        List<EssentiaDriveBlockEntity> result = new ArrayList<>();

        if (level == null || center == null) {
            return result;
        }

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof EssentiaDriveBlockEntity drive) {
                result.add(drive);
            }
        }

        return result;
    }

    public static List<EssentiaJarBlockEntity> jars(Level level, BlockPos center) {
        return jars(level, center, DEFAULT_RADIUS);
    }

    public static List<EssentiaJarBlockEntity> jars(Level level, BlockPos center, int radius) {
        List<EssentiaJarBlockEntity> result = new ArrayList<>();

        if (level == null || center == null) {
            return result;
        }

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof EssentiaJarBlockEntity jar) {
                result.add(jar);
            }
        }

        return result;
    }

    public static List<Container> containers(Level level, BlockPos center, int radius) {
        List<Container> result = new ArrayList<>();

        if (level == null || center == null) {
            return result;
        }

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof Container container && !(blockEntity instanceof EssentiaDriveBlockEntity)) {
                result.add(container);
            }
        }

        return result;
    }

    public static Map<Aspect, Integer> totals(Level level, BlockPos center) {
        EnumMap<Aspect, Integer> totals = new EnumMap<>(Aspect.class);

        for (EssentiaDriveBlockEntity drive : drives(level, center)) {
            for (Map.Entry<Aspect, Integer> entry : drive.totals().entrySet()) {
                totals.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }

        for (EssentiaJarBlockEntity jar : jars(level, center)) {
            Aspect aspect = jar.aspects().firstAspect();

            if (aspect != null) {
                int amount = jar.aspects().get(aspect);

                if (amount > 0) {
                    totals.merge(aspect, amount, Integer::sum);
                }
            }
        }

        return totals;
    }

    public static int amountOf(Level level, BlockPos center, Aspect aspect) {
        return totals(level, center).getOrDefault(aspect, 0);
    }

    public static boolean hasAspects(Level level, BlockPos center, Map<Aspect, Integer> costs) {
        if (costs == null || costs.isEmpty()) {
            return true;
        }

        Map<Aspect, Integer> totals = totals(level, center);

        for (Map.Entry<Aspect, Integer> entry : costs.entrySet()) {
            if (totals.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    public static int insert(Level level, BlockPos center, Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            return 0;
        }

        int moved = 0;

        for (EssentiaDriveBlockEntity drive : drives(level, center)) {
            moved += drive.insertEssentia(aspect, amount - moved);

            if (moved >= amount) {
                break;
            }
        }

        return moved;
    }

    public static int extract(Level level, BlockPos center, Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            return 0;
        }

        int moved = 0;

        for (EssentiaDriveBlockEntity drive : drives(level, center)) {
            moved += drive.extractEssentia(aspect, amount - moved);

            if (moved >= amount) {
                break;
            }
        }

        for (EssentiaJarBlockEntity jar : jars(level, center)) {
            if (moved >= amount) {
                break;
            }

            Aspect jarAspect = jar.aspects().firstAspect();

            if (jarAspect != aspect) {
                continue;
            }

            int removed = jar.aspects().removeUpTo(aspect, amount - moved);

            if (removed > 0) {
                jar.setChangedAndSync();
                moved += removed;
            }
        }

        return moved;
    }

    public static boolean extractAll(Level level, BlockPos center, Map<Aspect, Integer> costs) {
        if (!hasAspects(level, center, costs)) {
            return false;
        }

        for (Map.Entry<Aspect, Integer> entry : costs.entrySet()) {
            int moved = extract(level, center, entry.getKey(), entry.getValue());

            if (moved < entry.getValue()) {
                insert(level, center, entry.getKey(), entry.getValue() - moved);
                return false;
            }
        }

        return true;
    }

    public static int importNearbyJars(Level level, BlockPos center, int limit) {
        int moved = 0;

        for (EssentiaJarBlockEntity jar : jars(level, center, 6)) {
            Aspect aspect = jar.aspects().firstAspect();

            if (aspect == null) {
                continue;
            }

            int removed = jar.aspects().removeUpTo(aspect, Math.max(0, limit - moved));
            int inserted = insert(level, center, aspect, removed);

            if (inserted < removed) {
                jar.acceptFromTube(aspect, removed - inserted, false);
            }

            if (inserted > 0) {
                jar.setChangedAndSync();
                moved += inserted;
            }

            if (moved >= limit) {
                break;
            }
        }

        return moved;
    }

    public static void sendStatus(Level level, BlockPos center, Player player) {
        List<EssentiaDriveBlockEntity> drives = drives(level, center);
        List<EssentiaJarBlockEntity> jars = jars(level, center);
        Map<Aspect, Integer> totals = totals(level, center);

        player.displayClientMessage(Component.literal("Thaumic Energistics Network | drives: " + drives.size() + " | jars: " + jars.size()).withStyle(ChatFormatting.AQUA), false);

        if (totals.isEmpty()) {
            player.displayClientMessage(Component.literal("Сеть пустая или рядом нет цифрового хранилища.").withStyle(ChatFormatting.GRAY), false);
            return;
        }

        for (Map.Entry<Aspect, Integer> entry : totals.entrySet()) {
            player.displayClientMessage(Component.literal(entry.getKey().displayName() + ": " + entry.getValue()).withStyle(entry.getKey().color()), false);
        }
    }
}
