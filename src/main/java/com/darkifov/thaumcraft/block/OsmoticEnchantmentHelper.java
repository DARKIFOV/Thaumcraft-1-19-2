
package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.LinkedHashMap;
import java.util.Map;

public class OsmoticEnchantmentHelper {
    public enum Choice { UNBREAKING, EFFICIENCY, SHARPNESS, PROTECTION, POWER, FORTUNE }

    public static Component choiceName(Choice choice) {
        return switch (choice) {
            case UNBREAKING -> Component.literal("Unbreaking");
            case EFFICIENCY -> Component.literal("Efficiency");
            case SHARPNESS -> Component.literal("Sharpness");
            case PROTECTION -> Component.literal("Protection");
            case POWER -> Component.literal("Power");
            case FORTUNE -> Component.literal("Fortune");
        };
    }

    public static Enchantment enchantment(Choice choice) {
        return switch (choice) {
            case UNBREAKING -> Enchantments.UNBREAKING;
            case EFFICIENCY -> Enchantments.BLOCK_EFFICIENCY;
            case SHARPNESS -> Enchantments.SHARPNESS;
            case PROTECTION -> Enchantments.ALL_DAMAGE_PROTECTION;
            case POWER -> Enchantments.POWER_ARROWS;
            case FORTUNE -> Enchantments.BLOCK_FORTUNE;
        };
    }

    public static Aspect costAspect(Choice choice) {
        return switch (choice) {
            case UNBREAKING, FORTUNE -> Aspect.PRAECANTATIO;
            case EFFICIENCY -> Aspect.ORDO;
            case SHARPNESS -> Aspect.IGNIS;
            case PROTECTION -> Aspect.TERRA;
            case POWER -> Aspect.AER;
        };
    }

    public static int visCost(Choice choice, int nextLevel) {
        return switch (choice) {
            case UNBREAKING -> 10 + nextLevel * 4;
            case EFFICIENCY -> 8 + nextLevel * 3;
            case SHARPNESS -> 9 + nextLevel * 4;
            case PROTECTION -> 10 + nextLevel * 4;
            case POWER -> 8 + nextLevel * 3;
            case FORTUNE -> 18 + nextLevel * 8;
        };
    }

    public static int xpCost(Choice choice, int nextLevel) {
        return switch (choice) {
            case FORTUNE -> nextLevel + 1;
            case PROTECTION, SHARPNESS -> nextLevel;
            default -> Math.max(1, nextLevel);
        };
    }

    public static int maxLevel(Choice choice) {
        return switch (choice) {
            case UNBREAKING, FORTUNE -> 3;
            case EFFICIENCY, SHARPNESS, POWER -> 5;
            case PROTECTION -> 4;
        };
    }

    public static boolean canApply(Choice choice, ItemStack stack) {
        if (stack.isEmpty()) return false;
        return switch (choice) {
            case UNBREAKING -> stack.isDamageableItem();
            case EFFICIENCY -> stack.getItem() instanceof DiggerItem || stack.getItem() instanceof ShearsItem;
            case SHARPNESS -> stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
            case PROTECTION -> stack.getItem() instanceof ArmorItem;
            case POWER -> stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
            case FORTUNE -> stack.getItem() instanceof DiggerItem;
        };
    }

    public static int countValidPillars(Level level, BlockPos center) {
        int count = 0;
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
            if (dx == 0 && dz == 0) continue;
            if (isValidPillar(level, center.offset(dx, 0, dz))) count++;
        }
        return count;
    }

    public static boolean hasStructure(Level level, BlockPos pos) { return countValidPillars(level, pos) >= 6; }

    private static boolean isValidPillar(Level level, BlockPos base) {
        for (int height = 2; height <= 12; height++) {
            boolean allTotem = true;
            for (int y = 0; y < height; y++) {
                if (!level.getBlockState(base.above(y)).is(ThaumcraftMod.OBSIDIAN_TOTEM.get())) { allTotem = false; break; }
            }
            if (allTotem) {
                BlockState cap = level.getBlockState(base.above(height));
                if (cap.is(ThaumcraftMod.NITOR_LIGHT.get()) || cap.is(ThaumcraftMod.EXTRAS_LIGHT_BLOCK.get())) return true;
            }
        }
        return false;
    }

    public static void showStructureStatus(Level level, BlockPos pos, Player player) {
        int pillars = countValidPillars(level, pos);
        player.displayClientMessage(Component.literal("Osmotic Enchanter structure: " + pillars + "/6 pillars.").withStyle(pillars >= 6 ? ChatFormatting.GREEN : ChatFormatting.RED), false);
        player.displayClientMessage(Component.literal("Нужно 6 Obsidian Totem pillars высотой 2-12 с Nitor Light сверху в радиусе 4 блоков.").withStyle(ChatFormatting.GRAY), false);
    }

    public static boolean apply(Level level, BlockPos pos, Player player, Choice choice) {
        if (!hasStructure(level, pos)) { showStructureStatus(level, pos, player); return false; }
        ItemStack held = player.getMainHandItem();
        if (!canApply(choice, held)) { player.displayClientMessage(Component.literal("Это зачарование нельзя применить к предмету в руке.").withStyle(ChatFormatting.RED), false); return false; }
        Enchantment ench = enchantment(choice);
        int current = EnchantmentHelper.getItemEnchantmentLevel(ench, held);
        int next = current + 1;
        if (next > maxLevel(choice)) { player.displayClientMessage(Component.literal(choiceName(choice).getString() + " уже на максимальном уровне.").withStyle(ChatFormatting.RED), false); return false; }
        Aspect aspect = costAspect(choice);
        int vis = visCost(choice, next);
        int xp = xpCost(choice, next);
        if (!player.getAbilities().instabuild && player.experienceLevel < xp) { player.displayClientMessage(Component.literal("Нужно уровней опыта: " + xp).withStyle(ChatFormatting.RED), false); return false; }
        if (!WandItem.consumeVisFromInventory(player, aspect, vis)) { player.displayClientMessage(Component.literal("Нужно " + vis + " " + aspect.displayName() + " vis в жезле.").withStyle(ChatFormatting.RED), false); return false; }
        if (!player.getAbilities().instabuild) player.giveExperienceLevels(-xp);
        Map<Enchantment, Integer> enchants = new LinkedHashMap<>(EnchantmentHelper.getEnchantments(held));
        enchants.put(ench, next);
        EnchantmentHelper.setEnchantments(enchants, held);
        player.displayClientMessage(Component.literal("Osmotic Enchanter применил ").append(choiceName(choice)).append(Component.literal(" " + next + ".")).withStyle(ChatFormatting.AQUA), false);
        level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.15F);
        return true;
    }
}
