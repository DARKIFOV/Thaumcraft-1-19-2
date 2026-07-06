package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectCombinationRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ResearchNoteState {
    public static final String TAG_ROOT = "ThaumcraftResearchNote";
    public static final String TAG_TARGET = "TargetResearch";
    public static final String TAG_PROGRESS = "TheoryProgress";
    public static final String TAG_SOLVED = "Solved";
    public static final String TAG_SLOTS = "Slots";
    public static final String TAG_REQUIRED = "Required";

    private ResearchNoteState() {
    }

    public static CompoundTag root(ItemStack stack) {
        return stack.getOrCreateTagElement(TAG_ROOT);
    }

    public static void initialize(ItemStack stack, String targetResearch) {
        CompoundTag root = root(stack);
        String target = targetResearch == null ? "" : targetResearch;

        if (!target.isBlank()) {
            root.putString(TAG_TARGET, target);
        } else if (!root.contains(TAG_TARGET)) {
            root.putString(TAG_TARGET, "");
        }

        String activeTarget = root.getString(TAG_TARGET);

        if (!root.contains(TAG_PROGRESS)) {
            root.putInt(TAG_PROGRESS, 0);
        }

        CompoundTag slots = root.getCompound(TAG_SLOTS);
        slots.putString(String.valueOf(ResearchNoteGrid.defaultStartSlot()), ResearchNoteRequirements.startFor(activeTarget).id());
        slots.putString(String.valueOf(ResearchNoteGrid.defaultEndSlot()), ResearchNoteRequirements.endFor(activeTarget).id());
        root.put(TAG_SLOTS, slots);
        root.putString(TAG_REQUIRED, requiredString(activeTarget));
    }

    public static String target(ItemStack stack) {
        return root(stack).getString(TAG_TARGET);
    }

    public static int progress(ItemStack stack) {
        return root(stack).getInt(TAG_PROGRESS);
    }

    public static boolean solved(ItemStack stack) {
        return root(stack).getBoolean(TAG_SOLVED);
    }

    public static Set<Aspect> requiredAspects(ItemStack stack) {
        Set<Aspect> result = new LinkedHashSet<>();
        String stored = root(stack).getString(TAG_REQUIRED);

        if (stored == null || stored.isBlank()) {
            return ResearchNoteRequirements.requiredFor(target(stack));
        }

        for (String id : stored.split(",")) {
            Aspect aspect = Aspect.byId(id.trim());

            if (aspect != null) {
                result.add(aspect);
            }
        }

        return result;
    }

    private static String requiredString(String target) {
        StringBuilder builder = new StringBuilder();

        for (Aspect aspect : ResearchNoteRequirements.requiredFor(target)) {
            if (!builder.isEmpty()) {
                builder.append(",");
            }

            builder.append(aspect.id());
        }

        return builder.toString();
    }

    public static Map<Integer, Aspect> slots(ItemStack stack) {
        Map<Integer, Aspect> result = new LinkedHashMap<>();
        CompoundTag slots = root(stack).getCompound(TAG_SLOTS);

        for (int i = 0; i < ResearchNoteGrid.SLOT_COUNT; i++) {
            String id = slots.getString(String.valueOf(i));
            Aspect aspect = Aspect.byId(id);

            if (aspect != null) {
                result.put(i, aspect);
            }
        }

        return result;
    }

    public static Optional<Aspect> slot(ItemStack stack, int index) {
        return Optional.ofNullable(slots(stack).get(index));
    }

    public static boolean place(ItemStack stack, int index, Aspect aspect) {
        if (aspect == null || index < 0 || index >= ResearchNoteGrid.SLOT_COUNT) {
            return false;
        }

        if (index == ResearchNoteGrid.defaultStartSlot() || index == ResearchNoteGrid.defaultEndSlot()) {
            return false;
        }

        CompoundTag root = root(stack);
        CompoundTag slots = root.getCompound(TAG_SLOTS);
        String existing = slots.getString(String.valueOf(index));

        // Stage136: do not silently overwrite a placed aspect. TC4 research notes are
        // meant to be solved as a deliberate path puzzle; the previous implementation
        // could consume a new aspect and erase the old one with no refund.
        if (existing != null && !existing.isBlank()) {
            return false;
        }

        if (!touchesCompatibleNeighbor(stack, index, aspect)) {
            return false;
        }

        slots.putString(String.valueOf(index), aspect.id());
        root.put(TAG_SLOTS, slots);
        root.putInt(TAG_PROGRESS, completionPercent(stack));
        return true;
    }

    public static boolean isLockedSlot(int index) {
        return index == ResearchNoteGrid.defaultStartSlot() || index == ResearchNoteGrid.defaultEndSlot();
    }

    public static boolean touchesCompatibleNeighbor(ItemStack stack, int index, Aspect aspect) {
        if (aspect == null || index < 0 || index >= ResearchNoteGrid.SLOT_COUNT) {
            return false;
        }

        if (isLockedSlot(index)) {
            return false;
        }

        for (int neighbor : ResearchNoteGrid.neighbors(index)) {
            Aspect other = slot(stack, neighbor).orElse(null);

            if (canLink(aspect, other)) {
                return true;
            }
        }

        return false;
    }

    public static Optional<Aspect> clearSlot(ItemStack stack, int index) {
        if (index < 0 || index >= ResearchNoteGrid.SLOT_COUNT || isLockedSlot(index)) {
            return Optional.empty();
        }

        CompoundTag root = root(stack);
        CompoundTag slots = root.getCompound(TAG_SLOTS);
        Aspect existing = Aspect.byId(slots.getString(String.valueOf(index)));

        if (existing == null) {
            return Optional.empty();
        }

        slots.remove(String.valueOf(index));
        root.put(TAG_SLOTS, slots);
        root.putBoolean(TAG_SOLVED, false);
        root.putInt(TAG_PROGRESS, completionPercent(stack));
        return Optional.of(existing);
    }

    public static Set<Aspect> missingRequired(ItemStack stack) {
        Set<Aspect> missing = new LinkedHashSet<>(requiredAspects(stack));
        missing.removeAll(slots(stack).values());
        return missing;
    }

    public static int completionPercent(ItemStack stack) {
        Set<Aspect> required = requiredAspects(stack);
        Map<Integer, Aspect> slots = slots(stack);
        Set<Aspect> placed = new LinkedHashSet<>(slots.values());

        int requiredScore = required.isEmpty() ? 50 : (int) Math.round(50.0D * coveredRequiredCount(placed, required) / required.size());
        int linkScore = partialConnectionScore(stack, slots);
        int score = Math.max(0, Math.min(99, requiredScore + linkScore));
        return isSolved(stack) ? 100 : score;
    }

    private static int coveredRequiredCount(Set<Aspect> placed, Set<Aspect> required) {
        int count = 0;
        for (Aspect aspect : required) {
            if (placed.contains(aspect)) {
                count++;
            }
        }
        return count;
    }

    private static int partialConnectionScore(ItemStack stack, Map<Integer, Aspect> slots) {
        if (!slots.containsKey(ResearchNoteGrid.defaultStartSlot())) {
            return 0;
        }
        boolean[] seen = new boolean[ResearchNoteGrid.SLOT_COUNT];
        int reachable = reachableCount(stack, ResearchNoteGrid.defaultStartSlot(), seen);
        return Math.min(49, reachable * 49 / Math.max(1, slots.size()));
    }

    private static int reachableCount(ItemStack stack, int current, boolean[] seen) {
        if (seen[current]) {
            return 0;
        }
        seen[current] = true;
        int total = 1;
        Aspect currentAspect = slot(stack, current).orElse(null);
        for (int next : ResearchNoteGrid.neighbors(current)) {
            if (seen[next]) {
                continue;
            }
            Aspect nextAspect = slot(stack, next).orElse(null);
            if (canLink(currentAspect, nextAspect)) {
                total += reachableCount(stack, next, seen);
            }
        }
        return total;
    }

    public static boolean canLink(Aspect first, Aspect second) {
        return ResearchAspectGraph.canConnect(first, second);
    }

    public static boolean hasAllRequired(ItemStack stack) {
        Set<Aspect> placed = new LinkedHashSet<>(slots(stack).values());
        return placed.containsAll(requiredAspects(stack));
    }

    public static boolean isSolved(ItemStack stack) {
        Map<Integer, Aspect> slots = slots(stack);

        if (!slots.containsKey(ResearchNoteGrid.defaultStartSlot())
                || !slots.containsKey(ResearchNoteGrid.defaultEndSlot())) {
            return false;
        }

        return hasAllRequired(stack)
                && walk(stack, ResearchNoteGrid.defaultStartSlot(), ResearchNoteGrid.defaultEndSlot(), new boolean[ResearchNoteGrid.SLOT_COUNT]);
    }

    private static boolean walk(ItemStack stack, int current, int end, boolean[] seen) {
        if (current == end) {
            return true;
        }

        seen[current] = true;
        Aspect currentAspect = slot(stack, current).orElse(null);

        for (int next : ResearchNoteGrid.neighbors(current)) {
            if (seen[next]) {
                continue;
            }

            Aspect nextAspect = slot(stack, next).orElse(null);

            if (canLink(currentAspect, nextAspect) && walk(stack, next, end, seen)) {
                return true;
            }
        }

        return false;
    }

    public static void markSolved(ItemStack stack) {
        CompoundTag root = root(stack);
        root.putBoolean(TAG_SOLVED, true);
        root.putInt(TAG_PROGRESS, 100);
    }
}
