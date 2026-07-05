package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class AddonCompletionLedgerItem extends Item {
    public AddonCompletionLedgerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            PlayerThaumData.unlockResearch(player, "ADDONS_FINAL_COMPLETION");
            PlayerThaumData.unlockResearch(player, "THAUMIC_ENERGISTICS_FULL");
            PlayerThaumData.unlockResearch(player, "THAUMIC_TINKERER");
            PlayerThaumData.unlockResearch(player, "THAUMCRAFT_EXTRAS");

            player.displayClientMessage(Component.literal("=== Addons Final Completion ===").withStyle(ChatFormatting.GOLD), false);
            player.displayClientMessage(Component.literal("Thaumic Energistics: digital essentia, assembler, providers, wireless terminal, encoded patterns.").withStyle(ChatFormatting.AQUA), false);
            player.displayClientMessage(Component.literal("Thaumic Tinkerer: osmotic enchanting, transvector interface, ethereal platform, fume dissipator, ichor/KAMI utilities.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            player.displayClientMessage(Component.literal("Thaumcraft Extras: elemental blocks, Pech trading, experience items, utility foci, research cache.").withStyle(ChatFormatting.GREEN), false);
            player.displayClientMessage(Component.literal("Use this ledger as a one-click progression/audit unlock for addon systems.").withStyle(ChatFormatting.GRAY), false);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Final audit/progression ledger for all rebuilt addon branches.").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("ПКМ: открыть/разблокировать ветки Thaumic Energistics, TT и Extras.").withStyle(ChatFormatting.GRAY));
    }
}
