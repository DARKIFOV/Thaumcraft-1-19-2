package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.BellowsBlock;
import com.darkifov.thaumcraft.block.TC4ArcaneBellowsParity;
import com.darkifov.thaumcraft.menu.AlchemicalFurnaceMenu;
import com.darkifov.thaumcraft.aura.AuraVisRelayNetwork;
import com.darkifov.thaumcraft.essentia.TC4DistillationRuntime;
import com.darkifov.thaumcraft.alchemy.TC4AlchemicalFurnaceParity;
import com.darkifov.thaumcraft.block.AlchemicalFurnaceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TC4 TileAlchemyFurnace parity adapter.
 *
 * The normal furnace has two real slots (input and fuel), a 50-vis internal
 * reservoir, bellows-scaled cook time and the original alumentum speed flag.
 * The advanced registry block reuses the storage shell but exposes the original
 * 500-vis capacity until its complete multiblock/aura controller is finalized.
 */
public class AlchemicalFurnaceBlockEntity extends BlockEntity implements WorldlyContainer, net.minecraft.world.MenuProvider {
    public static final int CAPACITY = TC4AlchemicalFurnaceParity.FURNACE_CAPACITY;
    public static final int ADVANCED_CAPACITY = 500;
    public static final int ADVANCED_MAX_POWER = 500;
    public static final int ADVANCED_RECHARGE_CV = 50;
    public static final int MAX_BELLOWS = TC4ArcaneBellowsParity.MAX_GENERIC_ATTACHED_BELLOWS;
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    private static final int[] NO_SLOTS = new int[0];
    private static final int[] INPUT_SLOT = new int[] {SLOT_INPUT};
    private static final int[] FUEL_SLOT = new int[] {SLOT_FUEL};
    @SuppressWarnings("unchecked")
    private LazyOptional<IItemHandler>[] sidedHandlers = createSidedHandlers();

    @SuppressWarnings("unchecked")
    private LazyOptional<IItemHandler>[] createSidedHandlers() {
        LazyOptional<IItemHandler>[] handlers = new LazyOptional[Direction.values().length];
        for (Direction direction : Direction.values()) {
            handlers[direction.get3DDataValue()] = LazyOptional.of(() -> new SidedInvWrapper(this, direction));
        }
        return handlers;
    }

    private final AspectList aspects = new AspectList();
    /** Compatibility buffer for worlds made by the pre-v11.62.23 direct-use furnace. */
    private final AspectList pendingAspects = new AspectList();

    private ItemStack input = ItemStack.EMPTY;
    private ItemStack fuel = ItemStack.EMPTY;
    private int fuelTime;
    private int currentFuelTime;
    private int burnProgress;
    private int burnDuration = 100;
    private int bellows = -1;
    private int counter;
    private boolean speedBoost;

    // TC4 TileAlchemyFurnaceAdvanced controller state.
    private int advancedHeat;
    private int advancedEntropy;
    private int advancedWater;
    private int advancedProcessed;
    // Energized nodes store whole vis points. Credits preserve the unused half
    // when the original machine requests 50 centivis at a time.
    private int ignisCreditCv;
    private int perditioCreditCv;
    private int aquaCreditCv;

    public AlchemicalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ALCHEMICAL_FURNACE_BLOCK_ENTITY.get(), pos, state);
    }

    public AspectList aspects() {
        return aspects;
    }

    public AspectList pendingAspects() {
        return pendingAspects;
    }

    public ItemStack inputStack() {
        return input;
    }

    public ItemStack fuelStack() {
        return fuel;
    }

    public int fuelTime() {
        return fuelTime;
    }

    public int currentFuelTime() {
        return currentFuelTime;
    }

    public int burnProgress() {
        return burnProgress;
    }

    public int burnDuration() {
        return Math.max(1, burnDuration);
    }

    public int bellows() {
        return Math.max(0, bellows);
    }

    public boolean speedBoost() {
        return speedBoost;
    }

    public boolean active() {
        return fuelTime > 0 && (!input.isEmpty() || !pendingAspects.isEmpty());
    }

    public int capacity() {
        return getBlockState().is(ThaumcraftMod.ADVANCED_ALCHEMICAL_FURNACE.get()) ? ADVANCED_CAPACITY : CAPACITY;
    }

    public boolean isAdvanced() {
        return getBlockState().is(ThaumcraftMod.ADVANCED_ALCHEMICAL_FURNACE.get());
    }

    public int advancedHeat() {
        return advancedHeat;
    }

    public int advancedEntropy() {
        return advancedEntropy;
    }

    public int advancedWater() {
        return advancedWater;
    }

    public int advancedProcessedCooldown() {
        return advancedProcessed;
    }

    public boolean canAdvancedOutputTo(Direction face) {
        return isAdvanced() && face != null && face.getAxis().isHorizontal() && !aspects.isEmpty();
    }

    public Aspect advancedOutputType(Direction face) {
        return canAdvancedOutputTo(face) ? aspects.firstAspect() : null;
    }

    public int takeAdvancedOutput(Aspect aspect, int amount, Direction face) {
        if (!canAdvancedOutputTo(face) || aspect == null || amount <= 0) {
            return 0;
        }
        return removeUpTo(aspect, amount);
    }

    public void restoreAdvancedOutput(Aspect aspect, int amount) {
        if (isAdvanced() && aspect != null && amount > 0) {
            aspects.add(aspect, amount);
            setChangedAndSync();
        }
    }

    public int space() {
        return Math.max(0, capacity() - aspects.totalAmount());
    }

    public boolean canAccept(AspectList incoming) {
        return incoming != null && !incoming.isEmpty() && incoming.totalAmount() <= space();
    }

    /** Legacy direct-fuel hook retained for old saves/tests; new gameplay uses the fuel slot. */
    public int addFuel(int amount) {
        if (amount <= 0) {
            return 0;
        }
        fuelTime = Math.min(32000, fuelTime + amount);
        currentFuelTime = Math.max(currentFuelTime, amount);
        setChangedAndSync();
        return amount;
    }

    /** Legacy direct-burn hook retained as a migration path. */
    public boolean startBurn(AspectList incoming) {
        if (incoming == null || incoming.isEmpty() || !pendingAspects.isEmpty() || !canAccept(incoming)) {
            return false;
        }
        pendingAspects.clear();
        pendingAspects.addAll(incoming);
        burnProgress = 0;
        burnDuration = calculateSmeltTime(incoming.totalAmount());
        setChangedAndSync();
        return true;
    }

    public boolean insertInput(ItemStack offered) {
        if (offered == null || offered.isEmpty()) {
            return false;
        }
        AspectList tags = AspectDatabase.getAspectsForItem(offered);
        if (tags == null || tags.isEmpty()) {
            return false;
        }
        if (input.isEmpty()) {
            input = offered.copy();
            input.setCount(1);
        } else if (ItemStack.isSameItemSameTags(input, offered) && input.getCount() < input.getMaxStackSize()) {
            input.grow(1);
        } else {
            return false;
        }
        setChangedAndSync();
        return true;
    }

    public boolean insertFuel(ItemStack offered) {
        if (offered == null || offered.isEmpty() || burnValue(offered) <= 0) {
            return false;
        }
        if (fuel.isEmpty()) {
            fuel = offered.copy();
            fuel.setCount(1);
        } else if (ItemStack.isSameItemSameTags(fuel, offered) && fuel.getCount() < fuel.getMaxStackSize()) {
            fuel.grow(1);
        } else {
            return false;
        }
        setChangedAndSync();
        return true;
    }

    public ItemStack extractInput() {
        ItemStack result = input;
        input = ItemStack.EMPTY;
        burnProgress = 0;
        setChangedAndSync();
        return result;
    }

    public ItemStack extractFuel() {
        ItemStack result = fuel;
        fuel = ItemStack.EMPTY;
        setChangedAndSync();
        return result;
    }

    public int addAllLimited(AspectList incoming) {
        if (incoming == null || incoming.isEmpty()) {
            return 0;
        }
        int moved = 0;
        for (java.util.Map.Entry<Aspect, Integer> entry : incoming.entries().entrySet()) {
            if (space() <= 0) {
                break;
            }
            int amount = Math.min(entry.getValue(), space());
            aspects.add(entry.getKey(), amount);
            moved += amount;
        }
        if (moved > 0) {
            setChangedAndSync();
        }
        return moved;
    }

    public Aspect firstAspect() {
        return aspects.firstAspect();
    }

    public int removeUpTo(Aspect aspect, int amount) {
        int removed = aspects.removeUpTo(aspect, amount);
        if (removed > 0) {
            setChangedAndSync();
        }
        return removed;
    }

    public int nextDistillationInterval() {
        return TC4AlchemicalFurnaceParity.distillationInterval(speedBoost);
    }

    /** Original TileAlchemyFurnace uses its own persisted tile counter for the 40/20 tick distillation phase. */
    public int distillationCounter() {
        return counter;
    }

    private static int burnValue(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        // TC4 EventHandlerWorld registers Alumentum as a 6400-tick fuel.
        if (isAlumentum(stack)) {
            return TC4AlchemicalFurnaceParity.ALUMENTUM_BURN_TIME;
        }
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
    }

    private static boolean isAlumentum(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && id.equals(new ResourceLocation(ThaumcraftMod.MOD_ID, "tc4_alumentum"));
    }

    private void refreshBellows() {
        if (level == null) {
            bellows = 0;
            return;
        }
        bellows = Math.min(MAX_BELLOWS,
                BellowsBlock.countActiveBellows(level, worldPosition, Direction.values()));
    }

    private int calculateSmeltTime(int visSize) {
        if (bellows < 0) {
            refreshBellows();
        }
        int exact = TC4AlchemicalFurnaceParity.smeltTime(visSize, bellows);
        int legacyBridge = TC4ArcaneBellowsParity.alchemicalFurnaceSmeltTime(visSize, bellows);
        return exact == legacyBridge ? exact : legacyBridge;
    }

    private AspectList inputAspects() {
        if (input.isEmpty()) {
            return new AspectList();
        }
        AspectList tags = AspectDatabase.getAspectsForItem(input);
        return tags == null ? new AspectList() : tags;
    }

    private boolean canSmeltInput() {
        AspectList tags = inputAspects();
        if (tags.isEmpty()) {
            return false;
        }
        if (tags.totalAmount() > space()) {
            return false;
        }
        burnDuration = calculateSmeltTime(tags.totalAmount());
        return true;
    }

    private void consumeFuelIfNeeded() {
        if (fuelTime > 0 || !canSmeltInput() || fuel.isEmpty()) {
            return;
        }
        int value = burnValue(fuel);
        if (value <= 0) {
            return;
        }
        speedBoost = isAlumentum(fuel);
        currentFuelTime = value;
        fuelTime = value;
        ItemStack remainder = fuel.getCraftingRemainingItem();
        fuel.shrink(1);
        if (fuel.isEmpty()) {
            fuel = remainder;
        }
    }

    private void finishItemSmelt() {
        AspectList tags = inputAspects();
        if (!canAccept(tags)) {
            return;
        }
        addAllLimited(tags);
        input.shrink(1);
        if (input.isEmpty()) {
            input = ItemStack.EMPTY;
        }
        burnProgress = 0;
    }

    private void finishLegacyPending() {
        if (pendingAspects.isEmpty() || !canAccept(pendingAspects)) {
            return;
        }
        addAllLimited(pendingAspects);
        pendingAspects.clear();
        burnProgress = 0;
    }

    /**
     * Original advanced furnace item processing. The entity collision is handled
     * by AlchemicalFurnaceBlock; this method performs the atomic energy/storage pass.
     */
    public boolean processAdvancedItem(ItemStack offered) {
        if (!isAdvanced() || offered == null || offered.isEmpty() || advancedProcessed > 0) {
            return false;
        }
        AspectList tags = AspectDatabase.getAspectsForItem(offered);
        if (tags == null || tags.isEmpty() || !canAccept(tags)) {
            return false;
        }
        int visSize = tags.totalAmount();
        if (advancedHeat < visSize * 2 || advancedEntropy < visSize || advancedWater < visSize) {
            return false;
        }
        advancedHeat -= visSize * 2;
        advancedEntropy -= visSize;
        advancedWater -= visSize;
        advancedProcessed = (int) (5.0F + Math.max(0.0F,
                (1.0F - advancedHeat / (float) ADVANCED_MAX_POWER) * 100.0F));
        addAllLimited(tags);
        setChangedAndSync();
        return true;
    }

    private int refillMachineCredit(ServerLevel level, Aspect aspect, int credit) {
        if (credit >= ADVANCED_RECHARGE_CV) {
            return credit;
        }
        return credit + AuraVisRelayNetwork.drainMachineVis(level, worldPosition, aspect, 100);
    }

    private void tickAdvanced(ServerLevel level) {
        counter++;
        boolean changed = false;
        if (advancedProcessed > 0) {
            advancedProcessed--;
            changed = true;
        }
        if (counter % 5 == 0) {
            int oldHeat = advancedHeat;
            advancedHeat = Math.max(0, advancedHeat - 1);

            if (advancedHeat < ADVANCED_MAX_POWER) {
                ignisCreditCv = refillMachineCredit(level, Aspect.IGNIS, ignisCreditCv);
                int moved = Math.min(Math.min(ADVANCED_RECHARGE_CV, ignisCreditCv), ADVANCED_MAX_POWER - advancedHeat);
                advancedHeat += moved;
                ignisCreditCv -= moved;
            }
            if (advancedEntropy < ADVANCED_MAX_POWER) {
                perditioCreditCv = refillMachineCredit(level, Aspect.PERDITIO, perditioCreditCv);
                int moved = Math.min(Math.min(ADVANCED_RECHARGE_CV, perditioCreditCv), ADVANCED_MAX_POWER - advancedEntropy);
                advancedEntropy += moved;
                perditioCreditCv -= moved;
            }
            if (advancedWater < ADVANCED_MAX_POWER) {
                aquaCreditCv = refillMachineCredit(level, Aspect.AQUA, aquaCreditCv);
                int moved = Math.min(Math.min(ADVANCED_RECHARGE_CV, aquaCreditCv), ADVANCED_MAX_POWER - advancedWater);
                advancedWater += moved;
                aquaCreditCv -= moved;
            }
            changed = changed || oldHeat != advancedHeat;
        }
        if (changed) {
            setChangedAndSync();
        }
    }

    private void tickServer() {
        boolean wasBurning = fuelTime > 0;
        counter++;
        if (fuelTime > 0) fuelTime--;
        if (bellows < 0) refreshBellows();

        // Original order: distil first, then acquire fuel, then advance cooking.
        TC4DistillationRuntime.tickFurnaceToAlembics(level, worldPosition, this);

        if (!pendingAspects.isEmpty()) {
            if (fuelTime > 0) {
                burnProgress++;
                if (burnProgress >= burnDuration) finishLegacyPending();
            }
        } else {
            consumeFuelIfNeeded();
            if (fuelTime > 0 && canSmeltInput()) {
                burnProgress++;
                if (burnProgress >= burnDuration) finishItemSmelt();
            } else {
                burnProgress = 0;
            }
        }
        updateLitState(wasBurning, fuelTime > 0);
        setChangedAndSync();
    }

    private void updateLitState(boolean wasBurning, boolean burning) {
        if (level == null || wasBurning == burning || !getBlockState().hasProperty(AlchemicalFurnaceBlock.LIT)) return;
        level.setBlock(worldPosition, getBlockState().setValue(AlchemicalFurnaceBlock.LIT, burning), Block.UPDATE_ALL);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AlchemicalFurnaceBlockEntity furnace) {
        if (furnace.isAdvanced() && level instanceof ServerLevel serverLevel) {
            furnace.tickAdvanced(serverLevel);
            return;
        }
        furnace.tickServer();
    }

    public void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }


    @Override
    public Component getDisplayName() {
        return getBlockState().is(ThaumcraftMod.ADVANCED_ALCHEMICAL_FURNACE.get())
                ? Component.translatable("block.thaumcraft.advanced_alchemical_furnace")
                : Component.translatable("container.alchemyfurnace");
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInventory, Player player) {
        return new AlchemicalFurnaceMenu(containerId, playerInventory, this, furnaceData());
    }

    public net.minecraft.world.inventory.ContainerData furnaceData() {
        return new net.minecraft.world.inventory.ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> burnProgress;
                    case 1 -> fuelTime;
                    case 2 -> Math.max(1, currentFuelTime);
                    case 3 -> aspects.totalAmount();
                    case 4 -> Math.max(1, burnDuration);
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> burnProgress = value;
                    case 1 -> fuelTime = value;
                    case 2 -> currentFuelTime = value;
                    case 4 -> burnDuration = value;
                    default -> { }
                }
            }

            @Override
            public int getCount() {
                return 5;
            }
        };
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return input.isEmpty() && fuel.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == SLOT_INPUT ? input : slot == SLOT_FUEL ? fuel : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack current = getItem(slot);
        if (current.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = current.split(amount);
        if (current.isEmpty()) {
            setSlot(slot, ItemStack.EMPTY);
        } else {
            setChangedAndSync();
        }
        if (slot == SLOT_INPUT) {
            burnProgress = 0;
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack current = getItem(slot);
        setSlot(slot, ItemStack.EMPTY);
        if (slot == SLOT_INPUT) {
            burnProgress = 0;
        }
        return current;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack normalized = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!normalized.isEmpty()) {
            normalized.setCount(Math.min(normalized.getCount(), getMaxStackSize()));
        }
        setSlot(slot, normalized);
        if (slot == SLOT_INPUT) {
            burnProgress = 0;
        }
    }

    private void setSlot(int slot, ItemStack stack) {
        if (slot == SLOT_INPUT) {
            input = stack;
        } else if (slot == SLOT_FUEL) {
            fuel = stack;
        } else {
            return;
        }
        setChangedAndSync();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        input = ItemStack.EMPTY;
        fuel = ItemStack.EMPTY;
        burnProgress = 0;
        setChangedAndSync();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.UP) {
            return NO_SLOTS;
        }
        return side == Direction.DOWN ? FUEL_SLOT : INPUT_SLOT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_INPUT) {
            AspectList tags = AspectDatabase.getAspectsForItem(stack);
            return tags != null && !tags.isEmpty();
        }
        return slot == SLOT_FUEL && burnValue(stack) > 0;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (side == Direction.UP) {
            return false;
        }
        return side == Direction.DOWN
                ? slot == SLOT_FUEL && canPlaceItem(slot, stack)
                : slot == SLOT_INPUT && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (side == Direction.UP) {
            return false;
        }
        return side == Direction.DOWN
                ? slot == SLOT_FUEL && stack.is(Items.BUCKET)
                : slot == SLOT_INPUT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putShort("BurnTime", (short) fuelTime);
        tag.putShort("Vis", (short) aspects.totalAmount());
        tag.putBoolean("speedBoost", speedBoost);
        tag.putShort("CookTime", (short) burnProgress);

        ListTag items = new ListTag();
        if (!input.isEmpty()) {
            CompoundTag entry = input.save(new CompoundTag());
            entry.putByte("Slot", (byte) SLOT_INPUT);
            items.add(entry);
        }
        if (!fuel.isEmpty()) {
            CompoundTag entry = fuel.save(new CompoundTag());
            entry.putByte("Slot", (byte) SLOT_FUEL);
            items.add(entry);
        }
        tag.put("Items", items);
        writeOriginalAspects(tag, aspects);

        // Advanced-only controller state is outside TileAlchemyFurnace parity.
        if (isAdvanced()) {
            tag.putInt("AdvancedHeat", advancedHeat);
            tag.putInt("AdvancedEntropy", advancedEntropy);
            tag.putInt("AdvancedWater", advancedWater);
            tag.putInt("AdvancedProcessed", advancedProcessed);
            tag.putInt("IgnisCreditCv", ignisCreditCv);
            tag.putInt("PerditioCreditCv", perditioCreditCv);
            tag.putInt("AquaCreditCv", aquaCreditCv);
        }
    }

    private static void writeOriginalAspects(CompoundTag tag, AspectList list) {
        ListTag values = new ListTag();
        for (java.util.Map.Entry<Aspect, Integer> entry : list.entries().entrySet()) {
            if (entry.getValue() <= 0) continue;
            CompoundTag value = new CompoundTag();
            value.putString("key", entry.getKey().id());
            value.putInt("amount", entry.getValue());
            values.add(value);
        }
        tag.put("Aspects", values);
    }

    private static void readOriginalAspects(CompoundTag tag, AspectList list) {
        if (tag.contains("Aspects", Tag.TAG_LIST)) {
            for (Tag raw : tag.getList("Aspects", Tag.TAG_COMPOUND)) {
                CompoundTag value = (CompoundTag) raw;
                Aspect aspect = Aspect.byId(value.getString("key"));
                int amount = Math.max(0, value.getInt("amount"));
                if (aspect != null && amount > 0) list.add(aspect, amount);
            }
        } else if (tag.contains("Aspects", Tag.TAG_COMPOUND)) {
            list.load(tag.getCompound("Aspects")); // migration from pre-11.64.37
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        aspects.clear();
        pendingAspects.clear();
        readOriginalAspects(tag, aspects);

        input = ItemStack.EMPTY;
        fuel = ItemStack.EMPTY;
        if (tag.contains("Items", Tag.TAG_LIST)) {
            for (Tag raw : tag.getList("Items", Tag.TAG_COMPOUND)) {
                CompoundTag entry = (CompoundTag) raw;
                ItemStack stack = ItemStack.of(entry);
                if (entry.getByte("Slot") == SLOT_INPUT) input = stack;
                if (entry.getByte("Slot") == SLOT_FUEL) fuel = stack;
            }
        } else {
            input = tag.contains("Input", Tag.TAG_COMPOUND) ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
            fuel = tag.contains("Fuel", Tag.TAG_COMPOUND) ? ItemStack.of(tag.getCompound("Fuel")) : ItemStack.EMPTY;
            if (tag.contains("PendingAspects", Tag.TAG_COMPOUND)) pendingAspects.load(tag.getCompound("PendingAspects"));
        }

        fuelTime = Math.max(0, tag.contains("BurnTime") ? tag.getShort("BurnTime") : tag.getInt("FuelTime"));
        currentFuelTime = Math.max(0, burnValue(fuel));
        burnProgress = Math.max(0, tag.contains("CookTime") ? tag.getShort("CookTime") : tag.getInt("BurnProgress"));
        burnDuration = Math.max(1, tag.contains("BurnDuration") ? tag.getInt("BurnDuration") : 100);
        bellows = -1;
        counter = 0;
        speedBoost = tag.contains("speedBoost") ? tag.getBoolean("speedBoost") : tag.getBoolean("SpeedBoost");
        advancedHeat = Math.max(0, Math.min(ADVANCED_MAX_POWER, tag.getInt("AdvancedHeat")));
        advancedEntropy = Math.max(0, Math.min(ADVANCED_MAX_POWER, tag.getInt("AdvancedEntropy")));
        advancedWater = Math.max(0, Math.min(ADVANCED_MAX_POWER, tag.getInt("AdvancedWater")));
        advancedProcessed = Math.max(0, tag.getInt("AdvancedProcessed"));
        ignisCreditCv = Math.max(0, tag.getInt("IgnisCreditCv"));
        perditioCreditCv = Math.max(0, tag.getInt("PerditioCreditCv"));
        aquaCreditCv = Math.max(0, tag.getInt("AquaCreditCv"));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && side != null) {
            return sidedHandlers[side.get3DDataValue()].cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        for (LazyOptional<IItemHandler> handler : sidedHandlers) handler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        sidedHandlers = createSidedHandlers();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }
}
