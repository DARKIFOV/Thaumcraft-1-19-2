package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.thaumicenergistics.ThaumicEnergisticsRecipeBook;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class EncodedEssentiaPatternItem extends Item {
    private static final String TAG_PATTERN_TYPE = "PatternType";
    private static final String TAG_RECIPE_TARGET = "RecipeTarget";

    public enum PatternType {
        EMPTY,
        ARCANE_WORKBENCH,
        CRUCIBLE,
        INFUSION
    }

    public EncodedEssentiaPatternItem(Properties properties) {
        super(properties);
    }

    public static PatternType getPatternType(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_PATTERN_TYPE)) {
            return PatternType.EMPTY;
        }

        try {
            return PatternType.valueOf(stack.getTag().getString(TAG_PATTERN_TYPE));
        } catch (IllegalArgumentException exception) {
            return PatternType.EMPTY;
        }
    }

    public static void setPatternType(ItemStack stack, PatternType type) {
        stack.getOrCreateTag().putString(TAG_PATTERN_TYPE, type.name());
    }

    public static PatternType next(PatternType current) {
        PatternType[] values = PatternType.values();
        return values[(current.ordinal() + 1) % values.length];
    }

    public static void cycle(ItemStack stack) {
        setPatternType(stack, next(getPatternType(stack)));
    }

    public static String getRecipeTarget(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_RECIPE_TARGET)) {
            return ThaumicEnergisticsRecipeBook.firstId();
        }

        String id = stack.getTag().getString(TAG_RECIPE_TARGET);
        return id == null || id.isBlank() ? ThaumicEnergisticsRecipeBook.firstId() : id;
    }

    public static void setRecipeTarget(ItemStack stack, String target) {
        stack.getOrCreateTag().putString(TAG_RECIPE_TARGET, target == null ? "" : target);
    }

    public static void cycleRecipeTarget(ItemStack stack) {
        setRecipeTarget(stack, ThaumicEnergisticsRecipeBook.nextId(getRecipeTarget(stack)));
    }

    public static String displayName(PatternType type) {
        return switch (type) {
            case EMPTY -> "Empty";
            case ARCANE_WORKBENCH -> "Arcane Workbench";
            case CRUCIBLE -> "Crucible";
            case INFUSION -> "Infusion";
        };
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                cycleRecipeTarget(stack);
                player.displayClientMessage(Component.literal("Encoded Pattern target: " + ThaumicEnergisticsRecipeBook.displayName(getRecipeTarget(stack))).withStyle(ChatFormatting.GOLD), false);
            } else {
                cycle(stack);
                player.displayClientMessage(Component.literal("Encoded Pattern mode: " + displayName(getPatternType(stack))).withStyle(ChatFormatting.LIGHT_PURPLE), false);
            }

            player.displayClientMessage(Component.literal("ПКМ по Arcane Assembler: проверить. Shift+ПКМ по Assembler: автокрафт.").withStyle(ChatFormatting.GRAY), false);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getPatternType(stack) != PatternType.EMPTY || !getRecipeTarget(stack).isBlank();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(Component.literal("Pattern mode: " + displayName(getPatternType(stack))).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("Target: " + ThaumicEnergisticsRecipeBook.displayName(getRecipeTarget(stack))).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Shift+ПКМ: выбрать цель автокрафта.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Assembler/Provider: проверка и автокрафт через item + essentia.").withStyle(ChatFormatting.AQUA));
    }
}
