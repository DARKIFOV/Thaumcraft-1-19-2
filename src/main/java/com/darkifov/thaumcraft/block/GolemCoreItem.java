package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.darkifov.thaumcraft.golem.GolemCoreType;
import com.darkifov.thaumcraft.golem.GolemMarkerMode;
import com.darkifov.thaumcraft.golem.GolemMaterial;
import com.darkifov.thaumcraft.golem.GolemUpgradeType;
import com.darkifov.thaumcraft.golem.GolemOriginalRuntime;
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
                if (addUpgrade(tag, upgradeItem.getUpgradeType())) {
                    if (!player.getAbilities().instabuild) {
                        other.shrink(1);
                    }
                    int count = GolemOriginalRuntime.upgradeAmount(
                            tag.getByteArray(GolemOriginalRuntime.NBT_UPGRADES), upgradeItem.getUpgradeType());
                    player.displayClientMessage(Component.literal("Added golem upgrade: ")
                            .append(upgradeItem.getUpgradeType().displayName())
                            .append(Component.literal(" (" + count + "/2)")), true);
                } else {
                    player.displayClientMessage(Component.literal("No free upgrade slot, or that upgrade is already installed twice.")
                            .withStyle(ChatFormatting.RED), true);
                }
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
                if (addDecoration(tag, decorationItem.getDecorationType())) {
                    if (!player.getAbilities().instabuild) {
                        other.shrink(1);
                    }
                    player.displayClientMessage(Component.literal("Added golem decoration: ")
                            .append(decorationItem.getDecorationType().displayName()), true);
                } else {
                    player.displayClientMessage(Component.literal("That decoration is already installed or conflicts with another one.")
                            .withStyle(ChatFormatting.RED), true);
                }
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
                    tag.putByte(GolemOriginalRuntime.NBT_CORE, (byte) next.originalId());
                    player.displayClientMessage(Component.literal("Golem core: ").append(next.displayName()).append(Component.literal(" [TC4 meta " + next.originalId() + "]")), true);
                } else {
                    GolemMaterial next = GolemMaterial.byName(tag.getString(TAG_MATERIAL)).next();
                    tag.putString(TAG_MATERIAL, next.id());
                    tag.putByte(GolemOriginalRuntime.NBT_GOLEM_TYPE, (byte) next.ordinal());
                    byte[] normalized = GolemOriginalRuntime.normalizeUpgradeSlots(
                            tag.getByteArray(GolemOriginalRuntime.NBT_UPGRADES), next,
                            tag.getBoolean(GolemOriginalRuntime.NBT_ADVANCED));
                    tag.putByteArray(GolemOriginalRuntime.NBT_UPGRADES, normalized);
                    tag.putString(TAG_UPGRADES, GolemOriginalRuntime.upgradeDescription(normalized));
                    player.displayClientMessage(Component.literal("Golem body: ").append(next.displayName()).append(Component.literal(" [TC4 type " + next.ordinal() + "]")), true);
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
        GolemMaterial material = tag.contains(GolemOriginalRuntime.NBT_GOLEM_TYPE)
                ? GolemMaterial.values()[Math.max(0, Math.min(GolemMaterial.values().length - 1, tag.getByte(GolemOriginalRuntime.NBT_GOLEM_TYPE) & 255))]
                : GolemMaterial.byName(tag.getString(TAG_MATERIAL));
        GolemCoreType core = tag.contains(GolemOriginalRuntime.NBT_CORE)
                ? GolemCoreType.byOriginalId(tag.getByte(GolemOriginalRuntime.NBT_CORE))
                : GolemCoreType.byName(tag.getString(TAG_CORE));

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        ThaumGolemEntity golem = ThaumcraftMod.THAUM_GOLEM.get().create(level);

        if (golem == null) {
            return InteractionResult.PASS;
        }

        golem.setOwnerUuid(context.getPlayer().getUUID());
        golem.setHomePos(pos);
        golem.setHomeFacing(context.getClickedFace().ordinal());
        golem.setAdvancedGolem(tag.getBoolean(GolemOriginalRuntime.NBT_ADVANCED));
        golem.setGolemProfile(material, core);
        golem.loadGolemConfiguration(tag);
        if (stack.hasCustomHoverName()) {
            golem.setCustomName(stack.getHoverName());
        }
        golem.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, context.getPlayer().getYRot(), 0.0F);
        if (!level.noCollision(golem) || !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
            context.getPlayer().displayClientMessage(Component.literal("Not enough room to place this golem.")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }
        if (!level.addFreshEntity(golem)) {
            return InteractionResult.PASS;
        }

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
        GolemMaterial material = tag.contains(GolemOriginalRuntime.NBT_GOLEM_TYPE)
                ? GolemMaterial.values()[Math.max(0, Math.min(GolemMaterial.values().length - 1, tag.getByte(GolemOriginalRuntime.NBT_GOLEM_TYPE) & 255))]
                : GolemMaterial.byName(tag.getString(TAG_MATERIAL));
        GolemCoreType core = tag.contains(GolemOriginalRuntime.NBT_CORE)
                ? GolemCoreType.byOriginalId(tag.getByte(GolemOriginalRuntime.NBT_CORE))
                : GolemCoreType.byName(tag.getString(TAG_CORE));
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

    private static boolean addUpgrade(CompoundTag tag, GolemUpgradeType upgradeType) {
        GolemMaterial material = tag.contains(GolemOriginalRuntime.NBT_GOLEM_TYPE)
                ? GolemMaterial.values()[Math.max(0, Math.min(GolemMaterial.values().length - 1,
                tag.getByte(GolemOriginalRuntime.NBT_GOLEM_TYPE) & 255))]
                : GolemMaterial.byName(tag.getString(TAG_MATERIAL));
        byte[] slots = GolemOriginalRuntime.normalizeUpgradeSlots(
                tag.getByteArray(GolemOriginalRuntime.NBT_UPGRADES), material,
                tag.getBoolean(GolemOriginalRuntime.NBT_ADVANCED));
        if (!GolemOriginalRuntime.installUpgrade(slots, upgradeType)) {
            return false;
        }
        tag.putByteArray(GolemOriginalRuntime.NBT_UPGRADES, slots);
        tag.putString(TAG_UPGRADES, GolemOriginalRuntime.upgradeDescription(slots));
        return true;
    }

    private static boolean addDecoration(CompoundTag tag, GolemDecorationType decorationType) {
        String current = tag.getString(TAG_DECORATIONS);
        java.util.EnumSet<GolemDecorationType> installed = java.util.EnumSet.noneOf(GolemDecorationType.class);
        for (String part : current.split(",")) {
            if (!part.isBlank()) {
                installed.add(GolemDecorationType.byName(part));
            }
        }
        if (installed.contains(decorationType)) {
            return false;
        }
        if ((decorationType == GolemDecorationType.FEZ || decorationType == GolemDecorationType.TOP_HAT)
                && (installed.contains(GolemDecorationType.FEZ) || installed.contains(GolemDecorationType.TOP_HAT))) {
            return false;
        }
        if ((decorationType == GolemDecorationType.GLASSES || decorationType == GolemDecorationType.VISOR)
                && (installed.contains(GolemDecorationType.GLASSES) || installed.contains(GolemDecorationType.VISOR))) {
            return false;
        }
        if ((decorationType == GolemDecorationType.BOWTIE || decorationType == GolemDecorationType.ARMOR)
                && (installed.contains(GolemDecorationType.BOWTIE) || installed.contains(GolemDecorationType.ARMOR))) {
            return false;
        }
        installed.add(decorationType);
        StringBuilder names = new StringBuilder();
        StringBuilder code = new StringBuilder();
        for (GolemDecorationType type : GolemDecorationType.values()) {
            if (!installed.contains(type)) continue;
            if (names.length() > 0) names.append(',');
            names.append(type.id());
            code.append(switch (type) {
                case TOP_HAT -> 'H';
                case GLASSES -> 'G';
                case BOWTIE -> 'B';
                case FEZ -> 'F';
                case DART_LAUNCHER -> 'R';
                case VISOR -> 'V';
                case ARMOR -> 'P';
                case MACE -> 'M';
                case WIRELESS_BACKPACK -> 'W';
            });
        }
        tag.putString(TAG_DECORATIONS, names.toString());
        tag.putString(GolemOriginalRuntime.NBT_DECORATION, code.toString());
        return true;
    }

    private static String describeUpgrades(CompoundTag tag) {
        if (tag.contains(GolemOriginalRuntime.NBT_UPGRADES)) {
            return GolemOriginalRuntime.upgradeDescription(tag.getByteArray(GolemOriginalRuntime.NBT_UPGRADES));
        }
        String current = tag.getString(TAG_UPGRADES);
        return current == null || current.isBlank() ? "none" : current;
    }

    private static String describeDecorations(CompoundTag tag) {
        String current = tag.getString(TAG_DECORATIONS);
        return current == null || current.isBlank() ? "none" : current;
    }
}
