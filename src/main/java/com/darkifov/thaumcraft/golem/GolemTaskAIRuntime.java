package com.darkifov.thaumcraft.golem;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Stage197 explicit task table copied from original TC4 class layout.
 * The modern entity invokes compact Forge 1.19.2 adapters, but the task names,
 * priority groups and core mappings stay traceable to the original AI classes.
 */
public final class GolemTaskAIRuntime {
    public static final int ORIGINAL_GOLEM_DELAY_TICKS = 5;
    public static final double ORIGINAL_HOME_INTERACT_DISTANCE_SQ = 5.0D;
    public static final int ORIGINAL_CHEST_INTERACT_TICKS = 5;

    public enum OriginalTask {
        AIHomeReplace(3),
        AIHomeTake(3),
        AIHomeDrop(3),
        AIItemPickup(2),
        AIFillGoto(2),
        AIFillTake(3),
        AIEmptyGoto(2),
        AIEmptyPlace(3),
        AISortingPlace(3);

        private final int mutexBits;

        OriginalTask(int mutexBits) {
            this.mutexBits = mutexBits;
        }

        public int mutexBits() {
            return mutexBits;
        }
    }

    private static final Map<GolemCoreType, List<OriginalTask>> TASKS = new EnumMap<>(GolemCoreType.class);

    static {
        TASKS.put(GolemCoreType.FILL, List.of(OriginalTask.AIFillGoto, OriginalTask.AIFillTake, OriginalTask.AIHomeDrop));
        TASKS.put(GolemCoreType.EMPTY, List.of(OriginalTask.AIEmptyGoto, OriginalTask.AIEmptyPlace, OriginalTask.AIHomeDrop));
        TASKS.put(GolemCoreType.GATHER, List.of(OriginalTask.AIItemPickup, OriginalTask.AIHomeDrop));
        TASKS.put(GolemCoreType.SORTING, List.of(OriginalTask.AIItemPickup, OriginalTask.AISortingPlace, OriginalTask.AIHomeReplace));
        TASKS.put(GolemCoreType.USE, List.of(OriginalTask.AIHomeTake, OriginalTask.AIHomeReplace));
        TASKS.put(GolemCoreType.LIQUID, List.of(OriginalTask.AIHomeTake, OriginalTask.AIHomeDrop));
        TASKS.put(GolemCoreType.ESSENTIA, List.of(OriginalTask.AIHomeTake, OriginalTask.AIHomeDrop));
    }

    private GolemTaskAIRuntime() {
    }

    public static List<OriginalTask> tasksFor(GolemCoreType core) {
        return TASKS.getOrDefault(core, List.of());
    }

    public static boolean originalDelayReady(int tickCount) {
        return tickCount % ORIGINAL_GOLEM_DELAY_TICKS == 0;
    }

    public static String diagnostic(GolemCoreType core) {
        StringBuilder builder = new StringBuilder();
        for (OriginalTask task : tasksFor(core)) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(task.name());
        }
        return builder.length() == 0 ? "vanilla/entity-goal" : builder.toString();
    }
}
