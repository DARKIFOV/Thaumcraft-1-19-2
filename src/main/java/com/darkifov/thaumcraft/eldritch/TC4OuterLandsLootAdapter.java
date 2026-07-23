package com.darkifov.thaumcraft.eldritch;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.PermanentItemEntity;
import com.darkifov.thaumcraft.entity.SpecialItemEntity;
import com.darkifov.thaumcraft.entity.TC4ThaumcraftBossEntity;
import com.darkifov.thaumcraft.porting.TC4RegistryGarbageGuard;
import com.darkifov.thaumcraft.runic.TC4ChampionModifierRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 1.19.2 bridge for TC4 Outer Lands boss/key-room loot and loot bag tables.
 *
 * <p>Stage222 tightens this class to the original TC4 call chain:</p>
 * <ul>
 *   <li>BlockLoot -> Utils.generateLoot(md, rand), count {@code 1 + md + rand(3)}.</li>
 *   <li>ItemLootBag#onItemRightClick -> {@code 8 + rand(5)} generated stacks.</li>
 *   <li>Config.addLootBagItem(...) weighted common/uncommon/rare pools.</li>
 *   <li>GenKeyRoom permanent {@code itemEldritchObject:2} and 2/3/4 guardians.</li>
 * </ul>
 *
 * <p>The concrete ItemStacks are 1.19.2-safe equivalents for the flattened port.  Each
 * weighted entry preserves the original source identifier and weight in code comments / ids
 * instead of importing 1.7.10 classes.</p>
 */
public final class TC4OuterLandsLootAdapter {
    public static final String PERMANENT_ITEM_TAG = "TC4PermanentItem";
    public static final String ORIGINAL_ELDRITCH_OBJECT_META_2 = "ConfigItems.itemEldritchObject:2";
    public static final String ORIGINAL_ELDRITCH_OBJECT_META_3 = "ConfigItems.itemEldritchObject:3";
    public static final String ORIGINAL_RARE_LOOTBAG = "ConfigItems.itemLootbag:2";

    private static final List<WeightedLoot> COMMON = new ArrayList<>();
    private static final List<WeightedLoot> UNCOMMON = new ArrayList<>();
    private static final List<WeightedLoot> RARE = new ArrayList<>();

    static {
        // Config.java lines 600-630: exact original weights, mapped to flattened 1.19.2 items.
        add(() -> research("tc4_alumentum", ThaumcraftMod.ALCHEMY_DUST.get(), 1), 2500, COMMON);               // itemResource:18 x1
        add(() -> research("tc4_alumentum", ThaumcraftMod.ALCHEMY_DUST.get(), 2), 2250, UNCOMMON);             // itemResource:18 x2
        add(() -> research("tc4_alumentum", ThaumcraftMod.ALCHEMY_DUST.get(), 3), 2000, RARE);                 // itemResource:18 x3
        add(TC4OuterLandsLootAdapter::eldritchObjectMeta3, 1, RARE);                                           // itemEldritchObject:3
        add(() -> new ItemStack(Items.NETHER_STAR), 1, RARE);                                                  // vanilla ultra-rare
        add(() -> new ItemStack(Items.IRON_INGOT), 10, COMMON);                                                // Items.field_151045_i
        add(() -> new ItemStack(Items.IRON_INGOT), 50, UNCOMMON, RARE);
        add(() -> new ItemStack(Items.GOLD_INGOT), 15, COMMON);                                                // Items.field_151166_bC
        add(() -> new ItemStack(Items.GOLD_INGOT), 75, UNCOMMON, RARE);
        add(() -> new ItemStack(Items.DIAMOND), 100, COMMON, UNCOMMON, RARE);                                  // Items.field_151043_k
        add(() -> new ItemStack(Items.EMERALD), 100, COMMON, UNCOMMON, RARE);                                  // Items.field_151079_bi
        add(() -> research("tc4_knowledgefragment", ThaumcraftMod.ELDRITCH_RELIC.get(), 1), 25, COMMON, UNCOMMON, RARE); // itemResource:9
        add(() -> research("tc4_amuletblank", Items.GOLD_NUGGET, 1), 10, COMMON);                             // itemBaubleBlanks:0
        add(() -> research("tc4_ringblank", Items.GOLD_NUGGET, 1), 10, COMMON);                               // itemBaubleBlanks:1
        add(() -> research("tc4_beltblank", Items.GOLD_NUGGET, 1), 10, COMMON);                               // itemBaubleBlanks:2
        for (int a = 3; a <= 8; a++) {
            final int meta = a;
            add(() -> research("tc4_baubleblank" + meta, ThaumcraftMod.THAUMIUM_NUGGET.get(), 1), 5, UNCOMMON); // itemBaubleBlanks:3..8
            add(() -> research("tc4_baubleblank" + meta, ThaumcraftMod.THAUMIUM_NUGGET.get(), 1), 7, RARE);
        }
        add(() -> research("tc4_amuletvis", ThaumcraftMod.VOID_METAL_INGOT.get(), 1), 6, UNCOMMON, RARE);      // random vis amulet
        add(() -> research("tc4_ringrunic", ThaumcraftMod.VOID_METAL_INGOT.get(), 1), 5, UNCOMMON, RARE);      // itemRingRunic
        add(() -> new ItemStack(Items.EXPERIENCE_BOTTLE), 5, COMMON);
        add(() -> new ItemStack(Items.EXPERIENCE_BOTTLE), 10, UNCOMMON);
        add(() -> new ItemStack(Items.EXPERIENCE_BOTTLE), 20, RARE);
        add(() -> new ItemStack(Items.POTION), 1, COMMON);                                                     // potion damage/meta family
        add(() -> new ItemStack(Items.POTION), 2, UNCOMMON);
        add(() -> new ItemStack(Items.POTION), 3, RARE);
        add(() -> new ItemStack(Items.SPLASH_POTION), 3, COMMON);
        add(() -> new ItemStack(Items.SPLASH_POTION), 6, UNCOMMON);
        add(() -> new ItemStack(Items.SPLASH_POTION), 9, RARE);
        add(() -> new ItemStack(Items.BOOK), 10, COMMON, UNCOMMON, RARE);                                      // enchanted in original when selected
    }

    private TC4OuterLandsLootAdapter() {
    }

    public static ItemStack eldritchObjectMeta2() {
        return research("tc4_eldritch_object_2", ThaumcraftMod.ELDRITCH_EYE.get(), 1);
    }

    public static ItemStack eldritchObjectMeta3() {
        return research("primordial_pearl", ThaumcraftMod.PRIMORDIAL_PEARL.get(), 1);
    }

    public static ItemStack rareLootbag() {
        return research("tc4_lootbagrare", ThaumcraftMod.ELDRITCH_RELIC.get(), 1);
    }

    public static ItemStack lootbag(int rarity) {
        return switch (Math.max(0, Math.min(2, rarity))) {
            case 1 -> research("tc4_lootbagunc", ThaumcraftMod.ELDRITCH_RELIC.get(), 1);
            case 2 -> research("tc4_lootbagrare", ThaumcraftMod.ELDRITCH_RELIC.get(), 1);
            default -> research("tc4_lootbag", ThaumcraftMod.ELDRITCH_RELIC.get(), 1);
        };
    }

    /** Stage222 exact ItemLootBag count contract: 8 + rand(5). */
    public static List<ItemStack> openLootBag(int rarity, RandomSource random) {
        int q = 8 + random.nextInt(5);
        List<ItemStack> result = new ArrayList<>(q);
        for (int i = 0; i < q; i++) {
            ItemStack loot = generateLoot(rarity, random);
            if (!loot.isEmpty()) {
                result.add(loot.copy());
            }
        }
        return result;
    }

    /** Stage222 direct mirror for Utils.generateLoot(rarity, rand). */
    public static ItemStack generateLoot(int rarity, RandomSource random) {
        int md = Math.max(0, Math.min(2, rarity));
        ItemStack stack = ItemStack.EMPTY;
        if (md > 0 && random.nextFloat() < 0.025F * md) {
            stack = genGear(md, random);
            if (stack.isEmpty()) {
                stack = generateLoot(md, random);
            }
        } else {
            stack = pick(md == 2 ? RARE : md == 1 ? UNCOMMON : COMMON, random);
        }
        if (stack.is(Items.BOOK)) {
            stack.getOrCreateTag().putBoolean("TC4WouldEnchantBook", true);
            stack.getOrCreateTag().putInt("TC4EnchantPower", 5 + (int)(md * 0.75F * random.nextInt(18)));
        }
        stack = TC4LootPotionEnchantAdapter.postProcessGeneratedLoot(stack, md, random);
        if (TC4RegistryGarbageGuard.isHiddenFromCreative(stack)) {
            stack = new ItemStack(Items.GOLD_INGOT, 1 + md);
            stack.getOrCreateTag().putBoolean("TC4QuarantinedLootReplacement", true);
        }
        stack.getOrCreateTag().putString("TC4LootRarity", switch (md) { case 1 -> "uncommon"; case 2 -> "rare"; default -> "common"; });
        return stack.copy();
    }

    private static ItemStack pick(List<WeightedLoot> pool, RandomSource random) {
        int total = 0;
        for (WeightedLoot loot : pool) {
            total += Math.max(0, loot.weight);
        }
        if (total <= 0) {
            return ItemStack.EMPTY;
        }
        int roll = random.nextInt(total);
        for (WeightedLoot loot : pool) {
            roll -= Math.max(0, loot.weight);
            if (roll < 0) {
                ItemStack stack = loot.stack.get().copy();
                stack.getOrCreateTag().putString("TC4WeightedSource", loot.stack.toString());
                stack.getOrCreateTag().putInt("TC4WeightedWeight", loot.weight);
                return stack;
            }
        }
        return pool.get(pool.size() - 1).stack.get().copy();
    }

    private static ItemStack genGear(int rarity, RandomSource random) {
        int quality = random.nextInt(2);
        if (random.nextFloat() < 0.20F) quality++;
        if (random.nextFloat() < 0.15F) quality++;
        if (random.nextFloat() < 0.10F) quality++;
        if (random.nextFloat() < 0.095F) quality++;
        if (random.nextFloat() < 0.095F) quality++;
        int slot = random.nextInt(5);
        ItemStack stack;
        if (quality >= 4 && quality <= 5) {
            stack = switch (slot) {
                case 4 -> new ItemStack(ThaumcraftMod.CRIMSON_PLATE_HELM.get());
                case 3 -> new ItemStack(ThaumcraftMod.CRIMSON_PLATE_CHEST.get());
                case 2 -> new ItemStack(ThaumcraftMod.CRIMSON_PLATE_LEGS.get());
                case 1 -> new ItemStack(ThaumcraftMod.CRIMSON_PLATE_BOOTS.get());
                default -> new ItemStack(ThaumcraftMod.THAUMIUM_INGOT.get(), 1 + rarity);
            };
        } else {
            stack = switch (slot) {
                case 4 -> new ItemStack(Items.IRON_HELMET);
                case 3 -> new ItemStack(Items.IRON_CHESTPLATE);
                case 2 -> new ItemStack(Items.IRON_LEGGINGS);
                case 1 -> new ItemStack(Items.IRON_BOOTS);
                default -> new ItemStack(Items.IRON_SWORD);
            };
        }
        stack.getOrCreateTag().putInt("TC4GeneratedGearQuality", quality);
        return stack;
    }

    public static void dropBossDeathLoot(TC4ThaumcraftBossEntity boss, DamageSource source, int looting) {
        spawnSpecialBossDrop(boss, eldritchObjectMeta3());
        boss.spawnAtLocation(rareLootbag());
        if (boss.getRandom().nextFloat() < 0.20F + looting * 0.05F) {
            boss.spawnAtLocation(new ItemStack(ThaumcraftMod.ELDRITCH_GUARDIAN_CORE.get(), 1));
        }
    }

    /** Stage283-302 overload for TC4 boss entities that were not subclasses of EntityThaumcraftBoss. */
    public static void dropBossDeathLoot(LivingEntity boss, DamageSource source, int looting) {
        spawnSpecialBossDrop(boss, eldritchObjectMeta3());
        boss.spawnAtLocation(rareLootbag());
        if (boss.getRandom().nextFloat() < 0.20F + looting * 0.05F) {
            boss.spawnAtLocation(new ItemStack(ThaumcraftMod.ELDRITCH_GUARDIAN_CORE.get(), 1));
        }
    }

    public static void populateKeyRoomChest(ServerLevel level, BlockPos chestPos) {
        BlockEntity blockEntity = level.getBlockEntity(chestPos);
        if (!(blockEntity instanceof ChestBlockEntity chest)) {
            return;
        }
        chest.setItem(4, eldritchObjectMeta3());
        chest.setItem(10, rareLootbag());
        chest.setItem(13, new ItemStack(ThaumcraftMod.AWAKENED_CRIMSON_KEY.get(), 1));
        chest.setChanged();
    }

    public static void spawnPermanentKeyItem(ServerLevel level, BlockPos center) {
        PermanentItemEntity entity = new PermanentItemEntity(level, center.getX() + 0.5D,
                center.getY() + 1.5D, center.getZ() + 0.5D, eldritchObjectMeta2());
        entity.setDeltaMovement(0.0D, 0.0D, 0.0D);
        entity.setNoPickUpDelay();
        entity.getPersistentData().putBoolean(PERMANENT_ITEM_TAG, true);
        entity.getPersistentData().putString("TC4Original", ORIGINAL_ELDRITCH_OBJECT_META_2);
        level.addFreshEntity(entity);
    }

    private static void spawnSpecialBossDrop(LivingEntity boss, ItemStack stack) {
        if (boss.level.isClientSide || stack.isEmpty()) {
            return;
        }
        SpecialItemEntity entity = new SpecialItemEntity(boss.level, boss.getX(),
                boss.getY() + boss.getBbHeight() / 2.0D, boss.getZ(), stack);
        entity.setPickUpDelay(10);
        entity.setDeltaMovement(0.0D, 0.1D, 0.0D);
        boss.level.addFreshEntity(entity);
    }

    public static int keyRoomGuardianCount(ServerLevel level) {
        Difficulty difficulty = level.getDifficulty();
        return 2 + (difficulty == Difficulty.NORMAL ? 1 : difficulty == Difficulty.HARD ? 2 : 0);
    }

    public static void applyKeyRoomChampionRule(Mob entity, int index, int guardianCount) {
        if (index == 0 && guardianCount >= 4) {
            TC4ChampionModifierRuntime.makeChampion(entity, true);
        }
    }

    private static void add(Supplier<ItemStack> stack, int weight, List<WeightedLoot>... pools) {
        for (List<WeightedLoot> pool : pools) {
            pool.add(new WeightedLoot(stack, weight));
        }
    }

    private static ItemStack research(String id, Item fallback, int count) {
        Map<String, RegistryObject<Item>> map = ThaumcraftMod.TC4_RESEARCH_ITEMS;
        Optional<RegistryObject<Item>> object = Optional.ofNullable(map.get(id));
        Item item = object.map(RegistryObject::get).orElse(fallback);
        ItemStack stack = new ItemStack(item, count);
        stack.getOrCreateTag().putString("TC4OriginalLootId", id);
        return stack;
    }

    private record WeightedLoot(Supplier<ItemStack> stack, int weight) {
    }
}
