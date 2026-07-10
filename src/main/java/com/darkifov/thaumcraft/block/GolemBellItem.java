package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.darkifov.thaumcraft.golem.GolemBellMarkerRuntime;
import com.darkifov.thaumcraft.golem.GolemUpgradeType;
import com.darkifov.thaumcraft.golem.GolemCoreType;
import com.darkifov.thaumcraft.porting.TC4ResearchItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * TC4 ItemGolemBell parity adapter.
 * Right-clicking a golem binds it, right-clicking a block edits that bound
 * golem's marker list, and left-clicking the owned golem dismantles it into a
 * placer while preserving configuration and inventory.
 */
public class GolemBellItem extends Item {
    public GolemBellItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack bell = context.getItemInHand();
        ThaumGolemEntity golem = GolemBellMarkerRuntime.boundGolem(bell, level);
        if (golem == null) {
            player.displayClientMessage(Component.literal("Bind this bell to a golem first.")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.CONSUME;
        }
        if (!owns(player, golem)) {
            player.displayClientMessage(Component.literal("That golem belongs to another thaumaturge.")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }

        boolean multipleColors = golem.getUpgradeAmount(GolemUpgradeType.ORDER) > 0;
        GolemBellMarkerRuntime.ToggleResult result = GolemBellMarkerRuntime.changeMarkers(
                bell, player, level, context.getClickedPos(), context.getClickedFace(), multipleColors);
        level.playSound(null, context.getClickedPos(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 0.7F, 1.0F + level.random.nextFloat() * 0.1F);
        player.displayClientMessage(Component.literal("Marker " + result.action() + " (" + result.count() + ")")
                .withStyle(ChatFormatting.GOLD), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bell = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ThaumGolemEntity golem = GolemBellMarkerRuntime.boundGolem(bell, level);
            if (player.isShiftKeyDown()) {
                if (golem != null && owns(player, golem)) {
                    GolemBellMarkerRuntime.setMarkers(bell, List.of());
                    golem.applyOriginalMarkerList(GolemBellMarkerRuntime.getMarkersTag(bell));
                    level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                            SoundSource.PLAYERS, 0.7F, 0.9F);
                    player.displayClientMessage(Component.literal("Cleared all golem markers.")
                            .withStyle(ChatFormatting.YELLOW), true);
                } else {
                    GolemBellMarkerRuntime.clearBinding(bell);
                    player.displayClientMessage(Component.literal("Cleared stale golem binding.")
                            .withStyle(ChatFormatting.GRAY), true);
                }
            } else if (golem != null) {
                player.displayClientMessage(golem.statusSummary(), false);
            } else {
                player.displayClientMessage(Component.literal("Unbound Golem Bell")
                        .withStyle(ChatFormatting.GRAY), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(bell, level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof ThaumGolemEntity golem)) {
            return InteractionResult.PASS;
        }
        if (player.level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!owns(player, golem)) {
            player.displayClientMessage(Component.literal("That golem belongs to another thaumaturge.")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }
        GolemBellMarkerRuntime.bindGolem(stack, golem);
        player.level.playSound(null, golem.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 0.7F, 1.0F + player.level.random.nextFloat() * 0.1F);
        player.displayClientMessage(Component.literal("Golem Bell bound. Markers: ")
                .append(Component.literal(String.valueOf(GolemBellMarkerRuntime.getMarkers(stack).size())))
                .withStyle(ChatFormatting.GOLD), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!(entity instanceof ThaumGolemEntity golem) || golem.isRemoved()) {
            return false;
        }
        if (player.level.isClientSide) {
            player.swing(InteractionHand.MAIN_HAND);
            return true;
        }
        if (!owns(player, golem)) {
            player.displayClientMessage(Component.literal("You cannot dismantle another thaumaturge's golem.")
                    .withStyle(ChatFormatting.RED), true);
            return true;
        }

        // TC4 bell parity: normal dismantling serializes the whole configured
        // golem into its exact body item. Sneak-dismantling salvages the bare
        // body, drops the core separately and gives each installed upgrade its
        // original 50% recovery chance.
        ItemStack placer = player.isShiftKeyDown()
                ? golem.createBareGolemBodyStack()
                : golem.createGolemPlacerStack();
        if (golem.isAdvancedGolem()) {
            placer.getOrCreateTag().putBoolean("advanced", true);
        }
        golem.spawnAtLocation(placer, 0.5F);
        if (player.isShiftKeyDown()) {
            dropOriginalCore(golem);
            dropRecoveredUpgrades(golem);
        }
        golem.dropCarriedStackAfterDismantle();
        player.level.playSound(null, golem.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.NEUTRAL, 0.35F, 1.6F);
        GolemBellMarkerRuntime.clearBinding(stack);
        golem.discard();
        return true;
    }


    private static void dropOriginalCore(ThaumGolemEntity golem) {
        GolemCoreType core = golem.getCoreType();
        if (core == null || core == GolemCoreType.BLANK || core.originalId() < 0) {
            return;
        }
        TC4ResearchItems.registered("tc4_golem_core_" + core.id())
                .ifPresent(entry -> golem.spawnAtLocation(new ItemStack(entry.get()), 0.5F));
    }

    private static void dropRecoveredUpgrades(ThaumGolemEntity golem) {
        for (byte raw : golem.getOriginalUpgradeSlotsCopy()) {
            GolemUpgradeType type = GolemUpgradeType.byOriginalId(raw);
            if (type == null || !golem.getRandom().nextBoolean()) {
                continue;
            }
            TC4ResearchItems.registered("tc4_golem_upgrade_" + type.id())
                    .ifPresent(entry -> golem.spawnAtLocation(new ItemStack(entry.get()), 0.5F));
        }
    }

    private static boolean owns(Player player, ThaumGolemEntity golem) {
        return player.getAbilities().instabuild || golem.getOwnerUuid() == null
                || player.getUUID().equals(golem.getOwnerUuid());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Right-click a golem: bind bell").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right-click a block: add/remove marker").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Order upgrade: cycle marker colors; sneak removes").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Left-click owned golem: preserve it in its original body item").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Sneak + left-click: salvage body/core and 50% of upgrades").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Sneak + right-click air: clear markers/binding").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(GolemBellMarkerRuntime.markerSummary(stack).withStyle(ChatFormatting.DARK_AQUA));
    }
}
