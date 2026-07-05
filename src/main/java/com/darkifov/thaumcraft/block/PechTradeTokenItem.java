package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

public class PechTradeTokenItem extends Item {
    private final int tier;

    public PechTradeTokenItem(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int tier() {
        return tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack token = player.getItemInHand(hand);

        if (!level.isClientSide) {
            player.displayClientMessage(
                    Component.literal("Этот жетон лучше обменивать у Печа. ПКМ по Печу с жетоном в руке.").withStyle(ChatFormatting.GOLD),
                    false
            );
        }

        return InteractionResultHolder.success(token);
    }

    public static ItemStack rewardForTier(int tier, RandomSource random) {
        int roll = random.nextInt(100);

        if (tier <= 1) {
            if (roll < 45) return new ItemStack(Items.GOLD_NUGGET, 12 + random.nextInt(9));
            if (roll < 75) return new ItemStack(Items.EMERALD, 1);
            if (roll < 92) return new ItemStack(Items.EXPERIENCE_BOTTLE, 2);
            return new ItemStack(ThaumcraftMod.QUICKSILVER_DROP.get(), 2);
        }

        if (tier == 2) {
            if (roll < 35) return new ItemStack(Items.EMERALD, 2 + random.nextInt(2));
            if (roll < 65) return new ItemStack(ThaumcraftMod.BALANCED_SHARD.get(), 1);
            if (roll < 88) return new ItemStack(Items.GOLD_INGOT, 2);
            return new ItemStack(ThaumcraftMod.THAUMIUM_INGOT.get(), 1);
        }

        if (tier == 3) {
            if (roll < 30) return new ItemStack(ThaumcraftMod.THAUMIUM_INGOT.get(), 2 + random.nextInt(2));
            if (roll < 55) return new ItemStack(ThaumcraftMod.EXPERIENCE_SHARD.get(), 2);
            if (roll < 78) return new ItemStack(ThaumcraftMod.VOID_METAL_INGOT.get(), 1);
            if (roll < 92) return new ItemStack(ThaumcraftMod.IGNIS_FUEL.get(), 4);
            return new ItemStack(ThaumcraftMod.ELDRITCH_EYE.get(), 1);
        }

        if (tier == 4) {
            if (roll < 30) return new ItemStack(ThaumcraftMod.VOID_METAL_INGOT.get(), 2);
            if (roll < 55) return new ItemStack(ThaumcraftMod.ELDRITCH_RELIC.get(), 1);
            if (roll < 78) return new ItemStack(ThaumcraftMod.PRIMORDIAL_PEARL.get(), 1);
            if (roll < 92) return new ItemStack(ThaumcraftMod.ELDRITCH_GUARDIAN_CORE.get(), 1);
            return new ItemStack(ThaumcraftMod.AWAKENED_CRIMSON_KEY.get(), 1);
        }

        if (roll < 30) return new ItemStack(ThaumcraftMod.ELDRITCH_RELIC.get(), 2);
        if (roll < 55) return new ItemStack(ThaumcraftMod.ELDRITCH_GUARDIAN_CORE.get(), 1);
        if (roll < 80) return new ItemStack(ThaumcraftMod.PRIMORDIAL_PEARL.get(), 1);
        if (roll < 94) return new ItemStack(ThaumcraftMod.AWAKENED_CRIMSON_KEY.get(), 1);
        return new ItemStack(ThaumcraftMod.VOID_ESSENTIA_JAR.get(), 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(Component.literal("ПКМ по Печу: обменять на случайную награду.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Уровень жетона: " + tier).withStyle(ChatFormatting.DARK_PURPLE));
    }
}
