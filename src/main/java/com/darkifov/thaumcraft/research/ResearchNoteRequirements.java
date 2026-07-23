package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class ResearchNoteRequirements {
    private ResearchNoteRequirements() {
    }

    /** Original ConfigResearch notes use exactly the entry tag set, including one-tag notes. */
    public static Set<Aspect> requiredFor(String researchKey) {
        Optional<ResearchEntry> original = originalEntry(researchKey);
        if (original.isPresent()) {
            Set<Aspect> exact = new LinkedHashSet<>();
            for (String id : original.get().aspects().keySet()) {
                Aspect aspect = Aspect.byId(id);
                if (aspect != null) {
                    exact.add(aspect);
                }
            }
            return exact;
        }

        // Rebuild/addon-only entries retain a deterministic compatibility set,
        // but normal TC4 table progression is restricted to originalEntries().
        Set<Aspect> result = new LinkedHashSet<>();
        String key = researchKey == null ? "" : researchKey.toLowerCase(Locale.ROOT);
        if (key.contains("wand") || key.contains("focus")) {
            add(result, Aspect.PRAECANTATIO, Aspect.POTENTIA, Aspect.INSTRUMENTUM, Aspect.AURAM);
        } else if (key.contains("alchemy") || key.contains("crucible") || key.contains("essentia") || key.contains("jar")) {
            add(result, Aspect.PRAECANTATIO, Aspect.AQUA, Aspect.VITREUS, Aspect.PERMUTATIO);
        } else if (key.contains("golem")) {
            add(result, Aspect.MACHINA, Aspect.MOTUS, Aspect.HUMANUS, Aspect.SPIRITUS);
        } else if (key.contains("eldritch") || key.contains("void")) {
            add(result, Aspect.ALIENIS, Aspect.TENEBRAE, Aspect.VACUOS, Aspect.PRAECANTATIO);
        } else if (key.contains("infusion") || key.contains("matrix") || key.contains("altar")) {
            add(result, Aspect.PRAECANTATIO, Aspect.AURAM, Aspect.POTENTIA, Aspect.ORDO);
        } else {
            add(result, Aspect.COGNITIO, Aspect.PRAECANTATIO, Aspect.ORDO, Aspect.AURAM);
        }
        return result;
    }

    private static Optional<ResearchEntry> originalEntry(String researchKey) {
        if (researchKey == null || researchKey.isBlank()) {
            return Optional.empty();
        }
        for (ResearchEntry entry : ResearchRegistry.originalEntries()) {
            if (entry.key().equalsIgnoreCase(researchKey)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public static Aspect startFor(String researchKey) {
        return requiredFor(researchKey).stream().findFirst().orElse(Aspect.COGNITIO);
    }

    public static Aspect endFor(String researchKey) {
        Aspect result = Aspect.PRAECANTATIO;
        for (Aspect aspect : requiredFor(researchKey)) {
            result = aspect;
        }
        return result;
    }

    private static void add(Set<Aspect> target, Aspect... aspects) {
        for (Aspect aspect : aspects) {
            if (aspect != null) {
                target.add(aspect);
            }
        }
    }
}
