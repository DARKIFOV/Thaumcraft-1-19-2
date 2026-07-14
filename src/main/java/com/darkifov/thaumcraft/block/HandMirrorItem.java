package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.menu.HandMirrorMenu;
import com.darkifov.thaumcraft.mirror.MirrorBlockEntity;
import com.darkifov.thaumcraft.mirror.MirrorLink;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

/** Functional TC4 hand mirror: bind to a regular mirror, then send one GUI slot remotely. */
public final class HandMirrorItem extends Item {
    public HandMirrorItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MirrorBlockEntity)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }
        MirrorLink.at(serverLevel, context.getClickedPos()).write(context.getItemInHand());
        context.getLevel().playSound(null, context.getClickedPos(), TC4Sounds.event("jar"), SoundSource.BLOCKS, 1.0F, 2.0F);
        if (context.getPlayer() != null) {
            context.getPlayer().displayClientMessage(Component.translatable("tc.handmirrorlinked")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack mirror = player.getItemInHand(hand);
        if (!MirrorLink.has(mirror)) {
            return InteractionResultHolder.pass(mirror);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.success(mirror);
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(mirror);
        }
        Target target = resolveTarget(mirror, serverLevel, true);
        if (target == null) {
            player.displayClientMessage(Component.translatable("tc.handmirrorerror")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
            level.playSound(null, player.blockPosition(), TC4Sounds.event("zap"), SoundSource.PLAYERS, 1.0F, 0.8F);
            return InteractionResultHolder.fail(mirror);
        }

        boolean mainHand = hand == InteractionHand.MAIN_HAND;
        NetworkHooks.openScreen(serverPlayer,
                new SimpleMenuProvider(
                        (int id, Inventory inventory, Player menuPlayer) -> new HandMirrorMenu(id, inventory, mirror),
                        Component.translatable("container.thaumcraft.hand_mirror")),
                buffer -> buffer.writeBoolean(mainHand));
        return InteractionResultHolder.consume(mirror);
    }

    public static boolean transport(ItemStack mirror, ItemStack items, ServerPlayer player) {
        if (items.isEmpty() || items.getItem() instanceof HandMirrorItem) {
            return false;
        }
        ServerLevel origin = player.getLevel();
        Target target = resolveTarget(mirror, origin, true);
        if (target == null || !target.mirror().spawnDirect(items.copy())) {
            player.displayClientMessage(Component.translatable("tc.handmirrorerror")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
            origin.playSound(null, player.blockPosition(), TC4Sounds.event("zap"), SoundSource.PLAYERS, 1.0F, 0.8F);
            return false;
        }
        origin.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.1F, 1.0F);
        return true;
    }

    @Nullable
    private static Target resolveTarget(ItemStack mirror, ServerLevel origin, boolean clearInvalid) {
        MirrorLink link = MirrorLink.read(mirror);
        if (link == null) {
            return null;
        }
        ServerLevel targetLevel = link.resolveLevel(origin);
        if (targetLevel == null || !targetLevel.hasChunkAt(link.pos())) {
            return null;
        }
        if (targetLevel.getBlockEntity(link.pos()) instanceof MirrorBlockEntity target) {
            return new Target(targetLevel, target);
        }
        if (clearInvalid) {
            MirrorLink.clear(mirror);
        }
        return null;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return MirrorLink.has(stack) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        MirrorLink link = MirrorLink.read(stack);
        if (link == null) {
            tooltip.add(Component.translatable("tooltip.thaumcraft.hand_mirror.bind")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Component.translatable("tc.handmirrorlinkedto")
                .append(" " + link.pos().getX() + "," + link.pos().getY() + "," + link.pos().getZ()
                        + " in " + link.dimension().location())
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

    private record Target(ServerLevel level, MirrorBlockEntity mirror) {}
}
