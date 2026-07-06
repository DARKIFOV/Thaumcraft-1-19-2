package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.entity.TaintCrawlerEntity;
import com.darkifov.thaumcraft.taint.TaintSpreadRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class TaintSeedItem extends Item {
    public TaintSeedItem(Properties properties) {
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

        if (!PlayerThaumData.hasResearch(context.getPlayer(), "TAINT")
                && !PlayerThaumData.hasResearch(context.getPlayer(), "ELDRITCHMINOR")) {
            context.getPlayer().displayClientMessage(Component.literal("Research locked: TAINT / ELDRITCHMINOR").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        TaintSpreadRuntime.convert(level, context.getClickedPos(), true);
        level.setBlock(pos, ThaumcraftMod.TAINTED_SOIL.get().defaultBlockState(), 3);

        TaintCrawlerEntity crawler = ThaumcraftMod.TAINT_CRAWLER.get().create(level);

        if (crawler != null) {
            crawler.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, context.getPlayer().getYRot(), 0.0F);
            level.addFreshEntity(crawler);
        }

        PlayerThaumData.addWarpTemporary(context.getPlayer(), 1);

        if (!context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        context.getPlayer().displayClientMessage(Component.literal("Taint takes root.").withStyle(ChatFormatting.DARK_PURPLE), false);
        return InteractionResult.CONSUME;
    }
}
