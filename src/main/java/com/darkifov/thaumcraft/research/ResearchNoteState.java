package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ResearchNoteState {
    public static final String TAG_ROOT = "ThaumcraftResearchNote";
    public static final String TAG_TARGET = "TargetResearch";
    public static final String TAG_PROGRESS = "TheoryProgress";
    public static final String TAG_SOLVED = "Solved";
    public static final String TAG_SLOTS = "Slots";
    public static final String TAG_TYPES = "SlotTypes";
    public static final String TAG_RADIUS = "Radius";
    public static final String TAG_REQUIRED = "Required";
    public static final String TAG_COMPLETION_CLEANED = "CompletionCleaned";
    public static final String TAG_COPIES = "copies";
    public static final String TAG_TC4_CATEGORY = "tc4Category";
    public static final String TAG_TC4_PARENTS = "tc4Parents";
    public static final String TAG_TC4_HIDDEN_PARENTS = "tc4HiddenParents";
    public static final String TAG_TC4_SIBLINGS = "tc4Siblings";
    public static final String TAG_TC4_FLAGS = "tc4Flags";
    public static final String TAG_TC4_WARP = "tc4Warp";
    public static final String TAG_TC4_COMPLEXITY = "tc4Complexity";
    public static final String TAG_GRID_SEED = "GridSeed";
    public static final String TAG_TC4_HEXGRID = "hexgrid";

    private ResearchNoteState() {
    }

    public static CompoundTag root(ItemStack stack) {
        return stack.getOrCreateTagElement(TAG_ROOT);
    }

    public static void initialize(ItemStack stack, String targetResearch) {
        initialize(stack, targetResearch, 0L);
    }

    /**
     * v11.62.24: exact TC4 note generation needs a world random stream.  The
     * seed is stored on the note so server/client reloads never regenerate a
     * different matrix. A zero seed preserves existing worlds and derives one
     * only when a new grid actually has to be created.
     */
    public static void initialize(ItemStack stack, String targetResearch, long requestedSeed) {
        CompoundTag root = root(stack);
        CompoundTag top = stack.getOrCreateTag();
        String existingTarget = root.getString(TAG_TARGET);
        if (existingTarget.isBlank() && top.contains("key")) {
            existingTarget = top.getString("key");
        }
        String target = targetResearch == null || targetResearch.isBlank() ? existingTarget : targetResearch;
        if (target == null) {
            target = "";
        }

        importOriginalHexGrid(stack, root, target);

        boolean rebuild = !root.contains(TAG_SLOTS)
                || !root.contains(TAG_TYPES)
                || !target.equals(existingTarget)
                || root.getInt(TAG_RADIUS) <= 0;

        root.putString(TAG_TARGET, target);
        putOriginalResearchMetadata(root, target);
        if (!root.contains(TAG_PROGRESS)) {
            root.putInt(TAG_PROGRESS, 0);
        }

        if (rebuild) {
            long seed = requestedSeed != 0L ? requestedSeed : root.getLong(TAG_GRID_SEED);
            if (seed == 0L) {
                seed = 0x5443344D41545249L ^ ((long) target.hashCode() << 32) ^ System.nanoTime();
            }
            root.putLong(TAG_GRID_SEED, seed);
            rebuildOriginalGrid(stack, root, target, seed);
        } else {
            root.putString(TAG_REQUIRED, requiredString(target));
            root.putInt(TAG_PROGRESS, completionPercent(stack));
            writeOriginalHexGridMirror(root);
            syncOriginalTopLevel(stack, root);
        }
    }

    private static void rebuildOriginalGrid(ItemStack stack, CompoundTag root, String target, long seed) {
        int radius = ResearchNoteGrid.radiusForResearch(target);
        List<Aspect> anchors = new ArrayList<>(ResearchNoteRequirements.requiredFor(target));
        if (anchors.isEmpty()) {
            anchors.add(ResearchNoteGrid.defaultStartAspect());
            anchors.add(ResearchNoteGrid.defaultEndAspect());
        }
        if (anchors.size() == 1) {
            anchors.add(Aspect.PRAECANTATIO);
        }

        CompoundTag slots = new CompoundTag();
        CompoundTag types = new CompoundTag();
        for (ResearchNoteGrid.GridSlot slot : ResearchNoteGrid.activeSlotsForRadius(radius)) {
            types.putInt(String.valueOf(slot.index()), ResearchNoteGrid.TYPE_EMPTY);
        }

        java.util.Random random = new java.util.Random(seed);
        List<ResearchNoteGrid.GridSlot> anchorSlots = ResearchNoteGrid.distributeRingRandomly(radius, anchors.size(), random);
        for (int i = 0; i < Math.min(anchors.size(), anchorSlots.size()); i++) {
            ResearchNoteGrid.GridSlot slot = anchorSlots.get(i);
            Aspect aspect = anchors.get(i);
            slots.putString(String.valueOf(slot.index()), aspect.id());
            types.putInt(String.valueOf(slot.index()), ResearchNoteGrid.TYPE_RESEARCH_ANCHOR);
        }

        removeOriginalComplexityBlanks(target, radius, types, random);

        root.put(TAG_SLOTS, slots);
        root.put(TAG_TYPES, types);
        root.putInt(TAG_RADIUS, radius);
        putOriginalResearchMetadata(root, target);
        root.putBoolean(TAG_SOLVED, false);
        root.putBoolean(TAG_COMPLETION_CLEANED, false);
        root.putString(TAG_REQUIRED, requiredString(target));
        root.putInt(TAG_PROGRESS, 0);
        writeOriginalHexGridMirror(root);
        syncOriginalTopLevel(stack, root);
    }


    private static void putOriginalResearchMetadata(CompoundTag root, String target) {
        Optional<ResearchEntry> entry = target == null || target.isBlank() ? Optional.empty() : ResearchRegistry.byKey(target);
        if (entry.isEmpty()) {
            root.putString(TAG_TC4_CATEGORY, "");
            root.putString(TAG_TC4_PARENTS, "");
            root.putString(TAG_TC4_HIDDEN_PARENTS, "");
            root.putString(TAG_TC4_SIBLINGS, "");
            root.putString(TAG_TC4_FLAGS, "");
            root.putInt(TAG_TC4_WARP, 0);
            root.putInt(TAG_TC4_COMPLEXITY, 1);
            return;
        }
        ResearchEntry research = entry.get();
        root.putString(TAG_TC4_CATEGORY, research.category());
        root.putString(TAG_TC4_PARENTS, String.join(",", research.requirements()));
        root.putString(TAG_TC4_HIDDEN_PARENTS, String.join(",", research.hiddenRequirements()));
        root.putString(TAG_TC4_SIBLINGS, String.join(",", research.siblings()));
        root.putString(TAG_TC4_FLAGS, TC4ResearchFlagPolicy.joinedFlags(research));
        root.putInt(TAG_TC4_WARP, research.warp());
        root.putInt(TAG_TC4_COMPLEXITY, research.complexity());
    }

    /**
     * Mirrors the original TC4 note generation pass that removes complexity*2 blank
     * cells for complexity > 1 while avoiding deletion that would strand a fixed
     * research-tag anchor. 1.7.10 used world RNG; v11.62.24 stores that random
     * stream as GridSeed so the generated note remains stable across sync and saves.
     */
    private static void removeOriginalComplexityBlanks(String target, int radius, CompoundTag types, java.util.Random random) {
        Optional<ResearchEntry> entry = target == null || target.isBlank() ? Optional.empty() : ResearchRegistry.byKey(target);
        int complexity = entry.map(ResearchEntry::complexity).orElse(1);
        if (complexity <= 1) {
            return;
        }

        int blanks = complexity * 2;
        int guard = Math.max(64, ResearchNoteGrid.activeSlotsForRadius(radius).size() * 32);
        while (blanks > 0 && guard-- > 0) {
            List<Integer> candidates = new ArrayList<>();
            for (ResearchNoteGrid.GridSlot slot : ResearchNoteGrid.activeSlotsForRadius(radius)) {
                String id = String.valueOf(slot.index());
                if (types.contains(id) && types.getInt(id) == ResearchNoteGrid.TYPE_EMPTY) {
                    candidates.add(slot.index());
                }
            }
            if (candidates.isEmpty()) {
                return;
            }
            int chosen = candidates.get(random.nextInt(candidates.size()));
            if (canRemoveBlank(chosen, types)) {
                types.remove(String.valueOf(chosen));
                blanks--;
            }
        }
    }

    /** Mirrors the exact TC4 anchor protection check used while cutting holes. */
    private static boolean canRemoveBlank(int slot, CompoundTag types) {
        for (int neighbor : ResearchNoteGrid.neighbors(slot)) {
            if (!types.contains(String.valueOf(neighbor))) {
                continue;
            }
            if (types.getInt(String.valueOf(neighbor)) == ResearchNoteGrid.TYPE_RESEARCH_ANCHOR) {
                int activeAroundAnchor = 0;
                for (int around : ResearchNoteGrid.neighbors(neighbor)) {
                    // TC4 counts the candidate while testing and removes it only
                    // after the check. Therefore >=2 here leaves at least one
                    // active neighbour after the chosen blank is cut away.
                    if (types.contains(String.valueOf(around))) {
                        activeAroundAnchor++;
                    }
                }
                if (activeAroundAnchor < 2) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Imports a genuine 1.7.10 research note when only its top-level hexgrid exists. */
    private static void importOriginalHexGrid(ItemStack stack, CompoundTag root, String fallbackTarget) {
        if (root.contains(TAG_SLOTS) || root.contains(TAG_TYPES)) {
            return;
        }
        CompoundTag top = stack.getOrCreateTag();
        if (!top.contains(TAG_TC4_HEXGRID)) {
            return;
        }
        ListTag grid = top.getList(TAG_TC4_HEXGRID, 10);
        if (grid.isEmpty()) {
            return;
        }
        CompoundTag slots = new CompoundTag();
        CompoundTag types = new CompoundTag();
        int radius = ResearchNoteGrid.MIN_RADIUS;
        for (int i = 0; i < grid.size(); i++) {
            CompoundTag hex = grid.getCompound(i);
            int q = hex.getByte("hexq");
            int r = hex.getByte("hexr");
            Optional<ResearchNoteGrid.GridSlot> found = ResearchNoteGrid.byHex(q, r);
            if (found.isEmpty()) {
                continue;
            }
            ResearchNoteGrid.GridSlot slot = found.get();
            String id = String.valueOf(slot.index());
            types.putInt(id, hex.getByte("type"));
            if (hex.contains("aspect")) {
                Aspect aspect = Aspect.byId(hex.getString("aspect"));
                if (aspect != null) {
                    slots.putString(id, aspect.id());
                }
            }
            radius = Math.max(radius, ResearchNoteGrid.distance(q, r, 0, 0));
        }
        String key = top.getString("key");
        root.putString(TAG_TARGET, key.isBlank() ? fallbackTarget : key);
        root.put(TAG_SLOTS, slots);
        root.put(TAG_TYPES, types);
        root.putInt(TAG_RADIUS, ResearchNoteGrid.clampRadius(radius));
        root.putBoolean(TAG_SOLVED, top.getBoolean("complete"));
        root.putInt(TAG_COPIES, Math.max(0, top.getInt(TAG_COPIES)));
        root.putBoolean(TAG_COMPLETION_CLEANED, top.getBoolean("complete"));
        root.putString(TAG_REQUIRED, requiredString(root.getString(TAG_TARGET)));
        root.putInt(TAG_PROGRESS, top.getBoolean("complete") ? 100 : 0);
        putOriginalResearchMetadata(root, root.getString(TAG_TARGET));
    }

    /** Keeps the legacy fields at the real item root, exactly where TC4 readers expect them. */
    private static void syncOriginalTopLevel(ItemStack stack, CompoundTag root) {
        CompoundTag top = stack.getOrCreateTag();
        top.put(TAG_TC4_HEXGRID, root.getList(TAG_TC4_HEXGRID, 10).copy());
        top.putString("key", root.getString("key"));
        top.putBoolean("complete", root.getBoolean("complete"));
        top.putInt(TAG_COPIES, root.getInt(TAG_COPIES));
        top.putInt("color", root.getInt("color"));
    }

    /**
     * Writes the original 1.7.10 NBT representation in parallel with the indexed
     * modern representation.  This makes notes inspectable by parity tooling and
     * prevents q/r information from being lost when copied between implementations.
     */
    private static void writeOriginalHexGridMirror(CompoundTag root) {
        CompoundTag slots = root.getCompound(TAG_SLOTS);
        CompoundTag types = root.getCompound(TAG_TYPES);
        ListTag grid = new ListTag();
        for (String id : types.getAllKeys()) {
            int index;
            try {
                index = Integer.parseInt(id);
            } catch (NumberFormatException ignored) {
                continue;
            }
            ResearchNoteGrid.GridSlot slot = ResearchNoteGrid.slot(index);
            CompoundTag hex = new CompoundTag();
            hex.putByte("hexq", (byte) slot.q());
            hex.putByte("hexr", (byte) slot.r());
            hex.putByte("type", (byte) types.getInt(id));
            String aspect = slots.getString(id);
            if (!aspect.isBlank()) {
                hex.putString("aspect", aspect);
            }
            grid.add(hex);
        }
        root.put(TAG_TC4_HEXGRID, grid);
        root.putString("key", root.getString(TAG_TARGET));
        root.putBoolean("complete", root.getBoolean(TAG_SOLVED));
        root.putInt("copies", root.getInt(TAG_COPIES));
        Aspect primary = Aspect.byId(root.getString(TAG_REQUIRED).split(",", 2)[0]);
        root.putInt("color", primary == null ? Aspect.PRAECANTATIO.nativeColor() : primary.nativeColor());
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

    public static int radius(ItemStack stack) {
        int radius = root(stack).getInt(TAG_RADIUS);
        return radius <= 0 ? ResearchNoteGrid.radiusForResearch(target(stack)) : radius;
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
        for (String key : slots.getAllKeys()) {
            try {
                int index = Integer.parseInt(key);
                Aspect aspect = Aspect.byId(slots.getString(key));
                if (aspect != null) {
                    result.put(index, aspect);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    public static Map<Integer, Integer> slotTypes(ItemStack stack) {
        Map<Integer, Integer> result = new LinkedHashMap<>();
        CompoundTag types = root(stack).getCompound(TAG_TYPES);
        if (types.getAllKeys().isEmpty()) {
            initialize(stack, target(stack));
            types = root(stack).getCompound(TAG_TYPES);
        }
        for (String key : types.getAllKeys()) {
            try {
                result.put(Integer.parseInt(key), types.getInt(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    public static Optional<Aspect> slot(ItemStack stack, int index) {
        return Optional.ofNullable(slots(stack).get(index));
    }

    public static int type(ItemStack stack, int index) {
        return slotTypes(stack).getOrDefault(index, -1);
    }

    public static boolean place(ItemStack stack, int index, Aspect aspect) {
        if (aspect == null || index < 0 || index >= ResearchNoteGrid.SLOT_COUNT) {
            return false;
        }
        initialize(stack, target(stack));
        CompoundTag root = root(stack);
        CompoundTag types = root.getCompound(TAG_TYPES);
        String id = String.valueOf(index);
        if (!types.contains(id) || types.getInt(id) != ResearchNoteGrid.TYPE_EMPTY) {
            return false;
        }
        CompoundTag slots = root.getCompound(TAG_SLOTS);
        slots.putString(id, aspect.id());
        types.putInt(id, ResearchNoteGrid.TYPE_PLACED);
        root.put(TAG_SLOTS, slots);
        root.put(TAG_TYPES, types);
        root.putBoolean(TAG_SOLVED, false);
        root.putBoolean(TAG_COMPLETION_CLEANED, false);
        root.putInt(TAG_PROGRESS, completionPercent(stack));
        writeOriginalHexGridMirror(root);
        syncOriginalTopLevel(stack, root);
        return true;
    }

    public static boolean isLockedSlot(ItemStack stack, int index) {
        return type(stack, index) == ResearchNoteGrid.TYPE_RESEARCH_ANCHOR;
    }

    public static boolean isActiveSlot(ItemStack stack, int index) {
        return slotTypes(stack).containsKey(index);
    }

    public static boolean touchesCompatibleNeighbor(ItemStack stack, int index, Aspect aspect) {
        if (aspect == null || index < 0 || index >= ResearchNoteGrid.SLOT_COUNT) {
            return false;
        }
        if (!isActiveSlot(stack, index) || isLockedSlot(stack, index)) {
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
        if (index < 0 || index >= ResearchNoteGrid.SLOT_COUNT) {
            return Optional.empty();
        }
        initialize(stack, target(stack));
        CompoundTag root = root(stack);
        CompoundTag types = root.getCompound(TAG_TYPES);
        String id = String.valueOf(index);
        if (!types.contains(id) || types.getInt(id) != ResearchNoteGrid.TYPE_PLACED) {
            return Optional.empty();
        }
        CompoundTag slots = root.getCompound(TAG_SLOTS);
        Aspect existing = Aspect.byId(slots.getString(id));
        if (existing == null) {
            return Optional.empty();
        }
        slots.remove(id);
        types.putInt(id, ResearchNoteGrid.TYPE_EMPTY);
        root.put(TAG_SLOTS, slots);
        root.put(TAG_TYPES, types);
        root.putBoolean(TAG_SOLVED, false);
        root.putBoolean(TAG_COMPLETION_CLEANED, false);
        root.putInt(TAG_PROGRESS, completionPercent(stack));
        writeOriginalHexGridMirror(root);
        syncOriginalTopLevel(stack, root);
        return Optional.of(existing);
    }

    public static Set<Aspect> missingRequired(ItemStack stack) {
        Set<Aspect> missing = new LinkedHashSet<>(requiredAspects(stack));
        missing.removeAll(anchorAspects(stack));
        return missing;
    }

    public static int completionPercent(ItemStack stack) {
        if (root(stack).getBoolean(TAG_SOLVED)) {
            return 100;
        }
        Map<Integer, Aspect> slots = slots(stack);
        Set<Integer> anchors = anchorSlots(stack);
        if (anchors.isEmpty()) {
            return 0;
        }
        Set<Integer> reachable = reachableFilledSlots(stack, anchors.iterator().next(), false, null);
        int connectedAnchors = 0;
        for (int anchor : anchors) {
            if (reachable.contains(anchor)) {
                connectedAnchors++;
            }
        }
        int anchorScore = (int) Math.round(80.0D * connectedAnchors / anchors.size());
        int filledActive = slots.size();
        int active = Math.max(1, slotTypes(stack).size());
        int fillScore = Math.min(19, filledActive * 19 / active);
        return Math.max(0, Math.min(99, anchorScore + fillScore));
    }

    public static boolean canLink(Aspect first, Aspect second) {
        return ResearchAspectGraph.canConnect(first, second);
    }

    public static boolean hasAllRequired(ItemStack stack) {
        return anchorAspects(stack).containsAll(requiredAspects(stack));
    }

    public static boolean isSolved(ItemStack stack) {
        return hasAllRequired(stack) && allAnchorsConnected(stack, false, null);
    }

    public static boolean isSolvedForPlayer(ItemStack stack, Player player) {
        return hasAllRequired(stack) && allAnchorsConnected(stack, true, player);
    }

    private static boolean allAnchorsConnected(ItemStack stack, boolean requireKnown, Player player) {
        Set<Integer> anchors = anchorSlots(stack);
        if (anchors.size() < 2) {
            return false;
        }
        Set<Integer> reachable = reachableFilledSlots(stack, anchors.iterator().next(), requireKnown, player);
        return reachable.containsAll(anchors);
    }

    private static Set<Integer> reachableFilledSlots(ItemStack stack, int start, boolean requireKnown, Player player) {
        Set<Integer> seen = new LinkedHashSet<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start);
        while (!queue.isEmpty()) {
            int current = queue.removeFirst();
            Aspect currentAspect = slot(stack, current).orElse(null);
            for (int next : ResearchNoteGrid.neighbors(current)) {
                if (seen.contains(next)) {
                    continue;
                }
                if (type(stack, next) < ResearchNoteGrid.TYPE_RESEARCH_ANCHOR) {
                    continue;
                }
                Aspect nextAspect = slot(stack, next).orElse(null);
                if (!canLink(currentAspect, nextAspect)) {
                    continue;
                }
                if (requireKnown && player != null && (!PlayerAspectKnowledge.knows(player, currentAspect) || !PlayerAspectKnowledge.knows(player, nextAspect))) {
                    continue;
                }
                seen.add(next);
                queue.add(next);
            }
        }
        return seen;
    }

    public static Set<Integer> anchorSlots(ItemStack stack) {
        Set<Integer> result = new LinkedHashSet<>();
        for (Map.Entry<Integer, Integer> entry : slotTypes(stack).entrySet()) {
            if (entry.getValue() == ResearchNoteGrid.TYPE_RESEARCH_ANCHOR) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public static Set<Aspect> anchorAspects(ItemStack stack) {
        Set<Aspect> result = new LinkedHashSet<>();
        Map<Integer, Aspect> slots = slots(stack);
        for (int index : anchorSlots(stack)) {
            Aspect aspect = slots.get(index);
            if (aspect != null) {
                result.add(aspect);
            }
        }
        return result;
    }

    public static void markSolved(ItemStack stack) {
        CompoundTag root = root(stack);
        pruneDisconnectedNonAnchors(stack);
        root.putBoolean(TAG_SOLVED, true);
        root.putBoolean(TAG_COMPLETION_CLEANED, true);
        root.putInt(TAG_PROGRESS, 100);
        writeOriginalHexGridMirror(root);
        syncOriginalTopLevel(stack, root);
    }

    private static void pruneDisconnectedNonAnchors(ItemStack stack) {
        Set<Integer> anchors = anchorSlots(stack);
        if (anchors.isEmpty()) {
            return;
        }
        Set<Integer> keep = reachableFilledSlots(stack, anchors.iterator().next(), false, null);
        CompoundTag root = root(stack);
        CompoundTag slots = root.getCompound(TAG_SLOTS);
        CompoundTag types = root.getCompound(TAG_TYPES);
        for (Integer index : new ArrayList<>(slotTypes(stack).keySet())) {
            int type = types.getInt(String.valueOf(index));
            if (type != ResearchNoteGrid.TYPE_RESEARCH_ANCHOR && type != ResearchNoteGrid.TYPE_EMPTY && !keep.contains(index)) {
                slots.remove(String.valueOf(index));
                types.putInt(String.valueOf(index), ResearchNoteGrid.TYPE_EMPTY);
            }
        }
        root.put(TAG_SLOTS, slots);
        root.put(TAG_TYPES, types);
        writeOriginalHexGridMirror(root);
        syncOriginalTopLevel(stack, root);
    }

    public static int copyCount(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        CompoundTag root = root(stack);
        if (root.contains(TAG_COPIES)) {
            return Math.max(0, root.getInt(TAG_COPIES));
        }
        CompoundTag stackTag = stack.getOrCreateTag();
        return Math.max(0, stackTag.getInt(TAG_COPIES));
    }

    public static void setCopyCount(ItemStack stack, int copies) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        int value = Math.max(0, copies);
        root(stack).putInt(TAG_COPIES, value);
        // Compatibility mirror for old TC4 NBT readers and previous stage tooling.
        stack.getOrCreateTag().putInt(TAG_COPIES, value);
        CompoundTag root = root(stack);
        writeOriginalHexGridMirror(root);
        syncOriginalTopLevel(stack, root);
    }

    public static int incrementCopyCount(ItemStack stack) {
        int next = copyCount(stack) + 1;
        setCopyCount(stack, next);
        return next;
    }

    public static List<String> debugHexLines(ItemStack stack) {
        List<String> result = new ArrayList<>();
        Map<Integer, Aspect> slots = slots(stack);
        Map<Integer, Integer> types = slotTypes(stack);
        for (Map.Entry<Integer, Integer> entry : types.entrySet()) {
            ResearchNoteGrid.GridSlot slot = ResearchNoteGrid.slot(entry.getKey());
            Aspect aspect = slots.get(entry.getKey());
            result.add(slot.q() + ":" + slot.r() + ":" + entry.getValue() + ":" + (aspect == null ? "" : aspect.id()));
        }
        Collections.sort(result);
        return result;
    }
}
