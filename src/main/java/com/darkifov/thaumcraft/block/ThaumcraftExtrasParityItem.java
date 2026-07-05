package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ThaumcraftExtrasParityItem extends Item {
    public enum Mode {
        MAGIC_WRENCH,
        DARK_THAUMIUM_TOOL,
        WAND_CAP,
        WAND_ROD,
        COLOR_POUCH,
        EMPTY_FOCUS,
        INFO_BOOK,
        COMB,
        DARK_CRYSTAL,
        DARK_SHARD,
        DARK_NUGGET,
        API_CRYSTAL,
        API_SHARD,
        API_NUGGET
    }

    private final Mode mode;

    public ThaumcraftExtrasParityItem(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            PlayerThaumData.unlockResearch(player, "THAUMCRAFT_EXTRAS_TRUE_PARITY");

            switch (mode) {
                case MAGIC_WRENCH -> player.displayClientMessage(Component.literal("Magic Wrench toggles and configures Thaumcraft Extras machines.").withStyle(ChatFormatting.AQUA), false);
                case DARK_THAUMIUM_TOOL -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 20, 1));
                    player.displayClientMessage(Component.literal("Dark Thaumium tool parity: mining/combat boost.").withStyle(ChatFormatting.DARK_PURPLE), false);
                }
                case WAND_CAP -> player.displayClientMessage(Component.literal("Thaumcraft Extras wand cap component.").withStyle(ChatFormatting.GOLD), false);
                case WAND_ROD -> player.displayClientMessage(Component.literal("Thaumcraft Extras wand rod component.").withStyle(ChatFormatting.GOLD), false);
                case COLOR_POUCH -> player.displayClientMessage(Component.literal("Color Pouch stores and applies colour state in TCE machines.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                case EMPTY_FOCUS -> player.displayClientMessage(Component.literal("Empty Focus is a base for Extras foci crafting.").withStyle(ChatFormatting.GRAY), false);
                case INFO_BOOK -> player.displayClientMessage(Component.literal("Info Book opens/records Thaumcraft Extras knowledge.").withStyle(ChatFormatting.GREEN), false);
                case COMB -> player.displayClientMessage(Component.literal("Forestry comb compatibility item mapped from TCE.").withStyle(ChatFormatting.YELLOW), false);
                case DARK_CRYSTAL -> player.displayClientMessage(Component.literal("Dark Crystal: TCE API crystal parity material.").withStyle(ChatFormatting.DARK_PURPLE), false);
                case DARK_SHARD -> player.displayClientMessage(Component.literal("Dark Shard: TCE shard parity material.").withStyle(ChatFormatting.DARK_PURPLE), false);
                case DARK_NUGGET -> player.displayClientMessage(Component.literal("Dark Nugget: TCE nugget parity material.").withStyle(ChatFormatting.DARK_PURPLE), false);
                case API_CRYSTAL -> player.displayClientMessage(Component.literal("Crystal API item parity from Thaumcraft Extras.").withStyle(ChatFormatting.AQUA), false);
                case API_SHARD -> player.displayClientMessage(Component.literal("Shard API item parity from Thaumcraft Extras.").withStyle(ChatFormatting.AQUA), false);
                case API_NUGGET -> player.displayClientMessage(Component.literal("Nugget API item parity from Thaumcraft Extras.").withStyle(ChatFormatting.AQUA), false);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return mode.name().contains("DARK") || mode == Mode.MAGIC_WRENCH;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Thaumcraft Extras true parity: " + mode.name()).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Ported from 1.6.4/1.7.10 public item surface.").withStyle(ChatFormatting.GRAY));
    }
}
