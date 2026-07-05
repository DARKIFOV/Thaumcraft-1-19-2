package com.darkifov.thaumcraft.thaumicenergistics;

import com.darkifov.thaumcraft.Aspect;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class ThaumicAeGridReport {
    private final int controllers;
    private final int cables;
    private final int machines;
    private final int channelsUsed;
    private final int channelsTotal;
    private final int energyStored;
    private final int energyCapacity;
    private final int craftingCpus;
    private final Map<Aspect, Integer> essentia;

    public ThaumicAeGridReport(int controllers, int cables, int machines, int channelsUsed, int channelsTotal,
                               int energyStored, int energyCapacity, int craftingCpus, Map<Aspect, Integer> essentia) {
        this.controllers = controllers;
        this.cables = cables;
        this.machines = machines;
        this.channelsUsed = channelsUsed;
        this.channelsTotal = channelsTotal;
        this.energyStored = energyStored;
        this.energyCapacity = energyCapacity;
        this.craftingCpus = craftingCpus;
        this.essentia = new EnumMap<>(Aspect.class);
        if (essentia != null) {
            this.essentia.putAll(essentia);
        }
    }

    public int controllers() { return controllers; }
    public int cables() { return cables; }
    public int machines() { return machines; }
    public int channelsUsed() { return channelsUsed; }
    public int channelsTotal() { return channelsTotal; }
    public int energyStored() { return energyStored; }
    public int energyCapacity() { return energyCapacity; }
    public int craftingCpus() { return craftingCpus; }
    public Map<Aspect, Integer> essentia() { return Collections.unmodifiableMap(essentia); }

    public boolean hasController() { return controllers > 0; }
    public boolean hasEnergy() { return energyStored > 0; }
    public boolean hasFreeChannels() { return channelsUsed < channelsTotal; }
    public boolean hasCraftingCpu() { return craftingCpus > 0; }

    public int totalEssentia() {
        int total = 0;
        for (int value : essentia.values()) {
            total += value;
        }
        return total;
    }

    public String summary() {
        return "controllers=" + controllers
                + ", cables=" + cables
                + ", machines=" + machines
                + ", channels=" + channelsUsed + "/" + channelsTotal
                + ", energy=" + energyStored + "/" + energyCapacity
                + ", craftingCpu=" + craftingCpus
                + ", essentia=" + totalEssentia();
    }
}
