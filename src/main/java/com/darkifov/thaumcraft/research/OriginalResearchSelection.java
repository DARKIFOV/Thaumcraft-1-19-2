package com.darkifov.thaumcraft.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public final class OriginalResearchSelection {
    private static final String ROOT = "ThaumcraftOriginalResearchSelection";
    private static final String SELECTED_KEY = "SelectedResearchKey";

    private OriginalResearchSelection() {
    }

    public static void set(Player player, String key) {
        if (player == null || key == null || key.isBlank()) {
            return;
        }

        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        root.putString(SELECTED_KEY, key);
        player.getPersistentData().put(ROOT, root);
    }

    public static Optional<String> get(Player player) {
        if (player == null) {
            return Optional.empty();
        }

        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        if (!root.contains(SELECTED_KEY)) {
            return Optional.empty();
        }

        String key = root.getString(SELECTED_KEY);
        return key == null || key.isBlank() ? Optional.empty() : Optional.of(key);
    }

    public static Optional<ResearchEntry> getEntry(Player player) {
        return get(player).flatMap(OriginalResearchBridge::byKey);
    }

    public static void clear(Player player) {
        if (player == null) {
            return;
        }

        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        root.remove(SELECTED_KEY);
        player.getPersistentData().put(ROOT, root);
    }
}
