package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class PechLedgerItem extends Item {
    public PechLedgerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            int favor = PlayerThaumData.getPechFavor(player);
            String rank = rankName(favor);

            player.displayClientMessage(Component.literal("Репутация у Печей: " + favor + "/100").withStyle(ChatFormatting.GOLD), false);
            player.displayClientMessage(Component.literal("Статус: " + rank).withStyle(ChatFormatting.DARK_PURPLE), false);
            player.displayClientMessage(Component.literal("Больше favor = лучше шанс бонусной награды при торговле.").withStyle(ChatFormatting.GRAY), false);
        }

        return InteractionResultHolder.success(stack);
    }

    private String rankName(int favor) {
        if (favor >= 80) {
            return "Любимец Печей";
        }

        if (favor >= 50) {
            return "Надёжный торговец";
        }

        if (favor >= 25) {
            return "Знакомый";
        }

        return "Незнакомец";
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(Component.literal("ПКМ: посмотреть репутацию у Печей.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Favor повышается подарками и торговлей.").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
