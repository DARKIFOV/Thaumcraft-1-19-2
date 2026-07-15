package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.jar.JarTubeInteractionRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * TC4 jar label. A blank label has no NBT; an aspect label stores the original
 * essentia filter in the Aspect tag. This replaces TC4's itemResource:13
 * metadata/NBT variants without creating 48 registry placeholder items.
 */
public class JarLabelItem extends Item {
    private static final String TAG_ASPECT = "Aspect";
    private static final Aspect[] CYCLE = Aspect.values();

    public JarLabelItem(Properties properties) {
        super(properties);
    }

    public static Aspect getAspect(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        return tag == null ? null : Aspect.byId(tag.getString(TAG_ASPECT));
    }

    public static boolean isBlank(ItemStack stack) {
        return getAspect(stack) == null;
    }

    public static void setAspect(ItemStack stack, Aspect aspect) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        if (aspect == null) {
            clearAspect(stack);
            return;
        }
        stack.getOrCreateTag().putString(TAG_ASPECT, aspect.id());
    }

    public static void clearAspect(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getTag() == null) {
            return;
        }
        CompoundTag tag = stack.getTag();
        tag.remove(TAG_ASPECT);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    public static ItemStack withAspect(Aspect aspect) {
        ItemStack stack = new ItemStack(ThaumcraftMod.JAR_LABEL.get());
        setAspect(stack, aspect);
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide || context.getPlayer() == null) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(pos);
        BlockEntity blockEntity = context.getLevel().getBlockEntity(pos);

        if (!(blockEntity instanceof EssentiaJarBlockEntity jar)) {
            return InteractionResult.PASS;
        }

        if (JarTubeInteractionRuntime.applyLabelToJar(jar, context.getPlayer(), context.getItemInHand(),
                context.getPlayer().getItemInHand(context.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND),
                context.getClickedFace())) {
            return InteractionResult.CONSUME;
        }

        if (!state.is(ThaumcraftMod.FILTERED_ESSENTIA_JAR.get())) {
            context.getPlayer().displayClientMessage(Component.literal("Jar Labels only configure Filtered Essentia Jars.").withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        Aspect next = nextAspect(jar.filterAspect(), context.getPlayer().isShiftKeyDown());
        jar.setFilterAspect(next);

        context.getPlayer().displayClientMessage(
                Component.literal("Filtered Jar set to: ").append(Component.literal(next.displayName()).withStyle(next.color())),
                false
        );

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        Aspect aspect = getAspect(stack);
        if (aspect == null) {
            tooltip.add(Component.literal("Blank essentia label").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal(aspect.displayName()).withStyle(aspect.color()));
            tooltip.add(Component.literal("AspectFilter: " + aspect.id()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private Aspect nextAspect(Aspect current, boolean reverse) {
        if (current == null) {
            return reverse ? CYCLE[CYCLE.length - 1] : CYCLE[0];
        }

        for (int i = 0; i < CYCLE.length; i++) {
            if (CYCLE[i] == current) {
                int next = reverse ? i - 1 : i + 1;
                if (next < 0) next = CYCLE.length - 1;
                if (next >= CYCLE.length) next = 0;
                return CYCLE[next];
            }
        }
        return CYCLE[0];
    }
}
