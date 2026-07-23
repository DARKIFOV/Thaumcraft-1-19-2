package com.darkifov.thaumcraft.block;

/** Dependency-free Thaumcraft 4.2.3.5 Arcane Ear constants and formulas. */
public final class TC4ArcaneEarParity {
    public static final String CONTRACT_VERSION = "11.64.19";

    public static final float BLOCK_HARDNESS = 2.5F;
    public static final float BLOCK_RESISTANCE = 10.0F;
    public static final int MIN_NOTE = 0;
    public static final int MAX_NOTE = 24;
    public static final int NOTE_COUNT = 25;
    public static final int MIN_TONE = 0;
    public static final int MAX_TONE = 4;
    public static final int REDSTONE_SIGNAL = 15;
    public static final int REDSTONE_PULSE_TICKS = 10;
    public static final double LISTEN_RANGE_SQUARED = 4096.0D;
    public static final float NOTE_SOUND_VOLUME = 3.0F;
    public static final double NOTE_PARTICLE_Y = 1.2D;
    public static final int SILENT_NOTE_EVENT = 5;
    public static final int OBJECT_SENSUS = 4;

    public static final String NBT_NOTE = "note";
    public static final String NBT_TONE = "tone";

    private TC4ArcaneEarParity() {
    }

    public static int clampNote(int note) {
        return Math.max(MIN_NOTE, Math.min(MAX_NOTE, note));
    }

    public static int clampTone(int tone) {
        return Math.max(MIN_TONE, Math.min(MAX_TONE, tone));
    }

    public static int nextNote(int note) {
        return (clampNote(note) + 1) % NOTE_COUNT;
    }

    public static float notePitch(int note) {
        return (float) Math.pow(2.0D, (clampNote(note) - 12) / 12.0D);
    }

    public static boolean matchesPlayedNote(int configuredTone, int configuredNote,
                                            int playedTone, int playedNote,
                                            double distanceSquared) {
        return configuredTone == playedTone
                && configuredNote == playedNote
                && playedTone >= MIN_TONE && playedTone <= MAX_TONE
                && playedNote >= MIN_NOTE && playedNote <= MAX_NOTE
                && distanceSquared <= LISTEN_RANGE_SQUARED;
    }

    public static int pulseAfterTick(int remainingTicks) {
        return remainingTicks > 0 ? remainingTicks - 1 : 0;
    }
}
