package com.darkifov.thaumcraft.porting;

/**
 * Implemented by storage objects whose ItemStacks are not exposed through a
 * vanilla Container or Forge IItemHandler capability.
 */
public interface TC4LegacyStackMigrationTarget {
    int migrateLegacyStacks();
}
