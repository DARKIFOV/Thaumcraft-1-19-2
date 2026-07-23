package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** Creative migration utility matching ItemEldritchObject metadata 4. */
public final class TC4ObeliskPlacerItem extends Item {
    public TC4ObeliskPlacerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) return InteractionResult.PASS;
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            if (!level.isClientSide && player != null) {
                player.displayClientMessage(Component.translatable("message.thaumcraft.obelisk_placer.creative_only")
                        .withStyle(ChatFormatting.DARK_PURPLE), true);
            }
            return InteractionResult.FAIL;
        }
        BlockPos base = context.getClickedPos();
        // The original checked +1..+6 but placed the top at +7. The port validates all actual targets.
        int[] offsets = {1, 3, 4, 5, 6, 7};
        for (int offset : offsets) {
            BlockPos target = base.above(offset);
            if (!level.getBlockState(target).getMaterial().isReplaceable()) return InteractionResult.FAIL;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        place(level, base.above(1), ThaumcraftMod.ELDRITCH_ALTAR.get());
        place(level, base.above(3), ThaumcraftMod.ELDRITCH_STONE.get());
        for (int offset = 4; offset <= 7; offset++) {
            place(level, base.above(offset), ThaumcraftMod.ELDRITCH_OBELISK.get());
        }
        player.swing(context.getHand(), true);
        return InteractionResult.CONSUME;
    }

    private static void place(Level level, BlockPos pos, Block block) {
        level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.thaumcraft.obelisk_placer.creative")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
