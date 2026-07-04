package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class GolemCoreItem extends Item {
    public GolemCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (context.getPlayer() == null) {
            return InteractionResult.PASS;
        }

        if (!PlayerThaumData.hasResearch(context.getPlayer(), "GOLEMS")) {
            context.getPlayer().displayClientMessage(Component.literal("Research locked: GOLEMS").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        ThaumGolemEntity golem = ThaumcraftMod.THAUM_GOLEM.get().create(level);

        if (golem == null) {
            return InteractionResult.PASS;
        }

        golem.setOwnerUuid(context.getPlayer().getUUID());
        golem.setHomePos(pos);
        golem.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, context.getPlayer().getYRot(), 0.0F);
        level.addFreshEntity(golem);

        if (!context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        context.getPlayer().displayClientMessage(Component.literal("Thaumic Golem awakened. Home position saved. Place Collect Seals to expand its pickup area.").withStyle(ChatFormatting.GOLD), false);
        return InteractionResult.CONSUME;
    }
}
