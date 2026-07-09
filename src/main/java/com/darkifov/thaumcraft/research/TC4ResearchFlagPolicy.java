package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Stage583-602: centralised adapter for original TC4 ResearchItem flags.
 *
 * The data remains the ConfigResearch extraction already stored on ResearchEntry.
 * This class only decides how the Forge 1.19.2 screens/table should honour those
 * original flags so hidden/lost/stub/auto entries do not become fake table notes.
 */
public final class TC4ResearchFlagPolicy {
    public static final String AUTO_UNLOCK = "auto_unlock";
    public static final String STUB = "stub";
    public static final String SECONDARY = "secondary";
    public static final String CONCEALED = "concealed";
    public static final String HIDDEN = "hidden";
    public static final String LOST = "lost";
    public static final String SPECIAL = "special";
    public static final String ROUND = "round";

    private TC4ResearchFlagPolicy() {}

    public static boolean has(ResearchEntry entry, String flag) {
        if (entry == null || flag == null) {
            return false;
        }
        String wanted = normalize(flag);
        for (String value : entry.flags()) {
            if (normalize(value).equals(wanted)) {
                return true;
            }
        }
        return false;
    }

    public static Set<String> normalizedFlags(ResearchEntry entry) {
        Set<String> result = new LinkedHashSet<>();
        if (entry != null) {
            for (String flag : entry.flags()) {
                String normalized = normalize(flag);
                if (!normalized.isBlank()) {
                    result.add(normalized);
                }
            }
        }
        return result;
    }

    /**
     * Original TC4 does not create normal research-note theories for entries that
     * are auto pages/stubs or entries that are only revealed by hidden/lost/special
     * trigger paths. This keeps the Research Table from fabricating progression.
     */
    public static boolean canCreateNormalResearchNote(Player player, ResearchEntry entry) {
        if (entry == null) {
            return false;
        }
        if (has(entry, AUTO_UNLOCK) || has(entry, STUB) || has(entry, HIDDEN) || has(entry, LOST)) {
            return false;
        }
        // Strict v9.02 guard: TC4 ResearchTable may only create a theory/note for
        // a real ConfigResearch entry with actual page/recipe payload. This keeps
        // the port from fabricating progression out of rebuild-only placeholder nodes.
        if (!hasOriginalPagePayload(entry)) {
            return false;
        }
        return true;
    }

    /**
     * Thaumonomicon visibility: hidden/lost nodes stay invisible until actually
     * unlocked. Concealed entries may appear once their real parents/hidden parents
     * are met, matching TC4's reveal-on-available behaviour.
     */
    public static boolean visibleInBook(Player player, ResearchEntry entry, boolean parentsAvailable) {
        boolean unlocked = entry != null && player != null && PlayerThaumData.hasResearch(player, entry.key());
        return visibleInBook(entry, unlocked, parentsAvailable);
    }

    public static boolean visibleInBook(ResearchEntry entry, boolean alreadyUnlocked, boolean parentsAvailable) {
        if (entry == null) {
            return false;
        }
        if (alreadyUnlocked) {
            return true;
        }
        if (has(entry, HIDDEN) || has(entry, LOST)) {
            return false;
        }
        return parentsAvailable;
    }

    public static boolean hasOriginalPagePayload(ResearchEntry entry) {
        if (entry == null) {
            return false;
        }
        return entry.pageTypes().length > 0 || entry.pageTextKeys().length > 0 || entry.recipeKeys().length > 0;
    }

    public static String joinedFlags(ResearchEntry entry) {
        return String.join(",", normalizedFlags(entry));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
