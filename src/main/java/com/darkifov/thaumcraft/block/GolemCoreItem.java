package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.darkifov.thaumcraft.golem.GolemCoreType;
import com.darkifov.thaumcraft.golem.GolemMarkerMode;
import com.darkifov.thaumcraft.golem.GolemMaterial;
import com.darkifov.thaumcraft.golem.GolemUpgradeType;
import com.darkifov.thaumcraft.golem.GolemDecorationType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class GolemCoreItem extends Item {
    public static final String TAG_MATERIAL = "TC4GolemMaterial";
    public static final String TAG_CORE = "TC4GolemCore";
    public static final String TAG_UPGRADES = "TC4GolemUpgrades";
    public static final String TAG_FILTER = "TC4GolemFilter";
    public static final String TAG_FILTER_ALLOW = "TC4GolemFilterAllow";
    public static final String TAG_DECORATIONS = "TC4GolemDecorations";

    public GolemCoreItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            ItemStack other = hand == InteractionHand.MAIN_HAND ? player.getOffhandItem() : player.getMainHandItem();
            if (other.getItem() instanceof GolemUpgradeItem upgradeItem) {
                addUpgrade(tag, upgradeItem.getUpgradeType());
                if (!player.getAbilities().instabuild) {
                    other.shrink(1);
                }
                player.displayClientMessage(Component.literal("Added golem upgrade: ").append(upgradeItem.getUpgradeType().displayName()), true);
            } else if (other.getItem() instanceof GolemFilterItem) {
                ItemStack filter = GolemFilterItem.getFilterStack(other);
                if (!filter.isEmpty()) {
                    CompoundTag filterTag = new CompoundTag();
                    filter.save(filterTag);
                    tag.put(TAG_FILTER, filterTag);
                    tag.putBoolean(TAG_FILTER_ALLOW, GolemFilterItem.isAllowList(other));
                    player.displayClientMessage(Component.literal("Attached golem filter: ").append(filter.getHoverName()), true);
                }
            } else if (other.getItem() instanceof GolemDecorationItem decorationItem) {
                addDecoration(tag, decorationItem.getDecorationType());
                if (!player.getAbilities().instabuild) {
                    other.shrink(1);
                }
                player.displayClientMessage(Component.literal("Added golem decoration: ").append(decorationItem.getDecorationType().displayName()), true);
            } else if (other.getItem() instanceof GolemTaskMarkerItem) {
                BlockPos pos = GolemTaskMarkerItem.getPosition(other);
                GolemMarkerMode mode = GolemTaskMarkerItem.getMode(other);
                if (pos != null) {
                    GolemTaskMarkerItem.writePosition(tag, mode, pos, GolemTaskMarkerItem.getRadius(other), GolemTaskMarkerItem.getPriority(other));
                    player.displayClientMessage(Component.literal("Attached ").append(mode.displayName()).append(Component.literal(" marker.")), true);
                }
            } else if (player.isShiftKeyDown()) {
                if (player.getOffhandItem() == stack || hand == InteractionHand.OFF_HAND) {
                    GolemCoreType next = GolemCoreType.byName(tag.getString(TAG_CORE)).next();
                    tag.putString(TAG_CORE, next.id());
                    player.displayClientMessage(Component.literal("Golem core: ").append(next.displayName()), true);
                } else {
                    GolemMaterial next = GolemMaterial.byName(tag.getString(TAG_MATERIAL)).next();
                    tag.putString(TAG_MATERIAL, next.id());
                    player.displayClientMessage(Component.literal("Golem body: ").append(next.displayName()), true);
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
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

        if (context.getPlayer().isShiftKeyDown()) {
            // Let shift-use cycle material/core without accidentally spawning.
            return InteractionResult.PASS;
        }

        if (!PlayerThaumData.hasResearch(context.getPlayer(), "GOLEMS")) {
            context.getPlayer().displayClientMessage(Component.literal("Research locked: GOLEMS").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getOrCreateTag();
        GolemMaterial material = GolemMaterial.byName(tag.getString(TAG_MATERIAL));
        GolemCoreType core = GolemCoreType.byName(tag.getString(TAG_CORE));

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        ThaumGolemEntity golem = ThaumcraftMod.THAUM_GOLEM.get().create(level);

        if (golem == null) {
            return InteractionResult.PASS;
        }

        golem.setOwnerUuid(context.getPlayer().getUUID());
        golem.setHomePos(pos);
        golem.setGolemProfile(material, core);
        golem.loadGolemConfiguration(tag);
        golem.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, context.getPlayer().getYRot(), 0.0F);
        level.addFreshEntity(golem);

        if (!context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        context.getPlayer().displayClientMessage(
                Component.literal("Thaumic Golem awakened: ")
                        .append(material.displayName())
                        .append(Component.literal(" / "))
                        .append(core.displayName())
                        .append(Component.literal(". Use the Golem Bell to assign home, recall, or retask."))
                        .withStyle(ChatFormatting.GOLD),
                false
        );
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();
        GolemMaterial material = GolemMaterial.byName(tag.getString(TAG_MATERIAL));
        GolemCoreType core = GolemCoreType.byName(tag.getString(TAG_CORE));
        tooltip.add(Component.literal("Body: ").append(material.displayName()));
        tooltip.add(Component.literal("Core: ").append(core.displayName()));
        tooltip.add(Component.literal("Upgrades: " + describeUpgrades(tag)).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Decorations: " + describeDecorations(tag)).withStyle(ChatFormatting.GOLD));
        if (tag.contains(TAG_FILTER)) {
            ItemStack filter = ItemStack.of(tag.getCompound(TAG_FILTER));
            tooltip.add(Component.literal((tag.getBoolean(TAG_FILTER_ALLOW) ? "Allow" : "Deny") + " filter: ").append(filter.getHoverName()).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.literal("Hold marker/filter/upgrade in other hand and right-click to attach.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Shift + right-click main hand: cycle body").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Shift + right-click off-hand: cycle core").withStyle(ChatFormatting.GRAY));
    }

    private static void addUpgrade(CompoundTag tag, GolemUpgradeType upgradeType) {
        String current = tag.getString(TAG_UPGRADES);
        String id = upgradeType.id();
        for (String part : current.split(",")) {
            if (part.equalsIgnoreCase(id)) {
                return;
            }
        }
        tag.putString(TAG_UPGRADES, current == null || current.isBlank() ? id : current + "," + id);
    }

    private static void addDecoration(CompoundTag tag, GolemDecorationType decorationType) {
        String current = tag.getString(TAG_DECORATIONS);
        String id = decorationType.id();
        for (String part : current.split(",")) {
            if (part.equalsIgnoreCase(id)) {
                return;
            }
        }
        tag.putString(TAG_DECORATIONS, current == null || current.isBlank() ? id : current + "," + id);
    }

    private static String describeUpgrades(CompoundTag tag) {
        String current = tag.getString(TAG_UPGRADES);
        return current == null || current.isBlank() ? "none" : current;
    }

    private static String describeDecorations(CompoundTag tag) {
        String current = tag.getString(TAG_DECORATIONS);
        return current == null || current.isBlank() ? "none" : current;
    }
}
