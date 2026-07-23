package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraVisRelayNetwork;
import com.darkifov.thaumcraft.block.BellowsBlock;
import com.darkifov.thaumcraft.block.InfernalFurnaceBlock;
import com.darkifov.thaumcraft.block.TC4InfernalFurnaceParity;
import com.darkifov.thaumcraft.porting.TC4ResearchItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Exact TC4 TileArcaneFurnace inventory, cadence, bonuses, ejection and XP. */
public final class InfernalFurnaceBlockEntity extends BlockEntity {
    private static final Direction[] BELLOWS_DIRECTIONS = {
            Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };
    private final List<ItemStack> inventory = new ArrayList<>(TC4InfernalFurnaceParity.INVENTORY_SIZE);
    private int furnaceCookTime;
    private int furnaceMaxCookTime;
    private int speedyTime;

    public InfernalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.INFERNAL_FURNACE_BLOCK_ENTITY.get(), pos, state);
        for (int i = 0; i < TC4InfernalFurnaceParity.INVENTORY_SIZE; i++) inventory.add(ItemStack.EMPTY);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, InfernalFurnaceBlockEntity furnace) {
        if (!(level instanceof ServerLevel server)) return;
        boolean cookedFlag = false;
        if (furnace.furnaceCookTime > 0) {
            furnace.furnaceCookTime--;
            cookedFlag = true;
        }
        if (cookedFlag && furnace.speedyTime > 0) furnace.speedyTime--;
        if (furnace.speedyTime <= 0) {
            furnace.speedyTime = AuraVisRelayNetwork.drainMachineVis(server, pos, Aspect.IGNIS,
                    TC4InfernalFurnaceParity.IGNIS_VIS_REQUEST_CENTIVIS);
        }
        if (furnace.furnaceMaxCookTime == 0) furnace.furnaceMaxCookTime = furnace.calcCookTime();
        if (furnace.furnaceCookTime > furnace.furnaceMaxCookTime) furnace.furnaceCookTime = furnace.furnaceMaxCookTime;

        if (furnace.furnaceCookTime == 0 && cookedFlag) furnace.smeltFirst(server);
        if (furnace.furnaceCookTime == 0 && !cookedFlag) {
            for (int slot = 0; slot < furnace.inventory.size(); slot++) {
                if (furnace.canSmelt(slot)) {
                    furnace.furnaceMaxCookTime = furnace.calcCookTime();
                    furnace.furnaceCookTime = furnace.furnaceMaxCookTime;
                    break;
                }
            }
        }
        if (cookedFlag || furnace.furnaceCookTime > 0) furnace.markAndSync();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, InfernalFurnaceBlockEntity furnace) {
        // TC4 client effects are delivered by block event id 3 after a completed smelt.
    }

    public int speedyTime() {
        return speedyTime;
    }

    public int cookTime() {
        return furnaceCookTime;
    }

    public int maxCookTime() {
        return furnaceMaxCookTime;
    }

    public void addSpeedyTime(int amount) {
        if (amount <= 0) return;
        speedyTime += amount;
        markAndSync();
    }

    public void refreshFacing() {
        markAndSync();
    }

    public boolean addItemsToInventory(ItemStack items) {
        if (items.isEmpty()) return false;
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack current = inventory.get(slot);
            if (!current.isEmpty() && ItemStack.isSameItemSameTags(current, items)
                    && current.getCount() + items.getCount() <= items.getMaxStackSize()) {
                current.grow(items.getCount());
                if (!canSmelt(slot)) destroyItem(slot);
                markAndSync();
                return true;
            }
            if (current.isEmpty()) {
                inventory.set(slot, items.copy());
                if (!canSmelt(slot)) destroyItem(slot);
                markAndSync();
                return true;
            }
        }
        return false;
    }

    private void destroyItem(int slot) {
        inventory.set(slot, ItemStack.EMPTY);
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3F,
                    2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
            level.addParticle(ParticleTypes.LAVA,
                    worldPosition.getX() + level.random.nextFloat(), worldPosition.getY() + 1.0D,
                    worldPosition.getZ() + level.random.nextFloat(), 0.0D, 0.0D, 0.0D);
        }
    }

    private int activeBellows() {
        if (level == null) return 0;
        int count = 0;
        for (Direction direction : BELLOWS_DIRECTIONS) {
            BlockPos bellowsPos = worldPosition.relative(direction, 2);
            if (BellowsBlock.isActiveBellows(level, bellowsPos, direction.getOpposite())) count++;
        }
        return Math.min(TC4InfernalFurnaceParity.MAX_BELLOWS, count);
    }

    private int calcCookTime() {
        return TC4InfernalFurnaceParity.cookTime(speedyTime > 0, activeBellows());
    }

    private boolean canSmelt(int slot) {
        return recipeFor(inventory.get(slot)).isPresent();
    }

    private Optional<? extends AbstractCookingRecipe> recipeFor(ItemStack input) {
        if (level == null || input.isEmpty()) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(input), level);
    }

    private void smeltFirst(ServerLevel server) {
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack input = inventory.get(slot);
            Optional<? extends AbstractCookingRecipe> recipe = recipeFor(input);
            if (recipe.isEmpty()) continue;
            ItemStack output = recipe.get().getResultItem().copy();
            if (!output.isEmpty()) {
                ejectItem(server, output, input.copy(), recipe.get().getExperience());
                server.blockEvent(worldPosition, getBlockState().getBlock(), 3, 0);
                input.shrink(1);
                if (input.isEmpty()) inventory.set(slot, ItemStack.EMPTY);
                markAndSync();
            }
            break;
        }
    }

    private void ejectItem(ServerLevel server, ItemStack output, ItemStack originalInput, float experience) {
        Direction facing = getBlockState().hasProperty(InfernalFurnaceBlock.FACING)
                ? getBlockState().getValue(InfernalFurnaceBlock.FACING) : Direction.SOUTH;
        int bellows = activeBellows();
        spawnOutput(server, output, facing, 0.03F);

        Bonus bonus = smeltingBonus(originalInput);
        if (bonus != null) {
            int count = bonus.initialCount();
            if (bellows == 0) {
                if (server.random.nextInt(4) == 0) count++;
            } else {
                for (int i = 0; i < bellows; i++) if (server.random.nextFloat() < 0.44F) count++;
            }
            if (count > 0) {
                spawnOutput(server, new ItemStack(bonus.item(), count), facing, 0.03F);
            }
        }
        spawnExperience(server, output.getCount(), experience, facing);
    }

    private void spawnOutput(ServerLevel server, ItemStack stack, Direction facing, float randomMotion) {
        double x = worldPosition.getX() + 0.5D + facing.getStepX() * 1.2D;
        double z = worldPosition.getZ() + 0.5D + facing.getStepZ() * 1.2D;
        ItemEntity entity = new ItemEntity(server, x, worldPosition.getY() + 0.4D, z, stack);
        double mx = facing.getStepX() == 0
                ? (server.random.nextFloat() - server.random.nextFloat()) * randomMotion : facing.getStepX() * 0.13D;
        double mz = facing.getStepZ() == 0
                ? (server.random.nextFloat() - server.random.nextFloat()) * randomMotion : facing.getStepZ() * 0.13D;
        entity.setDeltaMovement(mx, 0.0D, mz);
        server.addFreshEntity(entity);
    }

    private void spawnExperience(ServerLevel server, int outputCount, float experience, Direction facing) {
        int total = outputCount;
        if (experience == 0.0F) total = 0;
        else if (experience < 1.0F) {
            float exact = outputCount * experience;
            total = Mth.floor(exact);
            if (total < Mth.ceil(exact) && Math.random() < exact - total) total++;
        }
        Vec3 origin = new Vec3(worldPosition.getX() + 0.5D + facing.getStepX() * 1.2D,
                worldPosition.getY() + 0.4D,
                worldPosition.getZ() + 0.5D + facing.getStepZ() * 1.2D);
        while (total > 0) {
            int split = ExperienceOrb.getExperienceValue(total);
            total -= split;
            ExperienceOrb orb = new ExperienceOrb(server, origin.x, origin.y, origin.z, split);
            double mx = facing.getStepX() == 0
                    ? (server.random.nextFloat() - server.random.nextFloat()) * 0.025D : facing.getStepX() * 0.13D;
            double mz = facing.getStepZ() == 0
                    ? (server.random.nextFloat() - server.random.nextFloat()) * 0.025D : facing.getStepZ() * 0.13D;
            orb.setDeltaMovement(mx, 0.0D, mz);
            server.addFreshEntity(orb);
        }
    }

    @Nullable
    private static Bonus smeltingBonus(ItemStack input) {
        if (input.isEmpty()) return null;
        String id = Registry.ITEM.getKey(input.getItem()).toString();
        if (id.equals("thaumcraft:tc4_clustergold") || input.is(oreTag("gold"))) return new Bonus(Items.GOLD_NUGGET, 0);
        if (id.equals("thaumcraft:tc4_clusteriron") || input.is(oreTag("iron"))) return registered("tc4_nuggetiron", 0);
        if (id.equals("thaumcraft:tc4_clustercinnabar") || input.is(oreTag("cinnabar"))) return registered("tc4_nuggetquicksilver", 0);
        if (id.equals("thaumcraft:tc4_clustercopper") || input.is(oreTag("copper"))) return registered("tc4_nuggetcopper", 0);
        if (id.equals("thaumcraft:tc4_clustertin") || input.is(oreTag("tin"))) return registered("tc4_nuggettin", 0);
        if (id.equals("thaumcraft:tc4_clustersilver") || input.is(oreTag("silver"))) return registered("tc4_nuggetsilver", 0);
        if (id.equals("thaumcraft:tc4_clusterlead") || input.is(oreTag("lead"))) return registered("tc4_nuggetlead", 0);
        if (input.is(Items.CHICKEN)) return registered("tc4_nuggetchicken", 1);
        if (input.is(Items.BEEF)) return registered("tc4_nuggetbeef", 1);
        if (input.is(Items.PORKCHOP)) return registered("tc4_nuggetpork", 1);
        if (input.is(Items.COD) || input.is(Items.SALMON) || input.is(Items.TROPICAL_FISH) || input.is(Items.PUFFERFISH)) {
            return registered("tc4_nuggetfish", 1);
        }
        return null;
    }

    private static TagKey<Item> oreTag(String metal) {
        return ItemTags.create(new ResourceLocation("forge", "ores/" + metal));
    }

    @Nullable
    private static Bonus registered(String id, int initialCount) {
        return TC4ResearchItems.registered(id).map(holder -> new Bonus(holder.get(), initialCount)).orElse(null);
    }

    private record Bonus(Item item, int initialCount) {
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 3 && level != null) {
            Direction facing = getBlockState().hasProperty(InfernalFurnaceBlock.FACING)
                    ? getBlockState().getValue(InfernalFurnaceBlock.FACING) : Direction.SOUTH;
            for (int i = 0; i < 5; i++) {
                level.addParticle(ParticleTypes.LAVA,
                        worldPosition.getX() + 0.5D + facing.getStepX() * 0.8D,
                        worldPosition.getY() + 0.4D,
                        worldPosition.getZ() + 0.5D + facing.getStepZ() * 0.8D,
                        facing.getStepX() * 0.05D, 0.05D, facing.getStepZ() * 0.05D);
                level.playLocalSound(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D, SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                        0.1F + level.random.nextFloat() * 0.1F, 0.9F + level.random.nextFloat() * 0.15F, false);
            }
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putShort(TC4InfernalFurnaceParity.NBT_COOK_TIME, (short) furnaceCookTime);
        tag.putShort(TC4InfernalFurnaceParity.NBT_SPEEDY_TIME, (short) speedyTime);
        ListTag items = new ListTag();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.get(slot);
            if (stack.isEmpty()) continue;
            CompoundTag item = new CompoundTag();
            item.putByte(TC4InfernalFurnaceParity.NBT_SLOT, (byte) slot);
            stack.save(item);
            items.add(item);
        }
        tag.put(TC4InfernalFurnaceParity.NBT_ITEMS, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        furnaceCookTime = tag.getShort(TC4InfernalFurnaceParity.NBT_COOK_TIME);
        speedyTime = tag.getShort(TC4InfernalFurnaceParity.NBT_SPEEDY_TIME);
        for (int i = 0; i < inventory.size(); i++) inventory.set(i, ItemStack.EMPTY);
        ListTag items = tag.getList(TC4InfernalFurnaceParity.NBT_ITEMS, Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag item = items.getCompound(i);
            int slot = item.getByte(TC4InfernalFurnaceParity.NBT_SLOT) & 255;
            if (slot < inventory.size()) inventory.set(slot, ItemStack.of(item));
        }
    }

    private void markAndSync() {
        setChanged();
        if (level instanceof ServerLevel server) {
            server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) load(tag);
    }
}
