package com.darkifov.thaumcraft.aura;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public final class AuraNodeWorldRuntime {
    private static final Aspect[] PRIMAL = new Aspect[]{Aspect.AER, Aspect.TERRA, Aspect.IGNIS, Aspect.AQUA, Aspect.ORDO, Aspect.PERDITIO};
    private static final Aspect[] COMPLEX = new Aspect[]{Aspect.VACUOS, Aspect.LUX, Aspect.TEMPESTAS, Aspect.MOTUS, Aspect.GELUM, Aspect.VITREUS, Aspect.VICTUS, Aspect.VENENUM, Aspect.POTENTIA, Aspect.PERMUTATIO, Aspect.METALLUM, Aspect.MORTUUS, Aspect.VOLATUS, Aspect.TENEBRAE, Aspect.SPIRITUS, Aspect.SANO, Aspect.ITER, Aspect.ALIENIS, Aspect.PRAECANTATIO, Aspect.AURAM, Aspect.VITIUM, Aspect.HERBA, Aspect.ARBOR, Aspect.BESTIA, Aspect.CORPUS, Aspect.EXANIMIS, Aspect.COGNITIO, Aspect.SENSUS, Aspect.HUMANUS, Aspect.MESSIS, Aspect.PERFODIO, Aspect.INSTRUMENTUM, Aspect.METO, Aspect.TELUM, Aspect.TUTAMEN, Aspect.FAMES, Aspect.LUCRUM, Aspect.FABRICO, Aspect.PANNUS, Aspect.MACHINA, Aspect.VINCULUM};
    public static final int TC4_DEFAULT_NODE_RARITY = 36;
    public static final int TC4_DEFAULT_SPECIAL_NODE_RARITY = 18;
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

    /**
     * v11.02 strict createRandomNodeAt bridge. Natural worldgen nodes must use
     * the same random distribution shape as TC4 ThaumcraftWorldGenerator:
     * silverwood/eerie override type, otherwise specialNodeRarity gates the
     * DARK/UNSTABLE/PURE/HUNGRY table; modifiers use specialNodeRarity / 2 and
     * may be absent. The older deterministic position-hash profile was useful
     * for placed/debug nodes, but it made natural node rarity noticeably drift.
     */
    public static AuraNodeProfile createRandomWorldgenProfile(ServerLevel level, BlockPos pos, RandomSource random,
                                                              boolean silverwood, boolean eerie, boolean small) {
        AuraNodeType type = chooseTypeLikeTC4(random, silverwood, eerie);
        boolean taintedBiome = isTaintedBiomeLikeTC4(level, pos);
        boolean taintedBoost = false;
        if (taintedBiome && type != AuraNodeType.PURE) {
            taintedBoost = random.nextBoolean();
            if (taintedBoost) {
                type = AuraNodeType.TAINTED;
            }
        }
        AuraNodeModifier modifier = chooseModifierLikeTC4(random);
        AspectList aspects = buildAspectsLikeTC4(level, pos, random, type, modifier, silverwood || small, taintedBiome, taintedBoost);
        return new AuraNodeProfile(type, modifier, aspects);
    }

    /**
     * TC4 natural nodes are a worldgen/populate feature, not a per-player
     * proximity backfill.  This method is only called from TC4WorldgenRuntime
     * for Forge new chunks.
     */
    public static void seedNaturalNodeForNewChunk(ServerLevel level, ChunkPos chunkPos, RandomSource random) {
        String key = level.dimension().location() + ":" + chunkPos.x + ":" + chunkPos.z;
        if (!CHECKED_CHUNKS.add(key)) {
            return;
        }

        // TC4 default Config.nodeRarity is 36.  Keep this as the baseline until
        // a full config bridge is ported; the old 10% player-area fallback was
        // much too aggressive and visibly different from original generation.
        if (random.nextInt(TC4_DEFAULT_NODE_RARITY) != 0) {
            return;
        }

        int x = chunkPos.getMinBlockX() + random.nextInt(16);
        int z = chunkPos.getMinBlockZ() + random.nextInt(16);
        int y = Math.min(level.getMaxBuildHeight() - 8, Math.max(level.getMinBuildHeight() + 8,
                level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z)).getY()
                        + 3 + random.nextInt(9)));
        BlockPos pos = new BlockPos(x, y, z);

        if (!canPlaceNaturalNode(level, pos)) {
            return;
        }

        level.setBlock(pos, ThaumcraftMod.AURA_NODE.get().defaultBlockState(), 3);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AuraNodeBlockEntity node) {
            AuraNodeProfile profile = createRandomWorldgenProfile(level, pos, random, false, false, false);
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


    private static AuraNodeType chooseTypeLikeTC4(RandomSource random, boolean silverwood, boolean eerie) {
        if (silverwood) {
            return AuraNodeType.PURE;
        }
        if (eerie) {
            return AuraNodeType.DARK;
        }
        if (random.nextInt(TC4_DEFAULT_SPECIAL_NODE_RARITY) == 0) {
            return switch (random.nextInt(10)) {
                case 0, 1, 2 -> AuraNodeType.DARK;
                case 3, 4, 5 -> AuraNodeType.UNSTABLE;
                case 6, 7, 8 -> AuraNodeType.PURE;
                default -> AuraNodeType.HUNGRY;
            };
        }
        return AuraNodeType.NORMAL;
    }

    private static AuraNodeModifier chooseModifierLikeTC4(RandomSource random) {
        if (random.nextInt(Math.max(1, TC4_DEFAULT_SPECIAL_NODE_RARITY / 2)) != 0) {
            return AuraNodeModifier.NORMAL;
        }
        return switch (random.nextInt(3)) {
            case 0 -> AuraNodeModifier.BRIGHT;
            case 1 -> AuraNodeModifier.PALE;
            default -> AuraNodeModifier.FADING;
        };
    }

    private static AspectList buildAspectsLikeTC4(ServerLevel level, BlockPos pos, RandomSource random,
                                                  AuraNodeType type, AuraNodeModifier modifier, boolean quarterAura,
                                                  boolean taintedBiome, boolean taintedBoost) {
        int baura = biomeAuraLikeTC4(level, pos);
        // v11.22 strict createRandomNodeAt parity: tainted biomes multiply aura
        // before the value roll, and a second multiplier applies when the node
        // actually becomes TAINTED.
        if (taintedBiome && type != AuraNodeType.PURE) {
            baura = Math.max(1, (int) (baura * 1.5F));
            if (taintedBoost) {
                baura = Math.max(1, (int) (baura * 1.5F));
            }
        }
        if (quarterAura) {
            // v11.42 strict createRandomNodeAt parity: TC4 simply quarters the
            // biome aura for silverwood/small nodes before rolling value. Keep a
            // small safety floor only to avoid invalid nextInt in modded biomes.
            baura = Math.max(2, baura / 4);
        }
        int value = random.nextInt(Math.max(1, baura / 2)) + Math.max(1, baura / 2);
        AspectList aspects = new AspectList();
        Aspect biomeTag = randomBiomeTagLikeTC4(level, pos, random);
        if (biomeTag != null) {
            aspects.add(biomeTag, 2);
        } else {
            aspects.add(COMPLEX[random.nextInt(COMPLEX.length)], 1);
            aspects.add(PRIMAL[random.nextInt(PRIMAL.length)], 1);
        }
        for (int a = 0; a < 3; a++) {
            if (!random.nextBoolean()) {
                continue;
            }
            if (random.nextInt(TC4_DEFAULT_SPECIAL_NODE_RARITY) == 0) {
                aspects.add(COMPLEX[random.nextInt(COMPLEX.length)], 1);
            } else {
                aspects.add(PRIMAL[random.nextInt(PRIMAL.length)], 1);
            }
        }
        if (type == AuraNodeType.HUNGRY) {
            aspects.add(Aspect.FAMES, 2);
            if (random.nextBoolean()) {
                aspects.add(Aspect.LUCRUM, 1);
            }
        } else if (type == AuraNodeType.PURE) {
            aspects.add(random.nextBoolean() ? Aspect.VICTUS : Aspect.ORDO, 2);
        } else if (type == AuraNodeType.DARK) {
            if (random.nextBoolean()) aspects.add(Aspect.MORTUUS, 1);
            if (random.nextBoolean()) aspects.add(Aspect.EXANIMIS, 1);
            if (random.nextBoolean()) aspects.add(Aspect.PERDITIO, 1);
            if (random.nextBoolean()) aspects.add(Aspect.TENEBRAE, 1);
        } else if (type == AuraNodeType.TAINTED) {
            aspects.add(Aspect.VITIUM, 2);
        }
        int water = 0;
        int lava = 0;
        int stone = 0;
        int foliage = 0;
        for (BlockPos scan : BlockPos.betweenClosed(pos.offset(-5, -5, -5), pos.offset(5, 5, 5))) {
            var state = level.getBlockState(scan);
            if (state.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) water++;
            if (state.getFluidState().is(net.minecraft.tags.FluidTags.LAVA)) lava++;
            if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE)) stone++;
            if (state.is(net.minecraft.tags.BlockTags.LEAVES) || state.is(net.minecraft.tags.BlockTags.FLOWERS)) foliage++;
        }
        if (water > 100) aspects.add(Aspect.AQUA, 1);
        if (lava > 100) { aspects.add(Aspect.IGNIS, 1); aspects.add(Aspect.TERRA, 1); }
        if (stone > 500) aspects.add(Aspect.TERRA, 1);
        if (foliage > 100) aspects.add(Aspect.HERBA, 1);

        AspectList scaled = new AspectList();
        java.util.List<AspectStack> entries = aspects.all();
        if (entries.isEmpty()) {
            scaled.add(PRIMAL[random.nextInt(PRIMAL.length)], value);
            return scaled;
        }
        int[] spread = new int[entries.size()];
        float total = 0.0F;
        for (int i = 0; i < entries.size(); i++) {
            spread[i] = entries.get(i).amount() == 2 ? 50 + random.nextInt(25) : 25 + random.nextInt(50);
            total += spread[i];
        }
        for (int i = 0; i < entries.size(); i++) {
            // TC4 AspectList.merge(...) keeps the seed amount already present in
            // the node's AspectList, then merges the scaled spread. v11.02/11.22
            // rebuilt a new list and accidentally dropped those initial 1/2
            // weights. The node modifier is stored separately; it must not scale
            // the generated AspectList here.
            int amount = Math.max(1, (int) (spread[i] / total * value));
            scaled.add(entries.get(i).aspect(), entries.get(i).amount() + amount);
        }
        return scaled;
    }


    private static boolean isTaintedBiomeLikeTC4(ServerLevel level, BlockPos pos) {
        String path = biomePath(level, pos);
        return path.contains("taint") || path.contains("crimson") || path.contains("corrupt");
    }

    private static int biomeAuraLikeTC4(ServerLevel level, BlockPos pos) {
        String path = biomePath(level, pos);
        int aura = 50;
        if (path.contains("forest") || path.contains("jungle") || path.contains("meadow")) aura += 10;
        if (path.contains("desert") || path.contains("badlands") || path.contains("nether")) aura -= 8;
        if (path.contains("swamp") || path.contains("mangrove")) aura += 6;
        if (path.contains("dark") || path.contains("deep_dark")) aura += 12;
        return Math.max(16, aura);
    }

    private static Aspect randomBiomeTagLikeTC4(ServerLevel level, BlockPos pos, RandomSource random) {
        // v11.02: mirrors BiomeHandler.getRandomBiomeTag use in createRandomNodeAt.
        // The 1.19.2 bridge maps biome names/tags to the nearest TC4 primal tag.
        if (random.nextInt(3) != 0) {
            return null;
        }
        String path = biomePath(level, pos);
        if (path.contains("desert") || path.contains("badlands") || path.contains("nether")) return Aspect.IGNIS;
        if (path.contains("ocean") || path.contains("river") || path.contains("swamp")) return Aspect.AQUA;
        if (path.contains("mountain") || path.contains("stony") || path.contains("cave") || path.contains("dripstone")) return Aspect.TERRA;
        if (path.contains("forest") || path.contains("jungle") || path.contains("meadow")) return Aspect.AER;
        if (path.contains("snow") || path.contains("ice") || path.contains("frozen")) return Aspect.ORDO;
        if (path.contains("taint") || path.contains("dark") || path.contains("deep_dark")) return Aspect.PERDITIO;
        return null;
    }

    private static String biomePath(ServerLevel level, BlockPos pos) {
        ResourceLocation id = level.registryAccess()
                .registryOrThrow(net.minecraft.core.Registry.BIOME_REGISTRY)
                .getKey(level.getBiome(pos).value());
        return id == null ? "" : id.getPath();
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
