package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.PechTradeTokenItem;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.menu.PechTradeMenu;
import com.darkifov.thaumcraft.porting.TC4ResearchItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class PechEntity extends PathfinderMob {
    public enum Variant {
        TRADER,
        HUNTER,
        ELDRITCH
    }

    private int happyTicks = 0;
    private Variant variant = Variant.TRADER;

    public PechEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        refreshName();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 22.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 18.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    public Variant variant() {
        return variant;
    }

    public void randomizeVariant() {
        int roll = random.nextInt(100);

        if (roll < 70) {
            variant = Variant.TRADER;
        } else if (roll < 92) {
            variant = Variant.HUNTER;
        } else {
            variant = Variant.ELDRITCH;
        }

        refreshName();
    }

    private void refreshName() {
        String name = switch (variant) {
            case TRADER -> "Печ-торговец";
            case HUNTER -> "Печ-охотник";
            case ELDRITCH -> "Древний Печ";
        };

        ChatFormatting color = switch (variant) {
            case TRADER -> ChatFormatting.GOLD;
            case HUNTER -> ChatFormatting.GREEN;
            case ELDRITCH -> ChatFormatting.DARK_PURPLE;
        };

        setCustomName(Component.literal(name).withStyle(color));
        setCustomNameVisible(true);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.75D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (held.getItem() instanceof PechTradeTokenItem token) {
            tradeWithHeldToken(player, held, token.tier());
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        int favorGift = giftFavorValue(held);

        if (favorGift > 0) {
            giveGift(player, held, favorGift);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            openTradeMenu(serverPlayer);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void openTradeMenu(ServerPlayer player) {
        NetworkHooks.openScreen(
                player,
                new SimpleMenuProvider(
                        (int id, Inventory inventory, Player menuPlayer) -> new PechTradeMenu(id, inventory, getId()),
                        Component.literal("Торговля Печа")
                ),
                buffer -> buffer.writeInt(getId())
        );
    }

    public void tradeFromGui(Player player, int tier) {
        if (level.isClientSide || distanceToSqr(player) > 64.0D) {
            return;
        }

        ItemStack token = findToken(player.getInventory(), tier);

        if (token.isEmpty() && !player.getAbilities().instabuild) {
            player.displayClientMessage(Component.literal("В инвентаре нет Pech Trade Token Tier " + tier + ".").withStyle(ChatFormatting.RED), false);
            return;
        }

        if (!player.getAbilities().instabuild) {
            token.shrink(1);
        }

        completeTrade(player, tier);
    }

    public void giftFromGui(Player player) {
        if (level.isClientSide || distanceToSqr(player) > 64.0D) {
            return;
        }

        ItemStack held = player.getMainHandItem();
        int favorGift = giftFavorValue(held);

        if (favorGift <= 0) {
            player.displayClientMessage(Component.literal("Печ принимает подарки: изумруд, золото, кристалл опыта, Ignis Fuel, Quicksilver, Eldritch Relic.").withStyle(ChatFormatting.GOLD), false);
            level.playSound(null, blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 0.7F, 0.9F);
            return;
        }

        giveGift(player, held, favorGift);
    }

    private void tradeWithHeldToken(Player player, ItemStack held, int tier) {
        if (level.isClientSide) {
            return;
        }

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }

        completeTrade(player, tier);
    }

    private void completeTrade(Player player, int tier) {
        ItemStack reward = PechTradeTokenItem.rewardForTier(tier, random);
        if (variant == Variant.ELDRITCH && tier >= 5 && random.nextInt(100) < 20) {
            reward = TC4ResearchItems.registered("tc4_focus_pech")
                    .map(item -> new ItemStack(item.get()))
                    .orElse(reward);
        }
        int favor = PlayerThaumData.getPechFavor(player);
        boolean bonus = random.nextInt(100) < Math.min(45, favor / 2 + tier * 3);

        if (variant == Variant.HUNTER && reward.is(ThaumcraftMod.IGNIS_FUEL.get())) {
            reward.grow(2);
        }

        if (variant == Variant.ELDRITCH && tier >= 3 && !reward.is(TC4ResearchItems.registered("tc4_focus_pech").map(r -> r.get()).orElse(Items.AIR)) && random.nextInt(100) < 20) {
            reward = new ItemStack(ThaumcraftMod.ELDRITCH_RELIC.get(), 1);
        }

        if (!player.getInventory().add(reward.copy())) {
            player.drop(reward.copy(), false);
        }

        if (bonus) {
            ItemStack bonusStack = PechTradeTokenItem.rewardForTier(Math.max(1, Math.min(5, tier - 1)), random);

            if (!player.getInventory().add(bonusStack.copy())) {
                player.drop(bonusStack.copy(), false);
            }

            player.displayClientMessage(Component.literal("Бонус за репутацию! Печ добавляет ещё одну награду.").withStyle(ChatFormatting.GREEN), false);
        }

        PlayerThaumData.addPechFavor(player, Math.max(1, tier));
        happyTicks = 120;

        player.displayClientMessage(
                Component.literal("Печ довольно ворчит и отдаёт: ")
                        .append(Component.literal(reward.getCount() + "x " + reward.getHoverName().getString()).withStyle(ChatFormatting.GOLD)),
                false
        );

        player.displayClientMessage(Component.literal("Репутация у Печей: " + PlayerThaumData.getPechFavor(player) + "/100").withStyle(ChatFormatting.DARK_PURPLE), false);
        level.playSound(null, blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.9F, 1.2F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.2D, getZ(), 12, 0.35D, 0.25D, 0.35D, 0.03D);
        }
    }

    private void giveGift(Player player, ItemStack held, int favorGift) {
        if (level.isClientSide) {
            return;
        }

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }

        PlayerThaumData.addPechFavor(player, favorGift);
        happyTicks = 120;
        player.displayClientMessage(
                Component.literal("Печ принимает подарок. Репутация: " + PlayerThaumData.getPechFavor(player) + "/100").withStyle(ChatFormatting.GOLD),
                false
        );

        level.playSound(null, blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.8F, 1.25F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.2D, getZ(), 14, 0.35D, 0.25D, 0.35D, 0.03D);
        }
    }

    private ItemStack findToken(Inventory inventory, int tier) {
        Item item = switch (tier) {
            case 1 -> ThaumcraftMod.PECH_TRADE_TIER_1.get();
            case 2 -> ThaumcraftMod.PECH_TRADE_TIER_2.get();
            case 3 -> ThaumcraftMod.PECH_TRADE_TIER_3.get();
            case 4 -> ThaumcraftMod.PECH_TRADE_TIER_4.get();
            case 5 -> ThaumcraftMod.PECH_TRADE_TIER_5.get();
            default -> ThaumcraftMod.PECH_TRADE_TIER_1.get();
        };

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (stack.is(item)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private int giftFavorValue(ItemStack stack) {
        if (stack.is(Items.EMERALD)) return 2;
        if (stack.is(Items.GOLD_INGOT)) return 2;
        if (stack.is(Items.GOLD_BLOCK)) return 8;
        if (stack.is(ThaumcraftMod.EXPERIENCE_SHARD.get())) return 3;
        if (stack.is(ThaumcraftMod.IGNIS_FUEL.get())) return 2;
        if (stack.is(ThaumcraftMod.QUICKSILVER_DROP.get())) return 1;
        if (stack.is(ThaumcraftMod.ELDRITCH_RELIC.get())) return 10;
        return 0;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (happyTicks > 0) {
            happyTicks--;

            if (!level.isClientSide && happyTicks % 20 == 0 && level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.2D, getZ(), 4, 0.25D, 0.2D, 0.25D, 0.02D);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("PechVariant", variant.name());
        tag.putInt("HappyTicks", happyTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        happyTicks = tag.getInt("HappyTicks");

        try {
            variant = Variant.valueOf(tag.getString("PechVariant"));
        } catch (IllegalArgumentException exception) {
            variant = Variant.TRADER;
        }

        refreshName();
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }
}
