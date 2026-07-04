package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ExperienceExtractorItem extends Item {
    public ExperienceExtractorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (player.experienceLevel <= 0 && !player.getAbilities().instabuild) {
                player.displayClientMessage(Component.literal("Нужен хотя бы один уровень опыта для извлечения.").withStyle(ChatFormatting.RED), false);
                return InteractionResultHolder.success(stack);
            }

            if (!player.getAbilities().instabuild) {
                player.giveExperienceLevels(-1);
            }

            player.getInventory().add(new ItemStack(ThaumcraftMod.EXPERIENCE_SHARD.get()));
            player.displayClientMessage(Component.literal("Извлекатель опыта создал кристалл опыта.").withStyle(ChatFormatting.GREEN), false);
        }

        return InteractionResultHolder.success(stack);
    }
}
