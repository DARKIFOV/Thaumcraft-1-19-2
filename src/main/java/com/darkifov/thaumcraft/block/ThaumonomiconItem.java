package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.client.ClientHooks;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.DistExecutor;

public class ThaumonomiconItem extends Item {
    public ThaumonomiconItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> ClientHooks::openThaumonomicon);
        } else {
            if (player instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncResearch(serverPlayer);
            }

            player.displayClientMessage(Component.literal("Opening Thaumonomicon...").withStyle(ChatFormatting.GOLD), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
