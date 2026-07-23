package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.block.ScribingToolsItem;
import com.darkifov.thaumcraft.menu.ResearchTableMenu;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.OriginalResearchBridge;
import com.darkifov.thaumcraft.research.PlayerAspectKnowledge;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchNoteState;
import com.darkifov.thaumcraft.research.ResearchTableInventoryRuntime;
import com.darkifov.thaumcraft.research.ResearchTableBonusRuntime;
import com.darkifov.thaumcraft.research.TC4ResearchTableBehaviorParity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Stage165: persistent 1.19.2 adapter for the original TC4 TileResearchTable.
 *
 * Original TC4 table inventory contract is kept exactly at the structural level:
 *   slot 0 = IScribeTools
 *   slot 1 = itemResearchNotes
 *
 * Modern Forge uses BlockEntity + Menu instead of the 1.7.10 TileEntity/Container
 * classes, but the data ownership is now table-local again rather than held-item
 * virtual slots.
 */
public class ResearchTableBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int SLOT_SCRIBING_TOOLS = ResearchTableInventoryRuntime.SLOT_SCRIBING_TOOLS;
    public static final int SLOT_RESEARCH_NOTE = ResearchTableInventoryRuntime.SLOT_RESEARCH_NOTE;
    public static final int SIZE = 2;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private final AspectList bonusAspects = new AspectList();
    private int nextRecalc = 0;

    public ResearchTableBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.RESEARCH_TABLE_BLOCK_ENTITY.get(), pos, state);
    }


    public static void serverTick(Level level, BlockPos pos, BlockState state, ResearchTableBlockEntity table) {
        if (level == null || level.isClientSide || table == null) {
            return;
        }
        int counterBeforeTick = table.nextRecalc;
        table.nextRecalc = TC4ResearchTableBehaviorParity.counterAfterTick(counterBeforeTick);
        if (TC4ResearchTableBehaviorParity.shouldRecalculate(counterBeforeTick)) {
            ResearchTableBonusRuntime.recalculateInto(level, pos, table.bonusAspects);
            // TC4 always marks and updates the tile after a recalc pass, even
            // when RNG adds no aspect. The reset counter is persistent state.
            table.setChanged();
            table.syncToClient();
        }
    }

    public AspectList bonusAspects() {
        return bonusAspects;
    }

    public int bonusAmount(Aspect aspect) {
        return bonusAspects.get(aspect);
    }

    public boolean hasBonusAspect(Aspect aspect) {
        return bonusAmount(aspect) > 0;
    }

    public boolean consumeBonusAspect(Aspect aspect) {
        return consumeBonusAspect(aspect, 1);
    }

    public boolean consumeBonusAspect(Aspect aspect, int amount) {
        if (aspect == null || amount < 0) {
            return false;
        }
        if (amount == 0) {
            return true;
        }
        boolean consumed = bonusAspects.remove(aspect, amount);
        if (consumed) {
            setChanged();
            syncToClient();
        }
        return consumed;
    }

    public void setBonusAmountForTransaction(Aspect aspect, int amount) {
        if (aspect == null) {
            return;
        }
        int current = bonusAspects.get(aspect);
        if (current > 0) {
            bonusAspects.remove(aspect, current);
        }
        if (amount > 0) {
            bonusAspects.add(aspect, amount);
        }
        setChanged();
        syncToClient();
    }

    public void recalculateBonusNow() {
        if (level != null && !level.isClientSide && ResearchTableBonusRuntime.recalculateInto(level, worldPosition, bonusAspects)) {
            setChanged();
            syncToClient();
        }
    }

    public void syncToClient() {
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Research Table");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ResearchTableMenu(containerId, playerInventory, this);
    }

    public boolean hasInkedScribingTools() {
        ItemStack tools = getItem(SLOT_SCRIBING_TOOLS);
        return tools.getItem() instanceof ScribingToolsItem && ScribingToolsItem.hasInk(tools);
    }

    public boolean consumeInk(int amount, Player player) {
        ItemStack tools = getItem(SLOT_SCRIBING_TOOLS);
        boolean consumed = ScribingToolsItem.consumeInk(tools, amount);
        if (consumed) {
            setChanged();
        }
        return consumed;
    }

    public ItemStack researchNote() {
        ItemStack stack = getItem(SLOT_RESEARCH_NOTE);
        return stack.getItem() instanceof ResearchNoteItem ? stack : ItemStack.EMPTY;
    }

    /**
     * Compatibility adapter only. TC4 creates a research note from the selected
     * Thaumonomicon entry, never from a Research Table button.
     */
    public boolean createResearchNote(ServerPlayer player) {
        return false;
    }

    /** TC4 renders and edits the note directly inside GuiResearchTable. */
    public void syncResearchNote(ServerPlayer player) {
        ThaumcraftNetwork.syncAspectKnowledge(player);
        ThaumcraftNetwork.syncResearchNote(player, researchNote());
    }

    /** Compatibility adapter for old clients: synchronize, but open no second GUI. */
    public boolean openResearchNote(ServerPlayer player) {
        if (researchNote().isEmpty()) {
            return false;
        }
        syncResearchNote(player);
        return true;
    }

    /**
     * Compatibility adapter only. A solved discovery is learned by taking it
     * from the table and using the note item, exactly as in ItemResearchNotes.
     */
    public boolean completeResearchNote(ServerPlayer player) {
        return false;
    }

    public boolean copyCompletedResearchNote(ServerPlayer player) {
        ItemStack note = researchNote();
        if (note.isEmpty()) {
            return false;
        }
        ResearchNoteState.initialize(note, ResearchNoteState.target(note));
        if (!ResearchNoteState.solved(note)) {
            return false;
        }
        if (!com.darkifov.thaumcraft.data.PlayerThaumData.hasResearch(player, "RESEARCHDUPE")) {
            return false;
        }
        Optional<ResearchEntry> target = OriginalResearchBridge.byKey(ResearchNoteState.target(note));
        if (target.isEmpty()) {
            return false;
        }

        int previousCopies = ResearchNoteState.copyCount(note);
        if (!hasCopyAspectCost(player, target.get(), previousCopies)) {
            return false;
        }
        if (!hasInventoryItem(player, Items.PAPER)) {
            return false;
        }
        if (!hasInventoryItem(player, Items.INK_SAC)) {
            return false;
        }
        // Original duplicate consumes paper, ink sac and exact tags+copies even in creative.
        consumeInventoryItem(player, Items.PAPER);
        consumeInventoryItem(player, Items.INK_SAC);
        consumeCopyAspectCost(player, target.get(), previousCopies);

        int nextCopies = ResearchNoteState.incrementCopyCount(note);
        note.grow(1);
        setItem(SLOT_RESEARCH_NOTE, note);
        setChanged();
        playOriginalResearchSound("learn", 1.0F, 1.0F);
        return true;
    }

    private boolean hasCopyAspectCost(Player player, ResearchEntry entry, int copies) {
        for (java.util.Map.Entry<String, Integer> aspectEntry : entry.aspects().entrySet()) {
            Aspect aspect = Aspect.byId(aspectEntry.getKey());
            int cost = Math.max(0, aspectEntry.getValue()) + Math.max(0, copies);
            if (aspect != null && cost > 0 && !PlayerAspectKnowledge.pool(player).contains(aspect, cost)) {
                return false;
            }
        }
        return true;
    }

    private void consumeCopyAspectCost(Player player, ResearchEntry entry, int copies) {
        for (java.util.Map.Entry<String, Integer> aspectEntry : entry.aspects().entrySet()) {
            Aspect aspect = Aspect.byId(aspectEntry.getKey());
            int cost = Math.max(0, aspectEntry.getValue()) + Math.max(0, copies);
            if (aspect != null && cost > 0) {
                PlayerAspectKnowledge.consumePool(player, aspect, cost);
            }
        }
    }

    private static int findInventoryItemSlot(Player player, net.minecraft.world.item.Item item) {
        if (player == null || item == null) {
            return -1;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(item)) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasInventoryItem(Player player, net.minecraft.world.item.Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                return true;
            }
        }
        return false;
    }

    private void playOriginalResearchSound(String key, float volume, float pitch) {
        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition, TC4Sounds.event(key), SoundSource.BLOCKS, volume, pitch);
        }
    }

    private boolean consumeInventoryItem(Player player, net.minecraft.world.item.Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private boolean consumePaper(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.PAPER)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < SIZE ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SIZE) {
            return;
        }
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_SCRIBING_TOOLS) {
            return ResearchTableInventoryRuntime.isScribingTools(stack);
        }
        if (slot == SLOT_RESEARCH_NOTE) {
            return ResearchTableInventoryRuntime.isResearchNote(stack);
        }
        return false;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt(TC4ResearchTableBehaviorParity.NEXT_RECALC_TAG, nextRecalc);
        tag.put(TC4ResearchTableBehaviorParity.BONUS_ASPECTS_TAG, saveBonusAspects());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(tag, items);
        nextRecalc = tag.getInt(TC4ResearchTableBehaviorParity.NEXT_RECALC_TAG);
        loadBonusAspects(tag.getList(TC4ResearchTableBehaviorParity.BONUS_ASPECTS_TAG, 10));
    }

    private ListTag saveBonusAspects() {
        ListTag list = new ListTag();
        for (java.util.Map.Entry<Aspect, Integer> entry : bonusAspects.entries().entrySet()) {
            if (TC4ResearchTableBehaviorParity.serializedCopiesForAmount(entry.getValue()) > 0) {
                CompoundTag tag = new CompoundTag();
                tag.putString(TC4ResearchTableBehaviorParity.BONUS_ASPECT_TAG, entry.getKey().id());
                list.add(tag);
            }
        }
        return list;
    }

    private void loadBonusAspects(ListTag list) {
        bonusAspects.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            Aspect aspect = Aspect.byId(tag.getString(TC4ResearchTableBehaviorParity.BONUS_ASPECT_TAG));
            if (aspect != null
                    && TC4ResearchTableBehaviorParity.shouldLoadSerializedType(bonusAspects.get(aspect) > 0)) {
                bonusAspects.add(aspect, 1);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
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
