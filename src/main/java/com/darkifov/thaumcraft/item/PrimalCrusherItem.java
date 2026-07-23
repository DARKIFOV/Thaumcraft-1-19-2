package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.item.gear.TC4PrimalCrusherTier;
import com.darkifov.thaumcraft.porting.TC4ResearchItems;
import com.darkifov.thaumcraft.runic.TC4WarpingGearAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Forge 1.19.2 port of TC4's Primal Crusher. */
public final class PrimalCrusherItem extends PickaxeItem {
    private static final ThreadLocal<Boolean> INTERNAL_HARVEST = ThreadLocal.withInitial(() -> false);

    public PrimalCrusherItem(Properties properties) {
        // Original ItemTool: base 3.5 damage + PRIMALVOID's 4 damage bonus.
        // Attack speed has no 1.7.10 equivalent; -3.0 keeps the crusher deliberately heavy.
        super(TC4PrimalCrusherTier.INSTANCE, 3, -3.0F, properties.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return toolAction == ToolActions.PICKAXE_DIG || toolAction == ToolActions.SHOVEL_DIG;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return isCrusherEffective(state) ? TC4PrimalCrusherTier.INSTANCE.getSpeed()
                : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        // TC4 accepted every non-air, non-water and non-lava material as
        // harvestable, while its fast-mining set remained pickaxe/shovel-like.
        return !state.isAir() && state.getFluidState().isEmpty();
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        boolean primalCharm = TC4ResearchItems.registered("tc4_charm")
                .map(item -> repair.is(item.get()))
                .orElse(false);
        return primalCharm || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos origin, Player player) {
        if (INTERNAL_HARVEST.get() || player.isShiftKeyDown()) {
            return false;
        }
        Level level = player.level;
        if (!isCrusherEffective(level.getBlockState(origin))) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return true;
        }

        Direction face = lookedFace(player, origin);
        INTERNAL_HARVEST.set(true);
        try {
            for (BlockPos target : miningPlane(origin, face)) {
                BlockState state = serverLevel.getBlockState(target);
                if (state.isAir() || !isCrusherEffective(state)
                        || state.getDestroySpeed(serverLevel, target) < 0.0F) {
                    continue;
                }
                serverPlayer.gameMode.destroyBlock(target);
                if (stack.isEmpty()) {
                    break;
                }
            }
        } finally {
            INTERNAL_HARVEST.remove();
        }
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && stack.isDamaged() && entity.tickCount % 20 == 0) {
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.thaumcraft.primal_crusher.self_repair")
                .withStyle(ChatFormatting.DARK_PURPLE));
        TC4WarpingGearAdapter.appendTooltip(stack, null, tooltip);
    }

    public static List<BlockPos> miningPlane(BlockPos origin, Direction face) {
        List<BlockPos> positions = new ArrayList<>(9);
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (face.getAxis() == Direction.Axis.Y) positions.add(origin.offset(a, 0, b));
                else if (face.getAxis() == Direction.Axis.Z) positions.add(origin.offset(a, b, 0));
                else positions.add(origin.offset(0, b, a));
            }
        }
        return positions;
    }

    private static boolean isCrusherEffective(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    private static Direction lookedFace(Player player, BlockPos expected) {
        HitResult result = player.pick(6.0D, 0.0F, false);
        if (result instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(expected)) {
            return blockHit.getDirection();
        }
        return Direction.UP;
    }
}
