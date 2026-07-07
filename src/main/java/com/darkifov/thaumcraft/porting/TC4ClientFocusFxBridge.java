package com.darkifov.thaumcraft.porting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * Stage181 Forge-1.19.2 client adapter for original TC4 focus FX calls.
 *
 * Original TC4 invoked client-only proxy methods directly from focus classes:
 *   - Thaumcraft.proxy.beamCont(...)
 *   - Thaumcraft.proxy.sparkle(...)
 *   - Thaumcraft.proxy.excavateFX(...)
 *   - ItemFocusShock.shootLightning(... FXLightningBolt ...)
 *
 * Common focus code cannot import client render classes directly on a dedicated
 * server in 1.19.2, so this bridge keeps the original call sites while routing
 * them to a client-only implementation only when the active distribution is the
 * Minecraft client.  It is an adapter, not a replacement mechanic.
 */
public final class TC4ClientFocusFxBridge {
    private TC4ClientFocusFxBridge() {
    }

    public static void beamCont(Level level, Player player, Vec3 target, int type, int color, boolean reverse, float endMod, int impact) {
        if (!level.isClientSide) {
            return;
        }
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.darkifov.thaumcraft.client.fx.TC4ClientFocusFx.beamCont(player, target, type, color, reverse, endMod, impact));
    }

    public static void sparkleCloud(Level level, Vec3 pos, float spread, int count, int colorType) {
        if (!level.isClientSide) {
            return;
        }
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.darkifov.thaumcraft.client.fx.TC4ClientFocusFx.sparkleCloud(pos, spread, count, colorType));
    }

    public static void shockLightning(Level level, Player player, Vec3 target, boolean offset) {
        if (!level.isClientSide) {
            return;
        }
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.darkifov.thaumcraft.client.fx.TC4ClientFocusFx.shockLightning(player, target, offset));
    }

    public static void excavateFX(Level level, BlockPos pos, Player player, int progress) {
        if (!level.isClientSide) {
            return;
        }
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.darkifov.thaumcraft.client.fx.TC4ClientFocusFx.excavateFX(pos, player, progress));
    }
}
