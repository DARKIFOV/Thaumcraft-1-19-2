package com.darkifov.thaumcraft.jar;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.block.EssentiaPhialItem;
import com.darkifov.thaumcraft.block.JarLabelItem;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Original TC4 label/filter interaction adapter shared by jars and filtered tubes. */
public final class JarTubeInteractionRuntime {
    public static final String NBT_ASPECT_FILTER = "AspectFilter";
    public static final String NBT_ASPECT = "Aspect";
    public static final String NBT_AMOUNT = "Amount";
    public static final String NBT_FACING = "facing";

    private JarTubeInteractionRuntime() {}

    public static boolean applyLabelToJar(EssentiaJarBlockEntity jar, Player player, ItemStack label, Direction clickedFace) {
        if (jar == null || player == null || label == null || label.isEmpty()
                || !(label.getItem() instanceof JarLabelItem) || jar.hasFilter()) {
            return false;
        }

        Aspect aspect = JarLabelItem.getAspect(label);
        if (aspect == null && jar.amount() > 0) aspect = jar.storedAspect();
        // TC4 leaves a blank label untouched when both the jar and label are untyped.
        if (aspect == null) return true;

        jar.setLabelFacing(Direction.from3DDataValue(TC4EssentiaJarParity.labelFacingDataValue(player.getYRot())));
        jar.setFilterAspect(aspect);
        if (!player.getAbilities().instabuild) label.shrink(1);
        if (jar.getLevel() != null) {
            jar.getLevel().playSound(null, jar.getBlockPos(), TC4Sounds.event("jar"), SoundSource.BLOCKS, 0.4F, 1.0F);
        }
        return true;
    }

    public static boolean removeLabelFromJar(EssentiaJarBlockEntity jar, Player player, Direction clickedFace) {
        if (jar == null || player == null || !player.isShiftKeyDown() || !jar.hasFilter()
                || clickedFace == null || clickedFace != jar.labelFacing()) {
            return false;
        }

        jar.clearFilter();
        if (jar.getLevel() != null) {
            Direction face = clickedFace;
            ItemStack drop = new ItemStack(com.darkifov.thaumcraft.ThaumcraftMod.JAR_LABEL.get());
            ItemEntity item = new ItemEntity(jar.getLevel(),
                    jar.getBlockPos().getX() + 0.5D + face.getStepX() / 3.0D,
                    jar.getBlockPos().getY() + 0.5D,
                    jar.getBlockPos().getZ() + 0.5D + face.getStepZ() / 3.0D,
                    drop);
            jar.getLevel().addFreshEntity(item);
            jar.getLevel().playSound(null, jar.getBlockPos(), TC4Sounds.event("page"), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return true;
    }

    public static boolean applyFilterToTube(EssentiaTubeBlockEntity tube, Player player, ItemStack held, ItemStack otherHand) {
        if (tube == null || player == null) return false;
        if (player.isShiftKeyDown()) {
            tube.setAspectFilter(null);
            return true;
        }
        Aspect aspect = JarLabelItem.getAspect(held);
        if (aspect == null) aspect = aspectFromPhial(held);
        if (aspect == null) aspect = aspectFromPhial(otherHand);
        if (aspect == null) return true;
        tube.setAspectFilter(aspect);
        return true;
    }

    public static Aspect aspectFromPhial(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof EssentiaPhialItem)) return null;
        return EssentiaPhialItem.isFilled(stack) ? EssentiaPhialItem.getAspect(stack) : null;
    }
}
