package com.darkifov.thaumcraft.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class EssentiaUpgradeCardItem extends Item {
    public enum Mode {
        SPEED,
        ADVANCED_SPEED,
        ACCELERATION,
        COPROCESSOR,
        FUZZY
    }

    private final Mode mode;

    public EssentiaUpgradeCardItem(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    public Mode mode() {
        return mode;
    }

    public int transferBonus() {
        return switch (mode) {
            case SPEED -> 64;
            case ADVANCED_SPEED -> 192;
            case ACCELERATION -> 448;
            case COPROCESSOR -> 128;
            case FUZZY -> 0;
        };
    }

    public int scanBonus() {
        return switch (mode) {
            case SPEED -> 1;
            case ADVANCED_SPEED -> 2;
            case ACCELERATION -> 4;
            case COPROCESSOR -> 3;
            case FUZZY -> 0;
        };
    }

    public String description() {
        return switch (mode) {
            case SPEED -> "Ускоряет Import/Export/Interface на +64 essentia за действие.";
            case ADVANCED_SPEED -> "Улучшенная карта скорости: +192 essentia за действие.";
            case ACCELERATION -> "Максимальная карта ускорения: +448 essentia за действие.";
            case COPROCESSOR -> "Карта сопроцессора: ускоряет сканирование и будущий автокрафт.";
            case FUZZY -> "Карта нечёткого режима: база под будущие fuzzy-правила aspect/recipes.";
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(Component.literal(description()).withStyle(ChatFormatting.GRAY));

        if (transferBonus() > 0) {
            tooltip.add(Component.literal("Transfer bonus: +" + transferBonus()).withStyle(ChatFormatting.AQUA));
        }

        if (scanBonus() > 0) {
            tooltip.add(Component.literal("Scan / autocraft bonus: +" + scanBonus()).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
