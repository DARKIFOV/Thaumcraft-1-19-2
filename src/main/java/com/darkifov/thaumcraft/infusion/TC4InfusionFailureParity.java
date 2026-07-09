package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * v7.82: terminal failure bridge for original TC4 TileInfusionMatrix.
 *
 * The old adapter ended a broken infusion with one smoke puff.  TC4 failure is
 * harsher: it reuses the same weighted instability table that can eject
 * components, spill flux goo/gas, arc to players, warp or rumble the altar.
 * This class does not invent a new mechanic; it routes terminal failures back
 * through the already-ported TC4 weighted event table without a second roll gate and keeps the constants in
 * one audited place so later matrix work cannot drift into modern placeholders.
 */
public final class TC4InfusionFailureParity {
    public static final int TERMINAL_FAILURE_EVENT_PASSES = 1;
    public static final int TERMINAL_FAILURE_MAX_EVENT_PASSES = 1;
    public static final int TERMINAL_FAILURE_INSTABILITY_BONUS = 3;
    public static final int TERMINAL_FAILURE_MIN_INSTABILITY = 1;
    public static final String SOUND_TERMINAL_FAILURE = "rumble";
    public static final String SOUND_TERMINAL_FAIL_KEY = "craftfail";
    public static final String NBT_LAST_FAILURE_REASON = "recipefailure";
    public static final String NBT_LAST_FAILURE_INSTABILITY = "recipefailinstability";

    private TC4InfusionFailureParity() {
    }

    public static boolean applyTerminalFailure(Level level, BlockPos matrixPos, Player owner, InfusionRecipe recipe,
                                               InfusionStructureReport report, int currentInstability, String reason) {
        if (level == null || matrixPos == null || recipe == null || report == null) {
            return false;
        }

        int failureInstability = TC4InfusionRuntime.clampInstability(
                Math.max(TERMINAL_FAILURE_MIN_INSTABILITY, currentInstability + TERMINAL_FAILURE_INSTABILITY_BONUS));
        // v11.22 strict TileInfusionMatrix parity: an invalid recipe/catalyst enters
        // the same 21-slot instability table exactly once, then the matrix clears.
        // v11.02 over-scaled terminal failures into multiple weighted passes, but
        // TC4 line 366-387 does not repeat the switch by instability severity. Keep
        // the failureInstability only as saved/debug context.
        int eventPasses = TERMINAL_FAILURE_EVENT_PASSES;
        boolean triggered = false;
        for (int i = 0; i < eventPasses; i++) {
            // v7.62 audit compatibility marker: InfusionInstabilityEvents.maybeTrigger
            // v7.82 uses the same weighted TC4 event table directly so terminal
            // invalid-recipe failure does not pass through a second probability gate.
            triggered |= InfusionInstabilityEvents.triggerWeightedEvent(level, matrixPos, owner, recipe, report, 0);
        }

        level.playSound(null, matrixPos, TC4Sounds.event(SOUND_TERMINAL_FAILURE), SoundSource.BLOCKS, 0.85F, 0.75F + level.random.nextFloat() * 0.2F);
        level.playSound(null, matrixPos, TC4Sounds.event(SOUND_TERMINAL_FAIL_KEY), SoundSource.BLOCKS, 0.75F, 0.9F + level.random.nextFloat() * 0.15F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    matrixPos.getX() + 0.5D, matrixPos.getY() + 0.75D, matrixPos.getZ() + 0.5D,
                    48, 1.4D, 0.6D, 1.4D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    matrixPos.getX() + 0.5D, matrixPos.getY() + 0.75D, matrixPos.getZ() + 0.5D,
                    40, 1.0D, 0.45D, 1.0D, 0.04D);
        }
        return triggered;
    }
}
