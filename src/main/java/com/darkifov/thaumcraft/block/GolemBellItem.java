package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class GolemBellItem extends Item {
    public GolemBellItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (level.isClientSide || context.getPlayer() == null) {
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.CONSUME;
        }

        Player player = context.getPlayer();
        BlockPos home = context.getClickedPos().relative(context.getClickedFace());
        int changed = 0;

        for (ThaumGolemEntity golem : serverLevel.getEntitiesOfClass(ThaumGolemEntity.class, player.getBoundingBox().inflate(32.0D))) {
            if (player.getUUID().equals(golem.getOwnerUuid())) {
                golem.setHomePos(home);
                golem.getNavigation().moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, 1.0D);
                changed++;
            }
        }

        player.displayClientMessage(Component.literal("Set home position for nearby owned golems: " + changed).withStyle(ChatFormatting.GOLD), false);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            int recalled = 0;

            for (ThaumGolemEntity golem : serverLevel.getEntitiesOfClass(ThaumGolemEntity.class, player.getBoundingBox().inflate(32.0D))) {
                if (player.getUUID().equals(golem.getOwnerUuid())) {
                    golem.getNavigation().moveTo(player, 1.2D);
                    recalled++;
                }
            }

            player.displayClientMessage(Component.literal("Recalled nearby golems: " + recalled).withStyle(ChatFormatting.AQUA), false);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
