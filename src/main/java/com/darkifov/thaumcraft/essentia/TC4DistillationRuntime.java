package com.darkifov.thaumcraft.essentia;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.blockentity.AlchemicalFurnaceBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Map;

/**
 * Stage483-502 strict TC4 distillation adapter.
 *
 * Original TC4 does not treat the alchemical furnace itself as a tube source. Essentia is
 * first distilled into one of the alembics stacked above the furnace, and only the alembic
 * is a transport source for pipes/jars. This 1.19.2 bridge preserves that data flow while
 * using the existing block-entity ticking system.
 */
public final class TC4DistillationRuntime {
    public static final int ORIGINAL_MAX_ALEMBICS_ABOVE_FURNACE = 5;
    public static final int ORIGINAL_DISTILLATION_INTERVAL_TICKS = 5;
    public static final int ORIGINAL_DISTILLATION_STEP = 1;

    private TC4DistillationRuntime() {
    }

    public static void tickFurnaceToAlembics(Level level, BlockPos furnacePos, AlchemicalFurnaceBlockEntity furnace) {
        if (level == null || level.isClientSide || furnace == null) {
            return;
        }
        if (level.getGameTime() % ORIGINAL_DISTILLATION_INTERVAL_TICKS != 0L) {
            return;
        }
        if (furnace.aspects().isEmpty()) {
            return;
        }

        for (Map.Entry<Aspect, Integer> entry : new ArrayList<>(furnace.aspects().entries().entrySet())) {
            Aspect aspect = entry.getKey();
            if (aspect == null || entry.getValue() <= 0) {
                continue;
            }
            AlembicBlockEntity target = findAcceptingAlembic(level, furnacePos, aspect);
            if (target == null) {
                continue;
            }
            int removed = furnace.removeUpTo(aspect, ORIGINAL_DISTILLATION_STEP);
            if (removed <= 0) {
                return;
            }
            int accepted = target.addEssentia(aspect, removed);
            if (accepted < removed) {
                furnace.aspects().add(aspect, removed - accepted);
                furnace.setChangedAndSync();
            }
            return;
        }
    }

    public static AlembicBlockEntity findAcceptingAlembic(Level level, BlockPos furnacePos, Aspect aspect) {
        if (level == null || furnacePos == null || aspect == null) {
            return null;
        }
        for (int offset = 1; offset <= ORIGINAL_MAX_ALEMBICS_ABOVE_FURNACE; offset++) {
            BlockEntity be = level.getBlockEntity(furnacePos.above(offset));
            if (be instanceof AlembicBlockEntity alembic && alembic.canAccept(aspect) && alembic.spaceLeft() > 0) {
                return alembic;
            }
        }
        return null;
    }

    public static int countAlembicsAbove(Level level, BlockPos furnacePos) {
        if (level == null || furnacePos == null) {
            return 0;
        }
        int count = 0;
        for (int offset = 1; offset <= ORIGINAL_MAX_ALEMBICS_ABOVE_FURNACE; offset++) {
            if (level.getBlockEntity(furnacePos.above(offset)) instanceof AlembicBlockEntity) {
                count++;
            }
        }
        return count;
    }
}
