package com.darkifov.thaumcraft.aura;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashSet;
import java.util.Set;

public final class AuraNodeWorldRuntime {
    private static final Aspect[] PRIMAL = new Aspect[]{Aspect.AER, Aspect.TERRA, Aspect.IGNIS, Aspect.AQUA, Aspect.ORDO, Aspect.PERDITIO};
    private static final Set<String> CHECKED_CHUNKS = new HashSet<>();

    private AuraNodeWorldRuntime() {
    }

    public static AuraNodeProfile createProfile(BlockPos pos) {
        long seed = seed(pos);
        AuraNodeType type = chooseType(seed);
        AuraNodeModifier modifier = chooseModifier(seed);
        AspectList aspects = buildAspects(pos, seed, type, modifier);
        return new AuraNodeProfile(type, modifier, aspects);
    }

    public static void seedNearbyNaturalNodes(ServerLevel level) {
        long time = level.getGameTime();
        if (time % 240L != 0L) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            ChunkPos center = player.chunkPosition();
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    trySeedChunk(level, new ChunkPos(center.x + dx, center.z + dz));
                }
            }
        }
    }

    private static void trySeedChunk(ServerLevel level, ChunkPos chunkPos) {
        String key = level.dimension().location() + ":" + chunkPos.x + ":" + chunkPos.z;
        if (!CHECKED_CHUNKS.add(key)) {
            return;
        }

        long seed = (((long) chunkPos.x) * 341873128712L) ^ (((long) chunkPos.z) * 132897987541L) ^ level.getSeed();
        int chance = Math.floorMod((int) (seed ^ (seed >>> 32)), 100);
        if (chance >= 10) {
            return;
        }

        int localX = 4 + Math.floorMod((int) seed, 8);
        int localZ = 4 + Math.floorMod((int) (seed >>> 8), 8);
        int x = chunkPos.getMinBlockX() + localX;
        int z = chunkPos.getMinBlockZ() + localZ;
        int y = Math.min(level.getMaxBuildHeight() - 8, Math.max(level.getMinBuildHeight() + 8, level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z)).getY() + 3 + Math.floorMod((int) (seed >>> 16), 9)));
        BlockPos pos = new BlockPos(x, y, z);

        if (!canPlaceNaturalNode(level, pos)) {
            return;
        }

        level.setBlock(pos, ThaumcraftMod.AURA_NODE.get().defaultBlockState(), 3);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AuraNodeBlockEntity node) {
            AuraNodeProfile profile = createProfile(pos);
            node.initializeAs(profile.type(), profile.modifier(), profile.aspects());
        }
    }

    private static boolean canPlaceNaturalNode(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }
        if (level.getBrightness(LightLayer.SKY, pos) <= 0 && pos.getY() < level.getSeaLevel() + 6) {
            return false;
        }
        for (BlockPos scan : BlockPos.betweenClosed(pos.offset(-24, -12, -24), pos.offset(24, 12, 24))) {
            if (level.getBlockState(scan).is(ThaumcraftMod.AURA_NODE.get())) {
                return false;
            }
        }
        LevelChunk chunk = level.getChunkAt(pos);
        return chunk != null && !level.getBlockState(pos.below()).is(Blocks.BEDROCK);
    }

    private static AuraNodeType chooseType(long seed) {
        int roll = Math.floorMod((int) seed, 100);
        if (roll < 3) {
            return AuraNodeType.PURE;
        }
        if (roll < 6) {
            return AuraNodeType.HUNGRY;
        }
        if (roll < 11) {
            return AuraNodeType.UNSTABLE;
        }
        if (roll < 16) {
            return AuraNodeType.DARK;
        }
        if (roll < 22) {
            return AuraNodeType.TAINTED;
        }
        return AuraNodeType.NORMAL;
    }

    private static AuraNodeModifier chooseModifier(long seed) {
        int roll = Math.floorMod((int) (seed >>> 20), 100);
        if (roll < 9) {
            return AuraNodeModifier.BRIGHT;
        }
        if (roll < 21) {
            return AuraNodeModifier.PALE;
        }
        if (roll < 27) {
            return AuraNodeModifier.FADING;
        }
        return AuraNodeModifier.NORMAL;
    }

    private static AspectList buildAspects(BlockPos pos, long seed, AuraNodeType type, AuraNodeModifier modifier) {
        AspectList aspects = new AspectList();
        int base = Math.max(6, Math.round((18 + Math.floorMod((int) (seed >>> 5), 34)) * modifier.capacityScale()));
        Aspect first = PRIMAL[Math.floorMod((int) seed, PRIMAL.length)];
        Aspect second = PRIMAL[Math.floorMod((int) (seed >>> 9), PRIMAL.length)];
        if (second == first) {
            second = PRIMAL[(second.ordinal() + 2) % PRIMAL.length];
        }
        aspects.add(first, base + bonus(type));
        aspects.add(second, Math.max(4, base / 2));

        if ((seed & 4L) != 0L || type == AuraNodeType.HUNGRY || type == AuraNodeType.UNSTABLE) {
            Aspect third = PRIMAL[Math.floorMod((int) (seed >>> 14), PRIMAL.length)];
            aspects.add(third, Math.max(3, base / 3));
        }

        if (type == AuraNodeType.PURE) {
            aspects.add(Aspect.PRAECANTATIO, Math.max(8, base / 2));
            aspects.add(Aspect.AURAM, Math.max(4, base / 3));
        } else if (type == AuraNodeType.TAINTED) {
            aspects.add(Aspect.VITIUM, Math.max(8, base / 2));
            aspects.add(Aspect.PERDITIO, 12);
        } else if (type == AuraNodeType.HUNGRY) {
            aspects.add(Aspect.VACUOS, Math.max(16, base));
            aspects.add(Aspect.FAMES, Math.max(8, base / 2));
        } else if (type == AuraNodeType.DARK) {
            aspects.add(Aspect.TENEBRAE, Math.max(10, base / 2));
            aspects.add(Aspect.MORTUUS, Math.max(4, base / 3));
        } else if (type == AuraNodeType.UNSTABLE) {
            aspects.add(Aspect.MOTUS, Math.max(8, base / 2));
            aspects.add(Aspect.PERDITIO, Math.max(6, base / 3));
        } else if (Math.floorMod(pos.getY(), 5) == 0) {
            aspects.add(Aspect.PRAECANTATIO, Math.max(3, base / 4));
        }
        return aspects;
    }

    private static int bonus(AuraNodeType type) {
        return switch (type) {
            case PURE -> 14;
            case HUNGRY -> 20;
            case UNSTABLE -> 8;
            case DARK -> 6;
            case TAINTED -> 10;
            default -> 0;
        };
    }

    private static long seed(BlockPos pos) {
        long seed = 1469598103934665603L;
        seed ^= pos.getX() * 1099511628211L;
        seed ^= pos.getY() * 1402946736689701973L;
        seed ^= pos.getZ() * 1609587929392839161L;
        seed ^= (seed >>> 33);
        seed *= 0xff51afd7ed558ccdL;
        seed ^= (seed >>> 33);
        return seed;
    }
}
