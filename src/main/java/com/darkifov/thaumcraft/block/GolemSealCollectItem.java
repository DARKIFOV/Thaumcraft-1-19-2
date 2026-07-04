package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class GolemSealCollectItem extends Item {
    private static final String TAG_X = "SealX";
    private static final String TAG_Y = "SealY";
    private static final String TAG_Z = "SealZ";
    private static final String TAG_BOUND = "Bound";

    public GolemSealCollectItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (level.isClientSide || context.getPlayer() == null) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(TAG_BOUND, true);
        tag.putInt(TAG_X, pos.getX());
        tag.putInt(TAG_Y, pos.getY());
        tag.putInt(TAG_Z, pos.getZ());

        PlayerThaumData.unlockResearch(context.getPlayer(), "GOLEM_SEAL_COLLECT");
        context.getPlayer().displayClientMessage(Component.literal("Collect seal target set: " + pos.toShortString()).withStyle(ChatFormatting.AQUA), false);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();

        if (tag != null && tag.getBoolean(TAG_BOUND)) {
            tooltip.add(Component.literal("Target: " + tag.getInt(TAG_X) + ", " + tag.getInt(TAG_Y) + ", " + tag.getInt(TAG_Z)).withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.literal("Right-click a block to bind collect target.").withStyle(ChatFormatting.GRAY));
        }
    }
}
