package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.block.TC4ArcanePressurePlateParity;
import com.darkifov.thaumcraft.blockentity.ArcaneAccessTarget;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/** Iron/gold TC4 keys for owner-bound Arcane Pressure Plates and Arcane Doors. */
public final class ArcaneKeyItem extends Item {
    public static final String TAG_LOCATION = "location";
    public static final String TAG_TYPE = "type";

    // Read-only migration support for keys created by early rebuild releases.
    private static final String OLD_TAG_BOUND = "Bound";
    private static final String OLD_TAG_X = "X";
    private static final String OLD_TAG_Y = "Y";
    private static final String OLD_TAG_Z = "Z";

    private final boolean gold;

    public ArcaneKeyItem(Properties properties, boolean gold) {
        super(properties.stacksTo(64));
        this.gold = gold;
    }

    public boolean isGold() {
        return gold;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ArcaneAccessTarget target)) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;

        ItemStack held = context.getItemInHand();
        // ItemKey#onItemUseFirst used hasTagCompound(), not a validated binding check.
        // Therefore any pre-existing NBT enters the bound-key branch and may not be
        // silently overwritten as a fresh binding.
        if (!hasBindingContainer(held)) {
            if (!target.mayBindKey(player, gold)) return InteractionResult.CONSUME;

            ItemStack bound = new ItemStack(this);
            bind(bound, target.keyBindingPos(), target.keyTargetType());
            deliverBoundCopy(player, held, bound);
            context.getLevel().playSound(null, context.getClickedPos(), TC4Sounds.event("key"),
                    SoundSource.BLOCKS, 1.0F, 0.9F);
            sendOriginalMessage(player, target.keyTargetType() == TC4ArcanePressurePlateParity.KEY_TARGET_DOOR
                    ? "tc.key1" : "tc.key2");
            return InteractionResult.CONSUME;
        }

        if (!matches(held, target.keyBindingPos())) {
            sendOriginalMessage(player, "tc.key7");
            return InteractionResult.CONSUME;
        }
        if (target.hasAccess(player)) {
            sendOriginalMessage(player, "tc.key8");
            return InteractionResult.CONSUME;
        }
        if (!target.addAccess(player, gold)) {
            sendOriginalMessage(player, "tc.key8");
            return InteractionResult.CONSUME;
        }

        if (!player.getAbilities().instabuild) held.shrink(1);
        context.getLevel().playSound(null, context.getClickedPos(), TC4Sounds.event("key"),
                SoundSource.BLOCKS, 1.0F, 1.1F);
        String base = target.keyTargetType() == TC4ArcanePressurePlateParity.KEY_TARGET_DOOR
                ? "tc.key3" : "tc.key5";
        sendOriginalMessage(player, base, gold
                ? (target.keyTargetType() == TC4ArcanePressurePlateParity.KEY_TARGET_DOOR
                    ? "tc.key4" : "tc.key6")
                : null);
        return InteractionResult.CONSUME;
    }

    /** Original order: insert/drop the new bound copy, then consume one blank key. */
    private static void deliverBoundCopy(Player player, ItemStack held, ItemStack bound) {
        if (!player.addItem(bound)) player.drop(bound, false);
        if (!player.getAbilities().instabuild) held.shrink(1);
    }

    public static void bind(ItemStack stack, BlockPos pos, byte targetType) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_LOCATION, TC4ArcanePressurePlateParity.legacyLocation(
                pos.getX(), pos.getY(), pos.getZ()));
        tag.putByte(TAG_TYPE, targetType);
        tag.remove(OLD_TAG_BOUND);
        tag.remove(OLD_TAG_X);
        tag.remove(OLD_TAG_Y);
        tag.remove(OLD_TAG_Z);
    }

    /** Exact legacy branch condition: even an empty or unrelated tag compound counts as bound. */
    public static boolean hasBindingContainer(ItemStack stack) {
        return stack.getTag() != null;
    }

    /** A usable original or early-rebuild binding exists. */
    public static boolean isBound(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && (tag.contains(TAG_LOCATION)
                || (tag.getBoolean(OLD_TAG_BOUND) && tag.contains(OLD_TAG_X)
                    && tag.contains(OLD_TAG_Y) && tag.contains(OLD_TAG_Z)));
    }

    public static String boundLocation(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return "";
        if (tag.contains(TAG_LOCATION)) return tag.getString(TAG_LOCATION);
        if (tag.getBoolean(OLD_TAG_BOUND) && tag.contains(OLD_TAG_X)
                && tag.contains(OLD_TAG_Y) && tag.contains(OLD_TAG_Z)) {
            return TC4ArcanePressurePlateParity.legacyLocation(
                    tag.getInt(OLD_TAG_X), tag.getInt(OLD_TAG_Y), tag.getInt(OLD_TAG_Z));
        }
        return "";
    }

    public static byte boundType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? TC4ArcanePressurePlateParity.KEY_TARGET_DOOR : tag.getByte(TAG_TYPE);
    }

    public static boolean matches(ItemStack stack, BlockPos pos) {
        return isBound(stack) && TC4ArcanePressurePlateParity.locationMatches(boundLocation(stack),
                pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasBindingContainer(stack) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!isBound(stack)) return;
        ChatFormatting[] style = {ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC};
        tooltip.add(Component.translatable("tc.key9").withStyle(style));
        tooltip.add(Component.translatable(boundType(stack) == TC4ArcanePressurePlateParity.KEY_TARGET_DOOR
                ? "tc.key10" : "tc.key11").withStyle(style));
        tooltip.add(Component.literal(TC4ArcanePressurePlateParity.tooltipLocation(boundLocation(stack)))
                .withStyle(style));
    }

    private static void sendOriginalMessage(Player player, String first) {
        sendOriginalMessage(player, first, null);
    }

    private static void sendOriginalMessage(Player player, String first, String second) {
        Component message = Component.translatable(first);
        if (second != null) message = message.copy().append(Component.translatable(second));
        player.displayClientMessage(message.copy().withStyle(ChatFormatting.DARK_PURPLE,
                ChatFormatting.ITALIC), false);
    }
}
