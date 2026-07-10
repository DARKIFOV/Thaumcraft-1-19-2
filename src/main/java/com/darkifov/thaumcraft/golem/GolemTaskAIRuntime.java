package com.darkifov.thaumcraft.golem;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Explicit task table copied from TC4 4.2.3.5 EntityGolemBase.setupGolem.
 * The 1.19.2 entity uses compact server adapters, but priority order and task
 * composition remain traceable to the original AI classes.
 */
public final class GolemTaskAIRuntime {
    public static final int ORIGINAL_GOLEM_DELAY_TICKS = 5;
    public static final double ORIGINAL_HOME_INTERACT_DISTANCE_SQ = 5.0D;
    public static final int ORIGINAL_CHEST_INTERACT_TICKS = 5;
    public static final int ORIGINAL_HOME_DROP_THROW_LOCK_TICKS = 200;
    /** TC4 Config.golemIgnoreDelay default, clamped to at least 1000ms. */
    public static final long ORIGINAL_GOLEM_IGNORE_DELAY_MS = 10_000L;

    public enum OriginalTask {
        AIAvoidCreeperSwell(0, 1),
        AIHomeReplace(0, 3),
        AIHomePlace(1, 3),
        AIHomeTake(4, 3),
        AIHomeTakeSorting(4, 3),
        AIHomeDrop(2, 3),
        AIItemPickup(2, 2),
        AIFillGoto(4, 2),
        AIFillTake(3, 3),
        AIEmptyGoto(3, 2),
        AIEmptyPlace(1, 3),
        AIEmptyDrop(2, 3),
        AISortingPlace(1, 3),
        AISortingGoto(3, 2),
        AIHarvestCrops(2, 3),
        AIHarvestLogs(2, 3),
        AIGolemAttackOnCollide(3, 3),
        AIDartAttack(2, 3),
        AINearestAttackableTarget(2, 1),
        AINearestButcherTarget(1, 3),
        AIHurtByTarget(1, 1),
        AILiquidEmpty(1, 3),
        AILiquidGather(2, 3),
        AILiquidGoto(3, 2),
        AIEssentiaEmpty(1, 3),
        AIEssentiaGather(2, 3),
        AIEssentiaGoto(3, 2),
        AIUseItem(0, 3),
        AIFish(2, 3),
        AIOpenDoor(5, 1),
        AIReturnHome(6, 1),
        AIWatchClosest(7, 1),
        AILookIdle(8, 1);

        private final int priority;
        private final int mutexBits;

        OriginalTask(int priority, int mutexBits) {
            this.priority = priority;
            this.mutexBits = mutexBits;
        }

        public int priority() {
            return priority;
        }

        public int mutexBits() {
            return mutexBits;
        }
    }

    private static final Map<GolemCoreType, List<OriginalTask>> TASKS = new EnumMap<>(GolemCoreType.class);

    static {
        TASKS.put(GolemCoreType.FILL, List.of(
                OriginalTask.AIAvoidCreeperSwell, OriginalTask.AIHomeReplace, OriginalTask.AIHomePlace,
                OriginalTask.AIHomeDrop, OriginalTask.AIFillTake, OriginalTask.AIFillGoto,
                OriginalTask.AIOpenDoor, OriginalTask.AIReturnHome, OriginalTask.AIWatchClosest, OriginalTask.AILookIdle));
        TASKS.put(GolemCoreType.EMPTY, List.of(
                OriginalTask.AIAvoidCreeperSwell, OriginalTask.AIHomeReplace, OriginalTask.AIEmptyPlace,
                OriginalTask.AIEmptyDrop, OriginalTask.AIEmptyGoto, OriginalTask.AIHomeTake,
                OriginalTask.AIOpenDoor, OriginalTask.AIReturnHome, OriginalTask.AIWatchClosest, OriginalTask.AILookIdle));
        TASKS.put(GolemCoreType.GATHER, List.of(
                OriginalTask.AIAvoidCreeperSwell, OriginalTask.AIHomeReplace, OriginalTask.AIHomePlace,
                OriginalTask.AIItemPickup, OriginalTask.AIOpenDoor, OriginalTask.AIReturnHome,
                OriginalTask.AIWatchClosest, OriginalTask.AILookIdle));
        TASKS.put(GolemCoreType.HARVEST, List.of(
                OriginalTask.AIAvoidCreeperSwell, OriginalTask.AIHarvestCrops,
                OriginalTask.AIOpenDoor, OriginalTask.AIReturnHome, OriginalTask.AIWatchClosest, OriginalTask.AILookIdle));
        TASKS.put(GolemCoreType.GUARD, List.of(
                OriginalTask.AIAvoidCreeperSwell, OriginalTask.AIDartAttack,
                OriginalTask.AIGolemAttackOnCollide, OriginalTask.AIHurtByTarget,
                OriginalTask.AINearestAttackableTarget, OriginalTask.AIOpenDoor,
                OriginalTask.AIReturnHome, OriginalTask.AIWatchClosest, OriginalTask.AILookIdle));
        TASKS.put(GolemCoreType.LIQUID, List.of(
                OriginalTask.AIAvoidCreeperSwell, OriginalTask.AILiquidEmpty,
                OriginalTask.AILiquidGather, OriginalTask.AILiquidGoto,
                OriginalTask.AIOpenDoor, OriginalTask.AIReturnHome, OriginalTask.AIWatchClosest, OriginalTask.AILookIdle));
        TASKS.put(GolemCoreType.ESSENTIA, List.of(
                OriginalTask.AIAvoidCreeperSwell, OriginalTask.AIEssentiaEmpty,
                OriginalTask.AIEssentiaGather, OriginalTask.AIEssentiaGoto,
                OriginalTask.AIOpenDoor, OriginalTask.AIReturnHome, OriginalTask.AIWatchClosest, OriginalTask.AILookIdle));
        TASKS.put(GolemCoreType.LUMBER, List.of(
                OriginalTask.AIAvoidCreeperSwell, OriginalTask.AIHarvestLogs,
                OriginalTask.AIOpenDoor, OriginalTask.AIReturnHome, OriginalTask.AIWatchClosest, OriginalTask.AILookIdle));
        TASKS.put(GolemCoreType.USE, List.of(
                OriginalTask.AIAvoidCreeperSwell, OriginalTask.AIHomeReplace,
                OriginalTask.AIUseItem, OriginalTask.AIHomeTake,
                OriginalTask.AIOpenDoor, OriginalTask.AIReturnHome, OriginalTask.AIWatchClosest, OriginalTask.AILookIdle));
        TASKS.put(GolemCoreType.BUTCHER, List.of(
                OriginalTask.AIAvoidCreeperSwell, OriginalTask.AIDartAttack,
                OriginalTask.AIGolemAttackOnCollide, OriginalTask.AINearestButcherTarget,
                OriginalTask.AIOpenDoor, OriginalTask.AIReturnHome, OriginalTask.AIWatchClosest, OriginalTask.AILookIdle));
        TASKS.put(GolemCoreType.SORTING, List.of(
                OriginalTask.AIAvoidCreeperSwell, OriginalTask.AIHomeReplace,
                OriginalTask.AISortingPlace, OriginalTask.AISortingGoto,
                OriginalTask.AIHomeTakeSorting, OriginalTask.AIOpenDoor,
                OriginalTask.AIReturnHome, OriginalTask.AIWatchClosest, OriginalTask.AILookIdle));
        TASKS.put(GolemCoreType.FISH, List.of(
                OriginalTask.AIAvoidCreeperSwell, OriginalTask.AIFish,
                OriginalTask.AIOpenDoor, OriginalTask.AIReturnHome, OriginalTask.AIWatchClosest, OriginalTask.AILookIdle));
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
            builder.append(task.name()).append('@').append(task.priority());
        }
        return builder.length() == 0 ? "vanilla/entity-goal" : builder.toString();
    }
}
