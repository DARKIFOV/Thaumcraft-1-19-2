package com.darkifov.thaumcraft.client.render;

import java.util.List;

/**
 * Stage218 metadata bridge for the original TC4 ModelEldritchGuardian and
 * ModelEldritchGolem trees.
 *
 * <p>The 1.7.10 classes are huge ModelRenderer graphs.  This class records the
 * part names, dominant proportions and animation constants used by the 1.19.2
 * render adapters so future stages can replace each bridge box with a direct
 * ModelPart port without changing renderer behaviour.  The names intentionally
 * match the original field names where the decompiled sources expose them.</p>
 */
public final class TC4EldritchBossModelParity {
    public static final String ORIGINAL_GUARDIAN_MODEL = "ModelEldritchGuardian";
    public static final String ORIGINAL_GOLEM_MODEL = "ModelEldritchGolem";

    public static final float GUARDIAN_RENDER_SCALE = 1.0F;
    public static final float WARDEN_RENDER_SCALE = 1.5F;
    public static final float GOLEM_RENDER_SCALE = 2.15F;
    public static final float SPAWN_TIMER_TICKS = 150.0F;
    public static final int GOLEM_HEADLESS_SPAWN_TIMER = 100;
    public static final int GOLEM_BEAM_CHARGE_TICKS = 150;

    public static final List<String> GUARDIAN_ORIGINAL_PARTS = List.of(
            "BeltR", "Mbelt", "MbeltL", "MbeltR", "BeltL", "Hood4", "Cloak3",
            "Chestplate", "HoodEye", "Hood1", "Hood2", "Hood3", "Backplate",
            "Cloak1", "Cloak2", "ShoulderplateTopR", "ShoulderplateR1",
            "ShoulderplateR2", "ShoulderplateR3", "ShoulderR", "ArmR3",
            "ArmL1", "ArmL3", "ArmR1", "ArmR2", "ArmL2", "ShoulderL",
            "ShoulderplateLtop", "ShoulderplateL1", "ShoulderplateL2",
            "ShoulderplateL3", "LegpanelR4", "LegpanelR5", "LegpanelR6",
            "SidepanelR1", "BackpanelR1", "BackpanelR2", "BackpanelR3",
            "BackpanelL3", "LegpanelL4", "LegpanelL5", "LegpanelL6",
            "SidepanelL1", "SidepanelR4", "BackpanelL1", "BackpanelL2",
            "LegpanelC1", "LegpanelC2", "LegpanelC3", "SidepanelR3",
            "SidepanelL4", "SidepanelL3", "SidepanelR2", "SidepanelL2"
    );

    public static final List<String> GOLEM_ORIGINAL_PARTS = List.of(
            "Head", "Jaw", "Body", "Core", "ShoulderL", "ShoulderR", "ArmL",
            "ArmR", "ForearmL", "ForearmR", "HandL", "HandR", "LegL", "LegR",
            "FootL", "FootR", "BackPlate", "ChestPlate", "HeadlessVent",
            "HeadlessGlow", "BeamCore"
    );

    public static final Box WARDEN_HEAD = new Box(-0.24F, 0.86F, -0.17F, 0.24F, 1.20F, 0.17F);
    public static final Box WARDEN_TORSO = new Box(-0.30F, 0.34F, -0.16F, 0.30F, 0.88F, 0.16F);
    public static final Box WARDEN_LEFT_ARM = new Box(-0.72F, 0.40F, -0.12F, -0.34F, 0.74F, 0.12F);
    public static final Box WARDEN_RIGHT_ARM = new Box(0.34F, 0.40F, -0.12F, 0.72F, 0.74F, 0.12F);
    public static final Box WARDEN_LEFT_LEG = new Box(-0.24F, 0.00F, -0.13F, -0.04F, 0.38F, 0.13F);
    public static final Box WARDEN_RIGHT_LEG = new Box(0.04F, 0.00F, -0.13F, 0.24F, 0.38F, 0.13F);
    public static final Box WARDEN_HALO = new Box(-0.35F, 0.28F, -0.22F, 0.35F, 1.18F, 0.22F);

    public static final Box GOLEM_TORSO = new Box(-0.34F, 0.38F, -0.22F, 0.34F, 0.96F, 0.22F);
    public static final Box GOLEM_HEAD = new Box(-0.28F, 0.96F, -0.20F, 0.28F, 1.34F, 0.20F);
    public static final Box GOLEM_HEADLESS_GLOW = new Box(-0.25F, 0.88F, -0.18F, 0.25F, 1.02F, 0.18F);
    public static final Box GOLEM_LEFT_ARM = new Box(-0.74F, 0.42F, -0.16F, -0.36F, 0.88F, 0.16F);
    public static final Box GOLEM_RIGHT_ARM = new Box(0.36F, 0.42F, -0.16F, 0.74F, 0.88F, 0.16F);
    public static final Box GOLEM_LEFT_LEG = new Box(-0.25F, 0.00F, -0.15F, -0.05F, 0.40F, 0.15F);
    public static final Box GOLEM_RIGHT_LEG = new Box(0.05F, 0.00F, -0.15F, 0.25F, 0.40F, 0.15F);
    public static final Box GOLEM_BEAM_GLOW = new Box(-0.42F, 0.30F, -0.27F, 0.42F, 1.08F, 0.27F);

    private TC4EldritchBossModelParity() {
    }

    public record Box(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        public Box yOffset(float deltaMin, float deltaMax) {
            return new Box(minX, minY + deltaMin, minZ, maxX, maxY + deltaMax, maxZ);
        }
    }
}
