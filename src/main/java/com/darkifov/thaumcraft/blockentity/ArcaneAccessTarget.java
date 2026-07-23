package com.darkifov.thaumcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.Set;
import java.util.UUID;

/** Shared owner/access contract used by TC4 warded plates and arcane doors. */
public interface ArcaneAccessTarget {
    BlockPos keyBindingPos();
    default byte keyTargetType() { return 0; }
    UUID owner();
    boolean isOwner(Player player);
    boolean hasAccess(Player player);
    boolean hasFullAccess(Player player);
    boolean mayBindKey(Player player, boolean goldKey);
    boolean addAccess(Player player, boolean full);
    Set<UUID> authorizedUsers();

    default boolean sharesAuthorization(ArcaneAccessTarget other) {
        if (other == null) return false;
        Set<UUID> mine = authorizedUsers();
        for (UUID id : other.authorizedUsers()) {
            if (mine.contains(id)) return true;
        }
        return false;
    }
}
