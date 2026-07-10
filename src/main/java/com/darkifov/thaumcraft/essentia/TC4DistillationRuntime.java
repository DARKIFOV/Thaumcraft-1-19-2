package com.darkifov.thaumcraft.essentia;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.blockentity.AlchemicalFurnaceBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Direct TileAlchemyFurnace -> TileAlembic distribution pass.
 *
 * TC4 updates the stack every 40 ticks (20 while burning alumentum). Existing
 * aspect alembics are topped up first, one point each, and only then the first
 * empty alembic receives a random not-yet-served aspect or its label-filtered
 * aspect. The furnace itself is never exposed as a tube source.
 */
public final class TC4DistillationRuntime {
    public static final int ORIGINAL_MAX_ALEMBICS_ABOVE_FURNACE = 5;
    public static final int ORIGINAL_DISTILLATION_INTERVAL_TICKS = 40;
    public static final int ORIGINAL_ALUMENTUM_INTERVAL_TICKS = 20;
    public static final int ORIGINAL_DISTILLATION_STEP = 1;

    private TC4DistillationRuntime() {
    }

    public static void tickFurnaceToAlembics(Level level, BlockPos furnacePos, AlchemicalFurnaceBlockEntity furnace) {
        if (level == null || level.isClientSide || furnace == null || furnace.aspects().isEmpty()) {
            return;
        }
        int interval = Math.max(1, furnace.nextDistillationInterval());
        if (level.getGameTime() % interval != 0L) {
            return;
        }

        List<AlembicBlockEntity> stack = contiguousAlembics(level, furnacePos);
        if (stack.isEmpty()) {
            return;
        }

        EnumSet<Aspect> served = EnumSet.noneOf(Aspect.class);
        for (AlembicBlockEntity alembic : stack) {
            Aspect stored = alembic.storedAspect();
            if (stored == null || alembic.spaceLeft() <= 0 || furnace.aspects().get(stored) <= 0) {
                continue;
            }
            int removed = furnace.removeUpTo(stored, ORIGINAL_DISTILLATION_STEP);
            if (removed <= 0) {
                continue;
            }
            int accepted = alembic.addEssentia(stored, removed);
            if (accepted < removed) {
                furnace.aspects().add(stored, removed - accepted);
                furnace.setChangedAndSync();
            }
            if (accepted > 0) {
                served.add(stored);
            }
        }

        for (AlembicBlockEntity alembic : stack) {
            if (alembic.storedAspect() != null && !alembic.aspects().isEmpty()) {
                continue;
            }
            Aspect selected = selectForEmptyAlembic(level, furnace, alembic, served);
            if (selected == null) {
                continue;
            }
            int removed = furnace.removeUpTo(selected, ORIGINAL_DISTILLATION_STEP);
            if (removed <= 0) {
                return;
            }
            int accepted = alembic.addEssentia(selected, removed);
            if (accepted < removed) {
                furnace.aspects().add(selected, removed - accepted);
                furnace.setChangedAndSync();
            }
            return;
        }
    }

    private static Aspect selectForEmptyAlembic(Level level, AlchemicalFurnaceBlockEntity furnace,
                                                  AlembicBlockEntity alembic, EnumSet<Aspect> served) {
        Aspect filter = alembic.aspectFilter();
        if (filter != null) {
            return furnace.aspects().get(filter) > 0 ? filter : null;
        }
        List<Aspect> candidates = new ArrayList<>();
        for (Map.Entry<Aspect, Integer> entry : furnace.aspects().entries().entrySet()) {
            if (entry.getValue() > 0 && !served.contains(entry.getKey()) && alembic.canAccept(entry.getKey())) {
                candidates.add(entry.getKey());
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(level.random.nextInt(candidates.size()));
    }

    private static List<AlembicBlockEntity> contiguousAlembics(Level level, BlockPos furnacePos) {
        List<AlembicBlockEntity> result = new ArrayList<>();
        for (int offset = 1; offset <= ORIGINAL_MAX_ALEMBICS_ABOVE_FURNACE; offset++) {
            BlockEntity be = level.getBlockEntity(furnacePos.above(offset));
            if (!(be instanceof AlembicBlockEntity alembic)) {
                break;
            }
            result.add(alembic);
        }
        return result;
    }

    public static AlembicBlockEntity findAcceptingAlembic(Level level, BlockPos furnacePos, Aspect aspect) {
        if (level == null || furnacePos == null || aspect == null) {
            return null;
        }
        for (AlembicBlockEntity alembic : contiguousAlembics(level, furnacePos)) {
            if (alembic.canAccept(aspect) && alembic.spaceLeft() > 0) {
                return alembic;
            }
        }
        return null;
    }

    public static int countAlembicsAbove(Level level, BlockPos furnacePos) {
        return level == null || furnacePos == null ? 0 : contiguousAlembics(level, furnacePos).size();
    }
}
