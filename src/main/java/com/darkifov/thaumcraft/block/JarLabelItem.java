package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.jar.JarTubeInteractionRuntime;
import net.minecraft.world.InteractionHand;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class JarLabelItem extends Item {
    private static final Aspect[] CYCLE = new Aspect[]{
            Aspect.AER,
            Aspect.TERRA,
            Aspect.IGNIS,
            Aspect.AQUA,
            Aspect.ORDO,
            Aspect.PERDITIO,
            Aspect.PRAECANTATIO,
            Aspect.LUX,
            Aspect.POTENTIA,
            Aspect.VACUOS
    };

    public JarLabelItem(Properties properties) {
        super(properties);
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

    private Aspect nextAspect(Aspect current, boolean reverse) {
        if (current == null) {
            return reverse ? CYCLE[CYCLE.length - 1] : CYCLE[0];
        }

        for (int i = 0; i < CYCLE.length; i++) {
            if (CYCLE[i] == current) {
                int next = reverse ? i - 1 : i + 1;

                if (next < 0) {
                    next = CYCLE.length - 1;
                }

                if (next >= CYCLE.length) {
                    next = 0;
                }

                return CYCLE[next];
            }
        }

        return CYCLE[0];
    }
}
