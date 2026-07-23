package com.darkifov.thaumcraft.essentia;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.block.EssentiaJarBlock;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaReservoirBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaTubeBlockEntity;
import com.darkifov.thaumcraft.mirror.EssentiaMirrorBlockEntity;

/**
 * v11.63.72 source-linked parity ledger for TC4 essentia storage, suction,
 * tube propagation cadence and essentia-mirror transfer constraints.
 */
public final class TC4EssentiaParity {
    public static final String CONTRACT_VERSION = "11.63.72";
    public static final int TUBE_SUBTYPE_COUNT = 6;

    private TC4EssentiaParity() {}

    public static boolean storageCapacitiesAndSuctionMatchTc4() {
        return EssentiaJarBlock.CAPACITY == 64
                && EssentiaReservoirBlockEntity.CAPACITY == 256
                && EssentiaSuction.JAR_NORMAL == 32
                && EssentiaSuction.JAR_FILTERED == 64
                && EssentiaSuction.JAR_VOID == 32
                && EssentiaSuction.JAR_VOID_FILTERED == 48
                && EssentiaReservoirBlockEntity.ORIGINAL_RESERVOIR_SUCTION == 24;
    }

    public static boolean storagePullCadenceMatchesTc4() {
        return EssentiaJarBlockEntity.ORIGINAL_FILL_INTERVAL_TICKS == 5
                && EssentiaReservoirBlockEntity.ORIGINAL_FILL_INTERVAL_TICKS == 5;
    }

    public static boolean tubeSubtypesAndPropagationMatchTc4() {
        return EssentiaTubeSubtype.values().length == TUBE_SUBTYPE_COUNT
                && EssentiaTubeSubtype.NORMAL.transformNeighbourSuction(32) == 31
                && EssentiaTubeSubtype.RESTRICT.transformNeighbourSuction(32) == 16
                && EssentiaTubeSubtype.BUFFER.minimumSuction() == 0
                && EssentiaTubeSubtype.NORMAL.minimumSuction() == 1
                && EssentiaTubeSubtype.FILTER.allowsAspect(Aspect.AER, Aspect.AER)
                && !EssentiaTubeSubtype.FILTER.allowsAspect(Aspect.AER, Aspect.IGNIS)
                && EssentiaTubeSubtype.ONEWAY.directionalFlow()
                && EssentiaTubeSubtype.VALVE.redstoneValve()
                && EssentiaTubeBlockEntity.ORIGINAL_SUCTION_RECALC_INTERVAL_TICKS == 2
                && EssentiaTubeBlockEntity.ORIGINAL_EQUALIZE_INTERVAL_TICKS == 5
                && EssentiaTubeBlockEntity.ORIGINAL_BUFFER_BELLOWS_REFRESH_TICKS == 20;
    }

    public static boolean mirrorTransferContractMatchesTc4() {
        return EssentiaMirrorBlockEntity.RANGE == 8
                && EssentiaMirrorBlockEntity.TRANSFER_UNIT == 1;
    }
}
