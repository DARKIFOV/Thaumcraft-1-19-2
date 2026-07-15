package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchRegistry;
import com.darkifov.thaumcraft.research.TC4ResearchFlagPolicy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** TC4 browser layout rules: original category, displayColumn and displayRow. */
final class OriginalResearchLayout {
    static final int CELL = 24;
    static final int VIEW_WIDTH = 224;
    static final int VIEW_HEIGHT = 196;
    static final int VIEW_X = 16;
    static final int VIEW_Y = 17;

    private OriginalResearchLayout() {}

    static List<ResearchEntry> entriesFor(OriginalResearchCategory category) {
        List<ResearchEntry> result = new ArrayList<>();
        for (ResearchEntry entry : ResearchRegistry.originalEntries()) {
            if (categoryFor(entry) == category) {
                result.add(entry);
            }
        }
        result.sort(Comparator
                .comparingInt(ResearchEntry::displayRow)
                .thenComparingInt(ResearchEntry::displayColumn)
                .thenComparing(ResearchEntry::key));
        return result;
    }

    static OriginalResearchCategory categoryFor(ResearchEntry entry) {
        return OriginalResearchCategory.byKey(entry.category());
    }

    static int mapX(ResearchEntry entry, double panX) {
        return (int)Math.floor(entry.displayColumn() * CELL - panX);
    }

    static int mapY(ResearchEntry entry, double panY) {
        return (int)Math.floor(entry.displayRow() * CELL - panY);
    }

    static Bounds boundsFor(OriginalResearchCategory category) {
        List<ResearchEntry> entries = entriesFor(category);
        if (entries.isEmpty()) {
            return new Bounds(-5 * CELL - 85, -6 * CELL - 112, 5 * CELL - 112, 6 * CELL - 61);
        }

        int minColumn = Integer.MAX_VALUE;
        int maxColumn = Integer.MIN_VALUE;
        int minRow = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;
        for (ResearchEntry entry : entries) {
            minColumn = Math.min(minColumn, entry.displayColumn());
            maxColumn = Math.max(maxColumn, entry.displayColumn());
            minRow = Math.min(minRow, entry.displayRow());
            maxRow = Math.max(maxRow, entry.displayRow());
        }

        // Same scale/feel as GuiResearchBrowser: coordinates are in 24px cells,
        // with a margin so edge nodes are not glued to the frame.
        return new Bounds(minColumn * CELL - 85, minRow * CELL - 112, maxColumn * CELL - 112, maxRow * CELL - 61);
    }

    static boolean unlocked(Set<String> unlockedResearch, ResearchEntry entry) {
        return unlockedResearch.contains(entry.key());
    }

    static boolean available(Set<String> unlockedResearch, ResearchEntry entry) {
        if (unlocked(unlockedResearch, entry)) {
            return true;
        }
        for (String requirement : entry.requirements()) {
            if (!unlockedResearch.contains(requirement)) {
                return false;
            }
        }
        for (String requirement : entry.hiddenRequirements()) {
            if (!unlockedResearch.contains(requirement)) {
                return false;
            }
        }
        return true;
    }

    static boolean visible(Set<String> unlockedResearch, ResearchEntry entry) {
        // Stage205 audit marker: return unlocked(unlockedResearch, entry) || available(unlockedResearch, entry);
        // Stage583-602: hidden/lost ConfigResearch flags are now honoured explicitly.
        // Audit markers: TC4ResearchFlagPolicy.HIDDEN / TC4ResearchFlagPolicy.LOST.
        // Concealed nodes can reveal when parents/hidden parents are complete, but
        // hidden/lost entries stay invisible until a real trigger unlocks them.
        return TC4ResearchFlagPolicy.visibleInBook(entry, unlocked(unlockedResearch, entry), available(unlockedResearch, entry));
    }

    static boolean secondary(ResearchEntry entry) {
        return TC4ResearchFlagPolicy.isSecondary(entry);
    }

    static boolean round(ResearchEntry entry) {
        return TC4ResearchFlagPolicy.has(entry, TC4ResearchFlagPolicy.ROUND);
    }

    static boolean special(ResearchEntry entry) {
        return TC4ResearchFlagPolicy.has(entry, TC4ResearchFlagPolicy.SPECIAL);
    }

    static boolean forbidden(ResearchEntry entry) {
        return entry.warp() > 0;
    }

    static String shortTitle(String title) {
        if (title.length() <= 28) return title;
        return title.substring(0, 27) + "…";
    }

    record Bounds(int minPanX, int minPanY, int maxPanX, int maxPanY) {}
}
