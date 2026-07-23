package com.darkifov.thaumcraft.block;

import net.minecraft.core.Direction;

/** Exact constants and pure calculations from TC4 Block/TileArcaneFurnace. */
public final class TC4InfernalFurnaceParity {
    public static final int SIZE = 3;
    public static final int INVENTORY_SIZE = 32;
    public static final int IGNIS_VIS_REQUEST_CENTIVIS = 5;
    public static final int IGNIS_ESSENTIA_SPEED_TICKS = 600;
    public static final int NOZZLE_DRAW_INTERVAL_TICKS = 5;
    public static final int NOZZLE_DRAW_ADMISSION_SPEED = 60;
    public static final int NOZZLE_SUCTION_ADMISSION_SPEED = 40;
    public static final int NOZZLE_SUCTION = 128;
    public static final int NORMAL_COOK_TIME = 140;
    public static final int SPEEDY_COOK_TIME = 80;
    public static final int COOK_REDUCTION_PER_BELLOWS = 20;
    public static final int MAX_BELLOWS = 3;
    public static final float BONUS_CHANCE_WITHOUT_BELLOWS = 0.25F;
    public static final float BONUS_CHANCE_PER_BELLOWS = 0.44F;
    public static final float LIVING_DAMAGE = 3.0F;
    public static final int LIVING_FIRE_SECONDS = 10;
    public static final double ITEM_BOUNCE_Y = 0.025D;
    public static final float BLOCK_HARDNESS = 10.0F;
    public static final float BLOCK_RESISTANCE = 500.0F;
    public static final int LIGHT_LEVEL = 13;
    public static final int FORMATION_IGNIS_CENTIVIS = 5000;
    public static final int FORMATION_TERRA_CENTIVIS = 5000;
    public static final String RESEARCH = "INFERNALFURNACE";
    public static final String NBT_ITEMS = "Items";
    public static final String NBT_SLOT = "Slot";
    public static final String NBT_COOK_TIME = "CookTime";
    public static final String NBT_SPEEDY_TIME = "SpeedyTime";

    private TC4InfernalFurnaceParity() {
    }

    public static int cookTime(boolean speedy, int bellows) {
        int clamped = Math.max(0, Math.min(MAX_BELLOWS, bellows));
        return (speedy ? SPEEDY_COOK_TIME : NORMAL_COOK_TIME) - COOK_REDUCTION_PER_BELLOWS * clamped;
    }

    public static int nozzlePart(Direction facing) {
        return switch (facing) {
            case NORTH -> 2;
            case SOUTH -> 8;
            case WEST -> 4;
            case EAST -> 6;
            default -> throw new IllegalArgumentException("Infernal Furnace nozzle must be horizontal");
        };
    }

    public static int partForLocal(int x, int z) {
        return 1 + z * 3 + x;
    }

    public static boolean originalPartRestoresObsidian(int part) {
        return part != 0 && part != 10 && ((part & 1) == 0 || part == 5);
    }

    public static int textureLevelCode(int part, InfernalFurnaceLayer layer, Direction nozzleFacing) {
        if (part == 0 || part == 10 || layer == InfernalFurnaceLayer.UPPER) return 0;
        if (layer == InfernalFurnaceLayer.MIDDLE) return 9;
        return part == 5 || part == nozzlePart(nozzleFacing) ? 0 : 18;
    }

    public static int textureIndex(int part, InfernalFurnaceLayer layer, Direction nozzleFacing,
                                   Direction face, boolean touchingNozzleOnFace) {
        if (part == 0) return -1; // lava renderer/model
        if (part == 10) return 7;
        int level = textureLevelCode(part, layer, nozzleFacing);
        int add = touchingNozzleOnFace ? 3 : 0;
        if (face == Direction.DOWN || face == Direction.UP) {
            if (face == Direction.UP && level == 18) {
                return switch (part) {
                    case 2 -> 16;
                    case 4 -> 17;
                    case 6 -> 26;
                    case 8 -> 25;
                    default -> add == 3 ? 6 : (part == 5 ? 10 : ((part - 1) % 3 + (part - 1) / 3 * 9));
                };
            }
            if (add == 3) return 6;
            if (part == 5) return 10;
            return (part - 1) % 3 + (part - 1) / 3 * 9;
        }
        return switch (face) {
            case NORTH -> switch (part) {
                case 1 -> 2 + level + add;
                case 2 -> 1 + level + add;
                case 3 -> level + add;
                default -> level == 9 ? 6 : 7;
            };
            case SOUTH -> switch (part) {
                case 7 -> level + add;
                case 8 -> 1 + level + add;
                case 9 -> 2 + level + add;
                default -> level == 9 ? 6 : 7;
            };
            case WEST -> switch (part) {
                case 1 -> level + add;
                case 4 -> 1 + level + add;
                case 7 -> 2 + level + add;
                default -> level == 9 ? 6 : 7;
            };
            case EAST -> switch (part) {
                case 3 -> 2 + level + add;
                case 6 -> 1 + level + add;
                case 9 -> level + add;
                default -> level == 9 ? 6 : 7;
            };
            default -> 7;
        };
    }
}
