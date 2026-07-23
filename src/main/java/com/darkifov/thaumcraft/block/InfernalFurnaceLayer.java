package com.darkifov.thaumcraft.block;

import net.minecraft.util.StringRepresentable;

public enum InfernalFurnaceLayer implements StringRepresentable {
    LOWER("lower"),
    MIDDLE("middle"),
    UPPER("upper");

    private final String name;

    InfernalFurnaceLayer(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
