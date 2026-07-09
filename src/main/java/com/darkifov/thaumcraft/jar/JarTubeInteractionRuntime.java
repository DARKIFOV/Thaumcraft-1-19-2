package com.darkifov.thaumcraft.jar;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.block.EssentiaPhialItem;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/**
 * Stage202: original jar/tube label and filter interaction parity adapter.
 * Original TC4 stores filters as AspectFilter on TileJarFillable/TileTubeFilter and lets
 * labels/wand-like interaction set or clear filters without creating a new GUI.
 */
public final class JarTubeInteractionRuntime {
    public static final String NBT_ASPECT_FILTER = "AspectFilter";
    public static final String NBT_ASPECT = "Aspect";
    public static final String NBT_AMOUNT = "Amount";
    public static final String NBT_FACING = "facing";

    private JarTubeInteractionRuntime() {
    }

    public static boolean applyLabelToJar(EssentiaJarBlockEntity jar, Player player, ItemStack held, ItemStack otherHand) {
        return applyLabelToJar(jar, player, held, otherHand, null);
    }

    public static boolean applyLabelToJar(EssentiaJarBlockEntity jar, Player player, ItemStack held, ItemStack otherHand, Direction clickedFace) {
        if (jar == null || player == null) {
            return false;
        }
        if (clickedFace != null) {
            jar.setLabelFacing(clickedFace);
        }
        if (player.isShiftKeyDown()) {
            jar.clearFilter();
            player.displayClientMessage(Component.literal("Jar AspectFilter cleared.").withStyle(ChatFormatting.GRAY), false);
            return true;
        }
        Aspect aspect = aspectFromPhial(otherHand);
        if (aspect == null) {
            aspect = jar.storedAspect();
        }
        if (aspect == null) {
            player.displayClientMessage(Component.literal("Jar has no stored essentia; use a filled phial in the other hand to set AspectFilter.").withStyle(ChatFormatting.GRAY), false);
            return true;
        }
        jar.setFilterAspect(aspect);
        final Aspect finalAspect = aspect;
        player.displayClientMessage(Component.literal("Jar AspectFilter set to ")
                .append(Component.literal(finalAspect.displayName()).withStyle(style -> style.withColor(finalAspect.textColor()))), false);
        return true;
    }

    public static boolean applyFilterToTube(EssentiaTubeBlockEntity tube, Player player, ItemStack held, ItemStack otherHand) {
        if (tube == null || player == null) {
            return false;
        }
        if (player.isShiftKeyDown()) {
            tube.setAspectFilter(null);
            player.displayClientMessage(Component.literal("Tube AspectFilter cleared.").withStyle(ChatFormatting.GRAY), false);
            return true;
        }
        Aspect aspect = aspectFromPhial(held);
        if (aspect == null) {
            aspect = aspectFromPhial(otherHand);
        }
        if (aspect == null) {
            player.displayClientMessage(Component.literal("Use a filled essentia phial with the label/filter interaction to set tube AspectFilter.").withStyle(ChatFormatting.GRAY), false);
            return true;
        }
        tube.setAspectFilter(aspect);
        final Aspect finalAspect = aspect;
        player.displayClientMessage(Component.literal(tube.subtype().originalClassName() + " AspectFilter set to ")
                .append(Component.literal(finalAspect.displayName()).withStyle(style -> style.withColor(finalAspect.textColor()))), false);
        return true;
    }

    public static Aspect aspectFromPhial(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof EssentiaPhialItem)) {
            return null;
        }
        return EssentiaPhialItem.isFilled(stack) ? EssentiaPhialItem.getAspect(stack) : null;
    }
}
