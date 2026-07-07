package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.darkifov.thaumcraft.golem.GolemBellMode;
import com.darkifov.thaumcraft.golem.GolemCoreType;
import com.darkifov.thaumcraft.golem.GolemMarkerMode;
import com.darkifov.thaumcraft.golem.GolemBellMarkerRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;

public class GolemBellItem extends Item {
    private static final String TAG_MODE = "TC4GolemBellMode";

    public GolemBellItem(Properties properties) {
        super(properties.stacksTo(1));
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
        ItemStack bell = context.getItemInHand();
        GolemBellMode mode = getMode(bell);
        BlockPos target = context.getClickedPos().relative(context.getClickedFace());
        BlockPos markerTarget = context.getClickedPos();
        List<ThaumGolemEntity> owned = ownedGolems(serverLevel, player, 32.0D);

        if (owned.isEmpty()) {
            player.displayClientMessage(Component.literal("No owned golems nearby.").withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        switch (mode) {
            case HOME -> {
                int changed = 0;
                for (ThaumGolemEntity golem : owned) {
                    golem.setHomePos(target);
                    golem.getNavigation().moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, 1.0D);
                    changed++;
                }
                player.displayClientMessage(Component.literal("Set home position for owned golems: " + changed).withStyle(ChatFormatting.GOLD), false);
            }
            case MARKER -> {
                ThaumGolemEntity bound = GolemBellMarkerRuntime.boundGolem(bell, level);
                boolean multiColor = bound != null && bound.hasUpgrade(com.darkifov.thaumcraft.golem.GolemUpgradeType.ORDER);
                GolemBellMarkerRuntime.ToggleResult result = GolemBellMarkerRuntime.changeMarkers(bell, player, level, markerTarget, context.getClickedFace(), multiColor);
                ItemStack offhand = player.getOffhandItem();
                GolemMarkerMode markerMode = offhand.getItem() instanceof GolemTaskMarkerItem ? GolemTaskMarkerItem.getMode(offhand) : GolemMarkerMode.WORK;
                BlockPos markerPos = offhand.getItem() instanceof GolemTaskMarkerItem && GolemTaskMarkerItem.getPosition(offhand) != null ? GolemTaskMarkerItem.getPosition(offhand) : markerTarget;
                int changed = 0;
                for (ThaumGolemEntity golem : owned) {
                    golem.setTaskMarker(markerMode, markerPos);
                    golem.applyOriginalMarkerList(GolemBellMarkerRuntime.getMarkersTag(bell));
                    changed++;
                }
                player.displayClientMessage(Component.literal("TC4 marker " + result.action() + " | markers=" + result.count() + " | synced golems=" + changed).withStyle(ChatFormatting.YELLOW), false);
            }
            case RETASK -> {
                GolemCoreType next = null;
                ItemStack offhand = player.getOffhandItem();
                if (offhand.getItem() instanceof GolemCoreItem) {
                    CompoundTag tag = offhand.getOrCreateTag();
                    next = GolemCoreType.byName(tag.getString(GolemCoreItem.TAG_CORE));
                }
                if (next == null) {
                    next = GolemCoreType.GATHER;
                }
                int changed = 0;
                for (ThaumGolemEntity golem : owned) {
                    golem.setCoreType(next);
                    changed++;
                }
                player.displayClientMessage(Component.literal("Retasked owned golems to ").append(next.displayName()).append(Component.literal(": " + changed)).withStyle(ChatFormatting.LIGHT_PURPLE), false);
            }
            case STATUS -> {
                player.displayClientMessage(Component.literal("Owned golems nearby: " + owned.size()).withStyle(ChatFormatting.GREEN), false);
                for (ThaumGolemEntity golem : owned) {
                    player.displayClientMessage(golem.statusSummary(), false);
                }
            }
            case RECALL, WAIT -> {
                // handled by use(); keep useOn from also setting home in these modes.
                return use(level, player, context.getHand()).getResult();
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (player.isShiftKeyDown()) {
                GolemBellMode next = getMode(stack).next();
                stack.getOrCreateTag().putString(TAG_MODE, next.id());
                player.displayClientMessage(Component.literal("Golem bell mode: ").append(next.displayName()), true);
                return InteractionResultHolder.consume(stack);
            }

            GolemBellMode mode = getMode(stack);
            List<ThaumGolemEntity> owned = ownedGolems(serverLevel, player, 32.0D);
            if (mode == GolemBellMode.STATUS) {
                player.displayClientMessage(Component.literal("Owned golems nearby: " + owned.size()).withStyle(ChatFormatting.GREEN), false);
                for (ThaumGolemEntity golem : owned) {
                    player.displayClientMessage(golem.statusSummary(), false);
                }
            } else if (mode == GolemBellMode.WAIT) {
                int changed = 0;
                for (ThaumGolemEntity golem : owned) {
                    golem.setWaiting(!golem.isWaiting());
                    changed++;
                }
                player.displayClientMessage(Component.literal("Toggled waiting for owned golems: " + changed).withStyle(ChatFormatting.GRAY), false);
            } else {
                int recalled = 0;
                for (ThaumGolemEntity golem : owned) {
                    golem.setWaiting(false);
                    golem.getNavigation().moveTo(player, 1.2D);
                    recalled++;
                }
                player.displayClientMessage(Component.literal("Recalled nearby golems: " + recalled).withStyle(ChatFormatting.AQUA), false);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private List<ThaumGolemEntity> ownedGolems(ServerLevel serverLevel, Player player, double radius) {
        List<ThaumGolemEntity> golems = serverLevel.getEntitiesOfClass(ThaumGolemEntity.class, player.getBoundingBox().inflate(radius), golem -> player.getUUID().equals(golem.getOwnerUuid()));
        golems.sort(Comparator.comparingDouble(player::distanceToSqr));
        return golems;
    }

    private GolemBellMode getMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? GolemBellMode.RECALL : GolemBellMode.byName(tag.getString(TAG_MODE));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        GolemBellMode mode = getMode(stack);
        tooltip.add(Component.literal("Mode: ").append(mode.displayName()));
        tooltip.add(Component.literal("Shift + right-click: cycle mode.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right-click air recalls/toggles wait/status. Right-click block assigns mode target.").withStyle(ChatFormatting.GRAY));
        tooltip.add(GolemBellMarkerRuntime.markerSummary(stack).withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.literal("Right-click a golem binds original golemid/home/markers NBT.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
