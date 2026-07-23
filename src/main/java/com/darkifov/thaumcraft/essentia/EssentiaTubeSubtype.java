package com.darkifov.thaumcraft.essentia;

import com.darkifov.thaumcraft.Aspect;

/**
 * Stage198 explicit mirror of original TC4 tube subclasses.
 * These names intentionally match the original 1.7.10 tile classes:
 * TileTube, TileTubeFilter, TileTubeRestrict, TileTubeOneway, TileTubeBuffer and TileTubeValve.
 */
public enum EssentiaTubeSubtype {
    NORMAL("TileTube", false, false, false, false, false),
    FILTER("TileTubeFilter", true, false, false, false, false),
    RESTRICT("TileTubeRestrict", false, true, false, false, false),
    ONEWAY("TileTubeOneway", false, false, true, false, false),
    BUFFER("TileTubeBuffer", false, false, false, true, false),
    VALVE("TileTubeValve", false, false, false, false, true);

    private final String originalClassName;
    private final boolean filter;
    private final boolean restrict;
    private final boolean directional;
    private final boolean buffer;
    private final boolean valve;

    EssentiaTubeSubtype(String originalClassName, boolean filter, boolean restrict, boolean directional, boolean buffer, boolean valve) {
        this.originalClassName = originalClassName;
        this.filter = filter;
        this.restrict = restrict;
        this.directional = directional;
        this.buffer = buffer;
        this.valve = valve;
    }

    public String originalClassName() {
        return originalClassName;
    }

    public boolean usesAspectFilter() {
        return filter;
    }

    public boolean restrictsSuction() {
        return restrict;
    }

    public boolean directionalFlow() {
        return directional;
    }

    public boolean storesBufferEssentia() {
        return buffer;
    }

    public boolean redstoneValve() {
        return valve;
    }

    /** TC4 TileTube and TileTubeBuffer both advertise minimum suction 0. */
    public int minimumSuction() {
        return TC4EssentiaTubeParity.MINIMUM_SUCTION;
    }

    public int transformNeighbourSuction(int neighbourSuction) {
        return TC4EssentiaTubeParity.propagatedSuction(neighbourSuction, restrict);
    }

    public boolean allowsAspect(Aspect filter, Aspect aspect) {
        return !usesAspectFilter() || filter == null || aspect == null || filter == aspect;
    }

    public static EssentiaTubeSubtype byName(String name) {
        if (name != null) {
            for (EssentiaTubeSubtype subtype : values()) {
                if (subtype.name().equalsIgnoreCase(name) || subtype.originalClassName.equalsIgnoreCase(name)) {
                    return subtype;
                }
            }
        }
        return NORMAL;
    }
}
