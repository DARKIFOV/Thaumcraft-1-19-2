package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.aura.AuraNodeType;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Client-visible compass stone that lights near a line-of-sight DARK node within 256 blocks. */
public final class TC4SinisterStoneItem extends Item {
    private static final String TAG_ACTIVE = "TC4SinisterActive";
    private static final double RANGE_SQR = 256.0D * 256.0D;
    private final boolean forcedActiveAlias;

    public TC4SinisterStoneItem(Properties properties, boolean forcedActiveAlias) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
        this.forcedActiveAlias = forcedActiveAlias;
    }

    public static float modelActive(ItemStack stack) {
        return stack.getItem() instanceof TC4SinisterStoneItem stone
                && (stone.forcedActiveAlias || (stack.getTag() != null && stack.getTag().getBoolean(TAG_ACTIVE))) ? 1.0F : 0.0F;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide || forcedActiveAlias || entity.tickCount % 10 != 0) return;
        stack.getOrCreateTag().putBoolean(TAG_ACTIVE, hasVisibleDarkNode(level, entity));
    }

    private static boolean hasVisibleDarkNode(Level level, Entity entity) {
        BlockPos origin = entity.blockPosition();
        int centerX = origin.getX() >> 4;
        int centerZ = origin.getZ() >> 4;
        Vec3 eye = entity.getEyePosition();
        for (int chunkX = centerX - 16; chunkX <= centerX + 16; chunkX++) {
            for (int chunkZ = centerZ - 16; chunkZ <= centerZ + 16; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) continue;
                for (BlockEntity blockEntity : level.getChunk(chunkX, chunkZ).getBlockEntities().values()) {
                    if (!(blockEntity instanceof AuraNodeBlockEntity node)
                            || node.typedNodeType() != AuraNodeType.DARK
                            || blockEntity.getBlockPos().distSqr(origin) > RANGE_SQR) continue;
                    Vec3 target = Vec3.atCenterOf(blockEntity.getBlockPos());
                    HitResult hit = level.clip(new ClipContext(eye, target, ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE, entity));
                    if (hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceToSqr(target) < 0.75D) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
