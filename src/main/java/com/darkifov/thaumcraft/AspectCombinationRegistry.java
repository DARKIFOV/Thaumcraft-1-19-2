package com.darkifov.thaumcraft;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class AspectCombinationRegistry {
    private static final Map<String, Aspect> COMBINATIONS = new HashMap<>();

    static {
        register(Aspect.AER, Aspect.PERDITIO, Aspect.VACUOS);
        register(Aspect.AER, Aspect.IGNIS, Aspect.LUX);
        register(Aspect.AER, Aspect.AQUA, Aspect.TEMPESTAS);
        register(Aspect.AER, Aspect.ORDO, Aspect.MOTUS);
        register(Aspect.IGNIS, Aspect.PERDITIO, Aspect.GELUM);
        register(Aspect.TERRA, Aspect.ORDO, Aspect.VITREUS);
        register(Aspect.AQUA, Aspect.TERRA, Aspect.VICTUS);
        register(Aspect.AQUA, Aspect.PERDITIO, Aspect.VENENUM);
        register(Aspect.ORDO, Aspect.IGNIS, Aspect.POTENTIA);
        register(Aspect.PERDITIO, Aspect.ORDO, Aspect.PERMUTATIO);
        register(Aspect.TERRA, Aspect.VITREUS, Aspect.METALLUM);
        register(Aspect.VICTUS, Aspect.PERDITIO, Aspect.MORTUUS);
        register(Aspect.AER, Aspect.MOTUS, Aspect.VOLATUS);
        register(Aspect.VACUOS, Aspect.LUX, Aspect.TENEBRAE);
        register(Aspect.VICTUS, Aspect.MORTUUS, Aspect.SPIRITUS);
        register(Aspect.VICTUS, Aspect.ORDO, Aspect.SANO);
        register(Aspect.MOTUS, Aspect.TERRA, Aspect.ITER);
        register(Aspect.VACUOS, Aspect.TENEBRAE, Aspect.ALIENIS);
        register(Aspect.VACUOS, Aspect.POTENTIA, Aspect.PRAECANTATIO);
        register(Aspect.PRAECANTATIO, Aspect.AER, Aspect.AURAM);
        register(Aspect.PRAECANTATIO, Aspect.PERDITIO, Aspect.VITIUM);
        register(Aspect.VICTUS, Aspect.AQUA, Aspect.LIMUS);
        register(Aspect.VICTUS, Aspect.TERRA, Aspect.HERBA);
        register(Aspect.AER, Aspect.HERBA, Aspect.ARBOR);
        register(Aspect.MOTUS, Aspect.VICTUS, Aspect.BESTIA);
        register(Aspect.MORTUUS, Aspect.BESTIA, Aspect.CORPUS);
        register(Aspect.MOTUS, Aspect.MORTUUS, Aspect.EXANIMIS);
        register(Aspect.IGNIS, Aspect.SPIRITUS, Aspect.COGNITIO);
        register(Aspect.AER, Aspect.SPIRITUS, Aspect.SENSUS);
        register(Aspect.BESTIA, Aspect.COGNITIO, Aspect.HUMANUS);
        register(Aspect.HERBA, Aspect.HUMANUS, Aspect.MESSIS);
        register(Aspect.HUMANUS, Aspect.TERRA, Aspect.PERFODIO);
        register(Aspect.HUMANUS, Aspect.ORDO, Aspect.INSTRUMENTUM);
        register(Aspect.MESSIS, Aspect.INSTRUMENTUM, Aspect.METO);
        register(Aspect.INSTRUMENTUM, Aspect.IGNIS, Aspect.TELUM);
        register(Aspect.INSTRUMENTUM, Aspect.TERRA, Aspect.TUTAMEN);
        register(Aspect.VICTUS, Aspect.VACUOS, Aspect.FAMES);
        register(Aspect.HUMANUS, Aspect.FAMES, Aspect.LUCRUM);
        register(Aspect.HUMANUS, Aspect.INSTRUMENTUM, Aspect.FABRICO);
        register(Aspect.INSTRUMENTUM, Aspect.BESTIA, Aspect.PANNUS);
        register(Aspect.MOTUS, Aspect.INSTRUMENTUM, Aspect.MACHINA);
        register(Aspect.MOTUS, Aspect.PERDITIO, Aspect.VINCULUM);
    }

    private AspectCombinationRegistry() {
    }

    private static void register(Aspect first, Aspect second, Aspect result) {
        COMBINATIONS.put(key(first, second), result);
        COMBINATIONS.put(key(second, first), result);
    }

    private static String key(Aspect first, Aspect second) {
        return first.id() + "+" + second.id();
    }

    public static Optional<Aspect> combine(Aspect first, Aspect second) {
        if (first == null || second == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(COMBINATIONS.get(key(first, second)));
    }


    public static Optional<Aspect[]> decompose(Aspect compound) {
        if (compound == null || compound.isPrimal()) {
            return Optional.empty();
        }
        return Optional.of(new Aspect[] {compound.firstComponent(), compound.secondComponent()});
    }

    public static boolean isOriginalComponentPair(Aspect result, Aspect first, Aspect second) {
        if (result == null || first == null || second == null || result.isPrimal()) {
            return false;
        }
        Aspect left = result.firstComponent();
        Aspect right = result.secondComponent();
        return (left == first && right == second) || (left == second && right == first);
    }

    public static boolean isOriginalDirectLink(Aspect first, Aspect second) {
        if (first == null || second == null) {
            return false;
        }
        if (first == second) {
            return true;
        }
        return first.firstComponent() == second
                || first.secondComponent() == second
                || second.firstComponent() == first
                || second.secondComponent() == first
                || combine(first, second).isPresent();
    }

    public static boolean canCombine(Aspect first, Aspect second) {
        return combine(first, second).isPresent();
    }

    public static int count() {
        return COMBINATIONS.size() / 2;
    }
}
