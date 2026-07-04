package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ResearchTableBlock extends Block {
    public ResearchTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (held.getItem() == Items.PAPER) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }

            ItemStack note = new ItemStack(ThaumcraftMod.RESEARCH_NOTE.get());

            if (!player.getInventory().add(note)) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, note);
            }

            player.displayClientMessage(Component.literal("Research note prepared.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            return InteractionResult.CONSUME;
        }

        if (held.is(ThaumcraftMod.RESEARCH_NOTE.get())) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }

            ItemStack point = new ItemStack(ThaumcraftMod.RESEARCH_POINT.get());

            if (!player.getInventory().add(point)) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, point);
            }

            player.displayClientMessage(Component.literal("Research completed into a Research Point placeholder.").withStyle(ChatFormatting.GOLD), false);
            return InteractionResult.CONSUME;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            ThaumcraftNetwork.openResearchTable(serverPlayer);
        }

        return InteractionResult.CONSUME;
    }
}
