package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.menu.DeconstructionTableMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Direct adapter for TC4 TileDeconstructionTable. */
public class DeconstructionTableBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int BREAK_TICKS = 40;
    private static final int[] SLOT = new int[]{0};
    private static final int[] NO_SLOTS = new int[0];

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private int breakTime;
    private Aspect outputAspect;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> breakTime;
                case 1 -> outputAspect == null ? 0 : outputAspect.ordinal() + 1;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            if (index == 0) breakTime = Math.max(0, value);
            if (index == 1) outputAspect = value <= 0 || value > Aspect.values().length ? null : Aspect.values()[value - 1];
        }
        @Override public int getCount() { return 2; }
    };

    public DeconstructionTableBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.DECONSTRUCTION_TABLE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DeconstructionTableBlockEntity table) {
        boolean changed = false;
        if (table.canBreak()) {
            if (table.breakTime <= 0) {
                table.breakTime = BREAK_TICKS;
                changed = true;
            } else if (--table.breakTime <= 0) {
                table.breakTime = 0;
                table.breakItem();
                changed = true;
            }
        } else if (table.breakTime != 0) {
            table.breakTime = 0;
            changed = true;
        }
        if (changed) table.setChangedAndSync();
    }

    private boolean canBreak() {
        ItemStack stack = items.get(0);
        return outputAspect == null && !stack.isEmpty() && hasAspects(stack);
    }

    private static boolean hasAspects(ItemStack stack) {
        AspectList list = AspectDatabase.getAspectsForItem(stack);
        return list != null && !list.isEmpty();
    }

    private void breakItem() {
        ItemStack input = items.get(0);
        if (input.isEmpty()) return;
        AspectList source = AspectDatabase.getAspectsForItem(input);
        AspectList primals = new AspectList();
        if (source != null) {
            for (Map.Entry<Aspect, Integer> entry : source.entries().entrySet()) {
                reduce(entry.getKey(), entry.getValue(), primals);
            }
        }
        if (level != null && !primals.isEmpty() && level.random.nextInt(80) < primals.totalAmount()) {
            List<Aspect> choices = new ArrayList<>(primals.entries().keySet());
            outputAspect = choices.get(level.random.nextInt(choices.size()));
        }
        input.shrink(1);
        if (input.isEmpty()) items.set(0, ItemStack.EMPTY);
        if (level != null) {
            level.playSound(null, worldPosition, TC4Sounds.event("craftfail"), SoundSource.BLOCKS, 0.25F, 1.2F);
        }
    }

    private static void reduce(Aspect aspect, int amount, AspectList target) {
        if (aspect == null || amount <= 0) return;
        if (aspect.isPrimal()) {
            target.add(aspect, amount);
            return;
        }
        reduce(aspect.firstComponent(), amount, target);
        reduce(aspect.secondComponent(), amount, target);
    }

    public boolean claimAspect(Player player) {
        if (outputAspect == null || player == null) return false;
        Aspect claimed = outputAspect;
        outputAspect = null;
        PlayerAspectKnowledge.addPool(player, claimed, 1);
        PlayerAspectKnowledge.discover(player, claimed);
        if (player instanceof ServerPlayer serverPlayer) {
            ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
        }
        if (level != null) {
            level.playSound(null, worldPosition, TC4Sounds.event("hhoff"), SoundSource.BLOCKS, 0.2F, 1.0F + level.random.nextFloat() * 0.1F);
        }
        setChangedAndSync();
        return true;
    }

    public ContainerData data() { return data; }
    public Aspect outputAspect() { return outputAspect; }
    public int breakTime() { return breakTime; }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override public Component getDisplayName() { return Component.translatable("container.thaumcraft.deconstruction_table"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new DeconstructionTableMenu(id, inventory, this, data); }
    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return items.get(0).isEmpty(); }
    @Override public ItemStack getItem(int slot) { return items.get(0); }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack out = ContainerHelper.removeItem(items, 0, amount); if (!out.isEmpty()) setChangedAndSync(); return out; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, 0); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(0, stack); if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize()); breakTime = 0; setChangedAndSync(); }
    @Override public boolean stillValid(Player player) { return level != null && level.getBlockEntity(worldPosition) == this && player.distanceToSqr(worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5) <= 64.0; }
    @Override public void clearContent() { items.clear(); breakTime = 0; setChangedAndSync(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return hasAspects(stack); }
    @Override public int[] getSlotsForFace(Direction side) { return side == Direction.UP ? NO_SLOTS : SLOT; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) { return side != Direction.UP && canPlaceItem(slot, stack); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return true; }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("BreakTime", breakTime);
        if (outputAspect != null) tag.putString("Aspect", outputAspect.id());
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        breakTime = Math.max(0, tag.getInt("BreakTime"));
        outputAspect = Aspect.byId(tag.getString("Aspect"));
    }

    @Override public CompoundTag getUpdateTag() { CompoundTag tag = super.getUpdateTag(); saveAdditional(tag); return tag; }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) { load(packet.getTag()); }
}
