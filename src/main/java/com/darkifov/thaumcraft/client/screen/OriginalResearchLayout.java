package com.darkifov.thaumcraft.client.screen;

import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class OriginalResearchLayout {
    private OriginalResearchLayout() {
    }

    static List<ResearchEntry> entriesFor(OriginalResearchCategory category) {
        List<ResearchEntry> result = new ArrayList<>();

        for (ResearchEntry entry : ResearchRegistry.entries()) {
            if (categoryFor(entry) == category) {
                result.add(entry);
            }
        }

        return result;
    }

    static OriginalResearchCategory categoryFor(ResearchEntry entry) {
        String key = entry.key().toUpperCase(Locale.ROOT);

        if (key.contains("ELDRITCH") || key.contains("CRIMSON") || key.contains("WARP") || key.contains("TAINT")) {
            return OriginalResearchCategory.ELDRITCH;
        }

        if (key.contains("GOLEM")) {
            return OriginalResearchCategory.GOLEMANCY;
        }

        if (key.contains("THAUMIC") || key.contains("EXTRAS") || key.contains("TINKERER") || key.contains("AE2") || key.contains("ADDON")) {
            return OriginalResearchCategory.ADDONS;
        }

        if (key.contains("ALCHEMY") || key.contains("CRUCIBLE") || key.contains("ESSENTIA") || key.contains("JAR") || key.contains("PHIAL") || key.contains("ALEMBIC")) {
            return OriginalResearchCategory.ALCHEMY;
        }

        if (key.contains("INFUSION") || key.contains("ARCANE") || key.contains("WAND") || key.contains("FOCUS") || key.contains("THAUMIUM")
                || key.contains("NODE") || key.contains("AURA") || key.contains("VIS")) {
            return OriginalResearchCategory.THAUMATURGY;
        }

        if (key.contains("MATRIX") || key.contains("DEVICE") || key.contains("PLATFORM") || key.contains("TRANSVECTOR")
                || key.contains("WORKBENCH") || key.contains("TABLE") || key.contains("TERMINAL") || key.contains("STORAGE")) {
            return OriginalResearchCategory.ARTIFICE;
        }

        return OriginalResearchCategory.BASICS;
    }

    static int xFor(int index) {
        return 44 + (index % 4) * 54;
    }

    static int yFor(int index) {
        return 62 + (index / 4) * 44;
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

        return true;
    }

    static String shortTitle(String title) {
        if (title.length() <= 16) {
            return title;
        }

        return title.substring(0, 15) + ".";
    }

    static String wrap(String text, int max) {
        if (text.length() <= max) {
            return text;
        }

        return text.substring(0, Math.max(0, max - 3)) + "...";
    }
}
