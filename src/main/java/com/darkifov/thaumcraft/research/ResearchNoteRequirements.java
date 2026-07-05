package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class ResearchNoteRequirements {
    private ResearchNoteRequirements() {
    }

    public static Set<Aspect> requiredFor(String researchKey) {
        Set<Aspect> result = originalAspectListFor(researchKey);
        if (result.size() >= 2) {
            return result;
        }

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
        } else if (key.contains("thaumometer") || key.contains("scan")) {
            add(result, Aspect.SENSUS, Aspect.COGNITIO, Aspect.AURAM, Aspect.PRAECANTATIO);
        } else if (key.contains("research")) {
            add(result, Aspect.COGNITIO, Aspect.PRAECANTATIO, Aspect.SENSUS, Aspect.ORDO);
        } else {
            add(result, Aspect.COGNITIO, Aspect.PRAECANTATIO, Aspect.ORDO, Aspect.AURAM);
        }

        return result;
    }

    private static Set<Aspect> originalAspectListFor(String researchKey) {
        Set<Aspect> result = new LinkedHashSet<>();
        if (researchKey == null || researchKey.isBlank()) {
            return result;
        }
        Optional<ResearchEntry> entry = ResearchRegistry.byKey(researchKey);
        if (entry.isEmpty()) {
            entry = ResearchRegistry.byKey(researchKey.toUpperCase(Locale.ROOT));
        }
        if (entry.isEmpty()) {
            return result;
        }
        for (String id : entry.get().aspects().keySet()) {
            Aspect aspect = Aspect.byId(id);
            if (aspect != null) {
                result.add(aspect);
            }
            if (result.size() >= 6) {
                break;
            }
        }
        return result;
    }

    public static Aspect startFor(String researchKey) {
        Set<Aspect> required = requiredFor(researchKey);
        return required.stream().findFirst().orElse(Aspect.COGNITIO);
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
