package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.TC4NodeJarRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Migration-only replacement for the old direct aura_node BlockItem.
 * TC4's supported player item is a Node in a Jar; raw nodes must not be
 * placeable or exposed in the creative inventory.
 */
public final class AuraNodeLegacyItem extends Item {
    public AuraNodeLegacyItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack legacy = player.getItemInHand(hand);
        CompoundTag node = findNodeData(legacy);
        if (node == null) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal(
                        "Raw aura nodes are not obtainable items. Capture the node with a Node in a Jar.")
                        .withStyle(ChatFormatting.DARK_PURPLE), true);
            }
            return InteractionResultHolder.fail(legacy);
        }

        ItemStack converted = new ItemStack(ThaumcraftMod.NODE_JAR.get());
        converted.getOrCreateTag().put(TC4NodeJarRuntime.TAG_NODE_JAR, node.copy());
        if (!level.isClientSide) {
            player.setItemInHand(hand, converted);
            player.displayClientMessage(Component.literal("Legacy aura node converted to Node in a Jar.")
                    .withStyle(ChatFormatting.AQUA), true);
        }
        return InteractionResultHolder.sidedSuccess(converted, level.isClientSide);
    }

    @Nullable
    private static CompoundTag findNodeData(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null) {
            return null;
        }
        if (root.contains(TC4NodeJarRuntime.TAG_NODE_JAR, Tag.TAG_COMPOUND)) {
            return root.getCompound(TC4NodeJarRuntime.TAG_NODE_JAR);
        }
        if (root.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
            CompoundTag nested = root.getCompound("BlockEntityTag");
            if (nested.contains("Aspects", Tag.TAG_COMPOUND)) {
                return nested;
            }
        }
        return root.contains("Aspects", Tag.TAG_COMPOUND) ? root : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Legacy migration item — not a placeable TC4 node.")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Use to convert stored node NBT into a Node in a Jar.")
                .withStyle(ChatFormatting.GRAY));
    }
}
