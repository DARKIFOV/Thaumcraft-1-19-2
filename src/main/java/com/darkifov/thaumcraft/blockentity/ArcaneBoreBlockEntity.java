package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraVisRelayNetwork;
import com.darkifov.thaumcraft.block.ArcaneBoreBaseBlock;
import com.darkifov.thaumcraft.block.ArcaneBoreBlock;
import com.darkifov.thaumcraft.block.TC4ArcaneLampParity;
import com.darkifov.thaumcraft.block.WandFocusItem;
import com.darkifov.thaumcraft.enchantment.TC4EnchantmentEvents;
import com.darkifov.thaumcraft.item.ElementalPickaxeItem;
import com.darkifov.thaumcraft.menu.ArcaneBoreMenu;
import com.darkifov.thaumcraft.wand.FocusUpgradeRuntime;
import com.darkifov.thaumcraft.wand.FocusUpgradeType;
import com.darkifov.thaumcraft.wand.WandFocusRuntime;
import com.darkifov.thaumcraft.wand.WandFocusType;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Server-authoritative full closure of TC4 TileArcaneBore. */
public final class ArcaneBoreBlockEntity extends BlockEntity implements MenuProvider {
    private static final String CLIENT_TARGET = "TC4ClientDigTarget";
    private static final String CLIENT_WORK = "TC4ClientWorkTicks";
    private static final GameProfile BORE_PROFILE = new GameProfile(
            UUID.nameUUIDFromBytes("FakeThaumcraftBore".getBytes(StandardCharsets.UTF_8)), "FakeThaumcraftBore");

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == 0 ? isExcavationFocus(stack) : stack.getItem() instanceof PickaxeItem;
        }

        @Override
        protected void onContentsChanged(int slot) {
            resetTarget();
            markAndSync();
        }
    };
    private LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> inventory);

    private BlockPos target;
    private int workTicks;
    private int spiral;
    private float currentRadius;
    private float radiusIncrement;
    private int lastLaneX;
    private int lastLaneY;
    private int lastLaneZ;
    private float speedyTime;
    private long repairCounter;
    private final EnumMap<Aspect, Integer> repairCost = new EnumMap<>(Aspect.class);
    private final EnumMap<Aspect, Integer> currentRepairVis = new EnumMap<>(Aspect.class);
    private FakePlayer fakePlayer;
    private Direction pendingOrientation;
    private Direction pendingBaseOrientation;

    private float clientTopRotation;

    private final ContainerData menuData = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> area();
                case 1 -> speed();
                case 2 -> fortune();
                case 3 -> silkTouch() ? 1 : 0;
                case 4 -> (int) speedyTime;
                case 5 -> workTicks;
                case 6 -> nativeClusters() ? 1 : 0;
                case 7 -> pickaxeNearBroken() ? 1 : 0;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            if (index == 4) speedyTime = Math.max(0, value);
            if (index == 5) workTicks = Math.max(0, value);
        }
        @Override public int getCount() { return 8; }
    };

    public ArcaneBoreBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ARCANE_BORE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneBoreBlockEntity bore) {
        if (!(level instanceof ServerLevel server)) return;
        BlockPos basePos = ArcaneBoreBlock.basePos(pos, state);
        ArcaneBoreBaseBlockEntity base = level.getBlockEntity(basePos) instanceof ArcaneBoreBaseBlockEntity found ? found : null;

        // TC4 refilled Perditio acceleration before every other server-side condition.
        bore.refillAcceleration(server, base);
        boolean validBase = base != null && level.getBlockState(basePos).is(ThaumcraftMod.ARCANE_BORE_BASE.get());
        boolean powered = level.hasNeighborSignal(pos) || level.hasNeighborSignal(basePos);
        if (validBase && powered && bore.hasValidTools()) {
            Direction facing = state.getValue(ArcaneBoreBlock.FACING);
            if (bore.target == null || !bore.canMineAt(bore.target)) {
                bore.target = bore.findNextTarget(facing);
                if (bore.target != null) {
                    float hardness = level.getBlockState(bore.target).getDestroySpeed(level, bore.target);
                    bore.workTicks = Math.max(1, TC4ArcaneBoreParity.miningDelay(
                            hardness, bore.speed(), bore.speedyTime >= 1.0F));
                    bore.markAndSync();
                }
            } else if (--bore.workTicks <= 0) {
                bore.mineTarget(basePos);
            }
        } else {
            bore.resetTarget();
        }

        // Original repair/special-pickaxe tick runs after the dig path and does not require redstone power.
        bore.tickPickaxe(server);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ArcaneBoreBlockEntity bore) {
        boolean active = bore.target != null && bore.hasValidTools()
                && (level.hasNeighborSignal(pos) || level.hasNeighborSignal(ArcaneBoreBlock.basePos(pos, state)));
        if (active) {
            double length = Math.sqrt(bore.target.distSqr(pos)) + 1.0D;
            bore.clientTopRotation = (bore.clientTopRotation + (float) (length / 6.0D)) % 360.0F;
            if ((level.getGameTime() & 3L) == 0L) {
                BlockState targetState = level.getBlockState(bore.target);
                if (!targetState.isAir()) {
                    level.addParticle(new net.minecraft.core.particles.BlockParticleOption(
                                    net.minecraft.core.particles.ParticleTypes.BLOCK, targetState),
                            bore.target.getX() + .5D, bore.target.getY() + .5D, bore.target.getZ() + .5D,
                            (level.random.nextDouble() - .5D) * .08D,
                            (level.random.nextDouble() - .5D) * .08D,
                            (level.random.nextDouble() - .5D) * .08D);
                }
            }
        } else {
            float mod = bore.clientTopRotation % 90.0F;
            if (mod != 0.0F) bore.clientTopRotation += Math.min(10.0F, 90.0F - mod);
        }
    }

    private void refillAcceleration(ServerLevel server, @Nullable ArcaneBoreBaseBlockEntity base) {
        if (speedyTime >= TC4ArcaneBoreParity.ESSENTIA_SPEED_CREDIT) return;
        int drained = AuraVisRelayNetwork.drainMachineVis(server, worldPosition, Aspect.PERDITIO,
                TC4ArcaneBoreParity.VIS_REQUEST_CENTIVIS);
        if (drained > 0) {
            speedyTime = TC4ArcaneBoreParity.addVisCredit(speedyTime, drained);
            markAndSync();
        }
        if (speedyTime < TC4ArcaneBoreParity.ESSENTIA_SPEED_CREDIT && base != null && base.tryDrawPerditio()) {
            speedyTime = TC4ArcaneBoreParity.addEssentiaCredit(speedyTime);
            markAndSync();
        }
    }

    private void tickPickaxe(ServerLevel server) {
        ItemStack pickaxe = inventory.getStackInSlot(1);
        if (!(pickaxe.getItem() instanceof PickaxeItem)) return;
        FakePlayer fake = fakePlayer(server);
        long before = repairCounter++;
        if (before % TC4ArcaneBoreParity.PICKAXE_REPAIR_INTERVAL == 0 && pickaxe.isDamaged()) {
            tryRepairPickaxe(server, pickaxe);
        }
        if (!repairCost.isEmpty() && repairCounter % TC4ArcaneBoreParity.REPAIR_VIS_DRAIN_INTERVAL == 0) {
            for (Map.Entry<Aspect, Integer> entry : repairCost.entrySet()) {
                int current = currentRepairVis.getOrDefault(entry.getKey(), 0);
                if (current < entry.getValue()) {
                    int drained = AuraVisRelayNetwork.drainMachineVis(server, worldPosition, entry.getKey(), entry.getValue());
                    if (drained > 0) currentRepairVis.merge(entry.getKey(), drained, Integer::sum);
                }
            }
        }
        fake.tickCount = (int) repairCounter;
        try {
            pickaxe.inventoryTick(server, fake, 0, true);
        } catch (RuntimeException ignored) {
            // Original TC4 swallowed special-pickaxe update exceptions in the fake-player path.
        }
    }

    private void tryRepairPickaxe(ServerLevel server, ItemStack pickaxe) {
        int level = Math.min(2, EnchantmentHelper.getItemEnchantmentLevel(ThaumcraftMod.REPAIR_ENCHANTMENT.get(), pickaxe));
        if (level <= 0 || !ThaumcraftMod.REPAIR_ENCHANTMENT.get().canEnchant(pickaxe)) return;
        repairCost.clear();
        repairCost.putAll(TC4EnchantmentEvents.repairCost(pickaxe, level));
        if (repairCost.isEmpty()) return;
        for (Map.Entry<Aspect, Integer> entry : repairCost.entrySet()) {
            if (currentRepairVis.getOrDefault(entry.getKey(), 0) < entry.getValue()) return;
        }
        for (Map.Entry<Aspect, Integer> entry : repairCost.entrySet()) {
            currentRepairVis.merge(entry.getKey(), -entry.getValue(), Integer::sum);
        }
        pickaxe.setDamageValue(Math.max(0, pickaxe.getDamageValue() - level));
        markAndSync();
    }

    private FakePlayer fakePlayer(ServerLevel server) {
        if (fakePlayer == null || fakePlayer.getLevel() != server) fakePlayer = FakePlayerFactory.get(server, BORE_PROFILE);
        return fakePlayer;
    }

    private boolean hasValidTools() {
        ItemStack pick = inventory.getStackInSlot(1);
        return isExcavationFocus(inventory.getStackInSlot(0))
                && pick.getItem() instanceof PickaxeItem
                && pick.isDamageableItem()
                && !TC4ArcaneBoreParity.pickaxeIsNearBroken(pick.getDamageValue(), pick.getMaxDamage());
    }

    private boolean pickaxeNearBroken() {
        ItemStack pick = inventory.getStackInSlot(1);
        return pick.isDamageableItem() && TC4ArcaneBoreParity.pickaxeIsNearBroken(pick.getDamageValue(), pick.getMaxDamage());
    }

    private static boolean isExcavationFocus(ItemStack stack) {
        return stack.getItem() instanceof WandFocusItem focus && focus.focusType() == WandFocusType.EXCAVATION;
    }

    private int area() {
        return FocusUpgradeRuntime.getUpgradeLevel(inventory.getStackInSlot(0), FocusUpgradeType.ENLARGE);
    }

    private int speed() {
        return FocusUpgradeRuntime.getUpgradeLevel(inventory.getStackInSlot(0), FocusUpgradeType.POTENCY)
                + EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, inventory.getStackInSlot(1));
    }

    private int fortune() {
        return Math.max(FocusUpgradeRuntime.getUpgradeLevel(inventory.getStackInSlot(0), FocusUpgradeType.TREASURE),
                EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, inventory.getStackInSlot(1)));
    }

    private boolean silkTouch() {
        return FocusUpgradeRuntime.isUpgradedWith(inventory.getStackInSlot(0), FocusUpgradeType.SILK_TOUCH)
                || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, inventory.getStackInSlot(1)) > 0;
    }

    private boolean nativeClusters() {
        return inventory.getStackInSlot(1).getItem() instanceof ElementalPickaxeItem
                || FocusUpgradeRuntime.isUpgradedWith(inventory.getStackInSlot(0), FocusUpgradeType.DOWSING);
    }

    @Nullable
    private BlockPos findNextTarget(Direction facing) {
        if (level == null) return null;
        TC4ArcaneBoreParity.SpiralLane lane = TC4ArcaneBoreParity.nextLane(
                worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                facing.getStepX(), facing.getStepY(), facing.getStepZ(), area(), spiral,
                currentRadius, radiusIncrement, lastLaneX, lastLaneY, lastLaneZ);
        spiral = lane.spiral();
        currentRadius = lane.currentRadius();
        radiusIncrement = lane.radiusIncrement();
        lastLaneX = lane.laneX();
        lastLaneY = lane.laneY();
        lastLaneZ = lane.laneZ();

        BlockPos cursor = new BlockPos(lastLaneX, lastLaneY, lastLaneZ).relative(facing);
        for (int depth = 0; depth < TC4ArcaneBoreParity.MAX_DEPTH; depth++) {
            cursor = cursor.relative(facing);
            if (!level.isLoaded(cursor)) break;
            BlockState candidate = level.getBlockState(cursor);
            if (candidate.getDestroySpeed(level, cursor) < 0.0F) break;
            if (canMineAt(cursor)) return cursor.immutable();
        }
        return null;
    }

    private boolean canMineAt(BlockPos pos) {
        if (level == null || !level.isLoaded(pos)) return false;
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && !state.getMaterial().isLiquid()
                && state.getDestroySpeed(level, pos) >= 0.0F
                && !state.getCollisionShape(level, pos).isEmpty();
    }

    private void mineTarget(BlockPos basePos) {
        if (!(level instanceof ServerLevel server) || target == null || !canMineAt(target)) {
            resetTarget();
            return;
        }
        BlockState mined = server.getBlockState(target);
        BlockEntity minedEntity = server.getBlockEntity(target);
        ItemStack tool = inventory.getStackInSlot(1);
        ItemStack effectiveTool = tool.copy();
        int fortune = fortune();
        boolean silk = silkTouch();
        Map<Enchantment, Integer> enchantments = new java.util.HashMap<>(EnchantmentHelper.getEnchantments(effectiveTool));
        if (silk) {
            enchantments.remove(Enchantments.BLOCK_FORTUNE);
            enchantments.put(Enchantments.SILK_TOUCH, 1);
        } else if (fortune > enchantments.getOrDefault(Enchantments.BLOCK_FORTUNE, 0)) {
            enchantments.put(Enchantments.BLOCK_FORTUNE, fortune);
        }
        EnchantmentHelper.setEnchantments(enchantments, effectiveTool);

        List<ItemStack> drops = new ArrayList<>(Block.getDrops(mined, server, target, minedEntity, fakePlayer(server), effectiveTool));
        List<ItemEntity> nearby = server.getEntitiesOfClass(ItemEntity.class, new AABB(target).inflate(1.0D));
        for (ItemEntity entity : nearby) {
            if (!entity.isRemoved() && !entity.getItem().isEmpty()) {
                drops.add(entity.getItem().copy());
                entity.discard();
            }
        }

        boolean dowsing = !silk && nativeClusters();
        for (ItemStack drop : drops) {
            ItemStack resolved = dowsing ? WandFocusRuntime.applyDowsing(drop, fortune, server.getRandom()) : drop.copy();
            outputDrop(basePos, resolved);
        }

        server.removeBlock(target, false);
        server.levelEvent(2001, target, Block.getId(mined));
        seedTunnelLightFromAdjacentLamp(server, worldPosition, basePos,
                getBlockState().getValue(ArcaneBoreBlock.FACING), server.getRandom());

        if (tool.isDamageableItem() && tool.hurt(1, server.getRandom(), fakePlayer(server))) {
            inventory.setStackInSlot(1, ItemStack.EMPTY);
        }
        speedyTime = TC4ArcaneBoreParity.consumeAcceleratedBlock(speedyTime);
        target = null;
        workTicks = 0;
        markAndSync();
    }

    /** Original TileArcaneBore integration: an adjacent Arcane Lamp seeds tunnel markers after mining. */
    public static boolean seedTunnelLightFromAdjacentLamp(ServerLevel level, BlockPos headPos, BlockPos basePos,
                                                           Direction facing, net.minecraft.util.RandomSource random) {
        return trySeedTunnelLight(level, headPos, basePos, facing,
                TC4ArcaneLampParity.boreDistance(random.nextInt(TC4ArcaneLampParity.BORE_DISTANCE_BOUND)));
    }

    /** Deterministic production entry used by GameTest while retaining the exact runtime path above. */
    public static boolean trySeedTunnelLight(ServerLevel level, BlockPos headPos, BlockPos basePos,
                                              Direction facing, int distance) {
        boolean lampFound = false;
        for (Direction direction : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
            if (level.getBlockEntity(basePos.relative(direction)) instanceof ArcaneLampBlockEntity) {
                lampFound = true;
                break;
            }
        }
        if (!lampFound) return false;
        BlockPos target = headPos.relative(facing, 1 + distance);
        int lateral = TC4ArcaneLampParity.boreLateralOffset(distance);
        if (facing.getAxis() == Direction.Axis.X) target = target.offset(0, 0, lateral);
        else target = target.offset(lateral, 0, 0);
        target = target.offset(0, TC4ArcaneLampParity.boreVerticalOffset(distance,
                facing.getAxis() == Direction.Axis.Y), 0);
        boolean inBuild = TC4ArcaneLampParity.insideBuildHeight(target.getY(),
                level.getMinBuildHeight(), level.getMaxBuildHeight());
        BlockState state = inBuild ? level.getBlockState(target) : null;
        if (!TC4ArcaneLampParity.shouldPlaceBoreLight(state != null && state.isAir(),
                state != null && state.is(ThaumcraftMod.ARCANE_LAMP_LIGHT.get()),
                state == null ? TC4ArcaneLampParity.BORE_DARKNESS_THRESHOLD
                        : level.getMaxLocalRawBrightness(target), inBuild)) return false;
        return level.setBlock(target, ThaumcraftMod.ARCANE_LAMP_LIGHT.get().defaultBlockState(), 3);
    }

    private void outputDrop(BlockPos basePos, ItemStack stack) {
        if (level == null || stack.isEmpty()) return;
        Direction output = level.getBlockState(basePos).getValue(ArcaneBoreBaseBlock.FACING);
        BlockPos destination = basePos.relative(output);
        ItemStack remainder = stack;
        if (!destination.equals(basePos) && level.getBlockEntity(destination) != null) {
            remainder = level.getBlockEntity(destination).getCapability(ForgeCapabilities.ITEM_HANDLER, output.getOpposite())
                    .map(handler -> ItemHandlerHelper.insertItemStacked(handler, stack, false)).orElse(stack);
        }
        if (!remainder.isEmpty()) {
            ItemEntity entity = new ItemEntity(level,
                    basePos.getX() + .5D + output.getStepX() * .66D,
                    basePos.getY() + .5D + output.getStepY() * .66D,
                    basePos.getZ() + .5D + output.getStepZ() * .66D,
                    remainder.copy());
            entity.setDeltaMovement(output.getStepX() * .075D, output.getStepY() * .075D + .025D,
                    output.getStepZ() * .075D);
            level.addFreshEntity(entity);
        }
    }

    public ItemStackHandler inventory() { return inventory; }
    public ContainerData menuData() { return menuData; }
    public float clientTopRotation() { return clientTopRotation; }
    public boolean hasFocusForRender() { return isExcavationFocus(inventory.getStackInSlot(0)); }
    @Nullable public BlockPos clientTarget() { return target; }

    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this;
    }

    public void onOrientationChanged() {
        spiral = 0;
        currentRadius = 0.0F;
        radiusIncrement = 0.0F;
        lastLaneX = 0;
        lastLaneY = 0;
        lastLaneZ = 0;
        resetTarget();
        markAndSync();
    }

    private void resetTarget() {
        target = null;
        workTicks = 0;
    }

    public void markAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override public Component getDisplayName() { return Component.translatable("container.thaumcraft.arcane_bore"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ArcaneBoreMenu(id, inv, this, menuData);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TC4ArcaneBoreParity.NBT_ORIENTATION,
                getBlockState().getValue(ArcaneBoreBlock.FACING).get3DDataValue());
        tag.putInt(TC4ArcaneBoreParity.NBT_BASE_ORIENTATION,
                ArcaneBoreBlock.baseOrientation(getBlockState()).get3DDataValue());
        ListTag items = new ListTag();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            CompoundTag item = new CompoundTag();
            item.putByte(TC4ArcaneBoreParity.NBT_SLOT, (byte) slot);
            stack.save(item);
            items.add(item);
        }
        tag.put(TC4ArcaneBoreParity.NBT_INVENTORY, items);
        tag.putShort(TC4ArcaneBoreParity.NBT_SPEEDY_TIME, (short) (int) speedyTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        pendingOrientation = tag.contains(TC4ArcaneBoreParity.NBT_ORIENTATION, Tag.TAG_INT)
                ? Direction.from3DDataValue(tag.getInt(TC4ArcaneBoreParity.NBT_ORIENTATION)) : null;
        pendingBaseOrientation = tag.contains(TC4ArcaneBoreParity.NBT_BASE_ORIENTATION, Tag.TAG_INT)
                ? Direction.from3DDataValue(tag.getInt(TC4ArcaneBoreParity.NBT_BASE_ORIENTATION)) : null;

        if (tag.contains(TC4ArcaneBoreParity.NBT_INVENTORY, Tag.TAG_LIST)) {
            for (int slot = 0; slot < inventory.getSlots(); slot++) inventory.setStackInSlot(slot, ItemStack.EMPTY);
            ListTag items = tag.getList(TC4ArcaneBoreParity.NBT_INVENTORY, Tag.TAG_COMPOUND);
            for (int i = 0; i < items.size(); i++) {
                CompoundTag item = items.getCompound(i);
                int slot = item.getByte(TC4ArcaneBoreParity.NBT_SLOT) & 255;
                if (slot < inventory.getSlots()) inventory.setStackInSlot(slot, ItemStack.of(item));
            }
        } else if (tag.contains(TC4ArcaneBoreParity.NBT_INVENTORY, Tag.TAG_COMPOUND)) {
            // Migration from the pre-v11.64.24 Forge ItemStackHandler compound.
            inventory.deserializeNBT(tag.getCompound(TC4ArcaneBoreParity.NBT_INVENTORY));
        }
        speedyTime = Math.max(0.0F, tag.getShort(TC4ArcaneBoreParity.NBT_SPEEDY_TIME));
        if (tag.contains(CLIENT_TARGET, Tag.TAG_LONG)) target = BlockPos.of(tag.getLong(CLIENT_TARGET));
        else target = null;
        workTicks = tag.getInt(CLIENT_WORK);
        // SpiralIndex from older ports is intentionally not written again: TC4 reset its spiral on reload.
        if (tag.contains(TC4ArcaneBoreParity.LEGACY_PORT_NBT_SPIRAL, Tag.TAG_INT)) spiral = 0;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level == null || pendingOrientation == null && pendingBaseOrientation == null) return;
        BlockState state = getBlockState();
        if (pendingOrientation != null) state = state.setValue(ArcaneBoreBlock.FACING, pendingOrientation);
        if (pendingBaseOrientation != null) state = state.setValue(ArcaneBoreBlock.INVERTED, pendingBaseOrientation == Direction.DOWN);
        if (!state.equals(getBlockState())) level.setBlock(worldPosition, state, 2);
        pendingOrientation = null;
        pendingBaseOrientation = null;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = saveWithoutMetadata();
        if (target != null) tag.putLong(CLIENT_TARGET, target.asLong());
        tag.putInt(CLIENT_WORK, workTicks);
        return tag;
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }

    @Nonnull @Override public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCapability.cast();
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
    }

    @Override public void reviveCaps() {
        super.reviveCaps();
        itemCapability = LazyOptional.of(() -> inventory);
    }
}
