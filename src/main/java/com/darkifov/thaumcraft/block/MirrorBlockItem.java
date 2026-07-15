package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.mirror.AbstractMirrorBlockEntity;
import com.darkifov.thaumcraft.mirror.EssentiaMirrorBlockEntity;
import com.darkifov.thaumcraft.mirror.MirrorBlockEntity;
import com.darkifov.thaumcraft.mirror.MirrorLink;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** Pairing item for regular and essentia mirrors, preserving TC4 link NBT on drops. */
public final class MirrorBlockItem extends BlockItem {
    private final MirrorBlock.Kind kind;

    public MirrorBlockItem(Block block, Properties properties, MirrorBlock.Kind kind) {
        super(block, properties);
        this.kind = kind;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof AbstractMirrorBlockEntity mirror)
                || !matches(mirror)) {
            return super.useOn(context);
        }
        if (mirror.isLinkValid()) {
            if (!level.isClientSide && player != null) {
                player.displayClientMessage(Component.translatable("message.thaumcraft.mirror.already_linked")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel) || player == null) {
            return InteractionResult.PASS;
        }

        ItemStack linkedStack = context.getItemInHand().copy();
        linkedStack.setCount(1);
        MirrorLink.at(serverLevel, context.getClickedPos()).write(linkedStack);
        if (!player.getInventory().add(linkedStack)) {
            level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), linkedStack));
        }
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        level.playSound(null, context.getClickedPos(), TC4Sounds.event("jar"), SoundSource.BLOCKS, 1.0F, 2.0F);
        player.displayClientMessage(Component.translatable("message.thaumcraft.mirror.first_bound")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
        return InteractionResult.CONSUME;
    }

    private boolean matches(AbstractMirrorBlockEntity mirror) {
        return kind == MirrorBlock.Kind.ITEM ? mirror instanceof MirrorBlockEntity : mirror instanceof EssentiaMirrorBlockEntity;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return MirrorLink.has(stack) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        MirrorLink link = MirrorLink.read(stack);
        if (link != null) {
            tooltip.add(Component.translatable("tooltip.thaumcraft.mirror.linked_to",
                            link.pos().getX(), link.pos().getY(), link.pos().getZ(), link.dimension().location())
                    .withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            tooltip.add(Component.translatable("tooltip.thaumcraft.mirror.unlinked")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
